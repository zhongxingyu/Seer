 package coolawesomeme.basics_plugin;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 
 public class AutoUpdater {
 
 	private static String version = Basics.version;
 	private static int pluginMajor = Basics.versionMajor;
 	private static int pluginMinor = Basics.versionMinor;
 	private static int pluginRevision = Basics.versionRevision;
 	private static String pluginAcquiredVersion;
 	private static boolean download = Basics.download;
 	
 	public static void checkForUpdate(Basics basics){
             try {
                 URL url = new URL("https://raw.github.com/coolawesomeme/Basics/master/UPDATE.txt");
                 BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                 	while ((pluginAcquiredVersion = in.readLine()) != null) {
                 		String[] temp;
                 		temp = pluginAcquiredVersion.split("-");
                		if(temp[0].contains("null") || temp[0].contains("void") || temp[0].contains("missingno") || temp[0].equals("")){
                 			temp[0] = version;
                 			temp[0].equals(version);
                 		}else{
                 			System.out.println("Latest plugin version found: Basics " + temp[0] + ".");                
                 			if(!isOutdated(Integer.parseInt(temp[3]), Integer.parseInt(temp[4]), Integer.parseInt(temp[5]))){
                 				System.out.println("[Basics] Plugin up to date!");
                 			}else{
                 				boolean isDownloaded = downloadLatestModFile(basics, temp[6], temp[0]);
                 				basics.getLogger().info("******************************************************");
                 				basics.getLogger().info("******************************************************");
                 				basics.getLogger().info("An update of " + "Basics (Version " + temp[0] + ") " + "is available!");
                 				if(isDownloaded){
                 					basics.getLogger().info("Located at: " + basics.getDataFolder() + "\\updates");
                 				}else{
                 					basics.getLogger().info("http://coolawesomeme.github.io/Basics");
                 				}
                 				basics.getLogger().info("******************************************************");
                 				basics.getLogger().info("******************************************************");
                 				if(!temp[1].isEmpty()){
                 					basics.getLogger().info(temp[1]);
                 				} if(!temp[2].isEmpty()){
                 					basics.getLogger().info(temp[2]);
                 				}
                 				System.out.println("An update of " + "Basics (Version " + temp[0] + ") " + "is available!");
                 				if(isDownloaded){
                 					System.out.println("Located at: " + basics.getDataFolder() + "\\updates");
                 				}else{
                 					System.out.println("http://coolawesomeme.github.io/Basics");
                 				}
                 			}
                 		}
                 	}
                 	in.close();
             } catch (Exception e) {
             	System.err.println("[Basics] Error: " + e);
             }
 		}
 	
 	private static boolean isOutdated(int release, int build, int revision){
 		if(pluginMajor <= release && pluginMinor <= build && pluginRevision < revision){
 			return true;
 		}else{
 			return false;
 		}
 	}
 	
	@SuppressWarnings("unused")
 	private static boolean isUpdated(int release, int build, int revision){
 		if(pluginMajor == release && pluginMinor == build && pluginRevision == revision){
 			return true;
 		}else{
 			return false;
 		}
 	}
 	
 	private static boolean downloadLatestModFile(Basics basics, String updateURL, String pluginVersion){
     	if(download){
     	    String saveTo = basics.getDataFolder() + "/updates";
     	    File saveFolder = new File(saveTo);
     	    saveFolder.mkdirs();
     	    try {
     	        URL url = new URL(updateURL);
     	        URLConnection conn = url.openConnection();
     	        InputStream in = conn.getInputStream();
     	        FileOutputStream out = new FileOutputStream(saveTo + "/Basics " + pluginVersion + ".jar");
     	        byte[] b = new byte[1024];
     	        int count;
     	        while ((count = in.read(b)) >= 0) {
     	            out.write(b, 0, count);
     	        }
     	        out.flush(); out.close(); in.close();                   
     	        System.out.println("Latest plugin version is downloaded!");
     	        System.out.println("Located here: " + saveTo + "/Basics "+ pluginVersion + ".jar");
     	        System.out.println("Put in 'plugins' folder & delete the old version.");
     	        return true;
     	    } catch (Exception e) {
     	        e.printStackTrace();
     	    }
     	}else{
     		return false;
     	}
 		return false;
     }
 }
