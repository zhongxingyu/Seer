 package org.bioinfo.infrared.core.dbsql;
 
 
 import static org.junit.Assert.fail;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.bioinfo.infrared.common.DBConnector;
 import org.bioinfo.infrared.core.KaryotypeDBManager;
 import org.bioinfo.infrared.core.common.FeatureList;
import org.bioinfo.infrared.core.feature.Cytoband;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class KaryotypeDBManagerTest {
 
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	
 	@Test
 	public void testGetAllDBNames() {
 		System.out.println("Test - 1");
 		try {
 			
 			DBConnector man = new DBConnector();
 			KaryotypeDBManager dbMan = new KaryotypeDBManager(man);
 			System.out.println("db connector = " + man);
			List<Cytoband> chromosomes = dbMan.getAllCytoband();
 			for(int i=0;i<chromosomes.size(); i++) {
 				System.out.println(" ---> " + chromosomes.get(i).toString());
 			}
 			System.out.println(" -> " + chromosomes.size() + " chromosomes");
 			
			FeatureList<Cytoband> chromosome = dbMan.getCytobandByChromosomes(Arrays.asList("9"));
 			System.out.println(" -> " + chromosome);
 
 		} catch (Exception e) {
 			e.printStackTrace();
 			fail("Not yet implemented");
 		} 
 	}
 
 }
