 package com.idragon.adastra.context;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import org.springframework.util.Assert;
 
 import java.awt.Dimension;
 import java.awt.Image;
 
 import java.util.HashMap;
 
 
 /**
  * Resource based image source with cache.
  *
  * @author  iDragon
  */
 public class ResourceCachingImageSource extends ResourceImageSource implements CachingImageSource {
 
     // SLF4J log
     private final Logger log = LoggerFactory.getLogger(ResourceCachingImageSource.class);
 
     /** Cache of default size images */
     private HashMap<String, Image> defaultCache = new HashMap<String, Image>();
 
     /** Cache of resized images */
     private HashMap<String, HashMap<Dimension, Image>> resizedCache =
         new HashMap<String, HashMap<Dimension, Image>>();
 
     /**
      * Resource based image source with cache.
      */
     public ResourceCachingImageSource() {
     }
 
     @Override public void clear() {
 
         log.debug("Clearing image source cache");
 
         defaultCache.clear();
         resizedCache.clear();
     }
 
     @Override public Image getImage(String imageCode, Dimension dimension) {
         Assert.hasText(imageCode, "invalid image code: " + imageCode);
 
         if (dimension == null) {
 
             if (defaultCache.containsKey(imageCode)) {
                 log.debug("Returning cached image: code={}", imageCode);
 
                 return defaultCache.get(imageCode);
             }
 
             Image image = getImageInternal(imageCode, null);
 
            // null values are cached too
             log.debug("Caching image: code={}", imageCode);
             defaultCache.put(imageCode, image);
 
             return image;
 
         } else {
 
             HashMap<Dimension, Image> cache = resizedCache.get(imageCode);
 
             if ((cache != null) && cache.containsKey(dimension)) {
 
                 log.debug("Returning cached image: code={}, dimension={}",
                     new Object[] { imageCode, dimension });
 
                 return cache.get(dimension);
             }
 
             Image image = getImageInternal(imageCode, dimension);
 
             if (cache == null) {
 
                 cache = new HashMap<Dimension, Image>();
                 resizedCache.put(imageCode, cache);
             }
 
             // Null values are to be cached!
             log.debug("Caching image: code={}, dimension={}",
                 new Object[] { imageCode, dimension });
 
             cache.put(dimension, image);
 
             return image;
         }
     }
 
     @Override public void remove(String imageCode) {
 
         defaultCache.remove(imageCode);
         resizedCache.remove(imageCode);
     }
 
     @Override public int size() {
 
         int size = defaultCache.size();
 
         for (HashMap<Dimension, Image> subCache : resizedCache.values()) {
             size += subCache.size();
         }
 
         return size;
     }
 }
