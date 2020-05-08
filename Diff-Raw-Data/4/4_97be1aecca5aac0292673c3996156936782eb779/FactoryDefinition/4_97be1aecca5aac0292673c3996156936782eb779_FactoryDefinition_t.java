 package se.l4.aurochs.serialization.internal.reflection;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import se.l4.aurochs.serialization.Expose;
 import se.l4.aurochs.serialization.SerializationException;
 import se.l4.aurochs.serialization.SerializerCollection;
 
 import com.fasterxml.classmate.ResolvedType;
 import com.fasterxml.classmate.members.ResolvedConstructor;
import com.google.common.primitives.Primitives;
 
 /**
  * Factory that can be used to create an instance of a certain object.
  * 
  * @author Andreas Holstenson
  *
  */
 public class FactoryDefinition<T>
 {
 	private final SerializerCollection collection;
 	private final Argument[] arguments;
 	private final boolean hasSerializedFields;
 	private final Constructor<?> raw;
 
 	public FactoryDefinition(SerializerCollection collection,
 			Map<String, FieldDefinition> fields,
 			ResolvedConstructor constructor)
 	{
 		this.collection = collection;
 		
 		List<Argument> args = new ArrayList<FactoryDefinition.Argument>();
 		
 		raw = constructor.getRawMember();
 		Annotation[][] annotations = raw.getParameterAnnotations();
 		
 		boolean hasSerializedFields = false;
 		
 		for(int i=0, n=constructor.getArgumentCount(); i<n; i++)
 		{
 			ResolvedType type = constructor.getArgumentType(i);
 			
 			Expose expose = findExpose(annotations[i]);
 			if(expose != null)
 			{
 				// Try to serialize
 				if("".equals(expose.value()))
 				{
 					throw new SerializationException("The annotation @" + 
 						Expose.class.getSimpleName() + 
 						" when used in a constructor must have a name (for " + 
 						raw.getDeclaringClass() + ")");
 				}
 				
 				FieldDefinition def = fields.get(expose.value());
 				if(def == null)
 				{
 					throw new SerializationException(expose + " was used on a " +
 							"constructor but the there was no such field declared" + 
 							" (for " + raw.getDeclaringClass() + ")");
 				}
				else if(Primitives.wrap(def.getType()) != Primitives.wrap(type.getErasedType()))
 				{
 					throw new SerializationException(expose + " was used on a " +
 						"constructor but the type of the argument was different " +
 						"from the field. The field was resolved to " + 
 						def.getType() + " but the argument was of type " + 
 						type.getErasedType() + 
 						" (for " + raw.getDeclaringClass() + ")");
 				}
 				
 				args.add(new SerializedArgument(expose.value()));
 				hasSerializedFields = true;
 			}
 			else
 			{
 				args.add(new InjectedArgument(type.getErasedType(), annotations[i]));
 			}
 		}
 		
 		arguments = args.toArray(new Argument[args.size()]);
 		this.hasSerializedFields = hasSerializedFields;
 	}
 	
 	private static Expose findExpose(Annotation[] annotations)
 	{
 		for(Annotation a : annotations)
 		{
 			if(a instanceof Expose)
 			{
 				return (Expose) a;
 			}
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * Get if this factory has any serialized fields.
 	 * 
 	 * @return
 	 */
 	public boolean hasSerializedFields()
 	{
 		return hasSerializedFields;
 	}
 	
 	/**
 	 * Get a score for this factory based on the given data. The higher the
 	 * score the more arguments were found.
 	 * 
 	 * @param data
 	 * @return
 	 */
 	public int getScore(Map<String, Object> data)
 	{
 		if(! hasSerializedFields)
 		{
 			return 0;
 		}
 		
 		int score = 0;
 		
 		for(Argument arg : arguments)
 		{
 			if(arg instanceof FactoryDefinition.SerializedArgument)
 			{
 				if(data.containsKey(((SerializedArgument) arg).name))
 				{
 					score++;
 				}
 			}
 		}
 		
 		return score;
 	}
 	
 	/**
 	 * Create a new instance using the given deserialized data. The data
 	 * is only used if this factory has any serialized fields.
 	 * 
 	 * @param data
 	 * @return
 	 */
 	public T create(Map<String, Object> data)
 	{
 		Object[] args = new Object[arguments.length];
 		for(int i=0, n=args.length; i<n; i++)
 		{
 			args[i] = arguments[i].getValue(data);
 		}
 		
 		try
 		{
 			return (T) raw.newInstance(args);
 		}
 		catch(IllegalArgumentException e)
 		{
 			throw new SerializationException("Unable to create; " + e.getMessage(), e);
 		}
 		catch(InstantiationException e)
 		{
 			throw new SerializationException("Unable to create; " + e.getMessage(), e);
 		}
 		catch(IllegalAccessException e)
 		{
 			throw new SerializationException("Unable to create; " + e.getMessage(), e);
 		}
 		catch(InvocationTargetException e)
 		{
 			throw new SerializationException("Unable to create; " + e.getCause().getMessage(), e.getCause());
 		}
 	}
 	
 	private interface Argument
 	{
 		Object getValue(Map<String, Object> data);
 	}
 	
 	private class SerializedArgument
 		implements Argument
 	{
 		private final String name;
 
 		public SerializedArgument(String name)
 		{
 			this.name = name;
 		}
 		
 		@Override
 		public Object getValue(Map<String, Object> data)
 		{
 			return data.get(name);
 		}
 	}
 	
 	private class InjectedArgument
 		implements Argument
 	{
 		private final Class<?> type;
 		private final Annotation[] annotations;
 
 		public InjectedArgument(Class<?> type, Annotation[] annotations)
 		{
 			this.type = type;
 			this.annotations = annotations;
 		}
 
 		@Override
 		public Object getValue(Map<String, Object> data)
 		{
 			return collection.getInstanceFactory()
 				.create(type, annotations);
 		}
 		
 	}
 }
