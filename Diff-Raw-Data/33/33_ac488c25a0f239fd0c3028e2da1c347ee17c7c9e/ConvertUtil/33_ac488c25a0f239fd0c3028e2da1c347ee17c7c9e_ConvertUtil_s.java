 package framework.base.utils;
 
 import java.util.Iterator;
 import java.util.Map;
 
 public class ConvertUtil
 {
 	public static String objToString(Object obj)
 	{
 		if (obj == null)
 		{
 			return null;
 		}
 		return obj.toString();
 	}
 
 	/**
 	 * 根据key反射value到obj中
 	 * 
 	 * @param map
 	 * @param cls
 	 * @return
 	 * @author hjin
 	 */
 	@SuppressWarnings("unchecked")
 	public static <T> T mapToObject(Map<String, Object> map, Class<T> cls)
 	{
 		if (map == null)
 		{
 			return null;
 		}
 
 		T obj = (T) ReflectUtil.newInstance(cls.getName());
 
 		Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator();
 		while (it.hasNext())
 		{
 			Map.Entry<String, Object> en = it.next();
 			String key = en.getKey();
 			Object o = en.getValue();
 
 			ReflectUtil.invokeSetMethod(obj, key, o);
 		}
 
 		return obj;
 	}
 
 }
