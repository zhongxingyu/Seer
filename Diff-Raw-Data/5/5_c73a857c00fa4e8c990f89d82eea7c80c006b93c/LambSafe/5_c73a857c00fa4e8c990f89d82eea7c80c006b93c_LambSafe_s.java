 /*
  * ModelCC, under ModelCC Shared Software License, www.modelcc.org. Luis Quesada Torres.
  */
 
 
 package org.modelcc.lexer.lamb;
 
 import java.io.Reader;
 import org.modelcc.lexer.recognizer.MatchedObject;
 import java.io.Serializable;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import org.modelcc.language.lexis.LexicalSpecification;
 import org.modelcc.language.lexis.TokenOption;
 import org.modelcc.language.lexis.TokenSpecification;
 import org.modelcc.lexer.recognizer.PatternRecognizer;
 
 /**
  * Lamb - Lexer with AMBiguity Support - Contents.
  * @author elezeta
  * @serial
  */
 public final class LambSafe implements Serializable {
 
     /**
      * Serial Version ID
      */
     private static final long serialVersionUID = 31415926535897932L;
 
     /**
      * Preceding set.
      */
     Map<Token,Set<Token>> preceding;
     
     /**
      * Following set.
      */
     Map<Token,Set<Token>> following;
     
     /**
      * Builds a token, filling its data, and validates it.
      * @param t token to be built.
      * @return true if the token is valid, false if not
      */
     private boolean build(TokenSpecification m,Token t) {
         return m.getBuilder().build(t);
     }
 
     /**
      * Search state enumeration.
      */
     private enum Search {
 
         // Search has to be performed.
         YES,
 
         // Search has not to be performed.
         NO,
 
         // Search has never to be performed (i.e. ignored pattern found).
         NEVER
     }
 
     /**
      * Adds a preceding/following relationship between tokens t1 and t2.
      * @param t1 the preceding token.
      * @param t2 the following token.
      */
     private void addPreceding(Token t1,Token t2) {
         addPFElement(preceding,t2,t1);
         addPFElement(following,t1,t2);
     }
 
     /**
      * Adds a token to the set of another key token.
      * @param t1 the key token.
      * @param t2 the token to be added.
      */
     private void addPFElement(Map<Token,Set<Token>> target,Token t1,Token t2) {
         Set<Token> set = target.get(t1);
         if (set == null) {
             set = new HashSet<Token>();
             target.put(t1,set);
         }
         set.add(t2);
     }
 
     /**
      * Performs a lexical analysis.
      * @param ls the lexer specification.
      * @param ignore the list of ignore patterns.
      * @param input the input string.
      * @return the obtained lexical graph.
      */
     public LexicalGraph scan(LexicalSpecification ls,Set<PatternRecognizer> ignore,Reader input) {
     	int inputstart = 0;
     	int inputend;
     	String inputs = "";
         int n;
         Writer writer = new StringWriter();
         char[] buffer = new char[1024];
         try {
             while ((n = input.read(buffer)) != -1) {
                 writer.write(buffer, 0, n);
             }
         }
         catch (Exception e) {
 
         }
         inputs = writer.toString();
         inputend = inputs.length()-1;
         
         List<TokenSpecification> stspecs = new ArrayList<TokenSpecification>();
         stspecs.addAll(ls.getTokenSpecifications());
         if (ignore != null)
             for (Iterator<PatternRecognizer> ite = ignore.iterator();ite.hasNext();)
                 stspecs.add(new TokenSpecification(null,ite.next(),TokenOption.IGNORE));
 
         Map<TokenSpecification,Set<TokenSpecification>> precedes = ls.getPrecedences();
         List<Token> tokenList = new ArrayList<Token>();
         Set<Token> tokens = new HashSet<Token>();
         Set<Token> starts = new HashSet<Token>();
 
         // -------------
         // Scanning step
         // -------------
 
         {
 
             // INITIALIZATION
             // --------------
 
             // Auxiliar variables.
             int i,j,k;
             Set<TokenSpecification> pset;
             boolean erase;
             Iterator<Token> ite;
             preceding = new HashMap<Token,Set<Token>>();
             following = new HashMap<Token,Set<Token>>();
 
             // List of elements forbidden by precedence in each position.
             Set<TokenSpecification>[] forbidden;
             forbidden = new Set[inputs.length()+1];
             for (i = 0;i < forbidden.length;i++)
                 forbidden[i] = new HashSet<TokenSpecification>();
 
             // Start and end positions of a token.
             int start,end;
 
             // MatchedObject.
             MatchedObject match;
 
             // Determines if search must be performed starting in an inputs
             //   string index.
             Search[] search;
             search = new Search[inputs.length()+1];
             for (i = 0;i < search.length;i++)
                 search[i] = Search.NO;
             search[0] = Search.YES;
 
             // Whether any match has been performed starting in the inputs string
             // current index or not.
             boolean isMatch;
 
             // Current token specification.
             TokenSpecification ts;
 
             // Current token.
             Token t;
 
 
             // PROCEDURE
             // --------------
 
             for (i = 0;i < inputs.length();i++) {
                 if (search[i] == Search.YES) {
                     isMatch = false;
 
                     // All not forbidden token specifications are tried to match.
                     for (j = 0;j < stspecs.size();j++) {
                         ts = stspecs.get(j);
                         if (!forbidden[i].contains(ts)) {
                             match = ts.getRecognizer().read(inputs,i);
                             if (match != null) {
                                 start = i;
                                 end = i+match.getText().length()-1;
                                 t = new Token(ts.getType(),match.getObject(),start,end,match.getText());
                                 if (build(ts,t)) {
                                     isMatch = true;
 
                                     // The forbidden token specifications list is updated.
                                     pset = precedes.get(ts);
                                     if (pset != null)
                                         forbidden[i].addAll(pset);
 
                                     if (ts.getTokenOption()==TokenOption.CONSIDER) {
                                     	//System.out.println("-- LAMB found token "+t.getType()+" "+t.getStartIndex()+" to "+t.getEndIndex()+" val "+t.getString()+" recog "+ts.getRecognizer()+" INPUTS "+inputs+" at "+i+"  "+inputs.charAt(i));
                                     	if (t.getStartIndex()<=t.getEndIndex()) {
                                     		//System.out.println("OK!");
                                     		tokenList.add(t);
                                     	}
                                     }
                                     else {
                                         for (k = start;k <= end;k++)
                                             search[k] = Search.NEVER;
                                         if (inputstart == start)
                                         	inputstart = end+1;
                                         if (inputend == end)
                                         	inputend = inputend-(end-start+1);
                                     }
                                 	if (start>end)
                                 		end = start;
                                     if (end+1 < inputs.length())
                                         if (search[end+1] == Search.NO)
                                             search[end+1] = Search.YES;
                                     for (k = start;k <= end;k++) {
                                         pset = precedes.get(ts);
                                         if (pset != null)
                                             forbidden[k].addAll(pset);
                                     }
                                 }
                             }
                         }
                     }
 
                     for (ite = tokenList.iterator();ite.hasNext();) {
                         t = ite.next();
                         erase = false;
                        for (k = t.getStartIndex();k <= t.getEndIndex();k++)
                            if (search[k] == Search.NEVER)
                                erase = true;
                         if (erase)
                             ite.remove();
                         else
                             tokens.add(t);
 
                     }
 
                     // If no matches were found in the current position, it is
                     //   skipped.
                     if (!isMatch)
                         if (search[i+1] == Search.NO)
                             search[i+1] = Search.YES;
                 }
             }
 
         }
 
 
         // -------------
         // Graph generation step
         // -------------
 
         {
 
             // INITIALIZATION
             // --------------
 
             // Auxiliar variables.
             int i,j;
             Set<Token> s;
 
             // Tokens.
             Token ti,tj;
 
             // State.
             int state;
 
             // Minimum end position.
             int minend;
 
 
             // PROCEDURE
             // --------------
 
             //Link tokens.
             for (i = tokenList.size()-1;i >= 0;i--) {
                 ti = tokenList.get(i);
                 state = 0;
                 minend = inputs.length()+1;
                 for (j = i+1;j < tokenList.size() && state != 2;j++) {
                     tj = tokenList.get(j);
                     switch (state) {
                         case 0:
                             if (tj.getStartIndex()>ti.getEndIndex())
                                 state = 1;
                             // no break!
                         case 1:
                             if (tj.getStartIndex()>ti.getEndIndex()) {
                                 if (tj.getStartIndex()>minend)
                                     state = 2;
                                 else {
                                     minend = Math.min(minend,tj.getEndIndex());
                                     addPreceding(ti,tj);
                                 }
                             }
                     }
                 }
             }
 
             // Generate start token set.
             for (i = 0;i < tokenList.size();i++) {
                 if (preceding.get(tokenList.get(i)) == null)
                     starts.add(tokenList.get(i));
             }
 
         }
 
 
         // -------------
         // Returns value
         // -------------
 
         return new LexicalGraph(tokens, starts, preceding, following,inputstart,inputend);
 
     }
 
 }
