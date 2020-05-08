 package jcue;
 
 import javax.swing.SwingUtilities;
 import jcue.domain.CueList;
 import jcue.domain.DeviceManager;
 import jcue.ui.MainWindow;
 import jouvieje.bass.BassInit;
 
 /**
  *
  * @author Jaakko
  */
 public class JCue {
     
     public static String libPath = null;
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         loadBASS();
         
         DeviceManager dm = DeviceManager.getInstance();
         CueList cueList = CueList.getInstance();
         
         if (dm.isInitialized()) {
             SwingUtilities.invokeLater(new MainWindow(cueList));
         }
     }
     
     /**
      * Loads BASS and necessary plugins.
      * 
      * @return Returns true if everything went smoothly
      */
     private static void loadBASS() {
         setLibraryPath();
         
         //Load the libraries
         BassInit.loadLibraries();
     }
     
     /**
      * Sets the library path based on the operating system.
      * 
      * @return Returns true if path succesfully set
      */
     private static boolean setLibraryPath() {
         //Get OS name and architecture
         String os = System.getProperty("os.name").toLowerCase();
         String arch = System.getProperty("os.arch");
         
         if (os.contains("windows")) {       //Is Windows
             if (arch.equals("x86")) {
                 libPath = "lib/win32";
            } else if (arch.equals("x64")) {
                 libPath = "lib/win64";
             }
         } else if (os.contains("linux")) {  //Is Linux
             if (arch.equals("x86")) {
                 libPath = "lib/linux32";
            } else if (arch.equals("x64")) {
                 libPath = "lib/linux64";
             }
         } else if (os.contains("mac")) {    //Is Mac
             libPath = "lib/mac";
         }
         
         //Set the BASS library location according to the os type
         if (libPath != null) {
             System.setProperty("java.library.path", libPath);
             return true;
         }
         
         return false;
     }
 }
