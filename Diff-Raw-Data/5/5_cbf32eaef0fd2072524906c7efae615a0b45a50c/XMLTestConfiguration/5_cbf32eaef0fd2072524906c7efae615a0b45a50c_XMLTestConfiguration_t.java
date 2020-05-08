 package org.eclipse.uomo.xml.test;
 
 /**
  * note: in osx, you cannot have spaces in the name of the temporary directory
  * 
  * @author grahame
  * 
  */
 
 interface XMLTestConfiguration {
 
     /**
      * The starting location, often the path to the test suite/test file that is
      * run by the launcher. (i.e. org.eclipse.ohf.h3et.tests when running the
      * AllH3etTests)
      */
     public static final String STARTDIR = System.getenv("STARTDIR") != null ? System
 	    .getenv("STARTDIR")
 	    : "./";
 
     // don't run third party programs
     public final static boolean SKIP_THIRD_PARTY = System
 	    .getenv("SKIP_THIRD_PARTY") != null;
 
     /**
      * Third party programs
      * 
      * on Windows, we use Firefox and WinMerge
      * 
      * on OSX, we use Firefox and FileMerge (from XCode)
      */
 
     // root folder of third party programs
     public final static String BIN_DIR_WIN = System.getenv("ProgramFiles") != null ? System
 	    .getenv("ProgramFiles")
 	    : "C:/Program Files";
     public final static String BROWSER_PATH = System.getProperty("os.name")
 	    .toLowerCase().startsWith("mac os x") ? "open " : BIN_DIR_WIN
 	    + "/Mozilla FireFox/FireFox.exe";
     public final static String COMPARE_PATH = System.getProperty("os.name")
 	    .toLowerCase().startsWith("mac os x") ? "/usr/bin/opendiff" : // consult
 									  // FileMerge.app
 									  // help
 									  // doco
 									  // (part
 									  // of
 									  // XCode)
 									  // +
 									  // remember
 									  // to
 									  // make
 									  // it
 									  // executable
 	    BIN_DIR_WIN + "/WinMerge/WinMerge.exe";
 
     /**
      * Temp dir for generating files
      */
     public static final String TEMP_PATH = System.getenv("TEMP") != null ? System
 	    .getenv("TEMP")
 	    : (System.getProperty("os.name").toLowerCase().startsWith(
 		    "mac os x") ? "/temp/" : "C:/temp/");
 
    public static final String PLUGIN_ID = "org.eclipse.uomo.xml.tests";
     public static final String RESOURCE_PATH = STARTDIR + "../" + PLUGIN_ID
	    + "/src/";
 
     public static final String TEMP_FILENAME = TEMP_PATH + "/res_tmp";
 
 }
