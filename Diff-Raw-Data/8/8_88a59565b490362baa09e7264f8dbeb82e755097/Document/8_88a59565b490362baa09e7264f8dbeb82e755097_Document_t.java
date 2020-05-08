 package name.kazennikov.annotations;
 
 import gnu.trove.iterator.TIntObjectIterator;
 import gnu.trove.map.hash.TIntObjectHashMap;
 
 import java.util.Collection;
 import java.util.Map;
 
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 import javax.xml.stream.XMLStreamWriter;
 
 import name.kazennikov.xml.XmlWritable;
 
 import com.google.common.base.Predicate;
 import com.google.common.base.Predicates;
 
 /**
  * Document is a representation of a text in the annotation framework.
  * It stores the text and all annotations assosiated with it
  * <p>
  * A document also extends {@link Annotation}, so it has all properties of an annotation
  * @author Anton Kazennikov
  *
  */
 public class Document extends Annotation implements CharSequence {
 
 	String text;
     TIntObjectHashMap<Annotation> annotationById = new TIntObjectHashMap<Annotation>();
 
     // annotation id of the document is always 0
     public static int DOCUMENT_ANNOTATION_ID = 0;
     
 	int nextID = 0;
 	
 	public Document() {	
 		this("");
 	}
 	
 	public Document(String text) {
 		this(AnnotationConstants.DOCUMENT, text);
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
 		addAnnotation(this);
 	}
 
 	@Override
 	public String getText() {
 		return text;
 	}
 
     /**
      * Get annotations by type
      * @param types annotations types
      * @return
      */
 	public AnnotationList get(String... types) {
         AnnotationList anns = new AnnotationList();
         TIntObjectIterator<Annotation> it = annotationById.iterator();
         
         while(it.hasNext()) {
         	it.advance();
         	Annotation a = it.value();
         	for(String type : types) {
         		if(a.getType().equals(type)) {
         			anns.add(a);
         			break;
         		}
         	}
         }
         
         anns.sort();
 		
 		return anns;
 	}
 	
 	public AnnotationList getAll() {
 		AnnotationList l = new AnnotationList(annotationById.size());
 		TIntObjectIterator<Annotation> it = annotationById.iterator();
 		
 		while(it.hasNext()) {
 			it.advance();
 			l.add(it.value());
 		}
 		
 		l.sort();
 		return l;
 	}	
 	
 	/**
 	 * Get annotations that covers given span
 	 * @param start span start
 	 * @param end span end
 	 * @return
 	 */
 	public AnnotationList getCovering(int start, int end) {
 		AnnotationList anns = new AnnotationList();
 		TIntObjectIterator<Annotation> it = annotationById.iterator();
 		
 		while(it.hasNext()) {
 			it.advance();
 			Annotation a = it.value();
 			if(a.getStart() <= start && a.getEnd() >= end)
 				anns.add(a);
 		}
 		
 		anns.sort();
 		return anns;
 		
 	}
 	
 	public AnnotationList get(Predicate<Annotation> predicate) {
 		AnnotationList anns = new AnnotationList();
 
 		TIntObjectIterator<Annotation> it = annotationById.iterator();
 		
 		while(it.hasNext()) {
 			it.advance();
 			Annotation a = it.value();
 			
 			if(predicate.apply(a))
 				anns.add(a);
 		}
 		
 		anns.sort();
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
 		
 		TIntObjectIterator<Annotation> it = annotationById.iterator();
 		
 		while(it.hasNext()) {
 			it.advance();
 			Annotation a = it.value();
 			if(a.getStart() <= start && a.getEnd() >= end)
 				anns.add(a);
 		}
 
 		anns.sort();
 		return anns;
 		
 	}
 
 
 	
 
     /**
      * Checks if document has any of this annotations
      * @param annotationTypes annotation names
      */
     public boolean contains(String... annotationTypes) {
 		TIntObjectIterator<Annotation> it = annotationById.iterator();
 		
 		while(it.hasNext()) {
 			it.advance();
 			Annotation a = it.value();
     		for(String s : annotationTypes) {
     			if(a.getType().equals(s))
     				return true;
     		}
     	}
 
         return false;
     }
 
 
 
     /**
      * Add annotation to the document.
      * An error is signaled if an annotation with same id already exists in the
      * document
      * 
      * @param ann annotation to add
      * @return added annotation
      */
 	protected Annotation addAnnotation(Annotation ann) {
 		ann.setDoc(this);
 		
		if(ann.id == Annotation.UNASSIGNED_ID) {
			ann.id = nextID;
		}
		
 		if(annotationById.contains(ann.id))
 			throw new IllegalStateException("Annotation with id=" + ann.id + " already exists in the document");
 		
 		annotationById.put(ann.id, ann);
 		nextID = Math.max(ann.id, nextID) + 1;
 		return ann;
 	}
 	
 	public Annotation addAnnotation(String name, int start, int end) {
 		Annotation a = new Annotation(this, name, start, end);
 		return addAnnotation(a);
 	}
 	
 	public Annotation addAnnotation(String name, int start, int end, Map<String, Object> features) {
 		Annotation a = new Annotation(this, name, start, end, features);
 		return addAnnotation(a);
 	}
 	
 	public Annotation addAnnotation(int id, String name, int start, int end, Map<String, Object> features) {
 		Annotation a = new Annotation(this, name, start, end, features);
 		a.id = id;
 		return addAnnotation(a);
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
 		
 		TIntObjectIterator<Annotation> it = annotationById.iterator();
 		
 		while(it.hasNext()) {
 			it.advance();
 			Annotation an = it.value();
 
 				// skip given
 				if(an == a)
 					continue;
 				
 				if(a.contains(an) && p.apply(an))
 					anns.add(an);
 		}
 		
 		anns.sort();
 		
 		return anns;
 	}
 	
 	/**
 	 * Get annotations from document that satisfies a predicate
 	 * @param predicate
 	 * @return
 	 */
 	@Override
 	public AnnotationList get(Predicate<Annotation>... predicate) {
 		
 		Predicate<Annotation> p = Predicates.and(predicate);		
 		return get(p);
 	}
 	
 	/**
 	 * Get annotations from document that satisfies a predicate
 	 * @param type
 	 * @param predicate
 	 * @return
 	 */
 	public AnnotationList get(String type, Predicate<Annotation> predicate) {
 		AnnotationList anns = new AnnotationList();
 		TIntObjectIterator<Annotation> it = annotationById.iterator();
 		
 		while(it.hasNext()) {
 			it.advance();
 			Annotation a = it.value();
 			if(a.getType().equals(type) && predicate.apply(a))
 				anns.add(a);
 		}
 		
 		anns.sort();
 		
 		return anns;
 	}
 	
 
 	public void toXml(XMLStreamWriter writer, Map<String, XmlWritable<Map<String, Object>>> anWriters) throws XMLStreamException {
 		writer.writeStartElement(AnnotationConstants.DOCUMENT);
 		writer.writeAttribute("text", getText());
 		writer.writeAttribute("type", getType()); // get root annotation
 
 		for(Annotation a : getAll()) {
 			XmlWritable<Map<String, Object>> featWriter = anWriters != null? anWriters.get(a.getType()) : null;
 			writer.writeStartElement("annotation");
 			writer.writeAttribute("type", a.getType());
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
 	 * @param stream xml stream
 	 * @param anLoaders annotation parsers
 	 * @return
 	 */
 	public static Document read(XMLStreamReader stream, Map<String, AnnotationXmlLoader> anLoaders) throws XMLStreamException {
 		String tag = stream.getName().getLocalPart();
 		if(!tag.equals(AnnotationConstants.DOCUMENT))
 			return null;
 		
 		String anDoc = stream.getAttributeValue(null, "type");
 		String text = stream.getAttributeValue(null, "text");
 		Document doc = new Document(anDoc, text);
 		
 		while(stream.hasNext()) {
 			if(stream.isEndElement() && stream.getLocalName().equals(AnnotationConstants.DOCUMENT))
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
 						doc.type = anType;
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
      * Rewrites annotations with side-effect rewrite
      * @param rewriter
      */
     public void rewrite(AnnotationRewriter rewriter) {
 
 
         for(Annotation a : getAll()) {
             a = rewriter.rewrite(a);
         }
     }
     
     public void removeIf(Predicate<Annotation> p) {
 
     	TIntObjectIterator<Annotation> it = annotationById.iterator();
 
         while(it.hasNext()) {
         	it.advance();
             Annotation a = it.value();
             if(p.apply(a)) {
                 it.remove();
             }
         }
     }
     
     public void removeIfNot(Predicate<Annotation> p) {
 
     	TIntObjectIterator<Annotation> it = annotationById.iterator();
 
         while(it.hasNext()) {
         	it.advance();
             Annotation a = it.value();
             if(!p.apply(a)) {
                 it.remove();
             }
         }
     }
   
 	public void remove(Annotation a) {
         annotationById.remove(a.getId());
 	}
 
     public Annotation getById(int id) {
         return annotationById.get(id);
     }
 
 	public void removeAll(Collection<? extends Annotation> c) {
 		for(Annotation a : c) {
 			annotationById.remove(a.getId());
 		}		
 	}
 
 	/**
 	 * Get document size in annotations
 	 * @return
 	 */
     public int size() {
     	return annotationById.size();
     }
 	
 	
 }
