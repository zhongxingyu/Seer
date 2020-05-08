 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package net.colar.netbeans.fan;
 
 import java.util.Collection;
 import java.util.Vector;
 import net.colar.netbeans.fan.antlr.FanLexer;
 import org.antlr.runtime.CommonTokenStream;
 import org.netbeans.api.lexer.Token;
 import org.netbeans.spi.lexer.Lexer;
 
 /**
  *
  * @author thibautc
  */
 public class NBFanLexer implements Lexer{
 
     private FanLexer lexer=null;
     private CommonTokenStream tokens=null;
     private static Vector tokenIds=new Vector();
 
     static
     {
 	FanLexer referenceLexer=new FanLexer();
 	String[] tokenNames=referenceLexer.getTokenNames();
 	int id=1;
 	for(int i=0; i!=tokenNames.length; i++)
 	{
 	    String name=tokenNames[i];
	    FanTokenID token=new FanTokenID(name, id, category);
 	    id++;
 	}
     }
 
     public static Collection getTokenIds()
     {
 	return tokenIds;
     }
 
     public NBFanLexer()
     {
 	lexer=new FanLexer();
 	tokens = new CommonTokenStream(lexer);
         //TokenFactory fact=new TokenFactory(this);
     }
 
     public Token nextToken()
     {
 	//tokens.
 	throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public Object state()
     {
 	throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public void release()
     {
 	throw new UnsupportedOperationException("Not supported yet.");
     }
 
 }
