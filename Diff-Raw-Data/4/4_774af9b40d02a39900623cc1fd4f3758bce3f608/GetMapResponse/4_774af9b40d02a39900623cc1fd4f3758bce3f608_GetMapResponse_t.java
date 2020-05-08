 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.wms.responses;
 
 import com.vividsolutions.jts.geom.Envelope;
 import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
 import org.geotools.data.DefaultQuery;
 import org.geotools.data.FeatureSource;
 import org.geotools.data.Query;
 import org.geotools.factory.FactoryConfigurationError;
 import org.geotools.feature.IllegalAttributeException;
 import org.geotools.feature.SchemaException;
 import org.geotools.map.DefaultMapLayer;
 import org.geotools.map.MapLayer;
 import org.geotools.referencing.crs.DefaultGeographicCRS;
 import org.geotools.resources.coverage.CoverageUtilities;
import org.geotools.resources.coverage.FeatureUtilities;
 import org.geotools.styling.Style;
 import org.opengis.filter.Filter;
 import org.opengis.parameter.ParameterDescriptor;
 import org.opengis.parameter.ParameterValue;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 import org.opengis.referencing.operation.TransformException;
 import org.springframework.context.ApplicationContext;
 import org.vfny.geoserver.Request;
 import org.vfny.geoserver.Response;
 import org.vfny.geoserver.ServiceException;
 import org.vfny.geoserver.global.GeoServer;
 import org.vfny.geoserver.global.MapLayerInfo;
 import org.vfny.geoserver.global.Service;
 import org.vfny.geoserver.global.WMS;
 import org.vfny.geoserver.wms.GetMapProducer;
 import org.vfny.geoserver.wms.GetMapProducerFactorySpi;
 import org.vfny.geoserver.wms.RasterMapProducer;
 import org.vfny.geoserver.wms.WMSMapContext;
 import org.vfny.geoserver.wms.WmsException;
 import org.vfny.geoserver.wms.requests.GetMapRequest;
 import org.vfny.geoserver.wms.responses.map.metatile.MetatileMapProducer;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 
 /**
  * A GetMapResponse object is responsible of generating a map based on a GetMap
  * request. The way the map is generated is independent of this class, wich will
  * use a delegate object based on the output format requested
  *
  * @author Gabriel Roldan, Axios Engineering
  * @version $Id$
  */
 public class GetMapResponse implements Response {
     /** DOCUMENT ME! */
     private static final Logger LOGGER = Logger.getLogger(GetMapResponse.class.getPackage().getName());
 
     /**
      * The map producer that will be used for the production of a map in the
      * requested format.
      */
     private GetMapProducer delegate;
 
     /**
      * The map context
      */
     private WMSMapContext map;
 
     /**
      * WMS module
      */
     private WMS wms;
 
     /**
      * custom response headers
      */
     private HashMap responseHeaders;
     String headerContentDisposition;
     private ApplicationContext applicationContext;
 
     /**
      * Creates a new GetMapResponse object.
      *
      * @param applicationContext
      */
     public GetMapResponse(WMS wms, ApplicationContext applicationContext) {
         this.wms = wms;
         this.applicationContext = applicationContext;
         responseHeaders = new HashMap(10);
     }
 
     /**
      * Returns any extra headers that this service might want to set in the HTTP
      * response object.
      */
     public HashMap getResponseHeaders() {
         return responseHeaders;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param req
      *            DOCUMENT ME!
      *
      * @throws ServiceException
      *             DOCUMENT ME!
      * @throws WmsException
      *             DOCUMENT ME!
      */
     public void execute(Request req) throws ServiceException {
         GetMapRequest request = (GetMapRequest) req;
 
         final String outputFormat = request.getFormat();
 
         this.delegate = getDelegate(outputFormat, wms);
 
         // enable on the fly meta tiling if request looks like a tiled one
         if (MetatileMapProducer.isRequestTiled(request, delegate)) {
             LOGGER.finer("Tiled request detected, activating on the fly meta tiler");
             this.delegate = new MetatileMapProducer(request, (RasterMapProducer) delegate);
         }
 
         final MapLayerInfo[] layers = request.getLayers();
         final Style[] styles = (Style[]) request.getStyles().toArray(new Style[] {  });
         final Filter[] filters = ((request.getFilters() != null)
             ? (Filter[]) request.getFilters().toArray(new Filter[] {  }) : null);
 
         // JD:make instance variable in order to release resources later
         // final WMSMapContext map = new WMSMapContext();
         map = new WMSMapContext(request);
 
         // DJB: the WMS spec says that the request must not be 0 area
         // if it is, throw a service exception!
         final Envelope env = request.getBbox();
 
         if (env.isNull() || (env.getWidth() <= 0) || (env.getHeight() <= 0)) {
             throw new WmsException(new StringBuffer("The request bounding box has zero area: ").append(
                     env).toString());
         }
 
         // DJB DONE: replace by setAreaOfInterest(Envelope,
         // CoordinateReferenceSystem)
         // with the user supplied SRS parameter
 
         // if there's a crs in the request, use that. If not, assume its 4326
         final CoordinateReferenceSystem mapcrs = request.getCrs();
 
         // DJB: added this to be nicer about the "NONE" srs.
         if (mapcrs != null) {
             map.setAreaOfInterest(env, mapcrs);
         } else {
             map.setAreaOfInterest(env, DefaultGeographicCRS.WGS84);
         }
 
         map.setMapWidth(request.getWidth());
         map.setMapHeight(request.getHeight());
         map.setBgColor(request.getBgColor());
         map.setTransparent(request.isTransparent());
         map.setBuffer(request.getBuffer());
         map.setPalette(request.getPalette());
 
         // //
         //
         // Check to see if we really have something to display. Sometimes width
         // or height or both are non positivie or the requested area is null.
         //
         // ///
         if ((request.getWidth() <= 0) || (request.getHeight() <= 0)
                 || (map.getAreaOfInterest().getLength(0) <= 0)
                 || (map.getAreaOfInterest().getLength(1) <= 0)) {
             if (LOGGER.isLoggable(Level.FINE)) {
                 LOGGER.fine(
                     "We are not going to render anything because either the are is null ar the dimensions are not positive.");
             }
 
             return;
         }
 
         if (LOGGER.isLoggable(Level.FINE)) {
             LOGGER.fine("setting up map");
         }
 
         try { // mapcontext can leak memory -- we make sure we done (see
               // finally block)
 
             MapLayer layer;
 
             // track the external caching strategy for any map layers
             boolean cachingPossible = request.getHttpServletRequest().getMethod().equals("GET");
             String featureVersion = request.getFeatureVersion();
             int maxAge = Integer.MAX_VALUE;
 
             FeatureSource source;
             AbstractGridCoverage2DReader reader;
             Style style;
             Filter definitionFilter;
             Filter optionalFilter;
             DefaultQuery definitionQuery;
             int nma;
             final int length = layers.length;
 
             for (int i = 0; i < length; i++) {
                 style = styles[i];
 
                 try {
                     optionalFilter = filters[i];
                 } catch (Exception e) {
                     optionalFilter = null;
                 }
 
                 if (layers[i].getType() == MapLayerInfo.TYPE_VECTOR) {
                     if (cachingPossible) {
                         if (layers[i].getFeature().isCachingEnabled()) {
                             nma = Integer.parseInt(layers[i].getFeature().getCacheMaxAge());
 
                             // suppose the map contains multiple cachable
                             // layers...we can only cache the combined map for
                             // the
                             // time specified by the shortest-cached layer.
                             if (nma < maxAge) {
                                 maxAge = nma;
                             }
                         } else {
                             // if one layer isn't cachable, then we can't cache
                             // any of them. Disable caching.
                             cachingPossible = false;
                         }
                     }
 
                     // /////////////////////////////////////////////////////////
                     //
                     // Adding a feature layer
                     //
                     // /////////////////////////////////////////////////////////
                     try {
                         source = layers[i].getFeature().getFeatureSource(true);
 
                         // NOTE for the feature. Here there was some code that
                         // sounded like:
                         // * get the bounding box from feature source
                         // * eventually reproject it to the actual CRS used for
                         // map
                         // * if no intersection, don't bother adding the feature
                         // source to the map
                         // This is not an optimization, on the contrary,
                         // computing the bbox may be
                         // very expensive depending on the data size. Using
                         // sigma.openplans.org data
                         // and a tiled client like OpenLayers, it dragged the
                         // server to his knees
                         // and the client simply timed out
                     } catch (IOException exp) {
                         if (LOGGER.isLoggable(Level.SEVERE)) {
                             LOGGER.log(Level.SEVERE,
                                 new StringBuffer("Getting feature source: ").append(
                                     exp.getMessage()).toString(), exp);
                         }
 
                         throw new WmsException(null,
                             new StringBuffer("Internal error : ").append(exp.getMessage()).toString());
                     }
 
                     layer = new DefaultMapLayer(source, style);
                     layer.setTitle(layers[i].getName());
 
                     definitionFilter = layers[i].getFeature().getDefinitionQuery();
 
                     if (definitionFilter != null) {
                         definitionQuery = new DefaultQuery(source.getSchema().getTypeName(),
                                 definitionFilter);
                         definitionQuery.setVersion(featureVersion);
 
                         layer.setQuery(definitionQuery);
                     } else if (optionalFilter != null) {
                         definitionQuery = new DefaultQuery(source.getSchema().getTypeName(),
                                 optionalFilter);
                         definitionQuery.setVersion(featureVersion);
 
                         layer.setQuery(definitionQuery);
                     } else if (featureVersion != null) {
                         definitionQuery = new DefaultQuery(source.getSchema().getTypeName());
                         definitionQuery.setVersion(featureVersion);
 
                         layer.setQuery(definitionQuery);
                     }
 
                     map.addLayer(layer);
                 } else if (layers[i].getType() == MapLayerInfo.TYPE_RASTER) {
                     // /////////////////////////////////////////////////////////
                     //
                     // Adding a coverage layer
                     //
                     // /////////////////////////////////////////////////////////
                     reader = (AbstractGridCoverage2DReader) layers[i].getCoverage().getReader();
 
                     if (reader != null) {
                         // /////////////////////////////////////////////////////////
                         //
                         // Setting coverage reading params.
                         //
                         // /////////////////////////////////////////////////////////
 
                         //
                         //A workaround for subsetting coverages configured from a netcdf file
                         //based on time and elevation parameters
                         //Added by Alex Petkov
                         //
                         //JD: this is pretty out of place, but a lot of stuff 
                         // in this class ( kml ) seems to be out of place, we 
                         // need a better way for people to plug into request
                         // parsing
                         if (reader.getFormat().getName().equalsIgnoreCase("netcdf")) {
                             ParameterValue time = reader.getFormat().getReadParameters()
                                                         .parameter("TIME");
                             ParameterValue elevation = reader.getFormat().getReadParameters()
                                                              .parameter("ELEVATION");
 
                             if (request.getTime() != null) {
                                 time.setValue(request.getTime().intValue());
                             } else {
                                 ParameterDescriptor timeDescriptor = (ParameterDescriptor) time
                                     .getDescriptor();
                                 time.setValue(timeDescriptor.getDefaultValue());
                             }
 
                             if (request.getElevation() != null) {
                                 elevation.setValue(request.getElevation().intValue());
                             } else {
                                 ParameterDescriptor elevDescriptor = (ParameterDescriptor) elevation
                                     .getDescriptor();
                                 elevation.setValue(elevDescriptor.getDefaultValue());
                             }
                         }
 
                         try {
                            layer = new DefaultMapLayer(FeatureUtilities.wrapGridCoverageReader(reader),
                                     style);
                             layer.setTitle(layers[i].getName());
                             layer.setQuery(Query.ALL);
                             map.addLayer(layer);
                         } catch (IllegalArgumentException e) {
                             if (LOGGER.isLoggable(Level.SEVERE)) {
                                 LOGGER.log(Level.SEVERE,
                                     new StringBuffer("Wrapping GC in feature source: ").append(
                                         e.getLocalizedMessage()).toString(), e);
                             }
 
                             throw new WmsException(null,
                                 new StringBuffer(
                                     "Internal error : unable to get reader for this coverage layer ").append(
                                     layers[i].toString()).toString());
                         }
                     } else {
                         throw new WmsException(null,
                             new StringBuffer(
                                 "Internal error : unable to get reader for this coverage layer ").append(
                                 layers[i].toString()).toString());
                     }
                 }
             }
 
             // /////////////////////////////////////////////////////////
             //
             // Producing the map in the requested format.
             //
             // /////////////////////////////////////////////////////////
             this.delegate.produceMap(map);
 
             if (cachingPossible) {
                 responseHeaders.put("Cache-Control",  "max-age=" + maxAge + ", must-revalidate");
             }
 
             String contentDisposition = this.delegate.getContentDisposition();
 
             if (contentDisposition != null) {
                 this.headerContentDisposition = contentDisposition;
             }
         } catch (ClassCastException e) {
             if (LOGGER.isLoggable(Level.WARNING)) {
                 LOGGER.log(Level.SEVERE,
                     new StringBuffer("Getting feature source: ").append(e.getMessage()).toString(),
                     e);
             }
 
             throw new WmsException(e,
                 new StringBuffer("Internal error : ").append(e.getMessage()).toString(), "");
         } catch (TransformException e) {
             throw new WmsException(e,
                 new StringBuffer("Internal error : ").append(e.getMessage()).toString(), "");
         } catch (FactoryConfigurationError e) {
             throw new WmsException(e,
                 new StringBuffer("Internal error : ").append(e.getMessage()).toString(), "");
         } catch (SchemaException e) {
             throw new WmsException(e,
                 new StringBuffer("Internal error : ").append(e.getMessage()).toString(), "");
         } catch (IllegalAttributeException e) {
             throw new WmsException(e,
                 new StringBuffer("Internal error : ").append(e.getMessage()).toString(), "");
         } finally {
             // clean
             try {
                 // map.clearLayerList();
             } catch (Exception e) // we dont want to propogate a new error
              {
                 if (LOGGER.isLoggable(Level.SEVERE)) {
                     LOGGER.log(Level.SEVERE,
                         new StringBuffer("Getting feature source: ").append(e.getMessage())
                                                                     .toString(), e);
                 }
             }
         }
     }
 
     /**
      * asks the internal GetMapDelegate for the MIME type of the map that it
      * will generate or is ready to, and returns it
      *
      * @param gs
      *            DOCUMENT ME!
      *
      * @return the MIME type of the map generated or ready to generate
      *
      * @throws IllegalStateException
      *             if a GetMapDelegate is not setted yet
      */
     public String getContentType(GeoServer gs) throws IllegalStateException {
         if (this.delegate == null) {
             throw new IllegalStateException("No request has been processed");
         }
 
         return this.delegate.getContentType();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     public String getContentEncoding() {
         if (LOGGER.isLoggable(Level.FINER)) {
             LOGGER.finer("returning content encoding null");
         }
 
         return null;
     }
 
     /**
      * if a GetMapDelegate is set, calls it's abort method. Elsewere do nothing.
      *
      * @param gs
      *            DOCUMENT ME!
      */
     public void abort(Service gs) {
         if (this.delegate != null) {
             if (LOGGER.isLoggable(Level.FINE)) {
                 LOGGER.fine("asking delegate for aborting the process");
             }
 
             this.delegate.abort();
         }
     }
 
     /**
      * delegates the writing and encoding of the results of the request to the
      * <code>GetMapDelegate</code> wich is actually processing it, and has
      * been obtained when <code>execute(Request)</code> was called
      *
      * @param out
      *            the output to where the map must be written
      *
      * @throws ServiceException
      *             if the delegate throws a ServiceException inside its
      *             <code>writeTo(OuptutStream)</code>, mostly due to
      * @throws IOException
      *             if the delegate throws an IOException inside its
      *             <code>writeTo(OuptutStream)</code>, mostly due to
      * @throws IllegalStateException
      *             if this method is called before <code>execute(Request)</code>
      *             has succeed
      */
     public void writeTo(OutputStream out) throws ServiceException, IOException {
         try { // mapcontext can leak memory -- we make sure we done (see
               // finally block)
 
             if (this.delegate == null) {
                 throw new IllegalStateException(
                     "No GetMapDelegate is setted, make sure you have called execute and it has succeed");
             }
 
             if (LOGGER.isLoggable(Level.FINER)) {
                 LOGGER.finer(new StringBuffer("asking delegate for write to ").append(out).toString());
             }
 
             this.delegate.writeTo(out);
         } finally {
             try {
                 map.clearLayerList();
             } catch (Exception e) // we dont want to propogate a new error
              {
                 if (LOGGER.isLoggable(Level.SEVERE)) {
                     LOGGER.log(Level.SEVERE,
                         new StringBuffer("Getting feature source: ").append(e.getMessage())
                                                                     .toString(), e);
                 }
             }
         }
     }
 
     /**
      * Creates a GetMapDelegate specialized in generating the requested map
      * format
      *
      * @param outputFormat
      *            a request parameter object wich holds the processed request
      *            objects, such as layers, bbox, outpu format, etc.
      *
      * @return A specialization of <code>GetMapDelegate</code> wich can
      *         produce the requested output map format
      *
      * @throws WmsException
      *             if no specialization is configured for the output format
      *             specified in <code>request</code> or if it can't be
      *             instantiated
      */
     private GetMapProducer getDelegate(String outputFormat, WMS wms)
         throws WmsException {
         Map beans = applicationContext.getBeansOfType(GetMapProducerFactorySpi.class);
         Collection producers = beans.values();
         GetMapProducerFactorySpi factory;
 
         for (Iterator iter = producers.iterator(); iter.hasNext();) {
             factory = (GetMapProducerFactorySpi) iter.next();
 
             if (factory.canProduce(outputFormat)) {
                 return factory.createMapProducer(outputFormat, wms);
             }
         }
 
         WmsException e = new WmsException("There is no support for creating maps in "
                 + outputFormat + " format");
         e.setCode("InvalidFormat");
         throw e;
     }
 
     /**
      * Convenient mehtod to inspect the available
      * <code>GetMapProducerFactorySpi</code> and return the set of all the map
      * formats' MIME types that the producers can handle
      *
      * @return a Set&lt;String&gt; with the supported mime types.
      */
     public Set getMapFormats() {
         Set wmsGetMapFormats = loadImageFormats(applicationContext);
 
         return wmsGetMapFormats;
     }
 
     /**
      * Convenience method for processing the GetMapProducerFactorySpi extension
      * point and returning the set of available image formats.
      *
      * @param applicationContext
      *            The application context.
      *
      */
     public static Set loadImageFormats(ApplicationContext applicationContext) {
         Map beans = applicationContext.getBeansOfType(GetMapProducerFactorySpi.class);
         Collection producers = beans.values();
         Set formats = new HashSet();
         GetMapProducerFactorySpi producer;
 
         for (Iterator iter = producers.iterator(); iter.hasNext();) {
             producer = (GetMapProducerFactorySpi) iter.next();
             formats.addAll(producer.getSupportedFormats());
         }
 
         return formats;
     }
 
     public String getContentDisposition() {
         return headerContentDisposition;
     }
 }
