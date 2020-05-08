 
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
  * Created on Mar 21, 2005
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package org.concord.datagraph.state;
 
 import java.awt.Color;
 import java.util.EventObject;
 
 import org.concord.data.state.OTDataStore;
 import org.concord.datagraph.engine.ControllableDataGraphable;
 import org.concord.datagraph.engine.ControllableDataGraphableDrawing;
 import org.concord.datagraph.engine.DataGraphable;
 import org.concord.framework.data.stream.DataProducer;
 import org.concord.framework.otrunk.DefaultOTObject;
 import org.concord.framework.otrunk.OTResourceSchema;
 import org.concord.framework.otrunk.OTWrapper;
 import org.concord.graph.event.GraphableListener;
 
 /**
  * @author scott
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 public class OTDataGraphable extends DefaultOTObject
 	implements OTWrapper, GraphableListener
 {
     public static interface ResourceSchema extends OTResourceSchema
     {
     	public static int DEFAULT_color = 0x00FF0000;
     	public int getColor();
     	public void setColor(int color);
     	
     	public static boolean DEFAULT_connectPoints = true;
     	public boolean getConnectPoints();
     	public void setConnectPoints(boolean flag);
 
     	public static boolean DEFAULT_drawMarks = true;
     	public boolean getDrawMarks();
     	public void setDrawMarks(boolean flag);
     	
     	public static boolean DEFAULT_controllable = false;
     	public boolean getControllable();
     	public void setControllable(boolean flag);
     	
     	public static boolean DEFAULT_drawing = false;
     	public boolean getDrawing();
     	public void setDrawing(boolean flag);
     	
     	public static boolean DEFAULT_allowHide = true;
     	public boolean getAllowHide();
     	public void setAllowHide(boolean flag);
     		
     	public OTDataStore getDataStore();
     	public void setDataStore(OTDataStore store);
     	
     	public DataProducer getDataProducer();
     	public void setDataProducer(DataProducer producer);
     	
     	public static int DEFAULT_xColumn = 0;
     	public int getXColumn();
     	public void setXColumn(int xCol);
     	
     	public static int DEFAULT_yColumn = 1;
     	public int getYColumn();
     	public void setYColumn(int yCol);
     }
     
     private ResourceSchema resources;
     
     protected DataGraphable wrappedObject;
     
 	public OTDataGraphable(ResourceSchema resources)
 	{
 	    super(resources);
 	    this.resources = resources;
 	}
 	
 	/**
 	 * This method is used by the otrunk framework to save this
 	 * object.  It will also be using during instanciation so 
 	 * objects that need DataGraphables don't need to know about
 	 * this OTDataGraphable they will just get the wrapped object.
 	 * Finally this is used ad Author time to validate the use of
 	 * this object
 	 * 
 	 * @return
 	 */
 	public Object createWrappedObject()
 	{
 		DataGraphable dg;
 
 		if (wrappedObject != null){
 			wrappedObject.releaseAll();
 		}
 		
 		if (resources.getDrawing()){
         	dg = new ControllableDataGraphableDrawing();
             
             // this is a bit of a hack.  This should be saved
             // in the datastore object.
             OTDataStore dataStore = resources.getDataStore();
             dataStore.setUseDtAsChannel(false);
 		}
         else if (resources.getControllable()){
         	dg = new ControllableDataGraphable();
         } 
         else {
         	dg = new DataGraphable();
         }
         dg.setColor(new Color(resources.getColor()));
         dg.setShowCrossPoint(resources.getDrawMarks());
         dg.setLabel(resources.getName());
         dg.setUseVirtualChannels(true);
         
 		DataProducer producer = resources.getDataProducer();
 		OTDataStore dataStore = resources.getDataStore();
 
 		if (resources.getControllable() && producer != null){
 		    System.err.println("Can't control a graphable with a data producer");
 		    return null;
 		}
 		
         if (resources.getDrawing()) {
             dataStore.setUseDtAsChannel(false);
         }
         
 		if (producer != null && dataStore != null){
 		    dataStore.setDataProducer(producer);
 		}
 		
 		if (dataStore != null){
 			dg.setDataStore(dataStore);
 		}
 		else if (producer != null){
 			dg.setDataProducer(producer);
 		}
 		else {
 		    System.err.println("OTDataGraphable without a valid " + 
 		            "datastore or dataproducer");
 		}
 		
 		dg.setChannelX(resources.getXColumn());
 		dg.setChannelY(resources.getYColumn());
 
 		wrappedObject = dg;
 
 		//Now, listen to this object so I can be updated automatically when it changes
 		registerWrappedObject(dg);
 		
         return dg;
     }
 
     /**
      * There should be a better way to do this.  Given a DataGraphable that can be started and 
      * stopped (similar to implementing DataFlow interface), it should be possible to get that
      * flow object from the graphable.  Or the data graphable itself should implement the
      * DataFlow interface.
      * 
      * @return
      */
     public DataProducer getDataProducer()
     {
         return resources.getDataProducer();
     }
     
 	/**
 	 * @see org.concord.framework.otrunk.OTWrapper#saveObject(java.lang.Object)
 	 */
 	public void saveObject(Object wrappedObject)
 	{
 		DataGraphable dg = (DataGraphable)wrappedObject;
 		
 		Color c = dg.getColor();
 		resources.setColor(c.getRGB() & 0x00FFFFFF);
 		resources.setConnectPoints(dg.isConnectPoints());
 		resources.setDrawMarks(dg.isShowCrossPoint());
 		resources.setXColumn(dg.getChannelX());
 		resources.setYColumn(dg.getChannelY());
 	}
 
 	/**
 	 * @see org.concord.framework.otrunk.OTWrapper#getWrappedObjectClass()
 	 */
 	public Class getWrappedObjectClass()
 	{
 		return DataGraphable.class;
 	}
 
 	/**
 	 * @see org.concord.framework.otrunk.OTWrapper#registerWrappedObject(java.lang.Object)
 	 */
 	public void registerWrappedObject(Object wrappedObject)
 	{
 		DataGraphable dg = (DataGraphable)wrappedObject;
 		
 		dg.addGraphableListener(this);
 	}
 
 	/**
 	 * @return
 	 */
 	public DataGraphable getDataGraphable()
 	{
 		return wrappedObject;
 	}
 
 	/**
 	 * @param otDataStore
 	 */
 	public void setDataStore(OTDataStore otDataStore)
 	{
 		resources.setDataStore(otDataStore);
 	}
 
 	/**
 	 * @param b
 	 */
 	public void setDrawing(boolean b)
 	{
 		resources.setDrawing(b);
 	}
 
 	/**
 	 * @see org.concord.graph.event.GraphableListener#graphableChanged(java.util.EventObject)
 	 */
 	public void graphableChanged(EventObject e)
 	{
 		saveObject(e.getSource());
 	}
 
 	/**
 	 * @see org.concord.graph.event.GraphableListener#graphableRemoved(java.util.EventObject)
 	 */
 	public void graphableRemoved(EventObject e)
 	{
 		// TODO Auto-generated method stub
 		
 	}
 }
