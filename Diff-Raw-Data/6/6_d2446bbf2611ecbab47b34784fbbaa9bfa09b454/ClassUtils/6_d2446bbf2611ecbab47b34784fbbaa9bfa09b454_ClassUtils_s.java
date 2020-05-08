 /*
  * Copyright 2011 cruxframework.org.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package org.cruxframework.crux.core.utils;
 
 import java.lang.reflect.Method;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.cruxframework.crux.core.client.utils.StringUtils;
 import org.cruxframework.crux.core.rebind.screen.widget.WidgetCreator;
 import org.cruxframework.crux.core.rebind.screen.widget.declarative.TagConstraints;
 
 
 import com.google.gwt.core.ext.typeinfo.JClassType;
 import com.google.gwt.core.ext.typeinfo.JField;
 import com.google.gwt.core.ext.typeinfo.JMethod;
 import com.google.gwt.core.ext.typeinfo.JParameter;
 import com.google.gwt.core.ext.typeinfo.JPrimitiveType;
 import com.google.gwt.core.ext.typeinfo.JType;
 import com.google.gwt.core.ext.typeinfo.NotFoundException;
 
 /**
  * @author Thiago da Rosa de Bustamante
  *
  */
 public class ClassUtils
 {
 
 	public static JType buildGetValueExpression(StringBuilder out, JClassType dtoType, String colKey, 
 			String recordObject, boolean finishCommand) 
 					throws NoSuchFieldException
     {
         if (StringUtils.isEmpty(colKey))
         {
 			throw new NoSuchFieldException(colKey);
         }
         String[] props;
         if (colKey.contains("."))
         {
         	props = colKey.split("\\.");
         }
         else
         {
         	props = new String[]{colKey};
         }
         
         if (props != null && props.length > 0)
         {
         	StringBuilder getExpression = new StringBuilder();
         	StringBuilder checkNullExpression = new StringBuilder();
         	
         	getExpression.append(recordObject);
         	JType baseType = dtoType;
         	JClassType baseClassType = baseType.isClassOrInterface();
         	for (int i=0; i < props.length; i++)
         	{
         		if (baseClassType == null && i < props.length-1)
         		{
         			throw new NoSuchFieldException(colKey);
         		}
         		String prop = props[i];
         		if (i>0)
         		{
         			if (i>1)
         			{
         				checkNullExpression.append(" || ");
         			}
         			checkNullExpression.append(getExpression.toString()+"==null ");
         		}
         		
         		String getterMethod = ClassUtils.getGetterMethod(prop, baseClassType);
         		if (getterMethod == null)
         		{
         			throw new NoSuchFieldException(colKey);
         		}
         		getExpression.append("."+getterMethod+"()");
         		baseType = ClassUtils.getReturnTypeFromMethodClass(baseClassType, getterMethod, new JType[]{});
         		baseClassType = baseType.isClassOrInterface();
         	}
         	if (finishCommand)
         	{
         		getExpression.append(";");
         	}
         	
         	if (checkNullExpression.length() > 0)
         	{
         		out.append(checkNullExpression.toString()+"?null:");
         	}
         	out.append(getExpression.toString());
         	
         	return baseType;
         }
         else
         {
 			throw new NoSuchFieldException(colKey);
         }
     }		
 	/**
 	 * 
 	 * @param propertyName
 	 * @return
 	 */
 	public static String getSetterMethod(String propertyName)
 	{
 		if (propertyName == null || propertyName.length() == 0)
 		{
 			return null;
 		}
 		String result = "set"+Character.toUpperCase(propertyName.charAt(0)); 
 		if (propertyName.length() > 1)
 		{
 			result += propertyName.substring(1);
 		}
 		return result;
 	}
 
 	/**
 	 * 
 	 * @param propertyName
 	 * @param baseClass 
 	 * @return
 	 */
 	public static String getGetterMethod(String propertyName, JClassType baseClass)
 	{
 		if (propertyName == null || propertyName.length() == 0)
 		{
 			return null;
 		}
 		String result = ""+Character.toUpperCase(propertyName.charAt(0)); 
 		result += propertyName.substring(1);
 		if (propertyName.length() > 1)
 		{
 			try
             {
 	            baseClass.getMethod("get"+result, new JType[]{});
                 result = "get"+result;
             }
             catch (Exception e)
             {
 	            try
                 {
 	                baseClass.getMethod("is"+result, new JType[]{});
 	                result = "is"+result;
                 }
                 catch (Exception e1)
                 {
                 	if (baseClass.getSuperclass() == null)
                 	{
                 		result = null;
                 	}
                 	else
                 	{
                 		result = getGetterMethod(propertyName, baseClass.getSuperclass());
                 	}
                 }
             }
 			
 		}
 		return result;
 	}
 
 	/**
 	 * 
 	 * @param propertyName
 	 * @param baseClass 
 	 * @return
 	 */
 	public static String getGetterMethod(String propertyName, Class<?> baseClass)
 	{
 		if (propertyName == null || propertyName.length() == 0)
 		{
 			return null;
 		}
 		String result = ""+Character.toUpperCase(propertyName.charAt(0)); 
 		result += propertyName.substring(1);
 		if (propertyName.length() > 1)
 		{
 			try
             {
 	            baseClass.getMethod("get"+result, new Class<?>[]{});
                 result = "get"+result;
             }
             catch (Exception e)
             {
 	            try
                 {
 	                baseClass.getMethod("is"+result, new Class<?>[]{});
 	                result = "is"+result;
                 }
                 catch (Exception e1)
                 {
                		result = null;
                 }
             }
 			
 		}
 		return result;
 	}
 
 	/**
 	 * @param propertyName
 	 * @return
 	 */
 	public static String getGetterMethod(String propertyName)
 	{
 		if (propertyName == null || propertyName.length() == 0)
 		{
 			return null;
 		}
 		String result = ""+Character.toUpperCase(propertyName.charAt(0)); 
 		result += propertyName.substring(1);
         result = "get"+result;
 		return result;
 	}
 
 	/**
 	 * 
 	 * @param widgetType
 	 * @param setterMethod
 	 * @return
 	 */
 	public static boolean hasValidSetter(Class<?> widgetType, String setterMethod, Class<?> attrType)
 	{
 		try
 		{
 			if (widgetType.getMethod(setterMethod, new Class<?>[]{attrType}) != null)
 			{
 				return true;
 			}
 		}
 		catch (Exception e) 
 		{
 			try
 			{
 				if (attrType.isPrimitive())
 				{
 					Class<?> wrapperType = getBoxedClassForPrimitive(attrType);
 					if (widgetType.getMethod(setterMethod, new Class<?>[]{wrapperType}) != null)
 					{
 						return true;
 					}
 				}
 				else
 				{
 					Class<?> primitiveType = getPrimitiveFromWrapper(attrType);
 					if (primitiveType != null && widgetType.getMethod(setterMethod, new Class<?>[]{primitiveType}) != null)
 					{
 						return true;
 					}
 				}
 			}
 			catch (Exception e1) 
 			{
 				// Do nothing... try superclass
 			}
 			if (attrType.getSuperclass() != null)
 			{
 				return hasValidSetter(widgetType, setterMethod, attrType.getSuperclass());
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * @param primitiveType
 	 * @return
 	 */
 	public static Class<?> getBoxedClassForPrimitive(Class<?> primitiveType)
 	{
 		if (primitiveType.equals(Integer.TYPE))
 		{
 			return Integer.class;
 		}
 		else if (primitiveType.equals(Short.TYPE))
 		{
 			return Short.class;
 		}
 		else if (primitiveType.equals(Byte.TYPE))
 		{
 			return Byte.class;
 		}
 		else if (primitiveType.equals(Long.TYPE))
 		{
 			return Long.class;
 		}
 		else if (primitiveType.equals(Float.TYPE))
 		{
 			return Float.class;
 		}
 		else if (primitiveType.equals(Double.TYPE))
 		{
 			return Double.class;
 		}
 		else if (primitiveType.equals(Boolean.TYPE))
 		{
 			return Boolean.class;
 		}
 		else if (primitiveType.equals(Character.TYPE))
 		{
 			return Character.class;
 		}
 		return null;
 	}
 	
 	
 	/**
 	 * @param attrType
 	 * @return
 	 */
 	private static Class<?> getPrimitiveFromWrapper(Class<?> attrType)
     {
 		if (attrType.equals(Integer.class))
 		{
 			return Integer.TYPE;
 		}
 		else if (attrType.equals(Short.class))
 		{
 			return Short.TYPE;
 		}
 		else if (attrType.equals(Long.class))
 		{
 			return Long.TYPE;
 		}
 		else if (attrType.equals(Byte.class))
 		{
 			return Byte.TYPE;
 		}
 		else if (attrType.equals(Float.class))
 		{
 			return Float.TYPE;
 		}
 		else if (attrType.equals(Double.class))
 		{
 			return Double.TYPE;
 		}
 		else if (attrType.equals(Boolean.class))
 		{
 			return Boolean.TYPE;
 		}
 		else if (attrType.equals(Character.class))
 		{
 			return Character.TYPE;
 		}
 	    return null;
     }
 
 	/**
 	 * @param methodName
 	 * @return
 	 */
 	public static JType getReturnTypeFromMethodClass(JClassType clazz, String methodName, JType[] params)
     {
 	    JMethod method = getMethod(clazz, methodName, params);
 		
 		if (method == null)
 		{
 			return null;
 		}
 		JType returnType = method.getReturnType();
 		return returnType;
     }
 
 	/**
 	 * @param clazz
 	 * @param methodName
 	 * @param params
 	 * @return
 	 */
 	public static JMethod getMethod(JClassType clazz, String methodName, JType[] params)
     {
 	    JMethod method = null;
 	    JClassType superClass = clazz;
 	    while (method == null && superClass.getSuperclass() != null)
 	    {
 	    	method = superClass.findMethod(methodName, params);
 	    	superClass = superClass.getSuperclass();
 	    }
 	    return method;
     }
 	
 	/**
 	 * 
 	 * @param valueVariable
 	 * @param expectedType
 	 * @return
 	 * @throws NotFoundException 
 	 */
 	public static String getParsingExpressionForSimpleType(String valueVariable, JType expectedType) throws NotFoundException
 	{
 		if (expectedType == JPrimitiveType.INT || Integer.class.getCanonicalName().equals(expectedType.getQualifiedSourceName()))
 		{
 			return "Integer.parseInt("+valueVariable+")";
 		}
 		else if (expectedType == JPrimitiveType.SHORT || Short.class.getCanonicalName().equals(expectedType.getQualifiedSourceName()))
 		{
 			return "Short.parseShort("+valueVariable+")";
 		}
 		else if (expectedType == JPrimitiveType.LONG || Long.class.getCanonicalName().equals(expectedType.getQualifiedSourceName()))
 		{
 			return "Long.parseLong("+valueVariable+")";
 		}
 		else if (expectedType == JPrimitiveType.BYTE || Byte.class.getCanonicalName().equals(expectedType.getQualifiedSourceName()))
 		{
 			return "Byte.parseByte("+valueVariable+")";
 		}
 		else if (expectedType == JPrimitiveType.FLOAT || Float.class.getCanonicalName().equals(expectedType.getQualifiedSourceName()))
 		{
 			return "Float.parseFloat("+valueVariable+")";
 		}
 		else if (expectedType == JPrimitiveType.DOUBLE || Double.class.getCanonicalName().equals(expectedType.getQualifiedSourceName()))
 		{
 			return "Double.parseDouble("+valueVariable+")";
 		}
 		else if (expectedType == JPrimitiveType.BOOLEAN || Boolean.class.getCanonicalName().equals(expectedType.getQualifiedSourceName()))
 		{
 			return "Boolean.parseBoolean("+valueVariable+")";
 		}
 		else if (expectedType == JPrimitiveType.CHAR || Character.class.getCanonicalName().equals(expectedType.getQualifiedSourceName()))
 		{
 			return valueVariable+".charAt(0)";
 		}
 		else if (expectedType.isEnum() != null)
 		{
 			return expectedType.getQualifiedSourceName()+".valueOf("+valueVariable+")";
 		}
 		else
 		{
 			JClassType stringType = ((JClassType)expectedType).getOracle().getType(String.class.getName());
 		    if (stringType.isAssignableFrom((JClassType)expectedType))
 		    {
 		    	return valueVariable;
 		    }
 			
 		}
 
 		return null;
 	}
 
 	/**
 	 * 
 	 * @param processorClass
 	 * @return
 	 */
 	public static TagConstraints getChildTagConstraintsAnnotation(Class<?> processorClass)
 	{
 		TagConstraints attributes = processorClass.getAnnotation(TagConstraints.class);
 		if (attributes == null)
 		{
 			Class<?> superClass = processorClass.getSuperclass();
 			if (superClass != null && !superClass.equals(WidgetCreator.class))
 			{
 				attributes = getChildTagConstraintsAnnotation(superClass);
 			}
 		}
 		
 		return attributes;
 	}
 	
 	/**
 	 * @param clazz
 	 * @param name
 	 * @return
 	 */
 	public static JField getDeclaredField(JClassType clazz, String name) throws NoSuchFieldException
 	{
 		JField field = clazz.getField(name);
 		if (field == null)
 		{
 			if (clazz.getSuperclass() == null)
 			{
 				throw new NoSuchFieldException(name);
 			}
 			field = getDeclaredField(clazz.getSuperclass(), name);
 		}
 
 		return field;
 	}
 	
 	/**
 	 * @param clazz
 	 * @return
 	 */
 	public static JField[] getDeclaredFields(JClassType clazz)
 	{
 		if (clazz.getSuperclass() == null)
 		{
 			return new JField[0];
 		}
 		Set<JField> result = new HashSet<JField>();
 		JField[] declaredFields = clazz.getFields();
 		for (JField field : declaredFields)
 		{
 			result.add(field);
 		}
 		clazz = clazz.getSuperclass();
 		while (clazz.getSuperclass() != null)
 		{
 			declaredFields = clazz.getFields();
 			for (JField field : declaredFields)
 			{
 				if (!result.contains(field))
 				{
 					result.add(field);
 				}
 			}
 			clazz = clazz.getSuperclass();
 		}
 		
 		return result.toArray(new JField[result.size()]);
 	}
 	
 	/**
 	 * @param method
 	 * @return
 	 */
 	public static String getMethodDescription(Method method)
 	{
 		StringBuilder str = new StringBuilder();
 		
 		str.append(method.getDeclaringClass().getCanonicalName());
 		str.append(".");
 		str.append(method.getName());
 		str.append("(");
 		boolean needsComma = false;
 		
 		for ( Class<?> type : method.getParameterTypes())
 		{
 			if (needsComma)
 			{
 				str.append(",");
 			}
 			needsComma = true;
 			str.append(type.getCanonicalName());
 		}
 		str.append(")");
 		
 		return str.toString();
 	}
 	
 	/**
 	 * @param method
 	 * @return
 	 */
 	public static String getMethodDescription(JMethod method)
 	{
 		StringBuilder str = new StringBuilder();
 		
 		str.append(method.getEnclosingType().getQualifiedSourceName());
 		str.append(".");
 		str.append(method.getName());
 		str.append("(");
 		boolean needsComma = false;
 		
 		for (JParameter parameter: method.getParameters())
 		{
 			if (needsComma)
 			{
 				str.append(",");
 			}
 			needsComma = true;
 			str.append(parameter.getType().getParameterizedQualifiedSourceName());
 		}
 		str.append(")");
 		
 		return str.toString();
 	}
 }
