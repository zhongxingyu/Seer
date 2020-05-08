 package com.tort.trade.journals;
 
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.assertFalse;
 import static org.testng.Assert.assertTrue;
 import static org.testng.Assert.fail;
 
 import org.testng.annotations.Test;
 
 import com.thoughtworks.selenium.DefaultSelenium;
 
 @Test(groups = {"functional"})
 public class FilterGoodsIT {
 	public void testFilterGoods() throws Exception {
		DefaultSelenium selenium = new DefaultSelenium("localhost", 4444, "*firefox", "http://localhost:8080/");		
 		selenium.start();
 		
 		selenium.open("/webapp/journal.html");
 		for (int second = 0;; second++) {
 			if (second >= 60) fail("timeout");
 			try { if (selenium.isElementPresent("//table[@id='goods']//tr[20]")) break; } catch (Exception e) {}
 			Thread.sleep(1000);
 		}
 		assertEquals(selenium.getTable("goods.0.1"), "джинсы труба голубая");
 		assertTrue(selenium.isElementPresent("//table[@id='goods']//tr[20]"));
 		selenium.focus("filter");
 		selenium.type("filter", "дж с");
 		selenium.keyUp("filter", "с");
		for (int second = 0;; second++) {
			if (second >= 60) fail("timeout");
			try { if (selenium.isElementPresent("//table[@id='goods']//tr[10]")) break; } catch (Exception e) {}
			Thread.sleep(1000);
		}
 		assertFalse(selenium.isElementPresent("//table[@id='goods']//tr[11]"));
 		assertEquals(selenium.getTable("goods.0.1"), "дж стеганые");
 		
 		selenium.stop();
 	}
 }
