 /*******************************************************************************
  * Copyright (c) 2007 IBM Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Robert Fuhrer (rfuhrer@watson.ibm.com) - initial API and implementation
 
  *******************************************************************************/
 
 package org.eclipse.imp.releng.metadata;
 
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.imp.releng.ReleaseEngineeringPlugin;
 import org.eclipse.imp.releng.ReleaseTool;
 
 public class PluginInfo {
     public static abstract class ChangeReason {
         public abstract boolean isChange();
     }
 
     private static class NoChange extends ChangeReason {
         private static final NoChange sInstance= new NoChange();
 
         public static final NoChange getInstance() {
             return sInstance;
         }
 
         private NoChange() {}
 
         @Override
         public String toString() {
             return "<unchanged>";
         }
 
         @Override
         public boolean isChange() {
             return false;
         }
     }
 
     public static class NewPluginChange extends ChangeReason {
         private static final NewPluginChange sInstance= new NewPluginChange();
 
         public static final NewPluginChange getInstance() {
             return sInstance;
         }
 
         private NewPluginChange() {}
 
         @Override
         public String toString() {
             return "<new plugin>";
         }
 
         @Override
         public boolean isChange() {
             return true;
         }
     }
 
     public abstract static class ResourceChange extends ChangeReason {
         protected final IPath fPath;
 
         public ResourceChange(IPath path) {
             fPath= path;
         }
 
         public IPath getPath() {
             return fPath;
         }
 
         @Override
         public boolean isChange() {
             return true;
         }
 
         public abstract String getType();
     }
 
     public static class FileChange extends ResourceChange {
         public FileChange(IPath path) {
             super(path);
         }
 
         @Override
         public String toString() {
             return "<file change: " + fPath.toPortableString() + ">";
         }
 
         @Override
         public String getType() {
             return "<changed>";
         }
     }
 
     public static class FileDeleted extends ResourceChange {
         public FileDeleted(IPath path) {
             super(path);
         }
 
         @Override
         public String toString() {
             return "<file deleted: " + fPath.toPortableString() + ">";
         }
 
         @Override
         public String getType() {
             return "<deleted>";
         }
     }
 
     public static class FileAdded extends ResourceChange {
         public FileAdded(IPath path) {
             super(path);
         }
 
         @Override
         public String toString() {
             return "<file added: " + fPath.toPortableString() + ">";
         }
 
         @Override
         public String getType() {
             return "<added>";
         }
     }
 
     public IFile fManifest;
 
     public String fPluginID;
 
     public String fProjectName;
 
     public String fPluginVersion;
 
     public String fPluginNewVersion;
 
     public FileVersionMap fCurMap;
 
     public FileVersionMap fNewMap;
 
     private boolean fPluginOk= true;
 
     private ChangeReason fChanged= NoChange.getInstance();
 
     private List<ChangeReason> fAllChanges= new ArrayList<ChangeReason>();
 
     private static final Pattern BUNDLE_ID_PATTERN= Pattern.compile("Bundle-SymbolicName: *([a-zA-Z0-9_]+(\\.[a-zA-Z0-9_]+)*)");
 
     public PluginInfo(String pluginID, String newVersion) {
         fPluginID= pluginID;
         fPluginVersion= newVersion;
         fProjectName= fPluginID; // Assume the normal convention; perhaps correct later
 
         IWorkspaceRoot wsRoot= ResourcesPlugin.getWorkspace().getRoot();
         IProject project= wsRoot.getProject(pluginID);
 
         if (project == null || !project.exists()) {
             project= findProjectByPluginID(wsRoot);
         }
 
         if (project != null) {
             fManifest= project.getFile(new Path("META-INF/MANIFEST.MF"));
             if (!fManifest.exists()) {
                ReleaseEngineeringPlugin.getMsgStream().println("     * Unable to find bundle manifest for plugin " + pluginID);
                 fPluginOk= false;
             }
         } else {
            ReleaseEngineeringPlugin.getMsgStream().println("     * Unable to find project for plugin " + pluginID);
             fPluginOk= false;
         }
     }
 
     /**
      * Find the plugin project whose ID is given by the value of <code>fPluginID</code>.
      * This is useful when a plugin project doesn't follow the convention of having
      * its project name be identical to its plugin ID.
      * @param wsRoot the workspace root
      * @return the plugin project, or null if not found
      */
     private IProject findProjectByPluginID(IWorkspaceRoot wsRoot) {
         IProject project= null;
         IProject[] allProjects= wsRoot.getProjects();
 
         for(int i= 0; i < allProjects.length; i++) {
             IProject aProject= allProjects[i];
             IFile manifestFile= aProject.getFile(new Path("META-INF/MANIFEST.MF"));
 
             if (!manifestFile.exists()) {
                 continue;
             }
 
             try {
                 String manifestContents= ReleaseTool.getFileContents(new InputStreamReader(manifestFile.getContents()));
                 Matcher matcher= BUNDLE_ID_PATTERN.matcher(manifestContents);
 
                 if (matcher.find()) {
                     String aPluginID= matcher.group(1);
 
                     if (aPluginID.equals(fPluginID)) {
                         project= allProjects[i];
                         fProjectName= allProjects[i].getName(); // Correct the earlier (incorrect) guess
                         break;
                     }
                 }
             } catch (CoreException e) {
                 e.printStackTrace();
             }
         }
         return project;
     }
 
     public ChangeReason getChangeState() {
         return fChanged;
     }
 
     public List<ChangeReason> getAllChanges() {
         return fAllChanges;
     }
 
     public void updateReason(ChangeReason reason) {
         fAllChanges.add(reason);
         if (fChanged instanceof NewPluginChange) {
             // do nothing; this is the most potent reason for bumping the version
         } else if (fChanged instanceof NoChange) {
             fChanged= reason;
         } else if (reason instanceof FileAdded) {
             fChanged= reason;
         } else if (reason instanceof FileChange && !(fChanged instanceof FileAdded)) {
             fChanged= reason;
         } // else the change must be a FileDeleted
     }
 
     public boolean pluginOk() {
         return fPluginOk;
     }
 
     @Override
     public String toString() {
         return fPluginID;
     }
 }
