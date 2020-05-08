 /**
  * 
  */
 package org.purl.wf4ever.rosrs.client;
 
 import java.io.Serializable;
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.vocabulary.DCTerms;
 
 /**
  * A simplified verion of {@link com.hp.hpl.jena.rdf.model.Statement}, which unlike the original is serializable.
  * 
  * @author piotrhol
  * 
  */
 public class Statement implements Serializable {
 
     /** id. */
     private static final long serialVersionUID = 1704407898614509230L;
 
     /** Is the subject a resource with a URI, based on {@link RDFNode#isURIResource()}. */
     private final boolean isSubjectURIResource;
 
     /** The subject URI if it's a resource with URI, the String value otherwise. */
     private final String subjectValue;
 
     /** The subject URI if it's a resource with URI, null otherwise. */
     private final URI subjectURI;
 
     /** Property URI. */
     private URI propertyURI;
 
     /** Property local name, based on {@link Property#getLocalName()}. */
     private String propertyLocalName;
 
     /** Annotation that this statement belongs to. */
     private Annotation annotation;
 
     /** The object as String. If the original object is a URI reference, the URI is used. */
     private String object;
 
 
     /**
      * Constructor.
      * 
      * @param original
      *            the original {@link com.hp.hpl.jena.rdf.model.Statement}
      * @param annotation
      *            annotation this statement belongs to
      * @throws URISyntaxException
      *             some URI in the original is incorrect
      */
     public Statement(com.hp.hpl.jena.rdf.model.Statement original, Annotation annotation)
             throws URISyntaxException {
         isSubjectURIResource = original.getSubject().isURIResource();
         if (isSubjectURIResource) {
             subjectURI = new URI(original.getSubject().getURI());
             subjectValue = original.getSubject().getURI();
         } else {
             subjectURI = null;
             subjectValue = original.getSubject().asResource().getId().getLabelString();
         }
         setPropertyURI(new URI(original.getPredicate().getURI()));
         RDFNode node = original.getObject();
         if (node.isURIResource()) {
             object = node.asResource().getURI();
         } else if (node.isResource()) {
             object = original.getObject().asResource().getId().getLabelString();
         } else {
             object = original.getObject().asLiteral().getValue().toString();
         }
         this.annotation = annotation;
     }
 
 
     /**
      * Constructor for an empty statement. The default property is dcterms:description, the default value is "".
      * 
      * @param subjectURI
      *            subject URI
      * @param annotation
      *            annotation this statement belongs to
      */
     //FIXME this constructor should be deleted
     public Statement(URI subjectURI, Annotation annotation) {
         this(subjectURI, URI.create(DCTerms.description.getURI()), "");
         this.annotation = annotation;
     }
 
 
     /**
      * Constructor.
      * 
      * @param subject
      *            subject
      * @param property
      *            property
      * @param value
      *            a literal value
      */
     public Statement(URI subject, URI property, String value) {
         this.subjectURI = subject;
         subjectValue = "";
         isSubjectURIResource = false;
         setPropertyURI(property);
         object = value;
     }
 
 
     public Annotation getAnnotation() {
         return annotation;
     }
 
 
     public URI getPropertyURI() {
         return propertyURI;
     }
 
 
     public String getPropertyLocalName() {
         return propertyLocalName;
     }
 
 
     public String getPropertyLocalNameNice() {
         return Utils.splitCamelCase(getPropertyLocalName()).toLowerCase();
     }
 
 
     public String getObject() {
         return object;
     }
 
 
     public void setObject(String object) {
         this.object = object;
     }
 
 
     /**
      * Set property URI and its local name.
      * 
      * @param propertyURI
      *            property URI, not null
      */
     public void setPropertyURI(URI propertyURI) {
         if (propertyURI == null) {
             throw new NullPointerException("Property URI cannot be null");
         }
         this.propertyURI = propertyURI;
         this.propertyLocalName = ModelFactory.createDefaultModel().createProperty(propertyURI.toString())
                 .getLocalName();
     }
 
 
     public URI getSubjectURI() {
         return subjectURI;
     }
 
 
     public String getSubjectValue() {
         return subjectValue;
     }
 
 
     public boolean isSubjectURIResource() {
         return isSubjectURIResource;
     }
 
 
     /**
      * Check if the statement has the subject, predicate or object matching the given parameters. Any parameter can be
      * set to null to match everything.
      * 
      * @param subjectUri2
      *            subject to match or null
      * @param property2
      *            property to match or null
      * @param value2
      *            value to match or null
      * @return true if all non-null parameters are equal to these of this statement
      */
     public boolean matches(URI subjectUri2, URI property2, String value2) {
        if (subjectUri2 != null && !getSubjectURI().equals(subjectUri2)) {
             return false;
         }
        if (property2 != null && !propertyURI.equals(property2)) {
             return false;
         }
        if (value2 != null && !object.equals(value2)) {
             return false;
         }
         return true;
     }
 
 
     /**
      * Create a {@link com.hp.hpl.jena.rdf.model.Statement} based on this one.
      * 
      * @return a statement
      */
     public com.hp.hpl.jena.rdf.model.Statement createJenaStatement() {
         Model model = ModelFactory.createDefaultModel();
         Resource subject = model.createResource(subjectURI.toString());
         Property property = model.createProperty(propertyURI.toString());
         RDFNode objectNode = null;
         if (Utils.isAbsoluteURI(object)) {
             objectNode = model.createResource(URI.create(object).toString());
         } else {
             objectNode = model.createTypedLiteral(object);
         }
         return model.createStatement(subject, property, objectNode);
     }
 
 
     @Override
     public String toString() {
         return subjectValue + " " + propertyURI + " " + object;
     }
 
 }
