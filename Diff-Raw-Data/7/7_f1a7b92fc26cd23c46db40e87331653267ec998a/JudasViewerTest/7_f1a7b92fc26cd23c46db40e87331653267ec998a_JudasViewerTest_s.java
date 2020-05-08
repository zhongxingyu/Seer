 package org.iugonet.www;
 
 import static org.junit.Assert.*;
 
 import org.fest.swing.core.MouseButton;
 import org.fest.swing.fixture.FrameFixture;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class JudasViewerTest {
 
 	JudasViewer judasViewer;
 	FrameFixture frameFixture;
 	MouseButton leftButton;
 	MouseButton rightButton;
 
 	@Before
 	public void setUp() throws Exception {
 		judasViewer = new JudasViewer("http://wdc-data.iugonet.org/data/hour/index/dst/1984/dst8410","DstIndex");
 
 		frameFixture = new FrameFixture(judasViewer);
 		frameFixture.show();
 		
 		leftButton = MouseButton.LEFT_BUTTON;
 		rightButton = MouseButton.RIGHT_BUTTON;
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		judasViewer.dispose();
 		frameFixture.cleanUp();
 	}
 
 	@Test
 	public void testPropertiesTitleShowTitle01() throws InterruptedException {
 		frameFixture.showPopupMenu();
 		for(int i=0;i<frameFixture.showPopupMenu().menuLabels().length;i++){
 			System.out.println(frameFixture.showPopupMenu().menuLabels()[i]);
 		}
 		System.out.println(frameFixture.showPopupMenu());
 		Thread.sleep(1500);
 		assertEquals(1, 1);
 	}
 	
 	/*
 	@Test
 	public void testPropertiesTitleShowTitleText01() throws InterruptedException {
 		frameFixture.showPopupMenu();
 		
 		Thread.sleep(1500);
 		assertEquals(1, 1);
 	}
 	
 	@Test
 	public void testPropertiesTitleShowTitleFont01() throws InterruptedException {
 		frameFixture.showPopupMenu();
 		
 		Thread.sleep(1500);
 		assertEquals(1, 1);
 	}
 	
 	@Test
 	public void testPropertiesTitleShowTitleColor01() throws InterruptedException {
 		frameFixture.showPopupMenu();
 		
 		Thread.sleep(1500);
 		assertEquals(1, 1);
 	}
 	
 	@Test
 	public void testPropertiesPlotDomainAxis01() throws InterruptedException {
 		frameFixture.showPopupMenu();
 		
 		Thread.sleep(1500);
 		assertEquals(1, 1);
 	}
 	
 	@Test
 	public void testPropertiesPlotRangeAixs01() throws InterruptedException {
 		frameFixture.showPopupMenu();
 		
 		Thread.sleep(1500);
 		assertEquals(1, 1);
 	}
 	
 	@Test
 	public void testPropertiesPlotAppearance01() throws InterruptedException {
 		frameFixture.showPopupMenu();
 		
 		Thread.sleep(1500);
 		assertEquals(1, 1);
 	}
 	
 	@Test
 	public void testPropertiesOther01() throws InterruptedException {
 		frameFixture.showPopupMenu();
 		
 		Thread.sleep(1500);
 		assertEquals(1, 1);
 	}	@Test
 	public void testProperties01() throws InterruptedException {
 		frameFixture.showPopupMenu();
 		
 		Thread.sleep(1500);
 		assertEquals(1, 1);
 	}
 	
 	@Test
 	public void testCopy01() throws InterruptedException {
 		frameFixture.showPopupMenu();
 		
 		Thread.sleep(1500);
 		assertEquals(1, 1);
 	}
 	
 	@Test
 	public void testSaveAsPng01() throws InterruptedException {
 		frameFixture.showPopupMenu();
 		
 		Thread.sleep(1500);
 		assertEquals(1, 1);
 	}
 	
 	@Test
 	public void testPrint01() throws InterruptedException {
 		frameFixture.showPopupMenu();
 		
 		Thread.sleep(1500);
 		assertEquals(1, 1);
 	}
 	
 	@Test
 	public void testZoomInBothAxes01() throws InterruptedException {
 		frameFixture.showPopupMenu();
 		
 		Thread.sleep(1500);
 		assertEquals(1, 1);
 	}
 	
 	@Test
 	public void testZoomInDomainAxes01() throws InterruptedException {
 		frameFixture.showPopupMenu();
 		
 		Thread.sleep(1500);
 		assertEquals(1, 1);
 	}
 	
 	@Test
 	public void testZoomInRangeAxes01() throws InterruptedException {
 		frameFixture.showPopupMenu();
 		
 		Thread.sleep(1500);
 		assertEquals(1, 1);
 	}
 	
 	@Test
 	public void testZoomOutBothAxes01() throws InterruptedException {
 		frameFixture.showPopupMenu();
 		
 		Thread.sleep(1500);
 		assertEquals(1, 1);
 	}
 	
 	@Test
 	public void testZoomOutDomainAxes01() throws InterruptedException {
 		frameFixture.showPopupMenu();
 		
 		Thread.sleep(1500);
 		assertEquals(1, 1);
 	}
 	
 	@Test
 	public void testZoomOutRangeAxes01() throws InterruptedException {
 		frameFixture.showPopupMenu();
 		
 		Thread.sleep(1500);
 		assertEquals(1, 1);
 	}
 	
 	@Test
 	public void testAutoRangeBothAxes01() throws InterruptedException {
 		frameFixture.showPopupMenu();
 		
 		Thread.sleep(1500);
 		assertEquals(1, 1);
 	}
 	
 	@Test
 	public void testAutoRangeDomainAxes01() throws InterruptedException {
 		frameFixture.showPopupMenu();
 		
 		Thread.sleep(1500);
 		assertEquals(1, 1);
 	}
 	
 	@Test
 	public void testAutoRangeRangeAxes01() throws InterruptedException {
 		frameFixture.showPopupMenu();
 		
 		Thread.sleep(1500);
 		assertEquals(1, 1);
 	}
 	*/
 }
