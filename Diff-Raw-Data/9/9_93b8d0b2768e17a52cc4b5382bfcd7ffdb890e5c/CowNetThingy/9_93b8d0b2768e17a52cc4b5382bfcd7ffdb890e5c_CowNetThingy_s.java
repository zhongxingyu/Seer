 package us.fitzpatricksr.cownet;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Player;
 import us.fitzpatricksr.cownet.utils.PersistentState;
 import us.fitzpatricksr.cownet.utils.StringUtils;
 
 import java.lang.annotation.Retention;
 import java.lang.annotation.RetentionPolicy;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.util.*;
 import java.util.logging.Logger;
 
 public class CowNetThingy implements CommandExecutor, PersistentState {
 
     @Retention(RetentionPolicy.RUNTIME)
     public @interface Setting {
         String name() default "";
     }
 
     @Retention(RetentionPolicy.RUNTIME)
     public @interface CowCommand {
         boolean opOnly() default false;
 
         String permission() default "";
     }
 
     private Logger logger = Logger.getLogger("Minecraft");
     private CowNetMod plugin;
     private String permissionNode = "";
 
     @Setting
     private boolean debug = false;
 
     public CowNetThingy() {
     }
 
     protected void onEnable() throws Exception {
     }
 
     protected void onDisable() {
     }
 
     final void setPlugin(CowNetMod plugin) {
         this.plugin = plugin;
     }
 
     final void setPermissionRoot(String permissionRoot) {
         this.permissionNode = permissionRoot + "." + getTrigger();
     }
 
     @Override
     public final boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
         if (sender instanceof Player) {
             Player player = (Player) sender;
             if (!hasPermissions(player)) {
                 player.sendMessage("Sorry, you don't have permission");
                 return true;
             }
         }
         return dispatchMethod(sender, cmd, label, args);
     }
 
     public final String getTrigger() {
         return getClass().getSimpleName().toLowerCase();
     }
 
     // check if player has permission to use this plugin
     public final boolean hasPermissions(Player player) {
         if (player.hasPermission(permissionNode)) {
             return true;
         } else {
             logInfo(player.getName() + " does not have " + permissionNode);
             return false;
         }
     }
 
     // check if player has the specified permission for this plugin
     public final boolean hasPermissions(CommandSender player, String perm) {
         return checkPermissions(player, perm);
     }
 
     // just a utility to help with the multiple checks above.
     public boolean checkPermissions(CommandSender player, String perm) {
         if (player.hasPermission(permissionNode + "." + perm)) {
             debugInfo(player.getName() + " has " + permissionNode + "." + perm);
             return true;
         } else {
             debugInfo(player.getName() + " does not have explicit " + permissionNode + "." + perm);
             return false;
         }
     }
 
     public final CowNetMod getPlugin() {
         return plugin;
     }
 
     public final void logInfo(String msg) {
         logger.info(permissionNode + ": " + msg);
     }
 
     public final void logStackTrace(String msg) {
         logger.info(permissionNode + ": " + msg);
         try {
             throw new Exception();
         } catch (Exception e) {
             for (StackTraceElement el : e.getStackTrace()) {
                 logger.info(el.toString());
             }
         }
     }
 
     public final void debugInfo(String msg) {
         if (debug) {
             logger.info("[" + permissionNode + "]: " + msg);
         }
     }
 
     public final boolean isDebug() {
         return debug;
     }
 
     //------------------------------------
     // built-in commands
 
     @CowCommand
     private boolean doHelp(CommandSender sender) {
         for (String help : getHelpText(sender)) {
             sender.sendMessage(help);
         }
         return true;
     }
 
     protected String[] getHelpText(CommandSender sender) {
         return new String[]{getHelpString(sender)};
     }
 
     protected String getHelpString(CommandSender sender) {
         return "There is no help for you...";
     }
 
     //------------------------------------
     // methods related to settings and persistent configuration
 
     @Override
     public final boolean hasConfigValue(String key) {
         return plugin.getConfig().contains(key);
     }
 
     @Override
     public final int getConfigValue(String key, int def) {
         return plugin.getConfig().getInt(getTrigger() + "." + key, def);
     }
 
     @Override
     public final long getConfigValue(String key, long def) {
         return plugin.getConfig().getLong(getTrigger() + "." + key, def);
     }
 
     @Override
     public final double getConfigValue(String key, double def) {
         return plugin.getConfig().getDouble(getTrigger() + "." + key, def);
     }
 
     @Override
     public final boolean getConfigValue(String key, boolean def) {
         return plugin.getConfig().getBoolean(getTrigger() + "." + key, def);
     }
 
     @Override
     public final String getConfigValue(String key, String def) {
         return plugin.getConfig().getString(getTrigger() + "." + key, def);
     }
 
     @Override
     public final Map getConfigValue(String key, Map def) {
         if (plugin.getConfig().isConfigurationSection(key)) {
             ConfigurationSection config = plugin.getConfig().getConfigurationSection(key);
             for (String k : config.getKeys(false)) {
                 def.put(k, config.get(k));
             }
             return def;
         } else {
             return def;
         }
     }
 
     @Override
     public final List<?> getConfigValue(String key, List<?> def) {
         return plugin.getConfig().getList(key, def);
     }
 
     @Override
     public final Object getConfigValue(String key, Object def) {
         return plugin.getConfig().get(key, def);
     }
 
     @Override
     public final List<String> getStringList(String key, List<String> def) {
         List<String> result = plugin.getConfig().getStringList(key);
         return (result != null) ? result : def;
     }
 
     @Override
     public final ConfigurationSection getConfigurationSection(String key) {
         return plugin.getConfig().getConfigurationSection(key);
     }
 
     @Override
     public final void updateConfigValue(String key, int value) {
         plugin.getConfig().set(getTrigger() + "." + key, value);
     }
 
     @Override
     public final void updateConfigValue(String key, long value) {
         plugin.getConfig().set(getTrigger() + "." + key, value);
     }
 
     @Override
     public final void updateConfigValue(String key, double value) {
         plugin.getConfig().set(getTrigger() + "." + key, value);
     }
 
     @Override
     public final void updateConfigValue(String key, boolean value) {
         plugin.getConfig().set(getTrigger() + "." + key, value);
     }
 
     @Override
     public final void updateConfigValue(String key, String value) {
         plugin.getConfig().set(getTrigger() + "." + key, value);
     }
 
     @Override
     public final void updateConfigValue(String key, Map<String, ?> value) {
         plugin.getConfig().set(getTrigger() + "." + key, value);
     }
 
     @Override
     public final void updateConfigValue(String key, List<?> value) {
         plugin.getConfig().set(getTrigger() + "." + key, value);
     }
 
     @Override
     public void updateConfigValue(String key, Object value) {
         plugin.getConfig().set(getTrigger() + "." + key, value);
     }
 
     @Override
     public void removeConfigValue(String key) {
         plugin.getConfig().set(getTrigger() + "." + key, null);
     }
 
     public final void saveConfiguration() {
         plugin.saveConfig();
     }
 
     /**
      * This hook allows subclasses to load non-automatic setting data.
      * You shouldn't call this directly, but through reloadSettings() instead.
      */
     protected void reloadManualSettings() throws Exception {
     }
 
     /**
      * Subclasses should implement this to return information about
      * the state of manual settings.
      *
      * @return a HashMap of (settingName -> settingValue)
      */
     protected HashMap<String, String> getManualSettings() {
         //return name, value
         return new HashMap<String, String>();
     }
 
     /**
      * This is a hook for subclasses to load configuration information
      * that isn't in one of the support formats.  Enums for example.
      * Subclasses should update the value of the runtime setting
      * and the persistent storage.  Storage will be save automatically.
      *
      * @param settingName  the name of the setting to update
      * @param settingValue the new setting value
      * @return true if the setting was updated.  false if not.
      */
     protected boolean updateManualSetting(String settingName, String settingValue) {
         return false;
     }
 
     /**
      * Command to reload configuration data from disk.
      */
     @CowCommand(opOnly = true)
     private boolean doReload(CommandSender sender) {
         getPlugin().reloadConfig();
         try {
             reloadSettings(sender);
             sender.sendMessage("Reloaded.");
         } catch (Exception e) {
             sender.sendMessage("Could not reload settings.");
             e.printStackTrace();
         }
         return true;
     }
 
     /**
      * Dump a list of setting that the specified sender has access to.
      */
     @CowCommand(opOnly = true)
     private boolean doSettings(CommandSender sender) {
         HashMap<String, Field> autoSettings = getAutomaticSettings();
         HashMap<String, String> manualSettings = getManualSettings();
         List<String> settingNames = new LinkedList<String>(autoSettings.keySet());
         settingNames.addAll(manualSettings.keySet());
         Collections.sort(settingNames);
         for (String settingName : settingNames) {
             Object value = manualSettings.get(settingName);
             if (value == null || value instanceof Field) {
                 // it's not a manual setting
                 Field f = (value != null) ? (Field) value : autoSettings.get(settingName);
                 try {
                     f.setAccessible(true);
                     value = f.get(this).toString();
                 } catch (IllegalAccessException e) {
                     e.printStackTrace();
                 } finally {
                     f.setAccessible(false);
                 }
             }
             if (sender != null) {
                 sender.sendMessage(settingName + ": " + value);
             } else {
                 // this is a hack to show settings at startup time since there's no sender then
                 logInfo(settingName + ": " + value);
             }
         }
         return true;
     }
 
     /**
      * command to set the value of a setting, both manual and automatic. Changes will be persistent.
      */
     @CowCommand(opOnly = true)
     private boolean doSettings(CommandSender sender, String settingName, String settingValue) {
         // set <setting> <value>
         if (!setAutoSettingValue(settingName, settingValue) && !updateManualSetting(settingName, settingValue)) {
             sender.sendMessage("Setting not found or could not be updated.");
         } else {
             saveConfiguration();
             doSettings(sender);
         }
         return true;
     }
 
     /**
      * Reload all manual and magic settings and dump them to the sender/console.
      * This is the method that should be called to load settings from a configuration
      * at startup or after the configuration has been changed by some external source.
      * When complete, net settings will be dumped to the console.
      */
     public final void reloadSettings() throws Exception {
         reloadSettings(null);
     }
 
     /**
      * Reload all manual and magic settings and dump them to the sender/console.
      * This is the method that should be called to load settings from a configuration
      * at startup or after the configuration has been changed by some external source.
      *
      * @param sender where to dump the new setting values when finished.  If NULL
      *               settings will be dumped to the console.
      */
     protected final void reloadSettings(CommandSender sender) throws Exception {
         reloadAutoSettings();
         reloadManualSettings();
         doSettings(sender);
     }
 
     /**
      * Get the value of the automaticSetting with the specified name for the specified instance
      *
      * @param source      the object containing the setting
      * @param settingName the name of the setting
      * @return the value of the setting for the specified object
      */
     private Object getAutoSettingValue(Object source, String settingName) {
         Field field = getAutomaticSettings(source).get(settingName);
         if (field != null) {
             try {
                 field.setAccessible(true);
                 return field.get(source);
             } catch (IllegalAccessException e) {
                 e.printStackTrace();
             } finally {
                 field.setAccessible(false);
             }
         }
         return null;
     }
 
     private boolean setAutoSettingValue(String settingName, String settingValue) {
         return setAutoSettingValue(this, settingName, settingValue);
     }
 
     /**
      * Update the value of the specified setting for the specified object.  The new setting value will
      * be persisted in the configuration for this CowNetThingy.
      *
      * @param source       The object whos setting will change
      * @param settingName  The name of the setting
      * @param settingValue The new value that will be set and persisted.
      * @return true if the new value was set.  false if the setting value was unchanged.
      */
     protected final boolean setAutoSettingValue(Object source, String settingName, String settingValue) {
         Field field = getAutomaticSettings(source).get(settingName);
         if (field != null) {
             boolean isStatic = Modifier.isStatic(field.getModifiers());
             boolean isClass = source instanceof Class;
             if (isStatic == isClass) {
                 try {
                     field.setAccessible(true);
                     if (field.getType().equals(Boolean.class) || field.getType().equals(boolean.class)) {
                         Boolean value = Boolean.valueOf(settingValue);
                         field.set(source, value);
                         updateConfigValue(settingName, value);
                         return true;
                     } else if (field.getType().equals(Integer.class) || field.getType().equals(int.class)) {
                         Integer value = Integer.valueOf(settingValue);
                         field.set(source, value);
                         updateConfigValue(settingName, value);
                         return true;
                     } else if (field.getType().equals(Long.class) || field.getType().equals(long.class)) {
                         Long value = Long.valueOf(settingValue);
                         field.set(source, value);
                         updateConfigValue(settingName, value);
                         return true;
                     } else if (field.getType().equals(Double.class) || field.getType().equals(double.class)) {
                         Double value = Double.valueOf(settingValue);
                         field.set(source, value);
                         updateConfigValue(settingName, value);
                         return true;
                     } else if (field.getType().equals(String.class)) {
                         field.set(source, settingValue);
                         updateConfigValue(settingName, settingValue);
                         return true;
                     }
                 } catch (IllegalAccessException e) {
                     e.printStackTrace();
                 } finally {
                     field.setAccessible(false);
                 }
             }
         }
         return false;
     }
 
     // reload settings that are handled by the settings magic
     private void reloadAutoSettings() {
         reloadAutoSettings(this);
     }
 
     /**
      * Reload all the automatic settings for the specified object.  The source of the new setting
      * values is this CowNetThingy.
      *
      * @param source Object instance whos settings should be reloaded.
      */
     protected final void reloadAutoSettings(Object source) {
         HashMap<String, Field> settings = getAutomaticSettings(source);
         for (String settingName : settings.keySet()) {
             Field field = settings.get(settingName);
             try {
                 field.setAccessible(true);
                 if (field.getType().equals(Boolean.class) || field.getType().equals(boolean.class)) {
                     field.setBoolean(source, getConfigValue(settingName, field.getBoolean(source)));
                 } else if (field.getType().equals(Integer.class) || field.getType().equals(int.class)) {
                     field.setInt(source, getConfigValue(settingName, field.getInt(source)));
                 } else if (field.getType().equals(Long.class) || field.getType().equals(long.class)) {
                     field.setLong(source, getConfigValue(settingName, field.getLong(source)));
                 } else if (field.getType().equals(Double.class) || field.getType().equals(double.class)) {
                     field.setDouble(source, getConfigValue(settingName, field.getDouble(source)));
                 } else if (field.getType().equals(String.class)) {
                     field.set(source, getConfigValue(settingName, field.get(source).toString()));
                 }
             } catch (IllegalAccessException e) {
                 e.printStackTrace();
             } finally {
                 field.setAccessible(false);
             }
         }
     }
 
     private HashMap<String, Field> getAutomaticSettings() {
         return getAutomaticSettings(this);
     }
 
     // return a Map of (settingName -> field) for the specified object
     private HashMap<String, Field> getAutomaticSettings(Object source) {
         Class clazz = (source instanceof Class) ? (Class) source : source.getClass();
         HashMap<String, Field> settings = new HashMap<String, Field>();
         while (clazz != null && !clazz.equals(Object.class)) {
             for (Field f : clazz.getDeclaredFields()) {
                 f.setAccessible(true);
                 try {
                     if (f.isAnnotationPresent(Setting.class)) {
                         Setting settingAnnotation = f.getAnnotation(Setting.class);
                         String name = (settingAnnotation.name().isEmpty()) ? f.getName() : settingAnnotation.name();
                         settings.put(name, f);
                     }
                 } finally {
                     f.setAccessible(false);
                 }
             }
             clazz = clazz.getSuperclass();
         }
         return settings;
     }
 
     /**
      * Return a map of all the automaticSettings for the specified object.  The key is the
      * name of the setting and the value is the settings current value.
      *
      * @param source object to pull settings from
      * @return a map of (settingName -> settingValue)
      */
     protected final HashMap<String, String> getSettingValueMapFor(Object source) {
         //this is a very expensive type cast.  I hate java generics.
         HashMap<String, Field> auto = getAutomaticSettings(source);
         HashMap<String, String> result = new HashMap<String, String>(auto.size());
         for (String key : auto.keySet()) {
             Field field = auto.get(key);
             try {
                 field.setAccessible(true);
                 boolean isClass = source instanceof Class;
                 boolean isStatic = Modifier.isStatic(field.getModifiers());
                 if (isClass == isStatic) {
                     // only expose static for classes and non-statics for instances
                     result.put(key, field.get(source).toString());
                 }
             } catch (IllegalAccessException e) {
                 e.printStackTrace();
             } finally {
                 field.setAccessible(false);
             }
         }
         return result;
     }
 
 
     //------------------------------------
     // command dispatch methods
 
     public void loadCommands() {
         logInfo("commands: " + StringUtils.flatten(getHandlerMethods()));
     }
 
     /**
      * dump a list of available commands to sender
      */
     @CowCommand
     private boolean doCommands(CommandSender sender) {
         for (String i : getHandlerMethods(sender)) {
             sender.sendMessage(i);
         }
         return true;
     }
 
     private List<String> getHandlerMethods() {
         return getHandlerMethods(null);
     }
 
     private List<String> getHandlerMethods(CommandSender sender) {
         HashSet<String> methods = new HashSet<String>();
         Class clazz = getClass();
         while (clazz != Object.class) {
             for (Method method : clazz.getDeclaredMethods()) {
                 if (method.getAnnotation(CowCommand.class) != null) {
                     //OK, we found a command, only add it if the sender has access to it.
                     if (hasMethodPermissions(sender, method.getAnnotation(CowCommand.class))) {
                         // strip off COMMAND_METHOD_PREFIX at the beginning.
                         String name = method.getName().substring("do".length()).toLowerCase();
                         methods.add(name);
                     }
                 }
             }
             clazz = clazz.getSuperclass();
         }
 
         LinkedList<String> result = new LinkedList<String>();
         result.addAll(methods);
         Collections.sort(result);
         return result;
     }
 
     // dispatch alias methods, thingy methods, and last global methods
     private boolean dispatchMethod(CommandSender sender, Command cmd, String label, String[] args) {
         String[] bases = (label.equalsIgnoreCase(getTrigger()) ? new String[]{
                 "",
                 // global commands first
                 getTrigger(),
                 // then local commands
         } : new String[]{
                 "",
                 // global commands
                 label,
                 // then alias commands
                 getTrigger(),
                 // them base commands
         });
         for (String command : bases) {
             for (int i = args.length; i >= 0; i--) {
                 boolean senderIsPlayer = sender instanceof Player;
                 MethodSignature signature = new MethodSignature(sender, command, args, i);
                 Method method = signature.getMethod(this.getClass(), senderIsPlayer);
                 if ((method == null) && senderIsPlayer) {
                     method = signature.getMethod(this.getClass(), false);
                 }
                 debugInfo(signature.toString() + ((method == null) ? " not found" : "found"));
                 if (method != null) {
                     if (!hasMethodPermissions(sender, method.getAnnotation(CowCommand.class))) {
                         sender.sendMessage("You don't have permissions.");
                         return true;
                     }
 
                     try {
                         method.setAccessible(true);
                         Object result = method.invoke(this, signature.args);
                         return (Boolean) result;
                     } catch (InvocationTargetException e) {
                         e.printStackTrace();
                         return false;
                     } catch (IllegalAccessException e) {
                         e.printStackTrace();
                         return false;
                     } finally {
                         method.setAccessible(false);
                     }
                 }
             }
         }
 
         return false;
     }
 
     private boolean hasMethodPermissions(CommandSender sender, CowCommand annotation) {
         if (sender == null) return true;
         if (annotation.opOnly() && !sender.isOp()) {
             return false;
         } else {
             //if it's a player and the method has an annotated permission, check it.
             if (sender instanceof Player) {
                 Player player = (Player) sender;
                 String permNeeded = annotation.permission();
                 if (!permNeeded.isEmpty() && !hasPermissions(player, permNeeded)) {
                     sender.sendMessage("You don't have permissions.");
                     return false;
                 }
             }
             return true;
         }
     }
 
     public class MethodSignature {
         public final String methodName;
         public final Object[] args;
         public final Class[] signature;
 
         public MethodSignature(CommandSender player, String base, String[] argsIn, int split) {
             methodName = generateMethodName(base, argsIn, split);
             int argCount = argsIn.length - split;
             signature = new Class[argCount + 1];
             args = new Object[argCount + 1];
             signature[0] = CommandSender.class;
             args[0] = player;
             for (int i = split; i < argsIn.length; i++) {
                 args[i - split + 1] = argsIn[i];
                 signature[i - split + 1] = String.class;
             }
         }
 
         public Method getMethod(Class clazz, boolean forPlayer) {
             signature[0] = (forPlayer) ? Player.class : CommandSender.class;
             while (clazz != Object.class) {
                 try {
                     Method method = clazz.getDeclaredMethod(methodName, signature);
                     if (method.getReturnType().equals(boolean.class) && method.isAnnotationPresent(CowCommand.class)) {
                         return method;
                     }
                 } catch (NoSuchMethodException e) {
                     // didn't find one, so check super class
                 }
                 clazz = clazz.getSuperclass();
             }
             return null;
         }
 
         private String generateMethodName(String base, String[] args, int split) {
             StringBuilder result = new StringBuilder("do");
             result.append(bumpCase(base));
            if (split == 0 && result.equals("do")) {
                 // You can't have a method named "do()", so we change it to "doIt()"
                 result.append("It");
             } else {
                 for (int i = 0; i < split; i++) {
                     result.append(bumpCase(args[i]));
                 }
             }
             return result.toString();
         }
 
         private String bumpCase(String s) {
             StringBuilder result = new StringBuilder();
             if (s.length() >= 1) {
                 result.append(s.toUpperCase().substring(0, 1));
                 if (s.length() >= 2) {
                     result.append(s.toLowerCase().substring(1));
                 }
             }
             return result.toString();
         }
 
         public String toString() {
             StringBuilder result = new StringBuilder(methodName);
             result.append("(");
             result.append((args[0] instanceof Player) ? ((Player) args[0]).getName() : "console");
             for (int i = 1; i < args.length; i++) {
                 result.append(",");
                 result.append(args[i]);
             }
             result.append(")");
             return result.toString();
         }
     }
 
     protected final void broadcastToAllOnlinePlayers(String msg) {
         for (Player player : getPlugin().getServer().getOnlinePlayers()) {
             player.sendMessage(msg);
         }
     }
 }
