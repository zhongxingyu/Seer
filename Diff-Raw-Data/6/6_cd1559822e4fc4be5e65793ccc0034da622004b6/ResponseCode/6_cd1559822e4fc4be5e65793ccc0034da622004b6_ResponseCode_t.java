 /*******************************************************************************
  * Copyright (c) Jan 27, 2011 Zend Technologies Ltd. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  *******************************************************************************/
 package org.zend.webapi.core.connection.response;
 
 /**
  * Response codes
  */
 public enum ResponseCode {
 
 	/** OK **/
 	OK(200, "ok", "The operation was completed successfully"),
 
 	/** ACCEPTED **/
 	ACCEPTED(202, "accepted",
 			"The operation has been accepted and is being processed, but processing is not complete yet."),
 
 	MISSING_HTTP_HEADER(400, "missingHttpHeader", "A required HTTP header is missing"),
 
 	UNEXPECTED_HTTP_METHOD(400, "unexpectedHttpMethod",
 			"Unexpected HTTP method, GET used but POST expected"),
 
 	INVALID_PARAM(400, "invalidParam",
 			"One or more request parameter contains invalid data"),
 			
 	INVALID_PARAMETER(400, "invalidParameter",
 			"One or more request parameter contains invalid data"),
 
 	MISSING_PARAMETER(400, "missingParam", "Request is missing a required parameter"),
 
 	MISSING_VIRTUAL_HOST(400, "missingVirtualHost",
 			"The virtual host in the baseUrl does not exist. Use createVhost flag to create it"),
 
 	UNKNOWN_METHOD(400, "unknownMethod", "Unknown Zend Server API method"),
 
 	MALFORMED_REQUEST(400, "malformedRequest", "The server is unable to understand the request"),
 
 	AUTH_ERROR(401, "authError", "Authentication error, unknown key or invalid request signature"),
 
 	INSUFFICIENT_ACCESS_LEVEL(401, "insufficientAccessLevel",
 			"User is not authorized to perform this action"),
 			
 	NO_XML_UNAUTORIZED(
 			401,
 			"noXmlUnautorized",
 			"Communication with Zend Server failed due to a problem accessing it's web interface. Response code: 401 \"Unathorized\"."),
 
 	TIME_SKEW_ERROR(401, "timeSkewError", "Request timestamp deviates too much from server time"),
 
 	DIRECT_ACCESS_FORBIDDEN(403, "directAccessForbidden",
 			"Direct access to a ZSCM managed Server is not allowed"),
 
 	NO_SUCH_APPLICATION(404, "noSuchApplication", "The provided application ID does not exist"),
 
 	NO_SUCH_FILTER_ID(404, "noSuchFilterId",
 			"The requested filter does not exist"),
 
 	NO_SUCH_ISSUE(404, "noSuchIssue", "The requested issue does not exist"),
 
 	NO_ROLLBACK_AVAILABLE(404, "noRollbackAvailable",
 			"The application does not have a version to rollback to"),
 			
 	NO_SUCH_TRACE(404, "noSuchTrace", "The requested trace could not be found"),
 	
 	PAGE_NOT_FOUND(404, "pageNotFound", "Cannot connect with specified URL"),
 
 	NOT_IMPLEMENTED_BY_EDITION(405, "notImplementedByEdition",
 			"Method is not implemented by this edition of Zend Server (e.g. cluster related methods on Zend Server)"),
 
 	UNSUPPORTED_API_VERSION(406, "unsupportedApiVersion",
 			"API version is not supported by this version of Zend Server"),
 
 	BASE_URL_CONFLICT(409, "baseUrlConflict", "Provided base URL is already in use"),
 
 	APPLICATION_CONFLICT(
 			409,
 			"applicationConflict",
 			"Provided application package includes a different application, and not a different version of the existing application"),
 
	LIBRARY_CONFLICT(
			409,
			"libraryAlreadyExists",
			"Could not deploy library: Library with the same name and version already exists"),
	
			
 	SERVER_NOT_CONFIGURED(
 			500,
 			"serverNotConfigured",
 			"This Zend Server installation was not yet initialized (user did not go through the initial setup wizard)"),
 
 	SERVER_NOT_LICENSED(500, "serverNotLicensed",
 			"Server does not have a valid license which is required to perform this operation"),
 
 	SERVER_VERSION_MISMATCH(
 			500,
 			"serverVersionMismatch",
 			"One or more servers in the cluster has a Zend Server version which does not support deployment feature"),
 	
 	INTERNAL_SERVER_ERROR(
 			500,
 			"internalServerError",
 			"The server encountered an internal error or misconfiguration and was unable to complete your request. Verify if your license has not expired."),		
 			
 	UNKNOWN(
 			0,
 			"unknown",
 			"Unknown Response Code");
 	
 	
 	private final int httpCode;
 	private final String errorCode;
 	private final String description;
 
 	private ResponseCode(int httpCode, String errorCode, String message) {
 		this.httpCode = httpCode;
 		this.errorCode = errorCode;
 		this.description = message;
 	}
 
 	public int getCode() {
 		return httpCode;
 	}
 
 	public String getErrorCode() {
 		return errorCode;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public static ResponseCode byErrorCode(String errorCode) {
 		final ResponseCode[] values = values();
 		for (int i = 0; i < values.length; i++) {
 			ResponseCode responseCode = values[i];
 			if (responseCode.getErrorCode().equals(errorCode)) {
 				return responseCode;
 			}
 		}
 		return UNKNOWN;
 	}
 
 	/**
 	 * Returns ResponseCode for specified HTTP code. HTTP codes are not unique
 	 * for different errors so this method may return incorrect ResponseCode
 	 * (but with the correct HTTP code) for the error HTTP codes.
 	 * 
 	 * @param errorCode
 	 * @return
 	 */
 	public static ResponseCode byHttpCode(int httpCode) {
 		final ResponseCode[] values = values();
 		for (int i = 0; i < values.length; i++) {
 			ResponseCode responseCode = values[i];
 			if (responseCode.getCode() == httpCode) {
 				return responseCode;
 			}
 		}
 		return UNKNOWN;
 	}
 }
