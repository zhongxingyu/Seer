 package sofia.util;
 
 import java.lang.ref.WeakReference;
 
 import sofia.app.Screen;
 import sofia.app.internal.LifecycleInjection;
 import sofia.app.internal.ScreenMixin;
 import sofia.internal.events.EventDispatcher;
 import android.content.Context;
 import android.os.Handler;
 import android.os.Looper;
 
 //-------------------------------------------------------------------------
 /**
  * <p>
  * This class provides a very simple API for calling methods either once or
  * repeatedly after a delay. Instead of creating objects of this class using
  * the {@code new} keyword, you can use the following static "factory" methods
  * to queue up delayed method calls in the background:
  * </p>
  * <dl>
  * <dt>{@code callOnce(Object receiver, String methodName, long delay)}</dt>
  * <dd>Requests that an object's method be called once after a number of
  * milliseconds has passed.</dd>
  * <dt>{@code callRepeatedly(Object receiver, String methodName,
  * long delay)}</dt>
  * <dd>Requests that an object's method be called repeatedly after a number of
  * milliseconds has passed.</dd>
  * <dt>{@code callRepeatedly(Object receiver, String methodName,
  * long initialDelay, long repeatDelay)}</dt>
  * <dd>Requests that an object's method be called repeatedly, with separate
  * controls over the initial delay (before the first call) and the repeat
  * delay (between subsequent calls).</dd>
  * </dl>
  * <p>
  * Each of these methods returns a {@code Timer} object, which can be stored
  * and used later if necessary. For example, you may want to keep the timer
  * in a field so that you can stop the repetition in response to a user action
  * (like a button click) or some other external event.
  * </p>
  *
  * <h3>Methods the timer can call</h3>
  * <p>
  * The method whose name is passed to these factory methods must be public,
  * take no arguments, and return either {@code void} or {@code boolean}. Be
  * careful: if a method with the correct name is found but the arguments do not
  * match, then no error will be produced at runtime, and the method will simply
  * not be called.
  * </p><p>
  * The version of the method that returns {@code boolean} is useful for timers
  * created by {@code callRepeatedly}. In this case, returning {@code true} will
  * cause the timer to stop (and the method will not be called again), or
  * returning {@code true} will cause the timer to continue processing and fire
  * again after the delay. If the method returns {@code void} instead, then the
  * timer will run indefinitely, until it is stopped by calling {@link #stop()}.
  * </p>
  *
  * <h3>Automatic lifecycle management</h3>
  * <p>
  * If the <strong>receiver</strong> for the timed method call is a
  * {@link Screen}, then the timer will be automatically managed as part of the
  * screen's lifecycle; the timer will be paused and resumed automatically
  * during the screen's {@code onPause} and {@code onResume} methods,
  * respectively.
  * </p><p>
  * If the method receiver is another object, then you will manually pause and
  * resume the timer. Backing out of an activity or leaving the application does
  * <strong>not</strong> automatically stop the timer, in most cases.
  * </p>
  *
  * <h3>Threading concerns</h3>
  * <p>
  * Timed methods are always called on the main GUI thread, regardless of which
  * thread they are started on. This is to avoid problems when users might want
  * to start timed method calls on the physics thread (for example, in reaction
  * to a collision between shapes).
  * </p>
  *
  * @author  Tony Allevato
  * @author  Last changed by $Author$
  * @version $Revision$, $Date$
  */
 public class Timer
 {
     //~ Fields ................................................................
 
     private WeakReference<Object> receiverRef;
     private long initialDelay;
     private long repeatDelay;
 
     private Handler handler;
     private long startTime;
     private long lastPostTime;
     private EventDispatcher timerFired;
 
 
     //~ Constructors ..........................................................
 
     // ----------------------------------------------------------
     private Timer(Object receiver, EventDispatcher event, long initialDelay,
             long repeatDelay)
     {
         this.receiverRef = new WeakReference<Object>(receiver);
         this.timerFired = event;
         this.initialDelay = initialDelay;
         this.repeatDelay = repeatDelay;
 
         handler = new Handler(Looper.getMainLooper());
         startTime = 0;
         lastPostTime = 0;
 
         if (receiver instanceof Context)
         {
             ScreenMixin.tryToAddLifecycleInjection((Context) receiver,
                     lifecycleInjection);
         }
     }
 
 
     //~ Factory methods .......................................................
 
     // ----------------------------------------------------------
     /**
      * Calls a method once after the specified delay.
      *
      * @param receiver the object on which the method will be called
      * @param methodName the name of the method to call
      * @param delay the delay, in milliseconds
      * @return the {@link Timer} object, which you can use to execute finer
      *     grained control over the timer if needed
      */
     public static Timer callOnce(
             Object receiver, String methodName, long delay)
     {
         Timer timer = new Timer(receiver,
                 new EventDispatcher(methodName), delay, 0);
         timer.start();
         return timer;
     }
 
 
     // ----------------------------------------------------------
     /**
      * Calls a method repeatedly, waiting the specified amount of time between
      * each call (and before the first call).
      *
      * @param receiver the object on which the method will be called
      * @param methodName the name of the method to call
      * @param delay the delay, in milliseconds, before the first call and
      *     between subsequent calls
      * @return the {@link Timer} object, which you can use to execute finer
      *     grained control over the timer if needed
      */
     public static Timer callRepeatedly(
             Object receiver, String methodName, long delay)
     {
         return callRepeatedly(receiver, methodName, delay, delay);
     }
 
 
     // ----------------------------------------------------------
     /**
      * Calls a method repeatedly, providing separate control over the initial
      * delay (time before the first call) and the repetition delay (time
      * between subsequent calls).
      *
      * @param receiver the object on which the method will be called
      * @param methodName the name of the method to call
      * @param initialDelay the delay, in milliseconds, before the first call
      * @param repeatDelay the delay, in milliseconds, between subsequent calls
      *     to the method
      * @return the {@link Timer} object, which you can use to execute finer
      *     grained control over the timer if needed
      */
     public static Timer callRepeatedly(
             Object receiver, String methodName, long initialDelay,
             long repeatDelay)
     {
         Timer timer = new Timer(
                 receiver, new EventDispatcher(methodName),
                 initialDelay, repeatDelay);
         timer.start();
         return timer;
     }
 
 
     //~ Public methods ........................................................
 
     // ----------------------------------------------------------
     /**
      * <p>
      * Starts the timer if it has been previously stopped.
      * </p><p>
      * Calling one of the factory methods
      * ({@link #callDelayed(Object, String, long)},
      * {@link #callRepeatedly(Object, String, long)}, or
      * {@link #callRepeatedly(Object, String, long, long)} will create a timer
      * that is started immediately, so you do not need to call the
      * {@code start} method on it. This method exists so that you can restart
      * a timer that you have previously called {@link #stop()} on.
      * </p>
      */
     public void start()
     {
         if (startTime == 0)
         {
             stop();
             startTime = System.currentTimeMillis();
             post(initialDelay);
         }
     }
 
 
     // ----------------------------------------------------------
     /**
      * Stops the timer, preventing it from firing in the future. To restart the
      * timer, call its {@link #start()} method.
      */
     public void stop()
     {
         handler.removeCallbacks(timerTask);
         startTime = 0;
     }
 
 
     // ----------------------------------------------------------
     /**
      * Gets a value indicating whether or not the timer is currently running.
      *
      * @return true if the timer is running, or false if it is not
      */
     public boolean isRunning()
     {
         return startTime != 0;
     }
 
 
     //~ Private methods .......................................................
 
     // ----------------------------------------------------------
     private void post(long delay)
     {
         lastPostTime = System.currentTimeMillis();
         handler.postDelayed(timerTask, delay);
     }
 
 
     //~ Inner classes .........................................................
 
     // ----------------------------------------------------------
     /**
      * The runnable that is posted to the handler's event queue.
      */
     private final Runnable timerTask = new Runnable()
     {
         // ----------------------------------------------------------
         @Override
         public void run()
         {
             // Subtract the actual time taken by the method execution from the
             // following delay so that method calls remain fairly synchronized
             // with respect to each other.
 
             long startTime = System.currentTimeMillis();
 
             Object receiver = receiverRef.get();
 
             if (receiver != null)
             {
                 boolean stop = timerFired.dispatch(receiver);
 
                // The ideal way to stop a repeating timer is to return true
                // from the handler method, but some users might call stop()
                // inside the handler instead (especially if it's a void
                // method). The check startTime != 0 makes this work correctly
                // as well.

                if (!stop && startTime != 0 && repeatDelay > 0)
                 {
                     long realDelay = repeatDelay
                             - (System.currentTimeMillis() - startTime);
 
                     post(realDelay);
                 }
             }
         }
     };
 
 
     // ----------------------------------------------------------
     /**
      * Hooks into the screen's lifecycle to pause and resume the timer if the
      * method receiver is a {@code Screen}.
      */
     private LifecycleInjection lifecycleInjection = new LifecycleInjection()
     {
         //~ Fields ............................................................
 
         private long timeRemaining;
 
 
         //~ Public methods ....................................................
 
         // ----------------------------------------------------------
         @Override
         public void pause()
         {
             if (isRunning())
             {
                 handler.removeCallbacks(timerTask);
                 timeRemaining = System.currentTimeMillis() - lastPostTime;
             }
         }
 
 
         // ----------------------------------------------------------
         @Override
         public void resume()
         {
             if (timeRemaining != 0)
             {
                 post(timeRemaining);
             }
         }
     };
 }
