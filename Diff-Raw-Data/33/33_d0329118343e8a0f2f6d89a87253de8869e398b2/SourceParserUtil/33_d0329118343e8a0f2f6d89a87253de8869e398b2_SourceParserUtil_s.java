 package org.eclipse.dltk.core;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
 import org.eclipse.dltk.ast.parser.ISourceParser;
 import org.eclipse.dltk.ast.parser.ISourceParserConstants;
 import org.eclipse.dltk.ast.parser.ISourceParserExtension;
 import org.eclipse.dltk.compiler.problem.IProblemReporter;
 import org.eclipse.dltk.core.ISourceModuleInfoCache.ISourceModuleInfo;
 import org.eclipse.dltk.internal.core.ModelManager;
 
 public class SourceParserUtil {
 	private static final Object AST = "ast";
 
 	/**
 	 * Used to retrive module declaration from source module.
 	 */
 	public static ModuleDeclaration getModuleDeclaration(ISourceModule module,
 			IProblemReporter reporter) {
 		return getModuleDeclaration(module, reporter, ISourceParserConstants.DEFAULT);
 	}
 	public static ModuleDeclaration getModuleDeclaration(ISourceModule module,
 			IProblemReporter reporter, int flags) {
 		ISourceModuleInfoCache sourceModuleInfoCache = ModelManager
 				.getModelManager().getSourceModuleInfoCache();
 		ISourceModuleInfo sourceModuleInfo = sourceModuleInfoCache.get(module);
 		return getModuleDeclaration(module, reporter, sourceModuleInfo, flags);
 	}
 
 	/**
 	 * Used to retrive module declaration from source module.
 	 */
 
 	public static ModuleDeclaration getModuleDeclaration(ISourceModule module,
 			IProblemReporter reporter, ISourceModuleInfo mifo) {
 		return getModuleDeclaration(module, reporter, mifo, ISourceParserConstants.DEFAULT);
 	}
 	public static ModuleDeclaration getModuleDeclaration(ISourceModule module,
 			IProblemReporter reporter, ISourceModuleInfo mifo, int flags) {
 
 		IDLTKLanguageToolkit toolkit;
 		try {
 			toolkit = DLTKLanguageManager.getLanguageToolkit(module);
 		} catch (CoreException e1) {
 			if (DLTKCore.DEBUG) {
 				e1.printStackTrace();
 			}
 			return null;
 		}
 		ModuleDeclaration moduleDeclaration = null;
 		if (mifo != null) {
 			moduleDeclaration = (ModuleDeclaration) mifo.get(AST);
 		}
 		if (moduleDeclaration == null) {
 			if (reporter != null) {
 				reporter.clearMarkers();
 			}
 			ISourceParser sourceParser = null;
 			try {
 				sourceParser = DLTKLanguageManager.getSourceParser(toolkit
 						.getNatureId());
 			} catch (CoreException e) {
 				if (DLTKCore.DEBUG) {
 					e.printStackTrace();
 				}
 			}
 			if (sourceParser != null) {
 				if (sourceParser instanceof ISourceParserExtension) {
 					((ISourceParserExtension) sourceParser).setFlags(flags);
 				}
 				try {
 					moduleDeclaration = sourceParser.parse(module.getPath()
 							.toOSString().toCharArray(), module
 							.getSourceAsCharArray(), reporter);
 				} catch (ModelException e) {
 					if (DLTKCore.DEBUG) {
 						e.printStackTrace();
 					}
 				}
 				if (moduleDeclaration != null && mifo != null) {
 					mifo.put(AST, moduleDeclaration);
 				}
 			}
 		}
 		return moduleDeclaration;
 	}
 
 	public static ModuleDeclaration getModuleDeclaration(char[] filename,
 			char[] content, String nature, IProblemReporter reporter,
 			ISourceModuleInfo mifo) {
 		return getModuleDeclaration(filename, content, nature, reporter, mifo,
 				ISourceParserConstants.DEFAULT);
 	}
 
 	public static ModuleDeclaration getModuleDeclaration(char[] filename,
 			char[] content, String nature, IProblemReporter reporter,
 			ISourceModuleInfo mifo, int flags) {
 		ISourceParser sourceParser;// = new PythonSourceParser(this.fReporter);
 		try {
 			sourceParser = DLTKLanguageManager.getSourceParser(nature);
 		} catch (CoreException e) {
 			throw new RuntimeException("Failed to create python source parser",
 					e);
 		}
 		if (sourceParser instanceof ISourceParserExtension) {
 			((ISourceParserExtension) sourceParser).setFlags(flags);
 		}
 		ModuleDeclaration moduleDeclaration = SourceParserUtil
				.getModuleFromCache(mifo);
 		if (moduleDeclaration == null) {
 			if (reporter != null) {
 				reporter.clearMarkers();
 			}
 			moduleDeclaration = sourceParser.parse(filename, content, reporter);
			SourceParserUtil.putModuleToCache(mifo, moduleDeclaration);
 		}
 		return moduleDeclaration;
 	}
 
 	/**
 	 * This is for use in parsers.
 	 */
	public static ModuleDeclaration getModuleFromCache(ISourceModuleInfo mifo) {
 		if (mifo != null) {
 			return (ModuleDeclaration) mifo.get(AST);
 		}
 		return null;
 	}
 
	/**
	 * This is for use in parsers.
	 */
 	public static void putModuleToCache(ISourceModuleInfo info,
			ModuleDeclaration module) {
 		if (info != null) {
 			info.put(AST, module);
 		}
 	}
 }
