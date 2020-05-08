 package hcontexts;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 public class Context implements Iterable<Context> {
 	private static final String				TAIL_PREFIX	= "tail$";
 
 	private static final int				TAIL_LIMIT	= 7;
 
 	private final Map<Property<?>, Object>	properties	= new HashMap<Property<?>, Object>();
 
 	public final String						name;
 	public final Context					parent;
 
 	private Context							next;
 
 	public Context(String name) {
 		this(name, null);
 	}
 
 	protected Context(String name, Context parent) {
 		this.name = name;
 		this.parent = parent;
 	}
 
 	public <T> T get(Property<T> property) {
 		@SuppressWarnings("unchecked")
 		T value = (T) properties.get(property);
 		if (value == null) {
 			return property.defaultValue;
 		}
 		return value;
 	}
 
 	@SuppressWarnings("unchecked")
 	protected <T extends Context> T newIntance(String name, T parent) {
 		return (T) new Context(name, parent);
 	}
 
 	public <T> Context put(Property<T> property, T value) {
 		properties.put(property, value);
 		return this;
 	}
 
 	@SuppressWarnings("unchecked")
 	public <T extends Context> T addInnerContext(String contextName) {
 		T context = (T) newIntance(contextName, this);
 		Property<Context> contextProperty = propertyFor(contextName, Context.class);
 		append(contextProperty, context);
 
 		return context;
 	}
 
 	private void append(Property<Context> contextProperty, Context newContext) {
 		Context context = get(contextProperty);
 		if (context == null) {
 			put(contextProperty, newContext);
 		}
 		else {
 			Property<Context> tailProperty = tailProperty(contextProperty);
 			Context tailContext = get(tailProperty);
 			if (tailContext == null) {
 				int limit = 0;
 				while (context.next != null) {
 					limit++;
 					context = context.next;
 				}
 				context.next = newContext;
 				// the limit > TAIL_LIMIT case can happen if the same context is accessed from
 				// multiple threads. Which is almost surely a horrible idea.
 				if (limit >= TAIL_LIMIT) {
 					put(tailProperty, newContext);
 				}
 			}
 			else {
 				tailContext.next = newContext;
 				put(tailProperty, newContext);
 			}
 		}
 	}
 
 	@Override
 	public Iterator<Context> iterator() {
 		Iterator<Context> iterator = new Iterator<Context>() {
 			Context	currentContext	= Context.this;
 
 			@Override
 			public boolean hasNext() {
 				return currentContext != null;
 			}
 
 			@Override
 			public Context next() {
 				Context context = currentContext;
 				currentContext = currentContext.next;
 
 				return context;
 			}
 
 			@Override
 			public void remove() {
 				throw new UnsupportedOperationException("Remove method not supported");
 			}
 		};
 		return iterator;
 	}
 
 	// Static utility methods and fields
 	private final static ConcurrentMap<String, Property<?>>	propertyPool	= new ConcurrentHashMap<String, Property<?>>();
 
 	public static <T> Property<T> propertyFor(String name, Class<T> clazz) {
 		return propertyFor(name, clazz, null);
 	}
 
 	public static <T> Property<T> propertyFor(String name, T defaultValue)
 			throws PropertyWithADifferentDefaultValueAlreadyExists {
 		@SuppressWarnings("unchecked")
 		Class<T> clazz = (Class<T>) defaultValue.getClass();
 
 		Property<T> property = propertyFor(name, clazz, defaultValue);
 		if (!defaultValue.equals(property.defaultValue)) {
 			throw new PropertyWithADifferentDefaultValueAlreadyExists();
 		}
 		return property;
 	}
 
 	public static <T> void removeProperty(String name, Class<T> clazz) {
 		String id = Property.id(name, clazz);
 		propertyPool.remove(id);
 
 		// Dunno, dunno.. The check can be more costly than the removal of an missing key.
 		if (Context.class.isAssignableFrom(clazz)) {
 			propertyPool.remove(TAIL_PREFIX + id);
 		}
 	}
 
 	private static <T> Property<T> propertyFor(String name, Class<T> clazz, T defaultValue) {
 		final String id = Property.id(name, clazz);
 
 		@SuppressWarnings("unchecked")
 		Property<T> property = (Property<T>) propertyPool.get(id);
 		if (property == null) {
 			Property<T> newProperty = new Property<T>(name, clazz, defaultValue);
 			@SuppressWarnings("unchecked")
			Property<T> oldProperty = (Property<T>) propertyPool.put(id, newProperty);
 			if (oldProperty == null) {
 				return newProperty;
 			}
 			else {
 				return oldProperty;
 			}
 		}
 
 		return property;
 	}
 
 	private static <T> Property<T> tailProperty(Property<T> p) {
 		final String id = TAIL_PREFIX + Property.id(p.name, p.clazz);
 
 		@SuppressWarnings("unchecked")
 		Property<T> property = (Property<T>) propertyPool.get(id);
 		if (property == null) {
 			Property<T> newProperty = new Property<T>(p.name, p.clazz, p.defaultValue);
 			@SuppressWarnings("unchecked")
			Property<T> oldProperty = (Property<T>) propertyPool.put(id, newProperty);
 			if (oldProperty == null) {
 				return newProperty;
 			}
 			else {
 				return oldProperty;
 			}
 		}
 
 		return property;
 	}
 }
