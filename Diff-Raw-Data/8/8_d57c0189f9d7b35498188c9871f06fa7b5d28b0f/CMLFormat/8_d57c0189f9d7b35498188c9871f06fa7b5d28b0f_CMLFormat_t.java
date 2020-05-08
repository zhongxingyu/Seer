 package org.corewall.geology.formats;
 
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.net.URL;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.corewall.data.formats.AbstractFormat;
 import org.corewall.geology.models.Image;
 import org.corewall.scene.Orientation;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import com.google.inject.internal.Lists;
 import com.google.inject.internal.Maps;
 import com.google.inject.internal.Sets;
 
 /**
  * Reads image models from the CML file format.
  * 
  * @author Josh Reed (jareed@andrill.org)
  */
 public class CMLFormat extends AbstractFormat<Image> {
 	/**
 	 * A SAX handler for Corelyzer's CML format.
 	 */
 	private static class CMLHandler extends DefaultHandler {
 		protected List<Map<String, String>> models = Lists.newArrayList();
 		protected String session = null;
 		protected String track = null;
 		protected Set<String> tracks = Sets.newHashSet();
 
 		@Override
 		public void startElement(final String uri, final String localName, final String qName,
 				final Attributes attributes) throws SAXException {
 			// build our base model
 			Map<String, String> model = Maps.newHashMap();
 			for (int i = 0; i < attributes.getLength(); i++) {
 				String key = attributes.getQName(i);
 				String value = attributes.getValue(i);
 				if ("depth".equals(key)) {
 					model.put("top", value);
 				} else if ("dpi_x".equals(key)) {
 					model.put("dpiX", value);
 				} else if ("dpi_y".equals(key)) {
 					model.put("dpiY", value);
 				} else if ("urn".equals(key)) {
 					model.put("path", value);
 				} else if ("orientation".equals(key)) {
 					if (value.toLowerCase().charAt(0) == 'p') { // portrait
 						model.put("orientation", Orientation.VERTICAL.toString());
 					} else {
 						model.put("orientation", Orientation.HORIZONTAL.toString());
 					}
 				} else if ("length".equals(key)) {
 					BigDecimal d = new BigDecimal(value);
 					if (d.doubleValue() > 0.0) {
 						model.put("length", value);
 					}
 				} else {
 					model.put(key, value);
 				}
 			}
 			if (!model.containsKey("type")) {
 				model.put("type", qName);
 			}
 			models.add(model);
 
 			// handle some special cases
 			String type = attributes.getValue("type");
 			if ("session".equals(qName)) {
 				session = attributes.getValue("name");
 			} else if ("track".equals(type)) {
 				track = attributes.getValue("name");
 				tracks.add(track);
 			} else if ("visual".equals(qName)) {
 				if (session != null) {
 					model.put("session", session);
 				}
 				if (track != null) {
 					model.put("track", track);
 				}
 			}
 		}
 	}
 
 	private static final Logger LOGGER = LoggerFactory.getLogger(CMLFormat.class);
 
 	/**
 	 * Create a new CMLFormat.
 	 * 
 	 * @param id
 	 *            the format id.
 	 */
 	public CMLFormat(final String id) {
 		super(id, null, Image.factory());
 	}
 
 	public List<Map<String, String>> getRaw(final URL url) throws IOException {
		List<Map<String, String>> models = Lists.newArrayList();
 		CMLHandler handler = new CMLHandler();
 		try {
 			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
 			handler = new CMLHandler();
 			parser.parse(url.openStream(), handler);
			models.addAll(handler.models);
 		} catch (ParserConfigurationException e) {
 			LOGGER.error("No SAX parser", e);
 			throw new RuntimeException("No SAX parser", e);
 		} catch (SAXException e) {
 			LOGGER.error("XML parsing error", e);
 		} catch (IOException e) {
 			LOGGER.error("I/O error", e);
 			throw new RuntimeException("I/O error", e);
 		}
		return models;
 	}
 }
