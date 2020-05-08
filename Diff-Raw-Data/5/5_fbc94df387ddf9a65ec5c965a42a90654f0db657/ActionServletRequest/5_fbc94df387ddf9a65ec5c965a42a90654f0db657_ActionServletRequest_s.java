 /*
  * Copyright (c) 2003, Rafael Steil
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, 
  * with or without modification, are permitted provided 
  * that the following conditions are met:
  * 
  * 1) Redistributions of source code must retain the above 
  * copyright notice, this list of conditions and the 
  * following  disclaimer.
  * 2)  Redistributions in binary form must reproduce the 
  * above copyright notice, this list of conditions and 
  * the following disclaimer in the documentation and/or 
  * other materials provided with the distribution.
  * 3) Neither the name of "Rafael Steil" nor 
  * the names of its contributors may be used to endorse 
  * or promote products derived from this software without 
  * specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT 
  * HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, 
  * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
  * MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL 
  * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
  * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
  * IN CONTRACT, STRICT LIABILITY, OR TORT 
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN 
  * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE
  * 
  * This file creation date: Mar 16, 2003 / 1:31:30 AM
  * The JForum Project
  * http://www.jforum.net
  */
 package net.jforum;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletRequestWrapper;
 
 import net.jforum.exceptions.InvalidURLPatternException;
 import net.jforum.exceptions.MultipartHandlingException;
 import net.jforum.util.legacy.commons.fileupload.FileItem;
 import net.jforum.util.legacy.commons.fileupload.FileUploadException;
 import net.jforum.util.legacy.commons.fileupload.disk.DiskFileItemFactory;
 import net.jforum.util.legacy.commons.fileupload.servlet.ServletFileUpload;
 import net.jforum.util.legacy.commons.fileupload.servlet.ServletRequestContext;
 import net.jforum.util.preferences.ConfigKeys;
 import net.jforum.util.preferences.SystemGlobals;
 
 /**
  * @author Rafael Steil
 * @version $Id: ActionServletRequest.java,v 1.22 2005/06/13 19:26:35 rafaelsteil Exp $
  */
 public class ActionServletRequest extends HttpServletRequestWrapper 
 {
 	/**
 	 * URL Patterns keeper.
 	 * Represents a single URL pattern. Each pattern is composed
 	 * by a name, the pattern itself, the pattern's size and the
 	 * splited variables. <br><br>
 	 * 
 	 * The pattern is expected in the form <i>var1, var2, varN</i>, in the
 	 * correct order. This means that if <i>var1</i> comes first, it <b>must</b>
 	 * come first in the URL. The same is valid to others.<br><br>
 	 * 
 	 * Please note that "first" here is "first" after regular URL, which is
 	 * composed by server and servlet name, in the most simple case.<br><br>
 	 * 
 	 * <b>Example:</b><br>
 	 * 
 	 * URL: <i>http://localhost:8080/webappName/someDir/myServlet/news/view/3.page<i>.
 	 * <br>
 	 * In this case, <i>http://localhost:8080/webappName/someDir/myServlet/</i> is the 
 	 * regular URL, the part that we don't care about. We only want the part 
 	 * <i>news/view/3.page</i> ( where .page is the servlet extension ). 
 	 * <br>For this URL, we could make the following pattern:<br><br>
 	 * 
 	 * <i>news.view.1 = news_id</i><br><br>
 	 * 
 	 * Here, <i>news.view.1</i> is the pattern's name, and <i>news_id</i> is
 	 * the patterns itself. <br>
 	 * Another example:<br><br>
 	 * 
 	 * <i>news.view.2 = page, news_id</i><br><br>
 	 *  
 	 * In this case we have a new var called <i>page</i>, that represents the page being seen.<br>
 	 * Each entry is composed in the form:<br><br>
 	 * 
 	 * <i>&lt;moduleName&gt;.&lt;actionName&gt;.&lt;numberOfParameters&gt; = &lt;var 1&gt;,&lt;var n&gt;</i>
 	 * <br><br>
 	 * 
 	 * Please note that module and action's name aren't pattern's composition, so 
 	 * don't put them there. The system will consider that the pattern only contains
 	 * the variables diferent to each request ( e.g, id's ). If the pattern you're
 	 * constructing doesn't have any variable, just leave it blank, like<br><br>
 	 * 
 	 * <i>myModule.myAction.0 = </i><br><br>
 	 * 
 	 * @author Rafael Steil
 	 */
 	private static class UrlPattern
 	{
 		private String name;
 		private String value;
 		private int size;
 		private String[] vars;
 		
 		public UrlPattern(String name, String value)
 		{
 			this.name = name;
 			this.value = value;
 			
 			this.processPattern();
 		}
 		
 		private void processPattern()
 		{
 			String[] p = this.value.split(",");
 			
 			this.vars = new String[p.length];
 			this.size = ((((p[0]).trim()).equals("")) ? 0 : p.length);
 			
 			for (int i = 0; i < this.size; i++) {
 				this.vars[i] = (p[i]).trim();
 			}
 		}
 
 		/**
 		 * Gets the pattern name
 		 * 
 		 * @return String with the pattern name
 		 */
 		public String getName()
 		{
 			return this.name;
 		}
 
 		/**
 		 * Get pattern's total vars
 		 * 
 		 * @return The total
 		 */
 		public int getSize()
 		{
 			return this.size;
 		}
 		
 		/**
 		 * Gets the vars.
 		 * The URL variables are in the correct order, which means
 		 * that the first position always will be "something1", the
 		 * second "something2" and so on. The system expects this
 		 * order never changes from requisition to requisition.
 		 * 
 		 * @return The vars
 		 */
 		public String[] getVars()
 		{
 			return this.vars;
 		}
 	}
 	
 	/**
 	 * Keeps a collection of <code>UrlPattern</code> objects.
 	 *  
 	 * @author Rafael Steil
 	 */
 	private static class UrlPatternCollection
 	{
 		private static HashMap patternsMap = new HashMap();
 		
 		/**
 		 * Try to find a <code>UrlPattern</code> by its name.
 		 * 
 		 * @param name The pattern name
 		 * @return The <code>UrlPattern</code> object if a match was found, or <code>null</code> if not
 		 */
 		public static UrlPattern findPattern(String name)
 		{
 			return (UrlPattern)UrlPatternCollection.patternsMap.get(name);
 		}
 		
 		/**
 		 * Adds a new <code>UrlPattern</code>.
 		 * 
 		 * @param name The pattern name
 		 * @param value The pattern value
 		 */
 		public static void addPattern(String name, String value)
 		{
 			UrlPatternCollection.patternsMap.put(name, new UrlPattern(name, value));
 		}
 	}
 	
 	private Map query;
 	
 	/**
 	 * Default constructor.
 	 * 
 	 * @param superRequest Original <code>HttpServletRequest</code> instance
 	 * @throws IOException
 	 */
 	public ActionServletRequest(HttpServletRequest superRequest) throws IOException
 	{
 		super(superRequest);
 
 		this.query = new HashMap();
 		boolean isMultipart = false;
 		
 		String requestType = (superRequest.getMethod()).toUpperCase();
 		String requestUri = superRequest.getRequestURI();
 		
 		// Remove the "jsessionid" (or similar) from the URI
 		// Probably this is not the right way to go, since we're
 		// discarting the value...
 		int index = requestUri.indexOf(';');
 		
 		if (index > -1) {
 			int lastIndex = requestUri.indexOf('?', index);
 			
 			if (lastIndex == -1) {
 				lastIndex = requestUri.indexOf('&', index);
 			}
 			
 			if (lastIndex == -1) {
 				requestUri = requestUri.substring(0, index);
 			}
 			else {
 				String part1 = requestUri.substring(0, index);
 				requestUri = part1 + requestUri.substring(lastIndex);
 			}
 		}
 		
 		String encoding = SystemGlobals.getValue(ConfigKeys.ENCODING);
 		
 		if ((("GET").equals(requestType) && (superRequest.getQueryString() == null)) 
 				&& requestUri.endsWith(SystemGlobals.getValue(ConfigKeys.SERVLET_EXTENSION))) {
 			superRequest.setCharacterEncoding(SystemGlobals.getValue(ConfigKeys.ENCODING));  
 			String[] urlModel = requestUri.split("/");
 			
 			// If (context name is not null) {
 				// 0: empty
 				// 1: context name
 				// 2: module
 				// 3: action
 				// 4 .. n: request dependent data
 			// } else {
 				// 0: empty
 				// 1: module
 				// 2: action
 				// 3 .. n: request dependent data
 			// }
 			
 			int moduleIndex = 2;
 			int actionIndex = 3;
 			int baseLen = 4;
 			
 			String contextName = superRequest.getContextPath();
 			if ((contextName == null) || contextName.equals("")) {
 				moduleIndex = 1;
 				actionIndex = 2;
 				baseLen = 3;
 			}
 			
 			urlModel[urlModel.length - 1] = (urlModel[urlModel.length - 1]).substring(0, (urlModel[urlModel.length - 1]).indexOf(SystemGlobals.getValue(ConfigKeys.SERVLET_EXTENSION)));
 			
 			// <moduleName>.<actionName>.<numberOfParameters>
 			UrlPattern url = UrlPatternCollection.findPattern(urlModel[moduleIndex] 
 					+ "." 
 					+ urlModel[actionIndex] 
 					+ "." 
 					+ (urlModel.length - baseLen));
 
 			if (url == null) {
 				throw new InvalidURLPatternException("The request '" + superRequest.getRequestURI() 
 						+ "' is not valid. A correspondent URL Pattern was not found");
 			}
 			
 			this.addParameter("module", urlModel[moduleIndex]);
 			this.addParameter("action", urlModel[actionIndex]);
 			
 			// We have parameters? 
 			if (url.getSize() >= urlModel.length - baseLen) {
 				for (int i = 0; i < url.getSize(); i++) {
 					this.addParameter(url.getVars()[i], urlModel[i + baseLen]);
 				}
 			}
 		}
 		else if (("POST").equals(requestType)) {
 			isMultipart = ServletFileUpload.isMultipartContent(new ServletRequestContext(superRequest));
 			if (isMultipart) {
 			    String tmpDir = SystemGlobals.getApplicationPath() + "/" + SystemGlobals.getValue(ConfigKeys.TMP_DIR);
 				ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory(100 * 1024, new File(tmpDir)));
 
 				try {
 					List items = upload.parseRequest(superRequest);
 					for (Iterator iter = items.iterator(); iter.hasNext(); ) {
 						FileItem item = (FileItem)iter.next();
 						if (item.isFormField()) {
 							this.query.put(item.getFieldName(), item.getString(encoding));
 						}
 						else {
 							if (item.getSize() > 0) {
 								this.query.put(item.getFieldName(), item);
 							}
 						}
 					}
 				}
 				catch (FileUploadException e) {
 					throw new MultipartHandlingException("Error while processing multipart content: " + e);
 				}
 			}
 		}
 		
 		if (isMultipart == false) {
 			superRequest.setCharacterEncoding(encoding);
 			String containerEncoding = SystemGlobals.getValue(ConfigKeys.DEFAULT_CONTAINER_ENCODING);
			
 			for (Enumeration e = superRequest.getParameterNames(); e.hasMoreElements(); ) {
 				String name = (String)e.nextElement();
 				this.query.put(name, new String(superRequest.getParameter(name).getBytes(containerEncoding), encoding));
 			}
 		}
 	}
 
 	/**
 	 * @see javax.servlet.ServletRequest#getParameter(java.lang.String)
 	 */
 	public String getParameter(String parameter) 
 	{
 		return (String)this.query.get(parameter);
 	}
 
 	/**
 	 * Gets an parameter that is a number.
 	 * A call to <code>Integer#parseInt(String)</code> is made
 	 * to do the conversion
 	 * @param parameter The parameter name to get the value
 	 * @return
 	 */
 	public int getIntParameter(String parameter)
 	{
 		return Integer.parseInt(this.getParameter(parameter));
 	}
 	
 	/**
 	 * Gets all parameters of the current request. 
 	 * 
 	 * @return <code>java.util.Map</code> with all request
 	 * data.
 	 */
 	public Map dumpRequest()
 	{
 		return this.query;
 	}
 	
 	/**
 	 * Restores a request "dump".
 	 * 
 	 * @param query A <code>java.util.Map</code> with all request
 	 * data. Usually it will be the result of a previous call
 	 * to @link #dumpRequest() 
 	 */
 	public void restoreDump(Map query)
 	{
 		this.query = query;
 	}
 	
 	/**
 	 * Gets some request parameter as <code>Object</code>.
 	 * This method may be used when you have to get some value
 	 * of a <i>multipart/form-data</i> request, like a image
 	 * of file. <br>
 	 * 
 	 * @param parameter
 	 * @return
 	 */
 	public Object getObjectParameter(String parameter)
 	{
 		return this.query.get(parameter);
 	}
 	
 	/**
 	 * Adds a new <code>UrlPattern</code>.
 	 * 
 	 * @param name The pattern name
 	 * @param value  The Pattern value
 	 */
 	public static void addUrlPattern(String name, String value)
 	{
 		UrlPatternCollection.addPattern(name, value);
 	}
 	
 	/**
 	 * Adds a new parameter to the request.
 	 * 
 	 * If you want to have one more, or to modify an existing one parameter,
 	 * you should use this method to the job. 
 	 * 
 	 * @param name Parameter name
 	 * @param value Parameter value
 	 */
 	public void addParameter(String name, Object value)
 	{
 		this.query.put(name, value);
 	}
 	
 	/**
 	 * Gets the <i>action</i> of the current request.
 	 * 
 	 * An <i>Action</i> is the parameter name which specifies
 	 * what next action should be done by the system. It may be
 	 * add or edit a post, editing the groups, whatever. In the URL, the
 	 * Action can the represented in two forms:
 	 * <p>
 	 * <blockquote>
 	 * <code>
 	 * http://www.host.com/webapp/servletName?module=groups&action=list
 	 * </code>
 	 * </blockquote>
 	 * <p>
 	 * or
 	 * <p>
 	 * <blockquote>
 	 * <code>
 	 * http://www.host.com/webapp/servletName/groups/list
 	 * </code>
 	 * </blockquote>
 	 * <p>
 	 * In both situations, the action's name is "list".
 	 * 
 	 * @return String representing the action name
 	 */
 	public String getAction()
 	{
 		return this.getParameter("action");
 	}
 	
 	/**
 	 * Gets the <i>module</i> of the current request.
 	 * 
 	 * A <i>Module</i> is the parameter name which specifies
 	 * what module the user is requesting. It may be the group
 	 * administration, the topics or anything else configured module.
 	 *In the URL, the Module can the represented in two forms:
 	 * <p>
 	 * <blockquote>
 	 * <code>
 	 * http://www.host.com/webapp/servletName?module=groups&action=list
 	 * </code>
 	 * </blockquote>
 	 * <p>
 	 * or
 	 * <p>
 	 * <blockquote>
 	 * <code>
 	 * http://www.host.com/webapp/servletName/groups/list
 	 * </code>
 	 * </blockquote>
 	 * <p>
 	 * In both situations, the module's name is "groups".
 	 * 
 	 * @return String representing the module name
 	 */
 	public String getModule()
 	{
 		return this.getParameter("module");
 	}
 	
 	public Object getObjectRequestParameter(String parameter)
 	{
 		return this.query.get(parameter);
 	}
 }
