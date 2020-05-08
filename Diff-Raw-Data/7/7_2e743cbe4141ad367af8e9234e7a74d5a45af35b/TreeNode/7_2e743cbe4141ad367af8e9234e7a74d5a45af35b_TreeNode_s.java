 package compiler.core.parser.tree;
 
 
 import compiler.core.parser.Nonterminal;
 import compiler.core.parser.tree.asm.CodeGenerator;
 import compiler.core.parser.tree.asm.GenerationRule;
 import compiler.core.scanner.finiteautomaton.Token;
 
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  */
 //Represents node of tree with parent and children nodes
 public class TreeNode {
 
     private Object nodeValue;   //The value of node if it's exists
 
     private LinkedList<TreeNode> childrenNodes; //List of children nodes
 
     private TreeNode parentNode = null; //Parent node
 
     private int rule = -1;  //The rule what was used to create the node
 
     private CodeGenerator codeGenerator;    //The instance of code generator
 
     public TreeNode(Object nodeValue, int rule) {
         this.nodeValue = nodeValue;
         this.rule = rule;
         childrenNodes = new LinkedList<TreeNode>();
         codeGenerator = new CodeGenerator();
     }
 
     //Returns true if node has at least one child
     public boolean hasChildren() {
         return !childrenNodes.isEmpty();
     }
 
     //Returns true if children nodes of current node don't have children
     public boolean isCompressable() {
         for (TreeNode child : childrenNodes) {
             if (child.hasChildren()) {
                 return false;
             }
         }
         return true;
     }
 
     //Removes all children nodes
     public void removeChildrenNodes() {
         for (TreeNode child : childrenNodes) {
             child.removeParent();
         }
         childrenNodes.clear();
     }
 
     //Removes parent of current node
     public void removeParent() {
         parentNode.childrenNodes.remove(this);
         parentNode = null;
     }
 
     //Adds the child to the node
     public void addChildNode(TreeNode childNode) {
         childNode.parentNode = this;
         childrenNodes.addFirst(childNode);
     }
 
     //Sets the parent node for the current node
     public void setParentNode(TreeNode parentNode) {
         parentNode.addChildNode(this);
     }
 
     public TreeNode getParentNode() {
         return parentNode;
     }
 
     public Object getNodeValue() {
         return nodeValue;
     }
 
     public void setNodeValue(Object nodeValue) {
         this.nodeValue = nodeValue;
     }
 
     public LinkedList<TreeNode> getChildrenNodes() {
         return childrenNodes;
     }
 
     public void setChildrenNodes(LinkedList<TreeNode> childrenNodes) {
         this.childrenNodes = childrenNodes;
     }
 
     public int getRule() {
         return rule;
     }
 
     //Returns the number of the whole tree under the current node
     public int getTreeNodes() {
         int sum = 0;
         for (TreeNode child : childrenNodes) {
             sum += child.getTreeNodes();
         }
         sum++;
         return sum;
     }
 
     //Returns the text representation of the tree
     public String getTreeText() {
         String a = "";
        a = a.concat(nodeValue.toString() + " " + rule + "\n");
         for (TreeNode child : childrenNodes) {
             a = a.concat("-" + child.getBranchText("|"));
         }
         return a;
     }
 
     //Returns the text representation of the branch of the tree
     private String getBranchText(String s) {
         String a = "";
        a = a.concat(nodeValue.toString() + " " + rule + "\n");
         for (TreeNode child : childrenNodes) {
             a = a.concat(s + "-" + child.getBranchText("|" + s));
         }
         return a;
     }
 
     //Method for code generating
     public StringBuilder generateCode() {
         StringBuilder returnCode = new StringBuilder(); //Generated code
         if (!isCompressable()) {    //If node is not compressable then try to compress all the children nodes recursively
             for (TreeNode child : childrenNodes) {
                 returnCode.append(child.generateCode());
             }
         }
         if (isCompressable()) { //Compress node and generate the code
             if (childrenNodes.size() == 1) {    //Compress node if it has one child (child node simply replaces the parent)
                 this.nodeValue = childrenNodes.getFirst().nodeValue;
                 removeChildrenNodes();
             }
             if (childrenNodes.size() > 1) { //Compress node if it has more then one child (generate code appropriately to children contain)
                System.out.println("there must be computing");
                 List<Token> commandTokens = new LinkedList<Token>();    //Choose only Tokens from children, delete all meaningless Nonterminals
                 for (TreeNode node : childrenNodes) {
                     if (node.nodeValue.getClass() == Token.class) {
                         commandTokens.add((Token) node.nodeValue);
                     }
                     node.parentNode = null;
                 }
                 if (commandTokens.size() == 1) {    //Compress node if it has one usefull child
                     this.nodeValue = commandTokens.get(0);
                 } else {    //Generate appropriate code using code generator
                     GenerationRule generationRule = codeGenerator.generateOperationCode(commandTokens);
                     returnCode.append(generationRule.getGeneratedCode());
                     this.nodeValue = generationRule.getCompressIntoToken();
                 }
             }
             if (childrenNodes.isEmpty() && nodeValue.getClass() == Nonterminal.class) { //If no children -> remove node from parent's children list
                 removeParent();
             }
             childrenNodes.clear();
         }
         return returnCode;
     }
 
 }
