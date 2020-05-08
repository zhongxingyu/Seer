 package org.synyx.minos.core.web.event;
 
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.Controller;
 
 
 
 /**
  * Base event handler. Invokes callback method
  * {@code AbstractEventHandler#prepareEvent(Event, EventContext)} before
  * triggering a controller with its full lifecycle.
  * <p>
  * <em>
  * Subclasses should be annotated with {@code ModuleDependent} as the
  * implementation is typesafe and thus would refer possibly not available
  * classes from other modules.</em>
  * 
  * @author Oliver Gierke - gierke@synyx.de
  */
 public abstract class AbstractEventHandler<T extends Event> implements
         EventHandler<T> {
 
     private Class<T> supportedEvent;
 
     private Controller controller;
 
 
     /**
      * Sets the event class, the handler is intrested in.
      * 
      * @param event the event to set
      */
     @Required
     public void setSupportedEvent(Class<T> event) {
 
         this.supportedEvent = event;
     }
 
 
     /**
      * Sets the controller, the handler will execute.
      * 
      * @param controller the controller to set
      */
     @Required
     public void setController(Controller controller) {
 
         this.controller = controller;
     }
 
 
     /*
      * (non-Javadoc)
      * 
      * @see com.synyx.minos.core.web.support.EventHandlerInterface#handleEvent(com.synyx.minos.core.web.support.EvenContext)
      */
     public void handleEvent(T event, EventContext context) throws Exception {
 
         // Prepare duplicate execution
         if (context.alreadyVisited(controller)) {
             return;
         }
 
         prepareEvent(event, context);
 
         ModelAndView mav = controller.handleRequest(context.getRequest(),
                 context.getResponse());
 
         context.getModelAndView().addAllObjects(mav.getModelMap());
     }
 
 
     /**
      * Callback method to prepare execution of the controller. Override this
      * method to prepare the request according to the event that was thrown and
      * the controller to be executed.
      * 
      * @param event
      * @param context
      */
     protected void prepareEvent(T event, EventContext context) {
 
         return;
     }
 
 
     /*
      * (non-Javadoc)
      * 
      * @see com.synyx.minos.core.web.support.EventHandlerInterface#supports(java.lang.Object)
      */
    public boolean supports(Class<T> eventClass) {
 
         return eventClass.isAssignableFrom(supportedEvent);
     }
 
 
     /*
      * (non-Javadoc)
      * 
      * @see java.lang.Object#toString()
      */
     @Override
     public String toString() {
 
         return "SimpleEventHandler: triggering " + controller.toString()
                 + " on events of type " + supportedEvent.getName();
     }
 }
