 package org.zend.php.zendserver.deployment.core.internal.validation;
 
 import java.io.File;
 
import org.zend.php.zendserver.deployment.core.internal.descriptor.Feature;

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
			if ("".equals(s.trim())) {
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
