 package com.az.dev.gittry001;
 
 public class SomeObj {
 
 	private String someObjName;
	private String someObjSecondName;
 
	public SomeObj(String someObjName, String someObjSecondName) {
 		this.someObjName = someObjName;
		this.someObjSecondName = someObjSecondName;
 	}
 	
 	public String toString(){
		return "someObjName:" + someObjName +" someObjSecondName:"+someObjSecondName;
 	}
 }
