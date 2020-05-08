 /*
  * Given a binary tree, find the maximum path sum.
 
 The path may start and end at any node in the tree.
 
 For example:
 Given the below binary tree,
 
        1
       / \
      2   3
 Return 6.
  * 
  */
 public class BinaryTreeMaxPath {
     public class TreeNode {
         private int val;
         private TreeNode left;
         private TreeNode right;
         TreeNode(int x) { val = x; }
     }
     
     public class TreeVal {
         private int self;
         private int export;
         TreeVal() {
             self = Integer.MIN_VALUE;
             export = 0;
         }
     }
     
     public int getMaxSub(TreeVal left, int root, TreeVal right) {
         int c1 = Math.max(left.self, right.self);
         int c2 = root;
         if (left.export > 0)
             c2 += left.export;
         if (right.export > 0)
             c2 += right.export;
         int out = Math.max(c1, c2);
         return out;
     }
     
     public TreeVal getTreeVal(TreeNode node) {
         TreeVal out = new TreeVal();
         
         if (node == null)
             return out;
         
         TreeVal left = getTreeVal(node.left);
         TreeVal right = getTreeVal(node.right);
         
         int childExport = Math.max(left.export, right.export);
        out.export = node.val + childExport;
         
         out.self = getMaxSub(left, node.val, right);
         return out;
     }
     
     public int maxPathSum(TreeNode root) {
         
         if (root == null)
             return 0;
         
         TreeVal left = getTreeVal(root.left);
         TreeVal right = getTreeVal(root.right);
         
         return getMaxSub(left, root.val, right);
              
     }
 }
