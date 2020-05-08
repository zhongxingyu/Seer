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
 }
