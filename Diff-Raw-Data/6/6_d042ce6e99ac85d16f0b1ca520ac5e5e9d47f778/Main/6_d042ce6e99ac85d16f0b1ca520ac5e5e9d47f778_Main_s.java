 package de.dieterholzmann.compiler;
 
 import java.util.HashMap;
 
 public class Main {
 
 	public static void main(String[] args) {
 		if (args.length <= 0)
 			System.err
 					.println("Usage: java -jar jscompiler [--r] --path /path/to/js/folder [[--out_path] /path/to/out/folder]");
 
 		for (int i = 0, l = args.length; i < l; i++) {
 			if (args[i].startsWith("--")) {
 				if (!(args[i + 1].startsWith("--")))
 					argsList.put(args[i].substring(2, args[i].length()),
 							args[i + 1]);
 			}
 		}
 
 		try {
 			if (argsList.get("path") == null && argsList.get("http") == null)
 				throw new IllegalArgumentException(
 						"Not a valid argument path, please select a folder");
 
 		} catch (IllegalArgumentException e) {
 			System.out.println(e.getMessage());
 		}
 		
 
 		if (argsList.get("out_path") == null)
 			argsList.put("out_path", "");
 
 		// TODO recursive arg
 //		if (argsList.get("--r") != null)
 //			recursive = true;
 		
//		new JSCompiler(argsList);
		new HTTPJSCompiler(argsList);
 	}
 
 	private static HashMap<String, String> argsList = new HashMap<String, String>();
 }
