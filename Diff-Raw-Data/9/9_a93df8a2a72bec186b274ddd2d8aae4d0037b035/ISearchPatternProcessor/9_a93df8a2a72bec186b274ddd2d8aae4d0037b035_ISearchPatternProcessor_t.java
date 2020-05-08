 /*******************************************************************************
  * Copyright (c) 2008 xored software, Inc.  
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html  
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Yuri Strot)
  *******************************************************************************/
 package org.eclipse.dltk.core;
 
 import org.eclipse.dltk.core.search.indexing.IIndexConstants;
 
 public interface ISearchPatternProcessor {
 
 	char TYPE_SEPARATOR = IIndexConstants.TYPE_SEPARATOR;
 
 	/**
 	 * Parsed type pattern
 	 */
 	public interface ITypePattern {
 		/**
 		 * Returns the type qualification as chars, can be <code>null</code> if
 		 * no qualification.
 		 * 
 		 * In qualification segment separators should be replaced with
 		 * {@link ISearchPatternProcessor#TYPE_SEPARATOR}
 		 * 
 		 * @return
 		 */
 		char[] qualification();
 
 		/**
 		 * Returns the type qualification as String, can be <code>null</code> if
 		 * no qualification
 		 * 
 		 * In qualification segment separators should be replaced with
 		 * {@link ISearchPatternProcessor#TYPE_SEPARATOR}
 		 * 
 		 * @return
 		 */
		String getQualification();
 
 		/**
 		 * Returns the simple type name as chars, not <code>null</code>
 		 * 
 		 * @return
 		 */
 		char[] simpleName();
 
 		/**
 		 * Returns the simple type name as String, not <code>null</code>
 		 * 
 		 * @return
 		 */
 		String getSimpleName();
 	}
 
 	/**
 	 * Returns the parsed type pattern. MUST NOT return <code>null</code>
 	 * 
 	 * @param patternString
 	 *            search pattern string, not <code>null</code>
 	 * @return
 	 */
 	ITypePattern parseType(String patternString);
 
 	/**
 	 * Delimiter replacement string. For example "::" for tcl, "." for python.
 	 * 
 	 * The returned value is used to replace internal class name separator '$'.
 	 * It is used to check the match of the method calls like
 	 * "ClassName::methodName()".
 	 */
 	String getDelimiterReplacementString();
 
 	/**
 	 * Extracts the "package" from the specified method search pattern. If the
 	 * pattern have no type the method should return <code>null</code>.
 	 * 
 	 * @param patternString
 	 * @return
 	 */
 	char[] extractDeclaringTypeQualification(String patternString);
 
 	/**
 	 * Extracts the "base" class name from the specified method search pattern.
 	 * If the pattern have no type the method should return <code>null</code>.
 	 * 
 	 * @param patternString
 	 * @return
 	 */
 	char[] extractDeclaringTypeSimpleName(String patternString);
 
 	/**
 	 * Extracts the method name from the specified method search pattern. If the
 	 * pattern have no type - just return it as is.
 	 * 
 	 * @param patternString
 	 * @return
 	 */
 	char[] extractSelector(String patternString);
 
 	/**
 	 * Extracts the "package" from the specified full type name. If the
 	 * specified pattern have no package - the method should return
 	 * <code>null</code>.
 	 * 
 	 * @param patternString
 	 * @return
 	 */
 	@Deprecated
 	char[] extractTypeQualification(String patternString);
 
 	/**
 	 * Extracts the "base" class name from the specified full type name. If the
 	 * specified pattern have no package - it should be returned as is.
 	 * 
 	 * @param patternString
 	 * @return
 	 */
 	@Deprecated
 	String extractTypeChars(String patternString);
 
 }
