 package de.weltraumschaf.caythe;
 
 import java.util.List;;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import de.weltraumschaf.caythe.util.ParseTreePrinter;
 import de.weltraumschaf.caythe.backend.Backend;
 import de.weltraumschaf.caythe.backend.BackendFactory;
 import de.weltraumschaf.caythe.frontend.FrontendFactory;
 import de.weltraumschaf.caythe.frontend.Parser;
 import de.weltraumschaf.caythe.frontend.Source;
 import de.weltraumschaf.caythe.frontend.TokenType;
 import de.weltraumschaf.caythe.intermediate.Code;
 import de.weltraumschaf.caythe.intermediate.SymbolTableEntry;
 import de.weltraumschaf.caythe.intermediate.SymbolTableStack;
 import de.weltraumschaf.caythe.message.Message;
 import de.weltraumschaf.caythe.message.MessageListener;
 import de.weltraumschaf.caythe.message.MessageType;
 import de.weltraumschaf.caythe.util.CrossReferencer;
 import java.io.BufferedReader;
 import java.io.FileReader;
 
 import static de.weltraumschaf.caythe.frontend.pascal.PascalTokenType.*;
 import static de.weltraumschaf.caythe.intermediate.symboltableimpl.SymbolTableKeyImpl.ROUTINE_INTERMEDIATE_CODE;
 
 /**
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  * @license http://www.weltraumschaf.de/the-beer-ware-license.txt THE BEER-WARE LICENSE
  */
 public class App {
 
     private static final String USAGE = "Usage: java -jar caythe.jar --lang <language> --mode execute|compile [-ixlafcrdh] <source file path>";
 
     private Code intermediateCode;
     private SymbolTableStack symbolTableStack;
     private Backend backend;
 
     private Options opts;
 
     /**
      * @todo
      * - maybe -n for line numbers and -l for language
      * - maybe -m for mode "compile" or "execute"
      *
      * @param args
      */
     public App(String[] args) throws Error {
         if (args.length == 0) {
             throw new Error("Too few arguments!\n" + USAGE, 1);
         }
 
         this.opts = new Options(Options.createParser().parse(args));
     }
 
     public static void main(String[] args) {
         try {
             App app = new App(args);
             System.exit(app.run());
         }
         catch (Error ex) {
             System.out.println(ex.getMessage());
             System.exit(ex.getCode());
         }
 
     }
 
     private static String formatError(Throwable t) {
         return formatError(t, false);
     }
 
     private static String formatError(Throwable t, boolean withMessage) {
         StringBuilder sb = new StringBuilder("DEBUG:\n");
         sb.append("Exception thrown");
 
         if (withMessage && null != t.getMessage()) {
            sb.append("wit message: ")
               .append(t.getMessage());
         }
 
         sb.append("!\n")
           .append("Stack trace:\n");
         StringWriter sw = new StringWriter();
         PrintWriter pw  = new PrintWriter(sw);
         t.printStackTrace(pw);
         sb.append(sw.toString());
         return sb.toString();
     }
 
     private static void help() {
         StringBuilder sb = new StringBuilder(USAGE);
         sb.append("\n\n");
         sb.append("  --compile      Compile to java bytecode.\n");
         sb.append("  --execute      Interpret the code.\n");
         sb.append("\n\n");
         sb.append("  --lang <language>  Language to parse: pascal, caythe.\n");
         sb.append("  -i                 Intermediate code tree.\n");
         sb.append("  -x                 Show variable cross reference\n");
         sb.append("  -d                 Show debug output.\n");
         sb.append("  -l                 ....\n");
         sb.append("  -a                 ....\n");
         sb.append("  -f                 ....\n");
         sb.append("  -c                 ....\n");
         sb.append("  -r                 Show this help.\n");
         System.out.println(sb);
     }
 
     private int run() {
         int exitCode = 0;
 
         if (opts.isHelpEnabled()) {
             help();
             return exitCode;
         }
 
         try {
             execute();
         } catch (Error err) {
             if (null != err.getMessage()) {
                 System.out.println(err.getMessage());
                 System.out.println();
             }
 
             if (opts.isDebugEnabled()) {
                 System.out.println(formatError(err));
             }
 
             exitCode = err.getCode();
         } catch (Exception ex) {
            System.out.println(USAGE);
             System.out.println();
 
             if (opts.isDebugEnabled()) {
                 System.out.println(formatError(ex, true));
             }
 
             exitCode = -1;
         }
 
         return exitCode;
     }
 
     public void execute() throws Error, Exception {
         BackendFactory.Operation operation = null;
 
         if ("execute".equalsIgnoreCase(opts.getMode())) {
             operation = BackendFactory.Operation.EXECUTE;
         } else if ("compile".equalsIgnoreCase(opts.getMode())) {
             operation = BackendFactory.Operation.COMPILE;
         } else {
             throw new Error("Specify either --compile or --execute!", 2);
         }
 
         List<String> noOpArgs = opts.nonOptionArguments();
 
         if (noOpArgs.size() != 1) {
             throw new Error("Specify one source file to process!", 3);
         }
 
         String filePath = noOpArgs.get(0);
         Source source   = new Source(new BufferedReader(new FileReader(filePath)));
         source.addMessageListener(new SourceMessageListener());
 
         Parser parser = FrontendFactory.createParser(
             FrontendFactory.Language.PASCAL,
             FrontendFactory.Type.TOP_DOWN,
             source
         );
         parser.addMessageListener(new ParserMessageListener());
 
         backend = BackendFactory.createBackend(operation);
         backend.addMessageListener(new BackendMessageListener());
 
         parser.parse();
         source.close();
 
         if (parser.getErrorCount() == 0) {
             symbolTableStack = parser.getSymbolTableStack();
             SymbolTableEntry programId = symbolTableStack.getProgramId();
             intermediateCode = (Code) programId.getAttribute(ROUTINE_INTERMEDIATE_CODE);
 
             if (opts.isCrossRefernecesEnabled()) {
                 CrossReferencer crossReferencer = new CrossReferencer(System.out);
                 crossReferencer.print(symbolTableStack);
             }
 
             if (opts.isIntermediateCodeEnabled()) {
                 ParseTreePrinter treePrinter = new ParseTreePrinter(System.out);
                 treePrinter.print(symbolTableStack);
             }
         }
 
         backend.process(intermediateCode, symbolTableStack);
     }
 
     private static final String SOURCE_LINE_FORMAT = "%03d %s";
 
     private class SourceMessageListener implements MessageListener {
         @Override
         public void messageReceived(Message message) {
             MessageType type = message.getType();
             Object body[]    = (Object[])message.getBody();
 
             switch (type) {
                 case SOURCE_LINE: {
                     int lineNumber  = (Integer) body[0];
                     String lineText = (String) body[1];
 
                     System.out.println(String.format(SOURCE_LINE_FORMAT, lineNumber, lineText));
                     break;
                 }
             }
         }
     }
 
     private static final String PARSER_SUMMARY_FORMAT = "\n%,20d source lines." +
                                                         "\n%,20d syntax errors." +
                                                         "\n%,20.2f seconds total parsing time.\n";
     private static final String TOKEN_FORMAT = ">>> %-15s line=%03d, pos=%2d, text=\"%s\"";
     private static final String VALUE_FORMAT = ">>>                 value=%s";
     private static final int PREFIX_WIDTH = 5;
 
     private class ParserMessageListener implements MessageListener {
         @Override
         public void messageReceived(Message message) {
             MessageType type = message.getType();
 
             switch (type) {
                 case TOKEN: {
                     Object body[]       = (Object[]) message.getBody();
                     int line            = (Integer) body[0];
                     int position        = (Integer) body[1];
                     TokenType tokenType = (TokenType) body[2];
                     String tokenText    = (String) body[3];
                     Object tokenValue   = body[4];
 
                     System.out.println(String.format(TOKEN_FORMAT,
 						     tokenType,
 						     line,
 						     position,
 						     tokenText));
 
                     if (null != tokenValue) {
                         if (STRING == tokenType) {
                             tokenValue = "\"" + tokenValue + "\"";
                         }
 
                         System.out.println(String.format(VALUE_FORMAT,
 							 tokenValue));
                     }
 
                     break;
                 }
                 case SYNTAX_ERROR: {
                     Object body[]       = (Object[]) message.getBody();
                     int lineNumber      = (Integer) body[0];
                     int position        = (Integer) body[1];
                     String tokenText    = (String) body[2];
                     String errorMessage = (String) body[3];
 
                     int spaceCount = PREFIX_WIDTH + position;
                     StringBuilder flagBuffer = new StringBuilder();
 
                     // Spaces up to the error position
                     for (int i =1; i < spaceCount; ++i) {
                         flagBuffer.append(' ');
                     }
 
                     flagBuffer.append("^\n*** ")
                               .append(errorMessage);
 
                     if (null != tokenText) {
                         flagBuffer.append(" [at\"")
                                   .append(tokenText)
                                   .append("\"]");
                     }
 
                     System.out.println(flagBuffer.toString());
                     break;
                 }
                 case PARSER_SUMMARY: {
                     Number body[]      = (Number[]) message.getBody();
                     int statementCount = (Integer) body[0];
                     int syntaxErrors   = (Integer) body[1];
                     float elapsedTime  = (Float)   body[2];
 
                     System.out.println(
                         String.format(PARSER_SUMMARY_FORMAT, statementCount, syntaxErrors, elapsedTime)
                     );
                     break;
                 }
             }
         }
     }
 
     private static final String INTERPRETER_SUMMARY_FORMAT = "\n%,20d statements executed." +
                                                              "\n%,20d runtime errors." +
                                                              "\n%,20.2f seconds total execution time.\n";
     private static final String COMPILER_SUMMARY_FORMAT = "\n%,20d instructions generated." +
                                                           "\n%,20.2f seconds total code generation time.\n";
     private static final String LINE_FORMAT   = ">>> AT LINE %03d\n";
     private static final String ASSIGN_FORMAT = ">>> AT LINE %03d: %s = %s\n";
 
     private static final String FETCH_FORMAT  = ">>> AT LINE %03d: %s : %s\n";
     private static final String CALL_FORMAT   = ">>> AT LINE %03d: CALL %s\n";
     private static final String RETUR_FORMAT  = ">>> AT LINE %03d: RETURN FROM %s\n";
 
     private class BackendMessageListener implements MessageListener {
         private boolean firstOutputMessage = true;
 
         @Override
         public void messageReceived(Message message) {
             MessageType type = message.getType();
 
             switch (type) {
                 case SOURCE_LINE: {
                     if (opts.isLineNumbersEnabled()) {
                         int lineNumber = (Integer) message.getBody();
                         System.out.printf(LINE_FORMAT, lineNumber);
                     }
 
                     break;
                 }
 
                 case ASSIGN: {
                     if (opts.isVarAssignsEnabled()) {
                         Object body[] = (Object[]) message.getBody();
                         int lineNumber = (Integer) body[0];
                         String variableName = (String) body[1];
                         Object value = body[2];
                         System.out.printf(ASSIGN_FORMAT, lineNumber, variableName, value);
                     }
 
                     break;
                 }
 
                 case FETCH: {
                     if (opts.isVarFetchesEnabled()) {
                         Object body[] = (Object[]) message.getBody();
                         int lineNumber      = (Integer) body[0];
                         String variableName = (String) body[1];
                         Object value        = body[2];
                         System.out.printf(FETCH_FORMAT, lineNumber, variableName, value);
                     }
 
                     break;
                 }
 
                 case CALL: {
                     if (opts.isFunctionCallsEnabled()) {
                         Object body[] = (Object[]) message.getBody();
                         int lineNumber = (Integer) body[0];
                         String routineName = (String) body[1];
                         System.out.printf(CALL_FORMAT, lineNumber, routineName);
                     }
 
                     break;
 
                 }
 
                 case RETURN: {
                     if (opts.isFunctionReturnsEnabled()) {
                         Object body[] = (Object[]) message.getBody();
                         int lineNumber = (Integer) body[0];
                         String routineName = (String) body[1];
                         System.out.printf(RETUR_FORMAT, lineNumber, routineName);
                     }
 
                     break;
                 }
 
                 case RUNTIME_ERROR: {
                     Object body[] = (Object []) message.getBody();
                     String errorMessage = (String) body[0];
                     Integer lineNumber = (Integer) body[1];
 
                     System.out.print("*** RUNTIME ERROR");
                     if (lineNumber != null) {
                         System.out.print(" AT LINE " +
                                          String.format("%03d", lineNumber));
                     }
                     System.out.println(": " + errorMessage);
                     break;
                 }
 
                 case INTERPRETER_SUMMARY: {
                     Number body[]      = (Number[]) message.getBody();
                     int executionCount = (Integer) body[0];
                     int runtimeErrors  = (Integer) body[1];
                     float elapsedTime  = (Float) body[2];
 
                     System.out.println(
                         String.format(INTERPRETER_SUMMARY_FORMAT, executionCount, runtimeErrors, elapsedTime)
                     );
                     break;
                 }
 
                 case COMPILER_SUMMARY: {
                     Number body[]        = (Number[]) message.getBody();
                     int instructionCount = (Integer) body[0];
                     float elapsedTime    = (Float) body[1];
 
                     System.out.println(
                         String.format(COMPILER_SUMMARY_FORMAT, instructionCount, elapsedTime)
                     );
                     break;
                 }
             }
         }
     }
 
 }
