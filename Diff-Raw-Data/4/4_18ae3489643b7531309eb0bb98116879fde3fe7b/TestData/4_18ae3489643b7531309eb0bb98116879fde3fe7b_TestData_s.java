 package org.alder.fotobuchconvert.ifolor;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public class TestData {
 	private final static Log log = LogFactory.getLog(TestData.class);
 
 	public static ProjectPath getTestProject() {
 		final String configFile = "testpaths.txt";
 		try {
 			InputStream is = TestData.class.getClassLoader()
 					.getResourceAsStream(configFile);
 			BufferedReader ir = new BufferedReader(new InputStreamReader(is));
 
 			String line;
 			while ((line = ir.readLine()) != null) {
 				if (line.isEmpty() || line.startsWith("#"))
 					continue;
 
 				File file = new File(line);
 				if (!file.exists())
 					continue;
 
 				// if (line.endsWith(".xml"))
 				// line = line.substring(0, line.length() - 4);
 				// if (line.endsWith(".dpp"))
 				// line = line.substring(0, line.length() - 4);
 
 				log.info("Using Test Data: " + file);
 
 				return new ProjectPath(file);
 			}
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 
 		throw new RuntimeException("No test file found (check " + configFile
 				+ ")");
 	}
 
 	public static File getTestOutputPath() {
 		if (isWindows())
 			return new File("C:\\Temp\\scribustest.sla");
 		else
 			return new File("/tmp/scribustest.sla");
 	}
 
 	private static boolean isWindows() {
 		String os = System.getProperty("os.name").toLowerCase();
 		return (os.indexOf("win") >= 0);
 	}
 
 	public static void main(String[] args) {
 		ProjectPath pp = getTestProject();
 		System.out.println(pp.projectFile);
 		System.out.println(pp.projectFolder);
 	}
 }
