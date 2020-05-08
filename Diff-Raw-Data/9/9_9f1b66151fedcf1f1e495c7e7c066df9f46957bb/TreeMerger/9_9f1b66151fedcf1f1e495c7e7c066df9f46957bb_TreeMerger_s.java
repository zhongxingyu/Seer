 /*This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; version 2 of the License.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 */
 
package edu.lif;

 import java.util.*;
 import java.io.*;
 
 class TreeMerger {
     public static final int INPUT_SEXP = 1;
     public static final int INPUT_CONLL = 2;
     public static final int OUTPUT_SEXP = 3;
     public static final int OUTPUT_CONLL = 4;
     public static final int OUTPUT_FSM = 5;
 
     public int outputFormat = OUTPUT_SEXP;
     public int inputFormat = INPUT_SEXP;
     public String orSymbol = "-OR-";
     public boolean expandFsm = false;
 
     public void mergeTrees() {
         Vector<Node> trees = new Vector<Node>();
         try {
             BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
             boolean wasNull = false;
             while(true) {
                 Node tree = null;
                 if(inputFormat == INPUT_CONLL) tree = Node.readCoNLL(input);
                 else if(inputFormat == INPUT_SEXP) tree = Node.readSexp(input);
                 if(tree == null) {
                     if(wasNull) break;
                     Node output = trees.get(0);
                     for(int i = 1; i < trees.size(); i++) {
                         Vector<Node> nodes = getDifferences(output, trees.get(i));
                         if(nodes.size() > 0) {
                             Node common = commonParent(nodes);
                             Node merged = mergeAt(output, trees.get(i), common);
                             Node minimized = minimizeTree(merged); 
                             output = minimized;
                         }
                     }
                     if(outputFormat == OUTPUT_CONLL) System.out.println(output.toCoNLL());
                     else if(outputFormat == OUTPUT_SEXP) System.out.println(output.toSexp());
                     else if(outputFormat == OUTPUT_FSM) {
                         nextState = 1;
                         printHyperGraph(output, 0, expandFsm);
                         System.out.println();
                     }
                     trees.clear();
                     wasNull = true;
                 } else {
                     wasNull = false;
                     trees.add(tree);
                 }
             }
             if(trees.size() > 0) {
                 Node output = trees.get(0);
                 for(int i = 1; i < trees.size(); i++) {
                     Vector<Node> nodes = getDifferences(output, trees.get(i));
                     if(nodes.size() > 0) {
                         Node common = commonParent(nodes);
                         Node merged = mergeAt(output, trees.get(i), common);
                         Node minimized = minimizeTree(merged); 
                         output = minimized;
                     }
                 }
                 if(outputFormat == OUTPUT_CONLL) System.out.println(output.toCoNLL());
                 else if(outputFormat == OUTPUT_SEXP) System.out.println(output.toSexp());
                 else if(outputFormat == OUTPUT_FSM) {
                     nextState = 1;
                     printHyperGraph(output, 0, expandFsm);
                     System.out.println();
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
             System.exit(1);
         }
     }
 
     // not used yet
     void setOrNodes(Node tree, String orSymbol) {
         if(tree.label.equals(orSymbol)) tree.isOrNode = true;
         else tree.isOrNode = false;
         for(Node child: tree.children) {
             setOrNodes(child, orSymbol);
         }
     }
 
     Vector<Node> expandOrChildren(Node tree) {
         Vector<Node> output = new Vector<Node>();
         for(Node child: tree.children) {
             if(child.isOrNode) output.addAll(expandOrChildren(child));
             else output.add(child);
         }
         return output;
     }
 
     public Node minimizeTree(Node tree) {
         if(tree.isOrNode) {
             // all directly accessible OR-nodes should be merged with this one
             Vector<Node> children = expandOrChildren(tree);
             Vector<Node> output = new Vector<Node>();
             for(int i = 0; i < children.size(); i++) {
                 boolean keep = true;
                 for(int j = 0; j < children.size(); j++) {
                     if(i != j && isRedundant(children.get(j), children.get(i))) {
                         // if red(a,b) and red(b,a): we have to keep one!
                         if(!(i < j && isRedundant(children.get(i), children.get(j)))) {
                             keep = false;
                             break;
                         }
                     }
                 }
                 if(keep) output.add(children.get(i));
             }
             tree.children = output;
             if(tree.children.size() == 1) return minimizeTree(tree.children.firstElement());
         }
         for(int i = 0; i < tree.children.size(); i++) {
             Node node = minimizeTree(tree.children.get(i));
             node.parent = tree;
             tree.children.set(i, node);
         }
         return tree;
     }
 
     public boolean isRedundant(Node tree1, Node tree2) { // tree2 is redundant given tree1 
         if(tree1.isOrNode && tree2.isOrNode) {
             for(Node child: tree2.children) {
                 if(!isRedundant(tree1, child)) return false;
             }
             return true;
         } else if(tree2.isOrNode) {
             return false; // tree1 should be a or-node
         } else if(tree1.isOrNode) {
             for(Node child: tree1.children) {
                 if(isRedundant(child, tree2)) return true;
             }
             return false;
         } else {
             if(!(tree1.label.equals(tree2.label) && tree1.children.size() == tree2.children.size())) return false;
             for(int i = 0; i < tree1.children.size(); i++) {
                 if(!isRedundant(tree1.children.get(i), tree2.children.get(i))) return false;
             }
             return true;
         }
     }
 
     public Node mergeAt(Node tree1, Node tree2, Node mergePointFromTree1) {
         if(tree1 == mergePointFromTree1) {
             Node output = new Node();
             output.isOrNode = true;
             output.label = orSymbol;
             output.children.add(tree1);
             output.children.add(tree2);
             return output;
         } else {
             if(tree1.isOrNode) {
                 for(int i = 0; i < tree1.children.size(); i++) {
                     Node node = mergeAt(tree1.children.get(i), tree2, mergePointFromTree1);
                     node.parent = tree1;
                     tree1.children.set(i, node);
                 }
             } else {
                 if(!(tree1.label.equals(tree2.label) && tree1.children.size() == tree2.children.size())) return tree1; // cut mismatches
                 for(int i = 0; i < tree1.children.size(); i++) {
                     Node node = mergeAt(tree1.children.get(i), tree2.children.get(i), mergePointFromTree1);
                     node.parent = tree1;
                     tree1.children.set(i, node);
                 }
             }
             return tree1;
         }
     }
 
     public Node commonParent(Vector<Node> nodes) {
         Node node = nodes.firstElement();
         for(int i = 1; i < nodes.size(); i++) {
             Node peer = nodes.get(i);
             Node parent1 = node;
             while(parent1 != null) {
                 Node parent2 = peer;
                 while(parent1 != parent2 && parent2 != null) {
                     parent2 = parent2.parent;
                 }
                 if(parent1 == parent2) break;
                 parent1 = parent1.parent;
             }
             node = parent1;
         }
         return node;
     }
 
     public Vector<Node> getDifferences(Node tree1, Node tree2) {
         Vector<Node> output = new Vector<Node>();
         if(tree1.isOrNode) {
             Vector<Node> argmin = null;
             int min = 0;
             for(Node child: tree1.children) {
                 Vector<Node> result = getDifferences(child, tree2);
                 int size = 0;
                 for(Node node: result) size += node.size();
                 if(argmin == null || size < min) {
                     argmin = result;
                     min = size;
                 }
             }
             output.addAll(argmin);
             return output;
         } else if(tree1.children.size() != tree2.children.size() || (tree1.label != null && !tree1.label.equals(tree2.label))) {
             output.add(tree1);
             return output;
         } else {
             for(int i = 0; i < tree1.children.size(); i++) {
                 output.addAll(getDifferences(tree1.children.get(i), tree2.children.get(i)));
             }
         }
         return output;
     }
 
     public int nextState = 1;
     public HashMap<String, Integer> stateId = new HashMap<String, Integer>();
     public void printHyperGraph(Node node, int from, boolean expand) {
         int state = 0;
         if(expand || node.children.size() == 0) {
             state = nextState++;
             for(Node child: node.children) {
                 printHyperGraph(child, state, expand);
             }
         } else {
             String key = node.factorRepresentation();
             if(stateId.containsKey(key)) {
                 state = stateId.get(key);
             } else {
                 state = nextState++;
                 stateId.put(key, state);
                 for(Node child: node.children) {
                     printHyperGraph(child, state, expand);
                 }
             }
         }
         System.out.printf("%d %d %s\n", from, state, node.label); 
         //if(node.children.size() == 0) System.out.printf("%d\n", state);
     }
 
     public static void usage() {
         System.err.println("usage: java TreeMerger [-i <input-format>] [-o <output-format>] [-s <or-symbol>] [-e]");
         System.err.println("   -i|input (sexp|conll)     set input format, defaults to s-expression");
         System.err.println("   -o|output (sexp|fsm)      set output format, defaults to s-expression");
         System.err.println("   -s|symbol <symbol>        symbol used to annotate or-nodes, defaults to -OR-");
         System.err.println("   -e|expand                 expand fsm by not factorizing subtrees");
         System.exit(1);
     }
     public static void main(String args[]) {
         TreeMerger merger = new TreeMerger();
         for(int i = 0; i < args.length; i++) {
             if(args[i].equals("-h") || args[i].equals("-help") || args[i].equals("--help")) {
                 usage();
             } else if(args[i].equals("-i") || args[i].equals("-input") || args[i].equals("--input")) {
                 if(i == args.length - 1) usage();
                 String format = args[++i];
                 if(format.equals("sexp")) merger.inputFormat = INPUT_SEXP;
                 else if(format.equals("conll")) merger.inputFormat = INPUT_CONLL;
             } else if(args[i].equals("-o") || args[i].equals("-output") || args[i].equals("--output")) {
                 if(i == args.length - 1) usage();
                 String format = args[++i];
                 if(format.equals("sexp")) merger.outputFormat = OUTPUT_SEXP;
                 else if(format.equals("conll")) merger.outputFormat = OUTPUT_CONLL;
                 else if(format.equals("fsm")) merger.outputFormat = OUTPUT_FSM;
             } else if(args[i].equals("-s") || args[i].equals("-symbol") || args[i].equals("--symbol")) {
                 if(i == args.length - 1) usage();
                 merger.orSymbol = args[++i];
             } else if(args[i].equals("-e") || args[i].equals("-expand") || args[i].equals("--expand")) {
                 merger.expandFsm = true;
             } else {
                 usage();
             }
         }
         merger.mergeTrees();
     }
 }
