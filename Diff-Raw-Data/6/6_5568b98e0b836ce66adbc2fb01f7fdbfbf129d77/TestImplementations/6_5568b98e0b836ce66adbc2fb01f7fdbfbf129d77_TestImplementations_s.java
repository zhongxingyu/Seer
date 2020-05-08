 import static org.junit.Assert.assertEquals;
 
 import org.junit.Before;
 import org.junit.Test;
 
 
 public class TestImplementations {
 	private static final int TIMEOUT = 2000; // 2000 ms = 2 s
 	Implementation imp1; Implementation imp1Other;
 	Implementation imp2; Implementation imp2Other;
 	
 	@Before
 	public void setUp() throws Exception {
 		CensusData data = PopulationQuery.parse("CenPop2010.txt");
		imp1 = new SimpleAndSequential(20,25,data); imp1Other = new SimpleAndSequential(9,14,data);
		imp2 = new SimpleAndParallel(20,25,data); imp2Other = new SimpleAndParallel(9,14,data);
 	}
 	
 	@Test(timeout = TIMEOUT)
 	public void testTotalPopulation() {
 		assertEquals(imp1.getPop(),312471327);
 		assertEquals(imp2.getPop(),312471327);
 	}
 	
 	@Test(timeout = TIMEOUT)
 	public void testHawaii() {
 		assertEquals(imp1.query(1, 1, 5, 4), 1360301);
 		assertEquals(imp2.query(1, 1, 5, 4), 1360301);
 	}
 	
 	@Test(timeout = TIMEOUT)
 	public void testAlaska() {
 		assertEquals(imp1.query(1, 12, 9, 25), 710231);
 		assertEquals(imp2.query(1, 12, 9, 25), 710231);
 	}
 	
 	@Test(timeout = TIMEOUT)
 	public void testMainUS() {
 		assertEquals(imp1.query(9, 1, 20, 13), 310400795);
 		assertEquals(imp2.query(9, 1, 20, 13), 310400795);
 	}
 	
 	@Test(timeout = TIMEOUT)
 	public void testWholeGrid() {
 		assertEquals(imp1.query(1, 1, 20, 25), 312471327);
 		assertEquals(imp2.query(1, 1, 20, 25), 312471327);
 	}
 	
 	@Test(timeout = TIMEOUT)
 	public void testBottomFourRows() {
 		assertEquals(imp1.query(1, 1, 20, 4), 36493611);
 		assertEquals(imp2.query(1, 1, 20, 4), 36493611);
 	}
 	
 	@Test(timeout = TIMEOUT)
 	public void testMiddleishThreeColumns() {
 		assertEquals(imp1.query(9, 1, 11, 25), 52392739);
 		assertEquals(imp2.query(9, 1, 11, 25), 52392739);
 	}
 	
 	@Test(timeout = TIMEOUT)
 	public void testOther() {
 		assertEquals(imp1Other.query(5, 5, 7, 5), 18820388);
 		assertEquals(imp2Other.query(5, 5, 7, 5), 18820388);
 		
 		assertEquals(imp1Other.query(6, 3, 8, 4), 105349619);
 		assertEquals(imp2Other.query(6, 3, 8, 4), 105349619);
 	}
 }
