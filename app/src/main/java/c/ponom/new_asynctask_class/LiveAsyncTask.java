/*
 * Copyright (C) 2021 Ponomarev Sergey
 *
 * Licensed under the MIT License
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package c.ponom.new_asynctask_class;

import androidx.lifecycle.MutableLiveData;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;


    /**
     * Extend this class for execution of asynchronous background task, that can publish completion
     * progress thread and return result of computation in ui thread. Each created task can be run
     * only once. The task can be canceled when necessary.
     */
@SuppressWarnings({"Convert2Lambda", "RedundantSuppression", "unused"})
public abstract class LiveAsyncTask<Params, Progress, Result> {




    private static final AtomicInteger treadCount = new AtomicInteger(1);
    private boolean isCanceled = false;
    private Status status= Status.PENDING;
    private Params argument=null;
    private MutableLiveData<Progress> updateValueLiveData=null;
    private MutableLiveData<Result> resultValueLiveData=null;
    private Integer priority =Thread.NORM_PRIORITY;

    public enum Status {
        PENDING,
        RUNNING,
        FINISHED,
    }


    private LiveAsyncTask(){
    }

    /**
     * @param argument The parameters of the task.
     * @param updateValueLiveData Pass a LiveData for publishing progress of task completion on the ui thread.
     * Can be null.
     * @param resultValueLiveData Pass a LiveData for publishing result off task completion on the ui thread.
     * Can be null.
     * @param priority Priority of the task execution thread, from Thread.MAX_PRIORITY (10),to Thread.MIN_PRIORITY (1).
     * Can be null for default priority (5).
     */
    public LiveAsyncTask(Params argument, MutableLiveData<Progress> updateValueLiveData,
                         MutableLiveData<Result> resultValueLiveData, Integer priority) {
        this.argument = argument;
        this.updateValueLiveData = updateValueLiveData;
        this.resultValueLiveData = resultValueLiveData;
        if (priority !=null) this.priority = priority;
    }


    /**
     * Override this method to perform a computation on a background thread.
     * <p>
     * This method will  run on a background thread, and can call publishProgress (Object) to publish
     * updates on the UI thread.
     * Check for value of {@link #isCanceled()} in code  if canceling of background task
     * can be necessary.
     *
     * @param argument The parameters of the task.
     * @return A result of task execution.
     * @see #callOnCompletion(Object)
     * @see #cancel()
     * @see #publishProgress
     */
    protected abstract Result doInBackground(Params argument);


    /**
     * Any task can be executed only once
     * @see #callOnCompletion(Object)
     * @see #cancel()
     * @see #publishProgress
     */
    public final void execute(){
            if (status!=Status.PENDING)
                throw new IllegalStateException("Task can be executed only once!");

            Runnable task = () -> {
                Result result=doInBackground(argument);
                if (!isCanceled()){
                updateOnResult(result);
                callOnCompletion(result);
                }
                status=Status.FINISHED;
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


    /**
     * If you call cancel() on the task,the value of {@link #isCanceled()}
     * should be checked periodically in {@link #doInBackground(Object)} to end
     *  the task as soon as possible. Cancelling task stops publishing
     *  progress immediately, terminal methods {@link #callOnCompletion(Object)},
     *  and {@link #updateOnResult(Object)}    won't be invoked after that
     */
    public final void  cancel() {
        isCanceled=true;
    }


        /**
         * Call that method in {@link #doInBackground(Object)} to publish progress on ui thread
         *
         * @param updateValue current progress level
         */
        protected final void publishProgress(Progress updateValue) {
            if (updateValueLiveData != null && updateValueLiveData.hasObservers() && !isCanceled())
                updateValueLiveData.postValue(updateValue);
        }


        /**
         * Override that method for a final operation with result.
         * Method will be called in background thread
         */
        protected void callOnCompletion(Result result) {

        }

        public Status getStatus() {
            return status;
        }

        public boolean isCanceled() {
            return isCanceled;
        }

        private void updateOnResult(Result result) {
            if (resultValueLiveData != null && resultValueLiveData.hasObservers())
                resultValueLiveData.postValue(result);
        }

    }
