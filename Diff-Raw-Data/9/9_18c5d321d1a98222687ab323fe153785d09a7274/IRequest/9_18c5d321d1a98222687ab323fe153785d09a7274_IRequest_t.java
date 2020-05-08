 /*******************************************************************************
  * Copyright (c) Jan 25, 2011 Zend Technologies Ltd. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  *******************************************************************************/
 package org.zend.webapi.core.connection.request;
 
 import java.util.Date;
 import java.util.List;
 
 import org.restlet.Request;
 import org.restlet.data.Method;
 import org.zend.webapi.core.connection.data.IResponseData.ResponseType;
 import org.zend.webapi.core.connection.data.values.WebApiVersion;
 import org.zend.webapi.internal.core.connection.auth.signature.SignatureException;
 
 /**
  * Represents the request to the server
  * 
  * @author Roy, 2011
  * 
  */
 public interface IRequest {
 
 	/**
 	 * @return supported API version
 	 */
 	public abstract WebApiVersion getVersion();
 
 	/**
 	 * @return The current date and time in the GMT time zone, in the format
 	 *         specified by the HTTP RFC for date fields (e.g. “Wed, 07 Jul 2010
	 *         17:10:55 GMT). This value will be used to verify the
 	 *         authenticity of the request, and is expected to be in sync with
 	 *         the server time up to the accuracy of 30 seconds.
 	 */
 	public abstract Date getDate();
 
 	/**
 	 * @return The user agent string will be logged by the server and used for
 	 *         message authenticity verification. It must not be empty.
 	 */
 	public abstract String getUserAgent();
 
 	/**
 	 * @return The HTTP host header is expected to be present, and will be used
 	 *         for message authenticity verification
 	 */
 	public abstract String getHost();
 
 	/**
 	 * @return The API key name and calculated request signature to be used for
 	 *         authenticating and validating the request. See section
 	 *         Authentication and Message Verification for additional
 	 *         information on calculating the signature
 	 * @throws SignatureException
 	 */
 	public abstract String getSignature() throws SignatureException;
 
 	/**
 	 * API key will also determine the access level granted when using this key.
 	 * API keys will be created by generating a 256 bit random number using a
 	 * cryptographic grade random number generation method, and encoding it as a
 	 * 64 digit hexadecimal number with digits a-f in lower case.
 	 * 
 	 * @return String the key name
 	 */
 	public abstract String getKeyName();
 
 	/**
 	 * @return the URI that will be used for this request
 	 */
 	public abstract String getUri();
 
 	/**
 	 * @return the secret key of the client
 	 */
 	public abstract String getSecretKey();
 
 	/**
 	 * @return the method of the request
 	 */
 	public abstract Method getMethod();
 
 	/**
 	 * @return the content type of the request null if no content type is
 	 *         required
 	 */
 	public abstract String getContentType();
 
 	/**
 	 * @return the list of parameters provided in this request
 	 */
 	public abstract List<RequestParameter<?>> getParameters();
 
 	/**
 	 * @param code
 	 * @return the expected response codes
 	 */
 	public abstract boolean isExpectedResponseCode(int code);
 
 	/**
 	 * @param code
 	 * @return the expected response data type
 	 */
 	public abstract ResponseType getExpectedResponseDataType();
 
 	/**
 	 * Apply parameters of the request to the low-level request object
 	 * 
 	 * @param request
 	 *            (low level request object)
 	 */
 	public abstract void applyParameters(Request request);
 }
