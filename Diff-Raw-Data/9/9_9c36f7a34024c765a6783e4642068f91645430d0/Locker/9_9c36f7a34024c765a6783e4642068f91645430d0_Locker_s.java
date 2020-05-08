 package gutenberg.workers;
 
 import gutenberg.blocs.EntryType;
 import gutenberg.blocs.ManifestType;
 
 import java.io.File;
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
 			scan = locker.resolve(scanIds[i] + ".jpg");
 			thumb = locker.resolve("thumb-" + scanIds[i] + ".jpg");
 			scans[i] = scan.toFile();
 			scans[i + 1] = thumb.toFile();
 		}
 		return scans;
 	}
 
 	public ManifestType save(File[] scans) throws Exception {
 		ManifestType manifest = new ManifestType();
 		manifest.setRoot(LOCKER);
 		Path locker = new File(LOCKER).toPath();
 		File target = null;
 		String scanId = null;
 		for (int i = 0; i < scans.length; i++) {
 			scanId = getScanId(scans[i]);
 			target = locker.resolve(scans[i].getName()).toFile();
 			if (!target.exists()) {
 				Files.move(scans[i].toPath(), target.toPath());
 				generateThumbnail(target);
 
 				EntryType image = new EntryType();
 				image.setId(scanId + ".jpg");
 				manifest.addImage(image);
 				EntryType thumbnail = new EntryType();
 				thumbnail.setId("thumb-" + scanId + ".jpg");
				manifest.addImage(image);
 			}
 		}
 		return manifest;
 	}
 
 	private String LOCKER;
 
 	private void generateThumbnail(File scan) throws Exception {
 		Path thumbnail = scan.getParentFile().toPath().resolve("thumb-" + scan.getName());
 		Files.copy(scan.toPath(), thumbnail);
 	}
 
 	private String getScanId(File scan) {
 		return scan.getName().split("\\.")[0];
 	}
 }
