 /*
  * Copyright 2007 Sebastien Brunot (sbrunot@gmail.com)
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  *   
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.sourceforge.buildmonitor.utils;
 
 import java.io.IOException;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 
 import junit.framework.TestCase;
 
 /**
  * Unit tests for the RssFeedReader class.
  * @author sbrunot
  *
  */
 public class RssFeedReaderTest extends TestCase
 {
 	public void testCallingConstructorWithNullURLThrowsAnIllegalArgumentException()
 	{
 		try
 		{
 			new RssFeedReader(null, new SimpleDateFormat("MM/DD/yyyy HH:mm:ss"));
 			fail("An IllegalArgumentException should have been thrown");
 		}
 		catch (IllegalArgumentException e)
 		{
 			// This is the expected behaviour
 			assertTrue(e.getMessage().contains("URL of the RSS feed cannot be null."));
 		}
 	}
 
 	public void testCallingConstructorWithNullDateFormatThrowsAnIllegalArgumentException()
 	{
 		try
 		{
 			new RssFeedReader(getClass().getClassLoader().getResource("cruise-control-feed.xml"), null);
 			fail("An IllegalArgumentException should have been thrown");
 		}
 		catch (IllegalArgumentException e)
 		{
 			// This is the expected behaviour
 			assertTrue(e.getMessage().contains("Date format for the RSS feed cannot be null."));
 		}
 	}
 
 	public void testGetFeedDocumentOnAWrongURLThrowIOException() throws Exception
 	{
		RssFeedReader reader = new RssFeedReader(new URL("http://does.not.exists.com/really/doesnotexists/"), new SimpleDateFormat());
 		try
 		{
 			reader.getRssFeedDocument();
 			fail("An IOException should have been thrown");
 		}
 		catch (IOException e)
 		{
 			// This is the expected behaviour
 		}
 	}
	
 	public void testReadCruiseControlFeed() throws Exception
 	{
 		RssFeedReader reader = new RssFeedReader(getClass().getClassLoader().getResource("cruise-control-feed.xml"), new SimpleDateFormat("MM/DD/yyyy HH:mm:ss"));
 		RssFeedDocument feed = reader.getRssFeedDocument();
 		assertEquals("The number of items is not the expected one !", 11, feed.size());
 
 		RssFeedItem firstItem = feed.getItem(0);
 		assertEquals(firstItem.getTitle(), "engine passed 02/23/2007 21:22:42");
 		assertEquals(firstItem.getDescription(), "Build passed");
 		assertEquals(firstItem.getPubDate(), new SimpleDateFormat("MM/DD/yyyy HH:mm:ss").parse("02/23/2007 21:22:42"));
 		assertEquals(firstItem.getLink(), "http://leo.ilog.fr:9080/cruisecontrol/buildresults/engine");
 
 		RssFeedItem lastItem = feed.getItem(10);
 		assertEquals(lastItem.getTitle(), "teamserver-v65updates passed 02/23/2007 20:27:50");
 		assertEquals(lastItem.getDescription(), "Build passed");
 		assertEquals(lastItem.getPubDate(), new SimpleDateFormat("MM/DD/yyyy HH:mm:ss").parse("02/23/2007 20:27:50"));
 		assertEquals(lastItem.getLink(), "http://leo.ilog.fr:9080/cruisecontrol/buildresults/teamserver-v65updates");
 	}
 	
 	public void testReadBambooFeed() throws Exception
 	{
 		RssFeedReader reader = new RssFeedReader(getClass().getClassLoader().getResource("bamboo-feed.xml"), new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z"));
 		RssFeedDocument feed = reader.getRssFeedDocument();
 		assertEquals("The number of items is not the expected one !", 60, feed.size());
 
 		RssFeedItem firstItem = feed.getItem(0);
 		assertEquals(firstItem.getTitle(), "JRules trunk - Rule Team Server build 36 has FAILED (13 tests failed)");
 		assertEquals(firstItem.getDescription(), "The build has 13 failed tests and 836 successful tests.");
 		assertEquals(firstItem.getPubDate(), new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z").parse("Fri, 30 Mar 2007 13:18:33 GMT"));
 		assertEquals(firstItem.getLink(), "http://9shsn2j:8085/browse/TRUNK-TEAMSERVER-36");
 	}
 }
