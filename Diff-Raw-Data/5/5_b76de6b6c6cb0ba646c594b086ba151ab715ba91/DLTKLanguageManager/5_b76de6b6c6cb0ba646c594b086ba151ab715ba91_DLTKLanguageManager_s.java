 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.core;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.dltk.ast.parser.ISourceParser;
 import org.eclipse.dltk.ast.parser.SourceParserManager;
 import org.eclipse.dltk.codeassist.ICompletionEngine;
 import org.eclipse.dltk.codeassist.ISelectionEngine;
 import org.eclipse.dltk.compiler.problem.DefaultProblemFactory;
 import org.eclipse.dltk.compiler.problem.IProblemFactory;
 import org.eclipse.dltk.core.PriorityDLTKExtensionManager.ElementInfo;
 import org.eclipse.dltk.core.search.DLTKSearchParticipant;
 import org.eclipse.dltk.core.search.IDLTKSearchScope;
 import org.eclipse.dltk.core.search.IMatchLocatorParser;
 import org.eclipse.dltk.core.search.SearchPattern;
 import org.eclipse.dltk.core.search.SearchRequestor;
 import org.eclipse.dltk.core.search.indexing.SourceIndexerRequestor;
 import org.eclipse.dltk.core.search.matching.MatchLocator;
 import org.eclipse.dltk.internal.core.InternalDLTKLanguageManager;
 
 public class DLTKLanguageManager {
 
 	public static IDLTKLanguageToolkit getLanguageToolkit(String natureId)
 			throws CoreException {
 
 		return (IDLTKLanguageToolkit) InternalDLTKLanguageManager
 				.getLanguageToolkitsManager().getObject(natureId);
 	}
 
 	public static IDLTKLanguageToolkit[] getLanguageToolkits()
 			throws CoreException {
 
 		ElementInfo[] elementInfos = InternalDLTKLanguageManager
 				.getLanguageToolkitsManager().getElementInfos();
 		IDLTKLanguageToolkit[] toolkits = new IDLTKLanguageToolkit[elementInfos.length];
 		for (int j = 0; j < elementInfos.length; j++) {
 			toolkits[j] = (IDLTKLanguageToolkit) InternalDLTKLanguageManager
 					.getLanguageToolkitsManager()
 					.getInitObject(elementInfos[j]);
 		}
 		return toolkits;
 	}
 
 	private static IDLTKLanguageToolkit findAppropriateToolkitByObject(
 			Object object) {
 		ElementInfo[] elementInfos = InternalDLTKLanguageManager
 				.getLanguageToolkitsManager().getElementInfos();
 		for (int j = 0; j < elementInfos.length; j++) {
 			IDLTKLanguageToolkit toolkit = (IDLTKLanguageToolkit) InternalDLTKLanguageManager
 					.getLanguageToolkitsManager()
 					.getInitObject(elementInfos[j]);
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
 
 	public static IDLTKLanguageToolkit getLanguageToolkit(IModelElement element)
 			throws CoreException {
 		IDLTKLanguageToolkit toolkit = (IDLTKLanguageToolkit) InternalDLTKLanguageManager
 				.getLanguageToolkitsManager().getObject(element);
 		if (toolkit == null
 				&& element.getElementType() == IModelElement.SOURCE_MODULE) {
 			return findAppropriateToolkitByObject(element.getPath());
 		}
 		return toolkit;
 	}
 
 	public static IDLTKLanguageToolkit findToolkit(IResource resource) {
 		return findAppropriateToolkitByObject(resource);
 	}
 
 	public static IDLTKLanguageToolkit findToolkit(IPath path) {
 		return findAppropriateToolkitByObject(path);
 	}
 
 	public static ISourceElementParser getSourceElementParser(String nature)
 			throws CoreException {
 		return (ISourceElementParser) InternalDLTKLanguageManager
 				.getSourceElementParsersManager().getObject(nature);
 	}
 
 	public static ISourceElementParser getSourceElementParser(
 			IModelElement element) throws CoreException {
 		return (ISourceElementParser) InternalDLTKLanguageManager
 				.getSourceElementParsersManager().getObject(element);
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
 
 	public static IProblemFactory getProblemFactory(String natureID)
 			throws CoreException {
 		IProblemFactory factory = (IProblemFactory) InternalDLTKLanguageManager
 				.getProblemFactoryManager().getObject(natureID);
 		if (factory != null) {
 			return factory;
 		}
 		return new DefaultProblemFactory();
 	}
 
 	public static IProblemFactory getProblemFactory(IModelElement element)
 			throws CoreException {
 		IProblemFactory factory = (IProblemFactory) InternalDLTKLanguageManager
 				.getProblemFactoryManager().getObject(element);
 		if (factory != null) {
 			return factory;
 		}
 		return new DefaultProblemFactory();
 	}
 
 	public static ICompletionEngine getCompletionEngine(String natureID)
 			throws CoreException {
 		return (ICompletionEngine) InternalDLTKLanguageManager
 				.getCompletionEngineManager().getObject(natureID);
 	}
 
 	public static ISelectionEngine getSelectionEngine(String natureID)
 			throws CoreException {
 		return (ISelectionEngine) InternalDLTKLanguageManager
 				.getSelectionEngineManager().getObject(natureID);
 	}
 
 	public static ISourceParser getSourceParser(String natureID)
 			throws CoreException {
 		return SourceParserManager.getInstance()
 				.getSourceParser(null, natureID);
 	}
 
 	// /**
 	// * Return source parser witch is one level lower from top. If this is only
 	// * one source parser for selected nature then return null.
 	// *
 	// */
 	// public static ISourceParser getSourceParserLower(String natureID)
 	// throws CoreException {
 	// return (ISourceParser)
 	// InternalDLTKLanguageManager.getSourceParsersManager().getObjectLower(natureID);
 	// }
 
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
 
 	private static ISearchFactory getSearchFactory(String natureId) {
 		return (ISearchFactory) InternalDLTKLanguageManager.getSearchManager()
 				.getObject(natureId);
 	}
 
 	public static MatchLocator createMatchLocator(String natureID,
 			SearchPattern pattern, SearchRequestor requestor,
 			IDLTKSearchScope scope, SubProgressMonitor subProgressMonitor) {
 		ISearchFactory factory = getSearchFactory(natureID);
 		if (factory != null) {
 			MatchLocator locator = factory.createMatchLocator(pattern,
 					requestor, scope, subProgressMonitor);
 			if (locator != null) {
 				return locator;
 			}
 		}
 		return new MatchLocator(pattern, requestor, scope, subProgressMonitor);
 	}
 
 	public static SourceIndexerRequestor createSourceRequestor(String natureID) {
 		ISearchFactory factory = getSearchFactory(natureID);
 		if (factory != null) {
 			SourceIndexerRequestor requestor = factory.createSourceRequestor();
 			if (requestor != null) {
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
		return null;
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
 }
