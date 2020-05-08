 package org.apache.jackrabbit.oak.tests.perf;
 
 import java.io.File;
import java.util.Random;
 import javax.jcr.Node;
 import javax.jcr.SimpleCredentials;
 import org.apache.jackrabbit.mk.testing.OakTarTestBase;
 import org.apache.jackrabbit.oak.Oak;
 import org.apache.jackrabbit.oak.jcr.Jcr;
 import org.apache.jackrabbit.oak.plugins.segment.SegmentNodeStore;
 import org.apache.jackrabbit.oak.plugins.segment.SegmentStore;
 import org.apache.jackrabbit.oak.plugins.segment.file.FileStore;
 import org.junit.Before;
 import org.junit.Test;

 public class OAKTarTest extends OakTarTestBase {
 
 	

 	@Before
 	public void beforeTest() throws Exception {
 
 		// create tarmk oak instance
		SegmentStore store = new FileStore(new File("tarFile"));
 		Oak oak = new Oak(new SegmentNodeStore(store));
 		repo = new Jcr(oak).createRepository();
 		
 		adminSession = repo.login(new SimpleCredentials("admin", "admin"
 				.toCharArray()));
 	}

 	// 10,000 nodes ; 100 nodes/commit
 	@Test
 	public void testFlatStructure() throws Exception {
 		int nodesNumber=100000;
 		int nodesPerSave = 1000;
 		Node root = adminSession.getRootNode();
 		for (int k = 1; k <= nodesNumber; k++) {
 			root.addNode(nodeNamePrefix + k, "nt:folder");
 			if ((k % nodesPerSave) == 0) {
 				monitor.start();
 				adminSession.save();
 				monitor.stop();
 			}
 		}
 	}
 
 	// 100,000 nodes 1,000 nodes/commit
 	@Test
 	public void testPyramidStructure() throws Exception {
 		Node root = adminSession.getRootNode();
 		for (int k = 0; k < 10; k++) {
 			Node nk = root.addNode("testA" + nodeNamePrefix + k, "nt:folder");
 			for (int j = 0; j < 10; j++) {
 				Node nj = nk.addNode("testB" + nodeNamePrefix + j, "nt:folder");
 				for (int i = 0; i < 1000; i++) {
 					nj.addNode("testC" + nodeNamePrefix + i, "nt:folder");
 				}
 				monitor.start();
 				adminSession.save();
 				monitor.stop();
 			}
 		}
 	}
 	
 	// 100,000 nodes 1,000 nodes/commit
 	@Test
 	public void testLargePyramidStructure() throws Exception {
 		Node root = adminSession.getRootNode();
 		for (int k = 0; k < 10; k++) {
 			Node nk = root.addNode("testA" + nodeNamePrefix + k, "nt:folder");
 			for (int j = 0; j < 100; j++) {
 				Node nj = nk.addNode("testB" + nodeNamePrefix + j, "nt:folder");
 				for (int i = 0; i < 1000; i++) {
 					nj.addNode("testC" + nodeNamePrefix + i, "nt:folder");
 				}
 				monitor.start();
 				adminSession.save();
 				monitor.stop();
 			}
 		}
 	}
 }
