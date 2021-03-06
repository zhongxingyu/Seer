 /*******************************************************************************
  * Copyright (c) 2000, 2006 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.team.internal.ccvs.ui.subscriber;
 
 import org.eclipse.compare.structuremergeviewer.IDiffElement;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.team.core.synchronize.FastSyncInfoFilter;
 import org.eclipse.team.core.synchronize.SyncInfo;
 import org.eclipse.team.core.synchronize.FastSyncInfoFilter.SyncInfoDirectionFilter;
 import org.eclipse.team.internal.ccvs.ui.ICVSUIConstants;
 import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
 import org.eclipse.team.ui.synchronize.SynchronizeModelOperation;
 
 public class WorkspaceUpdateAction extends CVSParticipantAction {
 	
 	private boolean promptBeforeUpdate;
 
 	public WorkspaceUpdateAction(ISynchronizePageConfiguration configuration) {
 		super(configuration);
 		setId(ICVSUIConstants.CMD_UPDATE);
 		setActionDefinitionId(ICVSUIConstants.CMD_UPDATE);
 	}
 
 	public WorkspaceUpdateAction(ISynchronizePageConfiguration configuration, ISelectionProvider provider, String bundleKey) {
 		super(configuration, provider, bundleKey);
		setId(ICVSUIConstants.CMD_UPDATE_ALL);
		setActionDefinitionId(ICVSUIConstants.CMD_UPDATE_ALL);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.team.ui.sync.SubscriberAction#getSyncInfoFilter()
 	 */
 	protected FastSyncInfoFilter getSyncInfoFilter() {
 		return new SyncInfoDirectionFilter(new int[] {SyncInfo.INCOMING, SyncInfo.CONFLICTING});
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.team.internal.ui.actions.SubscriberAction#getSubscriberOperation(org.eclipse.compare.structuremergeviewer.IDiffElement[])
 	 */
 	protected SynchronizeModelOperation getSubscriberOperation(ISynchronizePageConfiguration configuration, IDiffElement[] elements) {
 		return new WorkspaceUpdateOperation(configuration, elements, promptBeforeUpdate);
 	}
 
 	public void setPromptBeforeUpdate(boolean prompt) {
 		promptBeforeUpdate = prompt;
 	}
 }
