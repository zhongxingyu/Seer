 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.internal.core.search.matching;
 
 import org.eclipse.dltk.compiler.CharOperation;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.IType;
 import org.eclipse.dltk.core.search.SearchPattern;
 import org.eclipse.dltk.core.search.indexing.IIndexConstants;
 
 public class TypeReferencePattern extends AndPattern implements IIndexConstants {
 
 	protected char[] qualification;
 	protected char[] simpleName;
 
 	protected char[] currentCategory;
 
 	/* Optimization: case where simpleName == null */
 	public int segmentsSize;
 	protected char[][] segments;
 	protected int currentSegment;
 
 	protected static char[][] CATEGORIES = { REF };
 
 	public TypeReferencePattern(char[] qualification, char[] simpleName,
 			int matchRule, IDLTKLanguageToolkit toolkit) {
 		this(matchRule, toolkit);
 
 		this.qualification = isCaseSensitive() ? qualification : CharOperation
 				.toLowerCase(qualification);
 		this.simpleName = (isCaseSensitive() || isCamelCase()) ? simpleName
 				: CharOperation.toLowerCase(simpleName);
 
 		if (simpleName == null)
 			this.segments = this.qualification == null ? ONE_STAR_CHAR
 					: CharOperation.splitOn('.', this.qualification);
 		else
 			this.segments = null;
 
 		if (this.segments == null)
 			if (this.qualification == null)
 				this.segmentsSize = 0;
 			else
 				this.segmentsSize = CharOperation.occurencesOf('.',
 						this.qualification) + 1;
 		else
 			this.segmentsSize = this.segments.length;
 	}
 
 	/*
	 * Instantiate a type reference pattern with additional information for
	 * generic search
 	 */
 	public TypeReferencePattern(char[] qualification, char[] simpleName,
 			IType type, int matchRule, IDLTKLanguageToolkit toolkit) {
 		this(qualification, simpleName, matchRule, toolkit);
 	}
 
 	TypeReferencePattern(int matchRule, IDLTKLanguageToolkit toolkit) {
 		super(TYPE_REF_PATTERN, matchRule, toolkit);
 	}
 
 	public void decodeIndexKey(char[] key) {
 		this.simpleName = key;
 	}
 
 	public SearchPattern getBlankPattern() {
 		return new TypeReferencePattern(R_EXACT_MATCH | R_CASE_SENSITIVE,
 				getToolkit());
 	}
 
 	public char[] getIndexKey() {
 		if (this.simpleName != null)
 			return this.simpleName;
 
 		if (this.currentSegment >= 0)
 			return this.segments[this.currentSegment];
 		return null;
 	}
 
 	public char[][] getIndexCategories() {
 		return CATEGORIES;
 	}
 
 	protected boolean hasNextQuery() {
 		if (this.segments == null)
 			return false;
 
 		// Optimization, eg. type reference is 'org.eclipse.dltk.core.*'
 		// if package has at least 4 segments, don't look at the first 2 since
 		// they are mostly
 		// redundant (eg. in 'org.eclipse.dltk.core.*' 'org.eclipse' is used all
 		// the time)
 		return --this.currentSegment >= (this.segments.length >= 4 ? 2 : 0);
 	}
 
 	public boolean matchesDecodedKey(SearchPattern decodedPattern) {
 		return true; // index key is not encoded so query results all match
 	}
 
 	protected void resetQuery() {
 		/*
 		 * walk the segments from end to start as it will find less potential
 		 * references using 'lang' than 'java'
 		 */
 		if (this.segments != null)
 			this.currentSegment = this.segments.length - 1;
 	}
 
 	protected StringBuffer print(StringBuffer output) {
 		output.append("TypeReferencePattern: qualification<"); //$NON-NLS-1$
 		if (qualification != null)
 			output.append(qualification);
 		else
 			output.append("*"); //$NON-NLS-1$
 		output.append(">, type<"); //$NON-NLS-1$
 		if (simpleName != null)
 			output.append(simpleName);
 		else
 			output.append("*"); //$NON-NLS-1$
 		output.append(">"); //$NON-NLS-1$
 		return super.print(output);
 	}
 }
