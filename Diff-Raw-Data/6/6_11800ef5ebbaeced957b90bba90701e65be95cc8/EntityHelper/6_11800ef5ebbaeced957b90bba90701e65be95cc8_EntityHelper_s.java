 /*
  * Copyright 2006 Luca Garulli (luca.garulli--at--assetdata.it)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.romaframework.core.entity;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.romaframework.core.Roma;
 import org.romaframework.core.config.ContextException;
 import org.romaframework.core.domain.entity.ComposedEntity;
 import org.romaframework.core.exception.LocalizedRuntimeException;
 import org.romaframework.core.factory.GenericFactory;
 import org.romaframework.core.schema.SchemaClass;
 import org.romaframework.core.schema.SchemaClassDefinition;
 
 public class EntityHelper {
 	public static Object createObject(Object iSourceEntity, SchemaClass iClass) throws IllegalArgumentException, InstantiationException, IllegalAccessException,
 			InvocationTargetException {
 		Object newInstance = null;
 
 		try {
 			// TRY TO USE THE FACTORY IF ANY
 			GenericFactory<?> factory = Roma.component(iClass.getName() + GenericFactory.DEF_SUFFIX);
 
 			// INVOKE THE EMPTY CONSTRUCTOR
 			newInstance = factory.create();
 		} catch (ContextException ex) {
 			// FACTORY NOT DEFINED, USE THE REFLECTION
 			if (iSourceEntity == null) {
 				newInstance = createObjectUsingDefaultConstructor(iClass);
 			} else {
 				try {
 					newInstance = iClass.newInstance(iSourceEntity);
 				} catch (NoSuchMethodException e) {
 				}
 
 				if (newInstance == null) {
 					// INSTANCE THE OBJECT AND ASSIGN THE ENTITY AFTER BY SETTER
 					// METHOD
 					newInstance = createObjectUsingDefaultConstructor(iClass);
 
 					// CREATE THE COMPOSED EDIT INSTANCE
 					if (newInstance instanceof ComposedEntity<?>)
 						((ComposedEntity<Object>) newInstance).setEntity(iSourceEntity);
 				}
 			}
 		}
 
 		return newInstance;
 	}
 
 	private static Object createObjectUsingDefaultConstructor(SchemaClass iClass) {
 		try {
 			return iClass.newInstance();
 		} catch (InstantiationException e) {
 			throw new LocalizedRuntimeException("Cannot create object of class " + iClass + " since it has no default constructor!", e);
 		} catch (IllegalAccessError e) {
 			throw new LocalizedRuntimeException("Cannot create object of class " + iClass + " since constructor is not visible! Change its visibility to public.", e);
 		} catch (Exception e) {
 			throw new LocalizedRuntimeException("Cannot create object of class " + iClass + " since an error was thrown by executing the default constructor!", e);
 		}
 	}
 
 	public static Object getEntityObjectIfNeeded(Object iValue, SchemaClassDefinition target) {
 		SchemaClass sc = Roma.schema().getSchemaClass(iValue);
 		if (sc == null || target == null || sc.isAssignableAs(target.getSchemaClass()))
 			return iValue;
		return getEntityObject(iValue);
 	}
 
 	public static Object getEntityObject(Object iSource) {
 		if (iSource instanceof ComposedEntity<?>)
 			return ((ComposedEntity<?>) iSource).getEntity();
 		return iSource;
 	}
 
 	public static void assignEntity(Object iComposedObject, Object iSourceEntity) {
 		if (iComposedObject instanceof ComposedEntity<?>)
 			// CREATE THE COMPOSED EDIT INSTANCE
 			((ComposedEntity<Object>) iComposedObject).setEntity(iSourceEntity);
 	}
 
 	public static Object[] getEntitiesArray(ComposedEntity<?>[] iEntities) {
 		Object[] objects = new Object[iEntities.length];
 		for (int i = 0; i < iEntities.length; ++i) {
 			objects[i] = iEntities[i].getEntity();
 		}
 		return objects;
 	}
 
 	public static List<? extends ComposedEntity<?>> createComposedEntityList(Collection<?> iObjectList, SchemaClass iListClass) throws IllegalArgumentException,
 			InstantiationException, IllegalAccessException, InvocationTargetException {
 		List<ComposedEntity<?>> tempResult = null;
 		if (iListClass.isAssignableAs(ComposedEntity.class)) {
 			// CREATE THE COMPOSED RESULT SET
 			tempResult = new ArrayList<ComposedEntity<?>>();
 			ComposedEntity<?> entity;
 			if (iObjectList != null) {
 				for (Object o : iObjectList) {
 					entity = (ComposedEntity<?>) createObject(o, iListClass);
 					tempResult.add(entity);
 				}
 			}
 		}
 		return tempResult;
 	}
 
 	public Object createObjectPassingEntity(Class<?> iClass, Object iSourceEntity) {
 		Object newInstance = null;
 		Class<?> currentClass = iSourceEntity.getClass();
 		do {
 			try {
 				// TRY TO ASSIGN THE ENTITY IN THE CONSTRUCTOR
 				Constructor<?> constructor = iClass.getConstructor(new Class<?>[] { currentClass });
 				newInstance = constructor.newInstance(new Object[] { iSourceEntity });
 				break;
 			} catch (Exception e) {
 				currentClass = currentClass.getSuperclass();
 			}
 		} while (currentClass != null && currentClass != Object.class);
 		return newInstance;
 	}
 }
