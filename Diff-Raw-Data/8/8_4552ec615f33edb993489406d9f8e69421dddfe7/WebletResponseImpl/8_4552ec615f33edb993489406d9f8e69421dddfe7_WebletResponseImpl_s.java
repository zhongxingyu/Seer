 /*
  * Copyright 2005 John R. Fallows
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.java.dev.weblets.impl.servlets;
 
 import net.java.dev.weblets.WebletResponse;
 import net.java.dev.weblets.impl.WebletResponseBase;
 import net.java.dev.weblets.impl.util.ReflectUtilsImpl;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.Calendar;
 
 /**
  * Weblets Response impl object implementation of the internal weblets response object <p/> TODO check out how to enable all the header params in portlet
  * environments which do not provide the needed methods but follow the pure ri interfaces
  */
 public class WebletResponseImpl extends WebletResponseBase {
 
 	public WebletResponseImpl(String contentTypeDefault, ServletResponse httpResponse) {
 		super(contentTypeDefault);
 		// new GZIPResponseWrapper(
 		_httpResponse = httpResponse;
 	}
 
 	public void setContentLength(int length) {
 		_httpResponse.setContentLength(length);
 	}
 
 	public OutputStream getOutputStream() throws IOException {
 		// return _httpResponse.getOutputStream();
         return ReflectUtilsImpl.getOutputStream(getHttpResponse());
 	}
 
 	public void setStatus(int statusCode) {
 		switch (statusCode) {
 		case WebletResponse.SC_ACCEPTED:
 			setResponseStatus(HttpServletResponse.SC_ACCEPTED);
 			break;
 		case WebletResponse.SC_NOT_FOUND:
 			setResponseStatus(HttpServletResponse.SC_NOT_FOUND);
 			break;
 		case WebletResponse.SC_NOT_MODIFIED:
 			setResponseStatus(HttpServletResponse.SC_NOT_MODIFIED);
 			break;
 		default:
 			throw new IllegalArgumentException();
 		}
 	}
 
 	public ServletResponse getHttpResponse() {
 		return _httpResponse;
 	}
 
 	protected void setContentTypeImpl(String contentType) {
 		if(contentType != null) { //needed to avoid npes in was!
 			_httpResponse.setContentType(contentType);
 		}
 	}
 
 	protected void setLastModifiedImpl(long lastModified) {
 		if(_y2038_bug) {
 			lastModified = y2038k_fix(lastModified);
 		}
 		try {
 			setDateHeader(WebletResponse.HTTP_LAST_MODIFIED, lastModified);
 		} catch (IndexOutOfBoundsException ex) { // websphere 6.1 has a y2038k bug
 			_y2038_bug = true;
 			lastModified = y2038k_fix(lastModified);
 			setDateHeader(WebletResponse.HTTP_EXPIRES, lastModified);
 		}
 	}
 
     public void setContentLength(long length) {
        ((HttpServletResponse)_httpResponse).addHeader("Conten-Length", Long.toString(length));
     }
 
 	protected void setContentVersionImpl(String contentVersion, long timeout) {
 		if(_y2038_bug) {
 			timeout = y2038k_fix(timeout);
 		}
 		try {
 			setDateHeader(WebletResponse.HTTP_EXPIRES, timeout);
              //Cache-Control: max-age=3600, must-revalidate
             //pre-check=9000,post-check=9000,
             ((HttpServletResponse)_httpResponse).addHeader("Cache-Control", "pre-check=9000,post-check=9000,max-age="+((timeout-System.currentTimeMillis())/ 1000  ));
 		} catch (IndexOutOfBoundsException ex) { // websphere 6.1 has a y2038k bug
 			_y2038_bug = true;
 			timeout = y2038k_fix(timeout);
 			setDateHeader(WebletResponse.HTTP_EXPIRES, timeout);
 		}
 	}
 
 	/**
 	 * y2038k fix for certain servers (aka WAS 6.1)
 	 *
 	 * @param timeout the incoming time value to be fixed
 	 * @return
 	 */
 	private long y2038k_fix(long timeout) {
 		java.util.Calendar cal = Calendar.getInstance();
 		cal.setTimeInMillis(timeout);
 		if(cal.get(Calendar.YEAR) >= Y2038_BOUNDARY) {
 			cal.set(Calendar.YEAR, Y0238_VALIDYEAR);
 		}
 		return cal.getTimeInMillis();
 	}
 
 	/**
 	 * we fallback to various introspection methods to support portlet environments as well
 	 *
 	 * @param entry
 	 * @param lastModified
 	 */
 	private void setDateHeader(String entry, long lastModified) {
 		/*we can cover a simple httpservletresponse*/
 		/*we do it that way to improve speed*/
  		if(_httpResponse instanceof HttpServletResponse) {
 			((HttpServletResponse)_httpResponse).addDateHeader(entry, lastModified);
 			return;
 		}
 		/*with others we give introspection a try*/
 		Method[] supportedMethods = _httpResponse.getClass().getMethods();
 		// fetch the date header method
 		Method m = null;
 		try {
 			m = _httpResponse.getClass().getMethod(SETDATEHEADER, new Class[] { String.class, long.class });
 		} catch (NoSuchMethodException e) {
 			if (!_dateheader_warn) {
 				_dateheader_warn = true; // we do not want to flood warnings one is enough
 				Log log = LogFactory.getLog(getClass());
 				log.warn(ERR_NODATEHEADERSET);
 				log.warn(e);
 				return;
 			}
 		}
 		try {
 			Object[] params = new Object[2];
 			params[0] = entry;
 			params[1] = new Long( lastModified );
 			m.invoke(_httpResponse, params);
 		} catch (IllegalAccessException e) {
 			Log log = LogFactory.getLog(getClass());
 			log.error(e);
 		} catch (InvocationTargetException e) {
 			Log log = LogFactory.getLog(getClass());
 			log.error(e);
 		}
 		return;
 	}
 
 	/**
 	 * locking singleton to prevent too many warnings in certain portlet environments which dont have the needed methods implemented (which follow the pure ri)
 	 */
 	static boolean	responsestatus_warn	= false;
 
 	private void setResponseStatus(int status) {
 		if(_httpResponse instanceof HttpServletResponse) {
 			((HttpServletResponse)_httpResponse).setStatus(status);
 			return;
 		}
 		Method[] supportedMethods = _httpResponse.getClass().getMethods();
 		// fetch the date header method
 		Method m = null;
 		try {
 			m = _httpResponse.getClass().getMethod("setStatus", new Class[] { int.class });
 		} catch (NoSuchMethodException e) {
 			if (!responsestatus_warn) {
 				responsestatus_warn = true;
 				Log log = LogFactory.getLog(getClass());
 				log.warn("No satus setting possible reason: ");
 				log.warn(e);
 				return;
 			}
 		}
 		try {
 			Object[] params = new Object[1];
 			params[0] = new Integer(status);
 			m.invoke(_httpResponse, params);
 		} catch (IllegalAccessException e) {
 			Log log = LogFactory.getLog(getClass());
 			log.error(e);
 		} catch (InvocationTargetException e) {
 			Log log = LogFactory.getLog(getClass());
 			log.error(e);
 		}
 		return;
 	}
 
 	private static final int	Y0238_VALIDYEAR	= 2037;
 	private static final int	Y2038_BOUNDARY	= 2038;
 	private static final String	ERR_NODATEHEADERSET	= "No date header setting possible reason: ";
 	private static final String	SETDATEHEADER	= "setDateHeader";
 
 	/**
 	 * locking singleton to prevent too many warnings in certain portlet environments which dont have the needed methods implemented (which follow the pure ri)
 	 */
 	static boolean	_dateheader_warn	= false;
 	static boolean  _y2038_bug = false;	/*y2038 bug affected system, aka WAS <=6.1+*/
 	ServletResponse	_httpResponse	= null;
 }
