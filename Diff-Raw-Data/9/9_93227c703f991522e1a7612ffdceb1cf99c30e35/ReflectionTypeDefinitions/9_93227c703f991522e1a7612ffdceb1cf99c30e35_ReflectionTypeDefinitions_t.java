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
 * $Revision: 1.18 $
 * $Date: 2007-02-06 02:19:00 $
 * $Author: imoncada $
  *
  * Licence Information
  * Copyright 2004 The Concord Consortium 
 */
 package org.concord.otrunk.xml;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Method;
 import java.util.Vector;
 
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
 	    registerTypes(classNames, typeService, xmlDB, true);
 	}
     
 	public static void registerTypes(Vector classNames, TypeService typeService,
 			XMLDatabase xmlDB, boolean addShortcuts)
 		throws Exception 
 	{
 		ClassLoader classloader = ReflectionTypeDefinitions.class.getClassLoader();
 		Vector typeClasses = new Vector();
 		
 		for(int i=0; i<classNames.size(); i++) {
 	        String className = (String)classNames.get(i);			
 		    try {
 		        Class typeClass = classloader.loadClass(className);
 		        typeClasses.add(typeClass);
 		    } catch (Exception e) {
 		        System.err.println("Error importing class: " + className);
                 System.err.println("  this class was listed as an import in the otml file");
 		    }
 		}		
 		
 		for(int i=0; i<typeClasses.size(); i++) {
 			Class otObjectClass = (Class)typeClasses.get(i); 
 			String className = otObjectClass.getName();
 			
 			// do our magic here to register the type
 			// first figure out if it uses a seperate schema object
 			// get that object, otherwise use the class itself
 			Class resourceSchemaClass = null;
 			if(otObjectClass.isInterface()){
 				resourceSchemaClass = otObjectClass;
 			} else {
 				Constructor [] memberConstructors = otObjectClass.getConstructors();
 				Constructor resourceConstructor = memberConstructors[0]; 
 				Class [] params = resourceConstructor.getParameterTypes();
 						
 				// Check all the conditions for incorrect imports.
 				if(memberConstructors.length > 1 || params == null || 
 						params.length == 0 ||
 						!OTResourceSchema.class.isAssignableFrom(params[0])) {
					System.err.println("Invalid constructor for OTrunk Object: " + className +
 							"\n   If you are using an otml file check the import statements");
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
 			addResources(resourceDefs, resourceSchemaClass, typeClasses);
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
 			
 			if(addShortcuts) {
 			    int lastDot = className.lastIndexOf(".");
 			    String localClassName = className.substring(lastDot+1,className.length());
 			    typeService.registerUserType(localClassName, objectType);
 			}
 		}
 	}
 	
 	public static void addResources(Vector resources, Class resourceSchemaClass,
 	        Vector typeClasses)
 	{
 		// Then look for all the getters and their types
 		Method [] methods = resourceSchemaClass.getMethods();
 		for(int j=0; j<methods.length; j++) {
 			String methodName = methods[j].getName();
 			if(methodName.equals("getGlobalId") ||
 			        methodName.equals("getOTDatabase") ||
                     methodName.equals("getOTObjectService")) {
 				continue;
 			}
 			
 			if(!methodName.startsWith("get")) {
 				continue;				
 			}
 			
 			String resourceName = OTInvocationHandler.getResourceName(3,methodName);
 			Class resourceClass = methods[j].getReturnType();
 			String resourceType = TypeService.getPrimitiveType(resourceClass);
 
 			if(resourceType == null){
 			    // This resource type might be an interface that one of the other
 			    // typeClasses implements.  
 			    for(int k=0; k<typeClasses.size(); k++){
 			        Class typeClass = (Class)typeClasses.get(k);
 			        if(resourceClass.isAssignableFrom(typeClass)){
 			            resourceType = "object";
 			        }
 			    }
 			}			
 
 			if(resourceType == null){
 				System.err.println("Warning: the field: " + resourceName + " on class: " + resourceSchemaClass + "\n" + 
                         "    has an unknown type: " + resourceClass + "\n"  +
                         "  There are no imported classes that implement this type");
                 // in a strict assertion mode we might want to stop
                 // here, but setting the type to object seems pretty safe
                 resourceType = "object";
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
 			 * FIXME: this should be re-enabled.  We need to figure out
 			 * what this was breaking before.
 			 * 
 			if(OTObject.class.isAssignableFrom(interfaces[i]) ||
 					OTResourceSchema.class.isAssignableFrom(interfaces[i])) {
 				addResources(resources, interfaces[i]);
 			} else {
 				System.err.println("resource class implements invalid interface: " +
 						interfaces[i].getName());
 			}
 			*/
 			addResources(resources, interfaces[i], typeClasses);
 		}
 		
 		// we will keep the parameters for now but just leave them
 		// as null until we decide what to do.
 		
 		return;
 	}	
 }
