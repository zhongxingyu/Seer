 package com.barchart.missive.core;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.barchart.missive.api.Tag;
 import com.barchart.missive.util.ClassUtil;
 
 public final class TagFactory {
 	
 	private static AtomicInteger counter = new AtomicInteger(0);
 
 	public static final Logger log = LoggerFactory.getLogger(TagFactory.class);
 			
 	/** FIXME use thread-safe immutable set */
 	private static final Set<Class<?>> primitives = new HashSet<Class<?>>();
 	
 	private final static int IS_ENUM = 0x1;
 	private final static int IS_LIST = 0x2;
 	private final static int IS_PRIM = 0x4;
 	
 	static {
 		primitives.add(Byte.class);
 		primitives.add(Short.class);
 		primitives.add(Integer.class);
 		primitives.add(Long.class);
 		primitives.add(Float.class);
 		primitives.add(Double.class);
 		primitives.add(Boolean.class);
 		primitives.add(Character.class);
 	}
 	
 	private TagFactory() {
 		
 	}
 	
 	/* ***** ***** Begin public static methods ***** ***** */
 	
 	/**
 	 * Create tag for given type using constant field name as tag name.
 	 * 
 	 * @throws MissiveException
 	 */
 	public static <V> Tag<V> create(final Class<V> clazz)
 			throws MissiveException {
 		return build("Tag=" + counter.get(), clazz, nextIndex(), mask(clazz));
 	}
 	
 	/**
 	 * Create tag for given name and type.
 	 */
 	public static <V> Tag<V> create(final String name, final Class<V> clazz) {
 		return build(name, clazz, nextIndex(), mask(clazz));
 	}
 	
 	/**
 	 * Collect all tags from provided type.
 	 * 
 	 * @throws MissiveException
 	 */
 	public static Tag<?>[] collectAll(final Class<?> clazz)
 			throws MissiveException {
 		try {
 			return ClassUtil.constantFieldsAll(clazz, Tag.class).toArray(
 					new Tag[0]);
 		} catch (final Exception e) {
 			throw new MissiveException(e);
 		}
 	}
 	
 	/**
 	 * Collect top level tags from provided type.
 	 * 
 	 * @throws MissiveException
 	 */
 	public static Tag<?>[] collectTop(final Class<?> clazz)
 			throws MissiveException {
 		try {
 			return ClassUtil.constantFieldsTop(clazz, Tag.class).toArray(
 					new Tag[0]);
 		} catch (final Exception e) {
 			throw new MissiveException(e);
 		}
 	}
 	
 	/**
 	 * Returns the highest index assigned by this factory.
 	 * 
 	 * @return
 	 */
 	public static int maxIndex() {
 		return counter.get();
 	}
 	
 	/* ***** ***** Begin non-public static methods ***** ***** */
 
 	/**
 	 * 
 	 * @return
 	 */
 	protected static int nextIndex() {
 		Missive.incrementIndexRegistry();
 		return counter.getAndIncrement();
 	}
 	
 	/**
 	 * 
 	 * @param clazz
 	 * @return
 	 */
 	protected static int nameHash(final Class<?> clazz) {
 		return clazz.getName().hashCode();
 	}
 	
 	/**
 	 * 
 	 * @param clazz
 	 * @return
 	 */
 	protected static boolean isEnum(final Class<?> clazz) {
 		return clazz.isEnum();
 	}
 
 	/**
 	 * 
 	 * @param clazz
 	 * @return
 	 */
 	protected static boolean isList(final Class<?> clazz) {
 		if (clazz.isArray()) {
 			return true;
 		} else if (Collection.class.isAssignableFrom(clazz)) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 * 
 	 * @param clazz
 	 * @return
 	 */
 	protected static boolean isPrim(final Class<?> clazz) {
 		return primitives.contains(clazz);
 	}
 	
 	/**
 	 * 
 	 * @param clazz
 	 * @return
 	 */
 	protected static int mask(final Class<?> clazz) {
 		int mask = 0;
 		if (isEnum(clazz)) {
 			mask |= IS_ENUM;
 		}
 		if (isList(clazz)) {
 			mask |= IS_LIST;
 		}
 		if (isPrim(clazz)) {
 			mask |= IS_PRIM;
 		}
 		return mask;
 	}
 	
 	private static Object parsePrimitiveFromString(final Class<?> clazz,
 			final String value) {
 
 		if (clazz == Byte.class) {
 
 			return Byte.parseByte(value);
 
 		} else if (clazz == Short.class) {
 
 			return Short.parseShort(value);
 
 		} else if (clazz == Integer.class) {
 
 			return Integer.parseInt(value);
 
 		} else if (clazz == Long.class) {
 
 			return Long.parseLong(value);
 
 		} else if (clazz == Float.class) {
 
 			return Float.parseFloat(value);
 
 		} else if (clazz == Double.class) {
 
 			return Double.parseDouble(value);
 
 		} else if (clazz == Boolean.class) {
 
 			/** note : null for false */
 
 			if ("true".equalsIgnoreCase(value)) {
 				return true;
 			}
 			if ("y".equalsIgnoreCase(value)) {
 				return true;
 			}
 			if ("yes".equalsIgnoreCase(value)) {
 				return true;
 			}
 			return false;
 
 		} else if (clazz == Character.class) {
 
 			return new Character(value.charAt(0));
 
 		} else {
 
 			throw new MissiveException("parse failure " + clazz);
 
 		}
 
 	}
 	
 	private static <V> Tag<V> build(final String name, final Class<V> clazz, final int index,
 			final int mask) {
 		
 		return new Tag<V>() {
 
 			@Override
 			public String name() {
 				return name;
 			}
 
 			@Override
 			public int index() {
 				return index;
 			}
 
 			@SuppressWarnings({ "unchecked", "rawtypes" })
 			@Override
 			public V cast(Object o) {
 				try {
 
 					/* If the class is enum and object is string, attempt a valueOf */
 					if (isEnum() && o instanceof String) {
 
 						final Class<? extends Enum> klaz = (Class<? extends Enum>) clazz;
 						return (V) Enum.valueOf(klaz, (String) o);
 
 						/* If class is primitive and object is string, attempt to parse */
 					} else if (isPrim() && o instanceof String) {
 
 						return (V) parsePrimitiveFromString(clazz, (String) o);
 
 					} else {
 
 						try {
 							/* Attempt a normal cast */
 							return clazz.cast(o);
 						} catch (final ClassCastException e) {
 							/*
 							 * Last ditch, attempt to find constructor which accepts
 							 * object o
 							 */
 							return clazz.getConstructor(o.getClass()).newInstance(o);
 						}
 					}
 
 				} catch (final Throwable e) {
 
 					final String message = //
 					"Failed to cast object in tag " + name() + " " + o;
 					log.error(message, e);
 					throw new MissiveException(message, e);
 
 				}
 			}
 
 			@Override
 			public Class<V> classType() {
 				return clazz;
 			}
 
 			@Override
 			public boolean isEnum() {
 				return (mask & IS_ENUM) != 0;
 			}
 
 			@Override
 			public boolean isList() {
 				return (mask & IS_LIST) != 0;
 			}
 
 			@Override
 			public boolean isPrim() {
 				return (mask & IS_PRIM) != 0;
 			}
 
 			@Override
 			public int compareTo(final Tag<?> tag) {
 				return index - tag.index();
 			}
 			
 			@Override
 			public int hashCode() {
 				return index;
 			}
 			
 			@Override
 			public boolean equals(final Object that) {
 				
 				if(that == null) {
 					return false;
 				}
 				
 				if(!(that instanceof Tag<?>)) {
 					return false;
 				}
 				
 				return index == ((Tag<?>) that).index();
 				
 			}
 			
 		};
 		
 	}
 	
 	
 }
