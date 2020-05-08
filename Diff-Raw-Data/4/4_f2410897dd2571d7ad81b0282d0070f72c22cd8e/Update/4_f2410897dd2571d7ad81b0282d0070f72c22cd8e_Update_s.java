 package codemate.operator;
 
 import java.io.*;
 import java.util.*;
 import java.util.regex.*;
 
 import codemate.ui.*;
 import codemate.utils.*;
 
 public class Update {
 	public static void operate() {
 		Pattern hashPattern = Pattern.compile("prev_commit_hash = \\w*");
 		String urlBase = "https://raw.github.com/dongli/CodeMate/"+
 						 "master/products/installer/payload/";
 		String dirBase = System.getenv("HOME")+"/.codemate/";
 		// ---------------------------------------------------------------------
 		// get remote hash
 		UI.notice("codemate", "Fetch information from remote repository.");
 		String hashRemote = null;
 		String content = SystemUtils.downloadAndRead(urlBase+"install.info");
 		Matcher hashMatcher = hashPattern.matcher(content);
 		if (hashMatcher.find())
 			hashRemote = hashMatcher.group().split("=")[1];
 		else
 			UI.error("codemate", "Failed to find remote commit hash!");
 		// ---------------------------------------------------------------------
 		// get local hash
 		String hashLocal = null;
 		File file = new File(dirBase+"install.info");
 		if (!file.isFile()) {
 			SystemUtils.download(
 					"https://raw.github.com/dongli/CodeMate/"+
 					"master/products/installer/codemate.installer",
 					"codemate.installer");
 			UI.error("codemate", "There is no install.info in ~/.codemate. "+
 					"New codemate.installer has been downloaded, "+
 					"reinstall it please!");
 		}
 		try {
 			content = new Scanner(file).useDelimiter("\\Z").next();
 		} catch (FileNotFoundException e) {
 			UI.error("codemate", "Failed to find local commit hash!");
 		}
 		hashMatcher = hashPattern.matcher(content);
 		if (hashMatcher.find())
 			hashLocal = hashMatcher.group().split("=")[1];
 		else
 			UI.error("codemate", "Failed to find local commit hash!");
 		// ---------------------------------------------------------------------
 		// compare
 		if (hashRemote.equals(hashLocal)) {
 			UI.notice("codemate", "CodeMate is already up to date!");
 		} else {
 			UI.notice("codemate", "Update CodeMate.");
 			String[] fileNames = {
 				"codemate", "codemate.jar", "install.info", "setup.sh"
 			};
 			for (String fileName : fileNames) {
 				UI.notice("codemate", "Download "+urlBase+fileName+".");
 				SystemUtils.download(urlBase+fileName, dirBase+fileName);
 			}
 		}
 	}
 }
