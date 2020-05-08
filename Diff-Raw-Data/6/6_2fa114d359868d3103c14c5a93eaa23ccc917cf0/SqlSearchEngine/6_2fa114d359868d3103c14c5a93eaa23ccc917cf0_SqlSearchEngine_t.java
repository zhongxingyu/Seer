 /*******************************************************************************
  * Copyright (c) 2009 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *     Zend Technologies
  *******************************************************************************/
 package org.eclipse.dltk.internal.core.index.sql;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.IDLTKLanguageToolkitExtension;
 import org.eclipse.dltk.core.IProjectFragment;
 import org.eclipse.dltk.core.IScriptFolder;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.index.sql.Container;
 import org.eclipse.dltk.core.index.sql.DbFactory;
 import org.eclipse.dltk.core.index.sql.Element;
 import org.eclipse.dltk.core.index.sql.File;
 import org.eclipse.dltk.core.index.sql.IElementHandler;
 import org.eclipse.dltk.core.index.sql.SqlIndex;
 import org.eclipse.dltk.core.index2.search.ISearchEngine;
 import org.eclipse.dltk.core.index2.search.ISearchRequestor;
 import org.eclipse.dltk.core.search.IDLTKSearchScope;
 import org.eclipse.dltk.internal.core.search.DLTKSearchScope;
 import org.eclipse.dltk.internal.core.search.DLTKWorkspaceScope;
 
 /**
  * Search engine implementation for SQL-based index.
  * 
  * @author michael
  * @since 2.0
  */
 public class SqlSearchEngine implements ISearchEngine {
 
 	public void search(int elementType, String qualifier, String elementName,
 			int trueFlags, int falseFlags, int limit, SearchFor searchFor,
 			MatchRule matchRule, IDLTKSearchScope scope,
 			final ISearchRequestor requestor, IProgressMonitor monitor) {
 
 		try {
 			DbFactory dbFactory = DbFactory.getInstance();
 			if (dbFactory == null) {
 				return;
 			}
 			Connection connection = dbFactory.createConnection();
 			if (connection == null) {
 				return;
 			}
 			try {
 				String natureId = scope.getLanguageToolkit().getNatureId();
 				ElementHandler elementHandler = new ElementHandler(connection,
 						scope, requestor);
 
 				// Calculate filtering by container:
 				int[] containersId = null;
 				if (!(scope instanceof DLTKWorkspaceScope)) {
 					IPath[] containerPaths = scope.enclosingProjectsAndZips();
 					List<Integer> l = new LinkedList<Integer>();
 					for (int i = 0; i < containerPaths.length; ++i) {
 						Container container = dbFactory.getContainerDao()
 								.selectByPath(connection,
 										containerPaths[i].toString());
 						if (container != null) {
 							l.add(container.getId());
 						}
 					}
 					if (l.size() > 0) {
 						containersId = new int[l.size()];
 						for (int i = 0; i < l.size(); ++i) {
 							containersId[i] = l.get(i);
 						}
 					}
 				}
 
 				boolean searchForDecls = searchFor == SearchFor.DECLARATIONS
 						|| searchFor == SearchFor.ALL_OCCURENCES;
 				boolean searchForRefs = searchFor == SearchFor.REFERENCES
 						|| searchFor == SearchFor.ALL_OCCURENCES;
 
 				if (searchForDecls) {
 					dbFactory.getElementDao().search(connection, elementName,
 							matchRule, elementType, trueFlags, falseFlags,
 							qualifier, null, null, containersId, natureId,
 							limit, false, elementHandler, monitor);
 				}
 				if (searchForRefs) {
 					dbFactory.getElementDao().search(connection, elementName,
 							matchRule, elementType, trueFlags, falseFlags,
 							qualifier, null, null, containersId, natureId,
 							limit, true, elementHandler, monitor);
 				}
 			} finally {
 				connection.close();
 			}
 		} catch (SQLException e) {
 			SqlIndex.error("An exception has thrown while performing a search",
 					e);
 		}
 	}
 
 	class ElementHandler implements IElementHandler, ISearchRequestor {
 
 		private Map<Integer, File> fileCache = new HashMap<Integer, File>();
 		private Map<Integer, Container> containerCache = new HashMap<Integer, Container>();
 		private Map<String, IProjectFragment> projectFragmentCache = new HashMap<String, IProjectFragment>();
 		private Map<String, ISourceModule> sourceModuleCache = new HashMap<String, ISourceModule>();
 		private Connection connection;
 		private ISearchRequestor searchRequestor;
 		private IDLTKSearchScope scope;
 
 		public ElementHandler(Connection connection, IDLTKSearchScope scope,
 				ISearchRequestor searchRequestor) {
 
 			this.connection = connection;
 			this.scope = scope;
 			this.searchRequestor = searchRequestor;
 		}
 
 		public void handle(Element element) {
 			try {
 				DbFactory dbFactory = DbFactory.getInstance();
 
 				int fileId = element.getFileId();
 				File file = fileCache.get(fileId);
 				if (file == null) {
 					file = dbFactory.getFileDao()
 							.selectById(connection, fileId);
 					if (file == null) {
 						return;
 					}
 					fileCache.put(fileId, file);
 				}
 
 				int containerId = file.getContainerId();
 				Container container = containerCache.get(containerId);
 				if (container == null) {
 					container = dbFactory.getContainerDao().selectById(
 							connection, containerId);
 					if (container == null) {
 						return;
 					}
 					containerCache.put(containerId, container);
 				}
 
 				String containerPath = container.getPath();
 				IDLTKLanguageToolkit toolkit = ((DLTKSearchScope) scope)
 						.getLanguageToolkit();
 				if (toolkit instanceof IDLTKLanguageToolkitExtension
 						&& ((IDLTKLanguageToolkitExtension) toolkit)
 								.isArchiveFileName(containerPath)) {
 					containerPath = containerPath
 							+ IDLTKSearchScope.FILE_ENTRY_SEPARATOR;
 				}
 
 				String resourcePath = new Path(containerPath).append(
 						file.getPath()).toString();
 
 				IProjectFragment projectFragment = projectFragmentCache
 						.get(containerPath);
 
 				if (projectFragment == null) {
 					projectFragment = ((DLTKSearchScope) scope)
							.projectFragment(containerPath);
 					projectFragmentCache.put(containerPath, projectFragment);
 				}
 				if (projectFragment == null) {
 					return;
 				}
 
 				Path relativePath = new Path(file.getPath());
 				if (!scope.encloses(resourcePath)) {
 					return;
 				}
 				ISourceModule sourceModule = sourceModuleCache
 						.get(resourcePath);
 				if (sourceModule == null) {
 					IScriptFolder scriptFolder = projectFragment
 							.getScriptFolder(relativePath.removeLastSegments(1));
 					sourceModule = scriptFolder.getSourceModule(relativePath
 							.lastSegment());
 					sourceModuleCache.put(resourcePath, sourceModule);
 				}
 
 				match(element.getType(), element.getFlags(), element
 						.getOffset(), element.getLength(), element
 						.getNameOffset(), element.getNameLength(), element
 						.getName(), element.getMetadata(), element
 						.getQualifier(), element.getParent(), sourceModule,
 						element.isReference());
 
 			} catch (SQLException e) {
 				SqlIndex.error(
 						"An exception is thrown while searching elements", e);
 			}
 		}
 
 		public void match(int elementType, int flags, int offset, int length,
 				int nameOffset, int nameLength, String elementName,
 				String metadata, String qualifier, String parent,
 				ISourceModule sourceModule, boolean isReference) {
 
 			searchRequestor.match(elementType, flags, offset, length,
 					nameOffset, nameLength, elementName, metadata, qualifier,
 					parent, sourceModule, isReference);
 		}
 	}
 }
