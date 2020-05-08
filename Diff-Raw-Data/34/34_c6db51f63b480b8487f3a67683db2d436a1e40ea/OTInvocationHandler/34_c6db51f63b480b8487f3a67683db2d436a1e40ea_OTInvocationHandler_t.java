 /*
  * Last modification information:
 * $Revision: 1.10 $
 * $Date: 2005-01-31 17:43:20 $
  * $Author: scytacki $
  *
  * Licence Information
  * Copyright 2004 The Concord Consortium 
 */
 package org.concord.otrunk;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Method;
 
 import org.concord.framework.otrunk.OTID;
 import org.concord.framework.otrunk.OTObject;
 import org.concord.framework.otrunk.OTObjectList;
 import org.concord.framework.otrunk.OTObjectMap;
 import org.concord.framework.otrunk.OTResourceList;
 import org.concord.framework.otrunk.OTResourceMap;
 import org.concord.otrunk.datamodel.OTDataObject;
 
 
 /**
  * OTInvocationHandler
  * Class name and description
  *
  * Date created: Nov 8, 2004
  *
  * @author scott<p>
  *
  */
 public class OTInvocationHandler
 	implements InvocationHandler
 {
 	OTDataObject dataObject;
 	OTrunkImpl db;
 	
 	public OTInvocationHandler(OTDataObject dataObject, OTrunkImpl db)
 	{
 		this.dataObject = dataObject;
 		this.db = db;
 	}
 	
 	public Object handleGet(String resourceName, Class returnType, Class proxyClass)
 		throws Exception
 	{
 		// Handle the globalId specially
 		if(resourceName.equals("globalId")) {
 			return dataObject.getGlobalId();
 		}
 				
 		Object resourceValue = dataObject.getResource(resourceName);
 		if(OTObject.class.isAssignableFrom(returnType)) {
 			OTObject object;
 			try {
				if(resourceValue == null) {
 					return null;
 				}
				OTID objId = (OTID)resourceValue;
 				
 				object = (OTObject)db.getOTObject(dataObject, objId);
 				
 				return object;
 			} catch (Exception e)
 			{
 				e.printStackTrace();
 			}		
 			
 			return null;
 			
 		} else if(OTResourceMap.class.isAssignableFrom(returnType)) {
 			try {					
 				OTResourceMap map = (OTResourceMap)dataObject.getResource(resourceName);
 				if(map == null) {
 					map = (OTResourceMap)db.createCollection(dataObject, OTResourceMap.class);
 					dataObject.setResource(resourceName, map);
 				}
 				
 				return map;
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			
 			return null;
 		} else if(OTObjectMap.class.isAssignableFrom(returnType)) {
 			try {					
 				OTResourceMap map = (OTResourceMap)dataObject.getResource(resourceName);
 				if(map == null) {
 					map = (OTResourceMap)db.createCollection(dataObject, OTResourceList.class);
 					dataObject.setResource(resourceName, map);
 				}
 				
 				return new OTObjectMapImpl(map, dataObject, db);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			
 			return null;				
 		} else if(OTResourceList.class.isAssignableFrom(returnType)) {
 			try {					
 				OTResourceList list = (OTResourceList)dataObject.getResource(resourceName);
 				if(list == null) {
 					list = (OTResourceList)db.createCollection(dataObject, OTResourceList.class);
 					dataObject.setResource(resourceName, list);
 				}
 				
 				return list;
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			
 			return null;				
 		} else if(OTObjectList.class.isAssignableFrom(returnType)) {
 			try {					
 				OTResourceList list = (OTResourceList)dataObject.getResource(resourceName);
 				if(list == null) {
 					list = (OTResourceList)db.createCollection(dataObject, OTResourceList.class);
 					dataObject.setResource(resourceName, list);
 				}
 				
 				return new OTObjectListImpl(list, dataObject, db);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			
 			return null;				
 		} else if(resourceValue == null && returnType.isPrimitive()) {
 			Field defaultField = proxyClass.getField("DEFAULT_" + resourceName);
 			if(defaultField != null) {
 				return defaultField.get(null);
 			}
 			
 			if(returnType == Boolean.TYPE) {
 				return Boolean.TRUE;
 			} else if(returnType == Integer.TYPE) {
 				return new Integer(-1);
 			} else if(returnType == Float.TYPE) {
 				return new Float(Float.NaN);					
 			} else if(returnType == Byte.TYPE) {
 				return new Byte((byte)0);
 			} else if(returnType == Character.TYPE) {
 				return new Character('\0');
 			} else if(returnType == Short.TYPE) {
 				return new Short((short)-1);
 			} else if(returnType == Long.TYPE) {
 				return new Long(-1);
 			} else if(returnType == Double.TYPE) {
 				return new Double(Double.NaN);
 			}
 			System.err.println("Don't know what to do here yet..." + 
 					"asked for: " + resourceName + " but it is null and its " +
 					"type is: " + returnType);
 		}
 		
 		return resourceValue;
 		
 	}
 
 	public static String getResourceName(int prefixLen, String methodName)
 	{
 		String resourceName = methodName.substring(prefixLen, methodName.length());
 		resourceName = resourceName.substring(0,1).toLowerCase() + 
 			resourceName.substring(1,resourceName.length());
 		return resourceName;
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
 	 */
 	public Object invoke(Object proxy, Method method, Object[] args)
 		throws Throwable
 	{
 		String methodName = method.getName();
 		if(methodName.startsWith("get")) {
 			String resourceName = getResourceName(3, methodName); 
 			Class returnType = method.getReturnType();
 			Class proxyClass = proxy.getClass();
 			return handleGet(resourceName, returnType, proxyClass);
 		} else if(methodName.startsWith("add")) {
 			String resourceName = getResourceName(3, methodName); 
 			System.err.println("Don't handle add yet");
 			return null;
 		} else if(methodName.startsWith("removeAll")) {
 			System.err.println("Dont' handle remove All yet");
 			return null;
 		} else if(methodName.equals("toString")) {
 			return dataObject.getResource(OTrunkImpl.RES_CLASS_NAME) + "@" +  dataObject.getGlobalId();
 		} else if(methodName.equals("equals")) {
 			Object other = args[0];
 			if(!(other instanceof OTObject)){
 				return Boolean.FALSE;
 			}
 			
 			if(proxy == other) {
 				return Boolean.TRUE;
 			}
 			
 			if(((OTObject)other).getGlobalId().equals(dataObject.getGlobalId())) {
 				System.err.println("compared two ot objects with the same ID but different instances");
 				return Boolean.TRUE;
 			}
 			return Boolean.FALSE;
 
 		} else {
 			String resourceName = getResourceName(3, methodName); 
 			Object resourceValue = args[0];
 			
 			if(resourceValue instanceof OTObject) {
 				OTObject child = (OTObject)resourceValue;
 				OTID childId = child.getGlobalId();
 				resourceValue = childId;
 			} 
 			dataObject.setResource(resourceName, resourceValue);			
 		}
 		return null;
 	}
 
 }
