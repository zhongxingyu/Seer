 package org.eclipse.dltk.python.internal.core.search;
 
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.dltk.core.search.AbstractSearchFactory;
 import org.eclipse.dltk.core.search.IDLTKSearchScope;
 import org.eclipse.dltk.core.search.IMatchLocatorParser;
 import org.eclipse.dltk.core.search.SearchPattern;
 import org.eclipse.dltk.core.search.SearchRequestor;
 import org.eclipse.dltk.core.search.matching.MatchLocator;
 
 /**
  * Python search factory
  */
 public class PythonSearchFactory extends AbstractSearchFactory {
 
	/*
	 * @see org.eclipse.dltk.core.ISearchFactory#createMatchLocator(org.eclipse.dltk.core.search.SearchPattern,
	 *      org.eclipse.dltk.core.search.SearchRequestor,
	 *      org.eclipse.dltk.core.search.IDLTKSearchScope,
	 *      org.eclipse.core.runtime.SubProgressMonitor)
	 */
 	public MatchLocator createMatchLocator(SearchPattern pattern,
 			SearchRequestor requestor, IDLTKSearchScope scope,
 			SubProgressMonitor monitor) {
 		return new PythonMatchLocator(pattern, requestor, scope, monitor);
 	}
 
	/*
	 * @see org.eclipse.dltk.core.ISearchFactory#createMatchParser(org.eclipse.dltk.core.search.matching.MatchLocator)
	 */
 	public IMatchLocatorParser createMatchParser(MatchLocator locator) {
 		return new PythonMatchLocationParser(locator);
 	}
 }
