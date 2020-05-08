 package org.zza.parser;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class RuleTable {
     
     private static final String NON_TERMINAL_DELIMITER = "%;";
     private static final String TERMINAL_DELIMITER = "`";
     private static final String RULE_DELIMITER = "~";
     private HashMap<String, Map<String, Integer>> tableContents;
     private ArrayList<List<Entry>> ruleArray;
     private final String fileName = "parseTable.dat";
     
     public RuleTable() {
         buildRuleList();
         buildTable();
     }
     
     private void buildTable() {
         tableContents = new HashMap<String, Map<String, Integer>>();
         
         BufferedReader reader = null;
         try {
             
 //            File directory = new File (".");
 //            try {
 //            System.out.println ("Current directory's canonical path: " 
 //             + directory.getCanonicalPath()); 
 //              System.out.println ("Current directory's absolute  path: " 
 //             + directory.getAbsolutePath());
 //            }catch(Exception e) {
 //            System.out.println("Exceptione is ="+e.getMessage());
 //             }
             
             
             
             
             reader = new BufferedReader(new FileReader(new File(fileName)));
         } catch (final FileNotFoundException e) {
             System.out.println("Could not locate " + fileName + ". Unable to continue. Please locate this file.");
         }
         String nextLine = "";
         try {
             nextLine = reader.readLine();
         } catch (final IOException e) {
             e.printStackTrace();
         }
         
         while (nextLine != null) {
             String nonterminal = "";
             String terminal = "";
             Integer ruleIndex = 0;
             
             final String[] lineParts = nextLine.split(NON_TERMINAL_DELIMITER);
             final String[] terminalParts = lineParts[1].split(TERMINAL_DELIMITER);
             nonterminal = lineParts[0];
             
             for (final String terminalIndexPair : terminalParts) {
                 final String[] term = terminalIndexPair.split(RULE_DELIMITER);
                 terminal = term[0];
                 ruleIndex = Integer.parseInt(term[1]);
                 try {
                     addToTable(nonterminal, terminal, ruleIndex);   
                 } catch (final Exception e) {
                     e.printStackTrace();
                 }
             }
             
             try {
                 nextLine = reader.readLine();
             } catch (final IOException e) {
                 e.printStackTrace();
             }
         }
     }
     
     private void addToTable(final String nonterminal, final String terminal, final Integer ruleIndex) throws Exception {
         if (tableContents.containsKey(nonterminal)) {
             if (tableContents.get(nonterminal).containsKey(terminal)) {
                 throw new Exception("Attempting to overwrite rule in parsing table. :" + nonterminal + " , " + terminal + " , " + ruleIndex + ".");
             } else {
                 tableContents.get(nonterminal).put(terminal, ruleIndex);
             }
         } else {
             final HashMap<String, Integer> newEntry = new HashMap<String, Integer>();
             newEntry.put(terminal, ruleIndex);
             tableContents.put(nonterminal, newEntry);
         }
     }
     
     private void addToRuleArray(List<Entry> rule) {
         ruleArray.add(rule);
     }
     
     private void buildRuleList() {
         final ArrayList<Entry> rule = new ArrayList<Entry>();
         ruleArray = new ArrayList<List<Entry>>();
         // <PROGRAM>::=program <identifier> ( <PARAMETERS> ) ; <DECLARATIONS>
         // <COMPOUND_STATEMENT> .
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("program"),
                 new TerminalEntry("<identifier>"),
                 new TerminalEntry("("),
                 new NonterminalEntry("<PARAMETERS>"),
                 new TerminalEntry(")"),
                 new TerminalEntry(";"),
                 new NonterminalEntry("<DECLARATIONS>"),
                 new NonterminalEntry("<COMPOUND_STATEMENT>"), 
                 new TerminalEntry(".")}));
         // <DECLARATIONS>::=<VARIABLE_DECLARATIONS> <FUNCTION_DECLARATIONS>
         addToRuleArray(Arrays.asList(new Entry[] {new NonterminalEntry("<VARIABLE_DECLARATIONS>"), 
                 new NonterminalEntry("<FUNCTION_DECLARATIONS>")}));
         // <FUNCTION_DECLARATIONS>::=<FXN_DECLARATION_LIST>
         addToRuleArray(Arrays.asList(new Entry[] {new NonterminalEntry("<FXN_DECLARATION_LIST>")}));
         // <epsilon>
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("<epsilon>")}));
         // <FXN_DECLARATION_LIST_TAIL>::=<FXN_DECLARATION_LIST>
         addToRuleArray(Arrays.asList(new Entry[] {new NonterminalEntry("<FXN_DECLARATION_LIST>")}));
         // <epsilon>
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("<epsilon>")}));
         // <FXN_DECLARATION_LIST>::=<FXN_DECLARATION>
         // <FXN_DECLARATION_LIST_TAIL>
         addToRuleArray(Arrays.asList(new Entry[] {new NonterminalEntry("<FXN_DECLARATION>"), 
                 new NonterminalEntry("<FXN_DECLARATION_LIST_TAIL>")}));
         // <FXN_DECLARATION>::=<FXN_HEADING> <FXN_BODY> ;
        addToRuleArray(Arrays.asList(new Entry[] {new NonterminalEntry("FXN_HEADING>"), 
                new NonterminalEntry("<FXN_BODY>")}));
         // <FXN_HEADING>::=function <identifier> ( <PARAMETERS> ) : <TYPE>
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("function"),
                 new TerminalEntry("<identifier>"),
                 new TerminalEntry("("),
                new NonterminalEntry("<PAREMETERS>"),
                 new TerminalEntry(")"),
                 new TerminalEntry(":"),
                 new NonterminalEntry("<TYPE>")}));
         // <FXN_BODY>::=<VARIABLE_DECLARATIONS> <COMPOUND_STATEMENT>
         addToRuleArray(Arrays.asList(new Entry[] {new NonterminalEntry("<VARIABLE_DECLARATIONS>"), 
                 new NonterminalEntry("<COMPOUND_STATEMENT>")}));
         // <VARIABLE_DECLARATIONS>::=var <VAR_DECLARATION_LIST>
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("var"), 
                 new NonterminalEntry("<VAR_DECLARATION_LIST>")}));
         // <epsilon>
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("<epsilon>")}));
         // <VAR_DECLARATION_LIST_TAIL>::=<VAR_DECLARATION_LIST>
         addToRuleArray(Arrays.asList(new Entry[] {new NonterminalEntry("<VAR_DECLARATION_LIST>")}));
         // <epsilon>
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("<epsilon>")}));
         // <VAR_DECLARATION_LIST>::=<VAR_DECLARATION>
         // <VAR_DECLARATION_LIST_TAIL>
         addToRuleArray(Arrays.asList(new Entry[] {new NonterminalEntry("<VAR_DECLARATION>"), 
                 new NonterminalEntry("<VAR_DECLARATION_LIST_TAIL>")}));
         // <VAR_DECLARATION>::=<identifier> : <TYPE> ;
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("<identifier>"),
                 new TerminalEntry(":"),
                 new NonterminalEntry("<TYPE>"),
                 new TerminalEntry(";")}));
         // <PARAMETERS>::=<epsilon>
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("<epsilon>")}));
         // <PARAMETER_LIST>
         addToRuleArray(Arrays.asList(new Entry[] {new NonterminalEntry("<PARAMETER_LIST>")}));
         // <PARAMETER_LIST>::=<PARAMETER> <PARAMETER_LIST_TAIL>
         addToRuleArray(Arrays.asList(new Entry[] {new NonterminalEntry("<PARAMETER>"), 
                 new NonterminalEntry("<PARAMETER_LIST_TAIL>")}));
         // <PARAMETER_LIST_TAIL>::=, <PARAMETER_LIST>
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry(","), 
                 new NonterminalEntry("<PARAMETER_LIST>")}));
         // <epsilon>
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("<epsilon>")}));
         // <PARAMETER>::=<identifier> : <TYPE>
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("<identifier>"), 
                 new TerminalEntry(":"), 
                 new NonterminalEntry("<TYPE>")}));
         // <TYPE>::=integer
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("<integer>")}));
         // real
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("<real>")}));
         // <STATEMENT>::=<ASSIGNMENT_STATEMENT>
         addToRuleArray(Arrays.asList(new Entry[] {new NonterminalEntry("<ASSIGNMENT_STATEMENT>")}));
         // <IF_STATEMENT>
         addToRuleArray(Arrays.asList(new Entry[] {new NonterminalEntry("<IF_STATEMENT>")}));
         // <WHILE_STATEMENT>
         addToRuleArray(Arrays.asList(new Entry[] {new NonterminalEntry("<WHILE_STATEMENT>")}));
         // <COMPOUND_STATEMENT>
         addToRuleArray(Arrays.asList(new Entry[] {new NonterminalEntry("<COMPOUND_STATEMENT>")}));
         // <RETURN_STATEMENT>
         addToRuleArray(Arrays.asList(new Entry[] {new NonterminalEntry("<RETURN_STATEMENT>")}));
         // <epsilon>
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("<epsilon>")}));
         // <STATEMENT_LIST>::=<STATEMENT> <STATEMENT_LIST_TAIL>
         addToRuleArray(Arrays.asList(new Entry[] {new NonterminalEntry("<STATEMENT>"), 
                 new NonterminalEntry("<STATEMENT_LIST_TAIL>")}));
         // <STATEMENT_LIST_TAIL>::=; <STATEMENT_LIST>
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry(";"), 
                 new NonterminalEntry("<STATEMENT_LIST>")}));
         // <epsilon>
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("<epsilon>")}));
         // <ASSIGNMENT_STATEMENT>::=<identifier> := <EXPRESSION>
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("<identifier>"), 
                 new TerminalEntry(":="), 
                 new NonterminalEntry("<EXPRESSION>")}));
         // <IF_STATEMENT>::=if <COMPARISON> then <STATEMENT> else <STATEMENT>
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("if"),
                 new NonterminalEntry("<COMPARISON>"),
                 new TerminalEntry("then"),
                 new NonterminalEntry("<STATEMENT>"),
                 new TerminalEntry("else"),
                 new NonterminalEntry("<STATEMENT>")}));
         // <WHILE_STATEMENT>::=while <COMPARISON> do <STATEMENT>
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("while"),
                 new NonterminalEntry("<COMPARISON>"),
                 new TerminalEntry("do"),
                 new NonterminalEntry("<STATEMENT>")}));
         // <COMPOUND_STATEMENT>::=begin <STATEMENT_LIST> end
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("begin"), 
                 new NonterminalEntry("<STATEMENT_LIST>"), 
                 new TerminalEntry("end")}));
         // <RETURN_STATEMENT>::=return <EXPRESSION>
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("return"), 
                 new NonterminalEntry("<EXPRESSION>")}));
         // <COMPARISON>::=<EXPRESSION> <COMPARE_OP> <EXPRESSION>
         addToRuleArray(Arrays.asList(new Entry[] {new NonterminalEntry("<EXPRESSION>"), 
                 new NonterminalEntry("<COMPARE_OP>"), 
                 new NonterminalEntry("<EXPRESSION>")}));
         // <COMPARE_OP>::==
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("=")}));
         // <
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("<")}));
         // >
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry(">")}));
         // <=
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("<=")}));
         // >=
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry(">=")}));
         // !=
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("!=")}));
         // <ARGUMENTS>::=<epsilon>
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("<epsilon>")}));
         // <ARGUMENT_LIST>
         addToRuleArray(Arrays.asList(new Entry[] {new NonterminalEntry("<ARGUMENT_LIST>")}));
         // <ARGUMENT_LIST>::=<EXPRESSION> <ARGUMENT_LIST_TAIL>
         addToRuleArray(Arrays.asList(new Entry[] {new NonterminalEntry("<EXPRESSION>"), 
                 new NonterminalEntry("<ARGUMENT_LIST_TAIL>")}));
         // <ARGUMENT_LIST_TAIL>::=<epsilon>
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("<epsilon>")}));
         // , <ARGUMENT_LIST>
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry(","), 
                 new NonterminalEntry("<ARGUMENT_LIST>")}));
         // <EXPRESSION>::=- <EXPRESSION_BODY>
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("-"), 
                 new NonterminalEntry("<EXPRESSION_BODY>")}));
         // <EXPRESSION_BODY>
         addToRuleArray(Arrays.asList(new Entry[] {new NonterminalEntry("<EXPRESSION_BODY>")}));
         // <EXPRESSION_BODY>::=<TERM> <ADDITIVE_EXPRESSION>
         addToRuleArray(Arrays.asList(new Entry[] {new NonterminalEntry("<TERM>"), 
                 new NonterminalEntry("<ADDITIVE_EXPRESSION>")}));
         // <ADDITIVE_EXPRESSION>::=+ <TERM>
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("+"), 
                 new NonterminalEntry("<TERM>")}));
         // - <TERM>
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("-"), 
                 new NonterminalEntry("<TERM>")}));
         // <epsilon>
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("<epsilon>")}));
         // <TERM>::=<FACTOR> <MULTIPLICATIVE_EXPRESSION>
         addToRuleArray(Arrays.asList(new Entry[] {new NonterminalEntry("<FACTOR>"), 
                 new NonterminalEntry("<MULTIPLICATIVE_EXPRESSION>")}));
         // <MULTIPLICATIVE_EXPRESSION>::=* <TERM>
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("*"), 
                 new NonterminalEntry("<TERM>")}));
         // / <TERM>
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("/"), 
                 new NonterminalEntry("<TERM>")}));
         // <epsilon>
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("<epsilon>")}));
         // <FACTOR>::=( <EXPRESSION> )
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("("), 
                 new NonterminalEntry("<EXPRESSION>"), 
                 new TerminalEntry(")")}));
         // <identifier> FUNCTION_CALL
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("<identifier>"), 
                 new NonterminalEntry("<FUNCTION_CALL>")}));
         // <LITERAL>
         addToRuleArray(Arrays.asList(new Entry[] {new NonterminalEntry("<LITERAL>")}));
         // <FUNCTION_CALL>::= ( <ARGUMENTS> )
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("("),
                 new NonterminalEntry("<ARGUMENTS>"),
                 new TerminalEntry(")")}));
         // <epsilon>
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("<epsilon>")} ));
         // <LITERAL>::=<integer>
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("integer")}));
         // <real>
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("real")}));
         //STATEMENT::=PRINT_STATEMENT
         addToRuleArray(Arrays.asList(new Entry[] {new NonterminalEntry("<PRINT_STATEMENT>")} ));
         //PRINT_STATEMENT::=print ( ARGUMENTS )
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("print"),
                 new TerminalEntry("("),
                 new NonterminalEntry("<ARGUMENTS>"),
                 new TerminalEntry(")")} ));
     }
     
     public List<Entry> find(final String A, final String i) {
         System.out.println("'"+A+"' '"+i+"'");
         final int index = tableContents.get(A).get(i);
         System.out.println(index);
         return ruleArray.get(index);
     }
     
 }
