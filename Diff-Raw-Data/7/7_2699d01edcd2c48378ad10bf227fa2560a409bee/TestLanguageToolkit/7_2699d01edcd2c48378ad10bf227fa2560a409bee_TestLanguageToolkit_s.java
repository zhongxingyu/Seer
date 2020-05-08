 package org.eclipse.dltk.core.tests.model;
 
 import java.text.MessageFormat;
 import java.util.Map;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
 import org.eclipse.dltk.codeassist.ICompletionEngine;
 import org.eclipse.dltk.codeassist.ISelectionEngine;
 import org.eclipse.dltk.compiler.IProblemFactory;
 import org.eclipse.dltk.compiler.IProblemReporter;
 import org.eclipse.dltk.compiler.ISourceElementRequestor;
 import org.eclipse.dltk.core.CompletionRequestor;
 import org.eclipse.dltk.core.ICallProcessor;
 import org.eclipse.dltk.core.ICalleeProcessor;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.IDLTKProject;
 import org.eclipse.dltk.core.IMethod;
 import org.eclipse.dltk.core.IModelStatus;
 import org.eclipse.dltk.core.ISearchableEnvironment;
 import org.eclipse.dltk.core.ISourceElementParser;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.search.DLTKSearchParticipant;
 import org.eclipse.dltk.core.search.IDLTKSearchScope;
 import org.eclipse.dltk.core.search.IMatchLocatorParser;
 import org.eclipse.dltk.core.search.SearchPattern;
 import org.eclipse.dltk.core.search.SearchRequestor;
 import org.eclipse.dltk.core.search.indexing.SourceIndexerRequestor;
 import org.eclipse.dltk.core.search.matching.MatchLocator;
 import org.eclipse.dltk.internal.core.util.Messages;
 
 public class TestLanguageToolkit implements IDLTKLanguageToolkit {
 	private static TestLanguageToolkit toolkit = new TestLanguageToolkit();
 
 	public IProblemReporter createProblemReporter(IResource resource,
 			IProblemFactory factory) {
 		return null;
 	}
 
 	public IProblemFactory createProblemFactory() {
 		return null;
 	}
 
 	public ISourceElementParser createSourceElementParser(
 			ISourceElementRequestor requestor,
 			IProblemReporter problemReporter, Map options) throws CoreException {
 		return new TestSourceElementParser(requestor);
 	}
 
 	public IStatus validateSourceModule(String name) {
 		if (name.endsWith(".txt")) {
 			return IModelStatus.VERIFIED_OK;
 		}
 		return new Status(IStatus.ERROR, "TEST", -1, MessageFormat.format(
 				Messages.convention_unit_notScriptName, new String[] { "txt",
 						"Test" }), null);
 	}
 
 	public boolean languageSupportZIPBuildpath() {
 		return true;
 	}
 
 	public boolean validateSourcePackage(IPath path) {
 		return true;
 	}
 
 	public String getNatureID() {
 		return ModelTestsPlugin.TEST_NATURE;
 	}
 
 	public ICompletionEngine createCompletionEngine(
 			ISearchableEnvironment environment, CompletionRequestor requestor,
 			Map options, IDLTKProject project) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public String getPartitioningID() {
 		return "";
 	}
 
 	public IMatchLocatorParser createMatchParser(MatchLocator locator) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public IStatus validateSourceModule(IResource resource) {
 		return validateSourceModule(resource.getName());
 	}
 
 	public IStatus validateSourceModule(IPath resource) {
 		return validateSourceModule(resource.lastSegment());
 	}
 
 	public IStatus validateSourceModuleName(String str) {
 		return validateSourceModule(str);
 	}
 
 	public String getEditorID(Object inputElement) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public ModuleDeclaration createFullAST(ISourceModule module) {
 		return new ModuleDeclaration(0);
 	}
 
 	public ISelectionEngine createSelectionEngine(
 			ISearchableEnvironment environment, Map options) {
 		return null;
 	}
 
 	public SourceIndexerRequestor createSourceRequestor() {
 		return new SourceIndexerRequestor();
 	}
 
 	public DLTKSearchParticipant createSearchParticipant() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public static IDLTKLanguageToolkit getDefault() {
 		return toolkit;
 	}
 
 	public MatchLocator createMatchLocator(SearchPattern pattern,
 			SearchRequestor requestor, IDLTKSearchScope scope,
 			SubProgressMonitor monitor) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public ICalleeProcessor createCalleeProcessor(IMethod method,
 			IProgressMonitor monitor, IDLTKSearchScope scope) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public String getDelimeterReplacerString() {
 		return ".";
 	}
 
 	public ICallProcessor createCallProcessor() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public String[] getLanguageFileExtensions() {
 		return null;
 	}
 }
