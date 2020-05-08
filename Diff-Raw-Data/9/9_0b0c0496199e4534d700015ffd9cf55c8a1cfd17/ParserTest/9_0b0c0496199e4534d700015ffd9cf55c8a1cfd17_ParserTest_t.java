 import java.util.Iterator;
import java.util.LinkedHashSet;
 import java.util.Set;
 
 import org.junit.Test;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 public class ParserTest {
     @Test
     public void basicFirstSet() throws Exception {
 
         NonTerminal aNonTerm = new NonTerminal("<stmt-sequence>");
         Rule aRule = new Rule(new String[]{"<stmt>","<stmt-seq'>"});
         aNonTerm.addRule(aRule);
 
         NonTerminal bNonTerm = new NonTerminal("<stmt-seq'>");
         Rule bRule = new Rule(new String[]{";","<stmt-sequence>"});
         Rule bRuleTwo = new Rule(new String[]{"<epsilon>"});
         bNonTerm.addRule(bRule);
         bNonTerm.addRule(bRuleTwo);
 
         NonTerminal cNonTerm = new NonTerminal("<stmt>");
         Rule cRule = new Rule(new String[]{"s"});
         cNonTerm.addRule(cRule);
 
        Set<NonTerminal> grammar = new LinkedHashSet<NonTerminal>();
         grammar.add(aNonTerm);
         grammar.add(bNonTerm);
         grammar.add(cNonTerm);
 
         Parser.createFirstSets(grammar);
         Iterator<NonTerminal> iter = grammar.iterator();
 
         boolean test1 = false;
         boolean test2 = false;
         boolean test3 = false;
         for (Symbol sym : iter.next().getFirstSet()) {
             if (sym.getText().equals("s")) {
                 test1 = true;
             } else if (sym.getText().equals(";")) {
                 test2 = true;
             } else if (sym.getText().equals("<epsilon>")) {
                 test3 = true;
             } else {
                 throw new Exception();
             }
         }
         assertTrue(test1);
         assertFalse(test2);
         assertFalse(test3);
 
         boolean test4 = false;
         boolean test5 = false;
         boolean test6 = false;
         for (Symbol sym : iter.next().getFirstSet()) {
             if (sym.getText().equals("s")) {
                 test4 = true;
             } else if (sym.getText().equals(";")) {
                 test5 = true;
             } else if (sym.getText().equals("<epsilon>")) {
                 test6 = true;
             } else {
                 throw new Exception();
             }
         }
         assertFalse(test4);
         assertTrue(test5);
         assertTrue(test6);
 
         boolean test7 = false;
         boolean test8 = false;
         boolean test9= false;
         for (Symbol sym : iter.next().getFirstSet()) {
             if (sym.getText().equals("s")) {
                 test7 = true;
             } else if (sym.getText().equals(";")) {
                 test8 = true;
             } else if (sym.getText().equals("<epsilon>")) {
                 test9 = true;
             } else {
                 throw new Exception();
             }
         }
         assertTrue(test7);
         assertFalse(test8);
         assertFalse(test9);
     }
 
     @Test
     public void anotherFirstSet() throws Exception {
 
         NonTerminal aNonTerm = new NonTerminal("<statement>");
         Rule aRule = new Rule(new String[]{"<if-stmt>"});
         Rule aRuleTwo = new Rule(new String[]{"other"});
         aNonTerm.addRule(aRule);
         aNonTerm.addRule(aRuleTwo);
 
         NonTerminal bNonTerm = new NonTerminal("<if-stmt>");
         Rule bRule = new Rule(new String[]{"if", "(", "<exp>", ")", "<statement>", "<else-part>"});
         bNonTerm.addRule(bRule);
 
         NonTerminal cNonTerm = new NonTerminal("<else-part>");
         Rule cRule = new Rule(new String[]{"else", "<statement>"});
         Rule cRuleTwo = new Rule(new String[]{"<epsilon>"});
         cNonTerm.addRule(cRule);
         cNonTerm.addRule(cRuleTwo);
 
         NonTerminal dNonTerm = new NonTerminal("<exp>");
         Rule dRule = new Rule(new String[]{"0"});
         Rule dRuleTwo = new Rule(new String[]{"1"});
         dNonTerm.addRule(dRule);
         dNonTerm.addRule(dRuleTwo);
 
        Set<NonTerminal> grammar = new LinkedHashSet<NonTerminal>();
         grammar.add(aNonTerm);
         grammar.add(bNonTerm);
         grammar.add(cNonTerm);
         grammar.add(dNonTerm);
 
         Parser.createFirstSets(grammar);
         Iterator<NonTerminal> iter = grammar.iterator();
 
         boolean test1 = false;
         boolean test2 = false;
         boolean test3 = false;
         boolean test4 = false;
         boolean test5 = false;
         boolean test6 = false;
         for (Symbol sym : iter.next().getFirstSet()) {
             if (sym.getText().equals("if")) {
                 test1 = true;
             } else if (sym.getText().equals("other")) {
                 test2 = true;
             } else if (sym.getText().equals("else")) {
                 test3 = true;
             } else if (sym.getText().equals("0")) {
                 test4 = true;
             } else if (sym.getText().equals("1")) {
                 test5 = true;
             } else if (sym.getText().equals("<epsilon>")) {
                 test6 = true;
             } else {
                 throw new Exception();
             }
         }
         assertTrue(test1);
         assertTrue(test2);
         assertFalse(test3);
         assertFalse(test4);
         assertFalse(test5);
         assertFalse(test6);
 
         boolean test7 = false;
         boolean test8 = false;
         boolean test9 = false;
         boolean test10 = false;
         boolean test11 = false;
         boolean test12 = false;
         for (Symbol sym : iter.next().getFirstSet()) {
           if (sym.getText().equals("if")) {
             test7 = true;
           } else if (sym.getText().equals("other")) {
             test8 = true;
           } else if (sym.getText().equals("else")) {
             test9 = true;
           } else if (sym.getText().equals("0")) {
             test10 = true;
           } else if (sym.getText().equals("1")) {
             test11 = true;
           } else if (sym.getText().equals("<epsilon>")) {
             test12 = true;
           } else {
             throw new Exception();
           }
         }
         assertTrue(test7);
         assertFalse(test8);
         assertFalse(test9);
         assertFalse(test10);
         assertFalse(test11);
         assertFalse(test12);
 
         boolean test13 = false;
         boolean test14 = false;
         boolean test15 = false;
         boolean test16 = false;
         boolean test17 = false;
         boolean test18 = false;
         for (Symbol sym : iter.next().getFirstSet()) {
             if (sym.getText().equals("if")) {
                 test13 = true;
             } else if (sym.getText().equals("other")) {
                 test14 = true;
             } else if (sym.getText().equals("else")) {
                 test15 = true;
             } else if (sym.getText().equals("0")) {
                 test16 = true;
             } else if (sym.getText().equals("1")) {
                 test17 = true;
             } else if (sym.getText().equals("<epsilon>")) {
                 test18 = true;
             } else {
                 throw new Exception();
             }
         }
         assertFalse(test13);
         assertFalse(test14);
         assertTrue(test15);
         assertFalse(test16);
         assertFalse(test17);
         assertTrue(test18);
 
         boolean test19 = false;
         boolean test20 = false;
         boolean test21 = false;
         boolean test22 = false;
         boolean test23 = false;
         boolean test24 = false;
         for (Symbol sym : iter.next().getFirstSet()) {
             if (sym.getText().equals("if")) {
                 test19 = true;
             } else if (sym.getText().equals("other")) {
                 test20 = true;
             } else if (sym.getText().equals("else")) {
                 test21 = true;
             } else if (sym.getText().equals("0")) {
                 test22 = true;
             } else if (sym.getText().equals("1")) {
                 test23 = true;
             } else if (sym.getText().equals("<epsilon>")) {
                 test24 = true;
             } else {
                 throw new Exception();
             }
         }
         assertFalse(test19);
         assertFalse(test20);
         assertFalse(test21);
         assertTrue(test22);
         assertTrue(test23);
         assertFalse(test24);
 
     }
 }
