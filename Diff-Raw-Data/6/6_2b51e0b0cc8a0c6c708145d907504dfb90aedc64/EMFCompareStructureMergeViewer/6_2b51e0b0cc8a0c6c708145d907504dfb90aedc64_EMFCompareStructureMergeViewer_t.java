 /*******************************************************************************
  * Copyright (c) 2013 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.ide.ui.internal.structuremergeviewer;
 
 import static com.google.common.collect.Iterables.getFirst;
 
 import java.lang.reflect.Field;
 import java.security.AccessController;
 import java.security.PrivilegedAction;
 import java.util.Collection;
 import java.util.EventObject;
 import java.util.Iterator;
 
 import org.eclipse.compare.CompareConfiguration;
 import org.eclipse.compare.INavigatable;
 import org.eclipse.compare.ITypedElement;
 import org.eclipse.compare.internal.CompareHandlerService;
 import org.eclipse.compare.structuremergeviewer.ICompareInput;
 import org.eclipse.compare.structuremergeviewer.ICompareInputChangeListener;
 import org.eclipse.core.resources.IStorage;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.SubMonitor;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.emf.common.command.Command;
 import org.eclipse.emf.common.command.CommandStack;
 import org.eclipse.emf.common.command.CommandStackListener;
 import org.eclipse.emf.common.util.BasicMonitor;
 import org.eclipse.emf.compare.Comparison;
 import org.eclipse.emf.compare.EMFCompare;
 import org.eclipse.emf.compare.Match;
 import org.eclipse.emf.compare.command.ICompareCopyCommand;
 import org.eclipse.emf.compare.domain.ICompareEditingDomain;
 import org.eclipse.emf.compare.domain.impl.EMFCompareEditingDomain;
 import org.eclipse.emf.compare.ide.ui.internal.EMFCompareIDEUIPlugin;
 import org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.util.RedoAction;
 import org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.util.UndoAction;
 import org.eclipse.emf.compare.ide.ui.internal.editor.ComparisonScopeInput;
 import org.eclipse.emf.compare.ide.ui.internal.logical.ComparisonScopeBuilder;
 import org.eclipse.emf.compare.ide.ui.internal.logical.IdenticalResourceMinimizer;
 import org.eclipse.emf.compare.ide.ui.internal.logical.StreamAccessorStorage;
 import org.eclipse.emf.compare.ide.ui.internal.logical.SubscriberStorageAccessor;
 import org.eclipse.emf.compare.ide.ui.internal.util.PlatformElementUtil;
 import org.eclipse.emf.compare.ide.ui.internal.util.SWTUtil;
 import org.eclipse.emf.compare.ide.ui.logical.IModelResolver;
 import org.eclipse.emf.compare.ide.ui.logical.IStorageProviderAccessor;
 import org.eclipse.emf.compare.rcp.EMFCompareRCPPlugin;
 import org.eclipse.emf.compare.rcp.ui.internal.EMFCompareConstants;
 import org.eclipse.emf.compare.rcp.ui.internal.structuremergeviewer.groups.IDifferenceGroupProvider;
 import org.eclipse.emf.compare.scope.IComparisonScope;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
 import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory;
 import org.eclipse.emf.edit.provider.resource.ResourceItemProviderAdapterFactory;
 import org.eclipse.emf.edit.tree.TreeFactory;
 import org.eclipse.emf.edit.tree.TreeNode;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.jface.viewers.ITreeViewerListener;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TreeExpansionEvent;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.TreeItem;
 import org.eclipse.team.core.subscribers.Subscriber;
 import org.eclipse.team.core.subscribers.SubscriberMergeContext;
 import org.eclipse.team.internal.ui.mapping.ModelCompareEditorInput;
 import org.eclipse.team.ui.synchronize.ISynchronizeParticipant;
 import org.eclipse.team.ui.synchronize.ModelSynchronizeParticipant;
 import org.eclipse.ui.actions.ActionFactory;
 
 /**
  * Implementation of {@link AbstractViewerWrapper}.
  * 
  * @author <a href="mailto:axel.richard@obeo.fr">Axel Richard</a>
  */
 public class EMFCompareStructureMergeViewer extends AbstractViewerWrapper implements CommandStackListener {
 
 	/** The width of the tree ruler. */
 	private static final int TREE_RULER_WIDTH = 17;
 
 	/** The adapter factory. */
 	private ComposedAdapterFactory fAdapterFactory;
 
 	/** The tree ruler associated with this viewer. */
 	private EMFCompareDiffTreeRuler treeRuler;
 
 	private ICompareInputChangeListener fCompareInputChangeListener;
 
 	/** The expand/collapse item listener. */
 	private ITreeViewerListener fWrappedTreeListener;
 
 	/** The compare configuration property change listener. */
 	private IPropertyChangeListener fCompareConfigurationPropertyChangeListener;
 
 	/** The tree viewer. */
 	private EMFCompareDiffTreeViewer diffTreeViewer;
 
 	/** The undo action. */
 	private UndoAction undoAction;
 
 	/** The redo action. */
 	private RedoAction redoAction;
 
 	/** The compare handler service. */
 	private CompareHandlerService fHandlerService;
 
 	/**
 	 * When comparing EObjects from a resource, the resource involved doesn't need to be unload by EMF
 	 * Compare.
 	 */
 	private boolean resourcesShouldBeUnload;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param parent
 	 *            the SWT parent control under which to create the viewer's SWT control.
 	 * @param config
 	 *            a compare configuration the newly created viewer might want to use.
 	 */
 	public EMFCompareStructureMergeViewer(Composite parent, CompareConfiguration config) {
 		super(parent, config);
		inputChangedTask.setPriority(Job.LONG);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see 
 	 *      org.eclipse.emf.compare.ide.ui.internal.structuremergeviewer.ViewerWrapper.createControl(Composite,
 	 *      CompareConfiguration)
 	 */
 	@Override
 	protected Control createControl(Composite parent, CompareConfiguration config) {
 		Composite control = new Composite(parent, SWT.NONE);
 
 		fAdapterFactory = new ComposedAdapterFactory(EMFCompareRCPPlugin.getDefault()
 				.getAdapterFactoryRegistry());
 
 		fAdapterFactory.addAdapterFactory(new ReflectiveItemProviderAdapterFactory());
 		fAdapterFactory.addAdapterFactory(new ResourceItemProviderAdapterFactory());
 		config.setProperty(EMFCompareConstants.COMPOSED_ADAPTER_FACTORY, fAdapterFactory);
 
 		GridLayout layout = new GridLayout(2, false);
 		layout.marginWidth = 0;
 		layout.marginHeight = 0;
 		layout.horizontalSpacing = 0;
 		layout.verticalSpacing = 0;
 		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
 		control.setLayout(layout);
 		control.setLayoutData(data);
 		diffTreeViewer = new EMFCompareDiffTreeViewer(control, fAdapterFactory, config);
 		setViewer(diffTreeViewer);
 		control.setData(INavigatable.NAVIGATOR_PROPERTY, diffTreeViewer.getControl().getData(
 				INavigatable.NAVIGATOR_PROPERTY));
 		diffTreeViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, false, true);
 		layoutData.widthHint = TREE_RULER_WIDTH;
 		layoutData.minimumWidth = TREE_RULER_WIDTH;
 		treeRuler = new EMFCompareDiffTreeRuler(control, SWT.NONE, layoutData.widthHint, diffTreeViewer,
 				config);
 		treeRuler.setLayoutData(layoutData);
 
 		fCompareInputChangeListener = new ICompareInputChangeListener() {
 			public void compareInputChanged(ICompareInput input) {
 				EMFCompareStructureMergeViewer.this.compareInputChanged(input);
 			}
 		};
 
 		fWrappedTreeListener = new ITreeViewerListener() {
 
 			public void treeExpanded(TreeExpansionEvent event) {
 				treeRuler.redraw();
 			}
 
 			public void treeCollapsed(TreeExpansionEvent event) {
 				treeRuler.redraw();
 			}
 		};
 		diffTreeViewer.addTreeListener(fWrappedTreeListener);
 
 		fCompareConfigurationPropertyChangeListener = new IPropertyChangeListener() {
 			public void propertyChange(PropertyChangeEvent event) {
 				if (EMFCompareConstants.MERGE_WAY.equals(event.getProperty()) && event.getNewValue() != null) {
 					diffTreeViewer.configurationPropertyChanged();
 					treeRuler.computeConsequences();
 					treeRuler.redraw();
 				} else if (EMFCompareConstants.SELECTED_FILTERS.equals(event.getProperty())
 						&& event.getNewValue() != null) {
 					diffTreeViewer.createChildrenSilently(diffTreeViewer.getTree());
 					treeRuler.computeConsequences();
 					treeRuler.redraw();
 				} else if (EMFCompareConstants.SELECTED_GROUP.equals(event.getProperty())
 						&& event.getNewValue() != null) {
 					diffTreeViewer.createChildrenSilently(diffTreeViewer.getTree());
 					treeRuler.computeConsequences();
 					treeRuler.redraw();
 				}
 			}
 		};
 		getCompareConfiguration().addPropertyChangeListener(fCompareConfigurationPropertyChangeListener);
 
 		fHandlerService = CompareHandlerService.createFor(getCompareConfiguration().getContainer(),
 				diffTreeViewer.getControl().getShell());
 
 		return control;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.viewers.Viewer#fireSelectionChanged(SelectionChangedEvent)
 	 */
 	@Override
 	protected void fireSelectionChanged(SelectionChangedEvent event) {
 		super.fireSelectionChanged(event);
 		treeRuler.selectionChanged(event);
 		treeRuler.redraw();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.viewers.Viewer#inputChanged(Object, Object)
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
 
 			// Hack to display a message in the tree viewer while the differences are being computed.
 			TreeItem item = new TreeItem(diffTreeViewer.getTree(), SWT.NONE);
 			item.setText("Computing model differences...");
 
 			compareInputChanged(ci);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.ide.ui.internal.structuremergeviewer.AbstractViewerWrapper#handleDispose(DisposeEvent)
 	 */
 	@Override
 	protected void handleDispose(DisposeEvent event) {
 		if (fHandlerService != null) {
 			fHandlerService.dispose();
 		}
 		getCompareConfiguration().removePropertyChangeListener(fCompareConfigurationPropertyChangeListener);
 		diffTreeViewer.removeTreeListener(fWrappedTreeListener);
 		Object input = getInput();
 		if (input instanceof ICompareInput) {
 			ICompareInput ci = (ICompareInput)input;
 			ci.removeCompareInputChangeListener(fCompareInputChangeListener);
 		}
 		compareInputChanged((ICompareInput)null);
 		treeRuler.handleDispose();
 		fAdapterFactory.dispose();
 		super.handleDispose(event);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.common.command.CommandStackListener#commandStackChanged(java.util.EventObject)
 	 */
 	public void commandStackChanged(EventObject event) {
 		if (undoAction != null) {
 			undoAction.update();
 		}
 		if (redoAction != null) {
 			redoAction.update();
 		}
 
 		Command mostRecentCommand = ((CommandStack)event.getSource()).getMostRecentCommand();
 		if (mostRecentCommand instanceof ICompareCopyCommand) {
 			Collection<?> affectedObjects = mostRecentCommand.getAffectedObjects();
 
 			SWTUtil.safeAsyncExec(new Runnable() {
 				public void run() {
 					refresh(true);
 					diffTreeViewer.createChildrenSilently(diffTreeViewer.getTree());
 					treeRuler.computeConsequences();
 					treeRuler.redraw();
 				}
 			});
 			if (!affectedObjects.isEmpty()) {
 				// MUST NOT call a setSelection with a list, o.e.compare does not handle it (cf
 				// org.eclipse.compare.CompareEditorInput#getElement(ISelection))
 				Object first = getFirst(affectedObjects, null);
 				if (first instanceof EObject) {
 					TreeNode treeNode = TreeFactory.eINSTANCE.createTreeNode();
 					treeNode.setData((EObject)first);
 					final Object adaptedAffectedObject = fAdapterFactory.adapt(treeNode, ICompareInput.class);
 					SWTUtil.safeAsyncExec(new Runnable() {
 						public void run() {
 							setSelectionToWidget(new StructuredSelection(adaptedAffectedObject), true);
 						}
 					});
 				}
 			}
 		} else {
 			// FIXME, should recompute the difference, something happened outside of this compare editor
 		}
 
 	}
 
 	private Job inputChangedTask = new Job("Compute Model Differences") {
 		@Override
 		public IStatus run(IProgressMonitor monitor) {
 			SubMonitor subMonitor = SubMonitor.convert(monitor, "Computing Model Differences", 100);
 			compareInputChanged((ICompareInput)getInput(), subMonitor.newChild(100));
 			return Status.OK_STATUS;
 		}
 	};
 
 	/**
 	 * Triggered by fCompareInputChangeListener and {@link #inputChanged(Object, Object)}.
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
 			inputChangedTask.schedule();
 		}
 	}
 
 	void compareInputChanged(CompareInputAdapter input, IProgressMonitor monitor) {
 		ICompareEditingDomain editingDomain = (ICompareEditingDomain)getCompareConfiguration().getProperty(
 				EMFCompareConstants.EDITING_DOMAIN);
 		editingDomain.getCommandStack().addCommandStackListener(this);
 
 		compareInputChanged(null, (Comparison)input.getComparisonObject());
 	}
 
 	void compareInputChanged(ComparisonScopeInput input, IProgressMonitor monitor) {
 		ICompareEditingDomain editingDomain = (ICompareEditingDomain)getCompareConfiguration().getProperty(
 				EMFCompareConstants.EDITING_DOMAIN);
 		editingDomain.getCommandStack().addCommandStackListener(this);
 
 		EMFCompare comparator = (EMFCompare)getCompareConfiguration().getProperty(
 				EMFCompareConstants.COMPARATOR);
 
 		IComparisonScope comparisonScope = input.getComparisonScope();
 		Comparison comparison = comparator.compare(comparisonScope, BasicMonitor.toMonitor(monitor));
 		compareInputChanged(input.getComparisonScope(), comparison);
 	}
 
 	void compareInputChanged(final IComparisonScope scope, final Comparison comparison) {
 		if (!getControl().isDisposed()) { // guard against disposal
 			IDifferenceGroupProvider selectedGroup = (IDifferenceGroupProvider)getCompareConfiguration()
 					.getProperty(EMFCompareConstants.SELECTED_GROUP);
 			TreeNode treeNode = TreeFactory.eINSTANCE.createTreeNode();
 			treeNode.setData(comparison);
 			if (selectedGroup == null) {
 				selectedGroup = diffTreeViewer.getDefaultGroupProvider();
 			}
 			treeNode.eAdapters().add(selectedGroup);
 
 			diffTreeViewer.setRoot(fAdapterFactory.adapt(treeNode, ICompareInput.class));
 			getCompareConfiguration().setProperty(EMFCompareConstants.COMPARE_RESULT, comparison);
 
 			String message = null;
 			if (comparison.getDifferences().isEmpty()) {
 				message = "No Differences";
 			}
 
 			final String theMessage = message;
 			SWTUtil.safeAsyncExec(new Runnable() {
 				public void run() {
 					if (diffTreeViewer.getGroupActionMenu() != null) {
 						diffTreeViewer.getGroupActionMenu().createActions(scope, comparison);
 					}
 					if (diffTreeViewer.getFilterActionMenu() != null) {
 						diffTreeViewer.getFilterActionMenu().createActions(scope, comparison);
 					}
 					diffTreeViewer.refreshAfterDiff(theMessage, diffTreeViewer.getRoot());
 					// Mandatory for the EMFCompareDiffTreeRuler, all TreeItems must have been created
 					diffTreeViewer.createChildrenSilently(diffTreeViewer.getTree());
 					diffTreeViewer.initialSelection();
 				}
 			});
 
 			ICompareEditingDomain editingDomain = (ICompareEditingDomain)getCompareConfiguration()
 					.getProperty(EMFCompareConstants.EDITING_DOMAIN);
 
 			undoAction = new UndoAction(editingDomain);
 			redoAction = new RedoAction(editingDomain);
 
 			fHandlerService.setGlobalActionHandler(ActionFactory.UNDO.getId(), undoAction);
 			fHandlerService.setGlobalActionHandler(ActionFactory.REDO.getId(), redoAction);
 		}
 	}
 
 	void compareInputChanged(ICompareInput input, IProgressMonitor monitor) {
 		if (input != null) {
 			if (input instanceof CompareInputAdapter) {
 				resourcesShouldBeUnload = false;
 				compareInputChanged((CompareInputAdapter)input, monitor);
 			} else if (input instanceof ComparisonScopeInput) {
 				resourcesShouldBeUnload = false;
 				compareInputChanged((ComparisonScopeInput)input, monitor);
 			} else {
 				resourcesShouldBeUnload = true;
 				SubMonitor subMonitor = SubMonitor.convert(monitor, 100);
 
 				final ITypedElement left = input.getLeft();
 				final ITypedElement right = input.getRight();
 				final ITypedElement origin = input.getAncestor();
 
 				final IComparisonScope scope = buildComparisonScope(left, right, origin, subMonitor
 						.newChild(85));
 				final Comparison compareResult = EMFCompare
 						.builder()
 						.setMatchEngineFactoryRegistry(
 								EMFCompareRCPPlugin.getDefault().getMatchEngineFactoryRegistry())
 						.setPostProcessorRegistry(EMFCompareRCPPlugin.getDefault().getPostProcessorRegistry())
 						.build().compare(scope, BasicMonitor.toMonitor(subMonitor.newChild(15)));
 
 				final ResourceSet leftResourceSet = (ResourceSet)scope.getLeft();
 				final ResourceSet rightResourceSet = (ResourceSet)scope.getRight();
 				final ResourceSet originResourceSet = (ResourceSet)scope.getOrigin();
 
 				if (getCompareConfiguration() != null) {
 					ICompareEditingDomain editingDomain = (ICompareEditingDomain)getCompareConfiguration()
 							.getProperty(EMFCompareConstants.EDITING_DOMAIN);
 					if (editingDomain != null) {
 						editingDomain.getCommandStack().removeCommandStackListener(this);
 						editingDomain.dispose();
 					}
 
 					editingDomain = EMFCompareEditingDomain.create(leftResourceSet, rightResourceSet,
 							originResourceSet);
 					editingDomain.getCommandStack().addCommandStackListener(this);
 					getCompareConfiguration().setProperty(EMFCompareConstants.EDITING_DOMAIN, editingDomain);
 				}
 
 				compareInputChanged(scope, compareResult);
 			}
 		} else {
 			ResourceSet leftResourceSet = null;
 			ResourceSet rightResourceSet = null;
 			ResourceSet originResourceSet = null;
 
 			if (diffTreeViewer.getRoot() != null) {
 				Comparison comparison = (Comparison)((CompareInputAdapter)diffTreeViewer.getRoot())
 						.getComparisonObject();
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
 
 			ICompareEditingDomain editingDomain = (ICompareEditingDomain)getCompareConfiguration()
 					.getProperty(EMFCompareConstants.EDITING_DOMAIN);
 			if (editingDomain != null) {
 				editingDomain.getCommandStack().removeCommandStackListener(this);
 				getCompareConfiguration().setProperty(EMFCompareConstants.EDITING_DOMAIN, null);
 				editingDomain.dispose();
 				editingDomain = null;
 			}
 
 			if (resourcesShouldBeUnload) {
 				unload(leftResourceSet);
 				unload(rightResourceSet);
 				unload(originResourceSet);
 			}
 
 			if (getCompareConfiguration() != null) {
 				getCompareConfiguration().setProperty(EMFCompareConstants.COMPARE_RESULT, null);
 				getCompareConfiguration().setProperty(EMFCompareConstants.SELECTED_FILTERS, null);
 				getCompareConfiguration().setProperty(EMFCompareConstants.SELECTED_GROUP, null);
 				getCompareConfiguration().setProperty(EMFCompareConstants.MERGE_WAY, null);
 				getCompareConfiguration().setProperty(EMFCompareConstants.SMV_SELECTION, null);
 			}
 			diffTreeViewer.setRoot(null);
 		}
 	}
 
 	/**
 	 * Constructs the comparison scope corresponding to the given typed elements.
 	 * 
 	 * @param left
 	 *            Left of the compared elements.
 	 * @param right
 	 *            Right of the compared elements.
 	 * @param origin
 	 *            Common ancestor of the <code>left</code> and <code>right</code> compared elements.
 	 * @param monitor
 	 *            Monitor to report progress on.
 	 * @return The created comparison scope.
 	 */
 	private IComparisonScope buildComparisonScope(ITypedElement left, ITypedElement right,
 			ITypedElement origin, IProgressMonitor monitor) {
 		IStorageProviderAccessor storageAccessor = null;
 		if (getSubscriber() != null) {
 			storageAccessor = new SubscriberStorageAccessor(getSubscriber());
 		}
 		IStorage leftStorage = PlatformElementUtil.findFile(left);
 		if (leftStorage == null) {
 			leftStorage = StreamAccessorStorage.fromTypedElement(left);
 		}
 		IModelResolver resolver = EMFCompareIDEUIPlugin.getDefault().getModelResolverRegistry()
 				.getBestResolverFor(leftStorage);
 		final ComparisonScopeBuilder scopeBuilder = new ComparisonScopeBuilder(resolver,
 				new IdenticalResourceMinimizer(), storageAccessor);
 		return scopeBuilder.build(left, right, origin, monitor);
 	}
 
 	/**
 	 * Team left us with absolutely no way to determine whether our supplied input is the result of a
 	 * synchronization or not.
 	 * <p>
 	 * In order to properly resolve the logical model of the resource currently being compared we need to know
 	 * what "other" resources were part of its logical model, and we need to know the revisions of these
 	 * resources we are to load. All of this has already been computed by Team, but it would not let us know.
 	 * This method uses discouraged means to get around this "black box" locking from Team.
 	 * </p>
 	 * <p>
 	 * The basic need here is to retrieve the Subscriber from this point. We have a lot of accessible
 	 * variables, the two most important being the CompareConfiguration and ICompareInput... I could find no
 	 * way around the privileged access to the private ModelCompareEditorInput.participant field. There does
 	 * not seem to be any adapter (or Platform.getAdapterManager().getAdapter(...)) that would allow for this,
 	 * so I'm taking the long way 'round.
 	 * </p>
 	 * 
 	 * @return The subscriber used for this comparison if any could be found, <code>null</code> otherwise.
 	 */
 	@SuppressWarnings("restriction")
 	private Subscriber getSubscriber() {
 		if (getCompareConfiguration().getContainer() instanceof ModelCompareEditorInput) {
 			final ModelCompareEditorInput modelInput = (ModelCompareEditorInput)getCompareConfiguration()
 					.getContainer();
 			ISynchronizeParticipant participant = null;
 			try {
 				final Field field = ModelCompareEditorInput.class.getDeclaredField("participant"); //$NON-NLS-1$
 				AccessController.doPrivileged(new PrivilegedAction<Object>() {
 					public Object run() {
 						field.setAccessible(true);
 						return null;
 					}
 				});
 				participant = (ISynchronizeParticipant)field.get(modelInput);
 			} catch (NoSuchFieldException e) {
 				// Swallow this, this private field was there at least from 3.5 to 4.3
 			} catch (IllegalArgumentException e) {
 				// Cannot happen
 			} catch (IllegalAccessException e) {
 				// "Should" not happen, but ignore it anyway
 			}
 			if (participant instanceof ModelSynchronizeParticipant
 					&& ((ModelSynchronizeParticipant)participant).getContext() instanceof SubscriberMergeContext) {
 				return ((SubscriberMergeContext)((ModelSynchronizeParticipant)participant).getContext())
 						.getSubscriber();
 			}
 		}
 		return null;
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
 }
