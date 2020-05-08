 package mobi.monaca.framework.bootloader;
 
 import java.io.File;
 import java.util.ArrayList;
 
 public class LocalFileUtil {
 	public static void aggregateApplicationLocalFileList(File dir, ArrayList<String> result) {
 		for (File file : dir.listFiles()) {
 			if (file.isDirectory()) {
 				aggregateApplicationLocalFileList(file, result);
 			} else {
 				result.add(file.getAbsolutePath());
 			}
 		}
 	}
 
 	public static ArrayList<String> getLocalFilePathList(String path) {
 		ArrayList<String> result = new ArrayList<String>();
		File f = new File(path);
		if (f.exists()) {
			aggregateApplicationLocalFileList(new File(path), result);
		} else {
			f.mkdir();
		}
 		return result;
 	}
 }
