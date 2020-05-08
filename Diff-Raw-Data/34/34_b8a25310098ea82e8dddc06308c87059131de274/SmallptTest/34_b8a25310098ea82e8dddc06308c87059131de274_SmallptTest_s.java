 import static org.junit.Assert.assertEquals;
 
 import java.awt.Color;
 import java.awt.image.BufferedImage;
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 
 import javax.imageio.ImageIO;
 
 import org.apache.commons.collections.Bag;
 import org.apache.commons.collections.bag.HashBag;
 import org.junit.Test;
 
class SmallptTest {
 
 	@Test
 	public void testOutputImage() throws IOException {
 		Smallpt.main(new String[] {
 				"32", "32", "48"
 		});
 		final BufferedImage expected = readResource("expected.png");
 		final BufferedImage actual = ImageIO.read(new File("target/image.png"));
 		assertImage(expected, actual);
 	}
 
 	private void assertImage(final BufferedImage expected, final BufferedImage actual) throws IOException {
 		final Bag histogram = new HashBag();
 		for (int x = 0; x < expected.getWidth(); x++) {
 			for (int y = 0; y < expected.getHeight(); y++) {
 				final Color expectedPixel = new Color(expected.getRGB(x, y));
 				final Color actualPixel = new Color(actual.getRGB(x, y));
 				histogram.add(expectedPixel.getRed() - actualPixel.getRed());
 				histogram.add(expectedPixel.getBlue() - actualPixel.getBlue());
 				histogram.add(expectedPixel.getGreen() - actualPixel.getGreen());
 			}
 		}
 
 		assertEquals(histogram.toString(), histogram.getCount(0), 3 * expected.getWidth() * expected.getHeight());
 	}
 
 	private BufferedImage readResource(final String expectedImageFile) throws IOException {
 		final InputStream expectedFileStream = new BufferedInputStream(getClass().getClassLoader().getResourceAsStream(expectedImageFile));
 		try {
 			return ImageIO.read(expectedFileStream);
 		} finally {
 			expectedFileStream.close();
 		}
 	}
 }
