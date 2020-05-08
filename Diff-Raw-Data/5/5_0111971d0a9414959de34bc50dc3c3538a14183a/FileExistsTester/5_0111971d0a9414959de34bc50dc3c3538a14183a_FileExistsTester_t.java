 package org.zend.php.zendserver.deployment.core.internal.validation;
 
 import java.io.File;
 
 public class FileExistsTester extends PropertyTester {
 
 	public FileExistsTester() {
 		super(ValidationStatus.ERROR);
 	}
 
 	@Override
 	String test(Object value) {
 		if (value == null) {
 			return null;
 		}
 		if (value instanceof String) {
 			String s = (String) value;
			if (s.trim().length() == 0) {
 				return null;
 			}
 			File f = new File(s);
 			if (f.exists()) {
 				return null;
 			}
 		}
 		
 		return "File does not exist.";
 	}
 
 }
