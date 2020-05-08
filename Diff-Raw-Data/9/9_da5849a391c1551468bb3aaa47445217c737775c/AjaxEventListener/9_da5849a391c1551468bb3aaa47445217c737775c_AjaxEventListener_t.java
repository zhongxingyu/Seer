 package de.flower.common.ui.ajax.event;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.behavior.Behavior;
 import org.apache.wicket.event.IEvent;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Attach this behavior to any component that wants to be added to an ajaxRequestTarget on certain events.
  */
 public final class AjaxEventListener extends Behavior {
 
     private final static Logger log = LoggerFactory.getLogger(AjaxEventListener.class);
 
     /**
      * Events that this behavior listens for.
      */
     private final Object[] registeredEvents;
 
     /**
      * Instantiates a new ajax update behavior.
      *
      * @param eventObjects event (or event list) that the component wants to listen on. Events are matched by using {@link #equals(Object)}.
      */
     public AjaxEventListener(final Object... registeredEvents) {
         this.registeredEvents = registeredEvents;
     }
 
     /**
      * Called immediately after attaching the behavior. Helps to make the constructor leaner.
      *
      * @param component the component
      */
     @Override
     public void bind(final Component component) {
         // ajax updates always require that the component has markupId set to true.
         component.setOutputMarkupId(true);
     }
 
     @Override
     public void onEvent(final Component component, final IEvent<?> event) {
         super.onEvent(component, event);
         final Object payload = event.getPayload();
         if (!isAjaxEventPayload(payload)) {
             return;
         }
         if (checkForComponentUpdate(payload)) {
             // add to ajax request target
             final AjaxRequestTarget target = AjaxRequestTarget.get();
             if (target != null) {
                // detach models so that component loads fresh data when it is repainted.
                component.detach();
                 // components that are not visible and don't have outputmarkupplaceholder cannot be
                 // updated.
                 if (component.isVisibleInHierarchy()
                         || (component.getParent().isVisibleInHierarchy() && component.getOutputMarkupPlaceholderTag())) {
                     target.add(component);
                 } else {
                    // downgrad log level to info to avoid RMT-720.
                    log.info("Cannot add component [{}] to ajax request target. Component or parent is not visible.", component);
                 }
             }
         }
     }
 
     // NOTE (oliverb - 13.10.2011): implement this method if typed payloads are to be used.
     private boolean isAjaxEventPayload(final Object payload) {
         // return (payload instanceof AjaxEvent);
         return true;
     }
 
     /**
      * Determine if component needs to be updated (or added to ajaxRequestTarget).
      * Does any of the event objects match one of this.registeredEvents?
      *
      * @param triggeredEvent the triggered event
      * @return true, if component should be added to ajax request target.
      */
     // NOTE (oliverb - 13.10.2011): think about strongly typing the payload, e.g. as an AjaxEvent.
     public boolean checkForComponentUpdate(final Object triggeredEvent) {
         for (final Object registeredEvent : registeredEvents) {
             if (registeredEvent.equals(triggeredEvent)) {
                 return true;
             }
         }
         return false;
     }
 }
