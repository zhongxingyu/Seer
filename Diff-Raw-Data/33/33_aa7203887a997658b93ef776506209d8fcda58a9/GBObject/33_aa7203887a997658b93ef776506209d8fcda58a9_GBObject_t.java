 package harris.GiantBomb;
 
 import java.io.IOException;
 import java.net.URL;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.FactoryConfigurationError;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.xml.sax.SAXException;
 
 public class GBObject implements api {
 
 	public enum ObjectType {
 		GAME, FRANCHISE, CHARACTER, CONCEPT, OBJECT, LOCATION, PERSON, COMPANY
 	};
 
 	private ObjectType type;
 	private String name;
 	private String id;
 	private String description;
 	private String url;
 
 	public ObjectType getType() {
 		return type;
 	}
 
 	public void setType(ObjectType type) {
 		this.type = type;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getId() {
 		return id;
 	}
 
 	public void setId(String id) {
 		this.id = id;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public String getUrl() {
 		return url;
 	}
 
 	public void setUrl(String url) {
 		this.url = url;
 	}
 
 	public static GBObject getGBObject(String id, ObjectType type)
 			throws ParserConfigurationException, FactoryConfigurationError,
 			SAXException, IOException {
 		DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance()
 				.newDocumentBuilder();
 		URL url = new URL("http://api.giantbomb.com/"
 				+ type.toString().toLowerCase() + "/" + id + "/?api_key="
 				+ API_KEY + "&field_list=id,name,description,site_detail_url&format=xml");
 		Document doc = docBuilder.parse(url.openConnection().getInputStream());
 
		return parseObject((Element) doc.getElementsByTagName("results").item(0), type);
 	}
 
	public static GBObject parseObject(Element el, ObjectType type) {
 		GBObject item = new GBObject();
 		item.setName(el.getElementsByTagName("name").item(0).getFirstChild()
 				.getNodeValue());
 		item.setId(el.getElementsByTagName("id").item(0).getFirstChild()
 				.getNodeValue());
 
 		try {
 			item.setDescription(el.getElementsByTagName("description").item(0)
 					.getFirstChild().getNodeValue());
 		} catch (Exception e) {
 		}
 		;
 		
 		try {
 			item.setUrl(el.getElementsByTagName("site_detail_url").item(0)
 					.getFirstChild().getNodeValue());
 		} catch (Exception e) {
 		}
 		;
 
 		item.setType(type);
 
 		return item;
 	}
 
 }
