 /*
  * Copyright 2013, Bruce Mitchener, Jr.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.dylanfoundry.deft.module;
 
 import com.intellij.execution.ExecutionException;
 import com.intellij.execution.process.ProcessOutput;
 import com.intellij.openapi.projectRoots.*;
 import com.intellij.openapi.util.SystemInfo;
 import com.intellij.util.xmlb.XmlSerializer;
 import org.dylanfoundry.deft.DeftIcons;
 import org.dylanfoundry.deft.util.DeftSystemUtil;
 import org.jdom.Element;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 
 import javax.swing.*;
 import java.io.File;
 
 public class DeftSdkType extends SdkType {
   public DeftSdkType() {
     super("dylan");
   }
 
   @Nullable
   @Override
   public String suggestHomePath() {
     if (SystemInfo.isWindows) {
      if (SystemInfo.is64Bit) {
         return "C:\\Program Files (x86)\\Open Dylan";
       } else {
         return "C:\\Program Files\\Open Dylan";
       }
     } else if (SystemInfo.isUnix) {
       return "/opt/opendylan-2013.1";
     }
     return null;
   }
 
   @Override
   public boolean isValidSdkHome(String sdkPath) {
     File dylanCompiler = getCompilerExecutable(sdkPath);
     return dylanCompiler.canExecute();
   }
 
   private File getCompilerExecutable(@NotNull final String sdkPath) {
    if (SystemInfo.isWindows) {
       return getExecutable(sdkPath + File.separator + "bin", "dylan-compiler-with-tools");
     } else {
       return getExecutable(sdkPath + File.separator + "bin", "dylan-compiler");
     }
   }
 
   @NotNull
   private File getExecutable(@NotNull final String path, @NotNull final String command) {
     return new File(path, SystemInfo.isWindows ? command + ".exe" : command);
   }
 
   @Override
   public String suggestSdkName(String s, String s2) {
     return "Open Dylan SDK";
   }
 
   @Nullable
   @Override
   public AdditionalDataConfigurable createAdditionalDataConfigurable(SdkModel sdkModel, SdkModificator sdkModificator) {
     return null;
   }
 
   @Override
   public String getPresentableName() {
     return "Open Dylan SDK";
   }
 
   @Override
   public SdkAdditionalData loadAdditionalData(Element additional) {
     return XmlSerializer.deserialize(additional, DeftSdkData.class);
   }
 
   @Override
   public Icon getIcon() {
     return DeftIcons.DEFT;
   }
 
   @Override
   public Icon getIconForAddAction() {
     return getIcon();
   }
 
   @Override
   public void saveAdditionalData(SdkAdditionalData additionalData, Element additional) {
     if (additionalData instanceof DeftSdkData) {
       XmlSerializer.serializeInto(additionalData, additional);
     }
   }
 
   public static DeftSdkType getInstance() {
     return SdkType.findInstance(DeftSdkType.class);
   }
 
   @Nullable
   @Override
   public String getVersionString(@NotNull final String sdkPath) {
     final String exePath = getCompilerExecutable(sdkPath).getAbsolutePath();
     final ProcessOutput processOutput;
     try {
         processOutput = DeftSystemUtil.getProcessOutput(sdkPath, exePath, "-shortversion");
     } catch (final ExecutionException e) {
         return null;
     }
     if (processOutput.getExitCode() != 0) {
         return null;
     }
     final String stdout = processOutput.getStdout().trim();
     if (stdout.isEmpty()) {
         return null;
     }
     return stdout;
   }
 }
