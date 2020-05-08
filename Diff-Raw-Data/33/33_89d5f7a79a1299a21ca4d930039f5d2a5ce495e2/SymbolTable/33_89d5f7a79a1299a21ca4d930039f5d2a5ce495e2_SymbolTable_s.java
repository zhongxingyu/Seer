 package intermediate;
 
 import intermediate.IntermediateCode.CodeTree;
 
 import java.util.TreeMap;
 
 public class SymbolTable
 {
     private TreeMap<String, ParseTree> symbols;
     
     public SymbolTable()
     {
         symbols = new TreeMap<String, ParseTree>();
     }
     
     public TreeMap<String, ParseTree> getSymbols()
     {
         return symbols;
     }
     
     // string gymnastics, as traversing tree is a pain
     public void analyzeTree(CodeTree tree)
     {
         String[] noformattree = tree.toString(0).split("\n");
         for (int i = 0; i < noformattree.length; i++)
         {
         	noformattree[i] = noformattree[i].trim();
         	String[] words = noformattree[i].split(" ");
         	for (int j = 0; j < words.length; j++)
         	{
         		words[j] = words[j].replace(')', '\0');
         		words[j] = words[j].replace('(', '\0');
         		words[j] = words[j].trim();
         		
         		//exceptions
        		if (words[j].equals("") || words[j].equals("'") || words[j].contains("0"))
         			continue;
         		
         		if (!symbols.containsKey(words[j]))
         		{
         			symbols.put(words[j], null); //null attributes fine according to spec
         		}
         	}
         }
     }
     
     public void printSymbolTable()
     {
     	System.out.println("\nSymbol Table: " + symbols.toString());
     }
     
     public void clear()
     {
     	symbols.clear();
     }
 
     
     /**
      * to be used in symbol table for attributes
      */
     public static class ParseTree
     {
         public ParseTree()
         {
             left = null;
             right = null;
         }
         
         public ParseTree getLeft()
         {
             return left;
         }
         
         public void setLeft(ParseTree left)
         {
             this.left = left;
         }
         
         public ParseTree getRight()
         {
             return right;
         }
         
         public void setRight(ParseTree right)
         {
             this.right = right;
         }
         
         private ParseTree left;
         private ParseTree right;
         
         public static class Leaf extends ParseTree
         {
             
         }
     }
 }
