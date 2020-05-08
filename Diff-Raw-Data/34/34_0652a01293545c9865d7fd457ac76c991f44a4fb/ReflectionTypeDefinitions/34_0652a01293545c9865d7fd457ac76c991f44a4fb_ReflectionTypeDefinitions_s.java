 /*
  * Last modification information:
 * $Revision: 1.6 $
 * $Date: 2005-01-25 16:19:41 $
  * $Author: scytacki $
  *
  * Licence Information
  * Copyright 2004 The Concord Consortium 
 */
 package org.concord.otrunk.xml;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Method;
 import java.util.Vector;
 
 import org.concord.framework.otrunk.OTObject;
 import org.concord.framework.otrunk.OTResourceSchema;
 import org.concord.otrunk.OTInvocationHandler;
 
 
 /**
  * XMLTypeDefinitions
  * Class name and description
  *
  * Date created: Nov 17, 2004
  *
  * @author scott<p>
  *
  */
 public class ReflectionTypeDefinitions
 {
 	public static void registerTypes(Vector classNames, TypeService typeService,
 			XMLDatabase xmlDB)
 		throws Exception 
 	{
 		ClassLoader classloader = ReflectionTypeDefinitions.class.getClassLoader();
 		
 		for(int i=0; i<classNames.size(); i++) {
 			String className = (String)classNames.get(i);
 			Class otObjectClass = classloader.loadClass(className);
 			// do our magic here to register the type
 			// first figure out if it uses a seperate schema object
 			// get that object, otherwise use the class itself
 			Class resourceSchemaClass = null;
 			if(otObjectClass.isInterface() &&
 					OTObject.class.isAssignableFrom(otObjectClass)) {
 				resourceSchemaClass = otObjectClass;
 			} else {
 				Constructor [] memberConstructors = otObjectClass.getConstructors();
 				Constructor resourceConstructor = memberConstructors[0]; 
 				Class [] params = resourceConstructor.getParameterTypes();
 								
 				if(memberConstructors.length > 1 || params == null ||
 						!OTResourceSchema.class.isAssignableFrom(params[0])) {
 					System.err.println("Invlaid constructor for OTrunk Object: " + className);
 					throw new RuntimeException("OTObjects should only have 1 constructor" + "\n" +
 							" whose first argument is the resource schema");
 				}
 				
 				resourceSchemaClass = params[0];
 			}
 
 			if(resourceSchemaClass == null) {
 				throw new RuntimeException("Can't find valid schema class for: " +
 						className);
 			}
 			
 			Vector resourceDefs = new Vector();
 			addResources(resourceDefs, resourceSchemaClass);
 			ResourceDefinition [] resourceDefsArray = new ResourceDefinition[resourceDefs.size()];
 			for(int j=0; j<resourceDefsArray.length; j++) {
 				resourceDefsArray[j] = (ResourceDefinition)resourceDefs.get(j);
 			}
 			ObjectTypeHandler objectType = 
 				new ObjectTypeHandler(
 						className,
 						className,
 						null,
 						resourceDefsArray,
 						typeService,
 						xmlDB);
 						
 			typeService.registerUserType(className, objectType);
			int lastDot = className.lastIndexOf(".");
			String localClassName = className.substring(lastDot+1,className.length());
			typeService.registerUserType(localClassName, objectType);
 		}
 	}
 	
 	public static void addResources(Vector resources, Class resourceSchemaClass)
 	{
 		// Then look for all the getters and their types
 		Method [] methods = resourceSchemaClass.getMethods();
 		for(int j=0; j<methods.length; j++) {
 			String methodName = methods[j].getName();
 			if(methodName.equals("getGlobalId")) {
 				continue;
 			}
 			
 			if(!methodName.startsWith("get")) {
 				continue;				
 			}
 			
 			String resourceName = OTInvocationHandler.getResourceName(3,methodName);
 			Class resourceClass = methods[j].getReturnType();
 			String resourceType = TypeService.getPrimitiveType(resourceClass);
 			if(resourceType == null){
 				System.err.println("Unknown resourceType: " + resourceClass);
 			}
 			ResourceDefinition resourceDef = new ResourceDefinition(resourceName,
 					resourceType, null);
 			resources.add(resourceDef);
 		}
 		// and look for the parent classes.  We probably can 
 		// skip the dynamic extension and just follow the extension
 		// tree right now.  But how do we know which interfaces to 
 		// follow and which ones to skip...
 		// It looks like we can just follow ones that extend
 		// OTObject or OTResourceSchema
 		// This has been changed we now follow all interfaces.
 		Class [] interfaces = resourceSchemaClass.getInterfaces();
 		for(int i=0; i<interfaces.length; i++) {
 			/*
 			if(OTObject.class.isAssignableFrom(interfaces[i]) ||
 					OTResourceSchema.class.isAssignableFrom(interfaces[i])) {
 				addResources(resources, interfaces[i]);
 			} else {
 				System.err.println("resource class implements invalid interface: " +
 						interfaces[i].getName());
 			}
 			*/
 			addResources(resources, interfaces[i]);
 		}
 		
 		// we will keep the parameters for now but just leave them
 		// as null until we decide what to do.
 		
 		return;
 	}	
 }
