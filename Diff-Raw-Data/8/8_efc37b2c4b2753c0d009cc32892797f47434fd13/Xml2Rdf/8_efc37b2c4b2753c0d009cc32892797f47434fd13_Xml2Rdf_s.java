 package org.virtual.sr.transforms;
 
 import static javax.xml.stream.XMLStreamConstants.*;
 
 import java.net.URI;
 
 import javax.xml.namespace.QName;
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamReader;
 import javax.xml.transform.Source;
 
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.Resource;
 
 /**
  * Converts XML data into RDF data.
  * 
  * @author Fabio Simeoni
  *
  */
 public class Xml2Rdf {
 
 	private static final String pseudoNS = "s:r/";
 
 	static XMLInputFactory factory = XMLInputFactory.newInstance(); 
 	
 	private XMLStreamReader reader;
 	private Model model;
 	
 	static {
 		factory.setProperty(XMLInputFactory.IS_COALESCING, true);
 	}
 	
 	/**
 	 * Converts XML data into RDF data.
 	 * @param xml the XML data
 	 * @return the RDF conversion
 	 * @throws Exception if the data cannot not be converted
 	 */
 	public Model triplify(Source xml) throws Exception {
 		
 		try {
 			
 			reader = factory.createXMLStreamReader(xml); 
 			
 			model = ModelFactory.createDefaultModel();
 			
 			mapRoot();
 			
 		}
 		catch(Exception e) {
 			throw new Exception("cannot triplify xml source (see cause) ",e);
 		}
 		
 		return model;
 	}
 	
 	
 	//helpers
 	
 	private void mapRoot() throws Exception {
 		
 		reader.nextTag(); //move to root
 		
 		mapComplex(null);
 	}
 	
 	private URI mapComplex(URI parentUri) throws Exception {
 		
 		QName name = reader.getName();
 		URI uri = mint(name);
 		
 		if (parentUri==null) //root case
 			parentUri = uri; 
 		
 		boolean complex = false;
 		
 		for  (int i = 0;i< reader.getAttributeCount();i++) {
 			complex=true;
 			emit(uri,reader.getAttributeName(i),reader.getAttributeValue(i));
 		}
 		
 		loop: while (reader.hasNext()) {
 			
 			int next = reader.next();
 			
 			switch (next) {
 				
 				case START_ELEMENT: //child of this element
 					
 					QName childName = reader.getName();
 					URI childURI = mapComplex(uri); //recursion
 		            
 					if (childURI!=null)
 		            	emit(uri,childName,childURI); //link parent to child
 					
 					complex=true;
 					
 					break;
 				
 				case CHARACTERS: //content of this element
 					
					emit(complex?uri:parentUri,name,reader.getText());
 					
 					break;
 				
 				case END_ELEMENT: //end of this element
 					break loop;
 			}
 		}
 		
 		return complex?uri:null;
 	}
 	
 	void emit(URI source,QName predicate,String literal) {
 		//System.out.println(source+"->"+predicate+"->"+literal);
 		Resource s = model.createResource(source.toString());
 		Property p = model.createProperty(pseudoNS,predicate.getLocalPart());
 		model.add(s,p,literal);
 	}
 	
 	void emit(URI source,QName predicate,URI target) {
 		//System.out.println(source+"->"+predicate+"->"+target);
 		Resource s = model.createResource(source.toString());
 		Property p = model.createProperty(pseudoNS,predicate.getLocalPart());
 		Resource o = model.createResource(target.toString());
 		model.add(s, p, o);
 	}
 	
 	URI mint(QName name) throws Exception {
		return URI.create(pseudoNS+name.getLocalPart());
 	}
 	
 //	
 //	public static void main(String[] args) throws Exception {
 //		
 //		String xml = "<t:root xmlns:t='http://acme.org/' t:ra='r'><t:c1>text</t:c1><t:c2 t:attr='a'/><t:c3><t:c4>text2</t:c4></t:c3></t:root>";
 //		
 //		Source source = new StreamSource(new ByteArrayInputStream(xml.getBytes()));
 //		
 //		Model model = new Xml2Rdf().triplify(source);
 //		
 //		model.write(System.out);
 //	}
 	
 }
