 package com.gamevm.plugin.builder;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.List;
 import java.util.Map;
 
 import org.antlr.runtime.ANTLRInputStream;
 import org.antlr.runtime.CharStream;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.RecognitionException;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.resources.IResourceVisitor;
 import org.eclipse.core.resources.IncrementalProjectBuilder;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 
 import com.gamevm.compiler.assembly.ClassDefinition;
 import com.gamevm.compiler.assembly.GClassLoader;
 import com.gamevm.compiler.parser.ASTNode;
 import com.gamevm.compiler.parser.GCASTLexer;
 import com.gamevm.compiler.parser.GCASTParser;
 import com.gamevm.compiler.parser.ParserError;
 import com.gamevm.compiler.translator.TranslationException;
 import com.gamevm.compiler.translator.ast.ASTTranslator;
 import com.gamevm.compiler.translator.ast.SymbolTable;
 import com.gamevm.execution.ast.TreeCodeWriter;
 import com.gamevm.execution.ast.tree.Statement;
 
 public class GCBuilder extends IncrementalProjectBuilder {
 
 	public static final String BUILDER_ID = "com.gamevm.plugin.builder.gcbuilder";
 
 	private static final String MARKER_TYPE = "com.gamevm.plugin.marker.compilationError";
 	
 	class SampleDeltaVisitor implements IResourceDeltaVisitor {
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
 		 */
 		public boolean visit(IResourceDelta delta) throws CoreException {
 			IResource resource = delta.getResource();
 			switch (delta.getKind()) {
 			case IResourceDelta.ADDED:
 				// handle added resource
 				compile(resource);
 				break;
 			case IResourceDelta.REMOVED:
 				// handle removed resource
 				break;
 			case IResourceDelta.CHANGED:
 				// handle changed resource
 				compile(resource);
 				break;
 			}
 			//return true to continue visiting children.
 			return true;
 		}
 	}
 
 	class SampleResourceVisitor implements IResourceVisitor {
 		public boolean visit(IResource resource) {
 			compile(resource);
 			//return true to continue visiting children.
 			return true;
 		}
 	}
 
 	private void addMarker(IFile file, String message, int lineNumber,
 			int severity) {
 		try {
 			IMarker marker = file.createMarker(MARKER_TYPE);
 			marker.setAttribute(IMarker.MESSAGE, message);
 			marker.setAttribute(IMarker.SEVERITY, severity);
 			if (lineNumber == -1) {
 				lineNumber = 1;
 			}
 			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
 		} catch (CoreException e) {
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int,
 	 *      java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
 			throws CoreException {
 		if (kind == FULL_BUILD) {
 			fullBuild(monitor);
 		} else {
 			IResourceDelta delta = getDelta(getProject());
 			if (delta == null) {
 				fullBuild(monitor);
 			} else {
 				incrementalBuild(delta, monitor);
 			}
 		}
 		return null;
 	}
 
 	void compile(IResource resource) {
 		if (resource instanceof IFile && resource.getName().endsWith(".gc")) {
 			IFile file = (IFile) resource;
 			deleteMarkers(file);
 			
 			try {
 				CharStream charStream = new ANTLRInputStream(file.getContents());
 				GCASTLexer lexer = new GCASTLexer(charStream);
 				GCASTParser parser = new GCASTParser(new CommonTokenStream(lexer));
 				ClassDefinition<ASTNode> ast = parser.program();
 				
 				List<ParserError> errors = parser.getErrors();
 				for (ParserError e : errors) {
 					addMarker(file, e.getMessage(parser), e.getLine(), IMarker.SEVERITY_ERROR);
 				}
 				
 				IFolder classPath = getProject().getFolder("bin");
 				
 				ASTTranslator translator = new ASTTranslator(new SymbolTable(ast.getDeclaration(), new GClassLoader(classPath.getLocation().toFile())), true);
 				
 				ClassDefinition<Statement> statements = new ClassDefinition<Statement>(ast, translator);
 				
				List<TranslationException> transErrors = translator.getErrors();
				for (TranslationException e : transErrors) {
					addMarker(file, e.getLocalizedMessage(), e.getNode().getStartLine(), IMarker.SEVERITY_ERROR);
				}
				
				if (errors.size() == 0 && transErrors.size() == 0) {
					String name = statements.getDeclaration().getName();
					File classFile = new File(classPath.getLocation().toFile(), name.replace('.', '/') + ".gbc");
					classFile.getParentFile().mkdirs();
					if (!classFile.exists())
						classFile.createNewFile();
					OutputStream output = new FileOutputStream(classFile);
					statements.write(output, new TreeCodeWriter());
				}
 				
 			} catch (IOException e) {
 				addMarker(file, "Illegal class file format", 1, IMarker.SEVERITY_ERROR);
 				e.printStackTrace();
 			} catch (CoreException e) {
 				e.printStackTrace();
 			} catch (RecognitionException e) {
 				addMarker(file, e.getLocalizedMessage(), e.line, IMarker.SEVERITY_ERROR);
 				e.printStackTrace();
 			} catch (TranslationException e) {
 				addMarker(file, e.getLocalizedMessage(), e.getNode().getStartLine(), IMarker.SEVERITY_ERROR);
 				e.printStackTrace();
 			}
 			
 		}
 	}
 
 	private void deleteMarkers(IFile file) {
 		try {
 			file.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO);
 		} catch (CoreException ce) {
 		}
 	}
 
 	protected void fullBuild(final IProgressMonitor monitor)
 			throws CoreException {
 		try {
 			getProject().accept(new SampleResourceVisitor());
 		} catch (CoreException e) {
 		}
 	}
 
 	protected void incrementalBuild(IResourceDelta delta,
 			IProgressMonitor monitor) throws CoreException {
 		// the visitor does the work.
 		delta.accept(new SampleDeltaVisitor());
 	}
 }
