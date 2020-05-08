 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.responses.wfs;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import org.vfny.geoserver.global.FeatureTypeInfo;
 import org.vfny.geoserver.global.NameSpaceInfo;
 import org.vfny.geoserver.global.Service;
 import org.vfny.geoserver.global.WFS;
 import org.vfny.geoserver.global.dto.WFSDTO;
 import org.vfny.geoserver.requests.Request;
 import org.vfny.geoserver.responses.CapabilitiesResponseHandler;
 import org.xml.sax.ContentHandler;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.AttributesImpl;
 
 
 /**
  * Handles a Wfs specific sections of the capabilities response.
  *
  * @author Gabriel Roldn
  * @author Chris Holmes
 * @version $Id: WfsCapabilitiesResponseHandler.java,v 1.17 2004/02/20 19:17:49 cholmesny Exp $
  */
 public class WfsCapabilitiesResponseHandler extends CapabilitiesResponseHandler {
     protected static final String WFS_URI = "http://www.opengis.net/wfs";
     protected static final String CUR_VERSION = "1.0.0";
     protected static final String XSI_PREFIX = "xsi";
     protected static final String XSI_URI = "http://www.w3.org/2001/XMLSchema-instance";
     protected Request request;
 
     /**
      * Creates a new WfsCapabilitiesResponseHandler object.
      *
      * @param handler DOCUMENT ME!
      * @param request DOCUMENT ME!
      */
     public WfsCapabilitiesResponseHandler(ContentHandler handler,
         Request request) {
         super(handler);
         this.request = request;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param config DOCUMENT ME!
      *
      * @throws SAXException DOCUMENT ME!
      */
     protected void startDocument(Service config) throws SAXException {
         AttributesImpl attributes = new AttributesImpl();
         attributes.addAttribute("", "version", "version", "", CUR_VERSION);
         attributes.addAttribute("", "xmlns", "xmlns", "", WFS_URI);
 
         NameSpaceInfo[] namespaces = request.getWFS().getData()
                                             .getNameSpaces();
 
         for (int i = 0; i < namespaces.length; i++) {
             String prefixDef = "xmlns:" + namespaces[i].getPrefix();
             String uri = namespaces[i].getUri();
             attributes.addAttribute("", prefixDef, prefixDef, "", uri);
         }
 
         attributes.addAttribute("", "xmlns:ogc", "xmlns:ogc", "",
             "http://www.opengis.net/ogc");
 
         String prefixDef = "xmlns:" + XSI_PREFIX;
         attributes.addAttribute("", prefixDef, prefixDef, "", XSI_URI);
 
         String locationAtt = XSI_PREFIX + ":schemaLocation";
         String locationDef = WFS_URI + " " + request.getBaseUrl()
             + "wfs/1.0.0/" + "WFS-capabilities.xsd";
         attributes.addAttribute("", locationAtt, locationAtt, "", locationDef);
         startElement("WFS_Capabilities", attributes);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param config DOCUMENT ME!
      *
      * @throws SAXException DOCUMENT ME!
      */
     public void endDocument(Service config) throws SAXException {
         handleFilters();
         endElement("WFS_Capabilities");
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param serviceConfig DOCUMENT ME!
      *
      * @throws SAXException DOCUMENT ME!
      */
     protected void handleCapabilities(Service serviceConfig)
         throws SAXException {
         WFS config = (WFS) serviceConfig;
 
         cReturn();
 
         startElement("Capability");
 
         indent();
         startElement("Request");
 
         handleCapability(config, "GetCapabilities");
         handleCapability(config, "DescribeFeatureType");
         handleCapability(config, "GetFeature");
         handleCapability(config, "Transaction");
         handleCapability(config, "LockFeature");
         handleCapability(config, "GetFeatureWithLock");
 
         endElement("Request");
         unIndent();
         endElement("Capability");
 
         handleFeatureTypes(config);
     }
 
     private void handleCapability(WFS config, String capabilityName)
         throws SAXException {
         AttributesImpl attributes = new AttributesImpl();
         if("Transaction".equals(capabilityName) &&
         	(config.getServiceLevel() | WFSDTO.TRANSACTIONAL) == 0){
         	return;
         }
         indent();
         startElement(capabilityName);
         indent();
 
         if (capabilityName.equals("DescribeFeatureType")) {
             String schemaLanguage = "SchemaDescriptionLanguage";
             startElement(schemaLanguage);
             handleSingleElem("XMLSCHEMA", "");
             endElement(schemaLanguage);
             cReturn();
         }
 
         if (capabilityName.startsWith("GetFeature")) {
             String resultFormat = "ResultFormat";
             startElement(resultFormat);
             handleSingleElem("GML2", "");
             endElement(resultFormat);
             cReturn();
         }
 
         startElement("DCPType");
         startElement("HTTP");
 
         String url = "";
        String baseUrl = request.getBaseUrl() + "wfs/";
         if(request.isCGIRequest()){
         	url = request.getBaseUrl() + "wfs?";
         }else{
         	url = request.getBaseUrl() + "wfs/" + capabilityName + "?";
         }
         attributes.addAttribute("", "onlineResource", "onlineResource", "", url);
 
         startElement("Get", attributes);
         endElement("Get");
         endElement("HTTP");
         endElement("DCPType");
 
         cReturn();
 
         //if(!request.isCGIRequest()){ Even if it's a cgi request we can 
         //still say that we can do post, just not in the way they like...
         	attributes = new AttributesImpl();
         	url = baseUrl + capabilityName;
         	attributes.addAttribute("", "onlineResource", "onlineResource", "", url);
         	startElement("DCPType");
         	startElement("HTTP");
         	startElement("Post", attributes);
         	endElement("Post");
         	endElement("HTTP");
         	endElement("DCPType");
         //}
         unIndent();
         endElement(capabilityName);
         unIndent();
     }
 
     private void handleFeatureTypes(Service serviceConfig)
         throws SAXException {
         WFS config = (WFS) serviceConfig;
          
         if( !config.isEnabled() ){
             // should we return anything if we are disabled?
         }
         startElement("FeatureTypeList");
 
         indent();
         startElement("Operations");
         indent();
         if(( config.getServiceLevel() | WFSDTO.SERVICE_BASIC ) != 0 ){        
           startElement("Query");
           endElement("Query");
         }
         if(( config.getServiceLevel() | WFSDTO.SERVICE_INSERT ) != 0 ){
           startElement("Insert");
           endElement("Insert");
         }
         if(( config.getServiceLevel() | WFSDTO.SERVICE_UPDATE ) != 0 ){        
           startElement("Update");
           endElement("Update");
         }
         if(( config.getServiceLevel() | WFSDTO.SERVICE_DELETE ) != 0 ){        
           startElement("Delete");
           endElement("Delete");
         }
         if(( config.getServiceLevel() | WFSDTO.SERVICE_LOCKING ) != 0 ){        
           startElement("Lock");
           endElement("Lock");
         }
         unIndent();
         endElement("Operations");
 
         Collection featureTypes = request.getWFS().getData()
                                          .getFeatureTypeInfos().values();
         FeatureTypeInfo ftype;
 
         for (Iterator it = featureTypes.iterator(); it.hasNext();) {
             ftype = (FeatureTypeInfo) it.next();
 
             //can't handle ones that aren't enabled.
             //and they shouldn't be handled, as they won't function.
             if (ftype.isEnabled()) {
                 startElement("FeatureType");
                 handleFeatureType(ftype);
                 unIndent();
                 endElement("FeatureType");
                 cReturn();
             }
         }
 
         endElement("FeatureTypeList");
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param kwords DOCUMENT ME!
      *
      * @throws SAXException DOCUMENT ME!
      */
     protected void handleKeywords(List kwords) throws SAXException {
         startElement("Keywords");
 
         if (kwords != null) {
             for (Iterator it = kwords.iterator(); it.hasNext();) {
                 characters(it.next().toString());
 
                 if (it.hasNext()) {
                     characters(", ");
                 }
             }
         }
 
         endElement("Keywords");
     }
 
     protected void handleFilters() throws SAXException {
         String ogc = "ogc:";
 
         //REVISIT: for now I"m just prepending ogc onto the name element.
         //Is the proper way to only do that for the qname?  I guess it
         //would only really matter if we're going to be producing capabilities
         //documents that aren't qualified, and I don't see any reason to
         //do that.
         indent();
         startElement(ogc + "Filter_Capabilities");
         indent();
         startElement(ogc + "Spatial_Capabilities");
         indent();
         startElement(ogc + "Spatial_Operators");
         cReturn();
         handleSingleElem(ogc + "Disjoint");
         cReturn();
         handleSingleElem(ogc + "Equals");
         cReturn();
         handleSingleElem(ogc + "DWithin");
         cReturn();
         handleSingleElem(ogc + "Beyond");
         cReturn();
         handleSingleElem(ogc + "Intersect");
         cReturn();
         handleSingleElem(ogc + "Touches");
         cReturn();
         handleSingleElem(ogc + "Crosses");
         cReturn();
         handleSingleElem(ogc + "Within");
         cReturn();
         handleSingleElem(ogc + "Contains");
         cReturn();
         handleSingleElem(ogc + "Overlaps");
         cReturn();
         handleSingleElem(ogc + "BBOX");
         unIndent();
         endElement(ogc + "Spatial_Operators");
         unIndent();
         endElement(ogc + "Spatial_Capabilities");
         cReturn();
         startElement(ogc + "Scalar_Capabilities");
         indent();
         handleSingleElem(ogc + "Logical_Operators");
         indent();
         startElement(ogc + "Comparison_Operators");
         indent();
         handleSingleElem(ogc + "Simple_Comparisons");
         cReturn();
         handleSingleElem(ogc + "Between");
         cReturn();
         handleSingleElem(ogc + "Like");
         cReturn();
         handleSingleElem(ogc + "NullCheck");
         unIndent();
         endElement(ogc + "Comparison_Operators");
         cReturn();
         startElement(ogc + "Arithmetic_Operators");
         indent();
         handleSingleElem(ogc + "Simple_Arithmetic");
         unIndent();
         endElement(ogc + "Arithmetic_Operators");
         unIndent();
         endElement(ogc + "Scalar_Capabilities");
         unIndent();
         endElement(ogc + "Filter_Capabilities");
         unIndent();
     }
 
     protected String getBboxElementName() {
         return "LatLongBoundingBox";
     }
 }
