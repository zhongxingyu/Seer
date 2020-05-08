 package org.siraya.dconfig;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.*;
 import java.util.logging.Logger;
 import java.io.*;
 import org.yaml.snakeyaml.Yaml;
 import org.ini4j.Ini;
 public class QueryNodeUtil {
 
 	private static Logger logger = Logger.getLogger(QueryNodeUtil.class.getName());
 
 
 
 	/**
 	 * save setting to ini file
 	 * @param input
 	 * @param output
 	 * @throws IOException
 	 */
 	public static void saveToIni(Map<String,Object> input,OutputStream output) throws IOException{
 		Ini iniFile = new Ini();
 		for (String key: input.keySet()) {
 			Ini.Section section = iniFile.add(key);
 			Object tmp = input.get(key);
 			if (!(tmp instanceof Map)) {
 				throw new NodeException("section must be map");
 			}
 			
 			Map<String,Object> sectionData = (Map<String,Object>)tmp;
 			for (String key2 : sectionData.keySet()) {
 				Object obj = sectionData.get(key2);
 				section.put(key2, obj.toString());
 			}
 		}
 		iniFile.store(output);
 	}
 	
 	/**
 	 * create query node
 	 * @param path
 	 * @param queryString
 	 * @return
 	 */
 	public static QueryNode createQueryNode(String path, String queryString){
 		RootNode root = null;
 		try{
 			root = new RootNode(new File(path));
 		}catch (IOException e){
 			throw new NodeException("parse node fail " + e.getMessage());
 		}
 		Dimensions dimensions = root.getDimensions();
 		List<Branch> branches = new ArrayList<Branch>();
 		for (String condition: queryString.split(";")){
 			String[] tmp = condition.split("=");
 			Branch currentBranch = dimensions.getBranchMap(tmp[0]).get(tmp[1]);
 			branches.add(currentBranch);
 		}
 		QueryNode node = new QueryNode(root, branches);
 		return node;		
 	}
 }
