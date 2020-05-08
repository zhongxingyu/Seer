 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.Set;
 import java.util.Stack;
 
 /**
  * Reads a grammar and pulls out a list of nonterminals. The nonterminals keep
  * hold a list of rules that they are associated with.
  *
  * @author dgreenhalgh
  */
 public class Parser {
 
 	Set<NonTerminal> nonTerminals = new LinkedHashSet<NonTerminal>();
 	List<Token> tokens = new ArrayList<Token>();
 
 	List<ProductionRule> productionRules = new ArrayList<ProductionRule>();
 	Map<String, Token> idMap = new HashMap<String, Token>();
 
 	public Parser(Set<NonTerminal> nonTerminals,List<Token> tokens,List<ProductionRule> productionRules){
 		this.nonTerminals = nonTerminals;
 		this.tokens = tokens;
 		this.productionRules = productionRules;
 	}
 
 	public Parser(String grammarFile, String specFile, String scriptFile) {
 		File inFile = new File(grammarFile);
 	  ScannerGenerator.create(specFile, scriptFile);
 		try {
 			Scanner in = new Scanner(inFile);
 
 			while(in.hasNextLine()) {
 				String s = in.nextLine();
 
 				NonTerminal nt = parseLine(s);
 
         NonTerminal prev = null;
         for (NonTerminal nTerm : nonTerminals) {
           if (nTerm.getText().equals(nt.getText())) {
             prev = nTerm;
           }
         }
 
         if (prev == null) {
           nonTerminals.add(nt);
         } else {
           for (Rule rule : nt.getRules()) {
             prev.addRule(rule);
           }
         }
 
 				tokenizeRules(nt);
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 
     createFirstSets(nonTerminals);
     for (Token t : tokens) {
       int tokloc = ScannerGenerator.tokens.indexOf(t.getText());
       if (tokloc != -1) {
         idMap.put(ScannerGenerator.ids.get(tokloc), t);
       }
     }
 	}
 
 	/**
 	 * Parses the string and creates a nonterminal out of the contents
 	 *
 	 * @param s Next line in grammar
 	 * @return nt The nonterminal on line s
 	 */
 	public NonTerminal parseLine(String s) {
 		NonTerminal nt = new NonTerminal();
 
 		String[] ruleDivided = s.split("::=");
 		nt.setText(ruleDivided[0].trim());
 		for(String ruleStr : ruleDivided[1].split("\\|")) {
       String[] ruleToken = tokenizeRule(ruleStr.trim());
       nt.addRule(new Rule(ruleToken));
 		}
 
 		return nt;
 	}
 
   /**
    * Parses the contents of a rule and tokenizes the contents
    * @param str the string to be tokenized
    * @return string array containing all the contents
    */
   public String[] tokenizeRule(String str) {
     String[] splitOnSpace = str.split(" ");
     List<String> tokenizedStrings = new ArrayList<String>();
     List<String> finalTokenizedStrings = new ArrayList<String>();
 
     // handle nonterminals
     for(int ii = 0; ii < splitOnSpace.length; ii++) {
       if (splitOnSpace[ii].indexOf('<') > 0) {
         String pre = splitOnSpace[ii].substring(0, splitOnSpace[ii].indexOf('<'));
         tokenizedStrings.add(pre);
         splitOnSpace[ii] = splitOnSpace[ii].substring(splitOnSpace[ii].indexOf('<'));
       }
       while (splitOnSpace[ii].contains("<")) {
         int endIndex = splitOnSpace[ii].indexOf('>');
         String tok = splitOnSpace[ii].substring(0, endIndex + 1);
         tokenizedStrings.add(tok);
         splitOnSpace[ii] = splitOnSpace[ii].substring(splitOnSpace[ii].indexOf('>') + 1);
       }
       if(splitOnSpace[ii].length() > 0) {
         tokenizedStrings.add(splitOnSpace[ii]);
       }
     }
 
     // handle tokens with parens
     for(String tok : tokenizedStrings) {
       // splits on open and close parens, but does not loose the parens
       String[] splitOnParen = tok.split("((?<=[()])|(?=[()]))");
 
       String temp = "";
       for (String parenTok : splitOnParen) {
         if (parenTok.length() > 0) {
           if (parenTok.matches("[^a-z()]*")) {
             if (temp.length() > 0) {
               finalTokenizedStrings.add(temp);
               temp = "";
             }
             finalTokenizedStrings.add(parenTok);
           } else {
             temp += parenTok;
           }
         }
       }
 
       if (temp.length() > 0) {
         finalTokenizedStrings.add(temp);
       }
     }
 
     return finalTokenizedStrings.toArray(new String[finalTokenizedStrings.size()]);
   }
 
   /**
 	 * Adds all the tokens to a list
 	 */
 	public void tokenizeRules(NonTerminal nt) {
 		for(Rule r : nt.getRules()) {
 			productionRules.add(new ProductionRule(nt,r.getRule()));
 			for(Symbol sym : r.getRule()) {
 				if(!sym.getText().contains("<") && (sym.getText().length() > 0)) {
 					boolean contains = false;
 					for (Token token : tokens) {
 						if(token.getText().equals(sym.getText())) {
               contains = true;
             }
 					}
 
 					if(!contains) {
 						tokens.add((Token) sym);
           }
 				}
 			}
 		}
 	}
 
   /**
    * creates the first sets for all the nonterminals
    * @param nonterminals
    */
   public static void createFirstSets(Set<NonTerminal> nonterminals) {
     List<NonTerminal> nontermQueue = new ArrayList<NonTerminal>(nonterminals);
 
     while (!nontermQueue.isEmpty()) {
       NonTerminal nTerm = nontermQueue.remove(0);
 
       for (Rule rule : nTerm.getRules()) {
         List<Symbol> symbolList = rule.getRule();
         Symbol symbol;
         int idx = 0;
         boolean cont = true;
         boolean result;
 
         while (cont && idx < symbolList.size()) {
           symbol = symbolList.get(idx);
           NonTerminal next = null;
           for (NonTerminal item : nonterminals) {
             if (item.getText().equals(symbol.getText())) {
               next = item;
             }
           }
 
           if (next == null) {
             if (symbol instanceof Identifier) {
               cont = false;
               result = nTerm.addToFirstSet((Identifier)symbol);
               if (result) {
                 for (NonTerminal ter : nonterminals) {
                   if (!nontermQueue.contains(ter)) {
                     nontermQueue.add(ter);
                   }
                 }
               }
             } else if (symbol instanceof Terminal) {
               cont = false;
               result = nTerm.addToFirstSet((Terminal)symbol);
               if (result) {
                 for (NonTerminal ter : nonterminals) {
                   if (!nontermQueue.contains(ter)) {
                     nontermQueue.add(ter);
                   }
                 }
               }
             } else {
               try {
                 throw new Exception("YOU SHOULDN'T BE HERE.");
               } catch (Exception e) {
                 e.printStackTrace();
               }
             }
           } else {
             Set<Token> nextFirstSet = next.getFirstSet();
             if (nextFirstSet.isEmpty()) {
               cont = false;
             } else {
               Terminal epsilon = null;
               for (Symbol firstToken : nextFirstSet) {
                 if (firstToken.getText().equals("<epsilon>")) {
                   epsilon = (Terminal) firstToken;
                 }
               }
 
               if (epsilon != null) {
                 nextFirstSet.remove(epsilon);
               } else {
                 cont = false;
               }
 
               result = nTerm.addAllToFirstSet(nextFirstSet);
               if (result) {
                 for (NonTerminal ter : nonterminals) {
                   if (!nontermQueue.contains(ter)) {
                     nontermQueue.add(ter);
                   }
                 }
               }
             }
           }
 
           idx++;
         }
         if (cont) {
           result = nTerm.addToFirstSet(new Terminal("<epsilon>"));
           if (result) {
             for (NonTerminal ter : nonterminals) {
               if (!nontermQueue.contains(ter)) {
                 nontermQueue.add(ter);
               }
             }
           }
         }
       }
     }
     createFollowSet(nonterminals);
   }
 
 
 	public ParsingTable buildTable(){
 		tokens.add(new Token("$"));
 		ParsingTable table = new ParsingTable(tokens.size(),nonTerminals.size(),tokens,nonTerminals, idMap);
 		for(NonTerminal nt : nonTerminals){
 			for(Rule r : nt.getRules()){
 				Symbol s =r.getRule().get(0);
 				if(s instanceof Token){
 					if(s.getText().equals("<epsilon>")) {
 						for(Token t : nt.getFollowSet()){
 							table.addEntry(nt,t, new ProductionRule(nt,r.getRule()));
 						}
 					}
 					else
 						table.addEntry(nt,(Token) s, new ProductionRule(nt,r.getRule()));
 				}
 				else if(s instanceof NonTerminal){
 					NonTerminal temp = null;
 					for(NonTerminal N : nonTerminals){
 						if(N.getText().equals(s.getText())){
 							temp = N;
 						}
 					}
 					for(Token t : temp.getFirstSet()){
 						if(r.getRule() !=  null)
 							table.addEntry(nt, t, new ProductionRule(nt,r.getRule()));
 					}
 				}
 			}
 		}
 
 		return table;
 	}
 
   public static void createFollowSet(Set<NonTerminal> nonTerminals) {
     Map<String, NonTerminal> nonTerminalMap = new HashMap<String, NonTerminal>();
     for (NonTerminal nt : nonTerminals) {
       nonTerminalMap.put(nt.getText(), nt);
     }
 
     NonTerminal start = nonTerminals.iterator().next();
     start.addToFollowSet(new Token("$"));
     boolean changed = false;
     int d = 0;
     do {
       changed = false;
 	    for (NonTerminal a : nonTerminals) {
 	      for (Rule r : a.getRules()) {
 	        List<Symbol> xs = r.getRule();
 	        for (int i = 0; i < xs.size(); i++) {
 	          String xiName = xs.get(i).getText();
 	          if (nonTerminalMap.containsKey(xiName)) {
 	            Set<Token> newStuff = new HashSet<Token>();
 	            for (int j = i + 1; j < xs.size(); j++) {
 	              String xj = xs.get(j).getText();
 	              if (nonTerminalMap.containsKey(xj)) {
 	                newStuff.addAll(nonTerminalMap.get(xj).getFirstSet());
 	              } else {
 	                newStuff.add(new Token(xj));
 	              }
 	            }
 	            boolean hasEpsilon = newStuff.remove(new Token("<epsilon>"));
 	            NonTerminal xi = nonTerminalMap.get(xiName);
 	            if (i == xs.size() - 1) {
 	                xi.followSet.addAll(a.followSet);
 	            }
 	            int oldSize = xi.followSet.size();
 	            xi.followSet.addAll(newStuff);
 	            if (xi.followSet.size() > oldSize) {
 	              changed = true;
 	            }
 	            oldSize = xi.followSet.size();
 	            if (hasEpsilon) {
 	              xi.followSet.addAll(a.followSet);
 	              if (xi.followSet.size() > oldSize) {
 	                changed = true;
 	              }
 	            }
 	          }
 	        }
 	      }
 	    }
 
     } while (changed);
   }
 
   public boolean walkTable() {
     List<String> tokens = ScannerGenerator.tokens;
     List<String> ids = ScannerGenerator.ids;
 
     ParsingTable pt = buildTable();
     pt.printTable();
     Map<String, NonTerminal> ntMap = new HashMap<String, NonTerminal>();
     for (NonTerminal nt : nonTerminals) {
       ntMap.put(nt.getText(), nt);
     }
 
     Stack<Symbol> parsingStack = new Stack<Symbol>();
     Stack<Token> inputStack = new Stack<Token>();
     inputStack.push(new Token("$"));
     for (int i = ids.size() - 1; i >= 0; i--) {
       inputStack.push(new Token(ids.get(i)));
     }
 
     parsingStack.push(new Symbol("$"));
     parsingStack.push(nonTerminals.iterator().next());
 
     while (true) {
 
       System.out.println(parsingStack + "  " + inputStack);
       Symbol parseTop = parsingStack.peek();
       Token inputTop = inputStack.peek();
       if (inputTop.equals(parseTop) && parseTop.equals(new Symbol("$"))) {
         System.out.println("Accept");
         return true;
       }
 
       if (parseTop instanceof NonTerminal) {
         NonTerminal ptnt = (NonTerminal) parseTop;
         System.out.printf("Indexing at: [%s][%s]\n", ptnt.toString(), inputTop.toString());
        ProductionRule pr = pt.get(ptnt, inputTop);
         parsingStack.pop();
         List<Symbol> rules = pr.getRule();
         for (int i = rules.size() - 1; i >= 0; i--) {
           if (!rules.get(i).getText().equals("<epsilon>")) {
             parsingStack.push(rules.get(i));
           }
         }
       } else {
         Token other = null;
         if (idMap.containsKey(inputTop.getText())) {
           other = idMap.get(inputTop.getText());
         }
         if (parseTop.getText().equalsIgnoreCase(inputTop.getText()) || (other != null && parseTop.getText().equalsIgnoreCase(other.getText()))) {
           System.out.println("Match: "+ parseTop);
           parsingStack.pop();
           inputStack.pop();
         } else {
           System.out.printf("Error: expected: %s, but got: %s\n", parseTop.getText(), inputTop.getText());
           return false;
         }
       }
 
     }
   }
 
 	public static void main(String[] args) {
 	  if (args.length < 3) {
 	    System.err.println("Improper arguments. Usage <grammar-file> <spec-file> <script-file>");
 	    System.exit(1);
 	  }
 		Parser p = new Parser(args[0], args[1], args[2]);
 		p.walkTable();
 
 
 
 
 	}
 }
