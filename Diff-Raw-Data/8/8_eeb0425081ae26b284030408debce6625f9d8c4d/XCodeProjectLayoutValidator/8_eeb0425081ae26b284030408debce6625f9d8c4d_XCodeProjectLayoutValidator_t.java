 /*
  * #%L
  * xcode-maven-plugin
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
 package com.sap.prd.mobile.ios.mios;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.util.Arrays;
 import java.util.Collection;
 
 class XCodeProjectLayoutValidator
 {
 
   /**
    * Checks if a folder is a valid xcode folder. A folder is considered to be a valid xcode folder
    * if
    * <ul>
    * <li>it does exist
    * <li>it contains a folder named "&lt;projectName&gt;.xcodeproj"
    * <li>in the folder(s) mentioned before we have a file named project.pbxproj
    * 
    * @throws XCodeException
    *           is the folder is not a valid xcode project folder
    */
   static void verifyXcodeFolder(final File xcodeFolder, final String projectName) throws XCodeValidationException
   {
 
     if (!xcodeFolder.exists()) {
       throw new XCodeRootFolderDoesNotExistException("The folder \"" + xcodeFolder
             + "\" that is the root folder of the xcode project \"" + projectName + "\" does not exist.");
     }
 
     final String xcodeProjectFolderName = projectName + XCodeConstants.XCODE_PROJECT_EXTENTION;
     
     final Collection<File> xcodeProjectFolders = Arrays.asList(xcodeFolder.listFiles(new FileFilter() {
 
       @Override
       public boolean accept(File f)
       {
         return f.isDirectory() && f.getName().equals(xcodeProjectFolderName);
       }
     }));
 
     if (xcodeProjectFolders.size() == 0)
     {
       throw new XCodeProjectNotFoundException(
            "The folder \""
                   + xcodeFolder
                  + "\" must contain folder named \""
                   + xcodeProjectFolderName
                  + "\". This folder is expected to hold the xcode project configuration. The xcode project must have the same name as the maven artifactId");
     }
     
     for (final File xcodeProjectFolder : xcodeProjectFolders)
 
       if (xcodeProjectFolder.listFiles(new FileFilter() {
 
         @Override
         public boolean accept(File f)
         {
           return f.getName().equals(XCodeConstants.XCODE_CONFIGURATION_FILE_NAME);
         }
       }).length != 1)
         throw new XCodeProjectFileDoesNotExistException("Folder \"" + xcodeProjectFolders + "\" is expected to contain the file \""
               + XCodeConstants.XCODE_CONFIGURATION_FILE_NAME + "\" but that file does not exist.");
   }
 
 }
