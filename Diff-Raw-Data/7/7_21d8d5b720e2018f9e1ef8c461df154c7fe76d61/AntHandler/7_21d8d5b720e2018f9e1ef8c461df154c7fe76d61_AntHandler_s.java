 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.cismet.jpresso.project.filetypes;
 
 import de.cismet.jpresso.core.serviceprovider.ClassResourceProvider;
 import de.cismet.jpresso.core.serviceprovider.ClassResourceProviderFactory;
 import de.cismet.jpresso.core.utils.URLTools;
 import de.cismet.jpresso.core.serviceprovider.JPressoFileManager;
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Properties;
 import java.util.Set;
 import org.apache.log4j.PropertyConfigurator;
 import org.apache.tools.ant.module.api.support.ActionUtils;
 import org.openide.DialogDisplayer;
 import org.openide.ErrorManager;
 import org.openide.NotifyDescriptor;
 import org.openide.NotifyDescriptor.Confirmation;
 import org.openide.cookies.SaveCookie;
 import org.openide.filesystems.FileObject;
 import org.openide.filesystems.FileUtil;
 import org.openide.loaders.DataObject;
 
 /**
  * This class provides static methods to execute all possible Ant tasks from
  * Netbeans. It collects all information about classpaths, project directory, 
  * etc. and passes them as arguments to the Ánt script.
  * 
  * @author srichter
  */
 public abstract class AntHandler {
 
     private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AntHandler.class);
     //Target Names
     private static final String ANT_TARGET_SINGLE_RUN = "runSingle";
     private static final String ANT_TARGET_CONVERT = "runConvert";
     private static final String ANT_TARGET_EXPORT = "runExport";
     private static final String ANT_TARGET_IMPORT = "runImport";
     private static final String ANT_TARGET_PROJECT = "runProject";
     private static final String ANT_TARGET_COMPILE = "runCompile";
     private static final String ANT_TARGET_RUN_JAVA = "runJava";
     //Script Variable Names
     private static final String JPCORE_JAR = "jpcore.jar";
     private static final String LIB_DIR = "lib.dir";
     private static final String RUN_SOURCE = "run.sourcefile";
     private static final String CONVERT_SOURCE = "convert.sourcefile";
     private static final String CONVERT_DEST = "convert.destdir";
     private static final String MERGE_PROPS = "merge.properties";
     private static final String EXPORT_SOURCE = "export.sourcedir";
     private static final String EXPORT_DEST = "export.destfile";
     private static final String IMPORT_SOURCE = "import.sourcedir";
     private static final String IMPORT_DEST = "import.destdir";
     private static final String ADDITIONAL_CLASSPATH = "add.classpath";
     private static final String COMPILE_FILELIST = "compile.filelist";
     private static final String RUN_CLASS = "run.class";
     //Memory settings
//    private static final String MAX_MEMORY = "memory.max";
//    private static final String MAX_MEMORY_VALUE = "512M";
 
     /**
      * 
      * @param buildXML
      * @param source
      */
     public static void startSingleRun(final FileObject buildXML, final DataObject jpRun) {
         //todo ckeck dass es auch ein run ist!
         if (jpRun != null) {
             final Properties p = createProjectAntProperties();
             final String file = jpRun.getPrimaryFile().getNameExt();
             //String dest = FileUtil.toFile(source.getPrimaryFile()).getAbsolutePath();
 
             p.put(RUN_SOURCE, file);
 //            p.put(MAX_MEMORY, MAX_MEMORY_VALUE);
             log.debug("Ant " + ANT_TARGET_SINGLE_RUN + " properties: " + RUN_SOURCE + " = " + file);
             if (canExecute(jpRun)) {
                 try {
                     ActionUtils.runTarget(buildXML, new String[]{ANT_TARGET_SINGLE_RUN}, p);
                 } catch (IOException e) {
                     ErrorManager.getDefault().notify(e);
                 }
             }
         } else {
             //TODO notify
         }
     }
 
     /**
      *
      * @param buildXML
      * @param sources
      */
     public static void startCompile(final FileObject buildXML, final Collection<? extends DataObject> sources) {
         //todo check dass es auch ein run ist!
         if (sources != null) {
             final Properties p = createProjectAntProperties();
             addAdditionalClassPathToProperties(p, buildXML);
             final StringBuilder sourcefiles = new StringBuilder();
             for (final DataObject dob : sources) {
                 sourcefiles.append(JPressoFileManager.DIR_CDE + "/" + dob.getPrimaryFile().getNameExt()).append(",");
             }
             if (sourcefiles.length() > 0) {
                 sourcefiles.deleteCharAt(sourcefiles.length() - 1);
             }
 //            p.put(MAX_MEMORY, MAX_MEMORY_VALUE);
             p.put(COMPILE_FILELIST, sourcefiles.toString());
             log.debug("Ant " + ANT_TARGET_COMPILE + " properties: " + COMPILE_FILELIST + " = " + sourcefiles.toString());
 
             try {
                 ActionUtils.runTarget(buildXML, new String[]{ANT_TARGET_COMPILE}, p);
             } catch (IOException e) {
                 ErrorManager.getDefault().notify(e);
 
             }
         } else {
             //TODO notify
         }
     }
 
     /**
      *
      * @param buildXML
      * @param sources
      */
     public static void startJava(final FileObject buildXML, final DataObject runClass) {
         //todo ckeck dass es auch ein run ist!
         if (runClass != null) {
             final Properties p = createProjectAntProperties();
             addAdditionalClassPathToProperties(p, buildXML);
 //            p.put(MAX_MEMORY, MAX_MEMORY_VALUE);
             p.put(RUN_CLASS, JPressoFileManager.DIR_CDE + "." + runClass.getPrimaryFile().getName());
             p.put(COMPILE_FILELIST, JPressoFileManager.DIR_CDE + "/" + runClass.getPrimaryFile().getNameExt());
             log.debug("Ant " + ANT_TARGET_RUN_JAVA + " properties: " + RUN_CLASS + " = " + runClass.getPrimaryFile().getPath());
 
             try {
                 ActionUtils.runTarget(buildXML, new String[]{ANT_TARGET_RUN_JAVA}, p);
             } catch (IOException e) {
                 ErrorManager.getDefault().notify(e);
 
             }
         } else {
             //TODO notify
         }
     }
 
     /**
      * 
      * @param buildXML
      * @param dest
      * @param src
      */
     public static void startConvert(final FileObject buildXML, final String files, String dir, String mergeProperties) {
         final Properties p = createProjectAntProperties();
         if (files != null && dir != null && mergeProperties != null) {
             p.put(CONVERT_SOURCE, files);
             p.put(CONVERT_DEST, dir);
             p.put(MERGE_PROPS, mergeProperties);
 //            p.put(MAX_MEMORY, MAX_MEMORY_VALUE);
             log.debug("Ant " + ANT_TARGET_CONVERT + " properties: " + CONVERT_SOURCE + " = " + files + ", " + CONVERT_DEST + " = " + dir);
             try {
                 ActionUtils.runTarget(buildXML, new String[]{ANT_TARGET_CONVERT}, p);
             } catch (IOException e) {
                 ErrorManager.getDefault().notify(e);
             }
         } else {
             //TODO notify
         }
     }
 
     /**
      * 
      * @param buildXML
      * @param dest
      * @param src
      */
     public static void startExport(final FileObject buildXML, final String dest) {
         if (dest != null) {
             final Properties p = createProjectAntProperties();
             String source = FileUtil.toFile(buildXML.getParent()).getAbsolutePath();
             p.put(EXPORT_SOURCE, source);
             p.put(EXPORT_DEST, dest);
 //            p.put(MAX_MEMORY, MAX_MEMORY_VALUE);
             log.debug("Ant " + ANT_TARGET_EXPORT + " properties: " + EXPORT_DEST + " = " + dest + ", " + EXPORT_SOURCE + " = " + source);
             try {
                 ActionUtils.runTarget(buildXML, new String[]{ANT_TARGET_EXPORT}, p);
             } catch (IOException e) {
                 ErrorManager.getDefault().notify(e);
             }
         } else {
             //TODO notify
         }
     }
 
     /**
      * 
      * @param buildXML
      * @param dest
      * @param src
      */
     @Deprecated
     public static void startImport(final FileObject buildXML, final String src) {
         if (src != null) {
             final Properties p = createProjectAntProperties();
             String dest = FileUtil.toFile(buildXML.getParent()).getAbsolutePath();
             p.put(IMPORT_DEST, dest);
             p.put(IMPORT_SOURCE, src);
 //            p.put(MAX_MEMORY, MAX_MEMORY_VALUE);
             log.debug("Ant " + ANT_TARGET_IMPORT + " properties: " + IMPORT_DEST + " = " + src + ", " + IMPORT_SOURCE + " = " + dest);
             try {
                 ActionUtils.runTarget(buildXML, new String[]{ANT_TARGET_IMPORT}, p);
             } catch (IOException e) {
                 ErrorManager.getDefault().notify(e);
             }
         } else {
             //TODO notify
         }
     }
 
     /**
      * 
      * @param buildXML
      * @param dest
      */
     public static void saveAntProperties(final FileObject buildXML, final File dest) {
         if (buildXML != null && buildXML.isValid() && dest != null) {
             dest.mkdirs();
             final Properties p = createProjectAntProperties();
             try {
                 final FileWriter w = new FileWriter(dest);
                 p.store(w, "JPresso Ant Properties for Project " + buildXML.getParent().getName());
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
     }
 
     /**
      * 
      * @param buildXML
      */
     public static void startProject(final FileObject buildXML) {
         final Properties p = createProjectAntProperties();
 //        p.put(MAX_MEMORY, MAX_MEMORY_VALUE);
         try {
             ActionUtils.runTarget(buildXML, new String[]{ANT_TARGET_PROJECT}, p);
         } catch (IOException e) {
             ErrorManager.getDefault().notify(e);
         }
     }
 
     /**
      * 
      * @param dob
      * @return
      */
     private static boolean canExecute(final DataObject dob) {
         boolean start = true;
         if (dob.isModified()) {
             try {
                 final Confirmation msg = new NotifyDescriptor.Confirmation("You are trying to execute " + dob.getPrimaryFile().getNameExt() + ".\n" +
                         "There are changes that have to be saved before.\n" +
                         "Save file and continue?");
                 final Object result = DialogDisplayer.getDefault().notify(msg);
                 if (result.equals(NotifyDescriptor.YES_OPTION)) {
                     start = true;
                 } else {
                     start = false;
                 }
                 if (start) {
                     final SaveCookie sc = dob.getCookie(SaveCookie.class);
                     if (sc != null) {
                         sc.save();
                     } else {
                         //TODO notify
                     }
                 }
             } catch (IOException ex) {
                 ErrorManager.getDefault().notify(ex);
             }
         }
         return start;
     }
 
     /**
      * Creates properties to fill the ant script's variables.
      * These are project directory, the jpressocore.jar, external
      * library directory.
      * 
      * @param buildXML
      * @return parameter to execute the build.xml with.
      */
     private static Properties createProjectAntProperties() {
 
         final Properties props = new Properties();
         Class c = JPressoFileManager.class;
         final String pathCore = URLTools.convertURLToFile(JPressoFileManager.locateJarForClass(c)).getAbsolutePath();
         props.put(JPCORE_JAR, pathCore);
         log.debug("Ant properties: JPressoCore.jar -> jpcore.jar = " + pathCore);
         c = PropertyConfigurator.class;
         final String pathLib = URLTools.convertURLToFile(JPressoFileManager.locateJarForClass(c)).getParent();
         props.put(LIB_DIR, pathLib);
         return props;
     }
 
     public static void writeDefaultProperties() throws IOException {
 //        final File defProp = new File(FileUtil.toFile(buildXML.getParent()), JPressoFileManager.DEFAULT_PROPS);
         final File defProp = new File(System.getProperty("user.home"), File.separator + JPressoFileManager.ANT_PROPS);
         final Properties p = createProjectAntProperties();
//        p.put(MAX_MEMORY, MAX_MEMORY_VALUE);
         p.store(new BufferedOutputStream(new FileOutputStream(defProp)), "");
     }
 
     public static void addAdditionalClassPathToProperties(final Properties props, final FileObject buildXML) {
         if (buildXML != null && buildXML.isValid()) {
             final File projDir = FileUtil.toFile(buildXML.getParent());
             log.debug("Ant properties: Project Directory -> project.dir = " + projDir);
             final ClassResourceProvider crp = ClassResourceProviderFactory.createClassRessourceProvider(projDir);
             final Set<File> addCP = crp.getProjectClasspath();
             final StringBuilder addCPString = new StringBuilder();
             for (final File cur : addCP) {
                 addCPString.append(cur.getAbsolutePath()).append(";");
             }
             props.put(ADDITIONAL_CLASSPATH, addCPString.toString());
         } else {
             log.error("Could not get Project Directory from BuildXML, returning null");
         //TODO error message window & return!
         }
     }
 
     public static boolean defaultPropertiesExist() {
         final String homeDir = System.getProperty("user.home");
         final File defaultProps = new File(homeDir, File.separator + JPressoFileManager.ANT_PROPS);
         if (defaultProps.isFile()) {
             final Properties props = new Properties();
             try {
                 props.load(new BufferedInputStream(new FileInputStream(defaultProps)));
             } catch (IOException ex) {
                 System.err.println("Could not load Properties!\n" + ex);
             }
             final String corePath = props.getProperty(JPCORE_JAR);
             final String libPath = props.getProperty(LIB_DIR);
             if (corePath != null) {
                 final File coreFile = new File(corePath);
                 if (coreFile.isFile()) {
                     if (libPath != null) {
                         final File libDir = new File(libPath);
                         if (libDir.isDirectory() && libDir.list().length > 0) {
                             //alles ok
                             return true;
                         }
                     }
                 }
                 final File possibleLibDir = new File(coreFile.getParentFile(), File.separator + "ext");
                 if (possibleLibDir.isDirectory() && possibleLibDir.list().length > 0) {
                     //koennte stimmen, also true aber warning:
                     System.err.println("Warning: Could not locate given library directory " + libPath + "! Trying relative directory " + possibleLibDir);
                     return true;
                 }
             }
         }
         return false;
     }
 }
