 /*
  * $Id$
  *
  * Copyright 2003-2005 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.client;
 
 import org.xins.common.service.CallConfig;
 
 import org.xins.common.Log;
 import org.xins.common.MandatoryArgumentChecker;
 
 import org.xins.common.http.HTTPCallConfig;
 import org.xins.common.http.HTTPMethod;
 
 /**
  * Call configuration for the XINS service caller.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:ernst.dehaan@nl.wanadoo.com">ernst.dehaan@nl.wanadoo.com</a>)
  *
  * @since XINS 1.1.0
  */
 public final class XINSCallConfig extends CallConfig {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * Fully-qualified name of this class.
     */
    private static final String CLASSNAME = XINSCallConfig.class.getName();
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
    * Constructs a new <code>XINSCallConfig</code> object.
     */
    public XINSCallConfig() {
       _httpCallConfig = new HTTPCallConfig();
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The underlying HTTP call config. Cannot be <code>null</code>.
     */
    private HTTPCallConfig _httpCallConfig;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    // TODO: Override describe()
 
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
     *    the HTTP method to be associated with this configuration, can be
     *    <code>null</code>.
     */
    public void setHTTPMethod(HTTPMethod method) {
       _httpCallConfig.setMethod(method);
    }
 }
