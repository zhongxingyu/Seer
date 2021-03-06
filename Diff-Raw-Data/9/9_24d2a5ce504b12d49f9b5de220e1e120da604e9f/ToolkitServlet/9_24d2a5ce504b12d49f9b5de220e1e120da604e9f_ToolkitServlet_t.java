 /*
  *  Copyright IBM Corp. 2012
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
 
 package com.ibm.xsp.sbtsdk.servlets;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.ibm.commons.runtime.RuntimeConstants;
 import com.ibm.commons.runtime.util.UrlUtil;
 import com.ibm.sbt.jslibrary.SBTEnvironment;
 import com.ibm.sbt.jslibrary.servlet.LibraryRequest;
 import com.ibm.sbt.jslibrary.servlet.LibraryServlet;
 import com.ibm.xsp.extlib.resources.ExtlibResourceProvider;
 
 public class ToolkitServlet extends LibraryServlet {
 
 	private static final long serialVersionUID = 1L;
 
 	public static class DominoLibraryRequest extends LibraryRequest {
 		public DominoLibraryRequest(HttpServletRequest req, HttpServletResponse resp) {
 			super(req, resp);
 		}
	    public void init(SBTEnvironment defaultEnvironment, String toolkitUrl, String toolkitJsUrl, String serviceUrl, String iframeUrl, String toolkitExtUrl, String toolkitExtJsUrl) throws ServletException, IOException {
			//public void init(SBTEnvironment defaultEnvironment, String toolkitUrl, String toolkitJsUrl, String proxyUrl, String iframeUrl) throws ServletException, IOException {
 			// Calculate the toolkit URL
 	    	//http://priand2/xsp/.ibmxspres/.extlib/sbt/Cache.js
 			toolkitUrl =UrlUtil.getServerUrl(getHttpRequest())+"/xsp"+ExtlibResourceProvider.RESOURCE_PATH;
 			toolkitJsUrl = toolkitUrl;
 			
 			// Calculate the proxy URL
			serviceUrl = RuntimeConstants.get().getBaseProxyUrl(getHttpRequest());
 			
			super.init(defaultEnvironment, toolkitUrl, toolkitJsUrl, serviceUrl, iframeUrl, toolkitExtUrl, toolkitExtJsUrl);
 		}
 	}
 	
 	protected LibraryRequest createLibraryRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 		return new DominoLibraryRequest(req, resp);
 	}
 }
