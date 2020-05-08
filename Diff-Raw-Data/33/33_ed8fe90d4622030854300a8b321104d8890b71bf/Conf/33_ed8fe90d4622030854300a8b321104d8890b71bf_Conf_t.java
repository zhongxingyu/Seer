 package ontology;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 
 public class Conf {
 	public static final String dbDir;
 	public static final String domain;
 	public static final int suggestedMaxNumberOfTriples;
	public static final String myIp;
 	
 	static {
 		String filePath = "config/ontobuilder.properties";
 		Properties p = new Properties();
 		InputStream in = null;
 		try {
 			in = new FileInputStream(filePath);
 			p.load(in);
 		}
 		catch (FileNotFoundException e) {
 			e.printStackTrace();
 			throw new IllegalStateException("Could not find or open " + filePath);
 		}
 		catch (IOException e) {
 			e.printStackTrace();
 			throw new RuntimeException("Could not read " + filePath);
 		}
 
 		dbDir = p.getProperty("database_directory").trim();
 		domain = p.getProperty("domain").trim();
		myIp = p.getProperty("my_ip").trim();
 		suggestedMaxNumberOfTriples = Integer.parseInt(p.getProperty("suggested_max_number_of_triples").trim());
 		
 
 		try {
 			in.close();
 		}
 		catch (IOException e) {
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		}
 	}
 
 }
