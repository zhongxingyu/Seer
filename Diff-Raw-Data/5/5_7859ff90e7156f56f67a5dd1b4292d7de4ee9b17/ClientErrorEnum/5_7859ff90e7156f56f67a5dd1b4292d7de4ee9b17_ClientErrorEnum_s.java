 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cz.muni.fi.pa1685.pujcovnaStroju.restclient.util;
 
 /**
  *
  * @author gitti
  */
 public enum ClientErrorEnum {
     TIMEOUT_ERROR("Operation timed out."),
     CONNECTION_ERROR("Connection to server failed."),
     PARSE_ERROR("Invalid command, help can by displayed by typing 'help'."),
     TIMEOUT_NUMBER_FORMAT_ERROR("Timeout value hasn't been changed. "
	    + "Wrong number format was given.");
 
     private String errorString;
     
     private ClientErrorEnum(String errorString) {
 	this.errorString = errorString;
     }
     
     public String errorString() {
 	return this.errorString;
     }
 }
