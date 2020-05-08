 package org.stjs.javascript.jquery;
 
 import org.stjs.javascript.XMLHttpRequest;
 import org.stjs.javascript.annotation.SyntheticType;
 
 @SyntheticType
 public abstract class JQueryXHR extends XMLHttpRequest {
	public String responseText;
	public String responseXML;
 
 	@Override
 	abstract public String getResponseHeader(String header);
 
 	abstract public void overrideMimeType(String type);
 }
