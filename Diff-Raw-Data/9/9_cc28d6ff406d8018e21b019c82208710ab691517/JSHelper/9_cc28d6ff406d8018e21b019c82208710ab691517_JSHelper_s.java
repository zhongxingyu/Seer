 package com.intalio.simpel.util;
 
 import org.antlr.runtime.CharStream;
 import org.antlr.runtime.RecognitionException;
 import org.antlr.runtime.TokenSource;
 import com.intalio.simpel.antlr.SimPELLexer;
 import uk.co.badgersinfoil.e4x.antlr.LinkedListTokenStream;
 import uk.co.badgersinfoil.e4x.antlr.LinkedListTree;
 import uk.co.badgersinfoil.e4x.antlr.LinkedListToken;
 import uk.co.badgersinfoil.e4x.antlr.LinkedListTokenSource;
 
 /**
  * @author Matthieu Riou <mriou@apache.org>
  */
 public class JSHelper {
 
     public static LinkedListTree parseJSLiteral(TokenSource lexer, CharStream cs, LinkedListTokenStream stream)
             throws RecognitionException {
         String tail = cs.substring(cs.index(), cs.size()-1);
         int closingIndex = findClosingBracket(tail);
         // Skipping
         cs.seek(cs.index() + closingIndex);
         LinkedListTokenSource source = (LinkedListTokenSource)stream.getTokenSource();
         stream.setTokenSource(source);  // cause any remembered E4X state to be dropped
         stream.scrub(1); // erase the subsequent token that the E4X parser got from this stream
         source.setDelegate(lexer);
 
         LinkedListToken current = (LinkedListToken)stream.get(stream.size());
        LinkedListToken tok = new LinkedListToken(SimPELLexer.T80, tail.substring(0, closingIndex));
         current.setNext(tok);
         tok.setPrev(current);
         return new LinkedListTree(tok);
     }
 
     private static int findClosingBracket(String tail) {
         int count = 1;
         int pos = 0;
         char[] chars = tail.toCharArray();
         for (; pos < chars.length; pos++) {
             char ch = chars[pos];
             if (ch == '}') count--;
             if (ch == '{') count++;
             if (count == 0) break;
         }
         return pos;
     }
 
 }
