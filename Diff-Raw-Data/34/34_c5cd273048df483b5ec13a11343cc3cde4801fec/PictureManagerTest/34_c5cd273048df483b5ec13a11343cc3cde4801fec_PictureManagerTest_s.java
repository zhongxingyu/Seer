 package be.noselus.pictures;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 
 public class PictureManagerTest {
 
 	@Test
	public void getPictureParlement() throws IOException {
		InputStream is = PictureManager.get(77);
 		Assert.assertNotNull(is);
 		is.close();
 	}
 	
 	@Test
	public void getPictureChamber() throws IOException {
		InputStream is = PictureManager.get(857);
 		Assert.assertNotNull(is);
 		is.close();
 	}
 }
