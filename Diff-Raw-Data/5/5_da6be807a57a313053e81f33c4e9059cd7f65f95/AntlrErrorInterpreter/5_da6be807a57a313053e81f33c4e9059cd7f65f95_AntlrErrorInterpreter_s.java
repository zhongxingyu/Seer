 package novelang.parser.antlr;
 
 import novelang.parser.unicode.UnicodeNames;
 
 import org.antlr.runtime.EarlyExitException ;
 import org.antlr.runtime.FailedPredicateException ;
 import org.antlr.runtime.MismatchedNotSetException ;
 import org.antlr.runtime.MismatchedSetException ;
 import org.antlr.runtime.MismatchedTokenException ;
 import org.antlr.runtime.MismatchedTreeNodeException ;
 import org.antlr.runtime.MissingTokenException ;
 import org.antlr.runtime.NoViableAltException ;
 import org.antlr.runtime.RecognitionException ;
 import org.antlr.runtime.Token ;
 import org.antlr.runtime.UnwantedTokenException ;
 
 /**
  * Copied from {@link org.antlr.runtime.BaseRecognizer}
  *
  * @author Laurent Caillette
  */
 public class AntlrErrorInterpreter {
 
 
   public static String getErrorMessage( final RecognitionException e, final String[] tokenNames ) {
     String msg = e.getMessage() ;
     
     if ( e instanceof UnwantedTokenException ) {
       final UnwantedTokenException ute = ( UnwantedTokenException ) e ;
       String tokenName = "<unknown>" ;
       if ( ute.expecting == Token.EOF ) {
         tokenName = "EOF" ;
       } else {
         tokenName = tokenNames[ ute.expecting ] ;
       }
       msg = "extraneous input " + getTokenErrorDisplay( ute.getUnexpectedToken() ) +
           " expecting " + tokenName ;
       
     } else if ( e instanceof MissingTokenException ) {
       
       final MissingTokenException mte = ( MissingTokenException ) e ;
       String tokenName = "<unknown>" ;
       if ( mte.expecting == Token.EOF ) {
         tokenName = "EOF" ;
       } else {
         tokenName = tokenNames[ mte.expecting ] ;
       }
       msg = "missing " + tokenName + " at " + getTokenErrorDisplay( e.token ) ;
       
     } else if ( e instanceof MismatchedTokenException ) {
       
       final MismatchedTokenException mte = ( MismatchedTokenException ) e ;
       String tokenName = "<unknown>" ;
       if ( mte.expecting == Token.EOF ) {
         tokenName = "EOF" ;
       } else {
         tokenName = tokenNames[ mte.expecting ] ;
       }
       msg = "mismatched input " + getTokenErrorDisplay( e.token ) +
           " expecting " + tokenName ;
       
     } else if ( e instanceof MismatchedTreeNodeException ) {
       
       final MismatchedTreeNodeException mtne = ( MismatchedTreeNodeException ) e ;
       String tokenName = "<unknown>" ;
       if ( mtne.expecting == Token.EOF ) {
         tokenName = "EOF" ;
       } else {
         tokenName = tokenNames[ mtne.expecting ] ;
       }
       msg = "mismatched tree node: " + mtne.node +
           " expecting " + tokenName ;
       
     } else if ( e instanceof NoViableAltException ) {
       
       // NoViableAltException nvae = (NoViableAltException)e ;
       // for development, can add "decision=<<"+nvae.grammarDecisionDescription+">>"
       // and "(decision="+nvae.decisionNumber+") and
       // "state "+nvae.stateNumber
       final NoViableAltException noViableAltException = ( NoViableAltException ) e ;
       final Token token = noViableAltException.token ;
       final char charAtError ;
       if( token != null && token.getText() != null && token.getText().length() == 1 ) {
         charAtError = token.getText().charAt( 0 ) ;
       } else {
         charAtError = ( ( char ) noViableAltException.c ) ;
       }
       msg = "No viable alternative at input '" + charAtError + "' " +
           UnicodeNames.getDecoratedName( charAtError ) ;
 //      msg = "No viable alternative at input " + getTokenErrorDisplay( e.token ) ;
       
     } else if ( e instanceof EarlyExitException ) {
       
       //EarlyExitException eee = (EarlyExitException)e ;
       // for development, can add "(decision="+eee.decisionNumber+")"
       msg = "required (...)+ loop did not match anything at input " +
           getTokenErrorDisplay( e.token ) ;
       
     } else if ( e instanceof MismatchedSetException ) {
       
       final MismatchedSetException mse = ( MismatchedSetException ) e ;
       msg = "mismatched input " + getTokenErrorDisplay( e.token ) +
           " expecting set " + mse.expecting ;
       
     } else if ( e instanceof MismatchedNotSetException ) {
       
       final MismatchedNotSetException mse = ( MismatchedNotSetException ) e ;
       msg = "mismatched input " + getTokenErrorDisplay( e.token ) +
           " expecting set " + mse.expecting ;
       
     } else if ( e instanceof FailedPredicateException ) {
       final FailedPredicateException fpe = ( FailedPredicateException ) e ;
       msg = "rule " + fpe.ruleName + " failed predicate: {" +
           fpe.predicateText + "}?" ;
     }
     
     
     
     return msg ;
   }
 
  public static String getTokenErrorDisplay( final Token t ) {
     String s = t.getText() ;
     if ( s == null ) {
       if ( t.getType() == Token.EOF ) {
         s = "<EOF>" ;
       } else {
         s = "<" + t.getType() + ">" ;
       }
     }
     if( s.length() == 1 ) {
       s = "'" + s + "' " + UnicodeNames.getDecoratedName( s.charAt( 0 ) ) ;
     } else {
       s = s.replaceAll( "\n", "\\\\n" ) ;
       s = s.replaceAll( "\r", "\\\\r" ) ;
       s = s.replaceAll( "\t", "\\\\t" ) ;
       s = "'" + s + "'" ;
     }
     return s ;
   }
 
 
 }
