 /*******************************************************************************
  * Copyright (c) 2010 Oobium, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
  ******************************************************************************/
 package org.oobium.persist;
 
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 
 import org.oobium.utils.json.JsonUtils;
 
 @Retention(RetentionPolicy.RUNTIME)
 @Target(ElementType.TYPE)
 public @interface Attribute {
 
 	String check() default "";
 	
 	boolean indexed() default false;
 
 	/**
	 * <p>Initialize the value of this field when instantiating the Java class.<br/>
 	 * The value can be any valid Java code that will result in the correct type
 	 * for the field, but should not be null.</p>
 	 * <p>Has no affect on the database schema.</p>
 	 */
 	String init() default "";
 	
 	/**
	 * The name of the field in the Java class.
 	 * @return the name of this attribute
 	 */
 	String name();
 
 	/**
 	 * <p>Sets whether or not this field is required.  Required fields are not allowed to be null.</p>
 	 * <p>Default is false.</p>
 	 * @return true if this field is required, false otherwise
 	 */
 	boolean required() default false;
 	
 	/**
 	 * Only of interest for type BigDecimal
 	 * @return the precision
 	 */
 	int precision() default 8;
 	
 	/**
 	 * Set whether or not the field can be set through a public setter method.
 	 * If readOnly is set to true then the public setter method will not be generated.
 	 * The field will still exist and (like other fields) is protected, so there are
 	 * other ways that it can be modified.
 	 * <p>
 	 * Default is false.
 	 * </p>
 	 * @return true if this field is read only, false otherwise.
 	 */
 	boolean readOnly() default false;
 
 	/**
 	 * Only of interest for type BigDecimal
 	 * @return the scale
 	 */
 	int scale() default 2;
 	
 	/**
 	 * The runtime type of this attribute is also the type of the field in the java class.
 	 * @return the runtime type of this attribute
 	 */
 	Class<?> type();
 
 	/**
 	 * <p>Set whether or not the value of the field must be unique for all instances of this model.</p>
 	 * <p>Default is false.</p>
 	 * <p><b>Note:</b> if unique is true, then the field must also be declared as required</p>
 	 * @return the uniqueness of the attribute for this attribute
 	 * @see #required()
 	 */
 	boolean unique() default false;
 
 	/**
 	 * an array of validators... not yet implemented...
 	 * @return
 	 */
 	Class<?>[] validators() default {};
 
 	/**
 	 * not currently implemented...
 	 * @return
 	 */
 	boolean virtual() default false;
 
 }
