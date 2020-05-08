 /*******************************************************************************
  * Copyright (c) 2008, 2011 VMware Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   VMware Inc. - initial contribution
  *******************************************************************************/
 package org.eclipse.virgo.management.console.stubs.objects;
 
 import sun.org.mozilla.javascript.internal.Context;
 import sun.org.mozilla.javascript.internal.Function;
 import sun.org.mozilla.javascript.internal.Scriptable;
 import sun.org.mozilla.javascript.internal.ScriptableObject;
 
 /**
  * 
  *
  */
 public class Util {
 
     private final Context CONTEXT;
     
 	private final ScriptableObject SCOPE;
 	
 	private boolean pageReady = false;
 
     private String[] lastMakeTableRows = null;
 
     private String[] lastMakeTableHeaders = null;
 
     private Scriptable lastMakeTableTable = null;
 
     private String lastQuery = null;
 
     private Function lastQueryCallBack = null;
 
     private Function lastQueryErrorCallBack = null;
 
     private Scriptable lastBulkQuery = null;
 
     private Function lastBulkQueryCallBack = null;
 
     private Function lastBulkQueryErrorCallBack = null;
 
 	private Function async;
 
 	private String lastScriptLoaded;
 
 	public static int fxTime = 200;
 	
 	public Util(Context context, ScriptableObject SCOPE) {
 	    this.CONTEXT = context;
 		this.SCOPE = SCOPE;
 	}
 	
 	// Stub methods
 
 	public void pageReady(){
 		this.pageReady = true;
 	}
 	
	public Object getCurrentHost(){
 		return Context.javaToJS("hostPrefix", SCOPE);
 	}
 	
 	public Object readObjectName(String mbeanName){
 		return new MBean(mbeanName);	
 	}
 	
 	public void loadScript(Object scriptName, Object async){
 		this.lastScriptLoaded = (String) scriptName;
 		this.async = (Function) async;
 	}
 	
 	public Object makeTable(Scriptable properties) {
 		Object[] headers = (Object[]) Context.jsToJava(ScriptableObject.getProperty(properties, "headers"), Object[].class);
 	    this.lastMakeTableHeaders = new String[headers.length]; 
 	    for (int i = 0; i < headers.length; i++) {
 			this.lastMakeTableHeaders[i] = (String) Context.jsToJava(ScriptableObject.getProperty((Scriptable) headers[i], "title"), String.class);
 		}
 	    
 	    this.lastMakeTableRows = (String[]) Context.jsToJava(ScriptableObject.getProperty(properties, "rows"), String[].class);
 
 	    Function elementConstructor = (Function) SCOPE.get("Element", SCOPE);
         Object[] args = new Object[]{properties};
         Scriptable table = elementConstructor.construct(Context.getCurrentContext(), SCOPE, args);
         this.lastMakeTableTable = table;
         return table;
 	}
 	
 	public Object makeDiv(String clazz) {
 	    Function elementConstructor = (Function) SCOPE.get("Element", SCOPE);
         Object[] args = new Object[]{clazz};
         return elementConstructor.construct(this.CONTEXT, SCOPE, args);
 	}
 	
 	public void doQuery(String query, Function callBack) {
 	    this.lastQuery = query;
 	    this.lastQueryCallBack = callBack;
 	//    this.lastQueryErrorCallBack = errorCallBack;
 	}
 	
 	public void doBulkQuery(Scriptable query, Function callBack, Function errorCallBack) {
 	    this.lastBulkQuery = query;
 	    this.lastBulkQueryCallBack = callBack;
 	    this.lastBulkQueryErrorCallBack = errorCallBack;
 	}
 	
 	// Test methods
 	
 	public void clean(){
 		this.pageReady = false;
 		this.lastMakeTableRows = null;
 		this.lastMakeTableHeaders = null;
 		this.lastMakeTableTable = null;
 		this.lastQuery = null;
 		this.lastQueryCallBack = null;
 		this.lastQueryErrorCallBack = null;
 		this.lastBulkQuery = null;
 		this.lastBulkQueryCallBack = null;
 		this.lastBulkQueryErrorCallBack = null;
 	}
 	
 	public boolean isPageReady(){
 		return pageReady;
 	}
 
     public Function getLoadScriptAsync() {
         return this.async;
     }
     
 	public String[] getLastMakeTableHeaders() {
 	    return this.lastMakeTableHeaders;
 	}
 	
 	public String[] getLastMakeTableRows() {
 	    return this.lastMakeTableRows;
 	}
 	
 	public Scriptable getLastMakeTableTable() {
 	    return this.lastMakeTableTable;
 	}
 
     public String getLastQuery() {
         return lastQuery;
     }
 
     public Function getLastQueryCallBack() {
         return lastQueryCallBack;
     }
 
     public Function getLastQueryErrorCallBack() {
         return lastQueryErrorCallBack;
     }
 
     public Scriptable getLastBulkQuery() {
         return lastBulkQuery;
     }
 
     public Function getLastBulkQueryCallBack() {
         return lastBulkQueryCallBack;
     }
 
     public Function getLastBulkQueryErrorCallBack() {
         return lastBulkQueryErrorCallBack;
     }
 
     public String getLastScriptLoaded() {
         return lastScriptLoaded;
     }
 	
 }
