 /*******************************************************************************
  * Copyright (c) 2012 VMware Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   VMware Inc. - initial contribution
  *******************************************************************************/
 
 package org.eclipse.virgo.management.console.stubs.types;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.virgo.management.console.stubs.objects.Util;
 
 import sun.org.mozilla.javascript.internal.Context;
 import sun.org.mozilla.javascript.internal.Function;
 import sun.org.mozilla.javascript.internal.ScriptableObject;
 
 /**
  */
 public class Element extends ParentStub {
     
     private static final long serialVersionUID = 1L;
 
     private static List<String> CONSTRUCTOR_ARGUMENT_TRACE = new ArrayList<String>();
 
     private static Element LAST_REPLACEMENT;
 
 	private static Element LAST_APPENDED;
 
 	private static Function READY;
     
     private final String constructorArgument;
 	
     private List<String> CLASSES = new ArrayList<String>();
     
     private Map<String, String> CSS = new HashMap<String, String>();
 
 	private boolean isSubmitted;
 	
 	private static int CLICK_COUNT = 0;
 
     private Element contentDocument;
     
     /**
      * Prototype constructor
      */
     public Element() {
         this.constructorArgument = null;
     }
     
     /**
      * JavaScript Constructor
      */
     public Element(ScriptableObject constructorArgument) {
         this.constructorArgument = ((String) Context.jsToJava(constructorArgument, String.class));
         CONSTRUCTOR_ARGUMENT_TRACE.add(this.constructorArgument);
     }
     
     public void jsFunction_empty(){
     }
 
 	public void jsFunction_ready(Function readyFunction){
 		READY = readyFunction;
 	}
     
     public ScriptableObject jsFunction_replaceWith(Element replacement){
         LAST_REPLACEMENT = replacement;
         return this;
     }
     
     public ScriptableObject jsFunction_append(Element toAppend){
         LAST_APPENDED = toAppend;
         return this;
     }
 
     public ScriptableObject jsFunction_addClass(String newClass){
     	CLASSES.add(newClass);
         return this;
     }
     
 	public boolean jsFunction_hasClass(String className){
 		return CLASSES.contains(className);
 	}
 
 	public ScriptableObject jsFunction_removeClass(String className) {
 		CLASSES.remove(className);
         return this;
 	}
 	
     public ScriptableObject jsFunction_text(String text) {
         return this;
     }
 	
     public void jsFunction_attr(String attr) {
     }
     
     public void jsFunction_prop(String prop){
     }
    
    public void jsFunction_data(String name, String value){
    }
 
     public void jsFunction_click() {
     	CLICK_COUNT++;
     }
 
     public void jsFunction_load() {
     }
 
     public void jsFunction_submit() {
     	this.isSubmitted = true;
     }
     
     public ScriptableObject jsFunction_children(String filter) {
     	return this;
     }
 
 	public ScriptableObject jsFunction_css(String key, String value) {
 		CSS.put(key, value);
 		return this;
 	}
 	
 	public void jsFunction_slideToggle(int time){
 		assertEquals(Util.fxTime, time);
 	}
 	
 	public Element jsFunction_contentDocument(){
 	    return this.contentDocument;
 	}
     
     // Test helper methods
 	
 	public static Function getReadyFunction(){
 		return READY;
 	}
 	
 	public List<String> getClasses(){
 		return CLASSES;
 	}
     
 	public static List<String> getConstructorArgumentTrace() {
 		return CONSTRUCTOR_ARGUMENT_TRACE;
 	}
    
 	public static Element getLastReplacement() {
 		return LAST_REPLACEMENT;
 	}
    
 	public static Element getLastAppended() {
 		return LAST_APPENDED;
 	}
 
 	public String getConstructorArgument() {
 		return this.constructorArgument;
 	}
 	
 	public boolean isSubmitted() {
 		return this.isSubmitted;
 	}
 	
 	public static int isClicked() {
 		return CLICK_COUNT;
 	}
 	
 	public static void resetClicked() {
 		CLICK_COUNT = 0;
 	}
 	
 	public void setContentDocument(Element contentDocument){
 	    this.contentDocument = contentDocument;
 	}
 	
 }
