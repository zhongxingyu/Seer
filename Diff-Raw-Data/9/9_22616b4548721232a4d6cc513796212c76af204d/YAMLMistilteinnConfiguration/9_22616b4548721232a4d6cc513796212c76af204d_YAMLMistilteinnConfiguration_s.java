 package org.codefirst.mistilteinn.config;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.apache.commons.lang.ObjectUtils;
 import org.codefirst.mistilteinn.MistilteinnException;
 import org.yaml.snakeyaml.Yaml;
 
 /**
  * Configuration accessor with YAML.
  */
 @SuppressWarnings("unchecked")
 public class YAMLMistilteinnConfiguration implements MistilteinnConfiguration {
     /** the key of its configuration. */
     private static final String TICKET = "ticket";
     /** the sub key of its configuration. */
     private static final String SOURCE = "source";
 
     /** configuration representation. */
     private Map<String, Object> yaml;
 
     /**
      * Constructor.
      * @param is input stream
      */
     public YAMLMistilteinnConfiguration(InputStream is) {
         Yaml parser = new Yaml();
         yaml = (Map<String, Object>) parser.load(is);
     }
 
     public String getITS() {
         Map<String, Object> ticket = (Map<String, Object>) yaml.get(TICKET);
         return ObjectUtils.toString(ticket.get(SOURCE));
     }
 
     public void setITS(String id) {
         Map<String, Object> ticket = (Map<String, Object>) yaml.get(TICKET);
         ticket.put(SOURCE, id);
     }
 
     public Map<String, String> getConfiguration(String id) {
         Map<String, String> map = new LinkedHashMap<String, String>();
 
         Map<String, Object> section = (Map<String, Object>) yaml.get(id);
 
         Set<Entry<String, Object>> entrySet = section.entrySet();
         for (Entry<String, Object> entry : entrySet) {
             map.put(entry.getKey(), ObjectUtils.toString(entry.getValue()));
         }
 
         return map;
     }
 
     public void setConfiguration(String id, Map<String, String> configuration) {
         yaml.put(id, configuration);
     }
 
     public void save(OutputStream os) throws MistilteinnException {
         String dump = new Yaml().dump(yaml);
 
        OutputStreamWriter osw = new OutputStreamWriter(os);
         try {
             osw.write(dump);
         } catch (IOException e) {
             throw new MistilteinnException(e);
         } finally {
             try {
                osw.close();
             } catch (IOException e) {
                 // do nothing
             }
         }
     }
 
 }
