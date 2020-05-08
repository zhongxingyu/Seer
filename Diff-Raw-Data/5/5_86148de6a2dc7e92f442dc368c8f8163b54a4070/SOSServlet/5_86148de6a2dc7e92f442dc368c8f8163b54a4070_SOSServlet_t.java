 package gov.usgs.cida.ogc;
 
 import gov.usgs.cida.ogc.specs.OGC_WFSConstants;
 import gov.usgs.cida.ogc.specs.SOS_1_0_Operation;
 import gov.usgs.cida.ogc.utils.FileResponseUtil;
 import gov.usgs.cida.ogc.utils.ServletHandlingUtils;
 import gov.usgs.cida.ogc.utils.ServletHandlingUtils.RequestBodyExceededException;
 import gov.usgs.cida.utils.collections.CaseInsensitiveMap;
 import gov.usgs.webservices.ibatis.XMLStreamReaderDAO;
 import gov.usgs.webservices.stax.XMLStreamUtils;
 
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.stream.XMLStreamReader;
 import javax.xml.stream.XMLStreamWriter;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathFactory;
 
 import org.codehaus.stax2.XMLOutputFactory2;
 import org.springframework.context.ApplicationContext;
 import org.springframework.web.context.support.WebApplicationContextUtils;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 
 /**
  * Servlet implementation class to handle SOS requests
  */
 public class SOSServlet extends HttpServlet {
 	
 	private static final String DEFAULT_ENCODING = "UTF-8";
 
 	private static final long serialVersionUID = 1L;
 
 //	private final static String XPATH_Envelope = "//sos:GetObservation/sos:featureOfInterest/ogc:BBOX[ogc:PropertyName='gml:location']/gml:Envelope";
 	private final static String XPATH_Envelope = "//sos:GetObservation/sos:featureOfInterest/ogc:BBOX/gml:Envelope";
 	private final static String XPATH_cornerLower = "gml:lowerCorner/text()";
 	private final static String XPATH_upperCorner = "gml:upperCorner/text()";
 	
 	private final static Pattern PATTERN_cornerSplit = Pattern.compile("\\s+");
 	
 	private final static XMLOutputFactory2 xmlOutputFactory;
 	
 	static {
 		xmlOutputFactory = (XMLOutputFactory2)XMLOutputFactory2.newInstance();
 		xmlOutputFactory.setProperty(XMLOutputFactory2.IS_REPAIRING_NAMESPACES, false);
 		xmlOutputFactory.configureForSpeed();
 	}
 
 	/**
 	 * @see HttpServlet#HttpServlet()
 	 */
 	public SOSServlet() {
 		super();
 	}
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	@Override
 	@SuppressWarnings("unchecked")
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		Map<String, String[]> parameterMap = new CaseInsensitiveMap<String[]>(request.getParameterMap());
 		queryAndSend(request, response, parameterMap);
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	@Override
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws RequestBodyExceededException, IOException {
 		try {
 			Document document = ServletHandlingUtils.extractXMLRequestDocument(request);
 			Map<String, String[]> parameterMap = createParameterMapFromDocument(document);
 			queryAndSend(request, response, parameterMap);
 			
 		} catch (RequestBodyExceededException rbe) {
 			// If we get errors in the request parsing, then just send the error
 			int errorCode = (rbe.errorCode != null)? rbe.errorCode: 403; // 403 as default value for error code
 			rbe.printStackTrace();
 			response.sendError(errorCode, rbe.getMessage());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void queryAndSend(HttpServletRequest request, HttpServletResponse response, Map<String, String[]> parameterMap) throws IOException {
 		ServletHandlingUtils.dumpRequestParamsToConsole(parameterMap);
 		// TODO parameterMap may or may not be case-insensitive, depending on path of arrival post or get. Correct this later.
 		SOS_1_0_Operation opType = SOS_1_0_Operation.parse(parameterMap.get("request"));
 		
 		ServletOutputStream outputStream = response.getOutputStream();
 		response.setContentType(OGC_WFSConstants.DEFAULT_DESCRIBEFEATURETYPE_OUTPUTFORMAT);
 		response.setCharacterEncoding(DEFAULT_ENCODING);
 		switch (opType) {
 			case GetObservation:
 				cleanFeatureId(parameterMap);
 				
 
 				try {
 					XMLStreamReader streamReader = getXMLStreamReaderDAO().getStreamReader("sosMapper.observationsSelect", parameterMap);
 					XMLStreamWriter streamWriter = xmlOutputFactory.createXMLStreamWriter(outputStream);
 					XMLStreamUtils.copy(streamReader, streamWriter);
 				} catch (Exception e) {
 					e.printStackTrace();
 				} finally {
 					outputStream.flush();
 				}
 				break;
 			case GetCapabilities:
 				Map<String, String> replacementMap = new HashMap<String, String>();
 				replacementMap.put("base.url", ServletHandlingUtils.parseBaseURL(request));
 
 				// Just sending back static file for now.
 				String resource = "/ogc/sos/GetCapabilities.xml";
 				String errorMessage = "<error>Unable to retrieve resource " + resource + "</error";
 				FileResponseUtil.writeToStreamWithReplacements(resource, outputStream, replacementMap,
 						errorMessage);
 				break;
 			case DescribeSensor:
 				BufferedWriter writer = FileResponseUtil.wrapAsBufferedWriter(outputStream);
 				try {
 					writer.append("<error>DescribeSensor REQUEST type to be implemented</error>");
 				} catch (Exception e) {
 					// TODO: handle exception
 				} finally {
 					outputStream.flush();
 				}
 				
 				break;
 			default:
 				writer = FileResponseUtil.wrapAsBufferedWriter(outputStream);
 				try {
 					writer.append("unrecognized or unhandled REQUEST type = " + opType);
 				} catch (Exception e) {
 					// TODO: handle exception
 				} finally {
 					outputStream.flush();
 				}
 				break;
 		}
 
 	}
 
 	private void cleanFeatureId(
 			Map<String, String[]> parameterMap) {
 		String[] featureParam = parameterMap.get("featureId");
 		if (featureParam != null && featureParam[0] != null) {
 			String featureId = featureParam[0];
 			if (featureId.startsWith("USGS.")) {
 				System.out.println(featureId + " - ");
 				featureId = featureId.substring(5);
 				System.out.println(featureId);
 				featureParam[0] = featureId;
 			}
 		}
 	}
 
 	private Map<String, String[]> createParameterMapFromDocument(Document document) throws Exception {
 		if (document == null) {
 			return null;
 		}
 		// TODO Ask Tom why a LinkedHashMap?
 		Map<String, String[]> parameterMap = new LinkedHashMap<String, String[]>();
 		
 		XPathFactory xpathFactory = XPathFactory.newInstance();
 		XPath xpath = xpathFactory.newXPath();
 		xpath.setNamespaceContext(new OGCBinding.GetObservationNamespaceContext());
 		
 		// Currently, the only parameter we are handling is the bounding box
 		
 		// XPath expressions for the container of the bounding box and the upper and lower corners
 		XPathExpression envelopeExpression =  xpath.compile(XPATH_Envelope);
 		XPathExpression lowerCornerExpression =  xpath.compile(XPATH_cornerLower);
 		XPathExpression upperCornerExpression =  xpath.compile(XPATH_upperCorner);
 		
 		Object envelopeResult = envelopeExpression.evaluate(document, XPathConstants.NODE);
 		if (envelopeResult != null && envelopeResult instanceof Node) {
			// We are necessarily in the GetObservations element, by the expression for XPATH_Envelope
			{	// bad logic. Refactor out
				parameterMap.put("request", new String[] {"GetObservation"});
			}
			
 			Node envelopeNode = (Node)envelopeResult;
 			String lowerCornerString = lowerCornerExpression.evaluate(envelopeNode);
 			String upperCornerString = upperCornerExpression.evaluate(envelopeNode);
 			
 			// Parse the coordinates of the bounding box corner parameters
 			if (lowerCornerString != null && upperCornerString != null) {
 				String[] lowerSplit = PATTERN_cornerSplit.split(lowerCornerString.trim());
 				String[] upperSplit = PATTERN_cornerSplit.split(upperCornerString.trim());
 				if (lowerSplit.length == 2 && upperSplit.length == 2) {
 					try {
 						float lon0 = Float.parseFloat(lowerSplit[0]);
 						float lat0 = Float.parseFloat(lowerSplit[1]);
 						float lon1 = Float.parseFloat(upperSplit[0]);
 						float lat1 = Float.parseFloat(upperSplit[1]);
 						if (Float.isNaN(lon0) || Float.isNaN(lat0) || Float.isNaN(lon1) || Float.isNaN(lat1)) {
 							System.err.println("invalid number format");
 						} else {
 							if (lon0 < lon1) {
 								parameterMap.put("east", new String[] { upperSplit[0]} );
 								parameterMap.put("west", new String[] { lowerSplit[0]} );
 							} else {
 								parameterMap.put("east", new String[] { lowerSplit[0] } );
 								parameterMap.put("west", new String[] { upperSplit[0] } );
 							}
 							if (lat0 < lat1) {
 								parameterMap.put("south", new String[] { lowerSplit[1] } );
 								parameterMap.put("north", new String[] { upperSplit[1] } );
 							} else {
 								parameterMap.put("south", new String[] { upperSplit[1] } );
 								parameterMap.put("north", new String[] { lowerSplit[1] } );
 							}
 						}
 					} catch (NumberFormatException e) {
 						System.out.println(XPATH_cornerLower + " or " + XPATH_upperCorner + " contain value with invalid number format");
 					}
 				} else {
 					System.out.println(XPATH_cornerLower + " or " + XPATH_upperCorner + " contain an invalid parameter count (expected 2, whitespace delimited).");
 				}
 			} else {
 				System.out.println(XPATH_cornerLower + " or " + XPATH_upperCorner + " not found.");
 			}
 		} else {
 			System.out.println(XPATH_Envelope + " : not found.");
 		}
 		return parameterMap;
 	}
 
 	private XMLStreamReaderDAO getXMLStreamReaderDAO() throws ServletException {
 		XMLStreamReaderDAO xmlStreamReaderDAO = null;
 		ApplicationContext ac = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
 		if (ac != null) {
 			Object o = ac.getBean("xmlStreamReaderDAO");
 			if (o != null && o instanceof XMLStreamReaderDAO) {
 				xmlStreamReaderDAO = (XMLStreamReaderDAO)o;
 			}
 		}
 		if(xmlStreamReaderDAO == null) {
 			throw new ServletException("Configuation error, unable to obtain reference to XMLStreamReaderDAO");
 		}
 		return xmlStreamReaderDAO;
 	}
 }
