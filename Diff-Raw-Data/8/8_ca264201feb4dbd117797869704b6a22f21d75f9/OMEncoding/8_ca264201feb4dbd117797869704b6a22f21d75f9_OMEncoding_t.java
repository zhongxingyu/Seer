 package org.uncertweb.ps.encoding.xml;
 
 import java.io.ByteArrayInputStream;
 import java.util.Iterator;
 
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.Namespace;
 import org.jdom.filter.Filter;
 import org.jdom.input.SAXBuilder;
 import org.jdom.output.XMLOutputter;
 import org.uncertweb.api.om.io.XBObservationEncoder;
 import org.uncertweb.api.om.io.XBObservationParser;
 import org.uncertweb.api.om.observation.AbstractObservation;
 import org.uncertweb.api.om.observation.collections.IObservationCollection;
 import org.uncertweb.ps.encoding.EncodeException;
 import org.uncertweb.ps.encoding.ParseException;
 
 public class OMEncoding extends AbstractXMLEncoding {
 
 	public boolean isSupportedClass(Class<?> classOf) {
 		for (Class<?> interf : classOf.getInterfaces()) {
 			if (interf.equals(IObservationCollection.class)) {
 				return true;
 			}
 		}
 		Class<?> superClass = classOf.getSuperclass();
 		if (superClass != null) {
 			return superClass.equals(AbstractObservation.class);
 		}
 		return false;
 	}
 
 	public Object parse(Element element, Class<?> classOf) throws ParseException {
 		try {
 			// FIXME: workaround for broken parser
 			Iterator<?> fois = element.getDescendants(new Filter() {
 				private static final long serialVersionUID = 1L;
 
 				public boolean matches(Object obj) {
 					if (obj instanceof Element) {
 						Element e = (Element)obj;
 						return e.getName().equals("featureOfInterest");
 					}
 					return false;
 				}				
 			});
 			
 			while (fois.hasNext()) {
 				Element e = (Element)fois.next();			
				Element ssf = e.getChild("SF_SpatialSamplingFeature", Namespace.getNamespace("http://www.opengis.net/samplingSpatial/2.0"));
				if (ssf != null) {
					Element shape = ssf.getChild("shape", Namespace.getNamespace("http://www.opengis.net/samplingSpatial/2.0"));
					shape.removeAttribute("type");
				}
 			}			
 			
 			// convert to string for external parsing
 			String om = new XMLOutputter().outputString(element);
 			XBObservationParser parser = new XBObservationParser();			
 			if (element.getName().endsWith("Collection")) {
 				return parser.parseObservationCollection(om);
 			}
 			else {
 				return parser.parseObservation(om);
 			}
 		}
 		catch (Exception e) {
 			throw new ParseException("Couldn't parse O&M: " + e.getMessage(), e);
 		}
 	}
 
 	public Element encode(Object object) throws EncodeException {
 		try {
 			// generate random char
 			// to ensure we are encoding valid o&m, some instances will have multiple collections in one document
 			char idPrefix = (char) (System.nanoTime() % 26 + 'a');
 			
 			// and encode
 			XBObservationEncoder encoder = new XBObservationEncoder();
 			String om;
 			if (object instanceof IObservationCollection) {
 				om = encoder.encodeObservationCollectionWithId((IObservationCollection) object, String.valueOf(idPrefix));
 			}
 			else {
 				om = encoder.encodeObservationWithId((AbstractObservation) object, String.valueOf(idPrefix));
 			}
 			Document document = new SAXBuilder().build(new ByteArrayInputStream(om.getBytes()));
 			return document.getRootElement();
 		}
 		catch (Exception e) {
 			throw new EncodeException("Couldn't encode O&M: " + e.getMessage(), e);
 		}
 	}
 
 	public String getNamespace() {
 		return "http://www.opengis.net/om/2.0";
 	}
 
 	public String getSchemaLocation() {
 		return "http://52north.org/schema/geostatistics/uncertweb/Profiles/OM/UncertWeb_OM.xsd";
 	}
 
 	public Include getIncludeForClass(Class<?> classOf) {
 		return new IncludeRef("OM_" + classOf.getSimpleName());
 	}
 
 
 
 }
