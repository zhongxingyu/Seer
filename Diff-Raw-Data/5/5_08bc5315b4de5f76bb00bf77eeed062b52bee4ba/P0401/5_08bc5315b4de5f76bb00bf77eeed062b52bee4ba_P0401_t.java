 /* 4.1 Implement a function to check if a binary tree is balanced. For the 
  * purposes of this question, a balanced tree is defined to be a tree such
  * that the heights of the two subtrees of any node never differ by more than
  * one.
  */
 
 public class P0401 {
    private class Node {
       int value;
       Node left, right;
 
       public Node(int value) {
          this.value = value;
       }
    }
 
    public boolean isBalanced(Node root) {
       if (root == null) return true;
 
       int leftHeight = height(root.left);
       int rightHeight = height(root.right);
 
       boolean isRootBalanced = Math.abs(leftHeight - rightHeight) <= 1;
 
       if (isRootBalanced)
          return isBalanced(root.left) && isBalanced(root.right);
       else
          return false;
 
       // return isRootBalanced && isBalanced(root.left) && isBalanced(root.right);
    }
 
    public int height(Node x) {
       if (x == null) return 0;
 
       return 1 + Math.max(height(x.left), height(x.right));
    }
 
    // an improved solution:
    // every time we calculate height, the heights of subtrees (subtrees of 
    // subtrees) are repeatedly calculated, we coudl avaoid this redundancy by
    // adding extra variables to record the heights of subtrees at each level.
    public boolean isBalanced2(Node root, int[] height) {
       if (root == null) {
          height[0] = 0;
          return true;
       }
 
 
       int[] leftHeight = new int[1];
       int[] rightHeight = new int[1];
 
       boolean isLeftBalanced = isBalanced2(root.left, leftHeight);
 
       if (isLeftBalanced) {
          boolean isRightBalanced = isBalanced2(root.right, rightHeight);
 
          if (isRightBalanced) {
             if (Math.abs(leftHeight[0] - rightHeight[0]) <= 1) {
                int tmp = (leftHeight[0] > rightHeight[0]) ? leftHeight[0] : rightHeight[0];
                height[0] = height[0] + tmp + 1;
                System.out.println(height[0]);
                return true;
             }
          }
      }
 
      return false;
    }
 
    public Node buildTree() {
       Node root = new Node(1);
       root.left = new Node(2);
       root.right = new Node(3);
       root.left.left = new Node(4);
       root.left.right = new Node(5);
       root.right.left = new Node(6);
       root.left.left.left = new Node(7);
      //root.left.left.left.left = new Node(8);
 
       return root;
    }
 
    public static void main(String[] args) {
       P0401 p0401 = new P0401();
       Node root = p0401.buildTree();
       int[] height = new int[1];
       System.out.println(p0401.isBalanced2(root, height));
       System.out.println(p0401.isBalanced(root));
    }
 }
