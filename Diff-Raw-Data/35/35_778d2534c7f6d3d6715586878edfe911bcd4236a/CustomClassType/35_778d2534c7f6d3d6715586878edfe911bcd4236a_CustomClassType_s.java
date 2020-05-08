 package com.github.rd4j.form.types;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.Map;
 
 import com.github.rd4j.DefaultDispatcher;
 import com.github.rd4j.analysis.MethodParameter;
 import com.github.rd4j.expr.ExpressionUtil;
 
 public class CustomClassType implements ClassType {
 	final Class<?> clazz;
 	final Constructor constructor;
 	final Map<String, Rd4jType> propertyTypes = new HashMap<String, Rd4jType>();
 	final Map<String, Integer> propertyInteger = new HashMap<String, Integer>();
 
 	public CustomClassType(Class<?> clazz)
 	{
 		this.clazz = clazz;
 
 		Constructor[] constructors = this.clazz.getConstructors();
 		Constructor bestFound = null;
 		for(Constructor c : constructors)
 		{
 			// hack: take the first constructor that takes some parameters
 			if (c.getParameterTypes().length > 0)
 			{
 				bestFound = c;
 			}
 		}
 		
 		this.constructor = bestFound;
 		if(bestFound == null)
 		{
 			return;
 		}
 		
 		Class[] parameterTypes = constructor.getParameterTypes();
 		String parameterNames[] = getParameterNames(constructor);
 		
 		int i = 0;
 		for(Annotation[] annotations : this.constructor.getParameterAnnotations())
 		{
 			String parameterName = null;
 			for(Annotation annotation : annotations)
 			{
 				if(annotation instanceof ParameterName)
 				{
 					parameterName = ((ParameterName)annotation).value();
 				}
 			}
 			
 			if(parameterName == null)
 			{
 				parameterName = parameterNames[i];
 			}
 			
 			propertyInteger.put(parameterName, i);
 			propertyTypes.put(parameterName, ExpressionUtil.getRd4jType(parameterTypes[i]));
 			i++;
 		}
 	}
 
 	public String[] getParameterNames(Constructor method)
 	{
 		Map<String, MethodParameter[]> map = DefaultDispatcher.getMethodParameterMap(method.getDeclaringClass());
		MethodParameter [] parameters = map.get("_init_");
 		String [] parameterNames = new String[parameters.length];
 		for(int i=0;i<parameters.length;i++)
 		{
 			parameterNames[i] = parameters[i].getName();
 		}
 		return parameterNames;
 	}
 	
 	public Rd4jType getPropertyType(String childName) {
 		return propertyTypes.get(childName);
 	}
 
 	public Object createNew(Map<String, Object> propertyValues) {
 		Object[] parameters = new Object[propertyInteger.size()];
 		for(Map.Entry<String, Integer> entry : propertyInteger.entrySet())
 		{
 			String propertyName = entry.getKey();
 			int index = entry.getValue().intValue();
 			
 			parameters[index] = propertyValues.get(propertyName);
 		}
 
 		try {
 			return this.constructor.newInstance(parameters);
 		} catch (Exception ex) {
 			throw new RuntimeException("Could not instantiate "+clazz, ex);
 		}
 	}
 
 	public Class<?> getJavaClass() {
 		return clazz;
 	}
 }
