 /*
  *  Copyright Mariusz Jakubowski 2012
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); 
  * you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at:
  * 
  * http://www.apache.org/licenses/LICENSE-2.0 
  * 
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an "AS IS" BASIS, 
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
  * implied. See the License for the specific language governing 
  * permissions and limitations under the License.
  */
 
 package org.openntf.xsp.annotations;
 
 import java.lang.annotation.ElementType;
 import java.lang.annotation.Target;
 
 
 /**
  * Defines a XPage component
  * @author Mariusz Jakubowski
  *
  */
 @Target(ElementType.TYPE)
 public @interface XspComponent {
 	/**
 	 * Description of a component.
 	 */
 	String description();
 	
 	/**
 	 * Name of a component.
 	 */
 	String displayName();
 	
 	/**
 	 * Path to small icon.
 	 */
 	String smallIcon() default "";
 
 	/**
 	 * Path to large icon.
 	 */
 	String largeIcon() default "";
 	
 	/**
 	 * Tag used in xpage.
 	 */
 	String tagName();
 	
 	/**
 	 * Type of a component. Defaults to the class name. 
 	 */
 	String componentType() default "";
 
 	/**
 	 * Type of a base component. 
 	 */
 	String baseComponentType();
 	
 	/**
 	 * Component family. 
 	 */
	String componentFamily();
 	
 	/**
 	 * Symbol of a renderer. Defaults to the class name.
 	 */
 	String renderer() default "";
 	
 	/**
 	 * Show in palette.
 	 */
 	boolean inPalette() default true;
 	
 	/**
 	 * Category in palette.
 	 */
 	String paletteCategory() default "";
 	
 	/**
 	 * Preview markup.
 	 */
 	String markup() default "";
 	
 	/**
 	 * Flag indicating that a source code for a component should not be generated.
 	 */
 	boolean generateComponentCode() default true;
 	
 }
