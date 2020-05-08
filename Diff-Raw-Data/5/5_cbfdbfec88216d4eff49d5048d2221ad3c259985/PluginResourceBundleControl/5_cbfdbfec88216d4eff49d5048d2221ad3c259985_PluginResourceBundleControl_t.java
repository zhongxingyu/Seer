 package fr.aumgn.bukkitutils.localization;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 import java.util.ResourceBundle;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.google.common.base.Charsets;
 
 import fr.aumgn.bukkitutils.localization.bundle.PluginJsonResourceBundle;
 import fr.aumgn.bukkitutils.localization.bundle.PluginPropertyResourceBundle;
 import fr.aumgn.bukkitutils.localization.bundle.PluginResourceBundle;
 import fr.aumgn.bukkitutils.localization.bundle.PluginYmlResourceBundle;
 
 public class PluginResourceBundleControl extends ResourceBundle.Control {
 
     private final JavaPlugin plugin;
     private final File resourcesFolder;
 
     public PluginResourceBundleControl(JavaPlugin plugin, File resourcesFolder) {
         super();
         this.plugin = plugin;
         this.resourcesFolder = resourcesFolder;
     }
 
     @Override
     public List<String> getFormats(String baseName) {
         List<String> list = new ArrayList<String>();
 
         list.add("plugin.json");
         list.add("plugin.yml");
         list.add("plugin.properties");
 
         list.add("pluginjar.json");
         list.add("pluginjar.yml");
         list.add("pluginjar.properties");
 
         return list;
     }
 
     @Override
     public PluginResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
             throws IOException, IllegalAccessException, InstantiationException {
         InputStream iStream;
         String type;
         if (format.startsWith("pluginjar.")) {
             type = getType(format);
             iStream = plugin.getResource(toBundleName(baseName, locale, type));
             if (iStream == null) {
                 return null;
             }
         } else if (format.startsWith("plugin.")) {
             type = getType(format);
             String bundleName = toBundleName(baseName, locale, type);
             File file = new File(resourcesFolder, bundleName);
             if (!file.exists()) {
                 return null;
             }
 
             iStream = new FileInputStream(file);
         } else {
             throw new IllegalArgumentException("Unknown format: " + format);
         }
 
         Reader reader = new InputStreamReader(iStream, Charsets.UTF_8);
         if (type.equals("json")) {
             return new PluginJsonResourceBundle(reader);
         } else if (type.equals("yml")) {
             return new PluginYmlResourceBundle(reader);
         } else if (type.equals("properties")) {
             return new PluginPropertyResourceBundle(reader);
         } else {
             throw new IllegalArgumentException("Unknown format: " + format);
         }
     }
 
     private String getType(String format) {
        return format.split("\\.")[1];
     }
 
     private String toBundleName(String baseName, Locale locale, String extension) {
        return toBundleName(baseName, locale) + "." + extension;
     }
 }
