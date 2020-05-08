 package org.stonesutras.snippettool.util;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.ArrayList;
 
 import org.exist.xmldb.EXistResource;
 import org.exist.xmldb.ExtendedResource;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.input.SAXBuilder;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.xmldb.api.DatabaseManager;
 import org.xmldb.api.base.Collection;
 import org.xmldb.api.base.Resource;
 import org.xmldb.api.base.ResourceIterator;
 import org.xmldb.api.base.ResourceSet;
 import org.xmldb.api.base.XMLDBException;
 import org.xmldb.api.modules.BinaryResource;
 import org.xmldb.api.modules.XMLResource;
 import org.xmldb.api.modules.XPathQueryService;
 
 /**
  * Collection of functions to ease the use of XMLDB:API and to improve code
  * readability.
 * 
  * @author Alexei Bratuhin
 * 
  */
 public class DbUtil {
 
 	private static final Logger logger = LoggerFactory.getLogger(DbUtil.class);
 
 	public static ResourceSet executeQuery(String collection, String user, String password, String query) {
 		try {
 			Collection col = DatabaseManager.getCollection(collection, user, password);
 			XPathQueryService service = (XPathQueryService) col.getService("XPathQueryService", "1.0");
 			service.setProperty("indent", "yes");
 
 			ResourceSet result = service.query(query);
 			return result;
 
 		} catch (XMLDBException e) {
 			logger.error("Error querying database", e);
 		}
 		return null;
 	}
 
 	public static File downloadXMLResource(String collection, String resource, String user, String password,
 			String tempdirName) {
 		try {
 			File tempdir = FileUtil.getTempdir(tempdirName);
 			File f = new File(tempdir, resource);
 			Collection col = DatabaseManager.getCollection(collection);
 			XMLResource res = (XMLResource) col.getResource(resource);
			new FileOutputStream(f).write(((String) res.getContent()).getBytes());
 			// alternative:
 			// FileUtil.writeXMLStringToFile(f, (String)res.getContent());
 			return f;
 		} catch (XMLDBException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	public static File downloadBinaryResource(String collection, String resource, String user, String password,
 			String tempdirName) throws IOException {
 		try {
 			File tempdir = FileUtil.getTempdir(tempdirName);
 			File f = new File(tempdir, resource);
 			Collection col = DatabaseManager.getCollection(collection);
 			BinaryResource res = (BinaryResource) col.getResource(resource);
 			EXistResource exres = (EXistResource) res;
 			ExtendedResource eres = (ExtendedResource) res;
 
 			if (f.canRead() && f.lastModified() > exres.getLastModificationTime().getTime()
 					&& f.length() == exres.getContentLength()) {
 				logger.debug("Reusing downloaded image {}", f.getName());
 			} else {
 				logger.debug("Downloading {}", resource);
 				eres.getContentIntoAFile(f);
 			}
 			return f;
 		} catch (XMLDBException e) {
 			logger.error("XMLDB error in downloadBinaryResource", e);
 			throw new IOException(e.getLocalizedMessage(), e);
 		}
 	}
 
 	public static void uploadXMLResource(File f, String collection, String user, String password) {
 		try {
 			Collection current = DatabaseManager.getCollection(collection, user, password);
 			XMLResource resource = (XMLResource) current.createResource(f.getName(), "XMLResource");
 			resource.setContent(f);
 			current.storeResource(resource);
 		} catch (XMLDBException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void uploadBinaryResource(File f, String collection, String user, String password) {
 		try {
 			Collection current = DatabaseManager.getCollection(collection, user, password);
 			BinaryResource resource = (BinaryResource) current.createResource(f.getName(), "BinaryResource");
 			resource.setContent(f);
 			current.storeResource(resource);
 		} catch (XMLDBException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void uploadBinaryResources(File[] f, String collection, String user, String password) {
 		try {
 			Collection current = DatabaseManager.getCollection(collection, user, password);
 			for (File element : f) {
 				if (element != null) {
 					logger.debug("Uploading snippet {}", element.getName());
 					BinaryResource resource = (BinaryResource) current.createResource(element.getName(),
 					"BinaryResource");
 					resource.setContent(element);
 					current.storeResource(resource);
 				}
 			}
 		} catch (XMLDBException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static Element[] convertResourceSetToElements(ResourceSet resourceset) {
 		ArrayList<Element> elements = new ArrayList<Element>();
 		try {
 			ResourceIterator iterator = resourceset.getIterator();
 			while (iterator.hasMoreResources()) {
 				XMLResource resource = (XMLResource) iterator.nextResource();
 				Element element = convertXMLResourceToElement(resource);
 				if (element != null)
 					elements.add(element);
 			}
 		} catch (XMLDBException e) {
 			e.printStackTrace();
 		}
 		return elements.toArray(new Element[elements.size()]);
 	}
 
 	public static Element convertXMLResourceToElement(XMLResource xmlresource) {
 		Element element = null;
 		SAXBuilder saxbuilder = new SAXBuilder();
 		try {
 			element = (Element) saxbuilder.build(new StringReader((String) xmlresource.getContent())).getRootElement()
 			.detach();
 		} catch (JDOMException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (XMLDBException e) {
 			e.printStackTrace();
 		}
 		return element;
 	}
 
 	public static String[] convertResourceSetToStrings(ResourceSet resourceset) {
 		ArrayList<String> strings = new ArrayList<String>();
 		try {
 			ResourceIterator iterator = resourceset.getIterator();
 			while (iterator.hasMoreResources()) {
 				Resource resource = iterator.nextResource();
 				String str = (String) resource.getContent();
 				if (str != null)
 					strings.add(str);
 			}
 		} catch (XMLDBException e) {
 			e.printStackTrace();
 		}
 		return strings.toArray(new String[strings.size()]);
 	}
 
 }
