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
  * Created on Jan 12, 2005
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package org.concord.sensor.state;
 
 import org.concord.framework.otrunk.DefaultOTController;
 import org.concord.framework.otrunk.OTObjectService;
 import org.concord.sensor.ExperimentRequest;
 import org.concord.sensor.SensorDataManager;
 
 /**
 * @author scytacki
  *
  */
 public class OTSensorDataProxyController extends DefaultOTController
 {
 	public static Class [] realObjectClasses =  {SensorDataProxy.class};	
 	public static Class otObjectClass = OTSensorDataProxy.class;    
 
 	/* (non-Javadoc)
      * @see org.concord.framework.otrunk.OTController#loadRealObject(java.lang.Object)
      */
     public void loadRealObject(Object realObject)
     {
     	SensorDataProxy proxy = (SensorDataProxy) realObject;
     	OTSensorDataProxy otProxy = (OTSensorDataProxy) otObject;
     	OTObjectService objectService = otProxy.getOTObjectService();
     	
     	SensorDataManager sdm = 
     		(SensorDataManager) objectService.getOTrunkService(SensorDataManager.class);
     	
     	proxy.setup(sdm, otProxy.getRequest(), otProxy.getZeroSensor());
     }
 
 	/* (non-Javadoc)
      * @see org.concord.framework.otrunk.OTController#registerRealObject(java.lang.Object)
      */
     public void registerRealObject(Object realObject)
     {
 	    // TODO Auto-generated method stub
 	    
     }
 
 	/* (non-Javadoc)
      * @see org.concord.framework.otrunk.OTController#saveRealObject(java.lang.Object)
      */
     public void saveRealObject(Object realObject)
     {
     	// if someone made a copy of this object then we'll need to
     	// save its parts
     	SensorDataProxy proxy = (SensorDataProxy) realObject;
     	OTSensorDataProxy otProxy = (OTSensorDataProxy) otObject;
 
     	ExperimentRequest request = proxy.getExperimentRequest();
     	if(request instanceof OTExperimentRequest){
         	otProxy.setRequest((OTExperimentRequest)request);    		
 	        
         }
     	
     	otProxy.setZeroSensor(proxy.getZeroSensor());
 	    
     }
 	
     public void dispose(Object realObject) 
     {
     	super.dispose(realObject);
     	SensorDataProxy proxy = (SensorDataProxy) realObject;
     	proxy.close();
     }
 }
