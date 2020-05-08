 package com.kdgdev.jMFB;
 
 import brut.androlib.Androlib;
 import brut.androlib.AndrolibException;
 import brut.androlib.ApkDecoder;
 import brut.androlib.res.util.ExtFile;
 import brut.androlib.src.SmaliBuilder;
 import brut.common.BrutException;
 import brut.util.OS;
 import com.jgoodies.forms.factories.CC;
 import com.jgoodies.forms.layout.FormLayout;
 import com.kdgdev.jMFB.patchsupport.ResValuesModify;
 import com.kdgdev.jMFB.utils.*;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.FilenameUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 import javax.imageio.ImageIO;
 import javax.swing.*;
 import javax.swing.filechooser.FileFilter;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.io.*;
 import java.net.*;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import java.util.List;
 import java.util.logging.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import net.lingala.zip4j.core.ZipFile;
 import net.lingala.zip4j.exception.ZipException;
 import net.lingala.zip4j.model.*;
 import net.lingala.zip4j.progress.*;
 import net.lingala.zip4j.util.Zip4jConstants;
 
 
 /**
  * @author Kirill
  */
 public class mainForm extends JFrame {
 
     private final Androlib kAndrolib = new Androlib();
 
     private final static Logger LOGGER = Logger.getLogger(Androlib.class.getName());
 
     public mainForm() {
         initComponents();
     }
 
     /*private String getBootClassPathFromFolder(File fold) {
         String extraBootClassPath = null;
         searchTools su = new searchTools();
         try {
             List jarFiles = su.findAll(fold.getAbsolutePath(), ".*.jar");
             for (int i = 0; i < jarFiles.size(); i++) {
                 File jar = new File(jarFiles.get(i).toString());
                 if (extraBootClassPath != null) {
                     extraBootClassPath = extraBootClassPath + ":" + jar.getName();
                 } else {
                     extraBootClassPath = ":" + jar.getName();
                 }
 
             }
         } catch (Exception ex) {
             LOGGER.info(ex.getMessage());
         }
 
         if (extraBootClassPath != null) {
             extraBootClassPath = extraBootClassPath.replace(":core.jar:", ":");
             extraBootClassPath = extraBootClassPath.replace(":ext.jar:", ":");
             extraBootClassPath = extraBootClassPath.replace(":framework.jar:", ":");
             extraBootClassPath = extraBootClassPath.replace(":android.policy.jar:", ":");
             extraBootClassPath = extraBootClassPath.replace(":services.jar:", ":");
             extraBootClassPath = extraBootClassPath.replace(":miui-framework.jar:", ":");
         }
 
         return extraBootClassPath;
 
     }*/
 
     private void baksmali(File odexFile, File outDir, int apiLevel, File classPathDir) {
         List<String> cmd = new ArrayList<String>();
         cmd.add("java");
         cmd.add("-jar");
         cmd.add(binDir + File.separatorChar + "baksmali.jar");
         cmd.add("-a");
         cmd.add(String.valueOf(apiLevel));
         cmd.add("-x");
         cmd.add(odexFile.getAbsolutePath());
         cmd.add("-d");
         cmd.add(classPathDir.getAbsolutePath());
         cmd.add("-o");
         cmd.add(outDir.getAbsolutePath());
         try {
             OS.exec(cmd.toArray(new String[0]));
         } catch (AndrolibException e) {
             LOGGER.info(e.getMessage());
         } catch (BrutException e) {
             LOGGER.info(e.getMessage());
         }
     }
 
     private void smali(File inDir, File dexFile) {
         try {
             SmaliBuilder.build(new ExtFile(inDir.getAbsolutePath()), dexFile, false);
 
         } catch (AndrolibException ex) {
             LOGGER.info(ex.getMessage());
         }
     }
 
     private void packFile(File destFile, File fileToPack) {
 
         List<String> cmd = new ArrayList<String>();
         cmd.add(aAppsDir + File.separatorChar + "aapt");
         cmd.add("add");
         cmd.add(destFile.getAbsolutePath());
         cmd.add(fileToPack.getName());
         try {
             OS.exec(cmd.toArray(new String[0]), fileToPack.getParent());
         } catch (BrutException ex) {
             LOGGER.info(ex.getMessage());
         }
 
     }
 
     private void deodex(File odexFile, File outDir, int apiLevel, File classPathDir, String ext) {
         LOGGER.info("Deodexing file: " + odexFile.getName());
         File classesDir = new File(workDir + File.separatorChar + projectName + File.separatorChar + "deodexed" + File.separatorChar + odexFile.getName() + ".classes");
         if (!classesDir.exists()) {
             classesDir.mkdirs();
         }
         File classesFile = new File(classesDir.getAbsolutePath() + File.separatorChar + "classes.dex");
         baksmali(odexFile, outDir, apiLevel, classPathDir);
         smali(outDir, classesFile);
         packFile(new File(FilenameUtils.removeExtension(odexFile.toString()) + ext), classesFile);
         if (classesFile.exists()) {
             odexFile.delete();
             LOGGER.info("File deodexed successfully");
         } else {
             LOGGER.info("Error deodexing file. No classes.dex file found");
         }
         try {
             deleteDirectory(new File(workDir + File.separatorChar + projectName + File.separatorChar + "deodexed" + File.separatorChar + odexFile.getName() + ".classes"));
             deleteDirectory(new File(workDir + File.separatorChar + projectName + File.separatorChar + "deodexed" + File.separatorChar + odexFile.getName()));
         } catch (BrutException e) {
             System.out.println(e.getMessage());
         }
     }
 
     private void deodexFirmware(String FirmwarePath, String folder, String Ext) throws Exception {
         searchTools suApp = new searchTools();
         List odexAppFiles = suApp.findAll(FirmwarePath + File.separatorChar + "system" + File.separatorChar + folder, ".*.odex");
         pbProgress.setMaximum(odexAppFiles.size());
         pbProgress.setValue(0);
         pbProgress.setIndeterminate(false);
         for (int i = 0; i < odexAppFiles.size(); i++) {
             File odex = new File(odexAppFiles.get(i).toString());
             pbProgress.setValue(i + 1);
             String outDir = workDir + File.separatorChar + projectName + File.separatorChar + "deodexed" + File.separatorChar + odex.getName();
             deodex(odex, new File(outDir), 16, new File(FirmwarePath + File.separatorChar + "system" + File.separatorChar + "framework"), Ext);
         }
         pbProgress.setIndeterminate(true);
     }
 
     private String readBuildProp(String filename, String section) {
         String sPattern = section + "=(.*)";
         Pattern p = Pattern.compile(sPattern);
         File file = new File(filename);
         try {
             List<String> contents = FileUtils.readLines(file);
             for (String line : contents) {
                 Matcher m = p.matcher(line);
                 if (m.matches()) return m.group(1);
             }
         } catch (IOException e) {
             LOGGER.info(e.getMessage());
             JOptionPane.showMessageDialog(null, "<html><table width=300>" + e.getMessage());
         }
         return "none";
     }
 
     /*private class ApkFileSign {
 
         private File reader;
 
         public ApkFileSign(File fileName) {
             reader = fileName;
         }
 
         public String getCertificate(String fileName) {
             String sPattern = "name=\"" + fileName + "\" certificate=\"(.*)\" private_key=\"(.*)\"";
             Pattern p = Pattern.compile(sPattern, Pattern.CASE_INSENSITIVE);
             try {
                 List<String> contents = FileUtils.readLines(reader);
                 for (String line : contents) {
                     Matcher m = p.matcher(line);
                     if (m.matches()) return m.group(1);
                 }
             } catch (IOException e) {
                 LOGGER.info(e.getMessage());
                 JOptionPane.showMessageDialog(null, "<html><table width=300>" + e.getMessage());
             }
             return "PRESIGNED";
         }
 
         public String getPrivateKey(String fileName) {
             String sPattern = "name=\"" + fileName + "\" certificate=\"(.*)\" private_key=\"(.*)\"";
             Pattern p = Pattern.compile(sPattern, Pattern.CASE_INSENSITIVE);
             try {
                 List<String> contents = FileUtils.readLines(reader);
                 for (String line : contents) {
                     Matcher m = p.matcher(line);
                     if (m.matches()) return m.group(2);
                 }
             } catch (IOException e) {
                 LOGGER.info(e.getMessage());
                 JOptionPane.showMessageDialog(null, "<html><table width=300>" + e.getMessage());
             }
             return null;
         }
     }*/
 
     private void readLanuagesFile(String fileName, Boolean cleanLangs) {
         if (cleanLangs) {
             repos_names.clear();
             repos_git.clear();
             repos_lang.clear();
             repos_count = 0;
         }
         String sPattern = "(.*)=(.*)=(.*)";
         Pattern p = Pattern.compile(sPattern);
         File file = new File(fileName);
         try {
             List<String> contents = FileUtils.readLines(file);
             for (String line : contents) {
                 Matcher m = p.matcher(line);
                 if (m.matches()) {
                     repos_names.add(m.group(1));
                     repos_git.add(m.group(2));
                     repos_lang.add(m.group(3));
                     repos_count++;
                     LOGGER.info("Added language: " + m.group(1));
                 }
             }
         } catch (IOException e) {
             System.out.println(e.getMessage());
         }
     }
 
     private void writeBuildProp(String filename, String section, String value) {
         String sPattern = section + "=(.*)";
         Pattern p = Pattern.compile(sPattern);
         File file = new File(filename);
         Boolean replaced = false;
         try {
             List<String> contents = FileUtils.readLines(file);
             for (int i = 0; i < contents.size(); i++) {
                 Matcher m = p.matcher(contents.get(i).toLowerCase());
                 if (m.matches()) {
                     contents.set(i, section + "=" + value);
                     replaced = true;
                 }
             }
             if (!replaced) contents.add(section + "=" + value);
             FileUtils.writeLines(file, contents);
         } catch (IOException e) {
             LOGGER.info(e.getMessage());
             JOptionPane.showMessageDialog(null, "<html><table width=300>" + e.getMessage());
         }
     }
 
     public static boolean isOSX() {
         String osName = System.getProperty("os.name");
         return osName.contains("OS X");
     }
 
     public static boolean isWindows() {
 
         String os = System.getProperty("os.name").toLowerCase();
         // windows
         return (os.indexOf("win") >= 0);
 
     }
 
     public static boolean isUnix() {
 
         String os = System.getProperty("os.name").toLowerCase();
         // linux or unix
         return (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0);
 
     }
 
     public static String getOSDir() {
 
         if (isOSX()) return "osx";
         if (isWindows()) return "win";
         if (isUnix()) return "nix";
         return null;
 
     }
 
     public void extractFolder(String zipFile, String ExtractPath) throws ZipException {
         ZipFile FirmwareZip = new ZipFile(zipFile);
         net.lingala.zip4j.progress.ProgressMonitor progressMonitor = FirmwareZip.getProgressMonitor();
         FirmwareZip.setRunInThread(true);
         FirmwareZip.extractAll(ExtractPath);
         pbProgress.setIndeterminate(false);
         while (progressMonitor.getState() == net.lingala.zip4j.progress.ProgressMonitor.STATE_BUSY) {
 
             pbProgress.setValue(progressMonitor.getPercentDone());
 
         }
         pbProgress.setIndeterminate(true);
        /* LOGGER.info(zipFile);
         int BUFFER = 2048;
         File file = new File(zipFile);
 
         ZipFile zip = new ZipFile(file);
         String newPath = ExtractPath;
 
         new File(newPath).mkdir();
         Enumeration zipFileEntries = zip.entries();
 
         // Process each entry
         while (zipFileEntries.hasMoreElements()) {
             // grab a zip file entry
             ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
             String currentEntry = entry.getName();
             File destFile = new File(newPath, currentEntry);
             File destinationParent = destFile.getParentFile();
 
             // create the parent directory structure if needed
             destinationParent.mkdirs();
 
             if (!entry.isDirectory()) {
                 BufferedInputStream is = new BufferedInputStream(zip
                         .getInputStream(entry));
                 int currentByte;
                 // establish buffer for writing file
                 byte data[] = new byte[BUFFER];
 
                 // write the current file to disk
                 FileOutputStream fos = new FileOutputStream(destFile);
                 BufferedOutputStream dest = new BufferedOutputStream(fos,
                         BUFFER);
 
                 // read and write until last byte is encountered
                 while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
                     dest.write(data, 0, currentByte);
                 }
                 dest.flush();
                 dest.close();
                 is.close();
             }
         }*/
     }
 
     private boolean initMFB(String[] args) {
         setupLogging(Verbosity.NORMAL);
         LOGGER.info("========== Welcome to jMFB =========");
         LOGGER.info("| Original version only on ROMZ.bz |");
         LOGGER.info("|  This program code protected by  |");
         LOGGER.info("|             US Patents           |");
         LOGGER.info("====================================");
         workDir = System.getProperty("user.dir");
         LOGGER.info("WorkDir = " + workDir);
         aAppsDir = workDir + File.separatorChar + "aApps" + File.separatorChar + getOSDir();
         binDir = workDir + File.separatorChar + "aApps" + File.separatorChar + "bin";
         LOGGER.info("aAppsDir = " + aAppsDir);
         if (new File(workDir + File.separatorChar + "repos.list").exists())
             readLanuagesFile(workDir + File.separatorChar + "repos.list", true);
         if (new File(workDir + File.separatorChar + "repos_add.list").exists())
             readLanuagesFile(workDir + File.separatorChar + "repos_add.list", false);
         kBigBlackBox_Init bbb2 = new kBigBlackBox_Init();
         bbb2.execute();
         //<editor-fold desc="Init main patches">
         if (!execFile(false, "aapt") || !execFile(false, "zipalign")) {
             aAppsDir = "";
             if (!execFile(false, "aapt") || !execFile(false, "zipalign")) {
                 JOptionPane.showMessageDialog(null, "Failed to init jMFB", "Alert", JOptionPane.ERROR_MESSAGE);
                 System.exit(2);
             }
         }
         kAndrolib.setAapTool(aAppsDir + File.separatorChar + "aapt");
         if (args.length > 0) {
             if (args[0].equals("-build") || args[0].equals("-b")) {
                 cmd = true;
                 projectName = args[1] + ".mfbproj";
                 titleProjName = args[1];
                 edtFirmwareFile.setText(args[2]);
                 int[] sel = {1, 1, 0};
                 lstRepos.setSelectedIndices(sel);
                 kBigBlackBox_Decompiler bad = new kBigBlackBox_Decompiler();
                 bad.execute();
                 while (!bad.isDone()) {
                 }
                 ;
                 kBigBlackBox_Compile bac = new kBigBlackBox_Compile();
                 bac.execute();
                 while (!bac.isDone()) {
                 }
                 ;
                 System.exit(0);
                 return false;
             }
         }
         //</editor-fold>
         Properties writeProp = new Properties();
         try {
             writeProp.load(new FileInputStream(workDir + File.separatorChar + "jmfb.properties"));
             LOGGER.info("Properties loaded:");
             repo_Precompiled = writeProp.getProperty("repo_Precompiled", repo_Precompiled);
             LOGGER.info(" - repo_Precompiled = " + repo_Precompiled);
             repo_Bootanimation = writeProp.getProperty("repo_Bootanimation", repo_Bootanimation);
             LOGGER.info(" - repo_Bootanimation = " + repo_Bootanimation);
             repo_Overlay = writeProp.getProperty("repo_Overlay", repo_Overlay);
             LOGGER.info(" - repo_Overlay = " + repo_Overlay);
             repo_Patches = writeProp.getProperty("repo_Patches", repo_Patches);
             LOGGER.info(" - repo_Patches = " + repo_Patches);
             otaUpdateURL = writeProp.getProperty("otaUpdateURL", otaUpdateURL);
             LOGGER.info(" - otaUpdateURL = " + otaUpdateURL);
             writeBProp = Boolean.parseBoolean(writeProp.getProperty("writeBuildProp", writeBProp.toString()));
             LOGGER.info(" - writeBuildProp = " + writeBProp.toString());
             HWUpdate = Boolean.parseBoolean(writeProp.getProperty("HWUpdate", HWUpdate.toString()));
             LOGGER.info(" - HWUpdate = " + HWUpdate.toString());
             UpdateFromFolder = Boolean.parseBoolean(writeProp.getProperty("UpdateFromFolder", UpdateFromFolder.toString()));
             LOGGER.info(" - UpdateFromFolder = " + UpdateFromFolder.toString());
         } catch (IOException e) {
             LOGGER.info("Properties not found. Loading defaults...OK!");
         }
         return true;
     }
 
     private void installFrameworks(String Folder) {
         searchTools finder = new searchTools();
         pbProgress.setIndeterminate(false);
         pbProgress.setValue(0);
         List frameworks = null;
         try {
             frameworks = finder.findAll(Folder, ".*.apk");
             pbProgress.setMaximum(frameworks.size());
             for (int i = 0; i < frameworks.size(); i++) {
                 pbProgress.setValue(i);
                 kAndrolib.installFramework((File) frameworks.get(i), null);
             }
         } catch (Exception e) {
             LOGGER.info(e.getMessage());
             JOptionPane.showMessageDialog(null, "<html><table width=300>" + e.getMessage());
         }
         pbProgress.setIndeterminate(true);
     }
 
     private boolean execFile(Boolean testReturn, String... cmd) {
         if (!aAppsDir.equals("")) cmd[0] = aAppsDir + File.separatorChar + cmd[0];
         ProcessBuilder procBuilder = new ProcessBuilder(cmd);
         procBuilder.redirectErrorStream(true);
         Process process = null;
         try {
             process = procBuilder.start();
             InputStream stdout = process.getInputStream();
             InputStreamReader isrStdout = new InputStreamReader(stdout);
             BufferedReader brStdout = new BufferedReader(isrStdout);
             String line = null;
             while ((line = brStdout.readLine()) != null) {
                 if (testReturn) LOGGER.info(line);
             }
             ;
             int exitVal = process.waitFor();
             if (testReturn) {
                 if (exitVal == 2) return false;
             }
         } catch (IOException e) {
             e.printStackTrace();
             return false;
         } catch (InterruptedException e) {
             e.printStackTrace();
             return false;
         }
         return true;
     }
 
     private void getFilesFromGit(String Folder, String Git) throws IOException {
         getFilesFromGit(Folder, Git, "master");
     }
 
     private void getFilesFromGit(String Folder, String Git, String branch) throws IOException {
         pbProgress.setIndeterminate(true);
         new gitTools().downloadFromGit(Git, Folder, branch);
         //execFile(true, "git", "clone", Git, Folder);
         try {
             deleteDirectory(new File(Folder + File.separatorChar + ".git"));
         } catch (BrutException e) {
             LOGGER.info(e.getMessage());
             JOptionPane.showMessageDialog(null, "<html><table width=300>" + e.getMessage());
         }
         new File(Folder + File.separatorChar + "README").delete();
         pbProgress.setIndeterminate(false);
     }
 
     private void decompileFile(String Apk, String Folder, boolean Sources) {
         decompileFile(Apk, Folder, Sources, true);
     }
 
     private void decompileFile(String Apk, String Folder, boolean Sources, boolean Resources) {
         ApkDecoder decoder = new ApkDecoder(kAndrolib);
         try {
             decoder.setDecodeSources((Sources) ? ApkDecoder.DECODE_SOURCES_SMALI : ApkDecoder.DECODE_SOURCES_NONE);
             decoder.setDecodeResources((Resources) ? ApkDecoder.DECODE_RESOURCES_FULL : ApkDecoder.DECODE_RESOURCES_NONE);
             decoder.setForceDelete(true);
             decoder.setKeepBrokenResources(false);
             decoder.setOutDir(new File(Folder));
             decoder.setApkFile(new File(Apk));
             decoder.decode();
         } catch (AndrolibException e) {
             LOGGER.info(e.getMessage());
             JOptionPane.showMessageDialog(null, "<html><table width=300>" + e.getMessage());
         }
     }
 
     private void rebuildFiles(String Apk, String ApkName) {
         if (HWUpdate) {
             if (ApkName.equals("miuihome.apk")) {
                 File res = new File(Apk + File.separatorChar + "res" + File.separatorChar + "values" + File.separatorChar + "bools.xml");
                 if (res.exists()) {
                     try {
                         String patchFile = FileUtils.readFileToString(res);
                         patchFile = StringUtils.replace(patchFile, "<bool name=\"config_hardwareAccelerated\">false</bool>", "<bool name=\"config_hardwareAccelerated\">true</bool>");
                         FileUtils.writeStringToFile(res, patchFile);
                     } catch (IOException e) {
                         LOGGER.info(e.getMessage());
                         JOptionPane.showMessageDialog(null, "<html><table width=300>" + e.getMessage());
                     }
                 }
             }
         }
         //http://update.miui.com/updates/mi-updateV4.php
 
         if (ApkName.equals("updater.apk")) {
             File res = new File(Apk + File.separatorChar + "smali" + File.separatorChar + "com" + File.separatorChar + "android" + File.separatorChar + "updater" + File.separatorChar + "utils" + File.separatorChar + "SysUtils.smali");
             if (res.exists()) {
                 try {
                     String patchFile = FileUtils.readFileToString(res);
                     patchFile = StringUtils.replace(patchFile, "\"http://update.miui.com/updates/mi-updateV4.php\"", "\"" + otaUpdateURL + "\"");
                     patchFile = StringUtils.replace(patchFile, "\"http://update.miui.com/updates/mi-updateV5.php\"", "\"" + otaUpdateURL + "\"");
                     patchFile = StringUtils.replace(patchFile, "\"http://update.miui.com/updates/mi-updateV6.php\"", "\"" + otaUpdateURL + "\"");
                     patchFile = StringUtils.replace(patchFile, "\"http://update.miui.com/updates/mi-updateV7.php\"", "\"" + otaUpdateURL + "\"");
                     patchFile = StringUtils.replace(patchFile, "\"http://update.miui.com/updates/mi-updateV8.php\"", "\"" + otaUpdateURL + "\"");
                     patchFile = StringUtils.replace(patchFile, "\"http://update.miui.com/updates/mi-updateV9.php\"", "\"" + otaUpdateURL + "\"");
                     FileUtils.writeStringToFile(res, patchFile);
 
                 } catch (IOException e) {
                     LOGGER.info(e.getMessage());
                     JOptionPane.showMessageDialog(null, "<html><table width=300>" + e.getMessage());
                 }
             }
         }
 
         if (ApkName.equals("mms.apk")) {
             File res = new File(Apk + File.separatorChar + "smali" + File.separatorChar + "com" + File.separatorChar + "android" + File.separatorChar + "mms" + File.separatorChar + "data" + File.separatorChar + "FestivalSmsUpdater.smali");
             if (res.exists()) {
                 try {
                     String patchFile = FileUtils.readFileToString(res);
                     patchFile = StringUtils.replace(patchFile, "\"http://www.miui.com\"", "\"http://localhost\"");
                     FileUtils.writeStringToFile(res, patchFile);
 
                 } catch (IOException e) {
                     LOGGER.info(e.getMessage());
                     JOptionPane.showMessageDialog(null, "<html><table width=300>" + e.getMessage());
                 }
             }
             try {
                 new File(Apk + File.separatorChar + "build" + File.separatorChar + "apk").mkdirs();
                 kAndrolib.buildSourcesSmali(new File(Apk), false, false);
                 FileUtils.copyFile(new File(Apk + File.separatorChar + "build" + File.separatorChar + "apk" + File.separatorChar + "classes.dex"), new File(Apk + File.separatorChar + "classes.dex"));
                 deleteDirectory(new File(Apk + File.separatorChar + "build"));
                 deleteDirectory(new File(Apk + File.separatorChar + "smali"));
             } catch (AndrolibException e) {
                 LOGGER.info(e.getMessage());
                 JOptionPane.showMessageDialog(null, "<html><table width=300>" + e.getMessage());
             } catch (IOException e) {
                 LOGGER.info(e.getMessage());
                 JOptionPane.showMessageDialog(null, "<html><table width=300>" + e.getMessage());
             } catch (BrutException e) {
                 LOGGER.info(e.getMessage());
                 JOptionPane.showMessageDialog(null, "<html><table width=300>" + e.getMessage());
             }
         }
         if (ApkName.equals("miuicompass.apk")) {
             Map<String, Object> meta = new LinkedHashMap<String, Object>();
             try {
                 meta = new Androlib().readMetaFile(new ExtFile(Apk));
                 Map<String, Object> uses = new LinkedHashMap<String, Object>();
                 Integer[] ids = {1, 6};
                 uses.put("ids", ids);
                 meta.put("usesFramework", uses);
                 new Androlib().writeMetaFile(new File(Apk), meta);
             } catch (AndrolibException e) {
                 LOGGER.info(e.getMessage());
                 JOptionPane.showMessageDialog(null, "<html><table width=300>" + e.getMessage());
             }
         }
         if (ApkName.equals("framework-miui-res.apk")) {
             Map<String, Object> meta = new LinkedHashMap<String, Object>();
             try {
                 meta = new Androlib().readMetaFile(new ExtFile(Apk));
                 Map<String, Object> uses = new LinkedHashMap<String, Object>();
                 Integer[] ids = {1, 2, 3, 4, 5};
                 uses.put("ids", ids);
                 meta.put("usesFramework", uses);
                 new Androlib().writeMetaFile(new File(Apk), meta);
             } catch (AndrolibException e) {
                 LOGGER.info(e.getMessage());
                 JOptionPane.showMessageDialog(null, "<html><table width=300>" + e.getMessage());
             }
         }
     }
 
     private void writeAllBPValues(String buildPropPath) throws UnknownHostException {
         if (writeBProp) {
             writeBuildProp(buildPropPath, "ro.config.ringtone", "MI.ogg");
             writeBuildProp(buildPropPath, "ro.config.notification_sound", "FadeIn.ogg");
             writeBuildProp(buildPropPath, "ro.config.alarm_alert", "GoodMorning.ogg");
             writeBuildProp(buildPropPath, "ro.config.sms_received_sound", "FadeIn.ogg");
             writeBuildProp(buildPropPath, "ro.config.sms_delivered_sound", "MessageComplete.ogg");
         }
         writeBuildProp(buildPropPath, "ro.build.user", System.getProperty("user.name").toLowerCase());
         writeBuildProp(buildPropPath, "ro.build.host", InetAddress.getLocalHost().getHostName().toLowerCase());
         Calendar now = Calendar.getInstance(Locale.getDefault());
         writeBuildProp(buildPropPath, "persist.sys.timezone", timeZones.get(cbTimeZone.getSelectedIndex()));
         writeBuildProp(buildPropPath, "ro.jmfb.version", "test-2012");
         writeBuildProp(buildPropPath, "ro.build.type", "user");
         writeBuildProp(buildPropPath, "ro.build.tags", "release-keys");
         writeBuildProp(buildPropPath, "dalvik.vm.dexopt-flags", "m=y,o=v,u=y");
         writeBuildProp(buildPropPath, "dalvik.vm.verify-bytecode", "false");
         writeBuildProp(buildPropPath, "ro.build.date", now.getTime().toString());
         long unixTime = System.currentTimeMillis() / 1000L;
         writeBuildProp(buildPropPath, "ro.build.date.utc", Long.toString(unixTime));
         writeBuildProp(buildPropPath, "ro.product.locale.language", langs[cbLang.getSelectedIndex()]);
         writeBuildProp(buildPropPath, "ro.product.locale.region", regions[cbLang.getSelectedIndex()]);
     }
 
     private class kBigBlackBox_Decompiler extends SwingWorker<Integer, Object> {
 
         @Override
         protected Integer doInBackground() {
             try {
                 btnDeCompile.setEnabled(false);
                 pbProgress.setIndeterminate(true);
                 new File(workDir + File.separatorChar + projectName).mkdirs();
                 kAndrolib.setFrameworksDir(workDir + File.separatorChar + projectName + File.separatorChar + "MFB_Core");
                 //<editor-fold desc="Extracting firmware and update files">
                 pbProgress.setIndeterminate(true);
                 if (!new File(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware").exists()) {
                     lbProgressstate.setText("Extracting firmware...");
                     extractFolder(edtFirmwareFile.getText(), workDir + File.separatorChar + projectName + File.separatorChar + "Firmware");
                     extractFolder(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar + "system" + File.separatorChar + "media" + File.separatorChar + "theme" + File.separatorChar + "default" + File.separatorChar + "lockscreen", workDir + File.separatorChar + projectName + File.separatorChar + "Lockscreen");
                     String frmDir = new String(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar + "system" + File.separatorChar + "framework" + File.separatorChar);
 
                     if (new File(frmDir + "core.odex").exists() || new File(frmDir + "ext.odex").exists() || new File(frmDir + "framework.odex").exists() || new File(frmDir + "android.policy.odex").exists() || new File(frmDir + "services.odex").exists()) {
                         try {
                             lbProgressstate.setText("Deodexing firmware...");
                             LOGGER.info("----- Starting BurgerZ deodex code -----");
                             deodexFirmware(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware", "app", ".apk");
                             deodexFirmware(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware", "framework", ".jar");
                             LOGGER.info("----- DONE! -----");
                         } catch (Exception e) {
                             LOGGER.info(e.getMessage());
                             JOptionPane.showMessageDialog(null, "<html><table width=300>" + e.getMessage());
                         }
 
                     }
                 }
                 //</editor-fold>
                 installFrameworks(workDir + File.separatorChar + "aApps" + File.separatorChar + "plugs");
                 //<editor-fold desc="Downloading precompiled files from git">
                 if (!new File(workDir + File.separatorChar + projectName + File.separatorChar + "PrecompiledFiles").exists()) {
                     lbProgressstate.setText("Getting precompiled files...");
                     String device = readBuildProp(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar + "system" + File.separatorChar + "build.prop", "ro.product.device");
                     getFilesFromGit(workDir + File.separatorChar + projectName + File.separatorChar + "PrecompiledFiles", repo_Precompiled);
                     new File(workDir + File.separatorChar + projectName + File.separatorChar + "PrecompiledFiles" + File.separatorChar + "system" + File.separatorChar + "app").mkdirs();
                     new gitTools().downloadFileFromGit("MiCode/patchrom_miui", "system/app/Updater.apk", workDir + File.separatorChar + projectName + File.separatorChar + "PrecompiledFiles" + File.separatorChar + "system" + File.separatorChar + "app" + File.separatorChar + "Updater.apk", "ics");
                     new gitTools().downloadFileFromGit(repo_Bootanimation, "system/media/bootanimation.zip", workDir + File.separatorChar + projectName + File.separatorChar + "PrecompiledFiles" + File.separatorChar + "system" + File.separatorChar + "media" + File.separatorChar + "bootanimation.zip");
                     lbProgressstate.setText("Updating files...");
                     File src = new File(workDir + File.separatorChar + projectName + File.separatorChar + "PrecompiledFiles" + File.separatorChar);
                     File dsc = new File(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar);
                     try {
                         FileUtils.copyDirectory(src, dsc);
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                 }
                 if (new File(workDir + File.separatorChar + "Override" + File.separatorChar + readBuildProp(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar + "system" + File.separatorChar + "build.prop", "ro.product.device")).exists() && UpdateFromFolder) {
                     LOGGER.info("Override system files...");
                     lbProgressstate.setText("Updating files...");
                     File src = new File(workDir + File.separatorChar + "Override" + File.separatorChar + readBuildProp(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar + "system" + File.separatorChar + "build.prop", "ro.product.device") + File.separatorChar);
                     File dsc = new File(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar);
                     try {
                         FileUtils.copyDirectory(src, dsc);
                         LOGGER.info("Copy done: " + src.getAbsolutePath() + " -> " + dsc.getAbsolutePath());
                     } catch (IOException e) {
                         LOGGER.info("Copy failed");
                     }
                 }
                 //</editor-fold>
                 if (readBuildProp(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar + "system" + File.separatorChar + "build.prop", "ro.build.version.release").contains("4.1"))
                     isJB = true;
 
                 lbProgressstate.setText("Getting translation files...");
                 //<editor-fold desc="Downloading translation files from git">
                 deleteDirectory(new File(workDir + File.separatorChar + projectName + File.separatorChar + "Language_Git"));
 
                 for (int i = 0; i < repos_count; i++) {
                     if (lstRepos.isSelectedIndex(i)) {
                         getFilesFromGit(workDir + File.separatorChar + projectName + File.separatorChar + "Language" + ((Integer) i).toString(), repos_git.get(i));
                         File source = new File(workDir + File.separatorChar + projectName + File.separatorChar + "Language" + ((Integer) i).toString() + File.separatorChar + repos_lang.get(i));
                         File desc = new File(workDir + File.separatorChar + projectName + File.separatorChar + "Language_Git");
                         try {
                             FileUtils.copyDirectory(source, desc);
                         } catch (IOException e) {
                             e.printStackTrace();
                         }
                     }
                     deleteDirectory(new File(workDir + File.separatorChar + projectName + File.separatorChar + "Language" + ((Integer) i).toString()));
                 }
 
                 deleteDirectory(new File(workDir + File.separatorChar + projectName + File.separatorChar + "Language_Eng"));
                 if (!isJB || !cbDisableCS.isSelected()) {
                     getFilesFromGit(workDir + File.separatorChar + projectName + File.separatorChar + "Additional", repo_Overlay);
                     File source = new File(workDir + File.separatorChar + projectName + File.separatorChar + "Additional");
                     File desc = new File(workDir + File.separatorChar + projectName + File.separatorChar + "Language_Git");
                     try {
                         FileUtils.copyDirectory(source, desc);
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                     deleteDirectory(new File(workDir + File.separatorChar + projectName + File.separatorChar + "Additional"));
                 }
                 getFilesFromGit(workDir + File.separatorChar + projectName + File.separatorChar + "Additional", repo_Patches);
                 File source = new File(workDir + File.separatorChar + projectName + File.separatorChar + "Additional");
                 File desc = new File(workDir + File.separatorChar + projectName + File.separatorChar + "Language_Git");
                 try {
                     FileUtils.copyDirectory(source, desc);
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
                 deleteDirectory(new File(workDir + File.separatorChar + projectName + File.separatorChar + "Additional"));
                 if(new File(workDir + File.separatorChar + "Language_Overlay").exists()) {
                     source = new File(workDir + File.separatorChar + "Language_Overlay");
                     desc = new File(workDir + File.separatorChar + projectName + File.separatorChar + "Language_Git");
                     try {
                         FileUtils.copyDirectory(source, desc);
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                 }
                 //</editor-fold>
                 source = new File(workDir + File.separatorChar + projectName + File.separatorChar + "Language_Git" + File.separatorChar + "extras" + File.separatorChar + "lockscreen");
                 desc = new File(workDir + File.separatorChar + projectName + File.separatorChar + "Lockscreen");
                 try {
                     FileUtils.copyDirectory(source, desc);
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
                 String buildPropPath = workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar + "system" + File.separatorChar + "build.prop";
                 setTitle("jMFB - " + titleProjName + " (" + readBuildProp(buildPropPath, "ro.product.device") + ")");
                 writeAllBPValues(buildPropPath);
                 lbProgressstate.setText("Installing frameworks...");
                 installFrameworks(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar + "system" + File.separatorChar + "framework");
                 //<editor-fold desc="Building new workspace - decompiling app and frameworks">
                 if (!new File(workDir + File.separatorChar + projectName + File.separatorChar + "AppsSources").exists()) {
                     lbProgressstate.setText("Building new workspace - Decompiling apps...");
                     File apkSrc = new File(workDir + File.separatorChar + projectName + File.separatorChar + "AppsSources");
                     apkSrc.mkdirs();
                     pbProgress.setIndeterminate(false);
                     searchTools finder = new searchTools();
                     try {
                         List apkFiles = finder.findAll(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar + "system" + File.separatorChar + "app", ".*.apk");
                         if (new File(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar + "data" + File.separatorChar + "media" + File.separatorChar + "preinstall_apps").exists()) {
                             List addApkFiles = finder.findAll(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar + "data" + File.separatorChar + "media" + File.separatorChar + "preinstall_apps", ".*.apk");
                             for (int i = 0; i < addApkFiles.size(); i++) {
                                 apkFiles.add(addApkFiles.get(i));
                             }
                         }
                         pbProgress.setMaximum(apkFiles.size());
                         pbProgress.setValue(0);
                         for (int i = 0; i < apkFiles.size(); i++) {
                             File frm = new File(apkFiles.get(i).toString());
                             if (new File(workDir + File.separatorChar + projectName + File.separatorChar + "Language_Git" + File.separatorChar + "main" + File.separatorChar + frm.getName()).exists() || new File(workDir + File.separatorChar + projectName + File.separatorChar + "Language_Git" + File.separatorChar + "device" + File.separatorChar + readBuildProp(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar + "system" + File.separatorChar + "build.prop", "ro.product.device") + File.separatorChar + frm.getName()).exists() || decompAll.isSelected()) {
                                 pbProgress.setValue(i);
                                 LOGGER.info("======== Decompiling " + frm.getName() + " ========");
                                 decompileFile(apkFiles.get(i).toString(), workDir + File.separatorChar + projectName + File.separatorChar + "AppsSources" + File.separatorChar + frm.getName(), frm.getName().equalsIgnoreCase("mms.apk") || frm.getName().equalsIgnoreCase("updater.apk"));
                                 rebuildFiles(workDir + File.separatorChar + projectName + File.separatorChar + "AppsSources" + File.separatorChar + frm.getName(), frm.getName().toLowerCase());
                             } else {
                                 File dectFile = File.createTempFile("KDGDEV", ".kdg");
                                 File dctFile = frm;
                                 FileUtils.copyFile(dctFile, dectFile);
                                 dctFile.delete();
                                 if (execFile(true, "zipalign", "-f", "4", dectFile.getAbsolutePath(), dctFile.getAbsolutePath()))
                                     LOGGER.info("Zipaligning...");
                                 else FileUtils.copyFile(dectFile, dctFile);
                                 dectFile.delete();
                             }
                         }
                     } catch (Exception err) {
                         LOGGER.info(err.getMessage());
                     }
                 }
                 deleteDirectory(new File(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar + "data"));
                 if (!new File(workDir + File.separatorChar + projectName + File.separatorChar + "FrameworkSources").exists()) {
                     lbProgressstate.setText("Building new workspace - Decompiling frameworks...");
                     File frmSrc = new File(workDir + File.separatorChar + projectName + File.separatorChar + "FrameworkSources");
                     frmSrc.mkdirs();
                     searchTools finder = new searchTools();
                     try {
                         List frmFiles = finder.findAll(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar + "system" + File.separatorChar + "framework", ".*.apk");
                         pbProgress.setMaximum(frmFiles.size());
                         pbProgress.setValue(0);
                         for (int i = 0; i < frmFiles.size(); i++) {
                             File frm = new File(frmFiles.get(i).toString());
                             if (new File(workDir + File.separatorChar + projectName + File.separatorChar + "Language_Git" + File.separatorChar + "main" + File.separatorChar + frm.getName()).exists() || new File(workDir + File.separatorChar + projectName + File.separatorChar + "Language_Git" + File.separatorChar + "device" + File.separatorChar + readBuildProp(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar + "system" + File.separatorChar + "build.prop", "ro.product.device") + File.separatorChar + frm.getName()).exists() || decompAll.isSelected()) {
                                 pbProgress.setValue(i);
                                 LOGGER.info("======== Decompiling " + frm.getName() + " ========");
                                 decompileFile(frmFiles.get(i).toString(), workDir + File.separatorChar + projectName + File.separatorChar + "FrameworkSources" + File.separatorChar + frm.getName(), false);
                                 rebuildFiles(workDir + File.separatorChar + projectName + File.separatorChar + "FrameworkSources" + File.separatorChar + frm.getName(), frm.getName().toLowerCase());
                             } else {
                                 File dectFile = File.createTempFile("KDGDEV", ".kdg");
                                 File dctFile = frm;
                                 FileUtils.copyFile(dctFile, dectFile);
                                 dctFile.delete();
                                 if (execFile(true, "zipalign", "-f", "4", dectFile.getAbsolutePath(), dctFile.getAbsolutePath()))
                                     LOGGER.info("Zipaligning...");
                                 else FileUtils.copyFile(dectFile, dctFile);
                                 dectFile.delete();
                             }
                         }
                     } catch (Exception err) {
                         LOGGER.info(err.getMessage());
                     }
                 }
                 //</editor-fold>
                 pbProgress.setMaximum(100);
                 pbProgress.setValue(100);
                 pbProgress.setIndeterminate(false);
                 lbProgressstate.setText("Done!");
                 Properties sets = new Properties();
                 sets.setProperty("FirmwareFile", edtFirmwareFile.getText());
                 for (int i = 0; i < repos_count; i++) {
                     if (lstRepos.isSelectedIndex(i))
                         sets.setProperty("Repo" + ((Integer) i).toString(), "1");
                     else sets.setProperty("Repo" + ((Integer) i).toString(), "0");
                 }
                 sets.store(new FileOutputStream(workDir + File.separatorChar + projectName + File.separatorChar + "jmfb.prop"), "KDevGroup Settings");
                 LOGGER.info("======== End of decompiling files ========");
                 btnBuild.setEnabled(true);
             } catch (IOException e) {
                 LOGGER.info(e.getMessage());
                 JOptionPane.showMessageDialog(null, "<html><table width=300>" + e.getMessage());
             } catch (BrutException e) {
                 LOGGER.info(e.getMessage());
                 JOptionPane.showMessageDialog(null, "<html><table width=300>" + e.getMessage());
             } catch (ZipException e) {
                 System.out.println(e.getMessage());
             }
             return 1;
         }
     }
 
     private void deleteDirectory(File Dir) throws BrutException {
         OS.rmdir(Dir);
     }
 
     private void patchXMLs(String path) throws Exception {
         FileFinder finder = new FileFinder();
         LOGGER.info("Preparing for updating workspace with auth system...");
         List searchRes = finder.findAll(workDir + File.separatorChar + projectName + File.separatorChar + "Language_Git" + File.separatorChar + "main", ".*.part");
         for (int j = 0; j < searchRes.size(); j++) {
             String filePath = new relativePath().getRelativePath(workDir + File.separatorChar + projectName + File.separatorChar + "Language_Git" + File.separatorChar + "main", ((File)searchRes.get(j)).getParent());
             List<String> cmd = new ArrayList<String>();
             cmd.add(workDir + File.separatorChar + projectName + File.separatorChar + "Language_Git" + File.separatorChar + "main" + File.separatorChar + filePath);
             cmd.add(path + File.separatorChar + filePath);
             if(new File(path + File.separatorChar + filePath).exists()) {
                 new ResValuesModify().Modify(cmd.toArray(new String[0]));
                 new File(searchRes.get(j).toString()).delete();
             }
         }
         LOGGER.info("Updating workspace with auth system...Done!");
     }
 
     private class kBigBlackBox_Compile extends SwingWorker<Integer, Object> {
         @Override
         protected Integer doInBackground() {
             searchTools finder = new searchTools();
             btnBuild.setEnabled(false);
             try {
                 deleteDirectory(new File(workDir + File.separatorChar + projectName + File.separatorChar + "build"));
                 List buildFiles = finder.findDirectories(workDir + File.separatorChar + projectName + File.separatorChar + "AppsSources", ".*.apk");
                 pbProgress.setMaximum(buildFiles.size());
                 pbProgress.setValue(0);
                 lbProgressstate.setText("Building apps...");
                 deleteDirectory(new File(workDir + File.separatorChar + projectName + File.separatorChar + "AppsCompiled"));
                 new File(workDir + File.separatorChar + projectName + File.separatorChar + "AppsCompiled").mkdirs();
                 kAndrolib.setFrameworksDir(workDir + File.separatorChar + projectName + File.separatorChar + "MFB_Core");
                 //File fXml = new File(workDir + File.separatorChar + "aApps" + File.separatorChar + "security" + File.separatorChar + "apkcerts.txt");
                 patchXMLs(workDir + File.separatorChar + projectName + File.separatorChar + "AppsSources");
                 for (int i = 0; i < buildFiles.size(); i++) {
                     pbProgress.setValue(i);
                     LOGGER.info("======== Compiling " + new File(buildFiles.get(i).toString()).getName() + " ========");
                     File sourceDir = new File(buildFiles.get(i).toString());
                     File dectFile = File.createTempFile("KDGDEV", ".kdg");
                     //File signFile = File.createTempFile("KDGDEV", ".kdg");
                     //String FileName = (new File(buildFiles.get(i).toString()).getName());
                     File dctFile = new File(workDir + File.separatorChar + projectName + File.separatorChar + "AppsCompiled" + File.separatorChar + (new File(buildFiles.get(i).toString()).getName()));
                     if (new File(workDir + File.separatorChar + projectName + File.separatorChar + "Language_Git" + File.separatorChar + "main" + File.separatorChar + (new File(buildFiles.get(i).toString()).getName())).exists()) {
                         FileUtils.copyDirectory(new File(workDir + File.separatorChar + projectName + File.separatorChar + "Language_Git" + File.separatorChar + "main" + File.separatorChar + (new File(buildFiles.get(i).toString()).getName())), sourceDir);
                     }
                     if (new File(workDir + File.separatorChar + projectName + File.separatorChar + "Language_Git" + File.separatorChar + "device" + File.separatorChar + readBuildProp(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar + "system" + File.separatorChar + "build.prop", "ro.product.device") + File.separatorChar + (new File(buildFiles.get(i).toString()).getName())).exists()) {
                         FileUtils.copyDirectory(new File(workDir + File.separatorChar + projectName + File.separatorChar + "Language_Git" + File.separatorChar + "device" + File.separatorChar + readBuildProp(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar + "system" + File.separatorChar + "build.prop", "ro.product.device") + File.separatorChar + (new File(buildFiles.get(i).toString()).getName())), sourceDir);
                     }
                     if (buildFiles.get(i).toString().contains("Settings.apk")) {
                         if (new File(sourceDir.getAbsolutePath() + File.separatorChar + "res" + File.separatorChar + "drawable-ru-hdpi" + File.separatorChar + "miui_logo.9.png").exists()) {
                             watermarkImage(sourceDir.getAbsolutePath() + File.separatorChar + "res" + File.separatorChar + "drawable-ru-hdpi" + File.separatorChar + "miui_logo.9.png");
                         }
                         if (new File(sourceDir.getAbsolutePath() + File.separatorChar + "res" + File.separatorChar + "drawable-ru-xhdpi" + File.separatorChar + "miui_logo.9.png").exists()) {
                             watermarkImage(sourceDir.getAbsolutePath() + File.separatorChar + "res" + File.separatorChar + "drawable-ru-xhdpi" + File.separatorChar + "miui_logo.9.png");
                         }
                         if (new File(sourceDir.getAbsolutePath() + File.separatorChar + "res" + File.separatorChar + "drawable-uk-hdpi" + File.separatorChar + "miui_logo.9.png").exists()) {
                             watermarkImage(sourceDir.getAbsolutePath() + File.separatorChar + "res" + File.separatorChar + "drawable-uk-hdpi" + File.separatorChar + "miui_logo.9.png");
                         }
                         if (new File(sourceDir.getAbsolutePath() + File.separatorChar + "res" + File.separatorChar + "drawable-uk-xhdpi" + File.separatorChar + "miui_logo.9.png").exists()) {
                             watermarkImage(sourceDir.getAbsolutePath() + File.separatorChar + "res" + File.separatorChar + "drawable-uk-xhdpi" + File.separatorChar + "miui_logo.9.png");
                         }
                     }
                     //ApkFileSign signerData = new ApkFileSign(new File(workDir + File.separatorChar + "aApps" + File.separatorChar + "security" + File.separatorChar + "apkcerts.txt"));
 
                     kAndrolib.build(sourceDir, dectFile, true, false, true);
                     //kAndrolib.build(sourceDir, dectFile, true, false, (signerData.getCertificate(FileName).equalsIgnoreCase("PRESIGNED")));
                     //if(!(signerData.getCertificate(FileName).equalsIgnoreCase("PRESIGNED"))) {
                     //    signApk signer = new signApk();
                     //    LOGGER.info("Signing with "+signerData.getCertificate(FileName)+"...");
                     //    signer.signBuildFile(true, workDir + File.separatorChar + "aApps" + File.separatorChar + "security" + File.separatorChar +signerData.getCertificate(FileName), workDir + File.separatorChar + "aApps" + File.separatorChar + "security" + File.separatorChar +signerData.getPrivateKey(FileName), dectFile.getAbsolutePath(), dectFile.getAbsolutePath());
                     //}
                     if (execFile(true, "zipalign", "-f", "4", dectFile.getAbsolutePath(), dctFile.getAbsolutePath()))
                         LOGGER.info("Zipaligning...");
                     else FileUtils.copyFile(dectFile, dctFile);
                     dectFile.delete();
                 }
                 buildFiles = finder.findDirectories(workDir + File.separatorChar + projectName + File.separatorChar + "FrameworkSources", ".*.apk");
                 pbProgress.setMaximum(buildFiles.size());
                 pbProgress.setValue(0);
                 lbProgressstate.setText("Building framework...");
                 deleteDirectory(new File(workDir + File.separatorChar + projectName + File.separatorChar + "FrameworkCompiled"));
                 new File(workDir + File.separatorChar + projectName + File.separatorChar + "FrameworkCompiled").mkdirs();
                 patchXMLs(workDir + File.separatorChar + projectName + File.separatorChar + "FrameworkSources");
                 for (int i = 0; i < buildFiles.size(); i++) {
                     pbProgress.setValue(i);
                     LOGGER.info("======== Compiling " + new File(buildFiles.get(i).toString()).getName() + " ========");
                     File sourceDir = new File(buildFiles.get(i).toString());
                     File dectFile = File.createTempFile("KDGDEV", ".kdg");
                     //String FileName = (new File(buildFiles.get(i).toString()).getName());
                     File dctFile = new File(workDir + File.separatorChar + projectName + File.separatorChar + "FrameworkCompiled" + File.separatorChar + (new File(buildFiles.get(i).toString()).getName()));
                     if (new File(workDir + File.separatorChar + projectName + File.separatorChar + "Language_Git" + File.separatorChar + "main" + File.separatorChar + (new File(buildFiles.get(i).toString()).getName())).exists()) {
                         FileUtils.copyDirectory(new File(workDir + File.separatorChar + projectName + File.separatorChar + "Language_Git" + File.separatorChar + "main" + File.separatorChar + (new File(buildFiles.get(i).toString()).getName())), sourceDir);
                     }
                     if (new File(workDir + File.separatorChar + projectName + File.separatorChar + "Language_Git" + File.separatorChar + "device" + File.separatorChar + readBuildProp(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar + "system" + File.separatorChar + "build.prop", "ro.product.device") + File.separatorChar + (new File(buildFiles.get(i).toString()).getName())).exists()) {
                         FileUtils.copyDirectory(new File(workDir + File.separatorChar + projectName + File.separatorChar + "Language_Git" + File.separatorChar + "device" + File.separatorChar + readBuildProp(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar + "system" + File.separatorChar + "build.prop", "ro.product.device") + File.separatorChar + (new File(buildFiles.get(i).toString()).getName())), sourceDir);
                     }
                     //ApkFileSign signerData = new ApkFileSign(new File(workDir + File.separatorChar + "aApps" + File.separatorChar + "security" + File.separatorChar + "apkcerts.txt"));
                     kAndrolib.build(sourceDir, dectFile, true, false, true);
                     //kAndrolib.build(sourceDir, dectFile, true, false, (signerData.getCertificate(FileName).equalsIgnoreCase("PRESIGNED")||FileName.equals("Contacts.apk")));
                     //if(!(signerData.getCertificate(FileName).equalsIgnoreCase("PRESIGNED")||FileName.equals("Contacts.apk"))) {
                     //    signFile signer = new signFile();
                     //    LOGGER.info("Signing with "+signerData.getCertificate(FileName)+"...");
                     //    signer.signBuildFile(true, workDir + File.separatorChar + "aApps" + File.separatorChar + "security" + File.separatorChar +signerData.getCertificate(FileName), workDir + File.separatorChar + "aApps" + File.separatorChar + "security" + File.separatorChar +signerData.getPrivateKey(FileName), dectFile.getAbsolutePath(), dectFile.getAbsolutePath());
                     //}
                     if (execFile(true, "zipalign", "-f", "4", dectFile.getAbsolutePath(), dctFile.getAbsolutePath()))
                         LOGGER.info("Zipaligning...");
                     else FileUtils.copyFile(dectFile, dctFile);
                     dectFile.delete();
                 }
                 LOGGER.info("======== End of compiling files ========");
                 String buildPropPath = workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar + "system" + File.separatorChar + "build.prop";
                 writeAllBPValues(buildPropPath);
                 LOGGER.info("Building firmware file...");
                 lbProgressstate.setText("Building firmware file...");
                 pbProgress.setIndeterminate(true);
                 new File(workDir + File.separatorChar + projectName + File.separatorChar + "tmps" + File.separatorChar + "1").mkdirs();
                 decompileFile(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar + "system" + File.separatorChar + "framework" + File.separatorChar + "miui-framework.jar", workDir + File.separatorChar + projectName + File.separatorChar + "tmps" + File.separatorChar + "1", true);
                 FileUtils.copyFile(new File(workDir + File.separatorChar + "aApps" + File.separatorChar + "bin" + File.separatorChar + "androtech.bin"), new File(workDir + File.separatorChar + projectName + File.separatorChar + "tmps" + File.separatorChar + "1" + File.separatorChar + "smali" + File.separatorChar + "miui" + File.separatorChar + "util" + File.separatorChar + "HanziToPinyin.smali"));
                 kAndrolib.build(new File(workDir + File.separatorChar + projectName + File.separatorChar + "tmps" + File.separatorChar + "1"), new File(workDir + File.separatorChar + projectName + File.separatorChar + "FrameworkCompiled" + File.separatorChar + "miui-framework.jar"), true, false, true);
                 FileUtils.copyDirectory(new File(workDir + File.separatorChar + projectName + File.separatorChar + "FrameworkCompiled"), new File(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar + "system" + File.separatorChar + "framework"));
                 FileUtils.copyDirectory(new File(workDir + File.separatorChar + projectName + File.separatorChar + "AppsCompiled"), new File(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar + "system" + File.separatorChar + "app"));
                 new File(workDir + File.separatorChar + projectName + File.separatorChar + "build" + File.separatorChar + "out").mkdirs();
                 new File(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar + "META-INF" + File.separatorChar + "CERT.RSA").delete();
                 new File(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar + "META-INF" + File.separatorChar + "CERT.SF").delete();
                 new File(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar + "META-INF" + File.separatorChar + "MANIFEST.MF").delete();
                 deleteDirectory(new File(workDir + File.separatorChar + projectName + File.separatorChar + "tmps"));
 
                 new File(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar + "system" + File.separatorChar + "media" + File.separatorChar + "theme" + File.separatorChar + "default" + File.separatorChar + "lockscreen").delete();
 
                /*ZipFile LockscreenZip = new ZipFile(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar + "system" + File.separatorChar + "media" + File.separatorChar + "theme" + File.separatorChar + "default" + File.separatorChar + "lockscreen");
                 ZipParameters parameters = new ZipParameters();
                 parameters.setCompressionMethod(Zip4jConstants.COMP_STORE);
                 parameters.setCompressionLevel(0);
                 parameters.setIncludeRootFolder(false);
                 parameters.setRootFolderInZip("/");
                LockscreenZip.addFolder(workDir + File.separatorChar + projectName + File.separatorChar + "Lockscreen", parameters);*/
 
                zipTools.zipFile(workDir + File.separatorChar + projectName + File.separatorChar + "Lockscreen", workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar + "system" + File.separatorChar + "media" + File.separatorChar + "theme" + File.separatorChar + "default" + File.separatorChar + "lockscreen", true);
 
                 zipTools.zipFile(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware", workDir + File.separatorChar + projectName + File.separatorChar + "build" + File.separatorChar + "out" + File.separatorChar + "out.zip", true);
                 String firmwareVersion = readBuildProp(buildPropPath, "ro.build.version.incremental");
                 String phoneModel = readBuildProp(buildPropPath, "ro.product.device");
                 lbProgressstate.setText("Signing firmware file...");
                 LOGGER.info("Signing firmware file...");
                 if (cmd) {
                     new File(workDir + File.separatorChar + "build").mkdirs();
                     new signApk().signBuildFile(false, workDir + File.separatorChar + "aApps" + File.separatorChar + "security" + File.separatorChar + "testkey.x509.pem", workDir + File.separatorChar + "aApps" + File.separatorChar + "security" + File.separatorChar + "testkey.pk8", workDir + File.separatorChar + projectName + File.separatorChar + "build" + File.separatorChar + "out" + File.separatorChar + "out.zip", workDir + File.separatorChar + "build" + File.separatorChar + "miuirussia_" + phoneModel + "_" + firmwareVersion + ".zip");
                 } else {
                     new signApk().signBuildFile(true, workDir + File.separatorChar + "aApps" + File.separatorChar + "security" + File.separatorChar + "testkey.x509.pem", workDir + File.separatorChar + "aApps" + File.separatorChar + "security" + File.separatorChar + "testkey.pk8", workDir + File.separatorChar + projectName + File.separatorChar + "build" + File.separatorChar + "out" + File.separatorChar + "out.zip", workDir + File.separatorChar + projectName + File.separatorChar + "build" + File.separatorChar + "out" + File.separatorChar + "miuirussia_" + phoneModel + "_" + firmwareVersion + ".zip");
                     //String[] cmdArr = new String[]{"java", "-Xms2048m", "-Xmx2048m", "-jar", workDir + File.separatorChar + "aApps" + File.separatorChar + "bin" + File.separatorChar + "signapk.jar", "-w", workDir + File.separatorChar + "aApps" + File.separatorChar + "security" + File.separatorChar + "testkey.x509.pem", workDir + File.separatorChar + "aApps" + File.separatorChar + "security" + File.separatorChar + "testkey.pk8", workDir + File.separatorChar + projectName + File.separatorChar + "build" + File.separatorChar + "out" + File.separatorChar + "out.zip", workDir + File.separatorChar + projectName + File.separatorChar + "build" + File.separatorChar + "out" + File.separatorChar + "miuirussia_" + phoneModel + "_" + firmwareVersion + ".zip"};
                     //OS.exec(cmdArr);
                 }
                 new File(workDir + File.separatorChar + projectName + File.separatorChar + "build" + File.separatorChar + "out" + File.separatorChar + "out.zip").delete();
                 pbProgress.setIndeterminate(false);
                 pbProgress.setMaximum(100);
                 pbProgress.setValue(100);
                 lbProgressstate.setText("Done!");
                 if (!cmd) {
                     JTextArea textArea = new JTextArea();
                     textArea.setText("Firmware builded successfully!\nYou firmware available: <jMFB_Folder>" + File.separatorChar + projectName + File.separatorChar + "build" + File.separatorChar + "out" + File.separatorChar + "miuirussia_" + phoneModel + "_" + firmwareVersion + ".zip\nMD5: " + MD5Checksum.getMD5Checksum(workDir + File.separatorChar + projectName + File.separatorChar + "build" + File.separatorChar + "out" + File.separatorChar + "miuirussia_" + phoneModel + "_" + firmwareVersion + ".zip") + "\nThanks for using jMFB");
                     textArea.setSize(300, Short.MAX_VALUE);
                     textArea.setWrapStyleWord(true);
                     textArea.setLineWrap(true);
                     textArea.setEditable(false);
                     textArea.setOpaque(false);
                     JOptionPane.showMessageDialog(null, textArea);
 
                 } else deleteDirectory(new File(workDir + File.separatorChar + projectName));
                 LOGGER.info("======== Compiling done! ========");
             } catch (AndrolibException e) {
                 LOGGER.info(e.getMessage());
                 JOptionPane.showMessageDialog(null, "<html><table width=300>" + e.getMessage());
             } catch (IOException e) {
                 LOGGER.info(e.getMessage());
                 JOptionPane.showMessageDialog(null, "<html><table width=300>" + e.getMessage());
             } catch (Exception e) {
                 LOGGER.info(e.getMessage());
                 JOptionPane.showMessageDialog(null, "<html><table width=300>" + e.getMessage());
             }
             btnBuild.setEnabled(true);
             return 1;
         }
     }
 
     private class kBigBlackBox_Init extends SwingWorker<Integer, Object> {
 
         @Override
         protected Integer doInBackground() {
             //<editor-fold desc="Read timezones">
             DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
             factory.setValidating(false);
             factory.setIgnoringElementContentWhitespace(true);
             factory.setIgnoringComments(true);
             DocumentBuilder builder = null;
             try {
                 builder = factory.newDocumentBuilder();
                 builder.setErrorHandler(null);
             } catch (ParserConfigurationException e) {
                 LOGGER.info(e.getMessage());
                 JOptionPane.showMessageDialog(null, "<html><table width=300>" + e.getMessage());
             }
             Document domDocument = null;
             try {
                 domDocument = builder.parse(this.getClass().getResource("/com/kdgdev/jMFB/resources/timezones.xml").openStream());
             } catch (SAXException e) {
                 LOGGER.info(e.getMessage());
                 JOptionPane.showMessageDialog(null, "<html><table width=300>" + e.getMessage());
             } catch (IOException e) {
                 LOGGER.info(e.getMessage());
                 JOptionPane.showMessageDialog(null, "<html><table width=300>" + e.getMessage());
             }
             NodeList nodeList = domDocument.getElementsByTagName("timezone");
             for (int i = 0; i < nodeList.getLength(); i++) {
                 Node node = nodeList.item(i);
                 cbTimeZone.addItem(node.getTextContent());
                 timeZones.add(node.getAttributes().getNamedItem("id").getTextContent());
                 //LOGGER.info("Adding timezone: " + node.getAttributes().getNamedItem("id").getTextContent());
             }
 
             DefaultListModel listModel = new DefaultListModel();
             lstRepos.setModel(listModel);
             for (int i = 0; i < repos_count; i++) {
                 listModel.addElement(repos_names.get(i));
             }
 
             cbTimeZone.setSelectedIndex(76);
             //</editor-fold>
 
             return 1;
         }
     }
 
     private void btnCompileActionPerformed(ActionEvent e) {
         kBigBlackBox_Decompiler bbb1 = new kBigBlackBox_Decompiler();
         bbb1.execute();
     }
 
     private void btnBrowseActionPerformed(ActionEvent e) {
         JFileChooser fileopen = new JFileChooser();
         fileopen.addChoosableFileFilter(new FileFilter() {
             @Override
             public boolean accept(File file) {
                 return file.isDirectory() || file.getName().endsWith(".zip");  //To change body of implemented methods use File | Settings | File Templates.
             }
 
             @Override
             public String getDescription() {
                 return "Firmware files";  //To change body of implemented methods use File | Settings | File Templates.
             }
         });
         fileopen.showOpenDialog(null);
         edtFirmwareFile.setText(fileopen.getSelectedFile().toString());
         if (new File(fileopen.getSelectedFile().toString()).exists()) btnDeCompile.setEnabled(true);
     }
 
     private void setInterfaceState(Boolean State) {
         edtFirmwareFile.setEnabled(State);
         btnBrowse.setEnabled(State);
         //cbHWRender.setEnabled(State);
         cbTimeZone.setEnabled(State);
         decompAll.setEnabled(State);
         cbLang.setEnabled(State);
         lstRepos.setEnabled(State);
         cbDisableCS.setEnabled(State);
         //btnDeCompile.setEnabled(State);
     }
 
     private void miOpenProjectActionPerformed(ActionEvent e) {
         projectName = JOptionPane.showInputDialog(null, "Project name:", "Opening MFB project", JOptionPane.QUESTION_MESSAGE);
         titleProjName = projectName;
         projectName = projectName + ".mfbproj";
         if (!projectName.isEmpty() && new File(workDir + File.separatorChar + projectName).exists()) {
             setTitle("jMFB - " + titleProjName);
             setInterfaceState(true);
             Properties sets = new Properties();
             try {
                 sets.load(new FileInputStream(workDir + File.separatorChar + projectName + File.separatorChar + "jmfb.prop"));
                 edtFirmwareFile.setText(sets.getProperty("FirmwareFile"));
                 int[] sel = {0, 0, 0};
                 for (int i = 0; i < 3; i++) {
                     sel[i] = Integer.parseInt(sets.getProperty("Repo" + ((Integer) i).toString()));
                 }
                 lstRepos.setSelectedIndices(sel);
             } catch (IOException e1) {
                 LOGGER.info(e1.getMessage());
                 JOptionPane.showMessageDialog(null, "<html><table width=300>" + e1.getMessage());
             }
             btnCompileActionPerformed(null);
         } else {
             JOptionPane.showMessageDialog(null, "Project \"" + projectName + "\" does not exist!\nPlease open exist project or create new.");
         }
 
     }
 
     private void miNewProjectActionPerformed(ActionEvent e) {
         projectName = JOptionPane.showInputDialog(null, "Project name:", "Creating MFB project", JOptionPane.QUESTION_MESSAGE);
         titleProjName = projectName;
         projectName = projectName + ".mfbproj";
         if (!projectName.isEmpty()) {
             setTitle("jMFB - " + titleProjName);
             setInterfaceState(true);
         }
     }
 
     private void watermarkImage(String fileName) throws IOException {
         InputStream imageStream = new FileInputStream(fileName);
         BufferedImage img = ImageIO.read(imageStream);
         int h = img.getHeight();
         int w = img.getWidth();
         BufferedImage newImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
         Graphics g = newImage.getGraphics();
         g.drawImage(img, 0, 0, null);
         Date d = new Date();
         DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
         String str = "Build date: " + df.format(d);
         g.setColor(Color.WHITE);
         g.drawChars(str.toCharArray(), 0, str.length(), ((w / 2) - (str.length() * 3)) - 6, h - 8);
         g.dispose();
         new File(fileName).delete();
         ImageIO.write(newImage, "PNG", new File(fileName));
     }
 
     private void btnBuildActionPerformed(ActionEvent e) {
         kBigBlackBox_Compile bbb1 = new kBigBlackBox_Compile();
         bbb1.execute();
     }
 
     private void initComponents() {
         // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
         mbMainBar = new JMenuBar();
         fileMenu = new JMenu();
         miNewProject = new JMenuItem();
         miOpenProject = new JMenuItem();
         headerLogo = new JLabel();
         lblFirmwareHelp = new JLabel();
         pOpenFirmware = new JPanel();
         edtFirmwareFile = new JTextField();
         btnBrowse = new JButton();
         spSeparator1 = new JSeparator();
         lbAdditionalSets = new JLabel();
         pAddons = new JPanel();
         lbTimezone = new JLabel();
         cbTimeZone = new JComboBox();
         lbLang = new JLabel();
         cbLang = new JComboBox();
         decompAll = new JCheckBox();
         cbDisableCS = new JCheckBox();
         spSeparator2 = new JSeparator();
         lbTranslRepo = new JLabel();
         pRepos = new JPanel();
         spRepos = new JScrollPane();
         lstRepos = new JList();
         spSeparator3 = new JSeparator();
         lbProgressstate = new JLabel();
         pbProgress = new JProgressBar();
         pCmdButtons = new JPanel();
         btnDeCompile = new JButton();
         btnBuild = new JButton();
 
         //======== this ========
         setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
         setTitle("jMFB Lite");
         setBackground(Color.white);
         setResizable(false);
         Container contentPane = getContentPane();
         contentPane.setLayout(new FormLayout(
             "$lcgap, 204dlu:grow, 2*($lcgap)",
             "3*(default, $lgap), default, $lcgap, 8*(default, $lgap), default"));
 
         //======== mbMainBar ========
         {
 
             //======== fileMenu ========
             {
                 fileMenu.setText("File");
                 fileMenu.setForeground(SystemColor.activeCaption);
 
                 //---- miNewProject ----
                 miNewProject.setText("New project...");
                 miNewProject.addActionListener(new ActionListener() {
                     @Override
                     public void actionPerformed(ActionEvent e) {
                         miNewProjectActionPerformed(e);
                     }
                 });
                 fileMenu.add(miNewProject);
 
                 //---- miOpenProject ----
                 miOpenProject.setText("Open project...");
                 miOpenProject.addActionListener(new ActionListener() {
                     @Override
                     public void actionPerformed(ActionEvent e) {
                         miOpenProjectActionPerformed(e);
                     }
                 });
                 fileMenu.add(miOpenProject);
             }
             mbMainBar.add(fileMenu);
         }
         setJMenuBar(mbMainBar);
 
         //---- headerLogo ----
         headerLogo.setIcon(new ImageIcon(getClass().getResource("/com/kdgdev/jMFB/resources/Header.png")));
         contentPane.add(headerLogo, CC.xywh(1, 1, 4, 1, CC.CENTER, CC.DEFAULT));
 
         //---- lblFirmwareHelp ----
         lblFirmwareHelp.setText("Firmware file:");
         lblFirmwareHelp.setFont(lblFirmwareHelp.getFont().deriveFont(lblFirmwareHelp.getFont().getStyle() | Font.BOLD, lblFirmwareHelp.getFont().getSize() + 1f));
         contentPane.add(lblFirmwareHelp, CC.xy(2, 3));
 
         //======== pOpenFirmware ========
         {
             pOpenFirmware.setLayout(new FormLayout(
                 "default, $lcgap, default:grow, $lcgap, default",
                 "default"));
 
             //---- edtFirmwareFile ----
             edtFirmwareFile.setEnabled(false);
             pOpenFirmware.add(edtFirmwareFile, CC.xywh(1, 1, 3, 1));
 
             //---- btnBrowse ----
             btnBrowse.setText("...");
             btnBrowse.setFocusable(false);
             btnBrowse.setEnabled(false);
             btnBrowse.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     btnBrowseActionPerformed(e);
                 }
             });
             pOpenFirmware.add(btnBrowse, CC.xy(5, 1));
         }
         contentPane.add(pOpenFirmware, CC.xy(2, 5));
         contentPane.add(spSeparator1, CC.xywh(1, 7, 4, 1));
 
         //---- lbAdditionalSets ----
         lbAdditionalSets.setText("Additional settings:");
         lbAdditionalSets.setFont(lbAdditionalSets.getFont().deriveFont(lbAdditionalSets.getFont().getStyle() | Font.BOLD, lbAdditionalSets.getFont().getSize() + 1f));
         contentPane.add(lbAdditionalSets, CC.xy(2, 9));
 
         //======== pAddons ========
         {
             pAddons.setLayout(new FormLayout(
                 "default, $lcgap, default:grow",
                 "3*(default, $lgap), default"));
 
             //---- lbTimezone ----
             lbTimezone.setText("Timezone:");
             pAddons.add(lbTimezone, CC.xy(1, 1));
 
             //---- cbTimeZone ----
             cbTimeZone.setFocusable(false);
             cbTimeZone.setEnabled(false);
             pAddons.add(cbTimeZone, CC.xy(3, 1));
 
             //---- lbLang ----
             lbLang.setText("Language:");
             pAddons.add(lbLang, CC.xy(1, 3));
 
             //---- cbLang ----
             cbLang.setModel(new DefaultComboBoxModel(new String[] {
                 "Russian",
                 "Ukrainian",
                 "English (US)"
             }));
             cbLang.setFocusable(false);
             cbLang.setEnabled(false);
             pAddons.add(cbLang, CC.xy(3, 3));
 
             //---- decompAll ----
             decompAll.setText("Decompile all files");
             decompAll.setEnabled(false);
             pAddons.add(decompAll, CC.xywh(1, 5, 3, 1));
 
             //---- cbDisableCS ----
             cbDisableCS.setText("Disable centered clock");
             cbDisableCS.setEnabled(false);
             pAddons.add(cbDisableCS, CC.xywh(1, 7, 3, 1));
         }
         contentPane.add(pAddons, CC.xy(2, 11, CC.FILL, CC.FILL));
         contentPane.add(spSeparator2, CC.xywh(1, 13, 4, 1));
 
         //---- lbTranslRepo ----
         lbTranslRepo.setText("Translation repositories:");
         lbTranslRepo.setFont(lbTranslRepo.getFont().deriveFont(lbTranslRepo.getFont().getStyle() | Font.BOLD, lbTranslRepo.getFont().getSize() + 1f));
         contentPane.add(lbTranslRepo, CC.xy(2, 15));
 
         //======== pRepos ========
         {
             pRepos.setLayout(new FormLayout(
                 "$lcgap, default:grow, 2*($lcgap)",
                 "default"));
 
             //======== spRepos ========
             {
 
                 //---- lstRepos ----
                 lstRepos.setModel(new AbstractListModel() {
                     String[] values = {
                         "Repo1",
                         "Repo2",
                         "Repo3"
                     };
                     @Override
                     public int getSize() { return values.length; }
                     @Override
                     public Object getElementAt(int i) { return values[i]; }
                 });
                 lstRepos.setFocusable(false);
                 lstRepos.setEnabled(false);
                 spRepos.setViewportView(lstRepos);
             }
             pRepos.add(spRepos, CC.xy(2, 1));
         }
         contentPane.add(pRepos, CC.xy(2, 17));
         contentPane.add(spSeparator3, CC.xywh(1, 19, 4, 1));
 
         //---- lbProgressstate ----
         lbProgressstate.setText("Progress:");
         contentPane.add(lbProgressstate, CC.xy(2, 21));
         contentPane.add(pbProgress, CC.xy(2, 23));
 
         //======== pCmdButtons ========
         {
             pCmdButtons.setLayout(new FormLayout(
                 "$glue, $lcgap, default, $lcgap, $glue, $lcgap, default, $lcgap, $glue",
                 "default, $lgap, $lcgap"));
 
             //---- btnDeCompile ----
             btnDeCompile.setText("Decompile Firmware");
             btnDeCompile.setFocusable(false);
             btnDeCompile.setEnabled(false);
             btnDeCompile.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     btnCompileActionPerformed(e);
                 }
             });
             pCmdButtons.add(btnDeCompile, CC.xy(3, 1));
 
             //---- btnBuild ----
             btnBuild.setText("Build Firmware");
             btnBuild.setFocusable(false);
             btnBuild.setEnabled(false);
             btnBuild.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     btnBuildActionPerformed(e);
                 }
             });
             pCmdButtons.add(btnBuild, CC.xy(7, 1));
         }
         contentPane.add(pCmdButtons, CC.xy(2, 25));
         pack();
         setLocationRelativeTo(getOwner());
         // JFormDesigner - End of component initialization  //GEN-END:initComponents
     }
 
     public static void main(String[] args) {
         if (args.length > 0) {
             if (args[0].equals("-h") || args[0].equals("-help") || args[0].equals("/h") || args[0].equals("/help")) {
                 LOGGER.info("Usage: jmfb  -b[uild] <project name> <firmware.zip>");
                 System.exit(0);
             }
         }
         //<editor-fold desc="Init mainFrm">
         if (isOSX()) {
             System.setProperty("apple.laf.useScreenMenuBar", "true");
         }
         try {
             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         } catch (ClassNotFoundException e) {
             LOGGER.info(e.getMessage());
             JOptionPane.showMessageDialog(null, "<html><table width=300>" + e.getMessage());
         } catch (InstantiationException e) {
             LOGGER.info(e.getMessage());
             JOptionPane.showMessageDialog(null, "<html><table width=300>" + e.getMessage());
         } catch (IllegalAccessException e) {
             LOGGER.info(e.getMessage());
             JOptionPane.showMessageDialog(null, "<html><table width=300>" + e.getMessage());
         } catch (UnsupportedLookAndFeelException e) {
             LOGGER.info(e.getMessage());
             JOptionPane.showMessageDialog(null, "<html><table width=300>" + e.getMessage());
         }
         mainForm mainFrm = new mainForm();
         mainFrm.setDefaultCloseOperation(EXIT_ON_CLOSE);
         mainFrm.setVisible(mainFrm.initMFB(args));
         //</editor-fold>
     }
 
     private static void setupLogging(Verbosity verbosity) {
         Logger logger = Logger.getLogger("");
         for (Handler handler : logger.getHandlers()) {
             logger.removeHandler(handler);
         }
         if (verbosity == Verbosity.QUIET) {
             return;
         }
 
         Handler handler = new ConsoleHandler();
         logger.addHandler(handler);
 
         if (verbosity == Verbosity.VERBOSE) {
             handler.setLevel(Level.ALL);
             logger.setLevel(Level.ALL);
         } else {
             handler.setFormatter(new java.util.logging.Formatter() {
                 @Override
                 public String format(LogRecord logRecord) {
                     try {
                         Date now = new Date();
                         FileWriter sw = new FileWriter(System.getProperty("user.dir") + File.separatorChar + "Logging.txt", true);
                         sw.write("[" + now.toString() + "] " + logRecord.getMessage() + System.getProperty("line.separator"));
                         sw.close();
                     } catch (Exception e) {
                         System.out.print(e.getMessage());
                     }
                     return logRecord.getMessage() + System.getProperty("line.separator");
                 }
             });
         }
     }
 
     private static enum Verbosity {
         NORMAL, VERBOSE, QUIET;
     }
 
     // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
     private JMenuBar mbMainBar;
     private JMenu fileMenu;
     private JMenuItem miNewProject;
     private JMenuItem miOpenProject;
     private JLabel headerLogo;
     private JLabel lblFirmwareHelp;
     private JPanel pOpenFirmware;
     private JTextField edtFirmwareFile;
     private JButton btnBrowse;
     private JSeparator spSeparator1;
     private JLabel lbAdditionalSets;
     private JPanel pAddons;
     private JLabel lbTimezone;
     private JComboBox cbTimeZone;
     private JLabel lbLang;
     private JComboBox cbLang;
     private JCheckBox decompAll;
     private JCheckBox cbDisableCS;
     private JSeparator spSeparator2;
     private JLabel lbTranslRepo;
     private JPanel pRepos;
     private JScrollPane spRepos;
     private JList lstRepos;
     private JSeparator spSeparator3;
     private JLabel lbProgressstate;
     private JProgressBar pbProgress;
     private JPanel pCmdButtons;
     private JButton btnDeCompile;
     private JButton btnBuild;
     // JFormDesigner - End of variables declaration  //GEN-END:variables
     private String workDir;
     private String aAppsDir = "";
     private String binDir = "";
     private String projectName;
     private String titleProjName;
     private static boolean cmd = false;
     private String[] langs = {"ru", "uk", "en"};
     private String[] regions = {"RU", "UK", "US"};
     private List<String> timeZones = new ArrayList<String>();
     private int repos_count = 2;
     private List<String> repos_names = new ArrayList<String>(Arrays.asList("Russian translation for MIUI based on Android 4.x (KDGDev)", "Ukrainian translation for MIUI based on Android 4.x (KDGDev)"));
     private List<String> repos_git = new ArrayList<String>(Arrays.asList("KDGDev/miui-v4-russian-translation-for-miuiandroid", "KDGDev/miui-v4-ukrainian-translation-for-miuiandroid"));
     private List<String> repos_lang = new ArrayList<String>(Arrays.asList("Russian", "Ukrainian"));
     //private List<String> repos_branches = new ArrayList<String>(Arrays.asList("master", "master"));
     private String repo_Precompiled = "miuirussia/jmfb-firmware-precompiled";
     private String repo_Bootanimation = "KDGDev/jmfb-bootanimation";
     private String repo_Overlay = "KDGDev/jmfb-additional";
     private String repo_Patches = "KDGDev/jmfb-patches";
     private Boolean HWUpdate = false;
     private Boolean UpdateFromFolder = false;
     private Boolean writeBProp = true;
     private Boolean isJB = false;
     private String otaUpdateURL = "http://ota.romz.bz/update-v4.php";
 }
