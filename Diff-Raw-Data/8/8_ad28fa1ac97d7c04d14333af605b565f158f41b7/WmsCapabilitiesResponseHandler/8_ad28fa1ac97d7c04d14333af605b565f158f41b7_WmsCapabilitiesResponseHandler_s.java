 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.responses.wms;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import org.vfny.geoserver.global.Data;
 import org.vfny.geoserver.global.FeatureTypeInfo;
 import org.vfny.geoserver.global.GeoServer;
 import org.vfny.geoserver.global.Service;
 import org.vfny.geoserver.global.WMS;
 import org.vfny.geoserver.requests.Request;
 import org.vfny.geoserver.responses.CapabilitiesResponseHandler;
 import org.xml.sax.ContentHandler;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.AttributesImpl;
 
 
 /**
  * Handler methods to print out a valid wms capabilities document from the WMS
  * config.  Will be called by a transformer in Capabilities response, and
  * should turn itself into xml events that will then be consumed by a writer
  * and written to the output stream (ie sent to the client).
  *
  * @author dzwiers
  * @author Gabriel Roldn
  * @author Chris Holmes
 * @version $Id: WmsCapabilitiesResponseHandler.java,v 1.13 2004/02/19 18:38:34 dmzwiers Exp $
  */
 public class WmsCapabilitiesResponseHandler extends CapabilitiesResponseHandler {
     private static final String CAP_VERSION = WMS.getVersion();
     private GeoServer server = null;
     private String baseUrl = "";
     protected String BBOX_ELEM_NAME = "LatLonBoundingBox";
 
     /**
      * Creates a new WmsCapabilitiesResponseHandler object.
      *
      * @param handler DOCUMENT ME!
      * @param r DOCUMENT ME!
      */
     public WmsCapabilitiesResponseHandler(ContentHandler handler, Request r) {
         super(handler);
         server = r.getWMS().getGeoServer();
         baseUrl = r.getBaseUrl();
     }
 
     /**
      * Prints the dtd declaration and root WMT_MS_Capabilities declaration.
      *
      * @param config Must be an instance of WMS.  If not a class cast exception
      *        will be thrown.
      *
      * @throws SAXException For any problems creating the SAX.
      *
      * @task TODO: replace the digitalearth.gov dtd with our own locally
      *       referenced one.  To do this we will need to modify the build.xml
      *       file to copy it to the war.
      */
     protected void startDocument(Service config) throws SAXException {
         WMS WMS = (WMS) config;
 
         AttributesImpl atts = new AttributesImpl();
 
         startElement("!DOCTYPE WMT_MS_Capabilities SYSTEM \"http://www"
             + ".digitalearth.gov/wmt/xml/capabilities_1_1_1.dtd\"");
 
         atts.addAttribute("", "version", "version", "", CAP_VERSION);
 
         // atts.addAttribute("", "", "updateSequence", "updateSequence",
         //     WMS.getUpdateTime());
         startElement("WMT_MS_Capabilities", atts);
     }
 
     /**
      * Closes the WMT_MS_Capabilities element.
      *
      * @param config The wms config used to start the document.
      *
      * @throws SAXException For any problems.
      */
     public void endDocument(Service config) throws SAXException {
         unIndent();
         endElement("WMT_MS_Capabilities");
     }
 
     /**
      * Calls endElement for Service
      *
      * @param config The Wms config to turn into a capabilities document.
      *
      * @throws SAXException For any problems.
      */
     protected void endService(Service config) throws SAXException {
     	String tmp = "";
     	tmp = config.getGeoServer().getContactPerson();
     	if(tmp!=null && tmp!=""){
     		startElement("Contact");
         	handleSingleElem("ContactPerson",tmp);
         	
         	tmp = config.getGeoServer().getContactOrganization();
         	if(tmp!=null && tmp!="")
         		handleSingleElem("ContactOrganization",tmp);
         	
         	tmp = config.getGeoServer().getContactPosition();
         	if(tmp!=null && tmp!="")
         		handleSingleElem("ContactPosition",tmp);
         		
         	tmp = config.getGeoServer().getAddress();
         	if(tmp!=null && tmp!=""){
         		startElement("ContactAddress");
         		
         		tmp = config.getGeoServer().getAddressType();
         		if(tmp!=null && tmp!="")
         			handleSingleElem("AddressType",tmp);;
         		
         		tmp = config.getGeoServer().getAddress();
         		handleSingleElem("Address",tmp);;
         		
         		tmp = config.getGeoServer().getAddressCity();
         		if(tmp!=null && tmp!="")
         			handleSingleElem("City",tmp);;
         		
         		tmp = config.getGeoServer().getAddressState();
         		if(tmp!=null && tmp!="")
         			handleSingleElem("StateOrProvince",tmp);;
         		
         		tmp = config.getGeoServer().getAddressPostalCode();
         		if(tmp!=null && tmp!="")
         			handleSingleElem("PostCode",tmp);;
         		
         		tmp = config.getGeoServer().getAddressCountry();
         		if(tmp!=null && tmp!="")
         			handleSingleElem("Country",tmp);
         		
         		endElement("ContactAddress");
         	}
         	tmp = config.getGeoServer().getContactVoice();
         	if(tmp!=null && tmp!="")
         		handleSingleElem("ContactVoiceTelephone",tmp);
         	
         	tmp = config.getGeoServer().getContactFacsimile();
         	if(tmp!=null && tmp!="")
         		handleSingleElem("ContactFacsimileTelephone",tmp);
         	
         	tmp = config.getGeoServer().getContactEmail();
         	if(tmp!=null && tmp!="")
         		handleSingleElem("ContactElectronicMailAddress",tmp);
         	endElement("Contact");
     	}
     	super.endService(config);
     }
 
     /**
      * Handles the service section of the document.  Just calls super which
      * calls the appropriate sub methods.
      *
      * @param config The Wms config to turn into a capabilities document.
      *
      * @throws SAXException For any problems.
      */
     public void handleService(Service config) throws SAXException {
         super.handleService(config);
     }
 
     /**
      * Handles the capabilities section - request, exceptions, vendor, sld and
      * layers - of the caps document.
      *
      * @param Service The Wms config to turn into a capabilities document.
      *
      * @throws SAXException If anything goes wrong.
      */
     protected void handleCapabilities(Service Service)
         throws SAXException {
         WMS config = (WMS) Service;
 
         cReturn();
 
         startElement("Capability");
         indent();
         handleRequest(config);
         handleExceptions(config);
         handleVendorSpecifics(config);
         handleSLD(config);
         handleLayers(config);
         unIndent();
         endElement("Capability");
     }
 
     /**
      * Handles the layers portion of the document.  Prints a root layer that
      * all others descend from, as it appears that only one layer element is
      * allowed.  For now just calls handle config, which prints out the
      * server's title, abstract and keywords, which are all valid elements.
      *
      * @param config The Wms config to handle the layers of.
      *
      * @throws SAXException For any problems.
      *
      * @task REVISIT: print out a more wms appropriate root list.  Perhaps
      *       consider putting 4326 (latlon) for the SRS.  I'm not sure if
      *       child layers override or inherit.
      */
     protected void handleLayers(WMS config) throws SAXException {
         Data catalog = config.getData();
         Collection ftypes = catalog.getFeatureTypeInfos().values();
         FeatureTypeInfo layer;
         cReturn();
         startElement("Layer");
         handleService((Service) config);
 
         for (Iterator it = ftypes.iterator(); it.hasNext();) {
             layer = (FeatureTypeInfo) it.next();
 
             if (layer.isEnabled()) {
                 cReturn();
                 startElement("Layer");
                 indent();
                 handleFeatureType(layer);
                 unIndent();
                 endElement("Layer");
             }
         }
 
         endElement("Layer");
     }
 
     /**
      * Calls super.handleFeatureType to add common FeatureType content such as
      * Name, Title and LatLonBoundingBox, and then writes WMS specific layer
      * properties as Styles, Scale Hint, etc.
      *
      * @param ftype The featureType to write out.
      *
      * @throws SAXException For any problems.
      *
      * @task TODO: write wms specific elements.
      */
     protected void handleFeatureType(FeatureTypeInfo ftype)
         throws SAXException {
         super.handleFeatureType(ftype);
     }
 
     /**
      * Handles the sld styling support provided by the server.
      *
      * @param config The WMS config to get sld info from.
      *
      * @throws SAXException For any problems.
      */
     protected void handleSLD(WMS config) throws SAXException {
         AttributesImpl sldAtts = new AttributesImpl();
         String supportsSLD = config.supportsSLD() ? "1" : "0";
         String supportsUserLayer = config.supportsUserLayer() ? "1" : "0";
         String supportsUserStyle = config.supportsUserStyle() ? "1" : "0";
         String supportsRemoteWFS = config.supportsRemoteWFS() ? "1" : "0";
 
         sldAtts.addAttribute("", "SupportSLD", "SupportSLD", "", supportsSLD);
         sldAtts.addAttribute("", "UserLayer", "UserLayer", "", supportsUserLayer);
         sldAtts.addAttribute("", "UserStyle", "UserStyle", "", supportsUserStyle);
         sldAtts.addAttribute("", "RemoteWFS", "RemoteWFS", "", supportsRemoteWFS);
         cReturn();
         startElement("UserDefinedSymbolization", sldAtts);
         endElement("UserDefinedSymbolization");
     }
 
     /**
      * Handles the vendoer specific capabilities.  Right now there are none, so
      * we do nothing.
      *
      * @param config The global config that may contain vendor specifics.
      *
      * @throws SAXException For any problems.
      */
     protected void handleVendorSpecifics(WMS config) throws SAXException {
     }
 
     /**
      * Handles the request portion of the document, printing out the
      * capabilities and where to bind to them.
      *
      * @param config The global wms.
      *
      * @throws SAXException For any problems.
      */
     protected void handleRequest(WMS config) throws SAXException {
         startElement("Request");
         indent();
         handleCapability(config, "GetCapabilities");
         cReturn();
         handleCapability(config, "GetMap");
         unIndent();
         endElement("Request");
     }
 
     /**
      * Handles an individual capability, printing where it can be found and the
      * formats it supports.
      *
      * @param config The wms service config.
      * @param capabilityName The name of the capability to print out.
      *
      * @throws SAXException For any problems.
      */
     protected void handleCapability(WMS config, String capabilityName)
         throws SAXException {
         boolean isPost = false;
         startElement(capabilityName);
         indent();
 
         if (capabilityName.equals("GetCapabilities")) {
             //HACK - hardcode.  Also, do we actually return this mime-type?
             handleSingleElem("Format", "application/vnd.ogc.wms_xml");
             cReturn();
             isPost = true;
         }
 
         if (capabilityName.startsWith("GetMap")) {
             Iterator formats = GetMapResponse.getMapFormats().iterator();
 
             while (formats.hasNext()) {
                 handleSingleElem("Format", (String) formats.next());
                 cReturn();
             }
         }
 
         startElement("DCPType");
         indent();
         startElement("HTTP");
         indent();
         startElement("Get");
 
        String url = baseUrl + "?";
         handleOnlineResource(url);
         endElement("Get");
 
         if (isPost) {
             startElement("Post");
 
            String postUrl = baseUrl;
             handleOnlineResource(postUrl);
             endElement("Post");
         }
 
         unIndent();
         endElement("HTTP");
         unIndent();
         endElement("DCPType");
         unIndent();
         endElement(capabilityName);
     }
 
     /**
      * Handles the printing of the exceptions information, prints the formats
      * that GeoServer can return exceptions in.
      *
      * @param config The wms service global config.
      *
      * @throws SAXException For any problems.
      */
     protected void handleExceptions(WMS config) throws SAXException {
         cReturn();
         startElement("Exception");
         indent();
 
         String[] formats = config.getExceptionFormats();
 
         for (int i = 0; i < formats.length; i++) {
             handleSingleElem("Format", formats[i]);
 
             if (i < (formats.length - 1)) {
                 cReturn();
             }
         }
 
         unIndent();
         endElement("Exception");
         cReturn();
     }
 
     /**
      * Handles the contact information portion of the service section.
      *
      * @param config The global service information.
      *
      * @throws SAXException For any problems.
      */
     protected void handleContactInformation(Service config)
         throws SAXException {
         startElement("ContactInformation");
         indent();
         startElement("ContactPersonPrimary");
         indent();
         handleSingleElem("ContactPerson", server.getContactPerson());
         handleSingleElem("ContactOrganization", server.getContactOrganization());
         unIndent();
         endElement("ContactPersonPrimary");
 
         startElement("ContactAddress");
         indent();
         handleSingleElem("AddressType", server.getAddressType());
         handleSingleElem("Address", server.getAddress());
         handleSingleElem("City", server.getAddressCity());
         handleSingleElem("StateOrProvince", server.getAddressState());
         handleSingleElem("PostCode", server.getAddressPostalCode());
         handleSingleElem("Country", server.getAddressCountry());
         unIndent();
         endElement("ContactAddress");
 
         handleSingleElem("ContactVoiceTelephone", server.getContactVoice());
         handleSingleElem("ContactFacsimileTelephone",
             server.getContactFacsimile());
         handleSingleElem("ContactElectronicMailAddress",
             server.getContactEmail());
 
         unIndent();
         endElement("ContactInformation");
     }
 
     /**
      * Overrides BasicConfig.handleKeywords to write the keywords list in WMS
      * style
      *
      * @param kwords A list of keywords to print out.  If null or of size 0
      *        then nothing will be printed, since the keyword(s) element is
      *        not mandatory.
      *
      * @throws SAXException For any problems.
      */
     protected void handleKeywords(List kwords) throws SAXException {
         if ((kwords != null) && (kwords.size() > 0)) {
             startElement("KeywordList");
             indent();
 
             for (Iterator it = kwords.iterator(); it.hasNext();) {
                 startElement("Keyword");
                 characters(String.valueOf(it.next()));
                 endElement("Keyword");
 
                 if (it.hasNext()) {
                     cReturn();
                 }
             }
 
             unIndent();
 
             endElement("KeywordList");
         }
     }
 
     /**
      * Overrides CapabilitiesResponseHandler.handlerOnlineResource to write WMS
      * style service online resource
      *
      * @param config The WMS config to write out an online resource from.
      *
      * @throws SAXException For any problems.
      */
     protected void handleOnlineResource(Service config)
         throws SAXException {
         String url = config.getOnlineResource().toString();
         handleOnlineResource(url);
     }
 
     /**
      * Convenience method to print the appropriate xlink attributes for an
      * online resource.
      *
      * @param url The url to be linked to.
      *
      * @throws SAXException For any problems.
      */
     protected void handleOnlineResource(String url) throws SAXException {
         AttributesImpl attributes = new AttributesImpl();
         attributes.addAttribute("", "xmlns:xlink", "xmlns:xlink", "",
             "http://www.w3.org/1999/xlink");
         attributes.addAttribute("", "xlink:type", "xlink:type", "", "simple");
         attributes.addAttribute("", "xlink:href", "xlink:href", "", url);
 
         indent();
         startElement("OnlineResource", attributes);
         endElement("OnlineResource");
         unIndent();
     }
 
     /**
      * Gets the name of the WMS bbox element name.
      *
      * @return The string 'LatLonBoundingBox', as that's what wms uses.
      */
     protected String getBboxElementName() {
         return "LatLonBoundingBox";
     }
 }
