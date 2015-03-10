package ru.sofitlabs.lifecycleawaretask;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


public class MainActivity extends Activity implements LifecycleAwareTask.Starter<MainActivity> {

    private boolean canHandle = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        canHandle = true;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LifecycleAwareTask.onStarterCreate(this);
    }

    @Override
    protected void onPause() {
        canHandle = false;
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startTask();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void startTask() {
        showToast("Wake up, Neo!");
        LifecycleAwareTask.TaskCallback<MainActivity, String> callback = new LifecycleAwareTask.TaskCallback<MainActivity, String>(this) {

            @Override
            public void postExecute(final String data) {
                starter.showToast(data);
            }

        };
        LifecycleAwareTask<String> task = new LifecycleAwareTask<String>(callback) {
            @Override
            public String doInBackground() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return "Ohayo, nii-san!";
            }
        };
        task.start(this);
    }

    public void showToast(final String toast) {
        Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
    }

    @Override
    public LifecycleAwareTask.StarterTag<MainActivity> getTag() {
        return new LifecycleAwareTask.StarterTag<>(0, this);
    }

    @Override
    public boolean canHandle() {
        return canHandle;
    }

}
