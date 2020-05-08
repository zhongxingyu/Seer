 package name.kazennikov.annotations;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.Maps;
 
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamWriter;
 
 import name.kazennikov.xml.XmlWritable;
 
 import java.util.*;
 
 public class Document extends Annotation implements CharSequence {
 	String text;
 	
 	Map<String, List<Annotation>> annotations = Maps.newHashMap();
 	
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
 	public List<Annotation> get(String... names) {
         List<Annotation> anns = new ArrayList<Annotation>();
 
         for(String name : names) {
             if(name.equals(getName())) {
                 anns.add(this);
             }
 
             List<Annotation> ann = annotations.get(name);
             if(ann != null) {
                 anns.addAll(ann);
             }
         }
 
         Collections.sort(anns, Annotation.COMPARATOR);
 		
 		return anns;
 	}
 	
 	public List<Annotation> getAll() {
 		return getAllAnnotations();
 	}	
 	
 	/**
 	 * Get annotations that covers given span
 	 * @param start span start
 	 * @param end span end
 	 * @return
 	 */
 	public List<Annotation> getCovering(int start, int end) {
 		List<Annotation> anns = new ArrayList<Annotation>();
 		
 		for(Annotation a : getAll()) {
 			if(a.getStart() <= start && a.getEnd() >= end)
 				anns.add(a);
 		}
 		
 		return anns;
 		
 	}
 	
 	public List<Annotation> getFiltered(Predicate<Annotation> predicate) {
 		List<Annotation> anns = new ArrayList<Annotation>();
 		
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
 	public List<Annotation> getOverlapping(int start, int end) {
 		List<Annotation> anns = new ArrayList<Annotation>();
 		
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
         for(String annotationName : annotationNames) {
              boolean  res = getName().equals(annotationName) || annotations.containsKey(annotationName);
             if(res)
                 return true;
         }
 
         return false;
     }
 
 
 
     /**
      * Get set of all annotations present in the document
      * @return
      */
 	public Set<String> getAnnotationNames() {
         HashSet<String> annotNames = new HashSet<String>(annotations.keySet());
         annotNames.add(getName());
 		return annotNames;
 	}
 
     /**
      * Add single annotation to the document
      * @param ann
      */
 	public void addAnnotation(Annotation ann) {
 		//List<Annotation> anns = get(ann.getName());
 		if(ann.getName() == getName()) {
 			throw new IllegalArgumentException("Couldn't add annotation with same name as document root");
 		}
 		List<Annotation> annots = annotations.get(ann.getName());
 		
 		if(annots == null) {
 			annots = new ArrayList<Annotation>();
 			annotations.put(ann.getName(), annots);
 		}
 		
 		ann.setDoc(this);
 		annots.add(ann);
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
         for(List<Annotation> annots : annotations.values()) {
             Collections.sort(annots, Annotation.COMPARATOR);
         }
     }
 
     public void sortAnnotations(String... names) {
         for(String anName : names) {
             List<Annotation> annots = annotations.get(anName);
             if(annots == null || annots.isEmpty())
                 continue;
 
             Collections.sort(annots, Annotation.COMPARATOR);
         }
     }
 	
 	public void clearAnnotations(String name) {
 		get(name).clear();
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
 	public List<Annotation> getAnnotationsWithin(Annotation a, Predicate<Annotation> p) {
 		List<Annotation> anns = new ArrayList<Annotation>();
 		
 		for(List<Annotation> anList : annotations.values()) {
 			for(Annotation an : anList ) {
 				// skip given
 				if(an == a)
 					continue;
 				
 				if(a.contains(an) && p.apply(an))
 					anns.add(an);
 			}
 		}
 		
 		Collections.sort(anns, Annotation.COMPARATOR);
 		
 		return anns;
 	}
 	
 	/**
 	 * Get annotatations from document that satisfies a predicate
 	 * @param predicate
 	 * @return
 	 */
 	public List<Annotation> get(Predicate<Annotation> predicate) {
 		List<Annotation> anns = new ArrayList<Annotation>();
 		
 		for(Annotation a : getAll()) {
 			if(predicate.apply(a))
 				anns.add(a);
 		}
 		
 		Collections.sort(anns, Annotation.COMPARATOR);
 		
 		return anns;
 	}
 	
 	public List<Annotation> getAllAnnotations() {
 		List<Annotation> l = new ArrayList<Annotation>();
 		l.add(this);
 		for(List<Annotation> al : annotations.values()) {
 			l.addAll(al);
 		}
 		
 		return l;
 	}
 	
 
 	public void toXml(XMLStreamWriter writer, Map<String, XmlWritable<Map<String, Object>>> anWriters) throws XMLStreamException {
 		writer.writeStartElement("doc");
 		writer.writeAttribute("text", getText());
 		writer.writeAttribute("root", getName()); // get root annotation
 
 		for(Annotation a : getAll()) {
 			XmlWritable<Map<String, Object>> featWriter = anWriters != null? anWriters.get(a.getName()) : null;
 			writer.writeStartElement("annotation");
 			writer.writeAttribute("type", a.getName());
 			writer.writeAttribute("start", Integer.toString(a.getStart()));
 			writer.writeAttribute("end", Integer.toString(a.getEnd()));
 
 			if(featWriter != null) {
 				featWriter.write(writer, a.getFeatureMap());
 			} else {
				writer.writeStartElement("features");
 				for(Map.Entry<String, Object> e : a.getFeatureMap().entrySet()) {
 					if(e.getValue() != null) {
 						writer.writeStartElement("feat");
 						writer.writeAttribute("name", e.getKey());
 						writer.writeAttribute("value", e.getValue().toString());
 						writer.writeEndElement();
 					}
 				}
				writer.writeEndElement(); // features
 			}
 
 			writer.writeEndElement(); // annotation
 		}
 
 		writer.writeEndElement();
 	}
 	
 	
 }
