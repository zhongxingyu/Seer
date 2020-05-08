 package de.skuzzle.polly.core;
 
 
 import de.skuzzle.polly.core.util.ProxyClassLoader;
 
 public class PollyBootstrapper {
 
     public static void main(String[] args) throws Throwable {
         // Make sure that all classes are loaded by our own classloader
         ProxyClassLoader pcl = new ProxyClassLoader();
        pcl.invokeMain("polly.Polly", args);
     }
 }
