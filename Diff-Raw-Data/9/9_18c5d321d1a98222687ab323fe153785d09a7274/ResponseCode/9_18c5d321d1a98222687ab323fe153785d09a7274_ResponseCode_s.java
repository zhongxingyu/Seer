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
 	OK(200, "The operation was completed successfully"),
 
 	/** ACCEPTED **/
 	ACCEPTED(
 			202,
 			"The operation has been accepted and is being processed, but processing is not complete yet."),
 
 	/** BAD_REQUEST **/
 	BAD_REQUEST(
 			400,
 			"The request is not understood by the server, has bad format, is missing some required parameters or is otherwise not acceptable by the server"),
 
 	/** UNAUTHORIZED **/
 	UNAUTHORIZED(401,
 			"Authentication error, the user is unauthorized to perform the action"),
 
 	/** NOT_FOUND **/
 	NOT_FOUND(404, "Resource does not exist"),
 
 	/** METHOD_NOT_ALLOWED **/
 	METHOD_NOT_ALLOWED(
 			405,
 			"Method is not implemented by this edition of Zend Server (e.g. cluster related methods on Zend Server)"),
 
 	/** NOT_ACCEPTED **/
 	NOT_ACCEPTED(
 			406,
			"The server does not support any of the content types or API versions specified by the client in the “Accept” header"),
 
 	/** INTERNAL_SERVER_ERROR **/
 	INTERNAL_SERVER_ERROR(500,
 			"An error has occurred on the server while processing the request"),
 
 	/** SERVICE_UNAVAILABLE **/
 	SERVICE_UNAVAILABLE(503,
 			"A temporary situation is preventing the server from fulfilling the request"),
 
 	/** SERVICE_UNAVAILABLE **/
 	UNKNOWN(0,
 			"An unknown response code");
 	
 	
 	private final int code;
 	private final String description;
 
 	private ResponseCode(int code, String message) {
 		this.code = code;
 		this.description = message;
 	}
 
 	public int getCode() {
 		return code;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public static ResponseCode byCode(int code) {
 		final ResponseCode[] values = values();
 		for (int i = 0; i < values.length; i++) {
 			ResponseCode responseCode = values[i];
 			if (responseCode.getCode() == code) {
 				return responseCode;
 			}
 		}
 		return UNKNOWN;
 	}
 }
