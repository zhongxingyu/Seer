 package com.echologic.xfiles;
 
 import com.intellij.openapi.components.ProjectComponent;
 import com.intellij.openapi.diagnostic.Logger;
 import com.intellij.openapi.project.Project;
 import com.intellij.openapi.project.ProjectManager;
 import com.intellij.openapi.roots.ProjectRootManager;
 import com.intellij.openapi.roots.ProjectFileIndex;
 import com.intellij.openapi.roots.ModuleRootManager;
 import com.intellij.openapi.vfs.VirtualFile;
 import com.intellij.openapi.module.ModuleManager;
 import com.intellij.openapi.module.Module;
 import com.intellij.openapi.vcs.VcsManager;
 import com.intellij.openapi.vcs.AbstractVcs;
 
 /**
  * @author <a href="mailto:derek@echologic.com">Derek Scherger</a>
  */
 public class XFiles implements ProjectComponent {
 
 	private static final Logger log = Logger.getInstance(XFiles.class.getName());
 
 	private Project project;
 	private String name;
 
 	public XFiles(Project project) {
 	        this.project = project;
		this.name = project.getName();
 		log.debug(name + " constructed");
 		logStuff();
 	}
 
 	public String getComponentName() {
 		return "XFiles";
 	}
 
 	public void initComponent() {
 		log.debug(name + " initComponent");
 		logStuff();
 	}
 
 	public void disposeComponent() {
 		log.debug(name + " disposeComponent");
 		logStuff();
 	}
 
 	public void projectOpened() {
 		log.debug(name + " projectOpened");
 		logStuff();
 	}
 
 	public void projectClosed() {
 		log.debug(name + " projectClosed");
 		logStuff();
 	}
 
 	private void logStuff() {
 		ProjectManager projectManager = ProjectManager.getInstance();
 		log.debug(name + " project manager is " + projectManager.getClass());
 
 		//ProjectManagerListener
 
 		ProjectRootManager projectRootManager = ProjectRootManager.getInstance(project);
 
 		//ProjectRootManager projectRootManager = (ProjectRootManager) project.getComponent(ProjectRootManager.class);
 		VirtualFile[] roots = projectRootManager.getContentRoots();
 		for (int i = 0; i < roots.length; i++) {
 		    VirtualFile root = roots[i];
 		    log.debug(name + " root " + root.getPath());
 		}
 
 		VirtualFile[] sourceRoots = projectRootManager.getContentSourceRoots();
 		for (int i = 0; i < sourceRoots.length; i++) {
 		    VirtualFile sourceRoot = sourceRoots[i];
 		    log.debug(name + " source root " + sourceRoot.getPath());
 		}
 
 		// various index methods that look interesting and possibly relevant
 		ProjectFileIndex index = projectRootManager.getFileIndex();
 		//index.isIgnored(file)
 
 		ModuleManager moduleManager = ModuleManager.getInstance(project);
 		Module[] modules = moduleManager.getModules();
 
 		log.debug(name + " has " + modules.length + " modules");
 		for (int i = 0; i < modules.length; i++) {
 		    Module module = modules[i];
 		    log.debug(name + " module " + i + " is " + module.getName() + " path " + module.getModuleFilePath());
 		    ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(module);
 		    //ModuleFileIndex moduleIndex = moduleRootManager.
 		    //index.isIgnored();
 		    //index.isInContent();
 		    //index.isInSource();;
 		}
 
 		VcsManager vcsManager = VcsManager.getInstance(project);
 		AbstractVcs vcs = vcsManager.getActiveVcs();
 		log.debug(name + " active vcs " + vcs);
 
 		if (vcs != null) {
 			log.debug(name + " active vcs " + vcs.getDisplayName() + " " + vcs.getName());
 		}
 	}
 }
