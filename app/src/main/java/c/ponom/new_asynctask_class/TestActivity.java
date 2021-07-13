package c.ponom.new_asynctask_class;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;


import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Calendar;

import c.ponom.myapplication.R;

import static java.lang.Integer.valueOf;
import static java.lang.Math.*;
import static java.lang.Thread.sleep;
@SuppressLint("DefaultLocale")
public class TestActivity extends AppCompatActivity {



    private TestAsyncTask testAsyncTask;
    private ProgressBar progressBar;
    private static final int TEST_ROUNDS=100;
    private final MutableLiveData <Integer> progress = new MutableLiveData<>();
    private final MutableLiveData <String> result = new MutableLiveData<>();
    // todo - что  с многопоточностью  делать будем?


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView updateText = findViewById(R.id.updated_data);
        TextView resultText = findViewById(R.id.result_data);

        progressBar= findViewById(R.id.progress_bar);
        progressBar.setMax(TEST_ROUNDS-1);

        progress.observe(this, new Observer<Integer>() {

            @Override
            public void onChanged(Integer progress) {
                progressBar.setProgress(progress);
                updateText.setText(String.format("priority: %d", progress));
            }
        });
        result.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                resultText.setText(s);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (testAsyncTask!=null&&testAsyncTask.getStatus()
                == LiveAsyncTask.Status.RUNNING) testAsyncTask.cancel();
        progressBar.setProgress(0);
    }

    public void startTask(View view) {
        testAsyncTask = new TestAsyncTask(" Test argument string",
                progress,result,null);
        testAsyncTask.execute();
    }

    public void cancelTask(View view) {
        testAsyncTask.cancel();
    }



    static class TestAsyncTask  extends LiveAsyncTask<String, Integer, String> {
        private static final String TAG = "Test AsyncTasks" ;
        public TestAsyncTask(String argument,
                             MutableLiveData<Integer> updateValueLiveData,
                             MutableLiveData<String> resultValueLiveData,
                             Integer setPriority) {
            super(argument, updateValueLiveData, resultValueLiveData, setPriority);
        }

        @Override
        protected void callOnCompletion(String result) {
            Log.e(TAG, "callOnCompletion: done");
        }

        @Override
        protected String doInBackground(String argument) {
            for (int i = 0; i < TEST_ROUNDS; i++) {
                if (isCanceled()) break;
                try {
                    sleep((long) (random()* 400));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.e(TAG, "doInBackground: priority: "
                        +Thread.currentThread().getPriority() + " name: "
                        +Thread.currentThread().getName() );
                publishProgress(i);
            }
            return "Argument= "+argument+". All done!";
        }
    }


    public void restart(View view) {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis() + 3000,
                // 3 seconds
                PendingIntent.getActivity(this,
                        0, getIntent(),
                        PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_CANCEL_CURRENT));
        Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }
}