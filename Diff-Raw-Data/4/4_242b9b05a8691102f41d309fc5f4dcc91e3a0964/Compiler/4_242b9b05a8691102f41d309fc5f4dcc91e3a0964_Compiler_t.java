 /* -*- mode: Java; c-basic-offset: 2; -*- */
 
 /***
  * Compiler.java
  * Author: Oliver Steele, P T Withington
  * Description: JavaScript -> SWF bytecode compiler
  */
 
 package org.openlaszlo.sc;
 
 import java.io.*;
 import java.util.*;
 import java.util.regex.Pattern;
 import java.text.SimpleDateFormat;
 import java.text.DecimalFormat;
 
 import org.openlaszlo.server.LPS;
 import org.openlaszlo.sc.parser.*;
 import org.openlaszlo.sc.Translator;
 
 // Values
 import org.openlaszlo.sc.Values;
 
 // Instructions
 import org.openlaszlo.sc.Instructions;
 import org.openlaszlo.sc.Instructions.Instruction;
 import org.openlaszlo.sc.InstructionPrinter;
 
 public class Compiler {
   // The parse tree is stored with the key (fname) and the
   // value (ASTProgram, hasIncludes).
   // It doesn't save any time to persist this cache to disk.
   public static ScriptCompilerCache CachedParses;
   // The instructions are stored with the keys (fname, cpass) where
   // cpass is one of the compiler passes (1 or 2).  The checksum in
   // both cases is the file content string.
   // It costs 10s to persist this to disk, but speeds up subsequent
   // compiles.
   // Instantiate this lazily, so that we don't construct it in server
   // mode (since the call to os.getenv in the cache constructor won't
   // work there).
   public static ScriptCompilerCache CachedInstructions;
 
   public OptionMap options;
 
   //
   // Compiler Facade
   //
   public Compiler (Map initialOptions) {
     this.options = new OptionMap(initialOptions);
     if (! options.containsKey(ACTIVATION_OBJECT)) {
       options.putBoolean(ACTIVATION_OBJECT,
                          ! options.getBoolean(FLASH_COMPILER_COMPATABILITY));
     }
     if (! options.containsKey(COMPILE_TIME_CONSTANTS)) {
       options.put(COMPILE_TIME_CONSTANTS, new HashMap());
     }
     // TODO: [2002-1-05 ows] enable this instead of the line that
     // follows it, once the sources comply
     //- options.put(ALLOW_ROOT,
     //- options.get(FLASH_COMPILER_COMPATABILITY))
     if (! options.containsKey(ALLOW_ROOT)) {
       options.putBoolean(ALLOW_ROOT, true);
     }
     if (! options.containsKey(OBFUSCATE)) {
       options.putBoolean(OBFUSCATE, false);
     }
     if (! options.containsKey(RUNTIME)) {
       options.put(RUNTIME, LPS.getProperty("compiler.runtime.default", LPS.getRuntimeDefault()));
     }
     defaultOptions();
     if (options.getBoolean(PRINT_COMPILER_OPTIONS)) {
       System.err.println("init compiler options" +  options.toString());
     }
   }
 
   public Compiler () {
     this(new HashMap());
   }
 
   // Map for options
   public static class OptionMap extends HashMap {
     public OptionMap () {
       super();
     }
 
     public OptionMap (Map m) {
       super(m);
     }
 
     public OptionMap (List pairs) {
       for (Iterator i = pairs.iterator(); i.hasNext(); ) {
         List pair = (List)i.next();
         put(pair.get(0), pair.get(1));
       }
     }
 
     // Python
     public OptionMap copy() {
       return (OptionMap)clone();
     }
 
     public Object get(Object key) {
       if (containsKey(key)) {
         return (super.get(key));
       }
       return null;
     }
 
     // For Jython
     public Object get(Object key, Object deflt) {
       if (containsKey(key)) {
         return (get(key));
       }
       return deflt;
     }
 
     public boolean getBoolean(Object key) {
       boolean result = false;
       Object value = null;
       if (containsKey(key)) {
         value = get(key);
       }
       if (value != null) {
         if (value instanceof String) {
           result = "true".equalsIgnoreCase((String)value);
         } else if (value instanceof Integer) {
           result = (! Integer.valueOf("0").equals(value));
         } else {
           result = ((Boolean)value).booleanValue();
         }
       }
       return result;
     }
 
     public void putBoolean(Object key, boolean value) {
       put(key, Boolean.valueOf(value));
     }
 
     public void putBoolean(Object key, String value) {
       put(key, Boolean.valueOf(value));
     }
   }
 
   // Error support
   public static String getLocationString(SimpleNode node) {
     StringBuffer location = new StringBuffer();
     if (node != null) {
       if (node.filename != null) {
         location.append(node.filename);
         if (node.beginLine != 0) {
           location.append("#");
           location.append(Integer.toString(node.beginLine));
           location.append(".");
           location.append(Integer.toString(node.beginColumn));
         }
       }
     }
     return location.toString();
   }
 
   public static Instruction NONE = Instructions.NONE;
   public static Instruction NextFrame = Instructions.NextFrame;
   public static Instruction PreviousFrame = Instructions.PreviousFrame;
   public static Instruction PLAY = Instructions.PLAY;
   public static Instruction STOP = Instructions.STOP;
   public static Instruction ToggleQuality = Instructions.ToggleQuality;
   public static Instruction StopSounds = Instructions.StopSounds;
   public static Instruction NumericAdd = Instructions.NumericAdd;
   public static Instruction SUBTRACT = Instructions.SUBTRACT;
   public static Instruction MULTIPLY = Instructions.MULTIPLY;
   public static Instruction DIVIDE = Instructions.DIVIDE;
   public static Instruction OldEquals = Instructions.OldEquals;
   public static Instruction OldLessThan = Instructions.OldLessThan;
   public static Instruction LogicalAnd = Instructions.LogicalAnd;
   public static Instruction LogicalOr = Instructions.LogicalOr;
   public static Instruction NOT = Instructions.NOT;
   public static Instruction StringEqual = Instructions.StringEqual;
   public static Instruction StringLength = Instructions.StringLength;
   public static Instruction SUBSTRING = Instructions.SUBSTRING;
   public static Instruction POP = Instructions.POP;
   public static Instruction INT = Instructions.INT;
   public static Instruction GetVariable = Instructions.GetVariable;
   public static Instruction SetVariable = Instructions.SetVariable;
   public static Instruction SetTargetExpression = Instructions.SetTargetExpression;
   public static Instruction StringConcat = Instructions.StringConcat;
   public static Instruction GetProperty = Instructions.GetProperty;
   public static Instruction SetProperty = Instructions.SetProperty;
   public static Instruction DuplicateMovieClip = Instructions.DuplicateMovieClip;
   public static Instruction RemoveClip = Instructions.RemoveClip;
   public static Instruction TRACE = Instructions.TRACE;
   public static Instruction StartDragMovie = Instructions.StartDragMovie;
   public static Instruction StopDragMovie = Instructions.StopDragMovie;
   public static Instruction StringLessThan = Instructions.StringLessThan;
   public static Instruction RANDOM = Instructions.RANDOM;
   public static Instruction MBLENGTH = Instructions.MBLENGTH;
   public static Instruction ORD = Instructions.ORD;
   public static Instruction CHR = Instructions.CHR;
   public static Instruction GetTimer = Instructions.GetTimer;
   public static Instruction MBSUBSTRING = Instructions.MBSUBSTRING;
   public static Instruction MBORD = Instructions.MBORD;
   public static Instruction MBCHR = Instructions.MBCHR;
   public static Instruction GotoFrame = Instructions.GotoFrame;
   public static Instruction GetUrl = Instructions.GetUrl;
   public static Instruction WaitForFrame = Instructions.WaitForFrame;
   public static Instruction SetTarget = Instructions.SetTarget;
   public static Instruction GotoLabel = Instructions.GotoLabel;
   public static Instruction WaitForFrameExpression = Instructions.WaitForFrameExpression;
   public static Instruction PUSH = Instructions.PUSH;
   public static Instruction BRANCH = Instructions.BRANCH;
   public static Instruction GetURL2 = Instructions.GetURL2;
   public static Instruction BranchIfTrue = Instructions.BranchIfTrue;
   public static Instruction CallFrame = Instructions.CallFrame;
   public static Instruction GotoExpression = Instructions.GotoExpression;
   public static Instruction DELETE = Instructions.DELETE;
   public static Instruction DELETE2 = Instructions.DELETE2;
   public static Instruction VarEquals = Instructions.VarEquals;
   public static Instruction CallFunction = Instructions.CallFunction;
   public static Instruction RETURN = Instructions.RETURN;
   public static Instruction MODULO = Instructions.MODULO;
   public static Instruction NEW = Instructions.NEW;
   public static Instruction VAR = Instructions.VAR;
   public static Instruction InitArray = Instructions.InitArray;
   public static Instruction InitObject = Instructions.InitObject;
   public static Instruction TypeOf = Instructions.TypeOf;
   public static Instruction TargetPath = Instructions.TargetPath;
   public static Instruction ENUMERATE = Instructions.ENUMERATE;
   public static Instruction ADD = Instructions.ADD;
   public static Instruction LessThan = Instructions.LessThan;
   public static Instruction EQUALS = Instructions.EQUALS;
   public static Instruction ObjectToNumber = Instructions.ObjectToNumber;
   public static Instruction ObjectToString = Instructions.ObjectToString;
   public static Instruction DUP = Instructions.DUP;
   public static Instruction SWAP = Instructions.SWAP;
   public static Instruction GetMember = Instructions.GetMember;
   public static Instruction SetMember = Instructions.SetMember;
   public static Instruction Increment = Instructions.Increment;
   public static Instruction Decrement = Instructions.Decrement;
   public static Instruction CallMethod = Instructions.CallMethod;
   public static Instruction NewMethod = Instructions.NewMethod;
   public static Instruction BitwiseAnd = Instructions.BitwiseAnd;
   public static Instruction BitwiseOr = Instructions.BitwiseOr;
   public static Instruction BitwiseXor = Instructions.BitwiseXor;
   public static Instruction ShiftLeft = Instructions.ShiftLeft;
   public static Instruction ShiftRight = Instructions.ShiftRight;
   public static Instruction UShiftRight = Instructions.UShiftRight;
   public static Instruction SetRegister = Instructions.SetRegister;
   public static Instruction CONSTANTS = Instructions.CONSTANTS;
   public static Instruction WITH = Instructions.WITH;
   public static Instruction DefineFunction = Instructions.DefineFunction;
   public static Instruction DefineFunction2 = Instructions.DefineFunction2;
   public static Instruction InstanceOf = Instructions.InstanceOf;
   public static Instruction EnumerateValue = Instructions.EnumerateValue;
   public static Instruction StrictEquals = Instructions.StrictEquals;
   public static Instruction GreaterThan = Instructions.GreaterThan;
   public static Instruction StringGreaterThan = Instructions.StringGreaterThan;
   public static Instruction BranchIfFalse = Instructions.BranchIfFalse;
   public static Instruction LABEL = Instructions.LABEL;
   public static Instruction COMMENT = Instructions.COMMENT;
   public static Instruction CHECKPOINT = Instructions.CHECKPOINT;
   public static Instruction BLOB = Instructions.BLOB;
 
   // Set internal flags that depend on external flags
   public void defaultOptions() {
 
     // Disable debug compilation for swf9
     if ("swf9".equals((String)options.get(RUNTIME)) || "swf10".equals((String)options.get(RUNTIME))) {
       options.putBoolean(DEBUG_SWF9, options.getBoolean(DEBUG));
       options.putBoolean(DEBUG, false);
       options.putBoolean(DEBUG_BACKTRACE, false);
      if (! options.containsKey(CATCH_FUNCTION_EXCEPTIONS)) {
        options.put(CATCH_FUNCTION_EXCEPTIONS,
                    Boolean.valueOf(LPS.getProperty("compiler.swf9.catcherrors", "false")));
      }
     }
 
     // TODO: [2008-05-18 dda] It may be possible to clean this up
     // a little now that we know this is only called once.
 
     if (options.getBoolean(DEBUG)) {
       options.put(WARN_UNDEFINED_REFERENCES, Boolean.TRUE);
       if (! options.containsKey(WARN_GLOBAL_ASSIGNMENTS)) {
       options.put(WARN_GLOBAL_ASSIGNMENTS,
                   Boolean.valueOf(LPS.getProperty("compiler.warn.globalassignments", "false")));
       }
       if (! options.containsKey(WARN_UNUSED_LOCALS)) {
         options.put(WARN_UNUSED_LOCALS,
                     Boolean.valueOf(LPS.getProperty("compiler.warn.unusedlocals", "false")));
       }
       if (! options.containsKey(WARN_UNUSED_PARAMETERS)) {
         options.put(WARN_UNUSED_PARAMETERS,
                     Boolean.valueOf(LPS.getProperty("compiler.warn.unusedparameters", "false")));
       }
       options.putBoolean(NAME_FUNCTIONS, true);
     }
 
     // TODO: [2005-04-15 ptw] This pretty much sucks, but the debug
     // lfc only sets nameFunctions, not debug.  This can go away
     // when we can turn on debug for the lfc.
     if (options.getBoolean(DEBUG) ||
         options.getBoolean(NAME_FUNCTIONS)) {
       if (! options.containsKey(DEBUG_BACKTRACE)) {
         options.putBoolean(DEBUG_BACKTRACE, false);
       }
       if (! options.containsKey(DEBUG_SIMPLE)) {
         options.putBoolean(DEBUG_SIMPLE, false);
       }
     }
     if (! options.containsKey(PROFILE)) {
       options.putBoolean(PROFILE, false);
     }
     if (!options.containsKey(DISABLE_TRACK_LINES) &&
         options.getBoolean(NAME_FUNCTIONS)) {
       options.putBoolean(TRACK_LINES, true);
     }
     if (options.getBoolean(PROFILE)) {
       options.putBoolean(NAME_FUNCTIONS, true);
     }
     options.putBoolean(GENERATE_FUNCTION_2, true);
     options.putBoolean(GENERATE_FUNCTION_2_FOR_LZX, true);
 
     if (options.getBoolean(DEBUG_SWF9)) {
       options.putBoolean(NAME_FUNCTIONS, true);
     }
   }
 
   public byte[] compile(String source) {
     try {
       Profiler profiler = new Profiler();
       byte[] bytes;
       String runtime = (String)options.get(RUNTIME);
       boolean compress = (! options.getBoolean(NAME_FUNCTIONS));
       boolean obfuscate = options.getBoolean(OBFUSCATE);
       boolean isScript = org.openlaszlo.compiler.Compiler.SCRIPT_RUNTIMES.contains(runtime);
       Translator cg;
       if (runtime.equals("swf9") || runtime.equals("swf10")) {
         cg = new SWF9Generator();
       }
       else if (isScript) {
         cg = new JavascriptGenerator();
       }
       else {
         cg = new CodeGenerator();
       }
       cg.setOptions(options);
       cg.setOriginalSource(source);
 
       profiler.enter("parse");
       source = cg.preProcess(source);
       SimpleNode program = new Parser().parse(source);
 
       String astInputFile = (String)options.get(DUMP_AST_INPUT);
       if (astInputFile != null) {
         String newname = emitFile(astInputFile, program);
         System.err.println("Created " + newname);
       }
       profiler.phase("generate");
       SimpleNode translated = cg.translate(program);
       String astOutputFile = (String)options.get(DUMP_AST_OUTPUT);
       if (astOutputFile != null) {
         String newname = emitFile(astOutputFile, translated);
         System.err.println("Created " + newname);
       }
 
       if (isScript) {
 
         List tunits = cg.makeTranslationUnits(translated, compress, obfuscate);
         bytes = cg.postProcess(tunits);
 
       } else {
         if (options.getBoolean(PROGRESS)) {
           System.err.println("Assembling...");
         }
         profiler.phase("collect");
         List instrs = ((InstructionCollector)cg.getCollector()).getInstructions(true);
         if (options.getBoolean(PRINT_INSTRUCTIONS)) {
           new Optimizer(new InstructionPrinter()).assemble(instrs);
         }
         profiler.phase("assemble");
         Emitter asm = new Optimizer(new Assembler());
         // end marker
         instrs.add(NONE);
         bytes = asm.assemble(instrs);
       }
       profiler.exit();
       if (options.getBoolean(PROFILE_COMPILER)) {
         profiler.pprint();
         System.err.println();
       }
       if (options.getBoolean(PROGRESS)) {
         System.err.println("done.");
       }
       return bytes;
     }
     catch (CompilerImplementationError e) {
       String ellipses = source.trim().length() > 80 ? "..." : "";
       System.err.println("while compiling " +  source.trim().substring(0, 80) + ellipses);
       throw(e);
     }
     catch (CompilerError e) {
       throw(new CompilerException(e.toString()));
     }
   }
 
   /**
    * @param source javascript source for a function (including function keyword, etc.)
    * @param name name, will get name_dependencies as name of function
    */
   public String dependenciesForExpression(String estr) {
     String fname = "$lsc$dependencies";
     String source = "function " + fname + " () {" +
       "\n#pragma 'constraintFunction'\n" +
       "\n#pragma 'withThis'\n" +
       // Use this.setAttribute so that the compiler
       // will recognize it for inlining.
       "this.setAttribute(\"x\", " +
                     "\n#beginAttribute\n" + estr +
                     "\n#endAttribute\n)}";
 
     return dependenciesForFunction(source);
   }
 
   /**
    * @param source javascript source for a function
    */
   private String dependenciesForFunction(String source) {
     SimpleNode node = new Parser().parse(source);
     // Expect ASTProgram -> ASTModifiedDefinition -> ASTFunctionDeclaration
     SimpleNode fcn = node.get(0).get(0);
     if (!(fcn instanceof ASTFunctionDeclaration))
       throw new CompilerError("Internal error: bad AST for constraints");
 
     ReferenceCollector dependencies = new ReferenceCollector(options.getBoolean(Compiler.COMPUTE_METAREFERENCES));
 
     SimpleNode[] children = fcn.getChildren();
     int stmtpos = (children.length - 1);
     for (;stmtpos < children.length; stmtpos++) {
       dependencies.visit(children[stmtpos]);
     }
     SimpleNode depExpr = dependencies.computeReferencesAsExpression();
     String result = new ParseTreePrinter().text(depExpr);
     if (options.getBoolean(Compiler.PRINT_CONSTRAINTS)) {
       System.out.println(result);
     }
     return result;
   }
 
   //
   // Compiler Options
   //
 
   // TODO [2004-03-11 ptw] share with CompilationEnvironment.java
   public static String CANVAS_WIDTH = "canvasWidth";
   public static String CANVAS_HEIGHT = "canvasHeight";
   public static String ACTIVATION_OBJECT = "createActivationObject";
   public static String BUILD_SHARED_LIBRARY = "buildSharedLibrary";
   public static String COMPUTE_METAREFERENCES = "computeMetaReferences";
   public static String CONDITIONAL_COMPILATION = "conditionalCompilation";
   public static String ALLOW_ROOT = "allowRoot";
   public static String CACHE_COMPILES = "cacheCompiles";
   public static String CATCH_FUNCTION_EXCEPTIONS = "catchFunctionExceptions";
   public static String COMPILE_TRACE = "compileTrace";
   public static String COMPILE_TIME_CONSTANTS = "compileTimeConstants";
   public static String COMPILER_INFO = "compilerInfo";
   public static String CONSTRAINT_FUNCTION = "constraintFunction";
   public static String DEBUG = "debug";
   public static String DEBUG_BACKTRACE = "debugBacktrace";
   public static String DEBUG_SWF9 = "debugSWF9";
   public static String DEBUG_SIMPLE = "debugSimple";
   public static String DEBUG_EVAL = "debugEval";
   public static String DUMP_AST_INPUT = "dumpASTInput";
   public static String DUMP_AST_OUTPUT = "dumpASTOutput";
   public static String DUMP_LINE_ANNOTATIONS = "dumpLineAnnotations";
   public static String DISABLE_CONSTANT_POOL = "disableConstantPool";
   public static String DISABLE_TRACK_LINES = "disableTrackLines";
   public static String DISABLE_PUBLIC_FOR_DEBUG = "disablePublicForDebug";
   public static String EXPORTED_CLASS_DEFS = "exportedClassDefs";
   public static String ELIMINATE_DEAD_EXPRESSIONS = "eliminateDeadExpressions";
   public static String FLASH_COMPILER_COMPATABILITY = "flashCompilerCompatability";
   public static String GENERATE_FUNCTION_2 = "generateFunction2";
   public static String GENERATE_FUNCTION_2_FOR_LZX = "generateFunction2ForLZX";
   public static String GENERATE_PREDICTABLE_TEMPS = "generatePredictableTemps";
   public static String INCLUDES = "processIncludes";
   public static String INSTR_STATS = "instrStats";
   public static String LINK = "link";
   public static String RUNTIME = "runtime";
   public static String METHOD_NAME = "methodName";
   public static String NAME_FUNCTIONS = "nameFunctions";
   public static String OBFUSCATE = "obfuscate";
   public static String PASSTHROUGH_FORMAL_INITIALIZERS = "passthroughFormalInitializers";
   public static String PROFILE = "profile";
   public static String PROFILE_COMPILER = "profileCompiler";
   public static String PROGRESS = "progress";
   public static String PRINT_COMPILER_OPTIONS = "printCompilerOptions";
   public static String PRINT_CONSTRAINTS = "printConstraints";
   public static String PRINT_INSTRUCTIONS = "printInstructions";
   public static String RESOLVER = "resolver";
   public static String SCRIPT_ELEMENT = "scriptElement";
   public static String SWF9_APPLICATION_PREAMBLE = "SWF9applicationPreamble";
   public static String SWF9_APP_CLASSNAME = "SWF9MainClassName";
   public static String SWF9_WRAPPER_CLASSNAME = "SWF9WrapperClassName";
   public static String SWF9_LFC_CLASSNAME = "SWF9LFCClassName";  
   public static String SWF9_LOADABLE_LIB = "SWF9LoadableLib";
   public static String SWF9_USE_RUNTIME_SHARED_LIB = "SWF9RuntimeSharedLib";
   public static String SWF8_LOADABLE_LIB = "SWF8LoadableLib";
   public static String TRACK_LINES = "trackLines";
   public static String VALIDATE_CACHES = "validateCaches";
   public static String WARN_UNDEFINED_REFERENCES = "warnUndefinedReferences";
   public static String WARN_GLOBAL_ASSIGNMENTS = "warnGlobalAssignments";
   public static String WARN_UNUSED_LOCALS = "warnUnusedLocals";
   public static String WARN_UNUSED_PARAMETERS = "warnUnusedParameters";
   public static String WITH_THIS = "withThis";
 
 
   //
   // Parser
   //
 
   // A scanner and parser generated by JavaCC and jjtree are used to
   // create a Java AST of the input, with literals annotated by Java
   // objects (instances of String and the numeric types).
   public static class Ops implements ParserConstants {};
 
   // Wrapper for values that Parser.substitute should splice into
   // place, instead of substituting at the level of the template
   // variable.
   public static class Splice {
     SimpleNode value[];
 
     public Splice(SimpleNode[] value) {
       this.value = value;
     }
 
     public String toString() {
       return "Splice(" + value.toString() + ")";
     }
   }
 
   // Wrapper for the Java parser.  Returns a tuple-tree.
   public static class Parser {
     public SimpleNode parse0(String str, String type) {
       org.openlaszlo.sc.parser.Parser p =
         new org.openlaszlo.sc.parser.Parser(new StringReader(str));
       assert "Program".equals(type);
       try {
         return p.Program();
       } catch (ParseException pe) {
         // NOTE: [2007-03-27 ptw]
         // The parser tracks #file declarations, but does not pass the
         // file to the exception constructor, so we fix that up here.
         // (This is really a limitation of javacc.)
         pe.initPathname(p.token_source.pathname);
         throw pe;
       }
     }
 
     public SimpleNode parse0(String str) {
       return parse0(str, "Program");
     }
 
     public SimpleNode parse(String str) {
       SimpleNode node = parse0(str, "Program");
       SimpleNode refactored = refactorAST(node);
       if (refactored != null) {
         return refactored;
       } else {
         return node;
       }
     }
 
     private ParseTreePrinter ptp = new ParseTreePrinter();
     // The transforms in this branch insure that each binary
     // expression sequence has exactly two children (not
     // counting the operator).
     private void fold(SimpleNode node, int arity) {
       // Transform K(a0,a1,a2) -> K(K(a0,a1),a2), such that no K node
       // has an arity greater than arity.
       int size = node.size();
       if (size > arity) {
         try {
           // TODO: [2005-11-21 ptw] clone would be simpler, if you
           // could make it work
           java.lang.reflect.Constructor constructor = node.getClass().getConstructor(new Class[] { int.class });
           SimpleNode child = (SimpleNode)constructor.newInstance(new Object[] { Integer.valueOf("0") });
           child.setBeginLocation(node.filename, node.beginLine, node.beginColumn);
           int split = size - (arity - 1);
           SimpleNode[] children = new SimpleNode[split];
           for (int i = 0; i < split; i++) {
             children[i] = node.get(i);
           }
           child.setChildren(children);
           if (child.size() > arity) {
             fold(child, arity);
           }
           children = new SimpleNode[arity];
           children[0] = child;
           for (int i = split, j = 1; i < size; i++, j++) {
             children[j] = node.get(i);
           }
           node.setChildren(children);
         }
         catch (InstantiationException e) {
           assert false : e.toString();
         }
         catch (IllegalAccessException e) {
           assert false : e.toString();
         }
         catch (NoSuchMethodException e) {
           assert false : e.toString();
         }
         catch (java.lang.reflect.InvocationTargetException e) {
           assert false : e.toString();
         }
       }
     }
 
     // Modify the AST tree rooted at n so that its branching
     // structure matches evaluation order.  This is necessary because
     // the parser is right-recursive, and generates flat trees for
     // a+b+c and a.b.c.
     public SimpleNode refactorAST(SimpleNode node) {
       if (node == null || node.size() == 0) {
         return null;
       }
       for (int i = 0; i < node.size(); i++) {
         SimpleNode x = refactorAST(node.get(i));
         if (x != null) {node.set(i, x);}
       }
       if (node instanceof ASTBinaryExpressionSequence) {
         // Transform a flat sequence of subexpressions with
         // alternating operators into a right-branching binary
         // tree.  This corrects the fact that the parser, being
         // recursive-descent, is right-factored, but the operators
         // are left-associative.
         //
         // For example:
         // K(a, o1, b, o2, c) -> K(K(a, o1, b), o2, c)
         // K(a, o1, b, o2, c, o3, d) -> K(K(K(a, o1, b), o2, c), o3, d)
         fold(node, 3);
       }
       else if (node instanceof ASTAndExpressionSequence ||
                node instanceof ASTOrExpressionSequence) {
         // Transforms K(a, b, c) -> K(K(a, b), c),
         // where node is in (AndExpressionSequence, OrExpressionSequence)
         fold(node, 2);
       }
       if (node instanceof ASTCallExpression) {
         // cf., CallExpression in Parser.jjt
         // C(a, P(b)) -> P(a, b)
         // C(a, P(b), P(c)) -> P(P(a, b), c)
         // C(a, P(b), A) -> C(P(a, b), A)
         // C(a, P(b), P(c), A) -> C(P(P(a, b), c), A)
         // C(a, A) -> C(a, A)
         // C(a, A, P(b)) -> P(C(a, A), b)
         // where
         //   C = CallExpression
         //   P = PropertyIdentifierReference
         //   A = FunctionCallParameters
         while (node.size() > 1) {
           if (node.get(1) instanceof ASTFunctionCallParameters) {
             if (node.size() > 2) {
               try {
                 int size = node.size();
                 // TODO: [2005-11-21 ptw] clone would be simpler, if
                 // you could make it work
                 java.lang.reflect.Constructor constructor = node.getClass().getConstructor(new Class[] { int.class });
                 SimpleNode child = (SimpleNode)constructor.newInstance(new Object[] { Integer.valueOf("0") });
                 child.setBeginLocation(node.filename, node.beginLine, node.beginColumn);
                 SimpleNode children[] = new SimpleNode[2];
                 children[0] = node.get(0);
                 children[1] = node.get(1);
                 child.setChildren(children);
                 children = new SimpleNode[size - 2 + 1];
                 children[0] = child;
                 for (int i = 2, j = 1; i < size; i++, j++) {
                   children[j] = node.get(i);
                 }
                 node.setChildren(children);
               }
               catch (InstantiationException e) {
                 assert false : e.toString();
               }
               catch (IllegalAccessException e) {
                 assert false : e.toString();
               }
               catch (NoSuchMethodException e) {
                 assert false : e.toString();
               }
               catch (java.lang.reflect.InvocationTargetException e) {
                 assert false : e.toString();
               }
               continue;
             }
             else {
               break;
             }
           }
           SimpleNode prop = node.get(1);
           assert ((prop instanceof ASTPropertyIdentifierReference ||
                    prop instanceof ASTPropertyValueReference) &&
                   prop.size() > 0 ): (new ParseTreePrinter()).text(prop);
           int size = node.size();
           SimpleNode children[] = new SimpleNode[2];
           children[0] = node.get(0);
           children[1] = prop.get(0);
           prop.setChildren(children);
           children = new SimpleNode[size - 1];
           for (int i = 1, j = 0; i < size; i++, j++) {
             children[j] = node.get(i);
           }
           node.setChildren(children);
         }
         if (node.size() == 1) {
           return node.get(0);
         }
       }
       // After refactoring, assure each function has a name
       // for debugging and profiling
       if (node instanceof ASTAssignmentExpression) {
         SimpleNode rhs = node.get(2);
         if (rhs instanceof ASTFunctionExpression) {
           // fn children are [(name), arglist, body]
           if (rhs.size() == 2) {
             String name = ptp.text(node.get(0));
             SimpleNode child = rhs;
             int size = child.size();
             SimpleNode children[] = new SimpleNode[size + 1];
             children[0] = new ASTIdentifier(name);
             for (int i = 0, j = 1; i < size; i++, j++) {
               children[j] = child.get(i);
             }
             child.setChildren(children);
           }
         }
       }
       return node;
     }
 
 // UNUSED
 //     // Build a node out of an AST tuple-tree
 //     public void build(*tuple) {
 //         node = tuple[0]
 //         assert ! node.children
 //         for (child in tuple[1)]:
 //             if (isinstance(child, TupleType))
 //                 child = build(*child)
 //             node.jjtAddChild(child, len(node.children))
 //         return node
 
     private SimpleNode visit(SimpleNode node, Map keys) {
       List result = new ArrayList();
       int size = node.size();
       for (int i = 0; i < size; i++) {
         SimpleNode child = node.get(i);
         if (child instanceof ASTIdentifier) {
           String name = ((ASTIdentifier)child).getName();
           if (keys.containsKey(name)) {
             Object value = keys.get(name);
             if (value instanceof Splice) {
               result.addAll(Arrays.asList(((Splice)value).value));
             } else {
               result.add(value);
             }
             continue;
           }
         }
         result.add(visit(child, keys));
       }
       SimpleNode[] children = new SimpleNode[result.size()];
       node.setChildren((SimpleNode[])result.toArray(children));
       return node;
     }
 
     private String fileLineInputString(SimpleNode n) {
       if (n == null || n.getFilename() == null) {
         return "#file [Compiler.substitute]\n#line 0\n";
       }
       else {
         return "#file " + n.getFilename() + "\n#line " + n.getLineNumber() + "\n";
       }
     }
 
     // Parse an expression and replace any identifier with the same
     // name as a keyword argument to this function, with the value of
     // that key.  If the value has type Splice, it's spliced into
     // place instead of substituting at the same level.
     // If the 'nearnode' argument is non-null, its file/line info
     // is used.
     //
     // >>> s = Parser().substitute
     // >>> s("[0,1,2]")
     // (ASTArrayLiteral, Literal(0.0), Literal(1), Literal(2))
     // >>> s("[_0,1,2]", _0=Literal("sub"))
     // (ASTArrayLiteral, Literal(sub), Literal(1), Literal(2))
     // >>> s("[_0,1,2]", _0=s("[a,b,c]"))
     // (ASTArrayLiteral, (ASTArrayLiteral, ASTIdentifier(a), ASTIdentifier(b), ASTIdentifier(c)), Literal(1), Literal(2))
     // >>> s("[_0,1,2]", _0=Splice(s("[a,b,c]")))
     // (ASTArrayLiteral, ASTArrayLiteral, ASTIdentifier(a), ASTIdentifier(b), ASTIdentifier(c), Literal(1), Literal(2))
     //
     // N.B., there is no attempt to enforce macro hygiene
     public SimpleNode substitute(SimpleNode nearnode, String str, Map keys) {
       // Since the parser can't parse an Expression, turn the source
       // into a Program, and extract the Expression from the parse tree.
       SimpleNode node = parse("x = \n" + fileLineInputString(nearnode) + str).get(0).get(0).get(2);
       return visit(node, keys);
     }
 
     // Input is one or more statements, returns the nodes
     // that correspond to those statements
     public SimpleNode[] substituteStmts(SimpleNode nearnode, String str, Map keys) {
       SimpleNode fexpr = substitute(nearnode, "(function () {" + str + "})()", keys);
       return fexpr.get(0).get(1).getChildren();
     }
 
     public SimpleNode substituteStmt(SimpleNode nearnode, String str, Map keys) {
       SimpleNode node = parse(fileLineInputString(nearnode) + str).get(0);
       return visit(node, keys);
     }
 
   // Visitor -- only works for ParseTreePrinter so far
 //   public abstract static class Visitor {
 //     public java.lang.reflect.Method getVisitor(SimpleNode node) {
 //       // trim the module name, and the initial "AST"
 //       String name;
 //       if (node instanceof ASTIdentifier) {
 //         name = "Identifier";
 //       } else {
 //         name = node.getClass().getName();
 //         name = name.substring(name.lastIndexOf(".")+4, name.length());
 //       }
 //       try {
 //         return getClass().getMethod(
 //           "visit" + name,
 //           new Class[] { SimpleNode.class, String[].class }
 //           );
 //       } catch (NoSuchMethodException e) {
 //         System.err.println("Missing visitor: " + e.toString());
 //         try {
 //           return getClass().getMethod(
 //             "defaultVisitor",
 //             new Class[] { Object.class, Object[].class }
 //             );
 //         } catch (NoSuchMethodException ee) {
 //           assert false : ee.toString();
 //         }
 //       }
 //       assert false : "can't happen";
 //       return null;
 //     }
 
 //     public abstract Object defaultVisitor(Object o, Object[] children);
 //   }
 
   }
 
   // ASTNode -> fname, lineno
   public static class SourceLocation {
     private String file;
     private int line;
 
     public SourceLocation (String file, int line) {
       this.file = file;
       this.line = line;
     }
 
     public static SourceLocation get(SimpleNode node) {
       return new SourceLocation( node.filename != null ? node.filename : "unknown file", node.beginLine);
     }
   }
 
 
   public static class PassThroughNode extends SimpleNode {
     public SimpleNode realNode;
 
     private PassThroughNode() { }
 
     public PassThroughNode (SimpleNode realNode) {
       this.realNode = realNode;
     }
 
     public SimpleNode deepCopy() {
       PassThroughNode result = new PassThroughNode();
       result.copyFields(this);
       return result;
     }
 
     public SimpleNode copyFields(SimpleNode that) {
       super.copyFields(that);
       this.realNode = ((PassThroughNode)that).realNode.deepCopy();
       return this;
     }
 
     public void dump(String prefix) {
       super.dump(prefix);
       if (realNode != null) {
         realNode.dump(prefix + " ");
       }
     }
   }
 
   //
   // Profiler for hand-instrumentation of Compiler
   //
   public static class Profiler {
     static SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm:ss.SS");
     static {
       timeFormatter.setTimeZone(TimeZone.getTimeZone("GMT"));
     }
     static DecimalFormat percentFormatter = new DecimalFormat("###.00");
 
     public static class Block {
       Date startTime;
       long elapsed;
       String name;
       Block parent;
       List children;
 
       Block (String name) {
         this.startTime = new Date();
         this.name = name;
         this.children = new ArrayList();
       }
 
       Block make(String name) {
         Block block = new Block(name);
         block.parent = this;
         children.add(block);
         return block;
       }
     }
 
     List names;
     Block main;
     Block current;
 
     Profiler () {
       this.names = new ArrayList();
       Block block =  new Block("__main__");
       this.main = block;
       this.current = block;
     }
 
     public void enter(String name) {
       Block block = current.make(name);
       current = block;
     }
 
     public void exit() {
       current.elapsed = (new Date()).getTime() - current.startTime.getTime();
       current = current.parent;
     }
 
     public void phase(String name) {
       exit();
       enter(name);
     }
 
     public void pprint() {
       long total = 0;
       for (Iterator i = current.children.iterator(); i.hasNext(); ) {
         Block next = (Block)i.next();
         total += next.elapsed;
       }
       for (Iterator i = current.children.iterator(); i.hasNext(); ) {
         Block next = (Block)i.next();
         long time = next.elapsed;
         Object interval = new Date(next.elapsed);
         System.out.println(
           next.name + "\t" +
           timeFormatter.format(interval) + "\t" +
           percentFormatter.format(time*100/total));
       }
     }
   }
 
   private static ParseTreePrinter ptp = new ParseTreePrinter();
 
   public static String nodeString(SimpleNode node) {
     return ptp.text(node);
   }
 
   // TextEmitter and associated methods are used to create
   // files useful for debugging the compiler only, so errors are ignored.
 
   /**
    * A source of text data.
    */
   public interface TextEmitter {
     void emit(Writer writer)
       throws IOException;
   }
 
   /**
    * Emit a file using the TextEmitter as a source for the file text.
    * The filenamePattern argument can include '*', for this a number
    * is substituted such that the given filename does not yet exist.
    * The filename used is returned.
    */
   public static String emitFile(String filenamePattern, TextEmitter tw) {
     FileWriter writer = null;
     String filename;
     if (filenamePattern.indexOf("*") >= 0) {
       int index = 1;
       while ((new File((filename = filenamePattern.replaceAll("\\*", String.valueOf(index))))).exists()) {
         index++;
       }
     }
     else {
       filename = filenamePattern;
     }
     try {
       File f = new File(filename);
       f.delete();
       writer = new FileWriter(f);
       tw.emit(writer);
       writer.close();
       writer = null;
     }
     catch (IOException ioe) {
       System.err.println("Cannot write to " + filename);
       if (writer != null) {
         try {
           writer.close();
         }
         catch (IOException ioe2) {
           // ignored.
         }
       }
     }
     return filename;
   }
 
   /**
    * emit a file with the given String text
    */
   public static String emitFile(String filename, final String txt) {
     return emitFile(filename, new TextEmitter() {
         public void emit(Writer writer)
           throws IOException {
           writer.write(txt);
         }
       });
   }
 
   /**
    * emit a file with the given node (to be dumped) as the text.
    */
   public static String emitFile(String filename, final SimpleNode node) {
     return emitFile(filename, new TextEmitter() {
         public void emit(Writer writer)
           throws IOException {
           nodeFileDump(writer, "", node);
         }
       });
   }
 
   /**
    * Helper method to dump a node into a file.
    */
   public static void nodeFileDump(Writer writer, String prefix, SimpleNode node)
     throws IOException
   {
     writer.write(node.toString(prefix) + "\n");
     SimpleNode[] children = node.getChildren();
     if (children != null) {
       for (int i = 0; i < children.length; ++i) {
         SimpleNode n = (SimpleNode)children[i];
         if (n != null) {
           nodeFileDump(writer, prefix + " ", n);
         }
       }
     }
   }
 
 }
 
 /**
  * @copyright Copyright 2001-2008 Laszlo Systems, Inc.  All Rights
  * Reserved.  Use is subject to license terms.
  */
