 package harris.GiantBomb;
 
 import harris.GiantBomb.GBObject.ObjectType;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.FactoryConfigurationError;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 /**
  * This class parses the feed given to it, and returns a list of video objects
  * 
  */
 public class SearchResultParser implements api {
 
 	private final URL feedUrl;
 
 	public SearchResultParser(String feedUrl) {
 		try {
 			this.feedUrl = new URL(feedUrl);
 		} catch (MalformedURLException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public List<GBObject> parse() throws SAXException, IOException,
 			ParserConfigurationException, FactoryConfigurationError {
 		final List<GBObject> wikiObjects = new ArrayList<GBObject>();
 
 		DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance()
 				.newDocumentBuilder();
 		Document doc = docBuilder.parse(getInputStream());
 
 		NodeList results = doc.getElementsByTagName("results").item(0)
 				.getChildNodes();
 
 		for (int i = 0; i < results.getLength(); i++) {
 			Node node = results.item(i);
 			if (node.getNodeName().equals("game")) {
				wikiObjects.add(GBObject.parseObject((Element) node, ObjectType.GAME));
 			} else if (node.getNodeName().equals("franchise")) {
				wikiObjects.add(GBObject.parseObject((Element) node, ObjectType.FRANCHISE));
 			} else if (node.getNodeName().equals("character")) {
				wikiObjects.add(GBObject.parseObject((Element) node, ObjectType.CHARACTER));
 			} else if (node.getNodeName().equals("concept")) {
				wikiObjects.add(GBObject.parseObject((Element) node, ObjectType.CONCEPT));
 			} else if (node.getNodeName().equals("object")) {
				wikiObjects.add(GBObject.parseObject((Element) node, ObjectType.OBJECT));
 			} else if (node.getNodeName().equals("location")) {
				wikiObjects.add(GBObject.parseObject((Element) node, ObjectType.LOCATION));
 			} else if (node.getNodeName().equals("person")) {
				wikiObjects.add(GBObject.parseObject((Element) node, ObjectType.PERSON));
 			} else if (node.getNodeName().equals("company")) {
				wikiObjects.add(GBObject.parseObject((Element) node, ObjectType.COMPANY));
 			}
 		}
 
 		return wikiObjects;
 	}
 
 	private InputStream getInputStream() {
 		try {
 			return feedUrl.openConnection().getInputStream();
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 }
