 /* ===========================================================================
  *  Copyright (c) 2007 Serena Software. All rights reserved.
  *
  *  Use of the Sample Code provided by Serena is governed by the following
  *  terms and conditions. By using the Sample Code, you agree to be bound by
  *  the terms contained herein. If you do not agree to the terms herein, do
  *  not install, copy, or use the Sample Code.
  *
  *  1.  GRANT OF LICENSE.  Subject to the terms and conditions herein, you
  *  shall have the nonexclusive, nontransferable right to use the Sample Code
  *  for the sole purpose of developing applications for use solely with the
  *  Serena software product(s) that you have licensed separately from Serena.
  *  Such applications shall be for your internal use only.  You further agree
  *  that you will not: (a) sell, market, or distribute any copies of the
  *  Sample Code or any derivatives or components thereof; (b) use the Sample
  *  Code or any derivatives thereof for any commercial purpose; or (c) assign
  *  or transfer rights to the Sample Code or any derivatives thereof.
  *
  *  2.  DISCLAIMER OF WARRANTIES.  TO THE MAXIMUM EXTENT PERMITTED BY
  *  APPLICABLE LAW, SERENA PROVIDES THE SAMPLE CODE AS IS AND WITH ALL
  *  FAULTS, AND HEREBY DISCLAIMS ALL WARRANTIES AND CONDITIONS, EITHER
  *  EXPRESSED, IMPLIED OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY
  *  IMPLIED WARRANTIES OR CONDITIONS OF MERCHANTABILITY, OF FITNESS FOR A
  *  PARTICULAR PURPOSE, OF LACK OF VIRUSES, OF RESULTS, AND OF LACK OF
  *  NEGLIGENCE OR LACK OF WORKMANLIKE EFFORT, CONDITION OF TITLE, QUIET
  *  ENJOYMENT, OR NON-INFRINGEMENT.  THE ENTIRE RISK AS TO THE QUALITY OF
  *  OR ARISING OUT OF USE OR PERFORMANCE OF THE SAMPLE CODE, IF ANY,
  *  REMAINS WITH YOU.
  *
  *  3.  EXCLUSION OF DAMAGES.  TO THE MAXIMUM EXTENT PERMITTED BY APPLICABLE
  *  LAW, YOU AGREE THAT IN CONSIDERATION FOR RECEIVING THE SAMPLE CODE AT NO
  *  CHARGE TO YOU, SERENA SHALL NOT BE LIABLE FOR ANY DAMAGES WHATSOEVER,
  *  INCLUDING BUT NOT LIMITED TO DIRECT, SPECIAL, INCIDENTAL, INDIRECT, OR
  *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, DAMAGES FOR LOSS OF
  *  PROFITS OR CONFIDENTIAL OR OTHER INFORMATION, FOR BUSINESS INTERRUPTION,
  *  FOR PERSONAL INJURY, FOR LOSS OF PRIVACY, FOR NEGLIGENCE, AND FOR ANY
  *  OTHER LOSS WHATSOEVER) ARISING OUT OF OR IN ANY WAY RELATED TO THE USE
  *  OF OR INABILITY TO USE THE SAMPLE CODE, EVEN IN THE EVENT OF THE FAULT,
  *  TORT (INCLUDING NEGLIGENCE), STRICT LIABILITY, OR BREACH OF CONTRACT,
  *  EVEN IF SERENA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.  THE
  *  FOREGOING LIMITATIONS, EXCLUSIONS AND DISCLAIMERS SHALL APPLY TO THE
  *  MAXIMUM EXTENT PERMITTED BY APPLICABLE LAW.  NOTWITHSTANDING THE ABOVE,
  *  IN NO EVENT SHALL SERENA'S LIABILITY UNDER THIS AGREEMENT OR WITH RESPECT
  *  TO YOUR USE OF THE SAMPLE CODE AND DERIVATIVES THEREOF EXCEED US$10.00.
  *
  *  4.  INDEMNIFICATION. You hereby agree to defend, indemnify and hold
  *  harmless Serena from and against any and all liability, loss or claim
  *  arising from this agreement or from (i) your license of, use of or
  *  reliance upon the Sample Code or any related documentation or materials,
  *  or (ii) your development, use or reliance upon any application or
  *  derivative work created from the Sample Code.
  *
  *  5.  TERMINATION OF THE LICENSE.  This agreement and the underlying
  *  license granted hereby shall terminate if and when your license to the
  *  applicable Serena software product terminates or if you breach any terms
  *  and conditions of this agreement.
  *
  *  6.  CONFIDENTIALITY.  The Sample Code and all information relating to the
  *  Sample Code (collectively "Confidential Information") are the
  *  confidential information of Serena.  You agree to maintain the
  *  Confidential Information in strict confidence for Serena.  You agree not
  *  to disclose or duplicate, nor allow to be disclosed or duplicated, any
  *  Confidential Information, in whole or in part, except as permitted in
  *  this Agreement.  You shall take all reasonable steps necessary to ensure
  *  that the Confidential Information is not made available or disclosed by
  *  you or by your employees to any other person, firm, or corporation.  You
  *  agree that all authorized persons having access to the Confidential
  *  Information shall observe and perform under this nondisclosure covenant.
  *  You agree to immediately notify Serena of any unauthorized access to or
  *  possession of the Confidential Information.
  *
  *  7.  AFFILIATES.  Serena as used herein shall refer to Serena Software,
  *  Inc. and its affiliates.  An entity shall be considered to be an
  *  affiliate of Serena if it is an entity that controls, is controlled by,
  *  or is under common control with Serena.
  *
  *  8.  GENERAL.  Title and full ownership rights to the Sample Code,
  *  including any derivative works shall remain with Serena.  If a court of
  *  competent jurisdiction holds any provision of this agreement illegal or
  *  otherwise unenforceable, that provision shall be severed and the
  *  remainder of the agreement shall remain in full force and effect.
  * ===========================================================================
  */
 
 package com.urbancode.ds.jenkins.plugins.serenarapublisher;
 
 import hudson.model.BuildListener;
 import hudson.remoting.Callable;
 
 import java.io.File;
 import java.net.URI;
 import java.net.URLDecoder;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.ws.rs.core.UriBuilder;
 
 import org.codehaus.jettison.json.JSONArray;
 import org.codehaus.jettison.json.JSONObject;
 
 import com.urbancode.commons.fileutils.filelister.FileListerBuilder;
 import com.urbancode.vfs.client.Client;
 import com.urbancode.vfs.common.ClientChangeSet;
 import com.urbancode.vfs.common.ClientPathEntry;
 
 public class PublishArtifactsCallable implements Callable<Boolean, Exception> {
 
     //**********************************************************************************************
     // CLASS
     //**********************************************************************************************
     private static final long serialVersionUID = 34598734957L;
 
     //**********************************************************************************************
     // INSTANCE
     //**********************************************************************************************
     final private String resolvedBaseDir;
     final private String resolvedDirectoryOffset;
     final private UrbanDeploySite udSite;
     final private String resolvedFileIncludePatterns;
     final private String resolvedFileExcludePatterns;
     final private String resolvedComponent;
     final private String resolvedVersionName;
     final private BuildListener listener;
     
     public PublishArtifactsCallable(String resolvedBaseDir, String resolvedDirectoryOffset, UrbanDeploySite udSite,
             String resolvedFileIncludePatterns, String resolvedFileExcludePatterns, String resolvedComponent,
             String resolvedVersionName, BuildListener listener) {
         this.resolvedBaseDir = resolvedBaseDir;
         this.resolvedDirectoryOffset = resolvedDirectoryOffset;
         this.udSite = udSite;
         this.resolvedFileIncludePatterns = resolvedFileIncludePatterns;
         this.resolvedFileExcludePatterns = resolvedFileExcludePatterns;
         this.resolvedComponent = resolvedComponent;
         this.resolvedVersionName = resolvedVersionName;
         this.listener = listener;
     }
     
     
     @Override
     public Boolean call() throws Exception {
         File workDir = new File(resolvedBaseDir);
         if (!workDir.exists()) throw new Exception("Base artifact directory " + workDir.toString()
                 + " does not exist!");
         if (resolvedDirectoryOffset != null && resolvedDirectoryOffset.trim().length() > 0) {
             workDir = new File(workDir, resolvedDirectoryOffset.trim());
         }
 
         Set<String> includesSet = new HashSet<String>();
         Set<String> excludesSet = new HashSet<String>();
         for (String pattern : resolvedFileIncludePatterns.split("\n")) {
             if (pattern != null && pattern.trim().length() > 0) {
                 includesSet.add(pattern.trim());
             }
         }
         if (resolvedFileExcludePatterns != null) {
             for (String pattern : resolvedFileExcludePatterns.split("\n")) {
                 if (pattern != null && pattern.trim().length() > 0) {
                     excludesSet.add(pattern.trim());
                 }
             }
         }
 
         String[] includesArray = new String[includesSet.size()];
         includesArray = (String[]) includesSet.toArray(includesArray);
 
         String[] excludesArray = new String[excludesSet.size()];
         excludesArray = (String[]) excludesSet.toArray(excludesArray);
 
 
         listener.getLogger().println("Connecting to " + udSite.getUrl());
         createComponentVersion(udSite, resolvedComponent, resolvedVersionName, listener);
         listener.getLogger().println("Working Directory: " + workDir.getPath());
         listener.getLogger().println("Includes: " + resolvedFileIncludePatterns);
         listener.getLogger().println("Excludes: " + (resolvedFileExcludePatterns == null ? "" : resolvedFileExcludePatterns));
 
         Client client = null;
         String stageId = null;
         try {
             ClientPathEntry[] entries = ClientPathEntry
                     .createPathEntriesFromFileSystem(workDir, includesArray, excludesArray,
                             FileListerBuilder.Directories.INCLUDE_ALL, FileListerBuilder.Permissions.BEST_EFFORT,
                             FileListerBuilder.Symlinks.AS_LINK, "SHA-256");
     
             listener.getLogger().println("Invoke vfs client...");
             client = new Client(udSite.getUrl() + "/vfs", null, null);
             stageId = client.createStagingDirectory();
             listener.getLogger().println("Created staging directory: " + stageId);
     
             if (entries.length > 0) {
     
                 for (ClientPathEntry entry : entries) {
                     File entryFile = new File(workDir, entry.getPath());
                     listener.getLogger().println("Adding " + entry.getPath() + " to staging directory...");
                     client.addFileToStagingDirectory(stageId, entry.getPath(), entryFile);
                 }
     
                 String repositoryId = getComponentRepositoryId(udSite, resolvedComponent);
                 ClientChangeSet changeSet =
                         ClientChangeSet.newChangeSet(repositoryId, udSite.getUser(), "Uploaded by Jenkins", entries);
     
                 listener.getLogger().println("Committing change set...");
                 String changeSetId = client.commitStagingDirectory(stageId, changeSet);
                 listener.getLogger().println("Created change set: " + changeSetId);
     
                 listener.getLogger().println("Labeling change set with label: " + resolvedVersionName);
                 client.labelChangeSet(repositoryId, URLDecoder.decode(changeSetId, "UTF-8"), resolvedVersionName,
                 udSite.getUser(), "Associated with version " + resolvedVersionName);
                 listener.getLogger().println("Done labeling change set!");
             }
             else {
                 listener.getLogger().println("Did not find any files to upload!");
             }
         }
         catch (Throwable e) {
             throw new Exception("Failed to upload files", e);
         }
         finally {
             if (client != null && stageId != null) {
                 try {
                     client.deleteStagingDirectory(stageId);
                     listener.getLogger().println("Deleted staging directory: " + stageId);
                 }
                 catch (Exception e) {
                     listener.getLogger()
                             .println("Failed to delete staging directory " + stageId + ": " + e.getMessage());
                 }
             }
         }
         
         return true;
     }
 
     private String getComponentRepositoryId(UrbanDeploySite site, String componentName)
             throws Exception {
         String result = null;
         URI uri = UriBuilder.fromPath(site.getUrl()).path("rest").path("deploy").path("component").path(componentName)
                 .build();
 
         String componentContent = site.executeJSONGet(uri);
 
         JSONArray properties = new JSONObject(componentContent).getJSONArray("properties");
         if (properties != null) {
             for (int i = 0; i < properties.length(); i++) {
                 JSONObject propertyJson = properties.getJSONObject(i);
                 String propName = propertyJson.getString("name");
                 String propValue = propertyJson.getString("value");
 
                 if ("code_station/repository".equalsIgnoreCase(propName)) {
                     result = propValue.trim();
                     break;
                 }
             }
         }
         return result;
     }
 
     private void createComponentVersion(UrbanDeploySite site, String componentName,
             String versionName, BuildListener listener)
     throws Exception {
         UriBuilder uriBuilder = UriBuilder.fromPath(site.getUrl()).path("cli").path("version")
                         .path("createVersion");
         
         uriBuilder.queryParam("component", componentName);
         uriBuilder.queryParam("name", versionName);
         URI uri = uriBuilder.build();
        
         listener.getLogger().println("Creating new version \""+versionName+
                 "\" on component "+componentName+"...");
         site.executeJSONPost(uri);
         listener.getLogger().println("Successfully created new component version.");
     }
 }
