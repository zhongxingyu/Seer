 package com.github.neothemachine.glgegwt.client;
 
public final class OpenCTM extends GObject {
 
 	protected OpenCTM() {}
 	
 	public static native OpenCTM create() /*-{
 		return new $wnd.GLGE.OpenCTM();
 	}-*/;
 	
 	public native void setSrc(String url, String relativeTo) /*-{
 		this.setSrc(url, relativeTo);
 	}-*/;
 	
 }
