 package me.tehbeard.utils.factory;
 
 import java.lang.annotation.Annotation;
 import java.util.HashMap;
 import java.util.Map;
 
 
 /**
  * Represents a factory for various classes
  * Any item placed into the factory must have an annotation
  * @author james
  *
  * @param <T>
  */
 public abstract class ConfigurableFactory<T> {
     private Map<String,Class<? extends T>> products;
    private Class<Annotation> annotation;
     
     /**
      * Constructs a new Configurable factory
      */
    public ConfigurableFactory(Class<Annotation> annotation){
         this.annotation = annotation;
         products = new HashMap<String, Class<? extends T>>();
     }
 
     /**
      * Parse a product and add it to the factory
      * @param product product to add to the factory
      * @return wether it was added or not
      */
     public boolean addProduct(Class<? extends T> product){
         Annotation tag = product.getAnnotation(annotation);
         if(tag!=null){
             String t = getTag(tag);
             if(t!=null){
                 products.put(t,product);
                 return true;
             }
 
         }
         return false;
     }
 
     /**
      * Returns an instance of a product
      * @param tag tag of product to make
      * @return an instance of the product, or null if not found
      * @throws IllegalStateException
      */
     public T getProduct(String tag)throws IllegalStateException{
         if(products.containsKey(tag)){
             try {
                 T object = products.get(tag).newInstance();
                 return object;
             } catch (InstantiationException e) {
                 throw new IllegalStateException("Could not initialise instance of the object");
             } catch (IllegalAccessException e) {
                 throw new IllegalStateException("Could not initialise instance of the object due to an access exception");
 
             }	
         }
         return null;
     }
 
     /**
      * Return the tag code for a class
      * @param annotation annotation to parse
      * @return
      */
     public abstract String getTag(Annotation annotation);
 
 }
