 package tk.manf.serialisation;
 
 import java.io.File;
 import java.io.IOException;
 import tk.manf.serialisation.annotations.Unit;
 import tk.manf.serialisation.annotations.Property;
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import tk.manf.serialisation.annotations.Identification;
 import tk.manf.serialisation.handler.SerialisationHandler;
 
 /**
  * The ObjectSerialiser handles saving and loading of Units.
  *
  * @author Björn 'manf' Heinrichs
  *
  * @see Unit
  */
 public final class ObjectSerialiser {
     /**
      * Datafolder of Plugin
      */
     private final File dataFolder;
     /**
      * Prefix of Plugin
      */
     private final String prefix;
     /**
      * Default Logger
      */
     private static final Logger logger = Logger.getLogger("Minecraft");
 
     /**
      * Initialises Object Serialiser
      * <p/>
      * @param plugin Plugin for saving and loading Units
      */
     public ObjectSerialiser(JavaPlugin plugin) {
         this.dataFolder = plugin.getDataFolder();
         PluginDescriptionFile pdf = plugin.getDescription();
         this.prefix = pdf.getPrefix() == null ? pdf.getName() : pdf.getPrefix();
     }
 
     /**
      * Saves given Unit to with SerialisationHandler to memory
      *
      * @param o
      * <p/>
      * @throws IllegalArgumentException
      * @throws IllegalAccessException
      * @throws IOException
      * @throws SerialisationException
      */
     public void save(Object o) throws IllegalArgumentException, IllegalAccessException, IOException, SerialisationException {
         save(o, true);
     }
 
     /**
      * Saves given Unit to with SerialisationHandler to memory
      *
      * @param o
      * @param warn
      * <p/>
      * @throws IllegalArgumentException
      * @throws IllegalAccessException
      * @throws IOException
      * @throws SerialisationException
      */
     public void save(Object o, boolean warn) throws IllegalArgumentException, IllegalAccessException, IOException, SerialisationException {
         Unit unit = o.getClass().getAnnotation(Unit.class);
         if (unit == null) {
             return;
         }
         SerialisationHandler handler = unit.type().getHandler();
         String id = null;
         for (Field f : o.getClass().getDeclaredFields()) {
             f.setAccessible(true);
             if (!unit.isStatic() && id == null) {
                 if (f.getAnnotation(Identification.class) != null) {
                     id = f.get(o).toString();
                     warn(warn && !Modifier.isFinal(f.getModifiers()), "Identification {0} is not final! ", f.getName());
                 } else {
                     throw new SerialisationException("Identification not found! Found " + f.getName());
                 }
             }
             Property prop = f.getAnnotation(Property.class);
             if (prop == null) {
                 warn(warn, "Field {0} is no Propertie({1})", f.getName(), f.getClass().getName());
                 continue;
             }
             warn(warn && prop.name().length() == 0, "Field {0} has no name. Using field name!", f.getName());
             handler.save(unit, dataFolder, id, prop.name().length() == 0 ? f.getName() : prop.name(), f.get(o));
             f.setAccessible(false);
         }
         handler.save(unit, dataFolder, id);
     }
 
     /**
      * Loads all Objects for the given Unit. Static Units only return a List with one Index. Non-Static Units may return alot Unit objects, depending on folder size.
      *
      * @param type Units class that should be loaded
      * @return Unit objects
      * @throws Exception depending on implementation of SerialisationHandler
      * @throws SerialisationException if type is no Unit
      * @see SerialisationHandler
      */
    public <T> List<T> load(Class<T> type) throws Exception, SerialisationException {
         Unit unit = type.getAnnotation(Unit.class);
         if (unit == null) {
             throw new SerialisationException("Type is no Unit");
         }
         return unit.type().getHandler().load(type, unit, dataFolder);
     }
 
     /**
      * Loads all Objects for the given Unit. Will only return first Element. Used to get static Object
      *
      * @param type Units class that should be loaded
      * @return Unit objects
      * @throws Exception depending on implementation of SerialisationHandler
      * @throws SerialisationException if type is no Unit
      * @see SerialisationHandler
      */
    public <T> T loadStatic(Class<T> type) throws Exception, SerialisationException {
         return load(type).get(0);
     }
 
     /**
      * Prints a warning to our Logger if boolean is true
      *
      * @param warn if warning is to be printed
      * @param msg Message that should be printed
      * @param params parameter to the Message
      */
     private void warn(boolean warn, String msg, Object... params) {
         if (warn) {
             logger.log(Level.WARNING, "[" + prefix + "] [Serialisation] " + msg, params);
         }
     }
 }
