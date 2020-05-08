 
 package packetkit;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
import java.io.File;
 import java.util.HashMap;
 import java.util.Properties;
 
 public class AbstractPacketTest {
 	public static HashMap<Class<? extends AbstractPacketTest>, Properties> properties = new HashMap<Class<? extends AbstractPacketTest>, Properties>();
 
 
 	public String getProperty(String key) {
 		Properties prop = loadProperties(getClass());
 		return prop.getProperty(key);
 	}
 
 
 	public byte[] getBytes(String key) {
 		return PacketUtilities.parseHexDump(getProperty(key));
 	}
 
 
 	private Properties loadProperties(Class<? extends AbstractPacketTest> klass) {
 		if (properties.get(klass) != null)
 			return properties.get(klass);
 
		String fname = klass.getCanonicalName().replace('.', File.separatorChar) + ".properties";
 		InputStream istream = getClass().getClassLoader().getResourceAsStream(fname);
 		if (istream == null)
 			throw new ExceptionInInitializerError("Could not find properties for " + klass + " at " + fname);
 
 		Properties props = new Properties();
 		try {
 			props.load(istream);
 			properties.put(klass, props);
 			istream.close();
 
 			return props;
 		} catch (FileNotFoundException e) {
 			throw new ExceptionInInitializerError(e);
 		} catch (IOException e) {
 			throw new ExceptionInInitializerError(e);
 		} finally {
 			try {
 				istream.close();
 			} catch (IOException e) {
 				// ignore
 			}
 		}
 
 	}
 }
