 package ua.krem.agent.model;
 
 import java.io.Serializable;
 
 @SuppressWarnings("serial")
 public class Code implements Serializable{
 	
 	private String code;
 
 	public String getCode() {
		if( code.contains("code: ")){
			code = code.substring(code.indexOf("code: ") + 5).trim();
		}
		System.out.println("code: " + code);
 		return code;
 	}
 
 	public void setCode(String code) {
 		this.code = code;
 	}
 
 }
