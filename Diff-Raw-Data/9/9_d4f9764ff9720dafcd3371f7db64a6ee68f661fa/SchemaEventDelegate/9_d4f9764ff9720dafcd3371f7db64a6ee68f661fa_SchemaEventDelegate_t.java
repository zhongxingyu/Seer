 package org.romaframework.core.schema.reflection;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 
 import org.romaframework.core.schema.SchemaClassDefinition;
 import org.romaframework.core.schema.SchemaEvent;
 import org.romaframework.core.schema.SchemaField;
 import org.romaframework.core.schema.SchemaParameter;
 
 public class SchemaEventDelegate extends SchemaEventReflection {
 
 	private static final long	serialVersionUID	= 8218389759537742464L;
 	private SchemaField				object;
 	private SchemaEvent				delegate;
 
 	public SchemaEventDelegate(SchemaClassDefinition entity, SchemaField object, SchemaEvent delegate) {
 		super(entity, delegate.getName(), new ArrayList<SchemaParameter>(delegate.getParameters().values()));
 		this.object = object;
 		this.delegate = delegate;
 		this.parent = delegate;
 		SchemaEvent event = entity.getEvent(name);
 		if (event instanceof SchemaEventReflection && !(event instanceof SchemaEventDelegate)) {
 			SchemaEventReflection refEvent = (SchemaEventReflection) event;
 			this.method = refEvent.method;
 		}
 	}
 
 	@Override
 	public Object invokeFinal(Object iContent, Object[] params) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
 		if (method != null) {
			return super.invokeFinal(iContent, params);
 		}
 		iContent = object.getValue(iContent);
 		return delegate.invoke(iContent, params);
 	}
 
 	public SchemaField getFieldObject() {
 		return object;
 	}
 
 }
