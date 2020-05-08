 /*
  * Copyright (c) 2009 ioko365 Ltd
  *
  * This file is part of ioko tapestry-commons.
  *
  *     ioko tapestry-commons is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  *
  *     ioko tapestry-commons is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  *
  *     You should have received a copy of the GNU General Public License
  *     along with ioko tapestry-commons.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package uk.co.ioko.tapestry.cacheControl.integration;
 
 import org.apache.tapestry5.test.AbstractIntegrationTestSuite;
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 /**
  * Created by IntelliJ IDEA. User: ben Date: Jun 16, 2009 Time: 9:31:22 AM
  */
 public class CacheControlIntegrationTest extends AbstractIntegrationTestSuite {
 	public CacheControlIntegrationTest() {
 		super("src/test/webapp");
 	}
 
 	@Test
 	public void testShortCachedPage() throws Exception {
 
 		// Get the contents of the date time field
 		open(BASE_URL + "CacheControlShortTestPage");
 		String initialDate = getText("//dl[@id='shortlive']/dt[2]");
 
		// Wait 2 seconds
		Thread.sleep(2000);
 
 		// Get the contents of the date time field - this should be cached
 		open(BASE_URL + "CacheControlShortTestPage");
 		String interimDate = getText("//dl[@id='shortlive']/dt[2]");
 
		// Wait 2 seconds - short cache is 3 seconds
		Thread.sleep(2000);
 
 		// Get the contents of the date time field - this should be expired and refreshed
 		open(BASE_URL + "CacheControlShortTestPage");
 		String finalDate = getText("//dl[@id='shortlive']/dt[2]");
 
 		Assert.assertTrue(initialDate.equals(interimDate), "Initial date "+initialDate+" should be cached and so match interim date "+interimDate+".");
 		Assert.assertTrue(! initialDate.equals(finalDate), "Initial date "+initialDate+" should have expired and should not match final date "+finalDate+".");
 
 	}
 
 	@Test
 	public void testMediumCachedPage() throws Exception {
 		// Get the contents of the date time field
 		open(BASE_URL + "CacheControlMediumTestPage");
 		String initialDate = getText("//dl[@id='mediumlive']/dt[2]");
 
 		// Wait 3 seconds
		Thread.sleep(4000);
 
 		// Get the contents of the date time field - this should be cached
 		open(BASE_URL + "CacheControlMediumTestPage");
 		String interimDate = getText("//dl[@id='mediumlive']/dt[2]");
 
		// Wait 2 seconds - medium cache is 5 seconds
		Thread.sleep(2000);
 
 		// Get the contents of the date time field - this should be expired and refreshed
 		open(BASE_URL + "CacheControlMediumTestPage");
 		String finalDate = getText("//dl[@id='mediumlive']/dt[2]");
 
 		Assert.assertTrue(initialDate.equals(interimDate), "Initial date "+initialDate+" should be cached and so match interim date "+interimDate+".");
 		Assert.assertTrue(! initialDate.equals(finalDate), "Initial date "+initialDate+" should have expired and should not match final date "+finalDate+".");
 		
 	}
 
 	@Test
 	public void testLongCachedPage() throws Exception {
 		// Get the contents of the date time field
 		open(BASE_URL + "CacheControlLongTestPage");
 		String initialDate = getText("//dl[@id='longlive']/dt[2]");
 
 		// Wait 5 seconds
		Thread.sleep(6000);
 
 		// Get the contents of the date time field - this should be cached
 		open(BASE_URL + "CacheControlLongTestPage");
 		String interimDate = getText("//dl[@id='longlive']/dt[2]");
 
		// Wait 2 seconds - long cache is 7 seconds
		Thread.sleep(2000);
 
 		// Get the contents of the date time field - this should be expired and refreshed
 		open(BASE_URL + "CacheControlLongTestPage");
 		String finalDate = getText("//dl[@id='longlive']/dt[2]");
 
 		Assert.assertTrue(initialDate.equals(interimDate), "Initial date "+initialDate+" should be cached and so match interim date "+interimDate+".");
 		Assert.assertTrue(! initialDate.equals(finalDate), "Initial date "+initialDate+" should have expired and should not match final date "+finalDate+".");
 		
 	}
 
 	@Test
 	public void testFarFutureCachedPage() throws Exception {
 		// Get the contents of the date time field
 		open(BASE_URL + "CacheControlFarFutureTestPage");
 		String initialDate = getText("//dl[@id='farfuturelive']/dt[2]");
 
		// Wait 8 seconds
		Thread.sleep(8000);
 
 		// Get the contents of the date time field - this should be cached
 		open(BASE_URL + "CacheControlFarFutureTestPage");
 		String interimDate = getText("//dl[@id='farfuturelive']/dt[2]");
 
		// Wait 2 seconds - far future cache is 9 seconds
		Thread.sleep(2000);
 
 		// Get the contents of the date time field - this should be expired and refreshed
 		open(BASE_URL + "CacheControlFarFutureTestPage");
 		String finalDate = getText("//dl[@id='farfuturelive']/dt[2]");
 
 		Assert.assertTrue(initialDate.equals(interimDate), "Initial date "+initialDate+" should be cached and so match interim date "+interimDate+".");
 		Assert.assertTrue(! initialDate.equals(finalDate), "Initial date "+initialDate+" should have expired and should not match final date "+finalDate+".");
 		
 	}
 
 	
 	@Test
 	public void testNoneCachedPage() throws Exception {
 		open(BASE_URL + "CacheControlNoneTestPage");
 
 		// Get the contents of the date time field
 		String date = getText("//dl[@id='nonelive']/dt[2]");
 
 		// Wait 2 seconds
 		Thread.sleep(2000);
 
 		// Refresh the page it shouldn't have changed
 		open(BASE_URL + "CacheControlNoneTestPage");
 		
 		String newDate = getText("//dl[@id='nonelive']/dt[2]");
 		assertTrue(! date.equals(newDate));
 
 
 	}
 
 	
 
 	@Test
 	public void testNeverCachedPage() throws Exception {
 		open(BASE_URL + "CacheControlNeverTestPage");
 
 		// Get the contents of the date time field
 		String date = getText("//dl[@id='neverlive']/dt[2]");
 
 		// Wait 2 seconds
 		Thread.sleep(2000);
 
 		// Refresh the page it shouldn't have changed
 		open(BASE_URL + "CacheControlNeverTestPage");
 		
 		String newDate = getText("//dl[@id='neverlive']/dt[2]");
 		assertTrue(! date.equals(newDate));
 
 
 	}
 
 }
