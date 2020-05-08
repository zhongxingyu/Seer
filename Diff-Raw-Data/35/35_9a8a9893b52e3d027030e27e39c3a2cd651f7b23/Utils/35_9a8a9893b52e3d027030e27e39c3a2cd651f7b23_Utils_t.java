 package org.goobs.util;
 
 import org.goobs.exec.Log;
 
 import java.io.*;
 import java.lang.reflect.*;
 import java.text.DecimalFormat;
 import java.util.*;
 
 public class Utils {
 
 
 	public static final char[] ALPHABET_UPPER = { 'A', 'B', 'C', 'D', 'E', 'F',
 		'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S',
 		'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };
 	public static final char[] ALPHABET_LOWER = { 'a', 'b', 'c', 'd', 'e', 'f',
 		'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's',
 		't', 'u', 'v', 'w', 'x', 'y', 'z' };
 
 	public static final DecimalFormat df = new DecimalFormat("0.000");
 	
 	public static final void sleep(long mili){
 		sleepMili(mili);
 	}
 	
 	public static final void sleepMili(long mili) {
 		try {
 			Thread.sleep(mili);
 		} catch (InterruptedException e) {
 		}
 	}
 
 	public static final void sleepSeconds(long sec) {
 		try {
 			Thread.sleep(1000 * sec);
 		} catch (InterruptedException e) {
 		}
 	}
 
 	public static final void sleepMinutes(long minutes) {
 		try {
 			Thread.sleep(1000 * 60 * minutes);
 		} catch (InterruptedException e) {
 		}
 	}
 
 	public static final double distance(double a, double b) {
 		return Math.sqrt(a * a + b * b);
 	}
 
 	public static final int compareFilenames(String a, String b) {
 		// --Overhead
 		char[] first = a.toCharArray();
 		char[] second = b.toCharArray();
 		// --Check for equality
 		for (int i = 0; i < Math.max(first.length, second.length); i++) {
 			// (get the characters at the relevant position)
 			char c1 = ' ';
 			char c2 = ' ';
 			if (i < first.length) {
 				c1 = first[i];
 			}
 			if (i < second.length) {
 				c2 = second[i];
 			}
 			// (return if applicable)
 			if (c1 < c2) {
 				return -1;
 			}
 			if (c1 > c2) {
 				return 1;
 			}
 			// (otherwise continue)
 			continue;
 		}
 
 		// --They're equal
 		return 0;
 	}
 
 
 	/*
 	 * --------------------
 	 * MIN and MAX
 	 * --------------------
 	 */
 
 	//--INT MIN
 	public static final <E> E argmin(E[] elems, int[] scores) { return argmin(elems, scores, Integer.MIN_VALUE); }
 	public static final <E> E argmin(E[] elems, int[] scores, int atLeast){
 		int argmin = argmin(scores, atLeast);
 		return argmin >= 0 ? elems[argmin] : null;
 	}
 	public static final int argmin(int[] scores){ return argmin(scores, Integer.MIN_VALUE); }
 	public static final int argmin(int[] scores, int atLeast){
 		int min = Integer.MAX_VALUE;
 		int argmin = -1;
 		for(int i=0; i<scores.length; i++){
 			if(scores[i] < min && scores[i] >= atLeast){
 				min = scores[i];
 				argmin = i;
 			}
 		}
 		return argmin;
 	}
 	public static final int min(int[] scores){ return min(scores, Integer.MIN_VALUE); }
 	public static final int min(int[] scores, int atLeast){
 		int min = Integer.MAX_VALUE;
 		for(int i=0; i<scores.length; i++){
 			if(scores[i] < min && scores[i] >= atLeast){
 				min = scores[i];
 			}
 		}
 		return min;
 	}
 
 	//--INT MAX
 	public static final <E> E argmax(E[] elems, int[] scores) { return argmax(elems, scores, Integer.MAX_VALUE); }
 	public static final <E> E argmax(E[] elems, int[] scores, int atMost){
 		int argmax = argmax(scores, atMost);
 		return argmax >= 0 ? elems[argmax] : null;
 	}
 	public static final int argmax(int[] scores){ return argmax(scores, Integer.MAX_VALUE); }
 	public static final int argmax(int[] scores, int atMost){
 		int max = Integer.MIN_VALUE;
 		int argmax = -1;
 		for(int i=0; i<scores.length; i++){
 			if(scores[i] > max && scores[i] <= atMost){
 				max = scores[i];
 				argmax = i;
 			}
 		}
 		return argmax;
 	}
 	public static final int max(int[] scores){ return max(scores, Integer.MAX_VALUE); }
 	public static final int max(int[] scores, int atMost){
 		int max = Integer.MIN_VALUE;
 		for(int i=0; i<scores.length; i++){
 			if(scores[i] > max && scores[i] <= atMost){
 				max = scores[i];
 			}
 		}
 		return max;
 	}
 
 
 	//--DOUBLE MIN
 	public static final <E> E argmin(E[] elems, double[] scores) { return argmin(elems, scores, Double.NEGATIVE_INFINITY); }
 	public static final <E> E argmin(E[] elems, double[] scores, double atLeast){
 		int argmin = argmin(scores, atLeast);
 		return argmin >= 0 ? elems[argmin] : null;
 	}
 	public static final int argmin(double[] scores){ return argmin(scores, Double.NEGATIVE_INFINITY); }
 	public static final int argmin(double[] scores, double atLeast){
 		double min = Double.POSITIVE_INFINITY;
 		int argmin = -1;
 		for(int i=0; i<scores.length; i++){
 			if(scores[i] < min && scores[i] >= atLeast){
 				min = scores[i];
 				argmin = i;
 			}
 		}
 		return argmin;
 	}
 	public static final double min(double[] scores){ return min(scores, Double.NEGATIVE_INFINITY); }
 	public static final double min(double[] scores, double atLeast){
 		double min = Double.POSITIVE_INFINITY;
 		for(int i=0; i<scores.length; i++){
 			if(scores[i] < min && scores[i] >= atLeast){
 				min = scores[i];
 			}
 		}
 		return min;
 	}
 
 	//--DOUBLE MAX
 	public static final <E> E argmax(E[] elems, double[] scores) { return argmax(elems, scores, Double.POSITIVE_INFINITY); }
 	public static final <E> E argmax(E[] elems, double[] scores, double atMost){
 		int argmax = argmax(scores, atMost);
 		return argmax >= 0 ? elems[argmax] : null;
 	}
 	public static final int argmax(double[] scores){ return argmax(scores, Double.POSITIVE_INFINITY); }
 	public static final int argmax(double[] scores, double atMost){
 		double max = Double.NEGATIVE_INFINITY;
 		int argmax = -1;
 		for(int i=0; i<scores.length; i++){
 			if(scores[i] > max && scores[i] <= atMost){
 				max = scores[i];
 				argmax = i;
 			}
 		}
 		return argmax;
 	}
 	public static final double max(double[] scores){ return max(scores, Double.POSITIVE_INFINITY); }
 	public static final double max(double[] scores, double atMost){
 		double max = Double.NEGATIVE_INFINITY;
 		for(int i=0; i<scores.length; i++){
 			if(scores[i] > max && scores[i] <= atMost){
 				max = scores[i];
 			}
 		}
 		return max;
 	}
 
 
 	public static final int[] range(int stop){ return range(0, stop); }
 	public static final int[] range(int start, int stop){
 		int[] rtn = new int[stop-start];
 		for(int i=start; i<stop; i++){
 			rtn[i-start] = i;
 		}
 		return rtn;
 	}
 
 	public static final <E> boolean contains(E[] array, E elem){
 		for(E cand : array){
 			if(cand.equals(elem)){
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public static final <A,B> Class<?> commonSuperclass(Class<A> a, Class<B> b){
 		if(a.equals(b)){ return a; }
 		if(a.isAssignableFrom(b)){ return a; }
 		if(b.isAssignableFrom(a)){ return b; }
 		return Object.class;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public static final <E> E[] concat(E[] a, E[] b){
 		Class<?> clazz = commonSuperclass(a.getClass().getComponentType(), b.getClass().getComponentType());
 		E[] rtn = (E[]) Array.newInstance(clazz, a.length+b.length);
 		System.arraycopy(a, 0, rtn, 0, a.length);
 		System.arraycopy(b, 0, rtn, a.length, b.length);
 		return rtn;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public static final <E> E[] concat(E[] array, E elem){
 		Class<?> clazz = commonSuperclass(array.getClass().getComponentType(), elem.getClass());
 		E[] rtn = (E[]) Array.newInstance(clazz, array.length+1);
 		System.arraycopy(array, 0, rtn, 0, array.length);
 		rtn[rtn.length-1] = elem;
 		return rtn;
 	}
 
 	public static final double[] range(double stop){ return range(0.0, stop); }
 	public static final double[] range(double start, double stop){
 		int a = (int) start;
 		int b = (int) stop;
 		double[] rtn = new double[b - a];
 		for(int i=a; i<b; i++){
 			rtn[i-a] = (double) i;
 		}
 		return rtn;
 	}
 	
 	public static final <E> E randomElement(Collection<E> elements){
 		int index = new Random().nextInt( elements.size() );
 		int i = 0;
 		for(E e : elements){
 			if(i == index){
 				return e;
 			}
 			i += 1;
 		}
 		throw Log.internal("randomElement failed!");
 	}
 
 	public static final String encodeArray(Object...objs){
 		StringBuilder b = new StringBuilder();
 		b.append("(");
 		for(Object o : objs){
 			b.append(o.toString());
 		}
 		b.append(")");
 		return b.toString();
 	}
 	
 	public static final String[] decodeArray(String encoded){
 		char[] chars = encoded.trim().toCharArray();
 
 		//--Parse the String
 		//(state)
 		char quoteCloseChar = (char) 0;
 		List<StringBuilder> terms = new LinkedList<StringBuilder>();
 		StringBuilder current = new StringBuilder();
 		//(start/stop overhead)
 		int start = 0; int end = chars.length;
 		if(chars[0] == '('){ start += 1; end -= 1; if(chars[end] != ')') throw new IllegalArgumentException("Unclosed paren in encoded array: " + encoded); }
 		if(chars[0] == '['){ start += 1; end -= 1; if(chars[end] != ']') throw new IllegalArgumentException("Unclosed bracket in encoded array: " + encoded); }
 		//(finite state automata)
 		for(int i=start; i<end; i++){
 			if(chars[i] == Decodable.ESCAPE_CHAR){
 				//(case: escaped character)
 				if(i == chars.length - 1) throw new IllegalArgumentException("Last character of encoded pair is escape character: " + encoded);
 				current.append(chars[i+1]);
 				i += 1;
 			} else if(quoteCloseChar != 0){
 				//(case: in quotes)
 				if(chars[i] == quoteCloseChar){
 					quoteCloseChar = (char) 0;
 				}else{
 					current.append(chars[i]);
 				}
 			}else{
 				//(case: normal)
 				if(chars[i] == '"'){ quoteCloseChar = '"';
 				} else if(chars[i] == '\''){ quoteCloseChar = '\'';
 				} else if(chars[i] == ','){
 					//break
 					terms.add(current);
 					current = new StringBuilder();
 				}else{
 					current.append(chars[i]);
 				}
 			}
 		}
 		
 		//--Return
 		if(current.length() > 0) terms.add(current);
 		String[] rtn = new String[terms.size()];
 		int i=0;
 		for(StringBuilder b : terms){
 			rtn[i] = b.toString().trim();
 			i += 1;
 		}
 		return rtn;
 	}
 
 	@SuppressWarnings("unchecked")
 	public static final <E> E[] arrayToPrimitive(Object[] value){
 		Class <?> type = value.getClass().getComponentType();
 		if(type.isAssignableFrom(Boolean.class) || type.isAssignableFrom(boolean.class)){
 			//(case: boolean)
 			Boolean[] rtn = new Boolean[value.length];
 			for(int i=0; i<rtn.length; i++){ rtn[i] = (Boolean) value[i]; }
 			return (E[]) rtn;
 		}else if(type.isAssignableFrom(Integer.class) || type.isAssignableFrom(int.class)){
 			//(case: integer)
 			Integer[] rtn = new Integer[value.length];
 			for(int i=0; i<rtn.length; i++){ rtn[i] = (Integer) value[i]; }
 			return (E[]) rtn;
 		}else if(type.isAssignableFrom(Long.class) || type.isAssignableFrom(long.class)){
 			//(case: long)
 			Long[] rtn = new Long[value.length];
 			for(int i=0; i<rtn.length; i++){ rtn[i] = (Long) value[i]; }
 			return (E[]) rtn;
 		}else if(type.isAssignableFrom(Float.class) || type.isAssignableFrom(float.class)){
 			//(case: float)
 			Float[] rtn = new Float[value.length];
 			for(int i=0; i<rtn.length; i++){ rtn[i] = (Float) value[i]; }
 			return (E[]) rtn;
 		}else if(type.isAssignableFrom(Double.class) || type.isAssignableFrom(double.class)){
 			//(case: double)
 			Double[] rtn = new Double[value.length];
 			for(int i=0; i<rtn.length; i++){ rtn[i] = (Double) value[i]; }
 			return (E[]) rtn;
 		}else if(type.isAssignableFrom(Short.class) || type.isAssignableFrom(short.class)){
 			//(case: short)
 			Short[] rtn = new Short[value.length];
 			for(int i=0; i<rtn.length; i++){ rtn[i] = (Short) value[i]; }
 			return (E[]) rtn;
 		}else if(type.isAssignableFrom(Byte.class) || type.isAssignableFrom(byte.class)){
 			//(case: byte)
 			Byte[] rtn = new Byte[value.length];
 			for(int i=0; i<rtn.length; i++){ rtn[i] = (Byte) value[i]; }
 			return (E[]) rtn;
 		}else{
 			throw new IllegalArgumentException("Array type is not a primitive: " + type);
 		}
 	}
 	
 	public static Class <?> type2class(Type type){
 		if(type instanceof Class <?>){
 			return (Class <?>) type;	//base case
 		}else if(type instanceof ParameterizedType){
 			return type2class( ((ParameterizedType) type).getRawType() );
 		}else if(type instanceof TypeVariable<?>){
 			return type2class( ((TypeVariable<?>) type).getBounds()[0] );
 		}else if(type instanceof WildcardType){
 			return type2class( ((WildcardType) type).getUpperBounds()[0] );
 		}else{
 			throw new IllegalArgumentException("Cannot convert type to class: " + type);
 		}
 	}
 
 	public static Class<?> primitive2wrapper(Class<?> primitive){
 		if(primitive == boolean.class){ return Boolean.class; }
 		else if(primitive == byte.class){ return Byte.class; }
 		else if(primitive == short.class){ return Short.class; }
 		else if(primitive == char.class){ return Character.class; }
 		else if(primitive == int.class){ return Integer.class; }
 		else if(primitive == long.class){ return Long.class; }
 		else if(primitive == float.class){ return Float.class; }
 		else if(primitive == double.class){ return Double.class; }
 		else { throw new IllegalArgumentException("Class is not a primitive: " + primitive); }
 	}
 
 	public static void arraycopy(Object[] src, int srcPos, Object dst, int dstPos, Class<?> dstClass, int length){
 		if(dstClass.isPrimitive()){
 			if(dstClass == boolean.class){
 				boolean[] primitiveSrc = new boolean[src.length];
 				for(int i=0; i<primitiveSrc.length; i++){ primitiveSrc[i] = (Boolean) src[i]; }
 				System.arraycopy(primitiveSrc,srcPos,dst,dstPos,length);
 			} else if(dstClass == byte.class){
 				byte[] primitiveSrc = new byte[src.length];
 				for(int i=0; i<primitiveSrc.length; i++){ primitiveSrc[i] = (Byte) src[i]; }
 				System.arraycopy(primitiveSrc,srcPos,dst,dstPos,length);
 			} else if(dstClass == short.class){
 				short[] primitiveSrc = new short[src.length];
 				for(int i=0; i<primitiveSrc.length; i++){ primitiveSrc[i] = (Short) src[i]; }
 				System.arraycopy(primitiveSrc,srcPos,dst,dstPos,length);
 			} else if(dstClass == char.class){
 				char[] primitiveSrc = new char[src.length];
 				for(int i=0; i<primitiveSrc.length; i++){ primitiveSrc[i] = (Character) src[i]; }
 				System.arraycopy(primitiveSrc,srcPos,dst,dstPos,length);
 			} else if(dstClass == int.class){
 				int[] primitiveSrc = new int[src.length];
 				for(int i=0; i<primitiveSrc.length; i++){ primitiveSrc[i] = (Integer) src[i]; }
 				System.arraycopy(primitiveSrc,srcPos,dst,dstPos,length);
 				} else if(dstClass == long.class){
 				long[] primitiveSrc = new long[src.length];
 				for(int i=0; i<primitiveSrc.length; i++){ primitiveSrc[i] = (Long) src[i]; }
 				System.arraycopy(primitiveSrc,srcPos,dst,dstPos,length);
 			} else if(dstClass == float.class){
 				float[] primitiveSrc = new float[src.length];
 				for(int i=0; i<primitiveSrc.length; i++){ primitiveSrc[i] = (Float) src[i]; }
 				System.arraycopy(primitiveSrc,srcPos,dst,dstPos,length);
 			} else if(dstClass == double.class){
 				double[] primitiveSrc = new double[src.length];
 				for(int i=0; i<primitiveSrc.length; i++){ primitiveSrc[i] = (Double) src[i]; }
 				System.arraycopy(primitiveSrc,srcPos,dst,dstPos,length);
 			}
 		} else {
 			System.arraycopy(src,srcPos,dst,dstPos,length);
 		}
 	}
 	
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	public static final <E> E cast(String value, Type type){
 		//--Get Type
 		Class <?> clazz = null;
 		Type[] params = null;
 		if(type instanceof Class){
 			clazz = (Class <?>) type;
 		}else if(type instanceof ParameterizedType){
 			ParameterizedType pt = (ParameterizedType) type;
 			params = pt.getActualTypeArguments();
 			clazz = (Class <?>) pt.getRawType();
 		}else{
 			clazz = type2class(type);
 			throw new IllegalArgumentException("Cannot cast to type (unhandled type): " + type);
 		}
 		//--Cast
 		if(String.class.isAssignableFrom(clazz)){
 			// (case: String)
 			return (E) value;
 		}else if(Boolean.class.isAssignableFrom(clazz) || boolean.class.isAssignableFrom(clazz)){
 			//(case: boolean)
 			if(value.equals("1")){ return (E) new Boolean(true); }
 			return (E) new Boolean( Boolean.parseBoolean(value) );
 		}else if(Integer.class.isAssignableFrom(clazz) || int.class.isAssignableFrom(clazz)){
 			//(case: integer)
 			return (E) new Integer(Integer.parseInt(value));
 		}else if(Long.class.isAssignableFrom(clazz) || long.class.isAssignableFrom(clazz)){
 			//(case: long)
 			return (E) new Long(Long.parseLong(value));
 		}else if(Float.class.isAssignableFrom(clazz) || float.class.isAssignableFrom(clazz)){
 			//(case: float)
 			if(value == null){ return (E) new Float(Float.NaN); }
 			return (E) new Float(Float.parseFloat(value));
 		}else if(Double.class.isAssignableFrom(clazz) || double.class.isAssignableFrom(clazz)){
 			//(case: double)
 			if(value == null){ return (E) new Double(Double.NaN); }
 			return (E) new Double(Double.parseDouble(value));
 		}else if(Short.class.isAssignableFrom(clazz) || short.class.isAssignableFrom(clazz)){
 			//(case: short)
 			return (E) new Short(Short.parseShort(value));
 		}else if(Byte.class.isAssignableFrom(clazz) || byte.class.isAssignableFrom(clazz)){
 			//(case: byte)
 			return (E) new Byte(Byte.parseByte(value));
 		}else if(Character.class.isAssignableFrom(clazz) || char.class.isAssignableFrom(clazz)){
 			//(case: char)
 			return (E) new Character((char) Integer.parseInt(value));
 		}else if(Decodable.class.isAssignableFrom(clazz)){
 			//(case: decodable)
 			MetaClass mc = MetaClass.create(clazz);
 			if(!mc.checkConstructor( /*no parameters*/ )){
 				throw new IllegalArgumentException("Decodable must have a parameter-less constructor to be cast from String");
 			}
 			Decodable rtn = mc.createInstance();
			return (E) rtn.decode(value, params);
 		}else if(java.util.Date.class.isAssignableFrom(clazz)){
 			//(case: date)
 			try {
 				return (E) new Date(Long.parseLong(value));
 			} catch (NumberFormatException e) {
 				return null;
 			}
 		} else if(java.util.Calendar.class.isAssignableFrom(clazz)){
 			//(case: date)
 			try {
 				Date d = new Date(Long.parseLong(value));
 				GregorianCalendar cal = new GregorianCalendar();
 				cal.setTime(d);
 				return (E) cal;
 			} catch (NumberFormatException e) {
 				return null;
 			}
         } else if(File.class.isAssignableFrom(clazz)){
             return (E) new File(value);
         } else if(Class.class.isAssignableFrom(clazz)){
             try {
                 return (E) Class.forName(value);
             } catch (ClassNotFoundException e) {
                 return null;
             }
         } else if(clazz.isArray()){
 			if(value == null){ return null; }
 			Class <?> subType = clazz.getComponentType();
 			// (case: array)
 			String[] strings = Utils.decodeArray(value);
 			Object[] array = (Object[]) Array.newInstance(clazz.getComponentType(), strings.length);
 			for(int i=0; i<strings.length; i++){
 				array[i] = cast(strings[i], subType);
 			}
 			return (E) array;
 		} else if(clazz.isEnum()){
 			// (case: enumeration)
 			Class c = (Class) clazz;
 			if(value == null){ return null; }
 			try {
 				return (E) Enum.valueOf(c, value);
 			} catch (Exception e){
 				try {
 					return (E) Enum.valueOf(c, value.toLowerCase());
 				} catch (Exception e2){
 					try {
 						return (E) Enum.valueOf(c, value.toUpperCase());
 					} catch (Exception e3){
 						return (E) Enum.valueOf(c, 
 								(Character.isUpperCase(value.charAt(0)) ? Character.toLowerCase(value.charAt(0)) : Character.toUpperCase(value.charAt(0))) +
 								value.substring(1));
 					}
 				}
 			}
 		} else {
 			return null;
 		}
 	}
 
 	public static final void fileCopy(InputStream in, OutputStream out){
 		try{
 			byte[] buf = new byte[1024];
 			int len;
 			while ((len = in.read(buf)) > 0){
 				out.write(buf, 0, len);
 			}
 			in.close();
 			out.close();
 		} catch (Exception e){
 			throw new RuntimeException(e);
 		}
 	}
 	public static final void fileCopy(InputStream in, File dst){
 		try{ fileCopy(in, new FileOutputStream(dst)); }
 		catch(FileNotFoundException e){ throw new RuntimeException(e); }
 	}
 
 	public static final void fileCopy(File src, File dst){
 		try{ fileCopy(new FileInputStream(src), dst); }
 		catch(FileNotFoundException e){ throw new RuntimeException(e); }
 	}
 
 	public static final java.io.FileWriter writer(String path){
 		try{
 			return new java.io.FileWriter(new java.io.File(path),false);
 		}catch(IOException e){
 			return null;
 		}
 	}
 
 	public static final <E> int indexOf(E[] array, E[] subArray){
 		int cand = -1;
 		int matchLen = 0;
 		for(int i=0; i<array.length; i++){
 			if(subArray[matchLen].equals(array[i])){
 				if(matchLen == 0) cand = i;
 				matchLen += 1;
 			}else{
 				cand = -1;
 				matchLen = 0;
 			}
 			if(matchLen == subArray.length) return cand;
 		}
 		return -1;
 	}
 	
 	public static final <E, F extends E> int indexOf(E[] array, F term){
 		for(int i=0; i<array.length; i++){
 			if(array[i].equals(term)){
 				return i;
 			}
 		}
 		return -1;
 	}
 
 	public static final <E> String join(String glue, E... array){
 		int k = array.length;
 		if(k == 0){ return ""; }
 		StringBuilder out = new StringBuilder();
 		out.append(array[0].toString());
 		for(int x=1; x < k; x++){
 			out.append(glue).append(array[x] == null ? "null" : array[x].toString());
 		}
 		return out.toString();
 	}
 
 	public static final <E> String join(E[] array, String glue){
 		return join(glue,array);
 	}
 	
 	public static final byte[] obj2bytes(Serializable obj){
 		try {
 			ByteArrayOutputStream baos = new ByteArrayOutputStream();
 			ObjectOutputStream oout = new ObjectOutputStream(baos);
 			oout.writeObject(obj);
 			oout.close();
 			return baos.toByteArray();
 		} catch (IOException e) {
 			throw Log.fail("Object cannot be cast to bytes: obj: " + obj + " cause: " + e.getMessage());
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	public static final <E> E bytes2obj(byte[] bytes){
 		try {
 			ObjectInputStream objectIn = new ObjectInputStream( new ByteArrayInputStream(bytes) );
 			Object obj = objectIn.readObject(); // Contains the object
 			return (E) obj;
 		} catch (IOException e) {
 			throw Log.fail("Object cannot be inferred from bytes: cause: " + e.getMessage());
 		} catch (ClassNotFoundException e) {
 			throw Log.fail("Cannot find class of serialized object: cause: " + e.getMessage());
 		} catch (ClassCastException e) {
 			throw Log.fail("Cannot cast object to return type: cause: " + e.getMessage());
 		}
 	}
 	
 	private static final class FileLineIterator implements Iterator<String>{
 		private BufferedReader reader;
 		private String line = null;
 		private FileLineIterator(BufferedReader reader){
 			this.reader = reader;
 		}
 		@Override
 		public boolean hasNext() {
 			if(line == null){
 				try {
 					line = reader.readLine();
 				} catch (IOException e) {
 					throw Log.fail(e);
 				}
 				return line != null;
 			}else{
 				return true;
 			}
 		}
 		@Override
 		public String next() {
 			if(!hasNext()) throw new NoSuchElementException();
 			String rtn = line;
 			line = null;
 			return rtn;
 		}
 		@Override
 		public void remove() {
 			throw new NoSuchMethodError();
 		}
 	}
 	
 	public static final Iterator <String> readFile(String path){
 		File f = new File(path);
 		try {
 			BufferedReader reader = new BufferedReader(new FileReader(f));
 			return new FileLineIterator(reader);
 		} catch (FileNotFoundException e) {
 			throw Log.fail(e);
 		}
 	}
 	
 	private static final void bubblesort(int[] terms, int start, int stop, Object[][] others){
 		for(int bubbleStop=stop-1; bubbleStop>start; bubbleStop--){
 			boolean didSwap = false;
 			for(int bubbleI=start; bubbleI<bubbleStop; bubbleI++){
 				if(terms[bubbleI] > terms[bubbleI+1]){
 					//(swap this)
 					int tmp = terms[bubbleI];
 					terms[bubbleI] = terms[bubbleI+1];
 					terms[bubbleI+1] = tmp;
 					//(swap others)
 					if(others != null){
 						for(Object[] other : others){
 							Object t = other[bubbleI];
 							other[bubbleI] = other[bubbleI+1];
 							other[bubbleI+1] = t;
 						}
 					}
 					didSwap = true;
 				}
 			}
 			if(!didSwap){ return; }
 		}
 	}
 	
 	private static final void quicksort(int[] terms, int start, int stop, Object[][] others){
 		//(base case)
 		if(stop - start < 5){ 
 			bubblesort(terms, start, stop, others);
 			return; 
 		}
 		//(guess a pivot)
 		Object[] guessObj = null;
 		if(others != null){ guessObj = new Object[others.length]; }
 		int guessA = terms[start];
 		int guessB = terms[stop-1];
 		int guessC = terms[start+(stop-start)/2];
 		int guessX = guessA > guessB ? guessA : guessB;
 		int guessI = guessA > guessB ? start : stop-1;
 		int guess = guessX < guessC ? guessX : guessC;
 		guessI = guessX < guessC ? guessI : (start+(stop-start)/2);
 		if(others != null){
 			for(int i=0; i<others.length; i++){
 				guessObj[i] = others[i][guessI];
 			}
 		}
 		//(move pivot to end)
 		int tmp = terms[stop-1];
 		terms[stop-1] = guess;
 		terms[guessI] = tmp;
 		if(others != null){
 			for(int i=0; i<others.length; i++){
 				Object t = others[i][stop-1];
 				others[i][stop-1] = guessObj[i];
 				others[i][guessI] = t;
 			}
 		}
 		//(swaps)
 		int frontPointer = start;
 		int backPointer = stop-2;
 		while(frontPointer < backPointer){
 			if(terms[frontPointer] < guess){
 				frontPointer += 1;
 			}else{
 				while(terms[backPointer] >= guess && frontPointer < backPointer){ 
 					backPointer -= 1;
 				}
 				if(frontPointer < backPointer){
 					//(swap this)
 					tmp = terms[frontPointer];
 					terms[frontPointer] = terms[backPointer];
 					terms[backPointer] = tmp;
 					//(swap others)
 					if(others != null){
 						for(Object[] other : others){
 							Object t = other[frontPointer];
 							other[frontPointer] = other[backPointer];
 							other[backPointer] = t;
 						}
 					}
 				}
 			}
 		}
 		//(move pivot to place)
 		tmp = terms[frontPointer];
 		terms[frontPointer] = guess;
 		terms[stop-1] = tmp;
 		if(others != null){
 			for(int i=0; i<others.length; i++){
 				Object t = others[i][frontPointer];
 				others[i][frontPointer] = guessObj[i];
 				others[i][stop-1] = t;
 			}
 		}
 		//(recursive case)
 		quicksort(terms,start,frontPointer, others);
 		quicksort(terms,frontPointer+1,stop, others);
 	}
 
 	public static final void sort(int[] indices, Object[]... others){
 		quicksort(indices,0,indices.length, others);
 	}
 	
 
 }
