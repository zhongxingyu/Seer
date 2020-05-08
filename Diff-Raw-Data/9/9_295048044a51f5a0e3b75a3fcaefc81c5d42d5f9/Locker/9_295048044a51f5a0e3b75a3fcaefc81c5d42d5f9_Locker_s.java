 package gutenberg.workers;
 
 import gutenberg.blocs.EntryType;
 import gutenberg.blocs.ManifestType;
 import gutenberg.blocs.PointType;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.InputStreamReader;
 import java.nio.file.Files;
 import java.nio.file.Path;
 
 import javax.imageio.ImageIO;
 
 public class Locker {
 
 	public Locker(Config config) throws Exception {
 		LOCKER = config.getPath(Resource.locker);
 	}
 
 	public File[] fetch(String[] scanIds) {
 		Path scan = null, thumb = null;
 		File[] scans = new File[scanIds.length * 2];
 		Path locker = new File(LOCKER).toPath();
 		for (int i = 0; i < scanIds.length; i++) {
 			scan = locker.resolve(scanIds[i]);
 			thumb = locker.resolve("thumb-" + scanIds[i]);
 			scans[i] = scan.toFile();
 			scans[i + 1] = thumb.toFile();
 		}
 		return scans;
 	}
 
 	public ManifestType save(File[] scans) throws Exception {
 		
 		ManifestType manifest = new ManifestType();
 		manifest.setRoot(LOCKER);
 		
 		Path locker = new File(LOCKER).toPath();		
 		String scanId = null; 
 		boolean rotate = false; 
 		String[] tokens = null;
 		
 		for (File scan:scans) {
 			
 			tokens = scan.getName().split("\\.");
 			scanId = tokens[0];
 			rotate = tokens[1].equals("1") ? true:false;
 			if (!Files.exists(locker.resolve(scanId))) {						
 				if (convert(scan.toPath(), locker.resolve(scanId), SCAN_SIZE, rotate) != 0 ||
 					convert(scan.toPath(), locker.resolve("thumb-" + scanId), THUMB_SIZE, rotate) != 0) {
 					throw new Exception("Error running convert utility on "
 							+ scan.getName());
 				}
 
 				EntryType image = new EntryType();
 				image.setId(scanId);
 				manifest.addImage(image);
 			}
 			
 			if (!scan.delete()) {
 				throw new Exception("SaveScan error: could not delete" + scan.getName());
 			}
 			
 		}
 		return manifest;
 	}
 
 	/**
 	 * 
 	 * @param scanId
 	 * @throws Exception 
 	 */
 	public ManifestType annotate(String scanId, PointType[] points) throws Exception {
 		
 		ManifestType manifest = new ManifestType();
 		manifest.setRoot(scanId);
 		
 		Path locker = new File(LOCKER).toPath();
 		File imageFile = locker.resolve(scanId).toFile();
 		BufferedImage image = ImageIO.read(imageFile);
 		Graphics2D graphics = (Graphics2D) image.getGraphics();
 		graphics.setStroke(new BasicStroke(2.0f));
 		graphics.setColor(Color.RED);
 
 		PointType topLeft = null;
 		for (PointType point:points) {
 			if (topLeft != null) {
 				graphics.drawRoundRect(topLeft.getX(), topLeft.getY(),
 						point.getX() - topLeft.getX(), 
 						point.getY() - topLeft.getY(), ARC_SIZE, ARC_SIZE);
			}
			topLeft = point;
 		}
 		
 		ImageIO.write(image, FORMAT, imageFile);
 		return manifest;
 	}
 	
 	private String LOCKER;
 	
 	private int convert(Path src, Path target, String size, boolean rotate)
 			throws Exception {
 		ProcessBuilder pb = new ProcessBuilder();
 		pb.command("convert", src.toString(), "-resize", size, "-type", "TrueColor");
 		if (rotate) {
 			pb.command().add("-rotate");
 			pb.command().add("180");
 		}
 		pb.command().add(target.toString());
 		
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
 	private final int ARC_SIZE = 10;
 	private final String FORMAT = "JPG";
 }
