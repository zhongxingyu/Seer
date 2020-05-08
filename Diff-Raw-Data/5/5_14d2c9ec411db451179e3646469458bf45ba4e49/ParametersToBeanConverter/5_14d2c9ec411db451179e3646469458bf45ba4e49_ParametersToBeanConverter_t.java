 package model;
 
 import java.lang.reflect.InvocationTargetException;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.beanutils.BeanUtilsBean;
 import org.apache.commons.beanutils.ConvertUtilsBean;
 
 public class ParametersToBeanConverter {
 	private static BeanUtilsBean beanUtilsBean = new BeanUtilsBean(
 			new ConvertUtilsBean() {
 				@SuppressWarnings({ "unchecked", "rawtypes" })
 				@Override
 				public Object convert(String value, Class c) {
 					return (c.isEnum()) ? Enum.valueOf(c, value)
 							: super.convert(value, c);
 				}
 			});
 
	public static <T> T populate(Class<T> c, HttpServletRequest request) {
 		T object = null;
 		try {
			object = (T) c.newInstance();
 			beanUtilsBean.populate(object, request.getParameterMap());
 		} catch (InstantiationException | IllegalAccessException
 				| InvocationTargetException e) {
 			throw new ConvertException(e.getMessage());
 		}
 
 		return object;
 	}
 }
