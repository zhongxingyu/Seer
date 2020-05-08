 package org.jenkinsci.plugins.sharedobjects.service;
 
 import hudson.model.Hudson;
 import hudson.util.XStream2;
 import org.jenkinsci.plugins.sharedobjects.SharedObjectException;
 import org.jenkinsci.plugins.sharedobjects.SharedObjectManagement;
 import org.jenkinsci.plugins.sharedobjects.SharedObjectType;
 
 import java.io.*;
 
 /**
  * @author Gregory Boissinot
  */
 public class SharedObjectDataStore {
 
     public void writeSharedObjectsFile(SharedObjectType[] types) throws SharedObjectException {
         XStream2 xStream2 = new XStream2();
        File sharedObjectsFile = new File(Hudson.getInstance().getRootDir(), "sharedObjects.xml");
        sharedObjectsFile.mkdirs();
         FileWriter fileWriter = null;
         try {
             fileWriter = new FileWriter(sharedObjectsFile, false);
             xStream2.toXML(new SharedObjectManagement(types), fileWriter);
         } catch (IOException e) {
             throw new SharedObjectException(e);
         } finally {
             if (fileWriter != null) {
                 try {
                     fileWriter.close();
                 } catch (IOException e) {
                     throw new SharedObjectException(e);
                 }
             }
         }
     }
 
     public SharedObjectType[] readSharedObjectsFile() throws SharedObjectException {
         XStream2 xStream2 = new XStream2();
         File sharedObjectsFile = new File(Hudson.getInstance().getRootDir(), "sharedObjects.xml");
         if (sharedObjectsFile.exists()) {
             FileReader fileReader = null;
             try {
                 fileReader = new FileReader(sharedObjectsFile);
                 if (sharedObjectsFile.exists()) {
                     SharedObjectManagement sharedObjectsManagement = (SharedObjectManagement) xStream2.fromXML(fileReader);
                     return sharedObjectsManagement.getTypes();
                 }
             } catch (FileNotFoundException e) {
                 throw new SharedObjectException(e);
             } catch (IOException e) {
                 throw new SharedObjectException(e);
             } finally {
                 if (fileReader != null) {
                     try {
                         fileReader.close();
                     } catch (IOException e) {
                         throw new SharedObjectException(e);
                     }
                 }
 
             }
         }
         return new SharedObjectType[0];
     }
 
 }
