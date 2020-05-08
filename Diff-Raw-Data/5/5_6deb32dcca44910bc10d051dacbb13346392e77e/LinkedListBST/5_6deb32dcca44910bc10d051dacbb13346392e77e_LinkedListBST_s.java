 import java.util.List;
 import java.util.ArrayList;
 class Node
 {
     int data;
     Node root;
     Node left;
     Node right;
     public Node(int data)
     {
         this.data = data;
         this.root = null;
         this.left = null;
         this.right = null;
     }
     public Node(Node root)
     {
         this.root = root;
     }
 }
 class SLinkedList
 {
     Node head;
     public SLinkedList()
     {
         head = null;
     }
     public void addBST(Node root)
     {
         if(head == null)
             head = new Node(root);
         else
         {
             Node curr = head;
             while(curr.right != null)
             {
                 curr = curr.right;
             }
             curr.right = new Node(root);
         }
     }
     public void add(int data)
     {
         if(head == null)
         {
             head = new Node(data);
         }
         else
         {
             Node curr = head;
             while(curr.right != null)
             {
                 curr = curr.right; 
             }
             curr.right = new Node(data);
         }
     }
     public void show()
     {
         Node curr = head;
         while(curr != null)
         {
             System.out.print("["+curr.data+"]");
             curr = curr.right;
         }
     }
     public void inorder(Node r)
     {
         if( r != null)
         {
             inorder(r.left);
             System.out.print("["+r.data+"]");
             inorder(r.right);
         }
     }
     public void showBST()
     {
         Node curr = head;
         while(curr != null)
         {
             Node tmp = curr;
             inorder(tmp.root);    
             curr = curr.right;
             System.out.println("=========");
         }
     }
 }
 
 class BST
 {
     Node root;
     public BST()
     {
         root = null;
     }
     public void insert(int data)
     {
         if(root == null)
         {
             root = new Node(data);
         }
         else
         {
             Node curr = root;
             boolean done = false;
             while(!done)
             {
                 if(data < curr.data)
                 {
                     if(curr.left != null)
                     {
                         curr = curr.left;
                     }
                     else
                     {
                         curr.left = new Node(data);
                         done = true;
                     }
                 }    
                 else
                 {
                     if(curr.right != null)
                     {
                         curr = curr.right;
                     }
                     else
                     {
                         curr.right = new Node(data);
                         done = true;
                     }
                 }
             }
         }
     }
 }
 
 
 public class LinkedListBST 
 {
     public static void main(String[] args)
     {
         System.out.println("Hello World!");
         SLinkedList sll = new SLinkedList();
         //sll.add(1);
         //sll.add(2);
         //sll.add(3);
         //sll.add(4);
         //sll.show();
 
         BST bst1 = new BST();
         BST bst2 = new BST();
         BST bst3 = new BST();
         System.out.println("=========");
         bst1.insert(10); 
         bst1.insert(5); 
         bst1.insert(15); 
         bst1.insert(7); 
         bst1.insert(30); 
         bst1.insert(12); 
         bst1.insert(1); 
         inorder(bst1.root);
 
         System.out.println("=========");
         bst2.insert(100); 
         bst2.insert(50); 
         bst2.insert(15); 
         bst2.insert(70); 
         bst2.insert(200); 
         bst2.insert(140); 
         bst2.insert(300); 
         inorder(bst2.root);
 
         System.out.println("=========");
         bst3.insert(30); 
         bst3.insert(60); 
         bst3.insert(105); 
         bst3.insert(9); 
         bst3.insert(50); 
         bst3.insert(10); 
         bst3.insert(1); 
         inorder(bst3.root);
 
 
         System.out.println("=========");
         sll.addBST(bst1.root);
         sll.addBST(bst2.root);
         sll.addBST(bst3.root);
         System.out.println("=========");
         sll.showBST();
 
         System.out.println("----------");
         List<List<Integer>> listList = new ArrayList<List<Integer>>();
         List<Integer> list = new ArrayList<Integer>(); 
         DFS(sll.head, sll.head.root, list, listList);
     }
     public static void DFS(Node curr, Node root, List<Integer> list, List<List<Integer>> listList)
     {
         if(curr != null)
         {
             if(root != null)
             {
                 DFS(curr, root.left, list, listList);
                 DFS(curr, root.right, list, listList);
                 if(root.left == null && root.right == null)
                 {
                     //System.out.print("{"+root.data+"}");
                     list.add(root.data);
                 }
                if(list.size() > 1)
                 {
                     System.out.println("");
                     List<Integer> tmpList = new ArrayList<Integer>(list);
                     listList.add(tmpList);
                     list.clear();
                     if(curr.right != null)
                     {
                         DFS(curr.right, curr.right.root, list, listList);
                         list.clear();
                         if(listList.size() > 0)
                             listList.remove(listList.size()-1);
                     }
                     else
                     {
                         for(List<Integer> mylist : listList)
                         {
                             for(Integer num : mylist)
                             {
                                 System.out.print("["+num+"]");
                             }
                             System.out.println();
                         }
                         if(listList.size() > 0)
                             listList.remove(listList.size()-1);
                     }
                 }
             }
         }
     }
     public static void inorder(Node node)
     {
         if(node != null)
         {
             inorder(node.left);
             System.out.print("["+node.data+"]");
             inorder(node.right);
         }
     }
 }
