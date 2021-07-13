package c.ponom.new_asynctask_class;

import androidx.lifecycle.MutableLiveData;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class LiveAsyncTask<Params, Progress, Result> {

  private LiveAsyncTask(){

  }


    public enum Status {
        PENDING,
        RUNNING,
        FINISHED,
    }


    private static final AtomicInteger treadCount = new AtomicInteger(1);
    private boolean isCanceled = false;
    private Status status= Status.PENDING;
    private Params argument=null;
    private MutableLiveData<Progress> updateValueLiveData=null;
    private MutableLiveData<Result> resultValueLiveData=null;
    private Integer priority =Thread.NORM_PRIORITY;

    public Status getStatus() {
        return status;
    }

    public LiveAsyncTask(Params argument, MutableLiveData<Progress> updateValueLiveData,
                         MutableLiveData<Result> resultValueLiveData, Integer setPriority) {
        this.argument = argument;
        this.updateValueLiveData = updateValueLiveData;
        this.resultValueLiveData = resultValueLiveData;
        if (setPriority !=null) this.priority = setPriority;
    }



    /**
     * Override this method to perform a computation on a background thread.
     *
     * This will  run on a background thread.
     * This method can call publishProgress (Object) to publish updates
     * on the UI thread.
     * Check for value of i{@link #isCanceled()} if canceling of background task can be necessary
     * @param argument The parameters of the task.
     *
     * @return A result, defined by the subclass of this task.
     *
     * @see #callOnCompletion(Object)
     * @see #cancel()
     * @see #publishProgress
     */
    protected abstract Result doInBackground(Params argument);

    public final void execute(){
            if (status!=Status.PENDING)
                throw new IllegalStateException("Task can be executed only once!");

            Runnable task = new Runnable() {
                @Override
                public void run() {
                    Result result=doInBackground(argument);
                    if (!isCanceled()){
                    updateOnResult(result);
                    callOnCompletion(result);}
                    status=Status.FINISHED;
                }
            };

            ThreadFactory threadFactory = new ThreadFactory() {
             public Thread newThread(Runnable r) {
                Thread newThread= new Thread(r, "AsyncTask #" + treadCount.getAndIncrement());
                newThread.setPriority(priority);
                return newThread;
            }
            };
            ExecutorService executor = Executors.newSingleThreadExecutor(threadFactory);
            executor.submit(task);
            status=Status.RUNNING;
            executor.shutdown();
    }

    ;


    /*Override that method for a final operations with result. Method called in background tread*/
    protected void callOnCompletion(Result result) {

    }


    /**
     * Call that method in {@link #doInBackground(Object)} to publish progress on ui thread
     *@param updateValue The parameters of the task.
     */
    protected final void publishProgress(Progress updateValue){
     if (updateValueLiveData!=null&&updateValueLiveData.hasObservers())
         updateValueLiveData.postValue(updateValue);
    }

    private  void updateOnResult(Result result) {
        if (resultValueLiveData!=null&&resultValueLiveData.hasObservers())
            resultValueLiveData.postValue(result);
    }


    public boolean isCanceled() {
        return isCanceled;
    }

    /**
     * If you are calling cancel(boolean) on the task,the value of {@link #isCanceled()}
     * should be checked periodically in {@link #doInBackground(Object)} to end
     *  the task as soon as possible.
     */
    public final void  cancel() {
        isCanceled=true;
    }



}
