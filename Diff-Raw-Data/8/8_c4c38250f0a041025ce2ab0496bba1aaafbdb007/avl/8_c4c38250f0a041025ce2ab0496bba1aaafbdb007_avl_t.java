 // This software is in the public domain, furnished "as is", without technical
 // support, and with no warranty, express or implied, as to its usefulness for
 // any purpose.
 
 // AVL auto-balanced trees
 // Naive implementation: space inefficient and lots of rotation code
 class AVL {
   private int id;
   private int height;
   private AVL left;     //null iff height==0
   private AVL right;    //null iff height==0
 
   public int getHeight() {return height;}
 
   public AVL() {
     id = -1;
     height = 0;
     left = null;
     right = null;
   }
 
   public boolean isEmpty() {
     return height == 0;
   }
 
   public boolean hasKey(int key) {
     AVL current = this;
     while(!current.isEmpty()) {
      if(current.id == key)
         return true;
      if(key < current.id)
         current = current.left;
      if(key > current.id)
         current = current.right;
     }
     return false;
   }
 
   public void add(int key) {
     if(id == key)
       return;
     if(height == 0) {
       height++;
       id = key;
       left = new AVL();
       right = new AVL();
     }
     if(key < id)
       left.add(key);
     if(key > id)
       right.add(key);
     setHeight();
     rebalance();
   }
 
   public void setHeight() {
     height = 1 + Math.max(left.height, right.height);
   }
 
   private boolean checkTree(int low, int up) {
     if(height == 0)
       return true;
     if(id >= up || id <= low)
       return false;
     return(left.checkTree(low, id) && right.checkTree(id, up));
   }
 
   public boolean checkTree() {
     return checkTree(Integer.MIN_VALUE, Integer.MAX_VALUE);
   }
 
   public boolean checkBalanced() {
     if(height == 0)
       return true;
     int theoricHeight = 1 + Math.max(left.height, right.height);
     if(theoricHeight != height)
       return false;
     if(Math.abs(left.height - right.height) > 1)
       return false;
     return(left.checkBalanced() && right.checkBalanced());
   }
 
   public void rebalance() {
     if(height == 0 || Math.abs(left.height-right.height) <= 1)
       return;
     if(left.height < right.height) { //so right.height >= 1
       if(right.left.height <= right.right.height) {
         int oldId = id;
         AVL oldRight = right;
         AVL oldLeft = left;
         id = oldRight.id;
         right = oldRight.right;
         left = oldRight;
         left.id = oldId;
         left.right = oldRight.left;
         left.left = oldLeft;
         left.setHeight();
       }
       else {
         AVL oldLeft = left;
         AVL oldRightLeft = right.left;
         AVL oldRightLeftLeft = right.left.left;
         AVL oldRightLeftRight = right.left.right;
         int oldId = id;
         id = oldRightLeft.id;
         left = right.left;
         left.id = oldId;
         left.left = oldLeft;
         left.right = oldRightLeftLeft;
         right.left = oldRightLeftRight;
         left.setHeight();
         right.setHeight();
       }
     }
     else { //so left.height > right.height
       if(left.left.height >= left.right.height) {
         int oldId = id;
         AVL oldRight = right;
         AVL oldLeft = left;
         id = oldLeft.id;
         left = oldLeft.left;
         right = oldLeft;
         right.id = oldId;
         right.left = oldLeft.right;
         right.right = oldRight;
         right.setHeight();
       }
       else {
         AVL oldRight = right;
         AVL oldLeftRight = left.right;
         AVL oldLeftRightLeft = left.right.left;
         AVL oldLeftRightRight = left.right.right;
         int oldId = id;
         id = oldLeftRight.id;
         right = left.right;
         right.id = oldId;
         right.right = oldRight;
         right.left = oldLeftRightRight;
         left.right = oldLeftRightLeft;
         left.setHeight();
         right.setHeight();
       }
     }
     setHeight();
   }
 
   private void printPrefix(String pre) {
     if(height == 0)
       return;
     System.out.printf("%s%d  (%d)\n", pre, id, height);
     pre += "  ";
     left.printPrefix(pre);
     right.printPrefix(pre);
   }
 
   public void print() {
     this.printPrefix("");
   }
 }
 
