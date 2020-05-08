 package uk.bl.wa.interject.type;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 import org.apache.tika.Tika;
 import org.apache.tika.mime.MediaType;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import uk.bl.wa.interject.factory.InterjectionFactory;
 
 public class ContentTypeTest {
 
 	protected static Logger logger = LogManager.getLogger(ContentTypeTest.class);
 	private Tika tika = null;
 	
 	@Before
 	public void setUp() throws Exception {
 		tika = new Tika();
 	}
 
 	@Test
 	public void testTika() {
 		Tika tika = new Tika();
 		String strMime = tika.detect("test.bmp");
 		Assert.assertEquals("image/x-ms-bmp", strMime);
 		Assert.assertNotSame("Mime doesn't match", "image/x-ms-png", strMime);
 	}
 
 	@Test
 	public void testContentTypeBmp() {
 		try {
 			String filename = "/test.bmp";
 			InputStream inputStream = getClass().getResourceAsStream(filename);
 			Tika tika = new Tika();
 			String mimeType = tika.detect(inputStream);
 			String expected = MediaType.image("x-ms-bmp").toString();
 			Assert.assertEquals(expected, mimeType);
 			System.out.println(String.format(
 					"detected media type for given file %s: %s", filename,
 					mimeType));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Test
 	public void testContentTypeTiff() {
 		try {
 			String filename = "/test.tiff";
 			InputStream inputStream = getClass().getResourceAsStream(filename);
 			String mimeType = tika.detect(inputStream);
 			String expected = MediaType.image("tiff").toString();
 			Assert.assertEquals(expected, mimeType);
 			System.out.println(String.format(
 					"detected media type for given file %s: %s", filename,
 					mimeType));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Test
	public void testEnumerationType() {
 		String filename = "/test.tiff";
 		InputStream inputStream = getClass().getResourceAsStream(filename);
		String problemType = InterjectionFactory.INSTANCE.findProblemType(inputStream).getMimeType();
 		String expected = MediaType.image("tiff").toString();
 		String mimeType = problemType;
 		Assert.assertEquals(expected, mimeType);
 		System.out.println(String.format(
 				"detected media type for given file %s: %s", filename,
 				mimeType));
 	}
 }
