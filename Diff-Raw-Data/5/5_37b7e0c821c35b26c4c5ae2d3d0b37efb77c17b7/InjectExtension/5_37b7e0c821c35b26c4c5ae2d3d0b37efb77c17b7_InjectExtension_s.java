 /*******************************************************************************
  * Copyright (c) 2007, 2009 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.core.wire;
 
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 
 /**
  * Annotate a update method for extension injection.
  */
 @Retention(RetentionPolicy.RUNTIME)
 @Target(ElementType.METHOD)
 public @interface InjectExtension {
 
 	/**
 	 * The extension point id.
 	 * <p>
 	 * <b>Note: </b>If not given, it is expected that the {@code
	 * ExtensionInterface} contains a {@code String } field named {@code ID}
	 * that contains the extension point id.
 	 */
 	String id() default "";
 
 	/**
 	 * The minimum expected numbers of extensions.
 	 */
 	int min() default 0;
 
 	/**
 	 * The maximum expected numbers of extensions.
 	 */
 	int max() default Integer.MAX_VALUE;
 
 	/**
 	 * Is this a heterogeneous extension
 	 */
 	boolean heterogeneous() default false;
 
 	/**
 	 * Is this a specific extension
 	 */
 	boolean specific() default false;
 
 	/**
 	 * If {@code true} symbols (VariablManager) will not be replaced
 	 */
 	boolean doNotReplaceSymbols() default false;
 }
