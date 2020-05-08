 /*
  * $Id$
  *
  * Copyright 2003-2007 Orange Nederland Breedband B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.servlet;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 import javax.servlet.ServletRequest;
 import javax.servlet.http.HttpServletRequest;
 
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.collections.AbstractPropertyReader;
 import org.xins.common.text.ParseException;
 import org.xins.common.text.URLEncoding;
 
 /**
  * Implementation of a <code>PropertyReader</code> that returns the
  * request parameters from a <code>ServletRequest</code> object.
  *
  * @version $Revision$ $Date$
  * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
  * @author <a href="mailto:anthony.goubard@orange-ftgroup.com">Anthony Goubard</a>
  *
  * @since XINS 1.0.0
  */
 public final class ServletRequestPropertyReader
 extends AbstractPropertyReader {
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Sets a parameter to the specified value. If the parameter is already set
     * to a different value, then an exception is thrown.
     *
     * <p>This function is used during parsing of a HTTP query string, which is
     * why a {@link ParseException} is thrown in case of conflicting values.
     *
     * @param properties
     *    the set of parameters, should not be <code>null</code>.
     *
     * @param key
     *    the parameter key, should not be <code>null</code>.
     *
     * @param value
     *    the parameter value, should not be <code>null</code>.
     *
     * @throws NullPointerException
     *    if <code>properties == null</code>.
     *
     * @throws ParseException
     *    if a conflicting value is found for a certain parameter.
     */
    private static void add(Map properties, String key, String value)
    throws NullPointerException, ParseException {
 
       Object existingValue = properties.get(key);
       if (existingValue != null && ! existingValue.equals(value)) {
          String detail = "Conflicting values found for parameter \""
                        + key
                        + "\": \""
                        + (String) existingValue
                        + "\" versus \""
                        + value
                        + "\".";
          throw new ParseException("Failed to parse HTTP query string.",
                                   (Throwable) null, detail);
       }
 
       properties.put(key, value);
    }
 
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>ServletRequestPropertyReader</code> for a
     * <code>ServletRequest</code>.
     *
     * @param request
     *    the {@link ServletRequest} object, cannot be <code>null</code>.
     *
     * @throws NullPointerException
     *    if <code>request == null</code>.
     */
    public ServletRequestPropertyReader(ServletRequest request)
    throws NullPointerException {
       super(request.getParameterMap());
    }
 
    /**
     * Constructs a new <code>ServletRequestPropertyReader</code> for an
     * <code>HttpServletRequest</code>.
     *
     * @param request
     *    the {@link HttpServletRequest} object, cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>request == null</code>.
     *
     * @throws ParseException
     *    if the query string in the specified servlet request cannot be
     *    parsed.
     *
     * @since XINS 1.4.0
     */
    public ServletRequestPropertyReader(HttpServletRequest request)
    throws IllegalArgumentException, ParseException {
 
       // Initially allocate a complete HashMap already
       super(new HashMap(20));
 
       // Check preconditions
       MandatoryArgumentChecker.check("request", request);
 
       Map properties = getPropertiesMap();
 
       // Get the HTTP query string
       String query = request.getQueryString();
 
       // Parse the parameters in the HTTP query string
       try {
          StringTokenizer st = new StringTokenizer(query, "&");
          while (st.hasMoreTokens()) {
             String token = st.nextToken();
             int equalsPos = token.indexOf('=');
             if (equalsPos != -1) {
                String parameterKey = URLEncoding.decode(token.substring(0, equalsPos));
                String parameterValue = URLEncoding.decode(token.substring(equalsPos + 1));
                add(properties, parameterKey, parameterValue);
             } else {
                add(properties, token, "");
             }
          }
 
       // URLEncoder.decode(String url, String enc) may throw an UnsupportedEncodingException
       // or an IllegalArgumentException
       } catch (Exception cause) {
          throw new ParseException("Failed to parse HTTP query string.",
                                   cause,
                                   "URL decoding failed.");
       }
    }
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 }
