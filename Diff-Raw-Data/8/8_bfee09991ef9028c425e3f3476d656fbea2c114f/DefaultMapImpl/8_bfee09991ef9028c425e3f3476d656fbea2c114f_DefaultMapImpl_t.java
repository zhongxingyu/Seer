 package org.wyona.yarep.impl;
 
 import org.apache.avalon.framework.configuration.Configuration;
 
 import org.wyona.commons.io.FileUtil;
 import org.wyona.yarep.core.Map;
 import org.wyona.yarep.core.Path;
 import org.wyona.yarep.core.RepositoryException;
 import org.wyona.yarep.core.UID;
 import org.wyona.yarep.impl.VFileSystemMapImpl.ChildrenFilter;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.BufferedReader;
 import java.io.FilenameFilter;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.log4j.Category;
 
 /**
  *
  */
 public class DefaultMapImpl implements Map {
 
     private static Category log = Category.getInstance(DefaultMapImpl.class);
 
     protected File pathsDir;
     protected Pattern[] ignorePatterns;
     protected ChildrenFilter childrenFilter = new ChildrenFilter();
 
     private static String YAREP_UID_SUFFIX = "yarep-uid";
 
     /**
      *
      */
     public void readConfig(Configuration mapConfig, File repoConfigFile) throws RepositoryException {
         try {
             pathsDir = new File(mapConfig.getAttribute("src"));
             if (!pathsDir.isAbsolute()) {
                 pathsDir = FileUtil.file(repoConfigFile.getParent(), pathsDir.toString());
             }
             log.debug(pathsDir.toString());
             // TODO: Throw Exception
             if (!pathsDir.exists()) log.error("No such file or directory: " + pathsDir);
             
             Configuration[] ignoreElements = mapConfig.getChildren("ignore");
             ignorePatterns = new Pattern[ignoreElements.length + 1];
             // always ignore uid files:
             ignorePatterns[0] = Pattern.compile(".*\\." + YAREP_UID_SUFFIX);
             for (int i=0; i<ignoreElements.length; i++) {
                 String patternString = ignoreElements[i].getAttribute("pattern");
                 ignorePatterns[i+1] = Pattern.compile(patternString);
                 log.debug("adding ignore pattern: " + ignorePatterns[i+1].pattern());
             }
             
         } catch(Exception e) {
             log.error(e);
             throw new RepositoryException("Could not read map configuration: " 
                     + repoConfigFile.getAbsolutePath() + e.getMessage(), e);
         }
     }
 
     protected boolean ignorePath(String path) {
         for (int i=0; i<this.ignorePatterns.length; i++) {
             Matcher matcher = this.ignorePatterns[i].matcher(path); 
             if (matcher.matches()) {
                 if (log.isDebugEnabled()) {
                     log.debug(path + " matched ignore pattern " + ignorePatterns[i].pattern());
                 }
                 return true;
             }
         }
         if (log.isDebugEnabled()) {
             log.debug(path + " did not match any ignore patterns");
         }
         return false;
     }
 
     /**
      * Check if path is representing a resource
      */
     public boolean isResource(Path path) throws RepositoryException {
         File file = new File(pathsDir + path.toString());
         File uidFile = new File(pathsDir + path.toString() + File.separator + "." + YAREP_UID_SUFFIX);
         if(log.isDebugEnabled()) log.debug("UID File: " + uidFile);
        return (uidFile.isFile() || file.isFile()) && !isCollection(path);
     }
 
     /**
      *
      */
     public boolean exists(Path path) throws RepositoryException {
         File file = new File(pathsDir + path.toString());
         // TODO: Get name of repository for debugging ...
         //log.debug("Path (" + getName() + "): " + file);
         return file.exists() && !ignorePath(file.getPath());
     }
 
     /**
      *
      */
     public boolean delete(Path path) throws RepositoryException {
 /*
         File uidFile = new File(pathsDir + path.toString() + File.separator + "." + YAREP_UID_SUFFIX);
         if (uidFile.isFile()) uidFile.delete();
 */
         File file = new File(pathsDir + path.toString());
         log.debug("delete from map: " + file);
         return org.wyona.commons.io.FileUtil.deleteDirectory(file);
     }
 
     /**
      * Check if path is representing a collection
      */
     public boolean isCollection(Path path) throws RepositoryException {
         File file = new File(pathsDir + path.toString());
         if(log.isDebugEnabled()) log.debug("Check if path is representing a collection: " + file);
         if (file.isDirectory()) {
             File uidFile = new File(pathsDir + path.toString() + File.separator + "." + YAREP_UID_SUFFIX);
             if (uidFile.isFile()) {
                 try {
                     BufferedReader br = new BufferedReader(new FileReader(uidFile));
                     String existingUID = br.readLine();
                     String type = br.readLine();
                     br.close();
                    if (log.isDebugEnabled()) log.debug("type: " + type);
                    if (type != null && type.equals("collection")) {
                        return true;
                    }
                 } catch (Exception e) {
                     log.error(e.getMessage(), e);
                     throw new RepositoryException("Error reading uid of path: " + path.toString() + ": " + e.getMessage(), e);
                 }
                 return false;
             } else {
                 return true;
             }
         }
         return false;
     }
 
     /**
      *
      */
     public Path[] getChildren(Path path) throws RepositoryException {
         File file = new File(pathsDir + path.toString());
         String[] filenames = file.list(this.childrenFilter);
 
 	// NOTE: This situation should only occur if isResource(Path) didn't work properly!
         if (filenames == null) {
             log.warn("No children: " + path + " (" + file + ")");
             return new Path[0];
         }
 
         log.debug("Number of children: " + filenames.length + " (" + file + ")");
         Path[] children = new Path[filenames.length];
         for (int i = 0;i < children.length; i++) {
             if (path.toString().endsWith("/")) {
                 children[i] = new Path(path + filenames[i]);
             } else {
                 // NOTE: Do not use File.separator here, because it's the repository path and not the Operating System File System path
                 children[i] = new Path(path + "/" + filenames[i]);
             }
             log.debug("Child: " + children[i]);
         }
         return children;
     }
 
     /**
      * Get UID
      */
     public synchronized UID getUID(Path path) throws RepositoryException {
         File uidFile = new File(pathsDir + path.toString() + File.separator + "." + YAREP_UID_SUFFIX);
         if (log.isDebugEnabled()) log.debug(pathsDir.toString() + ", " + uidFile.toString());
         if (uidFile.exists()) {
             try {
                 FileReader fr = new FileReader(uidFile);
                 BufferedReader br = new BufferedReader(fr);
                 String existingUID = br.readLine();
                 br.close();
                 fr.close();
                 return new UID(existingUID);
             } catch (Exception e) {
                 log.error(e);
                 throw new RepositoryException("Error reading uid of path: " + path.toString() 
                         + ": " + e.getMessage(), e);
             }
         } else {
             if(log.isDebugEnabled()) log.debug("uid file [" + uidFile + "] does not exist for path: " + path);
         }
 
         return null;
     }
 
     /**
      * Get UID
      */
     public synchronized UID create(Path path, int type) throws RepositoryException {
         log.debug(pathsDir.toString());
         File uidFile = new File(pathsDir + path.toString() + File.separator + "." + YAREP_UID_SUFFIX);
         log.debug(uidFile.toString());
 
         // TODO: Shouldn't the uid be written only if the writer is being closed successfully!
 	//String uid = "" + System.currentTimeMillis();
 	String uuid = org.apache.commons.id.uuid.UUID.randomUUID().toString();
 	//String uid = java.util.UUID.randomUUID().toString(); // Java 1.5.x
         try {
             File parent = new File(uidFile.getParent());
             if (!parent.exists()) {
                 log.warn("Directory will be created: " + parent);
                 parent.mkdirs();
             }
             // TODO: ...
             if (parent.isFile()) {
                 log.warn("Parent is a file and not a directory: " + parent);
             }
             if (type == org.wyona.yarep.core.NodeType.RESOURCE) {
                 FileWriter fw = new FileWriter(uidFile);
                 fw.write(uuid);
                 fw.close();
             } else if (type == org.wyona.yarep.core.NodeType.COLLECTION) {
                 // TODO: Should a uidFile also be written in the case of a collection?!
                 FileWriter fw = new FileWriter(uidFile);
                 fw.write(uuid);
                 fw.close();
             } else {
                 log.error("Node type is neither resource nor collection!");
                 throw new RepositoryException("Node type is neither resource nor collection!");
             }
         } catch (Exception e) {
             log.error(e.getMessage(), e);
             throw new RepositoryException("Error creating uid for path: " + path.toString() 
                     + ": " + e.getMessage(), e);
         }
         return new UID(uuid);
     }
 
     /**
      *
      */
     public void addSymbolicLink(Path link, UID uid) throws RepositoryException {
         File uidFile = new File(pathsDir + link.toString() + File.separator + "." + YAREP_UID_SUFFIX);
 
         String uuid = uid.toString();
         try {
             File parent = new File(uidFile.getParent());
             if (!parent.exists()) {
                 log.warn("Directory will be created: " + parent);
                 parent.mkdirs();
             }
             // TODO: ...
             if (parent.isFile()) {
                 log.warn("Parent is a file and not a directory: " + parent);
             }
             FileWriter fw = new FileWriter(uidFile);
             fw.write(uuid);
             fw.close();
         } catch (Exception e) {
             log.error(e);
             throw new RepositoryException("Error creating uid for path: " + link.toString() 
                     + ": " + e.getMessage(), e);
         }
     }
     
     protected class ChildrenFilter implements FilenameFilter {
         public boolean accept(File dir, String name) {
             
             if (DefaultMapImpl.this.ignorePath(name)) {
                 return false;
             } else {
                 return true;
             }
         }
     }
 
 }
