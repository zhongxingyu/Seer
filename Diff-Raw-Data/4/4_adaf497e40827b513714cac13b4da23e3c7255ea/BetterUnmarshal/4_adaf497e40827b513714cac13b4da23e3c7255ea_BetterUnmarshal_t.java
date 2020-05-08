 package br.com.six2six.xstreamdsl.unmarshal;
 
 import static br.com.six2six.xstreamdsl.util.ReflectionUtils.genericTypeFromField;
 import static br.com.six2six.xstreamdsl.util.ReflectionUtils.invokeRecursiveSetter;
 import static br.com.six2six.xstreamdsl.util.ReflectionUtils.invokeRecursiveType;
 import static br.com.six2six.xstreamdsl.util.ReflectionUtils.newInstance;
 import static br.com.six2six.xstreamdsl.util.ReflectionUtils.newInstanceCollection;
 import static org.apache.commons.lang.ClassUtils.isAssignable;
 import static org.apache.commons.lang.ClassUtils.primitiveToWrapper;
 
 import java.util.Collection;
 
 import br.com.six2six.xstreamdsl.unmarshal.transform.EnumTransformer;
 import br.com.six2six.xstreamdsl.unmarshal.transform.NumberTransformer;
 import br.com.six2six.xstreamdsl.unmarshal.transform.Transformer;
 
 import com.thoughtworks.xstream.converters.UnmarshallingContext;
 import com.thoughtworks.xstream.io.HierarchicalStreamReader;
 
 public class BetterUnmarshal<T> {
 
 	private T bean;
 	
 	private final HierarchicalStreamReader reader;
 	private final UnmarshallingContext context;
 
 	private BetterUnmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
 		this.reader = reader;
 		this.context = context;
 	}
 	
 	public static <T> BetterUnmarshal<T> build(HierarchicalStreamReader reader, UnmarshallingContext context) {
 		return new BetterUnmarshal<T>(reader, context);
 	}
 	
 	@SuppressWarnings("unchecked")
 	public BetterUnmarshal<T> to(Class<?> clazz) {
 		bean = (T) newInstance(clazz);
 		return this;
 	}
 
 	public BetterUnmarshal<T> node(String property) {
 		invokeRecursiveSetter(bean, property, transform(property, getValue()));
 		return this;
 	}
 
 	public BetterUnmarshal<T> node(String property, Transformer transformer) {
 		invokeRecursiveSetter(bean, property, transform(property, getValue(), transformer));
 		return this;
 	}
 
 	public BetterUnmarshal<T> delegate(String property) {
 		Class<?> type = invokeRecursiveType(bean, property);
 		reader.moveDown();
 		invokeRecursiveSetter(bean, property, context.convertAnother(bean, type));
 		reader.moveUp();
 		return this;
 	}
 	
 	public BetterUnmarshal<T> collection(String property) {
 		Class<?> type = invokeRecursiveType(bean, property);
 		
 		Collection<Object> collection = newInstanceCollection(type);
 		
 		reader.moveDown();
 		while (reader.hasMoreChildren()) {
			reader.moveDown();
 			collection.add(context.convertAnother(bean, genericTypeFromField(bean, property)));
			reader.moveUp();
 		}
 		reader.moveUp();
 		
 		invokeRecursiveSetter(bean, property, collection);
 		return this;
 	}
 	
 	public T get() {
 		return bean;
 	}
 
 	private String getValue() {
 		if (!reader.hasMoreChildren()) return null;
 		
 		reader.moveDown();
 		String value = reader.getValue();
 		reader.moveUp();
 		return value;
 	}
 	
 	private Object transform(String property, String value) {
 		Class<?> type = primitiveToWrapper(invokeRecursiveType(bean, property));
 		
 		if (isAssignable(type, Number.class)) {
 			return new NumberTransformer().transform(value, type);
 		
 		} else if (isAssignable(type, Enum.class)) { 
 			return new EnumTransformer().transform(value, type);
 		}
 
 		return value;
 	}
 	
 	private Object transform(String property, String value, Transformer tranformer) {
 		Class<?> type = primitiveToWrapper(invokeRecursiveType(bean, property));
 		return tranformer.transform(value, type);
 	}
 }
