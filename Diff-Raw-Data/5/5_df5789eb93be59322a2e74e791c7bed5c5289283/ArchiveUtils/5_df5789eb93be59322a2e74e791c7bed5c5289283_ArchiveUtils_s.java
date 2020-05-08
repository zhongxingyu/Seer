 /**
  * This file is part of the XP-Framework
  *
  * Maven plugin for XP-Framework
  * Copyright (c) 2011, XP-Framework Team
  */
 package net.xp_forge.maven.plugins.xp.util;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Map;
 
 import org.apache.maven.artifact.Artifact;
 
 import org.codehaus.plexus.archiver.Archiver;
 import org.codehaus.plexus.archiver.UnArchiver;
 import org.codehaus.plexus.archiver.zip.ZipArchiver;
 import org.codehaus.plexus.archiver.zip.ZipUnArchiver;
 import org.codehaus.plexus.archiver.ArchiverException;
 
 import net.xp_forge.maven.plugins.xp.util.FileUtils;
 import net.xp_forge.maven.plugins.xp.archiver.xar.XarArchiver;
 import net.xp_forge.maven.plugins.xp.archiver.xar.XarUnArchiver;
 
 /**
  * Utility class
  *
  */
 public final class ArchiveUtils {
 
   /**
    * Utility classes should not have a public or default constructor
    *
    */
   private ArchiveUtils() {
   }
 
   /**
    * Get archiver based on specified file
    *
    * @param  java.io.File file
    * @return org.codehaus.plexus.archiver.Archiver
    */
   public static Archiver getArchiver(File file) throws IllegalArgumentException {
     String extension= file.getName().substring(file.getName().lastIndexOf('.') + 1);
 
     if (extension.equals("xar")) {
       Archiver retVal= new XarArchiver();
       retVal.setDestFile(file);
       return retVal;
     }
 
     if (extension.equals("zip")) {
       Archiver retVal= new ZipArchiver();
       retVal.setDestFile(file);
       return retVal;
     }
 
     // Invalid package type
     throw new IllegalArgumentException("Cannot get Archiver for [" + file + "]");
   }
 
   /**
    * Get unarchiver for the specified file
    *
    * @param  java.io.File file
    * @return org.codehaus.plexus.archiver.UnArchiver
    */
   public static UnArchiver getUnArchiver(File file) throws IllegalArgumentException {
     String extension= file.getName().substring(file.getName().lastIndexOf('.') + 1);
 
     if (extension.equals("xar")) {
       return new XarUnArchiver(file);
     }
 
     if (extension.equals("zip")) {
       return new ZipUnArchiver(file);
     }
 
     // Invalid extension
     throw new IllegalArgumentException("Cannot get UnArchiver for [" + file + "]");
   }
 
   /**
    * Get unarchiver for the specified Artifact
    *
    * @param  org.apache.maven.artifact.Artifact artifact
    * @return org.codehaus.plexus.archiver.UnArchiver
    */
   public static UnArchiver getUnArchiver(Artifact artifact) throws IllegalArgumentException {
     return ArchiveUtils.getUnArchiver(artifact.getFile());
   }
 
   /**
    * Dump artifact contents in the specified directory
    *
    * @param  org.apache.maven.artifact.Artifact artifact
    * @param  java.io.File destDirectory
    * @param  boolean overwrite
    * @throw  org.codehaus.plexus.archiver.ArchiverException
    */
   public static void dumpArtifact(Artifact artifact, File destDirectory, boolean overwrite) throws ArchiverException {
 
     // Create destination directory if not exists
     if (!destDirectory.exists()) {
       destDirectory.mkdirs();
     }
 
     // Dump artifact contents
     UnArchiver unarchiver= ArchiveUtils.getUnArchiver(artifact);
     unarchiver.setDestDirectory(destDirectory);
     unarchiver.setOverwrite(overwrite);
     unarchiver.extract();
   }
 
   /**
    * Copy entries from one archive to another
    *
    * @param  org.codehaus.plexus.archiver.UnArchiver src
    * @param  org.codehaus.plexus.archiver.Archiver dest
    * @param  java.util.Map<String, String> entries
    */
   public static void copyArchiveEntries(UnArchiver unArchiver, Archiver archiver, Map<String, String> entries) throws IOException {
 
     // Copy entries
     File tmpDirectory= FileUtils.getTempDirectory();
    for (String srcEntry: entries.keySet()) {
      String destEntry= entries.get(srcEntry);
 
       // Get entry name
       String entryName= srcEntry;
       if (srcEntry.contains("/")) {
         entryName= srcEntry.substring(srcEntry.lastIndexOf('/') + 1);
       }
 
       // Unpack & pack entry
       unArchiver.extract(srcEntry, tmpDirectory);
       archiver.addFile(new File(tmpDirectory, entryName), destEntry);
     }
   }
 
   /**
    * Copy entries from one archive to another
    *
    * @param  java.io.File srcFile
    * @param  org.codehaus.plexus.archiver.Archiver archiver
    * @param  java.util.Map<String, String> entries
    */
   public static void copyArchiveEntries(File srcFile, Archiver archiver, Map<String, String> entries) throws IOException {
     ArchiveUtils.copyArchiveEntries(ArchiveUtils.getUnArchiver(srcFile), archiver, entries);
   }
 
   /**
    * Copy entries from one archive to another
    *
    * @param  org.apache.maven.artifact.Artifact srcArtifact
    * @param  org.codehaus.plexus.archiver.Archiver archiver
    * @param  java.util.Map<String, String> entries
    */
   public static void copyArchiveEntries(Artifact srcArtifact, Archiver archiver, Map<String, String> entries) throws IOException {
     ArchiveUtils.copyArchiveEntries(srcArtifact.getFile(), archiver, entries);
   }
 }
