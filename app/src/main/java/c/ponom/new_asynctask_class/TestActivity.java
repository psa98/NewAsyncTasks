package c.ponom.new_asynctask_class;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import c.ponom.new_asynk_task.R;

import static java.lang.Math.random;
import static java.lang.Thread.sleep;

@SuppressWarnings({"Convert2Lambda", "Anonymous2MethodRef"})
@SuppressLint("DefaultLocale")
public class TestActivity extends AppCompatActivity {

    private TestAsyncTask testAsyncTask;
    private ProgressBar progressBar;
    private static final int TEST_ROUNDS = 100;
    private final MutableLiveData<Integer> progress = new MutableLiveData<>();
    private final MutableLiveData<String> result = new MutableLiveData<>();
    private static final String TAG = "Test AsyncTasks";


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
                updateText.setText(String.format("Completion progress: %d", progress+1));
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
        Log.i(TAG, "onStop: task cancelled ");
        progressBar.setProgress(0);
    }

    public void startTask(View view) {
        testAsyncTask = new TestAsyncTask(" Test argument ",
                progress,result,Thread.MAX_PRIORITY);
        testAsyncTask.execute();
    }

    public void cancelTask(View view) {
        testAsyncTask.cancel();
    }

    static class TestAsyncTask  extends LiveAsyncTask<String, Integer, String> {

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
                Log.i(TAG, "doInBackground: priority:  "
                        + Thread.currentThread().getPriority() + " name: "
                        + Thread.currentThread().getName());
                publishProgress(i);
            }
            return "Argument= "+argument+". Task completed!";
        }
    }

}