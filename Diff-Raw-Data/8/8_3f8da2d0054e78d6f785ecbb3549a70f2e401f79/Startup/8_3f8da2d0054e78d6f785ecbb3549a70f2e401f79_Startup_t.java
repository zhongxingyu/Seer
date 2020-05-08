 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.rc.rcp.accessor;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jface.action.IContributionItem;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.dialogs.IPageChangedListener;
 import org.eclipse.jface.dialogs.PageChangedEvent;
 import org.eclipse.jface.preference.IPreferenceNode;
 import org.eclipse.jface.preference.PreferenceDialog;
 import org.eclipse.jface.preference.PreferenceManager;
 import org.eclipse.jubula.rc.common.AUTServer;
import org.eclipse.jubula.rc.common.uiadapter.factory.GUIAdapterFactoryRegistry;
 import org.eclipse.jubula.rc.rcp.gef.inspector.GefInspectorListenerAppender;
 import org.eclipse.jubula.rc.rcp.gef.listener.GefPartListener;
 import org.eclipse.jubula.rc.swt.SwtAUTServer;
 import org.eclipse.jubula.rc.swt.listener.ComponentHandler;
import org.eclipse.jubula.rc.swt.uiadapter.factory.SWTAdapterFactory;
 import org.eclipse.jubula.tools.constants.AutConfigConstants;
 import org.eclipse.jubula.tools.constants.AutEnvironmentConstants;
 import org.eclipse.jubula.tools.constants.CommandConstants;
 import org.eclipse.jubula.tools.constants.SwtAUTHierarchyConstants;
 import org.eclipse.jubula.tools.utils.EnvironmentUtils;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.CoolBar;
 import org.eclipse.swt.widgets.CoolItem;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Item;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.ToolBar;
 import org.eclipse.swt.widgets.Widget;
 import org.eclipse.ui.IEditorReference;
 import org.eclipse.ui.IPartListener2;
 import org.eclipse.ui.IStartup;
 import org.eclipse.ui.IViewReference;
 import org.eclipse.ui.IWindowListener;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchPartReference;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.internal.WorkbenchPartReference;
 
 
 /**
  * Initializes an AUT Server in a plug-in context.
  * 
  * It is very important to avoid referencing GEF (org.eclipse.gef.*) classes 
  * directly from this class, as this will cause Class Not Found errors if the 
  * AUT does not contain the GEF plug-in.
  *
  * @author BREDEX GmbH
  * @created Oct 5, 2007
  */
 public class Startup implements IStartup {
     /** Key for GEF Viewer in component data */
     public static final String TEST_GEF_VIEWER_DATA_KEY = "TEST_GEF_VIEWER"; //$NON-NLS-1$
 
     /** bundle ID for Eclipse Graphical Editing Framework (GEF) */
     public static final String GEF_BUNDLE_ID = "org.eclipse.gef"; //$NON-NLS-1$
     
     /** Key for RCP partId in component data */
     private static final String TEST_RCP_DATA_KEY = 
         SwtAUTHierarchyConstants.RCP_NAME;
     
     /** ID suffix for toolbars belonging to a part (view/editor) */
     private static final String TOOLBAR_ID_SUFFIX = ".toolbar"; //$NON-NLS-1$
         
     /** Single listener instance */
     private static IPartListener2 partNamingListener = 
         new PartNamingListener();
     
     /** 
      * This listener 
      */
     private IPartListener2 m_gefListener = null;
 
     /**
      * This listener assigns names to components as they become visible. The
      * assigned name is determined by supporting data of the component and its
      * surroundings.
      *
      * @author BREDEX GmbH
      * @created Oct 19, 2007
      */
     private static class ComponentNamer implements Listener {
 
         /** map for naming dialog buttons */
         private static Map componentNAMES = new HashMap();
         
         /** is name generation enabled */
         private static boolean generateNames = false;
         
         static {
             generateNames = Boolean.valueOf(
                     EnvironmentUtils.getProcessEnvironment()
                         .getProperty(
                                 AutEnvironmentConstants
                                     .GENERATE_COMPONENT_NAMES))
                             .booleanValue();
             
             addCompName(IDialogConstants.ABORT_ID, "abort"); //$NON-NLS-1$
             addCompName(IDialogConstants.BACK_ID, "back"); //$NON-NLS-1$
             addCompName(IDialogConstants.CANCEL_ID, "cancel"); //$NON-NLS-1$
             addCompName(IDialogConstants.CLIENT_ID, "client"); //$NON-NLS-1$
             addCompName(IDialogConstants.CLOSE_ID, "close"); //$NON-NLS-1$
             addCompName(IDialogConstants.DESELECT_ALL_ID, "deselectAll"); //$NON-NLS-1$
             addCompName(IDialogConstants.DETAILS_ID, "details"); //$NON-NLS-1$
             addCompName(IDialogConstants.FINISH_ID, "finish"); //$NON-NLS-1$
             addCompName(IDialogConstants.HELP_ID, "help"); //$NON-NLS-1$
             addCompName(IDialogConstants.IGNORE_ID, "ignore"); //$NON-NLS-1$
             addCompName(IDialogConstants.INTERNAL_ID, "internal"); //$NON-NLS-1$
             addCompName(IDialogConstants.NEXT_ID, "next"); //$NON-NLS-1$
             addCompName(IDialogConstants.NO_ID, "no"); //$NON-NLS-1$
             addCompName(IDialogConstants.NO_TO_ALL_ID, "noToAll"); //$NON-NLS-1$
             addCompName(IDialogConstants.OK_ID, "ok"); //$NON-NLS-1$
             addCompName(IDialogConstants.OPEN_ID, "open"); //$NON-NLS-1$
             addCompName(IDialogConstants.PROCEED_ID, "proceed"); //$NON-NLS-1$
             addCompName(IDialogConstants.RETRY_ID, "retry"); //$NON-NLS-1$
             addCompName(IDialogConstants.SELECT_ALL_ID, "selectAll"); //$NON-NLS-1$
             addCompName(IDialogConstants.SELECT_TYPES_ID, "selectTypes"); //$NON-NLS-1$
             addCompName(IDialogConstants.SKIP_ID, "skip"); //$NON-NLS-1$
             addCompName(IDialogConstants.STOP_ID, "stop"); //$NON-NLS-1$
             addCompName(IDialogConstants.YES_ID, "yes"); //$NON-NLS-1$
             addCompName(IDialogConstants.YES_TO_ALL_ID, "yesToAll"); //$NON-NLS-1$
         }
         
         /**
          * add component id <-> name mapping
          * @param compID the component identifier
          * @param compName the component name
          */
         private static void addCompName(int compID, String compName) {
             String staticNamePreafix = "dialog.button."; //$NON-NLS-1$
             componentNAMES.put(new Integer(compID), 
                     staticNamePreafix + compName);
         }
         
         /**
          * {@inheritDoc}
          */
         public void handleEvent(Event event) {
             addNameData(event.widget);
             Item [] items = new Item [] {};
             if (event.widget instanceof ToolBar) {
                 items = ((ToolBar)event.widget).getItems();
             } else if (event.widget instanceof CoolBar) {
                 items = ((CoolBar)event.widget).getItems();
             }
             for (int i = 0; i < items.length; i++) {
                 addNameData(items[i]);
             }
         }
 
         /**
          * Adds name information to the given widget, if necessary.
          * 
          * @param widget The widget to name.
          */
         private void addNameData(Widget widget) {
             // Assign name
             if (widget != null && !widget.isDisposed()) {
                 if (widget.getData(Startup.TEST_RCP_DATA_KEY) != null) {
                     // Component already has a name, so we don't need to
                     // assign a new one.
                     return;
                 }
                 Object data = getWidgetData(widget);
 
                 if (data instanceof IContributionItem) {
                     // Name buttons and toolitems according to the action that
                     // they represent, if possible.
                     String actionId = ((IContributionItem)data).getId();
                     if (actionId != null && actionId.trim().length() > 0) {
 
                         widget.setData(Startup.TEST_RCP_DATA_KEY, actionId);
                         ComponentHandler.getAutHierarchy()
                             .refreshComponentName(widget);
 
                     }
                 } else if (data instanceof PreferenceDialog) {
                     PreferenceDialog prefDialog = (PreferenceDialog)data;
              
                     // Add a listener to add name data as pages are 
                     // selected/created.
                     prefDialog.addPageChangedListener(
                         new IPageChangedListener() {
 
                             public void pageChanged(PageChangedEvent event) {
                                 addNameDataToPrefPage(event.getSelectedPage());
                             }
                         
                         });
 
                     // The listener won't notice the initally selected page,
                     // so we have to add that name data here.
                     addNameDataToPrefPage(prefDialog.getSelectedPage());
                 }
                 if (generateNames && data instanceof Dialog) {
                     Dialog dialog = (Dialog)data;
                     setNameForDialogButtonBarButtons(dialog);
                 }
             }
         }
 
         /**
          * 
          * @param widget The widget for which to get the data.
          * @return the data object corresponding to the given widget.
          */
         private Object getWidgetData(Widget widget) {
             Object data = widget.getData();
 
             // Handle the case of CoolBar containing CoolItem containing ToolBar.
             // The CoolItem is the widget that represents the toolbar 
             // contribution, but it (the CoolItem) is not in our AUT 
             // component hierarchy, due to the fact that the ToolBar's 
             // getParent() returns the CoolBar rather than the CoolItem.
             // To resolve this discrepancy, we use the data from the 
             // coresponding CoolItem to generate a name for the ToolBar.
             try {
                 if (widget instanceof ToolBar) {
                     Composite toolbarParent = ((ToolBar)widget).getParent();
                     if (toolbarParent instanceof CoolBar) {
                         CoolItem [] coolItems = 
                             ((CoolBar)toolbarParent).getItems();
                         for (int i = 0; i < coolItems.length; i++) {
                             CoolItem item = coolItems[i];
                             if (item != null 
                                     && item.getControl() == widget) {
                                 data = item.getData();
                             }
                         }
                     }
                 }
             } catch (NoClassDefFoundError e) {
                 // we may be running in eRCP which doesn't know
                 // about
                 // toolbars, so we just ignore this
             }
 
             return data;
         }
         
         /**
          * @param dialog the dialog
          */
         private void setNameForDialogButtonBarButtons(Dialog dialog) {
             try {
                 Method getButtonMethod = Dialog.class.getDeclaredMethod(
                         "getButton", new Class[] { int.class }); //$NON-NLS-1$
                 getButtonMethod.setAccessible(true);
 
                 Iterator components = componentNAMES.keySet().iterator();
                 while (components.hasNext()) {
                     Integer componentID = (Integer)components.next();
                     invokeNameSetting(dialog, getButtonMethod, componentID,
                             componentNAMES.get(componentID));
                 }
             } catch (SecurityException e) {
                 // ignore exceptions
             } catch (NoSuchMethodException e) {
                 // ignore exceptions
             }
         }
 
         /**
          * use this method to set a name on the given object
          * @param useObject the object
          * @param methodToInvoke the method to invoke
          * @param buttonID the button id
          * @param buttonName the button name
          */
         private void invokeNameSetting(Object useObject, Method methodToInvoke,
                 Integer buttonID, Object buttonName) {
             Object ret = null;
             try {
                 ret = methodToInvoke.invoke(useObject,
                         new Object[] { buttonID });
             } catch (IllegalArgumentException e) {
                 // ignore exceptions
             } catch (IllegalAccessException e) {
                 // ignore exceptions
             } catch (InvocationTargetException e) {
                 // ignore exceptions
             }
             if (ret instanceof Button) {
                 Button button = (Button)ret;
                 if (button.getData(TEST_RCP_DATA_KEY) == null) {
                     button.setData(TEST_RCP_DATA_KEY, buttonName);
                 }
             }
         }
 
         /**
          * Attaches name data to the given page appropriate.
          * 
          * @param selectedPage The page to which we will try to attach the 
          *                     name data.
          */
         private void addNameDataToPrefPage(Object selectedPage) {
             if (selectedPage == null) {
                 return;
             }
             PreferenceManager prefMan = 
                 PlatformUI.getWorkbench().getPreferenceManager();
 
             Iterator iter = 
                 prefMan.getElements(PreferenceManager.PRE_ORDER).iterator();
             while (iter.hasNext()) {
                 IPreferenceNode prefNode = 
                     (IPreferenceNode)iter.next();
                 if (selectedPage.equals(prefNode.getPage())) {
                     Control pageControl = 
                         prefNode.getPage().getControl();
                     String prefNodeId = prefNode.getId();
                     
                     // Assign id to page composite only if the composite exists
                     // and if the id is usable
                     if (pageControl != null 
                         && !pageControl.isDisposed()
                         && pageControl.getData(
                             Startup.TEST_RCP_DATA_KEY) == null
                         && prefNodeId != null
                         && prefNodeId.trim().length() > 0) {
                        
                         pageControl.setData(
                             Startup.TEST_RCP_DATA_KEY, prefNodeId);
                        
                         Shell prefShell = 
                             pageControl.getDisplay().getActiveShell();
                         Event activateEvent = new Event();
                         activateEvent.time = (int)System.currentTimeMillis();
                         activateEvent.type = SWT.Activate;
                         activateEvent.widget = prefShell;
                         prefShell.notifyListeners(SWT.Activate, activateEvent);
 
                     }
 
                     // We found the page we were looking for, so we can stop 
                     // searching.
                     break;
                 }
             }
         }
     }
 
     /**
      * Assigns the controls (Composites) of Parts unique names based on 
      * their partId.
      *
      * @author BREDEX GmbH
      * @created Oct 5, 2007
      */
     static class PartNamingListener implements IPartListener2 {
 
         /**
          * 
          * {@inheritDoc}
          */
         public void partActivated(IWorkbenchPartReference partRef) {
             // Do nothing
         }
 
         /**
          * 
          * {@inheritDoc}
          */
         public void partBroughtToTop(IWorkbenchPartReference partRef) {
             // Do nothing
         }
 
         /**
          * 
          * {@inheritDoc}
          */
         public void partClosed(IWorkbenchPartReference partRef) {
             // Do nothing
         }
 
         /**
          * 
          * {@inheritDoc}
          */
         public void partDeactivated(IWorkbenchPartReference partRef) {
             // Do nothing
         }
 
         /**
          * 
          * {@inheritDoc}
          */
         public void partHidden(IWorkbenchPartReference partRef) {
             // Do nothing
         }
 
         /**
          * 
          * {@inheritDoc}
          */
         public void partInputChanged(IWorkbenchPartReference partRef) {
             // Do nothing
         }
 
         /**
          * 
          * {@inheritDoc}
          */
         public void partOpened(IWorkbenchPartReference partRef) {
             if (partRef instanceof WorkbenchPartReference) {
                 WorkbenchPartReference workbenchPartRef = 
                     (WorkbenchPartReference)partRef;
                 // Get pane contents and part id
                 Control partContent = 
                     workbenchPartRef.getPane().getControl();
 
                 if (partContent != null 
                     && !partContent.isDisposed()
                     && partContent.getData(TEST_RCP_DATA_KEY) == null) {
                     
                     // Name pane control based on part
                     String partId = workbenchPartRef.getId();
 
                     // Append secondary id, if necessary
                     if (partRef instanceof IViewReference) {
                         String secondaryId = 
                             ((IViewReference)partRef).getSecondaryId();
                         if (secondaryId != null) {
                             partId += "_" + secondaryId; //$NON-NLS-1$
                         }
                     }
 
                     if (partId == null || partId.trim().length() == 0) {
                         // Don't assign a name if the id is unusable
                         return;
                     }
                     partContent.setData(TEST_RCP_DATA_KEY, partId);
 
                     // Assign a corresponding id to the part's toolbar, if
                     // possible/usable.
                     Control partToolbar = 
                         workbenchPartRef.getPane().getToolBar();
                     if (partToolbar != null) {
                         partToolbar.setData(TEST_RCP_DATA_KEY, 
                                 partId + TOOLBAR_ID_SUFFIX);
                     }
                     
                     // A repaint is required in order for the aut component 
                     // hierarchy to notice the change.
                     Shell shell = PlatformUI.getWorkbench()
                         .getActiveWorkbenchWindow().getShell();
                     repaintToolbars(shell);
                 }
             }
 
         }
 
         /**
          * 
          * {@inheritDoc}
          */
         public void partVisible(IWorkbenchPartReference partRef) {
             partOpened(partRef);
         }
         
     }
 
     
     /**
      * {@inheritDoc}
      */
     public void earlyStartup() {
         final Properties envVars = 
             EnvironmentUtils.getProcessEnvironment();
         
         if (getValue(AutConfigConstants.AUT_AGENT_HOST, envVars) != null) {
             final IWorkbench workbench = PlatformUI.getWorkbench();
             final Display display = workbench.getDisplay();
             initAutServer(display, envVars);
 
             display.syncExec(new Runnable() {
                 public void run() {
                     // add GEF listeners (and listener appenders) for GEF, if available
                     if (Platform.getBundle(Startup.GEF_BUNDLE_ID) != null) {
                         m_gefListener = new GefPartListener();
                         AUTServer.getInstance().addInspectorListenerAppender(
                                 new GefInspectorListenerAppender());
                     }
                     
                     // add naming listener
                     ComponentNamer namer = new ComponentNamer();
                     display.addFilter(SWT.Paint, namer);
                     display.addFilter(SWT.Activate, namer);
 
                     // Add window listener
                     addWindowListener(workbench);
                     
                     IWorkbenchWindow window = 
                         workbench.getActiveWorkbenchWindow();
                     if (window != null) {
                         // Add part listeners
                         addPartListeners(window);
                         
                         // Handle existing parts
                         IWorkbenchPage [] pages = window.getPages();
                         for (int i = 0; i < pages.length; i++) {
                             IEditorReference[] editorRefs = 
                                 pages[i].getEditorReferences();
                             IViewReference[] viewRefs = 
                                 pages[i].getViewReferences();
                             for (int j = 0; j < editorRefs.length; j++) {
                                 partNamingListener.partOpened(editorRefs[j]);
                                 if (m_gefListener != null) {
                                     m_gefListener.partOpened(editorRefs[j]);
                                 }
                             }
                             for (int k = 0; k < viewRefs.length; k++) {
                                 partNamingListener.partOpened(viewRefs[k]);
                                 if (m_gefListener != null) {
                                     m_gefListener.partOpened(viewRefs[k]);
                                 }
                             }
                         }
 
                         // If a shell already exists, make sure that we get another
                         // chance to immediately add/use our naming listeners.
                         Shell mainShell = window.getShell();
                         if (mainShell != null && !mainShell.isDisposed()) {
                             repaintToolbars(mainShell);
                         }
                     }
                 }
 
             });
            // Registering the AdapterFactory for SWT at the registry
            GUIAdapterFactoryRegistry.getInstance()
                .registerFactory(new SWTAdapterFactory());
             // add listener to AUT
             AUTServer.getInstance().addToolKitEventListenerToAUT();
 
         }
 
     }
 
     /**
      * Initializes the AUT Server for the host application.
      * 
      * @param display The Display to use for the AUT Server.
      * @param envVars Environment variables to consult in configuring the 
      *                AUT Server.
      */
     private void initAutServer(Display display, Properties envVars) {
         ((SwtAUTServer)AUTServer.getInstance(CommandConstants
                 .AUT_SWT_SERVER)).setDisplay(display);
         AUTServer.getInstance().setAutAgentHost(getValue(
                 AutConfigConstants.AUT_AGENT_HOST, envVars));
         AUTServer.getInstance().setAutAgentPort(getValue(
                 AutConfigConstants.AUT_AGENT_PORT, envVars));
         AUTServer.getInstance().setAutID(getValue(
                 AutConfigConstants.AUT_NAME, envVars));
 
         AUTServer.getInstance().start(true);
     }
 
     /**
      * Adds a window listener to the given workbench. This listener adds a 
      * part naming listener to opening windows.
      * 
      * @param workbench The workbench to which the listener will be added.
      */
     private void addWindowListener(IWorkbench workbench) {
         workbench.addWindowListener(new IWindowListener() {
 
             public void windowActivated(IWorkbenchWindow window) {
                 addPartListeners(window);
             }
 
             public void windowClosed(IWorkbenchWindow window) {
                 // Do nothing
             }
 
             public void windowDeactivated(IWorkbenchWindow window) {
                 // Do nothing
             }
 
             public void windowOpened(IWorkbenchWindow window) {
                 addPartListeners(window);
             }
 
         });
     }
 
     /**
      * Fires a paint event on all Toolbars and Coolbars within the given shell.
      * 
      * @param mainShell The shell to search for Coolbars and Toolbars.
      */
     public static void repaintToolbars(Shell mainShell) {
         List toolbarList = new ArrayList();
         getToolbars(mainShell, toolbarList);
         Iterator iter = toolbarList.iterator();
         while (iter.hasNext()) {
             Control toolbar = (Control)iter.next();
             toolbar.update();
             toolbar.redraw();
             toolbar.update();
         }
     }
 
     /**
      * Adds all Coolbars and Toolbars within the given composite to the given
      * list. The search is is also performed recursively on children of the 
      * given composite.
      * 
      * @param composite The composite to search.
      * @param toolbarList The list to which found Toolbars and Coolbars will 
      * be added.
      */
     public static void getToolbars(Composite composite, 
         List toolbarList) {
         
         if (composite != null && !composite.isDisposed()) {
             Control [] children = composite.getChildren();
             for (int i = 0; i < children.length; i++) {
                 if (children[i] instanceof Composite) {
                     getToolbars((Composite)children[i], toolbarList);
                 }
                 try {
                     if (children[i] instanceof ToolBar
                             || children[i] instanceof CoolBar) {
 
                         toolbarList.add(children[i]);
                     }
                 } catch (NoClassDefFoundError e) {
                     // we may be running in eRCP which doesn't know about
                     // toolbars, so we just ignore this
                 }
             }
         }
     }
 
     /**
      * Add part listeners to the given window.
      * 
      * @param window The window to which the listeners will be added.
      */
     private void addPartListeners(IWorkbenchWindow window) {
         window.getPartService().addPartListener(partNamingListener);
         if (m_gefListener != null) {
             window.getPartService().addPartListener(m_gefListener);
         }
     }
 
     /**
      * Returns the value for a given property. First, <code>envVars</code> 
      * is checked for the given property. If this
      * property cannot be found there, the 
      * Java System Properties will be checked. If the property is not 
      * found there, <code>null</code> will be returned.
      * 
      * @param envVars The first source to check for the given property.
      * @param propName The name of the property for which to find the value.
      * @return The value for the given property name, or <code>null</code> if
      *         given property name cannot be found.
      */
     private String getValue(String propName, Properties envVars) {
         String value = 
             envVars.getProperty(propName);
         if (value == null) {
             value = System.getProperty(propName);
         }
         return value;
     }
 }
