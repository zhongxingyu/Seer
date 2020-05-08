 package se.l4.aurochs.serialization;
 
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.List;
 
 import se.l4.aurochs.serialization.format.StreamingInput;
 import se.l4.aurochs.serialization.format.StreamingOutput;
 import se.l4.aurochs.serialization.internal.TypeViaResolvedType;
 import se.l4.aurochs.serialization.internal.reflection.FactoryDefinition;
 import se.l4.aurochs.serialization.internal.reflection.FieldDefinition;
 import se.l4.aurochs.serialization.internal.reflection.ReflectionNonStreamingSerializer;
 import se.l4.aurochs.serialization.internal.reflection.ReflectionStreamingSerializer;
 import se.l4.aurochs.serialization.internal.reflection.TypeInfo;
 import se.l4.aurochs.serialization.spi.Type;
 import se.l4.aurochs.serialization.standard.DynamicSerializer;
 
 import com.fasterxml.classmate.MemberResolver;
 import com.fasterxml.classmate.ResolvedType;
 import com.fasterxml.classmate.ResolvedTypeWithMembers;
 import com.fasterxml.classmate.TypeResolver;
 import com.fasterxml.classmate.members.ResolvedConstructor;
 import com.fasterxml.classmate.members.ResolvedField;
 import com.google.common.collect.ImmutableMap;
 
 /**
  * Serializer that will use reflection to access fields and methods in a
  * class. Will export anything annotated with {@link Expose}.
  * 
  * <p>
  * <ul>
  * 	<li>{@link Named} can be used if you want a field to have a specific name
  * 		in serialized form.
  * 	<li>If you need to use a custom serializer for a field annotate it with
  * 		{@link Use}.
  * 	<li>{@link AllowAny} will cause dynamic serialization to be used for a
  * 		field.
  * </ul>
  * 
  * @author Andreas Holstenson
  */
 public class ReflectionSerializer<T>
 	implements Serializer<T>
 {
 	private ReflectionSerializer()
 	{
 	}
 	
 	@Override
 	public T read(StreamingInput in)
 		throws IOException
 	{
 		return null;
 	}
 	
 	public void write(T object, String name, StreamingOutput stream)
 		throws IOException
 	{
 	}
 	
 	/**
 	 * Create a new serializer that will use reflection.
 	 * 
 	 * @param type
 	 * @param collection
 	 * @return
 	 */
 	public static <T> Serializer<T> create(Type type, SerializerCollection collection)
 	{
 		// TODO: Support for constructors
 		
 		ImmutableMap.Builder<String, FieldDefinition> builder = ImmutableMap.builder();
 		
 		TypeResolver typeResolver = new TypeResolver();
 		MemberResolver memberResolver = new MemberResolver(typeResolver);
 		
 		Type[] params = type.getParameters();
 		ResolvedType rt = params.length == 0 
 			? typeResolver.resolve(type.getErasedType())
 			: resolveWithParams(typeResolver, type);
 		
 		ResolvedTypeWithMembers typeWithMembers = memberResolver.resolve(rt, null, null);
 		
 		for(ResolvedField field : typeWithMembers.getMemberFields())
 		{
 			Field reflectiveField = field.getRawMember();
 			
 			if(! reflectiveField.isAnnotationPresent(Expose.class))
 			{
 				continue;
 			}
 			
 			// Resolve the serializer to use for the field
 			ResolvedType fieldType = field.getType();
 			
 			Serializer<?> serializer;
 			if(reflectiveField.isAnnotationPresent(Use.class))
 			{
 				// Serializer has been set to a specific type
 				Use annotation = reflectiveField.getAnnotation(Use.class);
 				try
 				{
 					serializer = collection.getInstanceFactory().create(annotation.value());
 				}
 				catch(Exception e)
 				{
 					throw new SerializationException("Unable to create " + annotation.value() + " for " + type + "; " + e.getMessage(), e);
 				}
 			}
 			else if(reflectiveField.isAnnotationPresent(AllowAny.class))
 			{
 				serializer = new DynamicSerializer(collection);
 			}
 			else
 			{
 				// Dynamically find a suitable type
 				try
 				{
 					serializer = collection.find(new TypeViaResolvedType(fieldType), reflectiveField.getAnnotations());
 				}
 				catch(SerializationException e)
 				{
					throw new SerializationException("Could not resolve " + field.getName() + " for " + type.getErasedType() + "; " + e.getMessage(), e);
 				}
 			}
			
			if(serializer == null)
			{
				throw new SerializationException("Could not resolve " + field.getName() + " for " + type.getErasedType() + "; No serializer found");
			}
 
 			// Force the field to be accessible
 			reflectiveField.setAccessible(true);
 			
 			// Define how we access this field
 			String name = getName(reflectiveField);
 			FieldDefinition def = new FieldDefinition(reflectiveField, name, serializer, fieldType.getErasedType());
 			builder.put(name, def);
 		}
 		
 		// Create field map and cache
 		ImmutableMap<String, FieldDefinition> fields = builder.build();
 		FieldDefinition[] fieldsCache = fields.values().toArray(new FieldDefinition[0]);
 		
 		// Get all of the factories
 		boolean hasSerializerInFactory = false;
 		List<FactoryDefinition<T>> factories = new ArrayList<FactoryDefinition<T>>();
 		
 		for(ResolvedConstructor constructor : typeWithMembers.getConstructors())
 		{
 			FactoryDefinition<T> def = new FactoryDefinition<T>(collection, fields, constructor);
 			hasSerializerInFactory |= def.hasSerializedFields();
 			
 			factories.add(def);
 		}
 		
 		if(factories.isEmpty())
 		{
 			throw new SerializationException("Unable to create any instance of " + type + ", at least a default constructor is needed");
 		}
 		
 		FactoryDefinition<T>[] factoryCache = factories.toArray(new FactoryDefinition[factories.size()]);
 		
 		// Create the actual serializer to use
 		TypeInfo<T> typeInfo = new TypeInfo<T>((Class) type.getErasedType(), factoryCache, fields, fieldsCache);
 		
 		return hasSerializerInFactory 
 			? new ReflectionNonStreamingSerializer<T>(typeInfo)
 			: new ReflectionStreamingSerializer<T>(typeInfo);
 	}
 	
 	private static ResolvedType resolveWithParams(TypeResolver typeResolver, Type type)
 	{
 		if(type instanceof TypeViaResolvedType)
 		{
 			return ((TypeViaResolvedType) type).getResolvedType();
 		}
 		
 		return null;
 	}
 
 	private static String getName(Field field)
 	{
 		if(field.isAnnotationPresent(Expose.class))
 		{
 			Expose annotation = field.getAnnotation(Expose.class);
 			if(! "".equals(annotation.value()))
 			{
 				return annotation.value();
 			}
 		}
 		
 		return field.getName();
 	}
 }
