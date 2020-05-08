 /*******************************************************************************
  * Copyright (c) 2006-2012
  * Software Technology Group, Dresden University of Technology
  * DevBoost GmbH, Berlin, Amtsgericht Charlottenburg, HRB 140026
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *   Software Technology Group - TU Dresden, Germany;
  *   DevBoost GmbH - Berlin, Germany
  *      - initial API and implementation
  ******************************************************************************/
 package de.devboost.commenttemplate;
 
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

 
 /**
  * The {@link VariableAntiQuotation} annotation can be used to customize the
  * way variables are accessed from within template code (i.e., from within
  * comments in comment template method).
  * 
  * The value of an {@link VariableAntiQuotation} annotation is used as 
  * formatting pattern that is passed as first argument to 
  * {@link String#format(String, Object...)}. The second argument is the variable
  * name. For example, adding @VariableAntiQuotation("#%s#") tells the 
  * CommentTemplateCompiler to detect variable accesses that are enclosed
  * by the hash character.
  */
@Target({ElementType.METHOD, ElementType.TYPE})
 public @interface VariableAntiQuotation {
 
 	String value();
 }
