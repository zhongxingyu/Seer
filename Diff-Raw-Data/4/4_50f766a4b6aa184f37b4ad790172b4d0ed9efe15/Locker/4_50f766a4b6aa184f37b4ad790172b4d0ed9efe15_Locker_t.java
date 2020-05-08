 package gutenberg.workers;
 
 import gutenberg.blocs.EntryType;
 import gutenberg.blocs.ManifestType;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.InputStreamReader;
 import java.nio.file.Files;
 import java.nio.file.Path;
 
 public class Locker {
 
 	public Locker(Config config) throws Exception {
 		LOCKER = config.getPath(Resource.locker);
 	}
 
 	public File[] fetch(String[] scanIds) {
 		Path scan = null, thumb = null;
 		File[] scans = new File[scanIds.length * 2];
 		Path locker = new File(LOCKER).toPath();
 		for (int i = 0; i < scanIds.length; i++) {
 			scan = locker.resolve(scanIds[i] + IMG_FORMAT);
 			thumb = locker.resolve("thumb-" + scanIds[i] + IMG_FORMAT);
 			scans[i] = scan.toFile();
 			scans[i + 1] = thumb.toFile();
 		}
 		return scans;
 	}
 
 	public ManifestType save(File[] scans) throws Exception {
 		ManifestType manifest = new ManifestType();
 		manifest.setRoot(LOCKER);
 		Path locker = new File(LOCKER).toPath();
 		String scanFile = null, thumbFile = null;
 		for (int i = 0; i < scans.length; i++) {			
 			scanFile = scans[i].getName().split("\\.")[0] + IMG_FORMAT;
 			thumbFile = "thumb-" + scanFile;			
 			if (locker.resolve(scanFile).toFile().exists()) {
 				Files.delete(scans[i].toPath());
 				continue;
 			}
 			
 			if (convert(scans[i].getPath(),
 					locker.resolve(scanFile).toString(), SCAN_SIZE) == 0 &&
 				convert(scans[i].getPath(), 
 					locker.resolve(thumbFile).toString(), THUMB_SIZE) == 0) {
 				
 				Files.delete(scans[i].toPath());
 				
 			} else {
 				throw new Exception("Error resizing scan and thumbnail");
 			}
 			
 			EntryType image = new EntryType();
 			image.setId(scanFile);
 			manifest.addImage(image);
 		}
 		return manifest;
 	}
 
 	private String LOCKER;
 	
 	private int convert(String src, String target, String size)
 			throws Exception {
 		ProcessBuilder pb = 
 				new ProcessBuilder("convert", src, "-resize", size, target);
 		System.out.println("[recieveScan]: convert " + src + " -resize " + size +
 				" " + target);
 
 		pb.directory(new File(LOCKER));
 		pb.redirectErrorStream(true);
 
 		Process process = pb.start();
 		BufferedReader messages = new BufferedReader(new InputStreamReader(
 				process.getInputStream()));
 		String line = null;
 
 		while ((line = messages.readLine()) != null) {
 			System.out.println(line);
 		}
 
 		return process.waitFor();
 	}
 
	private final String SCAN_SIZE = "600x800";
	private final String THUMB_SIZE = "120x120";
 	private final String IMG_FORMAT = ".jpg";
 }
