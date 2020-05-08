
<!-- saved from url=(0067)http://github.com/stpettersens/Gaudi/raw/master/dist/JavaCheck.java -->
<html><body><pre style="word-wrap: break-word; white-space: pre-wrap;">/*
  Java version checker
  @author Sam Saint-Pettersen, 2010
  Released into the public domain.
 	
  Originally written for use with 
  the NSIS installation script for Gaudi.
 	
  But use as you like. No warranty.
 	
  Usage: java -cp . JavaCheck
  Output: Java vendor (e.g. Sun Microsystems Inc.)
  Exit code: -1 (neither true or false)
 	
 Usage: java -cp . JavaCheck &lt;minimal version&gt;
  Output: x.x.x_x (Java version)
  Exit code: 0 (false) / 1 (true)(Java installed, is minimal version or greater)
 */
 import java.util.regex.*;
 
 class JavaCheck {
 	public static void main(String[] args) {
 		byte returned = -1; // Return exit code -1 for neither true or false; default assumption
 		String detectedVer = System.getProperty("java.version");
 		String vendor = System.getProperty("java.vendor");
 		
 		// When minimal version argument is provided, return either exit code 0 or 1
 		if(args.length == 1) {
 			System.out.print(detectedVer);
 			Pattern p = Pattern.compile("\\d\\.\\d");
 			Matcher m = p.matcher(detectedVer); m.find();
 			float minimalVersion = Float.valueOf(args[0]).floatValue();
 			float detec_versionNum = Float.valueOf(m.group(0)).floatValue();
			if(detec_versionNum &gt;= minimalVersion) {
 				returned = 1; // Return exit code 1 for true
 			}
 			else returned = 0; // Return exit code 0 for false
 		}
 		else System.out.print(vendor);
 		
 		System.exit(returned);
 	}
 }
</pre></body></html>
