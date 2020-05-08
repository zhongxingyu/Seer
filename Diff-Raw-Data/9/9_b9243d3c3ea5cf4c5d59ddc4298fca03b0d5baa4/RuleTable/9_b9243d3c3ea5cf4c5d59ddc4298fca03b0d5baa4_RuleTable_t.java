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
 
 import org.zza.parser.parsingstack.Entry;
 import org.zza.parser.parsingstack.NonterminalEntry;
 import org.zza.parser.parsingstack.SemanticEntry;
 import org.zza.parser.parsingstack.TerminalEntry;
 
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
         ruleArray = new ArrayList<List<Entry>>();
         // <PROGRAM>::=program <identifier> ( <PARAMETERS> ) ; <DECLARATIONS>
         // <COMPOUND_STATEMENT> .
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("program"),
                 new TerminalEntry("<identifier>"),
                 new SemanticEntry("identifier"),
                 new TerminalEntry("("),
                 new NonterminalEntry("<PARAMETERS>"),
                 new SemanticEntry("AllParameters"),
                 new TerminalEntry(")"),
                 new TerminalEntry(";"),
                 new SemanticEntry("ProgramHeader"),
                 new NonterminalEntry("<DECLARATIONS>"),
                //new SemanticEntry("declarations"),
                 new NonterminalEntry("<COMPOUND_STATEMENT>"), 
                 new TerminalEntry("."),
                 new SemanticEntry("Program")}));
         // <DECLARATIONS>::=<VARIABLE_DECLARATIONS> <FUNCTION_DECLARATIONS>
         addToRuleArray(Arrays.asList(new Entry[] {new NonterminalEntry("<VARIABLE_DECLARATIONS>"), 
                 new SemanticEntry("allvariables"),
                 new NonterminalEntry("<FUNCTION_DECLARATIONS>"),
                 new SemanticEntry("allfunctions"),
                 new SemanticEntry("declarations")}));
         // <FUNCTION_DECLARATIONS>::=<FXN_DECLARATION_LIST>
         addToRuleArray(Arrays.asList(new Entry[] {new NonterminalEntry("<FXN_DECLARATION_LIST>")}));
         // <epsilon>
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("<epsilon>")}));
         // <FXN_DECLARATION_LIST_TAIL>::=<FXN_DECLARATION_LIST>
         addToRuleArray(Arrays.asList(new Entry[] {new NonterminalEntry("<FXN_DECLARATION_LIST>")}));
         // <epsilon>
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("<epsilon>")}));
         // <FXN_DECLARATION_LIST>::=<FXN_DECLARATION> <FXN_DECLARATION_LIST_TAIL>
         addToRuleArray(Arrays.asList(new Entry[] {new NonterminalEntry("<FXN_DECLARATION>"), 
                 new NonterminalEntry("<FXN_DECLARATION_LIST_TAIL>")}));
         // <FXN_DECLARATION>::=<FXN_HEADING> <FXN_BODY> ;
         addToRuleArray(Arrays.asList(new Entry[] {new NonterminalEntry("<FXN_HEADING>"), 
                 new NonterminalEntry("<FXN_BODY>"),
                 new TerminalEntry(";"),
                 new SemanticEntry("function")}));
         // <FXN_HEADING>::=function <identifier> ( <PARAMETERS> ) : <TYPE>
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("function"),
                 new TerminalEntry("<identifier>"),
                 new SemanticEntry("identifier"),
                 new TerminalEntry("("),
                 new NonterminalEntry("<PARAMETERS>"),
                 new SemanticEntry("AllParameters"),
                 new TerminalEntry(")"),
                 new TerminalEntry(":"),
                 new NonterminalEntry("<TYPE>"),
                 new SemanticEntry("functionheading")}));
         // <FXN_BODY>::=<VARIABLE_DECLARATIONS> <COMPOUND_STATEMENT>
         addToRuleArray(Arrays.asList(new Entry[] {new NonterminalEntry("<VARIABLE_DECLARATIONS>"),
                 new SemanticEntry("allvariables"),
                 new NonterminalEntry("<COMPOUND_STATEMENT>"),
                 new SemanticEntry("functionbody")}));
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
                 new SemanticEntry("identifier"),
                 new TerminalEntry(":"),
                 new NonterminalEntry("<TYPE>"),
                 new SemanticEntry("variabledeclaration"),
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
                 new SemanticEntry("identifier"),
                 new TerminalEntry(":"), 
                 new NonterminalEntry("<TYPE>"),
                 new SemanticEntry("ParameterNode")}));
         // <TYPE>::=integer
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("<integer>"),
                 new SemanticEntry("type")}));
         // real
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("<real>"),
                 new SemanticEntry("type")}));
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
                 new SemanticEntry("identifier"),
                 new TerminalEntry(":="), 
                 new NonterminalEntry("<EXPRESSION>"),
                 new SemanticEntry("assignment")}));
         // <IF_STATEMENT>::=if <COMPARISON> then <STATEMENT> else <STATEMENT>
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("if"),
                 new NonterminalEntry("<COMPARISON>"),
                 new TerminalEntry("then"),
                 new NonterminalEntry("<STATEMENT>"),
                 new TerminalEntry("else"),
                 new NonterminalEntry("<STATEMENT>"),
                 new SemanticEntry("if")}));
         // <WHILE_STATEMENT>::=while <COMPARISON> do <STATEMENT>
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("while"),
                 new NonterminalEntry("<COMPARISON>"),
                 new TerminalEntry("do"),
                 new NonterminalEntry("<STATEMENT>"),
                 new SemanticEntry("while")}));
         // <COMPOUND_STATEMENT>::=begin <STATEMENT_LIST> end
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("begin"),
                 new SemanticEntry("compoundbegin"),
                 new NonterminalEntry("<STATEMENT_LIST>"),
                 new SemanticEntry("compound"),
                 new TerminalEntry("end")}));
         // <RETURN_STATEMENT>::=return <EXPRESSION>
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("return"), 
                 new NonterminalEntry("<EXPRESSION>"),
                 new SemanticEntry("return")}));
         // <COMPARISON>::=<EXPRESSION> <COMPARE_OP> <EXPRESSION>
         addToRuleArray(Arrays.asList(new Entry[] {new NonterminalEntry("<EXPRESSION>"), 
                 new NonterminalEntry("<COMPARE_OP>"),
                 new SemanticEntry("compare"),
                 new NonterminalEntry("<EXPRESSION>"),
         		new SemanticEntry("comparison")}));
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
                 new NonterminalEntry("<EXPRESSION_BODY>"),
                 new SemanticEntry("negative")}));
         // <EXPRESSION_BODY>
         addToRuleArray(Arrays.asList(new Entry[] {new NonterminalEntry("<EXPRESSION_BODY>")}));
         // <EXPRESSION_BODY>::=<TERM> <ADDITIVE_EXPRESSION>
         addToRuleArray(Arrays.asList(new Entry[] {new NonterminalEntry("<TERM>"), 
                 new NonterminalEntry("<ADDITIVE_EXPRESSION>")}));
         // <ADDITIVE_EXPRESSION>::=+ <TERM>
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("+"), 
                 new NonterminalEntry("<TERM>"),
                 new SemanticEntry("plus")}));
         // - <TERM>
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("-"), 
                 new NonterminalEntry("<TERM>"),
                 new SemanticEntry("minus")}));
         // <epsilon>
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("<epsilon>")}));
         // <TERM>::=<FACTOR> <MULTIPLICATIVE_EXPRESSION>
         addToRuleArray(Arrays.asList(new Entry[] {new NonterminalEntry("<FACTOR>"), 
                 new NonterminalEntry("<MULTIPLICATIVE_EXPRESSION>")}));
         // <MULTIPLICATIVE_EXPRESSION>::=* <TERM>
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("*"), 
                 new NonterminalEntry("<TERM>"),
                 new SemanticEntry("multiplication")}));
         // / <TERM>
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("/"), 
                 new NonterminalEntry("<TERM>"),
                 new SemanticEntry("division")}));
         // <epsilon>
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("<epsilon>")}));
         // <FACTOR>::=( <EXPRESSION> )
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("("), 
                 new NonterminalEntry("<EXPRESSION>"), 
                 new TerminalEntry(")")}));
         // <identifier> FUNCTION_CALL
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("<identifier>"), 
                 new SemanticEntry("identifier"),
                 new NonterminalEntry("<FUNCTION_CALL>")}));
         // <LITERAL>
         addToRuleArray(Arrays.asList(new Entry[] {new NonterminalEntry("<LITERAL>")}));
         // <FUNCTION_CALL>::= ( <ARGUMENTS> )
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("("),
                 new SemanticEntry("argumentbegin"),
                 new NonterminalEntry("<ARGUMENTS>"),
                 new SemanticEntry("argument"),
                 new TerminalEntry(")"),
                 new SemanticEntry("functioncall")}));
         // <epsilon>
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("<epsilon>")} ));
         // <LITERAL>::=<integer>
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("integer"),
                 new SemanticEntry("integer")}));
         // <real>
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("real"),
                 new SemanticEntry("real")}));
         //STATEMENT::=PRINT_STATEMENT
         addToRuleArray(Arrays.asList(new Entry[] {new NonterminalEntry("<PRINT_STATEMENT>")} ));
         //PRINT_STATEMENT::=print ( ARGUMENTS )
         addToRuleArray(Arrays.asList(new Entry[] {new TerminalEntry("print"),
                 new TerminalEntry("("),
                 new SemanticEntry("argumentbegin"),
                 new NonterminalEntry("<ARGUMENTS>"),
                 new SemanticEntry("argument"),
                 new TerminalEntry(")"),
                 new SemanticEntry("print")} ));
     }
     
     public List<Entry> find(final String A, final String i) {
//        System.out.println("'"+A+"' '"+i+"'");
         final int index = tableContents.get(A).get(i);
         //System.out.println(index);
         return ruleArray.get(index);
     }
     
 }
