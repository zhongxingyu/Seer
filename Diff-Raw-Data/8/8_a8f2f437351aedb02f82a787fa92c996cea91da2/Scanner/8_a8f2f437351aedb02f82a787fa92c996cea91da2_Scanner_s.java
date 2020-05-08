 package compiler;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import compiler.fsa.AlphaFSA;
 import compiler.fsa.CommentFSA;
 import compiler.fsa.FSA;
 import compiler.fsa.LiteralFSA;
 import compiler.fsa.NumeralFSA;
 import compiler.fsa.SymbolFSA;
 import compiler.fsa.WhitespaceFSA;
 import compiler.io.Line;
 import compiler.io.MPFile;
 
 public class Scanner {
     private MPFile file;
 
     /**
      * Opens the micropascal file
      * 
      * @param filename
      */
     public void openFile(String filename) {
         try {
             file = new MPFile();
             BufferedReader br = new BufferedReader(new FileReader(filename));
             String line = "";
             while ((line = br.readLine()) != null) {
                 file.addLine(new Line(line));
             }
             // file.addLine(new Line(""));
             if (file.numberOfLines() == 0) {
                 System.out.println("Empty File. Not a valid program.");
                 System.exit(1);
             }
 
             file.print(true);
 
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     /**
      * Dispatcher to FSA implementation
      * 
      * @return ArrayList consisting of all tokens found in the file, in order they were found.
      */
     public ArrayList<Token> getTokens() {
         ArrayList<Token> tokens = new ArrayList<Token>();
 
         Token currentToken = null;
         do {
             currentToken = getNextToken();
 
             if (currentToken.getType() == TokenType.MP_RUN_COMMENT) {
                System.err.println("MP_RUN_COMMENT: Run away comment found starting at line "
                         + currentToken.getLineNumber() + " column "
                         + currentToken.getColumnNumber());
                 file.getLineAt(currentToken.getLineNumber()).printLineWithCaret(currentToken.getLineNumber(),
                         currentToken.getColumnNumber());
                 tokens.add(currentToken);
 
             } else if (currentToken.getType() == TokenType.MP_ERROR) {
                System.err.println("MP_ERROR: Found invalid character: " + currentToken.getLexeme()
                         + ". Invalid character found at line "
                         + currentToken.getLineNumber() + " column " + currentToken.getColumnNumber());
                 file.getLineAt(currentToken.getLineNumber()).printLineWithCaret(currentToken.getLineNumber(),
                         currentToken.getColumnNumber());
                 tokens.add(currentToken);
             } else if (currentToken.getType() == TokenType.MP_RUN_STRING) {
                System.err
                 .println("MP_RUN_STRING: String literal is not properly closed by a single-quote: "
                         + currentToken.getLexeme() + ". String found starting at line "
                         + currentToken.getLineNumber() + " column " + currentToken.getColumnNumber());
                 file.getLineAt(currentToken.getLineNumber()).printLineWithCaret(currentToken.getLineNumber(),
                         currentToken.getColumnNumber());
                 tokens.add(currentToken);
             } else if (currentToken.getType() != TokenType.MP_WHITESPACE
                     && currentToken.getType() != TokenType.MP_COMMENT) {
                 tokens.add(currentToken);
             }
 
         } while (currentToken.getType() != TokenType.MP_EOF);
 
         return tokens;
     }
 
     /**
      * Finds the next token in the file
      * 
      * @return the next token
      */
     private Token getNextToken() {
         Token foundToken = null;
 
         if (file.isOnLastLine() && !file.hasNextChar()) {
             foundToken = new Token(TokenType.MP_EOF, "EOF",
                     file.getLineIndex(), file.getColumnIndex());
         } else {
             char nextChar = file.getCurrentCharacter();
 
             FSA tokenFinder = null;
 
             switch (nextChar) {
             case 'a':
             case 'b':
             case 'c':
             case 'd':
             case 'e':
             case 'f':
             case 'g':
             case 'h':
             case 'i':
             case 'j':
             case 'k':
             case 'l':
             case 'm':
             case 'n':
             case 'o':
             case 'p':
             case 'q':
             case 'r':
             case 's':
             case 't':
             case 'u':
             case 'v':
             case 'w':
             case 'x':
             case 'y':
             case 'z':
             case 'A':
             case 'B':
             case 'C':
             case 'D':
             case 'E':
             case 'F':
             case 'G':
             case 'H':
             case 'I':
             case 'J':
             case 'K':
             case 'L':
             case 'M':
             case 'N':
             case 'O':
             case 'P':
             case 'Q':
             case 'R':
             case 'S':
             case 'T':
             case 'U':
             case 'V':
             case 'W':
             case 'X':
             case 'Y':
             case 'Z':
             case '_':
                 tokenFinder = new AlphaFSA();
                 break;
             case '0':
             case '1':
             case '2':
             case '3':
             case '4':
             case '5':
             case '6':
             case '7':
             case '8':
             case '9':
                 tokenFinder = new NumeralFSA();
                 break;
             case '\'':
                 tokenFinder = new LiteralFSA();
                 break;
             case '.':
             case ',':
             case ';':
             case '(':
             case ')':
             case '=':
             case '>':
             case '<':
             case '+':
             case '-':
             case '*':
             case ':':
                 tokenFinder = new SymbolFSA();
                 break;
             case ' ':
             case '\t':
                 tokenFinder = new WhitespaceFSA();
                 break;
             case '{':
                 tokenFinder = new CommentFSA();
                 break;
             default:
                 tokenFinder = null;
                 break;
             }
 
             if (tokenFinder != null) {
                 //                System.out.println("looking for token with : "
                 //                        + tokenFinder.getClass().getName());
                 foundToken = tokenFinder.getToken(file);
 
             } else {
                 nextChar = file.getNextCharacter(); //consume the error char
                 foundToken = new Token(TokenType.MP_ERROR, "" + nextChar, file.getLineIndex(),
                         file.getColumnIndex() - 1);
             }
 
             while (file.hasNextLine() && !file.hasNextChar()) {
                 file.increaseLineIndex();
             }
         }
         return foundToken;
     }
 }
