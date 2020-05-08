 package br.ufmg.dcc.vod.ncrawler;
 
 import java.io.File;
 import java.rmi.AlreadyBoundException;
 import java.rmi.RemoteException;
 import java.util.HashSet;
 import java.util.Map;
import java.util.Random;
 import java.util.Map.Entry;
 
 import junit.framework.TestCase;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
import br.ufmg.dcc.vod.ncrawler.distributed.rmi.client.EvaluatorClientFactory;
import br.ufmg.dcc.vod.ncrawler.distributed.rmi.client.EvaluatorClientImpl;
 import br.ufmg.dcc.vod.ncrawler.distributed.rmi.client.ServerID;
import br.ufmg.dcc.vod.ncrawler.distributed.rmi.server.JobExecutorFactory;
 import br.ufmg.dcc.vod.ncrawler.jobs.test_evaluator.RandomizedSyncGraph;
 import br.ufmg.dcc.vod.ncrawler.jobs.test_evaluator.TestEvaluator;
 import br.ufmg.dcc.vod.ncrawler.jobs.test_evaluator.TestSerializer;
 
 public class DistributedCrawlerTest  extends TestCase {
 
 	private File myTempDir;
	private HashSet<JobExecutorFactory> serverFactories;
	private EvaluatorClientFactory<Integer, int[]> clientFactory;
	private EvaluatorClientImpl<Integer, int[]> clientImpl;
 	private HashSet<ServerID> ids;
 
 	@Before
 	public void setUp() throws Exception {
 		String tmpDir = System.getProperty("java.io.tmpdir");
 		do  {
 			myTempDir = new File(tmpDir + File.separator + new Random().nextInt());
 		} while (myTempDir.exists());
 		
 		myTempDir.mkdirs();
		serverFactories = new HashSet<JobExecutorFactory>();
 		ids = new HashSet<ServerID>();
		clientFactory = new EvaluatorClientFactory<Integer, int[]>(6060);
		clientImpl = clientFactory.createAndBind();
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		for (File f : myTempDir.listFiles()) {
 			f.delete();
 			f.deleteOnExit();
 		}
 		myTempDir.delete();
 		myTempDir.deleteOnExit();
 		
		for (JobExecutorFactory f : serverFactories) {
 			f.shutdown();
 		}
 		
 		clientFactory.shutdown();
 	}
 	
 	public void initiateServers(int numServers) throws RemoteException, AlreadyBoundException {
 		for (int i = 0; i < numServers; i++) {
			JobExecutorFactory jef = new JobExecutorFactory(5000 + i);
 			jef.createAndBind();
 			ids.add(new ServerID("localhost", 5000+i));
 			serverFactories.add(jef);
 		}
 	}
 	
 	@Test
 	public void testCrawl1Thread() throws Exception {
 		initiateServers(1);
 		
 		RandomizedSyncGraph g = new RandomizedSyncGraph(100);
 		
 		TestEvaluator te = new TestEvaluator(g);
 		DistributedCrawler dc = new DistributedCrawler(ids, 0, clientImpl, te, myTempDir, new TestSerializer(g) ,1024 * 1024);
 		
 		dc.crawl();
 		
 		Map<Integer, int[]> crawled = te.getCrawled();
 		doTheAsserts(crawled, g);
 	}
 
 
 	@Test
 	public void testCrawl2Thread() throws Exception {
 		initiateServers(2);
 		
 		RandomizedSyncGraph g = new RandomizedSyncGraph(100);
 		
 		TestEvaluator te = new TestEvaluator(g);
 		DistributedCrawler dc = new DistributedCrawler(ids, 0, clientImpl, te, myTempDir, new TestSerializer(g) ,1024 * 1024);
 		
 		dc.crawl();
 		
 		Map<Integer, int[]> crawled = te.getCrawled();
 		doTheAsserts(crawled, g);
 	}
 	
 	@Test
 	public void testCrawl100Thread() throws Exception {
 		initiateServers(100);
 		
 		RandomizedSyncGraph g = new RandomizedSyncGraph(100);
 		
 		TestEvaluator te = new TestEvaluator(g);
 		DistributedCrawler dc = new DistributedCrawler(ids, 0, clientImpl, te, myTempDir, new TestSerializer(g) ,1024 * 1024);
 		
 		dc.crawl();
 		
 		Map<Integer, int[]> crawled = te.getCrawled();
 		doTheAsserts(crawled, g);
 	}
 
 	private void doTheAsserts(Map<Integer, int[]> crawled, RandomizedSyncGraph g) {
 		assertEquals(crawled.size(), g.getNumVertex());
 		
 		for (Entry<Integer, int[]> e: crawled.entrySet()) {
 			
 			int[] neighbours = g.getNeighbours(e.getKey());
 			int[] value = e.getValue();
 			
 			assertEquals(neighbours.length, value.length);
 			for (int i = 0; i < neighbours.length; i++) {
 				assertEquals(neighbours[i], value[i]);
 			}
 		}
 	}
 }
