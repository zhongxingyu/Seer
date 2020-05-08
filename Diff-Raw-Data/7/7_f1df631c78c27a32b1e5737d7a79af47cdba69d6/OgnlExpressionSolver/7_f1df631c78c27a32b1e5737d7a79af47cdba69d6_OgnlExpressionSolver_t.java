 package org.rulemaker.engine.expressions;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import ognl.Ognl;
 import javassist.ClassPool;
 import javassist.CtClass;
 import javassist.CtField;
 import javassist.CtMethod;
 import javassist.CtNewMethod;
 import javassist.NotFoundException;
 
 import org.apache.commons.beanutils.PropertyUtils;
 import org.rulemaker.engine.expressions.exception.InvalidExpressionException;
 
 public class OgnlExpressionSolver implements ExpressionSolver {
 
 	public Object eval(Map<String, Object> contextMap, String expression)
 			throws InvalidExpressionException {
 		try {
			String normalizedExpression = expression;
 			// Normalize variables to camel case notation
 			List<String> variableNamesToBeChanged = new ArrayList<String>();
 			for (String variableName : contextMap.keySet()) {
 				String firstChar = variableName.charAt(0) + "";
 				if (firstChar.equals(firstChar.toUpperCase())) {
 					variableNamesToBeChanged.add(variableName);
 				}
 			}
 			for (String aVariableNameToBeChanged : variableNamesToBeChanged) {
 				Object variableValue = contextMap.remove(aVariableNameToBeChanged);
 				String newVariableName = "a$" + aVariableNameToBeChanged;
				normalizedExpression = replaceAll(normalizedExpression, aVariableNameToBeChanged, newVariableName);
 				contextMap.put(newVariableName, variableValue);
 			}
			Object finalValue = Ognl.getValue(normalizedExpression, contextMap,
 					buildRootObject(contextMap));	
 			return finalValue;
 		} catch (Exception e) {
 			throw new InvalidExpressionException(e);
 		} finally {
 			// Undo changes to context map due to camel case notation
 			List<String> variableNamesToBeRestored = new ArrayList<String>();
 			for (String variableName : contextMap.keySet()) {
 				if (variableName.startsWith("a$")) {
 					variableNamesToBeRestored.add(variableName);
 				}
 			}
 			for (String aVariableNameToBeRestored : variableNamesToBeRestored) {
 				String oldVariableName = aVariableNameToBeRestored.substring(2);
 				Object variableValue = contextMap.remove(aVariableNameToBeRestored);
 				contextMap.put(oldVariableName, variableValue);
 			}
 		}
 	}
 
 	/**
 	 * Builds OGNL root object based on context map, for each
 	 * key-value pair in context map will be a matching property 
 	 * (member field, getter and setter) 
 	 * in root object with the same value.
 	 * 
 	 * @param contextMap The source context map to build OGNL root object
 	 * @return The resulting root object.
 	 * @throws Exception If something goes wrong during process.
 	 * 
 	 */
 	private Object buildRootObject(Map<String, Object> contextMap) 
 			throws Exception {
 		// First define $RootObject class
 		ClassPool pool = ClassPool.getDefault();
 		CtClass rootObjectCtClass = pool.makeClass("$RootObject");
 		// Later add member fields and getter from contextMap
 		for (Map.Entry<String, Object> entry : contextMap.entrySet()) {
 			String propertyName = entry.getKey();
 			Object propertyValue = entry.getValue();
 			Class<?> propertyValueType = (propertyValue != null) ? propertyValue.getClass() : Object.class;
 			CtField properyCtField = new CtField(resolveCtClass(propertyValueType), propertyName, rootObjectCtClass);
 			rootObjectCtClass.addField(properyCtField);
 			CtMethod getterMethod = CtNewMethod.getter(resolveGetterName(propertyName), properyCtField);
 			rootObjectCtClass.addMethod(getterMethod);
 			CtMethod setterMethod = CtNewMethod.setter(resolveSetterName(propertyName), properyCtField);
 			rootObjectCtClass.addMethod(setterMethod);
 		}
 		Class<?> rootObjectClass = rootObjectCtClass.toClass(new ClassLoader(Thread.currentThread().getContextClassLoader()) {}, null);
 		//String rootObjectClassSource = rootObjectCtClass.toString();
 		// Now create instance
 		Object rootObjectInstance = rootObjectClass.newInstance();
 		for (Map.Entry<String, Object> entry : contextMap.entrySet()) {
 			String propertyName = entry.getKey();
 			Object propertyValue = entry.getValue();
 			PropertyUtils
 					.setProperty(rootObjectInstance, propertyName, propertyValue);
 		}
 		rootObjectCtClass.detach();
 		return rootObjectInstance;
 	}
 	
 	private CtClass resolveCtClass(Class<?> clazz) throws NotFoundException {
 		ClassPool pool = ClassPool.getDefault();
 		return pool.get(clazz.getName());
 	}
 	
 	private String toUppercaseFirstLetter(String propertyName) {
 		return propertyName.substring(0, 1).toUpperCase() +
 			   propertyName.substring(1, propertyName.length());
 	}
 	
 	private String resolveGetterName(String propertyName) {
 		return "get" + toUppercaseFirstLetter(propertyName);
 	}
 	
 	private String resolveSetterName(String propertyName) {
 		return "set" + toUppercaseFirstLetter(propertyName);
 	}
 	
 	private String replaceAll(String sourceString, String oldChar, String newChar) {
 		int index = sourceString.indexOf(oldChar);
 		if (index >= 0) {
 			int sliceIndex = index + oldChar.length();
 			String lowerSourceString = sourceString.substring(0, sliceIndex);
 			String upperSourceString = sourceString.substring(sliceIndex, sourceString.length());
 			return lowerSourceString.replace(oldChar, newChar) +
 					replaceAll(upperSourceString, oldChar, newChar);
 		} else {
 			return sourceString;
 		}
 	}
 }
