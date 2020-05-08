 package model.mail.transformerimpl;
 
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.Map;
 import java.util.Scanner;
 
 import javax.imageio.ImageIO;
 
 import model.parser.mime.MimeHeader;
 import model.util.Base64Util;
 import model.util.ImageRotator;
 
 public class ImageTransformer2 implements Transformer {
 
 	private ImageRotator rotator = new ImageRotator();
 	
 	@Override
 	public StringBuilder transform(StringBuilder part, Map<String, MimeHeader> partheaders) throws IOException {
 		try {
 			File contents = Base64Util.decodeUsingOS(part.toString().replace("\r", ""));
 			BufferedImage image = ImageIO.read(contents); 
 			File rotated = rotator.createRotatedImage(image);
 			File encodedContents = Base64Util.encodeUsingOS(rotated);
 			StringBuilder rotatedEncodedImage = new StringBuilder();
 			Scanner scanner = new Scanner(encodedContents);
 			while (scanner.hasNextLine()) {
 				rotatedEncodedImage.append(scanner.nextLine() + "\r\n");
 			}
 			scanner.close();
 			return rotatedEncodedImage;
 		} catch (InterruptedException e) {
 			throw new IllegalStateException(e);
 		}
 	}
 
 }
