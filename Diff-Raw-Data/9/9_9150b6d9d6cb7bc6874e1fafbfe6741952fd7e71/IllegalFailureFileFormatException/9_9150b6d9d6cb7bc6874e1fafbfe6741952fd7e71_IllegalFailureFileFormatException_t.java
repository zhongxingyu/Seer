 package com.attask.jenkins.testreport;
 
 import hudson.FilePath;
 
 import java.io.File;
 
 /**
  * User: Joel Johnson
  * Date: 1/19/13
  * Time: 3:33 PM
  */
 public class IllegalFailureFileFormatException extends RuntimeException {
 	public IllegalFailureFileFormatException(FilePath file, int lineNumber, String message) {
		super(file.getRemote() + " is not a valid Failures File. Line: " + (lineNumber+1) +" . " + message.replace("\r", "\\r"));
 	}
 }
