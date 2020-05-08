 package com.olivelabs.loadbalancer.implementation;
 
 import static org.junit.Assert.*;
 import junit.framework.Assert;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 
 import com.olivelabs.data.INode;
 import com.olivelabs.data.Metric;
 import com.olivelabs.data.Node;
 import com.olivelabs.data.metrics.MetricCalculatorFactory;
 import com.olivelabs.routing.implementation.RoutingAlgorithmFactory;
 
 public class HttpBalancerTest {
 	
 	Balancer balancer;
 	
 	@Before
 	public void setUp() throws Exception{
 		balancer = new Balancer();
 		balancer.setAlgorithmName(RoutingAlgorithmFactory.DYNAMIC_ALGORITHM);
 		balancer.setMetricType(MetricCalculatorFactory.STRATEGY_REQUEST);
 		for(int i=0;i<10;i++){
 			balancer.addNode("localhost","1000"+i);
 		}
 	}
 
 	
 	@Test
 	public void testSetAlgorithmName() throws Exception{
 		balancer.setAlgorithmName(RoutingAlgorithmFactory.ROUND_ROBIN_ALGORITHM);
 		Assert.assertEquals(RoutingAlgorithmFactory.ROUND_ROBIN_ALGORITHM, balancer.getAlgorithmName());
 		balancer.setAlgorithmName(RoutingAlgorithmFactory.DYNAMIC_ALGORITHM);
 	}
 
 	@Ignore
 	@Test
 	public void testGetNodeDynamically() throws Exception {
 		balancer.setAlgorithmName(RoutingAlgorithmFactory.DYNAMIC_ALGORITHM);
 
 		for(int i=0;i<10000;i++){
 			if(balancer.isNodeQueueEmpty())
 				break;
 			INode node = balancer.getNode();
 			Assert.assertNotNull(node);
 			Assert.assertNotNull(node.getId());
 		}
 		
 		
 	}
 
 	@Test
 	public void testGetNodeRoundRobin() throws Exception {
 		balancer.setAlgorithmName(RoutingAlgorithmFactory.ROUND_ROBIN_ALGORITHM);
 		
 		for(int i=0;i<10000;i++){
 			if(balancer.isNodeQueueEmpty())
 				break;
 			INode node = balancer.getNode();
 			Assert.assertNotNull(node);
 			Assert.assertNotNull(node.getId());
 		}
 		
 		
 	}
 	@Test
 	public void testGetNodeRandom() throws Exception {
 		balancer.setAlgorithmName(RoutingAlgorithmFactory.RANDOM_ALGORITHM);
 		
 		for(int i=0;i<10000;i++){
 			if(balancer.isNodeQueueEmpty())
 				break;
 			INode node = balancer.getNode();
 			Assert.assertNotNull(node);
 			Assert.assertNotNull(node.getId());
 		}
 		
 		
 	}
 
 	@Test
 	public void testAddNode() throws Exception {
 		INode node = balancer.addNode("Localhost","100099");
 		Assert.assertNotNull(node);
 		Assert.assertNotNull(node.getId());
 		
 	}
 	
 	@Test
 	public void testRemoveNode() throws Exception {
 		INode node = balancer.addNode("Localhost","100098");
 		node.start();
 		Assert.assertNotNull(node);
 		Assert.assertNotNull(node.getId());
 		Assert.assertTrue(balancer.removeNode(node));
 	}
 
	@Test
	public void testHandle(){
		fail("Not yet implemented!");
	}
 	@After
 	public void tearDown(){
 		balancer = null;
 		try {
 			Thread.currentThread().sleep(1000);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
