 /*
  * Copyright (C) 2013 Tim Vaughan <tgvaughan@gmail.com>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package jphylopaint;
 
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.EnumMap;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import joptsimple.internal.Strings;
 
 /**
  * Class of graph objects initialised from extended Newick strings.
  *
  * @author Tim Vaughan <tgvaughan@gmail.com>
  */
 public class NewickGraph extends Graph {
     
     String newickString;
     boolean debug;
     
 
     
     /**
      * Initialise an inheritance graph from its extended Newick representation.
      * @param newickStr extended Newick representation of graph
      */
     public NewickGraph (String newickStr) throws ParseException {
         super();
         
         this.newickString = newickStr;
         
         this.debug = true;
         doLex();
         doParse();
         doTidy();
     }
     
     // String and token index:
     private int idx;
     
     // Enum describing type of individual tokens
     private enum Token {
         LPAREN, RPAREN, COLON, NUM, LABEL, HASH,
         OPENA, CLOSEA, EQUALS, OPENV, CLOSEV,
         COMMA, SEMI, SPACE
     }
     
     // Lists of tokens and their values appearing in input string
     List<Token> tokenList;
     List<String> valueList;
     
     /**
      * Perform lexical analysis of newickStr.
      */
     private void doLex() throws ParseException {
         
         Map <Token, Pattern> patterns = new EnumMap<Token,Pattern>(Token.class);
         patterns.put(Token.LPAREN, Pattern.compile("\\("));
         patterns.put(Token.RPAREN, Pattern.compile("\\)"));
         patterns.put(Token.COLON, Pattern.compile(":"));
         patterns.put(Token.NUM, Pattern.compile("\\d+(\\.\\d+)?([eE]-?\\d+)?"));
         patterns.put(Token.LABEL, Pattern.compile("[a-zA-Z0-9_]+"));
         patterns.put(Token.HASH, Pattern.compile("#"));
         patterns.put(Token.OPENA, Pattern.compile("\\[&"));
         patterns.put(Token.CLOSEA, Pattern.compile("\\]"));
         patterns.put(Token.EQUALS, Pattern.compile("="));
         patterns.put(Token.OPENV, Pattern.compile("\\{"));
         patterns.put(Token.CLOSEV, Pattern.compile("}"));
         patterns.put(Token.COMMA, Pattern.compile(","));
         patterns.put(Token.SEMI, Pattern.compile(";"));
         patterns.put(Token.SPACE, Pattern.compile("\\s+"));
         
         List<Token> valueTokens = new ArrayList<Token>();
         valueTokens.add(Token.NUM);
         valueTokens.add(Token.LABEL);
         
         tokenList = new ArrayList<Token>();
         valueList = new ArrayList<String>();
 
         idx=0;
         
         while (idx<newickString.length()) {
             
             boolean noMatch = true;
             
             for (Map.Entry<Token, Pattern> entry : patterns.entrySet()) {
                 
                 Matcher matcher = entry.getValue().matcher(newickString.substring(idx));
                 if (matcher.find() && matcher.start()==0) {
                     noMatch = false;
                     idx += matcher.group().length();
                     
                     // Discard whitespace:
                     if (entry.getKey() == Token.SPACE)
                         break;
                     
                     tokenList.add(entry.getKey());
                     if (valueTokens.contains(entry.getKey()))
                         valueList.add(matcher.group());
                     else
                         valueList.add("");
                     
                     if (debug)
                         System.out.format("%d: %s '%s'\n", idx,
                                 entry.getKey(), matcher.group());
                     break;
                 }
             }
             if (noMatch) {
                 throw new ParseException(newickString, idx);
             }
         }
     }
     
 
     /**
      * Map from label onto list of hybrid nodes to which that label corresponds.
      */
     Map<String, List<Node>> hybrids;
     
     /**
      * Recursive decent parser.
      * 
      * @throws ParseException 
      */
     private void doParse() throws ParseException {
         hybrids = new HashMap<String,List<Node>>();
         idx = 0;
         ruleG();
     }
     
     /**
      * Indent index.
      */
     private int indent;
     
     /**
      * Produce indent specified by indent index.
      */
     private void indentOut() {
         System.out.print(Strings.repeat(' ', indent));
     }
     
     /**
      * Test whether token at current index matches token supplied.
      * @param token
      * @return true for a match
      */
     private boolean acceptToken(Token token, boolean manditory) throws ParseException {
         if (tokenList.get(idx) == token) {
             idx += 1;
             return true;
         } else {
             if (manditory)
                 throw new ParseException(newickString, idx);
             else
                 return false;
         }
     }
     
     /**
      * G -> NZ;
      */
     private void ruleG() throws ParseException {
         
         if (debug)
             indent = 0;
         
         rootNodes.add(ruleN(null));
         rootNodes.addAll(ruleZ(null));
         acceptToken(Token.SEMI, true);
     }
     
     /**
      * Z -> NZ|eps
      * @return List of nodes 
      */
     private List<Node> ruleZ(Node parent) throws ParseException {
         List<Node> siblings = new ArrayList<Node>();
         if (acceptToken(Token.COMMA, false)) {
             siblings.add(ruleN(parent));
             siblings.addAll(ruleZ(parent));
         }
         return siblings;
     }
     
     /**
      * N -> SLHAB
      * @param parent
      * @return Node object
      */
     private Node ruleN(Node parent) throws ParseException {
         
         if (debug)
             indentOut();
         
         Node node = new Node();
         if (parent != null)
             node.addParent(parent);
         
         ruleS(node);
         ruleL(node);
         ruleH(node);
         ruleA(node);
         ruleB(node);
         
         if (debug)
             System.out.println();
         
         return node;
     }
     
     /**
      * S -> (NZ)|eps
      * @param node 
      */
     private void ruleS(Node node) throws ParseException {
         if (acceptToken(Token.LPAREN, false)) {
             if (debug) {
                 System.out.println("(");
                 indent += 1;
             }
             
             ruleN(node);
             ruleZ(node);
             
             acceptToken(Token.RPAREN, true);
             
             if (debug) {
                 indent -= 1;
                 indentOut();
                 System.out.print(")");
             }
         }
     }
     
     /**
      * L -> LABEL|NUM|eps
      * @param node 
      */
     private void ruleL(Node node) throws ParseException {
         if (acceptToken(Token.LABEL, false)
                 || acceptToken(Token.NUM, false)) {
             if (debug)
                System.out.print(" Lab:" + valueList.get(idx));
             
             node.setLabel(valueList.get(idx));
         }
     }
     
     /**
      * H -> #LABEL|#NUM|eps
      * @param node 
      */
     private void ruleH(Node node) throws ParseException {
         if (acceptToken(Token.HASH, false)) {
             if (!(acceptToken(Token.LABEL, false)
                     || acceptToken(Token.NUM, false)))
                 throw new ParseException(newickString, idx);
             
             String hlabel = valueList.get(idx-1);
             if (!hybrids.containsKey(hlabel))
                 hybrids.put(hlabel, new ArrayList<Node>());
             hybrids.get(hlabel).add(node);
             
             if (debug)
                System.out.print(" Hybrid:" + valueList.get(idx));
         }
     }
     
     /**
      * A -> [&CD]|eps
      * @param node 
      */
     private void ruleA(Node node) throws ParseException {
         if (acceptToken(Token.OPENA, false)) {
             ruleC(node);
             ruleD(node);
             acceptToken(Token.CLOSEA, true);
         }
     }
 
     /**
      * C -> LABEL=V
      * @param node
      * @throws ParseException 
      */
     private void ruleC(Node node) throws ParseException {
         acceptToken(Token.LABEL, true);
         
         String key = valueList.get(idx-1);
         
         acceptToken(Token.EQUALS, true);
         
         Object value = ruleV();
         
         node.annotate(key, value);
         
         if (debug) {
             System.out.print(" Annot:" + key + "=");
             if (value instanceof List) {
                 System.out.print("{");
                 boolean first = true;
                 for (String el : (List<String>)value) {
                     if (!first) {
                         System.out.print(",");
                     } else
                         first = false;
                     
                     System.out.print(el);
                 }
             } else
                 System.out.print((String)value);
         }
     }
     
     /**
      * D -> CD|eps
      * @param node
      * @throws ParseException 
      */
     private void ruleD(Node node) throws ParseException {
         if (acceptToken(Token.COMMA, false)) {
             ruleC(node);
             ruleD(node);
         }
     }
     
     /**
      * V -> LABEL|NUM|{NUM Q}
      * @return
      * @throws ParseException 
      */
     private Object ruleV() throws ParseException {
         if (acceptToken(Token.LABEL, false)
                 || acceptToken(Token.NUM, false)) {
             return valueList.get(idx-1);
         } else {
             acceptToken(Token.OPENV, true);
             acceptToken(Token.NUM, true);
             List<String> valueVec = new ArrayList<String>();
             valueVec.add(valueList.get(idx-1));
             ruleQ(valueVec);
             return valueVec;
         }
     }
     
     /**
      * Q -> ,NUM Q | eps
      * @param valueVec 
      */
     private void ruleQ(List<String> valueVec) throws ParseException {
         if (acceptToken(Token.COMMA, false)) {
             acceptToken(Token.NUM, true);
             valueVec.add(valueList.get(idx-1));
             ruleQ(valueVec);
         }
     }
     
     /**
      * B -> :NUM | eps
      * @param node
      * @throws ParseException 
      */
     private void ruleB(Node node) throws ParseException {
         if (acceptToken(Token.COLON, false)) {
             acceptToken(Token.NUM, true);
             node.setBranchLength(Double.valueOf(valueList.get(idx-1)));
             
             if (debug)
                 System.out.print(" Blength:" + Double.valueOf(valueList.get(idx-1)));
         }
     }
     
     /**
      * Post-parse tidy.
      * @throws ParseException 
      */
     private void doTidy() throws ParseException {
         
         /*
          * Calculate absolute times of nodes.
          */
         
         if (rootNodes.size()>1) {
             for (Node root : rootNodes) {
                 if (!root.getAnnotations().containsKey("height"))
                     throw new ParseException("Graphs with multiple roots "
                             + "require height annotation.", 0);
                 else {
                     root.setTime(Double.valueOf((String)root.getAnnotations().get("height")));
                     getTimesRecurse(root);
                 }
             }
         } else {
             Node root = rootNodes.get(0);
             root.setTime(0.0);
             getTimesRecurse(root);
         }
         
         /*
          * Merge hybrid nodes to form network.
          */
         
         for (String hybridLabel : hybrids.keySet()) {
             
             // Find primary node:
             Node primaryNode = null;
             for (Node hybrid : hybrids.get(hybridLabel)) {
                 if (primaryNode == null || hybrid.getChildren().size()>0)
                     primaryNode = hybrid;
             }
             
             // Replace all non-primary nodes with primary node:
             for (Node hybrid : hybrids.get(hybridLabel)) {
                 if (hybrid == primaryNode)
                     continue;
                 
                 hybrid.getParents().get(0).getChildren().remove(hybrid);
                 hybrid.getParents().get(0).addChild(primaryNode);
             }
         }
     }
     
     /**
      * Recursive traversal of subtree under node to calculate absolute node
      * times.
      * 
      * @param node specifies subtree.
      */
     private void getTimesRecurse(Node node) {
 
         if (!node.isRoot())
             node.setTime(node.getParents().get(0).getTime()+node.getBranchLength());
         
         for (Node child : node.getChildren())
             getTimesRecurse(child);
     }
     
 }
