 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipException;
 import java.util.zip.ZipFile;
 
import gamepatcher.Downloader;
 
 public class Main {
 	public static void main(String[] args){
 		Downloader downloader = new Downloader("PixelZombies.zip", "date.txt", "http://sammidysam.github.com/PixelZombies/PixelZombies.zip", "http://sammidysam.github.com/PixelZombies/date.txt", 1000);
 		downloader.checkForUpdate();
 		if(downloader.getDownloaded()){
 			try {
 				extractFolder(System.getProperty("user.dir") + File.separatorChar + "PixelZombies.zip");
 			} catch (ZipException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
		if(new File(System.getProperty("user.dir") + File.separatorChar + "PixelZombies.jar").exists()){
			ProcessBuilder pb = new ProcessBuilder("java", "-jar", System.getProperty("user.dir") + File.separatorChar + "PixelZombies.jar");
			pb.directory(new File(System.getProperty("user.dir")));
			try {
				pb.inheritIO();
				pb.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
 		}
 	}
	private static void extractFolder(String zipFile) throws ZipException, IOException {
 	    System.out.println("Extracting zip...");
 	    int BUFFER = 2048;
 	    File file = new File(zipFile);
 	    @SuppressWarnings("resource")
 		ZipFile zip = new ZipFile(file);
 	    String newPath = zipFile.substring(0, zipFile.lastIndexOf(File.separatorChar));
 	    new File(newPath).mkdir();
 	    Enumeration<? extends ZipEntry> zipFileEntries = zip.entries();
 	    while(zipFileEntries.hasMoreElements()){
 	        ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
 	        String currentEntry = entry.getName();
 	        File destFile = new File(newPath, currentEntry);
 	        File destinationParent = destFile.getParentFile();
 	        destinationParent.mkdirs();
 	        if(!entry.isDirectory()){
 	            BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry));
 	            int currentByte;
 	            byte data[] = new byte[BUFFER];
 	            FileOutputStream fos = new FileOutputStream(destFile);
 	            BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);
 	            while((currentByte = is.read(data, 0, BUFFER)) != -1){
 	                dest.write(data, 0, currentByte);
 	            }
 	            dest.flush();
 	            dest.close();
 	            is.close();
 	        }
 	        if(currentEntry.endsWith(".zip")){
 	            extractFolder(destFile.getAbsolutePath());
 	        }
 	    }
 	}
 }
