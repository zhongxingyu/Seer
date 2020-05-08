 
 /*
  *  Copyright (C) 2004  The Concord Consortium, Inc.,
  *  10 Concord Crossing, Concord, MA 01741
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
  */
 
 /*
  * Last modification information:
 * $Revision: 1.6 $
 * $Date: 2005-05-06 21:31:31 $
  * $Author: scytacki $
  *
  * Licence Information
  * Copyright 2004 The Concord Consortium 
 */
 package org.concord.otrunk.view;
 
 import java.awt.BorderLayout;
 import java.awt.LayoutManager;
 import java.util.Vector;
 
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import org.concord.framework.otrunk.OTObject;
 import org.concord.framework.otrunk.view.OTFrame;
 import org.concord.framework.otrunk.view.OTObjectView;
 import org.concord.framework.otrunk.view.OTViewContainer;
 import org.concord.framework.otrunk.view.OTViewContainerListener;
 
 
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
 	implements OTViewContainer
 {
     OTObject currentObject = null;
     OTObjectView currentView = null;
     
 	private static OTViewFactory otViewFactory;
 	
 	protected OTFrameManager frameManager;
 
 	private JFrame myFrame;
 
 	Vector containerListeners = new Vector();
 	
 	public static void setOTViewFactory(OTViewFactory factory)
 	{
 		otViewFactory = factory;
 	}
 		
 	/**
 	 * 
 	 */
 	public OTViewContainerPanel(OTFrameManager frameManager, JFrame frame)
 	{	
 		super(new BorderLayout());
 		this.frameManager = frameManager;
 		myFrame = frame;
 	}
 
 	public void showFrame()
 	{
 		myFrame.setVisible(true);
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.concord.otrunk.view.OTViewContainer#setCurrentObject(org.concord.framework.otrunk.OTObject, org.concord.otrunk.view.OTFrame)
 	 */
 	public void setCurrentObject(OTObject pfObject, OTFrame otFrame)
 	{
 		if(otFrame != null) {
 			frameManager.setFrameObject(pfObject, otFrame);
 			return;
 		}
 		
 		if(currentView != null) {
 		    currentView.viewClosed();
 		}
 		
 		currentObject = pfObject;
 		
 		JComponent newComponent = null;
 		if(pfObject != null) {
 		    currentView = otViewFactory.getObjectView(pfObject, this);
 		    if(currentView == null) {
 		        newComponent = new JLabel("No view for object: " + pfObject);
 				
 
 		    } else {
 		        newComponent = currentView.getComponent(true);
 		    }
 		} else {
 			newComponent = new JLabel("Null object");
 		}
 
 		removeAll();
 		add(newComponent, BorderLayout.CENTER);
 		revalidate();
 		notifyListeners();
		newComponent.requestFocus();
 	}
 
 	public OTObject getCurrentObject()
 	{
 	    return currentObject;
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.concord.otrunk.view.OTViewContainer#getComponent(org.concord.framework.otrunk.OTObject, org.concord.otrunk.view.OTViewContainer, boolean)
 	 */
 	public JComponent getComponent(OTObject pfObject,
 			OTViewContainer container, boolean editable)
 	{
 	    return otViewFactory.getComponent(pfObject, container, editable);
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
 	    for(int i=0; i<containerListeners.size(); i++) {
 	        ((OTViewContainerListener)containerListeners.get(i)).
 	        	currentObjectChanged(this);
 	        	
 	    }
 	}
 }
