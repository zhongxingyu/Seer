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
 
 	public MatchLocator createMatchLocator(SearchPattern pattern,
 			SearchRequestor requestor, IDLTKSearchScope scope,
 			SubProgressMonitor monitor) {
 		return new PythonMatchLocator(pattern, requestor, scope, monitor);
 	}
 
 	public IMatchLocatorParser createMatchParser(MatchLocator locator) {
 		return new PythonMatchLocationParser(locator);
 	}
 }
