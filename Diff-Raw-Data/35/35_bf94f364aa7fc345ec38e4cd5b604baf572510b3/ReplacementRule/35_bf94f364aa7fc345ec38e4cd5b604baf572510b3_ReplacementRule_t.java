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
  * This annotation can be added to methods that are annotated with the 
  * CommentTemplate annotation. It can also be added to classes which means that
  * the rule applied to all methods in the class.
  * 
  * Replacement rules are considered by the CommentTemplateCompiler and used to
  * replace text in the templates. This can be either use to create shortcuts for
  * long text fragment that appear frequently or to allow the embedding of Java
  * comments in templates.
  */
@Target({ElementType.METHOD, ElementType.TYPE})
 public @interface ReplacementRule {
 
 	public String pattern();
 	public String replacement();
 }
