 package be.noselus.pictures;
 
 import java.io.IOException;
 import java.io.InputStream;
import java.sql.SQLException;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 
 public class PictureManagerTest {
 
 	@Test
	public void getPictureParlement() throws IOException, ClassNotFoundException, SQLException {
		InputStream is = PictureManager.get().get(77);
 		Assert.assertNotNull(is);
 		is.close();
 	}
 	
 	@Test
	public void getPictureChamber() throws IOException, ClassNotFoundException, SQLException {
		InputStream is = PictureManager.get().get(857);
 		Assert.assertNotNull(is);
 		is.close();
 	}
 }
