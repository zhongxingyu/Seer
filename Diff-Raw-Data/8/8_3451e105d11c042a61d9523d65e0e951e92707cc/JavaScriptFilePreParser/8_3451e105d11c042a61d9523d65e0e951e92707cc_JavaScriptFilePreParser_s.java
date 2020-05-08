 package com.awesomecat.jslogger.preparser;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.Scanner;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import com.awesomecat.jslogger.JavaScriptLogger;
 import com.awesomecat.jslogger.mapper.Mapper;
 import com.awesomecat.jslogger.storage.Expression;
 
 public class JavaScriptFilePreParser {
 	/**
 	 * Takes the specified file and will evaluate it
 	 * @param file The file to parse
 	 * @param mapper The session mapper that we use to obtain IDs
 	 * @param staticFile Should saved expressions be static files?
 	 * @return The now parsed JavaScript file to be rendered to the user 
 	 * @throws FileNotFoundException
 	 */
 	public static String evaluateFile(File file, Mapper mapper, boolean staticFile) throws FileNotFoundException {
 		if(file.exists() == false) throw new RuntimeException("File does not exist.");
 		if(file.isDirectory() == true) throw new RuntimeException("File is a directory.");
 		if(file.canRead() == false) throw new RuntimeException("Must be able to read the file.");
 
 	    StringBuilder text = new StringBuilder();
 	    String NL = System.getProperty("line.separator");
 	    Scanner scanner = new Scanner(new FileInputStream(file), "UTF-8");
 	    try {
 	    	while (scanner.hasNextLine()){
 	    		text.append(scanner.nextLine() + NL);
 	    	}
 	    } finally {
 	    	scanner.close();
 	    }
 		return evaluateString(text.toString(), mapper, staticFile);
 	}
 	public static String evaluateFile(File file, Mapper mapper) throws FileNotFoundException {
 		return evaluateFile(file, mapper, false);
 	}
 	private static String extract(String str_id, String comment_block){
 		Pattern p = Pattern.compile("[\\s\\t]*\\*[\\s\\t]*"+str_id+"[\\s\\t]*([^\\s\\t].*?\\n)", Pattern.DOTALL);
 		Matcher regexMatcher = p.matcher(comment_block);
 		String output = "";
 		while (regexMatcher.find()) {
 			output = regexMatcher.group(1);
 		}
 		if(output.equals("") && str_id.equals("@valid-duration")){
 			if(!comment_block.toLowerCase().contains(str_id.toLowerCase())){
 				output = JavaScriptLogger.getConfig().getString("blocks.defaultValues.valid-duration");
 			}
 			else{
 			output = "";
 			}
 		}
 		if(output.equals("") && str_id.equals("@validate")){
 			output="";
 		}
 		if(output.equals("") && str_id.equals("@run-once")){
 			if(!comment_block.toLowerCase().contains(str_id.toLowerCase())){
 				output = JavaScriptLogger.getConfig().getString("blocks.defaultValues.run-once");
 			}
 			else{
 			output = "";
 			}
 		}
 		if(output.equals("") && str_id.equals("@window-size")){
 			if(!comment_block.toLowerCase().contains(str_id.toLowerCase())){
 				output = JavaScriptLogger.getConfig().getString("blocks.defaultValues.window-size");
 			}
 			else{
 			output = "";
 			}
 		}
 		if(output.equals("") && str_id.equals("@id")){
 			output = "";
 		}
 		return output;
 	}
 
 	/**
 	 * Will evaluate a string and add the expressions + associated IDs to mapper
 	 * @param content
 	 * @param mapper
 	 * @param staticFile
 	 * @return
 	 */
 	public static String evaluateString(String content, Mapper mapper, boolean staticFile){
 		//grabs comment values
 		ArrayList<Integer> val_dur_list = new ArrayList<Integer>();
 		ArrayList<String> express_list = new ArrayList<String>();
 		ArrayList<Boolean> run_once_list = new ArrayList<Boolean>();
 		ArrayList<Integer> window_list = new ArrayList<Integer>();
 		ArrayList<String> id_list = new ArrayList<String>();
 		Pattern p1 = Pattern.compile("(/\\*\\*#.*?#\\*\\*/)", Pattern.DOTALL);
 		Matcher regexMatcher1 = p1.matcher(content);
 		StringBuffer sb = new StringBuffer(content.length());
 		while (regexMatcher1.find()) {
 			String comment_block = regexMatcher1.group(1);
 			String val_dur = extract("@valid-duration", comment_block).trim();
 			String express = extract("@validate", comment_block).trim();
 			String run_once = extract("@run-once", comment_block).trim();
 			String id_val = extract("@id", comment_block).trim();
 			String window_size = extract("@window-size", comment_block).trim();
 			int val_dur_int;
 			boolean run_once_bool;
 			int wind_size_int;
 			if (!PreParserHelper.validRegularExpression(express)){
 				continue;
 			}
 			if (val_dur.equals("") || express.equals("") || run_once.equals("")
 					|| id_val.equals("") || window_size.equals("")
 					|| !(run_once.equals("true") || run_once.equals("false"))){
 				continue;
 			}
 			try{
 				val_dur_int = Integer.parseInt(val_dur);
 				run_once_bool = Boolean.parseBoolean(run_once);
 				wind_size_int = Integer.parseInt(window_size);
 			}
 			catch(Exception e){
 				continue;
 			}
 			val_dur_list.add(val_dur_int);
 			express_list.add(express);
 			run_once_list.add(!staticFile && run_once_bool); // Can't be run-once inside a static file;
 			window_list.add(wind_size_int);
 			id_list.add(id_val);
 			regexMatcher1.appendReplacement(sb, "");
 		} 
 		content = regexMatcher1.appendTail(sb).toString();
 		
 		ArrayList<String> new_id_list = new ArrayList<String>();
 		for(int i=0; i<id_list.size(); i++){
 			Expression expression = new Expression(val_dur_list.get(i),express_list.get(i),
 					run_once_list.get(i),window_list.get(i), staticFile);
 			new_id_list.add(mapper.registerExpressionAndGetAssociatedId(expression));
 		}
 		//update new_content
 		Pattern p3 = Pattern.compile(
 				"logger\\.log\\((.*?),[\\s\\t^\\n]*? " +
				"\"(\\$id=[^\\s\\t]+)\"\\);", Pattern.DOTALL);
 		Matcher regexMatcher3 = p3.matcher(content);
 		sb = new StringBuffer(content.length());
 		while (regexMatcher3.find()) {
 			String logBody = regexMatcher3.group(1);
 			String id_val = regexMatcher3.group(2);
 			for(int i=0; i<id_list.size(); i++){
 				if(id_list.get(i).equals(id_val.substring(4))){
					regexMatcher3.appendReplacement(sb, "logger.log("+logBody+", \""+new_id_list.get(i)+"\")");
 				}
 			}
 		}
 		content = regexMatcher3.appendTail(sb).toString();
 		return content;
 	}
 
 }
