package ru.sofitlabs.lifecycleawaretask;

import android.app.Activity;
import android.os.Handler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by semyon on 10.03.15.
 */
public abstract class LifecycleAwareTask<T> {

    protected static ThreadPoolExecutor defaultExecutor = new ThreadPoolExecutor(10, 10,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>());

    private static final Map<StarterTag, LifecycleAwareTask> tasks = new HashMap<>();

    public static <S> void onStarterCreate(final Starter<S> starter) {
        StarterTag<S> tag = starter.getTag();
        LifecycleAwareTask task = tasks.get(tag);
        if (task != null) {
            task.getCallback().setStarter(starter);
            task.doAfterInit();
        }
    }

    private Handler handler = null;
    private Starter starter = null;

    private T data;

    private TaskCallback<?, T> callback;

    public LifecycleAwareTask(final TaskCallback<?, T> callback) {
        this(new Handler(), callback);
    }

    public LifecycleAwareTask(final Handler handler, final TaskCallback<?, T> callback) {
        this.handler = handler;
        this.callback = callback;
    }

    public abstract T doInBackground();

    public <A extends Starter> void  start(final A starter) {
        tasks.put(starter.getTag(), this);
        defaultExecutor.execute(new _Runnable() {

            @Override
            public void runBefore() {
                data = doInBackground();
                handler.post(this);
            }

            @Override
            public void runAfter() {
                callback.execute(data);
            }

        });
    }

    private void doAfterInit() {
        if (data != null) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    callback.execute(data);
                }
            }, 1);
        }
    }

    private abstract class  _Runnable implements Runnable {

        private int a = 0;

        @Override
        public final void run() {
            switch (a) {
                case 0:
                    runBefore();
                    a = 1;
                    break;
                case 1:
                    runAfter();
                    break;
            }
        }

        public abstract void runBefore();

        public abstract void runAfter();

    }

    public static abstract class TaskCallback<StarterType extends Starter<StarterType>, DataType> {

        protected StarterType starter;

        protected TaskCallback(final StarterType starter) {
            this.starter = starter;
        }

        public void setStarter(final StarterType starter) {
            this.starter = starter;
        }

        final void execute(final DataType data) {
            if (starter.canHandle()) {
                tasks.remove(starter.getTag());
                postExecute(data);
            }
        }

        public abstract void postExecute(final DataType data);

    }

    public TaskCallback<?, T> getCallback() {
        return callback;
    }

    public static class StarterTag<T> {

        private int tag = -1;
        private T starter;

        public StarterTag(final int tag, final T starter) {
            this.tag = tag;
            this.starter = starter;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            StarterTag that = (StarterTag) o;

            if (tag != that.tag) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return tag;
        }

    }

    public interface Starter<S> {

        public StarterTag<S> getTag();

        public boolean canHandle();

    }


}
