 /*
  *  Copyright (C) 2004  The Concord Consortium, Inc.,
  *  10 Concord Crossing, Concord, MA 01742
  *
  *  Web Site: http://www.concord.org
  *  Email: info@concord.org
  *
  *  This library is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU Lesser General Public
  *  License as published by the Free Software Foundation; either
  *  version 2.1 of the License, or (at your option) any later version.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *  Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public
  *  License along with this library; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * END LICENSE */
 
 /*
  * Last modification information:
  * $Revision: 1.61 $
  * $Date: 2007-10-22 19:37:02 $
  * $Author: scytacki $
  *
  * Licence Information
  * Copyright 2004 The Concord Consortium 
 */
 package org.concord.otrunk.view;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.EventQueue;
 import java.awt.Rectangle;
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayOutputStream;
 import java.util.Vector;
 
 import javax.swing.BorderFactory;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JViewport;
 import javax.swing.SwingUtilities;
 
 import org.concord.framework.otrunk.OTControllerService;
 import org.concord.framework.otrunk.OTObject;
 import org.concord.framework.otrunk.view.OTControllerServiceFactory;
 import org.concord.framework.otrunk.view.OTFrameManager;
 import org.concord.framework.otrunk.view.OTJComponentService;
 import org.concord.framework.otrunk.view.OTJComponentServiceFactory;
 import org.concord.framework.otrunk.view.OTJComponentView;
 import org.concord.framework.otrunk.view.OTViewContainer;
 import org.concord.framework.otrunk.view.OTViewContainerChangeEvent;
 import org.concord.framework.otrunk.view.OTViewContainerListener;
 import org.concord.framework.otrunk.view.OTViewContext;
 import org.concord.framework.otrunk.view.OTViewEntry;
 import org.concord.framework.otrunk.view.OTViewFactory;
 import org.concord.otrunk.OTControllerServiceImpl;
 import org.concord.swing.util.ComponentScreenshot;
 
 
 /**
  * OTViewContainerPanel
  * Class name and description
  *
  * Date created: Jan 20, 2005
  *
  * @author scott<p>
  *
  */
 public class OTViewContainerPanel extends JPanel
 {
     /**
      * First version of this class
      */    
     private static final long serialVersionUID = 1L;
     
     OTObject currentObject = null;
     OTJComponentView currentView = null;
     OTViewEntry currentViewEntry = null;
     OTViewChild currentViewChild = null;    
     
     private OTJComponentService jComponentService;	
 	
 	protected OTFrameManager frameManager;
 
 	private boolean useScrollPane = true;
 	private boolean autoRequestFocus = true;
 	private boolean updateable = false;
 	private OTViewContainer parentContainer;
 	
 	Vector containerListeners = new Vector();
 	
 	MyViewContainer viewContainer;
 
 	/**
 	 * This is used to ignore the scrollRectToVisible method
 	 * both in the viewport and in the ourselves.  scrollRectToVisible
 	 * is called when the content of a child component is initialized.  One place
 	 * where it is called is when the caret position is changed during loading.
 	 * If the scrolling is not disabled then this causes the view to scroll to
 	 * the bottom.  If this view is embedded in another view then the scroll 
 	 * "event" is propgated to the parent and it is scrolled so the bottom of this
 	 * embedded view is visible.
 	 * 
 	 * This is a count, so that scrolling stays disabled until all of the 
 	 * scroll causing operations have finished.  This happens because setCurrentObject
 	 * is called multiple times not in the awt thread.  The first one disables
 	 * scrolling and queues up an invokeLater to enable it again.  Then the second
 	 * call to setCurrentObject happens before the queued up enableScrolling is
 	 * run.  And then the enableScrolling is run before the gui operations happen
 	 * from the second call. So without a counter, the scrolling would be enabled while some of theThis is a bit dangerous
 	 * so it would be better have some kind of logging so we can track this better.
 	 */
 	private int unwantedScrollingCount = 0;
 
 	private String viewMode = null;
 
 	private boolean topLevelContainer = false;
 	private OTControllerService controllerService;
 
 	private boolean scrollPanelHasBorder = true;
 	
 	private boolean showTempLoadingLabel = true;
 
 	private OTObject previousObject;
 	
 	/**
 	 * 
 	 */
 	public OTViewContainerPanel(OTFrameManager frameManager)
 	{
 		super(new BorderLayout());
		
		// using Box layout helps several things
		// but it affects lots of code, so we should try to turn it on when we have some
		// breathing room
		//setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
 
 		viewContainer = new MyViewContainer();
 		
 		this.frameManager = frameManager;
 		JLabel loadingLabel = new JLabel("Loading...");
 		add(loadingLabel);		
 	}
 	
 	public OTJComponentService getOTJComponentService()
 	{
 		return jComponentService;
 	}
 	
 	/**
 	 * This method is for legacy the object really only needs 
 	 * OTJComponentService.  But to make the migration easier this method is still
 	 * available.  And each time it is called more a new jComponentService is
 	 * created. 
 	 * 
 	 * @param factory
 	 */
 	public void setOTViewFactory(OTViewFactory factory)
 	{
 		// add our own controller service factory if we have been setup to do so.
 		if(isTopLevelContainer()){			
 			OTControllerServiceFactory controllerServiceFactory = new OTControllerServiceFactory(){
 
 				public OTControllerService createControllerService()
                 {
 					OTControllerService subControllerService = 
 						((OTControllerServiceImpl) controllerService).createSubControllerService();						
 					return subControllerService;
                 }
 				
 			};
 			OTViewContext factoryContext = factory.getViewContext();
 			factoryContext.addViewService(OTControllerServiceFactory.class, controllerServiceFactory);
 		} 
 
 		// get the OTJComponentService so we can create the OTJComponentView
 		OTViewContext viewContext = factory.getViewContext();
 		OTJComponentServiceFactory componentServiceFactory =
 			(OTJComponentServiceFactory) viewContext.getViewService(OTJComponentServiceFactory.class);
 		jComponentService = componentServiceFactory.createOTJComponentService(factory, false);		
 	}
 		
 	public boolean isTopLevelContainer()
     {
 		return topLevelContainer ;
     }
 
 	/**
 	 * This is the preferred method for giving this object the ability to create new views
 	 * Using this method will allow this object to share the jComponentService with
 	 * other viewContainerPanels or view creators.  This way those views can access
 	 * each other though the OTViewHost interface.
 	 * 
 	 * @param jComponentService
 	 */
 	public void setOTJComponentService(OTJComponentService jComponentService)
 	{
 		this.jComponentService = jComponentService;
 	}
 	
 	public void setMessage(String message)
 	{
 	    removeAll();
 	    add(new JLabel(message));
 	}
 	
 	public OTObject getCurrentObject()
 	{
 	    return currentObject;
 	}
 	
 	public void setCurrentObject(OTObject otObject)
 	{
 		setCurrentObject(otObject, null);
 	}
 		
 	public void setCurrentObject(OTObject otObject, OTViewEntry viewEntry)
 	{
 		if(currentView != null) {
 			try {
 				// Call on the event thread if we're not already on it
 				if (EventQueue.isDispatchThread()) {
 					currentView.viewClosed();
 				} else {
 					EventQueue.invokeAndWait(new Runnable() {
 						public void run() {
 							currentView.viewClosed();
                         }
 					});
 				}
 			} catch (Throwable t) {
 				// attempting to close the view caused some form of exception
 				// print the exception and keep going.  This might cause later
 				// instability.  So this type of event should trigger a message
 				// back to developers, so we can track down the problem.
 				System.err.println("Exception while closing view: " + currentView);
 				t.printStackTrace();
 			}
 		    currentView = null;
 		}
 		
 		if(controllerService != null){
 			controllerService.dispose();
 		}
 		
 		if (currentObject != null){
 			previousObject = currentObject;
 		}
 		// FIXME There might already be an event queued to setup this current object
 		// We should compare the last queued event with this request and 
 		// have the option to not queue this event.
 		
 		currentObject = otObject;
 		currentViewEntry = viewEntry;
 		currentViewChild = null;
 		
 		if (otObject instanceof OTViewChild){
 			OTViewChild viewChild = (OTViewChild) otObject; 
 						
 			currentObject = viewChild.getObject();
 			if (viewChild.getViewid() != null){
 				currentViewEntry = viewChild.getViewid();
 			}
 			
 			// We save the currentViewChild so its settings can be used for the scroll pane
 			// The scroll settings from the viewChild are not simply set here because 
 			// some views reuse their container panels, and set the scroll properties 
 			// when the panel is created.  Those settings should be preserved
 			// even if a viewChild temporarily overrides them.
 			currentViewChild = viewChild;
 		}		
 		
 		removeAll();
 		
 		// Even if if the object is null we want to show something on the screen
 		// so we should not give up if the object is null.
 		// if(otObject == null){
 		//	return;
 		// }
 		
 		if(isTopLevelContainer() && otObject != null){
 			controllerService = otObject.getOTObjectService().createControllerService();
 		}
 		
 		disableScrolling();
 
 		// Unfortunately the size of this label matters, when these objects
 		// are embedded in tables inside of the htmleditorkit.  I think the
 		// editorkit gets messed up when the width of a component changes.
 		// It seems to be a problem only when it shrinks, not when it grows.	
 		// so instead of this:
         // JLabel loading = new JLabel("Loading...");
 		// we'll use this which is really short.
 		if (showTempLoadingLabel){
     		JLabel loading = new JLabel("...");
     		loading.setBorder(BorderFactory.createLineBorder(Color.black));
     		add(loading);
 		}
 		
 		revalidate();				
 		
 		// By doing a double invokeLater we make sure this code doesn't get
 		// run until much later in the process.  Unless something else is doing
 		// an double invoke latter, this will execute after all of the currently
 		// queued up code which calls invokeLater.
 		// For some reason this allows popups to incrementally draw them selves
 		// without it, the popup blocks until the inside content is rendered
 		// before showing anything
 		Runnable createComponentTask = new CreateComponentTask(currentObject);
 
 		/**
 		 * This is disabled for now but we might have to come back to it it
 		 * fixed a loading problems with sidebars that were popup windows.
 		 * 
 		 * Only do a double invoke later if the item is a sidebar item
 		 * 
 		 *
 		String otObjectClassName = otObject.getClass().getInterfaces()[0].getName();
 		System.out.println(otObjectClassName);
 		if(otObject.getClass().getInterfaces()[0].getName().endsWith("OTUDLSideBarItem")){
 			System.out.println("delaying sidebar item update");
 			SwingUtilities.invokeLater(new Runnable(){
 				public void run()
 				{
 
 					SwingUtilities.invokeLater(createComponentTask);
 				} 
 			});			
 		} else {
 			SwingUtilities.invokeLater(createComponentTask);
 		}
 		*/
 		
 		SwingUtilities.invokeLater(createComponentTask);
 	}
 	
 	public void setScrollPanelHasBorder(boolean scrollPanelHasBorder){
 		this.scrollPanelHasBorder  = scrollPanelHasBorder;
 	}
 
 	/**
 	 * This method trys to make a view of the current object and viewEntry if 
 	 * there is one.
 	 * 
 	 * @return
 	 */
 	protected JComponent createJComponent()
 	{
 		if(currentObject == null) {
 			return new JLabel("Null object");
 		}
 		
 		// get the OTJComponentService so we can create the OTJComponentView
 		OTJComponentService jComponentService = getOTJComponentService();
 		
 		currentView = jComponentService.getObjectView(currentObject, viewContainer,
 				getViewMode(), currentViewEntry);		
 
 		if(currentView == null) {
 			return new JLabel("No view for object: " + currentObject);
 		} 
 		if (currentView instanceof AbstractOTJComponentContainerView){
 			((AbstractOTJComponentContainerView)currentView).setMode(getViewMode());
 		}
 		
 		return jComponentService.getComponent(currentObject, currentView);
 	}
 	
 	/* (non-Javadoc)
 	 * @see javax.swing.JComponent#scrollRectToVisible(java.awt.Rectangle)
 	 */
 	public void scrollRectToVisible(Rectangle aRect) 
 	{
 		// disabling this removes the flicker that occurs during the loading of the page.
 		// if we could 
 		if(!isScrollingAllowed()){
 			return;
 		}
 
 		super.scrollRectToVisible(aRect);					
 	}
 	
 	protected boolean isScrollingAllowed()
 	{
 		return unwantedScrollingCount <=0;
 	}
 	
 	protected void disableScrolling()
 	{
 		// System.out.println("disable Scrolling: " + unwantedScrollingCount + " " 
 		//		+ currentObject.getGlobalId());
 		unwantedScrollingCount++;
 	}
 	
 	protected void enableScrolling()
 	{
 		unwantedScrollingCount--;
 		//System.out.println("enabling Scrolling: " + 
 		//		unwantedScrollingCount + " " + 
 		//		currentObject.getGlobalId().toString());
 
 		if(unwantedScrollingCount < 0){
 			System.err.println("unwantedScrollingCount dropped below 0");
 		}
 	}
 	
     public Component getCurrentComponent()
     {   
     	Component currentComp = getComponent(0);
     	if(currentComp instanceof JScrollPane) {
     		currentComp = ((JScrollPane)currentComp).getViewport().getView();
     	}
 
         return currentComp;
     }
     
 	public void addViewContainerListener(OTViewContainerListener listener)
 	{
 	    containerListeners.add(listener);
 	}
 	
 	public void removeViewContainerListener(OTViewContainerListener listener)
 	{
 	    containerListeners.remove(listener);
 	}
 	
 	public void notifyListeners()
 	{
 		OTViewContainerChangeEvent evt;
 		if (currentObject == null){
 			evt = new OTViewContainerChangeEvent(viewContainer, OTViewContainerChangeEvent.DELETE_CURRENT_OBJECT_EVT);
 		} else if (previousObject == null){
 			evt = new OTViewContainerChangeEvent(viewContainer, OTViewContainerChangeEvent.NEW_CURRENT_OBJECT_EVT);
 		} else if (previousObject == currentObject){
 			evt = new OTViewContainerChangeEvent(viewContainer, OTViewContainerChangeEvent.CHANGE_CURRENT_OBJECT);
 		} else {
 			evt = new OTViewContainerChangeEvent(viewContainer, OTViewContainerChangeEvent.REPLACE_CURRENT_OBJECT_EVT, previousObject);
 		}
 		
 	    for(int i=0; i<containerListeners.size(); i++) {
 	        ((OTViewContainerListener)containerListeners.get(i)).
 	        	currentObjectChanged(evt);
 	        	
 	    }
 	}
 	
 	private final class CreateComponentTask
         implements Runnable
     {
 		private OTObject componentObject;
 
 		public CreateComponentTask(OTObject otObject)
 		{
 			this.componentObject = otObject;
 		}
 		
 	    public void run()
 	    {
 	    	if(currentObject != componentObject) {
 	    		// the object has been updated since this task started.  
 	    		// so we don't need to do anything here, because there will
 	    		// be another task happening later.
 	    		return;
 	    	}
 	    	
 	    	JComponent myComponent = createJComponent() ; 
 
 	    	boolean localUseScrollPane = isUseScrollPane();
 	    	if(currentViewChild != null){
 	    		localUseScrollPane = currentViewChild.getUseScrollPane();
 	    	}
 
 	    	if(localUseScrollPane) {
 	    		JScrollPane scrollPane = new JScrollPane();
 	    		scrollPane.setOpaque(false);
 	    		
 	    		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 	    		
 	    		scrollPane.setViewport(new JViewport(){
 	    			/**
 	    			 * Not intended to be serialized, just added remove compile warning
 	    			 */
 	    			private static final long serialVersionUID = 1L;
 	    			
 	    			public void scrollRectToVisible(Rectangle contentRect) {
 	    				// disabling this removes the flicker that occurs during the loading of the page.
 	    				// if we could 
 	    				if(!isScrollingAllowed()){
 	    					return;
 	    				}
 
 	    				super.scrollRectToVisible(contentRect);							
 	    			}
 	    		});
 	    		scrollPane.setViewportView(myComponent);
 	    		
 	    		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 	    		
 	        	boolean localScrollPanelHasBorder = scrollPanelHasBorder;
 	        	if(currentViewChild != null){
 	        		localScrollPanelHasBorder = currentViewChild.getScrollPanelHasBorder();
 	        	}
 	    		if (!localScrollPanelHasBorder){
 	    			scrollPane.setBorder(BorderFactory.createEmptyBorder());
 	    		}
 	    		
 	    		myComponent = scrollPane;
 	    	}
 	    	removeAll();
 	    	add(myComponent, BorderLayout.CENTER);
 	    	
 	    	revalidate();
 	    	notifyListeners();
 	    	if(isAutoRequestFocus()){
 	    		myComponent.requestFocus();
 	    	}
 	    	
 	    	// We have to queue this up, because during the setup of this
 	    	// component other things might be queued, that cause scrolling
 	    	// to happen. 
 	    	// this way the scrolling should remain disabled until all 
 	    	// of them are complete.
 	    	SwingUtilities.invokeLater(new Runnable(){
 	    		/* (non-Javadoc)
 	    		 * @see java.lang.Runnable#run()
 	    		 */
 	    		public void run() {
 	    			enableScrolling();
 	    		}
 	    	});
 	    }
     }
 
 	/**
 	 * Internal class so views which get passed a view container do not
 	 * have direct access to the viewcontainer panel.  This also makes it
 	 * easier to see who is using view containers and who is using 
 	 * the viewcontainerpanel directly
 	 * 
 	 * @author scott
 	 *
 	 */
 	class MyViewContainer implements OTViewContainer {
 		public OTObject getCurrentObject() {
 		    return OTViewContainerPanel.this.getCurrentObject();
 		}
 		public void setCurrentObject(OTObject pfObject) {
 			OTViewContainerPanel.this.setCurrentObject(pfObject);
 		}
 		
 		public void setCurrentObject(OTObject otObject, OTViewEntry viewEntry){
 			OTViewContainerPanel.this.setCurrentObject(otObject, viewEntry);
 		}
 
 		public boolean isUpdateable() {
 	        return OTViewContainerPanel.this.isUpdateable();
         }
 		public void setUpdateable(boolean b) {
 			OTViewContainerPanel.this.setUpdateable(b);
         }
 		
 		public void setParentContainer(OTViewContainer c) {
 			OTViewContainerPanel.this.setParentContainer(c);
 		}
 		
 		public OTViewContainer getParentContainer() {
 			return OTViewContainerPanel.this.getParentContainer();
 		}
 		
 		public OTViewContainer getUpdateableContainer() {
 			return OTViewContainerPanel.this.getUpdateableContainer();
 		}
 		
 		public void reloadView(){
 			OTViewContainerPanel.this.reloadView();
 		}
 		public OTViewEntry getCurrentViewEntry()
         {
 			return currentViewEntry;
         }
 	}
 
 	/**
 	 * Return the viewContainer of this bodyPanel, if you plan on changing
 	 * the currentObject of this container you should call getUpdateableContainer.
 	 * @return
 	 */
 	public OTViewContainer getViewContainer() 
 	{
 		return viewContainer;
 	}
 	
 	public void reloadView()
     {
 		setCurrentObject(currentObject, currentViewEntry);
     }
 
 	public OTViewContainer getUpdateableContainer()
     {
 	    if (this.isUpdateable()) {
 	    	return this.viewContainer;
 	    } else {
 	    	if(parentContainer == null){
 	    		System.err.println("No updatable parent container was found returning the root container");
 	    		return this.viewContainer;
 	    	}
 	    	return parentContainer.getUpdateableContainer();
 	    }
     }
 
 	public OTJComponentView getView() {
 		return currentView;
 	}
 
 	public boolean isUseScrollPane() {
 		return useScrollPane;
 	}
 
 	public void setUseScrollPane(boolean useScrollPane) {
 		this.useScrollPane = useScrollPane;
 	}
 
 	public boolean isAutoRequestFocus() {
 		return autoRequestFocus;
 	}
 
 	public void setAutoRequestFocus(boolean autoRequestFocus) {
 		this.autoRequestFocus = autoRequestFocus;
 	}
 
 	public boolean isUpdateable() {
 		return updateable;
 	}
 	
 	public void setUpdateable(boolean b) {
 		this.updateable = b;
 	}
 	/**
 	 * @param viewMode
 	 */
 	public void setViewMode(String viewMode) 
 	{
 		this.viewMode  = viewMode;
 		if (viewMode == null){
 			this.viewMode = jComponentService.getViewFactory().getDefaultViewMode();
 		}
 	}
 	
 	public String getViewMode()
 	{
 		return viewMode;
 	}
 	
 	public void saveScreenshotAsByteArrayOutputStream(ByteArrayOutputStream out, String type)
 	throws Throwable
 	{
 		ComponentScreenshot.saveScreenshotAsOutputStream(this, out, type);
 	}
 	
 	/**
 	 * 
 	 * @param type "png" or "gif" are best bets
 	 * @return ByteArrayOutputStream representing image
 	 * @throws Throwable
 	 */
 	public ByteArrayOutputStream getScreenshotAsByteArrayOutputStream(String type)
 	throws Throwable
 	{
 		return ComponentScreenshot.saveScreenshotAsByteArrayOutputStream(this, type);
 		
 	}
 	
 	public BufferedImage getScreenShot() throws Exception
 	{
 		BufferedImage image = ComponentScreenshot.getScreenshot(this);
 		return image;
 	}
 	
 	public void setParentContainer(OTViewContainer parentContainer)
     {
 	    this.parentContainer = parentContainer;
     }
 
 	public OTViewContainer getParentContainer()
     {
 	    return parentContainer;
     }
 
 	public void setTopLevelContainer(boolean topLevelContainer)
     {
     	this.topLevelContainer = topLevelContainer;
     }
 
 	public boolean isShowTemporaryLoadingLabel()
     {
     	return showTempLoadingLabel;
     }
 
 	public void setShowTemporaryLoadingLabel(boolean showTempLoadingLabel)
     {
     	this.showTempLoadingLabel = showTempLoadingLabel;
     }
 }
