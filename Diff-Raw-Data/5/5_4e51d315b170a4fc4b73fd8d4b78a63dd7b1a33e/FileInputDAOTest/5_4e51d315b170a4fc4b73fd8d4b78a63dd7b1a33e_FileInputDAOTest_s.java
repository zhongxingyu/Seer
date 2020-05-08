 package service;
 
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
 import model.Album;

 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 public class FileInputDAOTest {
 	
 	FileInputDAO unit;
 	
 	@BeforeMethod
 	public void before(){	
 		unit = new FileInputDAO();		
 	}
 	
 	@Test
 	public void testGetTrackFromCorrectFile(){
 		Album album = unit.getTracks("/DDT.txt");
 		assertNotNull(album);
 		assertEquals("DDT", album.getName());
 		assertEquals(2, album.getTracks().size());
 		assertEquals("Osen'", album.getTracks().get(0).getName());
 		assertEquals("DDT", album.getTracks().get(0).getAuthor());
 		assertNotNull(album.getTracks().get(0).getDuration());
 		assertEquals("Veter", album.getTracks().get(1).getName());
 		assertEquals("DDT", album.getTracks().get(1).getAuthor());
 		assertNotNull(album.getTracks().get(1).getDuration());
 	}
 
 }
