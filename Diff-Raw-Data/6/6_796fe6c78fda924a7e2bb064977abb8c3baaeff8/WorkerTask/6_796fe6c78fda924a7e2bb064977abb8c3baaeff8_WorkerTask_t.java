 package de.raptor2101.GalDroid.WebGallery.Tasks;
 
 import java.lang.Thread.State;
 import java.util.ArrayDeque;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 import java.util.concurrent.Callable;
 
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 
 public abstract class WorkerTask<ParameterType, ProgressType, ResultType> implements WorkerTaskInterface {
     protected static final String CLASS_TAG = "WorkerTask";
     private static final int MESSAGE_POST_RESULT = 1;
     private static final int MESSAGE_POST_PROGRESS = 2;
     private static final int MESSAGE_POST_ERROR = 3;
     private static final int MESSAGE_PRE_EXECUTION = 4;
 
     private class WorkerTaskMessage<MessageType> {
 	public final ParameterType mParameter;
 	public final MessageType mMessage;
 
 	public WorkerTaskMessage(ParameterType parameter, MessageType message) {
 	    mMessage = message;
 	    mParameter = parameter;
 	}
     }
 
     private class MessageHandler extends Handler {
 	@SuppressWarnings("unchecked")
 	@Override
 	public void handleMessage(Message msg) {
 	    switch (msg.what) {
 	    case MESSAGE_PRE_EXECUTION: {
 		WorkerTaskMessage<ResultType> message = (WorkerTaskMessage<ResultType>) msg.obj;
 		onPreExecute(message.mParameter);
 		break;
 	    }
 	    case MESSAGE_POST_RESULT: {
 		WorkerTaskMessage<ResultType> message = (WorkerTaskMessage<ResultType>) msg.obj;
 		onPostExecute(message.mParameter, message.mMessage);
 		break;
 	    }
 	    case MESSAGE_POST_PROGRESS: {
 		WorkerTaskMessage<ProgressType> message = (WorkerTaskMessage<ProgressType>) msg.obj;
 		onProgressUpdate(message.mParameter, message.mMessage);
 		break;
 	    }
 	    case MESSAGE_POST_ERROR: {
 		WorkerTaskMessage<Exception> message = (WorkerTaskMessage<Exception>) msg.obj;
 		onExceptionThrown(message.mParameter, message.mMessage);
 		break;
 	    }
 	    }
 	}
     }
 
     private final MessageHandler mMessageHandler = new MessageHandler();
 
     private class WorkerCallable implements Callable<ResultType> {
 	private ParameterType mParameter;
 
 	public WorkerCallable(ParameterType parameter) {
 	    mParameter = parameter;
 	}
 
 	public ResultType call() throws Exception {
 	    return doInBackground(mParameter);
 	}
 
 	@Override
 	public int hashCode() {
 	    return mParameter.hashCode();
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 	    if (this == obj)
 		return true;
 	    if (obj == null)
 		return false;
 	    if (getClass() != obj.getClass())
 		return false;
 	    @SuppressWarnings("unchecked")
 	    WorkerCallable other = (WorkerCallable) obj;
 	    if (!getOuterType().equals(other.getOuterType()))
 		return false;
 	    if (mParameter == null) {
 		if (other.mParameter != null)
 		    return false;
 	    } else if (!mParameter.equals(other.mParameter))
 		return false;
 	    return true;
 	}
 
 	@SuppressWarnings("rawtypes")
 	private WorkerTask getOuterType() {
 	    return WorkerTask.this;
 	}
	
	@Override
	public String toString() {
	    return mParameter.toString();
	}
     }
 
     private final Queue<WorkerCallable> mCallables = new LinkedList<WorkerCallable>();
 
     private class WorkerRunnable implements Runnable {
 
 	private boolean mRunning = true;
 	WorkerCallable mCurrentCallable;
 
 	public void run() {
 	    mIsStarted = true;
 	    while (mRunning) {
 		try {
 		    Message message;
 		    mCurrentCallable = waitForCallable();
 		    try {
 			WorkerTaskMessage<ResultType> messageBody = new WorkerTaskMessage<ResultType>(mCurrentCallable.mParameter, null);
 			message = mMessageHandler.obtainMessage(MESSAGE_PRE_EXECUTION, messageBody);
 			message.sendToTarget();
 
 			Log.d(String.format("%s (%s)", CLASS_TAG, mThreadName), String.format("Executing callable for %s", mCurrentCallable.mParameter));
 			ResultType result = mCurrentCallable.call();
 			messageBody = new WorkerTaskMessage<ResultType>(mCurrentCallable.mParameter, result);
 			message = mMessageHandler.obtainMessage(MESSAGE_POST_RESULT, messageBody);
 		    } catch (Exception e) {
 			Log.e(String.format("%s (%s)", CLASS_TAG, mThreadName), String.format("Something goes wrong while executing callable for %s", mCurrentCallable.mParameter),e);
 			WorkerTaskMessage<Exception> messageBody = new WorkerTaskMessage<Exception>(mCurrentCallable.mParameter, e);
 			message = mMessageHandler.obtainMessage(MESSAGE_POST_ERROR, messageBody);
 		    }
 		    Log.d(String.format("%s (%s)", CLASS_TAG, mThreadName), String.format("Invoke callback for %s", mCurrentCallable.mParameter));
 		    message.sendToTarget();
 		    mCurrentCallable = null;
 		    mIsCancelled = false;
 		} catch (InterruptedException e) {
 		    // TODO Auto-generated catch block
 		    e.printStackTrace();
 		}
 	    }
 	}
 
 	private WorkerCallable waitForCallable() throws InterruptedException {
 	    synchronized (mCallables) {
 		Log.d(String.format("%s (%s)", CLASS_TAG, mThreadName), "Looking for enqueued callables");
 		while (mCallables.size() == 0) {
 		    mIsSleeping = true;
 		    Log.d(String.format("%s (%s)", CLASS_TAG, mThreadName), "Waiting for enqueued callables");
 		    mCallables.wait();
 		}
 		mIsSleeping = false;
 		Log.d(String.format("%s (%s)", CLASS_TAG, mThreadName), "Returning enqueued callables");
 		return mCallables.poll();
 	    }
 	}
     }
 
     private Thread mWorkerThread;
     private final WorkerRunnable mWorkerRunnable = new WorkerRunnable();
     private boolean mIsStarted = false;
     private boolean mIsSleeping = false;
     private boolean mIsStopping = false;
     private boolean mIsCancelled = false;
 
     protected String mThreadName = "WorkerTask";
 
     protected void enqueue(ParameterType parameter) {
 	if (!mIsStopping) {
 	    Log.d(String.format("%s (%s)", CLASS_TAG, mThreadName), String.format("Enqueuing %s", parameter));
 	    synchronized (mCallables) {
 		mCallables.add(new WorkerCallable(parameter));
 		Log.d(String.format("%s (%s)", CLASS_TAG, mThreadName), "Notify waiting WorkerThread");
 		mCallables.notifyAll();
 	    }
 	}
     }
 
     public final void start() {
 	if (mWorkerThread == null || mWorkerThread.getState() == State.TERMINATED) {
 	    mIsStarted = false;
 	    mIsSleeping = false;
 	    mIsStopping = false;
 	    mWorkerRunnable.mRunning = true;
 	    mWorkerThread = new Thread(mWorkerRunnable, mThreadName);
 	    mWorkerThread.setDaemon(false);
 	    Log.d(String.format("%s (%s)", CLASS_TAG, mThreadName), "Starting WorkerThread");
 	    mWorkerThread.start();
 	}
     }
 
     public final void stop() {
 	mWorkerRunnable.mRunning = false;
     }
 
     public final void cancel() {
 	mIsCancelled = true;
 	mWorkerRunnable.mRunning = false;
 	synchronized (mCallables) {
 	    mCallables.clear();
 	}
 	mWorkerThread.interrupt();
     }
 
     private void finish(ParameterType parameter, ResultType result) {
 	if (mIsCancelled) {
 	    onCancelled(parameter, result);
 	} else {
 	    onPostExecute(parameter, result);
 	}
 
     }
 
     protected void onPreExecute(ParameterType parameter) {
     }
 
     protected abstract ResultType doInBackground(ParameterType parameter);
 
     protected abstract void onPostExecute(ParameterType parameter, ResultType result);
 
     protected void onExceptionThrown(ParameterType parameter, Exception exception) {
     }
 
     protected void onProgressUpdate(ParameterType parameter, ProgressType progress) {
     }
 
     protected void onCancelled(ParameterType parameter, ResultType result) {
 	onCancelled();
     }
 
     protected void onCancelled() {
     }
 
     public final Status getStatus() {
 	if (mWorkerThread == null || mWorkerThread.getState() == State.TERMINATED) {
 	    return Status.FINISHED;
 	}
 	if (!mIsStarted) {
 	    return Status.PENDING;
 	}
 	if (mIsSleeping) {
 	    return Status.SLEEPING;
 	}
 	return Status.RUNNING;
     }
 
     public boolean isCancelled() {
 	return mIsCancelled;
     }
 
     protected boolean isEnqueued(ParameterType parameter) {
 	synchronized (mCallables) {
 	    return mCallables.contains(new WorkerCallable(parameter));
 	}
     }
 
     protected boolean isActive(ParameterType parameter) {
 	Status status = getStatus();
 	Log.d(String.format("%s (%s)", CLASS_TAG, mThreadName), String.format("Status: %s", status));
 	if (status != Status.RUNNING) {
 	    return false;
 	}
 	Log.d(String.format("%s (%s)", CLASS_TAG, mThreadName), String.format("CurrentCallable %s Parameter %s", mWorkerRunnable.mCurrentCallable, parameter));
 	return mWorkerRunnable.mCurrentCallable != null && mWorkerRunnable.mCurrentCallable.mParameter.equals(parameter);
     }
 }
