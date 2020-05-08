 package org.eclipse.dltk.ruby.internal.typehierarchy;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.dltk.core.DLTKLanguageManager;
 import org.eclipse.dltk.core.IType;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.WorkingCopyOwner;
 import org.eclipse.dltk.core.search.IDLTKSearchConstants;
 import org.eclipse.dltk.core.search.IDLTKSearchScope;
 import org.eclipse.dltk.core.search.SearchEngine;
 import org.eclipse.dltk.core.search.SearchPattern;
 import org.eclipse.dltk.core.search.TypeNameMatch;
 import org.eclipse.dltk.core.search.TypeNameMatchRequestor;
 import org.eclipse.dltk.ruby.core.RubyPlugin;
 
 public class RubyTypeHierarchyEngine {
 	public static IType[] locateSuperTypes( IType type, IProgressMonitor monitor ) {
 		try {
 			String[] superTypes = type.getSuperClasses();
 			List types = new ArrayList();
 			monitor.beginTask("Collecting types...", superTypes.length);
 			IDLTKSearchScope scope = SearchEngine.createWorkspaceScope(DLTKLanguageManager.getLangaugeToolkit(type));
 			for( int i = 0; i < superTypes.length; ++i ) {
				if( RubyPlugin.DEBUG ) {
 					System.out.println("Type:" + type.getElementName() + " has supertype:" + superTypes[i]);
 				}
 				TypeNameMatch[] possibleTypes = searchTypesForName(superTypes[i], monitor, scope);
 				for( int j = 0; j < possibleTypes.length; ++i ) {
 					IType sType = possibleTypes[j].getType();
 					if( sType.exists() && filterTypeFromSelection(sType, type)) {
 						types.add(sType);
 					}
 				}
 			}
 			return (IType[])types.toArray(new IType[types.size()]);
 		} catch (ModelException e) {
 			e.printStackTrace();
 		} catch (CoreException e) {
 			e.printStackTrace();
 		}
 		finally {
 			monitor.done();
 		}
 		return null;
 	}
 	private static boolean filterTypeFromSelection(IType superType, IType type ) {
 		return true;
 	}
 	private static TypeNameMatch[] searchTypesForName(String name, IProgressMonitor monitor, IDLTKSearchScope scope ) {
 		SearchRequestor reqestor = new SearchRequestor();
 		SearchEngine engine= new SearchEngine((WorkingCopyOwner)null);
 		monitor.setTaskName("Search types...");
 		try {
 			engine.searchAllTypeNames(
 				null, 
 				0,
 				name.toCharArray(), 
 				SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE, 
 				IDLTKSearchConstants.TYPE, 
 				scope, 
 				reqestor, 
 				IDLTKSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, 
 				monitor);
 			return reqestor.getResult();
 		} catch (ModelException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	private static class SearchRequestor extends TypeNameMatchRequestor {
 		private volatile boolean fStop;
 
 		private List fResult;
 		
 		public SearchRequestor() {
 			super();
 			fResult= new ArrayList(5);
 		}
 		public TypeNameMatch[] getResult() {
 			return (TypeNameMatch[])fResult.toArray(new TypeNameMatch[fResult.size()]);
 		}
 		public void cancel() {
 			fStop= true;
 		}
 		/* (non-Javadoc)
 		 * @see org.eclipse.jdt.core.search.TypeNameMatchRequestor#acceptTypeNameMatch(org.eclipse.jdt.core.search.TypeNameMatch)
 		 */
 		public void acceptTypeNameMatch(TypeNameMatch match) {
 			if (fStop)
 				return;
 			fResult.add(match);
 		}
 	}
 }
