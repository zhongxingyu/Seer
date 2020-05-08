 package darep.plugins;
 
 import java.io.File;
 import java.util.ArrayList;
 
 import darep.server.CompletenessChecker;
 
 public class MarkerFileChecker implements CompletenessChecker {
 	
 	private String prefix;
 
 	@Override
 	public File[] getCompletedFiles(File directory) throws Exception {
 		ArrayList<File> complete = new ArrayList<File>();
 		File[] files = directory.listFiles();
 		
 		for (File file: files) {
 			String fileName = file.getName();
 			if (fileName.startsWith(prefix)) {
 				String newFileName = fileName.substring(prefix.length());
 				File newFile = new File(directory, newFileName);
 				if (!newFile.exists()) {
 					throw new Exception(
 							"Could not find matching file" +
 							" for lockfile '" + fileName + "'");
 				}
 				
 				complete.add(newFile);
 			}
 		}
 		
 		return complete.toArray(new File[0]);
 	}
 
 	@Override
 	public void setProperty(String key, String value)
 			throws IllegalArgumentException {
 		if (value == null)
 			throw new IllegalArgumentException("value for " + key + " can not be null");
		if (key != "prefix")
 			throw new IllegalArgumentException("MarkerFileChecker only accepts the property 'prefix'");
 		if (value.contains("/"))
 			throw new IllegalArgumentException("Prefix can not contain '/'");
 		
 		this.prefix = value;
 	}
 
 }
