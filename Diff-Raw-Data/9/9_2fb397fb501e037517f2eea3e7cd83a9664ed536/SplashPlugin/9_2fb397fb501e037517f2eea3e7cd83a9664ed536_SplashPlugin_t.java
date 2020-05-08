 package org.amanzi.splash.ui;
 
 /*
  * "The Java Developer's Guide to Eclipse"
  *   by D'Anjou, Fairbrother, Kehn, Kellerman, McCarthy
  * 
  * (C) Copyright International Business Machines Corporation, 2003, 2004. 
  * All Rights Reserved.
  * 
  * Code or samples provided herein are provided without warranty of any kind.
  */
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.amanzi.neo.services.ui.NeoServiceProviderUi;
 import org.amanzi.splash.database.services.SpreadsheetService;
 import org.amanzi.splash.utilities.Messages;
 import org.amanzi.splash.utilities.NeoSplashUtil;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.viewers.IOpenListener;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.OpenEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.ui.IPartListener2;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchPartReference;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.osgi.framework.BundleContext;
 import org.rubypeople.rdt.core.IRubyElement;
 import org.rubypeople.rdt.core.IRubyProject;
 import org.rubypeople.rdt.internal.ui.packageview.PackageExplorerPart;
 import org.rubypeople.rdt.ui.RubyUI;
 
 /**
  * User interface plug-in for mini-spreadsheet editor.
  */
 public class SplashPlugin extends AbstractUIPlugin {
 	static private SplashPlugin plugin;
 	
 	//Lagutko, 29.07.2009, additional field
 	/*
 	 * Field for Spreadsheet service
 	 */
 	private SpreadsheetService spreadsheetService;
 	
 	/*
 	 * PartListener for RubyExplorer
 	 */
 	private RubyExplorerListener rubyExplorerListener;
 	
 	/*
 	 * OpenListener for RubyExplorer
 	 */
 	private SpreadsheetOpenListener openListener;
 
 
 	
 	/**
 	 * Constructor for SplashPlugin.
 	 */
 	public SplashPlugin() {
 		super();
 		plugin = this;
 		
 	}
 	
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 		plugin = this;
 		//Tsinkel, add resource listener
 		org.eclipse.core.resources.ResourcesPlugin.getWorkspace().addResourceChangeListener(new EditorListener(),IResourceChangeEvent.POST_CHANGE);
		
         try {
             registerOpenSplashListenerInRDT();
         } catch (RuntimeException e) {
             System.err.println("Failed to initialize RDT-Splash open-splash listener: "+e);
             //e.printStackTrace(System.err);
         }
 	}
 
     private void registerOpenSplashListenerInRDT() {
         //Lagutko: 11.08.2009, listener for RubyExplorer activation
         // TODO fixed listener
         IWorkbenchWindow activeWorkbenchWindow = getWorkbench().getActiveWorkbenchWindow();
         if (activeWorkbenchWindow != null) {
             rubyExplorerListener = new RubyExplorerListener();
             activeWorkbenchWindow.getPartService().addPartListener(rubyExplorerListener);
 
             // registers listener on open view;
             IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
             if (activePage != null) {
                 IViewPart rubyView = activePage.findView(RubyUI.ID_RUBY_EXPLORER);
                 if (rubyView != null) {
                     registerOpenListener(rubyView);
                 }
             }
         }
     }
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
 	 */
 	public void stop(BundleContext context) throws Exception {
 	    unregisterOpenSplashListenerInRDT();
         NeoServiceProviderUi.getProvider().stopNeo();
 		super.stop(context);
 	}
 
     private void unregisterOpenSplashListenerInRDT() {
 	    if (rubyExplorerListener != null) {
 	        if ((getWorkbench() != null) && (getWorkbench().getActiveWorkbenchWindow() != null)) {
 	            getWorkbench().getActiveWorkbenchWindow().getPartService().removePartListener(rubyExplorerListener);
 	        }
 	    }
 		plugin = null;
     }
 	
 	/**
 	 * Return the plug-in ID.
 	 */	
 	public static String getId() {
 		return plugin.getBundle().getSymbolicName();
 	}
 	
 	/**
 	 * Returns the shared instance.
 	 */
 	public static SplashPlugin getDefault() {
 		return plugin;
 	}	
 	
 	public SpreadsheetService getSpreadsheetService() {
	    if (spreadsheetService == null) {
	        //Lagutko, 17.11.2010, initialize SpreadsheetService
	        spreadsheetService = new SpreadsheetService();
	    }
 	    return spreadsheetService;
 	}
 	
 	
 
     /**
      * Print a message and information about exception to Log
      *
      * @param message message
      * @param e exception
      */
 
     public static void error(String message, Throwable e) {
         getDefault().getLog().log(new Status(IStatus.ERROR, getId(), 0, message == null ? "" : message, e)); //$NON-NLS-1$
     }
     
     /**
      * Registers Listener for Open Event in Ruby Project Tree
      *
      * @param viewPart viewPart of RubyExplorer
      */
     private void registerOpenListener(IWorkbenchPart viewPart) {
         PackageExplorerPart explorer = (PackageExplorerPart)viewPart;
         openListener = new SpreadsheetOpenListener();
         explorer.getTreeViewer().addOpenListener(openListener);
     }
     
     /**
      * Unregisters Listener for Open Event in Ruby Project Tree
      *
      * @param viewPart viewPart of RubyExplorer
      */
     private void unregisterOpenListener(IWorkbenchPart viewPart) {
         PackageExplorerPart explorer = (PackageExplorerPart)viewPart;
         if (openListener != null) {
             explorer.getTreeViewer().removeOpenListener(openListener);
         }
     }
     
     /**
      * Listener for Open Event
      * 
      * @author Lagutko_N
      */
     private class SpreadsheetOpenListener implements IOpenListener {
 
         @Override
         public void open(OpenEvent event) {
             ISelection selection = event.getSelection();
             if (selection instanceof StructuredSelection) {
                 StructuredSelection structuredSelection = (StructuredSelection)selection;                
                 
                 IRubyElement rubyElement = (IRubyElement)structuredSelection.getFirstElement();
                 //check is the element is Spreadsheet
                 if (rubyElement.getElementType() == IRubyElement.SPREADSHEET) {
                     //if Spreadsheet than open it in Editor
                     IRubyProject rubyProject = rubyElement.getRubyProject();
                     
                     try {
                         URL spreadsheetURL = NeoSplashUtil.getSpeadsheetURL(rubyElement.getElementName());
                     
                         NeoSplashUtil.openSpreadsheet(getWorkbench(), spreadsheetURL, rubyProject.getElementName());
                     }
                     catch (MalformedURLException e) {
                         ErrorDialog.openError(PlatformUI.getWorkbench().getDisplay().getActiveShell(), 
                                               Messages.Open_Spreadsheet_Error_Title,
                                               Messages.Open_Spreadsheet_Error_Message,
                                               new Status(Status.ERROR, SplashPlugin.getId(), Messages.Open_Spreadsheet_Error_Message, e));
                     }
                 }
             }
         }
         
     }
     
     /**
      * Listener of ViewParts
      * 
      * @author Lagutko_N
      */
     private class RubyExplorerListener implements IPartListener2 {
 
         @Override
         public void partActivated(IWorkbenchPartReference partRef) {
             
         }
 
         @Override
         public void partBroughtToTop(IWorkbenchPartReference partRef) {
         }
 
         @Override
         public void partClosed(IWorkbenchPartReference partRef) {
             if (partRef.getId().equals(RubyUI.ID_RUBY_EXPLORER)) {
                 //if was closed RubyExplorer than unregister OpenListener
                 unregisterOpenListener(partRef.getPart(false));
             }
         }
 
         @Override
         public void partDeactivated(IWorkbenchPartReference partRef) {
         }
 
         @Override
         public void partHidden(IWorkbenchPartReference partRef) {
         }
 
         @Override
         public void partInputChanged(IWorkbenchPartReference partRef) {
         }
 
         @Override
         public void partOpened(IWorkbenchPartReference partRef) {
             if (partRef.getId().equals(RubyUI.ID_RUBY_EXPLORER)) {
                 //if was activated RubyExplorer than register OpenListener
                 registerOpenListener(partRef.getPart(false));
             }
         }
 
         @Override
         public void partVisible(IWorkbenchPartReference partRef) {
         }
 
         
     }
 }
