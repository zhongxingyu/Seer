 package no.uio.master.autoscale.agent.config;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.Map.Entry;
 
 import org.yaml.snakeyaml.Yaml;
 
 public class YamlReader {
 
 	/**
 	 * Load yaml-file into Config
 	 * @throws FileNotFoundException
 	 */
 	public static void loadYaml() throws FileNotFoundException {
 		InputStream input = new FileInputStream(new File(Config.configuration_file));
 	    Yaml yaml = new Yaml();
 	    
 	    LinkedHashMap<String, ?> data = (LinkedHashMap<String, ?>)yaml.load(input);
 	    reloadConfig(data);
 	    
 	}
 	
 	/**
 	 * Reload Config-variables with yaml-configurations
 	 * @param data
 	 */
 	private static void reloadConfig(LinkedHashMap<String, ?> data) {
 		Config.root = (String)data.get("root");
 		Config.startup_command = (String)data.get("startup_command");
 		Config.shutdown_command = (String)data.get("shutdown_command");
 		
 		LinkedHashMap<String, String> clr_dirs = (LinkedHashMap<String, String>)data.get("clear_directories");
 		Config.clean_directories = new ArrayList<String>();
 		
 		for (Iterator<Entry<String, String>> iterator = clr_dirs.entrySet().iterator(); iterator.hasNext();) {
 			Entry<String, String> entry = iterator.next();
 			 Config.clean_directories.add(entry.getValue());
 			 
 			 if(entry.getKey().equals("data")) {
 				 Config.storage_location = entry.getValue();
 			 }
 		}
 		
 		Config.node_address = (String)data.get("node_address");
 		Config.node_port = (Integer)data.get("node_port");
 	}
 }
