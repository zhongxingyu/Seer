 /*
  * (C) Copyright 2006-2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * (LGPL) version 2.1 which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/lgpl.html
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * Contributors:
  *     bstefanescu
  */
 package org.nuxeo.ide.sdk.deploy;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.core.resources.IProject;
 import org.nuxeo.ide.sdk.userlibs.UserLib;
 
 /**
  * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
  * 
  */
 public class Deployment {
 
     protected String name;
 
     protected Set<IProject> projects;
 
     protected Set<String> libs;
 
     public Deployment(String name) {
         this.name = name;
         projects = new HashSet<IProject>();
         libs = new HashSet<String>();
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getName() {
         return name;
     }
 
     public void addProject(IProject project) {
         projects.add(project);
     }
 
     public void removeProject(IProject project) {
         projects.remove(project);
     }
 
     public IProject[] getProjects() {
         return projects.toArray(new IProject[projects.size()]);
     }
 
     public void addLibrary(UserLib lib) {
         libs.add(lib.getPath());
     }
 
     public void removeLibrary(UserLib lib) {
         libs.remove(lib.getPath());
     }
 
     public List<String> getProjectNames() {
         ArrayList<String> result = new ArrayList<String>(projects.size());
         for (IProject project : projects) {
             result.add(project.getName());
         }
         return result;
     }
 
     public Set<String> getLibraryPaths() {
         return libs;
     }
 
     public UserLib[] getLibraries() {
         UserLib[] result = new UserLib[libs.size()];
         int i = 0;
         for (String path : libs) {
             result[i++] = new UserLib(path);
         }
         return result;
     }
 
    public String getContentAsString() {
         String crlf = "\n";
         StringBuilder builder = new StringBuilder();
         builder.append("# Projects").append(crlf);
         for (IProject project : projects) {
            File file = new File(project.getLocation().toFile(), "bin/main");
            if (file.isDirectory()) {
                String path = file.getAbsolutePath();
                builder.append(path).append(crlf);
             }
         }
         builder.append(crlf);
         builder.append("# User Libraries").append(crlf);
         for (String lib : libs) {
             File file = new File(lib);
             if (file.exists()) {
                 builder.append('!').append(lib).append(crlf);
             }
         }
         return builder.toString();
     }
 
 }
