 package monsters;
 
 import java.lang.reflect.Array;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import java.util.Random;
 
 public class RandomObject {
 	private static final Random		rand					= new Random();
 	private static final char		INDENT					= ' ';
 	private static volatile String	indentation				= "";
 	public static volatile boolean	DEBUG_OUTPUT			= true;
 	public static volatile boolean	DEBUG_RANDOBJ			= true;
 	public static volatile boolean	DEBUG_RANDOBJ_PRIMITIVE	= true;
 	public static volatile boolean	DEBUG_CONSTRS			= true;
 	public static volatile int		MAX_ARRAY_LENGTH		= 0b0000_0000__0000_1111;
 
 	public static Object randomizeObject(Object o) throws IllegalArgumentException, IllegalAccessException,
 			InstantiationException, InvocationTargetException {
 		for (Field f : o.getClass().getDeclaredFields())
 			randomizeField(o, f);
 		return o;
 	}
 
 	public static Object randomObject(Class<?> type) throws InstantiationException,
 			IllegalAccessException, IllegalArgumentException, InvocationTargetException {
 		try {
 			indentation += INDENT;
 			if (DEBUG_OUTPUT && DEBUG_RANDOBJ && (DEBUG_RANDOBJ_PRIMITIVE || !type.isPrimitive()))
 				System.out.println(indentation + "Retreiving random object of type " + type.toString());
 			if (type.isInterface() || type.isEnum() || type.isAnnotation())
 				throw new IllegalArgumentException("Type \"" + type.toString() + "\" is illegal!");
 			if (type.isArray()) {
 				int length = rand.nextInt(MAX_ARRAY_LENGTH);
 				Object a = Array.newInstance(type.getComponentType(), length);
 				for (int i = 0; i < length; i++)
 					Array.set(a, i, randomObject(type.getComponentType()));
 				return a;
 			}
 			if (type == Boolean.TYPE)
 				return rand.nextBoolean();
 			else if (type == Byte.TYPE)
 				return (byte) (rand.nextInt(256) - 128);
 			else if (type == Short.TYPE)
 				return (short) (rand.nextInt(65536) - 32768);
 			else if (type == Integer.TYPE)
 				return rand.nextInt();
 			else if (type == Long.TYPE)
 				return rand.nextLong();
 			else if (type == Float.TYPE)
 				return rand.nextFloat();
 			else if (type == Double.TYPE)
 				return rand.nextDouble();
 			else if (type == Character.TYPE)
 				return (char) rand.nextInt();
 			else {
 				boolean fail = true;
 				Constructor<?>[] constrsA = type.getDeclaredConstructors();
 				List<Constructor<?>> constrs = new ArrayList<>(Arrays.asList(constrsA));
 				Collections.shuffle(constrs);
 				Object o = null;
 				while (fail) {
 					if (DEBUG_OUTPUT && DEBUG_CONSTRS && constrs.size() == 0) {
 						System.out.println(indentation + "No more constructors for type " + type.toString());
 						throw new Error("No more contructors");
 					}
 					Constructor<?> constr = constrs.remove(constrs.size() - 1);
 					if (DEBUG_OUTPUT && DEBUG_CONSTRS)
 						System.out.println(indentation + "Trying constructor " + constr.toString());
 					Object[] args = new Object[constr.getParameterTypes().length];
 					for (int i = 0; i < args.length; i++)
 						args[i] = randomObject(constr.getParameterTypes()[i]);
 					constr.setAccessible(true);
 					try {
 						o = constr.newInstance(args);
 						if (DEBUG_OUTPUT && DEBUG_CONSTRS)
 							System.out.println(indentation + "Used constructor " + constr.toString());
 						fail = false;
 					}
 					catch (Throwable t) {
 						if (DEBUG_OUTPUT && DEBUG_CONSTRS)
 							System.out
 									.println(indentation + "Constructor " + constr.toString() + " failed due to a "
 											+ t.getClass().toString().substring("class ".length()) + ": "
 											+ (t.getMessage() == null ? "[No message]"
 													: t.getMessage()));
 					}
 				}
				return randomizeObject(o);
 			}
 		}
 		finally {
 			indentation = indentation.substring(0, indentation.length() - 1);
 		}
 	}
 
 	public static void randomizeField(Object o, Field f) throws IllegalArgumentException, IllegalAccessException,
 			InstantiationException, InvocationTargetException {
 		if ((f.getModifiers() & Modifier.STATIC) != 0)
 			return;
 		f.setAccessible(true);
 		Class<?> type = f.getType();
 		f.set(o, randomObject(type));
 	}
 
 	public static void main(String[] args) throws InstantiationException, IllegalAccessException,
 			IllegalArgumentException, InvocationTargetException {
		System.out.println(randomObject(StringBuilder.class));
 	}
 }
