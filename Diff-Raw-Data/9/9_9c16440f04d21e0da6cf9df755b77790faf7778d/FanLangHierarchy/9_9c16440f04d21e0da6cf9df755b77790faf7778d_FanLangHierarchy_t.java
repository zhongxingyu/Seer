 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package net.colar.netbeans.fan;
 
 import java.util.Collection;
 import java.util.Map;
 import org.netbeans.api.lexer.InputAttributes;
 import org.netbeans.api.lexer.LanguagePath;
 import org.netbeans.api.lexer.Token;
 import org.netbeans.spi.lexer.LanguageEmbedding;
 import org.netbeans.spi.lexer.LanguageHierarchy;
 import org.netbeans.spi.lexer.Lexer;
 import org.netbeans.spi.lexer.LexerRestartInfo;
 
 /**
  *
  * @author thibautc
  */
 public class FanLangHierarchy extends LanguageHierarchy{
 
     public static final String FAN_MIME_TYPE="text/x-fan";
 
     @Override
     protected Collection createTokenIds()
     {
	return NBFanLexer.getTokenIds();
     }
 
     @Override
     protected Lexer createLexer(LexerRestartInfo arg0)
     {
 	return new NBFanLexer();
     }
 
     @Override
     protected String mimeType()
     {
 	return FAN_MIME_TYPE;
     }
 
     /*@Override
     protected Map createTokenCategories()
     {
 	throw new UnsupportedOperationException("Not supported yet.");
     }*/
 
     @Override
     protected LanguageEmbedding embedding(Token arg0, LanguagePath arg1, InputAttributes arg2)
     {
 	return null;
     }
 
 }
