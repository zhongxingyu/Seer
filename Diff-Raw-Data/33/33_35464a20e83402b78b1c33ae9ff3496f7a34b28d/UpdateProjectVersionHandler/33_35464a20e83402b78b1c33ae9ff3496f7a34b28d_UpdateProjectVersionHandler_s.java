 /*******************************************************************************
  * Copyright (c) 2008-2012 EclipseSource Muenchen GmbH.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  ******************************************************************************/
 package org.eclipse.emf.emfstore.client.ui.handlers;
 
 import java.util.List;
 
 import org.eclipse.emf.emfstore.client.model.ProjectSpace;
 import org.eclipse.emf.emfstore.client.ui.controller.UIUpdateProjectController;
 import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
 import org.eclipse.emf.emfstore.server.model.versioning.HistoryInfo;
 import org.eclipse.emf.emfstore.server.model.versioning.RangeQuery;
 import org.eclipse.emf.emfstore.server.model.versioning.Versions;
 import org.eclipse.emf.emfstore.server.model.versioning.util.HistoryQueryBuilder;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.ui.dialogs.ListDialog;
 
 /**
  * Handler for updating a project to a specific version.<br/>
 * It is assumed that the user previously has selected a {@link ProjectSpace}
 * instance.<br/>
  * 
  * @author ovonwesen
  * @author emueller
  * @author eneufeld
  */
 public class UpdateProjectVersionHandler extends AbstractEMFStoreHandler {
 
 	@Override
 	public void handle() {
 		ProjectSpace ps = requireSelection(ProjectSpace.class);
 		if (ps != null) {
			RangeQuery query = HistoryQueryBuilder.rangeQuery(
					ps.getBaseVersion(), 5, 10, true, false, false, true);
 			try {
 				List<HistoryInfo> historyInfo = ps.getHistoryInfo(query);
 				if (historyInfo.size() == 0) {
					new UIUpdateProjectController(getShell(), ps,
							Versions.createHEAD(ps.getBaseVersion())).execute();
 					return;
 				}
 
 				ListDialog listDialog = new ListDialog(getShell());
				listDialog.setContentProvider(ArrayContentProvider
						.getInstance());
 				listDialog.setLabelProvider(new LabelProvider() {
 
 					@Override
 					public String getText(Object element) {
 
 						HistoryInfo historyInfo = (HistoryInfo) element;
 
 						StringBuilder sb = new StringBuilder();
 						sb.append("Version ");
						sb.append(Integer.toString(historyInfo.getPrimerySpec()
								.getIdentifier()));
 						sb.append("  -  ");
 						sb.append(historyInfo.getLogMessage().getMessage());
 
 						// TODO remove if the displayed data is enough
 						// String tags = " ";
 						// for (TagVersionSpec tag : historyInfo.getTagSpecs())
 						// {
 						// tags += tag.getName() + " ";
 						// }
 						// sb.append(tags);
 
 						// sb.append(historyInfo.getLogMessage().getDate()
 						// .toString());
 						//
 						// sb.append(historyInfo.getLogMessage().getAuthor());
 
 						return sb.toString();
 
 					}
 
 				});
 				listDialog.setInput(historyInfo);
 				listDialog.setTitle("Select a Version to update to");
				listDialog
						.setMessage("The project will be updated to the selected Version");
				listDialog.setInitialSelections(new Object[] { historyInfo
						.get(0) });
 				int result = listDialog.open();
 				if (Dialog.OK == result) {
 					Object[] selection = listDialog.getResult();
 					HistoryInfo info = (HistoryInfo) selection[0];
 
					new UIUpdateProjectController(getShell(), ps,
							Versions.createPRIMARY(info.getPrimerySpec()
									.getIdentifier())).execute();
 				}
 			} catch (EmfStoreException e) {
 
 			}
 		}
 
 	}
 
 }
