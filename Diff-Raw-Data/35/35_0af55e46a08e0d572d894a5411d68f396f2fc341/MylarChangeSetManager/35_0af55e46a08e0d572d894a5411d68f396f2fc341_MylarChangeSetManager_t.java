 /*******************************************************************************
  * Copyright (c) 2004 - 2006 University Of British Columbia and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     University Of British Columbia - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.mylar.internal.ide.team;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.mylar.internal.core.MylarContextManager;
 import org.eclipse.mylar.internal.core.util.MylarStatusHandler;
 import org.eclipse.mylar.internal.ide.MylarIdePlugin;
 import org.eclipse.mylar.provisional.core.IMylarContext;
 import org.eclipse.mylar.provisional.core.IMylarContextListener;
 import org.eclipse.mylar.provisional.core.IMylarElement;
 import org.eclipse.mylar.provisional.core.IMylarStructureBridge;
 import org.eclipse.mylar.provisional.core.MylarPlugin;
 import org.eclipse.mylar.provisional.tasklist.AbstractTaskContainer;
 import org.eclipse.mylar.provisional.tasklist.DateRangeContainer;
 import org.eclipse.mylar.provisional.tasklist.ITask;
 import org.eclipse.mylar.provisional.tasklist.ITaskActivityListener;
 import org.eclipse.mylar.provisional.tasklist.ITaskListChangeListener;
 import org.eclipse.mylar.provisional.tasklist.MylarTaskListPlugin;
 import org.eclipse.team.internal.ccvs.core.mapping.CVSActiveChangeSetCollector;
 import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
 import org.eclipse.team.internal.core.subscribers.ActiveChangeSetManager;
 import org.eclipse.team.internal.core.subscribers.ChangeSet;
 import org.eclipse.team.internal.core.subscribers.IChangeSetChangeListener;
 import org.osgi.framework.Bundle;
 
 /**
  * @author Mik Kersten
  */
 public class MylarChangeSetManager implements IMylarContextListener {
 
 	private final IChangeSetChangeListener CHANGE_SET_LISTENER = new IChangeSetChangeListener() {
 		public void setRemoved(ChangeSet set) {
 			if (set instanceof MylarActiveChangeSet) {
 				MylarActiveChangeSet contextChangeSet = (MylarActiveChangeSet) set;
 				if (contextChangeSet.getTask().isActive()) {
 					for (ActiveChangeSetManager collector : collectors) {
 						collector.add(contextChangeSet); // put it back
 					}
 				}
 			}
 		}
 
 		public void setAdded(ChangeSet set) {
 			// ignore
 		}
 
 		public void defaultSetChanged(ChangeSet previousDefault, ChangeSet set) {
 			// ignore
 		}
 
 		public void nameChanged(ChangeSet set) {
 			// ignore
 		}
 
 		public void resourcesChanged(ChangeSet set, IPath[] paths) {
 			// ignore
 		}
 	};
 
 	private List<ActiveChangeSetManager> collectors = new ArrayList<ActiveChangeSetManager>();
 
 	private Map<String, MylarActiveChangeSet> activeChangeSets = new HashMap<String, MylarActiveChangeSet>();
 
 	private ITaskActivityListener TASK_ACTIVITY_LISTENER = new ITaskActivityListener() {
 		
 		public void tasklistRead() {
 			initContextChangeSets();
 		}
 		
 		public void taskActivated(ITask task) {
 			// ignore
 		}
 
 		public void tasksActivated(List<ITask> tasks) {
 			// ignore
 		}
 
 		public void taskDeactivated(ITask task) {
 			// ignore			
 		}
 
 		public void activityChanged(DateRangeContainer week) {
 			// ignore	
 		}
 	};
 	
 	private ITaskListChangeListener TASK_CHANGE_LISTENER = new ITaskListChangeListener() {
 
 		public void localInfoChanged(ITask task) {
 			for (ActiveChangeSetManager collector : collectors) {
 				ChangeSet[] sets = collector.getSets();
 				for (int i = 0; i < sets.length; i++) {
 					ChangeSet set = sets[i];
 					if (set instanceof MylarActiveChangeSet) {
 						MylarActiveChangeSet contextChangeSet = (MylarActiveChangeSet) set;
 						if (contextChangeSet.getTask().equals(task)) {
 							contextChangeSet.initTitle();
 						}
 					}
 				}
 			}
 		}
 
 		public void repositoryInfoChanged(ITask task) {
 			// ignore
 		}
 
 		public void taskMoved(ITask task, AbstractTaskContainer fromContainer, AbstractTaskContainer toContainer) {
 			// ignore
 		}
 
 		public void taskDeleted(ITask task) {
 			// ignore
 		}
 
 		public void containerAdded(AbstractTaskContainer container) {
 			// ignore
 		}
 
 		public void containerDeleted(AbstractTaskContainer container) {
 			// ignore
 		}
 
 		public void taskAdded(ITask task) {
 			// ignore
 		}
 
 		public void containerInfoChanged(AbstractTaskContainer container) {
 			// ignore
 		}
 	};
 
 	private boolean isEnabled = false;
 
 	public MylarChangeSetManager() {
 		collectors.add((CVSActiveChangeSetCollector)CVSUIPlugin.getPlugin().getChangeSetManager());
 		Bundle svnBundle = Platform.getBundle("org.tigris.subversion.subclipse.core");
 		if (svnBundle != null) {
 			Method getChangeSetManagerMethod;
 			try {
 				Class providerPlugin = Class.forName("org.tigris.subversion.subclipse.core.SVNProviderPlugin");
 				Method getPluginMethod = providerPlugin.getMethod("getPlugin", new Class[0]);
 				Object pluginInstance = getPluginMethod.invoke(null, new Object[0]);
 				getChangeSetManagerMethod = providerPlugin.getDeclaredMethod("getChangeSetManager", new Class[0]);
 				collectors.add((ActiveChangeSetManager) getChangeSetManagerMethod.invoke(pluginInstance, new Object[0]));
 			} catch (Throwable t) {
 				// intore missing tigris collector
 			}
 		}
 	}
 
 	public void enable() {
 		if (!isEnabled) {
 			MylarPlugin.getContextManager().addListener(this);
 			MylarTaskListPlugin.getTaskListManager().getTaskList().addChangeListener(TASK_CHANGE_LISTENER);
 			MylarTaskListPlugin.getTaskListManager().addActivityListener(TASK_ACTIVITY_LISTENER);
 			if (MylarTaskListPlugin.getTaskListManager().isTaskListInitialized()) {
 				initContextChangeSets(); // otherwise listener will do it
 			}
 			for (ActiveChangeSetManager collector : collectors) {
 				collector.addListener(CHANGE_SET_LISTENER);
 			}
 			isEnabled = true;
 		}
 	}
 
 	public void disable() {
 		MylarPlugin.getContextManager().removeListener(this);
 		MylarTaskListPlugin.getTaskListManager().removeActivityListener(TASK_ACTIVITY_LISTENER);
 		MylarTaskListPlugin.getTaskListManager().getTaskList().removeChangeListener(TASK_CHANGE_LISTENER);
 		for (ActiveChangeSetManager collector : collectors) {
 			collector.removeListener(CHANGE_SET_LISTENER);
 		}
 		isEnabled = false;
 	}
 
 	private void initContextChangeSets() {
 		for (ActiveChangeSetManager collector : collectors) {
 			ChangeSet[] sets = collector.getSets();
 			for (int i = 0; i < sets.length; i++) {
 				ChangeSet restoredSet = sets[i];
 				if (!(restoredSet instanceof MylarActiveChangeSet)) {
 					String encodedTitle = restoredSet.getName();
 					String taskHandle = MylarActiveChangeSet.getHandleFromPersistedTitle(encodedTitle);
 					ITask task = MylarTaskListPlugin.getTaskListManager().getTaskList().getTask(taskHandle);
 					if (task != null) {
 						try {
 							MylarActiveChangeSet contextChangeSet = new MylarActiveChangeSet(task, collector);
 							contextChangeSet.restoreResources(restoredSet.getResources());
 							collector.remove(restoredSet);
 							collector.add(contextChangeSet);
 						} catch (Exception e) {
 							MylarStatusHandler.fail(e, "could not restore change set", false);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * For testing.
 	 */
 	public void clearActiveChangeSets() {
 		activeChangeSets.clear();
 	}
 
 	public IResource[] getResources(ITask task) {
 		for (ActiveChangeSetManager collector : collectors) {
 			ChangeSet[] sets = collector.getSets();
 			for (int i = 0; i < sets.length; i++) {
 				ChangeSet set = sets[i];
 				if (set instanceof MylarActiveChangeSet) {
 					MylarActiveChangeSet contextChangeSet = (MylarActiveChangeSet) set;
 					if (contextChangeSet.getTask().equals(task)) {
 						return contextChangeSet.getResources();
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	public void contextActivated(IMylarContext context) {
 		try {
 			ITask task = getTask(context);
 			if (task != null && !activeChangeSets.containsKey(task.getHandleIdentifier())) {
 				for (ActiveChangeSetManager collector : collectors) {
 					MylarActiveChangeSet contextChangeSet = new MylarActiveChangeSet(task, collector);
 					List<IResource> interestingResources = MylarIdePlugin.getDefault().getInterestingResources();
 					contextChangeSet.add(interestingResources.toArray(new IResource[interestingResources.size()]));
 	
 					activeChangeSets.put(task.getHandleIdentifier(), contextChangeSet);
 
 					if (!collector.contains(contextChangeSet)) {
 						collector.add(contextChangeSet);
 					}
 //					collector.makeDefault(contextChangeSet);
 //					IdeUiUtil.forceSynchronizeViewUpdate();
 //					DiffChangeEvent event = new DiffChangeEvent(contextChangeSet.getDiffTree());
 //					collector.diffsChanged(event, new NullProgressMonitor());
 				}
 			}
 		} catch (Exception e) {
 			MylarStatusHandler.fail(e, "could not update change set", false);
 		}
 	}
 
 	public void contextDeactivated(IMylarContext context) {
 		for (ActiveChangeSetManager collector : collectors) {
 			ChangeSet[] sets = collector.getSets();
 			for (int i = 0; i < sets.length; i++) {
 				ChangeSet set = sets[i];
 				if (set instanceof MylarActiveChangeSet) {
 					IResource[] resources = set.getResources();
 					if (resources == null || resources.length == 0) {
 						collector.remove(set);
 					}
 				}
 			}
 		}
 		activeChangeSets.clear();
 	}
 
 	public List<MylarActiveChangeSet> getActiveChangeSets() {
 		return new ArrayList<MylarActiveChangeSet>(activeChangeSets.values());
 	}
 
 	private ITask getTask(IMylarContext context) {
 		List<ITask> activeTasks = MylarTaskListPlugin.getTaskListManager().getTaskList().getActiveTasks();
 
 		// TODO: support multiple tasks
 		if (activeTasks.size() > 0) {
 			return activeTasks.get(0);
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * Ignores decay.
 	 */
 	private boolean shouldRemove(IMylarElement element) {
 		// TODO: generalize this logic?
 		return (element.getInterest().getValue() + element.getInterest().getDecayValue()) < MylarContextManager
 				.getScalingFactors().getInteresting();
 	}
 
 	public void interestChanged(List<IMylarElement> elements) {
 		for (IMylarElement element : elements) {
 			IMylarStructureBridge bridge = MylarPlugin.getDefault().getStructureBridge(element.getContentType());
 			try {
 				if (bridge.isDocument(element.getHandleIdentifier())) {
 					IResource resource = MylarIdePlugin.getDefault().getResourceForElement(element, false);
 					if (resource != null && resource.exists()) {
 						for (MylarActiveChangeSet activeContextChangeSet : getActiveChangeSets()) {
 							if (!activeContextChangeSet.contains(resource)) {
 								if (element.getInterest().isInteresting()) {
 									activeContextChangeSet.add(new IResource[] { resource });
 								}
 							}
 						}
 						if (shouldRemove(element)) {
 							for (ActiveChangeSetManager collector : collectors) {
 								ChangeSet[] sets = collector.getSets();
 								for (int i = 0; i < sets.length; i++) {
 									if (sets[i] instanceof MylarActiveChangeSet) {
 										sets[i].remove(resource);
 									}
 								}
 							}
 						}
 //						if (shouldRemove(element)) {
 //							ChangeSet[] sets = collector.getSets();
 //							for (int i = 0; i < sets.length; i++) {
 //								if (sets[i] instanceof MylarActiveChangeSet) {
 //									sets[i].remove(resource);
 //								}
 //							}
 //						}
 					}
 				}
 			} catch (Exception e) {
 				MylarStatusHandler.fail(e, "could not manipulate change set resources", false);
 			}
 		}
 	}
 
 	public void nodeDeleted(IMylarElement node) {
 		// TODO: handle?
 	}
 
 	public void landmarkAdded(IMylarElement node) {
 		// ignore
 	}
 
 	public void landmarkRemoved(IMylarElement node) {
 		// ignore
 	}
 
 	public void edgesChanged(IMylarElement node) {
 		// ignore
 	}
 
 	public void presentationSettingsChanging(UpdateKind kind) {
 		// ignore
 	}
 
 	public void presentationSettingsChanged(UpdateKind kind) {
 		// ignore
 	}
 }
 
 // private void touch(final IResource resource) throws CoreException {
 // final IWorkbench workbench = PlatformUI.getWorkbench();
 // workbench.getDisplay().asyncExec(new Runnable() {
 // public void run() {
 // if (resource instanceof IFile) {
 // try {
 // ((IFile)resource).touch(new NullProgressMonitor());
 // } catch (CoreException e) {
 // MylarPlugin.fail(e, "failed to touch resource: " + resource, false);
 // }
 // }
 // }
 // });
 // }
