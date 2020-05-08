 package org.unbunt.ella;
 
 import org.antlr.runtime.RecognitionException;
 import org.antlr.runtime.tree.CommonTree;
 import org.antlr.runtime.tree.CommonTreeNodeStream;
 import org.antlr.runtime.tree.DOTTreeGenerator;
 import org.antlr.stringtemplate.StringTemplate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
 import org.gnu.readline.Readline;
 import org.gnu.readline.ReadlineLibrary;
 import org.kohsuke.args4j.Argument;
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.CmdLineParser;
 import org.kohsuke.args4j.Option;
 import org.springframework.jdbc.datasource.DriverManagerDataSource;
 import org.unbunt.ella.compiler.*;
 import org.unbunt.ella.compiler.antlr.LazyInputStream;
 import org.unbunt.ella.compiler.antlr.LazyTokenStream;
 import org.unbunt.ella.compiler.statement.Block;
 import org.unbunt.ella.compiler.support.Scope;
 import org.unbunt.ella.engine.EllaCPSEngine;
 import org.unbunt.ella.engine.EllaEngine;
 import org.unbunt.ella.engine.context.Context;
 import org.unbunt.ella.exception.*;
 import org.unbunt.ella.lang.sql.DBUtils;
 import org.unbunt.ella.lang.sql.Drivers;
 import org.unbunt.ella.resource.FilesystemResource;
 import org.unbunt.ella.resource.FilesystemResourceLoader;
 import org.unbunt.ella.resource.SimpleResource;
 import org.unbunt.ella.resource.StringResource;
 import static org.unbunt.ella.utils.StringUtils.join;
 import sun.misc.Signal;
 import sun.misc.SignalHandler;
 
 import java.io.*;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Main interface for executing programs written in the EllaScript programming language. Also implements a
  * <code>main</code> method providing a command line interface which allows for the compilation, tree generation
  * and execution of EllaScript programs.
  */
 public class Ella {
    protected static Log logger = LogFactory.getLog(Ella.class);

     protected Context context;
     protected SimpleResource script;
     protected String scriptName;
 
     protected EllaLexer lexer;
     protected LazyTokenStream tokens;
 
     protected EllaParser parser;
     protected CommonTree tree;
 
     protected EllaWalker walker;
 
     protected EllaEngine engine = null;
     protected Block block; // the parsed script to be run by the engine
 
     /**
      * Creates a new Ella instance for compilation or execution of the EllaScript program
      * specified by the given resource.
      * <p>
      * The program is to be executed in an context made up by a fresh instance of class
      * <code>DefaultContext</code>.
      *
      * @param script the resource representing the EllaScript program to compile or execute.
      * @see org.unbunt.ella.DefaultContext
      */
     public Ella(SimpleResource script) {
         this(new DefaultContext(), script);
     }
 
     /**
      * Creates a new Ella instance for compilation or execution of the EllaScript program
      * specified by the given resource within the given context.
      *
      * @param context the context to execute the EllaScript program in.
      * @param script the resource representing the EllaScript program to compile or execute.
      */
     public Ella(Context context, SimpleResource script) {
         this.context = context;
         this.script = script;
         this.scriptName = script.getFilename();
 
         context.setScriptFilename(script.getFilename());
         context.setScriptResource(script);
     }
 
     protected boolean interactiveCancel = false;
 
     protected void initInteractive() {
         // init readline library
         ReadlineLibrary[] libs = new ReadlineLibrary[] {
             ReadlineLibrary.GnuReadline,
             ReadlineLibrary.Editline,
             ReadlineLibrary.Getline,
             ReadlineLibrary.PureJava
         };
         Readline.setVar(Readline.RL_CATCH_SIGNALS, 1);
         Readline.setVar(Readline.RL_CATCH_SIGWINCH, 1);
         for (ReadlineLibrary lib : libs) {
             try {
                 Readline.load(lib);
                 break;
             } catch (UnsatisfiedLinkError ignored) {
                logger.trace("Readline lib " + lib.getName() + " not found...");
             } catch (Exception ignored) {
             }
         }
         Readline.initReadline("Ella");
         Runtime.getRuntime().addShutdownHook(new Thread() {
             public void run() {
                 Readline.cleanup();
             }
         });
 
         // install signal handlers (naughty - using sun.misc.*)
         SignalHandler signalHandler = new SignalHandler() { // TODO: ensure thread-safety
             public void handle(Signal signal) {
                 if (!interactiveCancel) {
                     System.out.println();
                 }
                 interactiveCancel = true;
 //                Readline.setVar(Readline.RL_PENDING_INPUT, (int) '\n');
                 Readline.setVar(Readline.RL_DONE, 1);
 //                System.out.println("Caught sigTerm");
             }
         };
         Signal.handle(new Signal("TERM"), signalHandler);
         Signal.handle(new Signal("INT"), signalHandler);
     }
 
     /**
      * Executes an EllaScript program in interactive mode. In this mode the user is presented a shell-like
      * console interface where arbitrary EllaScript statements can be entered. As soon as a statement has been entered
      * completely and submitted by pressing <code>RETURN</code>, the statement will be executed.
      * <p>
      * Execution finishes when <code>EOF</code> if read or execution is terminated explicitly via the respective
      * function of the EllaScript language.
      * <p>
      * In this mode input is always read from the standard input stream. The script resource given to this Ella object
      * via one of it's constructors is not used.
      *
      * @throws EllaIOException if reading from standard input fails.
      */
     public void executeInteractive() throws EllaIOException {
         initInteractive();
         initEngine();
 
         try {
             String line;
             StringBuilder buf = new StringBuilder();
             boolean incompleteString = false;
             int stringType = -1;
             while (!engine.isExited()) {
                 boolean continued = buf.length() != 0;
 
                 String prompt = "=>";
                 if (continued) {
                     if (incompleteString) {
                         switch (stringType) {
                             case EllaParser.SQUOT:   prompt = "'>";  break;
                             case EllaParser.DQUOT:   prompt = "\">"; break;
                             case EllaParser.BTICK:   prompt = "`>";  break;
                             case EllaParser.QQUOT:   prompt = "Q>";  break;
                             case EllaParser.DOLQUOT: prompt = "$>";  break;
                         }
                     }
                     else {
                         prompt = "->";
                     }
                 }
 
                 try {
                     line = Readline.readline(prompt + " ", false);
                     if (interactiveCancel) {
                         interactiveCancel = false;
                         buf.setLength(0);
                         continue;
                     }
                     if (line == null) { // just pressed enter
                         continue;
                     }
                 } catch (EOFException e) { // input closed
                     break;
                 } catch (IOException e) {
                     throw new EllaIOException(e);
                 }
 
                 if (continued) {
                     buf.append('\n');
                 }
                 buf.append(line);
 
                 String unit = buf.toString();
                 InputStream input = new ByteArrayInputStream(unit.getBytes());
 
                 boolean incompleteInput = false;
                 try {
                     try {
                         tokenize(input);
                         parseTokens();
                         parseTreeIncremental();
                         runBlock();
                     } catch (EllaRecognitionException e) {
                         if (e.isCausedBy(UnterminatedStringException.class)) {
                             UnterminatedStringException ex = e.getCause(UnterminatedStringException.class);
                             stringType = ex.getStringType();
                             incompleteInput = true;
                             incompleteString = true;
                         }
                         else if (e.isCausedBy(UnexpectedEOFException.class)
                                 || (e.isCausedBy(RecognitionException.class)
                                     && (e.getCause(RecognitionException.class)).getUnexpectedType() == EllaParser.EOF)) {
                             incompleteInput = true;
                             incompleteString = false;
                         }
                         else {
                             throw e;
                         }
                     }
                 } catch (Exception e) {
                     System.err.println("ERROR: " + e.getMessage());
                     e.printStackTrace();
                 }
 
                 // keep current statement buffer in case it was an incomplete statement
                 if (!incompleteInput) {
                     Readline.addToHistory(unit);
                     buf.setLength(0);
                 }
             }
         }
         finally {
             finish();
             Readline.cleanup();
             System.out.println();
         }
     }
 
     /**
      * Executes the EllaScript program incrementally. This mode is intended to be used for large programs as it
      * requires only a limited amount of memory since only one statement from the program is compiled and executed
      * at a time.
      *
      * @return the result of the evaluation of the program.
      * @throws EllaIOException if reading the program fails.
      * @throws EllaParseException if compilation of the program fails.
      * @throws EllaException if an exception is thrown from the program.
      */
     public Object executeIncremental() throws EllaIOException, EllaParseException, EllaException {
         initParserIncremental();
         initEngine();
         try {
             Object result = null;
             while (parseTokensIncremental() && !engine.isExited()) {
                 if (tree == null) {
                     continue;
                 }
                 parseTree();
                 result = runBlock();
             }
             return result;
         }
         finally {
             finish();
         }
     }
 
     /**
      * Executes the EllaScript program.
      *
      * @return the result of the evaluation of the program.
      * @throws EllaIOException if reading the program fails.
      * @throws EllaParseException if compilation of the program fails.
      * @throws EllaException if an exception is thrown from the program.
      */
     public Object execute()
             throws EllaIOException, EllaParseException, EllaException {
         tokenize();
         parseTokens();
         parseTree();
         return run();
     }
 
     /**
      * Compiles the EllaScript program.
      *
      * @return the executable form of the program.
      * @throws EllaIOException if reading the program fails.
      * @throws EllaParseException if compilation of the program fails.
      */
     public Block compile() throws EllaIOException, EllaParseException {
         tokenize();
         parseTokens();
         parseTree();
         return block;
     }
 
     /**
      * Compiles the EllaScript program into an abstract syntax tree and generates a visual representation of it.
      * <p>
      * FIXME: Currently this is done in a platform specific way since it relies on the <code>dot</code> and
      *        <code>xdg-open</code> programs, which are not available on all platforms supporting Java.
      *
      * @throws EllaIOException if reading the program fails.
      * @throws EllaParseException if compilation of the program fails.
      */
     public void showAST() throws EllaIOException, EllaParseException {
         tokenize();
         parseTokens();
 
         try {
             DOTTreeGenerator dotGen = new DOTTreeGenerator();
             StringTemplate dotGraph = dotGen.toDOT(tree);
 
             File dotFile = File.createTempFile("tree", "dot");
             dotFile.deleteOnExit();
 
             Writer w = new FileWriter(dotFile);
             w.write(dotGraph.toString()
                     .replace("digraph {", "digraph {\n  rankdir=LR;")
                     .replace("fixedsize=true", "fixedsize=false")
             );
             w.close();
 
             File pngFile = File.createTempFile("tree", "png");
 
             Process dot = Runtime.getRuntime().exec(new String[] {
                     "dot", "-Tsvg", "-o", pngFile.getPath(), dotFile.getPath()
             });
 
             int result;
             while (true) {
                 try {
                     result = dot.waitFor();
                     break;
                 } catch (InterruptedException e) {
                     continue;
                 }
             }
 
             if (result != 0) {
                 throw new EllaIOException("dot command failed");
             }
 
             Runtime.getRuntime().exec(new String[] { "xdg-open", pngFile.getPath() });
         } catch (IOException e) {
             throw new EllaIOException(e);
         }
     }
 
     protected void tokenize() throws EllaIOException {
         try {
             tokenize(script.getInputStream());
         } catch (Exception e) {
             throw new EllaIOException("Failed to read sql script: " + scriptName + ": " + e.getMessage(), e);
         }
     }
 
     protected void tokenize(InputStream stream) throws EllaIOException {
         LazyInputStream input;
         try {
             input = new LazyInputStream(stream);
         } catch (Exception e) {
             throw new EllaIOException("Failed to read sql script: " + scriptName + ": " + e.getMessage(), e);
         }
 
         lexer = new EllaLexer(input);
         tokens = new LazyTokenStream(lexer);
     }
 
     protected void parseTokens() throws EllaParseException {
         if (parser == null) {
             parser = new EllaParser(tokens);
         }
         else {
             parser.setTokenStream(tokens);
         }
 
         EllaParser.script_return r;
         try {
             r = parser.parseScript();
         } catch (EllaRecognitionException e) {
             RecognitionException re = e.getCause();
             throw new EllaRecognitionException("Failed to parse sql script: " + scriptName + ": " +
                                               parser.getErrorHeader(re) + " " +
                                               parser.getErrorMessage(re, parser.getTokenNames()),
                                               re);
         } catch (EllaParseException e) {
             throw new EllaParseException("Failed to parse sql script: " + scriptName + ": " + e.getMessage(), e);
         }
 
         tree = (CommonTree) r.getTree();
     }
 
     protected void initParserIncremental() throws EllaIOException {
         InputStream stream;
         try {
             stream = script.getInputStream();
         } catch (IOException e) {
             throw new EllaIOException("Failed to read sql script: " + scriptName + ": " + e.getMessage(), e);
         }
 
         initParserIncremental(stream);
     }
 
     protected void initParserIncremental(InputStream stream) {
         LazyInputStream input = new LazyInputStream(stream);
 
         lexer = new EllaLexer(input);
         tokens = new LazyTokenStream(lexer);
         parser = new EllaParser(tokens);
     }
 
     protected boolean parseTokensIncremental() throws EllaParseException {
         EllaParser.scriptIncremental_return r;
         try {
             r = parser.parseScriptIncremental();
         } catch (EllaRecognitionException e) {
             RecognitionException re = e.getCause();
             throw new EllaRecognitionException("Failed to parse sql script: " + scriptName + ": " +
                                               parser.getErrorHeader(re) + " " +
                                               parser.getErrorMessage(re, parser.getTokenNames()),
                                               re);
         } catch (EllaParseException e) {
             throw new EllaParseException("Failed to parse sql script: " + scriptName + ": " + e.getMessage(), e);
         }
 
         tree = (CommonTree) r.getTree();
 
         return !parser.isEOF();
     }
 
     protected void parseTree() throws EllaRecognitionException {
         CommonTreeNodeStream nodes = new CommonTreeNodeStream(tree);
         nodes.setTokenStream(tokens);
 
         EllaWalker walker = new EllaWalker(nodes);
         try {
             block = walker.walkScript(context.getEnv().toScope());
             TailCallOptimizer.process(block);
         } catch (EllaRecognitionException e) {
             RecognitionException re = e.getCause();
             throw new EllaRecognitionException("Failed to parse sql script: " + scriptName + ": " +
                                               walker.getErrorHeader(re) + " " +
                                               walker.getErrorMessage(re, walker.getTokenNames()),
                                               re);
         }
     }
 
     protected Scope savedScope = null;
 
     protected void parseTreeIncremental() throws EllaRecognitionException {
         CommonTreeNodeStream nodes = new CommonTreeNodeStream(tree);
         nodes.setTokenStream(tokens);
 
         EllaWalker walker = new EllaWalker(nodes);
         try {
             if (savedScope == null) {
                 savedScope = context.getEnv().toScope();
             }
             block = walker.walkScript(savedScope);
             savedScope = block.getScope();
 
             TailCallOptimizer.process(block);
         } catch (EllaRecognitionException e) {
             RecognitionException re = e.getCause();
             throw new EllaRecognitionException("Failed to parse sql script: " + scriptName + ": " +
                                               walker.getErrorHeader(re) + " " +
                                               walker.getErrorMessage(re, walker.getTokenNames()),
                                               re);
         }
     }
 
     protected Object run() throws EllaException {
         initEngine();
         try {
             return runBlock();
         } finally {
             finish();
         }
     }
 
     protected Object runBlock() throws EllaException {
         return engine.eval(block);
     }
 
     protected void initEngine() {
         engine = EllaCPSEngine.create(context);
     }
 
     protected void finish() {
         if (engine == null) {
             return;
         }
         engine.finish();
     }
 
     /*
      * Static methods / Main program
      */
 
     /**
      * Evaluates the given EllaScript program in incremental mode.
      *
      * @param script the file containing the program to execute.
      * @param args arguments to pass to the program.
      * @return the result of the evaluation of the program.
      * @throws EllaIOException if reading the program file fails.
      * @throws EllaParseException if compilation of the program fails.
      * @throws EllaException if the program throws an exception.
      * @see #executeIncremental()
      */
     public static Object evalIncremental(File script, Object... args)
             throws EllaIOException, EllaParseException, EllaException {
         return evalIncremental(script, new DefaultContext(args));
     }
 
     /**
      * Evaluates the given EllaScript program in incremental mode using the given execution context.
      *
      * @param script the file containing the program to execute.
      * @param context the context the program is to be executed within.
      * @return the result of the evaluation of the program.
      * @throws EllaIOException if reading the program file fails.
      * @throws EllaParseException if compilation of the program fails.
      * @throws EllaException if the program throws an exception.
      * @see #executeIncremental()
      */
     public static Object evalIncremental(File script, Context context)
             throws EllaIOException, EllaParseException, EllaException {
         SimpleResource res = new FilesystemResource(script);
         Ella interp = new Ella(context, res);
         return interp.executeIncremental();
     }
 
     /**
      * Evaluates the given EllaScript program.
      *
      * @param script the file containing the program to execute.
      * @param args arguments to pass to the program.
      * @return the result of the evaluation of the program.
      * @throws EllaIOException if reading the program file fails.
      * @throws EllaParseException if compilation of the program fails.
      * @throws EllaException if the program throws an exception.
      */
     public static Object eval(File script, Object... args)
             throws EllaIOException, EllaParseException, EllaException {
         return eval(script, new DefaultContext(args));
     }
 
     /**
      * Evaluates the given EllaScript program using the given execution context.
      *
      * @param script the file containing the program to execute.
      * @param context the context the program is to be executed within.
      * @return the result of the evaluation of the program.
      * @throws EllaIOException if reading the program file fails.
      * @throws EllaParseException if compilation of the program fails.
      * @throws EllaException if the program throws an exception.
      */
     protected static Object eval(File script, Context context)
             throws EllaIOException, EllaParseException, EllaException {
         SimpleResource res = new FilesystemResource(script);
         Ella interp = new Ella(context, res);
         return interp.execute();
     }
 
     /**
      * Evaluates the given EllaScript program.
      *
      * @param script the source code of the program to execute.
      * @param args arguments to pass to the program.
      * @return the result of the evaluation of the program.
      * @throws EllaIOException if reading the program file fails.
      * @throws EllaParseException if compilation of the program fails.
      * @throws EllaException if the program throws an exception.
      */
     public static Object eval(String script, Object... args)
             throws EllaIOException, EllaParseException, EllaException {
         return eval(script, new DefaultContext(args));
     }
 
     /**
      * Evaluates the given EllaScript program using the given execution context.
      *
      * @param script the source code the program to execute.
      * @param context the context the program is to be executed within.
      * @return the result of the evaluation of the program.
      * @throws EllaIOException if reading the program file fails.
      * @throws EllaParseException if compilation of the program fails.
      * @throws EllaException if the program throws an exception.
      */
     protected static Object eval(String script, Context context)
             throws EllaIOException, EllaParseException, EllaException {
         SimpleResource res = new StringResource(script);
         Ella interp = new Ella(context, res);
         return interp.execute();
     }
 
     /**
      * Compiles the given EllaScript program into an executable form.
      *
      * @param script the file containing the program to compile.
      * @return the executable form of the program.
      * @throws EllaIOException if reading the program file fails.
      * @throws EllaParseException if compilation of the program fails.
      */
     public static Block compile(File script) throws EllaIOException, EllaParseException {
         SimpleResource res = new FilesystemResource(script);
         Ella interp = new Ella(res);
         return interp.compile();
     }
 
     /**
      * Compiles the given EllaScript program into an executable form.
      *
      * @param script the souce code of the program to compile.
      * @return the executable form of the program.
      * @throws EllaIOException if reading the program file fails.
      * @throws EllaParseException if compilation of the program fails.
      */
     public static Block compile(String script) throws EllaIOException, EllaParseException {
         SimpleResource res = new StringResource(script);
         Ella interp = new Ella(res);
         return interp.compile();
     }
 
     @SuppressWarnings({"UnusedDeclaration"})
     protected static void die(String msg, Exception e, int err) {
         System.err.println(msg);
 
         if (e != null) {
             e.printStackTrace();
         }
 
         System.exit(err);
     }
 
     protected static void die(String msg, int err) {
         die(msg, null, err);
     }
 
     protected static void die(String msg) {
         die(msg, 1);
     }
 
     protected static void usage(CmdLineParser parser) {
         System.err.println("usage: Ella [-url|-props] OPTION... {-i | [-large] [FILE]} [ARG]...");
         parser.printUsage(System.err);
         System.err.println();
         System.err.println("Known drivers:");
         for (Drivers driver : DBUtils.getKnownDrivers()) {
             System.err.print(driver);
             System.err.print(": ");
             System.err.println(join((Object[])driver.getDriverClasses()));
         }
         System.exit(1);
     }
 
     /**
      * Provides a command line interface to the EllaScript interpreter.
      *
      * @param args the command line arguments.
      */
     public static void main(String[] args) {
         Args pargs = new Args();
         CmdLineParser parser = new CmdLineParser(pargs);
         try {
             parser.parseArgument(args);
         } catch (CmdLineException e) {
             System.err.println(e.getMessage());
             usage(parser);
         }
 
         if (pargs.showUsage) {
             usage(parser);
         }
 
         String file = pargs.args.isEmpty() ? null : pargs.args.get(0);
         if (pargs.possiblyInteractive && file == null) {
             pargs.interactive = true;
         }
         if (pargs.interactive) {
             if (file != null || pargs.ast || pargs.large) {
                 usage(parser);
             }
         }
         else if (pargs.large && pargs.ast) {
             usage(parser);
         }
 
         String[] scriptArgs;
         if (pargs.args.size() > 1) {
             List<String> argsList = pargs.args.subList(1, pargs.args.size());
             scriptArgs = argsList.toArray(new String[argsList.size()]);
         }
         else {
             scriptArgs = new String[0];
         }
 
         DriverManagerDataSource ds = null;
         if (pargs.url != null && pargs.properties != null) {
             usage(parser);
         }
         try {
             if (pargs.properties != null) {
                 ds = DBUtils.createDataSourceFromProps(pargs.properties, pargs.user, pargs.pass, pargs.driver);
             }
             else if (pargs.url != null) {
                 ds = DBUtils.createDataSource(pargs.url, pargs.user, pargs.pass, pargs.driver);
             }
         } catch (DBConnectionFailedException e) {
             die("Could not connect database: " + e.getMessage());
         }
 
         Connection conn = null;
         if (ds != null) {
             try {
                 conn = ds.getConnection();
             } catch (SQLException e) {
                 die("Could not connect database: " + e.getMessage());
             }
         }
 
         try {
             FilesystemResourceLoader loader = new FilesystemResourceLoader();
             SimpleResource script = file == null ? loader.getStdinResource() : loader.getResource(file);
 
             DefaultContext context = new DefaultContext(scriptArgs);
             if (conn != null) {
                 context.getObjConnMgr().activate(conn);
             }
             context.addSQLResultListener(new SimpleSQLResultListener(System.out));
 
             Ella ella = new Ella(context, script);
             if (pargs.compile) {
                 ella.compile();
             }
             else {
                 if (pargs.verbose) {
                     // TODO: Provide functionality
                 }
                 if (pargs.interactive) {
                     ella.executeInteractive();
                 }
                 else if (pargs.ast) {
                     ella.showAST();
                 }
                 else if (pargs.large) {
                     ella.executeIncremental();
                 }
                 else {
                     ella.execute();
                 }
             }
         } catch (EllaIOException e) {
             die(e.getMessage(), e, 2);
         } catch (EllaParseException e) {
             die(e.getMessage(), e, 3);
         } catch (EllaException e) {
             die("Unhandled exception: " + e.getClass().getName() +
                 (e.getMessage() == null ? "" : ": " + e.getMessage()),
                 e, 4);
         }
     }
 
     protected static class Args {
         @Option(name = "-props", usage = "the properties file to be used to configure the database connection")
         public String properties = null;
 
         @Option(name = "-driver", usage = "the driver to be used - class name or alias (see below)")
         public String driver = null;
 
         @Option(name = "-url", usage = "the URL of the database to connect to")
         public String url = null;
 
         @Option(name = "-user", usage = "the username to use for connections")
         public String user = null;
 
         @Option(name = "-pass", usage = "the password to use for connections")
         public String pass = null;
 
         @Option(name = "-ast",
                 usage = "show AST instead of executing script (not available in interactive and large file modes)")
         public boolean ast = false;
 
 //        @Option(name = "-verbose", usage = "echo executed statements")
         public boolean verbose = false;
 
         @Option(name = "-large", usage = "optimize for large files")
         public boolean large = false;
 
         @Option(name = "-c", usage = "compile only")
         public boolean compile = false;
 
         @Option(name = "-i", usage = "operate in interactive mode")
         public boolean interactive = false;
 
         @Option(name = "-h", aliases = "-help", usage = "prints this message")
         public boolean showUsage = false;
 
         /**
          * This option is intended to be used by wrapper scripts to indicate that interactive mode should
          * be activated if input is stdin.
          * This avoids the need for wrapper scripts to contain logic determining when stdin would be used as input.
          */
         @Option(name = "-I")
         public boolean possiblyInteractive = false;
 
         @Argument
         public List<String> args = new ArrayList<String>();
     }
 }
