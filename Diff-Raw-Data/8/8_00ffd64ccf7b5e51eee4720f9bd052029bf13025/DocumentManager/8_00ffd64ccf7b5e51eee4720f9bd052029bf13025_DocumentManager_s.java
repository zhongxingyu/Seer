 /**
  * Copyright 2013 markiewb
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package de.markiew.netbeans.plugin.actions.closedocuments;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.LinkedHashSet;
 import org.netbeans.api.project.FileOwnerQuery;
 import org.netbeans.api.project.Project;
 import org.openide.loaders.DataObject;
 import org.openide.filesystems.FileObject;
 import org.openide.windows.Mode;
 import org.openide.windows.TopComponent;
 import org.openide.windows.WindowManager;
 
 public final class DocumentManager {
 
     public static DocumentManager getInstance() {
         return new DocumentManager();
     }
 
     private DocumentManager() {
     }
 
     /**
      *
      * @param projectToMatch
      * @param onlyMatching if true, it returns only {@link TopComponent}s which
      * are from the same project; if false, it returns only {@link TopComponent}s
      * which are not from the given project
      * @return
      */
     public Collection<TopComponent> getDocumentsForProject(Project projectToMatch, boolean onlyMatching) {
         final WindowManager wm = WindowManager.getDefault();
         final LinkedHashSet<TopComponent> result = new LinkedHashSet<TopComponent>();
         for (TopComponent tc : getCurrentEditors()) {
             DataObject dob = tc.getLookup().lookup(DataObject.class);
             if (dob != null && wm.isEditorTopComponent(tc)) {
                 FileObject primaryFile = dob.getPrimaryFile();
                 Project owner = FileOwnerQuery.getOwner(primaryFile);
                 if (null != owner) {
                     final boolean sameProject = projectToMatch.equals(owner);
                     if ((onlyMatching && sameProject) || (!onlyMatching && !sameProject)) {
                         result.add(tc);
                     }
                 }
             }
         }
         return result;
     }
 
     private Collection<TopComponent> getCurrentEditors() {
         final ArrayList<TopComponent> result = new ArrayList<TopComponent>();
         final WindowManager wm = WindowManager.getDefault();
         for (Mode mode : wm.getModes()) {
             if (wm.isEditorMode(mode)) {
                 result.addAll(Arrays.asList(mode.getTopComponents()));
             }
         }
         return result;
     }
 
 }
