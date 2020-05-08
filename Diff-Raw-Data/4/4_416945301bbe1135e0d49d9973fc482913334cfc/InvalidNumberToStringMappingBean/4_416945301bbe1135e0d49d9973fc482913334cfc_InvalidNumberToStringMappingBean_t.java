package ch.aeberhardo.xlsx2beans.converter.beans;

import ch.aeberhardo.xlsx2beans.converter.XlsxColumnName;
 
 public class InvalidNumberToStringMappingBean {
 
 	@XlsxColumnName("MyInteger")
 	public void setMyString(String myString) {
 		// nothing
 	}
 
 }
