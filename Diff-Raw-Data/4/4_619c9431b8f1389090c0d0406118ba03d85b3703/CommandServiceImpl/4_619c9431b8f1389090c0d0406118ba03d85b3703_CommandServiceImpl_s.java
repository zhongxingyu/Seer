 package com.cloudbees.sdk;
 
 import com.cloudbees.sdk.cli.*;
 import com.cloudbees.sdk.extensibility.AnnotationLiteral;
 import com.cloudbees.sdk.extensibility.ExtensionFinder;
 import com.cloudbees.sdk.extensibility.ExtensionPointList;
 import com.cloudbees.sdk.utils.Helper;
 import com.google.inject.*;
 import com.thoughtworks.xstream.XStream;
 
 import javax.inject.Inject;
 import javax.inject.Singleton;
 import java.io.*;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  */
 @Singleton
 public class CommandServiceImpl implements CommandService {
     private static final Logger LOGGER = Logger.getLogger(CommandServiceImpl.class.getName());
 
     static final String NL = System.getProperty("line.separator");
 
     @Inject
     private Injector injector;
 
     @Inject
     @ExtensionClassLoader
     private ClassLoader extClassLoader;
 
     @Inject
     private ExtensionPointList<CommandResolver> resolvers;
 
     @Inject
     private Verbose verbose;
 
     DirectoryStructure structure;
 
     List<Plugin> plugins;
     boolean localRepoLoaded;
 
     static XStream xstream;
 
     @Inject
     public CommandServiceImpl(DirectoryStructure structure) {
         this.structure = structure;
         localRepoLoaded = false;
     }
 
     public void loadCommandProperties() {
         plugins = loadCommandFiles(structure.sdkRepository, structure.pluginExtension);
     }
 
     private ArrayList<Plugin> loadCommandFiles(File dir, String fileExtension) {
         ArrayList<Plugin> plugins = new ArrayList<Plugin>();
 
         String[] files = Helper.getFiles(dir, fileExtension);
         if (files != null) {
             for (String file : files) {
                 plugins.addAll(loadPlugins(new File(dir, file)));
             }
         }
 
         return plugins;
     }
 
     private ArrayList<Plugin> loadPlugins(File commandFile) {
         ArrayList<Plugin> plugins = new ArrayList<Plugin>();
 
         FileReader reader = null;
         try {
             reader = new FileReader(commandFile);
             Plugin plugin = (Plugin) getXStream().fromXML(reader);
             plugins.add(plugin);
         } catch (IOException ex) {
             System.err.println("ERROR: Cannot find file: " + commandFile);
         } finally {
             if (reader != null) try {
                 reader.close();
             } catch (IOException ignored) {
             }
         }
 
         return plugins;
     }
 
     public ICommand getCommand(String name) throws IOException {
         PluginCommand pluginCommand = getPluginCommand(name, plugins);
 
         // Look for additional command definition in the local repository
         if (pluginCommand == null) {
             if (!localRepoLoaded) {
                 List<Plugin> localRepoCmds = loadCommandFiles(structure.getPluginDir(), structure.pluginExtension);
                 localRepoLoaded = true;
                 plugins.addAll(localRepoCmds);
                 pluginCommand = getPluginCommand(name, localRepoCmds);
             }
         }
 
         ICommand command = null;
 
         if (pluginCommand != null) {
             command = getCommand(name, pluginCommand);
         } else {
             // Try to find the plugin via bindings
             for (Plugin plugin : plugins) {
                 command = getInjectorCommand(name, plugin.getJars());
             }
         }
 //            command.setCommandProperties(commandProp);
 
         if (command != null)
             command.setJars(pluginCommand.plugin.getJars());
 
         return command;
     }
 
     public int getCount() {
         return plugins.size();
     }
 
     public String getHelp(URL helpTitleFile, String groupHelp, boolean all) {
         StringBuilder sb = new StringBuilder(getHelpTitle(helpTitleFile));
         Map<String, List<CommandProperties>> map = new LinkedHashMap<String, List<CommandProperties>>();
         if (!localRepoLoaded) plugins.addAll(loadCommandFiles(structure.getPluginDir(), structure.pluginExtension));
         for (Plugin plugin : plugins) {
             for (CommandProperties cmd : plugin.getProperties()) {
                 if (cmd.getGroup() != null && (!cmd.isExperimental() || all)) {
                     List<CommandProperties> list = map.get(cmd.getGroup());
                     if (list == null) {
                         list = new ArrayList<CommandProperties>();
                         map.put(cmd.getGroup(), list);
                     }
                     list.add(cmd);
                 }
             }
         }
         for (String group : map.keySet()) {
             sb.append(NL).append(group).append(" ").append(groupHelp).append(NL);
             for (CommandProperties cmd : map.get(group)) {
                 sb.append("    ").append(cmd.getName());
                 if (cmd.getDescription() != null)
                     sb.append("      ").append(cmd.getDescription()).append(NL);
                 else
                     sb.append(NL);
             }
         }
         return sb.toString();
     }
 
     private StringBuffer getHelpTitle(URL helpTitleFile) {
         StringBuffer sb = new StringBuffer();
         BufferedReader reader = null;
         try {
             reader = new BufferedReader(new InputStreamReader(helpTitleFile.openStream()));
             String line;
             while ((line = reader.readLine()) != null) {
                 sb.append(line).append(NL);
             }
         } catch (IOException ex) {
             System.err.println("ERROR: Cannot find help file: " + helpTitleFile);
         } finally {
             if (reader != null) try {
                 reader.close();
             } catch (IOException ignored) {
             }
         }
         return sb;
     }
 
     private PluginCommand getPluginCommand(String commandName, List<Plugin> plugins) {
         PluginCommand pluginCommand = null;
         for (Plugin plugin : plugins) {
             for (CommandProperties commandProperties : plugin.getProperties()) {
                 if (commandName.matches(commandProperties.getPattern())) {
                     if (pluginCommand != null) {
                         if (commandProperties.getPriority() < pluginCommand.commandProperties.getPriority())
                             pluginCommand = new PluginCommand(plugin, commandProperties);
                     } else
                         pluginCommand = new PluginCommand(plugin, commandProperties);
                 }
             }
         }
         return pluginCommand;
     }
 
     /**
      * Look up a command from Guice,
      */
     private ICommand getInjectorCommand(String name, List<String> jars) throws IOException {
         try {
             Injector injector = this.injector;
 
 
             if (jars != null) {
                 List<URL> urls = new ArrayList<URL>();
                 for (String jar : jars) {
                     urls.add(new File(jar).toURI().toURL());
                 }
                 extClassLoader = createClassLoader(urls, extClassLoader);
                 injector = createChildModule(injector, extClassLoader);
             }
 
             // CommandResolvers take precedence over our default
             for (CommandResolver cr : resolvers.list(injector)) {
                 ICommand cmd = cr.resolve(name);
                 if (cmd!=null)
                     return cmd;
             }
 
             Provider<ICommand> p;
             try {
                 p = injector.getProvider(Key.get(ICommand.class, AnnotationLiteral.of(CLICommand.class, name)));
             } catch (ConfigurationException e) {
                 if (verbose.isVerbose()) LOGGER.log(Level.WARNING, "failed to find the command", e);
                 return null; // failed to find the command
             }
             return p.get();
         } catch (IOException e) {
             throw (IOException) new IOException("Failed to resolve command: " + name).initCause(e);
         } catch (InstantiationException e) {
             throw (IOException) new IOException("Failed to resolve command: " + name).initCause(e);
         }
     }
 
     /**
      * Creates a classloader from all the artifacts resolved thus far.
      */
     private ClassLoader createClassLoader(List<URL> urls, ClassLoader parent) {
         // if (urls.isEmpty()) return parent;  // nothing to load // this makes it hard to differentiate newly loaded stuff from what's already visible
         return new URLClassLoader(urls.toArray(new URL[urls.size()]), parent);
     }
 
     private ICommand getCommand(String name, PluginCommand pluginCommand) throws IOException {
         ICommand command;
         try {
             String className = pluginCommand.commandProperties.getClassName();
             List<String> jars = pluginCommand.plugin.getJars();
             if (jars != null) {
                 List<URL> urls = new ArrayList<URL>();
                 for (String jar : jars) {
                     urls.add(new File(jar).toURI().toURL());
                 }
                 extClassLoader = createClassLoader(urls, extClassLoader);
                 injector = createChildModule(injector, extClassLoader);
             }
             Provider<ICommand> p;
             try {
                 Class cl = Class.forName(className, true, extClassLoader);
                 p = injector.getProvider(Key.get(cl));
             } catch (ConfigurationException e) {
                 if (verbose.isVerbose()) LOGGER.log(Level.WARNING, "failed to find the command", e);
                 return null; // failed to find the command
             }
             command = p.get();
         } catch (Exception e) {
             throw (IOException) new IOException("Failed to resolve command: " + name).initCause(e);
         }
         return command;
     }
 
     private static XStream getXStream() {
         if (xstream == null) {
 //            long start = System.currentTimeMillis();
             xstream = new XStream();
 /*
             xstream.alias("plugin", Plugin.class);
             xstream.addImplicitCollection(Plugin.class, "properties");
             xstream.alias("command", CommandProperties.class);
 */
             xstream.processAnnotations(Plugin.class);
             xstream.processAnnotations(CommandProperties.class);
 //            System.out.println("XStream: " + (System.currentTimeMillis()-start) + " ms");
         }
         return xstream;
     }
 
     protected Injector createChildModule(Injector parent, final ClassLoader cl) throws InstantiationException, IOException {
         final List<Module> childModules = new ArrayList<Module>();
         childModules.add(new ExtensionFinder(cl) {
             @Override
             protected <T> void bind(Class<? extends T> impl, Class<T> extensionPoint) {
                 if (impl.getClassLoader() != cl) return; // only add newly discovered stuff
 
                 // install CLIModules
                 if (extensionPoint == CLIModule.class) {
                     try {
                         install((Module) impl.newInstance());
                     } catch (InstantiationException e) {
                         throw (Error) new InstantiationError().initCause(e);
                     } catch (IllegalAccessException e) {
                         throw (Error) new IllegalAccessError().initCause(e);
                     }
                     return;
                 }
                 super.bind(impl, extensionPoint);
             }
         });
 
         return parent.createChildInjector(childModules);
     }
 
     class PluginCommand {
         Plugin plugin;
         CommandProperties commandProperties;
 
         PluginCommand() {
         }
 
         PluginCommand(Plugin plugin, CommandProperties commandProperties) {
             this.plugin = plugin;
             this.commandProperties = commandProperties;
         }
 
         public Plugin getPlugin() {
             return plugin;
         }
 
         public void setPlugin(Plugin plugin) {
             this.plugin = plugin;
         }
 
         public CommandProperties getCommandProperties() {
             return commandProperties;
         }
 
         public void setCommandProperties(CommandProperties commandProperties) {
             this.commandProperties = commandProperties;
         }
     }
 }
