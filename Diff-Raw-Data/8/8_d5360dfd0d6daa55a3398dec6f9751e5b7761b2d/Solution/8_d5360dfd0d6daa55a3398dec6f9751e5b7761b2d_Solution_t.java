 /**
  * Definition for binary tree
  * public class TreeNode {
  *     int val;
  *     TreeNode left;
  *     TreeNode right;
  *     TreeNode(int x) { val = x; }
  * }
  */
 public class Solution {
     class MetaData{
         int maxPathSum;
         int maxConnectPath;
         public MetaData(int maxPathSum, int maxConnectPath){
             this.maxPathSum = maxPathSum;
             this.maxConnectPath = maxConnectPath;
         }
     }
     public MetaData maxPathSumWithMeta(TreeNode root) {
         // Start typing your Java solution below
         // DO NOT write main() function
         if(root==null){
             return new MetaData(Integer.MIN_VALUE, Integer.MIN_VALUE);
         }
 
         MetaData leftData = maxPathSumWithMeta(root.left);
         MetaData rightData = maxPathSumWithMeta(root.right);
 
        int maxConnectPathOfChild = ((leftData.maxConnectPath > rightData.maxConnectPath)?
                            (leftData.maxConnectPath):(rightData.maxConnectPath));
        int myMaxConnectPath = (maxConnectPathOfChild > 0)?
                            (maxConnectPathOfChild+root.val):(root.val);
 
         int maxWithRoot = root.val;
         if(leftData.maxConnectPath > 0)
             maxWithRoot += leftData.maxConnectPath;
         if(rightData.maxConnectPath > 0)
             maxWithRoot += rightData.maxConnectPath;
         int maxPathSumOfChildren = ((leftData.maxPathSum > rightData.maxPathSum)?
                             (leftData.maxPathSum):(rightData.maxPathSum));
         int myMaxPathSum = (maxWithRoot > maxPathSumOfChildren)? 
             (maxWithRoot):(maxPathSumOfChildren);
         return new MetaData(myMaxPathSum, myMaxConnectPath);
     }
 
     public int maxPathSum(TreeNode root) {
         if(root == null)
             return 0;
         return maxPathSumWithMeta(root).maxPathSum;
     }
 }
