 /*******************************************************************************
  * Copyright (c) 2005, 2007 Intel Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Intel Corporation - Initial API and implementation
  *******************************************************************************/
 package org.eclipse.cdt.utils.envvar;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.cdt.core.envvar.EnvirinmentVariable;
 import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
 import org.eclipse.cdt.internal.core.envvar.EnvironmentVariableManager;
 
 
 /**
  * This is an utility class that implements environment variable operations 
  * functionality: append, prepend, replace and remove
  * 
  * @since 3.0
  */
 public class EnvVarOperationProcessor {
 	/**
 	 * performs the environment variable operation given an initial variable and
 	 * a variable representing an operation to be performed
 	 * Returns a new variable the represents the result of a performed operation 
 	 * 
 	 * @param initial the initial variable
 	 * @param added the variable that specifies an operation to be performed on the
 	 * initial variable value 
 	 * @return the new variable the represents the result of a performed operation 
 	 */
 	static public IEnvironmentVariable performOperation(IEnvironmentVariable initial, IEnvironmentVariable added){
 		if(initial == null){
 			return added;
 		}
 		if(added == null)
 			return initial;
 		
 		String name = added.getName();
 		
 		switch(added.getOperation()){
 		case IEnvironmentVariable.ENVVAR_REMOVE:
 			return new EnvirinmentVariable(name,null,IEnvironmentVariable.ENVVAR_REMOVE,null);
 		case IEnvironmentVariable.ENVVAR_APPEND:{
 				String delimiter = added.getDelimiter();
 				return new EnvirinmentVariable(name,
 						performAppend(initial.getValue(),added.getValue(),delimiter),
						IEnvironmentVariable.ENVVAR_APPEND,
 						delimiter);
 			}
 		case IEnvironmentVariable.ENVVAR_PREPEND:{
 				String delimiter = added.getDelimiter();
 				return new EnvirinmentVariable(name,
 						performPrepend(initial.getValue(),added.getValue(),delimiter),
						IEnvironmentVariable.ENVVAR_PREPEND,
 						delimiter);
 			}
 		case IEnvironmentVariable.ENVVAR_REPLACE:
 		default:
 			return new EnvirinmentVariable(added.getName(),added.getValue(),added.getDelimiter());
 		}
 	}
 
 	/**
 	 * performs append or prepend given an initial String, a string to be appended/prepended and a delimiter
 	 * Returns a String representing the result of the operation
 	 * @param initialValue
 	 * @param addValue
 	 * @param delimiter
 	 * @param prepend
 	 * @return String
 	 */
 	static public String performAppendPrepend(String initialValue, String addValue, String delimiter, boolean prepend){
 		if(initialValue == null)
 			return addValue;
 		if(addValue == null)
 			return initialValue;
 		
 		if(delimiter == null || "".equals(delimiter)){   //$NON-NLS-1$
 			return prepend ? addValue + initialValue : initialValue + addValue;
 		}
 		
 		List value = convertToList(initialValue, delimiter);
 		List added = convertToList(addValue, delimiter);
 
 		value = removeDuplicates(value, added);
 		
 		if(prepend)
 			value.addAll(0,added);
 		else
 			value.addAll(added);
 		
 		return convertToString(value, delimiter);
 	}
 	
 	/**
 	 * performs append given an initial String, a string to be appended and a delimiter
 	 * Returns a String representing the result of the operation
 	 * @param initialValue
 	 * @param addValue
 	 * @param delimiter
 	 * @return String
 	 */
 	static public String performAppend(String initialValue, String addValue, String delimiter){
 		return performAppendPrepend(initialValue,addValue,delimiter,false);
 	}
 	
 	/**
 	 * performs prepend given an initial String, a string to be prepended and a delimiter
 	 * Returns a String representing the result of the operation
 	 * @param initialValue
 	 * @param addValue
 	 * @param delimiter
 	 * @return String
 	 */
 	static public String performPrepend(String initialValue, String addValue, String delimiter){
 		return performAppendPrepend(initialValue,addValue,delimiter,true);
 	}
 	
 	/**
 	 * performs an environment variable operation
 	 * Returns String representing the result of the operation
 	 * @param initialValue
 	 * @param newValue
 	 * @param delimiter
 	 * @param op
 	 * @return String
 	 */
 	static public String performOperation(String initialValue, String newValue, String delimiter, int op){
 		switch(op){
 		case IEnvironmentVariable.ENVVAR_REMOVE:
 			return null;
 		case IEnvironmentVariable.ENVVAR_PREPEND:
 			return performPrepend(initialValue,newValue,delimiter);
 		case IEnvironmentVariable.ENVVAR_APPEND:
 			return performAppend(initialValue,newValue,delimiter);
 		case IEnvironmentVariable.ENVVAR_REPLACE:	
 		default:
 			return initialValue;
 		}
 	}
 
 	/**
 	 * Converts a given value to string using a delimiter passed to this method
 	 * @param value
 	 * @param delimiter
 	 * @return
 	 */
 	static public List convertToList(String value, String delimiter){
 		List list = new ArrayList();
 		int delLength = delimiter.length();
 		int valLength = value.length();
 
 		if(delLength == 0){
 			list.add(value);
 		}
 		else{
 			int start = 0;
 			int stop;
 			while(start < valLength){
 				stop = value.indexOf(delimiter,start);
 				if(stop == -1)
 					stop = valLength;
 				String subst = value.substring(start,stop);
 				list.add(subst);
 				start = stop + delLength;
 			}
 		}
 
 		return list;
 	}
 	
 	/*
 	 * removes duplicates
 	 */
 	static public List removeDuplicates(List value, List duplicates){
 		List list = new ArrayList();
 		Iterator valueIter = value.iterator();
 		while(valueIter.hasNext()){
 			String curVal = (String)valueIter.next();
 			boolean duplFound = false;
 			Iterator duplicatesIter = duplicates.iterator();
 			while(duplicatesIter.hasNext()){
 				String curDupl = (String)duplicatesIter.next();
 				if(curVal.equals(curDupl)){
 					duplFound = true;
 					break;
 				}
 			}
 			if(!duplFound)
 				list.add(curVal);
 		}
 		return list;
 	}
 	
 	/**
 	 * Converts list to a single String using a given delimiter to separate
 	 * the list value in the resulting String
 	 * @param list
 	 * @param delimiter
 	 * @return String
 	 */
 	static public String convertToString(List list, String delimiter){
 		Iterator iter = list.iterator();
 		StringBuffer buffer = new StringBuffer();
 		
 		while(iter.hasNext()){
 			buffer.append((String)iter.next());
 			
 			if(iter.hasNext())
 				buffer.append(delimiter);
 		}
 		
 		return buffer.toString();
 	}
 	
 	/*
 	 * concatenetes two Strings
 	 * Returns a resulting string
 	 */
 	static private String concatenateStrings(String str1, String str2, String delimiter){
 		if(str1 == null || "".equals(str1))   //$NON-NLS-1$
 			return str2;
 		if(str2 == null || "".equals(str2))   //$NON-NLS-1$
 			return str1;
 		
 		return str1 + delimiter + str2;
 	}
 	
 	/*
 	 * normalizes the variable name. That is: removes prepended and appended spaces
 	 * and converts the name to upper-case for Win32 systems
 	 * @return the normalized name or <code>null</code> in case the name is not valid
 	 */
 	static public String normalizeName(String name){
 		if(name == null)
 			return null;
 		if("".equals(name = name.trim()))   //$NON-NLS-1$
 			return null;
 		if(!EnvironmentVariableManager.getDefault().isVariableCaseSensitive())
 			name = name.toUpperCase();
 		return name;
 	}
 	
 	static public IEnvironmentVariable[] filterVariables(IEnvironmentVariable variables[], String remove[]){
 		
 		if(variables == null || variables.length == 0)
 			return variables;
 		
 		IEnvironmentVariable filtered[] = new IEnvironmentVariable[variables.length];
 		int filteredNum = 0;
 		for(int i = 0; i < variables.length; i++){
 			IEnvironmentVariable var = variables[i];
 			String name = null;
 			if(var != null && (name = normalizeName(var.getName())) != null){
 				boolean skip = false;
 				if(remove != null && remove.length > 0){
 					for(int j = 0; j < remove.length; j++){
 						if(remove[j] != null && remove[j].equals(name)){
 							skip = true;
 							break;
 						}
 					}
 				}
 				if(!skip)
 					filtered[filteredNum++] = var;
 			}
 		}
 
 		if(filteredNum != filtered.length){
 			IEnvironmentVariable vars[] = new IEnvironmentVariable[filteredNum];
 			for(int i = 0; i < filteredNum; i++)
 				vars[i] = filtered[i];
 			filtered = vars;
 		}
 		return filtered;
 	}
 }
