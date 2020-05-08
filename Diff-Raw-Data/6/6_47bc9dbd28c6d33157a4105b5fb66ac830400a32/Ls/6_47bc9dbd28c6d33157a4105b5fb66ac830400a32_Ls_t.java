 package cz.zcu.kiv.os.processes;
 
 import cz.zcu.kiv.os.core.Core;
 import java.io.File;
 import java.text.DateFormat;
 import java.util.*;
 
 /**
  *
  * @author Jakub Danek
  */
 public class Ls extends cz.zcu.kiv.os.core.Process {
 
 	private static final String IS_DIR = "d";
 	private static final String NOT_DIR = "-";
 
 	private List<String> paths;
 	private boolean showHidden = false;
 	private boolean showImpliedHidden = false;
 	private boolean showDetailed = false;
 
 	public Ls() {
 		paths = new ArrayList<String>();
 	}
 
 	@Override
 	protected void run(String[] args) throws Exception {
 		processParameters(args);
 
 		Map<String, List<File>> filesMap = new HashMap<String, List<File>>();
 		List<File> tmp;
 		for(String p : paths) {
 			tmp = Core.getInstance().getServices().listFiles(this, p);
 			filesMap.put(p, tmp);
 		}
 
 		String output = prepareFileListingOutput(filesMap);
 		this.getOutputStream().writeLine(output);
 	}
 
 	/**
 	 * Prepares output string containing lists of all directories given.
 	 * @param filesMap
 	 * @return
 	 */
 	private String prepareFileListingOutput(Map<String, List<File>> filesMap) {
 		StringBuilder builder = new StringBuilder();
 
 		for(Map.Entry<String, List<File>> entry : filesMap.entrySet()) {
 			if(filesMap.entrySet().size() > 1 && entry.getValue() != null) {//append header if more records
 				builder.append(entry.getKey()).append(":\n");
 			}
 			if(entry.getValue() != null) { //append dir list
 				builder.append(prepareSingleDirectoryList(entry.getValue()));
 			} else { //append error message
 				builder.append("ls: Nelze přistoupit k ");
 				builder.append(entry.getKey());
 				builder.append(": soubor nebo adresář neexistuje");
				builder.append("\n");
 			}
 		}
 		
 		return builder.toString();
 	}
 
 	/**
 	 * Prepares output for single directory list.
 	 * @param files
 	 * @return
 	 */
 	private String prepareSingleDirectoryList(List<File> files) {
 		StringBuilder builder = new StringBuilder();
 		List<File> itList = new ArrayList<File>(files);
 
 		if(showDetailed) { //detailed output
 			String tmp;
 			int maxLength = 0;
 			for(File f : itList) { //get longest size string, for proper formatting
 				if(!processFileName(f)) {
 					files.remove(f);
 					continue;
 				}
 
 				tmp = Long.toString(f.length());
 				maxLength = (maxLength > tmp.length()) ? maxLength : tmp.length();
 			}
 
 			DateFormat format = DateFormat.getDateInstance(DateFormat.SHORT);
 			for(File f : files) { //create output
 				if(f.isDirectory()) {
 					builder.append(IS_DIR);
 				} else {
 					builder.append(NOT_DIR);
 				}
 				builder.append(" ");
 				builder.append(String.format("%" + maxLength + "s", Long.toString(f.length())));
 				builder.append(" ");
 				builder.append(format.format(new Date(f.lastModified())));
 				builder.append(" ");
 				builder.append(f.getName());
 				builder.append("\n");
 			}
 		} else { //regular output
 			for(File f : itList) {
 				if(!processFileName(f)) {
 					files.remove(f);
 					continue;
 				}
 				
 				builder.append(f.getName());
 				builder.append(" ");
 			}
			//builder.append("\n");
 		}
 
 		return builder.toString();
 	}
 
 	/**
 	 * Remove hidden files unless proper flags set
 	 * @param f
 	 * @return
 	 */
 	private boolean processFileName(File f) {
 		if(f.getName().startsWith(".") && !showHidden) {
 			return false;
 		}
 		if((f.getName().equals(".") || f.getName().equals("..")) && !showImpliedHidden) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Process all command line arguments.
 	 * @param args
 	 */
 	private void processParameters(String[] args) {
 		String tmp;
 		for(int i = 1; i < args.length; i++) {
 			tmp = args[i];
 			if(tmp.startsWith("-")) {
 				for(int j = 1; j < tmp.length(); j++) {
 					if(tmp.charAt(j) == 'A') {
 						showHidden = true;
 					} else if(tmp.charAt(j) == 'a') {
 						showHidden = true;
 						showImpliedHidden = true;
 					} else if(tmp.charAt(j) == 'l') {
 						showDetailed = true;
 					}
 				}
 			} else {
 				paths.add(tmp);
 			}
 		}
 
 		if(paths.isEmpty()) {
 			paths.add(".");
 		}
 	}
 
 }
