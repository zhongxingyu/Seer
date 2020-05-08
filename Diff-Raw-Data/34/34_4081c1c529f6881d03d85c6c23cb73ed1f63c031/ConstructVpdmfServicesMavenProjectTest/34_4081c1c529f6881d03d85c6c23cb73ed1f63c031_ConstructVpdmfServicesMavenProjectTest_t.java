 package edu.isi.bmkeg.vpdmf.bin;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.File;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.context.ApplicationContext;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import edu.isi.bmkeg.uml.model.UMLmodel;
 import edu.isi.bmkeg.utils.springContext.AppContext;
 import edu.isi.bmkeg.utils.springContext.BmkegProperties;
 import edu.isi.bmkeg.vpdmf.controller.VPDMfKnowledgeBaseBuilder;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations={ "/edu/isi/bmkeg/vpdmf/appCtx-VPDMfTest.xml"})
 public class ConstructVpdmfServicesMavenProjectTest {
 	
 	ApplicationContext ctx;
 	
 	String login, password, dbUrl;
 	File ftd, digLib, triage, people;
 	
 	File dir;
 	
 	@Before
 	public void setUp() throws Exception {
 		
 		ctx = AppContext.getApplicationContext();
 		BmkegProperties prop = (BmkegProperties) ctx.getBean("bmkegProperties");
 
 		people = ctx.getResource(
 				"classpath:edu/isi/bmkeg/vpdmf/people/people_vpdmfSpec.xml").getFile();
 		
 		ftd = ctx.getResource(
 				"classpath:edu/isi/bmkeg/vpdmf/ftd/ftd_vpdmfSpec.xml").getFile();
 
 		digLib = ctx.getResource(
 				"classpath:edu/isi/bmkeg/vpdmf/digitalLibrary/digitalLibrary_vpdmfSpec.xml").getFile();
 
 		triage = ctx.getResource(
 				"classpath:edu/isi/bmkeg/vpdmf/triage/triage_vpdmfSpec.xml").getFile();
 
 		dir = digLib.getParentFile();
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		
 		int pauseHere = 0;
 		pauseHere++;
 		
 	}
 	
	@Test 
 	public final void testRunDigitalLibraryBuild() throws Exception {
			
 		String[] args = new String[] { 
				 digLib.getParent(), ftd.getParent(), people.getParent(), 
				 "target", 
				 "1.1.3-SNAPSHOT"
				 };
 		
 		BuildVpdmfServicesMavenProject.main(args);
 				
 	}
 		
 }
 
