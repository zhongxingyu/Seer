 package org.ritsuka.natsuo.yaconfig;
 
 import org.apache.commons.lang.StringUtils;
 import org.yaml.snakeyaml.Yaml;
 import org.yaml.snakeyaml.constructor.BaseConstructor;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 public final class YaConfig {
     private static final String CFG_DEFAULT_PATH = "conf/config.yml";
     private static final String CFG_DEFAULT_PROPERTY = "app.configFile";
     private static final String CFG_JVMPROPS_SECTION = "jvm.properties";
     public static Boolean verbose = false;
 
     private Map parsed = null;
     private BaseConstructor constructor = null;
     private static YaConfig config;
 
     public static boolean load() {
         return load(null);
     }
 
     public static boolean load(final BaseConstructor constructor) {
         config = new YaConfig();
         config.constructor = constructor;
         return config.loadConfig();
     }
 
 
     public boolean loadConfig() {
         String configPath = System.getProperty(CFG_DEFAULT_PROPERTY);
 
         if (configPath == null) {
             configPath = CFG_DEFAULT_PATH;
         }
 
 
         try {
             parsed = loadCfgFile(configPath);
             if (verbose)
                 System.out.print(String.format("YaConfig: dump:\n%s", (new Yaml()).dump(parsed)));
             if (parsed.containsKey(CFG_JVMPROPS_SECTION)) {
                 setProperties((Map) parsed.get(CFG_JVMPROPS_SECTION), "");
             } else if (verbose) {
                 System.out.println("Config doesn't contain JVM properties");
             }
 
             return true;
         } catch (final Exception e) {
             System.err.println(String.format(
                     "YaConfig: Can't load config file. "
                     + "Use -D%s=relative/path/to/cfg.yml. Exception: %s",
                     CFG_DEFAULT_PROPERTY,
                     e.toString()));
             e.printStackTrace();
             //throw new ExceptionInInitializerError(e);
         }
         return false;
     }
 
     private Map loadCfgFile(String configPath) throws FileNotFoundException {
         final Yaml yaml;
         if (null != constructor)
             yaml = new Yaml(constructor);
         else
             yaml = new Yaml();
 
         InputStream input = new FileInputStream(new File(configPath));
         return processInclusions((Map) yaml.load(input));
     }
 
 
     private Map processInclusions(final Map cfgtree) throws FileNotFoundException {
         if (null != cfgtree) {
             Map newtree = new HashMap();
             Set<Map.Entry> entries = cfgtree.entrySet();
             for (Map.Entry entry : entries) {
                 String key = (String) entry.getKey();
                 Object value = entry.getValue();
                 if ((value instanceof String) && (key.compareTo("cfg.inclusion") == 0)) {
                     Map inclusion = loadCfgFile((String)value);
                     newtree.putAll(inclusion);
                 } else if (value instanceof Map) {
                     newtree.put(key, processInclusions((Map)value));
                 }
                 else {
                     newtree.put(key, value);
                 }
             }
             return newtree;
         }
         return cfgtree;
     }
 
     // set jvm properties using tree structure
     @SuppressWarnings("unchecked")
     private void setProperties(final Map props, final String basepath) {
         if (null != props) {
             Set<Map.Entry> entries = props.entrySet();
             for (Map.Entry entry : entries) {
                 Object value = entry.getValue();
                 String key = entry.getKey().toString();
                 if (!basepath.isEmpty()) {
                     key = basepath + "." + key;
                 }
 
                 if (!(value instanceof Map)) {
                     System.setProperty(key, value.toString());
                     if (verbose) {
                         System.out.println(String.format("YaConfig: %s:=%s", key, value));
                     }
                 } else {
                     setProperties((Map) value, key);
                 }
             }
         }
     }
 
     @SuppressWarnings("unchecked")
     public static <T> T get(final IConfigKey<T> key) {
         Object val = config.getParsed();
         for (String entry : key.getPath()) {
             if (val instanceof Map) {
                 val = ((Map) val).get(entry);
             } else {
                 /* we reached end earlier?.. */
                 return key.getDefaultValue();
             }
         }
 
         if (val == null) {
             return key.getDefaultValue();
         }
 
         T assumedVal;
         try {
             IKeyVerifier<T> verifier = key.verifier();
             IConstructor<T> constructor = key.constructor();
             assumedVal = constructor.construct(val);
 
             if (!verifier.verify(assumedVal)) {
                 String path = StringUtils.join(key.getPath().toArray());
                 throw new IllegalArgumentException(String.format("Parameter '%s' has invalid value", path));
             }
         } catch (final ClassCastException e) {
             throw new IllegalStateException(e);
         }
 
         return assumedVal;
     }
 
     public Map getParsed() {
         return parsed;
     }
 }
