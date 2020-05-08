 /*
  * Copyright 2012 Alexey Hanin
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package getrest.android.service;
 
 import android.app.Service;
 import android.content.Intent;
 import android.os.IBinder;
 import getrest.android.request.Request;
 import getrest.android.util.Logger;
 import getrest.android.util.LoggerFactory;
 
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.RejectedExecutionException;
 import java.util.concurrent.ThreadFactory;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 /**
  * @author aha
  * @since 2012-01-13
  */
 public class RestService extends Service implements Broadcaster {
 
     private static final Logger LOGGER = LoggerFactory.getLogger("getrest.service");
 
     private final ThreadPoolExecutor jobExecutorService;
 
     public RestService() {
         this.jobExecutorService = new ThreadPoolExecutor(1, 5, 5, TimeUnit.SECONDS,
                 new LinkedBlockingQueue<Runnable>(),
                 new ThreadFactoryImpl());
     }
 
     @Override
     public int onStartCommand(final Intent intent, final int flags, final int startId) {
         final Request request = new RequestWrapper(intent).getRequest();
 
         LOGGER.debug("Received request: requestId={0}, url={1}, method={2}. Submitting it to queue.",
                 request.getRequestId(), request.getUri(), request.getMethod().getName());
 
         try {
             final RequestEventBus eventBus = new RequestEventBus();
             eventBus.setBroadcaster(this);
 
             eventBus.firePending(request.getRequestId());
 
             final RequestJob job = new RequestJob(request);
             job.setRequestEventBus(eventBus);
 
             jobExecutorService.submit(job);
         } catch (RejectedExecutionException ex) {
             // TODO implement rejected execution fallback
             throw new UnsupportedOperationException("Handling of rejected execution exception is yet to implement");
         }
 
         return START_NOT_STICKY;
     }
 
     @Override
     public IBinder onBind(final Intent intent) {
         return null;
     }
 
     @Override
     public void sendBroadcast(final Intent intent) {
         super.sendBroadcast(intent);
     }
 
     private static class ThreadFactoryImpl implements ThreadFactory {
         public Thread newThread(final Runnable runnable) {
             final Thread thread = new Thread(runnable);
             thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                 public void uncaughtException(final Thread thread, final Throwable throwable) {
                     LOGGER.error("Uncaught exception in thread '{0}'", throwable, thread.getId());
                 }
             });
             return thread;
         }
     }
 }
