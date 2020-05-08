 package org.vfny.geoserver.wms.responses.map.kml;
 
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.geoserver.feature.PagingFeatureSource;
 import org.geoserver.ows.util.RequestUtils;
 import org.geoserver.ows.util.ResponseUtils;
 import org.geotools.feature.FeatureCollection;
 import org.geotools.map.MapLayer;
 import org.geotools.referencing.CRS;
 import org.geotools.styling.FeatureTypeStyle;
 import org.geotools.xml.transform.Translator;
 import org.geotools.geometry.jts.ReferencedEnvelope;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 import org.opengis.referencing.operation.TransformException;
 import org.vfny.geoserver.global.Data;
 import org.vfny.geoserver.global.NameSpaceInfo;
 import org.vfny.geoserver.wms.WMSMapContext;
 import org.vfny.geoserver.wms.requests.GetMapRequest;
 import org.xml.sax.ContentHandler;
 
 import sun.security.action.GetBooleanAction;
 
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.Envelope;
 import com.vividsolutions.jts.geom.Geometry;
 
 public class GeoSearchVectorTransformer extends KMLVectorTransformer {
 
     Logger LOGGER = org.geotools.util.logging.Logging
         .getLogger("org.geoserver.geosearch");
 
     Data catalog;
 
     public GeoSearchVectorTransformer(WMSMapContext mapContext,
             MapLayer mapLayer, Data catalog) {
         super(mapContext, mapLayer);
         this.catalog = catalog;
     }
 
     public Translator createTranslator(ContentHandler handler) {
         GeoSearchKMLTranslator translator = new GeoSearchKMLTranslator(handler);
         return translator;
     }
 
     protected class GeoSearchKMLTranslator extends KMLTranslator {
         private RegionatingStrategy myStrategy;
 
         public GeoSearchKMLTranslator(ContentHandler handler) {
             super(handler);
         }
 
         public void setRegionatingStrategy(RegionatingStrategy rs){
             myStrategy = rs;
         }
 
         public void encode(Object o) throws IllegalArgumentException {
             FeatureCollection<SimpleFeatureType, SimpleFeature> features = (FeatureCollection) o;
             SimpleFeatureType featureType = features.getSchema();
 
             if (isStandAlone()) {
                 start("kml");
             }
 
             // start the root document, name it the name of the layer
             start("Document", KMLUtils.attributes(
                     new String[] {"xmlns:atom", "http://purl.org/atom/ns#" }));
             element("name", mapLayer.getTitle());
 
             String linkbase = "";
             try {
                 linkbase = getFeatureTypeURL();
                 linkbase += ".kml";
             } catch (IOException ioe) {
                 throw new RuntimeException(ioe);
             }
 
             if (mapContext.getRequest().getMaxFeatures() != null){
                 int maxFeatures = mapContext.getRequest().getMaxFeatures();
                 int startIndex =
                     (mapContext.getRequest().getStartIndex() == null)
                     ? 0 
                     : mapContext.getRequest().getStartIndex().intValue();
                 int prevStart = startIndex - maxFeatures;
                 int nextStart = startIndex + maxFeatures;
 
                 // Previous page, if any
                 if (prevStart >= 0) {
                     String prevLink = linkbase + "?startindex=" 
                         + prevStart + "&maxfeatures=" + maxFeatures;
                     element("atom:link", null, KMLUtils.attributes(new String[] {
                                 "rel", "prev", "href", prevLink }));
                     encodeSequentialNetworkLink(linkbase, prevStart,
                             maxFeatures, "prev", "Previous page");
                 }
 
                 // Next page, if any
                 if (features.size() >= maxFeatures) {
                     String nextLink = linkbase + "?startindex=" + nextStart
                         + "&maxfeatures=" + maxFeatures;
                     element("atom:link", null, KMLUtils.attributes(new String[] {
                                 "rel", "next", "href", nextLink }));
                     encodeSequentialNetworkLink(linkbase, nextStart,
                             maxFeatures, "next", "Next page");
                 }
             }
 
 
             // get the styles for the layer
             FeatureTypeStyle[] featureTypeStyles = filterFeatureTypeStyles(
                     mapLayer.getStyle(), featureType);
 
             String stratname = (String)mapContext.getRequest().getFormatOptions().get("regionateBy");
             String attname = (String)mapContext.getRequest().getFormatOptions().get("regionateAttr");
             if (attname == null)
                 attname = "count";
 
             if (stratname == null) {
                 LOGGER.info("No regionating strategy specified, using default data-based strategy");
                 setRegionatingStrategy(new DataRegionatingStrategy(attname));
             } else if (stratname.equalsIgnoreCase("sld")) {
                 setRegionatingStrategy(new SLDRegionatingStrategy(featureTypeStyles));
             } else if (stratname.equalsIgnoreCase("data")) { 
                 setRegionatingStrategy(new DataRegionatingStrategy(attname));
             } else if (stratname.equalsIgnoreCase("geo")) { 
                 setRegionatingStrategy(new GeometryRegionatingStrategy());
             } else {
                 LOGGER.info("Bogus regionating strategy [" + stratname + "] specified, using default data-based strategy.");
                 setRegionatingStrategy(new DataRegionatingStrategy(attname));
             }
             
             myStrategy.preProcess(mapContext, mapLayer);
 
             // encode the schemas (kml 2.2)
             encodeSchemas(features);
 
             // encode the layers
             encode(features, featureTypeStyles);
 
             // encode the legend
             // encodeLegendScreenOverlay();
             end("Document");
 
             if (isStandAlone()) {
                 end("kml");
             }
         }
 
         /**
          * 
          * Encodes a networklink for previous or next document in a sequence
          * 
          * Note that in KML 2.2 atom:link is supported and may be better.
          *
          * @param linkbase the base fore creating URLs
          * @param prevStart previous start value
          * @param maxFeatures maximum number of features to return
          * @param id attribute to use for this NetworkLink
          * @param readableName goes into linkName
          */
         private void encodeSequentialNetworkLink(String linkbase, int prevStart,
                 int maxFeatures, String id, String readableName) {
             String link = linkbase + "?startindex=" + prevStart
                     + "&maxfeatures=" + maxFeatures;
             start("NetworkLink", KMLUtils.attributes(new String[] {"id", id}));
             element("linkName",readableName);
             start("Link");
             element("href",link);
             end("Link");
             end("NetworkLink");
         }
         
         /**
          * 
          * Encodes a networklink
          * 
          * Note that in KML 2.2 atom:link is supported and may be better.
          *
          * @param linkbase the base fore creating URLs
          * @param id attribute to use for this NetworkLink, may be null
          * @param readableName goes into linkName, may be null
          */
 //        private void encodeNetworkLink(String link, String id, String readableName) {
 //            if(id != null) {
 //                start("NetworkLink", KMLUtils.attributes(new String[] {"id", id}));
 //            } else {
 //                start("NetworkLink");
 //            }
 //            if(readableName != null) {
 //                element("linkName",readableName);
 //            }   
 //            start("Link");
 //            element("href",link);
 //            end("Link");
 //            end("NetworkLink");
 //        }
                 
         /**
          * Encodes a KML Placemark from a feature and optional name.
          */
         protected void encodePlacemark(SimpleFeature feature,
                 FeatureTypeStyle[] styles) {
             Geometry geometry = featureGeometry(feature);
             Coordinate centroid = geometryCentroid(geometry);
 
             start("Placemark", KMLUtils.attributes(new String[] { "id",
                         feature.getID() }));
 
             // encode name + description only if kmattr was specified
             if (mapContext.getRequest().getKMattr()) {
                 // name
                 try {
                     encodePlacemarkName(feature, styles);
                 } catch (Exception e) {
                     String msg = "Error occured processing 'title' template.";
                     LOGGER.log(Level.WARNING, msg, e);
                 }
 
                 // snippet (only used by OWS5 prototype at the moment)
                 try {
                     encodePlacemarkSnippet(feature, styles);
                 } catch (Exception e) {
                     String msg = "Error occured processing 'description' template.";
                     LOGGER.log(Level.WARNING, msg, e);
                 }
 
                 // description
                 try {
                     encodePlacemarkDescription(feature, styles);
                 } catch (Exception e) {
                     String msg = "Error occured processing 'description' template.";
                     LOGGER.log(Level.WARNING, msg, e);
                 }
             }
             String id[] = feature.getID().split("\\.");
 
             // get namespace prefix
 
             GetMapRequest request = mapContext.getRequest();
 
             String link = "";
 
             try {
                 link = getFeatureTypeURL();
             } catch (IOException ioe) {
                 /* what could *possibly* go wrong? */
                 throw new RuntimeException(ioe);
             }
 
             link = link + "/" + id[1] + ".kml";
 
             element("atom:link", null, KMLUtils.attributes(new String[] {
                         "rel", "self", "href", link }));
 
             // This crashes Google Earth - encodeNetworkLink(link, id[1], null);
 
             // look at
             encodePlacemarkLookAt(centroid);
 
             // time
             try {
                 encodePlacemarkTime(feature, styles);
             } catch (Exception e) {
                 String msg = "Error occured processing 'time' template: "
                     + e.getMessage();
                 LOGGER.log(Level.WARNING, msg);
                 LOGGER.log(Level.FINE, "", e);
             }
 
             // style reference
             element("styleUrl", "#GeoServerStyle" + feature.getID());
 
             // encode extended data (kml 2.2)
             encodeExtendedData(feature);
 
             // geometry
             encodePlacemarkGeometry(geometry, centroid, styles);
 
             end("Placemark");
         }
 
         private String getFeatureTypeURL() throws IOException {
             String nsUri = mapLayer.getFeatureSource().getSchema().getName().getNamespaceURI();
             NameSpaceInfo ns = catalog.getNameSpaceFromURI(nsUri);
             String featureTypeName = mapLayer.getFeatureSource().getSchema().getName().getLocalPart();
             GetMapRequest request = mapContext.getRequest();
             
             //TODO The old code, commented out below, results in
             // link = http://localhost:8080/geoserver/rest/geosearch/topp/states.kml?st
             // due to getBaseUrl()
             //
             //String link = RequestUtils.proxifiedBaseURL(request.getBaseUrl(),
             //        request.getGeoServer().getProxyBaseUrl());
             //
 
             // If you prefer pretty code, this is a good point to close your eyes:
             String baseUrl = request.getHttpServletRequest().getRequestURL().toString();
             int searchIdx = baseUrl.indexOf("rest/geosearch");
             if(searchIdx < 0) {
//              LOGGER.log(Level.WARNING, "Unable to find rest/geosearch in URL " + baseUrl);
             } else {
                 baseUrl = baseUrl.substring(0,searchIdx);
             }
             baseUrl = RequestUtils.proxifiedBaseURL(baseUrl,
                     request.getGeoServer().getProxyBaseUrl());
                     
             return baseUrl + "rest/geosearch/" + ns.getPrefix()
                     + "/" + featureTypeName;
         }
 
         protected void encode(SimpleFeature feature, FeatureTypeStyle[] styles) {
         	ReferencedEnvelope bounds = mapContext.getAreaOfInterest();
         	ReferencedEnvelope featureBounds = 
         		new ReferencedEnvelope(((Geometry)feature.getDefaultGeometry()).getEnvelopeInternal(),
         				feature.getType().getDefaultGeometry().getCRS());
         	CoordinateReferenceSystem nativeCRS = featureBounds.getCoordinateReferenceSystem();
     
         	try {
 				boolean reprojectBBox = (nativeCRS != null)
 						&& !CRS.equalsIgnoreMetadata(bounds
 								.getCoordinateReferenceSystem(), nativeCRS);
 				if (reprojectBBox) {
 					bounds = bounds.transform(nativeCRS, true);
 				}
 			} catch (Exception e) {
 			}
 
             if (bounds.intersects((Envelope)featureBounds)
                 && myStrategy.include(feature)) {
                 encodeStyle(feature, styles);
                 encodePlacemark(feature,styles);    
             }
         }
     }
 }
