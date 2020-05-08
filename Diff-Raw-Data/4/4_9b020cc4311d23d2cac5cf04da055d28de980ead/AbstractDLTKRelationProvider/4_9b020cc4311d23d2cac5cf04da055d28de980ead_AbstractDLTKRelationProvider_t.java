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
 /*
  * Created on Jan 26, 2005
  */
 package org.eclipse.mylyn.internal.dltk.search;
 
 import java.util.ArrayList;
 import java.util.ConcurrentModificationException;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Set;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.IMember;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.IType;
 import org.eclipse.dltk.core.search.IDLTKSearchConstants;
 import org.eclipse.dltk.core.search.IDLTKSearchScope;
 import org.eclipse.dltk.core.search.SearchEngine;
 import org.eclipse.dltk.internal.ui.search.DLTKSearchQuery;
 import org.eclipse.dltk.internal.ui.search.DLTKSearchResult;
 import org.eclipse.dltk.ui.search.ElementQuerySpecification;
 import org.eclipse.dltk.ui.search.QuerySpecification;
 import org.eclipse.mylyn.context.core.AbstractContextStructureBridge;
 import org.eclipse.mylyn.context.core.IInteractionElement;
 import org.eclipse.mylyn.internal.context.core.AbstractRelationProvider;
 import org.eclipse.mylyn.internal.context.core.ContextCorePlugin;
 import org.eclipse.mylyn.internal.context.core.IActiveSearchListener;
 import org.eclipse.mylyn.internal.context.core.IActiveSearchOperation;
 import org.eclipse.mylyn.internal.dltk.DLTKStructureBridge;
import org.eclipse.mylyn.internal.dltk.MylynDLTKPlugin;
 import org.eclipse.mylyn.internal.dltk.MylynStatusHandler;
 import org.eclipse.mylyn.internal.resources.ui.ResourcesUiBridgePlugin;
 import org.eclipse.search.ui.ISearchResult;
 import org.eclipse.search2.internal.ui.InternalSearchUI;
 
 public abstract class AbstractDLTKRelationProvider extends
 		AbstractRelationProvider {
 
 	public static final String ID_GENERIC = "org.eclipse.dltk.mylyn.relation";
 
 	public static final String NAME = "DLTK relationships";
 
 	private static final int DEFAULT_DEGREE = 2;
 
 	private static final List runningJobs = new ArrayList();
 
 	private DLTKStructureBridge fBridge;
 	public String getGenericId() {
 		return ID_GENERIC;
 	}
 
 	protected AbstractDLTKRelationProvider(String structureKind, String id, DLTKStructureBridge bridge) {
 		super(structureKind, id);
 		this.fBridge = bridge;
 	}
 
 	protected void findRelated(final IInteractionElement node,
 			int degreeOfSeparation) {
 		if (node == null)
 			return;
 		if (node.getContentType() == null) {
 			MylynStatusHandler.log("null content type for: " + node, this);
 			return;
 		}
 		if (!node.getContentType().equals(fBridge.contentType))
 			return;
 		IModelElement modelElement = DLTKCore
 				.create(node.getHandleIdentifier());
 		if (!acceptElement(modelElement) || !modelElement.exists()) {
 			return;
 		}
 
 		IDLTKSearchScope scope = createSearchScope(modelElement,
 				degreeOfSeparation);
 		if (scope != null)
 			runJob(node, degreeOfSeparation, getId());
 	}
 
 	private IDLTKSearchScope createSearchScope(IModelElement element,
 			int degreeOfSeparation) {
 		Set landmarks = ContextCorePlugin.getContextManager()
 				.getActiveLandmarks();
 		List interestingElements = ContextCorePlugin.getContextManager()
 				.getActiveContext().getInteresting();
 
 		Set searchElements = new HashSet();
 		int includeMask = IDLTKSearchScope.SOURCES;
 		if (degreeOfSeparation == 1) {
 			// for (IInteractionElement landmark : landmarks) {
 			for (Iterator it = landmarks.iterator(); it.hasNext();) {
 				IInteractionElement landmark = (IInteractionElement) it.next();
 				AbstractContextStructureBridge bridge = ContextCorePlugin
 						.getDefault().getStructureBridge(
 								landmark.getContentType());
 				if (includeNodeInScope(landmark, bridge)) {
 					Object o = bridge.getObjectForHandle(landmark
 							.getHandleIdentifier());
 					if (o instanceof IModelElement) {
 						IModelElement landmarkElement = (IModelElement) o;
 						if (landmarkElement.exists()) {
 							if (landmarkElement instanceof IMember
 									&& !landmark.getInterest().isPropagated()) {
 								searchElements.add(((IMember) landmarkElement)
 										.getSourceModule());
 							} else if (landmarkElement instanceof ISourceModule) {
 								searchElements.add(landmarkElement);
 							}
 						}
 					}
 				}
 			}
 		} else if (degreeOfSeparation == 2) {
 			// for (IInteractionElement interesting : interestingElements) {
 			for (Iterator it = interestingElements.iterator(); it.hasNext();) {
 				IInteractionElement interesting = (IInteractionElement) it
 						.next();
 				AbstractContextStructureBridge bridge = ContextCorePlugin
 						.getDefault().getStructureBridge(
 								interesting.getContentType());
 				if (includeNodeInScope(interesting, bridge)) {
 					Object object = bridge.getObjectForHandle(interesting
 							.getHandleIdentifier());
 					if (object instanceof IModelElement) {
 						IModelElement interestingElement = (IModelElement) object;
 						if (interestingElement.exists()) {
 							if (interestingElement instanceof IMember
 									&& !interesting.getInterest()
 											.isPropagated()) {
 								searchElements
 										.add(((IMember) interestingElement)
 												.getSourceModule());
 							} else if (interestingElement instanceof ISourceModule) {
 								searchElements.add(interestingElement);
 							}
 						}
 					}
 				}
 			}
 		} else if (degreeOfSeparation == 3 || degreeOfSeparation == 4) {
 			// for (IInteractionElement interesting : interestingElements) {
 			for (Iterator it = interestingElements.iterator(); it.hasNext();) {
 				IInteractionElement interesting = (IInteractionElement) it
 						.next();
 
 				AbstractContextStructureBridge bridge = ContextCorePlugin
 						.getDefault().getStructureBridge(
 								interesting.getContentType());
 				if (includeNodeInScope(interesting, bridge)) {
 					IResource resource = ResourcesUiBridgePlugin.getDefault()
 							.getResourceForElement(interesting, true);
 					if (resource != null) {
 						IProject project = resource.getProject();
 
 						// if (project != null &&
 						// DLTKProject.hasScriptNature(project) &&
 						// project.exists()) {
 
 						if (project != null && project.exists()) {
 							IScriptProject scriptProject = DLTKCore
 									.create(project);// ((IModelElement)o).
 							if (scriptProject != null && scriptProject.exists())
 								searchElements.add(scriptProject);
 						}
 					}
 				}
 			}
 			if (degreeOfSeparation == 4) {
 
 				includeMask = IDLTKSearchScope.SOURCES
 						| IDLTKSearchScope.APPLICATION_LIBRARIES
 						| IDLTKSearchScope.SYSTEM_LIBRARIES;
 			}
 		} else if (degreeOfSeparation == 5) {
 			return SearchEngine.createWorkspaceScope(null);
 		}
 
 		if (searchElements.size() == 0) {
 			return null;
 		} else {
 			IModelElement[] elements = new IModelElement[searchElements.size()];
 			int j = 0;
 			// for (IModelElement searchElement : searchElements) {
 			for (Iterator it = searchElements.iterator(); it.hasNext();) {
 				IModelElement searchElement = (IModelElement) it.next();
 				elements[j] = searchElement;
 				j++;
 			}
 
 			return SearchEngine.createSearchScope(elements, includeMask, null);
 		}
 	}
 
 	/**
 	 * Only include Script elements and files.
 	 */
 	private boolean includeNodeInScope(IInteractionElement interesting,
 			AbstractContextStructureBridge bridge2) {
 		if (interesting == null || bridge2 == null) {
 			return false;
 		} else {
 			if (interesting.getContentType() == null) {
 				// TODO: remove
 				MylynStatusHandler.log("null content type for: "
 						+ interesting.getHandleIdentifier(), this);
 				return false;
 			} else {
 				return interesting.getContentType().equals(
 						fBridge.contentType)
 						|| bridge2.isDocument(interesting.getHandleIdentifier());
 			}
 		}
 	}
 
 	protected boolean acceptResultElement(IModelElement element) {
 		return true;
 	}
 
 	protected boolean acceptElement(IModelElement modelElement) {
 		return modelElement != null
 				&& (modelElement instanceof IMember || modelElement instanceof IType);
 	}
 
 	private void runJob(final IInteractionElement node,
 			final int degreeOfSeparation, final String kind) {
 
 		int limitTo = 0;
 		if (kind.equals(DLTKReferencesProvider.ID)) {
 			limitTo = IDLTKSearchConstants.REFERENCES;
 		} else if (kind.equals(DLTKImplementorsProvider.ID)) {
 			// limitTo = IDLTKSearchConstants.IMPLEMENTORS;
 		} else if (kind.equals(DLTKTestingReferencesProvider.ID)) {
 			limitTo = IDLTKSearchConstants.REFERENCES;
 		} else if (kind.equals(DLTKReadAccessProvider.ID)) {
 			limitTo = IDLTKSearchConstants.REFERENCES;
 		} else if (kind.equals(DLTKWriteAccessProvider.ID)) {
 			limitTo = IDLTKSearchConstants.REFERENCES;
 		}
 
 		final DLTKSearchOperation query = (DLTKSearchOperation) getSearchOperation(
 				node, limitTo, degreeOfSeparation);
 		if (query == null)
 			return;
 
 		DLTKSearchJob job = new DLTKSearchJob(query.getLabel(), query);
 		query.addListener(new IActiveSearchListener() {
 
 			private boolean gathered = false;
 
 			public boolean resultsGathered() {
 				return gathered;
 			}
 
 			public void searchCompleted(List l) {
 				if (l == null)
 					return;
 				List relatedHandles = new ArrayList();
 				Object[] elements = l.toArray();
 				for (int i = 0; i < elements.length; i++) {
 					if (elements[i] instanceof IModelElement)
 						relatedHandles.add((IModelElement) elements[i]);
 				}
 
 				// for (IModelElement element : relatedHandles) {
 				for (ListIterator it = relatedHandles.listIterator(); it
 						.hasNext();) {
 					IModelElement element = (IModelElement) it.next();
 					if (!acceptResultElement(element))
 						continue;
 					incrementInterest(node, fBridge.contentType,
 							element.getHandleIdentifier(), degreeOfSeparation);
 				}
 				gathered = true;
 				AbstractDLTKRelationProvider.this.searchCompleted(node);
 			}
 
 		});
 		InternalSearchUI.getInstance();
 
 		runningJobs.add(job);
 		job.setPriority(Job.DECORATE - 10);
 		job.schedule();
 	}
 
 	public IActiveSearchOperation getSearchOperation(IInteractionElement node,
 			int limitTo, int degreeOfSeparation) {
 		IModelElement modelElement = DLTKCore
 				.create(node.getHandleIdentifier());
 		if (modelElement == null || !modelElement.exists())
 			return null;
 
 		IDLTKSearchScope scope = createSearchScope(modelElement,
 				degreeOfSeparation);
 
 		if (scope == null)
 			return null;
 
 		QuerySpecification specs = new ElementQuerySpecification(modelElement,
 				limitTo, scope, "Mylyn degree of separation: "
 						+ degreeOfSeparation);
 
 		return new DLTKSearchOperation(specs);
 	}
 
 	protected static class DLTKSearchJob extends Job {
 
 		private DLTKSearchOperation op;
 
 		public DLTKSearchJob(String name, DLTKSearchOperation op) {
 			super(name);
 			this.op = op;
 		}
 
 		/**
 		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
 		 */
 
 		protected IStatus run(IProgressMonitor monitor) {
 			return op.run(monitor);
 		}
 
 	}
 
 	protected static class DLTKSearchOperation extends DLTKSearchQuery
 			implements IActiveSearchOperation {
 		private ISearchResult result = null;
 
 		public ISearchResult getSearchResult() {
 			if (result == null)
 				result = new DLTKSearchResult(this);
 			new DLTKActiveSearchResultUpdater((DLTKSearchResult) result);
 			return result;
 		}
 
 		public IStatus run(IProgressMonitor monitor) {
 			try {
 				IStatus runStatus = super.run(monitor);
 				ISearchResult result = getSearchResult();
 				if (result instanceof DLTKSearchResult) {
 					// TODO make better
 					Object[] objs = ((DLTKSearchResult) result).getElements();
 					if (objs == null) {
 						notifySearchCompleted(null);
 					} else {
 						List l = new ArrayList();
 						for (int i = 0; i < objs.length; i++) {
 							l.add(objs[i]);
 						}
 						notifySearchCompleted(l);
 					}
 				}
 				return runStatus;
 			} catch (ConcurrentModificationException cme) {
 				MylynStatusHandler.log(cme, "script search failed");
 			} catch (Throwable t) {
 				MylynStatusHandler.log(t, "script search failed");
 			}
 
 			IStatus status = new Status(IStatus.WARNING,
					MylynDLTKPlugin.PLUGIN_ID, IStatus.OK,
 					"could not run Script search", null);
 			notifySearchCompleted(null);
 			return status;
 		}
 
 		/**
 		 * Constructor
 		 * 
 		 * @param data
 		 */
 		public DLTKSearchOperation(QuerySpecification data) {
 			super(data);
 
 		}
 
 		/** List of listeners wanting to know about the searches */
 		private List listeners = new ArrayList();
 
 		/**
 		 * Add a listener for when the bugzilla search is completed
 		 * 
 		 * @param l
 		 *            The listener to add
 		 */
 		public void addListener(IActiveSearchListener l) {
 			// add the listener to the list
 			listeners.add(l);
 		}
 
 		/**
 		 * Remove a listener for when the bugzilla search is completed
 		 * 
 		 * @param l
 		 *            The listener to remove
 		 */
 		public void removeListener(IActiveSearchListener l) {
 			// remove the listener from the list
 			listeners.remove(l);
 		}
 
 		/**
 		 * Notify all of the listeners that the bugzilla search is completed
 		 * 
 		 * @param doiList
 		 *            A list of BugzillaSearchHitDoiInfo
 		 * @param member
 		 *            The IMember that the search was performed on
 		 */
 		public void notifySearchCompleted(List l) {
 			// go through all of the listeners and call
 			// searchCompleted(colelctor,
 			// member)
 			// for (IActiveSearchListener listener : listeners) {
 			for (ListIterator it = listeners.listIterator(); it.hasNext();) {
 				IActiveSearchListener listener = (IActiveSearchListener) it
 						.next();
 				listener.searchCompleted(l);
 			}
 		}
 
 	}
 
 	public void stopAllRunningJobs() {
 		// for (Job j : runningJobs) {
 		for (ListIterator it = runningJobs.listIterator(); it.hasNext();) {
 			Job j = (Job) it.next();
 			j.cancel();
 		}
 		runningJobs.clear();
 	}
 
 	protected int getDefaultDegreeOfSeparation() {
 		return DEFAULT_DEGREE;
 	}
 }
