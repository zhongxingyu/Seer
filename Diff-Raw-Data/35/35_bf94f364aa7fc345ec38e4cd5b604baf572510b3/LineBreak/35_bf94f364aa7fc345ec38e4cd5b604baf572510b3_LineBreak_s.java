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
 
 
 /**
  * The {@link LineBreak} annotation can be used to configure which kind of line
  * delimiter is used by the CommentTemplateCompiler. By default, a 
  * single new line character is used, but this behavior can be customized by
  * adding {@link LineBreak} annotations to methods with the 
  * {@link CommentTemplate} annotation.
  */
 public @interface LineBreak {
 
 	String value();
 }
