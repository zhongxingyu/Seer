 package marco.parser;
 
 import marco.MarcoException;
 import org.antlr.v4.runtime.CharStream;
 import org.antlr.v4.runtime.LexerNoViableAltException;
 
 public class Lexer extends marco.parser.antlr.MarcoLexer {
     public Lexer(CharStream input) {
         super(input);
     }
 
     @Override
     public void recover(LexerNoViableAltException e) {
        throw new MarcoException(e);
     }
 }
