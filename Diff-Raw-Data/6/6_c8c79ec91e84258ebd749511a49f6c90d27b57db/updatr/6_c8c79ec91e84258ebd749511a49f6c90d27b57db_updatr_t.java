 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 
 public class updatr extends WorldTools {
 
 	/**
 	 * Generate updatr file
 	 * This Does Only Generate If The updatr Folder Exist, so only if you use updatr this will be generated
 	 */
 	public static void createUpdatrFile() {
 		  try {
 		    File updatrDir = new File("Updatr");
 		    
 		    if (updatrDir.exists())
 		    {
 		      File updatrFile = new File("Updatr" + File.separator + Listener.pluginName() + ".updatr");
 		      if (!updatrFile.exists())
 		      {
 		        updatrFile.createNewFile();
 
 		        BufferedWriter writer = new BufferedWriter(new FileWriter(updatrFile));
 		        writer.write("name = WorldTools"); writer.newLine();
		        writer.write("version = 2.2"); writer.newLine();
		        writer.write("url = dl.dropbox.com/s/mtrt3xgumok6lud/WorldTools.updatr"); writer.newLine();
		        writer.write("file = dl.dropbox.com/s/aqvi05eaziuh4z6/WorldTools.jar"); writer.newLine();
 		        writer.write("notes = "); writer.newLine();
 		        writer.close();
 		      }
 		    }
 		  }
 		  catch (IOException e) {
 		    ((Logger)Listener.logger()).log(Level.SEVERE, null, e);
 		  }
 		}
 
 }
 
