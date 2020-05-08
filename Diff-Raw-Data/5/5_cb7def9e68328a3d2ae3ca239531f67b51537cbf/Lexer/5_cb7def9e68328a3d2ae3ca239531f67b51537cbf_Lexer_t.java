 package marco.parser;
 
 import marco.MarcoException;
 import org.antlr.v4.runtime.CharStream;
 import org.antlr.v4.runtime.LexerNoViableAltException;
import org.antlr.v4.runtime.misc.Interval;
 
 public class Lexer extends marco.parser.antlr.MarcoLexer {
     public Lexer(CharStream input) {
         super(input);
     }
 
     @Override
     public void recover(LexerNoViableAltException e) {
        String symbol = getInputStream().getText(Interval.of(e.getStartIndex(), e.getStartIndex()));
        throw new MarcoException("Unexpected character '" + symbol + "'.");
     }
 }
