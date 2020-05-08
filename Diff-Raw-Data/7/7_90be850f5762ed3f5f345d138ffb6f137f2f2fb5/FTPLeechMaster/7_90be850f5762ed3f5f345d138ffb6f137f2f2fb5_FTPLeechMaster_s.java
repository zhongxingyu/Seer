 package com.scurab.java.ftpleecher;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Cor class for implenting working queue
  */
 public class FTPLeechMaster implements FTPDownloadListener {
 
     /**
      * variable for count of running download threads
      */
     private int mWorkingThreads = 4;
     /**
      * Queue of threds *
      */
     private static List<FTPDownloadThread> mQueue = new ArrayList<FTPDownloadThread>();
     /**
      * Current working threads where is handled main logic
      */
     private Thread mWorkingThread;
 
     private boolean mIsRunning = true;
 
     private NotificationAdapter mAdapter;
 
     /** thread index counter **/
     private static int mThreadIndex = 0;
 
     public FTPLeechMaster() {
         mWorkingThread = new Thread(new Runnable() {
             @Override
             public void run() {
                 doImpl();
             }
         });
         mWorkingThread.setName("FTPLeechMaster");
         mWorkingThread.start();
     }
 
     public void doImpl() {
         try {
             while (mIsRunning) {
                 while (!mQueue.isEmpty()) {
                     synchronized (mQueue) {
                         /*
                          * Main cycle for starting new waiting threads
                          * This cycle is called every time if any thread change state
                          */
 
                         //region cycle
                         for (int i = 0, s = 0, n = mQueue.size(); i < n && s < mWorkingThreads; i++) {
                             FTPDownloadThread thread = mQueue.get(i);
 
                             if (thread != null) {
                                 final FTPDownloadThread.State state = thread.getFtpState();
                                 if (state == FTPDownloadThread.State.Created) {
                                     System.out.println("Starting " + thread.getContext().remoteFullPath + " part: " + thread.getContext().part + " State:" + thread.getFtpState());
 
                                     synchronized(mQueue){
                                         //started in lock, to be sure there is calling after state change
                                         thread.start();
                                         /*
                                          * wait for status change which should
                                          * be almost immediately to downloaded, finished or connecting
                                          */
                                         mQueue.wait();
                                     }
                                     if (!StateHelper.isActive(thread.getFtpState())) {
                                         continue;//nothing to do thread is not active
                                     }
                                 }else if (!StateHelper.isActive(state)) {
                                     continue;//nothing to do
                                 }
                                 s++;//increase working threads number
                             }
                         }
                         //endregion cycle
                         synchronized (mQueue){
                             //wait for any change
                             mQueue.wait();
                         }
                     }
                 }
                 //if queue is empty, still wait for new events
                 if (mIsRunning) {
                     synchronized (mQueue) {
                         mQueue.wait();
                     }
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     /**
      * Enqueue new download task to queue for download
      *
      * @param task
      */
     public void enqueue(DownloadTask task) {
         //register listener and inc thread index
         for (FTPDownloadThread t : task.getData()) {
             t.registerListener(this);
             t.setIndex(mThreadIndex++);
         }
 
         //add data and notify working thread about change
         synchronized (mQueue) {
             mQueue.addAll(task.getData());
             mQueue.notifyAll();
         }
 
         //notify adapter about big change
         if (mAdapter != null) {
             mAdapter.performNotifyDataChanged();
         }
     }
 
     //region notification
     @Override
     public void onError(FTPDownloadThread source, Exception e) {
 //        synchronized (mQueue){
 //            mQueue.notifyAll();
 //        }
         if (mAdapter != null) {
             mAdapter.performNotifyDataChanged(source);
         }
     }
 
     @Override
     public void onFatalError(FTPDownloadThread source, FatalFTPException e) {
         synchronized (mQueue) {
             mQueue.notifyAll();
         }
         if (mAdapter != null) {
             mAdapter.performNotifyDataChanged(source);
         }
     }
 
     @Override
     public void onDownloadProgress(FTPDownloadThread source, double down, double downPerSec) {
         if (mAdapter != null) {
             mAdapter.performNotifyDataChanged(source);
         }
     }
 
     @Override
     public void onStatusChange(FTPDownloadThread thread, FTPDownloadThread.State state) {
         if (state == FTPDownloadThread.State.Downloaded || state == FTPDownloadThread.State.Finished) {
             System.out.println("Downloaded " + thread.getContext().remoteFullPath + " part: " + thread.getContext().part);
            synchronized (mQueue) {
                mQueue.notifyAll();
            }
         }
         if (mAdapter != null) {
             mAdapter.performNotifyDataChanged(thread);
         }
     }
 
     //endregion notification
 
     /**
      * Set current number of working threads
      *
      * @param value
      */
     public void setWorkingThreads(int value) {
         synchronized (mQueue) {
             mWorkingThreads = value;
             mQueue.notifyAll();
         }
     }
 
     /**
      * Return current size of queue incl. already finished threads
      *
      * @return
      */
     public int size() {
         return mQueue.size();
     }
 
 
     public FTPDownloadThread getItem(int index) {
         return mQueue.get(index);
     }
 
     public void setNotificationAdapter(NotificationAdapter adapter) {
         mAdapter = adapter;
     }
 
     /**
      * Get current download speed through all of threads<br/>
      * This value is approx. download speed of whole app.
      *
      * @return
      */
     public int getCurrentDownloadSpeed() {
         int v = 0;
         for (FTPDownloadThread t : mQueue) {
             v += t.getSpeed();
         }
         return v;
     }
 }
