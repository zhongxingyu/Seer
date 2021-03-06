 /*
  * The FML Forge Mod Loader suite.
  * Copyright (C) 2012 cpw
  * 
  * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation; either version 2.1 of the License, or any later version.
  * 
  * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
  * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to the Free Software Foundation, Inc., 51
  * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
  */
 package fml;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.FileHandler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.logging.SimpleFormatter;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 import net.minecraft.src.BaseMod;
 import fml.ml.ModLoaderModContainer;
 
 public class Loader {
   private enum State {
     NOINIT, LOADING, PREINIT, INIT, POSTINIT, UP, ERRORED
   };
 
   private static Loader instance;
   private static Logger LOG = Logger.getLogger("ForgeModLoader.Loader");
 
   private static Pattern zipJar = Pattern.compile("([^\\s]+).(zip|jar)$");
   private static Pattern modClass = Pattern.compile("(.*/)(mod\\_[^\\s]+).class$");
 
   private static int major=Integer.parseInt("@MAJOR@");
   private static int minor=Integer.parseInt("@MINOR@");
   private static int rev  =Integer.parseInt("@REV@");
   private static int build=Integer.parseInt("@BUILD@");
   private static String mcversion="@MCVERSION@";
   
   private State state;
   private ModClassLoader modClassLoader;
   private List<ModContainer> mods;
   private Map<String,ModContainer> namedMods;
 
   public static Loader instance() {
    return instance;
   }
   private Loader() {
     Loader.LOG.setLevel(Level.ALL);
     FileHandler fileHandler;
     try {
       fileHandler = new FileHandler("ForgeModLoader-%g.log", 0, 3);
       System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tc %2$s%n%4$s: %5$s%6$s%n");
       fileHandler.setFormatter(new SimpleFormatter());
       fileHandler.setLevel(Level.ALL);
       Loader.LOG.addHandler(fileHandler);
     } catch (Exception e) {
       // Whatever - give up
     }
     LOG.info(String.format("Forge ModLoader version %d.%d.%d.%d for Minecraft %s loading.",major,minor,rev,build,mcversion));
   }
 
   private void sortModList() {
     // NOOP for a minute
   }
 
   private void preModInit() {
     state = State.PREINIT;
     for (ModContainer mod : mods) {
       if (mod.wantsPreInit()) {
         LOG.finer(String.format("Pre-initializing %s", mod.getName()));
         mod.preInit();
         namedMods.put(mod.getName(), mod);
       }
     }
   }
 
   private void modInit() {
     state = State.INIT;
     for (ModContainer mod : mods) {
       LOG.finer(String.format("Initializing %s", mod.getName()));
       mod.init();
     }
   }
 
   private void postModInit() {
     state = State.POSTINIT;
     for (ModContainer mod : mods) {
       if (mod.wantsPostInit()) {
         LOG.finer(String.format("Post-initializing %s", mod.getName()));
         mod.postInit();
       }
     }
   }
 
   private void load() {
     File modsDir = new File(".", "mods");
     String canonicalModsPath;
     try {
       canonicalModsPath = modsDir.getCanonicalPath();
     } catch (IOException ioe) {
       LOG.severe(String.format("Failed to resolve mods directory mods %s", modsDir.getAbsolutePath()));
       LOG.throwing("fml.server.Loader", "initialize", ioe);
       throw new LoaderException(ioe);
     }
     if (!modsDir.exists()) {
       LOG.fine(String.format("No mod directory found, creating one: %s", canonicalModsPath));
       try {
         modsDir.mkdir();
       } catch (Exception e) {
         LOG.throwing("fml.server.Loader", "initialize", e);
         throw new LoaderException(e);
       }
     }
     if (!modsDir.isDirectory()) {
       LOG.severe(String.format("Attempting to load mods from %s, which is not a directory", canonicalModsPath));
       LoaderException loaderException = new LoaderException();
       LOG.throwing("fml.server.Loader", "initialize", loaderException);
       throw loaderException;
     }
     File[] modList = modsDir.listFiles();
     // Sort the files into alphabetical order first
     Arrays.sort(modList);
 
     state = State.LOADING;
     for (File modFile : modList) {
       if (modFile.isDirectory()) {
         LOG.info(String.format("Found directory %s. Attempting load", modFile.getName()));
         attemptDirLoad(modFile);
         LOG.info(String.format("Directory %s loaded successfully", modFile.getName()));
       } else {
         Matcher matcher = zipJar.matcher(modFile.getName());
         if (matcher.matches()) {
           LOG.info(String.format("Found zip or jar file %s. Attempting load.", matcher.group(0)));
           attemptFileLoad(modFile);
           LOG.info(String.format("File %s loaded successfully.", matcher.group(0)));
         }
       }
     }
     if (state == State.ERRORED) {
       LOG.severe("A problem has occured during mod loading. Giving up now");
       throw new RuntimeException("Giving up please");
     }
   }
 
   private void attemptDirLoad(File modDir) {
     extendClassLoader(modDir);
 
     File[] content = modDir.listFiles(new FilenameFilter() {
       @Override
       public boolean accept(File dir, String name) {
         return modClass.matcher(name).find();
       }
     });
     for (File modClassFile : content) {
       String clazzName = modClass.matcher(modClassFile.getName()).group(2);
       LOG.fine(String.format("Found a mod class %s in directory %s. Attempting to load it", clazzName, modDir.getName()));
       loadModClass(modDir, modClassFile.getName(), clazzName);
       LOG.fine(String.format("Successfully loaded mod class %s", modClassFile.getName()));
     }
   }
 
   private void loadModClass(File classSource, String classFileName, String clazzName) {
     try {
       System.out.printf("Loading %s\n", clazzName);
       Class<?> clazz = Class.forName(clazzName, false, modClassLoader);
       System.out.printf("Got %s\n", clazz);
       if (clazz.isAnnotationPresent(Mod.class)) {
         // an FML mod
         mods.add(FMLModContainer.buildFor(clazz));
       } else if (BaseMod.class.isAssignableFrom(clazz)) {
         LOG.fine(String.format("ModLoader BaseMod class found: %s. Loading", clazzName));
         @SuppressWarnings("unchecked")
         Class<? extends BaseMod> bmClazz = (Class<? extends BaseMod>) clazz;
         ModContainer mc=new ModLoaderModContainer(bmClazz);
         mods.add(mc);
         LOG.fine(String.format("ModLoader BaseMod class loaded: %s.", clazzName));
       } else {
         // Unrecognized
       }
     } catch (Exception e) {
       LOG.warning(String.format("Failed to load mod class %s in %s", classFileName, classSource.getName()));
       LOG.throwing("fml.server.Loader", "attemptLoad", e);
       state = State.ERRORED;
     }
   }
 
   private void extendClassLoader(File file) {
     if (modClassLoader == null) {
       modClassLoader = new ModClassLoader();
     }
     try {
       modClassLoader.addFile(file);
     } catch (MalformedURLException e) {
       throw new LoaderException(e);
     }
   }
 
   private void attemptFileLoad(File modFile) {
     extendClassLoader(modFile);
 
     try {
       ZipFile jar = new ZipFile(modFile);
       for (ZipEntry ze : Collections.list(jar.entries())) {
         Matcher match = modClass.matcher(ze.getName());
         if (match.matches()) {
           String pkg = match.group(1).replace('/', '.');
           String clazzName = pkg + match.group(2);
           LOG.fine(String.format("Found a mod class %s in file %s. Attempting to load it", clazzName, modFile.getName()));
           loadModClass(modFile, ze.getName(), clazzName);
           LOG.fine(String.format("Mod class %s loaded successfully", clazzName, modFile.getName()));
         }
       }
     } catch (Exception e) {
       LOG.warning(String.format("Zip file %s failed to read properly", modFile.getName()));
       LOG.throwing("fml.server.Loader", "attemptFileLoad", e);
       state = State.ERRORED;
     }
   }
 
   public static List<ModContainer> getModList() {
     return instance().mods;
   }
 
   public void loadMods() {
     state = State.NOINIT;
     mods = new ArrayList<ModContainer>();
     load();
     sortModList();
     // Make mod list immutable
     mods=Collections.unmodifiableList(mods);
     preModInit();
   }
 
   public void initializeMods() {
     modInit();
     postModInit();
     state = State.UP;
     LOG.info("Forge Mod Loader load complete");
   }
 }
