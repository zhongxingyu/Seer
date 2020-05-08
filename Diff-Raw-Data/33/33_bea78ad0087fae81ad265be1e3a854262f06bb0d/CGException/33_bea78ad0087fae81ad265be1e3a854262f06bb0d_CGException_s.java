 /* Software License Agreement (BSD License)
  * 
  * Copyright (c) 2010-2011, Rustici Software, LLC
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in the
  *       documentation and/or other materials provided with the distribution.
  *     * Neither the name of the <organization> nor the
  *       names of its contributors may be used to endorse or promote products
  *       derived from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL Rustici Software, LLC BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package com.rusticisoftware.cheddargetter.client;
 
 public class CGException extends Exception {
 	public static final int REQUEST_INVALID = 400;
 	public static final int NOT_AUTHORIZED = 401;
 	public static final int NOT_FOUND = 404;
 	public static final int PRECONDITION_FAILED = 412;
 	public static final int DATA_INVALID = 500;
 	public static final int USAGE_INVALID = 500;
 	public static final int UNKNOWN = 500;
 	public static final int BAD_GATEWAY = 512;
 	
 	private int code = UNKNOWN;
	private int auxCode = 0;
 	
 	public int getCode(){
 		return code;
 	}
 	public void setCode(int code){
 		this.code = code;
 	}
 	
	public int getAuxCode(){
 		return auxCode;
 	}
	public void setAuxCode(int auxCode){
 		this.auxCode = auxCode;
 	}
 	
	public CGException (int code, int auxCode, String message){
 		super(message);
 		this.setCode(code);
 		this.setAuxCode(auxCode);
 	}
 	
 	public String toString(){
 		return "CGException: Code = " + getCode() +
				(auxCode == 0 ? "" : ", AuxCode = " + auxCode) +
 				", Message = " + this.getMessage();
 	}
 	
 	public boolean isGatewayError(){
		return (auxCode != 0 && (auxCode < 5000 || auxCode >= 7000));
 	}
 }
