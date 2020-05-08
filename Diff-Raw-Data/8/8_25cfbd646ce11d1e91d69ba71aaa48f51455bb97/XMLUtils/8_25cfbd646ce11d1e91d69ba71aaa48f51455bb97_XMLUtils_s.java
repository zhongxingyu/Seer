 package com.bezzotech.oracleucm.arx.common;
 
 import intradoc.common.ExecutionContext;
 import intradoc.common.Report;
 import intradoc.common.ServiceException;
 import intradoc.common.StringUtils;
 import intradoc.data.DataBinder;
 import intradoc.data.DataResultSet;
 import intradoc.server.Service;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.util.Enumeration;
 import java.util.Map;
 import java.util.Iterator;
 import java.util.Vector;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.DOMImplementation;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Text;
 import org.w3c.dom.ls.DOMImplementationLS;
 import org.w3c.dom.ls.LSOutput;
 import org.w3c.dom.ls.LSSerializer;
 
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import com.bezzotech.oracleucm.arx.shared.SharedObjects;
 
 public class XMLUtils {
 	/** The context for this request. */
 	public ExecutionContext m_context;
 	public Service m_service;
 
 	/** DataBinder The DataBinder for this request. */
 	public DataBinder m_binder;
 
 	/** SharedObjects pointer to use for this request. */
 	public SharedObjects m_shared;
 
 	protected XMLUtils( ExecutionContext context ) {
 		m_context = context;
 		m_shared = SharedObjects.getSharedObjects( m_context );
 		if ( m_context instanceof Service ) {
 			m_service = ( Service )m_context;
 			m_binder = m_service.getBinder();
 		}
 	}
 
 	/** Return a working FileStoreUtils object for a service.
 	 * @param context ExecutionContext to find a FileStoreProvider in.
 	 * @throws ServiceException if a FileStoreProvider cannot be found.
 	 * @return a ready-to-use FileStoreUtils object.
 	 */
 	static public XMLUtils getXMLUtils( ExecutionContext context ) {
 		return new XMLUtils( context );
 	}
 
 	/** Generate a blank Document object
 	 *
 	 */
 	public Document getNewDocument() throws ServiceException {
 		Report.debug( "bezzotechcosign", "Entering getNewDocument", null );
 		Document dom = null;
 		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
 		try {
 			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
 			dom = documentBuilder.newDocument();
 		} catch ( ParserConfigurationException e ) {
 			throwFullError( e );
 		}
 		return dom;
 	}
 
 	/** Generate Document object from file path
 	 *
 	 */
 	public Document getExistingDocument( String path ) throws ServiceException {
 		Report.debug( "bezzotechcosign", "Entering getExistingDocument, passed in parameter(s):\n\tpath: " +
 				path, null );
 		Document dom = null;
 		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
 		try {
 			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
 			dom = documentBuilder.parse( path );
 		} catch ( ParserConfigurationException e ) {
 			throwFullError( e );
 		} catch ( IOException e ) {
 			throwFullError( e );
 		} catch ( SAXException e ) {
 			throwFullError( e );
 		}
 		return dom;
 	}
 
 	/** Generate Document object from string content
 	 *
 	 */
 	public Document getNewDocument( String contents ) throws ServiceException {
 		Document dom = null;
 		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
 		StringReader _sr = null;
 		try {
 			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
 			_sr = new StringReader( contents );
 			dom = documentBuilder.parse( new InputSource( _sr ) );
 		} catch ( ParserConfigurationException e ) {
 			throwFullError( e );
 		} catch ( SAXException e ) {
 			throwFullError( e );
 		} catch ( IOException e ) {
 			throwFullError( e );
 		} finally {
 			_sr.close();
 		}
 		return dom;
 	}
 
 	/** Build XML document from binder local properties
 	 *
 	 *  appName  - [String] application name for extracting from binder
 	 *  doc      - [Document] XML document to create text node
 	 *  rootName - String - Name of Environmental base to retrieve from binder (this should also be
 	 *    the name of the expect XML Node we will be passing back
 	 *  Note: Environmental variable "fields" will be expected to return the field names available to
 	 *    retrieve
 	 *  Note: Environmental variables will be expected to be presented in the following format:
 	 *  {Application Name}.{XML Node name}.{Field Name}
 	 *  Where: "Application Name" will be stored within the application code
 	 *    "XML Node Name" will be determined by the expected XML output
 	 *    "Field Name" will be determined by the expected XML output
 	 */
 	public Element appendChildrenFromEnvironmental( String appName, Document doc, String rootName,
 			boolean appendTo ) throws ServiceException {
 		Report.debug( "bezzotechcosign", "Entering appendChildrenFromEnvironmental, passed in parameters:" +
 				"\n\tappName: " + appName + "\n\tdoc:\n\trootName: " + rootName + "\n\tappendTo: " + appendTo,
 				null );
 		Map fields = m_shared.getConfigSet( appName + "." + rootName );
 		Element root;
 		if ( appendTo ) root = ( Element )doc.getElementsByTagName( rootName ).item( 0 );
 		else root = doc.createElement( rootName );
 		for ( Iterator < String > fieldIter = fields.keySet().iterator(); fieldIter.hasNext(); ) {
 			String fieldName = ( String )fieldIter.next();
 			Element child = doc.createElement( fieldName );
 			try {
 				String fieldValue = m_service.getPageMerger()
 						.evaluateScript ( m_shared.getConfig( appName + "." + rootName + "." + fieldName ) );
				if( fieldValue == "0" ) fieldValue = "false";
				else if( fieldValue == "1" ) fieldValue = "true";
 				Text text = doc.createTextNode( fieldValue );
 				child.appendChild( text );
 			} catch ( IOException e ) {
 				throwFullError( e );
 			}
 			root.appendChild( child );
 		}
 		return root;
 	}
 
 	/** Build XML document from binder local properties
 	 *
 	 *  appName  - [String] application name for extracting from binder
 	 *  doc      - [Document] XML document to create text node
 	 *  rootName - [String] name of Local base to retrieve from binder (this should also be the name of
 	 *    the expect XML Node we will be passing back
 	 *  Note: Local variable "fields" will be expected to return the field names available to retrieve
 	 *  Note: Local variables will be expected to be presented in the following format:
 	 *    		{Application Name}.{XML Node name}.{Field Name}
 	 *  		Where: "Application Name" will be stored within the application code
 	 *    		"XML Node Name" will be determined by the expected XML output
 	 *    		"Field Name" will be determined by the expected XML output
 	 *
 	 *  Throws [ServiceException] error if not {appName}.{rootName}.fields defined in binder
 	 *  Throws [ServiceException] error if {appName}.{rootName}.fields value is blank
 	 */
 	public Element appendChildrenFromLocal( String appName, Document doc, String rootName )
 			throws ServiceException {
 		Report.debug( "bezzotechcosign", "Entering appendChildrenFromLocal, passed in parameters:" +
 				"\n\tappName: " + appName + "\n\tdoc:\n\trootName: " + rootName, null );
 		String locStr = m_binder.getLocal( appName + "." + rootName + ".fields" );
 		if ( locStr == null )
 			throw new ServiceException( appName + " has not been installed properly" );
 
 		Vector fields = ( Vector )StringUtils.parseArray( locStr, ';', '\\' );
 		Element root = doc.createElement( rootName );
 		for ( Enumeration fieldsEnum = fields.elements(); fieldsEnum.hasMoreElements(); ) {
 			String fieldName = ( String )fieldsEnum.nextElement();
 			String fieldValue = m_binder.getLocal( appName + "." + rootName + "." + fieldName );
 			if ( fieldValue != null || fieldValue != "" ) {
 				Vector fieldValues = StringUtils.parseArray( fieldValue, ',', '\\' );
 				for ( Enumeration fieldValuesEnum = fieldValues.elements(); fieldValuesEnum.hasMoreElements(); ) {
 					Element child = doc.createElement( fieldName );
 					fieldValue = ( String )fieldValuesEnum.nextElement();
 					Text text = doc.createTextNode( fieldValue );
 					child.appendChild( text );
 					root.appendChild( child );
 				}
 			} else {
 				Element child = doc.createElement( fieldName );
 				Text text = doc.createTextNode( "&#x200B;" );
 				child.appendChild( text );
 				root.appendChild( child );
 			}
 		}
 		return root;
 	}
 
 	/** Parse XML document at root element down through the named base element
 	 *
 	 *  appName  - [String] application name for extracting from binder
 	 *  root     - [Element] XML node, housing named node to inject in
 	 *  baseName - [String] name of Element under root
 	 */
 	public void parseChildrenToLocal( String appName, Element root, String baseName, int index ) {
 		Report.trace( "bezzotechcosign", "Entering parseChildrenToLocal, passed in parameter(s):" +
 				"\n\tappName: " + appName + "\n\troot:\n\tbaseName: " + baseName, null );
 		Element base = ( Element )root.getElementsByTagName( baseName ).item( index );
 		NodeList children = base.getChildNodes();
 		String fields = "";
 		for ( int i = 0; i < children.getLength(); i++ ) {
 			Node node = children.item( i );
 			if ( node.getNodeType() == Node.ELEMENT_NODE ) {
 				Element child = ( Element )node;
 				String fieldName = child.getTagName();
 				String localValue = m_binder.getLocal( appName + "." + baseName + "." + fieldName );
 				String fieldValue = child.getTextContent();
 				if ( localValue != null ) { fieldValue += "," + localValue; }
 				m_binder.putLocal( appName + "." + baseName + "." + fieldName, fieldValue );
 				fields = fields + (fields == "" ? "" : ";") + fieldName;
 			}
 		}
 		m_binder.putLocal( appName + "." + baseName + ".fields", fields );
 	}
 
 	public void parseChildrenToResultSet( String appName, Element root, String parentName ) {
 		Report.trace( "bezzotechcosign", "Entering parseChildrenToResultSet, passed in parameter(s):" +
 				"\n\tappName: " + appName + "\n\troot:\n\tparentName: " + parentName, null );
 		Element parent = ( Element )root.getElementsByTagName( parentName ).item( 0 );
 		NodeList children = parent.getChildNodes();
 		if( children.getLength() > 0 ) {
 			DataResultSet rset = null;
 			for( int i = 0; i < children.getLength(); i++ ) {
 				Node node = children.item( i );
 				if( node.getNodeType() == Node.ELEMENT_NODE ) {
 					Element child = ( Element )node;
 					NodeList gchildren = child.getChildNodes();
 					if( rset == null ) {
 
 						// Build result set from XML fields
 						Vector < String > fieldList = new Vector < String > ();
 						for( int j = 0; j < gchildren.getLength(); j++ ) {
 							Node gnode = gchildren.item( j );
 							if( gnode.getNodeType() == Node.ELEMENT_NODE ) {
 								Element gchild = ( Element )gnode;
 								fieldList.add( j, gchild.getTagName() );
 							}
 						}
 						rset = new DataResultSet( fieldList );
 					}
 					Vector row = rset.createEmptyRow();
 					for( int k = 0; k < gchildren.getLength(); k++ ) {
 						Node gnode = gchildren.item( k );
 						if( gnode.getNodeType() == Node.ELEMENT_NODE ) {
 							Element gchild = ( Element )gnode;
 							String rowName = gchild.getTagName();
 							int colIndex = rset.getFieldInfoIndex( rowName );
 							String rowValue = gchild.getTextContent();
 							row.setElementAt( rowValue, colIndex );
 						}
 					}
 					rset.insertRowAt( row, i );
 				}
 			}
 			if( rset != null )
 				m_binder.addResultSet( parentName, rset );
 		}
 	}
 
 	/** Translate XML Document to string
 	 *
 	 *  doc   - [Document] XML document to create text node
 	 */
 	public String getStringFromDocument( Document doc ) {
 		Report.trace( "bezzotechcosign", "Entering getStringFromDocument", null );
 		DOMImplementation domImpl = doc.getImplementation();
 		DOMImplementationLS domImplLS = ( DOMImplementationLS )domImpl.getFeature( "LS", "3.0" );
 		LSSerializer serializer = domImplLS.createLSSerializer();
 		serializer.getDomConfig().setParameter( "xml-declaration", Boolean.valueOf( false ) );
 		LSOutput lsOutput = domImplLS.createLSOutput();
 		lsOutput.setEncoding( "UTF-8" );
 		StringWriter _out = new StringWriter();
 		lsOutput.setCharacterStream( _out );
 		serializer.write(doc, lsOutput);
 		return _out.toString();
 	}
 
 	/**	Injects values, as text node, into named node under root element
 	 *
 	 *  doc   - [Document] XML document to create text node
 	 *  root  - [Element] XML node, housing named node to inject in
 	 *  name  - [String] Name of element to inject value into
 	 *  value - [String] Text to insert
 	 *
 	 *  Throws [ServiceException] error if named element not found
 	 */
 	public Element appendTextNodeToChild( Document doc, Element root, String name, String value )
 			throws ServiceException {
 		NodeList nl = root.getElementsByTagName( name );
 		if( nl == null || nl.getLength() == 0 ) throw new ServiceException( "csCoSignNotConfigProperly" );
 		Node n = nl.item( 0 );
 		Text t = doc.createTextNode( value );
 		n.appendChild( t );
 		return root;
 	}
 
 	/** Prints out error message and stack trace from caught exceptions and throws them as message in
 	 *  ServiceException
 	 */
 	protected void throwFullError( Exception e ) throws ServiceException {
 		StringBuilder sb = new StringBuilder();
 		for( StackTraceElement element : e.getStackTrace() ) {
 			sb.append( element.toString() );
 			sb.append( "\n" );
 		}
 		Report.debug( "bezzotechcosign", e.getMessage() + "\n" + sb.toString(), null );
 		throw new ServiceException( e.getMessage() + "\n" + sb.toString() );
 	}
 }
