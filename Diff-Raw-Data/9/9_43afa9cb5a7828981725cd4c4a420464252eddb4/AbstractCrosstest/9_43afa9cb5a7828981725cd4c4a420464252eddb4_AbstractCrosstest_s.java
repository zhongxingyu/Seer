 package swp_compiler_ss13.crosstest;
 
 import junit.extensions.PA;
 import org.apache.commons.io.IOUtils;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.junit.*;
 import org.junit.Test;
 import swp_compiler_ss13.common.backend.BackendException;
 import swp_compiler_ss13.common.ir.IntermediateCodeGeneratorException;
 import swp_compiler_ss13.common.report.ReportType;
 import swp_compiler_ss13.common.test.ExampleProgs;
 import swp_compiler_ss13.fuc.backend.LLVMBackend;
 import swp_compiler_ss13.fuc.ir.IntermediateCodeGeneratorImpl;
 import swp_compiler_ss13.fuc.lexer.LexerImpl;
 import swp_compiler_ss13.fuc.parser.ParserImpl;
 import swp_compiler_ss13.fuc.semantic_analyser.SemanticAnalyser;
 import swp_compiler_ss13.javabite.backend.BackendJb;
 import swp_compiler_ss13.javabite.codegen.IntermediateCodeGeneratorJb;
 import swp_compiler_ss13.javabite.lexer.LexerJb;
 import swp_compiler_ss13.javabite.parser.ParserJb;
 import swp_compiler_ss13.javabite.semantic.SemanticAnalyserJb;
 
 import java.io.*;
 import java.util.*;
 
 import static org.junit.Assert.*;
 
 /**
  * <p>
  * The cross tests test for the interchangeability of the fuc and the javabite
  * modules. All 32 possible combinations of the lexer, parser, semantic analyser
  * and backend are tests.
  * </p>
  * <p>
  * For the tests to work, the javabite moduls have to be placed into the
  * fuc/code/dist <code>fuc/code/dist/</code> directory as jar-files. The
  * javabite jars can be obtained be running <code>ant buildCompiler</code> in
  * the javabite repository.
  * </p>
  * <p>
  * The cross test are compilation tests, i.e. the test if the example program
  * compiles through all stages of the compiler, producing some kind of target
  * language code in the end. These tests only test if the compiler runs through
  * without producing errors, not for the correctness of the resulting target
  * language code.
  * </p>
  * <p>
  * All example progs can be found in {@link ExampleProgs}.
  * </p>
  * 
  * @author Jens V. Fischer
  */
 public abstract class AbstractCrosstest {
 
 	private static Logger logger = Logger.getLogger(AbstractCrosstest.class);
 	protected Compiler compiler;
 	private String progName;
 
 	private final static String BUILD_DIR = "build";
 
 
 
 	@Test
 	public void test() throws InterruptedException, IOException, IntermediateCodeGeneratorException, BackendException, CloneNotSupportedException {
 
 		Logger.getRootLogger().setLevel(Level.ERROR);
 
 		this.progName = getProgName();
 
 		Map<String, InputStream> compilationResult = compiler.compile(getProg());
 		ReportLogImpl log = compiler.getErrlog();
 
 
 		/* test for expected report log errors from parser if program does not compile */
 		if (compiler.errlogAfterParser.hasErrors()){
 			String msg = "Error in Parser: Expected ReportLog entries: " + Arrays.deepToString(getExpectedReportTypes())
 					+ ". Actual: " + log.getErrors().toString();
 			assertArrayEquals(msg, getExpectedReportTypes(), compiler.getErrlogAfterParser().getEntries().toArray());
 			return;
 		}
 
 		/* test for expected report log errors from analyzer if program does not compile */
 		if (compiler.errlogAfterAnalyzer.hasErrors()){
 			String msg = "Error in Analyzer: Expected ReportLog entries: " + Arrays.deepToString(getExpectedReportTypes())
 					+ ". Actual: " + log.getErrors().toString();
 			assertArrayEquals(msg, getExpectedReportTypes(), compiler.getErrlogAfterAnalyzer().getEntries().toArray());
 			return;
 		}
 
 		/* test for expected report log errors if program compiles */
 		String msg = "Unexpected errors after successfull compilation.\n Expected ReportLog entries: " + Arrays.deepToString(getExpectedReportTypes())
 				+ ". Actual: " + log.getErrors().toString();
 		assertArrayEquals(msg, getExpectedReportTypes(), log.getErrors().toArray());
 
 		LLVMIRExecutor.ExecutionResult executionResult = executeTargetCode(compilationResult);
 		assertEquals("Exit code doesn't match: ", getExpectedExitCode(), executionResult.exitCode);
 		assertEquals("Output doesn't macht: ", getExpectedOutput(), executionResult.output);
 	}
 
 
 	protected abstract String getProgName();
 
 
 	private String getProg(){
 		return (String) ( (Object[]) PA.invokeMethod(ExampleProgs.class, this.progName + "()"))[0];
 	}
 
 
 	private Integer getExpectedExitCode(){
 		return (Integer) ( (Object[]) PA.invokeMethod(ExampleProgs.class, this.progName + "()"))[1];
 	}
 
 
 	private String getExpectedOutput(){
 		return (String) ( (Object[]) PA.invokeMethod(ExampleProgs.class, this.progName + "()"))[2];
 	}
 
 
 	private ReportType[] getExpectedReportTypes(){
 		return (ReportType[]) ( (Object[]) PA.invokeMethod(ExampleProgs.class, this.progName + "()"))[3];
 	}
 
 
 	private LLVMIRExecutor.ExecutionResult executeTargetCode(Map<String, InputStream> compilationResult) throws InterruptedException, BackendException, IOException {
 
 		if (isLLVMBackend())
 			return executeLLVMIR(compilationResult);
 		else if (isJbBackend())
 			return executeJavabytecode(compilationResult);
 		else
 			return null;
 	}
 
 
 	private boolean isLLVMBackend() {
 		return compiler.backend.getClass() == LLVMBackend.class;
 	}
 
 
 	private boolean isJbBackend() {
 		return compiler.backend.getClass() == BackendJb.class;
 	}
 
 
 	private LLVMIRExecutor.ExecutionResult executeLLVMIR(Map<String, InputStream> compilationResult) throws InterruptedException, BackendException, IOException {
 		try {
 			LLVMIRExecutor.tryToStartLLI();
 		}
 		catch (IOException e) {
 			Assume.assumeNoException("No LLVM Installation found", e);
 		}
 		return LLVMIRExecutor.runIR(compilationResult.get(compilationResult.keySet().iterator().next()));
 	}
 
 
 	private LLVMIRExecutor.ExecutionResult executeJavabytecode(Map<String, InputStream> compilationResult) throws IOException {
 		File mainClassFile = null;
 		for (Map.Entry<String, InputStream> e : compilationResult.entrySet()) {
 			File outFile = new File(BUILD_DIR, e.getKey());
 			if (!outFile.getParentFile().exists() && !outFile.getParentFile().mkdirs()) {
 				logger.error("Could not generate output directories: " + outFile.getAbsolutePath());
 				return null;
 			}
 			if (mainClassFile == null)
 				mainClassFile = outFile;
 
 			FileOutputStream fos = new FileOutputStream(outFile);
 			IOUtils.copy(e.getValue(), fos);
 			fos.close();
 		}
 
 		JavabyteExecutor jbExecutor = new JavabyteExecutor(mainClassFile);
 
 		String output;
		if (getExpectedOutput().equals(""))
 			output = "";
 		else
			output = jbExecutor.getProcessOutput() + "\n";
 
 		return new LLVMIRExecutor.ExecutionResult(output, jbExecutor.getReturnValue(), null);
 	}
 
 
 	protected static Collection<Object[]> moduleCombinations() {
 		Class[] lexerClasses = new Class[]{LexerJb.class, LexerImpl.class};
 		Class[] parserClasses = new Class[]{ParserJb.class, ParserImpl.class};
 		Class[] analyserClasses = new Class[]{SemanticAnalyserJb.class, SemanticAnalyser.class};
 		Class[] irgenClasses = new Class[]{IntermediateCodeGeneratorJb.class, IntermediateCodeGeneratorImpl.class};
 		Class[] backendClasses = new Class[]{BackendJb.class, LLVMBackend.class};
 		ArrayList classes = new ArrayList();
 		for (Class lexer : lexerClasses) {
 			for (Class parser : parserClasses) {
 				for (Class analyzer : analyserClasses) {
 					for (Class irgen : irgenClasses) {
 						for (Class backend : backendClasses) {
 							String testname = lexer.getSimpleName() + "->" + parser.getSimpleName() + "->" + analyzer.getSimpleName() + "->" + irgen.getSimpleName() + "->" + backend.getSimpleName();
 							classes.add(new Object[]{testname, lexer, parser, analyzer, irgen, backend});
 						}
 
 					}
 				}
 			}
 		}
 
 		return classes;
 	}
 
 	protected void assumeAllModulesPreset() {
 		Assume.assumeTrue("no lexer found, aborting", compiler.lexer != null);
 		Assume.assumeTrue("no parser found, aborting", compiler.parser != null);
 		Assume.assumeTrue("no semantic analyser found, aborting", compiler.analyser != null);
 		Assume.assumeTrue("no irgen found, aborting", compiler.irgen != null);
 		Assume.assumeTrue("no lexer found, aborting", compiler.backend != null);
 	}
 
 }
