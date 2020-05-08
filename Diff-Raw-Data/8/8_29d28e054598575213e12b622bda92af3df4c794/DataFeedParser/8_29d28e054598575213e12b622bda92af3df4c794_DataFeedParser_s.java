 package edu.uri.ele.capstone.monitorfleet.util;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 public class DataFeedParser {
 
 	public static ArrayList<DataItem> GetData(String url){
 		ArrayList<DataItem> _data = new ArrayList<DataItem>();
		
		String xml = URL2XMLString(url);
		Document doc = String2XML(xml);
	
 		NodeList nodes = null;
 		
 		while(nodes == null){
 			try{
 				nodes = doc.getElementsByTagName("DataItem");
 			}catch(NullPointerException e){
 				nodes = null;
 			}
 		}
 		
 		for(int i = 0; i < nodes.getLength(); i++){
 			Element current = (Element)nodes.item(i);
 			String mN = current.getElementsByTagName("machineName").item(0).getFirstChild().getNodeValue();
 			String dN = current.getElementsByTagName("displayName").item(0).getFirstChild().getNodeValue();
 			String val = current.getElementsByTagName("value").item(0).getFirstChild().getNodeValue();
 			
 			_data.add(new DataItem(mN, dN, val));
 		}
 		
 		return _data;
 	}
 	
 	public static String URL2XMLString(String url){
 		String _xml = null;
 		
 		try {
 			HttpResponse httpResponse = new DefaultHttpClient().execute(new HttpPost(url));
 			_xml = EntityUtils.toString(httpResponse.getEntity());
 		} catch (UnsupportedEncodingException e) {
 			_xml= "<results status=\"error\"><msg>Can't connect to server</msg></results>";
 		} catch (MalformedURLException e) {
 			_xml= "<results status=\"error\"><msg>Can't connect to server</msg></results>";
 		} catch (IOException e) {
 			_xml= "<results status=\"error\"><msg>Can't connect to server</msg></results>";
 		}
 		
 		return _xml;
 	}
 	
 	public static Document String2XML(String xml){
 		Document doc = null;
 		
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		
 		try {
 			DocumentBuilder db = dbf.newDocumentBuilder();
 			
 			InputSource is = new InputSource();
 			is.setCharacterStream(new StringReader(xml));
 			doc = db.parse(is);
 		} catch (ParserConfigurationException e) {
 			System.out.println("XML parse error: " + e.getMessage());
 			return null;
 		} catch (SAXException e) {
 			System.out.println("Wrong XML file structure: " + e.getMessage());
 			return null;
 		} catch (IOException e) {
 			System.out.println("I/O exeption: " + e.getMessage());
 			return null;
 		}
 		
 		return doc;
 	}
 }
