 /*******************************************************************************
  * Copyright (c) 2007, 2011 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.core.injector.extension;
 
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 
 /**
  * Marks a <i>creator</i> method so that the returned instance is a proxy
 * instead of the real object. The real object will be created lazily on demand,
 * i.e. its first usage.<br>
 * The first usage can also be generated while debugging because the debugger
 * calls toString() on it!! This might result in undesired side effects.
 * <p>
  * <b>Note:</b> This requires that the executable extension object implements an
 * interface that is compatible to the return type of the <i>create</i> method.<br>
  * 
  * <pre>
  * &#064;CreateLazy()
  * ISomething createSomething();
  * </pre>
  * 
  */
 @Retention(RetentionPolicy.RUNTIME)
 @Target(ElementType.METHOD)
 public @interface CreateLazy {
 }
