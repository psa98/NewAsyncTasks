package c.ponom.new_asynctask_class;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import c.ponom.myapplication.R;

import static java.lang.Integer.valueOf;
import static java.lang.Math.*;
import static java.lang.Thread.sleep;
@SuppressLint("DefaultLocale")
public class TestActivity extends AppCompatActivity {



    TestAsyncTask testAsyncTask;
    private ProgressBar progressBar;
    private static final int TEST_ROUNDS=100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView updateText = findViewById(R.id.updated_data);
        TextView resultText = findViewById(R.id.result_data);
        MutableLiveData <Integer> progress = new MutableLiveData<>();
        MutableLiveData <String> result = new MutableLiveData<>();
        testAsyncTask = new TestAsyncTask("Argument string",progress,result,null);
        progressBar= findViewById(R.id.progress_bar);
        progressBar.setMax(TEST_ROUNDS-1);

        progress.observe(this, new Observer<Integer>() {

            @Override
            public void onChanged(Integer progress) {
                progressBar.setProgress(progress);
                updateText.setText(String.format("round: %d", progress));
            }
        });
        result.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                resultText.setText(s);
            }
        });
    }


    public void startTask(View view) {
        testAsyncTask.execute();
    }

    static class TestAsyncTask  extends AsyncTask<String, Integer, String>{


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

                try {
                    sleep((long) (random()* 400));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                updateProgress(i );
            }
            return "Argument= "+argument+". All done!";
        }
    }
}