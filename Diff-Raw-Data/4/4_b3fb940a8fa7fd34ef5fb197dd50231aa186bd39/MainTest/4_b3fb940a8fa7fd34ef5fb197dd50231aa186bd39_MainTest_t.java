 package unit;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 
 import dal.*;
 import bll.*;
 import gui.*;
 import resources.*;
 
 public class MainTest {
 	/**
 	 * USE THIS CLASS TO RETRIEVE PICTUREDATA, MAKE PICTURES IN DISPLAYCONTROLLER AND DISPLAY
 	 * 
 	 * @param args
 	 * @throws IOException 
 	 */
 	public static void main(String[] args) {
 		try {
 			for (UIManager.LookAndFeelInfo info : UIManager
 					.getInstalledLookAndFeels()) {
 				if ("Nimbus".equals(info.getName())) {
 					UIManager.setLookAndFeel(info.getClassName());
 					break;
 				}
 			}
 		} catch (ClassNotFoundException ex) {
 			java.util.logging.Logger.getLogger(SettingsFrame.class.getName())
 					.log(java.util.logging.Level.SEVERE, null, ex);
 		} catch (InstantiationException ex) {
 			java.util.logging.Logger.getLogger(SettingsFrame.class.getName())
 					.log(java.util.logging.Level.SEVERE, null, ex);
 		} catch (IllegalAccessException ex) {
 			java.util.logging.Logger.getLogger(SettingsFrame.class.getName())
 					.log(java.util.logging.Level.SEVERE, null, ex);
		} catch (UnsupportedLookAndFeelException ex) {
 			java.util.logging.Logger.getLogger(SettingsFrame.class.getName())
 					.log(java.util.logging.Level.SEVERE, null, ex);
 		}
 		// Test using dummy DatabaseManager and dummy hashtag
 		SwingUtilities.
 		invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				
 				// DUMMY CHECK
 				DatabaseHandler dbHandler     = new DatabaseHandler();
 				DatabaseManagerDummy dbManDum = new DatabaseManagerDummy(dbHandler);
 				PictureController picCtrl     = new PictureController(dbManDum);
 
 				// Do some dummy stuff
 				List<String> sourcesDummy;
 				Set<String> hashtagsDummy;
 
 				// Set dummy sources
 				sourcesDummy = new ArrayList<String>();
 				sourcesDummy.add("instagram");
 				sourcesDummy.add("twitter");
 
 				// Set test data for source and hashtag
 				hashtagsDummy = new HashSet<String>();
 
 				hashtagsDummy.add("winter");
 				hashtagsDummy.add("raskebriller");
 
 				// Set dummy sources and hashtag
 				dbManDum.setSources(sourcesDummy);
 				dbManDum.setHashtags(hashtagsDummy);
 
 				// Run test
 				picCtrl.getNewPictureData();
 				ViewController vc = new ViewController(dbManDum);
 				ShowInterface showInterface =null;
 
 				try {
 					showInterface = new ShowInterface(vc);
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 }
