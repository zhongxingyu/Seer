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
	
	@Test
	public void TreesWith29Leafs() {
		String t1 = "(((Ceud,(Pdep,Pang)),(Alum,Asuu)),(((((Cosb,(Tleo,(Crud1,(((Agal,((Pens,Pstr),Aphy1)),Crad),Orob)))),Tcat1),Tcan),(Azip,((Asim,Pdec1),Hbid))),((Racu,Cosc1),(Tvit,(Atyp,((Abre,Apeg1),(Tcfc,Pcra)))))),Hadu1);";
		String t2 = "((((Ceud,(Pdep,Pang)),(Alum,Asuu)),(((Asim,Pdec1),Azip),Hbid)),((((((Cosb,(Tleo,(((((Pens,Pstr),(Orob,Aphy1)),Agal),Crud1),Crad))),Tcat1),Tcan),Racu),Cosc1),(((Tvit,(Atyp,(Abre,Apeg1))),Tcfc),Pcra)),Hadu1);";
		
		Assert.assertEquals(4688., metric.getDistance(t1, t2));
	}
 }
