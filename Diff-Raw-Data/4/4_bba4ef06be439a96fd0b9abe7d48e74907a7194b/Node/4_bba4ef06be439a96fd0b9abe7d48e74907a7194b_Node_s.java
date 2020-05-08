 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.List;
 import java.util.ArrayList;
 
 /**
  * A box representing a block I guess.
  */
 public class Node {
    private static int labelCount = 0;
 
    protected ArrayList<Instruction> instructions;
    protected ArrayList<Node> parents;
    protected ArrayList<Node> children;
    private String label;
 
    public void addParent(Node parent) { parents.add(parent); }
    public void addChild(Node child) { children.add(child); }
 
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
 
    public Node() { 
       instructions = new ArrayList<Instruction>(); 
       parents = new ArrayList<Node>(); 
       children = new ArrayList<Node>();
    }
 
    public Node(String label) { 
       this();
       setLabel(nextLabel(label)); // label
    }
 
    public static String nextLabel() {
       return nextLabel("");
    }
 
    public static String nextLabel(String id) {
       return "." + id + labelCount++;
    }
 
    public String toString() {
       String ret = getLabel();
 
      ret += children;
      /*
       for (Node n : children) {
          ret += children;
       }
      */
 
       return ret;
    }
 }
