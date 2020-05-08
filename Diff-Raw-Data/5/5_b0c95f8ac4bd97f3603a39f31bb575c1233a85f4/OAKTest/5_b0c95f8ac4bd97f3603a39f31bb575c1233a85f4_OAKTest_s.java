 package org.apache.jackrabbit.oak.tests.perf;
 
 import java.util.Random;
 import javax.jcr.Node;
 import javax.jcr.SimpleCredentials;
 import org.apache.jackrabbit.mk.testing.OakMongoTestBase;
 import org.apache.jackrabbit.oak.plugins.mongomk.MongoMK;
 import org.apache.jackrabbit.oak.Oak;
 import org.apache.jackrabbit.oak.jcr.Jcr;
 import org.apache.jackrabbit.oak.plugins.segment.SegmentNodeStore;
 import org.apache.jackrabbit.oak.plugins.segment.SegmentStore;
 import org.apache.jackrabbit.oak.plugins.segment.mongo.MongoStore;
 import org.apache.jackrabbit.oak.plugins.mongomk.util.MongoConnection;
 import org.junit.Before;
 import org.junit.Test;
 
 public class OAKTest extends OakMongoTestBase {
 
 	int nodesNumber;
 
 	@Before
 	public void beforeTest() throws Exception {
 
 		Random random = new Random();
 
 		if (oakType.equals("mongomk")) {
 			// create mongomk oak instance
 	        MongoMK.Builder mkBuilder = new MongoMK.Builder();
 	        MongoConnection connection=new MongoConnection(conf.getHost(),
 	                conf.getMongoPort(), conf.getMongoDatabase());
 	        mkBuilder.setMongoDB(connection.getDB());
 	        mkBuilder.setClusterId(random.nextInt(1000));
 			// create repository
 			repo = new Jcr(mkBuilder.open()).createRepository();
 			nodesNumber = 6000;
 		} else {
 			// create segmentmk oak instance
 			SegmentStore store = new MongoStore(mongoConnection.getDB(),
 					1024 * 1024 * 200);
 			Oak oak = new Oak(new SegmentNodeStore(store));
 			repo = new Jcr(oak).createRepository();
			nodesNumber = 10000;
 		}
 		adminSession = repo.login(new SimpleCredentials("admin", "admin"
 				.toCharArray()));
 
 	}
 
 	// 10,000 nodes ; 100 nodes/commit
 	@Test
 	public void testFlatStructure() throws Exception {
 
		int nodesPerSave = 100;
 		int count = 0;
 		Node root = adminSession.getRootNode();
 		dbWriter.initialCommit("syncOAK");
 		dbWriter.syncMongos(mongosNumber, "syncOAK");
 		for (int k = 1; k <= nodesNumber; k++) {
 			root.addNode(nodeNamePrefix + k, "nt:folder");
 			if ((k % nodesPerSave) == 0) {
 				monitor.start();
 				adminSession.save();
 				monitor.stop();
 				dbWriter.insertResult(Integer.toString(count++),
 						(float) monitor.getLastValue(), "results");
 			}
 		}
 	}
 
 	// 100,000 nodes 1,000 nodes/commit
 	@Test
 	public void testPyramidStructure() throws Exception {
 		int count = 0;
 		Node root = adminSession.getRootNode();
 		dbWriter.initialCommit("syncOAK");
 		dbWriter.syncMongos(mongosNumber, "syncOAK");
 
 		if (oakType.equals("mongomk")) {
 			for (int k = 0; k < 5; k++) {
 				Node nk = root.addNode("testA" + nodeNamePrefix + k,
 						"nt:folder");
 				for (int j = 0; j < 10; j++) {
 					Node nj = nk.addNode("testB" + nodeNamePrefix + j,
 							"nt:folder");
 					for (int i = 0; i < 1000; i++) {
 						nj.addNode("testC" + nodeNamePrefix + i, "nt:folder");
 					}
 					monitor.start();
 					adminSession.save();
 					monitor.stop();
 					dbWriter.insertResult(Integer.toString(count++),
 							(float) monitor.getLastValue(), "results");
 					System.out.println("Cluster node #" + clusterNodeId
 							+ " commitId# " + count);
 				}
 			}
 		} else {
 			for (int k = 0; k < 5; k++) {
 				Node nk = root.addNode("testA" + nodeNamePrefix + k,
 						"nt:folder");
 				for (int j = 0; j < 10; j++) {
 					Node nj = nk.addNode("testB" + nodeNamePrefix + j,
 							"nt:folder");
 					for (int i = 0; i < 100; i++) {
 						nj.addNode("testC" + nodeNamePrefix + i, "nt:folder");
 					}
 					monitor.start();
 					adminSession.save();
 					monitor.stop();
 					dbWriter.insertResult(Integer.toString(count++),
 							(float) monitor.getLastValue(), "results");
 					System.out.println("Cluster node #" + clusterNodeId
 							+ " commitId# " + count);
 				}
 			}
 		}
 	}
 
 	// 2,000,000 nodes 10000 nodes/commit
 	@Test
 	public void testLargePyramidStructure() throws Exception {
 
 		int count = 0;
 		Node root = adminSession.getRootNode();
 		dbWriter.initialCommit("syncOAK");
 		dbWriter.syncMongos(mongosNumber, "syncOAK");
 		for (int k = 0; k < 10; k++) {
 			Node nk = root.addNode("test" + nodeNamePrefix + k, "nt:folder");
 			for (int j = 0; j < 20; j++) {
 				Node nj = nk.addNode("test" + nodeNamePrefix + j, "nt:folder");
 				for (int i = 0; i < 10; i++) {
 					Node ni = nj.addNode("test" + nodeNamePrefix + i,
 							"nt:folder");
 					for (int l = 0; l < 1000; l++) {
 						ni.addNode("child" + nodeNamePrefix + l, "nt:folder");
 					}
 					monitor.start();
 					adminSession.save();
 					monitor.stop();
 					dbWriter.insertResult(Integer.toString(count++),
 							(float) monitor.getLastValue(), "results");
 				}
 			}
 		}
 	}
 
 	// read randomly from 100,000 nodes/mongo
 	@Test
 	public void testReadPyramidStructure() throws Exception {
 
 		int count = 0;
 		Node root = adminSession.getRootNode();
 		for (int k = 0; k < 10; k++) {
 			Node nk = root.addNode("test" + nodeNamePrefix + k, "nt:folder");
 			for (int j = 0; j < 10; j++) {
 				Node nj = nk.addNode("test" + nodeNamePrefix + j, "nt:folder");
 				for (int i = 0; i < 1000; i++) {
 					nj.addNode("child" + nodeNamePrefix + i, "nt:folder");
 				}
 				adminSession.save();
 			}
 		}
 
 		dbWriter.initialCommit("syncOAK");
 		dbWriter.syncMongos(mongosNumber, "syncOAK");
 
 		System.out.println("Start reading");
 		for (int i = 0; i < 100; i++) {
 			Random randomGenerator = new Random();
 			int rand = randomGenerator.nextInt(3);
 			String path = "";
 			switch (rand) {
 			case 0:
 				path = String.format("/test%1$s%2$d", nodeNamePrefix,
 						randomGenerator.nextInt(10));
 				break;
 			case 1:
 				path = String.format("/test%1$s%2$d/test%1$s%3$d",
 						nodeNamePrefix, randomGenerator.nextInt(10),
 						randomGenerator.nextInt(10));
 				break;
 			case 2:
 				path = String.format(
 						"/test%1$s%2$d/test%1$s%3$d/child%1$s%4$d",
 						nodeNamePrefix, randomGenerator.nextInt(10),
 						randomGenerator.nextInt(10),
 						randomGenerator.nextInt(100));
 				break;
 			}
 			long startTime = System.nanoTime();
 			adminSession.getNode(path);
 			long estimatedTime = System.nanoTime() - startTime;
 			dbWriter.insertResult(Integer.toString(count++),
 					(float) (estimatedTime * 1.0 / 100000), "results");
 		}
 	}
 }
