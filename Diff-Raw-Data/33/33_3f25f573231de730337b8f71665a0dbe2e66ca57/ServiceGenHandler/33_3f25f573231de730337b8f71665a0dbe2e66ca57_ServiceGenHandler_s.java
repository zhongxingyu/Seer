 /*******************************************************************************
 * Copyright (c) 2013 Salinda Jayawardana. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 ******************************************************************************/
 package org.eclipse.ecf.tools.servicegenerator.handler;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.ecf.tools.serviceGenerator.processors.AstProcessor;
 import org.eclipse.ecf.tools.serviceGenerator.processors.IResourcesProcessor;
 import org.eclipse.ecf.tools.serviceGenerator.processors.TemplateProcessor;
 import org.eclipse.ecf.tools.servicegenerator.Activator;
 import org.eclipse.ecf.tools.servicegenerator.utils.AsyncProperties;
 import org.eclipse.ecf.tools.servicegenerator.utils.Logger;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.dom.TypeDeclaration;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IActionDelegate;
 import org.eclipse.ui.PlatformUI;
  
 
 
 public class ServiceGenHandler  implements IActionDelegate{
 
 	private IStructuredSelection selection;
     private Logger log;
 	@Override
 	public void run(IAction arg0) {
 		if (selection != null) {
 			 try {
 				 clazzGen(selection);
 			} catch (Exception e) {
 			 
 			}
 		}
 		
 	}
 		  
 	@Override
 	public void selectionChanged(IAction arg0, ISelection selection) {
 		
 		if (selection instanceof IStructuredSelection) {
 			this.selection = (IStructuredSelection) selection;
 			log = new Logger(Activator.context);
 		}	
 	}
 
     /**
      * control the new template generation flow
      * @param selection-selected file by the user
      */
 	private void clazzGen(ISelection selection) {
 		try { 
           /*Getting the selected file as icompilationUnit*/
 		 ICompilationUnit icompilationUnit = getIcompilationunit(selection);
 		 /* getting java project associated with selected compilation unit, this need to create new units*/
 		 IJavaProject javaProject = IResourcesProcessor.getJavaProject(icompilationUnit);
 		 /*getting  the service type  1-Async Service and 2-sync service return 0 if this is not annotated using
 		  * @remote Service */
          int serviceType = IResourcesProcessor.getServiceType(icompilationUnit);
 		 
 		 if(serviceType!=0){
 			 /*JSL4 based processor new units generate by this processor */
 			 AstProcessor astProcessor = new AstProcessor(icompilationUnit,log);
 			 /*This is used to serialize the new compilation units created by astprocessor */
 			 TemplateProcessor templateProcessor = new TemplateProcessor(javaProject);
 			 
 			 /*getting selected icompilationUnit information*/
 			 String pacKagename =IResourcesProcessor.getPackageName(icompilationUnit);
 			 String interfaceName  = IResourcesProcessor.getInterfaceName(icompilationUnit);
 			 
 			 /*creating new names for interface, package and Impl-clazz*/
 			 String generatedInterfaceName = createNewInterfaceName(serviceType, interfaceName);
 			 String impleName = createNewImpleClazzName(generatedInterfaceName);
 			 String implPackgeName = createImplePackageName(pacKagename);
 			 
 			 /*create a new Type for new Interface*/
 				TypeDeclaration createdType = astProcessor
 						.createType(
 								generatedInterfaceName,
 								true,
 								AsyncProperties.AsyncService_Str_IAsyncRemoteServiceProxy);
 			 /*create a new Type for Impl-Clazz*/
 			 TypeDeclaration createdImpleType = astProcessor.createType(impleName, false,generatedInterfaceName);
 			 /*create a new package for implClazz*/
 			 astProcessor.cretaePackage(pacKagename,implPackgeName);
 			
 			 /*adding explicit imports */
 			 List<String> exImports = new ArrayList<String>();
 			 /*importing interface into impl-clazz*/
 			 exImports.add(pacKagename+"."+generatedInterfaceName);
 			 astProcessor.addImports(exImports,serviceType);
 			 
 			 /*adding methods signatures and methods for interface and impl-clazz*/
 			 astProcessor.addMethods(createdType,createdImpleType,serviceType);
 			 
 			 /*for a Async-service create both Async interface and imple-clazz*/
 			 if(serviceType==1){
 			 templateProcessor.generateAstTemplate(astProcessor.getNewunit(), pacKagename, generatedInterfaceName);
 			 templateProcessor.generateAstTemplate(astProcessor.getImpleunit(), implPackgeName, impleName);
 			 }else{
 				 /*for a sync-service imple-clazz Only*/
 				 templateProcessor.generateAstTemplate(astProcessor.getImpleunit(), implPackgeName, impleName);
 			 }
		 }
 		 Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		 MessageDialog.openError(shell, "Generate R-Service", "Couldn't the RemoteService annotaion in selected file");
 		 } catch (Throwable e) {
 				log.log(1, "Class generating process has faild !"+e.getMessage(), e);
 	     }
 	}
 
 	private ICompilationUnit getIcompilationunit(ISelection selection) {
 		IStructuredSelection StructuredSelection = (IStructuredSelection) selection;
 		 ICompilationUnit icompilationUnit =(ICompilationUnit) StructuredSelection.getFirstElement();
 		return icompilationUnit;
 	}
 
 	private String createImplePackageName(String pacKagename) {
 		String implPackgeName = pacKagename+".Imple";
 		return implPackgeName;
 	}
 
 	private String createNewImpleClazzName(String name) {
 		String impleName = name + "Imple";
 		return impleName;
 	}
 
 	private String createNewInterfaceName(int serviceType, String interfaceName) {
 		if(serviceType==1){
 		      return interfaceName + "Async";
 		 }else if(serviceType==2){
 			 return interfaceName;
 		 }
 		return null;
 	}
 }
