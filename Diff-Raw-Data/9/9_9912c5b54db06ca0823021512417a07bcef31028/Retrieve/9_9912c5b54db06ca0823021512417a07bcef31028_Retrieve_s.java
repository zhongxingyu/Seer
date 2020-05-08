 /* ***** BEGIN LICENSE BLOCK *****
  * Version: MPL 1.1
  *
  * The contents of this file are subject to the Mozilla Public License Version
  * 1.1 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  * The Original Code is Gmodel.
  *
  * The Initial Developer of the Original Code is
  * Sofismo AG (Sofismo).
  * Portions created by the Initial Developer are
  * Copyright (C) 2009-2010 Sofismo AG.
  * All Rights Reserved.
  *
  * Contributor(s):
  * Jorn Bettin
  * Chul Kim
  * ***** END LICENSE BLOCK ***** */
 
 package org.s23m.cell.eclipse.visualization.containmenttree.viewer.commands;
 
 import java.util.UUID;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.swt.custom.BusyIndicator;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Tree;
 import org.s23m.cell.G;
 import org.s23m.cell.SemanticStateOfInMemoryModel;
 import org.s23m.cell.api.models.Root;
 import org.s23m.cell.api.serializerinterface.Reconstitution;
 import org.s23m.cell.eclipse.visualization.containmenttree.viewer.ContainmentTreeManager;
 import org.s23m.cell.eclipse.visualization.containmenttree.viewer.ContainmentTreeViewerStatus;
 import org.s23m.cell.eclipse.visualization.containmenttree.viewer.Viewer;
 import org.s23m.cell.repository.client.RepositoryClient;
 import org.s23m.cell.repository.client.mediator.RepositoryClientMediator;
 import org.s23m.cell.semanticextensions.outershells.SemanticExtensions;
 import org.s23m.cell.serialization.container.ArtefactContainer;
 import org.s23m.cell.serialization.container.ArtefactContainer.Content;
 import org.s23m.cell.serialization.container.ContentType;
 import org.s23m.cell.serialization.container.ObjectFactoryHolder;
 import org.s23m.cell.serialization.serializer.ArtifactContainerContentMapper;
 import org.s23m.cell.serialization.serializer.InstanceMap;
 import org.s23m.cell.serialization.serializer.ProtocolType;
 import org.s23m.cell.serialization.serializer.SerializationType;
 
 public final class Retrieve extends AbstractHandler {
 
 	public static final String COMMAND_ID = "org.s23m.cell.eclipse.visualization.containmenttree.viewer.commands.retrieve";
 
 	public Object execute(final ExecutionEvent event) throws ExecutionException {
 	    final Display display = Display.getCurrent();
 	    final UUID selectedNodeURR = (UUID) ((Event)event.getTrigger()).data;
 
 	  	BusyIndicator.showWhile(display, new Runnable() {
 			public void run() {
 	            try {
 	            	if (!ContainmentTreeViewerStatus.getInstance().isKernelInitialized()) {
 						initialFulldeserialization();
 					}
 	            } catch (final Throwable th) {
 	            	Logger.getLogger("global").log(Level.SEVERE, null, th); //$NON-NLS-1$
 	            }
 	        }
 
 			private void initialFulldeserialization() throws IllegalArgumentException, IllegalAccessException {
 				//bootstrappingWithRepository();
 				bootstrappingByScripts();
 
 				display.asyncExec(new Runnable() {
 		        public void run() {
 		                final Tree tree = Viewer.getContainmentTreeViewer().getTree();
 						if (tree.isDisposed()) {
 							return;
 						}
 		                tree.setCursor(null);
 		                tree.setEnabled(true);
 		                Viewer.getContainmentTreeViewer().setInput(ContainmentTreeManager.getTreeManager());
 		                Viewer.selectNode(selectedNodeURR);
 		        	}
 		      });
 			}
 
 			private void bootstrappingWithRepository() {
 				org.s23m.cell.G.completeOpenSourceKernelInitialization();
 				final RepositoryClient client = RepositoryClientMediator.getInstance().getComponent(ProtocolType.REPOSITORY_CLIENT);
 				final ArtefactContainer retrievalArtifact = ObjectFactoryHolder.getInstance().createArtefactContainer();
 				retrievalArtifact.setContentType(SerializationType.CONTAINMENT_TREE.toString());
 				final ContentType uuidContent = ObjectFactoryHolder.getInstance().createArtefactContainerContent();
 				final ContentType depthContent = ObjectFactoryHolder.getInstance().createArtefactContainerContent();
 				uuidContent.setContent(""+Root.root.identity().uniqueRepresentationReference());
 				depthContent.setContent(""+0);
 				retrievalArtifact.getContent().add((Content) uuidContent);
 				retrievalArtifact.getContent().add((Content) depthContent);
 				final ArtefactContainer returnedArtifacts = client.get(retrievalArtifact);
 				if (!returnedArtifacts.getContent().isEmpty()) {
 					new ArtifactContainerContentMapper().recreateInstancesFromArtifactContainer(returnedArtifacts);
 				}
 				G.goLiveWithGmodelEditor();
 				ContainmentTreeViewerStatus.getInstance().setKernelInitialized(true);
 			}
 
 			private void bootstrappingByScripts() {
				org.s23m.cell.G.boot();
				org.s23m.cell.kernel.artifactinstantiation.RunInstantiationSequence.run();
 				SemanticExtensions.instantiateFeature();
 				G.goLiveWithGmodelEditor();
 			}
 
 			private void retrievalInitialization() throws IllegalArgumentException, IllegalAccessException {
 				if (!SemanticStateOfInMemoryModel.gmodelEditorIsLive()) {
 					org.s23m.cell.semanticextensions.G.bootTemplate();
					org.s23m.cell.kernel.artifactinstantiation.RunInstantiationSequence.run();
 					InstanceMap.getInstance();
 					G.goLiveWithGmodelEditor();
 				}
 			}
 
 			private void fetchCoordInstances() throws IllegalArgumentException, IllegalAccessException {
 				final InstanceHandler deSz = InstanceHandler.getInstance();
 				deSz.doCoordinateInstanceDeserialization();
 			}
 
 			private void fullInstanceDeserialization() throws IllegalAccessException {
 				if (!SemanticStateOfInMemoryModel.gmodelSemanticDomainIsInitialized()) {
 					Reconstitution.completeGmodelSemanticDomainInitialization();
 				}
 				final InstanceHandler deSz = InstanceHandler.getInstance();
 				deSz.doInitialFullDeserialization();
 			}
 	    });
 	    return null;
 	}
 
 }
