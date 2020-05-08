 package org.pa.boundless.bsp;
 
 import static org.apache.commons.lang3.Validate.isTrue;
 import static org.apache.commons.lang3.Validate.notNull;
 
 import java.io.IOException;
 import java.lang.reflect.Array;
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 
 /**
  * A reader for types in the raw-package using reflection.
  * 
  * @author palador
  * 
  * @param <T>
  *            the chunk-type
  */
 public class ChunkReader<T> {
 
 	private static final HashSet<Class<?>> VALID_FIELD_TYPES = new HashSet<>(
 			Arrays.asList((Class<?>) int.class, char.class, float.class,
 					byte.class));
 
 	private final Class<T> type;
 	private final int chunkLength;
 	private final ArrayList<FieldLoader> fieldLoaders = new ArrayList<>();
 
 	/**
 	 * Creates a chunkreader for the specified type.
 	 * 
 	 * @param chunkType
 	 *            the type to load, must not be <code>null</code>
 	 * @throws IllegalArgumentException
 	 *             If <code>chunkType</code> is <code>null</code> or can't be
 	 *             read automatically.
 	 */
 	public ChunkReader(Class<T> chunkType) throws IllegalArgumentException {
 		type = notNull(chunkType);
 		try {
 			T dummy = chunkType.newInstance();
 			int chunkLen = 0;
 
 			// go through fields
 			for (Field field : chunkType.getFields()) {
 				int fieldMod = field.getModifiers();
 				Class<?> fieldType = field.getType();
 
 				// ignore static and non-public fields
 				if ((fieldMod & Modifier.STATIC) != 0
 						|| (fieldMod & Modifier.PUBLIC) == 0) {
 					continue;
 				}
 
 				if (fieldType.isArray()) {
 					isTrue((fieldMod & Modifier.FINAL) != 0,
 							"Array must be final: %s", field.getName());
 
 					// get and validate array-type and find out how many
 					// dimensions the array has
 					int dimensions = 0;
 					Class<?> arrayType = fieldType;
 					while (arrayType.isArray()) {
 						dimensions++;
 						arrayType = arrayType.getComponentType();
 					}
 					int[] dimensionsLengths = new int[dimensions];
 
 					isTrue(VALID_FIELD_TYPES.contains(arrayType),
 							"array-type of %s must not be %s", field.getName(),
 							arrayType.getName());
 
 					// get the current value to analyze
 					Object arrayValue = field.get(dummy);
 					notNull(arrayValue, "array-field must not be null: "
 							+ field.getName());
 
 					// find out how long each dimension is and increase chunkLen
					int totalElementCount = 1;
 					for (int dim = 0; dim < dimensions; dim++) {
 						dimensionsLengths[dim] = Array.getLength(arrayValue);
						totalElementCount *= dimensionsLengths[dim];
 
 						arrayValue = Array.get(arrayValue, 0);
 					}
					chunkLen += totalElementCount * sizeof(arrayType);
 
 					fieldLoaders.add(new ArrayFieldLoader(field, arrayType,
 							dimensionsLengths));
 				} else {
 					// not an array, field is simple
 					isTrue((fieldMod & Modifier.FINAL) == 0,
 							"Field must not be final: %s", field.getName());
 					isTrue(VALID_FIELD_TYPES.contains(fieldType),
 							"field-type of %s must not ne %s", field.getName(),
 							fieldType.getName());
 
 					chunkLen += sizeof(fieldType);
 
 					fieldLoaders.add(new SimpleFieldLoader(field));
 				}
 			}
 
 			chunkLength = chunkLen;
 		} catch (InstantiationException | IllegalAccessException e) {
 			throw new IllegalArgumentException(
 					"Type is not creatable or a field is not accessable: "
 							+ chunkType.getName(), e);
 		}
 	}
 
 	public int getChunkLength() {
 		return chunkLength;
 	}
 
 	public T loadChunk(ByteBuffer is) throws IOException {
 		T result;
 		try {
 			result = type.newInstance();
 		} catch (InstantiationException | IllegalAccessException e) {
 			throw new RuntimeException(e);
 		}
 		for (FieldLoader fieldLoader : fieldLoaders) {
 			fieldLoader.loadField(is, result);
 		}
 		return result;
 	}
 
 	/**
 	 * Returns the size of a primitive type in bytes. I know that chars are
 	 * actually 2 bytes in java, but not in bsp-files.
 	 * 
 	 * @param type
 	 * @return
 	 */
 	private static int sizeof(Class<?> type) {
 		if (type == char.class || type == byte.class) {
 			return 1;
 		} else {
 			return 4;
 		}
 	}
 
 	/**
 	 * Loads a value from an InputStream.
 	 * 
 	 * @param is
 	 * @param fieldType
 	 * @return
 	 * @throws IOException
 	 */
 	private static Object loadValue(ByteBuffer buf, Class<?> fieldType)
 			throws IOException {
 		Object value = null;
 		if (fieldType == byte.class) {
 			value = buf.get();
 		} else if (fieldType == char.class) {
 			value = (char) buf.get();
 		} else if (fieldType == int.class) {
 			value = buf.getInt();
 		} else if (fieldType == float.class) {
 			value = buf.getFloat();
 		}
 
 		return value;
 	}
 
 	private static interface FieldLoader {
 		void loadField(ByteBuffer buf, Object obj) throws IOException;
 	}
 
 	private static class SimpleFieldLoader implements FieldLoader {
 
 		private final Field field;
 
 		SimpleFieldLoader(Field field) {
 			this.field = field;
 		}
 
 		@Override
 		public void loadField(ByteBuffer buf, Object obj) throws IOException {
 			try {
 				field.set(obj, loadValue(buf, field.getType()));
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException(e);
 			}
 		}
 	}
 
 	private static class ArrayFieldLoader implements FieldLoader {
 		private final Field field;
 		private final Class<?> arrayType;
 		private final int[] dimensionLengths;
 		private final int[] blockLengths;
 
 		ArrayFieldLoader(Field field, Class<?> arrayType, int[] dimensionLengths) {
 			this.field = field;
 			this.arrayType = arrayType;
 			this.dimensionLengths = dimensionLengths;
 			blockLengths = new int[dimensionLengths.length];
 			for (int dim = dimensionLengths.length - 1; dim >= 0; dim--) {
 				blockLengths[dim] = dimensionLengths[dim];
 				if (dim < dimensionLengths.length - 1) {
 					blockLengths[dim] *= blockLengths[dim + 1];
 				}
 			}
 		}
 
 		@Override
 		public void loadField(ByteBuffer buf, Object obj) throws IOException {
 			Object array;
 			try {
 				array = field.get(obj);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException(e);
 			}
 
 			int totalElements = 1;
 			for (int dim = 0; dim < dimensionLengths.length; dim++) {
 				totalElements *= dimensionLengths[dim];
 			}
 
 			for (int element = 0; element < totalElements; element++) {
 				Object primitiveArray = array;
 				for (int dim = 0; dim < dimensionLengths.length - 1; dim++) {
 					// fancy: convert element index to index of the current
 					// dimension
 					int index = (element / blockLengths[dim + 1])
 							% dimensionLengths[dim];
 					primitiveArray = Array.get(primitiveArray, index);
 				}
 
 				Object elementValue = loadValue(buf, arrayType);
 				Array.set(primitiveArray, element
 						% dimensionLengths[dimensionLengths.length - 1],
 						elementValue);
 			}
 		}
 	}
 
 }
