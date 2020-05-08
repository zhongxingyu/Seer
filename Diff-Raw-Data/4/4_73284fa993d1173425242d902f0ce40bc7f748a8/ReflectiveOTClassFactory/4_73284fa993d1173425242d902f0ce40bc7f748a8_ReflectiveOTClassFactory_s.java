 package org.concord.otrunk.otcore.impl;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.concord.framework.otrunk.OTObject;
 import org.concord.framework.otrunk.OTObjectList;
 import org.concord.framework.otrunk.OTObjectMap;
 import org.concord.framework.otrunk.OTResourceList;
 import org.concord.framework.otrunk.OTResourceMap;
 import org.concord.framework.otrunk.OTResourceSchema;
 import org.concord.framework.otrunk.OTXMLString;
 import org.concord.framework.otrunk.otcore.OTClass;
 import org.concord.framework.otrunk.otcore.OTType;
 import org.concord.otrunk.OTInvocationHandler;
 import org.concord.otrunk.OTrunkImpl;
 import org.concord.otrunk.xml.TypeService;
 
 /**
  * This class uses the static methods in OTrunkImpl getOTClass and putOTClass, to lookup and 
  * store OTClasses.  If there is a set of interdependent OTClasses being created all of the Java Class
  * objects should be passed in at the same time so the dependencies can be found. Otherwise care must be taken
  * to pass in the classes in the correct order so any dependencies can be resolved.
  * 
  * @author scytacki
  *
  */
 public class ReflectiveOTClassFactory
 {
     public final static String STRING = "string";
     public final static String XML_STRING = "xmlstring";
     public final static String BOOLEAN = "boolean";
     public final static String INTEGER = "int";
     public final static String LONG = "long";
     public final static String FLOAT = "float";
     public final static String DOUBLE = "double";
     public final static String BLOB = "blob";
     public final static String LIST = "list";
     public final static String MAP = "map";
     public final static String OBJECT = "object";
 
     public static ReflectiveOTClassFactory singleton = new ReflectiveOTClassFactory(); 
     
     ArrayList newlyRegisteredOTClasses = new ArrayList();
     
     /**
      * 
      * @param javaClass this is the class which implements OTObject
      * @param javaSchemaClass if the OTObject class is not an interface then it must also have a schemaClass
      *   which is the interface extending OTResourceSchema
      */
     public OTClass registerClass(Class javaClass)
     {
     	String otClassName = javaClass.getName();
     	OTClass otClass = OTrunkImpl.getOTClass(otClassName);
     	
     	if(otClass != null){
     		return otClass;
     	}
 
 		otClass = new OTClassImpl(javaClass);
 		OTrunkImpl.putOTClass(otClassName, otClass);
 		newlyRegisteredOTClasses.add(otClass);
 
 		Class constructorSchemaClass = null;
 		if(!javaClass.isInterface()){
 			Constructor [] memberConstructors = javaClass.getConstructors();
 			Constructor resourceConstructor = memberConstructors[0]; 
 			Class [] params = resourceConstructor.getParameterTypes();
 					
 			// Check all the conditions for incorrect imports.
 			if(memberConstructors.length > 1 || params == null || 
 					params.length == 0 ||
 					!OTResourceSchema.class.isAssignableFrom(params[0])) {
 				System.err.println("Invalid constructor for OTrunk Object: " + otClassName +
 						"\n   If you are using an otml file check the import statements and " + 
 						"try removing this class.  If you are creating a new OTClass then you need to fix " +
 						"the constructor.");
 				throw new RuntimeException("OTObjects should only have 1 constructor" + "\n" +
 						" whose first argument is the resource schema");
 			}
 			
 			constructorSchemaClass = params[0];
 			((OTClassImpl)otClass).setConstructorSchemaClass(constructorSchemaClass);
 		}
 
 		// now we need to register the parent classes.
 		Class [] interfaces = javaClass.getInterfaces();
 		
 		for(int j=0; j<interfaces.length; j++){
 			Class parentInterface = interfaces[j];
 
 			processSuperType(otClass, parentInterface);
 		}
 
 		Class javaSuperclass = javaClass.getSuperclass();
 		if(javaSuperclass != null){
 			processSuperType(otClass, javaSuperclass);
 		}
 				
 		return otClass;
     }
     
     public void processSuperType(OTClass otClass, Class javaSuperType)
     {
 		if(!OTObject.class.isAssignableFrom(javaSuperType)){
 			// this interface isn't an OTClass
 			return;
 		}
 		
 		OTClass parentOTClass = OTrunkImpl.getOTClass(javaSuperType.getName());
 		if(parentOTClass == null){				
 			// This interface has not been registered yet
 
 			// This might cause an infinite loop if the parentInterface references this class, but
 			// because we already put our otClass in the OTrunkImpl that loop is cut off.
 			parentOTClass = registerClass(javaSuperType);
 		}
 		
 		otClass.getOTSuperTypes().add(parentOTClass);			    	
     }    
     
 	public void addClassProperties(OTClass otClass)
 	{
 		// This should be called after the javaClass has been passed to the registerClass method
 		
 		// Then look for all the getters and their types
 		// just in this class.  The class will have a list of parent classes that it will
 		// combine to return all of the possible class properties when they are requested.
 
 		Class javaClass = ((OTClassImpl)otClass).getInstanceClass();
 		Class schemaClass = ((OTClassImpl)otClass).getConstructorSchemaClass();
 		if(schemaClass != null){
 			javaClass = schemaClass;
 		}
 		
 		Method [] methods = javaClass.getDeclaredMethods();
 			
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
 			String resourceType = getObjectPrimitiveType(resourceClass);
 
 			if(resourceType == null){
 				String resourceClassName = resourceClass.getName();
 				OTClass resourceOTClass = OTrunkImpl.getOTClass(resourceClassName);
 				
 				if(resourceOTClass != null){
 					resourceType = "object";
 				}
 			}			
 
 			if(resourceType == null){
 				System.err.println("Warning: the field: " + resourceName + " on class: " + javaClass + "\n" + 
                         "    has an unknown type: " + resourceClass + "\n"  +
                         "  There are no imported classes that implement this type");
                 // in a strict assertion mode we might want to stop
                 // here, but setting the type to object seems pretty safe
                 resourceType = "object";
 			}
 
 			OTType otType = getOTType(resourceType, resourceClass);
 			
 			OTClassPropertyImpl property = new OTClassPropertyImpl(resourceName, otType, null);
 			
 	        try {
 	            Field defaultField = javaClass.getField("DEFAULT_" + resourceName);
 	            if(defaultField != null) {
 	                Object defaultValue =  defaultField.get(null);
 	                property.setDefault(defaultValue);
 	            }
 	        } catch (NoSuchFieldException e) {
 	        	// It is normal to have undefined default values so we shouldn't throw an
 	        	// exception in this case.
 	        } catch (IllegalArgumentException e) {
 	            e.printStackTrace();
             } catch (IllegalAccessException e) {
 	            e.printStackTrace();
             }
 			
 			otClass.getOTClassProperties().add(property);			
 		}		
 	}	
 
 	public void loadClasses(List classList)
 	{		
 		for(Iterator i=classList.iterator(); i.hasNext();){
 			Class nextClass = (Class) i.next();
 			registerClass(nextClass);
 		}
 		
 		for(Iterator i=newlyRegisteredOTClasses.iterator(); i.hasNext();){
 			OTClass nextOTClass = (OTClass) i.next();
 			addClassProperties(nextOTClass);
 		}
 		
 		newlyRegisteredOTClasses.clear();
 	}
 	
 	/**
 	 * This will return the type of the allowable classes or interfaces for
 	 * OTObjectS
 	 * 
 	 * @param klass
 	 * @return
 	 */
 	public static String getObjectPrimitiveType(Class klass)
 	{
 		String type = getPrimitiveType(klass);
 		
 		if(type != null){
 			return type;
 		}
 
 		if(klass.isArray() && 
 				klass.getComponentType().equals(Byte.TYPE)) {
 			return BLOB;
 		} else if(URL.class.isAssignableFrom(klass)){
 			return BLOB;
 		} else if(OTResourceList.class.isAssignableFrom(klass) ||
 				OTObjectList.class.isAssignableFrom(klass)) {
 			return LIST;
 		} else if(OTResourceMap.class.isAssignableFrom(klass) ||
 				OTObjectMap.class.isAssignableFrom(klass)) {
 			return MAP;
 		} else if(OTObject.class.isAssignableFrom(klass) ) {
 			// OTIDs used to be allowed here.
 			// If an OTID is the type of a parameter, I think the code which
 			// translates these will get messed up.  So they are not allowed
 			// now
 			return OBJECT;
 		}
 		return null;
 	}
 
     /**
      * These types are the same for OTObjects and OTDataObjects
      * 
      * @param klass
      * @return
      */
 	public static String getPrimitiveType(Class klass)
 	{
 		if(String.class.isAssignableFrom(klass)) {
 			return STRING;
 		} else if(OTXMLString.class.isAssignableFrom(klass)) {
 		    return XML_STRING;
 		} else if(Boolean.class.isAssignableFrom(klass) ||
 				Boolean.TYPE.equals(klass)) {
 			return BOOLEAN;
 		} else if(Integer.class.isAssignableFrom(klass) ||
 				Integer.TYPE.equals(klass)) {
 			return INTEGER;
 		} else if(Long.class.isAssignableFrom(klass) ||
 				Long.TYPE.equals(klass)) {
 			return LONG;
 		} else if(Float.class.isAssignableFrom(klass) ||
 				Float.TYPE.equals(klass)) {
 			return FLOAT;
 		} else if(Double.class.isAssignableFrom(klass) ||
 				Double.TYPE.equals(klass)) {
 			return DOUBLE;
 		} 
 	
 		return null;
 	}
 	
 	public static OTType getOTType(String resourceType, Class resourceTypeClass)
 	{
 		OTType otType = null;
 		
		if(TypeService.BOOLEAN.equals(resourceType)){
 			otType = OTCorePackage.BOOLEAN_TYPE;
 		} else if(TypeService.DOUBLE.equals(resourceType)){
 			otType = OTCorePackage.DOUBLE_TYPE;
 		} else if(TypeService.FLOAT.equals(resourceType)){
 			otType = OTCorePackage.FLOAT_TYPE;
 		} else if(TypeService.INTEGER.equals(resourceType)){
 			otType = OTCorePackage.INTEGER_TYPE;
 		} else if(TypeService.LONG.equals(resourceType)){
 			otType = OTCorePackage.LONG_TYPE;
 		} else if(TypeService.STRING.equals(resourceType)){
 			otType = OTCorePackage.STRING_TYPE;
 		} else if(TypeService.XML_STRING.equals(resourceType)){
 			otType = OTCorePackage.XML_STRING_TYPE;
 		} else if(TypeService.OBJECT.equals(resourceType)){
 			otType = OTrunkImpl.getOTClass(resourceTypeClass.getName());
 		} else if(TypeService.LIST.equals(resourceType)){
 			if(resourceTypeClass.equals(OTResourceList.class)){
 				otType = OTCorePackage.RESOURCE_LIST_TYPE;
 			} else if(resourceTypeClass.equals(OTObjectList.class)){
 				otType = OTCorePackage.OBJECT_LIST_TYPE;
 			}
 		} else if(TypeService.MAP.equals(resourceType)){
 			if(resourceTypeClass.equals(OTResourceMap.class)){
 				otType = OTCorePackage.RESOURCE_MAP_TYPE;
 			} else if(resourceTypeClass.equals(OTObjectMap.class)){
 				otType = OTCorePackage.OBJECT_MAP_TYPE;
 			}
 		} 
 
 		return otType;
 	}
 }
