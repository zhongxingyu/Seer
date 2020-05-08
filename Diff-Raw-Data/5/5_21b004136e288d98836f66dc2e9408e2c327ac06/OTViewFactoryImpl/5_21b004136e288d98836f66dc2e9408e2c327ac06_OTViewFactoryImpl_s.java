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
  * Created on Jan 11, 2005
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package org.concord.otrunk.view;
 
 import java.util.Vector;
 
 import org.concord.framework.otrunk.OTID;
 import org.concord.framework.otrunk.OTObject;
 import org.concord.framework.otrunk.OTObjectList;
 import org.concord.framework.otrunk.OTObjectMap;
 import org.concord.framework.otrunk.OTObjectService;
 import org.concord.framework.otrunk.view.OTRequestedViewEntryAware;
 import org.concord.framework.otrunk.view.OTView;
 import org.concord.framework.otrunk.view.OTViewContext;
 import org.concord.framework.otrunk.view.OTViewContextAware;
 import org.concord.framework.otrunk.view.OTViewEntry;
 import org.concord.framework.otrunk.view.OTViewEntryAware;
 import org.concord.framework.otrunk.view.OTViewFactory;
 import org.concord.otrunk.OTrunkUtil;
 
 /**
  * @author scytacki
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 public class OTViewFactoryImpl implements OTViewFactory 
 {
     OTViewFactoryImpl parent;
     Vector viewMap = new Vector();
     OTViewBundle viewBundle;
     OTViewContext viewContext;
 	private String mode;
     
     public OTViewFactoryImpl(OTViewBundle viewBundle)
     {
         this.viewBundle = viewBundle;
         
         // read in all the viewEntries and create a vector 
         // of class entries.
         OTObjectList viewEntries = viewBundle.getViewEntries();
         
         for(int i=0; i<viewEntries.size(); i++) {
             OTViewEntry entry = (OTViewEntry)viewEntries.get(i);
             addViewEntry(entry);
         }
         
         initServices();
     }
     
     protected OTViewFactoryImpl(OTViewFactoryImpl parent)
     {
         this.parent = parent;
         
         initServices();
     }
     
     protected void initServices()
     {    	
     	OTViewContext viewContextParent = null;
     	if(parent != null){
     		viewContextParent = parent.getViewContext();
     	}
         viewContext = new OTViewContextImpl(this, viewContextParent);  
     }
     
     class InternalViewEntry {
         Class objectClass;
         Class viewClass;
         OTID otEntryID;
         
         OTViewEntry getOTViewEntry(OTObject requestingObject)
         {
         	OTObjectService objService = requestingObject.getOTObjectService();
         	OTViewEntry otViewEntry;
             try {
 	            otViewEntry = (OTViewEntry) objService.getOTObject(otEntryID);
             } catch (Exception e) {
 	            // TODO Auto-generated catch block
 	            e.printStackTrace();
 	            return null;
             }
         	return otViewEntry;
         }
     }
 
     public OTViewFactory createChildViewFactory()
     {
     	return new OTViewFactoryImpl(this);
     }
     
     /**
 	 * @see org.concord.otrunk.view.OTViewFactory#getView(org.concord.framework.otrunk.OTObject, java.lang.Class)
 	 */
     public OTView getView(OTObject otObject, Class viewInterface)
     {
     	return getView(otObject, viewInterface, null);
     }
 
 	/**
 	 * @see org.concord.framework.otrunk.view.OTViewFactory#getView(org.concord.framework.otrunk.OTObject, java.lang.Class, java.lang.String)
 	 */
 	public OTView getView(OTObject otObject, Class viewInterface, String modeStr) 
 	{		
 		InternalViewEntry entry = getViewInternal(otObject, viewInterface);
 		if(entry == null) {
 			return null;
 		}
 		OTViewEntry viewEntry = entry.getOTViewEntry(otObject);
 		return getView(otObject, viewEntry, modeStr);
 	}
     
     protected void initView(OTView view, OTViewEntry viewEntry)
     {
         if(view != null) { 
         	if(view instanceof OTViewContextAware) {
         		((OTViewContextAware)view).setViewContext(viewContext);
         	}
         	
             if(view instanceof OTViewEntryAware) {
                 ((OTViewEntryAware)view).setViewEntry(viewEntry);
             }            
         }    	
     }
     
 	public OTView getView(OTObject otObject, OTViewEntry viewEntry) 
 	{
 		return getView(otObject, viewEntry, null);		
 	}
 	
 	private OTView getViewInternal(OTObject otObject, OTViewEntry viewEntry) 
 	{
 		// because we have the view entry we don't need to actually
 		// look up this view.
 		
         String viewClassStr = viewEntry.getViewClass();
         String objClassStr = viewEntry.getObjectClass();        
         
         ClassLoader loader = getClass().getClassLoader();
 		
         try {
             Class objectClass = loader.loadClass(objClassStr);
 
             if(!objectClass.isInstance(otObject)){
         		throw new RuntimeException("viewEntry: " + viewEntry + 
         				" cannot handle otObject: " + otObject);
         	}
             
             if(viewClassStr == null){
             	throw new RuntimeException("viewEntry " + viewEntry + 
             			" has a null viewClass");
             }
             
             OTView view = null;
             Class viewClass = loader.loadClass(viewClassStr);
             view = (OTView)viewClass.newInstance();
 
             initView(view, viewEntry);
         	return view;                       
         } catch (ClassNotFoundException e) {
             System.err.println("Can't find view: " + viewClassStr + 
                     " for object: " + objClassStr);
             System.err.println("  error: " + e.toString());
         } catch (InstantiationException e) {
         	e.printStackTrace();
         } catch (IllegalAccessException e) {
         	e.printStackTrace();
         }
 		
 		return null;
 	}
     
     private InternalViewEntry getViewInternal(OTObject otObject, Class viewInterface)
     {
     	InternalViewEntry match = null;
         for(int i=0; i<viewMap.size(); i++) {
             InternalViewEntry entry = (InternalViewEntry)viewMap.get(i);
             // FIXME this should map
             // 
             if(entry.objectClass.isInstance(otObject) &&
             		viewInterface.isAssignableFrom(entry.viewClass)) {
             	match = entry;
             	break;
             }
         }
 
         // can't find the view in our own list
         // check parent
         if(match == null && parent != null) {
             match = parent.getViewInternal(otObject, viewInterface);
         }
         
         return match;        
     }
     
     /* (non-Javadoc)
 	 * @see org.concord.otrunk.view.OTViewFactory#addViewEntry(java.lang.Class, java.lang.Class)
 	 */
     public void addViewEntry(OTViewEntry entry)
     {
         String objClassStr = entry.getObjectClass();
         String viewClassStr = entry.getViewClass();
 
         ClassLoader loader = getClass().getClassLoader();
         
         try {
             InternalViewEntry internalEntry = new InternalViewEntry();
             internalEntry.objectClass = loader.loadClass(objClassStr);
             
             if(viewClassStr != null){
             	internalEntry.viewClass = loader.loadClass(viewClassStr);
             }
 
             internalEntry.otEntryID = entry.getGlobalId();
             viewMap.add(internalEntry);
             
         } catch (ClassNotFoundException e) {
             System.err.println("Can't find view: " + viewClassStr + 
                     " for object: " + objClassStr);
             System.err.println("  error: " + e.toString());
         }
 
     }
 
     /**
      * This will return the viewEntry setup by this mode.  If no viewEntry is 
      * found for this mode, then it will return null. 
      * 
      * If modeStr == null then getDefaultViewMode will be used to look up the 
      * view mode.
      * If modeStr equals NO_VIEW_MODE then this method will return null;
      * 
      * @param viewEntry
      * @param modeStr
      * @return
      */
     protected OTViewEntry getModeViewEntry(OTViewEntry viewEntry, String modeStr)
     {
     	if(modeStr == null){
     		modeStr = getDefaultViewMode();
     	}
     	
     	if(NO_VIEW_MODE.equals(modeStr)){
     		return null;
     	}
 
 		OTViewBundle viewBundle = getViewBundle();
 		OTViewMode mode = null;
 		OTObjectList modes = viewBundle.getModes();
 		for(int i=0; i<viewBundle.getModes().size(); i++){
 			OTViewMode curMode = (OTViewMode)modes.get(i);
 			if(curMode.getName().equals(modeStr)){
 				mode = curMode;
 				break;
 			}
 		}		
 		
 		OTObjectMap map = mode.getMap();
 
 		OTViewEntry modeViewEntry = 
 			(OTViewEntry)OTrunkUtil.getObjectFromMapWithIdKeys(map, viewEntry);
 
 		if(modeViewEntry == null){
 			modeViewEntry = mode.getDefault();
 		}
 
 		return modeViewEntry;
     }
     
 	/* (non-Javadoc)
 	 * @see org.concord.framework.otrunk.view.OTViewFactory#getView(org.concord.framework.otrunk.OTObject, org.concord.framework.otrunk.view.OTViewEntry, java.lang.String)
 	 */
 	public OTView getView(OTObject otObject, OTViewEntry viewEntry, String modeStr) 
 	{
 		OTViewEntry activeViewEntry = viewEntry;
 		
 		OTViewEntry modeViewEntry = getModeViewEntry(viewEntry, modeStr);
 
 		if(modeViewEntry != null){
 			activeViewEntry = modeViewEntry;
 		}
 
 		OTView view = getViewInternal(otObject, activeViewEntry);
 
 		// if the modeViewEntry is not null then 
 		// pass the viewEntry that was requested to the newly created view
 		// this is useful for mode views that want to display other modes of
 		// the original view entry.
 		// this entry might have been specified by the user, or it could have 
 		// been determined by looking up an interface and object type.
 		if(modeViewEntry != null && 
 				view instanceof OTRequestedViewEntryAware) {
 			((OTRequestedViewEntryAware)view).setRequestedViewEntry(viewEntry);
 		}
 		
 		return view;
 		
 	}
 	
 	public OTViewBundle getViewBundle()
 	{
 		if (!(viewBundle == null)){
 			return viewBundle;
 		} else if (!(parent == null)){
 			return parent.getViewBundle();
 		}
 		else return null;
 	}
 	/* (non-Javadoc)
 	 * @see org.concord.framework.otrunk.view.OTViewFactory#getViewServiceProvider()
 	 */
 	public OTViewContext getViewContext() 
 	{
 		return viewContext;
 	}
 	
 	/**
 	 * Return the view mode set for this view factory.  If this is 
 	 * null then getDefaultViewMode will try to return the view mode
 	 * off the parent.
 	 * 
 	 * @return
 	 */
 	public String getDefaultViewModeLocal()
 	{
 		return mode;
 	}
 
 	/**
 	 * This returns the view mode used if null is passed in for the view mode
 	 * to the getView methods.
 	 * 
 	 * @see org.concord.framework.otrunk.view.OTViewFactory#getDefaultViewMode()
 	 */
 	public String getDefaultViewMode()
     {
 		if (mode == null && parent != null){
 			return parent.getDefaultViewMode();
 		}
 		
 		if(mode == null){
 			return NO_VIEW_MODE;
 		}
 	    return mode;
     }
 
 	/**
 	 * This sets the default view mode for this view factory.  If it is null
 	 * then the viewMode of the parent is used.  If this is set to null
 	 * and there is no parent, then NO_VIEW_MODE is used.
 	 * 
 	 * @see org.concord.framework.otrunk.view.OTViewFactory#setDefaultViewMode(java.lang.String)
 	 */
 	public void setDefaultViewMode(String mode)
     {
 	    this.mode = mode;
     }
 	
 }
