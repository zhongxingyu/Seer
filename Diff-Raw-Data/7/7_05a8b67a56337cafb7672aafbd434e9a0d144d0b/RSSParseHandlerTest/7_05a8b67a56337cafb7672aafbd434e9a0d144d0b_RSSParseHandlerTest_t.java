 package org.omships.omships.test;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.text.ParseException;
 import java.util.List;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.omships.omships.datatypes.FeedItem;
 import org.omships.omships.parse.RssParseHandler;
 import org.xml.sax.SAXException;
 
 import android.test.AndroidTestCase;
 
 public class RSSParseHandlerTest extends AndroidTestCase {
 
 	public static final String XML_HEAD = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?> "
 			+"<rss version=\"2.0\" xmlns:om=\"http://www.omships.org/dtd/rss.dtd\" xmlns:atom=\"http://www.w3.org/2005/Atom\">"
 			+"<channel>"
 			+"<title><![CDATA[Fake RSS feed]]></title>"
 			+"<link>http://link.org</link>"
 			+"<description><![CDATA[A fake feed top test our parser]]></description>"
 			+"<atom:link href=\"http://link.org\" rel=\"self\" type=\"application/rss+xml\" />"
 			+"<language>en-uk</language>";
 	public static final String XML_FOOT="</channel></rss>";
 	
 	protected void setUp() throws Exception {
 		super.setUp();
 	}
 
 	protected void tearDown() throws Exception {
 		super.tearDown();
 	}
 
 	public String xml_cdata_item(String title,String link,String date, String desc){
 		StringBuffer buff = new StringBuffer();
 		buff.append("<item>");
 		buff.append("<title><![CDATA[").append(title).append("]]></title>");
 		buff.append("<link><![CDATA[").append(link).append("]]></link>");
 		buff.append("<pubDate><![CDATA[").append(date).append("]]></pubDate>");
 		buff.append("<description><![CDATA[").append(desc).append("]]></description>");
 		buff.append("<guid isPermaLink=\"true\"><![CDATA[");
 		buff.append(buff.hashCode()).append("]]></guid>");
 		buff.append("</item>");
 		return buff.toString();
 	}
 	
 	public String xml_item(String title,String link,String date, String desc){
 		StringBuffer buff = new StringBuffer();
 		buff.append("<item>");
 		buff.append("<title>").append(title).append("</title>");
 		buff.append("<link>").append(link).append("</link>");
 		buff.append("<pubDate>").append(date).append("</pubDate>");
 		buff.append("<description>").append(desc).append("</description>");
 		buff.append("<guid isPermaLink=\"true\">");
 		buff.append(buff.hashCode()).append("</guid>");
 		buff.append("</item>");
 		return buff.toString();
 	}
 	public List<FeedItem> xml_parse(String test) throws ParserConfigurationException, SAXException, IOException{
 		SAXParserFactory factory = SAXParserFactory.newInstance();
         SAXParser saxParser = factory.newSAXParser();
         RssParseHandler handler = new RssParseHandler();     
         saxParser.parse(new ByteArrayInputStream(test.getBytes()), handler);
         return handler.getItems();
 	}
 	
 	public void testSmoke() throws ParserConfigurationException, SAXException, IOException {
 		String test = XML_HEAD
 				+ xml_item("Test Item","http://test.org",
 						"Sun, 31 May 2013 20:15:07 +0000",
 						"A smoke test of the parser")
 				+ XML_FOOT;
         assertEquals(1, xml_parse(test).size());
 	}
 	
 	public void testParsing() throws ParserConfigurationException, SAXException, IOException, ParseException{
 		String test = XML_HEAD
 				+xml_item("Da Title","http://link.org",
 				 "Sun, 31 May 2013 20:15:07 +0000","A generic description")
 				 +xml_cdata_item("Da Title","http://link.org",
 				 "Sun, 31 May 2013 20:15:07 +0000","A generic description")
 				+XML_FOOT;
 		List<FeedItem> list = xml_parse(test);
 		assertEquals("Da Title",list.get(0).getTitle());
 		assertEquals("http://link.org",list.get(0).getLink());
 		assertEquals("A generic description",list.get(0).getDescription());
 		assertEquals(0,
 				list.get(0).getPubDate().compareTo(
 					FeedItem.format.parse("Sun, 31 May 2013 20:15:07 +0000"))
 				);
 		assertEquals("Da Title",list.get(1).getTitle());
 		assertEquals("http://link.org",list.get(1).getLink());
 		assertEquals("A generic description",list.get(1).getDescription());
 		assertEquals(0,
 				list.get(1).getPubDate().compareTo(
 					FeedItem.format.parse("Sun, 31 May 2013 20:15:07 +0000"))
 				);
 	}
 	
 	public void testStringMarks() throws ParserConfigurationException, SAXException, IOException {
 		String texts[] = {
 				"A 'postrophe eludes us",
 				"He said \"Quiphobes\" my good sir.",
 				"Kneel before \'Zod\'!",
 				"The \' and \" do not match.",
 				"omships: Pray for the Puerto Princesa preparation team as they communicate the news of the ship&#8217;s delay and reschedule events. #omprayer",
 		};
 		StringBuffer test=new StringBuffer();
 		test.append(XML_HEAD);
 		test.append(xml_item("Apostrophe","http://test.org",
 						"Sun, 31 May 2013 20:15:07 +0000",
 						texts[0]));
 		test.append(xml_item("String String","http://test.org",
 						"Sun, 30 May 2013 20:15:07 +0000",
 						texts[1]));
 		test.append(xml_item("Single String","http://test.org",
 						"Sun, 29 May 2013 20:15:07 +0000",
 						texts[2]));
 		test.append(xml_item("Miss-Quotes","http://test.org",
 						"Sun, 28 May 2013 20:15:07 +0000",
 						texts[3]));
 		test.append(xml_item("Puerto Princesa","http://test.org",
 						"Sun, 27 May 2013 20:15:07 +0000",
 						texts[4]));
 		test.append(XML_FOOT);
         List<FeedItem> list = xml_parse(test.toString());
         assertEquals(texts.length, list.size());
        for(int i=0;i<texts.length-1;i++)
         	assertEquals(texts[i],list.get(i).getDescription());
        assertEquals("omships: Pray for the Puerto Princesa preparation team as they communicate the news of the ship’s delay and reschedule events. #omprayer",
        		list.get(4).getDescription());
 	}//end testStringMarks
 }
