 /*
  * $Id$
  *
  * Copyright 2003-2007 Orange Nederland Breedband B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.server;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.XML;
 
 import org.xins.common.collections.BasicPropertyReader;
 import org.xins.common.text.ParseException;
 import org.xins.common.text.TextUtils;
 import org.xins.common.xml.Element;
 import org.xins.common.xml.ElementParser;
 
 /**
  * The JSON calling convention.
  *
  * This version support Yahoo style JSON calls.
  * For the definition of the calling convention, look at
  * http://developer.yahoo.com/common/json.html
  *
  * This calling convention could be used for example with Google Web Toolkit.
  * For an example, look at
  * http://code.google.com/webtoolkit/documentation/examples/jsonrpc/
  *
  * @since XINS 2.0.
  * @version $Revision$ $Date$
  * @author <a href="mailto:anthony.goubard@orange-ftgroup.com">Anthony Goubard</a>
  */
 public class JSONCallingConvention extends CallingConvention {
 
    /**
     * The response encoding format.
     */
    protected static final String RESPONSE_ENCODING = "UTF-8";
 
    /**
     * The content type of the HTTP response.
     */
   protected static final String RESPONSE_CONTENT_TYPE = "text/javascript; charset=" + RESPONSE_ENCODING + "";
 
    protected String[] getSupportedMethods() {
       return new String[] { "GET", "POST" };
    }
 
    protected boolean matches(HttpServletRequest httpRequest) {
 
       String pathInfo = httpRequest.getPathInfo();
       return "json".equals(httpRequest.getParameter("output")) &&
             !TextUtils.isEmpty(pathInfo) && !pathInfo.endsWith("/");
    }
 
    protected FunctionRequest convertRequestImpl(HttpServletRequest httpRequest)
    throws InvalidRequestException, FunctionNotSpecifiedException {
 
       // Parse the parameters in the HTTP request
       BasicPropertyReader params = gatherParams(httpRequest);
 
       // Remove all invalid parameters
       cleanUpParameters(params);
 
       // Determine function name
       String pathInfo = httpRequest.getPathInfo();
       if (TextUtils.isEmpty(pathInfo) || pathInfo.endsWith("/")) {
          throw new FunctionNotSpecifiedException();
       }
       String functionName = pathInfo.substring(pathInfo.lastIndexOf("/") + 1);
 
       Element dataElement = null;
       String dataString = httpRequest.getParameter("_data");
      if (!TextUtils.isEmpty(dataString)) {
          try {
             JSONObject dataSectionObject = new JSONObject(dataString);
             String dataSectionString = XML.toString(dataSectionObject);
             dataElement = new ElementParser().parse(dataSectionString);
          } catch (JSONException jsonex) {
             throw new InvalidRequestException("Invalid JSON input data section.", jsonex);
          } catch (ParseException pex) {
             throw new InvalidRequestException("Invalid XML created from JSON object.", pex);
          }
       }
 
       return new FunctionRequest(functionName, params, dataElement);
    }
 
    protected void convertResultImpl(FunctionResult      xinsResult,
                                     HttpServletResponse httpResponse,
                                     HttpServletRequest  httpRequest)
    throws IOException {
 
       // Send the XML output to the stream and flush
       httpResponse.setContentType(RESPONSE_CONTENT_TYPE);
       PrintWriter out = httpResponse.getWriter();
       httpResponse.setStatus(HttpServletResponse.SC_OK);
 
       try {
          JSONObject jsonResult = JSONRPCCallingConvention.createResultObject(xinsResult);
          if (xinsResult.getErrorCode() != null) {
             jsonResult.put("errorCode", xinsResult.getErrorCode());
          }
          String callback = httpRequest.getParameter("callback");
          if (!TextUtils.isEmpty(callback)) {
             out.print(callback + "(");
          }
         String jsonString = jsonResult.toString();
         out.print(jsonString);
          if (!TextUtils.isEmpty(callback)) {
             out.print(")");
          }
       } catch (JSONException jsonex) {
          throw new IOException(jsonex.getMessage());
       }
 
       out.close();
    }
 }
