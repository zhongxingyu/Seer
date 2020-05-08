 package yang.pc.fileio;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.util.Properties;
 
 import yang.systemdependent.AbstractDataStorage;
 
 
 public class PCDataStorage extends AbstractDataStorage {
 
 	private Properties load() {
 		Properties props = new Properties();
 		try {
 
 			File f = new File(FILENAME);
 			if (!f.exists()) {
 				System.out.println("didnt exist, created");
 				f.createNewFile();
 			}
 
 			FileInputStream fis = new FileInputStream(FILENAME);
 			props.load(fis);
 			fis.close();
 		} catch (Exception e) {
 			System.err.println("failed to read '"+FILENAME+"'");
 			System.exit(0);
 		}
 
 		return props;
 	}
 
 	private void save(Properties props) {
 		try {
 			FileOutputStream fos = new FileOutputStream(FILENAME);
 			props.store(fos, null);
 			fos.close();
 		} catch (Exception e) {
 			System.err.println("failed to write '"+FILENAME+"'");
 			e.printStackTrace();
 			System.exit(0);
 		}
 	}
 
 	@Override
 	public void putFloat(String key, float value) {
 		Properties props = load();
 		props.put(key, ""+value);
 		save(props);
 	}
 
 	@Override
 	public void putInt(String key, int value) {
 		Properties props = load();
 		props.put(key, ""+value);
 		save(props);
 	}
 
 	@Override
 	public void putBoolean(String key, boolean value) {
 		Properties props = load();
 		props.put(key, ""+value);
 		save(props);
 	}
 
 	@Override
 	public void putString(String key, String value) {
 		Properties props = load();
 		props.put(key, value);
 		save(props);
 	}
 
 	@Override
 	public float getFloat(String key, float defaultValue) {
 		Properties props = load();
 		try {
			return Float.parseFloat(props.getProperty(key));
 		} catch (Exception e) {}
		return defaultValue;
 	}
 
 	@Override
 	public int getInt(String key, int defaultValue) {
 		Properties props = load();
 		try {
			return Integer.parseInt(props.getProperty(key));
		} catch (Exception e) { }
		return defaultValue;
 	}
 
 	@Override
 	public boolean getBoolean(String key, boolean defaultValue) {
 		Properties props = load();
		String prop = props.getProperty(key);
		if (prop != null && prop.equalsIgnoreCase("false")) return false;
		else if (prop != null && prop.equalsIgnoreCase("true")) return true;
		else return defaultValue;
 	}
 
 	@Override
 	public String getString(String key, String defaultValue) {
 		Properties props = load();
 		try {
 			String value = props.getProperty(key);
 			//need a null check here to deliver defaultValue if not existing
			if (value != null) return value;
 		} catch (Exception e) {}
		return defaultValue;
 	}
 }
