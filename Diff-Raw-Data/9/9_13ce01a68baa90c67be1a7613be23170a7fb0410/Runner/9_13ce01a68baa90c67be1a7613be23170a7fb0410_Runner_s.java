 package org.kevoree.boot;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 
 /**
  * Created by duke on 16/05/13.
  */
 public class Runner {
 
    private static final String runtimeURL = "http://maven.kevoree.org/release/org/kevoree/platform/org.kevoree.platform.standalone.min/kevoreeVersion/org.kevoree.platform.standalone.min-kevoreeVersion.jar";
 
     private static RuntimeDowloader downloader = new RuntimeDowloader();
 
     private static MavenVersionResolver snapshotResolver = new MavenVersionResolver();
 
     private static ChildManager childManager = new ChildManager();
 
     private static WatchDogCheck checker = new WatchDogCheck();
 
     public static void main(String[] args) throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
 
         Runtime.getRuntime().addShutdownHook(new Thread(childManager));
         System.out.println("Kevoree Boot Service");
         if (args.length != 2 && args.length != 1) {
             System.out.println("Usage : [kevoree.version] ([bootmodel])");
             System.exit(-1);
         }
         String kevoreeVersion = args[0].toString();
         String cleanRuntimeURL = runtimeURL.replaceAll("kevoreeVersion", kevoreeVersion);
         if (kevoreeVersion.endsWith("SNAPSHOT")) {
             cleanRuntimeURL = cleanRuntimeURL.replace("release", "snapshots");
             kevoreeVersion = kevoreeVersion.replace("SNAPSHOT", snapshotResolver.getLastVersion(cleanRuntimeURL));
             //CLEANUP URL WITH MAVEN RESOLVED VERSION
             cleanRuntimeURL = runtimeURL.replaceFirst("kevoreeVersion", args[0].toString());
             cleanRuntimeURL = cleanRuntimeURL.replaceAll("kevoreeVersion", kevoreeVersion);
             cleanRuntimeURL = cleanRuntimeURL.replace("release", "snapshots");
         }
         File runtime = downloader.get(cleanRuntimeURL, kevoreeVersion);
         checker.setRuntimeFile(runtime);
 
         //look for bootmodel
         if (args.length == 2) {
             String modelPath = args[1];
             File modelFile = new File(modelPath);
             if (modelFile.exists()) {
                 checker.setModelFile(modelFile);
             }
         }
 
         System.out.println("Kevoree " + kevoreeVersion + " Ready to run ");
         checker.startServer();
         checker.startKevoreeProcess();
     }
 
 }
