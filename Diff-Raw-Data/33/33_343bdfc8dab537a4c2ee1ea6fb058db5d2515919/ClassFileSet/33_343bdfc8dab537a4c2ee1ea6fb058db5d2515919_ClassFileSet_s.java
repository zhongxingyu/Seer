 package jsclassloader.classes;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import jsclassloader.Config;
 
 import com.esotericsoftware.wildcard.Paths;
 
 /**
  * Given a list of root folders this class will locate all js class files and store them, converting their
  * folder paths and file names into fully qualified class names.
  */
 public class ClassFileSet {

 	private final List<File> rootDirs;
 	private Map<String, File> classnameToFilelookup = new HashMap<String, File>();
 	private Map<File, String> fileToClassnamelookup = new HashMap<File, String>();
 	private List<String> allJsClasses = new ArrayList<String>();
 
 	public ClassFileSet(Config config) {
 		this.rootDirs = this.generateSourceFolderList(config);
 		this.initialize();
 	}
 	
 	public int size() {
 		return allJsClasses.size();
 	}
 
 	public List<String> getAllJsClasses() {
 		return allJsClasses;
 	}
 
 	public File getFileFromClassname(String classname) {
 		return classnameToFilelookup.get(classname);
 	}
 	
 	public String getClassnameFromFile(File file) {
 		return fileToClassnamelookup.get(file);
 	}
 
 	private void initialize() {
 		for (File rootDir : rootDirs) {
 			int rootFilenameLength = rootDir.getAbsolutePath().length();
 			findJsFiles(rootDir, rootFilenameLength);
 		}
 	}
 
 	private void findJsFiles(File directory, int rootFilenameLength) {
 
 		File[] files = directory.listFiles();
 		if (files == null) { return; }
 		for (int i = 0; i < files.length; i++) {
 			if (files[i].isDirectory()) {
 				findJsFiles(files[i], rootFilenameLength);
 			}
 			if (files[i].getName().endsWith(".js")) {
 				String classname = generateClassnameFromFile(files[i], rootFilenameLength);
 				allJsClasses.add(classname);
 				classnameToFilelookup.put(classname, files[i]);
 				fileToClassnamelookup.put(files[i], classname);
 			}
 		}
 	}
 
 	private String generateClassnameFromFile(File file, int rootFilenameLength) {
 		String name = file.getAbsolutePath();
 		int endPos = name.length() - 3;
 		String relative = name.substring(rootFilenameLength + 1 , endPos);
 		String result  = relative.replace(File.separatorChar, '.');
 		return result;
 	}
 	
 	private List<File> generateSourceFolderList(Config config) {
 		Paths paths = new Paths();
 		
 		String sourceFolders = config.getProperty(Config.PROP_SOURCE_FOLDERS);
 		String basePath = config.getProperty(Config.PROP_BASE_FOLDER);
 		for (String path : sourceFolders.split(",")) {
 			paths.glob(basePath, path);
 		}
 		return paths.dirsOnly().getFiles();
 	}
 }
