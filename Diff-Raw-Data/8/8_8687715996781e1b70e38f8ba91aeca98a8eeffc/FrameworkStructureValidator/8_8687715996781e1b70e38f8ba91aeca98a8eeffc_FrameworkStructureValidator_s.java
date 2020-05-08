 /**
  * 
  */
 package com.sap.prd.mobile.ios.mios;
 
 /*
  * #%L
  * Xcode Maven Plugin
  * %%
  * Copyright (C) 2012 SAP AG
  * %%
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * #L%
  */
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.io.FilenameUtils;
 
 /**
  * 
  *
  */
 public class FrameworkStructureValidator
 {
 
   private final File fmwkDir;
   private List<String> errorMsgs;
 
   public FrameworkStructureValidator(File fmwkDir)
   {
     this.fmwkDir = fmwkDir;
   }
 
   /**
    * 
    * @return a non <code>null</code> list containing the validation errors. The list is empty if the
    *         framework has a valid structure.
    */
   public List<String> validate()
   {
     errorMsgs = new ArrayList<String>();
     if (!fmwkDir.isDirectory()) {
       errorMsgs.add("'" + fmwkDir.getAbsolutePath() + "' is not a directory");
     }
     else {
       validateSubdirExistence("Versions", "Versions/A", "Versions/A/Headers", "Versions/A/Resources");
       String fmwkName = FilenameUtils.removeExtension(fmwkDir.getName());
       validateLib("Versions/A/" + fmwkName);
       validateLink("Headers", "Versions/A/Headers");
       validateLink("Resources", "Versions/A/Resources");
       validateLink(fmwkName, "Versions/A/" + fmwkName);
       validateLink("Versions/Current", "Versions/A");
     }
     return errorMsgs;
   }
 
   private void validateSubdirExistence(String... subDirNames)
   {
     for (String subDirName : subDirNames) {
       File dir = new File(fmwkDir, subDirName);
       if (!dir.isDirectory()) {
         errorMsgs.add("Missing the required subdirectory '" + subDirName + "'");
       }
     }
   }
 
   private void validateLib(String relativeLibPath)
   {
     File libFile = new File(fmwkDir, relativeLibPath);
     if (!libFile.exists()) {
       errorMsgs.add("Missing the required library file '" + relativeLibPath + "'");
     }
     else {
       try {
         if (!new FatLibAnalyzer(libFile).containsI386()) {
          errorMsgs.add("'" + libFile.getAbsolutePath() + "' does not contain i386 architecture.");
         }
       }
       catch (IOException e) {
         errorMsgs.add("Error ocurred during validation of the library '" + libFile.getAbsolutePath() + "' for i386");
       }
     }
   }
 
   private void validateLink(String from, String to)
   {
     File fromLink = new File(fmwkDir, from);
     File toFile = new File(fmwkDir, to);
     try {
       if (!FileUtils.isSymbolicLink(fromLink)) {
         errorMsgs.add("Expected inside the framework a symbolic link from '" + from + "' to '" + to + "'");
       }
       else if (!fromLink.getCanonicalPath().equals(toFile.getCanonicalPath())) {
         errorMsgs.add("The link '" + from + "' does not point to '" + to + "'");
       }
     }
     catch (IOException ioe) {
       errorMsgs.add("Could not validate the symbolic link from '" + from + "' to '" + to + "': " + ioe.getMessage());
     }
 
   }
 
 }
