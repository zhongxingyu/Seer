 package com.github.neothemachine.glgegwt.client;
 
 import com.google.gwt.core.client.JavaScriptObject;
 
 public final class MaterialLayer extends JavaScriptObject {
 	
 	/**
 	* Enumeration for first UV layer
 	*/
 	public static final int UV1=0;
 	/**
 	* Enumeration for second UV layer
 	*/
 	public static final int UV2=1;
 	
 	/**
 	* Flag for material colour
 	*/
 	public static final int M_COLOR=1;
 	/**
 	* Flag for material normal
 	*/
 	public static final int M_NOR=2;
 	/**
 	* Flag for material alpha
 	*/
 	public static final int M_ALPHA=4;
 	/**
 	* Flag for material specular color
 	*/
 	public static final int M_SPECCOLOR=8;
 	/**
 	* Flag for material specular cvalue
 	*/
 	public static final int M_SPECULAR=16;
 	/**
 	* Flag for material shineiness
 	*/
 	public static final int M_SHINE=32;
 	/**
 	* Flag for material reflectivity
 	*/
 	public static final int M_REFLECT=64;
 	/**
 	* Flag for material emision
 	*/
 	public static final int M_EMIT=128;
 	/**
 	* Flag for material alpha
 	*/
 	//public static final int M_ALPHA=256;
 	/**
 	* Flag for masking with textures red value
 	*/
 	public static final int M_MSKR=512;
 	/**
 	* Flag for masking with textures green value
 	*/
 	public static final int M_MSKG=1024;
 	/**
 	* Flag for masking with textures blue value
 	*/
 	public static final int M_MSKB=2048;
 	/**
 	* Flag for masking with textures alpha value
 	*/
 	public static final int M_MSKA=4096;
 	/**
 	* Flag for mapping of the height in parallax mapping
 	*/
 	public static final int M_HEIGHT=8192;
 
 	/**
 	* Flag for ambient mapping
 	*/
 	public static final int M_AMBIENT=16384;
 
 	/**
 	* Flag for Steep parallax mapng
 	*/
 	public static final int M_STEEP=32768;
 
 
 	protected MaterialLayer() {}
 	
 	public static native MaterialLayer create() /*-{
 		return new $wnd.GLGE.MaterialLayer();
 	}-*/;
 	
	public native void setMapinput(int input) /*-{
		this.setMapinput(input);
 	}-*/;
 	
 	public native void setMapto(int to) /*-{
 		this.setMapto(to);
 	}-*/;
 	
 	public native void setTexture(TextureCanvas texture) /*-{
 		this.setTexture(texture);
 	}-*/;
 	
 }
