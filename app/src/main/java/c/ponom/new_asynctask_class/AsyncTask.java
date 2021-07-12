package c.ponom.new_asynctask_class;

import androidx.lifecycle.MutableLiveData;

public abstract class AsyncTask<T,U,R> {

  private AsyncTask(){

  }

    private T argument=null;
    private MutableLiveData<U> updateValueLiveData=null;
    private MutableLiveData<R> resultValueLiveData=null;
    private Integer setPriority=Thread.NORM_PRIORITY;


    public AsyncTask(T argument, MutableLiveData<U> updateValueLiveData,
                      MutableLiveData<R> resultValueLiveData, Integer setPriority) {
        this.argument = argument;
        this.updateValueLiveData = updateValueLiveData;
        this.resultValueLiveData = resultValueLiveData;
        if (setPriority !=null) this.setPriority = setPriority;
    }

    protected abstract R doInBackground(T  argument);

    public void execute(){
            Runnable task = new Runnable() {
                @Override
                public void run() {
                    R result=doInBackground(argument);
                    updateOnResult(result);
                    callOnCompletion(result);
                }
            };
            Thread runningThread = new Thread(task);
            runningThread.setPriority(setPriority);
            runningThread.start();
    }


    //todo сделать джавадок и выложить
    protected void callOnCompletion(R result) {

    }

    protected void updateProgress(U updateValue){
     if (updateValueLiveData!=null)
         updateValueLiveData.postValue(updateValue);
    }

    private void updateOnResult(R result) {
        if (resultValueLiveData!=null)
            resultValueLiveData.postValue(result);
    }

}
