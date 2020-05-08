 import org.python.core.*;
 import org.python.util.PythonInterpreter;
 
 class PythonHandler {
 	PythonInterpreter interpreter;
 	
 	public PythonHandler() {
 		interpreter = new PythonInterpreter();
 	}
 	
 	public void invokeParser(String vcfFilePath) {
 		if (!fileExists(vcfFilePath)) {
 			print("Fatal Error: file does not exist");
 			return;
 		}
 		interpreter.exec("import sys");
 		//interpreter.execfile("/home/git-vcf/src/VcfParser/loadvcf");
 	}
 	
 	private boolean fileExists(String filePath) {
 		File f = new File(filePath);
 		return f.exists();
 	}
 	
 }
