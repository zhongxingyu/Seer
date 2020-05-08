 package com.github.neothemachine.glgegwt.client;
 
import com.google.gwt.core.client.JavaScriptObject;

public final class OpenCTM extends JavaScriptObject {
 
 	protected OpenCTM() {}
 	
 	public static native OpenCTM create() /*-{
 		return new $wnd.GLGE.OpenCTM();
 	}-*/;
 	
 	public native void setSrc(String url, String relativeTo) /*-{
 		this.setSrc(url, relativeTo);
 	}-*/;
 	
 }
