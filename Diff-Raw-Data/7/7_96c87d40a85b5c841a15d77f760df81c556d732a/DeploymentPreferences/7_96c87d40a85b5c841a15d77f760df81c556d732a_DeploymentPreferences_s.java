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
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.preferences.IEclipsePreferences;
 import org.eclipse.core.runtime.preferences.InstanceScope;
 import org.nuxeo.ide.common.StringUtils;
 import org.osgi.service.prefs.BackingStoreException;
 import org.osgi.service.prefs.Preferences;
 
 /**
  * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
  * 
  */
 public class DeploymentPreferences {
 
     public static IEclipsePreferences getDeploymentsNode() {
         return new InstanceScope().getNode("nuxeo.sdk.deployments");
     }
 
     protected List<Deployment> deployments;
 
     protected String defaultId;
 
     public DeploymentPreferences() {
         deployments = new ArrayList<Deployment>();
     }
 
     public void setDefault(String defaultId) {
         this.defaultId = defaultId;
     }
 
     public Deployment getDefault() {
         if (defaultId != null) {
             return getDeployment(defaultId);
         }
         return null;
     }
 
     public Deployment[] getDeployments() {
         return deployments.toArray(new Deployment[deployments.size()]);
     }
 
     public boolean isEmpty() {
         return deployments.isEmpty();
     }
 
     public int size() {
         return deployments.size();
     }
 
     public Deployment getDeployment(int index) {
         return deployments.get(index);
     }
 
     public String getDefaultId() {
         return defaultId;
     }
 
     public void addDeployment(Deployment deployment) {
         deployments.add(deployment);
     }
 
     public Deployment getDeployment(String id) {
         for (Deployment d : deployments) {
             if (d.getName().equals(id)) {
                 return d;
             }
         }
         return null;
     }
 
     public void removeDeployment(Deployment deployment) {
         deployments.remove(deployment);
     }
 
     public boolean hasDeployment(String id) {
         for (Deployment d : deployments) {
             if (d.getName().equals(id)) {
                 return true;
             }
         }
         return false;
     }
 
     public int indexOf(Deployment deployment) {
         return deployments.indexOf(deployment);
     }
 
     public synchronized void save() throws BackingStoreException {
         IEclipsePreferences prefs = getDeploymentsNode();
         for (String name : prefs.childrenNames()) {
             prefs.node(name).removeNode();
         }
         if (defaultId != null) {
             prefs.put("default", defaultId);
         } else {
             prefs.remove(defaultId);
         }
         for (Deployment deployment : deployments) {
             Preferences node = prefs.node(deployment.getName());
             List<String> projects = deployment.getProjectNames();
             Set<String> libs = deployment.getLibraryPaths();
             if (projects == null || projects.isEmpty()) {
                 node.remove("projects");
             } else {
                 node.put("projects", StringUtils.join(projects, ','));
             }
             if (libs == null || libs.isEmpty()) {
                 node.remove("libs");
             } else {
                 node.put("libs", StringUtils.join(libs, ','));
             }
         }
         prefs.flush();
         prefs.sync();
     }
 
     public synchronized static DeploymentPreferences load()
             throws BackingStoreException {
         DeploymentPreferences result = new DeploymentPreferences();
         IEclipsePreferences prefs = getDeploymentsNode();
         result.setDefault(prefs.get("default", null));
 
         for (String name : prefs.childrenNames()) {
             Deployment deployment = new Deployment(name);
             Preferences node = prefs.node(name);
             String projects = node.get("projects", null);
             if (projects != null) {
                 IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
                 for (String path : StringUtils.splitAsList(projects, ',')) {
                    deployment.projects.add(root.getProject(path));
                 }
             }
             String libs = node.get("libs", null);
             if (libs != null) {
                 deployment.libs.addAll(StringUtils.splitAsList(libs, ','));
             }
             result.addDeployment(deployment);
         }
 
         return result;
     }
 
 }
