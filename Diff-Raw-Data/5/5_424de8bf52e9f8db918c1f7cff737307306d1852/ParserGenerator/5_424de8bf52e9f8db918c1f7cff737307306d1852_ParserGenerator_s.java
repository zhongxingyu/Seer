 package org.rascalmpl.parser;
 
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.List;
 
 import org.eclipse.imp.pdb.facts.IConstructor;
 import org.eclipse.imp.pdb.facts.ISet;
 import org.eclipse.imp.pdb.facts.ISourceLocation;
 import org.eclipse.imp.pdb.facts.IString;
 import org.eclipse.imp.pdb.facts.IValueFactory;
 import org.rascalmpl.interpreter.Evaluator;
 import org.rascalmpl.interpreter.IRascalMonitor;
 import org.rascalmpl.interpreter.asserts.ImplementationError;
 import org.rascalmpl.interpreter.control_exceptions.Throw;
 import org.rascalmpl.interpreter.env.GlobalEnvironment;
 import org.rascalmpl.interpreter.env.ModuleEnvironment;
 import org.rascalmpl.interpreter.utils.JavaBridge;
 import org.rascalmpl.parser.gtd.IGTD;
 import org.rascalmpl.values.ValueFactoryFactory;
 
 public class ParserGenerator {
 	private final Evaluator evaluator;
 	private final JavaBridge bridge;
 	private final IValueFactory vf;
 	private static final String packageName = "org.rascalmpl.java.parser.object";
 
 	public ParserGenerator(IRascalMonitor monitor, PrintWriter out, List<ClassLoader> loaders, IValueFactory factory) {
 		this.bridge = new JavaBridge(loaders, factory);
 		GlobalEnvironment heap = new GlobalEnvironment();
 		ModuleEnvironment scope = new ModuleEnvironment("***parsergenerator***", heap);
 		this.evaluator = new Evaluator(ValueFactoryFactory.getValueFactory(), out, out, scope,heap);
 		this.vf = factory;
 		
		monitor.startJob("Loading parser generator", 100, 139);
 		evaluator.doImport(monitor, "lang::rascal::syntax::Generator");
 		evaluator.doImport(monitor, "lang::rascal::syntax::Normalization");
 		evaluator.doImport(monitor, "lang::rascal::syntax::Definition");
 		evaluator.doImport(monitor, "lang::rascal::syntax::Assimilator");
		monitor.endJob(true);
 	}
 	
 	/**
 	 * Generate a parser from a Rascal syntax definition (a set of production rules).
 	 * 
 	 * @param monitor a progress monitor; this method will contribute 100 work units
 	 * @param loc     a location for error reporting
 	 * @param name    the name of the parser for use in code generation and for later reference
 	 * @param imports a set of syntax definitions (which are imports in the Rascal grammar)
 	 * @return
 	 */
 	public Class<IGTD> getParser(IRascalMonitor monitor, ISourceLocation loc, String name, ISet imports) {
 		monitor.startJob("Generating parser", 100, 90);
 		
 		try {
 			monitor.event("Importing and normalizing grammar:" + name, 30);
 			IConstructor grammar = getGrammar(monitor, imports);
 			String normName = name.replaceAll("\\.", "_");
 			monitor.event("Generating java source code for parser: " + name,30);
 			IString classString = (IString) evaluator.call(monitor, "generateObjectParser", vf.string(packageName), vf.string(normName), grammar);
 			debugOutput(classString, "/tmp/parser.java");
 			monitor.event("Compiling generated java code: " + name, 30);
 			return bridge.compileJava(loc, packageName + "." + normName, classString.getValue());
 		}  catch (ClassCastException e) {
 			throw new ImplementationError("parser generator:" + e.getMessage(), e);
 		} catch (Throw e) {
 			throw new ImplementationError("parser generator: " + e.getMessage() + e.getTrace());
 		} finally {
 			monitor.endJob(true);
 		}
 	}
 
 	/**
 	 * Uses the user defined syntax definitions to generate a parser for Rascal that can deal
 	 * with embedded concrete syntax fragments
 	 * 
 	 * Note that this method works under the assumption that a normal parser was generated before!
 	 * The class that this parser generates will inherit from that previously generated parser.
 	 */
 	public Class<IGTD> getRascalParser(IRascalMonitor monitor, ISourceLocation loc, String name, ISet imports, IGTD objectParser) {
 		try {
 			monitor.event("Importing and normalizing grammar: " + name, 10);
 			IConstructor grammar = getGrammar(monitor, imports);
 			String normName = name.replaceAll("\\.", "_");
 			monitor.event("Generating java source code for Rascal parser:" + name, 10);
 			IString classString = (IString) evaluator.call(monitor, "generateMetaParser", vf.string(packageName), vf.string("$Rascal_" + normName), vf.string(packageName + "." + normName), grammar);
 			debugOutput(classString, "/tmp/metaParser.java");
 			monitor.event("compiling generated java code: " + name, 10);
 			return bridge.compileJava(loc, packageName + ".$Rascal_" + normName, objectParser.getClass(), classString.getValue());
 		}  catch (ClassCastException e) {
 			throw new ImplementationError("meta parser generator:" + e.getMessage(), e);
 		} catch (Throw e) {
 			throw new ImplementationError("meta parser generator: " + e.getMessage() + e.getTrace());
 		}
 	}
 
 	private void debugOutput(IString classString, String file) {
 		FileOutputStream s = null;
 		try {
 			s = new FileOutputStream(file);
 			s.write(classString.getValue().getBytes());
 			s.flush();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			if (s != null) {
 				try {
 					s.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 	
 	public IConstructor getGrammar(IRascalMonitor monitor, ISet imports) {
 		return (IConstructor) evaluator.call(monitor, "imports2grammar", imports);
 	}
 }
