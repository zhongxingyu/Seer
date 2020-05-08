 package net.sf.jabref.swat;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class ShrinkingDialogUtils {
 
 	public static Collection<String> collectKeysFromTexFiles(String texFolderPath) {
 		Collection<String> listOfKeys = new HashSet<String>();
 		Collection<File> listOfFolders = new ArrayList<File>();
 		listOfFolders.add(new File(texFolderPath));
 		while (!listOfFolders.isEmpty()) {
 			File folder = listOfFolders.iterator().next();
 			listOfFolders.remove(folder);
 
 			File[] folderContent = folder.listFiles();
 			for (File file : folderContent) {
 				if (file.canRead()) {
 					if (file.isDirectory() && file.canExecute()) {
 						listOfFolders.add(file);
 					} else if (file.getName().endsWith(".tex")) {
 						listOfKeys.addAll(extractKeys(file));
 					} else {
 						// not accessible folder or another type of file, do
 						// nothing
 					}
 				} else {
 					// not readable file, do nothing
 				}
 			}
 		}
 		return listOfKeys;
 	}
 
 	public final static String REGEX = "\\\\cite[pt]?\\s*\\{[^}]+\\}";
 
 	public static Collection<? extends String> extractKeys(File texFile) {
 		Collection<String> keys = new LinkedList<String>();
 		StringBuffer buffer = new StringBuffer();
 		String tex = null;
 		try {
 			FileInputStream in = new FileInputStream(texFile);
 			int c = -1;
 			do {
 				if (c != -1) {
 					buffer.append((char) c);
 				}
 				c = in.read();
 			} while (c != -1);
 			tex = buffer.toString();
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 		Pattern p = Pattern.compile(REGEX);
 		Matcher m = p.matcher(tex);
 		while (m.find()) {
			// could be either of the form: \cite{key1} or \cite{key1,key2,key3,...}
 			String cite = tex.substring(m.start(), m.end());
			cite = cite.substring(cite.indexOf('{') + 1, cite.length() - 1);
			for (String key : cite.split(",")){
				keys.add(key);
			}
 		}
 		return keys;
 	}
 
 }
