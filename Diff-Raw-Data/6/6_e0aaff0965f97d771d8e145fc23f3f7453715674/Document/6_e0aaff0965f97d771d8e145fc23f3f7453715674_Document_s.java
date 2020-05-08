 package name.kazennikov.annotations;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.Maps;
 import name.kazennikov.xml.XmlWritable;
 
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 import javax.xml.stream.XMLStreamWriter;
 import java.util.*;
 
 public class Document extends Annotation implements CharSequence {
 	String text;
 	
 	//Map<String, List<Annotation>> annotations = Maps.newHashMap();
 	AnnotationList annotations = new AnnotationList();
 	
 	public Document() {	
 		this("");
 	}
 	
 	public Document(String text) {
 		this("doc", text);
 	}
 
     /**
      * Construct a document with root annotation and given text
      * @param annotName global document annotation
      * @param text document text
      */
 	public Document(String annotName, String text) {
 		super(null, annotName, 0, text.length());
 		this.text = text;
 		setDoc(this);
 		annotations.add(this);
 	}
 
 	@Override
 	public String getText() {
 		return text;
 	}
 
     /**
      * Get annotations by name
      * @param names annotations names
      * @return
      */
 	public AnnotationList get(String... names) {
         AnnotationList anns = new AnnotationList();

        for(String name : names) {
            if(name.equals(getName())) {
                anns.add(this);
            }
        }
         
         for(Annotation a : annotations) {
         	for(String name : names) {
         		if(a.getName().equals(name)) {
         			anns.add(a);
         			break;
         		}
         	}
         }
 
         Collections.sort(anns, Annotation.COMPARATOR);
 		
 		return anns;
 	}
 	
 	public AnnotationList getAll() {
 		return annotations;
 	}	
 	
 	/**
 	 * Get annotations that covers given span
 	 * @param start span start
 	 * @param end span end
 	 * @return
 	 */
 	public AnnotationList getCovering(int start, int end) {
 		AnnotationList anns = new AnnotationList();
 		
 		for(Annotation a : getAll()) {
 			if(a.getStart() <= start && a.getEnd() >= end)
 				anns.add(a);
 		}
 		
 		return anns;
 		
 	}
 	
 	public AnnotationList getFiltered(Predicate<Annotation> predicate) {
 		AnnotationList anns = new AnnotationList();
 		
 		for(Annotation a : getAll()) {
 			if(predicate.apply(a))
 				anns.add(a);
 		}
 		
 		return anns;
 	}
 	
 	/**
 	 * Get all annotations that overlaps with given span
 	 * @param start
 	 * @param end
 	 * @return
 	 */
 	public AnnotationList getOverlapping(int start, int end) {
 		AnnotationList anns = new AnnotationList();
 		
 		for(Annotation a : getAll()) {
 			if(a.getStart() <= start && a.getEnd() >= end)
 				anns.add(a);
 		}
 		
 		return anns;
 		
 	}
 
 
 	
 
     /**
      * Checks if document has any of this annotations
      * @param annotationNames annotation names
      */
     public boolean contains(String... annotationNames) {
     	for(Annotation a : annotations) {
     		for(String s : annotationNames) {
     			if(a.getName().equals(s))
     				return true;
     		}
     	}
 
         return false;
     }
 
 
 
     /**
      * Add single annotation to the document
      * @param ann
      */
 	public void addAnnotation(Annotation ann) {
 		ann.setDoc(this);
 		annotations.add(ann);
 	}
 	
 	public void addAnnotation(String name, int start, int end) {
 		Annotation a = new Annotation(this, name, start, end);
 		addAnnotation(a);
 	}
 	
 	public void addAnnotation(String name, int start, int end, Map<String, Object> features) {
 		Annotation a = new Annotation(this, name, start, end);
 		a.features = features;
 		addAnnotation(a);
 	}
 
     /**
      * Add all annotations from annotation list
      * @param ans
      */
 	public void addAnnotations(Collection<? extends Annotation> ans) {
 		for(Annotation a : ans) {
 			addAnnotation(a);
 		}
 	}
 
     public void sortAnnotations() {
     	Collections.sort(annotations, Annotation.COMPARATOR);
     }
 
 	@Override
 	public int length() {
 		return text.length();
 	}
 
 	@Override
 	public char charAt(int index) {
 		return text.charAt(index);
 	}
 
 	@Override
 	public CharSequence subSequence(int start, int end) {
 		return text.subSequence(start, end);
 	}
 	
 	/**
 	 * Get annotations that are constrained by span of given annotation and properties of valid annotations
 	 * @param a constrain span of the output annotation
 	 * @param p select predicate for constrained annotations
 	 * @return list of annotations
 	 */
 	public AnnotationList getAnnotationsWithin(Annotation a, Predicate<Annotation> p) {
 		AnnotationList anns = new AnnotationList();
 		
 		for(Annotation an : annotations) {
 				// skip given
 				if(an == a)
 					continue;
 				
 				if(a.contains(an) && p.apply(an))
 					anns.add(an);
 		}
 		
 		Collections.sort(anns, Annotation.COMPARATOR);
 		
 		return anns;
 	}
 	
 	/**
 	 * Get annotations from document that satisfies a predicate
 	 * @param predicate
 	 * @return
 	 */
 	public AnnotationList get(Predicate<Annotation> predicate) {
 		AnnotationList anns = annotations.get(predicate);
 		Collections.sort(anns, Annotation.COMPARATOR);
 		return anns;
 	}
 	
 	/**
 	 * Get annotatations from document that satisfies a predicate
 	 * @param type
 	 * @param predicate
 	 * @return
 	 */
 	public AnnotationList get(String type, Predicate<Annotation> predicate) {
 		AnnotationList anns = new AnnotationList();
 		
 		for(Annotation a : anns) {
 			if(a.getName().equals(type) && predicate.apply(a))
 				anns.add(a);
 		}
 		
 		Collections.sort(anns, Annotation.COMPARATOR);
 		
 		return anns;
 	}
 	
 	public AnnotationList getAllAnnotations() {
 		return annotations;
 	}
 	
 
 	public void toXml(XMLStreamWriter writer, Map<String, XmlWritable<Map<String, Object>>> anWriters) throws XMLStreamException {
 		writer.writeStartElement(DOC);
 		writer.writeAttribute("text", getText());
 		writer.writeAttribute("type", getName()); // get root annotation
 
 		for(Annotation a : getAll()) {
 			XmlWritable<Map<String, Object>> featWriter = anWriters != null? anWriters.get(a.getName()) : null;
 			writer.writeStartElement("annotation");
 			writer.writeAttribute("type", a.getName());
 			writer.writeAttribute("start", Integer.toString(a.getStart()));
 			writer.writeAttribute("end", Integer.toString(a.getEnd()));
 
 			if(featWriter != null) {
 				featWriter.write(writer, a.getFeatureMap());
 			} else {
 
 				for(Map.Entry<String, Object> e : a.getFeatureMap().entrySet()) {
 					if(e.getValue() != null) {
 						writer.writeStartElement(e.getKey());
 						writer.writeCharacters(e.getValue().toString());
 						writer.writeEndElement();
 					}
 				}
 			}
 
 			writer.writeEndElement(); // annotation
 		}
 
 		writer.writeEndElement();
 	}
 	
 	
 	private static AnnotationXmlLoader BASE_LOADER = new AnnotationXmlLoader.Base();
 	/**
 	 * Reads document from STAX stream
 	 * @param reader xml stream
 	 * @param anLoaders annotation parsers
 	 * @return
 	 */
 	public static Document read(XMLStreamReader stream, Map<String, AnnotationXmlLoader> anLoaders) throws XMLStreamException {
 		String tag = stream.getName().getLocalPart();
 		if(!tag.equals(DOC))
 			return null;
 		
 		String anDoc = stream.getAttributeValue(null, "root");
 		String text = stream.getAttributeValue(null, "text");
 		Document doc = new Document(anDoc, text);
 		
 		while(stream.hasNext()) {
 			if(stream.isEndElement() && stream.getLocalName().equals(DOC))
 				break;
 			
 			if(stream.isStartElement()) {
 				String ctag = stream.getLocalName();
 				if(ctag.equals("annotation")) {
 					String anType = stream.getAttributeValue(null, "type");
 					AnnotationXmlLoader loader = anLoaders.get(anType);
 					if(loader == null)
 						loader = BASE_LOADER;
 					Annotation a = loader.load(stream);
 					if(anType.equals(anDoc)) {
 						// load root annotation
 						doc.name = anType;
 						doc.features = a.features;
 						doc.data = a.data;
 					} else {
 						// load other annotations
 						doc.addAnnotation(a);
 					}
 				}
 			}
 			
 			stream.next();
 		}
 		
 		return doc;
 	}
 
     /**
      * Rewrites annotations with side-effect rewriter
      * @param rewriter
      */
     public void rewrite(AnnotationRewriter rewriter) {
 
 
         for(Annotation a : getAll()) {
             a = rewriter.rewriter(a);
         }
     }
     
     public void removeIf(Predicate<Annotation> p) {
     	annotations.removeIf(p);
     }
     
     public void removeIfNot(Predicate<Annotation> p) {
     	annotations.removeIfNot(p);
     }
 
     
 	
 	
 }
