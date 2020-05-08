 /*******************************************************************************
  * Copyright (c) 2009, 2012 SpringSource, a divison of VMware, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     SpringSource, a division of VMware, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.virgo.ide.runtime.internal.ui.providers;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.jdt.internal.ui.JavaPlugin;
 import org.eclipse.jdt.internal.ui.JavaPluginImages;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.pde.internal.ui.PDEPlugin;
 import org.eclipse.pde.internal.ui.PDEPluginImages;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.virgo.ide.runtime.core.artefacts.ArtefactSet;
 import org.eclipse.virgo.ide.runtime.core.artefacts.ArtefactType;
 import org.eclipse.virgo.ide.runtime.core.artefacts.IArtefact;
 import org.eclipse.virgo.ide.runtime.core.artefacts.IArtefactTyped;
 import org.eclipse.virgo.ide.runtime.core.artefacts.LocalArtefactSet;
 import org.eclipse.virgo.ide.runtime.core.artefacts.LocalBundleArtefact;
 import org.eclipse.virgo.ide.runtime.internal.ui.ServerUiImages;
 import org.eclipse.virgo.ide.runtime.internal.ui.editor.Messages;
 import org.eclipse.virgo.ide.runtime.internal.ui.projects.IServerProjectArtefact;
 import org.eclipse.virgo.ide.runtime.internal.ui.projects.IServerProjectContainer;
 import org.eclipse.virgo.ide.runtime.internal.ui.projects.ProjectFileReference;
 
 /**
  * @author Miles Parker
  * @author Christian Dupuis
  */
 public class RuntimeLabelProvider extends LabelProvider {
 
 	@Override
 	public Image getImage(Object element) {
 		if (element instanceof IServerProjectArtefact) {
 			return getImage(((IServerProjectArtefact) element).getArtefact());
 		}
 		if (element instanceof IServerProjectContainer) {
 			return getImage(((IServerProjectContainer) element).getArtefactSet());
 		}
 		if (element instanceof ProjectFileReference) {
 			return ServerUiImages.getImage(ServerUiImages.IMG_OBJ_VIRGO);
 		}
 		if (element instanceof ArtefactSet) {
 			ArtefactType artefactType = ((IArtefactTyped) element).getArtefactType();
 			if (artefactType == ArtefactType.BUNDLE) {
 				return PDEPlugin.getDefault().getLabelProvider().get(PDEPluginImages.DESC_JAR_LIB_OBJ);
 			} else if (artefactType == ArtefactType.LIBRARY) {
 				return ServerUiImages.getImage(ServerUiImages.IMG_OBJ_VIRGO_LIB);
 			}
 		}
 		if (element instanceof IFile) {
 			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
 		}
 		if (element instanceof IArtefactTyped) {
 			ArtefactType artefactType = ((IArtefactTyped) element).getArtefactType();
 			if (artefactType == ArtefactType.BUNDLE) {
 				if (element instanceof LocalBundleArtefact && ((LocalBundleArtefact) element).isSourceDownloaded()) {
 					return JavaPlugin.getImageDescriptorRegistry().get(JavaPluginImages.DESC_OBJS_EXTJAR_WSRC);
 				}
 				return JavaPlugin.getImageDescriptorRegistry().get(JavaPluginImages.DESC_OBJS_EXTJAR);
 			} else if (artefactType == ArtefactType.LIBRARY) {
 				return ServerUiImages.getImage(ServerUiImages.IMG_OBJ_VIRGO_FILE);
 			}
 		}
 		if (element instanceof LibrariesNode) {
 			return ServerUiImages.getImage(ServerUiImages.IMG_OBJ_LIB);
 		}
 		return super.getImage(element);
 	}
 
 	@Override
 	public String getText(Object element) {
 		if (element instanceof IServerProjectArtefact) {
 			return getText(((IServerProjectArtefact) element).getArtefact());
 		}
 		if (element instanceof IServerProjectContainer) {
 			return getText(((IServerProjectContainer) element).getArtefactSet());
 		}
 		if (element instanceof LibrariesNode) {
			return Messages.RepositoryBrowserEditorPage_InstalledBundlesAndLibraries;
 		}
 		if (element instanceof IFile) {
 			return ((IFile) element).getName();
 		}
 		if (element instanceof ArtefactSet) {
 			ArtefactSet set = (ArtefactSet) element;
 			String label = set.getArtefactType().getPluralLabel();
 			if (element instanceof LocalArtefactSet) {
 				label = ((LocalArtefactSet) element).getRelativePath() + " [" + label + "]";
 			}
 			return label;
 		} else if (element instanceof IArtefact) {
 			IArtefact version = (IArtefact) element;
 			StringBuilder l = new StringBuilder();
 			if (version.getName() != null) {
 				l.append(version.getName());
 				l.append(" - ");
 			}
 			l.append(version.getSymbolicName());
 			l.append(" (");
 			l.append(version.getVersion());
 			l.append(")");
 			return l.toString();
 		}
 		return super.getText(element);
 	}
 
 }
