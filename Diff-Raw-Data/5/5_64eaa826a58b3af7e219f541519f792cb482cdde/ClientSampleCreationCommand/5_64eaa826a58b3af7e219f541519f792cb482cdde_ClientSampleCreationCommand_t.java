 /******************************************************************************* 
  * Copyright (c) 2008 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  ******************************************************************************/
 
 package org.jboss.tools.ws.creation.core.commands;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IPackageFragment;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.core.dom.AST;
 import org.eclipse.jdt.core.dom.ASTParser;
 import org.eclipse.jdt.core.dom.CompilationUnit;
 import org.eclipse.jdt.core.dom.MethodDeclaration;
 import org.eclipse.jdt.core.dom.NormalAnnotation;
 import org.eclipse.jdt.core.dom.TypeDeclaration;
 import org.eclipse.wst.common.frameworks.datamodel.AbstractDataModelOperation;
 import org.jboss.tools.ws.core.utils.StatusUtils;
 import org.jboss.tools.ws.creation.core.JBossWSCreationCore;
 import org.jboss.tools.ws.creation.core.data.ServiceModel;
 import org.jboss.tools.ws.creation.core.messages.JBossWSCreationCoreMessages;
 import org.jboss.tools.ws.creation.core.utils.JBossWSCreationUtils;
 
 /**
  * @author Grid Qian
  * 
  * create a sample class to call web service according to wsdl
  */
 public class ClientSampleCreationCommand extends AbstractDataModelOperation {
 
 	public static final String LINE_SEPARATOR = System
 			.getProperty("line.separator");
 	private static final String PACAKAGE = ".*";
 	private static final String PACAKAGESPLIT = "\\.";
 	private static final String SRC = "src";
 
 	private ServiceModel model;
 
 	public ClientSampleCreationCommand(ServiceModel model) {
 		this.model = model;
 	}
 
 	@Override
 	public IStatus execute(IProgressMonitor monitor, IAdaptable info)
 			throws ExecutionException {
 		IStatus status = Status.OK_STATUS;
 		IJavaProject project = null;
 		try {
 			project = JBossWSCreationUtils.getJavaProjectByName(model
 					.getWebProjectName());
 		} catch (JavaModelException e) {
 			JBossWSCreationCore.getDefault().logError(e);
 			return StatusUtils.errorStatus(JBossWSCreationCoreMessages.Error_Create_Client_Sample);
 		}
 
 		// find web service client classes
 		List<ICompilationUnit> clientUnits = findJavaUnitsByAnnotation(project,
 				JBossWSCreationCoreMessages.WebserviceClient_Annotation);
 
 		// find web service classes
 		List<ICompilationUnit> serviceUnits = findJavaUnitsByAnnotation(
 				project, JBossWSCreationCoreMessages.Webservice_Annotation_Check);
 
 		// create a client sample class
 		ICompilationUnit clientCls = createJavaClass(model.getCustomPackage()
 				+ JBossWSCreationCoreMessages.Client_Sample_Package_Name,
 				JBossWSCreationCoreMessages.Client_Sample_Class_Name, false, null,
 				project);
 		if(clientCls == null){
 			return StatusUtils.errorStatus(JBossWSCreationCoreMessages.Error_Create_Client_Sample);
 		}
 
 		// add imports to client sample class
 		try {
 			clientCls.createImport(model.getCustomPackage() + PACAKAGE, null,
 					null);
 			clientCls.save(null, true);
 		} catch (Exception e1) {
 			JBossWSCreationCore.getDefault().logError(e1);
 			return StatusUtils.errorStatus(JBossWSCreationCoreMessages.Error_Create_Client_Sample);
 		}
 
 		// create main method
 		IType clientClsType = clientCls.findPrimaryType();
 
 		StringBuffer sb = new StringBuffer();
 		sb.append("public static void main(String[] args) {");
 		sb.append(LINE_SEPARATOR);
 		sb.append("        System.out.println(\"***********************\");");
 		sb.append(LINE_SEPARATOR);
 		createWebServiceClient(clientUnits, serviceUnits, sb);
 		sb.append("        System.out.println(\"***********************\");");
 		sb.append(LINE_SEPARATOR);
		sb.append("        System.out.println(\"").append(JBossWSCreationCoreMessages.Client_Sample_Run_Over).append("\");");
 		sb.append(LINE_SEPARATOR);
 		sb.append("}");
 		try {
 			clientClsType.createMethod(sb.toString(), null, true, null);
 			clientCls.save(null, true);
 		} catch (JavaModelException e) {
 			JBossWSCreationCore.getDefault().logError(e);
 			return StatusUtils.errorStatus(JBossWSCreationCoreMessages.Error_Create_Client_Sample);
 		}
 
 		return status;
 	}
 
 	/**
 	 * create a code block used to new a web service from a method of web
 	 * service client
 	 * 
 	 * @param serviceUnits
 	 * @param method
 	 * @param sb
 	 * @param i
 	 */
 	@SuppressWarnings("unchecked")
 	private void createWebService(List<ICompilationUnit> serviceUnits,MethodDeclaration method, StringBuffer sb,int i) {
 		sb.append("        System.out.println(\""
 				+ "Create Web Service...\");");
 		sb.append(LINE_SEPARATOR);
 		sb.append("        "+method.getReturnType2().toString());
 		sb.append(" port").append(i).append(" = ");
 		sb.append("service").append(i).append(".");
 		sb.append(method.getName()).append("();");
 		sb.append(LINE_SEPARATOR);
 		
 		for (ICompilationUnit unit : serviceUnits) {
 			// parse the unit
 			ASTParser parser = ASTParser.newParser(AST.JLS3);
 			parser.setSource(unit);
 			parser.setResolveBindings(false);
 			parser.setFocalPosition(0);
 			CompilationUnit result = (CompilationUnit) parser.createAST(null);
 			List types = result.types();
 			TypeDeclaration typeDec1 = (TypeDeclaration) types.get(0);
 		    if(typeDec1.getName().toString().equals(method.getReturnType2().toString())){
 		    	callWebServiceOperation(typeDec1,sb,i);
 		    }		
 		}
 	}
 
 	/**
 	 * create a code block to call web service operation
 	 * 
 	 * @param typeDec
 	 * @param sb
 	 * @param i
 	 */
 	private void callWebServiceOperation(TypeDeclaration typeDec,
 			StringBuffer sb, int i) {
 		sb.append("        System.out.println(\""
				+ "Call Web Service Operation...\");");
 		sb.append(LINE_SEPARATOR);
 		
 		MethodDeclaration methodDec[] = typeDec.getMethods();
 
 		// call web serivce Operation
 		for (MethodDeclaration method : methodDec) {
 			sb.append("        System.out.println(\"Server said: \" + ");
 			sb.append("port").append(i).append(".");
 			sb.append(method.getName()).append("(");
 			
             for(int j=0;j<method.parameters().size();j++){
             	sb.append("args[").append(j).append("]");
             	if(j!=method.parameters().size()-1){
             		sb.append(",");
             	}
             }
             sb.append("));");
             sb.append(LINE_SEPARATOR);
 		}
 	}
 
 	/**
 	 * create a code block used to new a web service client
 	 * 
 	 * @param clientUnits
 	 * @param serviceUnits
 	 * @param sb
 	 */
 	@SuppressWarnings("unchecked")
 	private void createWebServiceClient(List<ICompilationUnit> clientUnits,
 			List<ICompilationUnit> serviceUnits, StringBuffer sb) {
 		int i = 1;
 		sb.append("        System.out.println(\""
 				+ "Create Web Service Client...\");");
 		sb.append(LINE_SEPARATOR);
 		for (ICompilationUnit unit : clientUnits) {
 			// parse the unit
 			ASTParser parser = ASTParser.newParser(AST.JLS3);
 			parser.setSource(unit);
 			parser.setResolveBindings(false);
 			parser.setFocalPosition(0);
 			CompilationUnit result = (CompilationUnit) parser.createAST(null);
 			List types = result.types();
 			TypeDeclaration typeDec = (TypeDeclaration) types.get(0);
 
 			sb.append("        " + typeDec.getName());
 			sb.append(" service").append(i).append(" = new ");
 			sb.append(typeDec.getName());
 			sb.append("();");
 			sb.append(LINE_SEPARATOR);
 
 			MethodDeclaration methodDec[] = typeDec.getMethods();
 
 			// create web service from web serivce client methods
 			for (MethodDeclaration method : methodDec) {
 				if (method.modifiers().get(0) instanceof NormalAnnotation) {
 					NormalAnnotation anno = (NormalAnnotation) method
 							.modifiers().get(0);
 					if (anno.getTypeName().getFullyQualifiedName().equals(
 							JBossWSCreationCoreMessages.WebEndpoint)) {
 						createWebService(serviceUnits, method, sb, i);
 					}
 				}
 			}
 
 		}
 	}
 
 	/**
 	 * find compilationunit by annotation
 	 * 
 	 * @param project
 	 * @param annotation
 	 * @return
 	 */
 	private List<ICompilationUnit> findJavaUnitsByAnnotation(
 			IJavaProject project, String annotation) {
 		List<ICompilationUnit> units = new LinkedList<ICompilationUnit>();
 		try {
 			ICompilationUnit[] javaFiles = project.findPackageFragment(
 					addPackagetoPath(project)).getCompilationUnits();
 			for (ICompilationUnit unit : javaFiles) {
 				if (unit.getSource().contains(annotation)) {
 					units.add(unit);
 				}
 			}
 		} catch (JavaModelException e) {
 			JBossWSCreationCore.getDefault().logError(e);
 		}
 		return units;
 	}
 
 	/**
 	 * new a path by adding a java package
 	 * 
 	 * @param project
 	 * @return
 	 */
 	private IPath addPackagetoPath(IJavaProject project) {
 		String packagename = model.getCustomPackage();
 		String[] names = packagename.split(PACAKAGESPLIT);
 		IPath path = project.getPath().append(SRC);
 		if (names != null && names.length > 0) {
 			for (String name : names) {
 				path = path.append(name);
 			}
 		}
 		return path;
 	}
 
 	/**
 	 * create a java class
 	 * 
 	 * @param packageName
 	 * @param className
 	 * @param isInterface
 	 * @param interfaceName
 	 * @param javaProject
 	 * @return
 	 */
 	public ICompilationUnit createJavaClass(String packageName,
 			String className, boolean isInterface, String interfaceName,
 			IJavaProject javaProject) {
 		try {
 			IPath srcPath = javaProject.getProject().getFolder(SRC)
 					.getFullPath();
 			IPackageFragmentRoot root = javaProject
 					.findPackageFragmentRoot(srcPath);
 			if (packageName == null) {
 				packageName = "";
 			}
 			IPackageFragment pkg = root.createPackageFragment(packageName,
 					false, null);
 			ICompilationUnit wrapperCls = pkg.createCompilationUnit(className
 					+ ".java", "", true, null);
 			if (!packageName.equals("")) {
 				wrapperCls.createPackageDeclaration(packageName, null);
 			}
 
 			String clsContent = "";
 			if (isInterface) {
 				clsContent = "public interface " + className + " {"
 						+ LINE_SEPARATOR;
 				clsContent += "}" + LINE_SEPARATOR;
 			} else {
 				clsContent = "public class " + className;
 				if (interfaceName != null) {
 					clsContent += " implements " + interfaceName;
 				}
 				clsContent += " {" + LINE_SEPARATOR;
 				clsContent += "}" + LINE_SEPARATOR;
 			}
 			wrapperCls.createType(clsContent, null, true, null);
 
 			wrapperCls.save(null, true);
 			return wrapperCls;
 		} catch (Exception e) {
 			JBossWSCreationCore.getDefault().logError(e);
 			return null;
 		}
 	}
 }
