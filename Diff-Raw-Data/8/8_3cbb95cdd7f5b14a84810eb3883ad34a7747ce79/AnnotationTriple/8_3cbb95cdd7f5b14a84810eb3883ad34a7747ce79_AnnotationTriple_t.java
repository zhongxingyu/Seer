 package org.purl.wf4ever.rosrs.client;
 
 import java.io.Serializable;
 import java.net.URI;
 
 import org.purl.wf4ever.rosrs.client.exception.ROSRSException;
 
 import com.hp.hpl.jena.rdf.model.Property;
 
 /**
  * Represents a triple in an annotation (a quad), for example <x> rfds:comment <y> in annotation <z>.
  * 
  * @author piotrekhol
  * 
  */
 public class AnnotationTriple implements Serializable {
 
     /** id. */
     private static final long serialVersionUID = 889959786166231224L;
 
     /** The annotation containing the triple. */
     private final Annotation annotation;
 
     /** The subject. */
     private final Annotable subject;
 
     /** The property (predicate). */
     private URI property;
 
     /** The object. */
     private String value;
 
     /** True if the values are a merge of multiple values in this annotation. */
     private boolean merge;
 
 
     /**
      * Constructor.
      * 
      * @param annotation
      *            the annotation containing the triple
      * @param subject
      *            the subject
      * @param property
      *            the property (predicate)
      * @param value
      *            the object
      * @param merge
      *            true if the values are a merge of multiple values in this annotation
      * @param anyExisting
      */
     public AnnotationTriple(Annotation annotation, Annotable subject, URI property, String value, boolean merge) {
         this.subject = subject;
         this.annotation = annotation;
         this.property = property;
         this.value = value;
         this.merge = merge;
     }
 
 
     /**
      * Constructor.
      * 
      * @param annotation
      *            the annotation containing the triple
      * @param subject
      *            the subject
      * @param property
      *            the property (predicate) as a Jena object
      * @param value
      *            the object
      * @param merge
      *            true if the values are a merge of multiple values in this annotation
      */
     public AnnotationTriple(Annotation annotation, Annotable subject, Property property, String value, boolean merge) {
         this(annotation, subject, property != null ? URI.create(property.getURI()) : null, value, merge);
     }
 
 
     public Annotation getAnnotation() {
         return annotation;
     }
 
 
     public Annotable getSubject() {
         return subject;
     }
 
 
     public URI getProperty() {
         return property;
     }
 
 
     public void setProperty(URI property) {
         this.property = property;
     }
 
 
     public String getValue() {
         return value;
     }
 
 
     public void setValue(String value) {
         this.value = value;
     }
 
 
     public boolean isMerge() {
         return merge;
     }
 
 
     /**
      * Delete all values of a property describing this resource from an annotation.
      * 
      * @throws ROSRSException
      *             unexpected response from the server
      */
     public void delete()
             throws ROSRSException {
         annotation.deletePropertyValues(subject, property, merge ? null : value);
         if (annotation.getStatements().isEmpty()) {
             annotation.delete();
         } else {
             annotation.update();
         }
        value = null;
     }
 
 
     /**
      * Update the list of statements by setting the property value to a given value. All other values of this property
      * describing this resource are removed.
      * 
      * @param newValue
      *            the new value
      * @throws ROSRSException
      *             unexpected response from the server
      */
     public void updateValue(String newValue)
             throws ROSRSException {
         updatePropertyValue(property, newValue);
     }
 
 
     /**
      * Update the list of statements by setting the property and value to given values. All other values of this
      * property describing this resource are removed.
      * 
      * @param newProperty
      *            the new property
      * @param newValue
      *            the new value
      * @throws ROSRSException
      *             unexpected response from the server
      */
     public void updatePropertyValue(URI newProperty, String newValue)
             throws ROSRSException {
         annotation.deletePropertyValues(subject, property, merge ? null : value);
         property = newProperty;
         value = newValue;
         annotation.getStatements().add(new Statement(subject.getUri(), property, value));
         annotation.update();
     }
 
 }
