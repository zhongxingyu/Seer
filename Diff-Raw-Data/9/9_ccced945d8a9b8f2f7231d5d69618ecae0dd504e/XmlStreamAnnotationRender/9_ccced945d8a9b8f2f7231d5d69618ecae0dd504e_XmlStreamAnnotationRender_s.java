 package name.kazennikov.annotations;
 
import java.util.Map;

 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamWriter;
 
 
 public class XmlStreamAnnotationRender extends AbstractAnnotationRender {
 	XMLStreamWriter writer;
 	
 	public XmlStreamAnnotationRender(XMLStreamWriter writer) {
 		this.writer = writer;
 	}
 
 	@Override
 	public void onStartContent() throws AnnotationRenderException {
 	}
 
 	@Override
 	public void onEndContent() throws AnnotationRenderException {
 	}
 
 	@Override
 	public void onAnnotationStart(Annotation annotation) throws AnnotationRenderException {
 		try {
 			writer.writeStartElement(annotation.getName());
 			Map<String, Object> featMap = annotation.getFeatureMap();
 			for(Map.Entry<String, Object> feat : featMap.entrySet()) {
				writer.writeAttribute(feat.getKey(), feat.getValue().toString());
 			}
 		} catch (XMLStreamException e) {
 			throw new AnnotationRenderException(e);
 		}
 	}
 
 	@Override
 	public void onAnnotationEnd(Annotation annotation) throws AnnotationRenderException {
 		try {
 			writer.writeEndElement();
 		} catch (XMLStreamException e) {
 			throw new AnnotationRenderException(e);
 		}
 	}
 
 	@Override
 	public void onText(String text) throws AnnotationRenderException {
 		try {
 			writer.writeCharacters(text);
 		} catch (XMLStreamException e) {
 			throw new AnnotationRenderException(e);
 		}
 	}
 }
