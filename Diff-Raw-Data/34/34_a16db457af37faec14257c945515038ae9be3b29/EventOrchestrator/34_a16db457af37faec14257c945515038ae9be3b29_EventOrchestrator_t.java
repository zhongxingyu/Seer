 package org.synyx.minos.core.web.event;
 
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.aspectj.lang.ProceedingJoinPoint;
 import org.aspectj.lang.annotation.Around;
 import org.aspectj.lang.annotation.Aspect;
 import org.aspectj.lang.annotation.Pointcut;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.servlet.ModelAndView;
 import org.synyx.minos.core.Core;
 
 
 
 /**
  * Aspect to decorate {@code Controller} and
  * {@code AnnotationMethodHandlerAdapter} invocations with an event handling
  * mechanism. Thus controllers can simply put instances of {@code Event} under
  * an event key into the model. These will be picked up by the orchestrator and
  * handed to registered {@code EventHandler}s.
  * <p>
  * If you declare an {@code OrchestrationPostProcessor} declarations of
  * {@code EventHandler}s will automatically be registered.
  * <p>
  * Default keys:
  * <ul>
  * <li>Event: {@value #DEFAULT_EVENT_KEY}</li>
  * <li>Event context: {@value #DEFAULT_EVENT_CONTEXT_KEY}</li>
  * </ul>
  * 
  * @author Oliver Gierke - gierke@synyx.de
  */
 @Aspect
 public class EventOrchestrator {
 
     private static final String EVENT_CONTEXT_KEY = Core.EVENT_KEY
             + ".context";
 
    private List<EventHandler<Event>> eventHandlers;
 
 
     /**
      * Sets all event h safaandlers the orchestrator shall be aware of. If you
      * set up event handling via {@code OrchestrationPostProcessor} this will be
      * automatically populated.
      * 
      * @param eventHandlers the eventHandlers to set
      */
     @Required
    public void setEventHandlers(List<EventHandler<Event>> eventHandlers) {
 
         this.eventHandlers = eventHandlers;
     }
 
 
     /**
      * Pointcut matching all {@code Controller} invocations. This allows
      * decoration of the legacy Spring WebMVC programming model.
      */
     @Pointcut("execution(* org.springframework.web.servlet.mvc.Controller.handleRequest(..))")
     @SuppressWarnings("unused")
     private void controllerInvocation() {
 
     }
 
 
     /**
      * Pointcut matching invocations of {@code AnnotationMethodHandlerAdapter}.
      * This is the equivalent to
      */
     @Pointcut("execution(* org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter.handle(..))")
     @SuppressWarnings("unused")
     private void annotationMethodHandlerInvocation() {
 
     }
 
 
     /**
      * Central orchestrator method.
      * 
      * @param joinPoint
      * @param request
      * @param response
      * @param controller
      * @return
      * @throws Throwable
      */
     @Around("(controllerInvocation() || annotationMethodHandlerInvocation()) && args(request, response) && this(controller)")
     public ModelAndView orchestrate(ProceedingJoinPoint joinPoint,
             HttpServletRequest request, HttpServletResponse response,
             Object controller) throws Throwable {
 
         // Pre execution
 
         // Invoke controller
         ModelAndView mav = (ModelAndView) joinPoint.proceed();
 
         ModelMap map = mav.getModelMap();
 
         // Event fired?
         if (map.containsAttribute(Core.EVENT_KEY)) {
 
             // Try to lookup context and add current controller ass "visited"
             EventContext context = retrieveEventContext(request, response, mav);
             context.visited(controller);
 
             // Retrieve event
             Event event = (Event) map.get(Core.EVENT_KEY);
 
            for (EventHandler<Event> handler : eventHandlers) {
 
                 // Hand execution to handler if she is intrested in the event
                 if (handler.supports(event.getClass())) {
                     handler.handleEvent(event, context);
                 }
             }
         }
 
         return mav;
     }
 
 
     /**
      * Retrieves the current event context from the request or creates a new
      * one.
      * 
      * @param request
      * @param response
      * @param mav
      * @return
      */
     public EventContext retrieveEventContext(HttpServletRequest request,
             HttpServletResponse response, ModelAndView mav) {
 
         EventContext context = null;
 
         if (null != request.getAttribute(EVENT_CONTEXT_KEY)) {
             context = (EventContext) request.getAttribute(EVENT_CONTEXT_KEY);
             return context;
         }
 
         context = new EventContext(request, response, mav);
         request.setAttribute(EVENT_CONTEXT_KEY, context);
 
         return context;
     }
 }
