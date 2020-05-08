 package org.zza.parser;
 
 import java.util.List;
 
 import org.zza.parser.parsingstack.Entry;
 import org.zza.parser.parsingstack.ParseStack;
 import org.zza.parser.parsingstack.TerminalEntry;
 import org.zza.parser.semanticstack.SemanticNodeFactory;
 import org.zza.parser.semanticstack.SemanticStack;
 import org.zza.parser.semanticstack.nodes.SemanticNode;
 import org.zza.scanner.CompilerToken;
 import org.zza.scanner.CompilerTokenStream;
 import org.zza.visitor.StackPrintingVisitor;
 
 public class CompilerParser {
     
     private final String COMMENT = "COMMENT";
     private final String EOF = "EOF";
     private final String startSymbol = "<PROGRAM>";
     private final String startToken = "program";
     private final ParseStack parseStack;
     private final SemanticStack semanticStack;
     private final SemanticNodeFactory nodeFactory;
     private final RuleTable ruleTable;
     private RecentTokensStack recentTokens;
     private final CompilerTokenStream stream;
     private Entry A;
     private CompilerToken i;
 //    private String previousStackValue;
     private String submissionOutput;
     
     public CompilerParser(final CompilerTokenStream tokenStream) {
         stream = tokenStream;
         recentTokens = new RecentTokensStack();
         parseStack = new ParseStack();
         semanticStack = new SemanticStack();
         ruleTable = new RuleTable();
         nodeFactory = new SemanticNodeFactory(recentTokens);
         try {
             run();
         } catch (final Exception e) {
             e.printStackTrace();
             System.exit(-1);
         }
     }
     
     public void run() throws ParsingException {
         parseStack.push(new TerminalEntry(EOF));
         addToParseStack(ruleTable.find(startSymbol, startToken));
 //        previousStackValue = "";
         submissionOutput = "";
         A = parseStack.peek();
         getNextToken();
         while ((A != null) && !A.getType().equals(EOF)) {
            
             A = parseStack.peek();
 //            System.out.println("Parse stack:" +parseStack);
//            System.out.println("working with: A:"+A +" i:"+i+" "+i.getValue()+" "+i.getStringType()+"\n");
             
             if (A.isTerminal()) {
                 
 //                System.out.println(A.getType() + " is terminal");
                 if (A.getType().equalsIgnoreCase(i.getStringType())) {
 //                System.out.println("A: "+A.getType() + " i: "+i.getId()+" "+i.getValue() + "prev: "+previousStackValue);
 //                    System.out.println("A and i match");
 //                    System.out.println("i: "+i.getId()+" "+i.getValue());
                     parseStack.pop();
                     if(parseStack.notEmpty()) {
                         A = parseStack.peek();
                         getNextToken();// i.consume();
                     }
 //                    System.out.println("i: "+i.getId()+" "+i.getValue());
                     
                 } else {
                     throw new ParsingException("Terminal mismatch. Expected: " + A.getType() + " Found: " + i.getStringType() + "");
                 }
             } else if (A.isSemanticEntry()) {
                 SemanticNode node = nodeFactory.getNewNode(A.getType());
                 node.runOnSemanticStack(semanticStack);
                 parseStack.pop();
             } else {
                 if (isRuleContained(A, i)) {
 //                    System.out.println("A is not terminal, rule was found");
                     parseStack.pop();
                     addToParseStack(ruleTable.find(A.getType(), i.getStringType()));
                     A = parseStack.peek();
                 } else {
                     throw new ParsingException("Non-terminal mismatch. No entry in the table for: " + A.getType() + " , " + i.getStringType());
                 }
             }
             
         }
         printOutSemanticStack();
         
         if (!stream.isEmpty()) {
             throw new ParsingException("Parser found the end of file marker but the token stream was not empty.");
         }
         
         if(submissionOutput.length() > 0) {
             System.out.println(submissionOutput);
         }
         StackPrintingVisitor printer = new StackPrintingVisitor();
         System.out.println(printer.visit(semanticStack.pop()));
     }
     
     private void printOutSemanticStack() {
         System.out.println("\n\nSemantic stack: ");
         for (SemanticNode node : semanticStack.getArrayToPrintAndTest()) {
             System.out.println(node.getStringRepresentation());
         }
         System.out.println("SemanticStack contains " + semanticStack.getSize() + " items");
     }
 
     private void getNextToken() {
         recentTokens.push(i);
         i = stream.getNext();
         if(i.getStringType().equals(COMMENT)) {
             getNextToken();
         }
     }
 
     private boolean isRuleContained(final Entry A, final CompilerToken i) {
        // System.out.println(A + " " + i);
         final List<Entry> returnValue = ruleTable.find(A.getType(), i.getStringType());
         return returnValue != null;
     }
     
     private void addToParseStack(final List<Entry> tableEntry) {
 //        System.out.println("adding to parse stack: "+tableEntry);
         for (int i = tableEntry.size() - 1; i >= 0; i--) {
             if(!tableEntry.get(i).isEpsilon()) {
                 parseStack.push(tableEntry.get(i));
             }
         }
     }
 }
