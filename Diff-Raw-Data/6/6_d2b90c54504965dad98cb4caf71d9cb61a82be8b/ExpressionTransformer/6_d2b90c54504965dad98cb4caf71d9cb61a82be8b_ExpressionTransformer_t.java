 import java.io.*;
 import java.util.*;
 
 import org.antlr.grammar.v3.ANTLRv3Lexer;
 import org.antlr.grammar.v3.ANTLRv3Parser;
 import org.antlr.runtime.ANTLRStringStream;
 import org.antlr.runtime.CharStream;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.Lexer;
 import org.antlr.runtime.RuleReturnScope;
 import org.antlr.runtime.TokenRewriteStream;
 import org.antlr.runtime.TokenSource;
 import org.antlr.runtime.tree.CommonTree;
 import org.antlr.runtime.tree.CommonTreeAdaptor;
 import org.antlr.runtime.tree.CommonTreeNodeStream;
 import org.antlr.runtime.tree.TreeWizard;
 import org.antlr.tool.*;
 import org.antlr.stringtemplate.*;
 import antlr.ANTLRParser;
 import antlr.RecognitionException;
 import antlr.TokenStreamException;
 
 public class ExpressionTransformer {
   int membersLocation;
   boolean hasMembersSection;
   TokenRewriteStream tokens;
   List<ExpressionRule> expressions;
   
   public ExpressionTransformer(String grammarText) throws RecognitionException, TokenStreamException, org.antlr.runtime.RecognitionException {
     ANTLRv3Lexer lex = new ANTLRv3Lexer(new ANTLRStringStream(grammarText));
     tokens = new TokenRewriteStream(lex);
     ANTLRv3Parser p = new ANTLRv3Parser(tokens);
     RuleReturnScope r = p.grammarDef();   
     CommonTree t = (CommonTree)r.getTree();
     System.err.println("tree: "+t.toStringTree());
     CommonTreeNodeStream nodes = new CommonTreeNodeStream(t);
     nodes.setTokenStream(tokens);
     ExpressionCrawler ref = new ExpressionCrawler(nodes);
     
     ref.downup(t);
     expressions = ref.expressionRules;
     membersLocation = ref.membersLocation;
     hasMembersSection = ref.hasMembersSection;
   }
   
   public void printGrammar(boolean buildTree, PrintStream out) throws FileNotFoundException {
    if (expressions.size() == 0){
       out.print(tokens);
      return;
    }
      
     
     StringTemplateGroup stg = new StringTemplateGroup(new FileReader("Greedy.stg"));
     
     String membersText;
     if (!hasMembersSection)
        membersText = "@members {";
     else {
       membersText = tokens.get(membersLocation).getText();
       //remove the closing bracket
       membersText = membersText.substring(0,membersText.lastIndexOf('}'));
       tokens.delete(membersLocation);
     }
     StringTemplate staticHeader = stg.getInstanceOf("staticHeader");
     membersText += staticHeader;
     
     for (ExpressionRule rule: expressions) {
       StringTemplate header = stg.getInstanceOf("header");
       header.setAttribute("precedences", rule.precidenceOpers);
       header.setAttribute("name", rule.name);
       membersText += header;
         
       
       List<String> binaryOps = new ArrayList<String>();
       List<String> unaryOps = new ArrayList<String>();
       for (List<Operator> ops : rule.precidenceOpers) {
         if (ops == null) continue;
         for (Operator op : ops) {
           if (op.kind == Operator.Kind.Binary)
             binaryOps.add(op.tokenText);
           else if (op.kind == Operator.Kind.Unary)
             unaryOps.add(op.tokenText);
         }
       }
       
       StringTemplate ruleTemplate = stg.getInstanceOf("exprRule");
       ruleTemplate.setAttribute("name",rule.name);
       ruleTemplate.setAttribute("terminals", rule.terminals);
       ruleTemplate.setAttribute("bops", binaryOps);
       ruleTemplate.setAttribute("uops", unaryOps);
       ruleTemplate.setAttribute("buildTree", buildTree);
       
       tokens.replace(rule.startIndex, rule.stopIndex, ruleTemplate.toString());
     }
     membersText += "}";
     tokens.insertBefore(membersLocation, membersText);
     out.print(tokens);
   }
   
   public static void main(String[] args) throws Exception {
     if (args.length != 1){
       System.err.println("usage: java ExpressionTransformer <filename>");
       System.exit(1);
     }
     ExpressionTransformer et = new ExpressionTransformer(readFileAsString(args[0]));
     et.printGrammar(true,System.out);
   }
   
   
   
   
   
   
   //stolen from the internet, copyright unclean
   private static String readFileAsString(String filePath) throws java.io.IOException{
       StringBuffer fileData = new StringBuffer(1000);
       BufferedReader reader = new BufferedReader(new FileReader(filePath));
       char[] buf = new char[1024];
       int numRead=0;
       while((numRead=reader.read(buf)) != -1){
           fileData.append(buf, 0, numRead);
       }
       reader.close();
       return fileData.toString();
   }
 }
