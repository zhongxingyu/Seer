 package codemate.utils;
 
 import static org.fusesource.jansi.Ansi.ansi;
 
 import java.io.*;
 import java.util.*;
 
 import org.fusesource.jansi.AnsiConsole;
 
 import jline.Terminal;
 import jline.TerminalFactory;
 
 public class SystemUtils {
 	/**
 	 * getSubdirectories
 	 * 
 	 * This method gets the subdirectories that under the given root. The hidden
 	 * subdirectories are ignored.
 	 * 
 	 * @param 	root		The root directory
 	 * @return	List<File>	The subdirectory list
 	 * 
 	 * @author Li Dong <dongli@lasg.iap.ac.cn>
 	 */
 	public static List<File> getSubdirectories(File root) {
 		List<File> subdirectories = new ArrayList<File>();
 		if (root.isHidden()) return subdirectories;
 		for (String fileName : root.list()) {
 			File file = new File(root+"/"+fileName);
 			if (file.isDirectory()) {
 				subdirectories.add(file);
 				subdirectories.addAll(getSubdirectories(file));
 			}
 		}
 		return subdirectories;
 	}
 	
 	public static String getAbsolutePath(File file) {
 		String res = file.getAbsolutePath();
		if (res.endsWith("/.."))
			res = res.replaceAll("/[^/]*/\\.\\.$", "");
		else if (res.endsWith("/."))
			res = res.replaceAll("/\\.$", "");
 		return res;
 	}
 	
 	public static String getAbsolutePath(String fileName) {
 		File file = new File(fileName);
 		return getAbsolutePath(file);
 	}
 	
 	public static int getConsoleWidth() {
 		Terminal terminal = TerminalFactory.create();
 		return terminal.getWidth();
 	}
 	
 	public static void printSeparateLine() {
 		char[] line = new char[60];
 		for (int i = 0; i < line.length; ++i)
 			line[i] = '-';
 		System.out.println(line);
 	}
 	
 	public static void print(String content) {
 		AnsiConsole.out.print(ansi().render(content));
 	}
 }
