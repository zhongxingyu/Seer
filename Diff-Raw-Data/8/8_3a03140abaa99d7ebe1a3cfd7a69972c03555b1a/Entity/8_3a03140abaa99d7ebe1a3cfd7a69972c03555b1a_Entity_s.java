 package at.r7r.schemaInject.entity;
 
 import java.io.PrintStream;
 import java.lang.reflect.Field;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import at.r7r.schemaInject.dao.SqlBuilder;
 
 import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
 import com.thoughtworks.xstream.annotations.XStreamOmitField;
 
 public abstract class Entity<ParentType extends Entity<?>> {
 	public interface EqualityHandler {
 		void entitiesDiffer(Entity<?> a, Entity<?> b, Field field);
 		void newEntity(Entity<?> e);
 		void deletedEntity(Entity<?> e);
 	}
 	
 	/**
 	 * The name of the constraint (set it to null to use implicit names)
 	 */
 	@XStreamAsAttribute
 	private String name;
 	
 	/**
 	 * Link to the parent entity (may be null)
 	 */
 	@XStreamOmitField
 	private ParentType parent;
 	
 	/**
 	 * Default constructor
 	 * @param name Entity name
 	 */
 	public Entity(ParentType parent, String name) {
 		this.parent = parent;
 		this.name = name;
 	}
 	
 	/**
 	 * Name getter
 	 * @return Entity name
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * Returns the entity's Parent
 	 * @return Parent entity (might be null)
 	 */
 	public ParentType getParent() {
 		return parent;
 	}
 
 	/**
 	 * Returns whether this entity has a non-empty name
 	 * @return name != null && !name.isEmpty() 
 	 */
 	public boolean hasName() {
 		return name != null && !name.isEmpty();
 	}
 
 	/**
 	 * Sets the name of the Entity (currently only used by {@link Constraint.assignNameIfUnnamed()}
 	 * @param name New Entity name
 	 */
 	protected void setName(String name) {
 		this.name = name;
 	}
 	
 	/**
 	 * Sets the parent object of this Entity
 	 * @param parent New parent object
 	 */
 	protected void setParent(ParentType parent) {
 		this.parent = parent;
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		if (obj instanceof Entity<?>) return equals((Entity<?>) obj, null);
 		else return false;
 	}
 
 	private final boolean isGenericTypeAnEntity(Field field, int typeIndex) {
 		Type type = field.getGenericType();
 		if (!(type instanceof ParameterizedType)) return false;
 		Type typeArgs[] = ((ParameterizedType)type).getActualTypeArguments();
 		if (typeArgs.length <= typeIndex) return false;
 		Type argType = typeArgs[typeIndex];
 		if (argType instanceof Class<?>) {
 			Class<?> argClass = (Class<?>) argType;
 			if (Entity.class.isAssignableFrom(argClass)) return true;
 		}
 		return false;
 	}
 
 	private boolean compareEntityLists(Collection<?> firstList, Collection<?> secondList, EqualityHandler handler) {
 		if (firstList == secondList) return true;
 		else if (firstList == null) return false;
 		else if (secondList == null) return false;
 		boolean rc = true;
 		
 		for (Object o: firstList) {
 			if (!(o instanceof Entity<?>)) return false;
 			Entity<?> e = (Entity<?>) o;
 			Entity<?> otherEntity = getListItemByName(secondList, e.getName());
 			if (!e.equals(otherEntity, handler)) rc = false; 
 		}
 		
 		return rc;
 	}
 
 	/**
 	 * 
 	 * @param fieldName
 	 * @param thisValue
 	 * @param otherValue
 	 * @param handler
 	 * @return
 	 */
 	protected boolean valueEquals(Field field, Object thisValue, Object otherValue, EqualityHandler handler) {
 		boolean rc;
 
 		if (thisValue == null) return thisValue == otherValue;
 		else if (otherValue == null) return false;
 
 		if (!thisValue.getClass().equals(otherValue.getClass())) rc = false;
 		else if (thisValue instanceof Entity<?>) {
 			rc = ((Entity<?>) thisValue).equals((Entity<?>) otherValue, handler);
 		}
 		else if (thisValue instanceof Collection<?>) {
 			boolean itemsAreEntities = isGenericTypeAnEntity(field, 0);
 			Iterator<?> thisIt = ((Iterable<?>)thisValue).iterator();
 			Iterator<?> otherIt = ((Iterable<?>)otherValue).iterator();
 
 			if (itemsAreEntities) {
 				rc = compareEntityLists((Collection<?>)thisValue, (Collection<?>) otherValue, handler);
 			}
 			else {
 				rc = true;
 				while (thisIt.hasNext() && otherIt.hasNext()) {
 					Object thisItem = thisIt.next(), otherItem = otherIt.next();
 					if (itemsAreEntities) {
 						if (!((Entity<?>)thisItem).equals((Entity<?>)otherItem, handler)) rc = false;
 					}
 					else if (!thisItem.equals(otherItem)) {
 						rc = false;
 						break;
 					}
 				}
 			}
 		}
 		else if (thisValue instanceof Map<?,?>) {
			throw new UnsupportedOperationException("Mäh");
 		}
 		else rc = thisValue.equals(otherValue);
 		
 		return rc;
 	}
 	
 	public boolean equals(Entity<?> other, EqualityHandler handler) {
 		if (other == null) return false; // if this was null, a NullPointerException would've been raised already
 		Class<?> cls = getClass();
 		boolean rc = true;
 
 		if (cls != other.getClass()) return false;
 		
 		for (Field field: cls.getDeclaredFields()) {
 			if (field.getAnnotation(XStreamOmitField.class) != null) continue;
 			try {
 				field.setAccessible(true);
 				Object thisValue = field.get(this), otherValue = field.get(other);
 				if (!valueEquals(field, thisValue, otherValue, handler)) {
 					if (handler != null) handler.entitiesDiffer(this, other, field);
 					rc = false;
 				}
 			}
 			catch (IllegalAccessException e) {
 				System.err.println("Entity equals error: Couldn't access field '"+field.getName()+"'");
 			}
 		}
 		return rc;
 	}
 	
 	public void printEquals(Entity<?> other, final PrintStream out) {
 		equals(other, new EqualityHandler() {
 			public void newEntity(Entity<?> e) {
 				out.println("New: "+e);
 			}
 			
 			public void entitiesDiffer(Entity<?> a, Entity<?> b, Field field) {
 				field.setAccessible(true);
 				try {
 					Object thisValue = field.get(a);
 					Object otherValue = field.get(b);
 					System.err.println("Values differ: "+a+" value "+field.getName()+"='"+thisValue+"', other='"+otherValue+"'");
 				}
 				catch (IllegalAccessException e) {
 					throw new RuntimeException(e);
 				}
 			}
 			
 			public void deletedEntity(Entity<?> e) {
 				out.println("Deleted: "+e);
 			}
 		});
 	}
 	
 	public void printEquals(Entity<?> other) {
 		printEquals(other, System.out);
 	}
 	
 	public void polish() {}
 	
 	@Override
 	public String toString() {
 		SqlBuilder hierarchy = new SqlBuilder(".", false);
 		
 		Entity<?> ancestor = this;
 		while (ancestor != null) {
 			String name = ancestor.hasName() ? ancestor.getName() : "";
 			hierarchy.prepend(name);
 			ancestor = ancestor.getParent();
 		}
 		
 		String hier = hierarchy.join();
 		if (hier.startsWith(".")) {
 			hier = hier.substring(1);
 		}
 		
 		return getClass().getSimpleName()+" "+hier;
 	}

 	static <T extends Entity<?>> T getItemByName(List<T> list, String name) {
		return getItemByName(list, name);
 	}
 	
 	protected static Entity<?> getListItemByName(Collection<?> list, String name) {
 		if (name == null) return null;
 		
 		for (Object o: list) {
 			if (!(o instanceof Entity<?>)) continue;
 			Entity<?> e = (Entity<?>) o;
 
 			if (name.equals(e.getName())) return e;
 		}
 		return null;
 	}
 
 }
