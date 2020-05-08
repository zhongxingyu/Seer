 package org.abratuhi.snippettool.util;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.ArrayList;
 
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.input.SAXBuilder;
 import org.xmldb.api.DatabaseManager;
 import org.xmldb.api.base.Collection;
 import org.xmldb.api.base.Database;
 import org.xmldb.api.base.Resource;
 import org.xmldb.api.base.ResourceIterator;
 import org.xmldb.api.base.ResourceSet;
 import org.xmldb.api.base.XMLDBException;
 import org.xmldb.api.modules.BinaryResource;
 import org.xmldb.api.modules.XMLResource;
 import org.xmldb.api.modules.XPathQueryService;
 
 /**
  * Collection of functions to ease the use of XMLDB:API and to improve code readability. 
  * 
  * @author Alexei Bratuhin
  *
  */
 public class DbUtil {
 	
 	public static ResourceSet executeQuery(String collection, String user, String password, String query){
 		try {
 			Database database = (Database) Class.forName("org.exist.xmldb.DatabaseImpl").newInstance();   
 			DatabaseManager.registerDatabase(database); 
 
 			Collection col = DatabaseManager.getCollection(collection, user, password);  
 			XPathQueryService service = (XPathQueryService) col.getService("XPathQueryService", "1.0");  
 			service.setProperty("indent", "yes");
 
 			ResourceSet result = service.query(query);
 			return result;
 			
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (XMLDBException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
	public static File downloadXMLResource(String collection, String resource, String user, String password, String tempdirName){
 		try {
			File tempdir = getTempdir(tempdirName);
			File f = new File(tempdir, resource);
 			Database database = (Database) Class.forName("org.exist.xmldb.DatabaseImpl").newInstance();   
 			DatabaseManager.registerDatabase(database); 
 			Collection col = DatabaseManager.getCollection(collection);
 			XMLResource res = (XMLResource)col.getResource(resource);
 			new FileOutputStream(f).write( ((String) res.getContent()).getBytes());
 			//alternative:
 			//FileUtil.writeXMLStringToFile(f, (String)res.getContent());
 			return f;
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (XMLDBException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}

	/**
	 * Gets the directory at tempdir name, creating it if did not exist.
	 * @param tempdirName
	 * @return a file object representing the tempdir
	 * @throws IOException
	 */
	private static File getTempdir(String tempdirName) throws IOException {
		File tempdir = new File(tempdirName);
		if (!tempdir.isDirectory()) {
			if (tempdir.exists())
				throw new IllegalArgumentException("Supplied tempdirName \""+tempdirName+"\" is not a directory");
			if (!tempdir.mkdirs())
				throw new IOException("Could not create tempdir at tempdirName \""+tempdirName+"\"");
		}
		return tempdir;
	}
 	
	public static File downloadBinaryResource(String collection, String resource, String user, String password, String tempdirName){
 		//System.out.println("DbUtil.downloadBinaryResource("+collection+", "+resource+", "+user+", "+password+", "+tempdir+")");
 		try {
			File tempdir = getTempdir(tempdirName);
			File f = new File(tempdir, resource);
 			Database database = (Database) Class.forName("org.exist.xmldb.DatabaseImpl").newInstance();   
 			DatabaseManager.registerDatabase(database); 
 			Collection col = DatabaseManager.getCollection(collection);
 			BinaryResource res = (BinaryResource)col.getResource(resource);
 			new FileOutputStream(f).write((byte[])res.getContent());
 			return f;
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (XMLDBException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	public static void uploadXMLResource(File f, String collection, String user, String password){
 		try {
 			Database database = (Database) Class.forName("org.exist.xmldb.DatabaseImpl").newInstance();   
 			DatabaseManager.registerDatabase(database);
 			Collection current = DatabaseManager.getCollection(collection, user, password);
 	        XMLResource resource = (XMLResource) current.createResource(f.getName(), "XMLResource");
 	        resource.setContent(f);
 	        current.storeResource(resource);
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (XMLDBException e) {
 			e.printStackTrace();
 		} 
 	}
 	
 	public static void uploadBinaryResource(File f, String collection, String user, String password){
 		try {
 			Database database = (Database) Class.forName("org.exist.xmldb.DatabaseImpl").newInstance();   
 			DatabaseManager.registerDatabase(database);
 			Collection current = DatabaseManager.getCollection(collection, user, password);
 	        BinaryResource resource = (BinaryResource) current.createResource(f.getName(), "BinaryResource");
 	        resource.setContent(f);
 	        current.storeResource(resource);
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (XMLDBException e) {
 			e.printStackTrace();
 		} 
 	}
 	
 	public static void uploadBinaryResources(File[] f, String collection, String user, String password){
 		try {
 			Database database = (Database) Class.forName("org.exist.xmldb.DatabaseImpl").newInstance();   
 			DatabaseManager.registerDatabase(database);
 			Collection current = DatabaseManager.getCollection(collection, user, password);
 			for(int i=0; i<f.length; i++){
 		        BinaryResource resource = (BinaryResource) current.createResource(f[i].getName(), "BinaryResource");
 		        resource.setContent(f[i]);
 		        current.storeResource(resource);
 			}
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (XMLDBException e) {
 			e.printStackTrace();
 		} 
 	}
 
 	public static Element[] convertResourceSetToElements(ResourceSet resourceset){
 		ArrayList<Element> elements = new ArrayList<Element>();
 		try {
 			ResourceIterator iterator = resourceset.getIterator();
 			while(iterator.hasMoreResources()){
 				XMLResource resource = (XMLResource) iterator.nextResource();
 				Element element = convertXMLResourceToElement(resource);
 				if(element != null) elements.add(element);
 			}
 		} catch (XMLDBException e) {
 			e.printStackTrace();
 		}
 		return elements.toArray(new Element[elements.size()]);
 	}
 
 	public static Element convertXMLResourceToElement(XMLResource xmlresource){
 		Element element = null;
 		SAXBuilder saxbuilder = new SAXBuilder();
 		try {
 			element = (Element) saxbuilder.build(new StringReader((String) xmlresource.getContent())).getRootElement().detach();
 		} catch (JDOMException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (XMLDBException e) {
 			e.printStackTrace();
 		}
 		return element;
 	}
 	
 	public static String[] convertResourceSetToStrings(ResourceSet resourceset){
 		ArrayList<String> strings = new ArrayList<String>();
 		try {
 			ResourceIterator iterator = resourceset.getIterator();
 			while(iterator.hasMoreResources()){
 				Resource resource = iterator.nextResource();
 				String str = (String) resource.getContent();
 				if(str != null) strings.add(str);
 			}
 		} catch (XMLDBException e) {
 			e.printStackTrace();
 		}
 		return strings.toArray(new String[strings.size()]);
 	}
 
 }
