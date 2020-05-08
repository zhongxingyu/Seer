 package org.romaframework.core.schema.reflection;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 
 import org.romaframework.core.schema.SchemaAction;
 import org.romaframework.core.schema.SchemaClassDefinition;
 import org.romaframework.core.schema.SchemaField;
 import org.romaframework.core.schema.SchemaParameter;
 
 public class SchemaActionDelegate extends SchemaActionReflection {
 
 	private static final long	serialVersionUID	= 8218389759537742464L;
 	private SchemaField				object;
 	private SchemaAction			delegate;
 
 	public SchemaActionDelegate(SchemaClassDefinition entity, SchemaField object, SchemaAction delegate) {
 		super(entity, delegate.getName(), new ArrayList<SchemaParameter>(delegate.getParameters().values()));
 		this.object = object;
 		this.delegate = delegate;
 		this.parent = delegate;
 		SchemaAction action = entity.getAction(name);
 		if (action instanceof SchemaActionReflection && !(action instanceof SchemaActionDelegate)) {
 			SchemaActionReflection refAction = (SchemaActionReflection) action;
 			this.method = refAction.method;
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
 
 	@Override
 	public Method getMethod() {
 		if (this.method != null)
 			return super.getMethod();
 		if (this.delegate instanceof SchemaActionReflection) {
 			return ((SchemaActionReflection) this.delegate).getMethod();
 		}
 		return null;
 	}
 }
