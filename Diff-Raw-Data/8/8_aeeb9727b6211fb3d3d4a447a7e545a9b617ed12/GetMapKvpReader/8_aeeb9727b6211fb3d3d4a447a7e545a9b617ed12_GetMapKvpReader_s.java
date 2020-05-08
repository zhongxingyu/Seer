 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.wms.requests;
  
 import java.awt.Color;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringBufferInputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.geotools.feature.FeatureType;
 import org.geotools.styling.FeatureTypeConstraint;
 import org.geotools.styling.NamedLayer;
 import org.geotools.styling.NamedStyle;
 import org.geotools.styling.SLDParser;
 import org.geotools.styling.Style;
 import org.geotools.styling.StyleAttributeExtractor;
 import org.geotools.styling.StyleFactory;
 import org.geotools.styling.StyledLayer;
 import org.geotools.styling.StyledLayerDescriptor;
 import org.geotools.styling.UserLayer;
 import org.vfny.geoserver.Request;
 import org.vfny.geoserver.ServiceException;
 import org.vfny.geoserver.global.Data;
 import org.vfny.geoserver.global.FeatureTypeInfo;
 import org.vfny.geoserver.global.TemporaryFeatureTypeInfo;
 import org.vfny.geoserver.util.SLDValidator;
 import org.vfny.geoserver.wms.WmsException;
 
 import com.vividsolutions.jts.geom.Envelope;
 
 
 /**
  * Builds a GetMapRequest object given by a set of CGI parameters supplied in
  * the constructor.
  * 
  * <p>
  * Mandatory parameters:
  * 
  * <ul>
  * <li>
  * LAYERS layer names, as exposed by the capabilities document, to compose a
  * map with, in the order they may appear, being the first layer the one at
  * the bottom of the layer stack and the last one the one at the top.
  * </li>
  * <li>
  * STYLES list of named styles known by this server and applicable to the
  * requested layers. It can be empty or contain exactly as many style names as
  * layers was requested, in which case empty strings could be used to denote
  * that the default layer style should be used. (exaple:
  * <code>LAYERS=buildings,roads,railroads&STYLES=,centerline,</code>. This
  * example says create a map with roads layer using its default style, roads
  * with "centerline" style, and railroads with its default style.
  * </li>
  * <li>
  * BBOX Area of interest for which to contruct the map image, in the Coordinate
  * Reference System given by the SRS parameter.
  * </li>
  * <li>
  * FORMAT MIME type of the resulting map, must be one of the advertised in the
  * capabilities document.
  * </li>
  * <li>
  * WIDTH desired map witdth in output units (pixels). UNITS support should be
  * added to the spec, and UNITS and DPI parameters added.
  * </li>
  * <li>
  * HEIGHT desired map height in output units (pixels). UNITS support should be
  * added to the spec, and UNITS and DPI parameters added.
  * </li>
  * </ul>
  * </p>
  * 
  * <p>
  * Optional parameters:
  * 
  * <ul>
  * <li>
  * SRS
  * </li>
  * <li>
  * TRANSPARENT boolean indicatin wether to create a map with transparent
  * background or not (if transparency is supported by the requested output
  * format).
  * </li>
  * <li>
  * EXCEPTIONS MIME type of the exception report.
  * </li>
  * <li>
  * BGCOLOR map background color, in <code>0xRRGGBB</code> format.
  * </li>
  * <li>
  * SLD client supplies a URL for a remote SLD document through this parameter.
  * This parameter takes precedence over STYLES. If present, replaces the
  * LAYERS and STYLES parameters, since they're defined in the remote document
  * itself. The document send by this way will be used in "literal" or
  * "library" mode, see explanation bellow.
  * </li>
  * <li>
  * SLD_BODY client spplies the SLD document itself through this parameter,
  * properly encoded  for an HTTP query string. This parameter takes
  * precendence over STYLES and SLD. If present, replaces the LAYERS and STYLES
  * parameters, since they're defined in the inline document itself. The
  * document send by this way will be used in "literal" or "library" mode, see
  * explanation bellow.
  * </li>
  * </ul>
  * </p>
  * 
  * <p>
  * As defined by the Styled Layer Descriptor specification, version 1.0.0, the
  * SLD document supplied by the SLD or SLD_BODY parameter can be used in
  * "literal" or "library" mode, depending on whether the
  * <strong>LAYERS=</strong> parameter is present.
  * </p>
  * 
  * <p>
  * Here is the explanation from the spec, section 6.4, page 10: "the SLD can
  * also be used in one of two different modes depending on whether the LAYERS
  * parameter is present in the request. If it is not present, then all layers
  * identified in the SLD document are rendered with all defined styles, which
  * is equivalent to the XML-POST method of usage. If the LAYERS parameter is
  * present, then only the layers identified by that parameter are rendered and
  * the SLD is used as a style library . "
  * </p>
  *
  * @author Gabriel Roldan, Axios Engineering
  * @version $Id: GetMapKvpReader.java,v 1.12 2004/09/16 22:20:54 cholmesny Exp $
  *
  * @task TODO: parse and respect SRS parameter (needs reprojection support)
  */
 public class GetMapKvpReader extends WmsKvpRequestReader {
     /** DOCUMENT ME! */
     private static final Logger LOGGER = Logger.getLogger(
             "org.vfny.geoserver.requests.readers.wms");
 
     /** Used to parse SLD documents from SLD and SLD_BODY parameters */
     private static final StyleFactory styleFactory = StyleFactory
         .createStyleFactory();
 
     /**
      * Indicates wether STYLES parameter must be parsed. Defaults to
      * <code>true</code>, but can be set to false, for example, when parsing a
      * GetFeatureInfo request, which shares most of the getmap parameter but
      * not STYLES.
      *
      * @task TODO: refactor this so it dont stay _so_ ugly
      */
     private boolean stylesRequired = true;
 
     /**
      * Creates a new GetMapKvpReader object.
      *
      * @param kvpPairs DOCUMENT ME!
      */
     public GetMapKvpReader(Map kvpPairs) {
         super(kvpPairs);
     }
 
     /**
      * Sets wether the STYLES parameter must be parsed
      *
      * @param parseStyles
      */
     public void setStylesRequired(boolean parseStyles) {
         this.stylesRequired = parseStyles;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     public boolean isStylesRquired() {
         return this.stylesRequired;
     }
 
     /**
      * Produces a <code>GetMapRequest</code> instance by parsing the GetMap
      * mandatory, optional and custom parameters.
      *
      * @param httpRequest the servlet request who's application object holds
      *        the server configuration
      *
      * @return a <code>GetMapRequest</code> completely setted up upon the
      *         parameters passed to this reader
      *
      * @throws ServiceException DOCUMENT ME!
      */
     public Request getRequest(HttpServletRequest httpRequest)
         throws ServiceException {
         GetMapRequest request = new GetMapRequest();
         request.setHttpServletRequest(httpRequest);
 
         String version = getRequestVersion();
         request.setVersion(version);
 
         parseMandatoryParameters(request,true);
         parseOptionalParameters(request);
 
         return request;
     }
 
     /**
      * Parses the optional parameters:
      * 
      * <ul>
      * <li>
      * SRS
      * </li>
      * <li>
      * TRANSPARENT
      * </li>
      * <li>
      * EXCEPTIONS
      * </li>
      * <li>
      * BGCOLOR
      * </li>
      * </ul>
      * 
      *
      * @param request DOCUMENT ME!
      *
      * @throws WmsException DOCUMENT ME!
      *
      * @task TODO: implement parsing of transparent, exceptions and bgcolor
      */
     public void parseOptionalParameters(GetMapRequest request)
         throws WmsException {
         String crs = getValue("SRS");
 
         if (crs != null) {
             request.setCrs(crs);
         }
 
         String transparentValue = getValue("TRANSPARENT");
         boolean transparent = (transparentValue == null) ? false
                                                          : Boolean.valueOf(transparentValue)
                                                                   .booleanValue();
         request.setTransparent(transparent);
 
         String bgcolor = getValue("BGCOLOR");
 
         if (bgcolor != null) {
             try {
                 request.setBgColor(Color.decode(bgcolor));
             } catch (NumberFormatException nfe) {
                 throw new WmsException("BGCOLOR " + bgcolor
                     + " incorrectly specified (0xRRGGBB format expected)");
             }
         }
     }
 
     /**
      * Parses the mandatory GetMap request parameters:
      * 
      * <p>
      * Mandatory parameters:
      * 
      * <ul>
      * <li>
      * LAYERS
      * </li>
      * <li>
      * STYLES ommited if SLD or SLD_BODY parameters are supplied
      * </li>
      * <li>
      * BBOX
      * </li>
      * <li>
      * FORMAT
      * </li>
      * <li>
      * WIDTH
      * </li>
      * <li>
      * HEIGHT
      * </li>
      * </ul>
      * </p>
      *
      * @param request DOCUMENT ME!
      * @parseStylesLayers true = normal operation, false = dont parse the styles and layers (used by the SLD GET/POST)
      *
      * @throws WmsException DOCUMENT ME!
      */
     public void parseMandatoryParameters(GetMapRequest request,boolean parseStylesLayers)
         throws WmsException {
         try {
             int width = Integer.parseInt(getValue("WIDTH"));
             int height = Integer.parseInt(getValue("HEIGHT"));
             request.setWidth(width);
             request.setHeight(height);
         } catch (NumberFormatException ex) {
             throw new WmsException("WIDTH and HEIGHT incorrectly specified");
         }
 
         String format = getValue("FORMAT");
 
         if (format == null) {
             throw new WmsException("parameter FORMAT is required");
         }
 
         request.setFormat(format);
 
         Envelope bbox = parseBbox();
         request.setBbox(bbox);
 
         //let styles and layers parsing for the end to give more trivial parameters 
         //a chance to fail before incurring in retrieving the SLD or SLD_BODY
         if (parseStylesLayers)
         	parseLayersAndStyles(request);
     }
 
     /**
      * creates a list of requested attributes, wich must be a valid attribute
      * name or one of the following special attributes:
      * 
      * <ul>
      * <li>
      * <b>#FID</b>: a map producer capable of handling attributes (such as
      * SVGMapResponse), will write the feature id of each feature
      * </li>
      * <li>
      * <b>#BOUNDS</b>: a map producer capable of handling attributes (such as
      * SVGMapResponse), will write the bounding box of each feature
      * </li>
      * </ul>
      * 
      *
      * @param layers info about the requested map layers
      *
      * @return an empty list if no attributes was requested, or a
      *         <code>List&lt;List&lt;String&gt;&gt;</code> with an entry for
      *         each requested layer, where each of them consists of a List of
      *         the attribute names requested
      *
      * @throws WmsException if: <ul><li>the number of attribute sets requested
      *         is not equal to the number of layers requested.</li> <li>an
      *         illegal attribute name was requested</li> <li>an IOException
      *         occurs while fetching a FeatureType schema to ask it for
      *         propper attribute names</li> </ul>
      */
     private List parseAttributes(FeatureTypeInfo[] layers)
         throws WmsException {
         String rawAtts = getValue("ATTRIBUTES");
         LOGGER.finer("parsing attributes " + rawAtts);
 
         if ((rawAtts == null) || "".equals(rawAtts)) {
             return Collections.EMPTY_LIST;
         }
 
         //raw list of attributes for each feature type requested
         List byFeatureTypes = readFlat(rawAtts, "|");
         int nLayers = layers.length;
 
         if (byFeatureTypes.size() != nLayers) {
             throw new WmsException(byFeatureTypes.size()
                 + " lists of attributes specified, expected " + layers.length,
                 getClass().getName() + "::parseAttributes()");
         }
 
         //fill byFeatureTypes with the split of its raw attributes requested
         //separated by commas, and check for the validity of each att name
         for (int i = 0; i < nLayers; i++) {
             rawAtts = (String) byFeatureTypes.get(i);
 
             List atts = readFlat(rawAtts, ",");
             byFeatureTypes.set(i, atts);
 
             //FeatureType schema = layers[i].getSchema();
             try {
                 FeatureType schema = layers[i].getFeatureType();
 
                 //verify that propper attributes has been requested
                 for (Iterator attIt = atts.iterator(); attIt.hasNext();) {
                     String attName = (String) attIt.next();
 
                     if (attName.length() > 0) {
                         LOGGER.finer("checking that " + attName + " is valid");
 
                         if ("#FID".equalsIgnoreCase(attName)
                                 || "#BOUNDS".equalsIgnoreCase(attName)) {
                             LOGGER.finer("special attribute name requested: "
                                 + attName);
 
                             continue;
                         }
 
                         if (schema.getAttributeType(attName) == null) {
                             throw new WmsException("Attribute '" + attName
                                 + "' requested for layer "
                                 + schema.getTypeName() + " does not exists");
                         }
                     } else {
                         LOGGER.finest(
                             "removing empty attribute name from request");
                         attIt.remove();
                     }
                 }
 
                 LOGGER.finest("attributes requested for "
                     + schema.getTypeName() + " checked: " + rawAtts);
             } catch (java.io.IOException e) {
                 throw new WmsException(e);
             }
         }
 
         return byFeatureTypes;
     }
 
     /**
      * parses the BBOX parameter, wich must be a String of the form
      * <code>minx,miny,maxx,maxy</code> and returns a corresponding
      * <code>Envelope</code> object
      *
      * @return the <code>Envelope</code> represented by the request BBOX
      *         parameter
      *
      * @throws WmsException if the value of the BBOX request parameter can't be
      *         parsed as four <code>double</code>'s
      */
     private Envelope parseBbox() throws WmsException {
         Envelope bbox = null;
         String bboxParam = getValue("BBOX");
         Object[] bboxValues = readFlat(bboxParam, INNER_DELIMETER).toArray();
 
         if (bboxValues.length != 4) {
             throw new WmsException(bboxParam
                 + " is not a valid pair of coordinates", getClass().getName());
         }
 
         try {
             double minx = Double.parseDouble(bboxValues[0].toString());
             double miny = Double.parseDouble(bboxValues[1].toString());
             double maxx = Double.parseDouble(bboxValues[2].toString());
             double maxy = Double.parseDouble(bboxValues[3].toString());
             bbox = new Envelope(minx, maxx, miny, maxy);
 
             if (minx > maxx) {
                 throw new WmsException("illegal bbox, minX: " + minx + " is "
                     + "greater than maxX: " + maxx);
             }
 
             if (miny > maxy) {
                 throw new WmsException("illegal bbox, minY: " + miny + " is "
                     + "greater than maxY: " + maxy);
             }
         } catch (NumberFormatException ex) {
             throw new WmsException(ex,
                 "Illegal value for BBOX parameter: " + bboxParam,
                 getClass().getName() + "::parseBbox()");
         }
 
         return bbox;
     }
 
     /**
      * Parses the list of style names requested for each requested layer and
      * looks up the actual Style objects, which are returned in an ordered
      * list.
      * 
      * <p>
      * A client _may_ request teh default Style using a null value (as in
      * "STYLES="). If  several layers are requested with a mixture of named
      * and default styles,  the STYLES parameter includes null values between
      * commas (as in  "STYLES=style1,,style2,,").  If all layers are to be
      * shown using the default style, either the  form "STYLES=" or
      * "STYLES=,,," is valid.
      * </p>
      *
      * @param request DOCUMENT ME!
      * @param layers the requested feature types
      *
      * @return a full <code>List</code> of the style names requested for the
      *         requiered layers with no null style names.
      *
      * @throws WmsException if some of the requested styles does not exist or
      *         its number if greater than zero and distinct of the number of
      *         requested layers
      */
     private List parseStylesParam(GetMapRequest request,
         FeatureTypeInfo[] layers) throws WmsException {
         String rawStyles = getValue("STYLES");
         List styles = styles = new ArrayList(layers.length);
 
         int numLayers = layers.length;
 
         if ("".equals(rawStyles)) {
             LOGGER.finer("Assigning default style to all the requested layers");
 
             for (int i = 0; i < numLayers; i++)
                 styles.add(layers[i].getDefaultStyle());
         } else {
             List styleNames = readFlat(rawStyles, INNER_DELIMETER);
 
             if (numLayers != styleNames.size()) {
                 String msg = numLayers + " layers requested, but found "
                     + styleNames.size() + " styles specified. "
                     + "Since SLD parameter is not yet implemented, the STYLES parameter "
                     + "is mandatory and MUST have exactly one value per requested layer";
                 throw new WmsException(msg, getClass().getName());
             }
 
             String currStyleName;
             Style currStyle;
             FeatureTypeInfo currLayer;
 
             for (int i = 0; i < numLayers; i++) {
                 currStyleName = (String) styleNames.get(i);
                 currLayer = layers[i];
 
                 if ((null == currStyleName) || "".equals(currStyleName)) {
                     currStyle = currLayer.getDefaultStyle();
                 } else {
                     currStyle = findStyle(request, currStyleName);
 
                     if (currStyle == null) {
                         String msg = "No default style has been defined for "
                             + currLayer.getName();
                         throw new WmsException(msg,
                             "StyleNotDefined");
                     }
                 }
 
                 try {
                     checkStyle(currStyle, layers[i].getFeatureType());
                 } catch (IOException e) {
                     throw new WmsException(
                         "Error obtaining FeatureType for layer "
                         + layers[i].getName());
                 }
 
                 LOGGER.fine("establishing " + currStyleName + " style for "
                     + layers[i].getName());
                 styles.add(currStyle);
             }
         }
 
         return styles;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param request
      * @param currStyleName
      *
      * @return the configured style named <code>currStyleName</code> or
      *         <code>null</code> if such a style does not exists on this
      *         server.
      */
     public static Style findStyle(GetMapRequest request, String currStyleName) {
         Style currStyle;
         Map configuredStyles = request.getWMS().getData().getStyles();
 
         currStyle = (Style) configuredStyles.get(currStyleName);
 
         return currStyle;
     }
 
     /**
      * Checks to make sure that the style passed in can process the
      * FeatureType.
      *
      * @param style The style to check
      * @param fType The source requested.
      *
      * @throws WmsException DOCUMENT ME!
      */
     private void checkStyle(Style style, FeatureType fType)
         throws WmsException {
         StyleAttributeExtractor sae = new StyleAttributeExtractor();
         sae.visit(style);
 
         String[] styleAttributes = sae.getAttributeNames();
 
         for (int i = 0; i < styleAttributes.length; i++) {
             String attName = styleAttributes[i];
 
             if (fType.getAttributeType(attName) == null) {
                 throw new WmsException(
                     "The requested Style can not be used with "
                     + "this featureType.  The style specifies an attribute of "
                     + attName + " and the featureType definition is: " + fType);
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param request DOCUMENT ME!
      *
      * @throws WmsException DOCUMENT ME!
      */
     private void parseLayersAndStyles(GetMapRequest request)
         throws WmsException {
         String sldParam = getValue("SLD");
         String sldBodyParam = getValue("SLD_BODY");
 
         if (sldBodyParam != null) {
             LOGGER.fine("Getting layers and styles from SLD_BODY");
             parseSldBodyParam(request);
         } else if (sldParam != null) {
             LOGGER.fine("Getting layers and styles from reomte SLD");
             parseSldParam(request);
         } else {
             FeatureTypeInfo[] featureTypes = null;
             List styles = null;
             featureTypes = parseLayersParam(request);
 
             request.setLayers(featureTypes);
 
             if (isStylesRquired()) {
                 styles = parseStylesParam(request, featureTypes);
                 if (isStylesRquired()) {
                     request.setStyles(styles);
                 }
             }
         }
 
     }
 
     /**
      * Takes the SLD_BODY parameter value and parses it to a geotools'
      * <code>StyledLayerDescriptor</code>, then takes the layers and styles to
      * use in the map composition from there.
      *
      * @param request DOCUMENT ME!
      *
      * @throws WmsException DOCUMENT ME!
      */
     private void parseSldBodyParam(GetMapRequest request)
         throws WmsException {
         final String sldBody = getValue("SLD_BODY");
 
         if (LOGGER.isLoggable(Level.FINE)) {
             LOGGER.fine("About to parse SLD body: " + sldBody);
         }
                 
         
         if (getValue("VALIDATESCHEMA") != null)
         {
         	InputStream in = new StringBufferInputStream(sldBody);
         	// user requested to validate the schema.
         	SLDValidator validator = new SLDValidator();
         	List errors =null;
 
         		errors = validator.validateSLD(in, request.getHttpServletRequest().getSession().getServletContext());
         		try{
         			in.close();
         		}
         		catch(Exception e)
 				{
         			// do nothing
 				}
         		if (errors.size() != 0)
         		{
         			in = new StringBufferInputStream(sldBody);
         			throw new WmsException(SLDValidator.getErrorMessage(in,errors));
         		}
         }
 
         InputStream in = new StringBufferInputStream(sldBody);
         SLDParser parser = new SLDParser(styleFactory, in);
         StyledLayerDescriptor sld = parser.parseSLD();
         parseStyledLayerDescriptor(request, sld);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param request DOCUMENT ME!
      *
      * @throws WmsException DOCUMENT ME!
      */
     private void parseSldParam(GetMapRequest request) throws WmsException 
 	{
         String urlValue = getValue("SLD");               
 
         if (LOGGER.isLoggable(Level.FINE)) {
             LOGGER.fine("about to load remote SLD document: '" + urlValue + "'");
         }
 
         URL sldUrl;
 
         try {
             sldUrl = new URL(urlValue);
         } catch (MalformedURLException e) {
             String msg = "Creating remote SLD url: " + e.getMessage();
             LOGGER.log(Level.WARNING, msg, e);
             throw new WmsException(e, msg, "parseSldParam");
         }
 
         
         if (getValue("VALIDATESCHEMA") != null)
         {
         	// user requested to validate the schema.
         	SLDValidator validator = new SLDValidator();
         	List errors =null;
         	try {
         		InputStream in = sldUrl.openStream();
         		errors = validator.validateSLD(in, request.getHttpServletRequest().getSession().getServletContext());
         		in.close();
         		if (errors.size() != 0)
         			throw new WmsException(SLDValidator.getErrorMessage(sldUrl.openStream(),errors));
         	}
         	catch (IOException e)
 			{
         		String msg = "Creating remote SLD url: " + e.getMessage();
                 LOGGER.log(Level.WARNING, msg, e);
                 throw new WmsException(e, msg, "parseSldParam");
 			}
         }
         SLDParser parser;
 
         try {
             parser = new SLDParser(styleFactory, sldUrl);
         } catch (IOException e) {
             String msg = "Creating remote SLD url: " + e.getMessage();
             LOGGER.log(Level.WARNING, msg, e);
             throw new WmsException(e, msg, "parseSldParam");
         }
 
         StyledLayerDescriptor sld = parser.parseSLD();
         parseStyledLayerDescriptor(request, sld);
     }
 
     /**
      * Looks in <code>sld</code> for the layers and styles to use in the map
      * composition and sets them to the <code>request</code>
      * 
      * <p>
      * If <code>sld</code> is used in "library" mode, that is, the LAYERS param
      * is also present, saying what layers must be taken in count, then only
      * the layers from the LAYERS parameter are used and <code>sld</code> is
      * used as a style library, which means that for each layer requested
      * through LAYERS=..., if a style if found in it for that layer it is
      * used, and if not, the layers default is used.
      * </p>
      * 
      * <p>
      * By the other hand, if the LAYERS parameter is not present all the layers
      * found in <code>sld</code> are setted to <code>request</code>.
      * </p>
      *
      * @param request the GetMap request to which to set the layers and styles
      * @param sld a SLD document to take layers and styles from, following the
      *        "literal" or "library" rule.
      *
      * @throws WmsException if anything goes wrong
      * @throws RuntimeException DOCUMENT ME!
      */
     private void parseStyledLayerDescriptor(final GetMapRequest request,
         final StyledLayerDescriptor sld) throws WmsException {
         FeatureTypeInfo[] libraryModeLayers = null;
 
         if (null != getValue("LAYERS")) {
             LOGGER.info("request comes in \"library\" mode");
             libraryModeLayers = parseLayersParam(request);
         }
 
         final StyledLayer[] styledLayers = sld.getStyledLayers();
         final int slCount = styledLayers.length;
 
         if (slCount == 0) {
             throw new WmsException("SLD document contains no layers");
         }
 
         final List layers = new ArrayList();
         final List styles = new ArrayList();
 
         FeatureTypeInfo currLayer;
         Style currStyle;
 
         if (null != libraryModeLayers) {
             int lCount = libraryModeLayers.length;
 
             for (int i = 0; i < lCount; i++) {
                 currLayer = libraryModeLayers[i];
                 currStyle = findStyleOf(request, currLayer, styledLayers); // DJB: this might not be 100% correct, but its close enough until someone complains
                 layers.add(currLayer);
                 styles.add(currStyle);
             }
         } else {
             StyledLayer sl = null;
             for (int i = 0; i < slCount; i++) {
                 sl = styledLayers[i];
                 String layerName = sl.getName();
                 if(null == layerName)
                 	throw new WmsException("A UserLayer without layer name was passed");
 
 
                 	// handle the InLineFeature stuff
                     // TODO: add support for remote WFS here
                 if ((sl instanceof UserLayer) && (((UserLayer)sl)).getInlineFeatureDatastore()!=null )
                 {
                 	//SPECIAL CASE - we make the temporary version
                 	UserLayer ul = ((UserLayer)sl);
                 	currLayer = new TemporaryFeatureTypeInfo(ul.getInlineFeatureDatastore(), ul.getInlineFeatureType());
                 }
                 else
                 	currLayer = GetMapKvpReader.findLayer(request, layerName);
                 
                 
                // currStyle = findStyleOf(request, currLayer, styledLayers); // DJB: this looks like a bug, we should get the style from styledLayers[i]
                 
                  // the correct thing to do its grab the style from styledLayers[i]
                  //   inside the styledLayers[i] will either be :
                  //     a) nothing - in which case grab the layer's default style
                  //     b) a set of:
                  //             i) NameStyle -- grab it from the pre-loaded styles
                  //             ii)UserStyle -- grab it from the sld the user uploaded
                  //
                 // NOTE: we're going to get a set of layer->style pairs for (b).
                 
                 addStyles(request,currLayer,styledLayers[i],   layers,styles);
                 
                 //layers.add(currLayer);
                // styles.add(currStyle);
             }
         }
         request.setLayers((FeatureTypeInfo[])layers.toArray(new FeatureTypeInfo[layers.size()]));
         request.setStyles(styles);
     }
 
  
 
 	/**
 	 * the correct thing to do its grab the style from styledLayers[i]
      * inside the styledLayers[i] will either be :
      *  a) nothing - in which case grab the layer's default style
      *  b) a set of:
      * i) NameStyle -- grab it from the pre-loaded styles
      *  ii)UserStyle -- grab it from the sld the user uploaded
      * 
      *  NOTE: we're going to get a set of layer->style pairs for (b).
      *        these are added to layers,styles
      * 
      *   NOTE: we also handle some featuretypeconstraints
      * 
 	 * @param request
 	 * @param currLayer
 	 * @param layer
 	 * @param layers
 	 * @param styles
 	 */
	public static void addStyles(GetMapRequest request, FeatureTypeInfo currLayer, StyledLayer layer, List layers, List styles) 
 	{
 		if (currLayer == null)
 			return; // protection
 		
 		Style[] layerStyles =null;  
 		FeatureTypeConstraint[]  ftcs = null;
 		
 		if (layer instanceof NamedLayer)
 		{
 			ftcs = ((NamedLayer) layer).getLayerFeatureConstrains();
 			layerStyles = ((NamedLayer) layer).getStyles();
 		}
 		else if (layer instanceof UserLayer)
 		{
 			ftcs = ((UserLayer)layer).getLayerFeatureConstraints();
 			layerStyles = ((UserLayer) layer).getUserStyles();
 		}
 		
 			//DJB:  TODO: this needs to do the whole thing, not just names
 			if (ftcs != null)
 			{
 				for (int t=0;t<ftcs.length;t++)
 				{
 					FeatureTypeConstraint ftc = ftcs[t];
 					if (ftc.getFeatureTypeName() != null)
 					{
 						String ftc_name = ftc.getFeatureTypeName();
 							//taken from lite renderer
 						boolean matches ;
 						try{
 							matches = currLayer.getFeatureType().isDescendedFrom(null,ftc_name) || currLayer.getFeatureType().getTypeName().equalsIgnoreCase(ftc_name);
 						}
 						catch(Exception e)
 						{
 							matches = false; // bad news
 						}
 						if (!matches)
 							continue ; // this layer is fitered out					
 					}
 				}
 			}
 		
 			//handle no styles -- use default
 			if ( (layerStyles == null) || (layerStyles.length ==0))
 			{
 				layers.add(currLayer);
 				styles.add( currLayer.getDefaultStyle() );
 				return;
 			}
 			
 			for (int t=0;t<layerStyles.length;t++)
 			{
 				if (layerStyles[t] instanceof NamedStyle)
 				{
 					layers.add(currLayer);
					styles.add(findStyle( request, ((NamedStyle) layerStyles[t]).getName() ));
 				}
 				else 
 				{
 					layers.add(currLayer);
 					styles.add(layerStyles[t]);
 				}
 			}
 	}
 
 	/**
      * Finds the style for <code>layer</code> in <code>styledLayers</code> or
      * the layer's default style if <code>styledLayers</code> has no a
      * UserLayer or a NamedLayer with the same name than <code>layer</code>
      * <p>
      * This method is used to parse the style of a layer for SLD and SLD_BODY parameters,
      * both in library and literal mode. Thus, once the declared style for the given layer
      * is found, it is checked for validity of appliance for that layer (i.e., whether the
      * featuretype contains the attributes needed for executing the style filters).
      * </p>
      *
      * @param request used to find out an internally configured style when referenced by name by a NamedLayer
      * 
      * @param layer one of the internal FeatureType that was requested through the LAYERS parameter
      * or through and SLD document when the request is in literal mode.
      * @param styledLayers a set of StyledLayers from where to find the SLD layer with the same
      * name as <code>layer</code> and extract the style to apply.
      *
      * @return the Style applicable to <code>layer</code> extracted from <code>styledLayers</code>.
      *
      * @throws RuntimeException if one of the StyledLayers is neither a UserLayer nor a NamedLayer. This
      * shuoldn't happen, since the only allowed subinterfaces of StyledLayer are NamedLayer and UserLayer.
      * @throws WmsException 
      */
     private Style findStyleOf(GetMapRequest request, FeatureTypeInfo layer,
         StyledLayer[] styledLayers)throws WmsException {
         Style style = null;
         String layerName = layer.getName();
         StyledLayer sl;
 
         for (int i = 0; i < styledLayers.length; i++) {
             sl = styledLayers[i];
 
             if (layerName.equals(sl.getName())) {
                 if (sl instanceof UserLayer) {
                     Style[] styles = ((UserLayer) sl).getUserStyles();
 
                     if ((null != styles) && (0 < styles.length)) {
                         style = styles[0];
                     }
                 } else if (sl instanceof NamedLayer) {
                     Style[] styles = ((NamedLayer) sl).getStyles();
 
                     if ((null != styles) && (0 < styles.length)) {
                         style = styles[0];
                     }
 
                     if (style instanceof NamedStyle) {
                         style = findStyle(request, style.getName());
                     }
                 } else {
                     throw new RuntimeException("Unknown layer type: " + sl);
                 }
 
                 break;
             }
         }
 
         if (null == style) {
             style = layer.getDefaultStyle();
         }
 
 		FeatureType type;
 		try{
 			type = layer.getFeatureType();
 		}catch(IOException ioe){
 			throw new RuntimeException("Error getting FeatureType, this should never happen!");
 		}
 		checkStyle(style, type);
         return style;
     }
 
     /**
      * Parses the requested layers given by the LAYERS request parameter and
      * looks up their corresponding FeatureTypeInfo objects in the server.
      *
      * @param request
      *
      * @return
      *
      * @throws WmsException
      */
     private FeatureTypeInfo[] parseLayersParam(GetMapRequest request)
         throws WmsException {
         FeatureTypeInfo[] featureTypes;
         String layersParam = getValue("LAYERS");
         List layerNames = layerNames = readFlat(layersParam, INNER_DELIMETER);
         int layerCount = layerNames.size();
 
         if (layerCount == 0) {
             throw new WmsException("No LAYERS has been requested",
                 getClass().getName());
         }
 
         featureTypes = new FeatureTypeInfo[layerCount];
 
         String layerName = null;
 
         for (int i = 0; i < layerCount; i++) {
             layerName = (String) layerNames.get(i);
 
             FeatureTypeInfo ftype = findLayer(request, layerName);
             featureTypes[i] = ftype;
         }
 
         return featureTypes;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param request
      * @param layerName
      *
      * @return
      *
      * @throws WmsException DOCUMENT ME!
      */
     public static FeatureTypeInfo findLayer(GetMapRequest request, String layerName)
         throws WmsException {
         Data catalog = request.getWMS().getData();
         FeatureTypeInfo ftype = null;
 
         try {
             ftype = catalog.getFeatureTypeInfo(layerName);
         } catch (NoSuchElementException ex) {
         	WmsException e = new WmsException(ex,
                 layerName + ": no such layer on this server", "LayerNotDefined");
         	e.setCode("LayerNotDefined"); //DJB: added this for cite tests
         	throw e;
         }
 
         return ftype;
     }
 }
