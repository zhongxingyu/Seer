 /*
  * (C) Copyright 2006-2008 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 package org.nuxeo.ide.webengine.server;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.ui.IMemento;
 
 /**
  * For now only one configuration is supported
  * 
  * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
  *
  */
 public class Configuration {
 
     public static HashMap<String, Configuration> configs = new HashMap<String, Configuration>();
 
     public String id = "default";
     public File launcher;    
     public List<IProject> projects = new ArrayList<IProject>();
     
     private Configuration() {
         
     }
     
     public Configuration(String id) {
         this.id = id;
     }
     
     public boolean isValid() {
         String name = launcher.getName();
        return launcher != null && name.endsWith(".jar") && name.startsWith("nuxeo-runtime-launcher"); 
     }
     
     public File getHome() {
         return launcher.getParentFile();
     }
     
     public static void add(Configuration config) {
         configs.put(config.id, config);
     }
     
     public static Configuration get(String id) {
         return configs.get(id);
     }
     
     public static void init(IMemento memento) {
         IMemento[] children = memento.getChildren("configuration");
         if (children != null && children.length > 0) {
             for (IMemento child : children) {
                 Configuration cfg = loadConfig(child);
                 configs.put(cfg.id, cfg);
             }
         }
         if (configs.isEmpty()) {
             Configuration config = new Configuration("default");
             configs.put(config.id, config);
         }
     }
     
     
     public static void store(IMemento memento) {
         for (Configuration config : configs.values()) {
             IMemento child = memento.createChild("configuration", config.id);
             storeConfig(child, config);
         }        
     }
     
     protected static void storeConfig(IMemento memento, Configuration config) {
         memento.putString("home", config.launcher == null ? null : config.launcher.getAbsolutePath());
         for (IProject project : config.projects) {
             if (project.exists()) {
                 memento.createChild("projects", project.getName());
             }
         }
     }
     
     protected static Configuration loadConfig(IMemento memento) {
         Configuration config = new Configuration();
         String h = memento.getString("home");
         config.launcher = h == null ? null : new File(h);
         loadPlugins(memento.getChildren("projects"), config);
         return config;
     }
     
     protected static void loadPlugins(IMemento[] mementos, Configuration config) {
         IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
         if (mementos == null) return;
         for (IMemento memento : mementos) {
             String id = memento.getID();
             IProject prj = root.getProject(id);
             if (prj.exists() && prj.isOpen()) {
                 config.projects.add(prj);
             }
         }
     }
 }
