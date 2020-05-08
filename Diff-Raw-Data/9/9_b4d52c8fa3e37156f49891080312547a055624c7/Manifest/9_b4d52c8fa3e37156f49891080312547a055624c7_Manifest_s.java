 package org.qooxdoo.charless.build.config;
 
 import java.io.File;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import org.codehaus.jackson.JsonParser;
 import org.codehaus.jackson.annotate.JsonIgnoreProperties;
 import org.codehaus.jackson.map.ObjectMapper;
 
 /**
  * Handles the configuration of a Qooxdoo Application
  * @author charless
  *
  */
 @JsonIgnoreProperties(ignoreUnknown=true)
 public class Manifest {
 	
 	private Map<String,Object> provides = new LinkedHashMap<String,Object>();
 	private Map<String,Object> info = new LinkedHashMap<String,Object>();
 	
 	public final static String APPLICATION_JSON_FILE = "Manifest.json";
 
 	
 	/**
 	 * Read the qbt json file from current dir
 	 * Return null if file does not exists
 	 * @return null|QbtConfig
 	 * @throws Exception
 	 */
 	public static Manifest read() throws Exception {
 		File jsonApp = new File("."+File.separator+APPLICATION_JSON_FILE);
 		if (jsonApp.exists()) {
 			return read(jsonApp);
 		}
 		return null;
 	}
 	
 	public static Manifest read(File jsonConfig) throws Exception {
 		ObjectMapper mapper = Json.getMapper();
 		mapper.configure(JsonParser.Feature.ALLOW_COMMENTS,true);
 		Manifest cfg = mapper.readValue(jsonConfig, Manifest.class);
 		mapper.configure(JsonParser.Feature.ALLOW_COMMENTS,false);
 		return cfg;
 	}
 
 	
 	public Object provides(String name) {
 		return this.provides.get(name);
 	}
 	
 	public Object info(String name) {
 		return this.info.get(name);
 	}
 	
 	public String getName() {
 		return (String)this.info("name");
 	}
 	
 	public String getSummary() {
 		return (String)this.info("summary");
 	}
 	
 	public String getDescription() {
 		return (String)this.info("description");
 	}
 	
 	public String getVersion() {
 		return (String)this.info("version");
 	}
 
 	public String getNamespace() {
 		return (String)this.provides("namespace");
 	}
 	
 	public String getEncoding() {
 		return (String)this.provides("encoding");
 	}
 	
 	public String getKlass() {
 		return (String)this.provides("class");
 	}
 	
 	public String getResource() {
 		return (String)this.provides("resource");
 	}
 	
 	public String getTranslation() {
 		return (String)this.provides("translation");
 	}
 	
 	public String getType() {
 		return (String)this.provides("type");
 	}
 	
 	
 	
 	@SuppressWarnings(value = { "unchecked", "unused" })
 	private void setProvides(Object o) {
 		provides = (LinkedHashMap<String,Object>) o;
 	}
 	
 	@SuppressWarnings("unused")
 	private Map<String,Object>  getProvides() {
 		return provides;
 	}
 	
 	@SuppressWarnings(value = { "unchecked", "unused" })
	private void setInfos(Object o) {
 		info = (LinkedHashMap<String,Object>) o;
 	}
 	
 	@SuppressWarnings("unused")
 	private Map<String,Object>  getInfo() {
 		return info;
 	}
 	
 }
