 package net.citizensnpcs.adventures.race;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.ExecutionException;
 
 import net.citizensnpcs.adventures.Adventures;
 import net.citizensnpcs.api.CitizensAPI;
 import net.citizensnpcs.api.scripting.ScriptFactory;
 import net.citizensnpcs.api.util.DataKey;
 import net.citizensnpcs.api.util.YamlStorage;
 
 import com.google.common.collect.Sets;
 
 public class RaceStorage {
     private boolean loadedLibs;
     private final Adventures plugin;
     private final RaceRegistry registry;
     private final File rootDirectory;
 
     public RaceStorage(Adventures plugin, File rootDirectory, RaceRegistry registry) {
         this.rootDirectory = rootDirectory;
         this.registry = registry;
         this.plugin = plugin;
         rootDirectory.mkdirs();
     }
 
     public void load() {
         for (File raceDirectory : rootDirectory.listFiles()) {
             if (!raceDirectory.isDirectory())
                 continue;
             for (File possibleInfoFile : raceDirectory.listFiles()) {
                 if (!possibleInfoFile.isFile() || !possibleInfoFile.getName().startsWith("info"))
                     continue;
                 RaceDescriptor desc = loadRaceDescriptor(possibleInfoFile);
                 if (desc != null) {
                     registry.registerRace(desc);
                 }
                 break;
             }
         }
     }
 
     private RaceDescriptor loadAsScript(File infoFile) {
         ScriptFactory factory = null;
         try {
             factory = CitizensAPI.getScriptCompiler().compile(infoFile).beginWithFuture().get();
         } catch (InterruptedException e) {
             e.printStackTrace();
         } catch (ExecutionException e) {
             e.printStackTrace();
         }
         if (factory == null)
             return null;
         Object object = factory.newInstance().invoke("getDescriptor", registry, infoFile.getParentFile());
         if (!(object instanceof RaceDescriptor))
             throw new RuntimeException("Expected RaceDescriptor, got " + object);
         return (RaceDescriptor) object;
     }
 
     private boolean loadDependencies(File file, DataKey root) throws IOException {
         PublicURLClassLoader loader = new PublicURLClassLoader(new URL[] {}, plugin.getClass().getClassLoader());
         if (!loadedLibs) {
             File libs = new File(rootDirectory, "lib");
             if (libs.exists()) {
                 for (File lib : libs.listFiles()) {
                     if (!lib.getName().endsWith(".jar"))
                         continue;
                     try {
                         loader.addURL(lib.toURI().toURL());
                     } catch (MalformedURLException e) {
                         e.printStackTrace(); // should never happen
                     }
                 }
             }
             loadedLibs = true;
         }
         if (!root.keyExists("depend")) {
             return true;
         }
         Set<URL> urls = Sets.newHashSet();
         for (String dependency : root.<List<String>> getRawUnchecked("depend")) {
             File dep = new File(file, dependency + ".jar");
             if (!dep.exists()) {
                 return false;
             }
             try {
                 urls.add(dep.toURI().toURL());
             } catch (MalformedURLException e) {
                 e.printStackTrace(); // should never happen
             }
         }
         for (URL url : urls) {
             loader.addURL(url);
         }
         return true;
     }
 
     private RaceDescriptor loadFromYaml(YamlStorage storage) {
         DataKey root = storage.getKey("");
         String name = root.getString("name");
         if (name == null)
             return null;
         try {
             if (!loadDependencies(storage.getFile(), root))
                 return null;
         } catch (IOException e) {
             e.printStackTrace();
             return null;
         }
         TribeGenerator gen = new TribeGenerator();
         gen.setNPCSupplier(new FlatfileNPCSupplier(storage));
         gen.addDecorator(new FlatfileBehaviorDecorator(storage));
         RaceDescriptor race = RaceDescriptor.builder(registry, name).generator(gen)
                 .folder(storage.getFile().getParentFile()).build();
         return race;
     }
 
     private RaceDescriptor loadRaceDescriptor(File infoFile) {
         if (infoFile.getName().contains(".yml")) {
             YamlStorage storage = new YamlStorage(infoFile);
             if (!storage.load())
                 throw new RuntimeException("Couldn't load " + infoFile.getName());
             return loadFromYaml(storage);
         }
         return loadAsScript(infoFile);
     }
 
     private static class PublicURLClassLoader extends URLClassLoader {
         public PublicURLClassLoader(URL[] urls, ClassLoader parent) {
             super(urls, parent);
         }
 
         @Override
         public void addURL(URL url) {
             super.addURL(url);
         }
     }
 }
