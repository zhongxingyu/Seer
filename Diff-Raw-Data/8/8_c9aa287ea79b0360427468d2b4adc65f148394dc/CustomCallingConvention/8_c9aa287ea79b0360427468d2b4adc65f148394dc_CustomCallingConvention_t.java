 /*
  * $Id$
  *
  * Copyright 2003-2005 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.server;
 
 import java.io.IOException;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * Abstraction of a custom calling convention. A custom calling convention
  * can be extended to create your own calling convention.
  *
  * <p>This class is abstract, to create your own colling convention you need
  * to implements the method .</p>
  *
  * @version $Revision$ $Date$
  * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
  */
 public abstract class CustomCallingConvention extends CallingConvention {
    
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>CustomCallingConvention</code> object.
     */
    public CustomCallingConvention() {
    }
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
    * Converts an HTTP request to a XINS request (implementation method).
    * This method should be implemented by your calling convention.
     *
     * @param httpRequest
     *    the HTTP request, will not be <code>null</code>.
     *
     * @return
     *    the XINS request object, should not be <code>null</code>.
     *
     * @throws InvalidRequestException
     *    if the request is considerd to be invalid.
     *
     * @throws FunctionNotSpecifiedException
     *    if the request does not indicate the name of the function to execute.
     */
    protected abstract FunctionRequest convertRequestImpl(HttpServletRequest httpRequest)
    throws InvalidRequestException,
           FunctionNotSpecifiedException;
 
    /**
     * Converts a XINS result to an HTTP response (implementation method).
    * This method should be implemented by your calling convention.
     *
     * @param xinsResult
     *    the XINS result object that should be converted to an HTTP response,
     *    will not be <code>null</code>.
     *
     * @param httpResponse
     *    the HTTP response object to configure, will not be <code>null</code>.
     *
     * @param httpRequest
     *    the HTTP request, will not be <code>null</code>.
     *
     * @throws IOException
     *    if calling any of the methods in <code>httpResponse</code> causes an
     *    I/O error.
     */
    protected abstract void convertResultImpl(FunctionResult      xinsResult,
                                              HttpServletResponse httpResponse,
                                              HttpServletRequest  httpRequest)
    throws IOException;
 
 }
