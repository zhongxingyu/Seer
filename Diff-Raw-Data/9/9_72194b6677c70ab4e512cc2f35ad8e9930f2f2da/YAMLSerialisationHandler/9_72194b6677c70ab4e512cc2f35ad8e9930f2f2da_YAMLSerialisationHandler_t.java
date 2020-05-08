 package tk.manf.serialisation.handler.flatfile;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import tk.manf.serialisation.SerialisationException;
 import tk.manf.serialisation.annotations.InitiationConstructor;
 import tk.manf.serialisation.annotations.Parameter;
 import tk.manf.serialisation.annotations.Property;
 import tk.manf.serialisation.annotations.Unit;
 import tk.manf.serialisation.handler.SerialisationHandler;
 
 /**
  * YAML Serialisation Handler for Bukkits FileConfiguration
  * 
  * @author Björn 'manf' Heinrichs
  */
 public class YAMLSerialisationHandler implements SerialisationHandler {
     /**
      * Configuration Cache
      */
     private static HashMap<String, FileConfiguration> cache;
 
     /**
      * Inintialises a new YAMLSerialisationHandler with capacity
      * @param opacity the initial capacity.
      */
     public YAMLSerialisationHandler(int capacity) {
         cache = new HashMap<String, FileConfiguration>(capacity);
     }
 
     /**
      * Saves given Unit to Memory
      * 
      * @param unit Unit
      * @param folder DataFolder of Plugin
      * @param id id of Unit for non static Units
      * 
      * @throws IOException 
      */
     public void save(Unit unit, File folder, String id) throws IOException {
         FileConfiguration config = loadConfig(unit, folder, id);
         File f = new File(folder, unit.name());
        f.getParentFile().mkdirs();
         if (!unit.isStatic()) {
             f = new File(f, id);
         }
         config.save(f);
         cache.remove(unit.isStatic() ? unit.name() : unit.name() + "-" + id);
     }
 
     /**
      * Saves current Value to Key in the Cache
      * 
      * @param unit Unit 
      * @param folder DataFolde of Plugin
      * @param id ID of Plugin
      * @param key Key
      * @param value Value to save
      */
     public void save(Unit unit, File folder, String id, String key, Object value) {
         FileConfiguration config = loadConfig(unit, folder, id);
         config.set(key, value);
         cacheConfig(unit, id, config);
     }
 
     /**
      * Loads current Units from Memory
      *
      * @param c Class of Unit
      * @param unit Unit
      * @param dataFolder dataFolder of Plugin
      * 
      * @return all Units
      * 
      * @throws SerialisationException if no Object was initiated, caused without an InitiationConstructor or if an InstantiationException is thrown. 
      * @throws InvocationTargetException if an Error in the Construcor of the Object occured
      * @throws IllegalAccessException if any access is denied
      */
     public <T> List<T> load(Class<T> c, Unit unit, File folder) throws SerialisationException, InvocationTargetException, IllegalAccessException {
         List<T> tmp;
         if (unit.isStatic()) {
             tmp = new ArrayList<T>(1);
             tmp.add(toObject(c, loadConfig(unit, folder, null)));
         } else {
             final File saves = new File(folder, unit.name());
            saves.mkdirs();
             tmp = new ArrayList<T>(saves.listFiles().length);
             for (File f : saves.listFiles()) {
                 tmp.add(toObject(c, loadConfig(folder, f.getName(), unit.name())));
             }
         }
         return tmp;
     }
 
     /**
      * Loads the Config
      * <p/>
      * @param folder DataFolder of Plugin
      * @param id id of Unit
      * @param name Name of Unit
      * <p/>
      * @return Configuration
      */
     private static FileConfiguration loadConfig(Unit unit, File folder, String id) {
         if (unit.isStatic()) {
             return loadConfig(folder, unit.name(), unit.name());
         }
         return loadConfig(new File(folder, unit.name()), unit.name() + "-" + id, id);
     }
 
     /**
      * Loads given Config from Cache or File
      * <p/>
      * @param folder DataFolder of Plugin
      * @param id id of Unit
      * @param name Name of Unit
      * <p/>
      * @return Configuration
      *
      * @throws IllegalArgumentException if no Configuration could be loaded
      */
     private static FileConfiguration loadConfig(File folder, String id, String name) {
         if (cache.containsKey(id)) {
             return cache.get(id);
         }
         return YamlConfiguration.loadConfiguration(new File(folder, name));
     }
 
     /**
      * Saves Configurations in Cache
      * <p/>
      * @param Unit unit
      * @param id id of Unit
      * @param config Configuration that saved current Values of Object
      */
     private static void cacheConfig(Unit unit, String id, FileConfiguration config) {
         if (unit.isStatic()) {
             cacheConfig(unit.name(), config);
         } else {
             cacheConfig(unit.name() + "-" + id, config);
         }
     }
 
     /**
      * Saves Config in Cache
      * <p/>
      * @param id id of Unit
      * @param config Configuration that saved current Values of Object
      */
     private static void cacheConfig(String id, FileConfiguration config) {
         if (cache.containsKey(id)) {
             return;
         }
         cache.put(id, config);
     }
 
     /**
      * Returns an Object of given Class based on Configuration
      *
      * @param c Class of the Object
      * @param config Configuration that saved all Values of Object
      *
      * @return Object of given Class
      *
      * @throws IllegalAccessException if any access is denied
      * @throws InvocationTargetException if an Error in the Construcor of the Object occured
      * @throws SerialisationException if no Object was initiated, caused without an InitiationConstructor or if an InstantiationException is thrown
      */
     private static <T> T toObject(Class<T> c, FileConfiguration config) throws SerialisationException, InvocationTargetException, IllegalAccessException {
         T o = null;
         try {
             for (Constructor<?> constr : c.getConstructors()) {
                 if (constr.getAnnotation(InitiationConstructor.class) == null) {
                     continue;
                 }
                 final List<Object> params = new ArrayList<Object>(constr.getParameterTypes().length);
                 for (Class<?> type : constr.getParameterTypes()) {
                     Parameter param = type.getAnnotation(Parameter.class);
                     if (param == null) {
                         params.add(type.isPrimitive() ? type.newInstance() : null);
                         continue;
                     }
                     params.add(type.cast(config.get(param.name())));
                 }
                 o = c.cast(constr.newInstance(params.toArray(new Object[params.size()])));
             }
         } catch (InstantiationException ex) {
             throw new SerialisationException(ex.getLocalizedMessage());
         } catch (IllegalArgumentException ex) {
             //Should not be thrown
         }
         if (o == null) {
             throw new SerialisationException("No Object initiated");
         }
         for (Field f : c.getDeclaredFields()) {
             if (f.isAccessible()) {
                 Property prop = f.getAnnotation(Property.class);
                 if (prop == null) {
                     continue;
                 }
                 f.set(o, config.get(prop.name().length() == 0 ? f.getName() : prop.name()));
             }
         }
         return o;
     }
 }
