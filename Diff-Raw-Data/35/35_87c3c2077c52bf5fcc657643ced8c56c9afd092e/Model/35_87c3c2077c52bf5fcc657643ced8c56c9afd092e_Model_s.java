 /*
  * ModelCC, under ModelCC Shared Software License, www.modelcc.org. Luis Quesada Torres.
  */
 
 
 package org.modelcc.metamodel;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.util.Collections;
 import java.util.Map;
 import java.util.Set;
 import org.modelcc.lexer.recognizer.PatternRecognizer;
 
 /**
  * Language model.
  * @author elezeta
  * @serial
  */
 public final class Model implements Serializable,Cloneable {
 
     /**
      * Serial Version ID
      */
     private static final long serialVersionUID = 31415926535897932L;
 
     /**
      * Set of elements of this model.
      */
     private Set<ModelElement> elements;
 
     /**
      * The start element of this model.
      */
     private ModelElement start;
 
     /**
      * Delimiters of this model.
      */
     private Set<PatternRecognizer> delimiters;
 
     /**
      * ModelElement precedence map.
      */
     private Map<ModelElement,Set<ModelElement>> precedences;
 
     /**
      * ModelElement subelement map.
      */
     private Map<ModelElement,Set<ModelElement>> subelements;
 
     /**
      * ModelElement superclass map.
      */
     private Map<ModelElement,ModelElement> superelements;
 
     /**
      * Class to element map.
      */
     private Map<Class,ModelElement> classToElement;
 
     /**
      * ModelElement default element map.
      */
     private Map<ModelElement,ModelElement> defaultElement;
 
     /**
      * Constructor.
      * @param elements the set of elements of this model.
      * @param start the start element of this model
      * @param delimiters the delimiters of this model
      * @param precedences the element precedence map
      * @param subelements the element subclass map
      * @param superelements the element superclass map
      * @param classToElement the class to element map
      */
     public Model(Set<ModelElement> elements,ModelElement start,Set<PatternRecognizer> delimiters,Map<ModelElement,Set<ModelElement>> precedences,Map<ModelElement,Set<ModelElement>> subelements,Map<ModelElement,ModelElement> superelements,Map<Class,ModelElement> classToElement,Map<ModelElement,ModelElement> defaultElement) {
         this.elements = elements;
         this.start = start;
         this.delimiters = delimiters;
         this.precedences = precedences;
         this.subelements = subelements;
         this.superelements = superelements;
         this.classToElement = classToElement;
         this.defaultElement = defaultElement;
     }
 
     /**
      * @return the elements of this model
      */
     public Set<ModelElement> getElements() {
         return Collections.unmodifiableSet(elements);
     }
 
     /**
      * @return the start element of this model
      */
     public ModelElement getStart() {
         return start;
     }
 
     /**
      * @return the delimiters of this model
      */
     public Set<PatternRecognizer> getDelimiters() {
         return Collections.unmodifiableSet(delimiters);
     }
 
     /**
      * @return the element precedences map
      */
     public Map<ModelElement, Set<ModelElement>> getPrecedences() {
         return Collections.unmodifiableMap(precedences);
     }
 
     /**
      * @return the element subelements map
      */
     public Map<ModelElement, Set<ModelElement>> getSubelements() {
         return Collections.unmodifiableMap(subelements);
     }
 
     /**
      * @return the element superelements map
      */
     public Map<ModelElement,ModelElement> getSuperelements() {
         return Collections.unmodifiableMap(superelements);
     }
 
     /**
      * @return the classToElement
      */
     public Map<Class, ModelElement> getClassToElement() {
         return Collections.unmodifiableMap(classToElement);
     }
 
     /**
      * @return the defaultElement
      */
     public Map<ModelElement, ModelElement> getDefaultElement() {
         return Collections.unmodifiableMap(defaultElement);
     }
 
     @Override
 	public Model clone(){
         ObjectOutputStream oos = null;
         ObjectInputStream ois = null;
         try {
             ByteArrayOutputStream bOs = new ByteArrayOutputStream();
             oos = new ObjectOutputStream(bOs);
             oos.writeObject(this);
             ois = new ObjectInputStream(new ByteArrayInputStream(bOs.toByteArray()));
            return  (Model)ois.readObject();
 
         } catch (Exception e) {
         	e.printStackTrace();
             return null;
         }finally {
             if (oos != null)
                 try {
                     oos.close();
                 } catch (IOException e) {
 
                 }
             if (ois != null)
                 try {
                     ois.close();
                 } catch (IOException e) {
 
                 }
         }
     }
 
 }
