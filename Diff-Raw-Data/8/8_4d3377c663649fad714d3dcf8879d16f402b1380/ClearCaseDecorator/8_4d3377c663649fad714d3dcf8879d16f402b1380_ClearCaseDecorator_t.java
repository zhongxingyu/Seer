 /*******************************************************************************
  * Copyright (c) 2002, 2004 eclipse-ccase.sourceforge.net.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     Matthew Conway - initial API and implementation
  *     IBM Corporation - concepts and ideas taken from Eclipse code
  *     Gunnar Wagenknecht - reworked to Eclipse 3.0 API and code clean-up
  *******************************************************************************/
 package net.sourceforge.eclipseccase.ui;
 
 import java.util.HashSet;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IResource;
 
 import java.util.*;
 import net.sourceforge.eclipseccase.*;
 import net.sourceforge.eclipseccase.ui.preferences.ClearCaseUIPreferences;
 import org.eclipse.core.resources.*;
 import org.eclipse.core.runtime.*;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.viewers.*;
 import org.eclipse.swt.graphics.ImageData;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.team.ui.ISharedImages;
 import org.eclipse.team.ui.TeamImages;
 
 /**
  * The ClearCase label decorator.
  */
 public class ClearCaseDecorator extends LabelProvider implements ILightweightLabelDecorator, IResourceStateListener, IResourceChangeListener {
 
 	/** trace if */
 	private static final String DECORATOR = "ClearCaseDecorator"; //$NON-NLS-1$
 
 	private Set<IResource> shownResources = new HashSet<IResource>();
 
 	/*
 	 * Define a cached image descriptor which only creates the image data once
 	 */
 	public static class CachedImageDescriptor extends ImageDescriptor {
 
 		ImageData data;
 
 		ImageDescriptor descriptor;
 
 		public CachedImageDescriptor(ImageDescriptor descriptor) {
 			if (null == descriptor)
 				throw new IllegalArgumentException("Image descriptor must not be null"); //$NON-NLS-1$
 			this.descriptor = descriptor;
 		}
 
 		@Override
 		public ImageData getImageData() {
 			if (data == null) {
 				data = descriptor.getImageData();
 			}
 			return data;
 		}
 	}
 
 	/** used to exit the resource visitor for calculating the dirty state */
 	static final CoreException CORE_DIRTY_EXCEPTION = new CoreException(new Status(IStatus.OK, "dirty", 1, "", null)); //$NON-NLS-1$ //$NON-NLS-2$
 
 	/** used to exit the resource visitor for calculating the dirty state */
 	static final CoreException CORE_UNKNOWN_EXCEPTION = new CoreException(new Status(IStatus.OK, "unknown", 1, "", null)); //$NON-NLS-1$ //$NON-NLS-2$
 
 	/** the decorator id */
 	public static final String ID = "net.sourceforge.eclipseccase.ui.decorator"; //$NON-NLS-1$
 
 	private static ImageDescriptor IMG_DESC_CHECKED_IN;
 
 	private static ImageDescriptor IMG_DESC_CHECKED_OUT;
 
 	// Images cached for better performance
 	private static ImageDescriptor IMG_DESC_DIRTY;
 
 	private static ImageDescriptor IMG_DESC_EDITED;
 
 	private static ImageDescriptor IMG_DESC_HIJACKED;
 
 	private static ImageDescriptor IMG_DESC_LINK;
 
 	private static ImageDescriptor IMG_DESC_LINK_WARNING;
 
 	private static ImageDescriptor IMG_DESC_NEW_RESOURCE;
 
 	private static ImageDescriptor IMG_DESC_UNKNOWN_STATE;
 
 	private static ImageDescriptor IMG_DESC_DERIVED_OBJECT;
 
 	private static ImageDescriptor IMG_DESC_ELEMENT_BG;
 
 	/** internal state constant */
 	private static final int STATE_CLEAN = 0;
 
 	/** internal state constant */
 	private static final int STATE_DIRTY = 1;
 
 	/** internal state constant */
 	private static final int STATE_UNKNOWN = 2;
 
 	static {
 		IMG_DESC_DIRTY = new CachedImageDescriptor(TeamImages.getImageDescriptor(ISharedImages.IMG_DIRTY_OVR));
 		IMG_DESC_CHECKED_IN = new CachedImageDescriptor(TeamImages.getImageDescriptor(ISharedImages.IMG_CHECKEDIN_OVR));
 		IMG_DESC_CHECKED_OUT = new CachedImageDescriptor(ClearCaseImages.getImageDescriptor(ClearCaseImages.IMG_CHECKEDOUT_OVR));
 		IMG_DESC_NEW_RESOURCE = new CachedImageDescriptor(ClearCaseImages.getImageDescriptor(ClearCaseImages.IMG_QUESTIONABLE_OVR));
 		IMG_DESC_EDITED = new CachedImageDescriptor(ClearCaseImages.getImageDescriptor(ClearCaseImages.IMG_EDITED_OVR));
 		IMG_DESC_UNKNOWN_STATE = new CachedImageDescriptor(ClearCaseImages.getImageDescriptor(ClearCaseImages.IMG_UNKNOWN_OVR));
 		IMG_DESC_DERIVED_OBJECT = new CachedImageDescriptor(ClearCaseImages.getImageDescriptor(ClearCaseImages.IMG_DERIVEDOBJECT_OVR));
 		IMG_DESC_LINK = new CachedImageDescriptor(ClearCaseImages.getImageDescriptor(ClearCaseImages.IMG_LINK_OVR));
 		IMG_DESC_LINK_WARNING = new CachedImageDescriptor(ClearCaseImages.getImageDescriptor(ClearCaseImages.IMG_LINK_WARNING_OVR));
 		IMG_DESC_HIJACKED = new CachedImageDescriptor(ClearCaseImages.getImageDescriptor(ClearCaseImages.IMG_HIJACKED_OVR));
 		IMG_DESC_ELEMENT_BG = new CachedImageDescriptor(ClearCaseImages.getImageDescriptor(ClearCaseImages.IMG_ELEMENT_BG));
 	}
 
 	/**
 	 * Detects the dirty state of the specified resource
 	 * 
 	 * @param resource
 	 * @return the dirty state of the specified resource
 	 */
 	private static int calculateDirtyState(IResource resource) {
 		/*
 		 * Since dirty == checkout/hijacked for files, redundant to show files
 		 * as dirty; we also need to filter out obsolete resources (removed due
 		 * to pending background jobs)
 		 */
 		if (resource.getType() == IResource.FILE || !resource.isAccessible() || resource.getLocation() == null)
 			return STATE_CLEAN;
 
 		// don't need to calculate if deep decoration is disabled
 		if (!ClearCaseUIPreferences.decorateFoldersDirty())
 			return STATE_CLEAN;
 
 		// cache some settings (visitor performance)
 		final boolean decorateNew = ClearCaseUIPreferences.decorateFoldersContainingViewPrivateElementsDirty() && (ClearCaseUIPreferences.decorateViewPrivateElements() || (ClearCaseUIPreferences.decorateElementStatesWithTextPrefix() && ClearCaseUI.getTextPrefixNew().length() > 0));
 		final boolean decorateUnknown = ClearCaseUIPreferences.decorateUnknownElements() || (ClearCaseUIPreferences.decorateElementStatesWithTextPrefix() && ClearCaseUI.getTextPrefixUnknown().length() > 0);
 		final boolean decorateHijacked = ClearCaseUIPreferences.decorateHijackedElements() || (ClearCaseUIPreferences.decorateElementStatesWithTextPrefix() && ClearCaseUI.getTextPrefixHijacked().length() > 0);
 		try {
 			// visit all children to determine the state
 			resource.accept(new IResourceVisitor() {
 
 				/*
 				 * (non-Javadoc)
 				 * 
 				 * @see
 				 * org.eclipse.core.resources.IResourceVisitor#visit(org.eclipse
 				 * .core.resources.IResource)
 				 */
 				public boolean visit(IResource childResource) throws CoreException {
 					// the provider of the child resource
 					ClearCaseProvider p = ClearCaseProvider.getClearCaseProvider(childResource);
 
 					// sanity check
 					if (p == null || !childResource.isAccessible())
 						return false;
 
 					// ignore some resources
 					if (p.isIgnored(childResource))
 						return false;
 
 					// test if unknown
 					if (decorateUnknown && p.isUnknownState(childResource)) {
 						if (ClearCaseUI.DEBUG_DECORATION) {
 							ClearCaseUI.trace(DECORATOR, "  is dirty: child with unknown state"); //$NON-NLS-1$
 						}
 						throw CORE_UNKNOWN_EXCEPTION;
 					}
 
 					// test if new
 					if (decorateNew && !p.isClearCaseElement(childResource)) {
 						if (ClearCaseUI.DEBUG_DECORATION) {
 							ClearCaseUI.trace(DECORATOR, "  is dirty: view-priv child"); //$NON-NLS-1$
 						}
 						throw CORE_DIRTY_EXCEPTION;
 					}
 
 					// test if hijacked
 					if (decorateHijacked && p.isHijacked(childResource)) {
 						if (ClearCaseUI.DEBUG_DECORATION) {
 							ClearCaseUI.trace(DECORATOR, "  is dirty: hijacked child"); //$NON-NLS-1$
 						}
 						throw CORE_DIRTY_EXCEPTION;
 					}
 
 					// test if checked out
 					if (p.isCheckedOut(childResource)) {
 						if (ClearCaseUI.DEBUG_DECORATION) {
 							ClearCaseUI.trace(DECORATOR, "  is dirty: child is checked out"); //$NON-NLS-1$
 						}
 						throw CORE_DIRTY_EXCEPTION;
 					}
 
 					// go into children
 					return true;
 				}
 			}, IResource.DEPTH_INFINITE, true);
 		} catch (CoreException e) {
 			// if our exception was caught, we know there's a dirty child
 			if (e == CORE_DIRTY_EXCEPTION)
 				return STATE_DIRTY;
 			else if (e == CORE_UNKNOWN_EXCEPTION)
 				return STATE_UNKNOWN;
 			else {
 				// should not occure
 				handleException(e);
 			}
 		}
 		return STATE_CLEAN;
 	}
 
 	/**
 	 * Adds decoration for checked in state.
 	 * 
 	 * @param decoration
 	 */
 	private static void decorateCheckedIn(IDecoration decoration) {
 		if (ClearCaseUI.DEBUG_DECORATION) {
 			ClearCaseUI.trace(DECORATOR, "  decorateCheckedIn"); //$NON-NLS-1$
 		}
 		if (ClearCaseUIPreferences.decorateClearCaseElements())
 			decoration.addOverlay(IMG_DESC_ELEMENT_BG, IDecoration.TOP_LEFT + IDecoration.UNDERLAY);
 		decoration.addOverlay(IMG_DESC_CHECKED_IN);
 	}
 
 	/**
 	 * Adds decoration for checked out state.
 	 * 
 	 * @param decoration
 	 */
 	private static void decorateCheckedOut(IDecoration decoration, String version) {
 		if (ClearCaseUI.DEBUG_DECORATION) {
 			ClearCaseUI.trace(DECORATOR, "  decorateCheckedOut"); //$NON-NLS-1$
 		}
 		if (ClearCaseUIPreferences.decorateClearCaseElements())
 			decoration.addOverlay(IMG_DESC_ELEMENT_BG, IDecoration.TOP_LEFT + IDecoration.UNDERLAY);
 		decoration.addOverlay(IMG_DESC_CHECKED_OUT);
 		if (ClearCaseUIPreferences.decorateElementStatesWithTextPrefix()) {
 			decoration.addPrefix(ClearCaseUI.getTextPrefixDirty());
 		}
 
 		if (ClearCaseUIPreferences.decorateElementsWithVersionInfo() && null != version) {
 			decoration.addSuffix("  " + version); //$NON-NLS-1$
 		}
 	}
 
 	/**
 	 * Adds decoration for dirty state.
 	 * 
 	 * @param decoration
 	 */
 	private static void decorateDirty(IDecoration decoration) {
 		if (ClearCaseUI.DEBUG_DECORATION) {
 			ClearCaseUI.trace(DECORATOR, "  decorateDirty"); //$NON-NLS-1$
 		}
 		if (ClearCaseUIPreferences.decorateClearCaseElements())
 			decoration.addOverlay(IMG_DESC_ELEMENT_BG, IDecoration.TOP_LEFT + IDecoration.UNDERLAY);
 		decoration.addOverlay(IMG_DESC_DIRTY);
 		if (ClearCaseUIPreferences.decorateElementStatesWithTextPrefix()) {
 			decoration.addPrefix(ClearCaseUI.getTextPrefixDirty());
 		}
 	}
 
 	/**
 	 * Adds decoration for edited state.
 	 * 
 	 * @param decoration
 	 */
 	private static void decorateEdited(IDecoration decoration) {
 		if (ClearCaseUI.DEBUG_DECORATION) {
 			ClearCaseUI.trace(DECORATOR, "  decorateEdited"); //$NON-NLS-1$
 		}
 		if (ClearCaseUIPreferences.decorateClearCaseElements())
 			decoration.addOverlay(IMG_DESC_ELEMENT_BG, IDecoration.TOP_LEFT + IDecoration.UNDERLAY);
 		if (ClearCaseUIPreferences.decorateEditedElements()) {
 			decoration.addOverlay(IMG_DESC_EDITED);
 		}
 		if (ClearCaseUIPreferences.decorateElementStatesWithTextPrefix()) {
 			decoration.addPrefix(ClearCaseUI.getTextPrefixEdited());
 		}
 	}
 
 	/**
 	 * Adds decoration for hijaced state.
 	 * 
 	 * @param decoration
 	 */
 	private static void decorateHijacked(IDecoration decoration, String version) {
 		if (ClearCaseUI.DEBUG_DECORATION) {
 			ClearCaseUI.trace(DECORATOR, "  decorateHijacked"); //$NON-NLS-1$
 		}
 		if (ClearCaseUIPreferences.decorateClearCaseElements())
 			decoration.addOverlay(IMG_DESC_ELEMENT_BG, IDecoration.TOP_LEFT + IDecoration.UNDERLAY);
 		if (ClearCaseUIPreferences.decorateHijackedElements()) {
 			decoration.addOverlay(IMG_DESC_HIJACKED);
 		}
 		if (ClearCaseUIPreferences.decorateElementStatesWithTextPrefix()) {
 			decoration.addPrefix(ClearCaseUI.getTextPrefixHijacked());
 		}
 		if (ClearCaseUIPreferences.decorateElementsWithVersionInfo() && null != version) {
 			decoration.addSuffix("  " + version); //$NON-NLS-1$
 		}
 	}
 
 	/**
 	 * Adds decoration for links.
 	 * 
 	 * @param decoration
 	 * @param isLinkTargetCheckedOut
 	 */
 	private static void decorateLink(IDecoration decoration, String linkTarget, boolean isValidLinkTarget, boolean isLinkTargetCheckedOut) {
 		if (ClearCaseUI.DEBUG_DECORATION) {
 			ClearCaseUI.trace(DECORATOR, "  decorateLink"); //$NON-NLS-1$
 		}
 		if (ClearCaseUIPreferences.decorateClearCaseElements())
 			decoration.addOverlay(IMG_DESC_ELEMENT_BG, IDecoration.TOP_LEFT + IDecoration.UNDERLAY);
 		if (isLinkTargetCheckedOut) {
 			decoration.addOverlay(IMG_DESC_CHECKED_OUT);
 		} else if (isValidLinkTarget) {
 			decoration.addOverlay(IMG_DESC_LINK);
 		} else {
 			decoration.addOverlay(IMG_DESC_LINK_WARNING);
 		}
 
 		decoration.addSuffix(" --> " + linkTarget); //$NON-NLS-1$
 	}
 
 	/**
 	 * Adds decoration for derived objects.
 	 * 
 	 * @param decoration
 	 * @param isLinkTargetCheckedOut
 	 */
 	private static void decorateDerivedObject(IDecoration decoration, String version) {
 		if (ClearCaseUI.DEBUG_DECORATION) {
 			ClearCaseUI.trace(DECORATOR, "  decorateDerivedObject"); //$NON-NLS-1$
 		}
 		if (ClearCaseUIPreferences.decorateDerivedObjects()) {
 			decoration.addOverlay(IMG_DESC_DERIVED_OBJECT);
 		}
 	}
 
 	/**
 	 * Adds decoration for new state.
 	 * 
 	 * @param decoration
 	 */
 	private static void decorateNew(IDecoration decoration) {
 		if (ClearCaseUI.DEBUG_DECORATION) {
 			ClearCaseUI.trace(DECORATOR, "  decorateNew"); //$NON-NLS-1$
 		}
 		if (ClearCaseUIPreferences.decorateViewPrivateElements()) {
 			decoration.addOverlay(IMG_DESC_NEW_RESOURCE);
 		}
 		if (ClearCaseUIPreferences.decorateElementStatesWithTextPrefix()) {
 			decoration.addPrefix(ClearCaseUI.getTextPrefixNew());
 		}
 	}
 
 	/**
 	 * Adds decoration for unknown state.
 	 * 
 	 * @param decoration
 	 */
 	private static void decorateUnknown(IDecoration decoration) {
 		if (ClearCaseUI.DEBUG_DECORATION) {
 			ClearCaseUI.trace(DECORATOR, "  decorateUnknown"); //$NON-NLS-1$
 		}
 		if (ClearCaseUIPreferences.decorateUnknownElements()) {
 			decoration.addOverlay(IMG_DESC_UNKNOWN_STATE);
 		}
 		if (ClearCaseUIPreferences.decorateElementStatesWithTextPrefix()) {
 			decoration.addPrefix(ClearCaseUI.getTextPrefixUnknown());
 		}
 	}
 
 	/**
 	 * Adds decoration for the version.
 	 * 
 	 * @param decoration
 	 */
 	private static void decorateVersion(IDecoration decoration, String version) {
 		if (ClearCaseUI.DEBUG_DECORATION) {
 			ClearCaseUI.trace(DECORATOR, "  decorateVersion"); //$NON-NLS-1$
 		}
 		if (ClearCaseUIPreferences.decorateElementsWithVersionInfo() && null != version) {
 			decoration.addSuffix("  " + version); //$NON-NLS-1$
 		}
 	}
 
 	/**
 	 * Adds decoration for the view name.
 	 * 
 	 * @param decoration
 	 */
 	private static void decorateViewName(IDecoration decoration, String viewName) {
 		if (ClearCaseUI.DEBUG_DECORATION) {
 			ClearCaseUI.trace(DECORATOR, "  decorateViewName"); //$NON-NLS-1$
 		}
 		if (ClearCaseUIPreferences.decorateProjectsWithViewInfo() && null != viewName) {
 			decoration.addSuffix(" [" + viewName + "]"); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 	}
 
 	/**
 	 * Returns the resource for the given input object, or null if there is no
 	 * resource associated with it.
 	 * 
 	 * @param object
 	 *            the object to find the resource for
 	 * @return the resource for the given object, or null
 	 */
 	private static IResource getResource(Object object) {
 		if (object instanceof IResource)
 			return (IResource) object;
 		if (object instanceof IAdaptable)
 			return (IResource) ((IAdaptable) object).getAdapter(IResource.class);
 		return null;
 	}
 
 	/**
 	 * Handles the specified exception
 	 * 
 	 * @param e
 	 */
 	private static void handleException(CoreException e) {
 		ClearCasePlugin.log(IStatus.ERROR, Messages.getString("ClearCaseDecorator.error.exception") //$NON-NLS-1$
 				+ e.getMessage(), e);
 	}
 
 	/**
 	 * Creates a new instance.
 	 */
 	public ClearCaseDecorator() {
 		super();
 		StateCacheFactory.getInstance().addStateChangeListerer(this);
 		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
 		if (ClearCaseUI.DEBUG_DECORATION) {
 			ClearCaseUI.trace(DECORATOR, "activated"); //$NON-NLS-1$
 		}
 	}
 
 	/**
 	 * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#decorate(java.lang.Object,
 	 *      org.eclipse.jface.viewers.IDecoration)
 	 */
 	public void decorate(Object element, IDecoration decoration) {
 
 		// don't do anything until the state cache is ready
 		if (!StateCacheFactory.getInstance().isInitialized())
 			return;
 
 		IResource resource = getResource(element);
 
 		// sanity check
 		if (resource == null || resource.getType() == IResource.ROOT || resource.getLocation() == null || !resource.isAccessible())
 			return;
 
 		if (ClearCaseUI.DEBUG_DECORATION) {
 			ClearCaseUI.trace(DECORATOR, "decorating " + resource.getFullPath().toString()); //$NON-NLS-1$
 		}
 		synchronized (shownResources) {
 			// remember that we have seen this resource. Doesn't matter if we
 			// handle it or not (that is, whether it is already in a CC
 			// project). This list is used later during the Associate or
 			// Dissociate actions.
 			shownResources.add(resource);
 		}
 
 		// get our provider
 		ClearCaseProvider p = ClearCaseProvider.getClearCaseProvider(resource);
 		if (p == null) {
 			if (ClearCaseUI.DEBUG_DECORATION) {
 				ClearCaseUI.trace(DECORATOR, "  no ClearCaseProvider"); //$NON-NLS-1$
 			}
 			return;
 		}
 
 		// test if ignored. Caution: p.isIgnored() implicitely schedules an
 		// async StateCache update if this resource is not ignored and not
 		// yet initialized
		if (!(resource instanceof IProject) && p.isIgnored(resource)) {
 			if (ClearCaseUI.DEBUG_DECORATION) {
 				ClearCaseUI.trace(DECORATOR, "  ignored"); //$NON-NLS-1$
 			}
 			return;
 		}
 
 		// test if uninitialized before all other checks
 		if (p.isUnknownState(resource)) {
 
 			// p.isIgnored() already initiated an async update in this case
 			if (ClearCaseUI.DEBUG_DECORATION) {
 				ClearCaseUI.trace(DECORATOR, " scheduled refresh for " + resource.getFullPath().toString()); //$NON-NLS-1$
 			}
 
 			// unknown state
 			decorateUnknown(decoration);
 
 			// no further decoration
 			return;
 		}
 
 		// Projects may be the view directory containing the VOBS, if so,
 		// they are not decoratable
 		if (p.isViewRoot(resource) || p.isVobRoot(resource)) {
 			if (ClearCaseUI.DEBUG_DECORATION) {
				ClearCaseUI.trace(DECORATOR, "  view or vob root ?"); //$NON-NLS-1$
 			}
			// return; // FIXME: no return because view tag decoration needed
 		}
 
 		// decorate view tag for projects
 		if (resource.getType() == IResource.PROJECT) {
 			decorateViewName(decoration, ClearCaseProvider.getViewName(resource));
 		}
 
 		// performance optimisation: get the StateCache only once:
 		StateCache cache = p.getCache(resource);
 
 		/*
 		 * test the different states
 		 */
 		if (resource.getType() != IResource.PROJECT && !cache.isClearCaseElement()) {
 			// decorate new elements not added to ClearCase
 			decorateNew(decoration);
 
 			// no further decoration
 			return;
 		} else if (cache.isSymbolicLink()) {
 			// symbolic link
 			decorateLink(decoration, cache.getSymbolicLinkTarget(), cache.isSymbolicLinkTargetValid(), cache.isCheckedOut());
 
 			// no further decoration
 			return;
 		} else if (cache.isCheckedOut()) {
 			// check out
 			decorateCheckedOut(decoration, cache.getVersion());
 
 			// no further decoration
 			return;
 		} else if (cache.isDerivedObject()) {
 			decorateDerivedObject(decoration, cache.getVersion());
 			// no further decoration
 			return;
 		} else if (cache.isHijacked()) {
 			// hijacked
 			decorateHijacked(decoration, cache.getVersion());
 
 			// no further decoration
 			return;
 		} else {
 			// calculate the state
 			int dirty = calculateDirtyState(resource);
 
 			switch (dirty) {
 			case STATE_CLEAN:
 				if (cache.isClearCaseElement()) {
 					if (cache.isEdited()) {
 						// the resource is edited by someone else
 						decorateEdited(decoration);
 					} else {
 						// at this point, we assume everything is ok
 						decorateCheckedIn(decoration);
 					}
 					// add version info only at this point
 					decorateVersion(decoration, p.getVersion(resource));
 				}
 				return;
 
 			case STATE_DIRTY:
 				// dirty
 				decorateDirty(decoration);
 				return;
 
 			case STATE_UNKNOWN:
 				// unknown
 				decorateUnknown(decoration);
 				return;
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
 	 */
 	@Override
 	public void dispose() {
 		if (ClearCaseUI.DEBUG_DECORATION) {
 			ClearCaseUI.trace(DECORATOR, "disposed"); //$NON-NLS-1$
 		}
 		StateCacheFactory.getInstance().removeStateChangeListerer(this);
 		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
 		super.dispose();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.jface.viewers.LabelProvider#fireLabelProviderChanged(org.
 	 * eclipse.jface.viewers.LabelProviderChangedEvent)
 	 */
 	@Override
 	protected void fireLabelProviderChanged(final LabelProviderChangedEvent event) {
 		// delegate to UI thread
 		Display display = ClearCaseUI.getDisplay();
 		if (null != display && !display.isDisposed()) {
 			display.asyncExec(new Runnable() {
 
 				public void run() {
 					superFireLabelProviderChanged(event);
 				}
 			});
 		}
 	}
 
 	/**
 	 * Updates all decorators on any resource.
 	 */
 	public void refresh() {
 		if (ClearCaseUI.DEBUG_DECORATION) {
 			ClearCaseUI.trace(DECORATOR, "  fireLabelProviderChanged,0,all"); //$NON-NLS-1$
 		}
 		fireLabelProviderChanged(new LabelProviderChangedEvent(this));
 	}
 
 	/**
 	 * Update the decorators for every resource in project. Used when
 	 * Associating/Deassociate project.
 	 * 
 	 * @param project
 	 */
 	public void refresh(IProject project) {
 		final List<IResource> resources = new ArrayList<IResource>(3000);
 		try {
 			project.accept(new IResourceVisitor() {
 				public boolean visit(IResource resource) {
 					resources.add(resource);
 					return true;
 				}
 			});
 			if (ClearCaseUI.DEBUG_DECORATION) {
 				ClearCaseUI.trace(DECORATOR, "  fireLabelProviderChanged,1, cnt=" + resources.size()); //$NON-NLS-1$
 			}
 			fireLabelProviderChanged(new LabelProviderChangedEvent(this, resources.toArray()));
 		} catch (CoreException e) {
 			handleException(e);
 		}
 	}
 
 	public void refresh(IResource[] changedResources) {
 		Set<IResource> resourcesToUpdate = new HashSet<IResource>();
 
 		for (int i = 0; i < changedResources.length; i++) {
 			IResource resource = changedResources[i];
 
 			if (ClearCaseUIPreferences.decorateFoldersDirty()) {
 				addWithParents(resource, resourcesToUpdate);
 			} else {
 				resourcesToUpdate.add(resource);
 			}
 		}
 
 		if (ClearCaseUI.DEBUG_DECORATION) {
 			ClearCaseUI.trace(DECORATOR, "  fireLabelProviderChanged,2, cnt=" + resourcesToUpdate.size()); //$NON-NLS-1$
 		}
 		fireLabelProviderChanged(new LabelProviderChangedEvent(this, resourcesToUpdate.toArray()));
 
 	}
 
 	/*
 	 * Add resource and its parents to the List
 	 */
 
 	private void addWithParents(IResource resource, Set<IResource> resources) {
 		IResource current = resource;
 
 		while (current.getType() != IResource.ROOT) {
 			resources.add(current);
 			current = current.getParent();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * net.sourceforge.eclipseccase.IResourceStateListener#stateChanged(net.
 	 * sourceforge.eclipseccase.StateCache)
 	 */
 	public void resourceStateChanged(IResource[] resources) {
 		refresh(resources);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org
 	 * .eclipse.core.resources.IResourceChangeEvent)
 	 */
 	public void resourceChanged(IResourceChangeEvent event) {
 
 		// get the delta
 		IResourceDelta rootDelta = event.getDelta();
 
 		// process delta
 		if (null != rootDelta) {
 
 			final Set<IContainer> resources = new HashSet<IContainer>();
 
 			try {
 				// recursive
 				rootDelta.accept(new IResourceDeltaVisitor() {
 
 					public boolean visit(IResourceDelta delta) throws CoreException {
 						switch (delta.getKind()) {
 
 						case IResourceDelta.ADDED:
 						case IResourceDelta.REMOVED:
 							// if resource was added or removed
 							if (ClearCaseUIPreferences.decorateFoldersDirty()) {
 								// refresh parent if deep decoration is enabled
 								resources.add(delta.getResource().getParent());
 							}
 							return false;
 
 						}
 						return true;
 					}
 				});
 
 			} catch (CoreException e) {
 				// ignore
 			}
 
 			if (!resources.isEmpty()) {
 				refresh(resources.toArray(new IResource[resources.size()]));
 			}
 		}
 	}
 
 	/**
 	 * Delegates the event to the super class for firing.
 	 * 
 	 * @param event
 	 */
 	final void superFireLabelProviderChanged(LabelProviderChangedEvent event) {
 		super.fireLabelProviderChanged(event);
 	}
 
 	// TODO:Testing to see if this will help take into
 	// account changes in workspace not within eclipse.
 	/**
 	 * 
 	 * @param resource
 	 * @param depth
 	 */
 	@SuppressWarnings("unused")
 	private void synchronizeResource(IResource resource, int depth) {
 		if (!resource.isSynchronized(depth)) {
 			try {
 				resource.refreshLocal(depth, null);
 			} catch (CoreException e) {
 				throw new RuntimeException("Problem refreshing resource " + resource.getLocation(), e);
 			}
 		}
 	}
 
 	/**
 	 * Return a list of all resources from a given project that were shown in
 	 * one of the navigators before and requested decoration. That list can be
 	 * used to refresh the decorators.
 	 * 
 	 * @param project
 	 *            which project to search
 	 * @return array of all resources that requested decoration
 	 */
 	public IResource[] getShownResources(IProject project) {
 		Vector<IResource> result = new Vector<IResource>();
 		synchronized (shownResources) {
 			for (IResource resource : shownResources) {
 				if (resource.getProject().equals(project)) {
 					result.add(resource);
 				}
 			}
 		}
 		return result.toArray(new IResource[result.size()]);
 	}
 }
