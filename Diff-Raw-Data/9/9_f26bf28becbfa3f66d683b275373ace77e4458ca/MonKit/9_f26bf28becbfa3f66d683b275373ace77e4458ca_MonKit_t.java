 package net.praqma.monkit;
 
 import java.io.BufferedWriter;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Result;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 public class MonKit {
     
     public static final String __ROOT_TAG = "categories";
     
     private File destination = null;
     
     private Element root = null;
     private Document doc = null;
     
     public MonKit( ) {
 	destination = new File( "monkit.xml" );
 	initialize();
     }
     
     public MonKit( File destination ) {
 	initialize();
     }
     
     public MonKit( Document doc ) throws MonKitException {
 	this.doc = doc;
 	Node node = doc.getFirstChild();
 	
 	if( node == null ) {
 	    throw new MonKitException( "Empty MonKit file" );
 	}
 	
 	if( !node.getNodeName().equals(MonKit.__ROOT_TAG) ) {
 	    throw new MonKitException( "Not a valid MonKit format" );
 	}
 	
 	root = (Element) node;
     }
     
     private void initialize() {
 	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 	factory.setNamespaceAware(true);
 	DocumentBuilder builder;
 	try {
 	    builder = factory.newDocumentBuilder();
 	    doc = builder.newDocument();
 	} catch (ParserConfigurationException e) {
 	    e.printStackTrace();
 	}
 
 	/* Preparing the root note */
 	root = (Element) doc.appendChild(doc.createElement(MonKit.__ROOT_TAG));
     }
     
     /**
      * Create a MonKit object from an xml file
      * @param xml A File with the MonKit obersations
      * @return A new MonKit object
      * @throws MonKitException
      */
     public static MonKit fromXML( File xml ) throws MonKitException {
 	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 	factory.setNamespaceAware( true );
 	
 	DocumentBuilder builder;
 	Document doc = null;
 	try {
 	    builder = factory.newDocumentBuilder();
 
 	    doc = builder.parse(xml);
 	} catch (Exception e) {
 	    throw new MonKitException( "Coult not parse the file" );
 	}
 	
 	return new MonKit( doc );
     }
     
     public static MonKit fromString( String str ) throws MonKitException {
 	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 	factory.setNamespaceAware( true );
 	
 	DocumentBuilder builder;
 	Document doc = null;
 	try {
 	    builder = factory.newDocumentBuilder();
 
 	    doc = builder.parse(new ByteArrayInputStream(str.getBytes("UTF-8")));
 	} catch (Exception e) {
 	    throw new MonKitException( "Coult not parse the file" );
 	}
 	
 	return new MonKit( doc );
     }
     
     public void addCategory( String name, String scale ) {
 	if( getCategory(name) != null ) {
 	    return;
 	}
 	
 	Element o = doc.createElement("category");
 	o.setAttribute("name", name);
 	o.setAttribute("scale", scale);
 	root.appendChild(o);
     }
     
     /**
      * Add an observation given the three arguments
      * @param name Name of the observation
      * @param scale The scale of the observation
      * @param value Value of the observation
      * @throws MonKitException
      */
     public void add(String name, String value, String category) {
 	_add(name, value, category);
     }
     
     /**
      * Add an observation given a {@link MonKitObservation}
      * @param mke A MonKitElement
      */
     public void add( MonKitObservation mke, String category ) {
 	_add(mke.getName(), mke.getValue(), category);
     }
     
     private void _add(String name, String value, String category) {
 	Element c = getCategory(category);
 	if( c == null ) {
 	    return;
 	}
 	
 	Element e = getObservation( name, category );
 	
 	if( e != null ) {
 	    e.setTextContent(value);
 	    e.setAttribute("name", name);
 	} else {
 	    Element o = doc.createElement("observation");
 	    o.setAttribute("name", name);
 	    o.setTextContent(value);
 	    c.appendChild(o);
 	}
     }
     
     public void add( List<MonKitCategory> elements ) {
 	for( MonKitCategory mkc : elements ) {
 	    add(mkc);
 	}
     }
     
     public void add( MonKitCategory mkc ) {
 	addCategory(mkc.getName(), mkc.getScale());
 	add( mkc, mkc.getName() );
     }
     
     /**
      * Add a series of observations
      * @param elements A list of {@link MonKitObservation}s
      */
     public void add( List<MonKitObservation> elements, String category ) {
 	for( MonKitObservation mke : elements ) {
 	    add(mke, category);
 	}
     }
     
     public List<MonKitCategory> getCategories() {
 	NodeList nodes = root.getElementsByTagName("category");
 	
 	List<MonKitCategory> elements = new ArrayList<MonKitCategory>();
 	
 	for( int i = 0, len = nodes.getLength() ; i < len ; ++i ) {
 	    Node node = nodes.item(i);
 	    if( node.getNodeType( ) == Node.ELEMENT_NODE ) {
 		Element e = (Element)node;
 		String name = e.getAttribute("name");
 		String scale = e.getAttribute("scale");
 		elements.add( new MonKitCategory(name, scale,getObservations(name)) );
 	    }
 	}
 	
 	return elements;
     }
     
     private Element getCategory( String name ) {
 	NodeList nodes = root.getElementsByTagName("category");
 	
 	for( int i = 0, len = nodes.getLength() ; i < len ; ++i ) {
 	    Node node = nodes.item(i);
 	    if( node.getNodeType( ) == Node.ELEMENT_NODE ) {
 		Element e = (Element)node;
 		if( e.getAttribute("name").equalsIgnoreCase(name) ) {
 		    return e;
 		}
 	    }
 	}
 	
 	return null;
     }
     
     /**
      * Retrieve the observations
      * @return A list of {@link MonKitObservation}s
      */
     public List<MonKitObservation> getObservations( String name ) {
 	Element category = getCategory(name);
 	if( category == null ) {
 	    return null;
 	}
 	
 	NodeList nodes = category.getElementsByTagName("observation");
 	
 	List<MonKitObservation> elements = new ArrayList<MonKitObservation>();
 	
 	for( int i = 0, len = nodes.getLength() ; i < len ; ++i ) {
 	    Node node = nodes.item(i);
 	    if( node.getNodeType( ) == Node.ELEMENT_NODE ) {
 		Element e = (Element)node;
 		elements.add( new MonKitObservation(e.getAttribute("name"), e.getTextContent()) );
 	    }
 	}
 	
 	return elements;
     }
    
     private Element getObservation( String name, String category ) {
 	Element c = getCategory(category);
 	if( c == null ) {
 	    return null;
 	}
 	
 	NodeList nodes = c.getElementsByTagName("observation");
 	
 	for( int i = 0, len = nodes.getLength() ; i < len ; ++i ) {
 	    Node node = nodes.item(i);
 	    if( node.getNodeType( ) == Node.ELEMENT_NODE ) {
 		Element e = (Element)node;
 		if( e.getAttribute("name").equalsIgnoreCase(name) ) {
 		    return e;
 		}
 	    }
 	}
 	
 	return null;
     }
     
     /**
      * Merge two or more {@link MonKit}s 
      * @param mks An array of MonKits
      * @return A new instantiation of MonKit
      */
     public static MonKit merge( MonKit ... mks ) {
 	MonKit nmk = new MonKit();
 	for( MonKit mk : mks ) {
 	    nmk.add(mk.getCategories());
 	}
 	
 	return nmk;
     }
     
     /**
      * Merge two or more {@link MonKit}s 
      * @param mks A List of MonKits
      * @return A new instantiation of MonKit
      */
     public static MonKit merge( List<MonKit> mks ) {
 	MonKit nmk = new MonKit();
 	for( MonKit mk : mks ) {
 	    nmk.add(mk.getCategories());
 	}
 	
 	return nmk;
     }
     
     /**
      * Get the observations as String
      * @return
      */
     public String getXML() {
 	StringWriter out = new StringWriter();
 
 	try {
 	    TransformerFactory factory = TransformerFactory.newInstance();
 
 	    Transformer transformer = factory.newTransformer();
 
 	    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
 	    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
 
 	    transformer.setOutputProperty( "{http://xml.apache.org/xslt}indent-amount", "4" );
 
 	    Source src = new DOMSource( doc );
 
 	    Result dest = new StreamResult(out);
 	    transformer.transform(src, dest);
 	} catch (Exception e) {
 	    e.printStackTrace();
 	}
 
 	return out.toString();
     }
     
     public String toString() {
 	return getXML();
     }
     
     public List<MonKitObservation> toList( String name ) {
 	return getObservations(name);
     }
     
     public void save() throws IOException {
 	save( destination );
     }
     
     public void save(File filename) throws IOException {
 	String xml = getXML();
 	try {
 	    BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
 	    bw.append(xml);
 	    bw.close();
 	} catch (IOException e) {
 	    throw e;
 	}
     }
 }
