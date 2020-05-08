 /*
  * Copyright 2009 Manuel Carrasco Moñino. (manuel_carrasco at users.sourceforge.net) 
  * http://code.google.com/p/gwtupload
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package gwtupload.server;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 import java.util.Map.Entry;
 
 import javax.servlet.Servlet;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.FileItemFactory;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.apache.log4j.Logger;
 
 /**
  * <p>
  * Upload servlet for the GwtUpload library.
  * </p>
  * 
  * For customizable application actions, it's better to extend the UloadAction
  * class instead of this.
  * 
  * <p>
  * <b>Example of web.xml</b>
  * </p>
  * 
  * <pre>
  * &lt;context-param&gt;
  *     &lt;!-- max size of the upload request --&gt;
  *     &lt;param-name&gt;maxSize&lt;/param-name&gt;
  *     &lt;param-value&gt;3145728&lt;/param-value&gt;
  *   &lt;/context-param&gt;
  *   
  *   &lt;context-param&gt;
  *     &lt;!-- useful in development mode to see the upload progress bar in fast networks --&gt;
  *     &lt;param-name&gt;slowUploads&lt;/param-name&gt;
  *     &lt;param-value&gt;true&lt;/param-value&gt;
  *   &lt;/context-param&gt;
  * 
  *   &lt;servlet&gt;
  *     &lt;servlet-name&gt;uploadServlet&lt;/servlet-name&gt;
  *     &lt;servlet-class&gt;gwtupload.server.UploadServlet&lt;/servlet-class&gt;
  *   &lt;/servlet&gt;
  *   
  *   &lt;servlet-mapping&gt;
  *     &lt;servlet-name&gt;uploadServlet&lt;/servlet-name&gt;
  *     &lt;url-pattern&gt;*.gupld&lt;/url-pattern&gt;
  *   &lt;/servlet-mapping&gt;
  * 
  * 
  * </pre>
  * 
  * @author Manolo Carrasco Moñino
  * 
  */
 public class UploadServlet extends HttpServlet implements Servlet {
 
 	private static final long serialVersionUID = 2740693677625051632L;
 
 	protected static final String PARAM_FILENAME = "filename";
 
 	protected static String PARAM_SHOW = "show";
 
 	protected static String PARAM_CONTENT = "content";
 
 	protected static final String TAG_FINISHED = "finished";
 
 	protected static final String TAG_ERROR = "error";
 
 	protected static final String ATTR_FILES = "FILES";
 
 	private static final String ATTR_LISTENER = "LISTENER";
 
 	private static final String ATTR_ERROR = "ERROR";
 
 	private static Logger logger = Logger.getLogger(UploadServlet.class);
 
 	private static String XML_TPL = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n<response>%%MESSAGE%%</response>\n";
 
 	private long maxSize = (5 * 1024 * 1024);
 
 
 	/**
 	 * Read configurable parameters during the servlet initialization.
 	 */
 	public void init(ServletConfig config) throws ServletException {
 		super.init(config);
 
 		String size = config.getServletContext().getInitParameter("maxSize");
 		if (size != null)
 			maxSize = Long.parseLong(size);
 
 		String slow = config.getServletContext().getInitParameter("slowUploads");
 		if (slow != null && "true".equalsIgnoreCase(slow))
 			UploadListener.setSlowUploads(true);
 
 		logger.debug("UPLOAD servlet init, maxSize=" + maxSize + ", slowUploads=" + slow);
 	}
 
 	/**
 	 * The get method is used to monitor the uploading process or to get the
 	 * content of the uploaded files
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
 		if (request.getParameter(PARAM_SHOW) != null) {
 			writeUploadedContent(request, response);
 		} else {
 			String message = "";
 			Map<String, String> status = getUploadStatus(request);
 			for (Entry<String, String> e : status.entrySet()) {
 				message += "<" + e.getKey() + ">" + e.getValue() + "</" + e.getKey() + ">\n";
 			}
 			writeResponse(request, response, message);
 		}
 	}
 
 	/**
 	 * The post method is used to receive the file and save it in the user
 	 * session. It returns a very XML page that the client receives in an
 	 * iframe.
 	 * 
 	 * The content of this xml document has a tag error in the case of error in
 	 * the upload process or the string OK in the case of success.
 	 * 
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		String error;
 		try {
 			error = parsePostRequest(request, response);
 		} catch (Exception e) {
 			error = "Exception " + e.toString();
 		}
 		writeResponse(request, response, error != null ? "<error>" + error + "</error>" : "OK");
 	}
 
 	/**
 	 * This method parses the submit action, put in session a listener where the
 	 * progress status is updated, and eventually stores the received data in
 	 * the user session.
 	 */
 	protected String parsePostRequest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
 		HttpSession session = request.getSession();
 		session.removeAttribute(ATTR_ERROR);
 
 		logger.debug(session.getId() + " UPLOAD new request  " + (session.getAttribute(ATTR_LISTENER) != null));
 
 		String error = null;
 		if (session.getAttribute(ATTR_LISTENER) != null) {
 			error = "The request has been rejected because the server is already receiving another file.";
 			logger.error(session.getId() + " UPLOAD " + error);
 			return error;
 		}
 
 		@SuppressWarnings("unchecked")
 		Vector<FileItem> sessionFiles = (Vector<FileItem>) session.getAttribute(ATTR_FILES);
 
 		logger.debug(session.getId() + " UPLOAD servlet procesing request " + request.getContentLength() + " < " + maxSize);
 		try {
 
 			// Create the factory used for uploading files,
 			// set file upload progress listener, and put it into user session,
 			// so the browser can use ajax to query status of the upload process
 			FileItemFactory factory = getFileItemFactory();
 			ServletFileUpload uploader = new ServletFileUpload(factory);
 			UploadListener listener = new UploadListener();
 			uploader.setProgressListener(listener);
 			logger.debug(session.getId() + " UPLOAD servlet putting listener in session");
 			session.setAttribute(ATTR_LISTENER, listener);
 
 			// uploader.setFileSizeMax(maxSize);
 			uploader.setSizeMax(maxSize);
 
 			// Receive the files
 			logger.debug(session.getId() + " UPLOAD servlet parsing HTTP request ");
 			@SuppressWarnings("unchecked")
 			List<FileItem> uploadedItems = uploader.parseRequest(request);
 			logger.debug(session.getId() + " UPLOAD servlet servlet received items: " + uploadedItems.size());
 
 			// We can do this before parsing the request, but normally sending an exception doesn't close the socket 
 			// and the client continues sending files until the form is completely submitted.
 			// So doing this the user sees the upload progress.
 			if (request.getContentLength() > maxSize) {
 				error = "\nThe request was rejected because the size of the request (" + request.getContentLength() / 1024 + " kB.)"
 				      + "\nexceeds the limit allowed by the server (" + maxSize / 1024 + " kB.)";
 				logger.error(session.getId() + " UPLOAD " + error);
 				return error;
 			}
 
 			// Put received files in session
 			if (sessionFiles == null && uploadedItems.size() > 0) {
 				sessionFiles = new Vector<FileItem>();
 			}
 			for (FileItem fileItem : uploadedItems) {
 				if (fileItem.isFormField() || fileItem.getSize() > 0) {
 					sessionFiles.add(fileItem);
 				} else {
 					logger.error(session.getId() + " UPLOAD servlet error File empty: " + fileItem);
 					error += "\nError, the reception of the file " + fileItem.getName()
 					      + " was unsuccesful.\nPlease verify that the file exists and you have enough permissions to read it";
 				}
 			}
 			
 			if (sessionFiles != null && sessionFiles.size() > 0) {
 				logger.debug(session.getId() + " UPLOAD servlet puting FILES in SESSION " + sessionFiles.elementAt(0));
 				session.setAttribute(ATTR_FILES, sessionFiles);
 			} else {
 				logger.error(session.getId() + " UPLOAD servlet error NO DATA received ");
 				error += "\nError, your browser has not sent any information.\nPlease try again or try it using another browser\n";
 			}
 		} catch (Exception e) {
 			logger.error(session.getId() + " UPLOAD servlet Exception: " + e.getMessage() + "\n" + stackTraceToString(e));
 			error += "\nUnexpected exception receiving the file: \n" + e.getMessage();
 		}
 		logger.debug(session.getId() + " UPLOAD servlet removing listener from session");
 		session.removeAttribute(ATTR_LISTENER);
 		return error;
 	}
 
 	/**
	 * Writes a XML response to the client
 	 * 
 	 * @param request
 	 * @param response
 	 * @param message
 	 * @throws IOException
 	 */
 	protected static void writeResponse(HttpServletRequest request, HttpServletResponse response, String message) throws IOException {
 		response.setContentType("text/html");
 		PrintWriter out = response.getWriter();
		String xml = XML_TPL.replaceAll("%%MESSAGE%%", message);
 		out.print(xml);
 		out.flush();
 		out.close();
 	}
 
 	private static Map<String, String> getUploadStatus(HttpServletRequest request) {
 		HttpSession session = request.getSession();
 		String filename = request.getParameter(PARAM_FILENAME);
 		return getUploadStatus(session, filename);
 	}
 
 	private static Map<String, String> getUploadStatus(HttpSession session, String filename) {
 
 		Map<String, String> ret = new HashMap<String, String>();
 		Long currentBytes = null;
 		Long totalBytes = null;
 		Long percent = null;
 		if (session.getAttribute(ATTR_LISTENER) != null) {
 			UploadListener listener = (UploadListener) session.getAttribute(ATTR_LISTENER);
 			currentBytes = listener.getBytesRead();
 			totalBytes = listener.getContentLength();
 			percent = totalBytes != 0 ? currentBytes * 100 / totalBytes : 0;
 			logger.debug(session.getId() + " UPLOAD status " + filename + " " + currentBytes + "/" + totalBytes + " " + percent);
 		} else if (session.getAttribute(ATTR_ERROR) != null) {
 			ret.put(TAG_ERROR, (String) session.getAttribute(ATTR_ERROR));
 			ret.put(TAG_FINISHED, TAG_ERROR);
 			logger.error(session.getId() + " UPLOAD status " + filename + " finished with error: " + session.getAttribute(ATTR_ERROR));
 			session.removeAttribute(ATTR_ERROR);
 		} else if (session.getAttribute(ATTR_FILES) != null) {
 			if (filename == null) {
 				ret.put(TAG_FINISHED, "ok");
 				logger.debug(session.getId() + " UPLOAD status filename=null finished with files: " + session.getAttribute(ATTR_FILES));
 			} else {
 				@SuppressWarnings("unchecked")
 				Vector<FileItem> sessionFiles = (Vector<FileItem>) session.getAttribute(ATTR_FILES);
 				for (FileItem file : sessionFiles) {
 					if (file.isFormField() == false && file.getFieldName().equals(filename)) {
 						ret.put(TAG_FINISHED, "ok");
 						ret.put(PARAM_FILENAME, filename);
 						logger.debug(session.getId() + " UPLOAD status " + filename + " finished with files: " + session.getAttribute(ATTR_FILES));
 					}
 				}
 			}
 		} else {
 			logger.debug(session.getId() + " UPLOAD wait listener is null");
 			ret.put("wait", "listener is null");
 			percent = 5L;
 			totalBytes = currentBytes = 0L;
 		}
 		if (percent != null) {
 			ret.put("percent", "" + percent);
 			ret.put("currentBytes", "" + currentBytes);
 			ret.put("totalBytes", "" + totalBytes);
 			if (currentBytes >= totalBytes) {
 				ret.put(TAG_FINISHED, "ok");
 			}
 		}
 		return ret;
 	}
 
 	/**
 	 * 
 	 * Write the response server with the content of an uploaded file. Setting
 	 * the appropriate content-type
 	 * 
 	 * @param request
 	 * @param response
 	 * @return true in the case of success
 	 * @throws IOException
 	 */
 	private static boolean writeUploadedContent(HttpServletRequest request, HttpServletResponse response) throws IOException {
 		String parameter = request.getParameter(PARAM_SHOW);
 		String contentRegexp = request.getParameter(PARAM_CONTENT);
 		logger.debug(request.getSession().getId() + " UPLOAD, getFileContent: " + parameter + " " + contentRegexp);
 		if (parameter != null) {
 			@SuppressWarnings("unchecked")
 			Vector<FileItem> sessionFiles = (Vector<FileItem>) request.getSession().getAttribute(ATTR_FILES);
 			if (sessionFiles != null) {
 				FileItem i = findItemByFieldName(sessionFiles, parameter);
 				if (i == null) {
 					i = findItemByFileName(sessionFiles, parameter);
 				}
 				if (i != null && !i.isFormField()) {
 					if (contentRegexp != null && contentRegexp.length() > 0 && !i.getContentType().matches(contentRegexp))
 						return false;
 					response.setContentType(i.getContentType());
 					copyFromInputToOutput(i, response.getOutputStream());
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	private static void copyFromInputToOutput(FileItem item, OutputStream out) throws IOException {
 		InputStream in = item.getInputStream();
 		byte[] a = new byte[2048];
 		while (in.read(a) != -1) {
 			out.write(a);
 			a = new byte[2048];
 		}
 		out.flush();
 		out.close();
 	}
 
 	/**
 	 * Utility method to get a fileItem from a vector using the attribute name
 	 * 
 	 * @param sessionFiles
 	 * @param attrName
 	 * @return fileItem found or null
 	 */
 	public static FileItem findItemByFieldName(Vector<FileItem> sessionFiles, String attrName) {
 		if (sessionFiles != null) {
 			for (FileItem fileItem : sessionFiles) {
 				if (fileItem.getFieldName().equalsIgnoreCase(attrName))
 					return fileItem;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Utility method to get a fileItem from a vector using the file name It
 	 * only returns fileItems that are uploaded files.
 	 * 
 	 * @param sessionFiles
 	 * @param fileName
 	 * @return fileItem of the file found or null
 	 */
 	public static FileItem findItemByFileName(Vector<FileItem> sessionFiles, String fileName) {
 		if (sessionFiles != null) {
 			for (FileItem fileItem : sessionFiles) {
 				if (fileItem.isFormField() == false && fileItem.getName().equalsIgnoreCase(fileName))
 					return fileItem;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Simple method to get a string from the exception stack
 	 * 
 	 * @param e
 	 * @return string
 	 */
 	protected static String stackTraceToString(Exception e) {
 		StringWriter writer = new StringWriter();
 		e.printStackTrace(new PrintWriter(writer));
 		return writer.getBuffer().toString();
 	}
 
 
 	/**
 	 * Override this method if you want to implement a different ItemFactory
 	 * DiskFileItemFactory doesn't work in app-engine
 	 *  
 	 * @return
 	 */
 	protected FileItemFactory getFileItemFactory() {
 		// This factory will create a files in disk if the size of the file
 		// is greater than the threshold
 		return new 	DiskFileItemFactory(){{
 			setSizeThreshold(8192000);
 		}};
 	}
 }
