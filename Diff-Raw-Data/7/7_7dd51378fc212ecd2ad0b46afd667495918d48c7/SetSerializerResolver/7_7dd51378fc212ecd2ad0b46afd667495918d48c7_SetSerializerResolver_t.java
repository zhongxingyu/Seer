 package se.l4.aurochs.serialization.collections;
 
 import java.util.Set;
 
 import se.l4.aurochs.serialization.SerializationException;
 import se.l4.aurochs.serialization.Serializer;
 import se.l4.aurochs.serialization.spi.SerializerResolver;
 import se.l4.aurochs.serialization.spi.Type;
 import se.l4.aurochs.serialization.spi.TypeEncounter;
 import se.l4.aurochs.serialization.spi.TypeViaClass;
 import se.l4.aurochs.serialization.standard.DynamicSerializer;
 
 public class SetSerializerResolver
 	implements SerializerResolver<Set<?>>
 {
 
 	@Override
 	public Serializer<Set<?>> find(TypeEncounter encounter)
 	{
 		Type[] params = encounter.getType().getParameters();
 		Type type = params.length == 0 ? new TypeViaClass(Object.class) : params[0];  
 		
 		// Check that we can create the type of list requested
 		Class<?> erasedType = encounter.getType().getErasedType();
		if(erasedType != Set.class)
 		{
			throw new SerializationException("Sets can only be serialized if they are declared as the interface Set");
 		}
 		
 		// Create the serializer, either a specific or dynamic
 		Serializer<?> itemSerializer =
 			encounter.getHint(AllowAnyItem.class) == null
 				? encounter.getCollection().find(type)
 				: new DynamicSerializer(encounter.getCollection());
 			
 		return new SetSerializer(itemSerializer);
 	}
 
 }
