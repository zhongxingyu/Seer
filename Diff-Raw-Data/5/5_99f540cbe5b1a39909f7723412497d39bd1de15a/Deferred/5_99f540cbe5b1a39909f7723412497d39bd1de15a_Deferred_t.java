 /*
  * Copyright 2009-2010 New Atlanta Communications, LLC
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
 package com.newatlanta.appengine.taskqueue;
 
 import static com.google.appengine.api.datastore.DatastoreServiceFactory.getDatastoreService;
 import static com.google.appengine.api.labs.taskqueue.QueueConstants.maxTaskSizeBytes;
 import static com.google.appengine.api.labs.taskqueue.QueueFactory.getQueue;
 import static com.google.appengine.api.labs.taskqueue.TaskOptions.Builder.url;
 import static com.google.appengine.api.labs.taskqueue.TaskOptions.Builder.withDefaults;
 import static com.google.appengine.api.labs.taskqueue.TaskOptions.Method.POST;
 import static org.apache.commons.codec.binary.Base64.decodeBase64;
 import static org.apache.commons.codec.binary.Base64.encodeBase64;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.appengine.api.datastore.Blob;
 import com.google.appengine.api.datastore.DatastoreFailureException;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.EntityNotFoundException;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.labs.taskqueue.QueueFailureException;
 import com.google.appengine.api.labs.taskqueue.TaskHandle;
 import com.google.appengine.api.labs.taskqueue.TaskOptions;
 import com.google.appengine.api.utils.SystemProperty;
 
 /**
  * Implements background tasks for
  * <a href="http://code.google.com/appengine/docs/java/overview.html">Google App
  * Engine for Java</a>, based on the
  * <a href="http://code.google.com/appengine/articles/deferred.html">Python 'deferred'
  * library</a>; simplifies use of the <a href="http://code.google.com/appengine/docs/java/taskqueue/overview.html">
  * Task Queue Java API</a> by automatically handling the serialization and
  * deserializtion of complex task arguments.
  * 
  * <p>Background tasks are implemented via the {@link Deferrable Deferrable}
  * interface; task logic is implemented in the {@link Deferrable#doTask() doTask()}.
  * Background tasks are queued for execution via the {@link Deferred#defer(Deferrable)
  * defer()} method and its overrides. For example:
  * <pre>
  * MyTask task = new MyTask(); // implements Deferrable
  * Deferred.defer( task );
  * </pre>
  * 
  * <p>{@link Deferrable Deferrable} task instances are serialized in order to be
  * queued for execution. If the serialized task size exceeds 10KB, it is saved to
  * the datastore and then removed prior to task execution.
  * 
  * <p><b>Configuration</b>
  * <p>There are several configuration steps that must be completed before
  * background tasks can be executed. First, the deferred task handler (this
  * servlet) needs to be configured within <code>web.xml</code>. There are two
  * optional init parameters that are discussed below; following is the minimal
  * configuration--without init parameters--required when using the default queue
  * name and default task URL:
  * <pre>
  * &lt;servlet>
  *     &lt;servlet-name>Deferred&lt;/servlet-name>
  *     &lt;servlet-class>com.newatlanta.appengine.taskqueue.Deferred&lt;/servlet-class>
  * &lt;/servlet>
  * &lt;servlet-mapping>
  *     &lt;servlet-name>Deferred&lt;/servlet-name>
  *     &lt;url-pattern>/_ah/queue/deferred&lt;/url-pattern>
  * &lt;/servlet-mapping>
  * </pre>
  * 
  * <p>The optional init parameters are <code>queueName</code> and
  * <code>taskUrl</code>. Note that if any init parameters are specified, the
  * <code>&lt;load-on-startup></code> element <b>must</b> also be specified.
  * 
  * <p>In the following example, only the <code>queueName</code>
  * is specified; note that the <code>&lt;url-pattern></code> element has also
  * been modified accordingly:
  * <pre>
  * &lt;servlet>
  *     &lt;servlet-name>Deferred&lt;/servlet-name>
  *     &lt;servlet-class>com.newatlanta.appengine.taskqueue.Deferred&lt;/servlet-class>
  *     &lt;init-param>
  *          &lt;param-name>queueName&lt;/param-name>
  *          &lt;param-value>background&lt;/param-value>
  *     &lt;/init-param>
  *     &lt;load-on-startup>1&lt;/load-on-startup>
  * &lt;/servlet>
  * &lt;servlet-mapping>
  *     &lt;servlet-name>Deferred&lt;/servlet-name>
  *     &lt;url-pattern>/_ah/queue/background&lt;/url-pattern>
  * &lt;/servlet-mapping>
  * </pre>
  * 
  * <p>In the following example, both the <code>queueName</code> and
  * <code>taskUrl</code> init parameters are specified; note that the
  * <code>&lt;url-pattern></code> element has been modified to match the
  * <code>taskUrl</code>:
  * <pre>
  * &lt;servlet>
  *     &lt;servlet-name>Deferred&lt;/servlet-name>
  *     &lt;servlet-class>com.newatlanta.appengine.taskqueue.Deferred&lt;/servlet-class>
  *     &lt;init-param>
  *          &lt;param-name>queueName&lt;/param-name>
  *          &lt;param-value>background&lt;/param-value>
  *     &lt;/init-param>
  *     &lt;init-param>
  *          &lt;param-name>taskUrl&lt;/param-name>
  *          &lt;param-value>/worker/deferred&lt;/param-value>
  *     &lt;/init-param>
  *     &lt;load-on-startup>1&lt;/load-on-startup>
  * &lt;/servlet>
  * &lt;servlet-mapping>
  *     &lt;servlet-name>Deferred&lt;/servlet-name>
  *     &lt;url-pattern>/worker/deferred&lt;/url-pattern>
  * &lt;/servlet-mapping>
  * </pre>
  * 
  * <p>Note that if you plan to specify the task URL via the task options
  * parameter to the {@link #defer(Deferrable, TaskOptions) defer()} method, you
  * must configure the task URL within a <code>&lt;url-pattern></code> element.
  * 
  * <p>After configuring <code>web.xml</code>, the queue name must be configured
  * within <code>queue.xml</code> (use whatever rate you want):
  * <pre>
  * &lt;queue>
  *     &lt;name>deferred&lt;/name>
  *     &lt;rate>10/s&lt;/rate>
  * &lt;/queue>
  * </pre>
  *    
  * @author <a href="mailto:vbonfanti@gmail.com">Vince Bonfanti</a>
  */
 @SuppressWarnings("serial")
 public class Deferred extends HttpServlet {
     
     private static final String DEFAULT_QUEUE_NAME = "deferred";
     private static final String TASK_CONTENT_TYPE = "application/x-java-serialized-object";
     private static final String ENTITY_KIND = Deferred.class.getName();
     private static final String TASK_PROPERTY = "taskBytes";
     
     private static final String QUEUE_NAME_INIT_PARAM = "queueName";
     private static final String TASK_URL_INIT_PARAM = "taskUrl";
     
     private static final Logger log = Logger.getLogger( Deferred.class.getName() );
     
     private static String queueName = DEFAULT_QUEUE_NAME;
     private static String taskUrl;
     
     /**
      * The <code>Deferrable</code> interface should be implemented by any class
      * whose instances are intended to be executed as background tasks. The
      * implementation class must define a method with no arguments named
      * {@link Deferrable#doTask()}.
      */
     public interface Deferrable extends Serializable {
         /**
          * Invoked to perform the background task.
          * 
          * @throws PermanentTaskFailure To indicate that the task should
          * <b>not</b> be retried; all other exceptions cause the task to be
          * retried. These exceptions are logged.
          * 
          * @throws ServletException To indicate that the task should be retried.
          * These exceptions are not logged.
          * 
          * @throws IOException To indicate that the task should be retried. These
          * exceptions are not logged.
          */
         public void doTask() throws ServletException, IOException;
     }
     
     /**
      * If thrown by the {@link Deferrable#doTask() doTask()} method, indicates
      * that a background task should <b>not</b> be retried.
      */
     public class PermanentTaskFailure extends ServletException {
         /**
          * Constructs a new exception with the specified detail message.
          * 
          * @param message The detailed message.
          */
         public PermanentTaskFailure( String message ) {
             super( message );
         }
     }
     
     /**
      * Performs initialization based on the <code>&lt;init-param></code> elements
      * in <code>web.xml</code>. The following <code>&lt;init-param></code> elements
      * are supported:
      * <ul>
      * <li><code>queueName</code> - the default task queue name, which must also
      * be configured within <code>queue.xml</code>; if not specified, the default
      * queue name is "deferred"</li>
      * <li><code>taskUrl</code> - the URL used to invoke the task, which must be
      * configured within <code>web.xml</code> as the <code>&lt;url-pattern></code>
      * within a <code>&lt;servlet-mapping></code> for the {@link Deferred}
      * servlet.</li>
      * </ul>
      */
     @Override
     public void init() {
         queueName = getInitParameter( QUEUE_NAME_INIT_PARAM );
         if ( ( queueName == null ) || ( queueName.length() == 0 ) ) {
             queueName = DEFAULT_QUEUE_NAME;
         }
         taskUrl = getInitParameter( TASK_URL_INIT_PARAM );
     }
     
     /**
      * Queues a task for background execution using the configured or default
      * queue name and the configured or default task URL.
      * 
      * <p>If the queue name is not configured via the <code>queueName</code>
      * init parameter, uses "deferred" as the queue name.
      * 
      * <p>If the task URL is not configured via the <code>taskUrl</code> init
      * parameter, uses the default task URL, which takes the form:
      * <blockquote>
      * <code>/_ah/queue/<i>&lt;queue name></i></code>
      * </blockquote>
      * 
      * @param task The task to be executed.
      * @throws QueueFailureException If an error occurs serializing the task.
      * @return A {@link TaskHandle} for the queued task.
      */
     public static TaskHandle defer( Deferrable task ) {
         return defer( task, queueName );
     }
     
     /**
      * Queues a task for background execution using the specified queue name and
      * the configured or default task URL.
      * 
      * <p>If the task URL is not configured via the <code>taskUrl</code> init
      * parameter, uses the default task URL, which takes the form:
      * <blockquote>
      * <code>/_ah/queue/<i>&lt;queue name></i></code>
      * </blockquote>
      * 
      * @param task The task to be executed.
      * @param queueName The name of the queue.
      * @throws QueueFailureException If an error occurs serializing the task.
      * @return A {@link TaskHandle} for the queued task.
      */
     public static TaskHandle defer( Deferrable task, String queueName ) {
         return defer( task, queueName, taskUrl != null ? url( taskUrl ) : withDefaults() );
     }
     
     /**
      * Queues a task for background execution using the configured or default
      * queue name and the specified task options (including the specified task
      * URL).
      * 
      * <p>If the queue name is not configured via the <code>queueName</code>
      * init parameter, uses "deferred" as the queue name.
      * 
      * <p>If the task URL is not specified in the task options, the
      * default task URL is used, even if a task URL is configured via the
      * <code>taskUrl</code> init parameter. The default task URL takes the form:
      * <blockquote>
      * <code>/_ah/queue/<i>&lt;queue name></i></code>
      * </blockquote>
      * 
      * <p>The following task options may be specified:
      * <ul>
      * <li><code>countdownMillis</code></li>
      * <li><code>etaMillis</code></li>
      * <li><code>taskName</code></li>
      * <li><code>url</code></li>
      * </ul>
      * 
      * <p>The following task options are ignored:
      * <ul>
      * <li><code>header</code></li>
      * <li><code>headers</code></li>
      * <li><code>method</code></li>
      * <li><code>payload</code></li>
      * </ul>
      * 
      * <p>The following task options will throw an {@link IllegalArgumentException}
      * if specified:
      * <ul>
      * <li><code>param</code></li>
      * </ul>
      * 
      * @param task The task to be executed.
      * @param taskOptions The task options.
      * @throws QueueFailureException If an error occurs serializing the task.
      * @throws IllegalArgumentException If any <code>param</code> task options
      * are specified.
      * @return A {@link TaskHandle} for the queued task.
      */
     public static TaskHandle defer( Deferrable task, TaskOptions taskOptions ) {
         return defer( task, queueName, taskOptions );
     }
     
     /**
      * Queue a task for background execution using the specified queue name and
      * the specified task options (including the specified task URL).
      * 
      * <p>If the task URL is not specified in the task options, the
      * default task URL is used, even if a task URL is configured via the
      * <code>taskUrl</code> init parameter. The default task URL takes the form:
      * <blockquote>
      * <code>/_ah/queue/<i>&lt;queue name></i></code>
      * </blockquote>
      * 
      * <p>The following task options may be specified:
      * <ul>
      * <li><code>countdownMillis</code></li>
      * <li><code>etaMillis</code></li>
      * <li><code>taskName</code></li>
      * <li><code>url</code></li>
      * </ul>
      * 
      * <p>The following task options are ignored:
      * <ul>
      * <li><code>header</code></li>
      * <li><code>headers</code></li>
      * <li><code>method</code></li>
      * <li><code>payload</code></li>
      * </ul>
      * 
      * <p>The following task options will throw an {@link IllegalArgumentException}
      * if specified:
      * <ul>
      * <li><code>param</code></li>
      * </ul>
      * 
      * @param task The task to be executed.
      * @param taskOptions The task options.
      * @throws QueueFailureException If an error occurs serializing the task.
      * @throws IllegalArgumentException If any <code>param</code> task options
      * are specified.
      * @return A {@link TaskHandle} for the queued task.
      */
     public static TaskHandle defer( Deferrable task, String queueName, TaskOptions taskOptions ) {
         // See issue #2461 (http://code.google.com/p/googleappengine/issues/detail?id=2461).
         // If this issue is ever resolved, the params should be removed from the TaskOptions.
         byte[] taskBytes = serialize( task );
         if ( taskBytes.length <= maxTaskSizeBytes() ) {
             try {
                 return queueTask( taskBytes, queueName, taskOptions );
             } catch ( IllegalArgumentException e ) {
                 log.warning( e.getMessage() + ": " + taskBytes.length );
                 // task size too large, fall through
             }
         }
         // create a datastore entity and add its key as the task payload
         Entity entity = new Entity( ENTITY_KIND );
         entity.setProperty( TASK_PROPERTY, new Blob( taskBytes ) );
         Key key = getDatastoreService().put( entity );
         log.info( "put datastore key: " + key );
         try {
             return queueTask( serialize( key ), queueName, taskOptions );
         } catch ( RuntimeException e ) {
             deleteEntity( key ); // delete entity if error queuing task
             throw e;
         }
     }
 
     /**
      * Add a task to the queue.
      * 
      * @param taskBytes The task payload.
      * @param queueName The queue name.
      * @param taskOptions The task options.
      * @return
      */
     private static TaskHandle queueTask( byte[] taskBytes, String queueName,
             TaskOptions taskOptions ) {
         return getQueue( queueName ).add( taskOptions.method( POST ).payload(
                                                 taskBytes, TASK_CONTENT_TYPE ) );
     }
     
     /**
      * Executes a background task.
      * 
      * The task payload is either type Deferrable or Key; in the latter case,
      * retrieve (then delete) the Deferrable instance from the datastore.
      */
     @Override
     public void doPost( HttpServletRequest req, HttpServletResponse res )
             throws ServletException, IOException {
         try {
             Object payload = deserialize( req );
             if ( payload instanceof Key ) {
                 // get Deferrable from datastore
                 Blob taskBlob = (Blob)getDatastoreService().get(
                                     (Key)payload ).getProperty( TASK_PROPERTY );
                 deleteEntity( (Key)payload );
                 if ( taskBlob != null ) {
                     payload = deserialize( taskBlob.getBytes() );
                 }
             }
             if ( payload instanceof Deferrable ) {
                 ((Deferrable)payload).doTask();
             } else if ( payload != null ) {
                 log.severe( "invalid payload type: " + payload.getClass().getName() );
                 // don't retry task
             }
         } catch ( EntityNotFoundException e ) {
             log.severe( e.toString() ); // don't retry task
         } catch ( PermanentTaskFailure e ) {
             log.severe( e.toString() ); // don't retry task
         } 
     }
     
     /**
      * Delete a datastore entity.
      * 
      * @param key The key of the entity to delete.
      */
     private static void deleteEntity( Key key ) {
         try {
             getDatastoreService().delete( key );
             log.info( "deleted datastore key: " + key );
         } catch ( DatastoreFailureException e ) {
             log.warning( "failed to delete datastore key: " + key );
             log.warning( e.toString() );
         }
     }
     
     /**
      * Serialize an object into a byte array.
      * 
      * @param obj An object to be serialized.
      * @return A byte array containing the serialized object
      * @throws QueueFailureException If an I/O error occurs during the
      * serialization process.
      */
     private static byte[] serialize( Object obj ) {
         try {
             ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
             ObjectOutputStream objectOut = new ObjectOutputStream( 
                                                 new BufferedOutputStream( bytesOut ) );
             objectOut.writeObject( obj );
             objectOut.close();
             if ( isDevelopment() ) { // workaround for issue #2097
                 return encodeBase64( bytesOut.toByteArray() );
             }
             return bytesOut.toByteArray();
         } catch ( IOException e ) {
             throw new QueueFailureException( e );
         }
     }
     
     /**
      * Deserialize an object from an HttpServletRequest input stream. Does not
      * throw any exceptions; instead, exceptions are logged and null is returned.
      * 
      * @param req An HttpServletRequest that contains a serialized object.
      * @return An object instance, or null if an exception occurred.
      */
     private static Object deserialize( HttpServletRequest req ) {
         if ( req.getContentLength() == 0 ) {
             log.severe( "request content length is 0" );
             return null;
         }
         try {
             byte[] bytesIn = new byte[ req.getContentLength() ];
             req.getInputStream().readLine( bytesIn, 0, bytesIn.length );
             return deserialize( bytesIn );
         } catch ( IOException e ) {
             log.log( Level.SEVERE, "Error deserializing task", e );
             return null; // don't retry task
         }
     }
 
     /**
      * Deserialize an object from a byte array. Does not throw any exceptions;
      * instead, exceptions are logged and null is returned.
      * 
      * @param bytesIn A byte array containing a previously serialized object.
      * @return An object instance, or null if an exception occurred.
      */
     private static Object deserialize( byte[] bytesIn ) {
         ObjectInputStream objectIn = null;
         try {
             if ( isDevelopment() ) { // workaround for issue #2097
                 bytesIn = decodeBase64( bytesIn );
             }
             objectIn = new ObjectInputStream( new BufferedInputStream(
                                         new ByteArrayInputStream( bytesIn ) ) );
             return objectIn.readObject();
         } catch ( Exception e ) {
             log.log( Level.SEVERE, "Error deserializing task", e );
             return null; // don't retry task
         } finally {
             try {
                 if ( objectIn != null ) {
                     objectIn.close();
                 }
             } catch ( IOException ignore ) {
             }
         }
     }
     
     private static boolean isDevelopment() {
    	return true;
//        return ( SystemProperty.environment.value() ==
//                    SystemProperty.Environment.Value.Development );
     }
 }
