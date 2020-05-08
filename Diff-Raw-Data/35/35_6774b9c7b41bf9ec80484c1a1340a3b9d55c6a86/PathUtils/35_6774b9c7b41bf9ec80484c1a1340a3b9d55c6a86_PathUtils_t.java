 package org.mxunit.eclipseplugin.utils;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.Collection;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOCase;
 import org.apache.commons.io.filefilter.TrueFileFilter;
 import org.apache.commons.io.filefilter.WildcardFileFilter;
 import org.mxunit.eclipseplugin.MXUnitPluginLog;
 
 public final class PathUtils {
 	
 	/**
 	 * Given the web root and the component root (always assumed to be underneath the 
 	 * webroot...if it's not, you're screwed.), returns the component path
 	 * as coldfusion would see it. For example, turns c:\inetpub\wwwroot\com\my\file.cfc
 	 * into com.my.file
 	 * @param webroot the webroot. parent of the CFC in question
 	 * @param component full path to CFC under the webroot
 	 * @return String representing coldfusion cfc path for this component
 	 */
 	public static String deriveComponentPath(String webroot, String component){
 		return deriveComponentPath(webroot, component, File.separator);
 	}
 	
 	/**
 	 * convenience method for use in unit tests for testing unix-style paths
 	 * @param webroot the webroot. parent of the CFC in question
 	 * @param component full path to CFC under the webroot
 	 * @param pathSeparator String representing the file path separator (\, /)
 	 * @return String representing coldfusion cfc path for this component
 	 */
 	public static String deriveComponentPath(String webroot, String component, String pathSeparator){
		webroot = webroot.trim();
 		//ensure the root ends with a slash
 		if(!webroot.endsWith(pathSeparator)){
 			webroot += pathSeparator;
 		}
 		
 		//convert all slashes to periods
 		webroot = webroot.replace(pathSeparator, ".");
 		component = component.replace(pathSeparator, ".");
 		
 		
 		//knock out the webroot from the component path. originally this was done with replaceFirst,
 		//but that couldn't handle the parentheses in a path like \\argus-nas3(alpha) since the parens
 		//are special cars in regex, and i didn't want to get into a cat and mouse game of replacing special chars
 		//String path = component.replaceFirst("(?i)"+webroot, "");
 		String path = component.substring(webroot.length());
 		
 		//strip extension
 		path = path.replaceFirst("(?i)\\.cfc$", "");	
 		
 		return path;
 	}
 	
 	
 	public static String deriveComponentPathFromComponentRoot(String componentRoot, String topLevelParentDirectory, String component){	    
 	    return deriveComponentPathFromComponentRoot(componentRoot, topLevelParentDirectory, component, File.separator);
 	}
 	
 	public static String deriveComponentPathFromComponentRoot(String componentRoot, String topLevelParentDirectory, String component, String fileSeparator){
 		//strip out the project root from the component path		
         String stripped = component.substring(topLevelParentDirectory.length());        
         //System.out.println(stripped);
         //strip extension
         stripped = stripped.replaceFirst("(?i)\\.cfc$", "");   
        
         //replace all slashes with periods
         stripped = stripped.replaceAll("\\\\|/", ".");      
         String path = componentRoot + "." + stripped;       
         //replace all double periods
         path = path.replaceAll("\\.{2,}", ".");
         //System.out.println(path);
         return path;
 	}
 	
 	public static String deriveComponentPathRelativeToProject(String componentRoot, String component){
 		return deriveComponentPathRelativeToProject(componentRoot, component, File.separator);
 	}
 	public static String deriveComponentPathRelativeToProject(String componentRoot, String component, String fileSeparator){
 		String stripped  = component.replaceFirst("(?i)\\.cfc$", "");   
        
         //replace all slashes with periods
         stripped = stripped.replaceAll("\\\\|/", ".");   
         String path = componentRoot + "." + stripped;
         path = path.replaceAll("\\.{2,}", ".");
 		return path;
 	}
 	
 	
 	public static Collection<File> getTestComponents(File dir){
 		String[] wildcards = new String[]{"test*.cfc","*test.cfc"};
 		WildcardFileFilter filter = new WildcardFileFilter(wildcards,IOCase.INSENSITIVE);
 		Collection<File>files = FileUtils.listFiles( dir, filter, TrueFileFilter.INSTANCE);		
 		MXUnitPluginLog.logInfo("PathUtils.getTestComponents(), returning " + files.size() + " files matching filter string " + Arrays.toString(wildcards));
 		return files;
 	}
 	
 }
