 package ca.thewhitakers.thumbnailmaker;
 
 import java.awt.Dimension;
 import java.awt.image.BufferedImage;
 import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import javax.imageio.ImageIO;
 
import junit.framework.Test;
import junit.framework.Assert;

 
 public class ThumbnailGeneratorTest {
 	/**
 	 * For a simple generator, a simple test.
 	 * @throws IOException 
 	 */
 	@Test
 	public void shouldGenerateThumbnail() throws IOException {
 		final InputStream input = ClassLoader.getSystemResourceAsStream("sample.jpg");
 		final OutputStream output = new FileOutputStream(new File("target/test.png"));
 		
 		new ThumbnailGenerator().generate(input, output, new Dimension(250, 200));
 		
 		BufferedImage bi = ImageIO.read(new File("target/test.png"));
 		
 		Assert.assertEquals(bi.getWidth(), 250);
 		Assert.assertEquals(bi.getHeight(), 200);
 	}
 }
