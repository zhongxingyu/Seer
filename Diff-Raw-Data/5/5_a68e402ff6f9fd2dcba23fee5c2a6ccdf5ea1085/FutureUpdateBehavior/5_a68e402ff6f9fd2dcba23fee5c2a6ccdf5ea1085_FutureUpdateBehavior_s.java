 package pl.psnc.dl.wf4ever.portal.behaviors;
 
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 
 import org.apache.log4j.Logger;
 import org.apache.wicket.Component;
 import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.util.time.Duration;
 
 /**
  * An Ajax timer behavior that polls a future waiting for it to be done and once it is updates the component model,
  * stops the timer and calls a callback function.
  * 
  * Based on https://gist.github.com/jonnywray/636875.
  * 
  * @param <T>
  * @author Jonny Wray
  * @author Piotr Hołubowicz
  */
 public class FutureUpdateBehavior<T> extends AbstractAjaxTimerBehavior {
 
     /** id. */
     private static final long serialVersionUID = 1293362922535868808L;
     /** Logger. */
     private static final Logger LOGGER = Logger.getLogger(FutureUpdateBehavior.class);
 
     /** The job that will finish in some time. */
     private IModel<Future<T>> future;
 
     /** The model to save the job result to. */
     private IModel<T> model;
     private Component[] components;
 
 
     /**
      * Constructor.
      * 
      * @param updateInterval
      *            the interval between AJAX polls
      * @param future
      *            The job that will finish in some time
      * @param model
      *            The model to save the job result to
      */
     public FutureUpdateBehavior(Duration updateInterval, IModel<Future<T>> future, IModel<T> model,
             Component... components) {
         super(updateInterval);
         this.model = model;
         this.future = future;
         this.components = components;
     }
 
 
     /**
      * The job has finished successfully. The default implementation posts an event.
      * 
      * @param target
      *            AJAX target
      */
     protected void onPostSuccess(AjaxRequestTarget target) {
         for (Component component : components) {
             target.add(component);
         }
     }
 
 
     /**
      * The job has thrown an exception or was interrupted.
      * 
      * @param target
      *            AJAX target
      * @param e
      *            the exception
      */
     protected void onUpdateError(AjaxRequestTarget target, Exception e) {
         getComponent().error("Could not finish the task: " + e.getLocalizedMessage());
         target.add(getComponent());
         LOGGER.error("Could not finish the task", e);
 
     }
 
 
     @Override
     protected void onTimer(final AjaxRequestTarget target) {
        if (future.getObject().isDone()) {
             try {
                 T data = future.getObject().get();
                 if (model != null) {
                     model.setObject(data);
                 }
                 stop();
                 onPostSuccess(target);
             } catch (InterruptedException | ExecutionException e) {
                 stop();
                 String message = "Error occurred while fetching data: " + e.getMessage();
                 LOGGER.error(message, e);
                 onUpdateError(target, e);
             }
         }
     }
 
 
     @Override
     public CharSequence getCallbackScript() {
         return super.getCallbackScript();
     }
 
 }
