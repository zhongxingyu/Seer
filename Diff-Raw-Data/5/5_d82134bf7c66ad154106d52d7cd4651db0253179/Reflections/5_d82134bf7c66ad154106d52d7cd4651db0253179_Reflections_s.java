 package sk.stuba.fiit.perconik.utilities.reflect;
 
 import java.lang.annotation.Annotation;
 import java.util.Arrays;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.Set;
 import com.google.common.base.Function;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 
 public final class Reflections
 {
 	private Reflections()
 	{
 		throw new AssertionError();
 	}
 	
 	private static enum ToAnnotationTypeFunction implements Function<Annotation, Class<? extends Annotation>>
 	{
 		INSTANCE;
 
 		public final Class<? extends Annotation> apply(Annotation annotation)
 		{
 			return annotation.annotationType();
 		}
 	}
 	
 	private static enum ToClassFunction implements Function<Object, Class<? extends Object>>
 	{
 		INSTANCE;
 
 		public final Class<? extends Object> apply(Object object)
 		{
 			return object.getClass();
 		}
 	}
 	
 	private static enum ToEnumTypeFunction implements Function<Enum<?>, Class<?>>
 	{
 		INSTANCE;
 
 		public final Class<?> apply(Enum<?> constant)
 		{
 			return constant.getDeclaringClass();
 		}
 	}
 	
 	private static final <F, T> Function<F, T> cast(final Function<?, ?> function)
 	{
 		// only for stateless internal singletons shared across all types
 		@SuppressWarnings({"unchecked", "rawtypes"})
 		Function<F, T> result = (Function) function;
 		
 		return result;
 	}
 	
 	public static final <A extends Annotation> Function<A, Class<? extends A>> toAnnotationTypeFunction()
 	{
 		return cast(ToAnnotationTypeFunction.INSTANCE);
 	}
 
 	public static final <T> Function<T, Class<? extends T>> toClassFunction()
 	{
 		return cast(ToClassFunction.INSTANCE);
 	}
 	
 	public static final <E extends Enum<E>> Function<E, Class<E>> toEnumTypeFunction()
 	{
 		return cast(ToEnumTypeFunction.INSTANCE);
 	}
 	
 	public static final <T> LinkedList<Class<? super T>> collectSuperclasses(Class<T> type)
 	{
 		LinkedList<Class<? super T>> superclasses = Lists.newLinkedList();
 		
 		Class<? super T> supertype = type;
 		
 		while ((supertype = supertype.getSuperclass()) != null)
 		{
 			superclasses.add(supertype);
 		}
 			
 		return superclasses;
 	}
 	
 	public static final LinkedHashSet<Class<?>> collectInterfaces(Class<?> type)
 	{
 		Set<Class<?>> resolved = Sets.newHashSet();
 		
 		LinkedHashSet<Class<?>> interfaces = Sets.newLinkedHashSet();
 		
 		interfaces.addAll(Arrays.asList(type.getInterfaces()));
 
 		resolved.add(type);
 		
 		for (Class<?> supertype: collectSuperclasses(type))
 		{
 			groupInterfaces(supertype, resolved, interfaces);
 		}
 		
 		return interfaces;
 	}
 	
 	private static final void groupInterfaces(Class<?> type, Set<Class<?>> resolved, Set<Class<?>> result)
 	{
 		if (resolved.add(type))
 		{
 			for (Class<?> supertype: type.getInterfaces())
 			{
 				groupInterfaces(supertype, resolved, result);
 			}
 		}
 		
		result.add(type);
 	}
 }
