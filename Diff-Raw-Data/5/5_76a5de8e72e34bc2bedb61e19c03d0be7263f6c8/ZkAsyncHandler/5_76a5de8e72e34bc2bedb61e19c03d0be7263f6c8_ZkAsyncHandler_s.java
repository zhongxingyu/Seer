 /*
  * Copyright (c) 2012, DataLite. All rights reserved.
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA 02110-1301  USA
  */
 package cz.datalite.zk.annotation.invoke;
 
 import cz.datalite.utils.ZkCancellable;
 import cz.datalite.zk.annotation.ZkAsync;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.zkoss.lang.Library;
 import org.zkoss.zk.ui.event.*;
 
 /**
  * <p>Handles binding request before and after method invocation. For all
  * registered component executes load or safe based on annotation's
  * properties.</p>
  *
  * @author Karel Čemus <cemus@datalite.cz>
  */
 public class ZkAsyncHandler extends Handler {
     
     /** Logger instance */
     private static final Logger LOGGER = LoggerFactory.getLogger( ZkAsyncHandler.class );
 
     /** used queue name, one queue for all users async requests */
     private final static String QUEUE = "qLongOperations";
     
     /** key of the exception in context map */
     private static final String ASYNC_EXCEPTION = "Async::exception";
     
     /** key of the busybox in context map */
     private static final String ASYNC_BUSYBOX = "Async::busybox";
     
      /** key of the interceptor in context map */
     private static final String ASYNC_INTERCEPTOR = "Async::interceptor";
 
     /** state of general property */
     private final static boolean localizeAll;
 
     /** name of event fired after async long operation is done */
     private final String eventAfter;
 
     /** if operation is cancellable */
     private final boolean cancellable;
     
     /** message to be shown */
     private final String message;
     
     /** use localization */
     private final boolean i18n;
     
     /** parent component */
     private final String component;
 
     static {
         /** Reads default configuration for library */
         localizeAll = Boolean.parseBoolean(Library.getProperty("zk-dl.annotation.i18n", "false"));
     }
 
     public static Invoke process(Invoke inner, ZkAsync annotation) {
         String message = annotation.message();
         // check for default localized message
         boolean i18n = localizeAll || message.startsWith("zkcomposer.") || annotation.i18n();
         // parent component
         String component = "".equals(annotation.component()) ? null : annotation.component();
         // return instance of handler
         return new ZkAsyncHandler(inner, message, i18n, annotation.cancellable(), component, annotation.doAfter());
     }
 
     public ZkAsyncHandler(Invoke inner, final String message, final boolean i18n, final boolean cancellable, final String component, final String eventAfter) {
         super(inner);
         this.message = message;
         this.i18n = i18n;
         this.component = component;
         this.cancellable = cancellable;
         this.eventAfter = eventAfter;
     }
 
     @Override
     public boolean invoke(final Context context) throws Exception {
 
         // check for already running
         if (isAsyncRunning()) {
             LOGGER.trace( "One operation is already running. Request was rejected." );
         } else {
             // subscribe to listen async events
             subscribe(context);
 
             // publish event to start async operation
             publish();
 
             // invokes status window informing user about operation
             final BusyBoxHandler busybox = new BusyBoxHandler( message, i18n, cancellable, component );
             if ( cancellable ) // listen for request to cancel
                 busybox.setEventListener( Events.ON_CLOSE, new EventListener() {
 
                     public void onEvent( Event event ) throws Exception {
                         LOGGER.trace( "Async operation was cancelled." );
                         // race conditions - 2 threads works with interruptor, NPE prevention
                         synchronized ( ZkAsyncHandler.this ) {
                             final ZkCancellable interruptor = ( ZkCancellable ) context.getParameter( ASYNC_INTERCEPTOR );
                             if ( interruptor != null )
                                 interruptor.cancel();
                         }
                         // prevent window closing
                         event.stopPropagation();
                     }
                 } );
             busybox.show( context.getRoot() );
             context.putParameter( ASYNC_BUSYBOX, busybox);
         }
 
         // invocation is not complete, prevent resuming
         return false;
     }
 
     @Override
     protected void doAfter(final Context context) {
         try {
             Throwable exception = ( Throwable ) context.getParameter( ASYNC_EXCEPTION );
             if (exception != null) {
                 LOGGER.error( "Execution of long running operation failed. Exception has been thrown." );
                 throw new RuntimeException(exception);
             }
 
             // in there is not exception, invoke event after
             // async processing is done, fire notification to process rest
             Events.postEvent(eventAfter, context.getRoot(), null);
 
         } finally {
             // close shown blocking window
             final BusyBoxHandler busybox = ( BusyBoxHandler ) context.getParameter( ASYNC_BUSYBOX );
             busybox.close(context.getRoot());
            // exception was passed through
            context.removeParameter( ASYNC_EXCEPTION );
         }
     }
 
     /** checks for already running async event. doesn't allow multiple execution */
     private boolean isAsyncRunning() {
         return EventQueues.exists(QUEUE, EventQueues.SESSION);
     }
 
     /** subscribes listeners on queue */
     private void subscribe(final Context context) {
         // subscribe async listener to handle long operation
         findQueue().subscribe(
                 createInvokeListener(context),
                 createCallbackToResume(context));
     }
 
     /** publishes any event to start invocation */
     private void publish() {
         // fire event to start the long operation
         findQueue().publish(new Event("AsyncEvent"));
     }
 
     /** destroys queue to allow simple detection of non-running event */
     private void cleanUp() {
         EventQueues.remove(QUEUE, EventQueues.SESSION);
     }
 
     /** returns queue for this user. If queue is not exist than it is created */
     private EventQueue findQueue() {
         return EventQueues.lookup(QUEUE, EventQueues.SESSION, true);
     }
 
     /**
      * Creates event listener to resume running events this listener serves to
      * sync callback
      */
     private EventListener createCallbackToResume(final Context context) {
         //callback
         return new EventListener() {
 
             public void onEvent(Event evt) throws Exception {
                 LOGGER.trace( "Async operation finished." );
                 // clean up queue
                 cleanUp();
 
                 // invocation is done
                 // do doAfterInvoke on all objects
                 context.getInvoker().doAfterInvoke(context);
             }
         };
     }
 
     /**
      * Async event listener to execute long running operation
      */
     private EventListener createInvokeListener(final Context context) {
         return new EventListener() {
 
             public void onEvent(Event evt) throws Exception { //asynchronous
                 try {
                     LOGGER.trace( "Starting async operation." );
 
                     if (cancellable) {
                         // add request to allow cancelling
                         // instance can be detected
                         ZkCancellable.setCancellable();
                         // race conditions - 2 threads works with interruptor, NPE prevention
                         synchronized (ZkAsyncHandler.this) {
                             final ZkCancellable interruptor = ZkCancellable.get();
                             context.putParameter( ASYNC_INTERCEPTOR, interruptor );
                         }
                     }
 
                     // resume invoking inner methods
                     inner.invoke(context);
                 } catch (Exception ex) {
                     // if an exception is caught, that it has to be rethrown 
                     // in a synchronnous event to allow simple displaying 
                     // displayingin UI
                     context.putParameter( ASYNC_EXCEPTION, ex);
                 } finally {
                     // clean up current thread. 
                     // This is not to be cancellable any more
                     ZkCancellable.clean();
                     // race conditions - 2 threads works with interruptor, NPE prevention
                     synchronized (ZkAsyncHandler.this) {
                         context.removeParameter( ASYNC_INTERCEPTOR );
                     }
                 }
             }
         };
     }
 }
