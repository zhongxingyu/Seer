 package com.dvlcube.cli;
 
 import java.lang.reflect.Array;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 
 /**
  * Parses command line args (Unix style).
  * <p>
  * Will populate any class that extends it with the parsed command line args
  * values.
  * <p>
  * The class that extends it should not have primitive types.
  * <p>
  * The parsing is based on the "valueOf" method of the type, so custom classes
  * can be parsed out of the box.
  * 
  * @since 21/03/2013
  * @author wonka
  */
 public abstract class CLArgs {
 
 	public CLArgs process(String[] args) {
 		Queue<String> queue = new LinkedList<>(Arrays.asList(args));
 
 		Map<String, Field> fieldMap = new HashMap<>();
 		Field[] fields = this.getClass().getDeclaredFields();
 		for (Field field : fields) {
 			fieldMap.put(field.getName(), field);
 		}
 
 		while (!queue.isEmpty()) {
 			String option = queue.poll();
 			String fieldName = normalize(option);
 			Field field = fieldMap.get(fieldName);
 			if (field == null)
 				throw new IllegalArgumentException("Unrecognized option: "
 						+ option);
 
 			Class<?> fieldType = field.getType();
 
 			if (isSwitch(queue)) {
 				set(field, true);
 			} else {
 				if (fieldType.isArray()) {
 					List<Object> values = new ArrayList<>();
 					while (!isOption(queue.peek())) {
 						Object value = parse(queue.poll(),
 								fieldType.getComponentType());
 						values.add(value);
 					}
 					Object typedInstance = Array.newInstance(
 							fieldType.getComponentType(), values.size());
 					set(field, values.toArray((Object[]) typedInstance));
 				} else {
 					Object value = parse(queue.poll(), fieldType);
 					set(field, value);
 				}
 			}
 		}
 		return this;
 	}
 
 	private Object parse(String string, Class<?> fieldType) {
 		Object value = null;
 		try {
 			String type = fieldType.getSimpleName();
 			if ("String".equals(type))
 				return string;
 			value = fieldType.getMethod("valueOf", String.class).invoke(this,
 					string);
 		} catch (NoSuchMethodException | IllegalAccessException
 				| IllegalArgumentException | InvocationTargetException e) {
			log(e.getMessage());
 		}
 		return value;
 	}
 
 	private boolean isSwitch(Queue<String> queue) {
 		return queue.peek() == null || isOption(queue.peek());
 	}
 
 	private void set(Field field, Object value) {
 		try {
 			field.set(this, value);
 		} catch (IllegalArgumentException | IllegalAccessException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private boolean isOption(String peek) {
 		if (peek == null)
 			return false;
 		return peek.startsWith("-");
 	}
 
 	private String normalize(String arg) {
 		arg = arg.replace("--", "");
 		return camelCase(arg.split("\\-"));
 	}
 
 	private String camelCase(String[] split) {
 		StringBuilder builder = new StringBuilder(split[0]);
 		for (int i = 1; i < split.length; i++) {
 			split[i] = capitalize(split[i]);
 			builder.append(split[i]);
 		}
 		return builder.toString();
 	}
 
 	private String capitalize(String string) {
 		return Character.toUpperCase(string.charAt(0)) + string.substring(1);
 	}
 
	public static void log(Object object) {
		System.out.println(object);
	}

 	@Override
 	public String toString() {
 		StringBuilder builder = new StringBuilder();
 
 		Field[] declaredFields = getClass().getDeclaredFields();
 		builder.append(getClass().getSimpleName() + " {");
 		for (Field field : declaredFields) {
 			builder.append("\n\t" + field.getName() + ": ");
 			try {
 				if (field.getType().isArray()) {
 					builder.append(Arrays.toString((Object[]) field.get(this)));
 				} else {
 					builder.append(field.get(this));
 				}
 			} catch (IllegalArgumentException | IllegalAccessException e) {
 				builder.append("n/a");
 			}
 		}
 		builder.append("\n}");
 		return builder.toString();
 	}
 }
