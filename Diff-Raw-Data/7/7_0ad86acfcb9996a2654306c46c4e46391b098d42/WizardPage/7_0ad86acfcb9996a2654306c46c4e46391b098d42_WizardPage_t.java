 /*
  *                 Sun Public License Notice
  * 
  * The contents of this file are subject to the Sun Public License
  * Version 1.0 (the "License"). You may not use this file except in
  * compliance with the License. A copy of the License is available at
  * http://www.sun.com/
  * 
  * The Original Code is NetBeans. The Initial Developer of the Original
  * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
  * Microsystems, Inc. All Rights Reserved.
  */
 /*
  * FixedWizard.java
  *
  * Created on August 19, 2005, 9:11 PM
  */
 
 package org.netbeans.spi.wizard;
 import java.awt.Component;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import javax.swing.AbstractButton;
 import javax.swing.BorderFactory;
 import javax.swing.JCheckBox;
 import javax.swing.JColorChooser;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JSlider;
 import javax.swing.JSpinner;
 import javax.swing.JToggleButton;
 import javax.swing.JTree;
 import javax.swing.text.JTextComponent;
 import javax.swing.tree.TreePath;
 
 
 /**
  * A convenience JPanel subclass that makes it easy to create wizard panels.
  * This class provides a number of conveniences:
  * <p>
  * <b>Automatic listening to child components</b><br>
  * If you add an editable component (all standard Swing controls are supported)
  * to a WizardPanel or a child JPanel inside it,
  * a listener is automatically attached to it.  If user input occurs, the
  * following things happen, in order:
  * <ul>
  * <li>If the <code>name</code> property of the component has been set, then
  * the value from the component (i.e. Boolean for checkboxes, selected item(s)
  * for lists/combo boxes/trees, etc.) will automatically be added to the 
  * wizard settings map, with the component name as the key.</li>
  * <li>Regardless of whether the <code>name</code> property is set, 
  * <code>validateContents()</code> will be called.  You can override that method
  * to enable/disable the finish button, call <code>setProblem()</code> to 
  * disable navigation and display a string to the user, etc.
  * </ul>
  * <p>
  * The above behavior can be disabled by passing <code>false</code> to the
  * appropriate constructor.  In that case, <code>validateContents</code> will
  * never be called automatically.
  * <p>
  * If you have custom components that WizardPage will not know how to listen
  * to automatically, attach an appropriate listener to them.
  * <p>
  * For convenience, this class implements the relevant methods for accessing
  * the <code>WizardController</code> and the settings map for the wizard that
  * the panel is a part of.
  * <p>
  * Instances of WizardPage can be returned from a WizardPanelProvider;  this
  * class also offers two methods for conveniently assembling a wizard:
  * <ul>
  * <li>Pass an array of already instantiated WizardPages to 
  * <code>createWizard()</code>.  Note that for large wizards, it is preferable
  *  to construct the panels on demand rather than at construction time.</li>
  * <li>Construct a wizard out of WizardPages, instantiating the panels as
  *     needed:  Pass an array of classes all of which
  *      <ul>
  *      <li>Are subclasses of WizardPage</li>
 *      <li>Have a static method with the following signature:
 *      <ul>
  *          <li><code>public static String getDescription()</code></li>
  *      </ul>
  * </li>
  * </ul>
  * 
  * Note that during development of a wizard, it is worthwhile to test/run with
  * assertions enabled, as there is quite a bit of validity checking via assertions
  * that can help find problems early. 
  *
  * @author Tim Boudreau
  */
 public class WizardPage extends JPanel {
     private final String description;
     private final String id;
     //Have an initial dummy map so it's never null.  We'll dump its contents
     //into the real map the first time it's set
     private Map wizardData;
     //An initial wizardController that will dump its settings into the real
     //one the first time it's set
     private WizardController wc = new WC();
     private final boolean autoListen;
 
     private static final boolean LOG = Boolean.getBoolean ("WizardPage.log"); //NOI18N
     
     /** Construct a new WizardPage with the passed step id and description.
      * Use this constructor for WizardPages which will be constructed ahead
      * of time and passed in an array to <code>createWizard</code>. 
      * @param stepId the unique ID for the step represented
      * @param stepDescription the localized description of this step
      * @param autoListen if true, components added will automatically be
      *  listened to for user input
      * @see validateContents(Object)
      */
     public WizardPage(String stepId, String stepDescription, boolean autoListen) {
         this.autoListen = autoListen;
         this.description = stepDescription;
         this.id = stepId;
         if (autoListen) {
             //It will attach itself
             new GenericListener(this);
         }
 //        if (getClass() == WizardPage.class && stepId == null ||
 //                description == null) {
 //            throw new NullPointerException ("Step or ID is null"); //NOI18N
 //        }
         setBorder (BorderFactory.createEmptyBorder(5,5,5,5)); //XXX
     }
     //XXX use class name as ID and don't require it for .class usage?
     
     public WizardPage(String stepId, String stepDescription) {
         this (stepId, stepDescription, true);
     }
     
     /** Use this constructor or the default constructor if you intend to
      * pass an array of Class objects to lazily create WizardPanels. */
     protected WizardPage (boolean autoListen) {
         this (null, null, autoListen);
     }
     
     /** Default constructor.  AutoListening will be on by default. 
      */
     protected WizardPage() {
         this(true);
     }
     
     private String getID() {
         return id;
     }
     
     private String getDescription() {
         return description;
     }
     
     public void addNotify() {
         try {
             setProblem(validateContents(null, null));
         } finally {
             super.addNotify();
         }
     }
     
     /**
      * Create a simple Wizard from an array of <code>WizardPage</code>s
      */
     public static final Wizard createWizard(WizardPage[] contents, WizardResultProducer finisher) {
         return new WPP (contents, finisher).createWizard();
     }
 
     /**
      * Create a simple Wizard from an array of WizardPages, with a
      * no-op WizardResultProducer.
      */
     public static final Wizard createWizard(WizardPage[] contents) {
         return createWizard (contents, WizardResultProducer.NO_OP);
     }
     
     /**
      * Create simple Wizard from an array of classes, each of which is a 
      * unique subclass of WizardPage.
      */
     public static final Wizard createWizard(Class[] wizardPageClasses, WizardResultProducer finisher) {
         return new CWPP (wizardPageClasses, finisher).createWizard();
     }
     
     /**
      * Create a simple Wizard from an array of classes, each of which is a 
      * unique subclass of WizardPage, with a
      * no-op WizardResultProducer.
      */
     public static final Wizard createWizard(Class[] wizardPageClasses) {
         return createWizard (wizardPageClasses, WizardResultProducer.NO_OP);
     }
     
     
     /** Called by createPanelForStep, with whatever map is passed.  In the
      * current impl this is always the same Map, but that is not guaranteed.
      * If any content was added by calls to putWizardData() during the
      * constructor, etc., such data is copied to the settings map the first 
      * time this method is called */
     void setWizardDataMap (Map m) {
         if (m == null) {
             wizardData = new HashMap();
         } else {
             if (wizardData instanceof HashMap) {
                 //We're using our initial dummy map
                 m.putAll(wizardData);
             }
             wizardData = m;
         }
     }
     
     /** Set the WizardController.  In the current impl, this is always the same
      * object, but the API does not guarantee that.  The first time this is
      * called, it will update the state of the passed controller to  match
      * any state that was set by components during the construction of this
      * component */
     void setController (WizardController controller) {
         if (this.wc instanceof WC) {
             ((WC) this.wc).configure (controller);
         }
         this.wc = controller;
     }
     
     /**
      * Get the WizardController for interacting with the Wizard that 
      * contains this panel.
      * Return value will never be null.
      */
     private final WizardController getController() {
         return wc;
     }
     
 
     /**
      * Set the problem string.  Call this method if next/finish should be
      * disabled.  The passed string will be visible to the user, and should
      * be a short, localized description of what is wrong.
      */
     protected final void setProblem(String value) {
         getController().setProblem(value);
     }
 
     /**
      * Set whether the Finish button should be enabled.  Note that this is
      * independent of whether the Next button is enabled.  If called with
      * true after <code>setProblem</code> has been called with a non-null
      * value, the Finish button will not be enabled until a call to
      * <code>setProblem(null)</code> has been performed.
      */
     protected final void setForwardNavigation(int value) {
         getController().setFwdNavMode(value);
     }
 
     /**
      * Disable all navigation.  Useful if some background task is being
      * completed during which no navigation should be allowed.  Use with care,
      * as it disables the cancel button as well.
      */
     protected final void setBusy(boolean busy) {
         getController().setBusy(busy);
     }
     
     /**
      * Store a value in response to user interaction with a GUI component.
      */
     protected final void putWizardData(Object key, Object value) {
         if (LOG) log ("putWizardData " + key + ", " + value); //NOI18N
         getWizardDataMap().put(key, value);
         if (!inBeginUIChanged) {
             validateContents(null, null);
         }
     }
     
     protected final Object[] getWizardDataKeys() {
         return getWizardDataMap().keySet().toArray();
     }
     
     /**
      * Retrieve a value stored in the wizard map, which may have been 
      * putWizardData there by this panel or any previous panel in the wizard which
      * contains this panel.
      */
     protected final Object getWizardData(Object key) {
         return getWizardDataMap().get(key);
     }
     
     /**
      * Determine if the wizard map contains the requested key.
      */
     protected final boolean wizardDataContainsKey(Object key) {
         return getWizardDataMap().containsKey(key);
     }
     
     //Flag to make sure we don't reenter userInputReceieved from maybeUpdateMap()
     private boolean inBeginUIChanged = false;
     //Flag to make sure we don't reenter userInputReceived because the 
     //implementation of validateContents changed a component's value, triggering
     //a new event on GenericListener
     private boolean inUiChanged = false;
     
     /** Called when an event is received from one of the components in the
      * panel that indicates user input.  Typically you won't need to touch this
      * method, unless your panel contains custom components which are not 
      * subclasses of any standard Swing component, which the framework won't
      * know how to listen for changes on.  For such cases, attach a listener
      * to the custom component, and call this method with the event if you want
      * validation to run when input happens.  Automatic updating of the 
      * settings map will not work for such custom components, for obvious
      * reasons, so update the settings map, if needed, in validateContents 
      * for this case.
      *
      * @param source The component that the user interacted with (if it can
      *  be determined from the event) or null
      * @param event Usually an instance of EventObject, except in the case of
      *  DocumentEvent.
      */
     protected final void userInputReceived(Component source, Object event) {
         if (inBeginUIChanged) {
             if (LOG) log ("Ignoring recursive entry to userInputReceived while updating map");
             return;
         }
         //Update the map no matter what
         inBeginUIChanged = true;
         if (source != null) {
             try {
                 maybeUpdateMap (source);
             } finally {
                 inBeginUIChanged = false;
             }
         }
         //Possibly some programmatic change from checkState could cause
         //a recursive call
         if (inUiChanged) {
             if (LOG) log ("Ignoring recursive entry to userInputReceieved from validateContents.");
             return;
         }
         inUiChanged = true;
         try {
             setProblem (validateContents(source, event));
         } finally {
             inUiChanged = false;
         }
     }
     
     /** Puts the value from the component in the settings map if the 
      * component's name property is not null */
     void maybeUpdateMap (Component comp) {
         if (LOG) log ("Maybe update map for " + comp.getClass().getName() +  //NOI18N
                 " named " + comp.getName()); //NOI18N
         Object mapKey = comp.getName();
         if (mapKey != null) {
             //XXX do it even if null?
             Object value = valueFrom (comp);
             if (LOG) log ("maybeUpdateMap putting " + mapKey + "," + value + 
                     " into settings"); //NOI18N
             putWizardData (mapKey, value);
         }
     }
     
     /** Callback for GenericListener to remove a component's value if its name
      * changes or it is removed from the panel. */
     void removeFromMap (String key) {
         if (LOG) log ("removeFromMap: " + key); //NOI18N
         getWizardDataMap().remove(key);
     }
     
     /** Given an ad-hoc swing component, fetch the likely value based on its state */
     private static Object valueFrom (Component comp) {
 //        assert SwingUtilities.isEventDispatchThread() :
 //            "Not on the event thread.  This is unsafe."; //NOI18N
         if (comp instanceof JRadioButton || comp instanceof JCheckBox || comp instanceof JToggleButton) {
             return ((AbstractButton) comp).getModel().isSelected() ? Boolean.TRUE : Boolean.FALSE;
         } else if (comp instanceof JTree) {
             TreePath path = ((JTree) comp).getSelectionPath();
             if (path != null) {
                 return path.getLastPathComponent();
             }
         } else if (comp instanceof JList) {
             Object[] o = ((JList) comp).getSelectedValues();
             if (o != null) {
                 if (o.length > 1) {
                     return o;
                 } else if (o.length == 1) {
                     return o[0];
                 }
             }
         } else if (comp instanceof JTextComponent) {
             return ((JTextComponent) comp).getText();
         } else if (comp instanceof JComboBox) {
             return ((JComboBox) comp).getSelectedItem();
         } else if (comp instanceof JColorChooser) {
             return ((JColorChooser) comp).getSelectionModel().getSelectedColor();
         } else if (comp instanceof JSpinner) {
             return ((JSpinner) comp).getValue();
         } else if (comp instanceof JSlider) {
             return new Integer(((JSlider) comp).getValue());
         }
         return null;
     }
     
     /**
      * Called when user interaction has occurred on a component contained by this 
      * panel or one of its children.  Override this method to check if all of
      * the values are legal, such that the Next/Finish button should be enabled,
      * optionally calling <code>setFwdNavMode()</code> if warranted.
      * <p>
      * This method also may be called with a null argument an effect of 
      * calling <code>putWizardData()</code> from someplace other than within
      * this method.
      * <p>
      * Note that this method may be called very frequently, so it is important
      * that validation code be fast.  For cases such as <code>DocumentEvent</code>s,
      * it may be desirable to delay validation with a timer, if the implementation
      * of this method is too expensive to call on each keystroke.
      * <p>
      * Either the component, or the event, or both may be null on some calls
      * to this method (such as when it is called because the settings map
      * has been written to).
      * <p>
      * The default implementation returns null.
      * 
      * 
      * @param component The component the user interacted with, if it can be
      *  determined.  The infrastructure does track the owners of list models
      *  and such, and can find the associated component, so this will usually
      *  (but not necessarily) be non-null.
      * @param event The event object (if any) that triggered this call to
      *  validateContents.  For most cases this will be an instance of 
      *  EventObject, and can be used to directly detect what component
      *  the user interacted with.  Since javax.swing.text.DocumentEvent is
      *  not a subclass of EventObject, the type of the argument is Object,
      *  so these events may be passed.
      * @return A localized string describing why navigation should be disabled,
      *  or null if the state of the components is valid and forward navigation
      *  should be enabled.
      */
     protected String validateContents(Component component, Object event) {
         return null;
     }
     
     /**
      * Called if the user is navigating into this panel when it has already
      * been displayed at least once - the user has navigated back to this
      * panel, or back past this panel and is now navigating forward again.
      * <p>
      * If some of the UI needs to be set up based on values from earlier 
      * pages that may have changed, do that here, fetching values from the
      * settings map by calling <code>getWizardData()</code>.
      * <p>
      * The default implementation simply calls 
      * <code>validateContents (null, null)</code>.
      */
     protected void recycle() {
         validateContents (null, null);
     }
     
     /**
      * Get the settings map into which the wizard gathers settings.
      * Return value will never be null.
      */
     final Map getWizardDataMap() {
         if (wizardData == null) {
             wizardData = new HashMap();
         }
         return wizardData;
     }
     
     /** WizardPanelProvider that takes an array of already created WizardPages */
     private static final class WPP extends WizardPanelProvider {
         private final WizardPage[] pages;
         private final WizardResultProducer finish;
         
         WPP (WizardPage[] pages, WizardResultProducer finish) {
             super (getSteps(pages), getDescriptions(pages));
             this.finish = finish;
             this.pages = pages;
             //Fail-fast validation - don't wait until something goes wrong
             //if the data are bad
             assert valid(pages) == null : valid (pages);
             assert finish != null;
         }
         
         protected JComponent createPanel(WizardController controller, String id, 
                 Map wizardData) {
             int idx = indexOfStep(id);
             assert idx != -1 : "Bad ID passed to createPanel: " + id; //NOI18N
             pages[idx].setController (controller);
             pages[idx].setWizardDataMap (wizardData);
             return pages[idx];
         }
         
         /** Make sure we haven't been passed bogus data */
         private String valid (WizardPage[] pages) {
             String result = new HashSet(Arrays.asList(pages)).size() == pages.length ?
                 (String) null : "Duplicate entry in array: " +  //NOI18N
                 Arrays.asList(pages);
             if (result == null) {
                 for (int i=0; i < pages.length; i++) {
                     if (pages[i] == null) {
                         return "Null entry at " + i + " in pages array"; //NOI18N
                     }
                 }
             }
             return null;
         }
         
         protected Object finish (Map settings) throws WizardException {
             return finish.finish(settings);
         }
     }
     
     /** WizardPanelProvider that takes an array of WizardPage subclasses and
      * instantiates them on demand */
     private static final class CWPP extends WizardPanelProvider {
         private final Class[] clazz;
         private final WizardResultProducer finish;
         
         CWPP (Class[] clazz, WizardResultProducer finish) {
             super (getSteps(clazz), getDescriptions(clazz));
             this.finish = finish;
             assert new HashSet(Arrays.asList(clazz)).size() == clazz.length :
                 "Duplicate entries in class array";
             this.clazz = clazz;
             assert finish != null;
         }
 
         protected JComponent createPanel(WizardController controller, String id, Map wizardData) {
             int idx = indexOfStep(id);
             assert idx != -1 : "Bad ID passed to createPanel: " + id; //NOI18N
             try {
                 WizardPage result = (WizardPage) clazz[idx].newInstance();
                 result.setController (controller);
                 result.setWizardDataMap (wizardData);
                 return result;
             } catch (Exception e) {
                 e.printStackTrace();
                 throw new IllegalArgumentException ("Could not instantiate " + //NOI18N
                         clazz[idx]);
             }
         }
 
         protected Object finish (Map settings) throws WizardException {
             return finish.finish(settings);
         }
     }
     
     /** A dummy wizard controller which is used until the panel has actually 
      * been put into use;  so state can be set during the constructor, etc.
      * Its state will be dumped into the real one once there is a real one. */
     private static final class WC implements WizardController {
         String problem = null;
         int canFinish = -1;
         Boolean busy = null;
         
         public void setProblem(String value) {
             this.problem = value;
         }
 
         public void setFwdNavMode(int value) {
             switch (value) {
                 case STATE_CAN_CONTINUE :
                 case STATE_CAN_FINISH :
                 case STATE_CAN_CONTINUE_OR_FINISH :
                     break;
                 default :
                     throw new IllegalArgumentException(Integer.toString(value));
             }
             this.canFinish = value;
         }
 
         public void setBusy(boolean busy) {
             this.busy = busy ? Boolean.TRUE : Boolean.FALSE;
         }
         
         void configure (WizardController other) {
             if (busy != null) {
                 other.setBusy (busy.booleanValue());
             }
             if (canFinish != -1) {
                 other.setFwdNavMode(canFinish);
             }
             if (problem != null) {
                 other.setProblem (problem);
             }
         }
     }
     
     /** Get an array of step ids from an array of WizardPages */
     private static final String[] getSteps (WizardPage[] pages) {
         String[] result = new String[pages.length];
         for (int i=0; i < pages.length; i++) {
             result[i] = pages[i].getID();
         }
         return result;
     }
     
     /** Get an array of descriptions from an array of WizardPages */
     private static final String[] getDescriptions (WizardPage[] pages) {
         String[] result = new String[pages.length];
         for (int i=0; i < pages.length; i++) {
             result[i] = pages[i].getDescription();
         }
         return result;
     }
     
     /** Get an array of steps by looking for a static method getID() on each
      * class object passed */
     private static final String[] getSteps (Class[] pages) {
         if (pages == null) {
             throw new NullPointerException ("Null array of classes"); //NOI18N
         }
         String[] result = new String[pages.length];
         for (int i=0; i < pages.length; i++) {
             if (pages[i] == null) {
                 throw new NullPointerException ("Null at " + i + " in array " + //NOI18N
                         "of panel classes"); //NOI18N
             }
             if (!WizardPage.class.isAssignableFrom(pages[i])) {
                 throw new IllegalArgumentException (pages[i].getName() + 
                         " is not a subclass of WizardPage"); //NOI18N
             }
             
 //            if (!hasDefaultConstructor (pages[i])) {
 //                throw new IllegalArgumentException (pages[i] + " has no " +
 //                        "default no argument constructor");
 //            }
             
             result[i] = pages[i].getName();
             /*
             Method m = null;
             try {
                 m = pages[i].getDeclaredMethod("getID", null); //NOI18N
             } catch (Exception e) {
                 throw new IllegalArgumentException ("Could not find or access " + //NOI18N
                         "public static String " + pages[i].getName() +  //NOI18N
                         ".getStep() - make sure it exists"); //NOI18N
             }
             if (m.getReturnType() != String.class) {
                 throw new IllegalArgumentException ("getStep has wrong " //NOI18N
                         + " return type: " + m.getReturnType() + " on " + //NOI18N
                         pages[i]);
             }
             if ((m.getModifiers() | Modifier.STATIC) == 0) {
                 throw new IllegalArgumentException ("getStep is not " + //NOI18N
                         "static on " + pages[i]); //NOI18N
             }
             try {
                 result[i] = (String) m.invoke(null, null);
             } catch (InvocationTargetException ite) {
                 throw new IllegalArgumentException ("Could not invoke " + //NOI18N
                         "public static String " + pages[i].getName() +  //NOI18N
                         ".getStep() - make sure it exists. " + ite); //NOI18N
             } catch (IllegalAccessException iae) {
                 throw new IllegalArgumentException ("Could not invoke " + //NOI18N
                         "public static String " + pages[i].getName() +  //NOI18N
                         ".getStep() - make sure it exists. " + iae); //NOI18N
             } */
         }
         return result;
     }
     
 //    /** Determine if a default constructor is present for a class */
 //    private static boolean hasDefaultConstructor (Class clazz) {
 //        try {
 //            Constructor c = clazz.getConstructor(new Class[0]);
 //            return c != null;
 //        } catch (Exception e) {
 //            return false;
 //        }
 //    }
     
     /** Get an array of descriptions by looking for the static method 
      * getDescription() on each passed class object */
     private static final String[] getDescriptions (Class[] pages) {
         String[] result = new String[pages.length];
         for (int i=0; i < pages.length; i++) {
             Method m = null;
             try {
                 m = pages[i].getDeclaredMethod("getDescription", null); //NOI18N
             } catch (Exception e) {
                 throw new IllegalArgumentException ("Could not find or access " + //NOI18N
                         "public static String " + pages[i].getName() +  //NOI18N
                         ".getStep() - make sure it exists"); //NOI18N
             }
             if (m.getReturnType() != String.class) {
                 throw new IllegalArgumentException ("getStep has wrong " //NOI18N
                         + " return type: " + m.getReturnType() + " on " + //NOI18N
                         pages[i]);
             }
             if ((m.getModifiers() | Modifier.STATIC) == 0) {
                 throw new IllegalArgumentException ("getStep is not " + //NOI18N
                         "static on " + pages[i]); //NOI18N
             }
             try {
                 result[i] = (String) m.invoke(null, null);
             } catch (InvocationTargetException ite) {
                 throw new IllegalArgumentException ("Could not invoke " + //NOI18N
                         "public static String " + pages[i].getName() +  //NOI18N
                         ".getStep() - make sure it exists."); //NOI18N
             } catch (IllegalAccessException iae) {
                 throw new IllegalArgumentException ("Could not invoke " + //NOI18N
                         "public static String " + pages[i].getName() +  //NOI18N
                         ".getStep() - make sure it exists."); //NOI18N
             }
         }
         return result;
     }    
     
     private static final void log (String s) {
         System.out.println(s);
     }
     
     /**
      * Interface that is passed to WizardPage.createWizard()
      */
     public static interface WizardResultProducer {
         public Object finish (Map wizardData) throws WizardException;
         public static final WizardResultProducer NO_OP = new WizardResultProducer() { 
             public Object finish (Map wizardData) { return null; } 
         };
     }
 }
