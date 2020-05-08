 package com.alta189.maven;
 
 import org.apache.maven.model.Plugin;
 import org.apache.maven.project.MavenProject;
 import org.codehaus.plexus.util.xml.Xpp3Dom;
 
 import java.io.File;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class WarPlugin {
 	private final MinifyMojo mojo;
 	List<File> outputFiles;
 
 	public WarPlugin(MinifyMojo mojo, List<File> outputFiles) {
 		this.mojo = mojo;
 		this.outputFiles = outputFiles;
 	}
 
 	public void run() {
 		MavenProject project = mojo.getProject();
 		Plugin plugin = project.getPlugin("org.apache.maven.plugins:maven-war-plugin");
 		Xpp3Dom config = CastUtil.safeCast(plugin.getConfiguration());
 
 		// Get existing web resources
 		Xpp3Dom webResources = config.getChild("webResources");
 		if (webResources == null) {
 			webResources = new Xpp3Dom("webResources");
 		}
 
 		// Remove old webResources
 		int i = 0;
 		for (Xpp3Dom child : config.getChildren()) {
 			if (child.getName().equals(webResources.getName())) {
 				config.removeChild(i);
 			}
 		}
 
 		// Build webResources
 		for (Xpp3Dom child : getResources()) {
 			webResources.addChild(child);
 		}
 
 		// add webResources back to the config
 		config.addChild(webResources);
 	}
 
 	public Collection<Xpp3Dom> getResources() {
 		Map<String, Xpp3Dom> result = new HashMap<String, Xpp3Dom>();
 		for (File file : outputFiles) {
 			Xpp3Dom resource = result.get(file.getParentFile().getAbsolutePath());
 
 			if (resource == null) {
 				resource = new Xpp3Dom("resource");
 
 				Xpp3Dom directory = new Xpp3Dom("directory");
 				directory.setValue(file.getParentFile().getAbsolutePath());
 
 				Xpp3Dom targetDir = new Xpp3Dom("targetDir");
 				targetDir.setValue(getTargetDir(file).getAbsolutePath());
 
 				Xpp3Dom filtering = new Xpp3Dom("filtering");
 				filtering.setValue("false");
 
 				Xpp3Dom includes = new Xpp3Dom("includes");
 
 				// Build resource
 				resource.addChild(targetDir);
 				resource.addChild(directory);
 				resource.addChild(filtering);
 				resource.addChild(includes);
 			}
 
 			Xpp3Dom includes = resource.getChild("includes");
 
 			Xpp3Dom include = new Xpp3Dom("include");
 			include.setValue(file.getName());
 
 			includes.addChild(include);
 		}
 
 		return result.values();
 	}
 
 	private File getTargetDir(File file) {
 		String path = file.getAbsolutePath().substring(mojo.getWebappSourceDir().getAbsolutePath().length());
 		if (path.startsWith("\\") || path.startsWith("/")) {
 			path = path.substring(1);
 		}
 		return new File(mojo.getWebappTargetDir(), path).getParentFile();
 	}
 }
