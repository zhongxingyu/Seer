 package app;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.HashMap;
 
 public final class Preferences
 {
 	private static HashMap<String, String> _preferences = new HashMap<String, String>();
 	private static File _preferencesFile = new File("jagexappletviewer.preferences");
 
 	public static void load()
 	{
 		BufferedReader reader = null;
 		try {
 			reader = new BufferedReader(new FileReader(_preferencesFile));
 			String prefLine;
 			while ((prefLine = reader.readLine()) != null) {
 				int i = prefLine.indexOf('=');
 				if (i != -1) {
 					_preferences.put(prefLine.substring(0, i), prefLine.substring(i - -1));
 				}
 			}
 		} catch (Exception ex) {
             if (appletviewer.debug) {
                 ex.printStackTrace();
             }
 		} finally {
 			try {
				if (reader != null) {
					reader.close();
				}
 			} catch (Exception ex) {
                 if (appletviewer.debug) {
                     ex.printStackTrace();
                 }
 			}
 		}
 	}
 
 	public static void set(String name, String value)
 	{
 		_preferences.put(name, value);
 	}
 
 	public static String get(String name)
 	{
 		return _preferences.get(name);
 	}
 
 	public static void save()
 	{
 		PrintStream writer = null;
 		try {
 			writer = new PrintStream(new FileOutputStream(_preferencesFile));
 			for (String name : _preferences.keySet()) {
 				writer.println(name + "=" + _preferences.get(name));
 			}
 		} catch (IOException ex) {
 			if (appletviewer.debug) {
 				ex.printStackTrace();
 			}
 		} finally {
 			if (writer != null) {
 				writer.close();
 			}
 		}
 	}
 }
 
 /*
  * Location: \\.psf\Home\Documents\java\jagexappletviewer\ Qualified Name:
  * app.Class_n JD-Core Version: 0.5.4
  */
