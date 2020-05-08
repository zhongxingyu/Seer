 package org.eclipse.emf.refactor.refactoring.henshin.managers;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.jar.Attributes;
 import java.util.jar.Manifest;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.emf.refactor.refactoring.generator.core.RefactoringInfo;
 import org.eclipse.emf.refactor.refactoring.generator.managers.DependenciesManager;
 
 public class HenshinDependenciesManager extends DependenciesManager {
 
 	private final static String REFACTORHENSHIN = "org.eclipse.emf.refactor.refactoring.henshin";
	private final static String HENSHINMODEL = "org.eclipse.emf.henshin.model";
 	
 	public static void updateDependencies(RefactoringInfo info) {
 		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(info.getProjectName());
 		try {			
 			String fileName = project.getLocation().append(MANIFEST_FILE)
 								.toOSString();
 			FileInputStream is = new FileInputStream(fileName);
 			Manifest mf = new Manifest(is);
 			Attributes att = mf.getMainAttributes();
 			String value = att.getValue(REQUIREBUNDLE);
 			if (! value.contains(EXPRESSIONS)) 
 				value = value + "," +  EXPRESSIONS;
 			if (! value.contains(ECORE))
 				value = value + "," +  ECORE;
 			if (! value.contains(ECORECHANGE))
 				value = value + "," +  ECORECHANGE;
 			if (! value.contains(LTKCORE))
 				value = value + "," +  LTKCORE;
 			if (! value.contains(LTKUI))
 				value = value + "," +  LTKUI;
 			if (! value.contains(REFACTORCOMMON)) 
 				value = value + "," +  REFACTORCOMMON;
 			if (! value.contains(REFACTORRUNTIME)) 
 				value = value + "," +  REFACTORRUNTIME;
 			if (! value.contains(REFACTORHENSHIN))
 				value = value + "," +  REFACTORHENSHIN;
			if (! value.contains(HENSHINMODEL))
				value = value + "," +  HENSHINMODEL;
 			if (! value.contains(JUNIT)) 
 				value = value + "," +  JUNIT + ";bundle-version=\"4.8.1\"";
 			if (! value.contains(info.getJar()))
 				value = value + "," +  info.getJar();
 			att.putValue(REQUIREBUNDLE, value);
 			FileOutputStream out = new FileOutputStream(fileName);  
 			mf.write(out); 
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	
 }
