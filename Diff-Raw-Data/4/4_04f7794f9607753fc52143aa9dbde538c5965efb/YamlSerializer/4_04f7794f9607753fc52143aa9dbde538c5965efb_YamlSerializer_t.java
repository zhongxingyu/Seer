 /**
  * 
  */
 package com.andune.minecraft.commonlib.reflections;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.charset.Charset;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.reflections.Reflections;
 import org.reflections.serializers.Serializer;
import org.reflections.util.ConfigurationBuilder;
 import org.reflections.util.Utils;
 import org.yaml.snakeyaml.Yaml;
 
 import com.google.common.collect.Multimap;
 import com.google.common.io.Files;
 
 /**
  * @author andune
  *
  */
 public class YamlSerializer implements Serializer {
 
     @Override
     public Reflections read(InputStream inputStream) {
         Yaml yaml = new Yaml();
        Reflections reflections = new Reflections(new ConfigurationBuilder());
         @SuppressWarnings("unchecked")
         Map<String, Map<String, Collection<String>>> map =
                 (Map<String, Map<String, Collection<String>>>) yaml.load(inputStream);
         for(String index : map.keySet()) {
             for(String key : map.get(index).keySet()) {
                 for(String value : map.get(index).get(key)) {
                     reflections.getStore().getOrCreate(index).put(key, value);
                 }
             }
         }
         return reflections;
     }
 
     @Override
     public File save(Reflections reflections, String filename) {
         try {
             File file = Utils.prepareFile(filename);
             Files.write(toString(reflections), file, Charset.defaultCharset());
             return file;
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     @Override
     public String toString(Reflections reflections) {
         Yaml yaml = new Yaml();
         // the Yaml dumper can only effectively dump primitives, Collections
         // and JavaBeans. Since the Reflection and it's Store class don't adhere
         // to JavaBeans, we first convert them into primitive maps that the
         // Yaml dumper has no problem dumping accurately.
         Map<String, Map<String, Collection<String>>> dumpMap = new HashMap<String, Map<String, Collection<String>>>();
         Map<String, Multimap<String, String>> storeMap = reflections.getStore().getStoreMap();
         for(String key : storeMap.keySet()) {
             dumpMap.put(key, storeMap.get(key).asMap());
         }
         return yaml.dump(dumpMap);
     }
 }
