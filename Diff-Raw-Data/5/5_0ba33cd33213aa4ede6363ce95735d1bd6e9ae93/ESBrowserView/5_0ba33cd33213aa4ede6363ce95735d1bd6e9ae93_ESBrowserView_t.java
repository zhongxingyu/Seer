 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  ******************************************************************************/
 package org.eclipse.emf.emfstore.client.ui.views.emfstorebrowser.views;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.impl.AdapterImpl;
 import org.eclipse.emf.emfstore.client.model.ModelPackage;
 import org.eclipse.emf.emfstore.client.model.ServerInfo;
 import org.eclipse.emf.emfstore.client.model.Usersession;
 import org.eclipse.emf.emfstore.client.model.Workspace;
 import org.eclipse.emf.emfstore.client.model.WorkspaceManager;
 import org.eclipse.emf.emfstore.client.model.observers.LoginObserver;
 import org.eclipse.emf.emfstore.client.ui.views.emfstorebrowser.provider.ESBrowserContentProvider;
 import org.eclipse.emf.emfstore.client.ui.views.emfstorebrowser.provider.ESBrowserLabelProvider;
 import org.eclipse.emf.emfstore.client.ui.views.emfstorebrowser.provider.ESBrowserViewerSorter;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.viewers.DecoratingLabelProvider;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.ui.IDecoratorManager;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.ViewPart;
 import org.eclipse.ui.services.IEvaluationService;
 
 /**
  * View containing the remote repositories.
  * 
  * @author shterev
  */
 public class ESBrowserView extends ViewPart implements LoginObserver {
 
 	/**
 	 * Listener for changes in the workspace.
 	 * 
 	 * @author koegel
 	 */
 	private final class WorkspaceAdapter extends AdapterImpl {
 		@Override
 		public void notifyChanged(Notification msg) {
 			if (msg.getNewValue() instanceof ServerInfo) {
 				final ServerInfo serverInfo = (ServerInfo) msg.getNewValue();
 				AdapterImpl serverInfoAdapter = new ServerInfoAdapter(serverInfo);
 				serverInfo.eAdapters().add(serverInfoAdapter);
 				serverInfoAdapterMap.put(serverInfo, serverInfoAdapter);
 				viewer.refresh();
 			} else if (msg.getOldValue() instanceof ServerInfo) {
 				ServerInfo serverInfo = (ServerInfo) msg.getOldValue();
 				serverInfo.eAdapters().remove(serverInfoAdapterMap.get(serverInfo));
 				viewer.refresh();
 			}
 			super.notifyChanged(msg);
 		}
 	}
 
 	/**
 	 * Listener for changes in the server infos.
 	 * 
 	 * @author koegel
 	 */
 	private final class ServerInfoAdapter extends AdapterImpl {
 		private final ServerInfo serverInfo;
 
 		private ServerInfoAdapter(ServerInfo serverInfo) {
 			this.serverInfo = serverInfo;
 		}
 
 		@Override
 		public void notifyChanged(final Notification msg) {
 			if (msg.getFeature() != null
 				&& msg.getFeature().equals(ModelPackage.eINSTANCE.getServerInfo_ProjectInfos())) {
 				Display.getDefault().asyncExec(new Runnable() {
 					public void run() {
 						if (msg.getEventType() == Notification.REMOVE_MANY || msg.getEventType() == Notification.REMOVE) {
 							viewer.collapseToLevel(serverInfo, 0);
 						}
 						viewer.refresh(serverInfo, true);
 						// re-evaluate property tester
 						IEvaluationService service = (IEvaluationService) getSite()
 							.getService(IEvaluationService.class);
 						service.requestEvaluation("org.eclipse.emf.emfstore.client.ui.commands.ServerInfoIsLoggedIn");
 					}
 				});
 			}
 		}
 	}
 
 	private TreeViewer viewer;
 
 	private ESBrowserContentProvider contentProvider;
 	private MenuManager menuMgr;
 	private AdapterImpl workspaceAdapter;
 
 	private Map<ServerInfo, AdapterImpl> serverInfoAdapterMap = new HashMap<ServerInfo, AdapterImpl>();
 
 	/**
 	 * The constructor.
 	 */
 	public ESBrowserView() {
 		Workspace currentWorkspace = WorkspaceManager.getInstance().getCurrentWorkspace();
 		WorkspaceManager.getObserverBus().register(this);
 		for (final ServerInfo serverInfo : currentWorkspace.getServerInfos()) {
 			AdapterImpl serverInfoAdapter = new ServerInfoAdapter(serverInfo);
 			serverInfo.eAdapters().add(serverInfoAdapter);
 			serverInfoAdapterMap.put(serverInfo, serverInfoAdapter);
 		}
 		workspaceAdapter = new WorkspaceAdapter();
 		currentWorkspace.eAdapters().add(workspaceAdapter);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void createPartControl(Composite parent) {
 		viewer = new TreeViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
 
 		contentProvider = new ESBrowserContentProvider();
 		viewer.setContentProvider(contentProvider);
 		IDecoratorManager decoratorManager = PlatformUI.getWorkbench().getDecoratorManager();
 		viewer.setLabelProvider(new DecoratingLabelProvider(new ESBrowserLabelProvider(), decoratorManager
 			.getLabelDecorator()));
 		viewer.setSorter(new ESBrowserViewerSorter());
 		viewer.setInput(WorkspaceManager.getInstance().getCurrentWorkspace());
 		// viewer.addTreeListener(new ITreeViewerListener() {
 		//
 		// /**
 		// * {@inheritDoc}
 		// */
 		// public void treeExpanded(TreeExpansionEvent event) {
 		// if (event.getElement() instanceof ServerInfo) {
 		//
 		// ServerInfo value = (ServerInfo) event.getElement();
 		// // new UIServerLoginController(PlatformUI.getWorkbench().getDisplay().getActiveShell(), value)
 		// // .openLoginDialog();
 		//
 		// // TODO: refresh is always performed
 		// if (!event.getTreeViewer().isBusy()) {
 		// event.getTreeViewer().refresh(value, true);
 		// }
 		// }
 		// }
 		//
 		// public void treeCollapsed(TreeExpansionEvent event) {
 		// }
 		// });
 
 		PlatformUI.getWorkbench().getHelpSystem()
 			.setHelp(viewer.getControl(), "org.eclipse.emf.emfstore.client.ui.views.RepositoryView");
 
 		menuMgr = new MenuManager();
 		menuMgr.add(new Separator("additions"));
 		Control control = viewer.getControl();
 		Menu menu = menuMgr.createContextMenu(control);
 		control.setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
		
 		getSite().setSelectionProvider(viewer);
 		hookDoubleClickAction();
 
 	}
 
 	private void hookDoubleClickAction() {
 		viewer.addDoubleClickListener(new IDoubleClickListener() {
 			public void doubleClick(DoubleClickEvent event) {
 				Object firstElement = ((IStructuredSelection) viewer.getSelection()).getFirstElement();
 				viewer.refresh(firstElement);
 				viewer.expandToLevel(firstElement, 1);
 			}
 		});
 	}
 
 	/**
 	 * Passing the focus request to the viewer's control.
 	 */
 	@Override
 	public void setFocus() {
 		viewer.getControl().setFocus();
 	}
 
 	/**
 	 * @return the {@link TreeViewer} for this view.
 	 */
 	public TreeViewer getViewer() {
 		return viewer;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void loginCompleted(final Usersession session) {
 		getSite().getShell().getDisplay().asyncExec(new Runnable() {
 			public void run() {
 				viewer.refresh(session.getServerInfo(), true);
 			}
 		});
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
 	 */
 	@Override
 	public void dispose() {
 		super.dispose();
 		Workspace currentWorkspace = WorkspaceManager.getInstance().getCurrentWorkspace();
 		currentWorkspace.eAdapters().remove(workspaceAdapter);
 		WorkspaceManager.getObserverBus().unregister(this);
 		for (ServerInfo s : currentWorkspace.getServerInfos()) {
 			s.eAdapters().remove(serverInfoAdapterMap.get(s));
 		}
 
 	}
 
 }
