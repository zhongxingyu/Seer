 /*
  *  Copyright 2010 mathieuancelin.
  * 
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  * 
  *       http://www.apache.org/licenses/LICENSE-2.0
  * 
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *  under the License.
  */
 package cx.ath.mancel01.webframework;
 
 import ch.qos.logback.classic.Level;
 import ch.qos.logback.classic.LoggerContext;
 import ch.qos.logback.classic.PatternLayout;
 import ch.qos.logback.core.ConsoleAppender;
 import ch.qos.logback.core.FileAppender;
 import cx.ath.mancel01.webframework.util.FileUtils.FileGrabber;
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author mathieuancelin
  */
 public class WebFramework {
 
     public static final Logger logger = LoggerFactory.getLogger(WebFramework.class);
     public static final Properties config = new Properties();
 
     public static File ROOT;
     public static File SOURCES;
     public static File TARGET;
 
     public static File WEB_SOURCES;
     public static File VIEWS;
     public static File CONF;
     public static File PUBLIC_RESOURCES;
     public static File JAVA_SOURCES;
     public static File RESOURCES;
     
     public static File MVN_COMPILED_CLASSES_PATH;
     public static File FWK_COMPILED_CLASSES_PATH;
     public static File DB;
     public static File LOGS;
 
     public static boolean dev = false;
     public static boolean keepDefaultRoutes = true;
     public static boolean proxyInjectionForCompilation = false;
     public static boolean recompileServices = true;
     public static String classpath = "";
 
     public static FileGrabber grabber;
 
     private static Map<String, Class<?>> applicationClasses
             = new HashMap<String, Class<?>>();
 
     public static void init(File rootDir) {
         initFiles(rootDir);
         findApplicationClasses();
         initClasspath();
         initConfig();
         initLogger();
         if (dev) {
             if (!FWK_COMPILED_CLASSES_PATH.exists()) {
                 FWK_COMPILED_CLASSES_PATH.mkdir();
             }
         }
     }
 
     private static void initFiles(File rootDir) {
         ROOT = rootDir;
         SOURCES = new File(rootDir, "src");
         TARGET = new File(rootDir, "target");
         WEB_SOURCES = new File(SOURCES, "main/webapp");
         VIEWS = new File(WEB_SOURCES, "views");
         CONF = new File(WEB_SOURCES, "conf");
         PUBLIC_RESOURCES = new File(WEB_SOURCES, "public");
         JAVA_SOURCES = new File(SOURCES, "main/java");
         RESOURCES = new File(SOURCES, "main/resources");
         MVN_COMPILED_CLASSES_PATH = new File(TARGET, "classes");
         FWK_COMPILED_CLASSES_PATH = new File(TARGET, "compclasses");
         DB = new File(TARGET, "db");
         LOGS = new File(TARGET, "logs");
         createDir(TARGET);
         createDir(DB);
         createDir(MVN_COMPILED_CLASSES_PATH);
         createDir(FWK_COMPILED_CLASSES_PATH);
         createDir(LOGS);
         grabber = new FileGrabber() {
             @Override
             public File getFile(String file) {
                 return new File(ROOT, file);
             }
         };
     }
 
     private static boolean createDir(File dir) {
         if (!dir.exists()) {
             return dir.mkdirs();
         }
         return false;
     }
     
     private static void initClasspath() {
         ClassLoader sysClassLoader = ClassLoader.getSystemClassLoader();
         URL[] urls = ((URLClassLoader) sysClassLoader).getURLs();
         StringBuilder builder = new StringBuilder();
         for (int i = 0; i < urls.length; i++) {
             builder.append(urls[i].getFile());
             builder.append(":");
         }
         classpath = builder.toString();
         if (classpath.endsWith(":")) {
             classpath = classpath.substring(0, classpath.length() - 1);
         }
     }
 
     static void initConfig() {
         try {
            config.load(WebFramework.class.getClassLoader().getResourceAsStream("config.properties"));
             String mode = config.getProperty("framework.mode", "dev");
             if (mode.equals("dev")) {
                 dev = true;
             } else {
                 dev = false;
             }
             String recServ = config.getProperty("framework.recompile.services", "false");
             if (recServ.equals("true")) {
                 recompileServices = true;
             } else {
                 recompileServices = false;
             }
             String keep = config.getProperty("framework.keep.default.routes", "true");
             if (keep.equals("true")) {
                 keepDefaultRoutes = true;
             } else {
                 keepDefaultRoutes = false;
             }
         } catch (IOException e) {
             logger.error("Error while loading configuration file", e);
         }
     }
 
     private static void initLogger() {
         LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
         lc.stop(); lc.reset();
         ch.qos.logback.classic.Logger backLogger = (ch.qos.logback.classic.Logger) logger;
         ConsoleAppender consoleAppender = new ConsoleAppender();
         consoleAppender.setContext(lc);
         PatternLayout pl = new PatternLayout();
         pl.setPattern("%d{HH:mm:ss.SSS} %-5level - %msg%n");
         pl.setContext(lc);
         pl.start();
         consoleAppender.setLayout(pl);
         consoleAppender.start();
         backLogger.detachAndStopAllAppenders();
         if (!WebFramework.dev) {
             FileAppender fileAppender = new FileAppender();
             fileAppender.setContext(lc);
             pl = new PatternLayout();
             pl.setPattern("%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
             pl.setContext(lc);
             pl.start();
             fileAppender.setFile(TARGET.getAbsolutePath() + "/webframework.log");
             fileAppender.setLayout(pl);
             fileAppender.start();
             backLogger.addAppender(fileAppender);
         }
         backLogger.addAppender(consoleAppender);
         backLogger.setLevel(Level.valueOf(config.getProperty("framework.logger.level", "info")));
         lc.start();
     }
 
     public static Collection<Class<?>> getApplicationClasses() {
         return applicationClasses.values();
     }
 
     private static void findApplicationClasses() {
         List<String> classesNames = new ArrayList<String>();
         findClasses(classesNames, WebFramework.JAVA_SOURCES);
         for(String className : classesNames) {
             String name = className.replace(WebFramework.JAVA_SOURCES.getAbsolutePath() + "/", "").replace("/", ".").replace(".java", "");
             applicationClasses.put(name, null);
         }
         for(String name : applicationClasses.keySet()) {
             try {
                 applicationClasses.put(name, Class.forName(name));
             } catch (ClassNotFoundException ex) {
                 ex.printStackTrace();
             }
         }
     }
 
     private static void findClasses(List<String> builder, File file) {
         final File[] children = file.listFiles();
         if (children != null) {
             for (File f : children) {
                 if (f.isDirectory()) {
                     findClasses(builder, f);
                 }
                 if (f.isFile()) {
                     if (f.getName().endsWith(".java")) {
                         builder.add(f.getAbsolutePath());
                     }
                 }
             }
         }
     }
 
     //public static String compile = "javac -encoding utf-8 -source 1.6 -target 1.6 -d {1} -classpath {2} {3}";
     //public static String compile = "-encoding utf-8 -source 1.6 -target 1.6 -d {1} -classpath {2}";
     //compile = compile.replace("{2}", classpath);
     //compile = compile.replace("{1}", new File("target/compclasses").getAbsolutePath());
 }
