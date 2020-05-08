 /*******************************************************************************
  * Copyright (c) 2007, 2010 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.navigation.model;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.CoreException;
 
 import org.eclipse.riena.core.util.VariableManagerUtil;
 import org.eclipse.riena.navigation.AbstractNavigationAssembler;
 import org.eclipse.riena.navigation.IAssemblerProvider;
 import org.eclipse.riena.navigation.IGenericNavigationAssembler;
 import org.eclipse.riena.navigation.IModuleGroupNode;
 import org.eclipse.riena.navigation.IModuleNode;
 import org.eclipse.riena.navigation.INavigationNode;
 import org.eclipse.riena.navigation.ISubApplicationNode;
 import org.eclipse.riena.navigation.ISubModuleNode;
 import org.eclipse.riena.navigation.NavigationArgument;
 import org.eclipse.riena.navigation.NavigationNodeId;
 import org.eclipse.riena.navigation.extension.IModuleGroupNode2Extension;
 import org.eclipse.riena.navigation.extension.IModuleNode2Extension;
 import org.eclipse.riena.navigation.extension.INode2Extension;
 import org.eclipse.riena.navigation.extension.ISubApplicationNode2Extension;
 import org.eclipse.riena.navigation.extension.ISubModuleNode2Extension;
 import org.eclipse.riena.navigation.extension.Node2Extension;
 
 /**
  * This assembler builds navigation nodes according to the definition stored in
  * extensions.
  */
 public class GenericNavigationAssembler extends AbstractNavigationAssembler implements IGenericNavigationAssembler {
 
 	/** dynamic variable referencing navigation node id */
 	public static final String VAR_NAVIGATION_NODEID = "riena.navigation.nodeid"; //$NON-NLS-1$
 
 	/** dynamic variable referencing navigation node id */
 	public static final String VAR_NAVIGATION_NODECONTEXT = "riena.navigation.nodecontext"; //$NON-NLS-1$
 
 	/** dynamic variable referencing navigation parameter */
 	public static final String VAR_NAVIGATION_PARAMETER = "riena.navigation.parameter"; //$NON-NLS-1$
 
 	/**
 	 * @since 2.0
 	 */
 	protected Set<String> acceptedTargetIds = null;
 	// the IAssemblyProvider that can be used to resolve assembly references
 	private IAssemblerProvider assemblerProvider;
 
 	/**
 	 * @since 2.0
 	 */
 	protected final void initializeAcceptedTargetIds() {
 
 		if (getAssembly() != null) {
 			// resolve sub-application if it exists
 			final ISubApplicationNode2Extension[] subApplications = getAssembly().getSubApplications();
 			for (final ISubApplicationNode2Extension subApplication : subApplications) {
 				resolveTargetIds(subApplication);
 			}
 			// resolve module group if it exists
 			final IModuleGroupNode2Extension[] groups = getAssembly().getModuleGroups();
 			for (final IModuleGroupNode2Extension group : groups) {
 				resolveTargetIds(group);
 			}
 			// otherwise try module
 			final IModuleNode2Extension[] modules = getAssembly().getModules();
 			for (final IModuleNode2Extension module : modules) {
 				resolveTargetIds(module);
 			}
 			// last resort is sub-module
 			final ISubModuleNode2Extension[] subModules = getAssembly().getSubModules();
 			for (final ISubModuleNode2Extension subModule : subModules) {
 				resolveTargetIds(subModule);
 			}
 		}
 
 	}
 
 	private void resolveTargetIds(final ISubApplicationNode2Extension subapplicationDefinition) {
 
 		updateAcceptedTargetIds(subapplicationDefinition.getNodeId());
 		if (subapplicationDefinition.getChildNodes() != null) {
 			for (final IModuleGroupNode2Extension groupDefinition : subapplicationDefinition.getChildNodes()) {
 				resolveTargetIds(groupDefinition);
 			}
 		}
 	}
 
 	private void resolveTargetIds(final IModuleGroupNode2Extension groupDefinition) {
 
 		updateAcceptedTargetIds(groupDefinition.getNodeId());
 		if (groupDefinition.getChildNodes() != null) {
 			for (final IModuleNode2Extension moduleDefinition : groupDefinition.getChildNodes()) {
 				resolveTargetIds(moduleDefinition);
 			}
 		}
 	}
 
 	private void resolveTargetIds(final IModuleNode2Extension moduleDefinition) {
 
 		updateAcceptedTargetIds(moduleDefinition.getNodeId());
 		if (moduleDefinition.getChildNodes() != null) {
 			for (final ISubModuleNode2Extension submoduleDefinition : moduleDefinition.getChildNodes()) {
 				resolveTargetIds(submoduleDefinition);
 			}
 		}
 	}
 
 	private void resolveTargetIds(final ISubModuleNode2Extension submoduleDefinition) {
 
 		updateAcceptedTargetIds(submoduleDefinition.getNodeId());
 		if (submoduleDefinition.getChildNodes() != null) {
 			for (final ISubModuleNode2Extension nestedDefinition : submoduleDefinition.getChildNodes()) {
 				resolveTargetIds(nestedDefinition);
 			}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @since 2.0
 	 */
 	public INavigationNode<?>[] buildNode(final NavigationNodeId targetId, final NavigationArgument navigationArgument) {
 
 		if (getAssembly() != null) {
 			final Map<String, Object> context = new HashMap<String, Object>();
 			// build module group if it exists
 			final ISubApplicationNode2Extension[] subApplications = getAssembly().getSubApplications();
 			if ((subApplications != null) && (subApplications.length > 0)) {
 				final INavigationNode<?>[] nodes = new INavigationNode<?>[subApplications.length];
 				for (int i = 0; i < subApplications.length; i++) {
 					nodes[i] = build(subApplications[i], targetId, navigationArgument, context);
 				}
 				return nodes;
 			}
 			// build module group if it exists
 			final IModuleGroupNode2Extension[] groups = getAssembly().getModuleGroups();
 			if ((groups != null) && (groups.length > 0)) {
 				final INavigationNode<?>[] nodes = new INavigationNode<?>[groups.length];
 				for (int i = 0; i < groups.length; i++) {
 					nodes[i] = build(groups[i], targetId, navigationArgument, context);
 				}
 				return nodes;
 			}
 			// otherwise try module
 			final IModuleNode2Extension[] modules = getAssembly().getModules();
 			if ((modules != null) && (modules.length > 0)) {
 				final INavigationNode<?>[] nodes = new INavigationNode<?>[modules.length];
 				for (int i = 0; i < modules.length; i++) {
 					nodes[i] = build(modules[i], targetId, navigationArgument, context);
 				}
 				return nodes;
 			}
 			// last resort is submodule
 			final ISubModuleNode2Extension[] subModules = getAssembly().getSubModules();
 			if ((subModules != null) && (subModules.length > 0)) {
 				final INavigationNode<?>[] nodes = new INavigationNode<?>[subModules.length];
 				for (int i = 0; i < subModules.length; i++) {
 					nodes[i] = build(subModules[i], targetId, navigationArgument, context);
 				}
 				return nodes;
 			}
 		}
 
 		throw new ExtensionPointFailure(
 				"'subapplication', 'modulegroup', 'module' or 'submodule' element expected. ID=" + targetId.getTypeId()); //$NON-NLS-1$
 
 	}
 
 	/**
 	 * Builds the node of a sub-application and all its children.
 	 * 
 	 * @param subApplicationDefinition
 	 *            definition of a sub-application
 	 * @param targetId
 	 *            The ID of the node to create.
 	 * @param navigationArgument
 	 *            Optional argument passed on from the navigate(..) method. May
 	 *            be null.
 	 * @param context
 	 * @return created sub-application node
 	 * @since 2.0
 	 */
 	protected ISubApplicationNode build(final ISubApplicationNode2Extension subApplicationDefinition,
 			final NavigationNodeId targetId, final NavigationArgument navigationArgument,
 			final Map<String, Object> context) {
 
 		Assert.isNotNull(subApplicationDefinition, "Error building sub-application. Sub-application cannot be null"); //$NON-NLS-1$
 		Assert.isNotNull(subApplicationDefinition.getPerspectiveId(),
 				"Error building sub-application. Attribute 'perspectiveId' cannot be null for sub-application = " //$NON-NLS-1$
 						+ subApplicationDefinition.getPerspectiveId());
 
 		// a module group can only contain modules
 		final ISubApplicationNode subapplication = new SubApplicationNode(createNavigationNodeIdFromTemplate(targetId,
 				subApplicationDefinition, navigationArgument), subApplicationDefinition.getName());
 		subapplication.setIcon(subApplicationDefinition.getIcon());
 		updateContext(subapplication, navigationArgument);
 
 		if (subApplicationDefinition.getChildNodes() != null) {
 			for (final IModuleGroupNode2Extension child : subApplicationDefinition.getChildNodes()) {
 				subapplication.addChild(build(child, targetId, navigationArgument, copy(context)));
 			}
 		}
 
 		return subapplication;
 
 	}
 
 	/**
 	 * Builds the node of a module group and all its children.
 	 * 
 	 * @param groupDefinition
 	 *            definition of a module group
 	 * @param targetId
 	 *            The ID of the node to create.
 	 * @param navigationArgument
 	 *            Optional argument passed on from the navigate(..) method. May
 	 *            be null.
 	 * @param context
 	 * @return created module group node
 	 * @since 2.0
 	 */
 	protected IModuleGroupNode build(final IModuleGroupNode2Extension groupDefinition, final NavigationNodeId targetId,
 			final NavigationArgument navigationArgument, final Map<String, Object> context) {
 
 		// a module group can only contain modules
 		final IModuleGroupNode moduleGroup = new ModuleGroupNode(createNavigationNodeIdFromTemplate(targetId,
 				groupDefinition, navigationArgument));
 		moduleGroup.setLabel(groupDefinition.getName());
 		moduleGroup.setIcon(groupDefinition.getIcon());
 		updateContext(moduleGroup, navigationArgument);
 
 		if (groupDefinition.getChildNodes() != null) {
 			for (final IModuleNode2Extension child : groupDefinition.getChildNodes()) {
 				moduleGroup.addChild(build(child, targetId, navigationArgument, copy(context)));
 			}
 		}
 
 		return moduleGroup;
 
 	}
 
 	/**
 	 * Builds the node of a module and all its children.
 	 * 
 	 * @param groupDefinition
 	 *            definition of a module
 	 * @param targetId
 	 *            The ID of the node to create.
 	 * @param navigationArgument
 	 *            Optional argument passed on from the navigate(..) method. May
 	 *            be null.
 	 * @param context
 	 * @return created module node
 	 * @since 2.0
 	 */
 	protected IModuleNode build(final IModuleNode2Extension moduleDefinition, final NavigationNodeId targetId,
 			final NavigationArgument navigationArgument, final Map<String, Object> context) {
 
 		IModuleNode module = null;
 		final Map<String, Object> mapping = createMapping(targetId, navigationArgument);
 		try {
 			startVariableResolver(mapping);
 			// create module node with label (and icon)
 			String label = moduleDefinition.getName();
 			if (moduleDefinition instanceof Node2Extension) {
 				// it's only necessary for converted assembly nodes
 				// (not converted nodes are proxies)
 				label = resolveVariables(label);
 			}
 			module = new ModuleNode(createNavigationNodeIdFromTemplate(targetId, moduleDefinition, navigationArgument),
 					label);
 			module.setIcon(moduleDefinition.getIcon());
 			module.setClosable(moduleDefinition.isClosable());
 			updateContext(module, navigationArgument);
 
 			if (moduleDefinition.getChildNodes() != null) {
 				for (final ISubModuleNode2Extension child : moduleDefinition.getChildNodes()) {
 					module.addChild(build(child, targetId, navigationArgument, copy(context)));
 				}
 			}
 
 		} finally {
 			cleanupVariableResolver();
 		}
 
 		return module;
 
 	}
 
 	/**
 	 * Builds the node of a sub-module and all its children.
 	 * 
 	 * @param subModuleDefinition
 	 *            definition of a sub-module
 	 * @param targetId
 	 *            The ID of the node to create.
 	 * @param navigationArgument
 	 *            Optional argument passed on from the navigate(..) method. May
 	 *            be null.
 	 * @param context
 	 * @return created sub-module node
 	 * @since 2.0
 	 */
 	protected ISubModuleNode build(final ISubModuleNode2Extension subModuleDefinition, final NavigationNodeId targetId,
 			final NavigationArgument navigationArgument, final Map<String, Object> context) {
 
 		ISubModuleNode submodule = null;
 		final Map<String, Object> mapping = createMapping(targetId, navigationArgument);
 		mapping.put(VAR_NAVIGATION_NODECONTEXT, context);
 
 		try {
 			startVariableResolver(mapping);
 			String label = subModuleDefinition.getName();
 			if (subModuleDefinition instanceof Node2Extension) {
 				// it's only necessary for converted assembly nodes
 				// (not converted nodes are proxies)
 				label = resolveVariables(label);
 			}
 			// create submodule node with label (and icon)
 			submodule = new SubModuleNode(createNavigationNodeIdFromTemplate(targetId, subModuleDefinition,
 					navigationArgument), label);
 			submodule.setIcon(subModuleDefinition.getIcon());
 			submodule.setVisible(subModuleDefinition.isVisible());
 			submodule.setExpanded(subModuleDefinition.isExpanded());
 			submodule.setSelectable(subModuleDefinition.isSelectable());
 
 			updateContext(submodule, navigationArgument);
 
 			if (subModuleDefinition.getChildNodes() != null) {
 				for (final ISubModuleNode2Extension child : subModuleDefinition.getChildNodes()) {
 					submodule.addChild(build(child, targetId, navigationArgument, copy(context)));
 				}
 			}
 
 		} finally {
 			cleanupVariableResolver();
 		}
 
 		return submodule;
 
 	}
 
 	/**
 	 * Creates an ID of a navigation node with the given informations.
 	 * 
 	 * @param template
 	 * @param nodeExtension
 	 *            definition of node
 	 * @param navigationArgument
 	 *            Optional argument passed on from the navigate(..) method. May
 	 *            be null. (<i>not used in this implementation.</i>)
 	 * @return create ID of a navigation node
 	 * @since 2.0
 	 */
 	protected NavigationNodeId createNavigationNodeIdFromTemplate(final NavigationNodeId template,
 			final INode2Extension nodeExtension, final NavigationArgument navigationArgument) {
 		final String typeId = nodeExtension.getNodeId();
 		final String instanceId = template.getInstanceId();
 		return new NavigationNodeId(typeId, instanceId);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void setAssemblerProvider(final IAssemblerProvider assemblyProvider) {
 		this.assemblerProvider = assemblyProvider;
 	}
 
 	/**
 	 * Returns an assembly provider that may be used by the resolve assembly
 	 * references.
 	 * 
 	 * @param assemblyProvider
 	 */
 	public IAssemblerProvider getAssemblerProvider() {
 		return assemblerProvider;
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationAssembler#acceptsTargetId(java
 	 *      .lang.String)
 	 */
 	public boolean acceptsToBuildNode(final NavigationNodeId nodeId, final NavigationArgument argument) {
 
 		return getAcceptedTargetIds().contains(nodeId.getTypeId());
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.INavigationAssembler#getAcceptedNodeIds()
 	 */
 	public Collection<String> getAcceptedTargetIds() {
 
 		if (acceptedTargetIds == null) {
 			acceptedTargetIds = new HashSet<String>();
 			initializeAcceptedTargetIds();
 			acceptedTargetIds = Collections.unmodifiableSet(acceptedTargetIds);
 		}
 
 		return new HashSet<String>(acceptedTargetIds);
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	protected void updateAcceptedTargetIds(final String typeId) {
 		if (typeId != null) {
 			acceptedTargetIds.add(typeId);
 		}
 	}
 
 	protected Map<String, Object> copy(final Map<String, Object> context) {
 		return new HashMap<String, Object>(context);
 	}
 
 	protected void updateContext(final INavigationNode<?> node, final NavigationArgument navigationArgument) {
 		// this logic is now in GenericNavigationAssembler and available for Generic assemblers and handwritten ones....
 		//		if (navigationArgument != null) {
 		//			node.setContext(NavigationArgument.CONTEXT_KEY_PARAMETER, navigationArgument.getParameter());
 		//		}
 	}
 
 	protected Map<String, Object> createMapping(final NavigationNodeId targetId,
 			final NavigationArgument navigationArgument) {
 
 		final Map<String, Object> mapping = new HashMap<String, Object>();
 		mapping.put(VAR_NAVIGATION_NODEID, targetId);
 		if (navigationArgument != null) {
 			mapping.put(VAR_NAVIGATION_PARAMETER, navigationArgument.getParameter());
 		}
 
 		return mapping;
 	}
 
 	protected void startVariableResolver(final Map<String, Object> mapping) {
 		ThreadLocalMapResolver.configure(mapping);
 	}
 
 	protected void cleanupVariableResolver() {
 		ThreadLocalMapResolver.cleanup();
 	}
 
 	protected String resolveVariables(final String string) {
 
 		try {
 			return VariableManagerUtil.substitute(string);
 		} catch (final CoreException ex) {
 			ex.printStackTrace();
 			return string;
 		}
 	}

 }
