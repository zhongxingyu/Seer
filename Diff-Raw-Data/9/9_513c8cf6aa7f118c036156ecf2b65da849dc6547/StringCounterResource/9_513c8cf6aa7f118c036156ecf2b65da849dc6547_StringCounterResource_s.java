 package txtfnnl.uima.resource;
 
 import org.apache.uima.resource.ResourceInitializationException;
 import org.apache.uima.resource.SharedResourceObject;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public
 class StringCounterResource extends LineBasedStringMapResource<Map<String, Integer>> {
 
   public static
   class Builder extends LineBasedStringMapResource.Builder {
     protected
     Builder(Class<? extends SharedResourceObject> klass, String url) {
       super(klass, url);
     }
 
     public
     Builder(String resourceUrl) {
      this(EntityStringMapResource.class, resourceUrl);
     }
   }
 
   /**
    * Configure a new Entity String Map resource.
    *
    * @param resourceUrl the URL where this resource is located (In the case of a file, make sure
    *                    that the (data) resource URL is prefixed with the "file:" schema prefix.)
    */
   public static
   Builder configure(String resourceUrl) {
     return new Builder(resourceUrl);
   }
 
   @Override
   void parse(String[] items) throws ResourceInitializationException {
     try {
       Map<String, Integer> counter;
       if (!resourceMap.containsKey(items[0])) {
         counter = new HashMap<String, Integer>();
         resourceMap.put(items[0], counter);
       } else {
         counter = resourceMap.get(items[0]);
       }
       counter.put(items[1], Integer.parseInt(items[2]));
     } catch (final IndexOutOfBoundsException e1) {
       throw new ResourceInitializationException(
           new RuntimeException(
               "illegal line: '" + line +
               "' with " + items.length + " fields"
           )
       );
     } catch (final NumberFormatException e2) {
       throw new ResourceInitializationException(
           new RuntimeException("not a counter: '" + items[2] + "'")
       );
     }
   }
 }
