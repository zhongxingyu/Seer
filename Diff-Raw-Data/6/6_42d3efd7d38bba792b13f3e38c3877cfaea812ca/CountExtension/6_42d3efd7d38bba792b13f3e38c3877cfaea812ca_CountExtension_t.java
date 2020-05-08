 package by.minsler.bat.exception12;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 public class CountExtension {
 
 	public static void main(String[] args) {
 		String dirName = "/Volumes/Data/download_chrome";
 
 		File dir = new File(dirName);
 
 		List<File> fileList = new ArrayList<File>();
 		Map<String, Integer> extenstions = new TreeMap<String, Integer>();
 
 		if (dir.exists() && dir.isDirectory()) {
 			fileList = Arrays.asList(dir.listFiles());
 			for (File file : fileList) {
 				if (file.isDirectory()) {
 					continue;
 				}
 				String ext = getExtension(file.getName());
 				Integer freq = extenstions.get(ext);
 				if (freq != null) {
 					extenstions.put(ext, freq + 1);
 				} else {
 					extenstions.put(ext, 1);
 				}
 			}
			System.out.println(extenstions);
		} else {
			System.out.println(dirName + " is not directory or not exist");
 		}
 
 	}
 
 	private static String getExtension(String name) {
 		String ext = "";
 		int lastDot = name.lastIndexOf('.');
 		if (lastDot != -1)
 			ext = name.substring(lastDot, name.length());
 		return ext;
 	}
 }
