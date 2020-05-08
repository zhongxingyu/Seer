 package controllers;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import play.Logger;
 import play.api.Play;
 import play.mvc.Controller;
 import play.mvc.Result;
 import views.html.index;
 
 /**
  * This controller is supposed to simply expose the index view.
  * 
  * @author Tiago Garcia
  * @see http://github.com/tiagorg
  */
 public class Application extends Controller {
 
 	/**
 	 * This method will render the index view.
 	 * 
 	 * @return the result
 	 */
 	public static Result index() {
 		List<String> stylesheetsList = getIncludes("conf/stylesheets", "stylesheets");
 		List<String> scriptsList = getIncludes("conf/scripts", "javascripts");
 
 		return ok(index.render(stylesheetsList, scriptsList));
 	}
 
 	/**
	 * This method will route to the index view.
	 * 
	 * @return the result
	 */
	public static Result routeToIndex(String path) {
		return index();
	}

	/**
 	 * Reads a includes file and returns as a list of qualified filenames
 	 * 
 	 * @param includeFile
 	 *           the file containing the includes
 	 * @param includeFolder
 	 *           the folder where the included files will be retrieved from
 	 * @return a list of qualified filenames
 	 */
 	private static List<String> getIncludes(String includeFile,
 			String includeFolder) {
 		List<String> scriptsList = new ArrayList<String>();
 
 		try {
 			String line = null;
 			BufferedReader scriptsFileReader = new BufferedReader(new FileReader(
 					Play.current().getFile(includeFile)));
 
 			while ((line = scriptsFileReader.readLine()) != null) {
 				line = line.trim();
 				if (!line.isEmpty() && line.charAt(0) != '#') {
 					File file = Play.current().getFile(
 							"app/assets/" + includeFolder + "/" + line);
 					if (file.exists() == false) {
 						file = Play.current().getFile(
 								"public/" + includeFolder + "/" + line);
 					}
 					addFile(file, scriptsList, includeFolder + "/");
 				}
 			}
 
 			scriptsFileReader.close();
 		} catch (Exception e) {
 			Logger.error(e.getLocalizedMessage());
 		}
 		return scriptsList;
 	}
 
 	/**
 	 * Recursively adds a file to the includes list
 	 * 
 	 * @param file
 	 *           the file
 	 * @param includesList
 	 *           the includes list
 	 * @param parentDirectory
 	 *           the parent directory
 	 */
 	private static void addFile(File file, List<String> includesList,
 			String parentDirectory) {
 		if (file.isDirectory()) {
 			parentDirectory += file.getName() + "/";
 			File[] childrenFiles = file.listFiles();
 			for (File childrenFile : childrenFiles) {
 				addFile(childrenFile, includesList, parentDirectory);
 			}
 		}
 		else {
 			includesList.add(parentDirectory + file.getName());	
 		}
 	}
 
 }
