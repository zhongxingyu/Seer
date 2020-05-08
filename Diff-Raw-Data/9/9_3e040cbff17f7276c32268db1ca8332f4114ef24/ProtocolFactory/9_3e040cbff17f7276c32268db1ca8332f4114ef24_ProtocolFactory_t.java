 /*******************************************************************************
  * Copyright (c) 2010, 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.mylyn.docs.intent.retro;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceVisitor;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
 
 public class ProtocolFactory implements Resource.Factory {
 
 	public Resource createResource(URI uri) {
 		String projectName = parse(uri);
 		if (projectName != null) {
 			IWorkspace wksps = ResourcesPlugin.getWorkspace();
 			if (wksps != null) {
 				IProject prj = wksps.getRoot().getProject(projectName);
 				if (prj != null) {
 					Resource result = new ResourceImpl(uri);
 					Project rPrj = RetroFactory.eINSTANCE.createProject();
 					rPrj.setId(projectName);
 					try {
 						fillProjectWithTests(rPrj, prj);
 					} catch (CoreException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					result.getContents().add(rPrj);
 					addListeners(prj);
 					return result;
 				}
 			}
 
 		}
 		return null;
 	}
 
 	private void addListeners(IProject prj) {
 		if (RetroGeneratedElementListener.getInstance() != null) {
 			RetroGeneratedElementListener.getInstance().addElementToListen(
 					URI.createURI("retro:/" + prj.getName()));
 		}
 
 	}
 
 	private void fillProjectWithTests(final Project rPrj, IProject prj) throws CoreException {
 		prj.accept(new IResourceVisitor() {
 
 			public boolean visit(IResource resource) throws CoreException {
 				if ("java".equals(resource.getFileExtension())
 						&& resource.getFullPath().toString().contains("acceptance/comparedialog/patch")) {
 					// TODO really parse Java and retrieve U-tests
 					AcceptanceTest tst = RetroFactory.eINSTANCE.createAcceptanceTest();
					tst.setSwtBotClassName(resource.getFullPath().lastSegment().replace(".java", ""));
					tst.setPackage(resource.getFullPath().removeFirstSegments(2).removeFirstSegments(5)
							.removeLastSegments(1).toString().replaceAll("/", "."));
 					rPrj.getAcceptanceTests().add(tst);
 
 				}
 				return true;
 			}
 
 		});
 
 	}
 
 	private String parse(URI uri) {
 		// expected URIS are : retro:/myProject
 		if (uri.segmentCount() == 1) {
 			return uri.segment(0);
 		}
 		return null;
 	}
 
 	class ParsedURI {
 
 	}
 
 }
