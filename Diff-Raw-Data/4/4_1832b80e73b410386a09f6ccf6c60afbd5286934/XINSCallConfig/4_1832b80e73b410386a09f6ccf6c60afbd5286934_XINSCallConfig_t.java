 /*
  * $Id$
  *
  * Copyright 2003-2007 Orange Nederland Breedband B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.client;
 
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.http.HTTPCallConfig;
 import org.xins.common.http.HTTPMethod;
 import org.xins.common.service.CallConfig;
 import org.xins.common.text.FastStringBuffer;
 import org.xins.common.text.TextUtils;
 
 /**
 * Call configuration for the XINS service caller. The HTTP method can be configured. 
 * By default it is set to <em>POST</em>.
  *
  * @version $Revision$ $Date$
  * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
  *
  * @since XINS 1.1.0
  */
 public final class XINSCallConfig extends CallConfig {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * The number of instances of this class. Initially zero.
     */
    private static int INSTANCE_COUNT;
 
    /**
     * Lock object for field <code>INSTANCE_COUNT</code>.
     */
    private static Object INSTANCE_COUNT_LOCK = new Object();
 
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>XINSCallConfig</code> object.
     */
    public XINSCallConfig() {
 
       // First determine instance number
       synchronized (INSTANCE_COUNT_LOCK) {
          _instanceNumber = ++INSTANCE_COUNT;
       }
 
       // Construct an underlying HTTPCallConfig
       _httpCallConfig = new HTTPCallConfig();
 
       // Configure the User-Agent header
       String userAgent = "XINS/Java Client Framework " + Library.getVersion();
       _httpCallConfig.setUserAgent(userAgent);
 
       // NOTE: HTTPCallConfig already defaults to HTTP POST
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The 1-based sequence number of this instance. Since this number is
     * 1-based, the first instance of this class will have instance number 1
     * assigned to it.
     */
    private final int _instanceNumber;
 
    /**
     * The underlying HTTP call config. Cannot be <code>null</code>.
     */
    private HTTPCallConfig _httpCallConfig;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Returns an <code>HTTPCallConfig</code> object that corresponds with this
     * XINS call configuration object.
     *
     * @return
     *    an {@link HTTPCallConfig} object, never <code>null</code>.
     */
    HTTPCallConfig getHTTPCallConfig() {
       return _httpCallConfig;
    }
 
    /**
     * Returns the HTTP method associated with this configuration.
     *
     * @return
     *    the HTTP method, never <code>null</code>.
     */
    public HTTPMethod getHTTPMethod() {
       return _httpCallConfig.getMethod();
    }
 
    /**
     * Sets the HTTP method associated with this configuration.
     *
     * @param method
     *    the HTTP method to be associated with this configuration, cannot be
     *    <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>method == null</code>.
     */
    public void setHTTPMethod(HTTPMethod method)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("method", method);
 
       // Store the setting in the HTTP call configuration
       _httpCallConfig.setMethod(method);
    }
 
    /**
     * Describes this configuration.
     *
     * @return
     *    the description of this configuration, should never be
     *    <code>null</code>, should never be empty and should never start or
     *    end with whitespace characters.
     */
    public String describe() {
 
       boolean    failOverAllowed;
       HTTPMethod method;
       synchronized (getLock()) {
          failOverAllowed = isFailOverAllowed();
          method          = _httpCallConfig.getMethod();
       }
 
       FastStringBuffer buffer = new FastStringBuffer(51);
       buffer.append("XINS call config #");
       buffer.append(_instanceNumber);
       buffer.append(" [failOverAllowed=");
       buffer.append(failOverAllowed);
       buffer.append("; method=");
       buffer.append(TextUtils.quote(method.toString()));
       buffer.append(']');
 
       return buffer.toString();
    }
 }
