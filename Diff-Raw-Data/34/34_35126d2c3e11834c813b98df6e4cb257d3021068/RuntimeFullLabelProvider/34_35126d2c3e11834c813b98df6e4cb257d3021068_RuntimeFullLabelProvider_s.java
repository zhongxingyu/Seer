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
 import org.eclipse.virgo.ide.runtime.core.artefacts.ArtefactSet;
 import org.eclipse.virgo.ide.runtime.core.artefacts.IArtefact;
 import org.eclipse.virgo.ide.runtime.core.artefacts.LocalArtefactSet;
 import org.eclipse.virgo.ide.runtime.internal.ui.editor.Messages;
 import org.eclipse.virgo.ide.runtime.internal.ui.projects.IServerProjectArtefact;
 import org.eclipse.virgo.ide.runtime.internal.ui.projects.IServerProjectContainer;
 
 /**
  * @author Miles Parker
  * @author Christian Dupuis
  */
 public class RuntimeFullLabelProvider extends RuntimeLabelProvider {
 
 	@Override
 	public String getText(Object element) {
 		if (element instanceof IServerProjectArtefact) {
 			return getText(((IServerProjectArtefact) element).getContainer()) + " - "
 					+ getText(((IServerProjectArtefact) element).getArtefact());
 		}
 		if (element instanceof IServerProjectContainer) {
 			return getText(((IServerProjectContainer) element).getServer()) + " - "
 					+ getText(((IServerProjectContainer) element).getArtefactSet());
 		}
 		if (element instanceof LibrariesNode) {
 			return getText(((LibrariesNode) element).getServer()) + " "
					+ Messages.RepositoryBrowserEditorPage_InstalledBundlesAndLibraries;
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
