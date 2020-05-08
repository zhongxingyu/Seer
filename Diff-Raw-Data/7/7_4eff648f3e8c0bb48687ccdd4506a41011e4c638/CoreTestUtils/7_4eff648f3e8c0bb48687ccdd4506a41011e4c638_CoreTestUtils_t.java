 package mmrnmhrm.tests;
 
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.net.URL;
 
 import melnorme.miscutil.StringUtil;
 import mmrnmhrm.core.model.CompilationUnit;
 import mmrnmhrm.core.model.DeePackageFragment;
 import mmrnmhrm.core.model.EModelStatus;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.osgi.framework.Bundle;
 
 import descent.internal.core.dom.ParserFacade;
 import dtool.descentadapter.DescentASTConverter;
 import dtool.dom.ast.ASTNode;
 import dtool.dom.definitions.Module;
 
 
 public class CoreTestUtils {
 
 	
 	public static ASTNode testParser(final String source) throws CoreException {
 
		descent.internal.core.dom.Module mod = ParserFacade.parseCompilationUnit(source).mod;
 		BasePluginTest.assertTrue(mod.getProblems().length == 0,
 				"Found syntax errors while parsing.");
 
		Module neoModule = DescentASTConverter.convertModule(mod);
 		return neoModule;
 	}
 
 	/** FIXME, copy each file ourselfs, to prevent copying .svn files. */
 	static IFolder createWorkspaceFolderFromBundle(String srcpath, IContainer parent, String destname)
 			throws CoreException, URISyntaxException, IOException {
 		Bundle bundle = Platform.getBundle(DeeTestsPlugin.PLUGIN_ID);
 		IPath bundlesrcpath = new Path(DeeTestsPlugin.TESTDATA + srcpath);
 		URL sampleURL = FileLocator.find(bundle, bundlesrcpath, null);
 		IFolder folder = parent.getFolder(new Path("__"+destname+"link"));
 		folder.createLink(FileLocator.toFileURL(sampleURL).toURI(), IResource.NONE, null);
 		
 		IPath projpath = parent.getFullPath(); 
 		folder.copy(projpath.append(destname), false, null);
 		folder.delete(false, null);
 		return parent.getFolder(new Path(destname));
 	}
 }
