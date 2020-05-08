 package com.tort.trade.journals;
 
 import org.testng.annotations.Test;
 import static org.testng.AssertJUnit.*;
 
 @Test(groups = {"functional"})
 public class GoodsBalanceIT extends FunctionalTest {
	public void getBalance(){
		_selenium.open("/webapp/journal.html");
		_selenium.click("a[@name='balance']");
 		
		_selenium.waitForPageToLoad("5000");
 		
 		assertEquals("Goods balance", _selenium.getTitle());
 	}
 }
