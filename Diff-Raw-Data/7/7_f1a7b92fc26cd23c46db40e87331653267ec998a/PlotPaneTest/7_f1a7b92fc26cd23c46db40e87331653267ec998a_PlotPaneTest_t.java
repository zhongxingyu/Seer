 package org.iugonet.www;
 
 
 import static org.junit.Assert.*;
 

 import org.fest.swing.fixture.DialogFixture;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class PlotPaneTest {
 
 	PlotPane plotPane;
 	DialogFixture dialogFixture;
 
 	@Before
 	public void setUp() throws Exception {
 		plotPane = new PlotPane();
 //		dialogFixture = new DialogFixture(plotPane);
 //		dialogFixture.show();
 
 	}
 
 	@After
 	public void tearDown() throws Exception {
 //		plotPane.dispose();
 
 		dialogFixture.cleanUp();
 	}
 
 	@Test
 	public void test() {
 		dialogFixture.button("btnCancel").click();
 //		assertFalse(plotPane.isActive());
 	}
 
 }
