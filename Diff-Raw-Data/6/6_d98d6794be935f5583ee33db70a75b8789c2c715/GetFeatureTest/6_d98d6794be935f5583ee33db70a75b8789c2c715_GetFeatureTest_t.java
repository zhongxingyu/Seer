 package org.geoserver.wfs.v1_1;
 
 import javax.xml.namespace.QName;
 
 import junit.textui.TestRunner;
 
 import org.geoserver.data.test.MockData;
 import org.geoserver.wfs.WFSTestSupport;
 import org.geotools.gml3.bindings.GML;
 import org.geotools.referencing.CRS;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 public class GetFeatureTest extends WFSTestSupport {
 
     public void testGet() throws Exception {
         Document doc = getAsDOM("wfs?request=GetFeature&typename=cdf:Fifteen&version=1.1.0&service=wfs");
         assertEquals("wfs:FeatureCollection", doc.getDocumentElement()
                 .getNodeName());
 
         NodeList features = doc.getElementsByTagName("cdf:Fifteen");
         assertFalse(features.getLength() == 0);
 
         for (int i = 0; i < features.getLength(); i++) {
             Element feature = (Element) features.item(i);
             assertTrue(feature.hasAttribute("gml:id"));
         }
     }
 
     public void testPost() throws Exception {
 
         String xml = "<wfs:GetFeature " + "service=\"WFS\" "
                 + "version=\"1.1.0\" "
                 + "xmlns:cdf=\"http://www.opengis.net/cite/data\" "
                 + "xmlns:ogc=\"http://www.opengis.net/ogc\" "
                 + "xmlns:wfs=\"http://www.opengis.net/wfs\" " + "> "
                 + "<wfs:Query typeName=\"cdf:Other\"> "
                 + "<wfs:PropertyName>cdf:string2</wfs:PropertyName> "
                 + "</wfs:Query> " + "</wfs:GetFeature>";
 
         Document doc = postAsDOM("wfs", xml);
         assertEquals("wfs:FeatureCollection", doc.getDocumentElement()
                 .getNodeName());
 
         NodeList features = doc.getElementsByTagName("cdf:Other");
         assertFalse(features.getLength() == 0);
 
         for (int i = 0; i < features.getLength(); i++) {
             Element feature = (Element) features.item(i);
             assertTrue(feature.hasAttribute("gml:id"));
         }
 
     }
 
     public void testPostFormEncoded() throws Exception {
         String request = "wfs?service=WFS&version=1.1.0&request=GetFeature&typename=sf:PrimitiveGeoFeature"
                 + "&namespace=xmlns(sf=http://cite.opengeospatial.org/gmlsf)";
 
         Document doc = postAsDOM(request);
         assertEquals("wfs:FeatureCollection", doc.getDocumentElement()
                 .getNodeName());
 
         assertEquals(5, doc.getElementsByTagName("sf:PrimitiveGeoFeature")
                 .getLength());
     }
 
     public void testPostWithFilter() throws Exception {
         String xml = "<wfs:GetFeature " + "service=\"WFS\" "
                 + "version=\"1.1.0\" "
                 + "outputFormat=\"text/xml; subtype=gml/3.1.1\" "
                 + "xmlns:cdf=\"http://www.opengis.net/cite/data\" "
                 + "xmlns:wfs=\"http://www.opengis.net/wfs\" "
                 + "xmlns:ogc=\"http://www.opengis.net/ogc\" > "
                 + "<wfs:Query typeName=\"cdf:Other\"> " + "<ogc:Filter> "
                 + "<ogc:PropertyIsEqualTo> "
                 + "<ogc:PropertyName>cdf:integers</ogc:PropertyName> "
                 + "<ogc:Add> " + "<ogc:Literal>4</ogc:Literal> "
                 + "<ogc:Literal>3</ogc:Literal> " + "</ogc:Add> "
                 + "</ogc:PropertyIsEqualTo> " + "</ogc:Filter> "
                 + "</wfs:Query> " + "</wfs:GetFeature>";
 
         Document doc = postAsDOM("wfs", xml);
         assertEquals("wfs:FeatureCollection", doc.getDocumentElement()
                 .getNodeName());
 
         NodeList features = doc.getElementsByTagName("cdf:Other");
         assertFalse(features.getLength() == 0);
 
         for (int i = 0; i < features.getLength(); i++) {
             Element feature = (Element) features.item(i);
             assertTrue(feature.hasAttribute("gml:id"));
         }
     }
     
     public void testPostWithBboxFilter() throws Exception {
         String xml = "<wfs:GetFeature " + "service=\"WFS\" "
                 + "version=\"1.1.0\" "
                 + "outputFormat=\"text/xml; subtype=gml/3.1.1\" "
                 + "xmlns:gml=\"http://www.opengis.net/gml\" " 
                 + "xmlns:sf=\"http://www.opengis.net/cite/data\" "
                 + "xmlns:wfs=\"http://www.opengis.net/wfs\" "
                 + "xmlns:ogc=\"http://www.opengis.net/ogc\" > "
                 + "<wfs:Query typeName=\"sf:PrimitiveGeoFeature\">"
                 + "<ogc:Filter>"
                 + "<ogc:BBOX>"
                 + "   <ogc:PropertyName>pointProperty</ogc:PropertyName>"
                 + "   <gml:Envelope srsName=\"EPSG:4326\">"
                 + "      <gml:lowerCorner>57.0 -4.5</gml:lowerCorner>"
                 + "      <gml:upperCorner>62.0 1.0</gml:upperCorner>"
                 + "   </gml:Envelope>"
                 + "</ogc:BBOX>"
                 + "</ogc:Filter>"
                 + "</wfs:Query>"
                 + "</wfs:GetFeature>";
         
         Document doc = postAsDOM("wfs", xml);
         assertEquals("wfs:FeatureCollection", doc.getDocumentElement()
                 .getNodeName());
 
         NodeList features = doc.getElementsByTagName("sf:PrimitiveGeoFeature");
         assertEquals(1, features.getLength());
     }
     
     public void testPostWithFailingUrnBboxFilter() throws Exception {
         String xml = "<wfs:GetFeature " + "service=\"WFS\" "
             + "version=\"1.1.0\" "
             + "outputFormat=\"text/xml; subtype=gml/3.1.1\" "
             + "xmlns:gml=\"http://www.opengis.net/gml\" " 
             + "xmlns:sf=\"http://www.opengis.net/cite/data\" "
             + "xmlns:wfs=\"http://www.opengis.net/wfs\" "
             + "xmlns:ogc=\"http://www.opengis.net/ogc\" > "
             + "<wfs:Query typeName=\"sf:PrimitiveGeoFeature\">"
             + "<ogc:Filter>"
             + "<ogc:BBOX>"
             + "   <ogc:PropertyName>pointProperty</ogc:PropertyName>"
             + "   <gml:Envelope srsName=\"urn:x-ogc:def:crs:EPSG:6.11.2:4326\">"
             + "      <gml:lowerCorner>57.0 -4.5</gml:lowerCorner>"
             + "      <gml:upperCorner>62.0 1.0</gml:upperCorner>"
             + "   </gml:Envelope>"
             + "</ogc:BBOX>"
             + "</ogc:Filter>"
             + "</wfs:Query>"
             + "</wfs:GetFeature>";
 
         Document doc = postAsDOM("wfs", xml);
         assertEquals("wfs:FeatureCollection", doc.getDocumentElement().getNodeName());
         NodeList features = doc.getElementsByTagName("sf:PrimitiveGeoFeature");
         assertEquals(0, features.getLength());
     }
     
     public void testPostWithMatchingUrnBboxFilter() throws Exception {
         String xml = "<wfs:GetFeature " + "service=\"WFS\" "
             + "version=\"1.1.0\" "
             + "outputFormat=\"text/xml; subtype=gml/3.1.1\" "
             + "xmlns:gml=\"http://www.opengis.net/gml\" " 
             + "xmlns:sf=\"http://www.opengis.net/cite/data\" "
             + "xmlns:wfs=\"http://www.opengis.net/wfs\" "
             + "xmlns:ogc=\"http://www.opengis.net/ogc\" > "
             + "<wfs:Query typeName=\"sf:PrimitiveGeoFeature\">"
             + "<ogc:Filter>"
             + "<ogc:BBOX>"
             + "   <ogc:PropertyName>pointProperty</ogc:PropertyName>"
             + "   <gml:Envelope srsName=\"urn:x-ogc:def:crs:EPSG:6.11.2:4326\">"
             + "      <gml:lowerCorner>-4.5 57.0</gml:lowerCorner>"
             + "      <gml:upperCorner>1.0 62.0</gml:upperCorner>"
             + "   </gml:Envelope>"
             + "</ogc:BBOX>"
             + "</ogc:Filter>"
             + "</wfs:Query>"
             + "</wfs:GetFeature>";
 
         Document doc = postAsDOM("wfs", xml);
         assertEquals("wfs:FeatureCollection", doc.getDocumentElement().getNodeName());
         NodeList features = doc.getElementsByTagName("sf:PrimitiveGeoFeature");
         assertEquals(1, features.getLength());
     }
 
     public void testResultTypeHitsGet() throws Exception {
         Document doc = getAsDOM("wfs?request=GetFeature&typename=cdf:Fifteen&version=1.1.0&resultType=hits&service=wfs");
         assertEquals("wfs:FeatureCollection", doc.getDocumentElement()
                 .getNodeName());
 
         NodeList features = doc.getElementsByTagName("cdf:Fifteen");
         assertEquals(0, features.getLength());
 
         assertEquals("15", doc.getDocumentElement().getAttribute(
                 "numberOfFeatures"));
     }
 
     public void testResultTypeHitsPost() throws Exception {
         String xml = "<wfs:GetFeature " + "service=\"WFS\" "
                 + "version=\"1.1.0\" "
                 + "outputFormat=\"text/xml; subtype=gml/3.1.1\" "
                 + "xmlns:cdf=\"http://www.opengis.net/cite/data\" "
                 + "xmlns:wfs=\"http://www.opengis.net/wfs\" "
                 + "xmlns:ogc=\"http://www.opengis.net/ogc\" "
                 + "resultType=\"hits\"> "
                 + "<wfs:Query typeName=\"cdf:Seven\"/> " + "</wfs:GetFeature>";
 
         Document doc = postAsDOM("wfs", xml);
         assertEquals("wfs:FeatureCollection", doc.getDocumentElement()
                 .getNodeName());
 
         NodeList features = doc.getElementsByTagName("cdf:Fifteen");
         assertEquals(0, features.getLength());
 
         assertEquals("7", doc.getDocumentElement().getAttribute(
                 "numberOfFeatures"));
     }
 
     public void testWithSRS() throws Exception {
         String xml = "<wfs:GetFeature xmlns:wfs=\"http://www.opengis.net/wfs\" version=\"1.1.0\" service=\"WFS\">"
                 + "<wfs:Query xmlns:cdf=\"http://www.opengis.net/cite/data\" typeName=\"cdf:Other\" srsName=\"urn:x-ogc:def:crs:EPSG:6.11.2:4326\"/>"
                 + "</wfs:GetFeature>";
 
         Document dom = postAsDOM("wfs", xml);
         assertEquals(1, dom.getElementsByTagName("cdf:Other")
                 .getLength());
     }
 
     public void testWithSillyLiteral() throws Exception {
         String xml = "<wfs:GetFeature xmlns:cdf=\"http://www.opengis.net/cite/data\" xmlns:wfs=\"http://www.opengis.net/wfs\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:ogc=\"http://www.opengis.net/ogc\" version=\"1.1.0\" service=\"WFS\">"
                 + "<wfs:Query  typeName=\"cdf:Other\" srsName=\"urn:x-ogc:def:crs:EPSG:6.11.2:4326\">"
                 + "<ogc:Filter>"
                 + "  <ogc:PropertyIsEqualTo>"
                 + "   <ogc:PropertyName>description</ogc:PropertyName>"
                 + "   <ogc:Literal>"
                 + "       <wfs:Native vendorId=\"foo\" safeToIgnore=\"true\"/>"
                 + "   </ogc:Literal>"
                 + "   </ogc:PropertyIsEqualTo>"
                 + " </ogc:Filter>" + "</wfs:Query>" + "</wfs:GetFeature>";
 
         Document dom = postAsDOM("wfs", xml);
         assertEquals("wfs:FeatureCollection", dom.getDocumentElement()
                 .getNodeName());
         assertEquals(0, dom.getElementsByTagName("cdf:Other")
                 .getLength());
     }
 
     public void testWithGmlObjectId() throws Exception {
         String xml = "<wfs:GetFeature xmlns:cdf=\"http://www.opengis.net/cite/data\" xmlns:wfs=\"http://www.opengis.net/wfs\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:ogc=\"http://www.opengis.net/ogc\" version=\"1.1.0\" service=\"WFS\">"
                 + "<wfs:Query  typeName=\"cdf:Seven\" srsName=\"urn:x-ogc:def:crs:EPSG:6.11.2:4326\">"
                 + "</wfs:Query>" + "</wfs:GetFeature>";
 
         Document dom = postAsDOM("wfs", xml);
         assertEquals("wfs:FeatureCollection", dom.getDocumentElement()
                 .getNodeName());
         assertEquals(7, dom.getElementsByTagName("cdf:Seven")
                 .getLength());
 
         NodeList others = dom.getElementsByTagName("cdf:Seven");
         String id = ((Element) others.item(0)).getAttributeNS(GML.NAMESPACE,
                 "id");
         assertNotNull(id);
 
         xml = "<wfs:GetFeature xmlns:cdf=\"http://www.opengis.net/cite/data\" xmlns:wfs=\"http://www.opengis.net/wfs\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:ogc=\"http://www.opengis.net/ogc\" version=\"1.1.0\" service=\"WFS\">"
                 + "<wfs:Query  typeName=\"cdf:Seven\" srsName=\"urn:x-ogc:def:crs:EPSG:6.11.2:4326\">"
                 + "<ogc:Filter>"
                 + "<ogc:GmlObjectId gml:id=\""
                 + id
                 + "\"/>"
                 + "</ogc:Filter>" + "</wfs:Query>" + "</wfs:GetFeature>";
         dom = postAsDOM("wfs", xml);
 
         assertEquals(1, dom.getElementsByTagName("cdf:Seven")
                 .getLength());
     }
     
     public void testPostWithBoundsEnabled() throws Exception {
         // enable feature bounds computation
         boolean oldFeatureBounding = getWFS().isFeatureBounding();
         getWFS().setFeatureBounding(true);
         try {
             String xml = "<wfs:GetFeature " + "service=\"WFS\" "
                     + "version=\"1.1.0\" "
                     + "xmlns:cdf=\"http://www.opengis.net/cite/data\" "
                     + "xmlns:ogc=\"http://www.opengis.net/ogc\" "
                     + "xmlns:wfs=\"http://www.opengis.net/wfs\" " + "> "
                     + "<wfs:Query typeName=\"cdf:Other\"> "
                     + "<wfs:PropertyName>cdf:string2</wfs:PropertyName> "
                     + "</wfs:Query> " + "</wfs:GetFeature>";
     
             Document doc = postAsDOM("wfs", xml);
             assertEquals("wfs:FeatureCollection", doc.getDocumentElement()
                     .getNodeName());
     
             NodeList features = doc.getElementsByTagName("cdf:Other");
             assertFalse(features.getLength() == 0);
     
             for (int i = 0; i < features.getLength(); i++) {
                 Element feature = (Element) features.item(i);
                 assertTrue(feature.hasAttribute("gml:id"));
                 NodeList boundList = feature.getElementsByTagName("gml:boundedBy");
                 assertEquals(1, boundList.getLength());
                 Element boundedBy = (Element) boundList.item(0);
                 NodeList boxList = boundedBy.getElementsByTagName("gml:Envelope");
                 assertEquals(1, boxList.getLength());
                 Element box = (Element) boxList.item(0);
                 assertTrue(box.hasAttribute("srsName"));
             }
         } finally {
             getWFS().setFeatureBounding(oldFeatureBounding);
         }
     }
 
     public void testAfterFeatureTypeAdded() throws Exception {
         Document dom = getAsDOM( "wfs?request=getfeature&service=wfs&version=1.1.0&typename=sf:new");
         assertEquals( "ExceptionReport", dom.getDocumentElement().getLocalName() );
         
         //add a feature type
        dataDirectory.addFeatureType( 
            new QName( MockData.SF_URI, "new", MockData.SF_PREFIX ), 
            getClass().getResourceAsStream("new.properties")
        );
        applicationContext.refresh();
         
         dom = getAsDOM( "wfs?request=getfeature&service=wfs&version=1.1.0&typename=sf:new");
         assertEquals( "FeatureCollection", dom.getDocumentElement().getLocalName() );
     }
     
     public void testWithGMLProperties() throws Exception {
         
         dataDirectory.addFeatureType( 
            new QName( MockData.SF_URI, "WithGMLProperties", MockData.SF_PREFIX ), 
            getClass().getResourceAsStream("WithGMLProperties.properties") 
         );
         applicationContext.refresh();
         
         Document dom = getAsDOM( "wfs?request=getfeature&service=wfs&version=1.1.0&typename=sf:WithGMLProperties");
         
         assertEquals( "FeatureCollection", dom.getDocumentElement().getLocalName() );
         
         NodeList features = dom.getElementsByTagName("sf:WithGMLProperties");
         assertEquals( 1, features.getLength() );
         
         for ( int i = 0; i < features.getLength(); i++ ) {
             Element feature = (Element) features.item( i );
             assertEquals( "one", getFirstElementByTagName( feature, "gml:name").getFirstChild().getNodeValue() );
             assertEquals( "1", getFirstElementByTagName( feature, "sf:foo").getFirstChild().getNodeValue());
             
             Element location = getFirstElementByTagName( feature, "gml:location" );
             assertNotNull( getFirstElementByTagName( location, "gml:Point" ) );
         }
     }
     
     public static void main(String[] args) {
         TestRunner runner = new TestRunner();
         runner.run(GetFeatureTest.class);
     }
 }
