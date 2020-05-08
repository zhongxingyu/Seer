 package org.jmlspecs.jml4.boogie;
 
 import org.eclipse.jdt.internal.compiler.Compiler;
 import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
 import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
 import org.jmlspecs.jml4.compiler.DefaultCompilerExtension;
 import org.jmlspecs.jml4.util.Logger;
 
 /**
  * A compiler extension that enables Java code to be passed through
  * Microsoft Boogie in order to formally verify JML DbC annotations.
  */
 public class Boogie extends DefaultCompilerExtension {
 	private static final boolean DEBUG = true;
 
 	/**
 	 * The name of the compiler extension
 	 * 
 	 * @return the compiler extension's name
 	 */
 	public String name() { return "JML4BOOGIE"; } //$NON-NLS-1$
 
 	/**
 	 * Called by Eclipse's compilation mechanism before Java bytecode is generated for output.
 	 * 
 	 * @param compiler the compiler object 
 	 * @param unit the root AST node of a Java source file to parse
 	 */
 	public void preCodeGeneration(Compiler compiler, CompilationUnitDeclaration unit) {
 		if (DEBUG) {
 			Logger.println(this + " - compiler.options.jmlEnabled:     "+compiler.options.jmlEnabled); //$NON-NLS-1$
 			Logger.println(this + " - compiler.options.jmlBoogieEnabled:  "+compiler.options.jmlBoogieEnabled); //$NON-NLS-1$
 			Logger.println(this + " - compiler.options.jmlBoogieOutputOnly:  "+compiler.options.jmlBoogieOutputOnly); //$NON-NLS-1$
 		}
 		if (compiler.options.jmlEnabled && compiler.options.jmlDbcEnabled && compiler.options.jmlBoogieEnabled) {
 			process(compiler, unit);
 		}
 	}
 
 	/**
 	 * Processes the AST, converts it to Boogie code and passes it to a Boogie runtime.
 	 * 
 	 * @param compiler the compiler object passed by Eclipse
 	 * @param unit the root ASTNode object
 	 */
 	private void process(Compiler compiler, CompilationUnitDeclaration unit) {
 		if (compiler.options.jmlBoogieOutputOnly) {
 			// debugging / testing
 			BoogieSource source = BoogieVisitor.visit(unit);
 			String results = source.getResults();
 			String[] resultsArray = results.split("/\\*!BOOGIESTART!\\*/"); //$NON-NLS-1$
			compiler.problemReporter.jmlEsc2Error(resultsArray[1], 0, 0);
 		}
 		else {
 			BoogieAdapter adapter = new BoogieAdapter(compiler);
 			adapter.prove(unit);
 		}
 	}
 
 	/**
 	 * Debugging method to print any compiler options relevant to this extension.
 	 * 
 	 * @param options the compiler options to check
 	 * @param buf the output buffer to add debugging info to
 	 */
 	public void optionsToBuffer(CompilerOptions options, StringBuffer buf) {
 		buf.append("\n\t\t- BOOGIE: ").append(options.jmlBoogieEnabled ?  //$NON-NLS-1$
 				CompilerOptions.ENABLED : CompilerOptions.DISABLED); 
 	}
 
 }
