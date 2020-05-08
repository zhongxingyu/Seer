 package identifierscanner;
 
 import identifierscanner.dfa.*;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 
 /**
  *
  * @author taylor
  */
 public class Scanner {
     private BufferedReader charReader;
     private DFA dfa;
     private char savedChar;
     private boolean ignoreWhiteSpace;
 
     public Scanner(InputStreamReader charReader) {
         this(charReader, false);
     }
 
     public Scanner(InputStreamReader charReader, boolean ignoreWhiteSpace) {
         savedChar = (char) 0;
         this.ignoreWhiteSpace = ignoreWhiteSpace;
         this.charReader = new BufferedReader(charReader);
         this.dfa = new DFA();
 
         State start = dfa.getStartState();
 
 
         // IDENTIFIERS
         State id1 = new State();
         State idAccept = new State();
         id1.addOtherEdge(idAccept);
         idAccept.setFinalState(true);
         idAccept.setTokenType("IDENTIFIER");
 
         start.addNewEdge("_", id1);
         id1.addNewEdge("_", id1);
        for (char character = 'a'; character <= 'z'; character++) {
             start.addNewEdge(String.valueOf(character), id1);
             id1.addNewEdge(String.valueOf(character), id1);
         }
        for (char character = 'A'; character <= 'Z'; character++) {
             start.addNewEdge(String.valueOf(character), id1);
             id1.addNewEdge(String.valueOf(character), id1);
         }
        for (char character = '0'; character <= '9'; character++) {
             id1.addNewEdge(String.valueOf(character), id1);
         }
 
         // WHITE-SPACE
         State whiteSpace1 = new State();
         State whiteSpaceAccept = new State();
         whiteSpace1.addOtherEdge(whiteSpaceAccept);
         whiteSpaceAccept.setFinalState(true);
         whiteSpaceAccept.setTokenType("WHITESPACE");
 
         start.addNewEdge(" ", whiteSpace1);
         whiteSpace1.addNewEdge(" ", whiteSpace1);
         start.addNewEdge("\t", whiteSpace1);
         whiteSpace1.addNewEdge("\t", whiteSpace1);
         start.addNewEdge("\n", whiteSpace1);
         whiteSpace1.addNewEdge("\n", whiteSpace1);
 
         // OPEN-CURLY
         State openCurly1 = new State();
         State openCurlyAccept = new State();
         openCurly1.addOtherEdge(openCurlyAccept);
         openCurlyAccept.setFinalState(true);
         openCurlyAccept.setTokenType("OPENCURLY");
         start.addNewEdge("{", openCurly1);
 
         // CLOSE-CURLY
         State closedCurly1 = new State();
         State closedCurlyAccept = new State();
         closedCurly1.addOtherEdge(closedCurlyAccept);
         closedCurlyAccept.setFinalState(true);
         closedCurlyAccept.setTokenType("CLOSECURLY");
         start.addNewEdge("}", closedCurly1);
 
         // COMMA
         State comma1 = new State();
         State commaAccept = new State();
         comma1.addOtherEdge(commaAccept);
         commaAccept.setFinalState(true);
         commaAccept.setTokenType("COMMA");
         start.addNewEdge(",", comma1);
 
         // SEMI-COLON
         State semi1 = new State();
         State semiAccept = new State();
         semi1.addOtherEdge(semiAccept);
         semiAccept.setFinalState(true);
         semiAccept.setTokenType("SEMICOLON");
         start.addNewEdge(";", semi1);
 
         // MYSTERY STATES
         State mysteryState1 = new State();
         State mysteryState2 = new State();
         mysteryState2.setFinalState(true);
         mysteryState2.setTokenType("?");
         start.addOtherEdge(mysteryState1);
         mysteryState1.addOtherEdge(mysteryState2);
     }
 
     public boolean hasNextToken() {
         try {
             return charReader.ready();
         } catch (IOException ex) {
             System.out.println("Error: " + ex.getMessage());
             return false;
         }
     }
 
     public Token nextToken() throws IOException {
         State currState = dfa.getStartState();
         StringBuilder tokenValue = new StringBuilder();
         char currentChar = (char) 0;
 
         while (!currState.isFinalState()) {
             if (savedChar != 0) {
                 currentChar = savedChar;
                 savedChar = (char) 0;
             } else {
                 currentChar = (char) charReader.read();
             }
             tokenValue.append(currentChar);
             Edge travelEdge = currState.getEdge(String.valueOf(currentChar));
             currState = travelEdge.getToState();
         }
 
         if (currentChar != 0) {
             savedChar = currentChar;
             tokenValue.deleteCharAt(tokenValue.length() - 1);
         }
 
         String tokenType = currState.getTokenType();
         Token token = new Token(tokenValue.toString(), tokenType);
 
         if (tokenType.equals("IDENTIFIER")) {
             token = checkReservedWords(token);
         }
 
         if (tokenType.equals("WHITESPACE") && ignoreWhiteSpace) {
             return this.nextToken();
         }
 
         return token;
 
     }
 
     private Token checkReservedWords(Token token) {
         HashMap<String, Token> reservedWords = new HashMap<String, Token>();
 
         reservedWords.put("auto", new Token("auto", "AUTO"));
         reservedWords.put("break", new Token("break", "BREAK"));
         reservedWords.put("case", new Token("case", "CASE"));
         reservedWords.put("char", new Token("char", "TYPE"));
         reservedWords.put("const", new Token("const", "CONST"));
         reservedWords.put("continue", new Token("continue", "CONTINUE"));
         reservedWords.put("default", new Token("default", "DEFAULT"));
         reservedWords.put("do", new Token("do", "DO"));
         reservedWords.put("double", new Token("double", "TYPE"));
         reservedWords.put("else", new Token("else", "ELSE"));
         reservedWords.put("entry", new Token("entry", "ENTRY"));
         reservedWords.put("enum", new Token("enum", "ENUM"));
         reservedWords.put("extern", new Token("extern", "EXTERN"));
         reservedWords.put("float", new Token("float", "FLOAT"));
         reservedWords.put("for", new Token("for", "FOR"));
         reservedWords.put("goto", new Token("goto", "GOTO"));
         reservedWords.put("if", new Token("if", "IF"));
         reservedWords.put("int", new Token("int", "TYPE"));
         reservedWords.put("long", new Token("long", "TYPE"));
         reservedWords.put("register", new Token("register", "REGISTER"));
         reservedWords.put("return", new Token("return", "RETURN"));
         reservedWords.put("short", new Token("short", "TYPE"));
         reservedWords.put("signed", new Token("signed", "SIGNED"));
         reservedWords.put("sizeof", new Token("sizeof", "SIZEOF"));
         reservedWords.put("static", new Token("static", "STATIC"));
         reservedWords.put("struct", new Token("struct", "STRUCT"));
         reservedWords.put("switch", new Token("switch", "SWITCH"));
         reservedWords.put("typedef", new Token("typedef", "TYPEDEF"));
         reservedWords.put("union", new Token("union", "UNION"));
         reservedWords.put("unsigned", new Token("unsigned", "UNSIGNED"));
         reservedWords.put("void", new Token("void", "TYPE"));
         reservedWords.put("volatile", new Token("volatile", "VOLATILE"));
         reservedWords.put("while", new Token("while", "WHILE"));
 
         reservedWords.put("inline", new Token("inline", "ISO_INLINE"));
         reservedWords.put("restrict", new Token("restrict", "ISO_RESTRICT"));
         reservedWords.put("_Bool", new Token("_Bool", "ISO_BOOL"));
         reservedWords.put("_Complex", new Token("_Complex", "ISO_COMPLEX"));
         reservedWords.put("_Imaginary", new Token("_Imaginary", "ISO_IMAGINARY"));
 
         reservedWords.put("__FUNCTION__", new Token("__FUNCTION__", "GNU_FUNCTION"));
         reservedWords.put("__PRETTY_FUNCTION__", new Token("__PRETTY_FUNCTION__", "GNU_FUNCTION"));
         reservedWords.put("__alignof", new Token("__alignof", "GNU_ALIGNOF"));
         reservedWords.put("__alignof__", new Token("__alignof", "GNU_ALIGNOF"));
         reservedWords.put("__asm", new Token("__asm", "GNU_ASM"));
         reservedWords.put("__asm__", new Token("__asm__", "GNU_ASM"));
         reservedWords.put("__attribute", new Token("__attribute", "GNU_ATTRIBUTE"));
         reservedWords.put("__attribute__", new Token("__attribute__", "GNU_ATTRIBUTE"));
         reservedWords.put("__builtin_offsetof", new Token("__builtin_offsetof", "GNU_BUILTINOFFSET"));
         reservedWords.put("__builtin_va_arg", new Token("__builtin_va_arg", "GNU_BUILTINVARG"));
         reservedWords.put("__complex", new Token("__complex", "GNU_COMPLEX"));
         reservedWords.put("__complex__", new Token("__complex__", "GNU_COMPLEX"));
         reservedWords.put("__const", new Token("__const", "GNU_CONST"));
         reservedWords.put("__extension__", new Token("__extension__", "GNU_EXTENSION"));
         reservedWords.put("__func__", new Token("__func__", "GNU_FUNCTION"));
         reservedWords.put("__imag", new Token("__imag", "GNU_IMAG"));
         reservedWords.put("__imag__", new Token("__imag__", "GNU_IMAG"));
         reservedWords.put("__inline", new Token("__inline", "GNU_INLINE"));
         reservedWords.put("__inline__", new Token("__inline__", "GNU_INLINE"));
         reservedWords.put("__label__", new Token("__label__", "GNU_LABEL"));
         reservedWords.put("__null", new Token("__null", "GNU_NULL"));
         reservedWords.put("__real", new Token("__real", "GNU_REAL"));
         reservedWords.put("__real__", new Token("__real__", "GNU_REAL"));
         reservedWords.put("__restrict", new Token("__restrict", "GNU_RESTRICT"));
         reservedWords.put("__restrict__", new Token("__restrict__", "GNU_RESTRICT"));
         reservedWords.put("__signed", new Token("__signed", "GNU_SIGNED"));
         reservedWords.put("__signed__", new Token("__signed__", "GNU_SIGNED"));
         reservedWords.put("__thread", new Token("__thread", "GNU_THREAD"));
         reservedWords.put("__typeof", new Token("__typeof", "GNU_TYPEOF"));
         reservedWords.put("__volatile", new Token("__volatile", "GNU_VOLATILE"));
         reservedWords.put("__volatile__", new Token("__volatile__", "GNU_VOLATILE"));
 
         if (reservedWords.containsKey(token.getValue())) {
             return reservedWords.get(token.getValue());
         }
         return token;
     }
 }
