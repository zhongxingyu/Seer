 package edu.puc.concurrentavl;
 
 import java.io.PrintStream;
 
 public class Node {
 
 	public static final int LEFT = 0;
 	public static final int RIGHT = 1;
 	public static final int BLACK = 0;
 	public static final int RED = 1;
 
 	protected int color = RED;
 	private ITreeHolder owner;
 	private Node parent = null;
 	private Node[] children = new Node[2];
 
 	int value;
 
 	Node(Node parent, int value, ITreeHolder owner) {
 		this.parent = parent;
         if(this.parent == null) this.color = BLACK;
 		this.value = value;
         this.owner = owner;
 	}
 
 
     public boolean find(int value) {
 
         int nodeVal = this.value;
         if(value == nodeVal)
             return true;
 
         else if(children[LEFT] != null && value < nodeVal )
             return children[LEFT].find(value);
 
         else if(children[RIGHT] != null)
             return children[RIGHT].find(value);
 
         return false;
     }
 
     public Node findMin() {
         if(children[LEFT] != null)
             return children[LEFT].findMin();
         else
             return this;
     }
 
     public Node getUncle() {
 
         Node uncle = null;
         if(parent.parent != null) {
 
             int uncleDirection = parent.equals(parent.parent.children[RIGHT]) ? LEFT : RIGHT;
             uncle = parent.parent.children[uncleDirection];
         }
 
         return uncle;
     }
 
     public void delete(int delVal) {
 
         int numChildren = numChildren();
 
         if(value == delVal)
         {
             if(numChildren == 2)
             {
                 System.out.println("Deleting " + value + " with numChildren = " + numChildren);
                 Node successor = children[RIGHT].findMin();
                 System.out.println("Successor: " + successor.value);
                 this.value = successor.value;
                 successor.delete(successor.value);
             }
             else if(numChildren == 1)
             {
 
                 Node child = children[LEFT] != null ? children[LEFT] : children[RIGHT];
                 //Check if root
                 if(parent != null)
                 {
                     int directionFromParent = this.equals(parent.children[LEFT]) ? LEFT : RIGHT;
                     parent.children[directionFromParent] = child;
                 }
                 else {
                     owner.updateRoot(child);
                 }
                 child.parent = this.parent;
             }
             else {
                 if(parent == null) {
                     owner.updateRoot(null);
                 }
                 else {
                     int directionFromParent = this.equals(parent.children[LEFT]) ? LEFT : RIGHT;
                     parent.children[directionFromParent] = null;
                 }
             }
         }
         else {
             int direction = delVal < value ? LEFT : RIGHT;
             Node child = children[direction];
             if(child != null)
                 child.delete(delVal);
         }
 
     }
 
 
     public String colorToString() {
 		String s = this.color == BLACK ? "B" : "R";
 		return s;
 	}
 
     public void print(PrintStream ps, int extraWidth)
     {
         if(children[RIGHT] != null)
             print(ps, "", "", '-', RIGHT, extraWidth);
         else if(children[LEFT] != null)
             print(ps, "", "", '-', LEFT, extraWidth);
         else
             ps.println("" + this.value);
 
     }
 
     public void print(PrintStream ps)
     {
         if(children[RIGHT] != null)
             print(ps, "", "", '-', RIGHT, 0);
         else if(children[LEFT] != null)
             print(ps, "", "", '-', LEFT, 0);
         else
             ps.println("" + this.value);
 
     }
 
    protected void print(PrintStream ps, String whitespace, String prefix,
                         char padding, int lastDirection, int extraWidth) {
         int numChildren = numChildren();
         String newSpace = whitespace;
 
         if(children[RIGHT] != null)
         {
             if(parent != null && lastDirection == RIGHT)
             {
                 newSpace = replaceLastOf(whitespace, '|', ' ');
             }
 
             children[RIGHT].print(ps, newSpace + "          |", "----",  padding, RIGHT, extraWidth);
 
             for(int i = 0; i < extraWidth; ++ i)
                 ps.println(newSpace + "          |");
         }
 
         String numString = "" + value;
 
         while(numChildren > 0 && numString.length() + prefix.length() < 10)
             numString += padding;
 
 
         String line = whitespace + prefix + numString;
         ps.println(line);
 
         newSpace = whitespace;
 
         if(children[LEFT] != null)
         {
             if(parent != null && lastDirection == LEFT)
             {
                 newSpace = replaceLastOf(whitespace, '|', ' ');
             }
             for(int i = 0; i < extraWidth; ++ i)
                 ps.println(newSpace + "          |");
             children[LEFT].print(ps, newSpace + "          |", "----", padding, LEFT, extraWidth);
 
 
         }
     }
 
     protected String replaceLastOf(String str, char ch, char newChar) {
         System.out.println("Value: " + value);
         System.out.println("Parent value: " + parent.value);
         if(children[LEFT] != null)
             System.out.println("Left value: " + children[LEFT].value);
         if(children[RIGHT] != null)
             System.out.println("Right value: " + children[RIGHT].value);
         int idx = str.lastIndexOf(ch);
         char[] arr = str.toCharArray();
         arr[idx] = newChar;
         return new String(arr);
     }
 
 
     public void insert(int newValue) {
         //Value already exists, do nothing
         if(this.value == newValue)
             return;
 
         int direction = newValue < value ? LEFT : RIGHT;
         Node child = children[direction];
         if(child == null) {
             children[direction] = new Node(this, newValue, owner);
             children[direction].balanceTree();
         }
         else {
             child.insert(newValue);
         }
     }
 
    public Node getBrother() {
        if(this.parent == null) {
            return null;
        }
        Node brother = this.equals(parent.children[LEFT]) ? parent.children[RIGHT] : parent.children[LEFT];
        return brother;
    }
 
     public void switchColors(Node other) {
         if(other == null) {
             return;
         }
         else {
             int otherColor = other.color;
             other.color = color;
             color = otherColor;
         }
     }

     public void rotate(int direction) {
 
         int otherDirection = direction ^ 1;
         assert(children[otherDirection] != null);
 
         Node oldParent = this.parent;
         Node crossChild = this.children[otherDirection].children[direction];
 
         //children[otherDirection] will be my parent, so it becomes root
         if(parent == null) {
             owner.updateRoot(children[otherDirection]);
         }
         //My parent is the child in otherDirection
         parent = children[otherDirection];
         //His parent is now my old parent
         parent.parent = oldParent;
 
         if(oldParent != null) {
             oldParent.children[otherDirection] = parent;
         }
 
         //...and I am now his child
         parent.children[direction] = this;
         children[otherDirection] = crossChild;
 
         if(crossChild != null)
             crossChild.parent = this;
 
         return;
 
     }
 
     //Direction: Position inserted FROM parent
     public void balanceTree() {
         //If parent is RED, parent.parent is never null or parent would be BLACK
         if(parent == null) {
             color = BLACK;
         }
         else {
             int direction = this.equals(parent.children[LEFT]) ? LEFT : RIGHT;
             int otherDirection = direction ^ 1;
             Node uncle = getUncle();
             int uncleColor = uncle == null ? BLACK : uncle.color;
 
             if(parent.color == BLACK) {
                 return;
             }
             else if(parent.color == RED && uncleColor == RED) {
                 parent.color = BLACK;
                 uncle.color = BLACK;
                 parent.parent.color = RED;
                 parent.parent.balanceTree();
             }
             else if(parent.color == RED && uncleColor == BLACK &&
                     parent.equals(parent.parent.children[otherDirection])) {
                 parent.rotate(otherDirection);
                 //I now have a child going in otherDirection, who was formerly my parent.
                 children[otherDirection].balanceTree();
             }
             else if(parent.color == RED && uncleColor == BLACK &&
                     parent.equals(parent.parent.children[direction])) {
                 parent.parent.switchColors(parent);
                 parent.parent.rotate(otherDirection);
             }
         }
     }
 
     public int numChildren()
     {
         int numChildren = 0;
         if(children[LEFT] != null)
             ++numChildren;
         if(children[RIGHT] != null)
             ++numChildren;
 
         return numChildren;
     }
 
 
 }
 
