 package tree;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import error.ErrorManager;
 
 import output.OutputManager;
 
 public abstract class Node {
 	
     protected String data;
 	private List<Node> children = new ArrayList<Node>();
     private HashMap<String, String> variables = new HashMap<String, String>();
     private Node parent;
     private int line;
     
     public static final String[] primitives = {"int", "double", "char", "float", "boolean", "long", "short", "byte"};
     
     public static final int INT = 1;
     public static final int DOUBLE = 2;
     public static final int CHAR = 3;
     public static final int FLOAT = 4;
     public static final int BOOLEAN = 5;
     public static final int LONG = 6;
     public static final int SHORT = 7; 
     public static final int BYTE = 8; 
     public static final int VOID = 9;
     public static final int STRING = 10; 
     public static final int OBJECT = 11; 
 
     public List<Node> getChildren() {
     	return this.children;
     }
     public int numChildren() {
     	return this.children.size();
     }

    public void setParent(Node parent) {
	this.parent = parent;
    }
     
     public void addChild(Node n) {
     	this.children.add(n);
     	n.parent = this;
     }
     
     public String getData() {
     	return this.data;
     }
     
     public Node getParent() {
     	return parent;
     }
     
     public void setLine (int line) {
     	this.line = line;
     }
     
     public int getLine () {
     	return this.line;
     }
     
     public void addVariable (String name, String type) {
     	if (variables.get(name) != null) 
     		ErrorManager.nonFatal(this.getLine(), name + " already defined");
     	variables.put(name, type);
     }
     
     public void checkScope (String name) {
     	if (variables.get(name) != null)
     		return;
     	if (this.getParent() == null) {
     		ErrorManager.nonFatal(this.getLine(), name + " undefined");
     		return;
     	}
     	this.getParent().checkScope(name);
     }
     
     public String getType (String name) {
     	String type = variables.get(name);
     	if (type != null)
     		return type;
     	if (this.parent == null)
     		//TODO check resources?
     		return null;
     	return this.parent.getType(name);
     }
     
     public abstract void traverse(int depth, OutputManager out);
 
     public abstract void evaluatePre(int depth, OutputManager out);
     
     public abstract void evaluatePost(int depth, OutputManager out);
     
     public abstract void analyzePre();
     
     public abstract void analyzePost();
     
     protected static void indent(OutputManager out, int depth) {
 		for(int i=0; i<depth; i++)
 			out.write(" ");
 	}
     
     public static boolean isPrimitive (String type) {
     	for (String s : primitives) {
     		if (s.equals(type)) {
     			return true;
     		}
     	}
     	return false;
     }
 }
