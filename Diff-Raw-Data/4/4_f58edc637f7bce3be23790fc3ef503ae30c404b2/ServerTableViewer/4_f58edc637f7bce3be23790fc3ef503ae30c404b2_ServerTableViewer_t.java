 /*******************************************************************************
  * Copyright (c) 2003, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - Initial API and implementation
  *******************************************************************************/
 package org.eclipse.wst.server.ui.internal.view.servers;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jface.viewers.*;
 
 import org.eclipse.wst.server.core.*;
 import org.eclipse.wst.server.core.internal.IPublishListener;
 import org.eclipse.wst.server.core.internal.PublishAdapter;
 import org.eclipse.wst.server.core.internal.Server;
 import org.eclipse.wst.server.ui.internal.ServerTree;
 import org.eclipse.wst.server.ui.internal.Trace;
 import org.eclipse.wst.server.ui.internal.view.tree.ServerTreeAction;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.ISelectionListener;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.actions.ActionFactory;
 /**
  * Tree view showing servers and their associations.
  */
 public class ServerTableViewer extends TreeViewer {
 	protected static final String ROOT = "root";
 
 	protected IServerLifecycleListener serverResourceListener;
 	protected IPublishListener publishListener;
 	protected IServerListener serverListener;
 
 	protected static Object deletedElement = null;
 
 	// servers that are currently publishing and starting
 	protected static List publishing = new ArrayList();
 	protected static List starting = new ArrayList();
 	
 	protected ServerTableLabelProvider labelProvider;
 	protected ISelectionListener dsListener;
 
 	protected ServersView view;
 	
 	public class ServerContentProvider implements IStructuredContentProvider, ITreeContentProvider {
 		public Object[] getElements(Object element) {
 			List list = new ArrayList();
 			IServer[] servers = ServerCore.getServers();
 			if (servers != null) {
 				int size = servers.length;
 				for (int i = 0; i < size; i++) {
 					if (!((Server)servers[i]).isPrivate())
 						list.add(servers[i]);
 				}
 			}
 			return list.toArray();
 		}
 
 		public void inputChanged(Viewer theViewer, Object oldInput, Object newInput) {
 			// do nothing
 		}
 		
 		public void dispose() {
 			// do nothing
 		}
 
 		public Object[] getChildren(Object element) {
 			if (element instanceof ModuleServer) {
 				ModuleServer ms = (ModuleServer) element;
 				try {
 					IModule[] children = ms.server.getChildModules(ms.module, null);
 					int size = children.length;
 					ModuleServer[] ms2 = new ModuleServer[size];
 					for (int i = 0; i < size; i++) {
 						int size2 = ms.module.length;
 						IModule[] module = new IModule[size2 + 1];
 						System.arraycopy(ms.module, 0, ms2[i].module, 0, size2);
 						module[size2] = children[i];
 						ms2[i] = new ModuleServer(ms.server, module);
 					}
 					return ms2;
 				} catch (Exception e) {
 					return null;
 				}
 			}
 			
 			IServer server = (IServer) element;
 			IModule[] modules = server.getModules(); 
 			int size = modules.length;
 			ModuleServer[] ms = new ModuleServer[size];
 			for (int i = 0; i < size; i++) {
 				ms[i] = new ModuleServer(server, new IModule[] { modules[i] });
 			}
 			return ms;
 		}
 
 		public Object getParent(Object element) {
 			if (element instanceof ModuleServer) {
 				ModuleServer ms = (ModuleServer) element;
 				return ms.server;
 			}
 			return null;
 		}
 
 		public boolean hasChildren(Object element) {
 			if (element instanceof ModuleServer)
 				return false;
 			
 			IServer server = (IServer) element;
 			return server.getModules().length > 0;
 		}
 	}
 
 	/*protected void createHover(Shell parent, Point p) {
 		final Shell fShell = new Shell(parent, SWT.NO_FOCUS | SWT.ON_TOP | SWT.RESIZE | SWT.NO_TRIM);
 		GridLayout layout = new GridLayout();
 		layout.marginHeight = 1;
 		layout.marginWidth = 1;
 		fShell.setLayout(layout);
 		
 		Display display = parent.getDisplay();
 		StyledText text = new StyledText(fShell, SWT.NONE);
 		text.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
 		text.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
 		text.append("Testing <b>me</b>");
 		
 		fShell.setLocation(p.x, p.y);
 		fShell.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
 		fShell.pack();
 		fShell.setVisible(true);
 		
 		Thread t = new Thread() {
 			public void run() {
 				try {
 					Thread.sleep(2000);
 				} catch (Exception e) { }
 				Display.getDefault().asyncExec(new Runnable() {
 					public void run() {
 						fShell.dispose();
 					}
 				});
 			}
 		};
 		t.start();
 	}*/
 	
 	protected Thread thread = null;
 	protected boolean stopThread = false;
 	
 	protected void startThread() {
 		stopThread = false;
 		if (thread != null)
 			return;
 		
 		thread = new Thread("Servers view animator") {
 			public void run() {
 				while (!stopThread) {
 					try {
 						labelProvider.animate();
						final Object[] rootElements = ((ITreeContentProvider)getContentProvider()).getElements(null); 
 						Display.getDefault().asyncExec(new Runnable() {
 							public void run() {
 								if (getTree() != null && !getTree().isDisposed())
									update(rootElements, null);
 							}
 						});
 						Thread.sleep(200);
 					} catch (Exception e) {
 						Trace.trace(Trace.FINEST, "Error in animated server view", e);
 					}
 					thread = null;
 				}
 			}
 		};
 		thread.setDaemon(true);
 		thread.start();
 	}
 
 	protected void stopThread() {
 		stopThread = true;
 	}
 
 	/**
 	 * ServerTableViewer constructor comment.
 	 * 
 	 * @param view the view 
 	 * @param tree the tree
 	 */
 	public ServerTableViewer(final ServersView view, final Tree tree) {
 		super(tree);
 		this.view = view;
 		/*table.addMouseTrackListener(new MouseTrackListener() {
 			public void mouseEnter(MouseEvent event) {
 			}
 
 			public void mouseExit(MouseEvent event) {
 			}
 
 			public void mouseHover(MouseEvent event) {
 				createHover(table.getShell(), table.toDisplay(event.x, event.y));
 			}
 		});*/
 	
 		setContentProvider(new ServerContentProvider());
 		labelProvider = new ServerTableLabelProvider();
 		setLabelProvider(labelProvider);
 		setSorter(new ViewerSorter() {
 			// empty
 		});
 	
 		setInput(ROOT);
 	
 		addListeners();
 		
 		IActionBars actionBars = view.getViewSite().getActionBars();
 		actionBars.setGlobalActionHandler(ActionFactory.DELETE.getId(), new ServerTreeAction(getControl().getShell(), this, "Delete it!", ServerTree.ACTION_DELETE));
 		
 		dsListener = new ISelectionListener() {
 			public void selectionChanged(IWorkbenchPart part, ISelection selection) {
 				if (!(selection instanceof IStructuredSelection))
 					return;
 				IStructuredSelection sel = (IStructuredSelection) selection;
 				final Object obj = sel.getFirstElement();
 				IProject proj = null;
 				if (obj instanceof IResource) {
 					IResource res = (IResource) obj;
 					proj = res.getProject();
 				}
 				if (proj == null) {
 					try {
 						IResource res = (IResource) Platform.getAdapterManager().getAdapter(obj, IResource.class);
 						if (res != null)
 							proj = res.getProject();
 					} catch (Exception e) {
 						// ignore
 					}
 				}
 				if (proj != null) {
 					final IProject project = proj;
 					Display.getDefault().asyncExec(new Runnable() {
 						public void run() {
 							if (getTree() == null || getTree().isDisposed())
 								return;
 
 							IServer defaultServer = null;
 							if (project != null) {
 								IProjectProperties props = ServerCore.getProjectProperties(project);
 								defaultServer = props.getDefaultServer();
 							}
 							IServer oldDefaultServer = labelProvider.getDefaultServer();
 							if ((oldDefaultServer == null && defaultServer == null)
 									|| (oldDefaultServer != null && oldDefaultServer.equals(defaultServer)))
 								return;
 							labelProvider.setDefaultServer(defaultServer);
 							
 							if (oldDefaultServer != null)
 								refresh(oldDefaultServer);
 							if (defaultServer != null)
 								refresh(defaultServer);
 						}
 					});
 				}
 			}
 		};
 		view.getViewSite().getPage().addSelectionListener(dsListener);
 	}
 
 	protected void addListeners() {
 		serverResourceListener = new IServerLifecycleListener() {
 			public void serverAdded(IServer server) {
 				addServer(server);
 				server.addServerListener(serverListener);
 				((Server) server).addPublishListener(publishListener);
 			}
 			public void serverChanged(IServer server) {
 				refreshServer(server);
 			}
 			public void serverRemoved(IServer server) {
 				removeServer(server);
 				server.removeServerListener(serverListener);
 				((Server) server).removePublishListener(publishListener);
 			}
 		};
 		ServerCore.addServerLifecycleListener(serverResourceListener);
 		
 		publishListener = new PublishAdapter() {
 			public void publishStarted(IServer server) {
 				handlePublishChange(server, true);
 			}
 			
 			public void publishFinished(IServer server, IStatus status) {
 				handlePublishChange(server, false);
 			}
 		};
 		
 		serverListener = new IServerListener() {
 			public void serverChanged(ServerEvent event) {
 				if (event == null) {
 					return;
 				}
 				int eventKind = event.getKind();
 				IServer server = event.getServer();
 				if ((eventKind & ServerEvent.SERVER_CHANGE) != 0) {
 					// server change event
 					if ((eventKind & ServerEvent.STATE_CHANGE) != 0) {
 						refreshServer(server);
 						int state = event.getState();
 						String id = server.getId();
 						if (state == IServer.STATE_STARTING || state == IServer.STATE_STOPPING) {
 							if (!starting.contains(id)) {
 								if (starting.isEmpty())
 									startThread();
 								starting.add(id);
 							}
 						} else {
 							if (starting.contains(id)) {
 								starting.remove(id);
 								if (starting.isEmpty())
 									stopThread();
 							}
 						}
 					} else if ((eventKind & ServerEvent.RESTART_STATE_CHANGE) != 0) {
 						refreshServer(server);
 					} 
 				} else if ((eventKind & ServerEvent.MODULE_CHANGE) != 0) {
 					// module change event
 					if ((eventKind & ServerEvent.STATE_CHANGE) != 0) {
 						refreshServer(server);
 					} 
 				}
 			}
 		};
 		
 		// add listeners to servers
 		IServer[] servers = ServerCore.getServers();
 		if (servers != null) {
 			int size = servers.length;
 			for (int i = 0; i < size; i++) {
 				servers[i].addServerListener(serverListener);
 				((Server) servers[i]).addPublishListener(publishListener);
 			}
 		}
 	}
 	
 	protected void refreshServer(final IServer server) {
 		Display.getDefault().asyncExec(new Runnable() {
 			public void run() {
 				try {
 					refresh(server);
 					ISelection sel = ServerTableViewer.this.getSelection();
 					ServerTableViewer.this.setSelection(sel);
 				} catch (Exception e) {
 					// ignore
 				}
 			}
 		});
 	}
 
 	protected void handleDispose(DisposeEvent event) {
 		stopThread();
 		view.getViewSite().getPage().removeSelectionListener(dsListener);
 
 		ServerCore.removeServerLifecycleListener(serverResourceListener);
 		
 		// remove listeners from server
 		IServer[] servers = ServerCore.getServers();
 		if (servers != null) {
 			int size = servers.length;
 			for (int i = 0; i < size; i++) {
 				servers[i].removeServerListener(serverListener);
 				((Server) servers[i]).removePublishListener(publishListener);
 			}
 		}
 	
 		super.handleDispose(event);
 	}
 
 	/**
 	 * Called when the publish state changes.
 	 * @param server org.eclipse.wst.server.core.IServer
 	 */
 	protected void handlePublishChange(IServer server, boolean isPublishing) {
 		String serverId = server.getId();
 		if (isPublishing)
 			publishing.add(serverId);
 		else
 			publishing.remove(serverId);
 	
 		refreshServer(server);
 	}
 	
 	/**
 	 * 
 	 */
 	protected void handleServerModulesChanged(IServer server2) {
 		if (server2 == null)
 			return;
 
 		IServer[] servers = ServerCore.getServers();
 		if (servers != null) {
 			int size = servers.length;
 			for (int i = 0; i < size; i++) {
 				if (server2.equals(servers[i]))
 					refresh(servers[i]);
 			}
 		}
 	}
 	
 	/**
 	 * Called when an element is added.
 	 * @param server org.eclipse.wst.server.core.IServer
 	 */
 	protected void handleServerResourceAdded(IServer server) {
 		add(null, server);
 	}
 	
 	/*protected void handleServerResourceAdded(IServerConfiguration configuration) {
 		configurationChange(configuration, true);
 	}*/
 	
 	/**
 	 * Called when an element is changed.
 	 * @param server org.eclipse.wst.server.core.IServer
 	 */
 	protected void handleServerResourceChanged(IServer server) {
 		refresh(server);
 	}
 	
 	/*protected void handleServerResourceChanged(IServerConfiguration configuration) {
 		IServer[] servers = ServerCore.getServers();
 		if (servers != null) {
 			int size = servers.length;
 			for (int i = 0; i < size; i++) {
 				IServerConfiguration config = servers[i].getServerConfiguration();
 				if (configuration.equals(config))
 					refresh(servers[i]);
 			}
 		}
 	}*/
 	
 	/**
 	 * Called when an element is removed.
 	 * @param server org.eclipse.wst.server.core.IServer
 	 */
 	protected void handleServerResourceRemoved(IServer server) {
 		remove(server);
 
 		String serverId = server.getId();
 		publishing.remove(serverId);
 
 		view.getViewSite().getActionBars().getStatusLineManager().setMessage(null, null);
 	}
 	
 	/*protected void handleServerResourceRemoved(IServerConfiguration configuration) {
 		configurationChange(configuration, false);
 	}*/
 	
 	protected void addServer(final IServer server) {
 		Display.getDefault().asyncExec(new Runnable() {
 			public void run() {
 				add(ROOT, server);
 			}
 		});
 	}
 	
 	protected void removeServer(final IServer server) {
 		Display.getDefault().asyncExec(new Runnable() {
 			public void run() {
 				remove(server);
 			}
 		});
 	}
 }
