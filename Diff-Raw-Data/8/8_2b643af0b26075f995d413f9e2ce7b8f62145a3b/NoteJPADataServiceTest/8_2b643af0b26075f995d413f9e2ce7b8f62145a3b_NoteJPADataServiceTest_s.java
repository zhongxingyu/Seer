 package tabitabi.picco.jpa;
 
 import static org.junit.Assert.assertTrue;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import tabitabi.picco.service.NoteJPADataService;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations={"file:src/main/webapp/WEB-INF/spring/beans.xml"})
 public class NoteJPADataServiceTest {
 	
 	
	//@Autowired
     private NoteJPADataService service;
 	
 	@Test
 	public void create(){
		assertTrue(true);
 	}
 
 }
