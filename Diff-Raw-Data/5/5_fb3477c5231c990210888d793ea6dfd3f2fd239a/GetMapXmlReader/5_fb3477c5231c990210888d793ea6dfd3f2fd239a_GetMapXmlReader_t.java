 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.wms.requests;
 
 import java.awt.Color;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.geotools.filter.ExpressionDOMParser;
 import org.geotools.filter.FilterFilter;
 import org.geotools.gml.GMLFilterDocument;
 import org.geotools.gml.GMLFilterGeometry;
 import org.geotools.styling.SLDParser;
 import org.geotools.styling.Style;
 import org.geotools.styling.StyleFactory;
 import org.geotools.styling.StyledLayer;
 import org.geotools.styling.StyledLayerDescriptor;
 import org.geotools.styling.UserLayer;
 import org.vfny.geoserver.Request;
 import org.vfny.geoserver.global.FeatureTypeInfo;
 import org.vfny.geoserver.global.TemporaryFeatureTypeInfo;
 import org.vfny.geoserver.util.GETMAPValidator;
 import org.vfny.geoserver.util.SLDValidator;
 import org.vfny.geoserver.util.requests.readers.XmlRequestReader;
 import org.vfny.geoserver.wfs.WfsException;
 import org.vfny.geoserver.wfs.requests.FeatureHandler;
 import org.vfny.geoserver.wms.WmsException;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 import org.xml.sax.helpers.ParserAdapter;
 
 import com.vividsolutions.jts.geom.Coordinate;
 
 
 /**
  * reads in a GetFeature XML WFS request from a XML stream
  *
  * @author Rob Hranac, TOPP
  * @author Chris Holmes, TOPP
  * @version $Id$
  */
 public class GetMapXmlReader extends XmlRequestReader 
 {
 	
 	  private static final StyleFactory styleFactory = StyleFactory.createStyleFactory();
 
 	  
 	  
     /**
      * Creates a new GetMapXmlReader object.
      */
     public GetMapXmlReader() {
     }
 
     
     /**
      * Reads the GetMap XML request into a GetMap Request object.
      *
      * @param reader The plain POST text from the client.
      *
      * @return The GetMap Request from the xml reader.
      *
      * @throws WmsException For any problems reading the request.
      */
     public Request read(Reader reader, HttpServletRequest req) throws WmsException 
 	{
         GetMapRequest getMapRequest = new GetMapRequest();
         getMapRequest.setHttpServletRequest(req);
         boolean validateSchema = wantToValidate(req);
         
         try{
         	parseGetMapXML(reader,getMapRequest,validateSchema );
         }
         catch (java.net.UnknownHostException unh)
 		{
         	 //http://www.oreilly.com/catalog/jenut2/chapter/ch19.html ---
         	// There is one complication to this example. Most web.xml files contain a <!DOCTYPE> tag that specifies 
         	//the document type (or DTD). Despite the fact that Example 19.1 specifies that the parser should not 
         	//validate the document, a conforming XML parser must still read the DTD for any document that has a 
         	//<!DOCTYPE> declaration. Most web.xml have a declaration like this:
         	//..
         	//In order to read the DTD, the parser must be able to read the specified URL. If your system is not 
         	//connected to the Internet when you run the example, it will hang.
         	//. Another workaround to this DTD problem is to simply remove (or comment out) the <!DOCTYPE> declaration from the web.xml file you process with ListServlets1./
         	//
         	//also see:
         	//http://doctypechanger.sourceforge.net/
         	
         	throw new WmsException("unknown host - "+unh.getLocalizedMessage()+" - if its in a !DOCTYPE, remove the !DOCTYPE tag.");
 		}
         catch (SAXParseException se)
 		{
         	throw new WmsException("line "+se.getLineNumber()+" column "+se.getColumnNumber()+" -- "+  se.getLocalizedMessage() );
 		}
         catch (Exception e)
 		{
         	throw new WmsException( e );
 		}
         return getMapRequest;
     }
 
 	/**
 	 *  Actually read in the XML request and stick it in the request object.
 	 *  We do this using the DOM parser (because the SLD parser is DOM based and
 	 *  we can integrate).
 	 * 
 	 *     1. parse into DOM
 	 *     2. parse the SLD 
 	 *     3. grab the rest of the attribute
 	 *     4. stuff #3 attributes in the request object
 	 *     5. stuff the SLD info into the request object
 	 *     6. return
 	 * 
 	 *  GetMap schema is at http://www.opengeospatial.org/docs/02-017r1.pdf (page 18)
 	 * 
 	 *   NOTE: see handlePostGet() for people who put the SLD in the POST and the parameters in the GET.
 	 * 
 	 * @param reader
 	 * @param getMapRequest
 	 */
 	private void parseGetMapXML(Reader xml, GetMapRequest getMapRequest,boolean validateSchema)  throws Exception
 	{
 		File temp = null;
 		
 		if (validateSchema)  //copy POST to a file
 		{
 			//make tempfile
 	           temp = File.createTempFile("getMapPost", "xml");
 	           temp.deleteOnExit();
 
 	            FileOutputStream fos = new FileOutputStream(temp);
 	            BufferedOutputStream out = new BufferedOutputStream(fos);
 
 	            int c;
 	            while (-1 != (c = xml.read())) {
 	                out.write(c);
 	            }
 
 	            xml.close();
 	            out.flush();
 	            out.close();
 	            xml = new BufferedReader(new FileReader(temp));   // pretend like nothing has happened
 		}
 		
 		try
 		{		
 				javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
 		
 				dbf.setExpandEntityReferences(false);
 				dbf.setValidating(false);
 				dbf.setNamespaceAware(true);
 			
 					javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
 					
 					InputSource input = new InputSource(xml);
 					org.w3c.dom.Document dom = db.parse( input );
 					
 					SLDParser sldParser = new SLDParser(styleFactory); 
 					
 					Node rootNode = dom.getDocumentElement();
 						
 					// we have the SLD component, now we get all the GetMAp components
 					     // step a  -- attribute "version"
 					    Node nodeGetMap = rootNode;
 						if (!(nodeNameEqual(nodeGetMap,"getmap"))) 
 						{
 							if (nodeNameEqual(nodeGetMap,"StyledLayerDescriptor"))  //oopsy!!  its a SLD POST with get parameters!
 							{								
 								if (validateSchema)
 								{
 									validateSchemaSLD(temp,getMapRequest);
 								}
 								handlePostGet(rootNode,sldParser,getMapRequest);
 								return;
 							}
 								
 							throw new Exception ("GetMap XML parser - start node isnt 'GetMap' or 'StyledLayerDescriptor' tag");
 						}
 						if (validateSchema)
 						{
 							validateSchemaGETMAP(temp,getMapRequest);
 						}
 						
 						NamedNodeMap atts = nodeGetMap.getAttributes();
 						Node wmsVersion = atts.getNamedItem("version");
 						if (wmsVersion == null)
 							throw new Exception ("GetMap XML parser - couldnt find attribute 'version' in GetMap tag");
 						getMapRequest.setVersion( wmsVersion.getNodeValue() );
 					       //ignore the OWSType since we know its supposed to be WMS
 						
 						   //step b -bounding box
 						parseBBox(getMapRequest, nodeGetMap);
 		
 						  // for SLD we already have it (from above) (which we'll handle as layers later)
 						StyledLayerDescriptor sld = sldParser.parseDescriptor( getNode(rootNode,"StyledLayerDescriptor"));
 						processStyles(getMapRequest,sld);
 						   //step c - "Output"
 						  parseXMLOutput(nodeGetMap,getMapRequest); //make this function easier to read
 						  
 						   //step d - "exceptions"
 						  getMapRequest.setExceptions(getNodeValue(nodeGetMap,"Exceptions"));
 						  
 						   //step e - "VendorType
 						   // we dont actually do anything with this, so...
 						  
 						   //step f - rebuild SLD info.  Ie. fill in the Layer and Style information, just like SLD post-get
 		}
 		finally
 		{
 			if (temp != null)
 				temp.delete();
 		}
                    
 	}
 	
 	
 
 	/**
 	 * 
 	 * This is the hybrid SLD POST way.
 	 *   Normal is the GetMap - with a built in SLD.
 	 * 
 	 *   The alternate, for stupid people, is the WMS parameters in the GET, and the SLD in the post.
 	 *   This handles that case.
 	 * 
 	 * @param rootNode
 	 * @param sldParser
 	 * @param getMapRequest
 	 */
 	private void handlePostGet(Node rootNode, SLDParser sldParser, GetMapRequest getMapRequest) throws Exception
 	{
 		
 		//get the GET parmeters
 		HttpServletRequest request = getMapRequest.getHttpServletRequest();
 	
             String qString = request.getQueryString();
             LOGGER.fine("reading request: " + qString);
 
             //Map requestParams = KvpRequestReader.parseKvpSet(qString);
             Map requestParams = new HashMap();
             String paramName;
             String paramValue;
 
             for (Enumeration pnames = request.getParameterNames();
                     pnames.hasMoreElements();) {
                 paramName = (String) pnames.nextElement();
                 paramValue = request.getParameter(paramName);
                 requestParams.put(paramName.toUpperCase(), paramValue);
             }
 		 
 		GetMapKvpReader kvpReader = new GetMapKvpReader(requestParams);
 		
 
         String version = kvpReader.getRequestVersion();
         getMapRequest.setVersion(version);
 
         kvpReader.parseMandatoryParameters(getMapRequest,false); //false means dont do styles/layers (see below)
         kvpReader.parseOptionalParameters(getMapRequest);
         
         
         //get styles/layers from the sld.
        
         StyledLayerDescriptor sld = sldParser.parseDescriptor(rootNode); //root = <StyledLayerDescriptor>
 		processStyles(getMapRequest,sld);		
 	}
 
 
 	/**
 	 *  taken from the kvp reader, with modifications
 	 * 
 	 * @param getMapRequest
 	 * @param sld
 	 */
 	private void processStyles(GetMapRequest getMapRequest, StyledLayerDescriptor sld) throws Exception
 	{
 		    final StyledLayer[] styledLayers = sld.getStyledLayers();
 	        final int slCount = styledLayers.length;
 
 	        if (slCount == 0) {
 	            throw new WmsException("SLD document contains no layers");
 	        }
 	        
 	        final List layers = new ArrayList();
 	        final List styles = new ArrayList();
 	        FeatureTypeInfo currLayer;
 	        Style currStyle;
 	        
 	        StyledLayer sl = null;
 	        
 	        for (int i = 0; i < slCount; i++) 
 	        {
                 sl = styledLayers[i];
                 String layerName = sl.getName();
                 if(null == layerName)
                 	throw new WmsException("A UserLayer without layer name was passed");
                 
                 // TODO: add support for remote WFS here
                 //handle the InLineFeature stuff
                 if ((sl instanceof UserLayer) && (((UserLayer)sl)).getInlineFeatureDatastore()!=null )
                 {
                 	//SPECIAL CASE - we make the temporary version
                 	UserLayer ul = ((UserLayer)sl);
                 	currLayer = new TemporaryFeatureTypeInfo(ul.getInlineFeatureDatastore(), ul.getInlineFeatureType());
                 }
                 else
                 	currLayer = GetMapKvpReader.findLayer(getMapRequest, layerName);
                 
                 GetMapKvpReader.addStyles(getMapRequest,currLayer,styledLayers[i],   layers,styles);
 	        }
 	        
 	        getMapRequest.setLayers((FeatureTypeInfo[])layers.toArray(new FeatureTypeInfo[layers.size()]));
 	        getMapRequest.setStyles(styles);
 		
 	}
 
 
 	/**
 	 * <xs:element name="BoundingBox" type="gml:BoxType"/>
 	 *  dont forget the SRS!
 	 * 
 	 * @param getMapRequest
 	 * @param nodeGetMap
 	 */
 	private void parseBBox(GetMapRequest getMapRequest, Node nodeGetMap)  throws Exception
 	{
 		Node bboxNode = getNode(nodeGetMap,"BoundingBox");
 		if (bboxNode == null)
 			throw new Exception ("GetMap XML parser - couldnt find node 'BoundingBox' in GetMap tag");
 		
 		List coordList = ExpressionDOMParser.parseCoords(bboxNode);
 		if (coordList.size() != 2)
 			throw new Exception ("GetMap XML parser - node 'BoundingBox' in GetMap tag should have 2 coordinates in it");
 		
 		 com.vividsolutions.jts.geom.Envelope env = new com.vividsolutions.jts.geom.Envelope();
 
          for (int i = 0; i < coordList.size(); i++) {
              env.expandToInclude((Coordinate) coordList.get(i));
          }
          
 		getMapRequest.setBbox(env);
 		
 		// SRS
 		
 		NamedNodeMap atts = bboxNode.getAttributes();
 		Node srsNode = atts.getNamedItem("srsName");
 		if (srsNode != null)
 		{
 			 String srs = srsNode.getNodeValue();
 			 String epsgCode = srs.substring(srs.indexOf('#')+1);
 			 getMapRequest.setCrs("EPSG:"+epsgCode);
 		}
 
 	}
 
 
 	/**
 	 * 
 	 * <xs:element name="Format" type="ogc:FormatType"/>
 	 * <xs:element name="Transparent" type="xs:boolean" minOccurs="0"/>
 	 * <xs:element name="BGcolor" type="xs:string" minOccurs="0"/>
 	 * <xs:element name="Size">
 	 * 	<xs:complexType>
 	 * 	<xs:sequence>
 	 * 		<xs:element name="Width" type="xs:positiveInteger"/>
 	 * 		<xs:element name="Height" type="xs:positiveInteger"/>
 	 * 	</xs:sequence>
 	 * 	</xs:complexType>
 	 * </xs:element><!--Size-->
 	 * 
 	 * @param nodeGetMap
 	 * @param getMapRequest
 	 */
 	private void parseXMLOutput(Node nodeGetMap, GetMapRequest getMapRequest) throws Exception
 	{
 		Node outputNode = getNode(nodeGetMap,"Output");
 		if (outputNode == null)
 			throw new Exception ("GetMap XML parser - couldnt find node 'Output' in GetMap tag");
 		//Format
 		String format = getNodeValue(outputNode,"Format");
 		if (format == null)
 			throw new Exception ("GetMap XML parser - couldnt find node 'Format' in GetMap/Output tag");
 		getMapRequest.setFormat(format);
 		
 		//Transparent
 		String trans = getNodeValue(outputNode,"Transparent");
 		if (trans != null)
 		{			
 			if (  trans.equalsIgnoreCase("false") || trans.equalsIgnoreCase("0") )
 					getMapRequest.setTransparent(false);
 			else
 					getMapRequest.setTransparent(true);
 		}
 		
 		//BGColor
 		String bgColor = getNodeValue(outputNode,"BGcolor");
 		if (bgColor != null)
 		{			
 			getMapRequest.setBgColor(  Color.decode(bgColor) );
 		}
 		
 		//SIZE
 		Node sizeNode = getNode(outputNode,"Size");
 		if (sizeNode == null)
 			throw new Exception ("GetMap XML parser - couldnt find node 'Size' in GetMap/Output tag");
 		
 		 //Size/Width
 		String width = getNodeValue(sizeNode,"Width");
 		if (width == null)
 			throw new Exception ("GetMap XML parser - couldnt find node 'Width' in GetMap/Output/Size tag");
 		getMapRequest.setWidth( Integer.parseInt(width ));
 		
 		 //Size/Height
 		String height = getNodeValue(sizeNode,"Height");
 		if (width == null)
 			throw new Exception ("GetMap XML parser - couldnt find node 'Height' in GetMap/Output/Size tag");
 		getMapRequest.setHeight( Integer.parseInt(height ));		
 	}	
 
 	/**
 	 *  *   Give a node and the name of a child of that node, return it.
 	 *   This doesnt do anything complex.
      *
 	 * @param parentNode
 	 * @param wantedChildName
 	 * @return
 	 */
 	public Node getNode(Node parentNode, String wantedChildName)
 	{
 		NodeList children = parentNode.getChildNodes();
 
 		for (int i = 0; i < children.getLength(); i++) 
 		{
 			Node child = children.item(i);
 
 			if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
 				continue;
 			}
 			String childName = child.getLocalName();
 			if (childName == null) {
 				childName = child.getNodeName();
 			}
 			if (childName.equalsIgnoreCase(wantedChildName)) 
 			{
 				return child;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 *   Give a node and the name of a child of that node, find its (string) value.
 	 *   This doesnt do anything complex.
 	 *  
 	 * @param node
 	 * @param childName
 	 * @return
 	 */
 	public String getNodeValue(Node parentNode, String wantedChildName)
 	{
 		NodeList children = parentNode.getChildNodes();
 
 		for (int i = 0; i < children.getLength(); i++) 
 		{
 			Node child = children.item(i);
 
 			if ((child == null) || (child.getNodeType() != Node.ELEMENT_NODE)) {
 				continue;
 			}
 			String childName = child.getLocalName();
 			if (childName == null) {
 				childName = child.getNodeName();
 			}
 			if (childName.equalsIgnoreCase(wantedChildName)) 
 			{
 				return child.getChildNodes().item(0).getNodeValue();
 			}
 		}
 		return null;
 	}
 	
 	
 
 	/**
 	 * returns true if this node is named "name".  Ignores case and namespaces.
 	 * @param n
 	 * @param name
 	 * @return
 	 */
 	public boolean nodeNameEqual(Node n, String name)
 	{
 		if (n.getNodeName().equalsIgnoreCase(name))
 			return true;
 		String nname = n.getNodeName();
 		int idx = nname.indexOf(':');
 		
 		if (idx == -1)
 			return false;
 		
 		if (nname.substring(idx+1).equalsIgnoreCase(name))
 			return true;
 		return false;		
 	}
 
 	/**
 	 * 
 	 *  This should only be called if the xml starts with <StyledLayerDescriptor>
 	 *  Don't use on a GetMap.
 	 * 
 	 * @param xml
 	 * @param getMapRequest
 	 * @throws Exception
 	 */
 	public void validateSchemaSLD(File f,GetMapRequest getMapRequest) throws Exception
 	{
 		SLDValidator validator = new SLDValidator();
     	List errors =null;
     	try { 
     		FileInputStream in = null;
     		try{
     			in = new FileInputStream(f);
     			errors = validator.validateSLD(in, getMapRequest.getHttpServletRequest().getSession().getServletContext());
     		}
     		finally{
     			if (in != null)
     				in.close();
     		}
     		
     		if (errors.size() != 0)
     		{
     			in = new FileInputStream(f);
     			throw new WmsException(SLDValidator.getErrorMessage(in,errors));
     		}
     	}
     	catch (IOException e)
 		{
     		String msg = "Creating remote SLD url: " + e.getMessage();
             LOGGER.log(Level.WARNING, msg, e);
             throw new WmsException(e, msg, "parseSldParam");
 		}
 	}
 	
 	/**
 	 * 
 	 *  This should only be called if the xml starts with <GetMap>
 	 *  Don't use on a SLD.
 	 * 
 	 * @param xml
 	 * @param getMapRequest
 	 * @throws Exception
 	 */
 	public void validateSchemaGETMAP(File f,GetMapRequest getMapRequest) throws Exception
 	{
 		GETMAPValidator validator = new GETMAPValidator();
     	List errors =null;
     	try { 
     		FileInputStream in = null;
     		try{ 
     			in = new FileInputStream(f);
     			errors = validator.validateGETMAP(in, getMapRequest.getHttpServletRequest().getSession().getServletContext());
     		}
     		finally{
     			if (in != null)
     				in.close();
     		}
     		
     		if (errors.size() != 0)
     		{
     			in = new FileInputStream(f);
     			throw new WmsException(GETMAPValidator.getErrorMessage(in,errors));
     		}
     	}
     	catch (IOException e)
 		{
     		String msg = "Creating remote GETMAP url: " + e.getMessage();
             LOGGER.log(Level.WARNING, msg, e);
             throw new WmsException(e, msg, "GETMAP validator");
 		}
 	}
 	
 	
     /**
 	 * @param httpServletRequest
 	 * @return
 	 */
 	private boolean wantToValidate(HttpServletRequest request) 
 	{
 		
 		String queryString = request.getQueryString(); // ie.   FORMAT=image/png&TRANSPARENT=TRUE&HEIGHT=480&REQUEST=GetMap&BBOX=-73.94896388053894,40.77323718492597,-73.94105110168456,40.77796711500081&WIDTH=803&SRS=EPSG:4326&VERSION=1.1.1	
      
		if (queryString==null)
        	return false; // pure POST without any query
        
 		queryString = queryString.toLowerCase();
 		
 		if  (   queryString.startsWith("validateschema") 
 				      || (queryString.indexOf("&validateschema")!=-1) ) 
 			return true;
 		return false;
 	}
 
 	
 }
