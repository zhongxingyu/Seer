 package org.romaframework.core.schema.reflection;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.romaframework.core.Roma;
 import org.romaframework.core.aspect.Aspect;
 import org.romaframework.core.binding.BindingException;
 import org.romaframework.core.flow.Controller;
 import org.romaframework.core.flow.SchemaFieldListener;
 import org.romaframework.core.schema.FeatureLoader;
 import org.romaframework.core.schema.SchemaClass;
 import org.romaframework.core.schema.SchemaClassDefinition;
 import org.romaframework.core.schema.SchemaField;
import org.romaframework.core.schema.SchemaParameter;
 import org.romaframework.core.schema.config.SchemaConfiguration;
 import org.romaframework.core.schema.xmlannotations.XmlEventAnnotation;
 import org.romaframework.core.schema.xmlannotations.XmlFieldAnnotation;
 
 public class SchemaFieldReflection extends SchemaField {
 
 	private static final long	serialVersionUID	= 8401682154724053320L;
 
 	protected Field						field;
 	protected Class<?>				languageType;
 	protected Method					getterMethod;
 	protected Method					setterMethod;
 	private static Log				log								= LogFactory.getLog(SchemaFieldReflection.class);
 
 	public SchemaFieldReflection(SchemaClassDefinition entity, String iName) {
 		super(entity, iName);
 	}
 
 	@Override
 	public boolean isArray() {
 		return getLanguageType().isArray();
 	}
 
 	public void configure() {
 
 		SchemaConfiguration classDescriptor = entity.getSchemaClass().getDescriptor();
 
 		XmlFieldAnnotation parentDescriptor = null;
 
 		if (classDescriptor != null && classDescriptor.getType() != null && classDescriptor.getType().getFields() != null) {
 			// SEARCH FORM DEFINITION IN DESCRIPTOR
 			parentDescriptor = classDescriptor.getType().getField(name);
 		}
 
 		FeatureLoader.loadFieldFeatures(this, parentDescriptor);
 
 		// BROWSE ALL ASPECTS
 		for (Aspect aspect : Roma.aspects()) {
 			aspect.configField(this);
 		}
 
 		if (parentDescriptor != null && parentDescriptor.getEvents() != null) {
 			Set<XmlEventAnnotation> events = parentDescriptor.getEvents();
 			for (XmlEventAnnotation xmlConfigEventType : events) {
 
 				SchemaEventReflection eventInfo = (SchemaEventReflection) getEvent(xmlConfigEventType.getName());
 
 				if (eventInfo == null) {
 					// EVENT NOT EXISTENT: CREATE IT AND INSERT IN THE COLLECTION
					eventInfo = new SchemaEventReflection(this, xmlConfigEventType.getName(), new ArrayList<SchemaParameter>());
 					setEvent(xmlConfigEventType.getName(), eventInfo);
 				}
 				eventInfo.configure();
 			}
 		}
 	}
 
 	@Override
 	public Object getValue(Object iObject) throws BindingException {
 		if (iObject == null)
 			return null;
 
 		List<SchemaFieldListener> listeners = Controller.getInstance().getListeners(SchemaFieldListener.class);
 
 		try {
 			// CREATE THE CONTEXT BEFORE TO CALL THE ACTION
 			Roma.context().create();
 
 			// CALL ALL LISTENERS BEFORE TO RETURN THE VALUE
 			Object value = invokeCallbackBeforeFieldRead(listeners, iObject);
 
 			if (value != SchemaFieldListener.IGNORED)
 				return value;
 
 			// TRY TO INVOKE GETTER IF ANY
 			if (getterMethod != null) {
 				try {
 					value = getterMethod.invoke(iObject, (Object[]) null);
 				} catch (IllegalArgumentException e) {
 					// PROBABLY AN ISSUE OF CLASS VERSION
 					throw new BindingException(iObject, name,
 							"Error on using different classes together. Maybe it's an issue due to class reloading. Assure to use objects of the same class.");
 				} catch (InvocationTargetException e) {
 					Throwable nested = e.getCause();
 					if (nested == null) {
 						nested = e.getTargetException();
 					}
 
 					// BYPASS REFLECTION EXCEPTION: THROW REAL NESTED EXCEPTION
 					throw new BindingException(iObject, name, nested);
 				}
 			} else {
 				// READ FROM FIELD
 				if (!field.isAccessible()) {
 					field.setAccessible(true);
 				}
 
 				value = field.get(iObject);
 			}
 
 			// CALL ALL LISTENERS BEFORE TO RETURN THE VALUE
 			value = invokeCallbackAfterFieldRead(listeners, iObject, value);
 
 			return value;
 
 		} catch (Exception e) {
 			throw new BindingException(iObject, name, e);
 		} finally {
 
 			// ASSURE TO DESTROY THE CONTEXT
 			Roma.context().destroy();
 		}
 	}
 
 	protected void setValueFinal(Object iObject, Object iValue) throws IllegalAccessException, InvocationTargetException {
 		// TRY TO INVOKE SETTER IF ANY
 		if (setterMethod != null) {
 			setterMethod.invoke(iObject, new Object[] { iValue });
 		} else {
 			// WRITE THE FIELD
 			if (field == null) {
 				log.debug("[SchemaHelper.setFieldValue] Cannot set the value '" + iValue + "' for field '" + name + "' on object " + iObject
 						+ " since it has neither setter neir field declared");
 				return;
 			}
 
 			if (!field.isAccessible()) {
 				field.setAccessible(true);
 			}
 
 			field.set(iObject, iValue);
 		}
 	}
 
 	@Override
 	public String toString() {
 		return name + " (field:" + field + ")";
 	}
 
 	public Field getField() {
 		return field;
 	}
 
 	public Method getGetterMethod() {
 		return getterMethod;
 	}
 
 	public Method getSetterMethod() {
 		return setterMethod;
 	}
 
 	public Class<?> getLanguageType() {
 		return languageType;
 	}
 
 	public void setLanguageType(Class<?> clazz) {
 		languageType = clazz;
 	}
 
 	@Override
 	protected SchemaClass getSchemaClassFromLanguageType() {
 		return Roma.schema().getSchemaClass(getLanguageType());
 	}
 
 }
