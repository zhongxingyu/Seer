 package net.frontlinesms.plugins.httptrigger.httplistener.groovy;
 
 import java.io.File;
 
 import net.frontlinesms.resources.ResourceUtils;
 
 /**
  * Class that decides which script to use according to a specific URL
  * 
  * @author Gonçalo Silva
  * @author Alex Anderson <alex@frontlinesms.com>
  */
 class ScriptFinder {
	private final File scriptDirectory;
	
//> CONSTRUCTORS
	public ScriptFinder() {
		this.scriptDirectory = new File(ResourceUtils.getConfigDirectoryPath(), "httptrigger.scripts");
	}
 	
 //> INSTANCE METHODS
 	/**
 	 * Determines which script to call with the passed URI
 	 * 
 	 * @param The URI path for the required script
 	 * @return A {@link File} instance with the script's path
 	 * @throws {@link IllegalArgumentException} if the path starts with a '/'
 	 */
 	public File mapToFile(String path) {
 
 		if(path == null || path.length() == 0){
 			path = "index.groovy";
 		} else {
 			if(path.charAt(0) == '/'){
 				throw new IllegalArgumentException();
 			}
 	
 			while(path.endsWith("/")){
 				path = path.substring(0, path.length() - 1);
 			}
 			
 			if (!path.endsWith(".groovy")) {
 				path += ".groovy";
 			}
 		}
 		
 		return new File(getScriptDirectory(), path);
 	}
 
 	/** @return the root directory that scripts are stored in */
	private File getScriptDirectory() {
		return this.scriptDirectory;
 	}
 }
