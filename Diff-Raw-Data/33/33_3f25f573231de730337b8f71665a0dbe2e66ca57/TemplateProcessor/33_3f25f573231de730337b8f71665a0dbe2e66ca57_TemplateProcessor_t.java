 /*******************************************************************************
 * Copyright (c) 2013 Salinda Jayawardana. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 ******************************************************************************/
 package org.eclipse.ecf.tools.serviceGenerator.processors;
 
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IPackageFragment;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jdt.core.dom.CompilationUnit;
 
 
 public class TemplateProcessor {
 	private IPackageFragmentRoot root;
 	private IJavaProject javaProject;
 	
 	public TemplateProcessor(IJavaProject javaProject){
 		
 		 this.javaProject = javaProject;
 		 IFolder srcFolder = javaProject.getProject().getFolder("src");
 		 root = javaProject.getPackageFragmentRoot(srcFolder);
 	}
 	
 	public void generateAstTemplate(CompilationUnit unit,String packgeName,String newClazz) throws CoreException {
 		IPackageFragment sourcePackage = root.createPackageFragment(packgeName, false, null);
 		
 		try{
 		    sourcePackage.createCompilationUnit(newClazz+".java",unit.toString() , false, null);
 		}catch(Exception e){
			  ICompilationUnit compilationUnit = sourcePackage.getCompilationUnit(newClazz+".java");
 			  compilationUnit.delete(false, new NullProgressMonitor());
 			  javaProject.getProject().refreshLocal(IResource.DEPTH_INFINITE,new NullProgressMonitor());
 			  sourcePackage.createCompilationUnit(newClazz+".java",unit.toString() , false, null);
 		}
 		 
 		javaProject.getProject().refreshLocal(IResource.DEPTH_INFINITE,new NullProgressMonitor());
 	}
 
 }
