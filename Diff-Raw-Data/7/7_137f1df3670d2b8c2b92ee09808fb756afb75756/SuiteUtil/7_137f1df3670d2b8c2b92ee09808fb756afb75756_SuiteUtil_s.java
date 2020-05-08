 package org.suite;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.instructionexecutor.FunctionInstructionExecutor;
 import org.instructionexecutor.LogicInstructionExecutor;
 import org.suite.doer.Generalizer;
 import org.suite.doer.Prover;
 import org.suite.doer.TermParser;
 import org.suite.kb.RuleSet;
 import org.suite.node.Atom;
 import org.suite.node.Node;
 import org.suite.node.Reference;
 import org.util.Util;
 
 public class SuiteUtil {
 
 	private static TermParser parser = new TermParser();
 	private static Prover logicalCompiler;
 	private static Prover eagerFunctionalCompiler;
 	private static Prover lazyFunctionalCompiler;
 
 	// The directory of the file we are now importing
 	private static boolean isImportFromClasspath = false;
 	private static String importerRoot = "";
 
 	public static void addRule(RuleSet rs, String rule) {
 		rs.addRule(parser.parse(rule));
 	}
 
 	public static synchronized void importFrom(RuleSet rs, String name)
 			throws IOException {
 		if (isImportFromClasspath)
 			SuiteUtil.importResource(rs, name);
 		else
 			SuiteUtil.importFile(rs, name);
 	}
 
 	public static synchronized void importFile(RuleSet rs, String filename)
 			throws IOException {
 		FileInputStream is = null;
 
 		boolean wasFromClasspath = isImportFromClasspath;
 		String oldRoot = importerRoot;
 		filename = setImporterRoot(false, filename, oldRoot);
 
 		try {
 			is = new FileInputStream(filename);
 			rs.importFrom(SuiteUtil.parse(is));
 		} finally {
 			Util.closeQuietly(is);
 			isImportFromClasspath = wasFromClasspath;
 			importerRoot = oldRoot;
 		}
 	}
 
 	public static synchronized void importResource(RuleSet rs, String classpath)
 			throws IOException {
 		ClassLoader cl = SuiteUtil.class.getClassLoader();
 		InputStream is = null;
 
 		boolean wasFromClasspath = isImportFromClasspath;
 		String oldRoot = importerRoot;
 		classpath = setImporterRoot(true, classpath, oldRoot);
 
 		try {
 			is = cl.getResourceAsStream(classpath);
 			rs.importFrom(SuiteUtil.parse(is));
 		} finally {
 			Util.closeQuietly(is);
 			isImportFromClasspath = wasFromClasspath;
 			importerRoot = oldRoot;
 		}
 	}
 
 	private static String setImporterRoot(boolean isFromClasspath, String name,
 			String oldRoot) {
 		isImportFromClasspath = isFromClasspath;
 
		int pos = name.lastIndexOf(File.separator);
		importerRoot = pos >= 0 ? name.substring(0, pos + 1) : "";

 		if (!name.startsWith(File.separator))
 			name = oldRoot + name;
 		return name;
 	}
 
 	public static boolean proveThis(RuleSet rs, String s) {
 		Node node = parse(s);
 		node = new Generalizer().generalize(node);
 		Prover prover = new Prover(rs);
 		return prover.prove(node);
 	}
 
 	public static boolean evaluateLogical(String program) {
 		return evaluateLogical(parse(program));
 	}
 
 	public static Node evaluateFunctional(String program, boolean lazyEvaluation) {
 		return evaluateFunctional(parse(program), lazyEvaluation);
 	}
 
 	public static boolean evaluateLogical(Node program) {
 		Prover lc = getLogicalCompiler();
 		Node node = SuiteUtil.parse("compile-logic .program .code");
 		// + ", pp-list .code"
 
 		Generalizer generalizer = new Generalizer();
 		node = generalizer.generalize(node);
 		Node variable = generalizer.getVariable(Atom.create(".program"));
 		Node ics = generalizer.getVariable(Atom.create(".code"));
 
 		((Reference) variable).bound(program);
 		if (lc.prove(node)) {
 			Node result = new LogicInstructionExecutor(lc, ics).execute();
 			return result == Atom.create("true");
 		} else
 			throw new RuntimeException("Logic compilation error");
 	}
 
 	public static Node evaluateFunctional(Node program, boolean lazyEvaluation) {
 		Prover compiler = lazyEvaluation ? getLazyFunCompiler()
 				: getEagerFunCompiler();
 
 		return evaluateFunctional(program, compiler);
 	}
 
 	public static Node evaluateFunctional(Node program, Prover compiler) {
 		Node node = SuiteUtil.parse("compile-function .program .code");
 		// + ", pp-list .code"
 
 		Generalizer generalizer = new Generalizer();
 		node = generalizer.generalize(node);
 		Node variable = generalizer.getVariable(Atom.create(".program"));
 		Node ics = generalizer.getVariable(Atom.create(".code"));
 
 		((Reference) variable).bound(program);
 		if (compiler.prove(node))
 			return new FunctionInstructionExecutor(ics).execute();
 		else
 			throw new RuntimeException("Function compilation error");
 	}
 
 	private static synchronized Prover getLogicalCompiler() {
 		if (logicalCompiler == null)
 			logicalCompiler = getProver(new String[] { "auto.sl", "lc.sl" });
 		return logicalCompiler;
 	}
 
 	private static synchronized Prover getEagerFunCompiler() {
 		if (eagerFunctionalCompiler == null) {
 			String imports[] = { "auto.sl", "fc.sl", "fc-eager-evaluation.sl" };
 			eagerFunctionalCompiler = getProver(imports);
 		}
 		return eagerFunctionalCompiler;
 	}
 
 	private static synchronized Prover getLazyFunCompiler() {
 		if (lazyFunctionalCompiler == null) {
 			String imports[] = { "auto.sl", "fc.sl", "fc-lazy-evaluation.sl" };
 			lazyFunctionalCompiler = getProver(imports);
 		}
 		return lazyFunctionalCompiler;
 	}
 
 	public static Prover getProver(String toImports[]) {
 		RuleSet rs = new RuleSet();
 
 		try {
 			for (String toImport : toImports)
 				SuiteUtil.importResource(rs, toImport);
 		} catch (IOException ex) {
 			throw new RuntimeException(ex);
 		}
 
 		return new Prover(rs);
 	}
 
 	public static Node parse(String s) {
 		return parser.parse(s);
 	}
 
 	public static Node parse(InputStream s) throws IOException {
 		return parser.parse(s);
 	}
 
 }
