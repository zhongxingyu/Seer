 /*
  * Copyright (c) 2005, 2008 Sven Efftinge and others.
  * All rights reserved.   This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sven Efftinge - Initial API and implementation
  *     Artem Tikhomirov (Borland) - Migration to OCL expressions
  */
 package org.eclipse.gmf.internal.xpand.model;
 
 import org.eclipse.gmf.internal.xpand.expression.ast.SyntaxElement;
 import org.eclipse.gmf.internal.xpand.ocl.ExpressionHelper;
 
 /**
  * FIXME: refactor - hide enum type into factory method, check all types are still in use, get rid of isWarningNotError
  * @author Sven Efftinge
  * @author Arno Haase
  */
 public class AnalysationIssue {
 
     public enum Type {
         INCOMPATIBLE_TYPES("Incompatible types"),
         UNNECESSARY_CAST("Unnecessary cast"),
         FEATURE_NOT_FOUND("Callable not found"),
         TYPE_NOT_FOUND ("Type not found"),
         INTERNAL_ERROR("Internal error"),
         JAVA_TYPE_NOT_FOUND("Java Type not found"),
         SYNTAX_ERROR("Syntax error"),
        DEFINITION_NOT_FOUND("Definition not found"),
        EXTENSION_NOT_FOUND("Extension not found"),
        NAMESPACE_NOT_FOUND("Namespace not found");
 
         private String name;
 
         private Type(final String name) {
             this.name = name;
         }
 
         @Override
         public String toString() {
             return name;
         }
     }
 
     private final Type type;
 
     private final String message;
 
     private final boolean isWarningNotError;
 
 	private final int start;
 
 	private final int end;
 
 	private final int line;
 
     public AnalysationIssue(final Type type, final String message, final SyntaxElement element) {
     	this(type, message, element.getStart(), element.getEnd(), element.getLine(), false);
     }
 
     public AnalysationIssue(final Type type, final String message, final ExpressionHelper exprHelper) {
     	this(type, message, exprHelper.getStart(), exprHelper.getEnd(), -1, false);
     }
 
     private AnalysationIssue(final Type type, final String message, final int start, int end, int line, boolean isWarningNotError) {
         this.type = type;
         this.message = message;
         this.isWarningNotError = isWarningNotError;
         this.start = start;
         this.end = end;
         this.line = line;
     }
 
     public boolean isWarningNotError() {
     	return isWarningNotError;
     }
 
     public String getMessage() {
         return message;
     }
 
     public Type getType() {
         return type;
     }
     
     public int getStart() {
     	return start;
     }
     
     public int getEnd() {
     	return end;
     }
 
     
     public int getLine() {
     	return line;
     }
 
     @Override
     public String toString() {
         return "[" + type.name + "] - " + message;
     }
 
 }
