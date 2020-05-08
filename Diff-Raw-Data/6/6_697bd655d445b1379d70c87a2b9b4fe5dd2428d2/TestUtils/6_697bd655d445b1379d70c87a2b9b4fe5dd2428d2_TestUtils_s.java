 package sf.pnr.tests;
 
 import sf.pnr.base.Board;
 import sf.pnr.base.Configurable;
 import sf.pnr.base.Configuration;
 import sf.pnr.base.StringUtils;
 import sf.pnr.io.UCI;
 import sf.pnr.io.UncloseableOutputStream;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.lang.reflect.UndeclaredThrowableException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.regex.Pattern;
 
 public class TestUtils {
 
     public static UciRunner[] getEngines() throws IOException {
         UciRunner[] engines = getEngines(System.getProperty("searchTask.engines"));
         final boolean includeLatest = Boolean.parseBoolean(System.getProperty("searchTask.includeLatest", "false"));
         if (includeLatest) {
             final UciRunner[] enginesWithLatest = new UciRunner[engines.length + 1];
             System.arraycopy(engines, 0, enginesWithLatest, 1, engines.length);
             engines = enginesWithLatest;
             final Map<String, String> options = new HashMap<String, String>();
             options.put(UCI.toUciOption(Configurable.Key.TRANSP_TABLE_SIZE), "128");
             options.put(UCI.toUciOption(Configurable.Key.EVAL_TABLE_SIZE), "8");
             options.putAll(getDefaultConfigs());
             final Map<Configurable.Key, String> systemProps = Configuration.preprocess(System.getProperties(), true);
             for (Map.Entry<Configurable.Key, String> entry: systemProps.entrySet()) {
                 options.put(UCI.toUciOption(entry.getKey()), entry.getValue());
             }
             engines[0] = new UciRunner("Pawns N' Roses Latest", options, new PipedUciProcess());
         }
         final String debugFile = System.getProperty("searchTask.debugFile");
         if (debugFile != null) {
             final UncloseableOutputStream os = new UncloseableOutputStream(new FileOutputStream(debugFile));
             for (UciRunner player: engines) {
                 player.setDebugOutputStream(os, player.getName() + " ");
                 os.incCounter();
             }
         }
         System.out.println("Engines found: " + Arrays.toString(engines));
         return engines;
     }
 
     public static UciRunner[] getReferenceEngines() throws IOException {
         final String refEnginePattern = System.getProperty("searchTask.referenceEngines");
         final UciRunner[] engines = getEngines(refEnginePattern);
         System.out.println("Reference engines found: " + Arrays.toString(engines));
         return engines;
     }
 
     public static UciRunner[] getEngines(final String patternStr) throws IOException {
         System.out.println("Searching for engines with pattern " + patternStr);
         if (patternStr == null || patternStr.trim().length() == 0) {
             return new UciRunner[0];
         }
         final File parent = getRootDir(patternStr);
         final List<File> allFiles = collectAllFiles(parent);
         final List<File> matches = new ArrayList<File>();
         int patternStart = parent.getAbsolutePath().length();
         if (!parent.getAbsolutePath().endsWith(File.pathSeparator)) {
             patternStart++;
         }
         if (patternStart < patternStr.length()) {
             final Pattern pattern = Pattern.compile(patternStr.substring(patternStart));
             for (File child: allFiles) {
                 if (pattern.matcher(child.getAbsolutePath().substring(patternStart)).matches()) {
                     matches.add(child);
                 }
             }
         } else {
             matches.addAll(allFiles);
         }
         Map<String, String> defaults = null;
         final List<UciRunner> engines = new ArrayList<UciRunner>(matches.size());
         for (final File file: matches) {
             final String name = getPlayerName(file);
             if (file.getName().endsWith(".ini")) {
                 try {
                     if (defaults == null) {
                         defaults = getDefaultConfigs();
                     }
                     engines.add(new UciRunner(name, getConfigs(file, defaults), new PipedUciProcess()));
                 } catch (Throwable e) {
                     // skip the file
                 }
             } else {
                 final File configFile = new File(file.getParentFile(), file.getName() + ".ini");
                 final Map<String, String> configs = getUnprocessedConfigs(configFile);
                 engines.add(new UciRunner(name, configs, new ExternalUciProcess(file.getAbsolutePath())));
             }
         }
         return engines.toArray(new UciRunner[engines.size()]);
     }
 
     private static File getRootDir(final String patternStr) {
         File parent = new File(patternStr);
         while (!parent.exists()) {
             parent = parent.getParentFile();
         }
         return parent;
     }
 
     static List<File> collectAllFiles(final File file) {
         final List<File> files = new ArrayList<File>();
         if (file.isDirectory()) {
             final File[] children = file.listFiles();
             for (File child: children) {
                 files.addAll(collectAllFiles(child));
             }
         } else {
             files.addAll(Collections.singletonList(file));
         }
         return files;
     }
 
     public static File getEngineDir() {
         final String pattern = System.getProperty("searchTask.engines");
         if (pattern == null || pattern.length() == 0) {
             return null;
         } else {
             return getRootDir(pattern);
         }
     }
 
     public static String getPlayerName(final File executable) {
         final String fileName = executable.getName();
         final int pos = fileName.lastIndexOf('.');
         final String name;
         if (pos != -1 && pos >= fileName.length() - 4) {
             name = fileName.substring(0, pos);
         } else {
             name = fileName;
         }
         return name;
     }
 
     public static Map<String, String> getDefaultConfigs() throws IOException {
         final Map<String, String> defaults = new HashMap<String, String>();
         for (Configurable.Key key: Configurable.Key.values()) {
             final String value = Configuration.getInstance().getString(key);
             defaults.put(UCI.toUciOption(key), value);
         }
         final String defaultConfigFile = System.getProperty("searchTask.defaultConfigs");
         if (defaultConfigFile != null) {
             System.out.println("Loading default values from " + defaultConfigFile);
             final Properties defaultOverwrites = new Properties();
             final FileReader reader = new FileReader(defaultConfigFile);
             try {
                 defaultOverwrites.load(reader);
             } finally {
                 reader.close();
             }
             for (String name: defaultOverwrites.stringPropertyNames()) {
                defaults.put(name, defaultOverwrites.getProperty(name));
             }
         }
         return defaults;
     }
 
     public static Map<String, String> getConfigs(final File file, final Map<String, String> defaults) throws IOException {
         final Properties properties = new Properties();
         final FileReader reader = new FileReader(file);
         try {
             properties.load(reader);
         } finally {
             reader.close();
         }
         final Map<Configurable.Key, String> configs = Configuration.preprocess(properties);
         final Map<String, String> uciOptions = new HashMap<String, String>(defaults);
         for (Map.Entry<Configurable.Key, String> entry: configs.entrySet()) {
             uciOptions.put(UCI.toUciOption(entry.getKey()), entry.getValue());
         }
         return uciOptions;
     }
 
     private static Map<String, String> getUnprocessedConfigs(final File file) throws IOException {
         final Map<String, String> configs = new HashMap<String, String>();
         if (file.exists()) {
             final Properties properties = new Properties();
             properties.load(new FileReader(file));
             for (String key: properties.stringPropertyNames()) {
                 configs.put(key, properties.getProperty(key));
             }
         }
         return configs;
     }
 
     public static void compute(final UciRunner engine, final Board board, final int depth, final int time) {
         compute(engine, board, depth, time, true);
     }
 
     public static void compute(final UciRunner engine, final Board board, final int depth, final int time, final boolean newEngine) {
         Throwable t = null;
         try {
             if (newEngine) {
                 engine.restart();
             }
             engine.uciNewGame();
             engine.position(board);
             if (time == 0) {
                 engine.go(depth, 0);
             } else {
                 engine.go(0, time);
             }
         } catch (IOException e) {
             t = e;
         } finally {
             if (newEngine) {
                 try {
                     engine.close();
                 } catch (IOException e) {
                     if (t != null) {
                         // do nothing, propagate the outer exception
                         e.printStackTrace(System.out);
                     } else {
                         t = e;
                     }
                 }
             }
         }
         if (t != null) {
             throw new UndeclaredThrowableException(t, String.format("Engine '%s' failed on test FEN '%s'",
                 engine.getName(), StringUtils.toFen(board)));
         }
     }
 
     public static int getMaxNameLen(final UciRunner[] engines) {
         int maxNameLen = 0;
         for (UciRunner runner: engines) {
             final int nameLen = runner.getName().length();
             if (nameLen > maxNameLen) {
                 maxNameLen = nameLen;
             }
         }
         return maxNameLen;
     }
 
     public static String getFileNameWithoutExtension(final File file) {
         final String name = file.getName();
         final int pos = name.lastIndexOf('.');
         final String result;
         if (pos > 0) {
             result = name.substring(0, pos);
         } else {
             result = name;
         }
         return result;
     }
 }
