 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
 
  *******************************************************************************/
 package org.eclipse.dltk.core;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.dltk.ast.parser.ISourceParser;
 import org.eclipse.dltk.ast.parser.SourceParserManager;
 import org.eclipse.dltk.codeassist.ICompletionEngine;
 import org.eclipse.dltk.codeassist.ISelectionEngine;
 import org.eclipse.dltk.compiler.problem.DefaultProblemFactory;
 import org.eclipse.dltk.compiler.problem.IProblemFactory;
 import org.eclipse.dltk.core.PriorityDLTKExtensionManager.ElementInfo;
 import org.eclipse.dltk.core.model.binary.IBinaryElementParser;
 import org.eclipse.dltk.core.search.DLTKSearchParticipant;
 import org.eclipse.dltk.core.search.IDLTKSearchScope;
 import org.eclipse.dltk.core.search.IMatchLocatorParser;
 import org.eclipse.dltk.core.search.SearchPatternProcessor;
 import org.eclipse.dltk.core.search.indexing.SourceIndexerRequestor;
 import org.eclipse.dltk.core.search.matching.IMatchLocator;
 import org.eclipse.dltk.core.search.matching.MatchLocator;
 import org.eclipse.dltk.core.search.matching.MatchLocatorParser;
 import org.eclipse.dltk.internal.core.InternalDLTKLanguageManager;
 
 public class DLTKLanguageManager {
 
 	public static IDLTKLanguageToolkit getLanguageToolkit(String natureId) {
 		return (IDLTKLanguageToolkit) InternalDLTKLanguageManager
 				.getLanguageToolkitsManager().getObject(natureId);
 	}
 
 	public static IDLTKLanguageToolkit[] getLanguageToolkits() {
 
 		final PriorityClassDLTKExtensionManager tkManager = InternalDLTKLanguageManager
 				.getLanguageToolkitsManager();
 		ElementInfo[] elementInfos = tkManager.getElementInfos();
 		IDLTKLanguageToolkit[] toolkits = new IDLTKLanguageToolkit[elementInfos.length];
 		for (int j = 0; j < elementInfos.length; j++) {
 			toolkits[j] = (IDLTKLanguageToolkit) tkManager
 					.getInitObject(elementInfos[j]);
 		}
 		return toolkits;
 	}
 
 	private static IDLTKLanguageToolkit findAppropriateToolkitByObject(
 			Object object) {
 		final PriorityClassDLTKExtensionManager toolkitManager = InternalDLTKLanguageManager
 				.getLanguageToolkitsManager();
 		final ElementInfo[] elementInfos = toolkitManager.getElementInfos();
 		for (int j = 0; j < elementInfos.length; j++) {
 			IDLTKLanguageToolkit toolkit = (IDLTKLanguageToolkit) toolkitManager
 					.getInitObject(elementInfos[j]);
			if (toolkit == null) {
				// toolkit instantiation failed, skip it
				// TODO (alex) remove this ElementInfo entry
				continue;
			}
 			if (object instanceof IResource) {
 				if (DLTKContentTypeManager.isValidResourceForContentType(
 						toolkit, (IResource) object)) {
 					return toolkit;
 				}
 			} else if (object instanceof IPath) {
 				if (DLTKContentTypeManager.isValidFileNameForContentType(
 						toolkit, (IPath) object)) {
 					return toolkit;
 				}
 			} else {
 				return null;
 			}
 		}
 		return null;
 	}
 
 	public static boolean hasScriptNature(IProject project) {
 		return InternalDLTKLanguageManager.getLanguageToolkitsManager()
 				.findScriptNature(project) != null;
 	}
 
 	public static IDLTKLanguageToolkit getLanguageToolkit(IModelElement element) {
 		IDLTKLanguageToolkit toolkit = (IDLTKLanguageToolkit) InternalDLTKLanguageManager
 				.getLanguageToolkitsManager().getObject(element);
 		if (toolkit == null) {
 			while (element != null
 					&& element.getElementType() != IModelElement.SOURCE_MODULE) {
 				element = element.getParent();
 			}
 			if (element != null
 					&& element.getElementType() == IModelElement.SOURCE_MODULE) {
 				if (element.getResource() != null) {
 					IDLTKLanguageToolkit tk = findAppropriateToolkitByObject(element
 							.getResource());
 					if (tk != null) {
 						return tk;
 					}
 				}
 				return findAppropriateToolkitByObject(element.getPath());
 			}
 		}
 		return toolkit;
 	}
 
 	/**
 	 * The behavior of this method was not correct - it could return incorrect
 	 * results for files without extension. For compatibility purposes and to
 	 * allow smooth migration it is marked as deprecated -- AlexPanchenko
 	 */
 	@Deprecated
 	public static IDLTKLanguageToolkit findToolkit(IResource resource) {
 		IDLTKLanguageToolkit toolkit = findAppropriateToolkitByObject(resource);
 		if (toolkit == null) {
 			toolkit = findToolkit(resource.getProject());
 		}
 		return toolkit;
 	}
 
 	/**
 	 * Returns the toolkit of the specified {@link IProject} or null if not
 	 * found.
 	 * 
 	 * @param project
 	 * @return
 	 */
 	public static IDLTKLanguageToolkit findToolkit(IProject project) {
 		return (IDLTKLanguageToolkit) InternalDLTKLanguageManager
 				.getLanguageToolkitsManager().getObject(project);
 	}
 
 	/**
 	 * Return the toolkit of the specified resource or <code>null</code>.
 	 * 
 	 * @param resource
 	 * @return
 	 */
 	public static IDLTKLanguageToolkit findToolkitForResource(IResource resource) {
 		if (resource.getType() == IResource.PROJECT) {
 			return findToolkit((IProject) resource);
 		} else {
 			final IModelElement parent = DLTKCore.create(resource.getParent());
 			if (parent == null) {
 				return null;
 			}
 			return DLTKLanguageManager.findToolkit(parent, resource, false);
 		}
 	}
 
 	/**
 	 * Return the language toolkit of the specified resource in the specified
 	 * project. Until multiple languages are allowed for the same project - it
 	 * will just return the first matching toolkit of the project.
 	 * 
 	 * @param scriptProject
 	 * @param resource
 	 * @param useDefault
 	 *            if resource does not match project toolkit - return project
 	 *            toolkit or <code>null</code>
 	 * @return
 	 */
 	public static IDLTKLanguageToolkit findToolkit(IModelElement parent,
 			IResource resource, boolean useDefault) {
 		final IDLTKLanguageToolkit toolkit = getLanguageToolkit(parent);
 		if (toolkit != null) {
 			if (DLTKContentTypeManager.isValidResourceForContentType(toolkit,
 					resource)) {
 				return toolkit;
 			}
 			/*
 			 * TODO check other toolkits of the projects when projects will be
 			 * supporting multiple DLTK languages
 			 */
 			return useDefault ? toolkit : null;
 		} else {
 			return findAppropriateToolkitByObject(resource);
 		}
 	}
 
 	public static IDLTKLanguageToolkit findToolkit(IPath path) {
 		return findAppropriateToolkitByObject(path);
 	}
 
 	public static ISourceElementParser getSourceElementParser(String nature) {
 		return (ISourceElementParser) InternalDLTKLanguageManager
 				.getSourceElementParsersManager().getObject(nature);
 	}
 
 	public static ISourceElementParser getSourceElementParser(
 			IModelElement element) {
 		return (ISourceElementParser) InternalDLTKLanguageManager
 				.getSourceElementParsersManager().getObject(element);
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	public static IBinaryElementParser getBinaryElementParser(String nature) {
 		return (IBinaryElementParser) InternalDLTKLanguageManager
 				.getBinaryElementParsersManager().getObject(nature);
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	public static IBinaryElementParser getBinaryElementParser(
 			IModelElement element) {
 		return (IBinaryElementParser) InternalDLTKLanguageManager
 				.getBinaryElementParsersManager().getObject(element);
 	}
 
 	// public static ISourceParser getSourceParser( String nature ) throws
 	// CoreException {
 	// return (ISourceElementParser) sourceParsersManager.getObject(nature);
 	// }
 	//
 	// public static ISourceParser getSourceParser( IModelElement element )
 	// throws
 	// CoreException {
 	// return (ISourceElementParser) sourceParsersManager.getObject(element);
 	// }
 
 	public static IProblemFactory getProblemFactory(String natureID) {
 		IProblemFactory factory = (IProblemFactory) InternalDLTKLanguageManager
 				.getProblemFactoryManager().getObject(natureID);
 		if (factory != null) {
 			return factory;
 		}
 		return new DefaultProblemFactory();
 	}
 
 	public static IProblemFactory getProblemFactory(IModelElement element) {
 		IProblemFactory factory = (IProblemFactory) InternalDLTKLanguageManager
 				.getProblemFactoryManager().getObject(element);
 		if (factory != null) {
 			return factory;
 		}
 		return new DefaultProblemFactory();
 	}
 
 	@Deprecated
 	public static ICompletionEngine getCompletionEngine(String natureID) {
 		final ICompletionEngine[] engines = getCompletionEngines(natureID);
 		return engines != null ? engines[0] : null;
 	}
 
 	public static ICompletionEngine[] getCompletionEngines(String natureID) {
 		return InternalDLTKLanguageManager.getCompletionEngineManager()
 				.getInstances(natureID);
 	}
 
 	public static ISelectionEngine getSelectionEngine(String natureID) {
 		return (ISelectionEngine) InternalDLTKLanguageManager
 				.getSelectionEngineManager().getObject(natureID);
 	}
 
 	public static ISourceParser getSourceParser(String natureID) {
 		return getSourceParser(null, natureID);
 	}
 
 	/**
 	 * @param project
 	 * @param natureID
 	 * @return
 	 * @since 2.0
 	 */
 	public static ISourceParser getSourceParser(IProject project,
 			String natureID) {
 		return SourceParserManager.getInstance().getSourceParser(project,
 				natureID);
 	}
 
 	public static DLTKSearchParticipant createSearchParticipant(String natureID) {
 		ISearchFactory factory = getSearchFactory(natureID);
 		if (factory != null) {
 			DLTKSearchParticipant participant = factory
 					.createSearchParticipant();
 			if (participant != null) {
 				return participant;
 			}
 		}
 		return new DLTKSearchParticipant();
 	}
 
 	public static ISearchFactory getSearchFactory(String natureId) {
 		return (ISearchFactory) InternalDLTKLanguageManager.getSearchManager()
 				.getObject(natureId);
 	}
 
 	public static IMatchLocator createMatchLocator(String natureID) {
 		return InternalDLTKLanguageManager.createMatchLocator(natureID);
 	}
 
 	public static SourceIndexerRequestor createSourceRequestor(String natureID) {
 		ISearchFactory factory = getSearchFactory(natureID);
 		if (factory != null) {
 			SourceIndexerRequestor requestor = factory.createSourceRequestor();
 			if (requestor != null) {
 				requestor.setSearchFactory(factory);
 				return requestor;
 			}
 		}
 		return new SourceIndexerRequestor();
 	}
 
 	public static IMatchLocatorParser createMatchParser(String natureID,
 			MatchLocator matchLocator) {
 		ISearchFactory factory = getSearchFactory(natureID);
 		if (factory != null) {
 			return factory.createMatchParser(matchLocator);
 		}
 		return new MatchLocatorParser(matchLocator) {
 		};
 	}
 
 	public static ICalleeProcessor createCalleeProcessor(String natureID,
 			IMethod member, IProgressMonitor progressMonitor,
 			IDLTKSearchScope scope) {
 		ICallHierarchyFactory factory = getCallHierarchyFactory(natureID);
 		if (factory != null) {
 			ICalleeProcessor processor = factory.createCalleeProcessor(member,
 					progressMonitor, scope);
 			return processor;
 		}
 		return null;
 	}
 
 	private static ICallHierarchyFactory getCallHierarchyFactory(String natureId) {
 		return (ICallHierarchyFactory) InternalDLTKLanguageManager
 				.getCallHierarchyManager().getObject(natureId);
 	}
 
 	public static ICallProcessor createCallProcessor(String natureID) {
 		ICallHierarchyFactory factory = getCallHierarchyFactory(natureID);
 		if (factory != null) {
 			return factory.createCallProcessor();
 		}
 		return null;
 	}
 
 	public static IFileHierarchyResolver getFileHierarchyResolver(
 			String natureId) {
 		return (IFileHierarchyResolver) InternalDLTKLanguageManager
 				.getFileHierarchyResolversManager().getObject(natureId);
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	public static ISearchPatternProcessor getSearchPatternProcessor(
 			String natureId) {
 		final ISearchFactory factory = getSearchFactory(natureId);
 		if (factory != null) {
 			return factory.createSearchPatternProcessor();
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	public static ISearchPatternProcessor getSearchPatternProcessor(
 			IDLTKLanguageToolkit toolkit) {
 		return getSearchPatternProcessor(toolkit, false);
 	}
 
 	/**
 	 * @since 3.0
 	 */
 	public static ISearchPatternProcessor getSearchPatternProcessor(
 			IDLTKLanguageToolkit toolkit, boolean allowDefault) {
 		if (toolkit != null) {
 			final ISearchPatternProcessor processor = getSearchPatternProcessor(toolkit
 					.getNatureId());
 			if (processor != null) {
 				return processor;
 			}
 		}
 		if (allowDefault) {
 			return SearchPatternProcessor.getDefault();
 		} else {
 			return null;
 		}
 	}
 
 	private static final String FILENAME_ASSOCIATION_EXT_POINT = DLTKCore.PLUGIN_ID
 			+ ".filenameAssociation"; //$NON-NLS-1$
 
 	/**
 	 * @since 2.0
 	 */
 	public static Set<String> loadFilenameAssociations(final String natureId) {
 		final IConfigurationElement[] elements = Platform
 				.getExtensionRegistry().getConfigurationElementsFor(
 						FILENAME_ASSOCIATION_EXT_POINT);
 		final Set<String> patterns = new HashSet<String>();
 		for (IConfigurationElement element : elements) {
 			if (natureId.equals(element.getAttribute("nature"))) { //$NON-NLS-1$
 				final String pattern = element.getAttribute("pattern"); //$NON-NLS-1$
 				if (pattern != null && pattern.length() != 0) {
 					patterns.add(pattern);
 				}
 			}
 		}
 		return patterns;
 	}
 }
