 /**
  * http://leetcode.com/onlinejudge#question_114
  * 
  * @author fanzhang
  * 
  */
 public class FlattenBinaryTree {
 	public void flatten(TreeNode root) {
 		// Start typing your Java solution below
 		// DO NOT write main() function
 		traverse(root);
 	}
 
 	private TreeNode traverse(TreeNode root) {
 		if (root == null) {
 			return null;
 		} else if (root.left == null && root.right == null) {
 			return root;
 		}
 		TreeNode left = root.left;
 		TreeNode right = root.right;
 		root.left = null;
 		if (left != null) {
 			root.right = left;
 		}
 		TreeNode leftTail = traverse(left);
 		TreeNode rightTail = traverse(right);

 		if (leftTail != null) {
 			leftTail.right = right;
 		}
		if (rightTail != null) {
			return rightTail;
		} else {
			return leftTail;
		}
 	}
 }
