 /**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 */
 
 package truelauncher.utils;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.text.SimpleDateFormat;
 
 import truelauncher.config.AllSettings;
 
 
 public class LauncherUtils {
 	
 
 	
 	//get Directory (Addpata for win and home for linux) begin
     public static String getDir() {
         String OS = System.getProperty("os.name").toLowerCase();
         if (OS.contains("win")) {
             String appData = System.getenv("APPDATA");
             String Dir = new File(appData).toString();
             return Dir;
         } else if (OS.contains("linux") || OS.contains("unix")) {
             String Directory = System.getProperty("user.home", ".");
             String Dir = new File(Directory).toString();
             return Dir;
         }
         return null;
     }
 	//get Directory end
     
     
     //get Last Launcher version begin
 	public static String readURLStreamToString(InputStream in) throws Exception
 	{
 		StringBuffer buf = new StringBuffer();
 		InputStreamReader read = new InputStreamReader(in, "UTF-8");
 
 		while (true) {
 			int c = read.read();
 			if (c != -1 && c != 10) {
 				buf.append((char) c);
 			} else {
 				break;
 			}
 		}
 		return buf.toString();
 	}
     //get Last Launcher version end
 
 	
 	//Log error to file start
 	public static void logError(Exception err)
 	{
 		File errLogFile = new File(LauncherUtils.getDir() + File.separator + AllSettings.errFolder + File.separator + "LError.log");
 		errLogFile.getParentFile().mkdirs();
 	     try {
 	    	 err.printStackTrace();
	    	 if (errLogFile.length()/1024/1024 > 1) 
	    	 {
	    		 errLogFile.delete();
	    	 }
 	         FileOutputStream fos = new FileOutputStream(errLogFile, true);
 	         PrintWriter ps = new PrintWriter(fos);
 	         ps.println(new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(System.currentTimeMillis())+":");
 	         err.printStackTrace(ps);
 	         ps.flush();
 	         ps.close();
 	         fos.close();
 	     } catch (Exception e) {}
 		
 	}
 	//Log error to file end
 }
