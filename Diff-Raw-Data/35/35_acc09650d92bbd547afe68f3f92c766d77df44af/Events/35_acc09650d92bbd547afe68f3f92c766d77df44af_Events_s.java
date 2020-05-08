 package com.github.neothemachine.glgegwt.client;
 
 import com.google.gwt.core.client.JavaScriptObject;
 
 public class Events extends JavaScriptObject {
 
 	protected Events() {}
 	
 	public final native void addEventListener(String event, Runnable listener) /*-{
 		var callbackFn = $entry(function() {
			listener.@java.lang.Runnable.run()();
 		});
 		this.addEventListener(event, callbackFn);
 	}-*/;
 }
