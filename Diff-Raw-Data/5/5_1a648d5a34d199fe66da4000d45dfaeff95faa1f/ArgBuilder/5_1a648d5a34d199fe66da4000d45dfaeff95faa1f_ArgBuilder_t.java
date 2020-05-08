 package soot.jimple.infoflow.util;
 
 public class ArgBuilder {
 	/**
 	 * build the arguments
 	 * at the moment this is build: -w -p cg.spark on -cp . -pp [className]
 	 * @param input
 	 * @return
 	 */
 	public String[] buildArgs(String path, String className){
 		String[] result = {
 			"-w",
 			"-no-bodies-for-excluded",
 			"-p",
 			"cg.spark",
 			"on",
 //			"-p",
 //			"cg.spark",
 //			//"dump-html",
 //			"verbose:true",
 			"-cp",
 			path,//or ".\\bin",
 			"-pp",
 			className,
 			"-p",
 			"jb",
 			"use-original-names:true",
 			"-f",
			"n",
			"-p",
			"jb.ulp",
			"off"
 		};
 		
 		return result;
 	}
 
 }
