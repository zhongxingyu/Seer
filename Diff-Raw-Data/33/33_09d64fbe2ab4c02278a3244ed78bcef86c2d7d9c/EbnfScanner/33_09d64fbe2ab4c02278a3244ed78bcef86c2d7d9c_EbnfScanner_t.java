 /*
  * LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 42):
  * "Sven Strittmatter" <ich(at)weltraumschaf(dot)de> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a beer in return.
  *
  */
 
 package de.weltraumschaf.jebnf.parser;
 
 import com.google.common.collect.Lists;
 import static de.weltraumschaf.jebnf.parser.CharacterHelper.*;
 import java.io.IOException;
 import java.io.Reader;
 import java.util.List;
 
 /**
  * Scans an input string for EBNF syntax tokens.
  *
  * This class implements a standard lexical scanner pattern with one
  * character lookahead and iterator interface for receiving token by token.
  * On lexical syntax errors a SyntaxException will be thrown.
  *
  * Example:
  * <code>
  * BufferedReader grammar = new BufferedReader(new StringReader("...")); // the EBNF grammar
  *
  * try {
  *     Scanner scanner = new Scanner(grammar);
  *
  *     while (scanner->hasNextToken()) {
  *         scanner->nextToken();
  *         Token token = scanner->currentToken();
  *         doSomething(token);
  *     }
  * } catch (SyntaxtError ex) {
  *      logError(ex);
  * }
  * </code>
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public class EbnfScanner implements Scanner {
 
     /**
      * The input stream to read from.
      */
     private final Reader input;
 
     /**
      * The file name associated with the input.
      *
      * This piece of information is only used to generated better error messages.
      * This value is passed to all {@link Position} objects created during scanning.
      *
      * This may be null, if a StringReader buffer is supplied!
      */
     private final String file;
 
     /**
      * Holds all scanned tokens.
      */
     private final List<Token> tokens = Lists.newArrayList();
 
     /**
      * Holds all read characters.
      */
     private final StringBuilder buffer = new StringBuilder();
 
     /**
      * Current scanner position in buffer string.
      */
     private int currentCharacter;
 
     /**
      * Index of current token in token list.
      */
     private int currentToken;
 
     /**
      * Current column in source.
      */
     private int column;
 
     /**
      * Current line in source.
      */
     private int line;
 
     /**
      * True if end of file reached.
      */
     private boolean atEof;
 
     /**
      * Use this constructor if your input buffer has no file name associated.
      *
      * @param inputStream The input stream to scan.
      */
     public EbnfScanner(final Reader inputStream) {
         this(inputStream, null);
     }
 
     /**
      * Dedicated constructor.
      *
      * @param inputStream The input stream to scan.
      * @param fileName    The file name associated with the scanned source.
      */
     public EbnfScanner(final Reader inputStream, final String fileName) {
         super();
         this.input            = inputStream;
         this.file             = fileName;
         this.currentCharacter = -1;
         this.currentToken     = -1;
         this.column           = 0;
         this.line             = 1;
     }
 
     @Override
     public final String getFile() {
         return file;
     }
 
     @Override
     public boolean hasNextCharacter() {
         return !atEof;
     }
 
     @Override
     public void nextCharacter() throws IOException {
         if (currentCharacter > -1 && EOF == getCurrentCharacter()) {
             return;
         }
 
         if (currentCharacter + 1 >= buffer.length()) {
             final int chr = input.read();
 
             if (-1 == chr) {
                 atEof = true;
             } else {
                 buffer.append((char) chr);
             }
         }
 
         currentCharacter++;
         column++;
     }
 
     @Override
     public char getCurrentCharacter() {
         try {
             return buffer.charAt(currentCharacter);
         } catch (StringIndexOutOfBoundsException ex) {
             if (atEof) {
                 return EOF;
             } else {
                 throw new IllegalStateException("Invoke Scanner#nextCharacter() first!", ex);
             }
         }
     }
 
     @Override
     public void backupCharacter() {
         currentCharacter--;
         column--;
     }
 
     @Override
     public char peekCharacter() throws IOException {
         char character = EOF;
 
         if (hasNextCharacter()) {
             nextCharacter();
             character = getCurrentCharacter();
             backupCharacter();
         }
 
         return character;
     }
 
     @Override
     public void raiseError(final String msg) throws SyntaxException {
         throw new SyntaxException(msg, createPosition());
     }
 
     @Override
     public Position createPosition() {
         return new Position(line, column, file);
     }
 
     @Override
     public Token getCurrentToken() {
         try {
             return tokens.get(currentToken);
         } catch (IndexOutOfBoundsException ex) {
             return null;
         }
     }
 
     @Override
     public void backtrackToken() {
         backtrackToken(1);
     }
 
     @Override
     public void backtrackToken(final int count) {
         final int backtrackedIndex = currentToken - count;
 
         if (backtrackedIndex < 0) {
             throw new IllegalArgumentException(
                 String.format("Can't backup current token to index %d! There are only %d tokens.",
                               backtrackedIndex, tokens.size()));
         } else {
             currentToken = backtrackedIndex;
         }
     }
 
     @Override
     public boolean hasNextToken() {
         final Token token = getCurrentToken();
 
         return null == token
                ? true
                : !token.isType(TokenType.EOF);
     }
 
     @Override
     public Token peekToken() throws SyntaxException, IOException {
         nextToken();
         final Token token = getCurrentToken();
         currentToken--;
 
         if (currentToken < 0) {
             currentToken = 0;
         }
 
         return token;
     }
 
     @Override
     public void nextToken() throws SyntaxException, IOException {
         if (!hasNextToken()) {
             return;
         }
 
         if (currentToken > -1 && currentToken < (tokens.size() - 1)) {
             // recover backtracked tokens.
             currentToken++;
             return;
         }
 
         processToken();
     }
 
     /**
      * Processes a token lexically.
      *
      * This method performs the lexical analysis of input characters to recognize EBNF tokens.
      *
      * @throws SyntaxException On syntax errors.
      * @throws IOException     On input stream IO errors.
      */
     private void processToken() throws SyntaxException, IOException {
         while (hasNextCharacter()) {
             nextCharacter();
 
             if (isAlpha(getCurrentCharacter())) {
                 tokens.add(EbnfScannerHelper.scanIdentifier(this));
                 currentToken++;
                 return;
             } else if (isQuote(getCurrentCharacter())) {
                 tokens.add(EbnfScannerHelper.scanLiteral(this));
                 currentToken++;
                 return;
             } else if (isOperator(getCurrentCharacter())) {
                 if ('(' == getCurrentCharacter() && '*' == peekCharacter()) {
                     tokens.add(EbnfScannerHelper.scanComment(this));
                 } else {
                     tokens.add(EbnfScannerHelper.scanOperator(this));
                 }
                 currentToken++;
                 return;
             } else if (isWhiteSpace(getCurrentCharacter())) { // NOPMD
                 // Ignore white spaces.
            } else if (EOF == getCurrentCharacter()) {
                // Break out the loop, if there are no more characters.
                 break;
             } else {
                 raiseError(String.format("Invalid character '%s' at %s",
                                         getCurrentCharacter(),
                                         createPosition()));
             }
 
             checkNewline();
         }
 
         tokens.add(new Token(TokenType.EOF, file, createPosition()));
         currentToken++;
     }
 
     @Override
     public void checkNewline() {
         if ('\n' == getCurrentCharacter() || '\r' == getCurrentCharacter()) {
             line++;
             column = 0;
         }
     }
 
     @Override
     public void close() throws IOException {
         input.close();
     }
 
 }
