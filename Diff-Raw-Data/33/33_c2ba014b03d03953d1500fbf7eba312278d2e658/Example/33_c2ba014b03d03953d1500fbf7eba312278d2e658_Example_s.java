 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
import java.io.InputStream;
 import java.util.Enumeration;
import java.util.Scanner;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipException;
 import java.util.zip.ZipFile;
 
import gamepatcher.*;
 
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
		ProcessBuilder pb = new ProcessBuilder("java", "-jar", System.getProperty("user.dir") + File.separatorChar + "PixelZombies.jar");
		pb.directory(new File(System.getProperty("user.dir")));
		try {
			Process p = pb.start();
			System.out.println(convertStreamToString(p.getInputStream()));
			System.out.println(convertStreamToString(p.getErrorStream()));
		} catch (IOException e) {
			e.printStackTrace();
 		}
 	}
	static public void extractFolder(String zipFile) throws ZipException, IOException {
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
	public static String convertStreamToString(InputStream is) {
	    @SuppressWarnings("resource")
		Scanner s = new Scanner(is).useDelimiter("\\A");
	    return s.hasNext() ? s.next() : "";
	}
 }
