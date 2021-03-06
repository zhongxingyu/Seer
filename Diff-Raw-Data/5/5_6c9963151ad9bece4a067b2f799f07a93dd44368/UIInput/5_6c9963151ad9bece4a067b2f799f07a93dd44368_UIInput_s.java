 /*
 * $Id: UIInput.java,v 1.10 2003/01/22 00:38:41 craigmcc Exp $
  */
 
 /*
  * Copyright 2002-2003 Sun Microsystems, Inc. All rights reserved.
  * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
  */
 
 package javax.faces.component;
 
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import javax.faces.context.FacesContext;
 import javax.faces.context.ResponseWriter;
 import javax.faces.event.AbortProcessingException;
 import javax.faces.event.FacesEvent;
 import javax.faces.event.PhaseId;
 import javax.faces.event.ValueChangedEvent;
 import javax.faces.event.ValueChangedListener;
 
 /**
  * <p><strong>UIInput</strong> is a {@link UIComponent} that represents
  * a component that both displays output to the user (like
  * {@link UIOutput} components do) and includes request parameters on the
  * subsequent request that need to be decoded.  There are no restrictions
  * on the data type of the local value, or the object referenced by the
  * model reference expression (if any); however, individual
  * <code>Renderer</code>s will generally impose restrictions on the type
  * of data they know how to display.</p>
  *
  * <p>When the <code>validate()</code> method of this {@link UIInput}
  * detects that a value change has actually occurred, and that all validations
  * have been successfully passed, it will queue a
  * {@link ValueChangedEvent}.  Later on, the <code>broadcast()</code>
  * method will ensure that this event is broadcast to all interested
  * listeners.</p>
  */
 
 public class UIInput extends UIComponentBase {
 
 
     // ------------------------------------------------------- Static Variables
 
 
     /**
      * The component type of this {@link UIComponent} subclass.
      */
     public static final String TYPE = "javax.faces.component.UIInput";
 
 
     // ----------------------------------------------------- Instance Variables
 
 
     /**
      * <p>The previous local value of this {@link UIInput}, saved during
      * <code>decode()</code> processing and used during <code>validate()</code>
      * processing to determine whether a {@link ValueChangedEvent} should
      * be queued.</p>
      */
     protected transient Object previous = null;
 
 
     // ------------------------------------------------------------- Properties
 
 
     /**
      * <p>Return the component type of this <code>UIComponent</code>.</p>
      */
     public String getComponentType() {
 
         return (TYPE);
 
     }
 
 
     // ------------------------------------------- Lifecycle Processing Methods
 
 
     /**
      * <p>Decode the new value of this component from the incoming request.</p>
      *
      * @param context FacesContext for the request we are processing
      *
      * @exception IOException if an input/output error occurs while reading
      * @exception NullPointerException if <code>context</code>
      *  is <code>null</code>
      */
     public void decode(FacesContext context) throws IOException {
 
         if (context == null) {
             throw new NullPointerException();
         }
 
         // Delegate to our associated Renderer if needed
        previous = getValue();
         if (getRendererType() != null) {
             super.decode(context);
             return;
         }
 
         // Perform the default decoding
         String newValue =
             context.getServletRequest().getParameter(getClientId(context));
         setValue(newValue);
         setValid(true);
 
     }
 
 
     /**
      * <p>Render the current value of this component if the value 
      * of the rendered attribute is <code>true</code>. </p>
      *
      * @param context FacesContext for the response we are creating
      *
      * @exception IOException if an input/output error occurs while rendering
      * @exception NullPointerException if <code>context</code>
      *  is <code>null</code>
      */
     public void encodeEnd(FacesContext context) throws IOException {
 
         if (context == null) {
             throw new NullPointerException();
         }
 
         // Delegate to our associated Renderer if needed
         if (getRendererType() != null) {
             super.encodeEnd(context);
             return;
         }
 
         // if rendered is false, do not perform default encoding.
         if (!isRendered()) {
             return;
         }
 
         // Perform the default encoding
         Object value = currentValue(context);
         ResponseWriter writer = context.getResponseWriter();
         writer.write("<input type=\"text\" name=\"");
         writer.write(getClientId(context));
         writer.write("\" value=\"");
         if (value != null) {
             writer.write(value.toString());
         }
         writer.write("\">");
 
     }
 
 
     /**
      * <p>Perform validations and, if validation is successful, queue
      * a {@link ValueChangedEvent} to be processed later.</p>
      *
      * @param context The {@link FacesContext} for the current request
      *
      * @exception NullPointerException if <code>context</code>
      *  is null
      */
     public void validate(FacesContext context) {
 
         if (context == null) {
             throw new NullPointerException();
         }
 
         // Save and reset the previous value for this component
         Object previous = this.previous;
         this.previous = null;
 
         // Determine whether a value change has actually occurred
         Object value = getValue();
         boolean changed;
         if (previous == value) {
             changed = false;
         } else if (previous == null) {
             changed = (value != null);
         } else /* if (previous != null) */ {
             if (value == null) {
                 changed = false;
             } else if (previous == value) {
                 changed = false;
             } else {
                 changed = !previous.equals(value);
             }
         }
 
         // Queue a ValueChangedEvent if appropriate
         if (changed) {
             fireValueChangedEvent(context, previous, value);
         }
 
     }
 
 
     // ----------------------------------------------- Event Processing Methods
 
 
     /**
      * <p>Array of {@link List}s of {@link ValueChangedListener}s registered
      * for particular phases.  The array, as well as the individual
      * elements, are lazily instantiated as necessary.</p>
      */
     protected List listeners[] = null;
 
 
     /**
      * <p>Add a new {@link ValueChangedListener} to the set of listeners
      * interested in being notified when {@link ValueChangedEvent}s occur.</p>
      *
      * @param listener The {@link ValueChangedListener} to be added
      *
      * @exception NullPointerException if <code>listener</code>
      *  is <code>null</code>
      */
     public void addValueChangedListener(ValueChangedListener listener) {
 
         if (listener == null) {
             throw new NullPointerException();
         }
         if (listeners == null) {
             listeners = new List[PhaseId.VALUES.size()];
         }
         int ordinal = listener.getPhaseId().getOrdinal();
         if (listeners[ordinal] == null) {
             listeners[ordinal] = new ArrayList();
         }
         listeners[ordinal].add(listener);
 
     }
 
 
     /**
      * <p>Broadcast the specified {@link FacesEvent} to all registered
      * event listeners who have expressed an interest in events of this
      * type, for the specified {@link PhaseId}.  The order in which
      * registered listeners are notified is implementation dependent.</p>
      *
      * <p>After all interested listeners have been notified, return
      * <code>false</code> if this event does not have any listeners
      * interested in this event in future phases of the request processing
      * lifecycle.  Otherwise, return <code>true</code>.</p>
      *
      * @param event The {@link FacesEvent} to be broadcast
      * @param phaseId The {@link PhaseId} of the current phase of the
      *  request processing lifecycle
      *
      * @exception AbortProcessingException Signal the JavaServer Faces
      *  implementation that no further processing on the current event
      *  should be performed
      * @exception IllegalArgumentException if the implementation class
      *  of this {@link FacesEvent} is not supported by this component
      * @exception IllegalStateException if PhaseId.ANY_PHASE is passed
      *  for the phase identifier
      * @exception NullPointerException if <code>event</code> or
      *  <code>phaseId</code> is <code>null</code>
      */
     public boolean broadcast(FacesEvent event, PhaseId phaseId)
         throws AbortProcessingException {
 
         if ((event == null) || (phaseId == null)) {
             throw new NullPointerException();
         }
         if (phaseId.equals(PhaseId.ANY_PHASE)) {
             throw new IllegalStateException();
         }
         if (event instanceof ValueChangedEvent) {
             if (listeners == null) {
                 return (false);
             }
             ValueChangedEvent vcevent = (ValueChangedEvent) event;
             int ordinal = phaseId.getOrdinal();
             broadcast(vcevent, listeners[PhaseId.ANY_PHASE.getOrdinal()]);
             broadcast(vcevent, listeners[ordinal]);
             for (int i = ordinal + 1; i < listeners.length; i++) {
                 if ((listeners[i] != null) && (listeners[i].size() > 0)) {
                     return (true);
                 }
             }
             return (false);
         } else {
             throw new IllegalArgumentException();
         }
 
     }
 
 
     /**
      * <p>Broadcast the specified {@link ValueChangedEvent} to the
      * {@link ValueChangedListener}s on the specified list (if any)
      *
      * @param event The {@link ValueChangedEvent} to be broadcast
      * @param list The list of {@link ValueChangedListener}s, or
      *  <code>null</code> for no interested listeners
      */
     protected void broadcast(ValueChangedEvent event, List list) {
 
         if (list == null) {
             return;
         }
         Iterator listeners = list.iterator();
         while (listeners.hasNext()) {
             ValueChangedListener listener =
                 (ValueChangedListener) listeners.next();
             listener.processValueChanged(event);
         }
 
     }
 
 
     /**
      * <p>Queue an {@link ValueChangedEvent} for processing during the next
      * event processing cycle.</p>
      *
      * @param context The {@link FacesContext} for the current request
      * @param oldValue The original value of this component
      * @param newValue The new value of this component
      */
     protected void fireValueChangedEvent(FacesContext context,
                                          Object oldValue,
                                          Object newValue) {
 
         context.addFacesEvent
             (new ValueChangedEvent(this, oldValue, newValue));
 
     }
 
 
     /**
      * <p>Remove an existing {@link ValueChangedListener} (if any) from the
      * set of listeners interested in being notified when
      * {@link ValueChangedEvent}s occur.</p>
      *
      * @param listener The {@link ValueChangedListener} to be removed
      *
      * @exception NullPointerException if <code>listener</code>
      *  is <code>null</code>
      */
     public void removeValueChangedListener(ValueChangedListener listener) {
 
         if (listener == null) {
             throw new NullPointerException();
         }
         if (listeners == null) {
             return;
         }
         int ordinal = listener.getPhaseId().getOrdinal();
         if (listeners[ordinal] == null) {
             return;
         }
         listeners[ordinal].remove(listener);
 
     }
 
 
 }
