 /*******************************************************************************
  * Copyright (c) 2012 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.ide.ui.internal.structuremergeviewer;
 
 import static com.google.common.collect.Lists.newArrayList;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.Collection;
 import java.util.EventObject;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.compare.CompareConfiguration;
 import org.eclipse.compare.CompareViewerSwitchingPane;
 import org.eclipse.compare.ITypedElement;
 import org.eclipse.compare.structuremergeviewer.DiffTreeViewer;
 import org.eclipse.compare.structuremergeviewer.ICompareInput;
 import org.eclipse.compare.structuremergeviewer.ICompareInputChangeListener;
 import org.eclipse.compare.structuremergeviewer.IDiffElement;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.emf.common.command.Command;
 import org.eclipse.emf.common.command.CommandStackListener;
 import org.eclipse.emf.common.notify.Adapter;
 import org.eclipse.emf.common.util.BasicMonitor;
 import org.eclipse.emf.compare.Comparison;
 import org.eclipse.emf.compare.EMFCompare;
 import org.eclipse.emf.compare.Match;
import org.eclipse.emf.compare.command.ICompareCopyCommand;
 import org.eclipse.emf.compare.domain.ICompareEditingDomain;
 import org.eclipse.emf.compare.domain.impl.EMFCompareEditingDomain;
 import org.eclipse.emf.compare.ide.EMFCompareIDE;
 import org.eclipse.emf.compare.ide.ui.internal.EMFCompareConstants;
 import org.eclipse.emf.compare.ide.ui.internal.EMFCompareIDEUIPlugin;
 import org.eclipse.emf.compare.ide.ui.internal.actions.filter.DifferenceFilter;
 import org.eclipse.emf.compare.ide.ui.internal.actions.filter.FilterActionMenu;
 import org.eclipse.emf.compare.ide.ui.internal.actions.group.DifferenceGrouper;
 import org.eclipse.emf.compare.ide.ui.internal.actions.group.GroupActionMenu;
 import org.eclipse.emf.compare.ide.ui.internal.actions.save.SaveComparisonModelAction;
 import org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.util.CompareConfigurationExtension;
 import org.eclipse.emf.compare.ide.ui.internal.editor.ComparisonScopeInput;
 import org.eclipse.emf.compare.ide.ui.internal.structuremergeviewer.provider.ComparisonNode;
 import org.eclipse.emf.compare.ide.ui.logical.EMFSynchronizationModel;
 import org.eclipse.emf.compare.scope.IComparisonScope;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
 import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory;
 import org.eclipse.emf.edit.provider.resource.ResourceItemProviderAdapterFactory;
 import org.eclipse.jface.action.ToolBarManager;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.viewers.IElementComparer;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.swt.custom.BusyIndicator;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 
 /**
  * @author <a href="mailto:mikael.barbero@obeo.fr">Mikael Barbero</a>
  */
 public class EMFCompareStructureMergeViewer extends DiffTreeViewer implements CommandStackListener {
 
 	private final ICompareInputChangeListener fCompareInputChangeListener;
 
 	private final ComposedAdapterFactory fAdapterFactory;
 
 	private final CompareViewerSwitchingPane fParent;
 
 	private Object fRoot;
 
 	private ICompareEditingDomain editingDomain;
 
 	/**
 	 * The difference filter that will be applied to the structure viewer. Note that this will be initialized
 	 * from {@link #createToolItems(ToolBarManager)} since that method is called from the super-constructor
 	 * and we cannot init ourselves beforehand.
 	 */
 	private DifferenceFilter differenceFilter;
 
 	/**
 	 * This will be used by our adapter factory in order to group together the differences located under the
 	 * Comparison. Note that this will be initialized from {@link #createToolItems(ToolBarManager)} since that
 	 * method is called from the super-constructor and we cannot init ourselves beforehand.
 	 */
 	private DifferenceGrouper differenceGrouper;
 
 	/**
 	 * @param parent
 	 * @param configuration
 	 */
 	public EMFCompareStructureMergeViewer(Composite parent, CompareConfiguration configuration) {
 		super(parent, configuration);
 
 		fAdapterFactory = new ComposedAdapterFactory(ComposedAdapterFactory.Descriptor.Registry.INSTANCE);
 		fAdapterFactory.addAdapterFactory(new ReflectiveItemProviderAdapterFactory());
 		fAdapterFactory.addAdapterFactory(new ResourceItemProviderAdapterFactory());
 
 		boolean leftIsLocal = CompareConfigurationExtension.getBoolean(configuration, "LEFT_IS_LOCAL", false); //$NON-NLS-1$
 		setLabelProvider(new EMFCompareStructureMergeViewerLabelProvider(fAdapterFactory, this, leftIsLocal));
 		setContentProvider(new EMFCompareStructureMergeViewerContentProvider(fAdapterFactory,
 				differenceGrouper));
 
 		if (parent instanceof CompareViewerSwitchingPane) {
 			fParent = (CompareViewerSwitchingPane)parent;
 		} else {
 			fParent = null;
 		}
 
 		fCompareInputChangeListener = new ICompareInputChangeListener() {
 			public void compareInputChanged(ICompareInput input) {
 				EMFCompareStructureMergeViewer.this.compareInputChanged(input);
 			}
 		};
 
 		// Wrap the defined comparer in our own.
 		setComparer(new DiffNodeComparer(super.getComparer()));
 	}
 
 	/**
 	 * Triggered by fCompareInputChangeListener
 	 */
 	void compareInputChanged(ICompareInput input) {
 		if (input == null) {
 			// When closing, we don't need a progress monitor to handle the input change
 			compareInputChanged((ICompareInput)null, new NullProgressMonitor());
 			return;
 		}
 		CompareConfiguration cc = getCompareConfiguration();
 		// The compare configuration is nulled when the viewer is disposed
 		if (cc != null) {
 			BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
 				public void run() {
 					try {
 						inputChangedTask.run(new NullProgressMonitor());
 					} catch (InvocationTargetException e) {
 						EMFCompareIDEUIPlugin.getDefault().log(e.getTargetException());
 					} catch (InterruptedException e) {
 						// Ignore
 					}
 				}
 			});
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.viewers.StructuredViewer#getRoot()
 	 */
 	@Override
 	protected Object getRoot() {
 		return fRoot;
 	}
 
 	private IRunnableWithProgress inputChangedTask = new IRunnableWithProgress() {
 		public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
 			monitor.beginTask("Computing Structure Differences", 100); //$NON-NLS-1$
 			compareInputChanged((ICompareInput)getInput(), new SubProgressMonitor(monitor, 100));
 			monitor.done();
 		}
 	};
 
 	void compareInputChanged(ICompareInput input, IProgressMonitor monitor) {
 		if (input != null) {
 			if (input instanceof ComparisonNode) {
 				compareInputChanged((ComparisonNode)input, monitor);
 			} else if (input instanceof ComparisonScopeInput) {
 				compareInputChanged((ComparisonScopeInput)input, monitor);
 			} else {
 				final ITypedElement left = input.getLeft();
 				final ITypedElement right = input.getRight();
 				final ITypedElement origin = input.getAncestor();
 				final EMFSynchronizationModel syncModel = EMFSynchronizationModel.createSynchronizationModel(
 						left, right, origin);
 
 				// Double check : git allows modification of the index file ... but we cannot
 				final CompareConfiguration config = getCompareConfiguration();
 				if (!syncModel.isLeftEditable()) {
 					config.setLeftEditable(false);
 				}
 				if (!syncModel.isRightEditable()) {
 					config.setRightEditable(false);
 				}
 
 				final IComparisonScope scope = syncModel.createMinimizedScope();
 				final Comparison compareResult = EMFCompareIDE.builder().build().compare(scope,
 						BasicMonitor.toMonitor(monitor));
 
 				final ResourceSet leftResourceSet = (ResourceSet)scope.getLeft();
 				final ResourceSet rightResourceSet = (ResourceSet)scope.getRight();
 				final ResourceSet originResourceSet = (ResourceSet)scope.getOrigin();
 
 				editingDomain = (ICompareEditingDomain)getCompareConfiguration().getProperty(
 						EMFCompareConstants.EDITING_DOMAIN);
 				if (editingDomain == null) {
 					editingDomain = EMFCompareEditingDomain.create(leftResourceSet, rightResourceSet,
 							originResourceSet);
 					getCompareConfiguration().setProperty(EMFCompareConstants.EDITING_DOMAIN, editingDomain);
 				}
 
 				editingDomain.getCommandStack().addCommandStackListener(this);
 
 				compareInputChanged(compareResult);
 			}
 		} else {
 			ResourceSet leftResourceSet = null;
 			ResourceSet rightResourceSet = null;
 			ResourceSet originResourceSet = null;
 
 			if (fRoot != null) {
 				Comparison comparison = (Comparison)((Adapter)fRoot).getTarget();
 				Iterator<Match> matchIt = comparison.getMatches().iterator();
 				if (comparison.isThreeWay()) {
 					while (matchIt.hasNext()
 							&& (leftResourceSet == null || rightResourceSet == null || originResourceSet == null)) {
 						Match match = matchIt.next();
 						if (leftResourceSet == null) {
 							leftResourceSet = getResourceSet(match.getLeft());
 						}
 						if (rightResourceSet == null) {
 							rightResourceSet = getResourceSet(match.getRight());
 						}
 						if (originResourceSet == null) {
 							originResourceSet = getResourceSet(match.getOrigin());
 						}
 					}
 				} else {
 					while (matchIt.hasNext() && (leftResourceSet == null || rightResourceSet == null)) {
 						Match match = matchIt.next();
 						if (leftResourceSet == null) {
 							leftResourceSet = getResourceSet(match.getLeft());
 						}
 						if (rightResourceSet == null) {
 							rightResourceSet = getResourceSet(match.getRight());
 						}
 					}
 				}
 			}
 
 			if (editingDomain != null) {
 				editingDomain.getCommandStack().removeCommandStackListener(this);
 				getCompareConfiguration().setProperty(EMFCompareConstants.EDITING_DOMAIN, null);
 				editingDomain = null;
 			}
 
 			// FIXME: should unload only if input.getLeft/Right/Ancestor (previously stored in field) are
 			// instanceof ResourceNode
 			unload(leftResourceSet);
 			unload(rightResourceSet);
 			unload(originResourceSet);
 
 			getCompareConfiguration().setProperty(EMFCompareConstants.COMPARE_RESULT, null);
 			fRoot = null;
 		}
 	}
 
 	void compareInputChanged(ComparisonNode input, IProgressMonitor monitor) {
 		editingDomain = (ICompareEditingDomain)getCompareConfiguration().getProperty(
 				EMFCompareConstants.EDITING_DOMAIN);
 		editingDomain.getCommandStack().addCommandStackListener(this);
 
 		compareInputChanged(input.getTarget());
 	}
 
 	void compareInputChanged(ComparisonScopeInput input, IProgressMonitor monitor) {
 		editingDomain = (ICompareEditingDomain)getCompareConfiguration().getProperty(
 				EMFCompareConstants.EDITING_DOMAIN);
 		editingDomain.getCommandStack().addCommandStackListener(this);
 
 		EMFCompare comparator = (EMFCompare)getCompareConfiguration().getProperty(
 				EMFCompareConstants.COMPARATOR);
 		Comparison comparison = comparator.compare(input.getComparisonScope(), BasicMonitor
 				.toMonitor(monitor));
 		compareInputChanged(comparison);
 	}
 
 	private static void unload(ResourceSet resourceSet) {
 		if (resourceSet != null) {
 			for (Resource resource : resourceSet.getResources()) {
 				resource.unload();
 			}
 			resourceSet.getResources().clear();
 		}
 	}
 
 	private static ResourceSet getResourceSet(EObject eObject) {
 		if (eObject != null) {
 			Resource eResource = eObject.eResource();
 			if (eResource != null) {
 				return eResource.getResourceSet();
 			}
 		}
 		return null;
 	}
 
 	void compareInputChanged(final Comparison comparison) {
 		getCompareConfiguration().setProperty(EMFCompareConstants.COMPARE_RESULT, comparison);
 		fRoot = fAdapterFactory.adapt(comparison, IDiffElement.class);
 
 		getCompareConfiguration().getContainer().runAsynchronously(new IRunnableWithProgress() {
 			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
 				String message = null;
 				if (comparison.getDifferences().isEmpty()) {
 					message = "No Differences"; //$NON-NLS-1$
 				}
 
 				if (Display.getCurrent() != null) {
 					refreshAfterDiff(message, fRoot);
 				} else {
 					final String theMessage = message;
 					Display.getDefault().asyncExec(new Runnable() {
 						public void run() {
 							refreshAfterDiff(theMessage, fRoot);
 						}
 					});
 				}
 			}
 		});
 	}
 
 	private void refreshAfterDiff(String message, Object root) {
 		if (getControl().isDisposed()) {
 			return;
 		}
 
 		if (fParent != null) {
 			fParent.setTitleArgument(message);
 		}
 
 		refresh(root);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.viewers.StructuredViewer#setComparer(org.eclipse.jface.viewers.IElementComparer)
 	 */
 	@Override
 	public void setComparer(IElementComparer comparer) {
 		// Wrap this new comparer in our own
 		super.setComparer(new DiffNodeComparer(comparer));
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.structuremergeviewer.DiffTreeViewer#createToolItems(org.eclipse.jface.action.ToolBarManager)
 	 */
 	@Override
 	protected void createToolItems(ToolBarManager toolbarManager) {
 		super.createToolItems(toolbarManager);
 
 		toolbarManager.add(new SaveComparisonModelAction(getCompareConfiguration()));
 		toolbarManager.add(new GroupActionMenu(getDifferenceGrouper()));
 		toolbarManager.add(new FilterActionMenu(getDifferenceFilter()));
 	}
 
 	/**
 	 * Returns the difference filter that is to be applied on the structure viewer.
 	 * <p>
 	 * Note that this will be called from {@link #createToolItems(ToolBarManager)}, which is called from the
 	 * super-constructor, when we have had no time to initialize the {@link #differenceFilter} field.
 	 * </p>
 	 * 
 	 * @return The difference filter that is to be applied on the structure viewer.
 	 */
 	protected DifferenceFilter getDifferenceFilter() {
 		if (differenceFilter == null) {
 			differenceFilter = new DifferenceFilter();
 			differenceFilter.install(this);
 		}
 		return differenceFilter;
 	}
 
 	/**
 	 * Returns the difference grouper that is to be applied on the structure viewer.
 	 * <p>
 	 * Note that this will be called from {@link #createToolItems(ToolBarManager)}, which is called from the
 	 * super-constructor, when we have had no time to initialize the {@link #differenceGrouper} field.
 	 * </p>
 	 * 
 	 * @return The difference grouper that is to be applied on the structure viewer.
 	 */
 	protected DifferenceGrouper getDifferenceGrouper() {
 		if (differenceGrouper == null) {
 			differenceGrouper = new DifferenceGrouper();
 			differenceGrouper.install(this);
 		}
 		return differenceGrouper;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.structuremergeviewer.DiffTreeViewer#inputChanged(java.lang.Object,
 	 *      java.lang.Object)
 	 */
 	@Override
 	protected void inputChanged(Object input, Object oldInput) {
 		if (oldInput instanceof ICompareInput) {
 			ICompareInput old = (ICompareInput)oldInput;
 			old.removeCompareInputChangeListener(fCompareInputChangeListener);
 		}
 		if (input instanceof ICompareInput) {
 			ICompareInput ci = (ICompareInput)input;
 			ci.addCompareInputChangeListener(fCompareInputChangeListener);
 			compareInputChanged(ci);
 			if (input != oldInput) {
 				initialSelection();
 			}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.compare.structuremergeviewer.DiffTreeViewer#handleDispose(org.eclipse.swt.events.DisposeEvent)
 	 */
 	@Override
 	protected void handleDispose(DisposeEvent event) {
 		Object input = getInput();
 		if (input instanceof ICompareInput) {
 			ICompareInput ci = (ICompareInput)input;
 			ci.removeCompareInputChangeListener(fCompareInputChangeListener);
 		}
 		compareInputChanged((ICompareInput)null);
 		fAdapterFactory.dispose();
 
 		super.handleDispose(event);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.common.command.CommandStackListener#commandStackChanged(java.util.EventObject)
 	 */
 	public void commandStackChanged(EventObject event) {
 		Command mostRecentCommand = editingDomain.getCommandStack().getMostRecentCommand();
		if (mostRecentCommand instanceof ICompareCopyCommand) {
 			Collection<?> affectedObjects = mostRecentCommand.getAffectedObjects();
 			refresh(true);
 			if (!affectedObjects.isEmpty()) {
 				List<Object> adaptedAffectedObject = newArrayList();
 				for (Object affectedObject : affectedObjects) {
 					adaptedAffectedObject.add(fAdapterFactory.adapt(affectedObject, IDiffElement.class));
 				}
 				setSelection(new StructuredSelection(adaptedAffectedObject), true);
 			}
 		} else {
 			// FIXME, should recompute the difference, something happened outside of this compare editor
 		}
 	}
 }
