 /*This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; version 2 of the License.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 */
 
 import java.util.*;
 import java.io.*;
 
 class Node implements Comparable<Node> {
     Node parent;
     public int id;
     public String label;
     public String originalText;
     public boolean isOrNode;
     public Vector<Node> children;
 
     public Node(int id) {
         this.id = id;
         children = new Vector<Node>();
     }
     public Node() {
         children = new Vector<Node>();
     }
     public Node(Node peer) {
         id = peer.id;
         label = peer.label;
         children = new Vector<Node>();
     }
     public Node(int id, String label) {
         this.id = id;
         this.label = label;
         children = new Vector<Node>();
     }
 
     public void setParent(Node parent) {
         this.parent = parent;
         for(Node child: children) {
             child.setParent(this);
         }
     }
 
     public String toSexp() {
         StringBuffer output = new StringBuffer("(");
         output.append(children.size() == 0 ? label : label.replace(" ", "/"));//.append(":" + id);
         for(Node child: children) {
             output.append(" ");
             output.append(child.toSexp());
         }
         output.append(")");
         return output.toString();
     }
 
     public String toCoNLL() {
         Vector<Node> nodes = collect();
         Vector<Node> orNodes = new Vector<Node>();
         for(Node node: nodes) {
             if(node.isOrNode) {
                 orNodes.add(node);
             }
         }
         int nextId = nodes.size() + 1 - orNodes.size();
         for(Node node: orNodes) {
             node.id = nextId++;
         }
         //Collections.sort(nodes);
         StringBuffer output = new StringBuffer();
         for(Node node: nodes) {
             if(node.isOrNode) {
                 output.append(node.id);
                 for(int i = 0; i < 5; i++) {
                     output.append("\t_");
                 }
                 output.append("\t");
                 if(node.parent != null) output.append(node.parent.id);
                 else output.append("0");
                 output.append("\t");
                 output.append(node.label);
             } else {
                 if(node.originalText != null) node.label = node.originalText;
                 String tokens[] = node.label.split("\t");
                 tokens[0] = "" + node.id;
                 if(node.parent != null) tokens[6] = "" + node.parent.id;
                 else tokens[6] = "0";
                 for(String token: tokens) {
                     output.append(token);
                     output.append("\t");
                 }
             }
             output.append("\n");
         }
         return output.toString();
     }
 
     public int compareTo(Node o) {
         if(((Node)o).id < id) return 1;
         return -1;
     }
 
     public boolean equals(Node o) {
         return ((o.label == null && label == null) || o.label.equals(label)) && o.id == id;
     }
 
     public String factorRepresentation() {
         StringBuffer output = new StringBuffer("");
         for(Node child: children) {
             output.append(child.subTreeRepresentation());
         }
         return output.toString();
     }
 
     public String subTreeRepresentation() {
         StringBuffer output = new StringBuffer("(");
         output.append(label);// + ":" + id);
         for(Node child: children) {
             output.append(child.subTreeRepresentation());
         }
         output.append(")");
         return output.toString();
     }
 
     public Vector<Node> collect() {
         Vector<Node> output = new Vector<Node>();
         output.add(this);
         for(int i = 0; i < children.size(); i++) {
             output.addAll(children.get(i).collect());
         }
         return output;
     }
     public void sortChildren() {
         Collections.sort(children);
         for(Node child: children) {
             child.sortChildren();
         }
     }
     public int size() {
         int size = 1;
         for(Node child: children) {
             size += child.size();
         }
         return size;
     }
     public Node getRoot() {
         if(parent != null) return parent.getRoot();
         return this;
     }
 
     public static Node readCoNLL(BufferedReader reader) throws IOException {
         String line;
         Vector<Node> nodes = new Vector<Node>();
         Vector<Integer> parentId = new Vector<Integer>();
         while(true) {
             line = reader.readLine();
             if(line == null) return null;
             line = line.trim();
             if(line.startsWith("#")) continue;
             if("".equals(line)) break;
             String tokens[] = line.split("\t");
             if(tokens.length < 8) {
                 System.err.println("WARNING: invalid CoNLL format \"" + line + "\"");
                 return null;
             }
             nodes.add(new Node(Integer.parseInt(tokens[0]), tokens[1] + " " + tokens[7]));
             nodes.lastElement().originalText = line;
             parentId.add(new Integer(tokens[6]) - 1);
         }
         Node output = null;
         for(int i = 0; i < nodes.size(); i++) {
             int parent = parentId.get(i);
             if(parent == -1) output = nodes.get(i);
             else {
                 nodes.get(parent).children.add(nodes.get(i));
             }
         }
         if(output == null) {
             return null;
         }
         output.sortChildren();
         output.setParent(null);
         return output;
     }
 
     public static int fromSexp(String line, int from, Node output) {
         StringBuffer label = new StringBuffer();
         if(line.charAt(from) != '(') {
             System.err.println("WARNING: malformed s-exp \"" + line + "\"");
             return -1;
         }
         for(int i = from + 1; i < line.length(); i++) {
             char next = line.charAt(i);
             if(next == '(') {
                 Node child = new Node(i);
                 output.children.add(child);
                 i = fromSexp(line, i, child);
                 if(i == -1) return -1;
             } else if(next == ')') {
                 output.label = label.toString().trim();
                 return i;
             } else {
                 label.append(next);
             }
         }
         System.err.println("WARNING: s-exp not properly closed \"" + line + "\"");
         return -1;
     }
 
     public static Node readSexp(BufferedReader reader) throws IOException {
         String line = null;
         while(true) {
             line = reader.readLine();
             if(line == null) return null;
             line = line.trim();
             if(line.startsWith("#")) continue;
             break;
         }
         if(line.equals("")) return null;
         Node output = new Node(0);
         int result = fromSexp(line, 0, output);
         if(result == -1) return null;
         output.sortChildren();
         output.setParent(null);
         return output;
     }
 
     public boolean isProjective() {
         Vector<Node> nodes = collect();
         for(Node node: nodes) {
             for(Node peer: nodes) {
                 if(node != peer) {
                     int a = node.id < node.parent.id ? node.id : node.parent.id;
                     int b = node.id < node.parent.id ? node.parent.id : node.id;
                     int c = peer.id < peer.parent.id ? peer.id : peer.parent.id;
                     int d = peer.id < peer.parent.id ? peer.parent.id : peer.id;
                     if(a < c && c < b && b < d) return false;
                     if(c < a && a < d && d < b) return false;
                 }
             }
         }
         return true;
     }
 
     public static void main(String args[]) {
         try {
             BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
             Node tree;
             while(null != (tree = Node.readSexp(input))) {
                 System.out.println(tree.toSexp());
             }
         } catch (Exception e) {
             e.printStackTrace();
             System.exit(1);
         }
     }
 }
