 package requirejsExtractor;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Queue;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import model.RequireJsModule;
 import model.Tupel;
 
 public class Extractor {
 	private static Pattern NAMED_REQUIRE_REGEX = Pattern.compile("([a-zA-Z][a-zA-Z0-9]*)[\\s]*= require\\(\"(.*)\"\\)");
 	private static Pattern ANON_REQUIRE_REGEX = Pattern.compile("^[\\s]*require\\(\"(.*)\"\\)");
 	private Set<String> traversedFiles;
 	private String baseFolder;
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		String filename = args[0];
 		String baseFolder = "";
 		int slashIndex = filename.lastIndexOf('/'); 
 		if(slashIndex >= 0) {
 			baseFolder = filename.substring(0, slashIndex + 1);
 			filename = filename.substring(slashIndex + 1);
 		}
 		Extractor extract = new Extractor(baseFolder);
 		RequireJsModule tree = extract.extractModules(filename);
		System.out.println(tree.getTreeString(0));
 	}
 	
 	public Extractor(String baseFolder) {
 		this.traversedFiles = new HashSet<String>();
 		this.baseFolder = baseFolder;
 	}
 	
 	public RequireJsModule extractModules(String id) {
 		// create module
 		RequireJsModule module = new RequireJsModule(id);
 		
 		// check whether we already traversed it (to avoid recursion)
 		if(traversedFiles.contains(id)) {
 			return module;
 		} else {
 			traversedFiles.add(id);
 		}
 		
 		// read module
 		Queue<Tupel<String,String>> dependencies = readModule(id);
 		
 		// extract dependencies
 		for (Tupel<String,String> dependency : dependencies) {
 			RequireJsModule depModule = extractModules(dependency.b);
 			if(depModule != null) {
 				if(dependency.a != null) 
 					module.addDependency(dependency.a, depModule);
 				else
 					module.addDependency(depModule);
 			}
 		}
 		
 		return module;
 	}
 	
 	private Queue<Tupel<String,String>> readModule(String id) {
 		Queue<Tupel<String,String>> dependencies = new LinkedList<Tupel<String,String>>();
 		try {
 			String filename = this.baseFolder + id + ".js";
 			BufferedReader reader = new BufferedReader(new FileReader(filename));
 			
 			String line = null;
 			do {
 				line = reader.readLine();
 				if(line != null) {
 					String varName = null;
 					String dependency = null;
 					
 					Matcher match = NAMED_REQUIRE_REGEX.matcher(line);
 					if(match.find()) {
 						varName = match.group(1);
 						String tentativeDependency = match.group(2);
 						if(!tentativeDependency.startsWith("text!") && !tentativeDependency .startsWith("i18n!")) {
 							dependency = tentativeDependency;
 						}
 					} else {
 						match = ANON_REQUIRE_REGEX.matcher(line);
 						if(match.find()) {
 							String tentativeDependency = match.group(1);
 							if(!tentativeDependency .startsWith("text!") && !tentativeDependency .startsWith("i18n!")) {
 								dependency = tentativeDependency;
 							}
 						}
 					}
 					if(dependency != null) {
 						dependencies.add(new Tupel<String,String>(varName, dependency));
 					}
 				}
 			} while (line != null);
 			
 			reader.close();
 		} catch (IOException iox) {
 			iox.printStackTrace();
 		}
 		return dependencies;
 	}
 }
