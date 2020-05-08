 package com.kdgdev.jMFB;
 
 import com.jgoodies.forms.factories.CC;
 import com.jgoodies.forms.layout.FormLayout;
 import com.kdgdev.apkengine.utils.*;
 import com.kdgdev.frontend;
 import net.lingala.zip4j.core.ZipFile;
 import net.lingala.zip4j.exception.ZipException;
 import org.apache.commons.io.FileUtils;
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
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import java.util.List;
 import java.util.logging.*;
 
 
 /**
  * @author Kirill
  */
 
 public class mainForm extends JFrame {
 
     private final static frontend kFrontend = new frontend(System.getProperty("user.dir") + File.separatorChar + "aApps");
 
     private final static Logger LOGGER = Logger.getLogger(frontend.class.getName());
 
     public mainForm() {
         initComponents();
     }
 
     private void readLanuagesFile(String fileName, Boolean cleanLangs) throws IOException {
         if (cleanLangs) {
             repos_names.clear();
             repos_git.clear();
             repos_lang.clear();
             repos_count = 0;
         }
         //String sPattern = "(.*)=(.*)=(.*)";
         //Pattern p = Pattern.compile(sPattern);
         File file = new File(fileName);
         List<String> contents = FileUtils.readLines(file);
         for (String line : contents) {
             //Matcher m = p.matcher(line);
             String[] all = line.split("=");
             repos_names.add(all[0]);
             repos_git.add(all[1]);
             repos_lang.add(all[2]);
             repos_count++;
             //LOGGER.info("Added language: " + all[0]);
         }
     }
 
     public static boolean isOSX() {
         String osName = System.getProperty("os.name");
         return osName.contains("OS X");
     }
 
     public static boolean isWindows() {
 
         String os = System.getProperty("os.name").toLowerCase();
         return (os.contains("win"));
 
     }
 
     public static boolean isUnix() {
 
         String os = System.getProperty("os.name").toLowerCase();
         return (os.contains("nix") || os.contains("nux"));
 
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
 
     }
 
     private boolean initMFB(String[] args) {
         setupLogging(Verbosity.NORMAL);
         Date now = new Date();
         SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
         LOGGER.info("[Program log started at " + formatter.format(now) + "]");
         LOGGER.info("");
         LOGGER.info("====================================");
         LOGGER.info("|    Welcome to " + Branding + "   |");
         LOGGER.info("| Original version only on ROMZ.bz |");
         LOGGER.info("|  This program code protected by  |");
         LOGGER.info("|             US Patents           |");
         LOGGER.info("====================================");
         workDir = System.getProperty("user.dir");
         //LOGGER.info("WorkDir = " + workDir);
         aAppsDir = workDir + File.separatorChar + "aApps" + File.separatorChar + getOSDir();
         binDir = workDir + File.separatorChar + "aApps" + File.separatorChar + "bin";
         //LOGGER.info("aAppsDir = " + aAppsDir);
         try {
             if (new File(workDir + File.separatorChar + "repos.list").exists())
                 readLanuagesFile(workDir + File.separatorChar + "repos.list", true);
             if (new File(workDir + File.separatorChar + "repos_add.list").exists())
                 readLanuagesFile(workDir + File.separatorChar + "repos_add.list", false);
         } catch (IOException e) {
             StringWriter sw = new StringWriter();
             PrintWriter pw = new PrintWriter(sw);
             e.printStackTrace(pw);
             LOGGER.info(sw.toString());
         }
         jmfbInit bbb2 = new jmfbInit();
         bbb2.execute();
 
 
         //<editor-fold desc="Init main patches">
         if (!execFile(false, "aapt") || !execFile(false, "zipalign")) {
             aAppsDir = "";
             if (!execFile(false, "aapt") || !execFile(false, "zipalign")) {
                 JOptionPane.showMessageDialog(null, "Failed to init " + Branding, "Alert", JOptionPane.ERROR_MESSAGE);
                 System.exit(2);
             }
         }
         kFrontend.setAAPTdir("aapt");
         //kAndrolib.setAapTool(aAppsDir + File.separatorChar + "aapt");
         if (args.length > 0) {
             if (args[0].equals("-v")) setupLogging(Verbosity.VERBOSE);
         }
         //</editor-fold>
         Properties writeProp = new Properties();
         try {
             if (new File(workDir + File.separatorChar + "jmfb.properties").exists()) {
                 writeProp.load(new FileInputStream(workDir + File.separatorChar + "jmfb.properties"));
             } else {
                 InputStream in = mainForm.class.getResourceAsStream("/properties/jmfb.properties");
                 writeProp.load(in);
             }
             //LOGGER.info("Properties loaded:");
             repo_Precompiled = writeProp.getProperty("repo_Precompiled", repo_Precompiled);
             //LOGGER.info(" - repo_Precompiled = " + repo_Precompiled);
             repo_Bootanimation = writeProp.getProperty("repo_Bootanimation", repo_Bootanimation);
             //LOGGER.info(" - repo_Bootanimation = " + repo_Bootanimation);
             repo_Overlay = writeProp.getProperty("repo_Overlay", repo_Overlay);
             //LOGGER.info(" - repo_Overlay = " + repo_Overlay);
             repo_Patches = writeProp.getProperty("repo_Patches", repo_Patches);
             //LOGGER.info(" - repo_Patches = " + repo_Patches);
             otaUpdateURL = writeProp.getProperty("otaUpdateURL", otaUpdateURL);
             //LOGGER.info(" - otaUpdateURL = " + otaUpdateURL);
             writeBProp = Boolean.parseBoolean(writeProp.getProperty("writeBuildProp", writeBProp.toString()));
             //LOGGER.info(" - writeBuildProp = " + writeBProp.toString());
             HWUpdate = Boolean.parseBoolean(writeProp.getProperty("HWUpdate", HWUpdate.toString()));
             //LOGGER.info(" - HWUpdate = " + HWUpdate.toString());
             UpdateFromFolder = Boolean.parseBoolean(writeProp.getProperty("UpdateFromFolder", UpdateFromFolder.toString()));
             //LOGGER.info(" - UpdateFromFolder = " + UpdateFromFolder.toString());
         } catch (IOException e) {
             LOGGER.info("Properties not found. Loading defaults...OK!");
         }
         return true;
     }
 
     private boolean execFile(Boolean testReturn, String... cmd) {
         if (!aAppsDir.equals("")) cmd[0] = aAppsDir + File.separatorChar + cmd[0];
         ProcessBuilder procBuilder = new ProcessBuilder(cmd);
         procBuilder.redirectErrorStream(true);
         Process process;
         try {
             process = procBuilder.start();
             InputStream stdout = process.getInputStream();
             InputStreamReader isrStdout = new InputStreamReader(stdout);
             BufferedReader brStdout = new BufferedReader(isrStdout);
             String line;
             while ((line = brStdout.readLine()) != null) {
                 if (testReturn) LOGGER.info(line);
             }
             int exitVal = process.waitFor();
             if (testReturn) {
                 if (exitVal == 2) return false;
             }
         } catch (Exception e) {
             StringWriter sw = new StringWriter();
             PrintWriter pw = new PrintWriter(sw);
             e.printStackTrace(pw);
             LOGGER.info(sw.toString());
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
         } catch (Exception e) {
             StringWriter sw = new StringWriter();
             PrintWriter pw = new PrintWriter(sw);
             e.printStackTrace(pw);
             LOGGER.info(sw.toString());
             JOptionPane.showMessageDialog(null, "<html><table width=300>" + sw.toString());
         }
         new File(Folder + File.separatorChar + "README").delete();
         pbProgress.setIndeterminate(false);
     }
 
     private void writeAllBPValues(buildPropTools bldprp, boolean onlySafe) throws UnknownHostException {
         if (writeBProp) {
             bldprp.writeProp("ro.config.ringtone", "MI.ogg");
             bldprp.writeProp("ro.config.notification_sound", "FadeIn.ogg");
             bldprp.writeProp("ro.config.alarm_alert", "GoodMorning.ogg");
             bldprp.writeProp("ro.config.sms_received_sound", "FadeIn.ogg");
             bldprp.writeProp("ro.config.sms_delivered_sound", "MessageComplete.ogg");
         }
         bldprp.writeProp("ro.build.user", System.getProperty("user.name").toLowerCase());
         bldprp.writeProp("ro.build.host", InetAddress.getLocalHost().getHostName().toLowerCase());
         Calendar now = Calendar.getInstance(Locale.getDefault());
         bldprp.writeProp("persist.sys.timezone", timeZones.get(cbTimeZone.getSelectedIndex()));
         bldprp.writeProp("ro.tt.version", "user-2013");
         bldprp.writeProp("ro.build.type", "user");
         bldprp.writeProp("ro.build.tags", "release-keys");
         bldprp.writeProp("ro.build.date", now.getTime().toString());
         bldprp.writeProp("ro.repo.build", "kdgdev");
         long unixTime = System.currentTimeMillis() / 1000L;
         bldprp.writeProp("ro.build.date.utc", Long.toString(unixTime));
         bldprp.writeProp("ro.product.locale.language", langs[cbLang.getSelectedIndex()]);
         bldprp.writeProp("ro.product.locale.region", regions[cbLang.getSelectedIndex()]);
 
 
         if (!onlySafe) {
 
             bldprp.writeProp("dalvik.vm.dexopt-flags", "m=y,o=v,u=y");
             bldprp.writeProp("ro.product.mod_device", bldprp.readProp("ro.product.device"));
             bldprp.writeProp("dalvik.vm.verify-bytecode", "false");
             bldprp.writeProp("persist.sys.purgeable_assets", "1");
             bldprp.writeProp("persist.sys.use_dithering", "1");
 
             //Энергосбережение
             //bldprp.writeProp("ro.ril.disable.power.collapse", "1");
             bldprp.writeProp("pm.sleep_mode", "1");
             bldprp.writeProp("windowsmgr.max_events_per_sec", "60");
             bldprp.writeProp("wifi.supplicant_scan_interval", "180");
 
             //Ускорение скорости передачи данных
             bldprp.writeProp("net.tcp.buffersize.default", "4096,87380,256960,4096,16384,256960");
             bldprp.writeProp("net.tcp.buffersize.wifi", "4096,87380,256960,4096,16384,256960");
             bldprp.writeProp("net.tcp.buffersize.umts", "4096,87380,256960,4096,16384,256960");
             bldprp.writeProp("net.tcp.buffersize.gprs", "4096,87380,256960,4096,16384,256960");
             bldprp.writeProp("net.tcp.buffersize.edge", "4096,87380,256960,4096,16384,256960");
             bldprp.writeProp("net.tcp.buffersize.evdo_b", "4096,87380,256960,4096,16384,256960");
 
             //Отключение пересылки информации о использовании
             bldprp.writeProp("ro.config.nocheckin", "1");
 
             bldprp.writeProp("ro.kernel.android.checkjni", "0");
             bldprp.writeProp("ro.kernel.checkjni", "0");
 
         }
     }
 
     private boolean isSmaliPatch(String fileName, String translationDir) {
         File patchFile = new File(translationDir + File.separatorChar + fileName + File.separatorChar + "smali");
         return patchFile.exists();
     }
 
     private void DecompileFrmw() {
         try {
             btnBuild.setEnabled(false);
             Properties sets = new Properties();
             if (new File(workDir + File.separatorChar + projectName + File.separatorChar + "jmfb.prop").exists())
                 sets.load(new FileInputStream(workDir + File.separatorChar + projectName + File.separatorChar + "jmfb.prop"));
             btnDeCompile.setEnabled(false);
             pbProgress.setIndeterminate(true);
             new File(workDir + File.separatorChar + projectName).mkdirs();
             kFrontend.setFrameworksFolder(workDir + File.separatorChar + projectName + File.separatorChar + "MFB_Core");
             File fromFile = new File(edtFirmwareFile.getText());
             File toFile = new File(workDir + File.separatorChar + projectName + File.separatorChar + "stockrom.zip");
             //<editor-fold desc="Extracting firmware and update files">
             pbProgress.setIndeterminate(true);
             if (!new File(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware").exists()) {
                 toggleFirmwareChoise(false);
                 lbProgressstate.setText("Extracting firmware...");
 
                 if (!toFile.exists()) FileUtils.copyFile(fromFile, toFile);
                 extractFolder(toFile.getAbsolutePath(), workDir + File.separatorChar + projectName + File.separatorChar + "Firmware");
                 extractFolder(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar + "system" + File.separatorChar + "media" + File.separatorChar + "theme" + File.separatorChar + "default" + File.separatorChar + "lockscreen", workDir + File.separatorChar + projectName + File.separatorChar + "Lockscreen");
                 String frmDir = workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar + "system" + File.separatorChar + "framework" + File.separatorChar;
                 if (!cbNotOdex.isSelected()) {
                     if (new File(frmDir + "core.odex").exists() || new File(frmDir + "ext.odex").exists() || new File(frmDir + "framework.odex").exists() || new File(frmDir + "android.policy.odex").exists() || new File(frmDir + "services.odex").exists()) {
                         lbProgressstate.setText("Deodexing firmware...");
                         LOGGER.info("----- Starting BurgerZ deodex code -----");
                         try {
                             kFrontend.deodexFirmware(workDir, workDir + File.separatorChar + projectName + File.separatorChar + "Firmware", 16, "app", ".apk");
                             kFrontend.deodexFirmware(workDir, workDir + File.separatorChar + projectName + File.separatorChar + "Firmware", 16, "framework", ".jar");
                             LOGGER.info("----- DONE! -----");
                         } catch (Exception e) {
                             StringWriter sw = new StringWriter();
                             PrintWriter pw = new PrintWriter(sw);
                             e.printStackTrace(pw);
                             LOGGER.info(sw.toString());
                             JOptionPane.showMessageDialog(null, "<html><table width=300>" + sw.toString());
                         }
                     }
                 }
             }
             //</editor-fold>
 
             buildPropTools bldprop = new buildPropTools(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar + "system" + File.separatorChar + "build.prop");
 
 
             LOGGER.info("!--------------- Firmware info: ---------------------!");
             LOGGER.info("!!Device name: " + bldprop.readProp("ro.product.device"));
             LOGGER.info("!!Version: " + bldprop.readProp("ro.build.version.incremental"));
             LOGGER.info("!!Android version: " + bldprop.readProp("ro.build.version.release"));
             LOGGER.info("!----------------------------------------------------!");
 
             kFrontend.
                     installFrameworks(workDir + File.separatorChar + "aApps" + File.separatorChar + "plugs");
             //<editor-fold desc="Downloading precompiled files from git">
             if (!new File(workDir + File.separatorChar + projectName + File.separatorChar + "PrecompiledFiles").exists()) {
                 lbProgressstate.setText("Getting precompiled files...");
                 getFilesFromGit(workDir + File.separatorChar + projectName + File.separatorChar + "PrecompiledFiles", repo_Precompiled);
                 new File(workDir + File.separatorChar + projectName + File.separatorChar + "PrecompiledFiles" + File.separatorChar + "main" + File.separatorChar + "system" + File.separatorChar + "app").mkdirs();
                 new gitTools().downloadFileFromGit("MiCode/patchrom_miui", "system/app/Updater.apk", workDir + File.separatorChar + projectName + File.separatorChar + "PrecompiledFiles" + File.separatorChar + "main" + File.separatorChar + "system" + File.separatorChar + "app" + File.separatorChar + "Updater.apk", "ics");
                 //new gitTools().downloadFileFromGit("MiCode/patchrom_miui", "system/app/LatinIME.apk", workDir + File.separatorChar + projectName + File.separatorChar + "PrecompiledFiles" + File.separatorChar + "system" + File.separatorChar + "app" + File.separatorChar + "LatinIME.apk", "ics");
                 //new gitTools().downloadFileFromGit("MiCode/patchrom_miui", "system/lib/libjni_latinime.so", workDir + File.separatorChar + projectName + File.separatorChar + "PrecompiledFiles" + File.separatorChar + "system" + File.separatorChar + "lib" + File.separatorChar + "libjni_latinime.so", "ics");
                 new gitTools().downloadFileFromGit(repo_Bootanimation, "system/media/bootanimation.zip", workDir + File.separatorChar + projectName + File.separatorChar + "PrecompiledFiles" + File.separatorChar + "main" + File.separatorChar + "system" + File.separatorChar + "media" + File.separatorChar + "bootanimation.zip");
                 lbProgressstate.setText("Updating files...");
                 File src = new File(workDir + File.separatorChar + projectName + File.separatorChar + "PrecompiledFiles" + File.separatorChar + "main" + File.separatorChar);
                 File dsc = new File(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar);
                 FileUtils.copyDirectory(src, dsc);
                 if (new File(workDir + File.separatorChar + projectName + File.separatorChar + "PrecompiledFiles" + File.separatorChar + "device" + File.separatorChar + bldprop.readProp("ro.product.device") + File.separatorChar).exists()) {
                     src = new File(workDir + File.separatorChar + projectName + File.separatorChar + "PrecompiledFiles" + File.separatorChar + "device" + File.separatorChar + bldprop.readProp("ro.product.device") + File.separatorChar);
                     dsc = new File(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar);
                     FileUtils.copyDirectory(src, dsc);
                 }
             }
             if (new File(workDir + File.separatorChar + "Override" + File.separatorChar + bldprop.readProp("ro.product.device")).exists() && UpdateFromFolder) {
                 LOGGER.info("Override system files...");
                 lbProgressstate.setText("Updating files...");
                 File src = new File(workDir + File.separatorChar + "Override" + File.separatorChar + bldprop.readProp("ro.product.device") + File.separatorChar);
                 File dsc = new File(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar);
                 FileUtils.copyDirectory(src, dsc);
                 LOGGER.info("Copy done: " + src.getAbsolutePath() + " -> " + dsc.getAbsolutePath());
             }
             //</editor-fold>
 
             lbProgressstate.setText("Getting translation files...");
             //<editor-fold desc="Downloading translation files from git">
             deleteDirectory(new File(workDir + File.separatorChar + projectName + File.separatorChar + "Language_Git"));
 
             for (int i = 0; i < repos_count; i++) {
                 if (lstRepos.isSelectedIndex(i)) {
                     getFilesFromGit(workDir + File.separatorChar + projectName + File.separatorChar + "Language" + ((Integer) i).toString(), repos_git.get(i));
                     File source = new File(workDir + File.separatorChar + projectName + File.separatorChar + "Language" + ((Integer) i).toString() + File.separatorChar + repos_lang.get(i));
                     File desc = new File(workDir + File.separatorChar + projectName + File.separatorChar + "Language_Git");
                     FileUtils.copyDirectory(source, desc);
                 }
                 deleteDirectory(new File(workDir + File.separatorChar + projectName + File.separatorChar + "Language" + ((Integer) i).toString()));
             }
 
             deleteDirectory(new File(workDir + File.separatorChar + projectName + File.separatorChar + "Language_Eng"));
             getFilesFromGit(workDir + File.separatorChar + projectName + File.separatorChar + "Additional", repo_Overlay);
             File source = new File(workDir + File.separatorChar + projectName + File.separatorChar + "Additional");
             File desc = new File(workDir + File.separatorChar + projectName + File.separatorChar + "Language_Git");
             FileUtils.copyDirectory(source, desc);
             deleteDirectory(new File(workDir + File.separatorChar + projectName + File.separatorChar + "Additional"));
             if (!sets.getProperty("Patches", "no").contains("downloaded")) {
                 getFilesFromGit(workDir + File.separatorChar + projectName + File.separatorChar + "Additional", repo_Patches);
                 source = new File(workDir + File.separatorChar + projectName + File.separatorChar + "Additional");
                 desc = new File(workDir + File.separatorChar + projectName + File.separatorChar + "Language_Git");
                 FileUtils.copyDirectory(source, desc);
                 deleteDirectory(new File(workDir + File.separatorChar + projectName + File.separatorChar + "Additional"));
                 sets.setProperty("Patches", "downloaded");
             }
             if (new File(workDir + File.separatorChar + "Language_Overlay").exists()) {
                 source = new File(workDir + File.separatorChar + "Language_Overlay");
                 desc = new File(workDir + File.separatorChar + projectName + File.separatorChar + "Language_Git");
                 FileUtils.copyDirectory(source, desc);
             }
             //</editor-fold>
             source = new File(workDir + File.separatorChar + projectName + File.separatorChar + "Language_Git" + File.separatorChar + "extras" + File.separatorChar + "lockscreen");
             desc = new File(workDir + File.separatorChar + projectName + File.separatorChar + "Lockscreen");
             FileUtils.copyDirectory(source, desc);
             //String buildPropPath = workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar + "system" + File.separatorChar + "build.prop";
             setTitle(Branding + " - " + titleProjName + " (" + bldprop.readProp("ro.product.device") + ")");
             writeAllBPValues(bldprop, bldprop.readProp("ro.product.device").contains("H958"));
             lbProgressstate.setText("Installing frameworks...");
             kFrontend.installFrameworks(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar + "system" + File.separatorChar + "framework");
             //<editor-fold desc="Building new workspace - decompiling app and frameworks">
 
             Boolean molp = new File(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar + "data" + File.separatorChar + "media" + File.separatorChar + "preinstall_apps").exists();
             if (!molp)
                 molp = new File(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar + "data" + File.separatorChar + "preinstall_apps").exists();
             if (!molp)
                 molp = !new File(workDir + File.separatorChar + projectName + File.separatorChar + "DataSources").exists();
 
             if (molp) {
                 try {
                     lbProgressstate.setText("Building new workspace - Decompiling data apps...");
                     File apkSrc = new File(workDir + File.separatorChar + projectName + File.separatorChar + "DataSources");
                     apkSrc.mkdirs();
                     pbProgress.setIndeterminate(false);
                     searchTools finder = new searchTools();
                     List apkFiles;
                     if (new File(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar + "data" + File.separatorChar + "media" + File.separatorChar + "preinstall_apps").exists()) {
                         apkFiles = finder.findAll(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar + "data" + File.separatorChar + "media" + File.separatorChar + "preinstall_apps", ".*.apk");
                     } else {
                         apkFiles = finder.findAll(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar + "data" + File.separatorChar + "preinstall_apps", ".*.apk");
                     }
                     pbProgress.setMaximum(apkFiles.size());
                     pbProgress.setValue(0);
                     for (int i = 0; i < apkFiles.size(); i++) {
                         File frm = new File(apkFiles.get(i).toString());
                         if (new File(workDir + File.separatorChar + projectName + File.separatorChar + "Language_Git" + File.separatorChar + "main" + File.separatorChar + frm.getName()).exists() || new File(workDir + File.separatorChar + projectName + File.separatorChar + "Language_Git" + File.separatorChar + "device" + File.separatorChar + bldprop.readProp("ro.product.device") + File.separatorChar + frm.getName()).exists() || decompAll.isSelected()) {
                             pbProgress.setValue(i);
                             LOGGER.info("======== Decompiling " + frm.getName() + " ========");
                             kFrontend.decompileFile(apkFiles.get(i).toString(), workDir + File.separatorChar + projectName + File.separatorChar + "DataSources" + File.separatorChar + frm.getName(), isSmaliPatch(frm.getName(), workDir + File.separatorChar + projectName + File.separatorChar + "Language_Git" + File.separatorChar + "main") || isSmaliPatch(frm.getName(), workDir + File.separatorChar + projectName + File.separatorChar + "Language_Git" + File.separatorChar + "device" + File.separatorChar + bldprop.readProp("ro.product.device")));
                             kFrontend.rebuildFiles(workDir + File.separatorChar + projectName + File.separatorChar + "DataSources" + File.separatorChar + frm.getName(), frm.getName().toLowerCase(), otaUpdateURL, bldprop.readProp("ro.product.device"));
                         } else {
                             frm.delete();
                         }
                     }
                 } catch (Exception e) {
                     LOGGER.info(e.getMessage());
                 }
             }
 
             if (!new File(workDir + File.separatorChar + projectName + File.separatorChar + "AppsSources").exists()) {
                 try {
                     lbProgressstate.setText("Building new workspace - Decompiling apps...");
                     File apkSrc = new File(workDir + File.separatorChar + projectName + File.separatorChar + "AppsSources");
                     apkSrc.mkdirs();
                     pbProgress.setIndeterminate(false);
                     searchTools finder = new searchTools();
                     List apkFiles = finder.findAll(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar + "system" + File.separatorChar + "app", ".*.apk");
                     pbProgress.setMaximum(apkFiles.size());
                     pbProgress.setValue(0);
                     for (int i = 0; i < apkFiles.size(); i++) {
                         File frm = new File(apkFiles.get(i).toString());
                         if (new File(workDir + File.separatorChar + projectName + File.separatorChar + "Language_Git" + File.separatorChar + "main" + File.separatorChar + frm.getName()).exists() || new File(workDir + File.separatorChar + projectName + File.separatorChar + "Language_Git" + File.separatorChar + "device" + File.separatorChar + bldprop.readProp("ro.product.device") + File.separatorChar + frm.getName()).exists() || decompAll.isSelected()) {
                             pbProgress.setValue(i);
                             LOGGER.info("======== Decompiling " + frm.getName() + " ========");
                             kFrontend.decompileFile(apkFiles.get(i).toString(), workDir + File.separatorChar + projectName + File.separatorChar + "AppsSources" + File.separatorChar + frm.getName(), isSmaliPatch(frm.getName(), workDir + File.separatorChar + projectName + File.separatorChar + "Language_Git" + File.separatorChar + "main") || isSmaliPatch(frm.getName(), workDir + File.separatorChar + projectName + File.separatorChar + "Language_Git" + File.separatorChar + "device" + File.separatorChar + bldprop.readProp("ro.product.device")) || frm.getName().equalsIgnoreCase("updater.apk") || frm.getName().equalsIgnoreCase("mms.apk") || frm.getName().equalsIgnoreCase("miuihome.apk"));
                             kFrontend.rebuildFiles(workDir + File.separatorChar + projectName + File.separatorChar + "AppsSources" + File.separatorChar + frm.getName(), frm.getName().toLowerCase(), otaUpdateURL, bldprop.readProp("ro.product.device"));
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
                 } catch (Exception e) {
                     LOGGER.info(e.getMessage());
                 }
             }
             //deleteDirectory(new File(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar + "data"));
             if (!new File(workDir + File.separatorChar + projectName + File.separatorChar + "FrameworkSources").exists()) {
                 try {
                     lbProgressstate.setText("Building new workspace - Decompiling frameworks...");
                     File frmSrc = new File(workDir + File.separatorChar + projectName + File.separatorChar + "FrameworkSources");
                     frmSrc.mkdirs();
                     searchTools finder = new searchTools();
                     List frmFiles = finder.findAll(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar + "system" + File.separatorChar + "framework", ".*.apk");
                     pbProgress.setMaximum(frmFiles.size());
                     pbProgress.setValue(0);
                     for (int i = 0; i < frmFiles.size(); i++) {
                         File frm = new File(frmFiles.get(i).toString());
                         if (new File(workDir + File.separatorChar + projectName + File.separatorChar + "Language_Git" + File.separatorChar + "main" + File.separatorChar + frm.getName()).exists() || new File(workDir + File.separatorChar + projectName + File.separatorChar + "Language_Git" + File.separatorChar + "device" + File.separatorChar + bldprop.readProp("ro.product.device") + File.separatorChar + frm.getName()).exists() || decompAll.isSelected()) {
                             pbProgress.setValue(i);
                             LOGGER.info("======== Decompiling " + frm.getName() + " ========");
                             kFrontend.decompileFile(frmFiles.get(i).toString(), workDir + File.separatorChar + projectName + File.separatorChar + "FrameworkSources" + File.separatorChar + frm.getName(), false);
                             kFrontend.rebuildFiles(workDir + File.separatorChar + projectName + File.separatorChar + "FrameworkSources" + File.separatorChar + frm.getName(), frm.getName().toLowerCase(), otaUpdateURL, bldprop.readProp("ro.product.device"));
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
                 } catch (Exception e) {
                     LOGGER.info(e.getMessage());
                 }
             }
             //</editor-fold>
             pbProgress.setMaximum(100);
             pbProgress.setValue(100);
             pbProgress.setIndeterminate(false);
             lbProgressstate.setText("Done!");
             //sets.setProperty("FirmwareFile", toFile.getAbsolutePath());
             for (int i = 0; i < repos_count; i++) {
                 if (lstRepos.isSelectedIndex(i))
                     sets.setProperty("Repo" + ((Integer) i).toString(), "1");
                 else sets.setProperty("Repo" + ((Integer) i).toString(), "0");
             }
             sets.store(new FileOutputStream(workDir + File.separatorChar + projectName + File.separatorChar + "jmfb.prop"), "KDevGroup Settings");
             LOGGER.info("======== End of decompiling files ========");
             btnBuild.setEnabled(true);
             bldprop.write();
         } catch (Exception e) {
             StringWriter sw = new StringWriter();
             PrintWriter pw = new PrintWriter(sw);
             e.printStackTrace(pw);
             LOGGER.info(sw.toString());
             JOptionPane.showMessageDialog(null, "<html><table width=300>" + sw.toString());
         }
     }
 
     private class DecompileFirmware extends SwingWorker {
 
         @Override
         protected Object doInBackground() {
             oneClick.setEnabled(false);
             DecompileFrmw();
             //return 1;
             return null;
         }
     }
 
     private void deleteDirectory(File Dir) throws Exception {
         kFrontend.deleteDirectory(Dir);
     }
 
     private void CompileFrmw() {
         searchTools finder = new searchTools();
         btnBuild.setEnabled(false);
         try {
             buildPropTools bldprop = new buildPropTools(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar + "system" + File.separatorChar + "build.prop");
             deleteDirectory(new File(workDir + File.separatorChar + projectName + File.separatorChar + "build"));
             List buildFiles = finder.findDirectories_InFolder(workDir + File.separatorChar + projectName + File.separatorChar + "AppsSources", ".*.apk");
             pbProgress.setMaximum(buildFiles.size());
             pbProgress.setValue(0);
             lbProgressstate.setText("Building apps...");
             deleteDirectory(new File(workDir + File.separatorChar + projectName + File.separatorChar + "AppsCompiled"));
             new File(workDir + File.separatorChar + projectName + File.separatorChar + "AppsCompiled").mkdirs();
             kFrontend.setFrameworksFolder(workDir + File.separatorChar + projectName + File.separatorChar + "MFB_Core");
             File fXml = new File(workDir + File.separatorChar + "aApps" + File.separatorChar + "security" + File.separatorChar + "apkcerts.txt");
            kFrontend.patchXMLs(workDir + File.separatorChar + projectName + File.separatorChar + "AppsSources", workDir + File.separatorChar + projectName + File.separatorChar + "Language_Git", workDir + File.separatorChar+ "aApps" + File.separatorChar + "patcher.config");
             for (int i = 0; i < buildFiles.size(); i++) {
                 pbProgress.setValue(i);
                 File sourceDir = new File(buildFiles.get(i).toString());
                 if (new File(workDir + File.separatorChar + projectName + File.separatorChar + "Language_Git" + File.separatorChar + "main" + File.separatorChar + (new File(buildFiles.get(i).toString()).getName())).exists()) {
                     FileUtils.copyDirectory(new File(workDir + File.separatorChar + projectName + File.separatorChar + "Language_Git" + File.separatorChar + "main" + File.separatorChar + (new File(buildFiles.get(i).toString()).getName())), sourceDir);
                 }
                 if (new File(workDir + File.separatorChar + projectName + File.separatorChar + "Language_Git" + File.separatorChar + "device" + File.separatorChar + bldprop.readProp("ro.product.device") + File.separatorChar + (new File(buildFiles.get(i).toString()).getName())).exists()) {
                     FileUtils.copyDirectory(new File(workDir + File.separatorChar + projectName + File.separatorChar + "Language_Git" + File.separatorChar + "device" + File.separatorChar + bldprop.readProp("ro.product.device") + File.separatorChar + (new File(buildFiles.get(i).toString()).getName())), sourceDir);
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
                 kFrontend.buildFile(buildFiles.get(i).toString(), workDir + File.separatorChar + projectName + File.separatorChar + "AppsCompiled");
             }
             buildFiles = finder.findDirectories_InFolder(workDir + File.separatorChar + projectName + File.separatorChar + "FrameworkSources", ".*.apk");
             pbProgress.setMaximum(buildFiles.size());
             pbProgress.setValue(0);
             lbProgressstate.setText("Building framework...");
             deleteDirectory(new File(workDir + File.separatorChar + projectName + File.separatorChar + "FrameworkCompiled"));
             new File(workDir + File.separatorChar + projectName + File.separatorChar + "FrameworkCompiled").mkdirs();
            kFrontend.patchXMLs(workDir + File.separatorChar + projectName + File.separatorChar + "FrameworkSources", workDir + File.separatorChar + projectName + File.separatorChar + "Language_Git", workDir + File.separatorChar+ "aApps" + File.separatorChar + "patcher.config");
             for (int i = 0; i < buildFiles.size(); i++) {
                 pbProgress.setValue(i);
                 File sourceDir = new File(buildFiles.get(i).toString());
                 if (new File(workDir + File.separatorChar + projectName + File.separatorChar + "Language_Git" + File.separatorChar + "main" + File.separatorChar + (new File(buildFiles.get(i).toString()).getName())).exists()) {
                     FileUtils.copyDirectory(new File(workDir + File.separatorChar + projectName + File.separatorChar + "Language_Git" + File.separatorChar + "main" + File.separatorChar + (new File(buildFiles.get(i).toString()).getName())), sourceDir);
                 }
                 if (new File(workDir + File.separatorChar + projectName + File.separatorChar + "Language_Git" + File.separatorChar + "device" + File.separatorChar + bldprop.readProp("ro.product.device") + File.separatorChar + (new File(buildFiles.get(i).toString()).getName())).exists()) {
                     FileUtils.copyDirectory(new File(workDir + File.separatorChar + projectName + File.separatorChar + "Language_Git" + File.separatorChar + "device" + File.separatorChar + bldprop.readProp("ro.product.device") + File.separatorChar + (new File(buildFiles.get(i).toString()).getName())), sourceDir);
                 }
                 kFrontend.buildFile(buildFiles.get(i).toString(), workDir + File.separatorChar + projectName + File.separatorChar + "FrameworkCompiled");
             }
             if (new File(workDir + File.separatorChar + projectName + File.separatorChar + "DataSources").exists()) {
                 buildFiles = finder.findDirectories_InFolder(workDir + File.separatorChar + projectName + File.separatorChar + "DataSources", ".*.apk");
                 pbProgress.setMaximum(buildFiles.size());
                 pbProgress.setValue(0);
                 lbProgressstate.setText("Building data...");
                 deleteDirectory(new File(workDir + File.separatorChar + projectName + File.separatorChar + "DataCompiled"));
                 new File(workDir + File.separatorChar + projectName + File.separatorChar + "DataCompiled").mkdirs();
                kFrontend.patchXMLs(workDir + File.separatorChar + projectName + File.separatorChar + "DataSources", workDir + File.separatorChar + projectName + File.separatorChar + "Language_Git", workDir + File.separatorChar+ "aApps" + File.separatorChar + "patcher.config");
                 for (int i = 0; i < buildFiles.size(); i++) {
                     pbProgress.setValue(i);
                     LOGGER.info("======== Compiling " + new File(buildFiles.get(i).toString()).getName() + " ========");
                     File sourceDir = new File(buildFiles.get(i).toString());
                     File dectFile = File.createTempFile("KDGDEV", ".kdg");
                     LOGGER.info(dectFile.getAbsolutePath());
                     File signFile = File.createTempFile("KDGDEV", ".kdg");
                     //String FileName = (new File(buildFiles.get(i).toString()).getName());
                     File dctFile = new File(workDir + File.separatorChar + projectName + File.separatorChar + "DataCompiled" + File.separatorChar + (new File(buildFiles.get(i).toString()).getName()));
                     if (new File(workDir + File.separatorChar + projectName + File.separatorChar + "Language_Git" + File.separatorChar + "main" + File.separatorChar + (new File(buildFiles.get(i).toString()).getName())).exists()) {
                         FileUtils.copyDirectory(new File(workDir + File.separatorChar + projectName + File.separatorChar + "Language_Git" + File.separatorChar + "main" + File.separatorChar + (new File(buildFiles.get(i).toString()).getName())), sourceDir);
                     }
                     if (new File(workDir + File.separatorChar + projectName + File.separatorChar + "Language_Git" + File.separatorChar + "device" + File.separatorChar + bldprop.readProp("ro.product.device") + File.separatorChar + (new File(buildFiles.get(i).toString()).getName())).exists()) {
                         FileUtils.copyDirectory(new File(workDir + File.separatorChar + projectName + File.separatorChar + "Language_Git" + File.separatorChar + "device" + File.separatorChar + bldprop.readProp("ro.product.device") + File.separatorChar + (new File(buildFiles.get(i).toString()).getName())), sourceDir);
                     }
                     //ApkFileSign signerData = new ApkFileSign(new File(workDir + File.separatorChar + "aApps" + File.separatorChar + "security" + File.separatorChar + "apkcerts.txt"));
 
                     //kAndrolib.build(sourceDir, dectFile, true, false, true);
                     kFrontend.buildOnly(sourceDir, dectFile, true, false, false);
                     signApk signer = new signApk();
                     LOGGER.info("Signing with testkey...");
                     signer.signBuildFile(true, workDir + File.separatorChar + "aApps" + File.separatorChar + "security" + File.separatorChar + "testkey.x509.pem", workDir + File.separatorChar + "aApps" + File.separatorChar + "security" + File.separatorChar + "testkey.pk8", dectFile.getAbsolutePath(), signFile.getAbsolutePath());
                     if (execFile(true, "zipalign", "-f", "4", signFile.getAbsolutePath(), dctFile.getAbsolutePath()))
                         LOGGER.info("Zipaligning...");
                     else FileUtils.copyFile(signFile, dctFile);
                     dectFile.delete();
                     signFile.delete();
                 }
             }
             LOGGER.info("======== End of compiling files ========");
             //String buildPropPath = workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar + "system" + File.separatorChar + "build.prop";
             writeAllBPValues(bldprop, bldprop.readProp("ro.product.device").contains("H958"));
             LOGGER.info("Building firmware file...");
             lbProgressstate.setText("Building firmware file...");
             pbProgress.setIndeterminate(true);
             FileUtils.copyDirectory(new File(workDir + File.separatorChar + projectName + File.separatorChar + "FrameworkCompiled"), new File(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar + "system" + File.separatorChar + "framework"));
             FileUtils.copyDirectory(new File(workDir + File.separatorChar + projectName + File.separatorChar + "AppsCompiled"), new File(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar + "system" + File.separatorChar + "app"));
             if (new File(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar + "data" + File.separatorChar + "media" + File.separatorChar + "preinstall_apps").exists())
                 FileUtils.copyDirectory(new File(workDir + File.separatorChar + projectName + File.separatorChar + "DataCompiled"), new File(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar + "data" + File.separatorChar + "media" + File.separatorChar + "preinstall_apps"));
             if (new File(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar + "data" + File.separatorChar + "preinstall_apps").exists())
                 FileUtils.copyDirectory(new File(workDir + File.separatorChar + projectName + File.separatorChar + "DataCompiled"), new File(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar + "data" + File.separatorChar + "preinstall_apps"));
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
             if (new File(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar + "system" + File.separatorChar + "customize").exists())
                 new gitTools().downloadFileFromGit("BurgerZ/MIUI-v4-extra", "device/pyramid/system/app/HTC_IME_fix.apk", (new File(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar + "data" + File.separatorChar + "media" + File.separatorChar + "preinstall_apps").exists()) ? workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar + "data" + File.separatorChar + "media" + File.separatorChar + "preinstall_apps" + File.separatorChar + "HTC_IME_fix.apk" : workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar + "data" + File.separatorChar + "preinstall_apps" + File.separatorChar + "HTC_IME_fix.apk");
             zipTools.zipFile(workDir + File.separatorChar + projectName + File.separatorChar + "Lockscreen", workDir + File.separatorChar + projectName + File.separatorChar + "Firmware" + File.separatorChar + "system" + File.separatorChar + "media" + File.separatorChar + "theme" + File.separatorChar + "default" + File.separatorChar + "lockscreen", true);
             bldprop.write();
 
             zipTools.zipFile(workDir + File.separatorChar + projectName + File.separatorChar + "Firmware", workDir + File.separatorChar + projectName + File.separatorChar + "build" + File.separatorChar + "out" + File.separatorChar + "out.zip", true);
             String firmwareVersion = bldprop.readProp("ro.build.version.incremental");
             String phoneModel = bldprop.readProp("ro.product.device");
             lbProgressstate.setText("Signing firmware file...");
             LOGGER.info("Signing firmware file...");
             new signApk().signBuildFile(true, workDir + File.separatorChar + "aApps" + File.separatorChar + "security" + File.separatorChar + "testkey.x509.pem", workDir + File.separatorChar + "aApps" + File.separatorChar + "security" + File.separatorChar + "testkey.pk8", workDir + File.separatorChar + projectName + File.separatorChar + "build" + File.separatorChar + "out" + File.separatorChar + "out.zip", workDir + File.separatorChar + projectName + File.separatorChar + "build" + File.separatorChar + "out" + File.separatorChar + "miuirussia_" + phoneModel + "_" + firmwareVersion + ".zip");
             new File(workDir + File.separatorChar + projectName + File.separatorChar + "build" + File.separatorChar + "out" + File.separatorChar + "out.zip").delete();
             pbProgress.setIndeterminate(false);
             pbProgress.setMaximum(100);
             pbProgress.setValue(100);
             lbProgressstate.setText("Done!");
             if (!cmd) {
                 JTextArea textArea = new JTextArea();
                 textArea.setText("Firmware builded successfully!\nYou firmware available: <" + Branding + "_Folder>" + File.separatorChar + projectName + File.separatorChar + "build" + File.separatorChar + "out" + File.separatorChar + "miuirussia_" + phoneModel + "_" + firmwareVersion + ".zip\nMD5: " + MD5Checksum.getMD5Checksum(workDir + File.separatorChar + projectName + File.separatorChar + "build" + File.separatorChar + "out" + File.separatorChar + "miuirussia_" + phoneModel + "_" + firmwareVersion + ".zip") + "\nThanks for using " + Branding);
                 textArea.setSize(300, Short.MAX_VALUE);
                 textArea.setWrapStyleWord(true);
                 textArea.setLineWrap(true);
                 textArea.setEditable(false);
                 textArea.setOpaque(false);
                 JOptionPane.showMessageDialog(null, textArea);
 
             } else deleteDirectory(new File(workDir + File.separatorChar + projectName));
             LOGGER.info("======== Compiling done! ========");
         } catch (Exception e) {
             StringWriter sw = new StringWriter();
             PrintWriter pw = new PrintWriter(sw);
             e.printStackTrace(pw);
             LOGGER.info(sw.toString());
             JOptionPane.showMessageDialog(null, "<html><table width=300>" + sw.toString());
         }
         btnBuild.setEnabled(true);
 
     }
 
     private class CompileFirmware extends SwingWorker {
         @Override
         protected Object doInBackground() {
             oneClick.setEnabled(false);
             CompileFrmw();
             return null;
         }
     }
 
     private class OneClickBuild extends SwingWorker {
 
         @Override
         protected Object doInBackground() {
             oneClick.setEnabled(false);
             DecompileFrmw();
             CompileFrmw();
             oneClick.setEnabled(true);
             return null;
         }
     }
 
     private class jmfbInit extends SwingWorker {
 
         @Override
         protected Object doInBackground() {
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
 
             return null;
         }
     }
 
     private void btnCompileActionPerformed(ActionEvent e) {
         DecompileFirmware bbb1 = new DecompileFirmware();
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
         cbNotOdex.setEnabled(State);
         cbTimeZone.setEnabled(State);
         decompAll.setEnabled(State);
         cbLang.setEnabled(State);
         lstRepos.setEnabled(State);
         oneClick.setEnabled(State);
         //cbDisableCS.setEnabled(State);
         //btnDeCompile.setEnabled(State);
     }
 
     public static String removeExtension(String s) {
 
         String separator = System.getProperty("file.separator");
         String filename;
 
         // Remove the path upto the filename.
         int lastSeparatorIndex = s.lastIndexOf(separator);
         if (lastSeparatorIndex == -1) {
             filename = s;
         } else {
             filename = s.substring(lastSeparatorIndex + 1);
         }
 
         // Remove the extension.
         int extensionIndex = filename.lastIndexOf(".");
         if (extensionIndex == -1)
             return filename;
 
         return filename.substring(0, extensionIndex);
     }
 
     private void toggleFirmwareChoise(boolean choise) {
         lblFirmwareHelp.setVisible(choise);
         pOpenFirmware.setVisible(choise);
         spSeparator1.setVisible(choise);
         this.pack();
     }
 
     private void miOpenProjectActionPerformed(ActionEvent e) {
         searchTools finder = new searchTools();
         try {
             List<String> projects = new ArrayList<String>();
             List<File> buildFiles = finder.findDirectories_InFolder(workDir, ".*.mfbproj");
             for (int i = 0; i < buildFiles.size(); i++) {
                 projects.add(removeExtension(buildFiles.get(i).getAbsolutePath()));
             }
             projectName = (String) JOptionPane.showInputDialog(this,
                     "Which project you want to load?",
                     "Selecting project",
                     JOptionPane.QUESTION_MESSAGE,
                     null,
                     projects.toArray(),
                     projects.get(0));
             //projectName = JOptionPane.showInputDialog(null, "Project name:", "Opening MFB project", JOptionPane.QUESTION_MESSAGE);
             titleProjName = projectName;
             projectName = projectName + ".mfbproj";
             if (!projectName.isEmpty() && new File(workDir + File.separatorChar + projectName).exists()) {
                 setTitle(Branding + " - " + titleProjName);
                 setInterfaceState(true);
                 Properties sets = new Properties();
                 try {
                     sets.load(new FileInputStream(workDir + File.separatorChar + projectName + File.separatorChar + "jmfb.prop"));
                     edtFirmwareFile.setText(sets.getProperty("FirmwareFile"));
                     int[] sel = new int[repos_count];
                     for (int i = 0; i < repos_count; i++) {
                         try {
                             String repo = sets.getProperty("Repo" + ((Integer) i).toString());
                             sel[i] = Integer.parseInt(repo);
                         } catch (NumberFormatException ex) {
                             sel[i] = 0;
                         }
 
                     }
                     lstRepos.setSelectedIndices(sel);
                     toggleFirmwareChoise(false);
 
                 } catch (IOException e1) {
                     setInterfaceState(false);
                 }
                 btnCompileActionPerformed(null);
             } else {
                 JOptionPane.showMessageDialog(null, "Project does not exist!\nPlease open exist project or create new.");
             }
         } catch (Exception e1) {
             setInterfaceState(false);
         }
 
     }
 
     private void miNewProjectActionPerformed(ActionEvent e) {
         projectName = JOptionPane.showInputDialog(null, "Project name:", "Creating translation project", JOptionPane.QUESTION_MESSAGE);
         if (projectName == null) projectName = "NoName";
         titleProjName = projectName;
         projectName = projectName + ".mfbproj";
         if (!projectName.isEmpty()) {
             setTitle(Branding + " - " + titleProjName);
             int[] sel = new int[repos_count];
             sel[0] = 1;
             sel[1] = 1;
             lstRepos.setSelectedIndices(sel);
             toggleFirmwareChoise(true);
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
         CompileFirmware bbb1 = new CompileFirmware();
         bbb1.execute();
     }
 
     private void oneClickActionPerformed(ActionEvent e) {
         OneClickBuild bbb1 = new OneClickBuild();
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
         cbNotOdex = new JCheckBox();
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
         oneClick = new JButton();
         btnBuild = new JButton();
 
         //======== this ========
         setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
         setTitle("jMFB v2");
         setBackground(Color.white);
         Container contentPane = getContentPane();
         contentPane.setLayout(new FormLayout(
                 "$lcgap, 204dlu:grow, 2*($lcgap)",
                 "3*(default, $lgap), default, $lcgap, 8*(default, $lgap), default"));
         setTitle(Branding);
 
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
         headerLogo.setIcon(new ImageIcon(getClass().getResource("/com/kdgdev/jMFB/resources/header.png")));
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
             cbLang.setModel(new DefaultComboBoxModel(new String[]{
                     "Russian",
                     "Ukrainian",
                     "English (US)"
             }));
             cbLang.setFocusable(false);
             cbLang.setEnabled(false);
             pAddons.add(cbLang, CC.xy(3, 3));
 
             //---- decompAll ----
             decompAll.setVisible(devversion);
             decompAll.setText("Decompile all files");
             decompAll.setEnabled(false);
             pAddons.add(decompAll, CC.xywh(1, 5, 3, 1));
 
             //---- cbNotOdex ----
             cbNotOdex.setVisible(devversion);
             cbNotOdex.setText("Don't deodex firmware");
             cbNotOdex.setEnabled(false);
             pAddons.add(cbNotOdex, CC.xywh(1, 7, 3, 1));
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
                     public int getSize() {
                         return values.length;
                     }
 
                     @Override
                     public Object getElementAt(int i) {
                         return values[i];
                     }
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
                     "default, 2*($lcgap), default:grow, $glue, $lcgap, default",
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
             pCmdButtons.add(btnDeCompile, CC.xy(1, 1));
 
             //---- oneClick ----
             oneClick.setText("1ClickBuild\u2122");
             oneClick.setEnabled(false);
             oneClick.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     oneClickActionPerformed(e);
                 }
             });
             pCmdButtons.add(oneClick, CC.xywh(4, 1, 2, 1));
 
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
         setLocationRelativeTo(null);
         // JFormDesigner - End of component initialization  //GEN-END:initComponents
     }
 
     public static void main(String[] args) {
         //<editor-fold desc="Init mainFrm">
         if (isOSX()) {
             System.setProperty("apple.laf.useScreenMenuBar", "true");
         }
         try {
             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         } catch (Exception e) {
             StringWriter sw = new StringWriter();
             PrintWriter pw = new PrintWriter(sw);
             e.printStackTrace(pw);
             LOGGER.info(sw.toString());
             JOptionPane.showMessageDialog(null, "<html><table width=300>" + sw.toString());
         }
         mainForm mainFrm = new mainForm();
         mainFrm.setDefaultCloseOperation(EXIT_ON_CLOSE);
         mainFrm.setVisible(mainFrm.initMFB(args));
         //</editor-fold>
     }
 
     private void setupLogging(Verbosity verbosity) {
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
                         SimpleDateFormat formatter = new SimpleDateFormat("H:mm");
                         FileWriter sw;
                         if (!logRecord.getMessage().contains("warning")) {
                             sw = new FileWriter(System.getProperty("user.dir") + File.separatorChar + "Logging.txt", true);
                             sw.write("[" + formatter.format(now) + "] " + logRecord.getMessage() + System.getProperty("line.separator"));
                             sw.close();
                         }
                     } catch (Exception e) {
                         StringWriter sw = new StringWriter();
                         PrintWriter pw = new PrintWriter(sw);
                         e.printStackTrace(pw);
                         LOGGER.info(sw.toString());
                         JOptionPane.showMessageDialog(null, "<html><table width=300>" + sw.toString());
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
     private JCheckBox cbNotOdex;
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
     private JButton oneClick;
     private JButton btnBuild;
     // JFormDesigner - End of variables declaration  //GEN-END:variables
     private String workDir;
     private String aAppsDir = "";
     private String binDir = "";
     private String projectName = null;
     private String titleProjName = null;
     private static boolean cmd = false;
     private String[] langs = {"ru", "uk", "en"};
     private String[] regions = {"RU", "UK", "US"};
     private List<String> timeZones = new ArrayList<String>();
     private int repos_count = 4;
     private List<String> repos_names = new ArrayList<String>(Arrays.asList("Russian translation for MIUI based on Android 4.x (KDGDev)", "Ukrainian translation for MIUI based on Android 4.x (KDGDev)", "Russian translation for MIUI based on Android 4.x (malchik-solnce)", "Russian translation for MIUI based on Android 4.x (BurgerZ)"));
     private List<String> repos_git = new ArrayList<String>(Arrays.asList("KDGDev/miui-v4-russian-translation-for-miuiandroid", "KDGDev/miui-v4-ukrainian-translation-for-miuiandroid", "malchik-solnce/miui-v4-ms", "BurgerZ/MIUI-v4-Translation"));
     private List<String> repos_lang = new ArrayList<String>(Arrays.asList("Russian", "Ukrainian", "Russian", "Russian"));
     //private List<String> repos_branches = new ArrayList<String>(Arrays.asList("master", "master"));
     private String repo_Precompiled = "KDGDev/jmfb2-precompiled";
     private String repo_Bootanimation = "KDGDev/jmfb-bootanimation";
     private String repo_Overlay = "KDGDev/jmfb-additional";
     private String repo_Patches = "KDGDev/jmfb-patches";
     private Boolean HWUpdate = false;
     private Boolean UpdateFromFolder = false;
     private Boolean writeBProp = true;
     private String Branding = "Translating Tool";
     private String otaUpdateURL = "http://ota.romz.bz/update-v4.php";
     //private Boolean isJB = false;
     //private Boolean fullLog=false;
     private Boolean devversion = false;
 }
