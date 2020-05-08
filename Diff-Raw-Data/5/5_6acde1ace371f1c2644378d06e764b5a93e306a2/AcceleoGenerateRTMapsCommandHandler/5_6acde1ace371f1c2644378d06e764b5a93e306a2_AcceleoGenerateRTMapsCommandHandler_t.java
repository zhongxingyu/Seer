 /*****************************************************************************
  * Copyright (c) 2011 CEA LIST.
  *
  *    
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *  Saadia DHOUIB (CEA LIST) - Initial API and implementation
  *
  *****************************************************************************/
 package org.eclipse.papyrus.robotml.generators.intempora.rtmaps.ui.handler;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.gmf.runtime.common.core.command.ICommand;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.papyrus.infra.core.services.ServiceException;
 import org.eclipse.papyrus.infra.emf.utils.EMFHelper;
 import org.eclipse.papyrus.infra.emf.utils.ServiceUtilsForEObject;
 import org.eclipse.papyrus.robotml.generators.intempora.rtmaps.ui.Activator;
 import org.eclipse.ui.handlers.HandlerUtil;
 
 
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class AcceleoGenerateRTMapsCommandHandler.
  */
 public class AcceleoGenerateRTMapsCommandHandler extends AbstractHandler {
 
 	/** The rtmaps folder. */
	private final String rtmapsFolder = "rtmaps-generated-files";
 
 //	/**
 //	 * @see org.eclipse.papyrus.modelexplorer.handler.AbstractCommandHandler#getCommand()
 //	 * 
 //	 * @return
 //	 */
 //
 //	@Override
 //	protected Command getCommand() {
 //		List<EObject> selectedObjects = getSelectedElements();
 //		EObject selectedElement = getSelectedElement();
 //		List<?> selection = getSelection();
 //		TransactionalEditingDomain editingDomain = getEditingDomain();
 //		if ((selectedObjects != null) && (selectedObjects.size()>0)){
 //			EObject selectedObject = selectedObjects.get(0);
 //			URI targetFolderURI = selectedObject.eResource().getURI();
 //			int lastindex = targetFolderURI.toPlatformString(false).lastIndexOf("/");
 //			String targetPath = targetFolderURI.toPlatformString(false).substring(0, lastindex);
 //			return new org.eclipse.papyrus.commands.wrappers.GMFtoEMFCommandWrapper(new GenerateRTMapsCodeCommand("Generate RTMaps code command", editingDomain, selectedObject, targetPath + rtmapsFolder));
 //		}
 //			
 //		
 //return null;
 //		
 //
 //	}
 
 	public Object execute(ExecutionEvent event) throws ExecutionException {
 		ISelection selection = HandlerUtil.getCurrentSelection(event);
 		
 		if (selection instanceof IStructuredSelection){
 			IStructuredSelection structuredSelection = (IStructuredSelection)selection;
 			EObject selectedElement = EMFHelper.getEObject(structuredSelection.getFirstElement());
 			if (selectedElement == null){
 				return null;
 			}
 			
 			URI resourceURI = selectedElement.eResource().getURI();
 			int lastSegment = resourceURI.segmentCount()-1;
			URI targetFolderURI = resourceURI.trimSegments(1).appendSegment(rtmapsFolder);
 			
 			try {
 				TransactionalEditingDomain editingDomain = ServiceUtilsForEObject.getInstance().getTransactionalEditingDomain(selectedElement);
 				
 				ICommand generationCommand = new GenerateRTMapsCodeCommand("Generate RTMaps code command", editingDomain, selectedElement, targetFolderURI.toPlatformString(true));
 				
 				if (generationCommand.canExecute()){
 					generationCommand.execute(new NullProgressMonitor(), null);
 				}
 			} catch (ServiceException ex){
 				Activator.log.error(ex);
 			}
 		}
 		
 		return null;
 	}
 
 }
