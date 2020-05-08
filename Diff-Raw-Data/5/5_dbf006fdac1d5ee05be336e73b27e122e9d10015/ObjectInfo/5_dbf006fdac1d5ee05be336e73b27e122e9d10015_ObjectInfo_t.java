 import java.lang.reflect.Array;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Member;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Observable;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 
 public class ObjectInfo extends Observable{
 	public Object obj;
 	public Object ret;
 	public Method method;
 	public Field field;
 	public Object[] array;
 	public List<Object> stockObjs = new ArrayList<Object>();
 	public Constructor constructor;
 	public String exception;
 	public List<Field> fields = new ArrayList<Field>();
 	public List<Method> methods = new ArrayList<Method>();
 	public List<Constructor> constructors = new ArrayList<Constructor>();
 	public List<String> fieldNames = new ArrayList<String>();
 	public List<String> fieldTypes = new ArrayList<String>();
 	public List<String> methodNames = new ArrayList<String>();
 	public List<String> methodTypes = new ArrayList<String>();
 	public List<String> fieldVals = new ArrayList<String>();
 	public List<String> parameterTypes = new ArrayList<String>();
 	public List<String> conNames = new ArrayList<String>();
 	public List<String> conParameterTypes = new ArrayList<String>();
 	
 	Boolean isError = false;
 	
 	public void saveObject(Object o) {
 		obj = o;
 		stockObjs.add(obj);
 		reset();
 		saveFields(getAllFieldsFromClass(o.getClass()));
 		saveMethods(getAllMethodsFromClass(o.getClass()));
 
 		isError = false;
 		setChanged();
 		notifyObservers();
 	}
 	
 	public void setStockedObjectToWorkSpace() {
 		reset();
 		saveFields(getAllFieldsFromClass(obj.getClass()));
 		saveMethods(getAllMethodsFromClass(obj.getClass()));
 
 		isError = false;
 		setChanged();
 		notifyObservers();
 	}
 	
 	public void setArrayObjectToWorkSpace(Object obj) {
 		reset();
 		saveFields(getAllFieldsFromClass(obj.getClass()));
 		saveMethods(getAllMethodsFromClass(obj.getClass()));
 
 		isError = false;
 		setChanged();
 		notifyObservers();
 	}
 	
 	public void setFieldValues(String[] vals) {
 		fieldVals.clear();
 		for (int i = 0; i < vals.length; i++) {			
 			try {
 				Field f = fields.get(i);
 				f.setAccessible(true);
 				System.out.println("fieldName: " + f.getName() + ", valueToInput: " + vals[i] + ", isAccessible: " + f.isAccessible());
 				if (!Modifier.isStatic(f.getModifiers())) {
 					setFieldValue(f, vals[i]);
 				}
 				fieldVals.add(vals[i]);
 			} catch (NullPointerException e) {				
 				fieldVals.add("");
 			} catch (IllegalArgumentException e) {
 				fieldVals.add("");
 			} catch (IllegalAccessException e) {
 				fieldVals.add("");
 			}
 		}
 		setChanged();
 		notifyObservers();
 	}
 	
 	private void setFieldValue(Field f, String val) throws NumberFormatException, IllegalArgumentException, IllegalAccessException {
 		String typeStr = f.getType().toString();
 		f.setAccessible(true);
 		if (typeStr.equals("int")) {
 			f.set(obj, Integer.parseInt(val));
 		} else if (typeStr.equals("short")) {
 			f.set(obj, Short.parseShort(val));
 		} else if (typeStr.equals("long")) {
 			f.set(obj, Long.parseLong(val));
 		} else if (typeStr.equals("float")) {
 			f.set(obj, Float.parseFloat(val));
 		} else if (typeStr.equals("double")) {
 			f.set(obj, Double.parseDouble(val));
 		} else if (typeStr.equals("byte")) {
 			f.set(obj, Byte.parseByte(val));
 		} else if (typeStr.equals("boolean")) {
 			f.set(obj, Boolean.valueOf(val));
 		} else if (typeStr.equals("class java.lang.String")) {
 			f.set(obj, val);
 		} else {
 			f.set(obj, (Object)val);
 		}
 	}
 	
 	private Object[] getMethodPrameterValue(Method m, String[] args) {
 		if (args == null || args.length == 0)
 			return null;
 		
 		Object[] result = new Object[args.length];
 		Type[] types = m.getGenericParameterTypes();
 		for (int i = 0; i < types.length; i++) {
 			if (isStocked(args[i])) {
 				int re = Integer.parseInt(args[i].replaceAll("[^0-9]",""));
 				result[i] = stockObjs.get(re);
 			} else {
 				String typeStr = types[i].toString();
 				if (typeStr.equals("int")) {
 					result[i] = Integer.parseInt(args[i]);
 				} else if (typeStr.equals("short")) {
 					result[i] = Short.parseShort(args[i]);
 				} else if (typeStr.equals("long")) {
 					result[i] = Long.parseLong(args[i]);
 				} else if (typeStr.equals("float")) {
 					result[i] = Float.parseFloat(args[i]);
 				} else if (typeStr.equals("double")) {
 					result[i] = Double.parseDouble(args[i]);
 				} else if (typeStr.equals("byte")) {
 					result[i] = Byte.parseByte(args[i]);
 				} else if (typeStr.equals("boolean")) {
 					result[i] = Boolean.valueOf(args[i]);
 				} else if (typeStr.equals("class java.lang.String")) {
 					result[i] = args[i];
 				} else {
 					result[i] = (Object) args[i];
 				}
 			}
 		}
 		return result;
 	}
 	
 	private Object[] getConPrameterValue(Constructor m, String[] args) {
 		if (args == null || args.length == 0)
 			return null;
 		
 		Object[] result = new Object[args.length];
 		Type[] types = m.getGenericParameterTypes();
 		for (int i = 0; i < types.length; i++) {
 			if (isStocked(args[i])) {
 				int re = Integer.parseInt(args[i].replaceAll("[^0-9]",""));
 				result[i] = stockObjs.get(re);
 			} else {
 				
 				String typeStr = types[i].toString();
 				if (typeStr.equals("int")) {
 					result[i] = Integer.parseInt(args[i]);
 				} else if (typeStr.equals("short")) {
 					result[i] = Short.parseShort(args[i]);
 				} else if (typeStr.equals("long")) {
 					result[i] = Long.parseLong(args[i]);
 				} else if (typeStr.equals("float")) {
 					result[i] = Float.parseFloat(args[i]);
 				} else if (typeStr.equals("double")) {
 					result[i] = Double.parseDouble(args[i]);
 				} else if (typeStr.equals("byte")) {
 					result[i] = Byte.parseByte(args[i]);
 				} else if (typeStr.equals("boolean")) {
 					result[i] = Boolean.valueOf(args[i]);
 				} else if (typeStr.equals("class java.lang.String")) {
 					result[i] = args[i];
 				} else {
 					result[i] = (Object) args[i];
 				}
 			}
 		}
 		return result;
 	}
 	
 	public Boolean IsError() {
 		return isError;
 	}
 	
 	public void notifyError(String ex) {
 		isError = true;
 		this.exception = ex;
 		setChanged();
 		notifyObservers();
 	}
 	
 	public String getReturnType() {
 		return method.getGenericReturnType().toString();
 	}
 	
 	public String getReturnValue() {
 		if (ret == null) {
 			return "";
 		} else {
 			return ret.toString();
 		}
 	}
 	
 	public void setSelectedField(int index) {
 		parameterTypes.clear();
 		field = fields.get(index);
 		field.setAccessible(true);
 		
 		setChanged();
 		notifyObservers("field");
 	}
 	
 	public void setSelectedMethod(int index) {
 		parameterTypes.clear();
 		method = methods.get(index);
 		Type[] paras = method.getGenericParameterTypes();
 		for (Type para : paras) {
 			parameterTypes.add(para.toString());
 		}
 		setChanged();
 		notifyObservers("methodParameter");
 	}
 	
 	public void setSelectedConstructor(int index) {
 		conParameterTypes.clear();
 		constructor = constructors.get(index);
 		Type[] paras = constructor.getGenericParameterTypes();
 		for (Type para : paras) {
 			conParameterTypes.add(para.toString());
 		}
 		setChanged();
 		notifyObservers("conParameter");
 	}
 	
 	public void invokeMethod(String[] args) {
 		if (method == null)
 			return;	
 		try {
 			if (args.length == 0) args = null;
 			ret = method.invoke(obj, getMethodPrameterValue(method, args));
 		} catch (IllegalArgumentException e) {
 			setException(e);
 		} catch (IllegalAccessException e) {
 			setException(e);
 		} catch (InvocationTargetException e) {
 			setException(e);
 		}
 		setChanged();
 		notifyObservers("methodReturnVal");
 	}
 	
 	private void setException(Exception e) {
 		Exception newEx = new Exception();
 		newEx.initCause(e);
 		newEx.printStackTrace();
 		exception = newEx.getCause().toString();
 	}
 	
 	public void createArray(String type, String size) {
 		try {
 			Class<?> clazz = Class.forName(type);
 			array = (Object[]) Array.newInstance(clazz, Integer.parseInt(size));
 		} catch (ClassNotFoundException e) {
 			setException(e);
 		}
 		setChanged();
 		notifyObservers("arrayReturnVal");
 	}
 	
 	public void invokeConstructor(String[] args) {
 		if (constructor == null)
 			return;	
 		try {
 			if (args.length == 0) args = null;
 			saveObject(constructor.newInstance(getConPrameterValue(constructor, args)));
 			
 		} catch (IllegalArgumentException e) {
 			setException(e);
 		} catch (IllegalAccessException e) {
 			setException(e);
 		} catch (InvocationTargetException e) {
 			setException(e);
 		} catch (InstantiationException e) {
 			setException(e);
 		}
 		setChanged();
 		notifyObservers("conReturnVal");
 	}
 	
 	public void saveConstructors(Constructor[] cons) {
 		constructors.clear();
 		conNames.clear();
 		for (Constructor con : cons) {
 			constructors.add(con);
 			conNames.add(con.toString());
 		}
 		setChanged();
 		notifyObservers("constructors");
 	}
 	
 	public void setFieldVal(int index, String val) {
 		try {
 			setFieldValue(field, val);
 		} catch (NumberFormatException e) {
 			setException(e);
 		} catch (IllegalArgumentException e) {
 			setException(e);
 		} catch (IllegalAccessException e) {
 			setException(e);
 		}
 		setChanged();
 		notifyObservers("setFieldVal");
 	}
 	
 	private void saveFields(List<Field> fs) {
 		fields = fs;
 		
 		for (Field f : fields) {
 			f.setAccessible(true);
 			fieldNames.add(f.getName());
 			fieldTypes.add(f.getGenericType().toString());
 			try {
 				fieldVals.add(f.get(obj).toString());
 			} catch (NullPointerException e) {				
 				e.printStackTrace();
 				fieldVals.add("");
 			} catch (IllegalArgumentException e) {
 				e.printStackTrace();
 				fieldVals.add("");
 			} catch (IllegalAccessException e) {
 				e.printStackTrace();
 				fieldVals.add("");
 			}
 		}		
 	}
 	
 	private void saveMethods(List<Method> ms) {
 		methods = ms;
 		
 		for (Method m : methods) {
 			m.setAccessible(true);
 			methodNames.add(m.getName());
 			methodTypes.add(m.getGenericReturnType().toString());
 		}		
 	}
 	
 	public void setElement(int index) {
 		array[index] = obj;
 		setChanged();
 		notifyObservers("arrayReturnVal");
 	}
 	
 	public void readSelectedElement(int index) {
 		setArrayObjectToWorkSpace(array[index]);
 	}
 	
 	private void reset() {
 		fieldNames.clear();
 		fieldTypes.clear();
 		fieldVals.clear();
 		methodNames.clear();
 		methodTypes.clear();
 	}
 	
 	private List<Field> getAllFieldsFromClass(Class cls) {
 		List<Field> allFields = new ArrayList<Field>(); 
 		Field[] fields = cls.getDeclaredFields();
 		Field[] publicFields = cls.getFields();
 		for (Field f : publicFields) {
             if(f.getDeclaringClass() == Object.class)
                     continue;
             if(cls.toString().equals(f.getDeclaringClass().toString()))
                     continue;
             allFields.add(f);
         }
 		
 		for (Field f : fields) {
 			allFields.add(f);
 		}
 		return allFields;
 	}
 	
 	private List<Method> getAllMethodsFromClass(Class cls) {
 		List<Method> allMethods = new ArrayList<Method>(); 
 		Method[] methods = cls.getDeclaredMethods();
 		Method[] publicMethods = cls.getMethods();
 		for (Method m : publicMethods) {
             if(m.getDeclaringClass() == Object.class)
                     continue;
             if(cls.toString().equals(m.getDeclaringClass().toString()))
                     continue;
             allMethods.add(m);
         }
 		
 		for (Method m : methods) {
 			allMethods.add(m);
 		}
 		return allMethods;
 	}
 	
 	public final List<String> getFieldNames(){
 	    return fieldNames;
 	}
 	
 	public final List<String> getFieldTypes(){
 	    return fieldTypes;
 	}
 	
 	public final List<String> getFieldVals(){
 	    return fieldVals;
 	}
 	
 	public final List<String> getMethodNames(){
 	    return methodNames;
 	}
 	
 	public final List<String> getMethodParaTypes(){
 	    return parameterTypes;
 	}
 	
 	public final String getExceptionStr(){
 	    return exception;
 	}
 	
 	public final List<String> getConNames(){
 	    return conNames;
 	}
 	
 	public final List<String> getConParaTypes(){
 	    return conParameterTypes;
 	}
 	
 	public final String getFieldName() {
 		return field.getName();
 	}
 	
 	public final String getFieldType() {
 		return field.getGenericType().toString();
 	}
 	
 	public final String getFieldVal() {
 		try {
 			return field.get(obj).toString();
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		}
 		return "";
 	}
 	
 	public final List<String> getArrayNames(){
 		List<String> list = new ArrayList<String>();
		if (array == null)
			return list;
 		for (Object o : array) {
 			if (o == null) {
 				list.add("null");
 			} else {
 				list.add(o.toString());
 			}
 		}
 		return list;
 	}
 	
 	public final void getStockObject(String name) {
 		if (isStocked(name)) {			
 			int re = Integer.parseInt(name.replaceAll("[^0-9]",""));
 			obj = stockObjs.get(re);
 			setStockedObjectToWorkSpace();
 			isError = false;
 			setChanged();
 			notifyObservers();
 		}
 	}
 	
 	public final List<String> getStockObjs() {
 		List<String> list = new ArrayList<String>();
		if (stockObjs == null)
			return list;
 		for (Object o : stockObjs) {
 			if (o == null) {
 				list.add("null");
 			} else {
 				list.add(o.toString());
 			}
 		}
 		return list;
 	}
 	
 	private Boolean isStocked(String str) {
         if (str.indexOf("#") == -1) {
         	return false;
         } else {
         	return true;
         }
 	}
 }
