 package intermediate;
 
 import java.util.ArrayList;
 
 public class IntermediateCode
 {
     private ArrayList<CodeTree> trees;
     private SymbolTable stable;
     
     public IntermediateCode()
     {
         trees = new ArrayList<CodeTree>();
         stable = new SymbolTable();
     }
     
     public void printTrees()
     {
     	for (CodeTree tree : trees)
     	{
     		System.out.println("\n" + tree.toString(0));
     	}
     	
     }
     
     public void fillSymbolTable()
     {
     	stable.clear();
     	for (CodeTree tree : trees)
     	{
     		stable.analyzeTree(tree);
     	}
     	
     	stable.printSymbolTable();
     }
     
     public void addCodeTree(CodeTree tree)
     {
         trees.add(tree);
     }
     
    public void reset()
    {
    	trees.clear();
    	stable.clear();
    }
     
     public static class CodeTree extends TreePart
     {
         protected TreePart left;
         protected Blank    right;
         
         public CodeTree()
         {
             left = null;
             right = null;
         }
         
         @Override
         public String toString(int indent)
         {
             String s = spaces(indent);
             s = s.concat("(");
             if (left != null)
             {
                 s = s.concat((left instanceof CodeTree ? "\n" : "").concat(left
                                 .toString(indent
                                                 + (left instanceof CodeTree ? INDENT
                                                                 : 0))));
             }
             if (right != null)
             {
                 s = s.concat(right.toString(indent));
             }
             return s;
         }
         
         public TreePart getLeft()
         {
             return left;
         }
         
         public void setLeft(TreePart left)
         {
             this.left = left;
         }
         
         public Blank getRight()
         {
             return right;
         }
         
         public void setRight(Blank right)
         {
             this.right = right;
         }
         
 
         
         public static class Node extends TreePart
         {
             private String value;
             
             public Node(String value)
             {
                 this.value = value;
             }
             
             @Override
             public String toString(int indent)
             {
                 return value;
             }
         }
         
         public static class Blank extends CodeTree
         {
             @Override
             public String toString(int indent)
             {
                 String s = " ";
                 if (left != null)
                 {
                     s = s.concat((left instanceof CodeTree ? "\n" : "").concat(left
                                     .toString(indent
                                                     + (left instanceof CodeTree ? INDENT
                                                                     : 0))));
                 }
                 if (right != null)
                 {
                     s = s.concat(right.toString(indent));
                 }
                 else
                 {
                     s = s.concat(")");
                 }
                 return s;
             }
         }
     }
     
     public static abstract class TreePart
     {
         public static final int INDENT = 4;
         public abstract String toString(int indentation);
         
         public static String spaces(int count)
         {
             String s = "";
             for (int i = 0; i < count; i++)
             {
                 s = s.concat(" ");
             }
             return s;
         }
     }
 }
