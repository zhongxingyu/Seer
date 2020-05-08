 package com.gentics.cr.rest.xml;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.UnsupportedEncodingException;
 import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.TransformerFactoryConfigurationError;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Text;
 
 import com.gentics.cr.CRResolvableBean;
 import com.gentics.cr.exceptions.CRException;
 import com.gentics.cr.rest.ContentRepository;
 import com.gentics.cr.util.Constants;
 
 /**
  * 
  * Last changed: $Date: 2010-04-01 15:25:54 +0200 (Do, 01 Apr 2010) $
  * @version $Revision: 545 $
  * @author $Author: supnig@constantinopel.at $
  *
  */
 public class CSSitemapContentRepository extends ContentRepository {
 
 	
 	/**
 	 * generated serial version unique id.
 	 */
 	private static final long serialVersionUID = -6929053170765114770L;
 
 	/**
 	 * the root element in the xml code.
 	 */
 	private Element rootElement;
 
 	/**
 	 * the xml document to write to the stream.
 	 */
 	private Document doc;
 
 	/**
 	 * The {@link DOMSource} to write the elements into.
 	 */
 	private DOMSource src;
 
 	/**
 	 * Initialize a new {@link CSSitemapContentRepository}.
 	 * @param attr Attributes to write into the stream for each object.
 	 */
 	public CSSitemapContentRepository(final String[] attr) {
 		
 		this(attr, "utf-8");
 
 	}
 	/**
 	 * Initialize a new {@link CSSitemapContentRepository}.
 	 * @param attr Attributes to write into the stream for each object.
 	 * @param encoding Encoding to use for the stream.
 	 */
 	public CSSitemapContentRepository(final String[] attr,
 			final String encoding) {
 		this(attr, encoding, null);
 
 	}
 	/**
 	 * Initialize a new {@link CSSitemapContentRepository}.
 	 * @param attr Attributes to write into the stream for each object.
 	 * @param encoding Encoding to use for the stream.
 	 * @param options Options array TODO javadoc
 	 */
 	public CSSitemapContentRepository(final String[] attr,
 			final String encoding, final String[] options) {
 		super(attr, encoding, options);
 
 		// Create XML Document
 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 		DocumentBuilder builder;
 		//this.setResponseEncoding(encoding);
 		try {
 			
 			builder = factory.newDocumentBuilder();
 			this.doc = builder.newDocument();
 		
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		}
 		
 		this.src = new DOMSource(doc);
 	}
 	
 
 	/**
 	 * returns text/xml as contenttype.
 	 * @return "text/xml"
 	 */
 	public final String getContentType() {
 		return "text/xml";
 	}
 	
 	/**
 	 * clear the given element from all attributes.
 	 */
 	private void clearElement(Element elem) {
 		if (elem != null) {
 			NodeList list = elem.getChildNodes();
 		
 			//int len = list.getLength();
 			for (int i = 0; i < list.getLength(); i++) {
 				elem.removeChild(list.item(i));
 			}
 			NamedNodeMap map = elem.getAttributes();
 			//len =map.getLength();
 			for (int i = 0; i < map.getLength(); i++) {
 				elem.removeAttribute(map.item(i).getNodeName());
 			}
 		}
 	}
 	
 	/**
 	 * Write an error to the specified stream.
 	 * @param stream Stream to write the error into
 	 * @param ex exception to write into the stream
 	 * @param isDebug specifies if debug is enabled (e.g. Output the stacktrace)
 	 */
 	@Override
 	public final void respondWithError(final OutputStream stream,
 			final CRException ex, final boolean isDebug) {
 		// Create Root Element
 		this.rootElement = doc.createElement("Contentrepository");
 		doc.appendChild(rootElement);
 		clearElement(this.rootElement);
 		Element errElement = doc.createElement("Error");
 		errElement.setAttribute("type", ex.getType());
 		errElement.setAttribute("messge", ex.getMessage());
 		if (isDebug) {
 			Element stackTrace = doc.createElement("StackTrace");
 			Text text = doc.createCDATASection(ex.getStringStackTrace());
 			stackTrace.appendChild(text);
 			errElement.appendChild(stackTrace);
 		}
 		
 		this.rootElement.setAttribute("status", "error");
 		this.rootElement.appendChild(errElement);
 		
 		StreamResult strRes = new StreamResult(stream);
 		try {
 			
 			TransformerFactory.newInstance().newTransformer().transform(
 					this.src, strRes);
 		} catch (TransformerConfigurationException e) {
 			e.printStackTrace();
 		} catch (TransformerException e) {
 			e.printStackTrace();
 		} catch (TransformerFactoryConfigurationError e) {
 			e.printStackTrace();
 		}
 		
 	}
 
 	/**
 	 * Write XML Elements to the specified stream.
 	 * @param stream Stream to write the xml code into
 	 * @throws CRException if there was no data to write into the stream
 	 */
 	@Override
 	public final void toStream(final OutputStream stream) throws CRException {
 		if (this.resolvableColl.isEmpty()) {
 			throw new CRException("NoDataFound", "Data could not be found.");
 		} else {
 			rootElement = processElement(resolvableColl.get(0));
 			doc.appendChild(rootElement);
 		}
 		
 		try {
 			OutputStreamWriter wr =
 				new OutputStreamWriter(stream, this.getResponseEncoding());
 									
 			StreamResult strRes = new StreamResult(wr);
 			Transformer xmlTransformer =
 				TransformerFactory.newInstance().newTransformer();
 			xmlTransformer.setOutputProperty(OutputKeys.INDENT, "yes");
 			xmlTransformer.transform(this.src, strRes);
 			wr.flush();
 			wr.close();
 		} catch (TransformerConfigurationException e) {
 			e.printStackTrace();
 		} catch (TransformerException e) {
 			e.printStackTrace();
 		} catch (TransformerFactoryConfigurationError e) {
 			e.printStackTrace();
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Process a single bean and its children to a XML node.
 	 * @param crBean bean to process into a xml node
 	 * @return xml node for the bean including its children.
 	 */
 	private Element processElement(final CRResolvableBean crBean) {
 		Element objElement = doc.createElement("folder");
 		
 		
 		objElement.setAttribute("id", "" + crBean.getObj_id());
 		Map<String, Object> attributes = crBean.getAttrMap();
 		if (attributes != null && (!attributes.isEmpty())) {
 			for (String attributeName : attributes.keySet()) {
 				if (!"".equals(attributeName)) {
 					Object bValue = attributes.get(attributeName);
 					String value = "";
 					if (bValue != null) {
 						if (attributeName.equals("binarycontent")) {
 							try {
 								value = new String((byte[]) bValue);
 							} catch (ClassCastException x) {
 								try {
 									value = new String(getBytes(bValue));
 								} catch (IOException e) {
 									value = bValue.toString();
 									log.debug("Error converting binary to"
 											+ "String", e);
 								}
 							}
 						} else if (attributeName.equals("updatetimestamp")) {
 							long timestamp = 0;
 							if (bValue instanceof Integer) {
 								timestamp = (Integer) bValue;
 							} else if (bValue instanceof Long) {
 								timestamp = (Long) bValue;
 							} else {
 								timestamp = Long.parseLong(bValue.toString());
 							}
 							Date updatetime = new Date(timestamp
 									* Constants.MILLISECONDS_IN_A_SECOND);
 							SimpleDateFormat dateFormat =
 								new SimpleDateFormat("dd.MM.yyy HH:mm:ss");
 							value = dateFormat.format(updatetime);
 						} else if (bValue.getClass().isArray()
 										|| bValue instanceof Collection) {
 							Object[] arr;
 							if (bValue instanceof Collection) {
 								arr = ((Collection<?>) bValue).toArray();
 							} else {
 								arr = (Object[]) bValue;
 							}
 							for (int i = 0; i < arr.length; i++) {
 								if (arr[i] instanceof String) {
 									value = (String) arr[i];
 								} else {
 									try {
 										value = new String(getBytes(bValue));
 									} catch (IOException e) {
 										log.error("Error reading bytes for"
 												+ " attribute "
 												+ attributeName, e);
 									}
 								}
 							}
 						} else if (bValue instanceof String) {
 							value = (String) bValue;
 						} else {
 							value = bValue.toString();
 						}
 						objElement.setAttribute(attributeName, value);
 					} else {
 						objElement.setAttribute(attributeName, value);
 					}
 				}
 			}
 		}
 		Collection<CRResolvableBean> children = crBean.getChildRepository();
 		if (children != null && children.size() > 0) {
 			Element childContainer = doc.createElement("subfolders");
 			for (CRResolvableBean chBean : children) {
 				Element chElement = processElement(chBean);
 				childContainer.appendChild(chElement);
 			}
 			objElement.appendChild(childContainer);
 		}
 		return objElement;
 	}
 }
