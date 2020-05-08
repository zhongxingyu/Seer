 /*******************************************************************************
  * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  * William Chen (Wind River)- [345552] Edit the remote files with a proper editor
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.filesystem.internal.adapters;
 
 import java.net.URL;
 import java.util.List;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.tcf.te.tcf.filesystem.activator.UIPlugin;
 import org.eclipse.tcf.te.tcf.filesystem.internal.handlers.StateManager;
 import org.eclipse.tcf.te.tcf.filesystem.internal.operations.FSClipboard;
 import org.eclipse.tcf.te.tcf.filesystem.model.CacheState;
 import org.eclipse.tcf.te.tcf.filesystem.model.FSTreeNode;
 import org.eclipse.ui.IActionFilter;
 
 /**
  * This action filter wraps an FSTreeNode and test its attribute of "cache.state".
  * It serves as the expression filter of decorations of Target Explorer.
  */
 public class NodeStateFilter implements IActionFilter {
 	private FSTreeNode node;
 
 	/**
 	 * Constructor.
 	 *
 	 * @param node
 	 *            The wrapped tree node. Must not be <code>null</code>.
 	 */
 	public NodeStateFilter(FSTreeNode node) {
 		Assert.isNotNull(node);
 		this.node = node;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.IActionFilter#testAttribute(java.lang.Object, java.lang.String, java.lang.String)
 	 */
 	@Override
 	public boolean testAttribute(Object target, String name, String value) {
 		if (name.equals("cache.state") && node.isFile()) { //$NON-NLS-1$
 			CacheState state = StateManager.getInstance().getCacheState(node);
 			if (value == null)
 				value = CacheState.consistent.name();
 			return value.equals(state.name());
 		}
 		else if (name.equals("edit.cut")) { //$NON-NLS-1$
 			FSClipboard cb = UIPlugin.getDefault().getClipboard();
 			if (!cb.isEmpty()) {
 				int operation = cb.getOperation();
 				if (operation == FSClipboard.CUT) {
 					URL nodeURL = node.getLocationURL();
 					List<URL> files = cb.getFiles();
 					for (URL file : files) {
 						if (nodeURL.equals(file)) return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 }
