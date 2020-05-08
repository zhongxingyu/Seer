 package MP3Suite;
 
import java.io.InputStream;
 
 import java.util.logging.Logger;
 import java.util.logging.LogManager;
 
 import javax.swing.UIManager;
 public class Application {
 	static {
 		try {
			InputStream is = Application.class.getResourceAsStream("/logging.properties");
 			LogManager.getLogManager().readConfiguration(is);
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			System.exit(1);
 		}
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (Exception e) {e.printStackTrace();}
 	}
 	private static final Logger log = Logger.getLogger(Application.class.getName());
 	static {
 		log.setLevel(java.util.logging.Level.FINEST);
 	}
 
 	public static Logger getLog() {
 		return log;
 	}
 
 	public static void main(String[] args) {
 		new JavaTest(); //will be replaced by the applications main frame's class (once it exists)
 	}
 }
