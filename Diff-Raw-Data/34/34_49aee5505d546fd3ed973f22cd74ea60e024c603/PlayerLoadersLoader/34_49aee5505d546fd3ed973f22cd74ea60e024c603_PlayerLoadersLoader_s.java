 package com.xebia.xke.algo.minimax.connect4;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 public class PlayerLoadersLoader {
 
     private File jarDirectory;
 
     public PlayerLoadersLoader(File jarDirectory) {
         this.jarDirectory = jarDirectory;
     }
 
    public Map<String, PlayerLoader> loadAllPlayers() throws PlayerLoadingException {
         Map<String, PlayerLoader> playerLoaders = new HashMap<String,PlayerLoader>();
 
         File[] jarFiles = jarDirectory.listFiles(new FilenameFilter() {
             @Override
             public boolean accept(File file, String s) {
                 if (s.endsWith(".jar")) {
                     return true;
                 }
                 return false;
             }
 
         });
         for (File jarFile : jarFiles) {
            PlayerLoader playerLoader = loadPlayer(jarFile);

            playerLoaders.put(playerLoader.getName(), playerLoader);
         }
         return playerLoaders;
     }
 
     private PlayerLoader loadPlayer(File jarFile) throws PlayerLoadingException {
         if (!jarFile.exists()) {
             throw new PlayerLoadingException("Jar file doesn't exist : " + jarFile.getAbsolutePath());
         }
         try {
             URLClassLoader classLoader = URLClassLoader.newInstance(new URL[] {jarFile.toURI().toURL()});
             Properties properties = getPlayerProperties(jarFile, classLoader);
             Class<? extends Player> clazz = (Class<? extends Player>) classLoader.loadClass(properties.getProperty("player.class"));
             String name = properties.getProperty("player.name");
             String gravatarMail = properties.getProperty("player.gravatar.mail");
             return new PlayerLoader(name, clazz, gravatarMail);
         } catch (MalformedURLException e) {
             throw new PlayerLoadingException("Cannot load player from jar : " + jarFile.getAbsolutePath(), e);
         } catch (ClassNotFoundException e) {
             throw new PlayerLoadingException("Cannot load player from jar : " + jarFile.getAbsolutePath(), e);
         }
     }
 
     private Properties getPlayerProperties(File jarFile, URLClassLoader classLoader) throws PlayerLoadingException {
         InputStream playerPropertiesIS = null;
         Properties properties = new Properties();
         try {
             URL playerPropertiesURL = classLoader.findResource("player.properties");
 
             if (playerPropertiesURL == null) {
                 throw new PlayerLoadingException("Cannot load player.properties from jar : " + jarFile.getAbsolutePath());
             }
 
             playerPropertiesIS = playerPropertiesURL.openStream();
             properties.load(playerPropertiesIS);
             return properties;
         } catch (IOException e) {
             throw new PlayerLoadingException("Cannot load player.properties from jar : " + jarFile.getAbsolutePath(), e);
         } finally {
             if (playerPropertiesIS != null) {
                 try {
                     playerPropertiesIS.close();
                 } catch (IOException e) {
                     //
                 }
             }
         }
     }
 
 }
