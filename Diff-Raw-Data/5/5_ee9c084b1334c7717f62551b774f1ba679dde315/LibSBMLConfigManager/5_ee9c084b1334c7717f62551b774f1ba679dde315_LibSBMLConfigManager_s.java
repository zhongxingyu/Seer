 package uk.ac.ed.inf.Metabolic.sbmlexport;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 public class LibSBMLConfigManager {
 	
 	private static boolean isWindows() {
 		return System.getProperty("os.name").indexOf("Win") != -1;
 	}
 	static int i;
 	static boolean isLoaded;
 
 	private static String getPath() throws Exception {
 		InputStream is = LibSBMLConfigManager.class.getResourceAsStream("PathToNativeSBMLLibraries.setUp");
 		BufferedReader br = new BufferedReader(new InputStreamReader(is));
 		String rc = br.readLine();
 		return rc;
        
 	}
 	/**
 	 * 
 	 * @return true if sbml libraries loaded ok
 	 */
 	static boolean configure () {
 		try {
 		String libName = System.mapLibraryName("sbmlj");
 		System.loadLibrary(libName);
 		return true;
		}catch(Exception e) {
 		 try{ 
 			 String path = getPath();
 			 if(isWindows()) {
 				 System.load(path + "sbmlj.dll");
     	    
 				 return true;
     		
 			 } else {
 				 return false;
 			 }
		 }catch (Exception e2) {
 			 return false;
 		 }
 		}
   
 	}
 
 
 }
