 package com.github.neothemachine.glgegwt.client;
 
public final class Wavefront extends Object {
 
 	protected Wavefront() {}
 	
 	public static native Wavefront create() /*-{
 		return new $wnd.GLGE.Wavefront();
 	}-*/;
 	
 	public native void setSrc(String url, String relativeTo) /*-{
 		this.setSrc(url, relativeTo);
 	}-*/;
 	
 }
