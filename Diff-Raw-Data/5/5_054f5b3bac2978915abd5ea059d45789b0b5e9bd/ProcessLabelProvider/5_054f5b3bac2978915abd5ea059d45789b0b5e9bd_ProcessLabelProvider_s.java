 /*******************************************************************************
  * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.processes.ui.internal.columns;
 
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.tcf.te.tcf.processes.ui.activator.UIPlugin;
 import org.eclipse.tcf.te.tcf.processes.ui.controls.ProcessesTreeNode;
 import org.eclipse.tcf.te.tcf.processes.ui.interfaces.ImageConsts;
 import org.eclipse.tcf.te.ui.trees.TreeColumnLabelProvider;
 
 /**
  * The label provider for the tree column "name".
  */
 public class ProcessLabelProvider extends TreeColumnLabelProvider {
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
 	 */
 	@Override
 	public String getText(Object element) {
 		if (element instanceof ProcessesTreeNode) {
 			ProcessesTreeNode node = (ProcessesTreeNode) element;
 			String name = node.name;
 			if (name == null) name = "System"; //$NON-NLS-1$
			int slash = name.lastIndexOf("/"); //$NON-NLS-1$
			if (slash == -1) slash = name.lastIndexOf("\\"); //$NON-NLS-1$
			if (slash == -1) return name;
			return name.substring(slash + 1);
 		}
 		return super.getText(element);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
 	 */
 	@Override
 	public Image getImage(Object element) {
 		if (element instanceof ProcessesTreeNode) {
 			return UIPlugin.getImage(ImageConsts.OBJ_Process);
 		}
 		return super.getImage(element);
 	}
 }
