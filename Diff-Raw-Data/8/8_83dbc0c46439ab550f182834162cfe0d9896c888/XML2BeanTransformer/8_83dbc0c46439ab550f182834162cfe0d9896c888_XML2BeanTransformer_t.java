 /*
  * Created on 21-lug-2005
  *
  */
 package it.javalinux.tee.transport.tranformer;
 
 import it.javalinux.tee.event.Event;
 import it.javalinux.tee.event.XMLEvent;
 
 import java.io.StringReader;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.apache.commons.beanutils.BeanUtils;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 
 public class XML2BeanTransformer implements TransformerInterface {
 	
 	/* (non-Javadoc)
 	 * @see it.javalinux.tee.transport.tranformer.TransformerInterface#transform(it.javalinux.tee.event.Event)
 	 */
 	public Event transform(Event inputEvent) throws IllegalArgumentException {
 		if (!(inputEvent instanceof XMLEvent) ) {
 			throw new IllegalArgumentException("Cant apply Xml2BeanTransformer to an event that is not an XMLEvent");
 		}
 		InputSource src = new InputSource(new StringReader(((XMLEvent) inputEvent).getXmlString()));
 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 		//factory.setNamespaceAware(true);
 		Document doc =null;
 		try {
 			doc = factory.newDocumentBuilder().parse(src);
 		} catch (Exception e) {
 			throw new IllegalArgumentException("unparsable xml");
 		}
 		Element element = (Element) doc.getElementsByTagName("XmlEvent").item(0);
 		String eventName = element.getElementsByTagName("EventName").item(0).getFirstChild().getNodeValue();
 		if (eventName == null || eventName.equals("")) {
 			throw new IllegalArgumentException("EventName not present in xml");
 		}
 		Event bean;
 		try {
 			bean = (Event) Thread.currentThread().getContextClassLoader().loadClass(eventName).newInstance();
 		} catch (Exception e) {
 			throw new IllegalArgumentException("Impossible to instanciate EventName bean:" + eventName);
 		}
 		NodeList nodes = element.getChildNodes();
 		for (int i=0; i< nodes.getLength(); i++) {
 			if (nodes.item(i).getNodeName().equalsIgnoreCase("EventName")) {
 				continue;
 			}
 			try {
                if (nodes.item(i).getFirstChild()!=null) {
                    BeanUtils.copyProperty(bean,nodes.item(i).getNodeName(), nodes.item(i).getFirstChild().getNodeValue());
                }
 			} catch (Exception e) {
                 e.printStackTrace();
 				throw new IllegalArgumentException("Check your xml.I Can't set this property:" + nodes.item(i).getNodeName());
 			}
 			
 		}
 		return bean;
 	}
 
 }
