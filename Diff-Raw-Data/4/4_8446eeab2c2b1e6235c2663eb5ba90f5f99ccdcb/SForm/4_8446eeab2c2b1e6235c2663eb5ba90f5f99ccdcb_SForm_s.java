 /*
  * $Id$
  * Copyright 2000,2005 wingS development team.
  *
  * This file is part of wingS (http://www.j-wings.org).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 package org.wings;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.wings.plaf.FormCG;
 
 import javax.swing.event.EventListenerList;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.net.URL;
 import java.util.*;
 
 
 /**
  * Container in which you need to wrap HTML input fields (ie&#x2e; <code>STextField</code>)
  * to work correctly.
  * <p/>
  * The browser uses this object/tag to identify how (POST or GET) and where
  * to send an request originating from any input inside this form.
  * <p/>
  * <b>Note:</b>Please be aware, that some components render differently if
  * placed inside a <code>SForm</code>.
  *
  * @author <a href="mailto:armin.haaf@mercatis.de">Armin Haaf</a>
  * @version $Revision$
  */
 public class SForm        extends SContainer        implements LowLevelEventListener {
     private final transient static Log log = LogFactory.getLog(SForm.class);
 
     /**
      * Default Form encoding type. See {@link #setEncodingType(String)}.
      */
     // TODO check this encoding type!
     public final static String ENC_TYPE_TEXT_PLAIN = "text/plain";
     /**
      * Multipart form encoding. Needed for file uploads. See {@link #setEncodingType(String)}.
      */
     public final static String ENC_TYPE_MULTIPART_FORM = "multipart/form-data";
     /**
      * URL form encoding. See {@link #setEncodingType(String)}.
      */
     public static final String URL_ENCODING = "application/x-www-form-urlencoded";
 
     /**
      * Use method POST for submission of the data.
      */
     private boolean postMethod = true;
 
     /**
      * EncondingType for submission of the data.
      */
     private String encType;
 
     /**
      * URL to which data
      * should be sent to
      */
     private URL action;
 
     protected final EventListenerList listenerList = new EventListenerList();
 
     protected String actionCommand;
 
     /**
      * the button, that is activated, if no other button is pressed in this
      * form.
      */
     private SButton defaultButton;
 
     /**
      * the WingS event thread is the servlet doGet()/doPost() context
      * thread. Within this thread, we collect all armed components. A
      * 'armed' component is a component, that will 'fire' an event after the
      * first processRequest() stage is completed.
      */
     private static ThreadLocal threadArmedComponents = new ThreadLocal() {
         protected synchronized Object initialValue() {
             return new HashSet(2);
         }
     };
 
     /**
      * Create a standard form component.
      */
     public SForm() {
     }
 
     /**
      * Create a standard form component but redirects the request to the passed
      * URL. Use this i.e. to address other servlets.
      *
      * @param action The target URL.
      */
     public SForm(URL action) {
         setAction(action);
     }
 
 
     /**
      * Create a standard form component.
      *
      * @param layout The layout to apply to this container.
      * @see SContainer
      */
     public SForm(SLayoutManager layout) {
         super(layout);
     }
 
     /**
      * A SForm fires an event each time it was triggered (i.e. pressing asubmit button inside)
      *
      * @param actionCommand The action command to place insiside the {@link ActionEvent}
      */
     public void setActionCommand(String actionCommand) {
         this.actionCommand = actionCommand;
     }
 
     /**
      * @see #setActionCommand(String)
      */
     public String getActionCommand() {
         return actionCommand;
     }
 
     /**
      * Set the default button activated upon <b>enter</b>.
      * The button is triggered if you press <b>enter</b> inside a form to submit it.
      * @param defaultButton A button which will be rendered <b>invisible</b>.
      * If <code>null</code> enter key pressed will be catched by the wings framework.
      */
     public void setDefaultButton(SButton defaultButton) {
         this.defaultButton = defaultButton;
     }
 
     /**
      * @see #setDefaultButton(SButton)
      */
     public SButton getDefaultButton() {
         return this.defaultButton;
     }
 
     /**
      * Add a listener for Form events. A Form event is always triggered, when
      * a form has been submitted. Usually, this happens, whenever a submit
      * button is pressed or some other mechanism triggered the posting of the
      * form. Other mechanisms are
      * <ul>
      * <li> Java Script submit() event</li>
      * <li> If a form contains a single text input, then many browsers
      * submit the form, if the user presses RETURN in that field. In that
      * case, the submit button will <em>not</em> receive any event but
      * only the form.
      * <li> The {@link SFileChooser} will trigger a form event, if the file
      * size exceeded the allowed size. In that case, even if the submit
      * button has been pressed, no submit-button event will be triggered.
      * (For details, see {@link SFileChooser}).
      * </ul>
      * Form events are guaranteed to be triggered <em>after</em> all
      * Selection-Changes and Button ActionListeners.
      */
     public void addActionListener(ActionListener listener) {
         listenerList.add(ActionListener.class, listener);
     }
 
     /**
      * Remove a form action listener, that has been added in
      * {@link #addActionListener(ActionListener)}
      */
     public void removeActionListener(ActionListener listener) {
         listenerList.remove(ActionListener.class, listener);
     }
 
     /**
      * Fire a ActionEvent at each registered listener.
      */
     protected void fireActionPerformed(String pActionCommand) {
         ActionEvent e = null;
         // Guaranteed to return a non-null array
         Object[] listeners = listenerList.getListenerList();
         // Process the listeners last to first, notifying
         // those that are interested in this event
         for (int i = listeners.length - 2; i >= 0; i -= 2) {
             if (listeners[i] == ActionListener.class) {
                 // lazy create ActionEvent
                 if (e == null) {
                     e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED,
                             pActionCommand);
                 }
                 ((ActionListener) listeners[i + 1]).actionPerformed(e);
             }
         }
     }
 
     public final static void addArmedComponent(LowLevelEventListener component) {
         Set armedComponents = (Set) threadArmedComponents.get();
         armedComponents.add(component);
     }
 
     /**
      * clear armed components. This is usually not necessary, since sessions
      * clear clear their armed components. But if there was some Exception, it
      * might well be, that this does not happen.
      */
     public static void clearArmedComponents() {
         Set armedComponents = (Set) threadArmedComponents.get();
         armedComponents.clear();
     }
 
     /*
      * Die Sache muss natuerlich Thread Save sein, d.h. es duerfen nur
      * die Events gefeuert werden, die auch aus dem feuernden Thread
      * stammen (eben dem Dispatcher Thread). Sichergestellt wird das
      * dadurch das beim abfeuern der Event in eine Queue (ArrayList)
      * gestellt wird, die zu dem feuernden Event gehoert. Diese Queues
      * der verschiedenen Threads werden in einer Map verwaltet.
      * Beim feuern wird dann die Queue, die dem aktuellen Thread
      * entspricht gefeuert und aus der Map entfernt.
      */
     /**
      * This method fires the low level events for all "armed" components of
      * this thread (http session) in an ordered manner:
      * <ul><li>forms
      * <li>buttons / clickables
      * <li>"regular" components</ul>
      * This order derives out of the assumption, that a user first modifies
      * regular components before he presses the button submitting his changes.
      * Otherwise button actions would get fired before the edit components
      * fired their events.
      */
     public static void fireEvents() {
        Set armedComponents = (Set) threadArmedComponents.get();
         try {
             LowLevelEventListener component;
             // handle form special, form event should be fired last
             // hopefully there is only one form ;-)
             Iterator iterator = armedComponents.iterator();
             LinkedList formEvents = null;
             LinkedList buttonEvents = null;
 
             while (iterator.hasNext()) {
                 component = (LowLevelEventListener) iterator.next();
                 /* fire form events at last
                  * there could be more than one form event (e.g. mozilla posts a
                  * hidden element even if it is in a form outside the posted
                  * form (if the form is nested). Forms should not be nested in HTML.
                  */
                 if (component instanceof SForm) {
                     if (formEvents == null) {
                         formEvents = new LinkedList();
                     } // end of if ()
                     formEvents.add(component);
                 } else if (component instanceof SAbstractIconTextCompound) {
                     if (buttonEvents == null) {
                         buttonEvents = new LinkedList();
                     }
                     buttonEvents.add(component);
                 } else {
                     component.fireIntermediateEvents();
                 }
             }
 
             /*
              * no buttons in forms pressed ? Then consider the default-Button.
              */
             if (buttonEvents == null && formEvents != null) {
                 Iterator fit = formEvents.iterator();
                 while (fit.hasNext()) {
                     SForm form = (SForm) fit.next();
                     SButton defaultButton = form.getDefaultButton();
                     if (defaultButton != null) {
                         if (buttonEvents == null) {
                             buttonEvents = new LinkedList();
                         }
                         buttonEvents.add(defaultButton);
                     }
                 }
             }
 
             if (buttonEvents != null) {
                 iterator = buttonEvents.iterator();
                 while (iterator.hasNext()) {
                     ((SAbstractIconTextCompound) iterator.next()).fireIntermediateEvents();
                 }
             }
 
             if (formEvents != null) {
                 iterator = formEvents.iterator();
                 while (iterator.hasNext()) {
                     ((SForm) iterator.next()).fireIntermediateEvents();
                 }
             }
 
             iterator = armedComponents.iterator();
             while (iterator.hasNext()) {
                 component = (LowLevelEventListener) iterator.next();
                 // fire form events at last
                 if (!(component instanceof SForm || component instanceof SAbstractIconTextCompound)) {
                     component.fireFinalEvents();
                 }
             }
 
             if (buttonEvents != null) {
                 iterator = buttonEvents.iterator();
                 while (iterator.hasNext()) {
                     ((SAbstractIconTextCompound) iterator.next()).fireFinalEvents();
                 }
                 buttonEvents.clear();
             }
 
             if (formEvents != null) {
                 iterator = formEvents.iterator();
                 while (iterator.hasNext()) {
                     ((SForm) iterator.next()).fireFinalEvents();
                 }
                 formEvents.clear();
             }
         } finally {
             armedComponents.clear();
         }
     }
 
 
     /**
      * Set, whether this form is to be transmitted via <code>POST</code> (true)
      * or <code>GET</code> (false). The default, and this is what you
      * usually want, is <code>POST</code>.
      */
     public void setPostMethod(boolean postMethod) {
         this.postMethod = postMethod;
     }
 
     /**
      * Returns, whether this form is transmitted via <code>POST</code> (true)
      * or <code>GET</code> (false). <p>
      * <b>Default</b> is <code>true</code>.
      *
      * @return <code>true</code> if form postedt via <code>POST</code>,
      * <code>false</code> if via <code>GET</code> (false).
      */
     public boolean isPostMethod() {
         return postMethod;
     }
 
     /**
      * Set the encoding of this form. This actually is an HTML interna
      * that bubbles up here. By default, the encoding type of any HTML-form
      * is <code>application/x-www-form-urlencoded</code>, and as such, needn't
      * be explicitly set with this setter. However, if you've included a
      * file upload element (as represented by {@link SFileChooser}) in your
      * form, this must be set to <code>multipart/form-data</code>, since only
      * then, files are transmitted correctly. In 'normal' forms without
      * file upload, it is not necessary to set it to
      * <code>multipart/form-data</code>; actually it enlarges the data to
      * be transmitted, so you probably don't want to do this, then.
      *
      * @param type the encoding type; one of <code>multipart/form-data</code>,
      *             <code>application/x-www-form-urlencoded</code> or null to detect encoding.
      */
     public void setEncodingType(String type) {
         encType = type;
     }
 
     /**
      * Get the current encoding type, as set with
      * {@link #setEncodingType(String)}. If no encoding type was set, this
      * method detects the best encoding type. This can be expensive, so if
      * you can, set the encoding type. 
      *
      * @return string containing the encoding type. This is something like
      *         <code>multipart/form-data</code>,
      *         <code>application/x-www-form-urlencoded</code> .. or 'null'
      *         by default. 
      */
     public String getEncodingType() {
         if (encType == null) {
             return detectEncodingType(this);
         } else {
             return encType;
         }
     }
 
     /**
      * Detects if the Container contains a component that needs a certain encoding type
      * @param pContainer
      * @return <code>null</code> or {@link #ENC_TYPE_MULTIPART_FORM}   
      */
     protected String detectEncodingType(SContainer pContainer) {
         for (int i = 0; i < pContainer.getComponentCount(); i++) {
             SComponent tComponent = pContainer.getComponent(i);
             if (tComponent instanceof SFileChooser && tComponent.isVisible()) {
                 return ENC_TYPE_MULTIPART_FORM;
             } else if ((tComponent instanceof SContainer) && tComponent.isVisible()) {
                 String tContainerEncoding = detectEncodingType((SContainer) tComponent);
                 if (tContainerEncoding != null) {
                     return tContainerEncoding;
                 }
             }
         }
         return null;
     }
 
 
     public void setAction(URL action) {
         this.action = action;
     }
 
 
     public URL getAction() {
         return action;
     }
 
 
     public RequestURL getRequestURL() {
         RequestURL addr = super.getRequestURL();
         if (getAction() != null) {
             addr.addParameter(getAction().toString()); // ??
         }
         return addr;
     }
 
     public void processLowLevelEvent(String action, String[] values) {
         processKeyEvents(values);
 
         // we have to wait, until all changed states of our form have
         // changed, before we anything can happen.
         SForm.addArmedComponent(this);
     }
 
     public void fireIntermediateEvents() {
     }
 
     public void fireFinalEvents() {
         fireKeyEvents();
         fireActionPerformed(getActionCommand());
     }
 
     /** @see LowLevelEventListener#isEpochCheckEnabled() */
     private boolean epochCheckEnabled = true;
 
     /** @see LowLevelEventListener#isEpochCheckEnabled() */
     public boolean isEpochCheckEnabled() {
         return epochCheckEnabled;
     }
 
     /** @see LowLevelEventListener#isEpochCheckEnabled() */
     public void setEpochCheckEnabled(boolean epochCheckEnabled) {
         this.epochCheckEnabled = epochCheckEnabled;
     }
 
     public SComponent addComponent(SComponent c, Object constraint, int index) {
         if (c instanceof SForm)
             log.warn("WARNING: attempt to nest forms; won't work.");
         return super.addComponent(c, constraint, index);
     }
 
     public void setCG(FormCG cg) {
         super.setCG(cg);
     }
 }
