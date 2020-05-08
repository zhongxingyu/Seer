 /*******************************************************************************
  * Copyright () 2009, 2011 David Wong
  *
  * This file is part of TestDataCaptureJ.
  *
  * TestDataCaptureJ is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * TestDataCaptureJ is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Afferro General Public License for more details.
  *
  * You should have received a copy of the GNU Afferro General Public License
  * along with TestDataCaptureJ.  If not, see <http://www.gnu.org/licenses/>.
  *******************************************************************************/
 package au.com.dw.testdatacapturej.reflection;
 
 import java.lang.reflect.AccessibleObject;
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.collections.iterators.ArrayIterator;
 
 import au.com.dw.testdatacapturej.config.CollectionAdderConfig;
 import au.com.dw.testdatacapturej.config.ConfigUtil;
 import au.com.dw.testdatacapturej.meta.ContainmentType;
 import au.com.dw.testdatacapturej.meta.ObjectInfo;
 import au.com.dw.testdatacapturej.meta.ObjectType;
 import au.com.dw.testdatacapturej.meta.SetterGenerationType;
 import au.com.dw.testdatacapturej.reflection.util.ReflectionUtil;
 import au.com.dw.testdatacapturej.util.TypeUtil;
 
 
 /**
  * Implementation of ReflectionHandler that generates meta-data information about objects. This meta-data
  * can be used to generate the test code for the objects.
  * 
  * Use the log(..) method to start the process for the initial object (i.e. parameter or
  * return value).
  * 
  * @author David Wong
  *
  */
 public class MetadataGenerationHandler implements ReflectionHandler {
 	
 	/**
 	 * Entry into the reflective meta-data generation process. Determines the type of the object and then
 	 * passes to the appropriate handler methods. The handler methods are recursive so if any of the objects
 	 * are classes with fields or container classes such as Collections and Arrays, then the child or element
 	 * objects will also be passed to the appropriate handler method.
 	 *
 	 * For each object, whether it is the initial object or a child object, an ObjectInfo is created to store
 	 * the meta-data for that object. This meta-data will later will used to determine what test code is generated
 	 * for each object.
 	 * 
 	 * This initial object can't be null, so any null check should be done before this method is called.
 	 * 
 	 * @param initialObject
 	 * @throws IllegalArgumentException
 	 * @throws IllegalAccessException
 	 * 
 	 * @see au.com.dw.testdatacapturej.meta.ObjectInfo
 	 */
 	public ObjectInfo handle(Object initialObject) throws IllegalArgumentException, IllegalAccessException
 	{
 		ObjectInfo info = new ObjectInfo();
 		info.setValue(initialObject);
 		info.setInitalObject(true);
 		info.setContainmentType(ContainmentType.NONE);
 		info.setHasDefaultConstructor(hasDefaultConstructor(info.getValue()));
 		
 		if (TypeUtil.isJavaClass(initialObject))
 		{
 			info.setType(ObjectType.SIMPLE);
 		}
 		else
 		{
 			String fieldName;
 
 			Class<?> clazz = initialObject.getClass();
 			info.setClassName(clazz.getName());
 
         	if (TypeUtil.isArray(initialObject))
         	{
         		fieldName = ReflectionUtil.ARGUMENT_ARRAY_FIELD_NAME;       		
         		info.setType(ObjectType.ARRAY);
     			
     			// special handling for array class names
     			info.setClassName(ReflectionUtil.getArrayClassName(initialObject));
         		
         		handleArray(info);
         	}
         	else if (TypeUtil.isCollection(initialObject))
         	{
         		fieldName = ReflectionUtil.ARGUMENT_COLLECTION_FIELD_NAME;
         		info.setType(ObjectType.COLLECTION);
        		
          		handleCollection(info);
         	}
         	else if (TypeUtil.isMap(initialObject))
         	{
         		fieldName = ReflectionUtil.ARGUMENT_MAP_FIELD_NAME;
            		info.setType(ObjectType.MAP);
         		
            		handleMap(info);
         	}
         	else
         	{
         		fieldName = ReflectionUtil.ARGUMENT_OBJECT_FIELD_NAME;
            		info.setType(ObjectType.OBJECT);
         		
            		handleFields(info);
            		
 	            // check if configured parameterized constructor is to be used for the initial object
 	            ConfigUtil configUtil = new ConfigUtil();
 	            info.setConstructorParamFieldNames(configUtil.getConstructionParameters(info)); 
         	}
         	
         	info.setFieldName(fieldName);
 		}
 		
 		return info;
 	}
 	
 	/**
 	 * Use reflection to process the fields in an object. Iterates through the fields in the object
 	 * and passes them to the appropriate handlers.
 	 * 
 	 * Note: does not handle static fields yet.
 	 * 
 	 * @param object
 	 * @throws IllegalArgumentException
 	 * @throws IllegalAccessException
 	 */
 	protected void handleFields(ObjectInfo info) throws IllegalArgumentException, IllegalAccessException {
 
 		// use reflection to get the fields of the class
 		Object object = info.getValue();
 		Class<?> clazz = object.getClass();
         Field[] fields = clazz.getDeclaredFields();
         AccessibleObject.setAccessible(fields, true);
         
         // check configuration for non-standard handling
         ConfigUtil configUtil = new ConfigUtil();
         
         // get list of field names that have been configured to have non-standard setter method generation
         List<String> ignoredSetterFieldNames = configUtil.getIgnoredSetters(info);
         
         // get list of collection field names for collection that are only accessed through adder methods
         List<CollectionAdderConfig> collectionConfigs = configUtil.getAddedCollections(info);
         
         for (int i = 0; i < fields.length; i++) {
             Field field = fields[i];
             
             int mod = field.getModifiers();
             
             // ignore static fields
             if (!Modifier.isStatic(mod))
             {       	
 	            // get the field info
             	String fieldName = field.getName();
 	            Object fieldValue = field.get(object);
 	            
 	            // create new ObjectInfo for the field object
 	    		ObjectInfo fieldInfo = new ObjectInfo();
 	    		fieldInfo.setFieldName(fieldName);
 	    		fieldInfo.setValue(fieldValue);
 	    		fieldInfo.setContainingClassFieldName(info.getClassFieldName());
 	    		fieldInfo.setHasDefaultConstructor(hasDefaultConstructor(fieldValue));
 	    		
 	    		// check if requires any special setter method generation
 	    		if (ignoredSetterFieldNames != null)
 	    		{
 	    			if (ignoredSetterFieldNames.contains(fieldName))
 	    			{
 	    				fieldInfo.setSetterGenerationType(SetterGenerationType.IGNORE);
 	    			}
 	    		}
 	    		
 	            // determine type of field and pass to handler
 	            if (fieldValue != null)
 	            {
 	            	if (!ReflectionUtil.hasSetterMethod(object, fieldName, fieldValue))
 	            	{
 	            		fieldInfo.setHasSetter(false);
 	            	}
 		    		fieldInfo.setClassName(fieldValue.getClass().getName());
 
 		    		if (TypeUtil.isJavaClass(fieldValue))
 		    		{
 		    			fieldInfo.setType(ObjectType.SIMPLE);
 		    		}
 		    		else if (TypeUtil.isArray(fieldValue))
 		        	{
 		        		fieldInfo.setType(ObjectType.ARRAY);
 
 		    			// special handling for array class names
 		        		fieldInfo.setClassName(ReflectionUtil.getArrayClassName(fieldValue));
 
 		        		if (!fieldInfo.isSetterIgnoreType())
 		        		{
 		        			handleArray(fieldInfo);
 		        		}
 		        	}
 		        	else if (TypeUtil.isCollection(fieldValue))
 		        	{
 		        		fieldInfo.setType(ObjectType.COLLECTION);
 		        		
 		        		// check if the collection field is only accessed through adder methods
 		        		// Note: the adder check overrides the ignored setter check if both are set
 		        		CollectionAdderConfig foundConfig = configUtil.getCollectionAdderConfig(collectionConfigs, fieldName);
 		        		
 		        		if (foundConfig != null)
 		        		{
 		        			fieldInfo.setUsesAdder(true);
 		        			fieldInfo.setAdderMethodName(foundConfig.getAdderMethodName());
 		        			
 		        			handleCollection(fieldInfo);
 		        		}
 		        		else if (!fieldInfo.isSetterIgnoreType())
 		        		{
 		        			handleCollection(fieldInfo);
 		        		}
 		        	}
 		        	else if (TypeUtil.isMap(fieldValue))
 		        	{
 		        		fieldInfo.setType(ObjectType.MAP);
 		        		
 		        		if (!fieldInfo.isSetterIgnoreType())
 		        		{
 		        			handleMap(fieldInfo);
 		        		}
 		        	}
 		        	else
 		        	{
 		        		fieldInfo.setType(ObjectType.OBJECT);
 		        		
 		        		if (!fieldInfo.isSetterIgnoreType())
 		        		{
 		        			handleFields(fieldInfo);
 		        		}
 		        	}
 	            }
 	            else
 	            {
 	            	// get class name from the Field if the field value is null
 	            	fieldInfo.setClassName(ReflectionUtil.getClassNameFromField(field));
 	            	
 	            	fieldInfo.setType(ObjectType.SIMPLE);
 	            }
 	            
 	            // check if configured parameterized constructor is to be used for the field
 	            fieldInfo.setConstructorParamFieldNames(configUtil.getConstructionParameters(fieldInfo));
 	            
 	            // add the parent class to field class link
 	            fieldInfo.setParentInfo(info);
 	            info.addFieldToList(fieldInfo);
             }
         }
     }
 
 	/**
 	 * The handler for array classes. Determines the type of objects
 	 * inside the array and passes them to the appropriate handler.
 	 * 
 	 * @param array
 	 * @throws IllegalAccessException
 	 */
 	protected void handleArray(ObjectInfo info) throws IllegalAccessException {
 		ArrayIterator iter = new ArrayIterator(info.getValue());
 		int index = 0;
 		ContainmentType elementType = ContainmentType.ARRAY_ELEMENT;
 		
 		// handle each array element
 		while(iter.hasNext())
 		{
 			Object elementObject = iter.next();
 
 			ObjectInfo elementInfo = new ObjectInfo();
 			
 			// increment the array index so each array element can later be assigned to successive array slots
 			elementInfo.setIndex(index++);
 
 			
 			if (elementObject != null)
 			{
 				elementInfo.setValue(elementObject);
 	    		elementInfo.setContainingClassFieldName(info.getClassFieldName());
 				elementInfo.setClassName(elementObject.getClass().getName());
 				elementInfo.setHasDefaultConstructor(hasDefaultConstructor(elementObject));
 	    		// no need to check for setter since is an element of an array, so would be assigned instead
 	    		elementInfo.setHasSetter(false);
 				
 				if (TypeUtil.isJavaClass(elementObject))
 				{
 					elementInfo.setType(ObjectType.SIMPLE);
 					elementInfo.setContainmentType(elementType);
 				}
 				else if (TypeUtil.isArray(elementObject))
 		    	{
 					elementInfo.setType(ObjectType.ARRAY);
 					elementInfo.setContainmentType(elementType);
 
 	    			// special handling for array class names
 					elementInfo.setClassName(ReflectionUtil.getArrayClassName(elementObject));
 
 					handleArray(elementInfo);
 		    	}
 				else if (TypeUtil.isMap(elementObject))
 		    	{
 					elementInfo.setType(ObjectType.MAP);
 					elementInfo.setContainmentType(elementType);
 					
 					handleMap(elementInfo);
 		    	}
 				else if (TypeUtil.isCollection(elementObject))
 		    	{
 					elementInfo.setType(ObjectType.COLLECTION);
 					elementInfo.setContainmentType(elementType);
 		    		
 		    		handleCollection(elementInfo);
 		    	}
 		    	else
 		    	{
 					elementInfo.setType(ObjectType.OBJECT);
 					elementInfo.setContainmentType(elementType);
 		    		
 		    		handleFields(elementInfo);
 		    	}				
 			}
 			else
 			{
 				elementInfo.setType(ObjectType.SIMPLE);
 				elementInfo.setContainmentType(elementType);
 			}
 			
 			// add the link between the array and each element
 			elementInfo.setParentInfo(info);
 			info.addFieldToList(elementInfo);
 		}
 	}
 
 	/**
 	 * The handler for Collection implementation classes. Determines the type of objects
 	 * inside the Collection and passes them to the appropriate handler.
 	 * 
 	 * Note that different implementations of Collection will allow / disallow null value for elements.
 	 * 
 	 * @param fieldName
 	 * @param collection
 	 * @throws IllegalAccessException
 	 */
 	protected void handleCollection(ObjectInfo info)
 			throws IllegalAccessException {
 		
 		Collection<?> collection = (Collection<?>)info.getValue();
 		
 		// determine whether the collection is accessed through an adder method in it's containing class
 		ContainmentType elementType = info.isUsesAdder() ? ContainmentType.ADDED_COLLECTION_ELEMENT : ContainmentType.COLLECTION_ELEMENT;
 		
 		// handle each collection element
 		for (Object elementObject : collection)
 	    {
 			ObjectInfo elementInfo = new ObjectInfo();
 			
 			if (elementObject != null)
 			{
 				elementInfo.setValue(elementObject);
 	    		elementInfo.setContainingClassFieldName(info.getClassFieldName());
 				elementInfo.setClassName(elementObject.getClass().getName());
 				elementInfo.setHasDefaultConstructor(hasDefaultConstructor(elementObject));
 	    		// no need to check for setter since is an element of a collection, so would be added to the collection
 	    		elementInfo.setHasSetter(false);
 	    				
 				if (TypeUtil.isJavaClass(elementObject))
 		    	{
 					elementInfo.setType(ObjectType.SIMPLE);
 					elementInfo.setContainmentType(elementType);
 		    	}
 		    	else if (TypeUtil.isArray(elementObject))
 		    	{
 					elementInfo.setType(ObjectType.ARRAY);
 					elementInfo.setContainmentType(elementType);
 
 	    			// special handling for array class names
 					elementInfo.setClassName(ReflectionUtil.getArrayClassName(elementObject));
 
 		    		handleArray(elementInfo);
 		    	}
 		    	else if (TypeUtil.isCollection(elementObject))
 		    	{
 					elementInfo.setType(ObjectType.COLLECTION);
 					elementInfo.setContainmentType(elementType);
 		    		
 		    		handleCollection(elementInfo);
 		    	}
 		    	else if (TypeUtil.isMap(elementObject))
 		    	{
 					elementInfo.setType(ObjectType.MAP);
 					elementInfo.setContainmentType(elementType);
 		    		
 		    		handleMap(elementInfo);
 		    	}
 		    	else
 		    	{
 					elementInfo.setType(ObjectType.OBJECT);
					elementInfo.setContainmentType(ContainmentType.COLLECTION_ELEMENT);
 		    		
 		    		handleFields(elementInfo);
 		    	}
 			}
 			else
 			{
 				// for Collection implementations that allow null elements
 				
 				elementInfo.setType(ObjectType.SIMPLE);
				elementInfo.setContainmentType(ContainmentType.COLLECTION_ELEMENT);
 			}
 			
 			// add the link between the collection and the contained elements
 			elementInfo.setParentInfo(info);
 			info.addFieldToList(elementInfo);
 		}
 	}
 
 	/**
 	 * The handler for Map implementation classes. Determines the type of objects
 	 * inside the Map (both key and value) and passes them to the appropriate handler.
 	 * 
 	 * Note that different implementations of Map will allow / disallow null values for the key
 	 * and / or value.
 	 * 
 	 * @param map
 	 * @throws IllegalAccessException
 	 */
 	protected void handleMap(ObjectInfo info) throws IllegalAccessException {
 		Map map = (Map)info.getValue();
 		ContainmentType elementType = ContainmentType.MAP_ENTRY;
 		
 		// handle each map entry, doing both the key and value for each entry
 		Set<Map.Entry<?, ?>> entrySet = map.entrySet();
 		for (Map.Entry entry : entrySet)
 	    {
 			Object key = entry.getKey();
 			Object value = entry.getValue();
 			
 			ObjectInfo valueInfo = new ObjectInfo();
 			ObjectInfo keyInfo = new ObjectInfo();
 			
 			// handle the map key
 			if (key != null)
 			{
 				keyInfo.setValue(key);
 				keyInfo.setClassName(key.getClass().getName());
 				keyInfo.setContainingClassFieldName(info.getClassFieldName());
 				keyInfo.setHasDefaultConstructor(hasDefaultConstructor(key));
 	    		// no need to check for setter since is a part of an entry to a map, so would be put
 	    		// into the map instead
 	    		keyInfo.setHasSetter(false);
 				
 				if (TypeUtil.isJavaClass(key))
 				{
 					keyInfo.setType(ObjectType.SIMPLE);
 				}
 				else if (TypeUtil.isArray(key))
 				{
 					keyInfo.setType(ObjectType.ARRAY);
 					
 					// special handling for array class names
 					keyInfo.setClassName(ReflectionUtil.getArrayClassName(key));
 					
 					handleArray(keyInfo);
 				}
 				else if (TypeUtil.isCollection(key))
 				{
 					keyInfo.setType(ObjectType.COLLECTION);
 					
 					handleCollection(keyInfo);
 				}
 				else if (TypeUtil.isMap(key))
 				{
 					keyInfo.setType(ObjectType.MAP);
 					
 					handleMap(keyInfo);
 				}
 				else
 				{
 					keyInfo.setType(ObjectType.OBJECT);
 					
 					handleFields(keyInfo);
 				}
 			}
 			else
 			{
 				// for Map implementations that allow null for key
 				
 				keyInfo.setType(ObjectType.SIMPLE);
 			}
 			
 			// handle the map value
 			
 			
 			if (value != null)
 			{
 				valueInfo.setValue(value);
 	    		valueInfo.setContainingClassFieldName(info.getClassFieldName());
 				valueInfo.setClassName(value.getClass().getName());
 				valueInfo.setHasDefaultConstructor(hasDefaultConstructor(value));
 	    		// no need to check for setter since is a part of an entry to a map, so would be put
 	    		// into the map instead
 	    		valueInfo.setHasSetter(false);
 		
 		    	if (TypeUtil.isJavaClass(value))
 		    	{
 					valueInfo.setType(ObjectType.SIMPLE);
 					valueInfo.setContainmentType(elementType);
 		    	}
 		    	else if (TypeUtil.isArray(value))
 	    		{
 					valueInfo.setType(ObjectType.ARRAY);
 					valueInfo.setContainmentType(elementType);
 
 	    			// special handling for array class names
 					valueInfo.setClassName(ReflectionUtil.getArrayClassName(value));
 
 	    			handleArray(valueInfo);
 	    		}
 	    		else if (TypeUtil.isCollection(value))
 	    		{
 					valueInfo.setType(ObjectType.COLLECTION);
 					valueInfo.setContainmentType(elementType);
 					
 	    			handleCollection(valueInfo);
 	    		}
 	    		else if (TypeUtil.isMap(value))
 	    		{
 					valueInfo.setType(ObjectType.MAP);
 					valueInfo.setContainmentType(elementType);
 					
 	    			handleMap(valueInfo);
 	    		}
 	    		else
 	    		{
 					valueInfo.setType(ObjectType.OBJECT);
 					valueInfo.setContainmentType(elementType);
 					
 	    			handleFields(valueInfo);
 	    		}
 			}
 			else
 			{
 				// for Map implementations that allow null for value
 				
 				valueInfo.setType(ObjectType.SIMPLE);
 				valueInfo.setContainmentType(elementType);
 			}
 			
 			// add the link between the key and the value, and also between the map the the contained entries
 			valueInfo.setKeyInfo(keyInfo);
 			valueInfo.setParentInfo(info);
 			info.addFieldToList(valueInfo);
 		}
 	}
 
 	/**
 	 * Check if a class has a default no-argument constructor.
 	 * 
 	 * @param value
 	 * @return
 	 */
 	private boolean hasDefaultConstructor(Object value)
 	{
 		boolean hasDefaultConstructor = false;
 		
     	// no need to check for default constructor for null value, since it doesn't need to be constructed   	
 		if (value != null)
 		{
 			// simple types don't need to be constructed
 			if (!TypeUtil.isJavaClass(value))
 			{
 				// arrays use a different constructor
 				if (!TypeUtil.isArray(value))
 				{
 					hasDefaultConstructor = ReflectionUtil.hasDefaultConstructor(value);
 				}
 			}
 		}
 			
 		return hasDefaultConstructor;
 	}
 }
