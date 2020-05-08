 package org.weso.wesearch.model;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.weso.utils.OntoModelException;
 import org.weso.wesearch.domain.Matter;
 import org.weso.wesearch.domain.Matters;
 import org.weso.wesearch.domain.Properties;
 import org.weso.wesearch.domain.Property;
 import org.weso.wesearch.domain.ValueSelector;
 import org.weso.wesearch.domain.impl.JenaPropertyImpl;
 import org.weso.wesearch.domain.impl.MatterImpl;
 import org.weso.wesearch.domain.impl.PropertiesImpl;
 import org.weso.wesearch.domain.impl.SubjectsImpl;
 
 import com.hp.hpl.jena.ontology.OntClass;
 import com.hp.hpl.jena.ontology.OntModel;
 import com.hp.hpl.jena.ontology.OntProperty;
 import com.hp.hpl.jena.ontology.OntResource;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.rdf.model.Statement;
 import com.hp.hpl.jena.util.iterator.ExtendedIterator;
 import com.hp.hpl.jena.vocabulary.RDFS;
 
 /**
  * This is an auxiliary class that allow wesearch work with ontologies using
  * Apache Jena.
  * @author Ignacio Fuertes Bernardo
  *
  */
 public class OntologyHelper {
 	
 	private static Logger logger = Logger.getLogger(OntologyHelper.class);
 	
 	/**
 	 * This method creates a matter from a uri and a jena model
 	 * @param resource The uri of the resource
 	 * @param model The model where is the resource
 	 * @return The created matter
 	 */
 	public static Matter createMatter(String resource, OntModel model) {
 		String label = getLabel(resource, model);
 		String comment = getComment(resource, model);
 		return new MatterImpl(label, resource, comment);
 	}
 	
 	/**
 	 * This method creates a matter from a jena resource
 	 * @param res The resource to create the matter
 	 * @return The created matter
 	 */
 	public static Matter createMatter(Resource res) {
 		String uri = res.getURI();
 		String label = getLabel(res);
 		String comment = getComment(res);
 		
 		return new MatterImpl(label, uri, comment);
 	}
 	
 	/**
 	 * This method obtains the property rdfs:label from a Jena resource
 	 * @param res The resource to obtain the property rdfs:label
 	 * @return The value of property rdfs:label
 	 */
 	public static String getLabel(Resource res) {
 		if(res instanceof OntResource) {
 			return getLabelFromOntResource(res);
 		} else {
 			 return getLabelFromResource(res);
 		}
 		
 	}
 
 	/**
 	 * This method obtains the property rdfs:label from a Jena resource
 	 * @param res The resource to obtain the property rdfs:label
 	 * @return The value of property rdfs:label
 	 */
 	private static String getLabelFromResource(Resource res) {
 		Statement labelProp = res.getProperty(RDFS.label);
 		 if (labelProp != null && labelProp.getString().length() > 0) {
 		     return labelProp.getString();
 		 } else {
 		     return (res.getURI()!=null?res.getModel().getGraph()
 		                 .getPrefixMapping().shortForm(res.getURI()):
 		                	 "Label not available");
 		 }
 	}
 
 	/**
 	 * This method obtains the property rdfs:label from a jena OntResource
 	 * using xml:lang attribute.
 	 * @param res The resource to obtain the property rdfs:label
 	 * @return The value of the property rdfs:label
 	 */
 	private static String getLabelFromOntResource(Resource res) {
 		OntResource resource = ((OntResource)res);
 		if(resource.getLabel("es") != null) {
 			return resource.getLabel("es");
 		} else if (resource.getLabel("en") != null) {
 			return resource.getLabel("en");
 		} else if (resource.getLabel(null) != null) {
 			return resource.getLabel(null);
 		} else {
 			return (res.getURI()!=null?res.getModel().getGraph()
 		    		.getPrefixMapping().shortForm(res.getURI()):
 		    			"Label not available");
 		}
 	}
 	
 	/**
 	 * This method obtains the property rdfs:comment from a Jena resource
 	 * @param res The resource to obtain the property rdfs:comment
 	 * @return The value of property rdfs:comment
 	 */
 	public static String getComment(Resource res) {
 		if(res instanceof OntResource) {
 			return getCommentFromOntResource(res);
 		} else {
 			return getCommentFromResource(res);
 		}		
 	}
 
 	/**
 	 * This method obtains the property rdfs:comment from a Jena resource
 	 * @param res The resource to obtain the property rdfs:comment
 	 * @return The value of property rdfs:comment
 	 */
 	private static String getCommentFromResource(Resource res) {
 		Statement labelProp = res.getProperty(RDFS.comment);
 		if (labelProp != null && labelProp.getString().length() > 0) {
 		    return labelProp.getString();
 		} else {
 		    return (res.getURI()!=null?res.getModel().getGraph()
 		    		.getPrefixMapping().shortForm(res.getURI()):
 		    			"Comment not available");
 		}
 	}
 
 	/**
 	 * This method obtains the property rdfs:comment from a jena OntResource
 	 * using xml:lang attribute.
 	 * @param res The resource to obtain the property rdfs:comment
 	 * @return The value of the property rdfs:comment
 	 */
 	private static String getCommentFromOntResource(Resource res) {
 		OntResource ontResource = (OntResource)res;
 		if(ontResource.getComment("es") != null) {
 			return ontResource.getComment("es");
 		} else if (ontResource.getComment("en") != null) {
 			return ontResource.getComment("en");
 		} else if (ontResource.getComment(null) != null) {
 			return ontResource.getComment(null);
 		} else {
 			 return (res.getURI()!=null?res.getModel().getGraph()
 		        		.getPrefixMapping().shortForm(res.getURI()):
 		        			"Comment not available");
 		}
 	}
 	
 	/**
 	 * This method obtains the property rdfs:label from the URI of a Jena 
 	 * resource
 	 * @param resource The URI of the resource to obtain the property rdfs:label
 	 * @param model The ontology model to obtain the property value
 	 * @return The value of property rdfs:label
 	 */
 	public static String getLabel(String resource, OntModel model) {
 		OntClass res = model.getOntClass(resource);
 		if(res == null) {
 			return "Label not available";
 		}
 		return getLabel(res);
 	}
 	
 	/**
 	 * This method obtains the property rdfs:comment from the URI of a Jena 
 	 * resource
 	 * @param resource The URI of the resource to obtain the property 
 	 * rdfs:comment
 	 * @param model The ontology model to obtain the value
 	 * @return The value of property rdfs:comment
 	 */
 	public static String getComment(String resource, OntModel model) {
 		OntClass res = model.getOntClass(resource);
 		if(res == null) {
 			return "Comment not available";
 		}
 		return getComment(res);
 	}
 
 	/**
 	 * This method obtains all properties of one class of the ontologies
 	 * @param ontClass The class to obtain all its properties
 	 * @param superClasses A list of super-classes of the class to obtain all 
 	 * inherited properties
 	 * @return A collection of all properties of one class
 	 */
 	public static Properties obtainPropertiesByMatter(OntClass ontClass,
 			ExtendedIterator<OntClass> superClasses) {
 		Properties properties = new PropertiesImpl();
 		extractPropertiesFromOntClass(properties, ontClass);
 		while(superClasses.hasNext()) {
 			OntClass auxOntClass = superClasses.next();
 			extractPropertiesFromOntClass(properties, auxOntClass);
 		}
 		
 		return properties;
 	}
 
 	/**
 	 * This method has to extract all properties of one class and insert them 
 	 * in a collection
 	 * @param properties The collection in which the method has to insert the 
 	 * properties
 	 * @param ontClass The class to extract all its properties
 	 */
 	public static void extractPropertiesFromOntClass(Properties properties,
 			OntClass ontClass) {
 		ExtendedIterator<OntProperty> ontProperties = 
 				ontClass.listDeclaredProperties();
 		while(ontProperties.hasNext()) {
 			OntProperty ontProp = ontProperties.next();
 			properties.addProperty(createProperty(ontProp));
 		}
 	}
 	
 	/**
 	 * This method has to create a property object from a Jena resource
 	 * @param res The resource from which the method has to create the property 
 	 * object
 	 * @return The created Property
 	 */
 	public static Property createProperty(Resource res) {
 		String uri = res.getURI();
 		String label = getLabel(res);
 		String description = getComment(res);
 		return new JenaPropertyImpl(uri, label, description);
 	}
 
 	/**
 	 * Given an OntProperty object, the method has to obtains its range.
 	 * @param ontProperty The given property to obtain its range
 	 * @return A String indicating the range of the property
 	 */
 	@SuppressWarnings("rawtypes")
 	public static String extractPropertyRange(OntProperty ontProperty) {
 		if(ontProperty != null) {
 			ExtendedIterator ranges = null;
 			if(ontProperty.isDatatypeProperty()) {
 				ranges = ontProperty.asDatatypeProperty().listRange();
 				if(ranges != null) {
 					return extractValueSelectorFromList(ranges);
 				}
 			} else if(ontProperty.isObjectProperty()) {
 				return ValueSelector.OBJECT;
 			}
 		}		
 		return ValueSelector.UNDEFINED;
 	}
 	
 	/**
 	 * This method has to extract the value selector according to a list of
 	 * ontology resources.
 	 * @param it The list of ontology resources
 	 * @return The string corresponding to the value selector of the list of
 	 * ontolgy resources
 	 */
 	@SuppressWarnings("rawtypes")
 	private static String extractValueSelectorFromList(
 			ExtendedIterator it) {
 		String result = null;
 		while(it.hasNext()) {
 			OntResource ont = (OntResource)it.next();
 			result = extractValueSelector(ont);
 			if(result != null && !result.equals(ValueSelector.UNDEFINED)) {
 				return result;
 			}
 		}
 		return (result==null)?ValueSelector.UNDEFINED:result;
 	}
 
 	/**
 	 * This method extract the value selector of one ontolgy resource
 	 * @param range The ontology resource to obtain its value selector
 	 * @return A string indicates the value selector
 	 */
 	private static String extractValueSelector(OntResource range) {
 		if(range == null) {
 			return ValueSelector.UNDEFINED;
 		}
 		String uriRange = range.getURI();
 		if(isDateRange(uriRange)) {
 			return ValueSelector.DATE;
 		} else if (isNumericRange(uriRange)) {
 			return ValueSelector.NUMERIC;
 		} else if (isTextRange(uriRange)) {
 			return ValueSelector.TEXT;
 		} 
 		return ValueSelector.UNDEFINED;
 	}
 
 	/**
 	 * This method has to return if the range of a property is a text or no
 	 * @param uriRange The range of a property
 	 * @return A boolean indicates if the range must be a string or no
 	 */
 	private static boolean isTextRange(String uriRange) {
 		return uriRange.equals("http://www.w3.org/2001/XMLSchema#string") || 
 				uriRange.equals(
 						"http://www.w3.org/2000/01/rdf-schema#Literal");
 	}
 
 	/**
 	 * This method has to return if the range of a property is numeric or no
 	 * @param uriRange The range of a property
 	 * @return A boolean indicates if the range must be a number or no
 	 */
 	private static boolean isNumericRange(String uriRange) {
 		return uriRange.equals("http://www.w3.org/2001/XMLSchema#decimal") 
 				|| uriRange.equals("http://www.w3.org/2001/XMLSchema#float") || 
 				uriRange.equals("http://www.w3.org/2001/XMLSchema#double") || 
 				uriRange.equals("http://www.w3.org/2001/XMLSchema#long") ||
 				uriRange.equals("http://www.w3.org/2001/XMLSchema#integer") || 
 				uriRange.equals("http://www.w3.org/2001/XMLSchema#int");
 	}
 
 	/**
 	 * This method has to return if the range is of date type
 	 * @param uriRange The range of a property
 	 * @return A boolean indicates if the range must be a date or no
 	 */
 	private static boolean isDateRange(String uriRange) {
 		return uriRange.equals("http://www.w3.org/2001/XMLSchema#date") || 
 				uriRange.equals("http://www.w3.org/2001/XMLSchema#dateTime") || 
 				uriRange.equals("http://www.w3.org/2001/XMLSchema#time");
 	}
 
 	/**
 	 * This method has to create a collection of Matter objects that represents
 	 * the range of a resource when its range is a class of the ontology
 	 * @param listRange A list of classes of the ontology that can be range of
 	 * a property
 	 * @return A colleciont of Matter objects
 	 * @throws OntoModelException This exception is thrown if there are some 
 	 * problem with the ontology
 	 */
 	public static Matters createRangeMatters(
 			ExtendedIterator<? extends OntResource> listRange) 
 					throws OntoModelException {
 		if(listRange == null) {
 			logger.error("The list of ranges cannot be null");
 			throw new OntoModelException("The list of ranges cannot be null");
 		}
 		Matters matters = new SubjectsImpl();
 		while(listRange.hasNext()) {
 			OntResource res = listRange.next();
			Matter m = new MatterImpl(getLabel(res), res.getURI(), 
					getComment(res));
			matters.addMatter(m);
 		}
 		return matters;
 	}
 	
 	/**
 	 * This method has to extracts all subclasses of one class of the ontology
 	 * @param matter The class of the ontolgy that the method has to extract 
 	 * its subclasses
 	 * @param model The ontology model from which the method has to obtain 
 	 * the subclasses
 	 * @return A list of URIs of the subclasses
 	 */
 	public static List<String> extractSubclasses(Matter matter, 
 			OntModel model) {
 		List<String> result = new LinkedList<String>();
 		OntClass ontClass = model.getOntClass(matter.getUri());
 		if(ontClass != null) {
 			ExtendedIterator<OntClass> it = ontClass.listSubClasses();
 			while(it.hasNext()) {
 				OntClass aux = it.next();
 				result.add(aux.getURI());
 			}
 		}
 		return result;
 	}
 
 }
