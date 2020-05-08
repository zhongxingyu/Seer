 package me.bukp.uioc.context;
 
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.Map;
 
 import me.bukp.uioc.common.Constants;
 import me.bukp.uioc.common.IocTools;
 import me.bukp.uioc.exception.BeanCreateException;
 import me.bukp.uioc.exception.PropertyHandleException;
 
 /**
  * 对属性进行操作的静态工具类
  * 2012-12-26
  */
 public class PropertyHandler {
 	/**
 	 * 为对象设置属性值
 	 * @param obj 对象
 	 * @param properties 属性名和属性值Map
 	 * @return 属性设置完毕后的对象
 	 */
 	public static Object setProperties(Object obj, Map<String, Object> properties) {
 		Class<?> clazz = obj.getClass();
 		try {
 			for (String prop : properties.keySet()) {
 				String methodName = getSetterMethodName(prop);
 				Method method = findMethod(clazz, methodName, new Class[] { properties.get(prop).getClass() });
 				method.invoke(obj, properties.get(prop));
 			}
 			return obj;
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new PropertyHandleException(e.getMessage());
 		}
 	}
 	
 	/**
 	 * 得到对象所有setter方法
 	 * @param obj 对象
 	 * @return 属性及其对应setter方法的Map
 	 */
 	public static Map<String, Method> getSetterMethods(Object obj) {
 		Map<String, Method> methodsMap = new HashMap<>();
 		Class<?> clazz = obj.getClass();
 		Method[] methods = clazz.getMethods();
 		for (Method method : methods) {
 			String methodname = method.getName();
 			if (methodname.startsWith("set")) {
 				methodsMap.put(getPropertyName(methodname), method);
 			}
 		}
 		return methodsMap;
 	}
 	
 	/**
 	 * 执行对象的setter方法
 	 * @param obj 执行setter方法的对象
 	 * @param bean setter方法参数
 	 * @param method setter方法
 	 */
 	public static void executeSetterMethod(Object obj, Object bean, Method method) {
 		try {
 			method.invoke(obj, bean);
 		} catch (Exception e) {
 			throw new BeanCreateException(e.getMessage());
 		}
 	}
 	
 	/**
 	 * 根据方法签名寻找对应方法
 	 * @param clazz 类对象
 	 * @param methodName 方法名
 	 * @param argsClass 方法的参数对象数组
 	 * @return 与方法签名匹配的方法
 	 */
 	private static Method findMethod(Class<?> clazz, String methodName, Class<?>[] argsClass) {
 		Method setter = null;
 		try {
 			setter = clazz.getMethod(methodName, argsClass);
 		} catch (Exception e) {
 			setter = findMethodExtend(clazz, methodName, argsClass);
 		}
 		if (setter != null) {
 			return setter;
 		}
 		throw new PropertyHandleException(Constants.EXCEPTION_MESSAGE_NO_METHOD);
 	}
 	
 	/**
 	 * 如果类的构造器中的参数是某一接口，而传入的参数是接口的某个实现
 	 * 直接使用getMethod会抛出NoSuchMethodException异常
 	 * 所以，取出Class的所有构造函数，并对传入的参数进行强制转换后与构造函数的参数进行比较
 	 * @param clazz 类对象
 	 * @param methodName 方法名
 	 * @param argsClass 方法的参数对象数组
 	 * @return 与方法签名匹配的方法
 	 */
 	private static Method findMethodExtend(Class<?> clazz, String methodName, Class<?>[] argsClass) {
 		Method[] methods = clazz.getMethods();
 		for (Method method : methods) {
 			if (!method.getName().equals(methodName)) {
 				continue;
 			}
 			Class<?>[] methodClasses = method.getParameterTypes();
 			if (IocTools.ifSameArgsClass(argsClass, methodClasses)) {
 				return method;
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * 根据属性名构造方法名
 	 * @param propertyName 属性名
 	 * @return setter方法名
 	 */
 	private static String getSetterMethodName(String propertyName) {
 		String first = propertyName.substring(0, 1).toUpperCase();
 		int length = propertyName.length();
 		String other = propertyName.substring(1, length);
 		return "set" + first + other;
 	}
 	
 	/**
 	 * 根据方法名得到属性名
 	 * @param methodName 方法名
 	 * @return 属性名
 	 */
 	private static String getPropertyName(String methodName) {
		int length = methodName.length();
		String field = methodName.substring(3, length);
 		String first = field.substring(0, 1).toLowerCase();
		String other = field.substring(1, length);
 		return first + other;
 	}
 }
