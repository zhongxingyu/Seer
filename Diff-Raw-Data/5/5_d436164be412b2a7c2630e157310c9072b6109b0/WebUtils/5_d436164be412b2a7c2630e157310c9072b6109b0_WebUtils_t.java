 /*
  * Copyright (C) 2003 - 2013 OpenSubsystems.com/net/org and its owners. All rights reserved.
  * 
  * This file is part of OpenSubsystems.
  *
  * OpenSubsystems is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
  */
 
 package org.opensubsystems.core.util.servlet;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Enumeration;
 import java.util.Properties;
 import java.util.logging.Logger;
 
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.opensubsystems.core.util.Config;
 import org.opensubsystems.core.util.Log;
 import org.opensubsystems.core.util.MimeTypeConstants;
 import org.opensubsystems.core.util.OSSObject;
 import org.opensubsystems.core.util.PropertyUtils;
 import org.opensubsystems.core.util.WebConstants;
 
 /**
  * Collection of useful methods for Web environment.
  *
  * @author bastafidli
  */
 public final class WebUtils extends OSSObject
 {
    // Configuration settings ///////////////////////////////////////////////////
    
    /** 
     * Name of the property specifying what port the embedded web server should 
     * start on.
     */   
    public static final String WEBSERVER_PORT = "oss.webserver.port";
 
    /** 
     * Name of the property specifying what port the embedded web server should
     * accepts SSL requests. 
     */   
    public static final String WEBSERVER_PORT_SECURE 
                                  = "oss.webserver.port.secure";
 
    /** 
     * Name of the property for size of the buffer used to serve files.
     * @see #WEBFILE_BUFFER_DEFAULT_SIZE
     */   
    public static final String WEBUTILS_WEBFILE_BUFFER_SIZE 
                                  = "oss.webserver.servebuffer.size";   
 
    // Constants ////////////////////////////////////////////////////////////////
 
    /**
     * Default value for the WEBUTILS_WEBFILE_BUFFER_SIZE.
     * @see #WEBUTILS_WEBFILE_BUFFER_SIZE
     */
    public static final int WEBFILE_BUFFER_DEFAULT_SIZE = 40960;
 
    // Cached values ////////////////////////////////////////////////////////////
 
    /**
     * Logger for this class
     */
    private static Logger s_logger = Log.getInstance(WebUtils.class);
 
    /**
     * HTTP server port the last request to the web application came on.
     */
   private static int s_iWebPort;
 
    /**
     * HTTPS server port the last request to the web application came on (SSL 
     * port).
     */
   private static int s_iWebPortSecure;
 
    // Constructors /////////////////////////////////////////////////////////////
    
    /** 
     * Private constructor since this class cannot be instantiated
     */
    private WebUtils(
    )
    {
       // Do nothing
    }
    
    // Logic ////////////////////////////////////////////////////////////////////
    
    /**
     * Get value of port, which was configured to accepts non SSL requests on.
     * This port may differ from the actual port in case the server was not able 
     * to start on the configured port and it started on a different one and it 
     * adjusted the configured port.
     * 
     *  @return int - port configured to accepts non SSL requests
     */
    public static int getConfiguredPort(
    )
    {
       Properties prpSettings;
       int        iConfiguredWebPort;
       
       prpSettings = Config.getInstance().getProperties();
 
       // Load the port where the web server should be accepting requests in non 
       // SSL mode
       iConfiguredWebPort = PropertyUtils.getIntPropertyInRange(
                                 prpSettings, WEBSERVER_PORT,
                                 WebConstants.HTTP_PORT_DEFAULT,
                                 "Web server port",
                                 WebConstants.HTTP_PORT_MIN,
                                 WebConstants.HTTP_PORT_MAX);
       
       return iConfiguredWebPort;
    }
    
    /**
     * Get the port number on which the last non SSL requests came.
     * 
     *  @return int - port to accepts non SSL requests
     */
    public static int getActualPort(
    )
    {
       if (s_iWebPort == 0)
       {
          // No actual port has been set yet from a request so lets see what port
          // was the application configured to run on
          s_iWebPort = getConfiguredPort();
       }
       return s_iWebPort;
    }
    
    /**
     * Get value of port, which was configured to accepts SSL requests on. This 
     * port may differ from the actual port in case the server was not able to 
     * start on the configured port and it started on a different one and it 
     * adjusted the configured port.
     * 
     *  @return int - port specified in configuration to accepts SSL requests
     */
    public static int getConfiguredSSLPort(
    )
    {
       Properties prpSettings;
       int        iConfiguredWebPortSecure;
       
       prpSettings = Config.getInstance().getProperties();
 
       // Load the port where the web server should be accepting requests in non 
       // SSL mode
       iConfiguredWebPortSecure = PropertyUtils.getIntPropertyInRange(
                                     prpSettings, WEBSERVER_PORT_SECURE,
                                     WebConstants.HTTP_SECURE_PORT_DEFAULT,
                                     "Web server port",
                                     WebConstants.HTTP_PORT_MIN,
                                     WebConstants.HTTP_PORT_MAX);
       
       return iConfiguredWebPortSecure;
    }
    
    /**
     * Get the port number on which the last SSL requests came.
     * 
     *  @return int - port to accepts SSL requests
     */
    public static int getActualSSLPort(
    )
    {
       if (s_iWebPort == 0)
       {
          // No actual port has been set yet from a request so lets see what port
          // was the application configured to run on
          s_iWebPort = getConfiguredSSLPort();
       }
       return s_iWebPortSecure;
    }
    
    /**
     * Serve files to the Internet.
     *
     * @param hsrpResponse - the servlet response.
     * @param strRealPath - real path to the file to server
     * @throws IOException - an error has occurred while accessing the file or writing response
     */
    public static void serveFile(
       HttpServletResponse hsrpResponse,
       String              strRealPath
    ) throws IOException
    {
       // TODO: Improve: Figure out, how we don't have to serve the file, 
       // but the webserver will!!! (cos.jar has a method for it, but the license
       // is to prohibitive to use it. Maybe Jetty has one too) 
       Properties prpSettings;
       int        iWebFileBufferSize;
       
       prpSettings = Config.getInstance().getProperties();
 
       // Load default size of buffer to serve files 
       iWebFileBufferSize = PropertyUtils.getIntPropertyInRange(
                                 prpSettings, WEBUTILS_WEBFILE_BUFFER_SIZE,
                                 WEBFILE_BUFFER_DEFAULT_SIZE,
                                 "Size of a buffer to serve files ",
                                 // Use some reasonable lower limit
                                 4096,
                                 // This should be really limited 
                                 // by size of available memory
                                 100000000);
             
       ServletOutputStream sosOut      = hsrpResponse.getOutputStream();
       byte[]              arBuffer    = new byte[iWebFileBufferSize];
       File                flImage     = new File(strRealPath);
       FileInputStream     fisReader   = new FileInputStream(flImage);
 
       // get extension of the file
       String strExt = strRealPath.substring(strRealPath.lastIndexOf(".") + 1, 
                                             strRealPath.length());
       // set content type for particular extension
       hsrpResponse.setContentType(MimeTypeConstants.getMimeType(strExt));
       hsrpResponse.setBufferSize(WEBFILE_BUFFER_DEFAULT_SIZE);
       hsrpResponse.setContentLength((int)flImage.length());
 
       // TODO: Performance: BufferedInputStream allocate additional internal 
       // buffer so we have two buffers for each file of given size, evaluate 
       // if this is faster than non buffered read and if it is not, then get 
       // rid of it. But the preference is to get rid of this method all together,
       BufferedInputStream bisReader = new BufferedInputStream(
                                              fisReader, iWebFileBufferSize);
       int                 iRead;
 
       try
       {
          while (true)
          {
             iRead = bisReader.read(arBuffer);
             if (iRead != -1)
             {
                sosOut.write(arBuffer, 0, iRead);
             }
             else
             {
                break;
             }
          }
       }
       finally
       {
          try
          {
             fisReader.close();
          }
          finally
          {
             sosOut.close();
          }
       }     
    }
    
    /**
     * Reconstruct full URL from HTTP request with protocol, full path and query
     * strings.
     *
     * @param hsrqRequest - the servlet request.
     * @return String - full URL
     */
    public static String getFullRequestURL(
       HttpServletRequest hsrqRequest
    )
    {
       String strQueryString;
       StringBuffer sbURL;
 
       strQueryString = hsrqRequest.getQueryString();
       if (strQueryString == null)
       {
          sbURL = new StringBuffer();
       }
       else
       {
          sbURL = new StringBuffer("?");
          sbURL.append(strQueryString);
       }
 
       return hsrqRequest.getRequestURL().append(sbURL).toString();
    }
 
    /**
     * Reconstruct full URL from HTTP request with protocol, full path but 
     * without query strings.
     *
     * @param hsrqRequest - the servlet request.
     * @return String - full URL
     */
    public static String getRequestURLWithoutQuery(
       HttpServletRequest hsrqRequest
    )
    {
       return hsrqRequest.getRequestURL().toString();
    }
 
    /**
     * Get full path from HTTP request consisting of the servlet path and the 
     * path info
     * 
     * @param hsrqRequest - the servlet request
     * @return - full URL
     */
    public static String getFullRequestPath(
       HttpServletRequest hsrqRequest
    )
    {
       StringBuffer sbPath;
       String       strPath;
       String       strExtraPath;
 
       strPath = hsrqRequest.getServletPath(); 
       if (strPath == null)
       {
          strPath = "";
       }
       strExtraPath = hsrqRequest.getPathInfo();
       if (strExtraPath != null)
       {
          sbPath = new StringBuffer(strPath);
          sbPath.append(strExtraPath);
          strPath = sbPath.toString();  
       }
       
       return strPath;
    }
    
    /**
     * Adjust the secure and nonsecure port numbers the web application is 
     * currently running on. 
     * 
     * @param hsrqRequest - the servlet request.
     */
    public static void adjustPorts(
       HttpServletRequest hsrqRequest
    )
    {
       if (hsrqRequest.isSecure())
       {
          s_iWebPortSecure = hsrqRequest.getServerPort();         
       }
       else
       {
          s_iWebPort = hsrqRequest.getServerPort();
       }
    }
    
    /**
     * Switch URL to HTTP or HTTPS and also particular ports.
     *
     * @param hsrqRequest - the servlet request
     * @param strURL - relative URL
     * @param bIsSecure - flag signaling if url should be switched to secure
     *                  - true = switch to secure (use HTTPS)
     *                  - false = switch to unsecure (use HTTP)
     * @return String - absolute URL
     */
    public static String toggleSecure(
       HttpServletRequest hsrqRequest,
       String             strURL,
       boolean            bIsSecure
    )
    {
       URL uURL;
       URL uSwitchedURL;
       
       String strUrlReturn = strURL;
       
       // find out if strURL sent as parameter is absolute or relative
       try
       {
          // try to construct URL from source parameter
          uURL = new URL(strURL);
          // at this point we should have switched URL object so retrieve 
          // and return string of absolute URL 
          strUrlReturn = uURL.toExternalForm();
 
          // if there is used currently secure mode (used HTTPS) and application secure
          // is TRUE, don't make changes
          // if there is used currently secure mode (used HTTPS) and application secure
          // is FALSE, change url to not secure (HTTP)
          if (bIsSecure)
          {
             // -------------------------------------------------------
             // change http -> https and not secure port -> secure port
             // -------------------------------------------------------
             if (uURL.getProtocol().equals(WebConstants.PROTOCOL_HTTP))
             {
                // change protocol and port number to secure one
                uSwitchedURL = new URL(WebConstants.PROTOCOL_HTTPS,
                                       uURL.getHost(), getActualSSLPort(),
                                       uURL.getFile());
                // at this point we should have switched URL object so retrieve 
                // and return string of absolute URL 
                strUrlReturn = uSwitchedURL.toExternalForm();
             }
          }
          else
          {
             // -------------------------------------------------------
             // change https -> http and secure port -> not secure port
             // -------------------------------------------------------
             if (uURL.getProtocol().equals(WebConstants.PROTOCOL_HTTPS))
             {
                // change secure protocol and port number to not secure one
                uSwitchedURL = new URL(WebConstants.PROTOCOL_HTTP,
                                       uURL.getHost(),
                                       getActualPort(),
                                       uURL.getFile());
                // at this point we should have switched URL object so retrieve 
                // and return string of absolute URL 
                strUrlReturn = uSwitchedURL.toExternalForm();
             }
          }
       }
       catch (MalformedURLException excMURLE)
       {
          // there was problem with constructing URL so log this and do nothing
          s_logger.warning("Error occurred while constructing URL.");
       }
       
       return strUrlReturn;
    }
 
    /**
     * Create debug string containing all parameter names and their values from
     * the request.
     *
     * @param  hsrqRequest - the servlet request.
     * @return String - debug string containing all parameter names and their
     *                  values from the request
     */
    public static String debug(
       HttpServletRequest hsrqRequest
    )
    {
       Enumeration   enumNames;
       Enumeration   enumValues;
       String        strName;
       String[]      arValues;
       int           iIndex;
       StringBuilder sbfReturn = new StringBuilder();
 
       sbfReturn.append("HttpServletRequest=[");
       sbfReturn.append("FullURL=");
       sbfReturn.append(getFullRequestURL(hsrqRequest));
       sbfReturn.append(";");
       for (enumNames = hsrqRequest.getParameterNames();
            enumNames.hasMoreElements();)
       {
          strName = (String)enumNames.nextElement();
          arValues = hsrqRequest.getParameterValues(strName);
          sbfReturn.append("\nParam=");
          sbfReturn.append(strName);
          sbfReturn.append(" values=");
          for (iIndex = 0; iIndex < arValues.length; iIndex++)
          {
             sbfReturn.append(arValues[iIndex]);
             if (iIndex < (arValues.length - 1))
             {
                sbfReturn.append(";");
             }
          }
          if (enumNames.hasMoreElements())
          {
             sbfReturn.append(";");
          }
       }
       for (enumNames = hsrqRequest.getHeaderNames();
            enumNames.hasMoreElements();)
       {
          strName = (String)enumNames.nextElement();
          sbfReturn.append("\nHeader=");
          sbfReturn.append(strName);
          sbfReturn.append(" values=");
          for (enumValues = hsrqRequest.getHeaders(strName);
               enumValues.hasMoreElements();)
          {
             sbfReturn.append(enumValues.nextElement());
             if (enumValues.hasMoreElements())
             {
                sbfReturn.append(";");
             }
          }
          if (enumNames.hasMoreElements())
          {
             sbfReturn.append(";");
          }
       }
       for (enumNames = hsrqRequest.getAttributeNames();
            enumNames.hasMoreElements();)
       {
          strName = (String)enumNames.nextElement();
          sbfReturn.append("\nAttribute=");
          sbfReturn.append(strName);
          sbfReturn.append(" value=");
          sbfReturn.append(hsrqRequest.getAttribute(strName));
          if (enumNames.hasMoreElements())
          {
             sbfReturn.append(";");
          }
       }
       sbfReturn.append("]");
 
       return sbfReturn.toString();
    }
    
    /**
     * Create debug string containing all parameter names and their values from
     * the config.
     *
     * @param  scConfig - config to print out
     * @return String - debug string containing all parameter names and their
     *                  values from the config
     */
    public static String debug(
       ServletConfig scConfig
    )
    {
       Enumeration   enumNames;
       String        strParam;
       StringBuilder sbfReturn = new StringBuilder();
 
       sbfReturn.append("ServletConfig=[ServletName=");
       sbfReturn.append(scConfig.getServletName());
       sbfReturn.append(";");
       for (enumNames = scConfig.getInitParameterNames();
            enumNames.hasMoreElements();)
       {
          strParam = (String)enumNames.nextElement();
          sbfReturn.append("Param=");
          sbfReturn.append(strParam);
          sbfReturn.append(" value=");
          sbfReturn.append(scConfig.getInitParameter(strParam));
          if (enumNames.hasMoreElements())
          {
             sbfReturn.append(";");
          }
       }
       sbfReturn.append("]");
 
       return sbfReturn.toString();
    }
 
    /**
     * Create debug string containing all parameter names and their values from
     * the context.
     *
     * @param  scContext - context to print out
     * @return String - debug string containing all parameter names and their
     *                  values from the context
     */
    public static String debug(
       ServletContext scContext
    )
    {
       Enumeration   enumNames;
       String        strParam;
       StringBuilder sbfReturn = new StringBuilder();
 
       sbfReturn.append("ServletContext=[ServletContextName=");
       sbfReturn.append(scContext.getServletContextName());
       sbfReturn.append(";");
       for (enumNames = scContext.getInitParameterNames();
            enumNames.hasMoreElements();)
       {
          strParam = (String)enumNames.nextElement();
          sbfReturn.append("Param=");
          sbfReturn.append(strParam);
          sbfReturn.append(" value=");
          sbfReturn.append(scContext.getInitParameter(strParam));
          if (enumNames.hasMoreElements())
          {
             sbfReturn.append(";");
          }
       }
       sbfReturn.append("]");
 
       return sbfReturn.toString();
    }
 
    /**
     * Forward request back to the same page where it came from.
     *
     * @param  hsrqRequest  - the servlet request.
     * @param  hsrpResponse - the servlet response.
     * @throws IOException - an error while writing response
     * @throws ServletException - an error while serving the request
     */
    public static void forwardToOrigin(
       HttpServletRequest  hsrqRequest,
       HttpServletResponse hsrpResponse
    ) throws IOException,
             ServletException
    {
       hsrpResponse.sendRedirect(getFullRequestURL(hsrqRequest));
    }
 
    /**
     * Test if the real path is path for the main index page. It either doesn't 
     * contain any path e.g. http://www.bastafidli.com or just a root directory
     * http://www.bastafidli.com/.
     *
     * @param  hsrqRequest  - the servlet request.
     * @return boolean - true if the page is main index page 
     */
    public static boolean isMainIndexPage(
       HttpServletRequest hsrqRequest
    )
    {
       String strPath;
       int    iFirstIndex;
       int    iLastIndex = -1;
 
       strPath = getFullRequestPath(hsrqRequest); 
 
       // If there are two folder separators, the request is for the subindex
       // page, otherwise it is for the main index page
       iFirstIndex = strPath.indexOf(WebConstants.URL_SEPARATOR_CHAR);
       if (iFirstIndex == 0)
       {
          iLastIndex  = strPath.lastIndexOf(WebConstants.URL_SEPARATOR_CHAR);
       }
 
       // If there is no / both values will be -1
               // there is only one or none /
       return (iFirstIndex == iLastIndex)
                 // there is no / 
              && ((iFirstIndex == -1) 
                 // there is one / and it is the last
                 || (strPath.endsWith(WebConstants.URL_SEPARATOR) 
                 // or it ends with index.html
                 || strPath.endsWith(WebConstants.DEFAULT_DIRECTORY_WEB_PAGE))); 
    }
 
    /**
     * Test if the requested path is path for the index page. It either points 
     * just to a directory http://www.bastafidli.com/directory/ or it ends with 
     * index.html e.g. http://www.bastafidli.com/directory.html.
     *
     * @param  hsrqRequest  - the servlet request.
     * @return boolean - true if the page is index page for the folder false otherwise
     */
    public static boolean isIndexPage(
       HttpServletRequest hsrqRequest
    )
    {
       String strPath;
 
       strPath = getFullRequestPath(hsrqRequest); 
 
       return strPath.endsWith(WebConstants.URL_SEPARATOR) 
              || strPath.endsWith(WebConstants.DEFAULT_DIRECTORY_WEB_PAGE)
              // Or it doesnt' end with '/' but there is no extension
              || (!strPath.endsWith(WebConstants.URL_SEPARATOR)
                 && (strPath.indexOf(WebConstants.EXTENSION_SEPARATOR) == -1));
    }
 
    /**
     * Test if the requested path is path for the regular (.html) page. 
     *
     * @param  hsrqRequest  - the servlet request.
     * @return boolean - true if the page is regular web page (ends with .html)
     */
    public static boolean isStaticWebPage(
       HttpServletRequest hsrqRequest
    )
    {
       String strPath;
 
       strPath = getFullRequestPath(hsrqRequest); 
 
       return strPath.endsWith(WebConstants.WEB_PAGE_EXTENSION);
    }
    
    /**
     * Get names and values for all the init parameters from the specified 
     * context.
     * 
     * @param  scContext - context from where to retrieve the init parameters
     * @return Properties - names and values of the init parameters or empty
     *                      properties if no init parameters are specified
     */
    public static Properties getInitParameters(
       ServletContext scContext
    )
    {
       Properties prpSettings = new Properties();
       String     strName;
       String     strValue;
       
       for (Enumeration paramNames = scContext.getInitParameterNames();
            paramNames.hasMoreElements();)
       {
          strName = (String)paramNames.nextElement();
          strValue = scContext.getInitParameter(strName);
          prpSettings.put(strName, strValue);
       }
       
       return prpSettings;
    }
 
    /**
     * Get names and values for all the init parameters from the specified 
     * servlet config.
     * 
     * @param  scConfig - config from where to retrieve the init parameters
     * @return Properties - names and values of the init parameters or empty
     *                      properties if no init parameters are specified
     */
    public static Properties getInitParameters(
       ServletConfig scConfig
    )
    {
       Properties prpSettings = new Properties();
       String     strName;
       String     strValue;
       
       for (Enumeration paramNames = scConfig.getInitParameterNames();
            paramNames.hasMoreElements();)
       {
          strName = (String)paramNames.nextElement();
          strValue = scConfig.getInitParameter(strName);
          prpSettings.put(strName, strValue);
       }
       
       return prpSettings;
    }
 
    /**
     * Get names and values for all the init parameters from the specified 
     * filter config.
     * 
     * @param  fcConfig - config from where to retrieve the init parameters
     * @return Properties - names and values of the init parameters or empty
     *                      properties if no init parameters are specified
     */
    public static Properties getInitParameters(
       FilterConfig fcConfig
    )
    {
       Properties prpSettings = new Properties();
       String     strName;
       String     strValue;
       
       for (Enumeration paramNames = fcConfig.getInitParameterNames();
            paramNames.hasMoreElements();)
       {
          strName = (String)paramNames.nextElement();
          strValue = fcConfig.getInitParameter(strName);
          prpSettings.put(strName, strValue);
       }
       
       return prpSettings;
    }
 }
