 package mesquite.treecomp.metrics.TripletQuartetMetric;
 
 
 import junit.framework.Assert;
 
 import mesquite.treecomp.metrics.MetricTestHelper;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class QuartetMetricTestCase {
 	MetricTestHelper metric;
 
 	@Before
 	public void setUp() throws Exception {
 		metric = new MetricTestHelper(new TripletQuartetMetric());
 	}
 	
 	@Test
 	public void TwoTrees() {
 		String t1 = "((A,B),C,D);";
 		String t2 = "(A,(B,C),D);";
 
 		Assert.assertEquals(1., metric.getDistance(t1, t2));
 	}
 	
 	@Test
 	public void SameTrees() {
 		String t1 = "((A,B),C,D);";
 
 		Assert.assertEquals(0., metric.getDistance(t1, t1));
 	}
 
 	@Test
 	public void TwoIsomorphicTrees() {
 		String t1 = "((A,B),C,D);";
 		String t2 = "(C,(A,B),D);";
 
 		Assert.assertEquals(0., metric.getDistance(t1, t2));
 	}
 	
 	@Test
 	public void TwoTreesWithExtraLeafs () {
 		String t1 = "((A,B),C,(D,E));";
 		String t2 = "(A,(B,(C,F)),D);";
 
 		Assert.assertEquals(9., metric.getDistance(t1, t2));
 	}
 	
 	@Test
 	public void TwoFourLeafTreesWithExtraLeaf() {
 		String t1 = "((A,B),C,D);";
 		String t2 = "((A,B),C,E);";
 
 		Assert.assertEquals(1., metric.getDistance(t1, t2));
 	}
 		
 	@Test
 	public void NonBinaryTreeOfFourLeaves() {
 		String t2 = "((A,B),C,D);";
 		String t1 = "(A,B,C,D);";
 
 		Assert.assertEquals(1., metric.getDistance(t1, t2));
 	}
 	
 	@Test
 	public void NonBinaryTreeOfFiveLeafs() {
 		String t2 = "((A,B),C,(D,E));";
 		String t1 = "((A,B,C,D),E);";
 
 		Assert.assertEquals(5., metric.getDistance(t1, t2));
 	}
 }
