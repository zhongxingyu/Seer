 /*
  * Copyright 2004-2012 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 
 package org.icefaces.ace.component.dataexporter;
 
 import javax.faces.application.Resource;
 import java.io.InputStream;
 import java.io.ByteArrayInputStream;
 import java.util.Map;
 import java.util.HashMap;
 import java.net.URL;
 import javax.faces.context.FacesContext;
 	
 public class ExporterResource extends Resource implements java.io.Serializable {
 
 	private InputStream in;
 	private String path = "";
 	private HashMap<String, String> headers;
 	private byte[] bytes;
 	
 	public ExporterResource(InputStream in) {
 		this.in = in;
 		this.headers = new HashMap<String, String>();
 	}
 	
 	public ExporterResource(byte[] bytes) {
 		this.bytes = bytes;
 		this.headers = new HashMap<String, String>();
 	}
 	
 	public InputStream getInputStream() {
		//return in;
		return new ByteArrayInputStream(bytes);
 	}
 
 	public String getRequestPath() {
 		return path;
 	}
 	
 	public void setRequestPath(String path) {
 		this.path = path;
 	}
 
 	public Map<String, String> getResponseHeaders() {
 		return headers;
 	}
 
 	public URL	getURL() {
 		return null;
 	}
 
 	public boolean userAgentNeedsUpdate(FacesContext context) {
 		return false;
 	}
 }
