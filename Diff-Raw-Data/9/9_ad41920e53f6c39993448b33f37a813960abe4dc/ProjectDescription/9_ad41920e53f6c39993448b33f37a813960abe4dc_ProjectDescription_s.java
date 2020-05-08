 /*******************************************************************************
  * Copyright (c) 2000, 2010 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *     Martin Oberhuber (Wind River) - [245937] setLinkLocation() detects non-change
  *     Serge Beauchamp (Freescale Semiconductor) - [229633] Project Path Variable Support
  * Markus Schorn (Wind River) - [306575] Save snapshot location with project
  * Broadcom Corporation - project variants and references
  *******************************************************************************/
 package org.eclipse.core.internal.resources;
 
 import java.net.URI;
 import java.util.*;
 import org.eclipse.core.filesystem.URIUtil;
 import org.eclipse.core.internal.events.BuildCommand;
 import org.eclipse.core.internal.utils.FileUtil;
 import org.eclipse.core.resources.*;
 import org.eclipse.core.runtime.*;
 
 public class ProjectDescription extends ModelObject implements IProjectDescription {
 	private static final ICommand[] EMPTY_COMMAND_ARRAY = new ICommand[0];
 	// constants
 	private static final IProject[] EMPTY_PROJECT_ARRAY = new IProject[0];
 	private static final IProjectVariantReference[] EMPTY_PROJECT_VARIANT_REFERENCE_ARRAY = new IProjectVariantReference[0];
 	private static final String[] EMPTY_STRING_ARRAY = new String[0];
 	private static final String EMPTY_STR = ""; //$NON-NLS-1$
 	private static final IProjectVariant[] DEFAULT_VARIANTS = new IProjectVariant[]{new ProjectVariant()};
 	protected static boolean isReading = false;
 
 	//flags to indicate when we are in the middle of reading or writing a
 	// workspace description
 	//these can be static because only one description can be read at once.
 	protected static boolean isWriting = false;
 	protected ICommand[] buildSpec = EMPTY_COMMAND_ARRAY;
 	/*
 	 * Cached union of static and dynamic references (duplicates omitted).
 	 * This cache is not persisted.
 	 */
 	protected HashMap/*<String, IProjectVariantReference[]>*/ cachedRefs = new HashMap();
 	/*
 	 * Cached union of static and dynamic project variant references (duplicates omitted).
 	 * This cache is not persisted.
 	 */
 	protected IProject[] cachedProjectRefs = null;
 	/*
 	 * Cached dynamic project references, generated from project variant references (duplicates omitted).
 	 * This cache is not persisted.
 	 */
 	protected  IProject[] cachedDynamicProjectRefs = null;
 	/*
 	 * Cached static project references, generated from project variant references (duplicates omitted).
 	 * This cache is not persisted.
 	 */
 	protected IProject[] cachedStaticProjectRefs = null;
 
 	protected String comment = EMPTY_STR;
 	
 	/**
 	 * Map of (IPath -> LinkDescription) pairs for each linked resource
 	 * in this project, where IPath is the project relative path of the resource.
 	 */
 	protected HashMap linkDescriptions = null;
 	
 	/**
 	 * Map of (IPath -> LinkedList<FilterDescription>) pairs for each filtered resource
 	 * in this project, where IPath is the project relative path of the resource.
 	 */
 	protected HashMap filterDescriptions = null;
 
 	/**
 	 * Map of (String -> VariableDescription) pairs for each variable in this
 	 * project, where String is the name of the variable.
 	 */
 	protected HashMap variableDescriptions = null;
 
 	// fields
 	protected URI location = null;
 	protected String[] natures = EMPTY_STRING_ARRAY;
 	protected IProjectVariant[] variants = DEFAULT_VARIANTS;
 	protected Set variantNames = null;
 	protected String activeVariant = null;
 	protected HashMap/*<String, IProjectVariantReference[]>*/ staticRefs = new HashMap();
 	protected HashMap/*<String, IProjectVariantReference[]>*/ dynamicRefs = new HashMap();
 	protected URI snapshotLocation= null;
 
 	public ProjectDescription() {
 		super();
 	}
 
 	public Object clone() {
 		ProjectDescription clone = (ProjectDescription) super.clone();
 		//don't want the clone to have access to our internal link locations table or builders
 		clone.linkDescriptions = null;
 		clone.filterDescriptions = null;
 		if (variableDescriptions != null)
 			clone.variableDescriptions = (HashMap) variableDescriptions.clone();
 		clone.staticRefs = (HashMap) staticRefs.clone();
 		clone.dynamicRefs = (HashMap) dynamicRefs.clone();
 		clone.cachedRefs = new HashMap();
 		clone.buildSpec = getBuildSpec(true);
 		return clone;
 	}
 
 	/* (non-Javadoc)
 	 * @see IProjectDescription#getBuildSpec()
 	 */
 	public ICommand[] getBuildSpec() {
 		return getBuildSpec(true);
 	}
 
 	public ICommand[] getBuildSpec(boolean makeCopy) {
 		//thread safety: copy reference in case of concurrent write
 		ICommand[] oldCommands = this.buildSpec;
 		if (oldCommands == null)
 			return EMPTY_COMMAND_ARRAY;
 		if (!makeCopy)
 			return oldCommands;
 		ICommand[] result = new ICommand[oldCommands.length];
 		for (int i = 0; i < result.length; i++)
 			result[i] = (ICommand) ((BuildCommand) oldCommands[i]).clone();
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see IProjectDescription#getComment()
 	 */
 	public String getComment() {
 		return comment;
 	}
 
 	/**
 	 * Returns the link location for the given resource name. Returns null if
 	 * no such link exists.
 	 */
 	public URI getLinkLocationURI(IPath aPath) {
 		if (linkDescriptions == null)
 			return null;
 		LinkDescription desc = (LinkDescription) linkDescriptions.get(aPath);
 		return desc == null ? null : desc.getLocationURI();
 	}
 
 	/**
 	 * Returns the filter for the given resource name. Returns null if
 	 * no such filter exists.
 	 */
 	synchronized public LinkedList/*<FilterDescription>*/ getFilter(IPath aPath) {
 		if (filterDescriptions == null)
 			return null;
 		return (LinkedList /*<FilterDescription> */) filterDescriptions.get(aPath);
 	}
 
 	/**
 	 * Returns the map of link descriptions (IPath (project relative path) -> LinkDescription).
 	 * Since this method is only used internally, it never creates a copy.
 	 * Returns null if the project does not have any linked resources.
 	 */
 	public HashMap getLinks() {
 		return linkDescriptions;
 	}
 
 	/**
 	 * Returns the map of filter descriptions (IPath (project relative path) -> LinkedList<FilterDescription>).
 	 * Since this method is only used internally, it never creates a copy.
 	 * Returns null if the project does not have any filtered resources.
 	 */
 	public HashMap getFilters() {
 		return filterDescriptions;
 	}
 
 	/**
 	 * Returns the map of variable descriptions (String (variable name) ->
 	 * VariableDescription). Since this method is only used internally, it never
 	 * creates a copy. Returns null if the project does not have any variables.
 	 */
 	public HashMap getVariables() {
 		return variableDescriptions;
 	}
 
 	/**
 	 * @see IProjectDescription#getLocation()
 	 * @deprecated
 	 */
 	public IPath getLocation() {
 		if (location == null)
 			return null;
 		return FileUtil.toPath(location);
 	}
 
 	/* (non-Javadoc)
 	 * @see IProjectDescription#getLocationURI()
 	 */
 	public URI getLocationURI() {
 		return location;
 	}
 
 	/* (non-Javadoc)
 	 * @see IProjectDescription#getNatureIds()
 	 */
 	public String[] getNatureIds() {
 		return getNatureIds(true);
 	}
 
 	public String[] getNatureIds(boolean makeCopy) {
 		if (natures == null)
 			return EMPTY_STRING_ARRAY;
 		return makeCopy ? (String[]) natures.clone() : natures;
 	}
 
 	/** 
 	 * Returns the URI to load a resource snapshot from.
 	 * May return <code>null</code> if no snapshot is set.
 	 * <p>
 	 * <strong>EXPERIMENTAL</strong>. This constant has been added as
 	 * part of a work in progress. There is no guarantee that this API will
 	 * work or that it will remain the same. Please do not use this API without
 	 * consulting with the Platform Core team.
 	 * </p>
 	 * @return the snapshot location URI,
 	 *   or <code>null</code>.
 	 * @see IProject#loadSnapshot(int, URI, IProgressMonitor)
 	 * @see #setSnapshotLocationURI(URI)
 	 * @since 3.6
 	 */
 	public URI getSnapshotLocationURI() {
 		return snapshotLocation;
 	}
 
 	/* (non-Javadoc)
 	 * @see IProjectDescription#hasNature(String)
 	 */
 	public boolean hasNature(String natureID) {
 		String[] natureIDs = getNatureIds(false);
 		for (int i = 0; i < natureIDs.length; ++i)
 			if (natureIDs[i].equals(natureID))
 				return true;
 		return false;
 	}
 
 	/**
 	 * Returns true if any private attributes of the description have changed.
 	 * Private attributes are those that are not stored in the project description
 	 * file (.project).
 	 */
 	public boolean hasPrivateChanges(ProjectDescription description) {
 		if (!dynamicRefs.equals(description.dynamicRefs))
 			return true;
 		IPath otherLocation = description.getLocation();
 		if (location == null)
 			return otherLocation != null;
 		return !location.equals(otherLocation);
 	}
 
 	/**
 	 * Returns true if any public attributes of the description have changed.
 	 * Public attributes are those that are stored in the project description
 	 * file (.project).
 	 */
 	public boolean hasPublicChanges(ProjectDescription description) {
 		if (!getName().equals(description.getName()))
 			return true;
 		if (!comment.equals(description.getComment()))
 			return true;
 		//don't bother optimizing if the order has changed
 		if (!Arrays.equals(buildSpec, description.getBuildSpec(false)))
 			return true;
 		if (!staticRefs.equals(description.staticRefs))
 			return true;
 		if (!Arrays.equals(natures, description.getNatureIds(false)))
 			return true;
 		if (!Arrays.equals(variants, description.variants))
 			return true;
 
 		HashMap otherFilters = description.getFilters();
 		if ((filterDescriptions == null) && (otherFilters != null))
 			return otherFilters != null;
 		if ((filterDescriptions != null) && !filterDescriptions.equals(otherFilters))
 			return true;
 
 		HashMap otherVariables = description.getVariables();
 		if ((variableDescriptions == null) && (otherVariables != null))
 			return true;
 		if ((variableDescriptions != null) && !variableDescriptions.equals(otherVariables))
 			return true;
 
 		final HashMap otherLinks = description.getLinks();
 		if (linkDescriptions != otherLinks) { 
 			if (linkDescriptions == null || !linkDescriptions.equals(otherLinks))
 				return true;
 		}
 		
 		final URI otherSnapshotLoc= description.getSnapshotLocationURI();
 		if (snapshotLocation != otherSnapshotLoc) {
 			if (snapshotLocation == null || !snapshotLocation.equals(otherSnapshotLoc))
 				return true;
 		}
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see IProjectDescription#newCommand()
 	 */
 	public ICommand newCommand() {
 		return new BuildCommand();
 	}
 
 	/* (non-Javadoc)
 	 * @see IProjectDescription#setBuildSpec(ICommand[])
 	 */
 	public void setBuildSpec(ICommand[] value) {
 		Assert.isLegal(value != null);
 		//perform a deep copy in case clients perform further changes to the command
 		ICommand[] result = new ICommand[value.length];
 		for (int i = 0; i < result.length; i++) {
 			result[i] = (ICommand) ((BuildCommand) value[i]).clone();
 			//copy the reference to any builder instance from the old build spec
 			//to preserve builder states if possible.
 			for (int j = 0; j < buildSpec.length; j++) {
 				if (result[i].equals(buildSpec[j])) {
 					((BuildCommand) result[i]).setBuilders(((BuildCommand) buildSpec[j]).getBuilders());
 					break;
 				}
 			}
 		}
 		buildSpec = result;
 	}
 
 	/* (non-Javadoc)
 	 * @see IProjectDescription#setComment(String)
 	 */
 	public void setComment(String value) {
 		comment = value;
 	}
 
 	/**
 	 * Sets the map of link descriptions (String name -> LinkDescription).
 	 * Since this method is only used internally, it never creates a copy. May
 	 * pass null if this project does not have any linked resources
 	 */
 	public void setLinkDescriptions(HashMap linkDescriptions) {
 		this.linkDescriptions = linkDescriptions;
 	}
 
 	/**
 	 * Sets the description of a link. Setting to a description of null will
 	 * remove the link from the project description.
 	 * @return <code>true</code> if the description was actually changed,
 	 *     <code>false</code> otherwise.
 	 * @since 3.5 returns boolean (was void before)
 	 */
 	public boolean setLinkLocation(IPath path, LinkDescription description) {
 		HashMap tempMap = linkDescriptions;
 		if (description != null) {
 			//addition or modification
 			if (tempMap == null)
 				tempMap = new HashMap(10);
 			else 
 				//copy on write to protect against concurrent read
 				tempMap = (HashMap) tempMap.clone();
 			Object oldValue = tempMap.put(path, description);
 			if (oldValue!=null && description.equals(oldValue)) {
 				//not actually changed anything
 				return false;
 			}
 			linkDescriptions = tempMap;
 		} else {
 			//removal
 			if (tempMap == null)
 				return false;
 			//copy on write to protect against concurrent access
 			HashMap newMap = (HashMap) tempMap.clone();
 			Object oldValue = newMap.remove(path);
 			if (oldValue == null) {
 				//not actually changed anything
 				return false;
 			}
 			linkDescriptions = newMap.size() == 0 ? null : newMap;
 		}
 		return true;
 	}
 
 	/**
 	 * Sets the map of filter descriptions (String name -> LinkedList<LinkDescription>).
 	 * Since this method is only used internally, it never creates a copy. May
 	 * pass null if this project does not have any filtered resources
 	 */
 	public void setFilterDescriptions(HashMap filterDescriptions) {
 		this.filterDescriptions = filterDescriptions;
 	}
 
 	/**
 	 * Sets the map of variable descriptions (String name ->
 	 * VariableDescription). Since this method is only used internally, it never
 	 * creates a copy. May pass null if this project does not have any variables
 	 */
 	public void setVariableDescriptions(HashMap variableDescriptions) {
 		this.variableDescriptions = variableDescriptions;
 	}
 
 	/**
 	 * Add the description of a filter. Setting to a description of null will
 	 * remove the filter from the project description.
 	 */
 	synchronized public void addFilter(IPath path, FilterDescription description) {
 		Assert.isNotNull(description);
 		if (filterDescriptions == null)
 			filterDescriptions = new HashMap(10);
 		LinkedList/*<FilterDescription>*/ descList = (LinkedList /*<FilterDescription> */) filterDescriptions.get(path);
 		if (descList == null) {
 			descList = new LinkedList/*<FilterDescription>*/();
 			filterDescriptions.put(path, descList);
 		}
 		descList.add(description);
 	}
 	
 	/**
 	 * Add the description of a filter. Setting to a description of null will
 	 * remove the filter from the project description.
 	 */
 	synchronized public void removeFilter(IPath path, FilterDescription description) {
 		if (filterDescriptions != null) {
 			LinkedList/*<FilterDescription>*/ descList = (LinkedList /*<FilterDescription> */) filterDescriptions.get(path);
 			if (descList != null) {
 				descList.remove(description);
 				if (descList.size() == 0) {
 					filterDescriptions.remove(path);
 					if (filterDescriptions.size() == 0)
 						filterDescriptions = null;
 				}
 			}
 		}
 	}
 
 	/**
 	 * Sets the description of a variable. Setting to a description of null will
 	 * remove the variable from the project description.
 	 * @return <code>true</code> if the description was actually changed,
 	 *     <code>false</code> otherwise.
 	 * @since 3.5
 	 */
 	public boolean setVariableDescription(String name,
 			VariableDescription description) {
 		HashMap tempMap = variableDescriptions;
 		if (description != null) {
 			// addition or modification
 			if (tempMap == null)
 				tempMap = new HashMap(10);
 			else
 				// copy on write to protect against concurrent read
 				tempMap = (HashMap) tempMap.clone();
 			Object oldValue = tempMap.put(name, description);
 			if (oldValue!=null && description.equals(oldValue)) {
 				//not actually changed anything
 				return false;
 			}
 			variableDescriptions = tempMap;
 		} else {
 			// removal
 			if (tempMap == null)
 				return false;
 			// copy on write to protect against concurrent access
 			HashMap newMap = (HashMap) tempMap.clone();
 			Object oldValue = newMap.remove(name);
 			if (oldValue == null) {
 				//not actually changed anything
 				return false;
 			}
 			variableDescriptions = newMap.size() == 0 ? null : newMap;
 		}
 		return true;
 	}
 
 	/**
 	 * set the filters for a given resource. Setting to a description of null will
 	 * remove the filter from the project description.
 	 * @return <code>true</code> if the description was actually changed,
 	 *     <code>false</code> otherwise.
 	 */
 	synchronized public boolean setFilters(IPath path, LinkedList/*<FilterDescription>*/ descriptions) {
 		if (descriptions != null) {
 			// addition
 			if (filterDescriptions == null)
 				filterDescriptions = new HashMap(10);
 			Object oldValue = filterDescriptions.put(path, descriptions);
 			if (oldValue!=null && descriptions.equals(oldValue)) {
 				//not actually changed anything
 				return false;
 			}
 		}
 		else { 
 			// removal
 			if (filterDescriptions == null)
 				return false;
 			
 			Object oldValue = filterDescriptions.remove(path);
 			if (oldValue == null) {
 				//not actually changed anything
 				return false;
 			}
 			if (filterDescriptions.size() == 0)
 				filterDescriptions = null;
 		}
 		return true;
 	}
 
 	/* (non-Javadoc)
 	 * @see IProjectDescription#setLocation(IPath)
 	 */
 	public void setLocation(IPath path) {
 		this.location = path == null ? null : URIUtil.toURI(path);
 	}
 
 	public void setLocationURI(URI location) {
 		this.location = location;
 	}
 
 	/* (non-Javadoc)
 	 * @see IProjectDescription#setName(String)
 	 */
 	public void setName(String value) {
 		super.setName(value);
 	}
 
 	/* (non-Javadoc)
 	 * @see IProjectDescription#setNatureIds(String[])
 	 */
 	public void setNatureIds(String[] value) {
 		natures = (String[]) value.clone();
 	}
 
 	/**
 	 * Sets the location URI for a project snapshot that may be
 	 * loaded automatically when the project is created in a workspace.
 	 * <p>
 	 * <strong>EXPERIMENTAL</strong>. This method has been added as
 	 * part of a work in progress. There is no guarantee that this API will
 	 * work or that it will remain the same. Please do not use this API without
 	 * consulting with the Platform Core team.
 	 * </p>
 	 * @param snapshotLocation the location URI or
 	 *    <code>null</code> to clear the setting 
 	 * @see IProject#loadSnapshot(int, URI, IProgressMonitor)
 	 * @see #getSnapshotLocationURI()
 	 * @since 3.6 
 	 */
 	public void setSnapshotLocationURI(URI snapshotLocation) {
 		this.snapshotLocation = snapshotLocation;
 	}
 
 	public URI getGroupLocationURI(IPath projectRelativePath) {
 		return LinkDescription.VIRTUAL_LOCATION;
 	}
 
 	/**
 	 * Returns the union of the description's static and dynamic project variant references,
 	 * for the variant with the given name, with duplicates omitted. The calculation is
 	 * optimized by caching the result.
 	 * Returns an empty array if the given variant does not exist in the description.
 	 * @nooverride This method is not intended to be re-implemented or extended by clients.
 	 */
 	public IProjectVariantReference[] getAllVariantReferences(String variant, boolean makeCopy) {
 		if (!hasVariant(variant))
 			return EMPTY_PROJECT_VARIANT_REFERENCE_ARRAY;
 		if (!cachedRefs.containsKey(variant)) {
 			IProjectVariantReference[] statik = getReferencedProjectVariants(variant, false);
 			IProjectVariantReference[] dynamic = getDynamicVariantReferences(variant, false);
 			if (dynamic.length == 0) {
 				cachedRefs.put(variant, statik);
 			} else if (statik.length == 0) {
 				cachedRefs.put(variant, dynamic);
 			} else {
 				//combine all references
 				IProjectVariantReference[] result = new IProjectVariantReference[dynamic.length + statik.length];
 				System.arraycopy(statik, 0, result, 0, statik.length);
 				System.arraycopy(dynamic, 0, result, statik.length, dynamic.length);
 				cachedRefs.put(variant, copyAndRemoveDuplicates(result));
 			}
 		}
 		//still need to copy the result to prevent tampering with the cache
 		IProjectVariantReference[] result = (IProjectVariantReference[]) cachedRefs.get(variant);
 		return makeCopy ? (IProjectVariantReference[]) result.clone() : result;
 	}
 
 	/* (non-Javadoc)
 	 * @see IProjectDescription#getReferencedVariants(String)
 	 */
 	public IProjectVariantReference[] getReferencedProjectVariants(String variant) {
 		return getReferencedProjectVariants(variant, true);
 	}
 
 	/**
 	 * @nooverride This method is not intended to be re-implemented or extended by clients.
 	 */
 	public IProjectVariantReference[] getReferencedProjectVariants(String variant, boolean makeCopy) {
 		if (!hasVariant(variant) || !staticRefs.containsKey(variant))
 			return EMPTY_PROJECT_VARIANT_REFERENCE_ARRAY;
 		IProjectVariantReference[] result = (IProjectVariantReference[]) staticRefs.get(variant);
 		return makeCopy ? (IProjectVariantReference[]) result.clone() : result;
 	}
 
 	/* (non-Javadoc)
 	 * @see IProjectDescription#setReferencedVariants(String, IProjectVariantReference[])
 	 */
 	public void setReferencedProjectVariants(String variant, IProjectVariantReference[] references) {
 		Assert.isLegal(references != null);
 		if (!hasVariant(variant))
 			return;
 		staticRefs.put(variant, copyAndRemoveDuplicates(references));
 		clearCachedReferences(variant);
 	}
 
 	/* (non-Javadoc)
 	 * @see IProjectDescription#getDynamicVariantReferences(String)
 	 */
 	public IProjectVariantReference[] getDynamicVariantReferences(String variant) {
 		return getDynamicVariantReferences(variant, true);
 	}
 
 	/**
 	 * @nooverride This method is not intended to be re-implemented or extended by clients.
 	 */
 	public IProjectVariantReference[] getDynamicVariantReferences(String variant, boolean makeCopy) {
 		if (!hasVariant(variant) || !dynamicRefs.containsKey(variant))
 			return EMPTY_PROJECT_VARIANT_REFERENCE_ARRAY;
 		IProjectVariantReference[] result = (IProjectVariantReference[]) dynamicRefs.get(variant);
 		return makeCopy ? (IProjectVariantReference[]) result.clone() : result;
 	}
 
 	/* (non-Javadoc)
 	 * @see IProjectDescription#setDynamicVariantReferences(String, IProjectVariantReference[])
 	 */
 	public void setDynamicVariantReferences(String variant, IProjectVariantReference[] references) {
 		Assert.isLegal(references != null);
 		if (!hasVariant(variant))
 			return;
 		dynamicRefs.put(variant, copyAndRemoveDuplicates(references));
 		clearCachedReferences(variant);
 	}
 
 	/* (non-Javadoc)
 	 * @see IProjectDescription#newVariant(String)
 	 */
 	public IProjectVariant newVariant(String variantName) {
 		return new ProjectVariant(variantName);
 	}
 
 	/* (non-Javadoc)
 	 * @see IProjectDescription#setVariants(IProjectVariant[])
 	 */
 	public void setVariants(IProjectVariant[] value) {
 		if (value == null || value.length == 0)
 			variants = DEFAULT_VARIANTS;
 		else {
 			// Filter out duplicates
 			Set filtered = new LinkedHashSet(value.length);
 			for (int i = 0; i < value.length; i++) {
 				IProjectVariant variant = (IProjectVariant) value[i].clone();
 				// Ensure the project is not set
 				((ProjectVariant) variant).clearProject();
 				Assert.isTrue(((ProjectVariant) variant).internalGetProject() == null);
 				filtered.add(variant);
 			}
 
 			if (filtered.isEmpty())
 				variants = DEFAULT_VARIANTS;
 			else {
 				variants = new IProjectVariant[filtered.size()];
 				filtered.toArray(variants);
 			}
 		}
 
 		// Remove references for deleted variants
 		variantNames = new HashSet(variants.length);
 		for (int i = 0; i < variants.length; i++)
 			variantNames.add(variants[i].getVariantName());
 		boolean modified = false;
 		modified |= staticRefs.keySet().retainAll(variantNames);
 		modified |= dynamicRefs.keySet().retainAll(variantNames);
 		if (modified)
 			clearCachedReferences();
 	}
 
 	/**
 	 * Used by Project to get the variants on the description
 	 * @nooverride This method is not intended to be re-implemented or extended by clients.
 	 */
 	public IProjectVariant[] internalGetVariants(boolean makeCopy) {
 		if (variants == null || variants.length == 0)
 			variants = DEFAULT_VARIANTS;
 		for (int i = 0; i < variants.length; i++)
 			Assert.isTrue(((ProjectVariant) variants[i]).internalGetProject() == null);
 		return makeCopy ? copyVariants(variants) : variants;
 	}
 
 	private IProjectVariant[] copyVariants(IProjectVariant[] variants) {
 		IProjectVariant[] result = new IProjectVariant[variants.length];
 		for (int i = 0; i < variants.length; i++)
 			result[i] = (IProjectVariant) variants[i].clone();
 		return result;
 	}
 
 	/**
 	 * Used by Project to get the active variant.
 	 * @nooverride This method is not intended to be re-implemented or extended by clients.
 	 */
 	public IProjectVariant internalGetActiveVariant(boolean makeCopy) {
 		IProjectVariant result = null;
 		if (!hasVariant(activeVariant)) {
 			activeVariant = variants[0].getVariantName();
 			result = variants[0];
 		}
 		for (int i = 0; i < variants.length; i++) {
 			if (variants[i].getVariantName().equals(activeVariant)) {
 				result = variants[i];
 				break;
 			}
 		}
 		Assert.isTrue(result != null);
 		Assert.isTrue(((ProjectVariant) result).internalGetProject() == null);
 		return makeCopy ? (IProjectVariant) result.clone() : result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see IProjectDescription#setActiveVariant(String)
 	 */
 	public void setActiveVariant(String variantName) {
 		if (hasVariant(variantName)) {
 			activeVariant = variantName;
 		}
 	}
 
 	/**
 	 * Internal method to check if the description has a given variant.
 	 */
 	private boolean hasVariant(String variantName) {
 		if (variantName == null)
 			return false;
 		for (int i = 0; i < variants.length; i++)
 			if (variants[i].getVariantName().equals(variantName))
 				return true;
 		return false;
 	}
 
 	/**
 	 * Clear all cached references for the given variant
 	 * @param variant the variant to clear cached references for
 	 */
 	private void clearCachedReferences(String variant)
 	{
 		cachedRefs.remove(variant);
 		cachedProjectRefs = null;
 		cachedStaticProjectRefs = null;
 		cachedDynamicProjectRefs = null;
 	}
 
 	/**
 	 * Clear all cached references for all variants
 	 */
 	private void clearCachedReferences()
 	{
 		cachedRefs = new HashMap();
 		cachedProjectRefs = null;
 		cachedStaticProjectRefs = null;
 		cachedDynamicProjectRefs = null;
 	}
 
 	/**
 	 * Returns a copy of the given array of project variants with all duplicates removed
 	 */
 	private IProjectVariantReference[] copyAndRemoveDuplicates(IProjectVariantReference[] values) {
 		Set set = new LinkedHashSet();
 		set.addAll(Arrays.asList(values));
 		return (IProjectVariantReference[]) set.toArray(new IProjectVariantReference[set.size()]);
 	}
 
 	/**
 	 * Returns the union of the description's static and dynamic project references,
 	 * with duplicates omitted. The calculation is optimized by caching the result
 	 * @see #getAllVariantReferences(String, boolean)
 	 * @nooverride This method is not intended to be re-implemented or extended by clients.
 	 */
 	public IProject[] getAllReferences(boolean makeCopy) {
 		if (cachedProjectRefs == null) {
 			IProject[] statik = getReferencedProjects(false);
 			IProject[] dynamic = getDynamicReferences(false);
 			if (dynamic.length == 0) {
 				cachedProjectRefs = statik;
 			} else if (statik.length == 0) {
 				cachedProjectRefs = dynamic;
 			} else {
 				Set set = new LinkedHashSet();
 				set.addAll(Arrays.asList(statik));
 				set.addAll(Arrays.asList(dynamic));
 				cachedProjectRefs = (IProject[]) set.toArray(new IProject[set.size()]);
 			}
 		}
 		//still need to copy the result to prevent tampering with the cache
 		return makeCopy ? (IProject[]) cachedProjectRefs.clone() : cachedProjectRefs;
 	}
 
 	/* (non-Javadoc)
 	 * @see IProjectDescription#getReferencedProjects()
 	 */
 	public IProject[] getReferencedProjects() {
 		return getReferencedProjects(true);
 	}
 
 	/**
 	 * @nooverride This method is not intended to be re-implemented or extended by clients.
 	 */
 	public IProject[] getReferencedProjects(boolean makeCopy) {
 		if (staticRefs == null)
 			return EMPTY_PROJECT_ARRAY;
 		// Generate project references from project variants references
 		if (cachedStaticProjectRefs == null) {
 			cachedStaticProjectRefs = getProjectsFromProjectVariantReferences(staticRefs);
 		}
 		//still need to copy the result to prevent tampering with the cache
 		return makeCopy ? (IProject[]) cachedStaticProjectRefs.clone() : cachedStaticProjectRefs;
 	}
 
 	/* (non-Javadoc)
 	 * @see IProjectDescription#setReferencedProjects(IProject[])
 	 */
 	public void setReferencedProjects(IProject[] projects) {
 		Assert.isLegal(projects != null);
 		// Add all variants in each of the projects as a reference
 		for (int i = 0; i < variants.length; i++)
 			setReferencedProjectVariants(variants[i].getVariantName(), getProjectVariantReferencesFromProjects(projects));
 	}
 
 	/* (non-Javadoc)
 	 * @see IProjectDescription#getDynamicReferences()
 	 */
 	public IProject[] getDynamicReferences() {
 		return getDynamicReferences(true);
 	}
 
 	/**
 	 * @nooverride This method is not intended to be re-implemented or extended by clients.
 	 */
 	public IProject[] getDynamicReferences(boolean makeCopy) {
 		if (dynamicRefs == null)
 			return EMPTY_PROJECT_ARRAY;
 		// Generate dynamic project references from dynamic project variants references
 		if (cachedDynamicProjectRefs == null) {
 			cachedDynamicProjectRefs = getProjectsFromProjectVariantReferences(dynamicRefs);
 		}
 		return makeCopy ? (IProject[]) cachedDynamicProjectRefs.clone() : cachedDynamicProjectRefs;
 	}
 
 	/* (non-Javadoc)
 	 * @see IProjectDescription#setDynamicReferences(IProject[])
 	 */
 	public void setDynamicReferences(IProject[] projects) {
 		Assert.isLegal(projects != null);
 		for (int i = 0; i < variants.length; i++)
 			setDynamicVariantReferences(variants[i].getVariantName(), getProjectVariantReferencesFromProjects(projects));
 	}
 
 	/**
 	 * Get a list of projects, without duplicates, from a list of project variant references.
 	 * Order is preserved, and is according to the first occurrence of a project in the
 	 * array of project variants.
 	 * @param refsMap map containing the project variant references to get the projects from
 	 * @return list of projects
 	 */
 	private IProject[] getProjectsFromProjectVariantReferences(Map/*<String, IProjectVariantReference[]>*/ refsMap) {
 		Set projects = new LinkedHashSet();
 		Iterator i = refsMap.values().iterator();
 		while (i.hasNext()) {
 			IProjectVariantReference[] refs = (IProjectVariantReference[]) i.next();
 			for (int j = 0; j < refs.length; j++) {
 				projects.add(refs[j].getProject());
 			}
 		}
 		return (IProject[]) projects.toArray(new Project[projects.size()]);
 	}
 
 	/**
 	 * Get a list of project variant references, without duplicates, from a list of projects.
 	 * A reference to each of the variants for each of the projects is returned.
 	 * Order is preserved - the variants appear for each project in the order
 	 * that the projects were specified.
 	 * If the project is not accessible, a reference is added to its active variant.
 	 * @param projects projects to get the project variants from
 	 * @return list of project variant references
 	 */
 	private IProjectVariantReference[] getProjectVariantReferencesFromProjects(IProject[] projects) {
 		List refs = new ArrayList();
 		for (int i = 0; i < projects.length; i++) {
 			IProject project = projects[i];
 			if (project.isAccessible()) {
 				IProjectVariant[] projVariants = ((Project) project).internalGetVariants();
 				for (int j = 0; j < projVariants.length; j++) {
 					refs.add(new ProjectVariantReference(project, projVariants[j].getVariantName()));
 				}
 			} else
 				refs.add(new ProjectVariantReference(project));
 		}
 		return (IProjectVariantReference[]) refs.toArray(new IProjectVariantReference[refs.size()]);
 	}
 }
