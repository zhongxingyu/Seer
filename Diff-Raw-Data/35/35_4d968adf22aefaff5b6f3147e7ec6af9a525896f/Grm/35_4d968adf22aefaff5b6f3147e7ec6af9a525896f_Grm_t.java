 package net.cscott.sdr.calls.grm;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import net.cscott.sdr.calls.ast.Apply;
 import net.cscott.sdr.calls.transform.CallFileBuilder;
 
 /**
  * This class contains inner classes creating an AST for the 'natural language'
  * grammar of square dance calls and concepts.  This AST is transformed into
  * a Java Speech grammar for the Sphinx speech recognition engine, which then
  * generates text strings.  The AST is also transformed into an ANTLR v3
  * grammar parsing those text strings, which creates {@link Apply} trees.
  * The raw rules from the call file need to be processed to remove
  * left recursion and to disambiguate using precedence levels.
  * 
  * @author C. Scott Ananian
  * @version $Id: Grm.java,v 1.3 2006-10-22 15:46:06 cananian Exp $
  */
 public abstract class Grm {
     public abstract int precedence();
     public abstract <T> T accept(GrmVisitor<T> v);
     public final String toString() {
         return accept(new ToStringVisitor());
     }
     
     /** Alternation: a|b. */
     public static class Alt extends Grm {
         public final List<Grm> alternates;
         public Alt(List<Grm> alternates) {
             this.alternates = Collections.unmodifiableList
             (Arrays.asList(alternates.toArray(new Grm[alternates.size()])));
         }
         public int precedence() { return 0; }
         @Override
         public <T> T accept(GrmVisitor<T> v) {
             return v.visit(this);
         }
     }
     /** Concatanation: a b. */
     public static class Concat extends Grm {
         public final List<Grm> sequence;
         public Concat(List<Grm> sequence) {
             this.sequence = Collections.unmodifiableList
             (Arrays.asList(sequence.toArray(new Grm[sequence.size()])));
         }
         public int precedence() { return 1; }
         @Override
         public <T> T accept(GrmVisitor<T> v) {
             return v.visit(this);
         }
     }
     /** Multiplicity marker: a*, a+, or a?. */
     public static class Mult extends Grm {
         public enum Type {
             STAR('*'), PLUS('+'), QUESTION('?');
             public final char value;
             Type(char value) { this.value = value; }
             public String toString() { return ""+value; }
         };
         public final Grm operand;
         public final Type type;
         public Mult(Grm operand, Type type) {
             this.operand=operand; this.type=type;
         }
         public int precedence() { return 2; }
         @Override
         public <T> T accept(GrmVisitor<T> v) {
             return v.visit(this);
         }
     }
     /** A nonterminal reference to an external rule. */
     public static class Nonterminal extends Grm {
         // Name of the grammar rule referenced.
         public final String ruleName;
         // If not -1, which parameter should get the value of this rule
         public final int param;
         public Nonterminal(String ruleName, int param) {
             this.ruleName=ruleName; this.param=param;
         }
         public int precedence() { return 3; }
         @Override
         public <T> T accept(GrmVisitor<T> v) {
             return v.visit(this);
         }
     }
     /** A grammar terminal: a string literal to match. */
     public static class Terminal extends Grm {
         public final String literal;
         public Terminal(String literal) {
             this.literal=literal;
         }
         public int precedence() { return 3; }
         @Override
         public <T> T accept(GrmVisitor<T> v) {
             return v.visit(this);
         }
     }
 
     // helper functions on Grm
     /** Make a {@link Grm} which will recognize the given
      * sequence of terminal symbols. */
     public static Grm mkGrm(String... terminals) {
         List<Grm> l = new ArrayList<Grm>(terminals.length);
         for (String s : terminals)
             l.add(new Terminal(s));
         return new Concat(l);
     }
     /**
      * Parse a string representing a {@link Grm}.  Parameter
      * references must be numerical. 
      * @throws IllegalArgumentException if the rule is malformed.
      * @doc.test Successful parse:
      *  js> Grm.parse("foo bar|bat? baz")
      *  foo bar|bat? baz
      * @doc.test Unsuccessful parse:
      *  js> try {
      *    >   Grm.parse("[abc]")
      *    > } catch (e) { print (e.javaException) }
      *  java.lang.IllegalArgumentException: bad grammar rule: [abc]
      */
     public static Grm parse(String rule) {
         try {
 	    return CallFileBuilder.parseGrm(rule);
         } catch (Exception e) {
             throw new IllegalArgumentException("bad grammar rule: "+rule);
         }
     }
 }
