 import java.util.* ;
 
 
 public class Node {
 
     // class
     public static TreeMap<String, String> criteria ;
 
     // instances
     String name ;
     Integer level ;
     Boolean show ;
     Node up ;
     ArrayList<Node> down ;
     Boolean gui ; // FIXME
     
     public Node (String name, Node up) {
         this.name = name ;
         this.up = up ;
         if (up==null)
             this.level = 1 ;
         else {
             this.level = up.level + 1 ;
            up.down.add(this) ; // NullPointer Error : must postpone 'this' in main
         }
         this.show = true ;
     }
 
     public String toString() {
         String up ;
         if (this.up==null) up = "" ;
         else               up = this.up.name ;
         return "("+level+") "+name+" : "+show+" : "+up+" : "+down.size() ;
     }
 
     public void print_tree() {
         if (!show) return ;
         String blank = "" ;
         for (int i=0 ; i<level ; i++ ) blank += " " ;
         System.out.println( blank + this ) ;
         for (Node d : this.down) d.print_tree() ;
     }
 
     public static void main(String args[]) {
 
         Node pm = new Node("papy mamie",null) ;
         Node j = new Node("jerome",pm) ;
         Node i = new Node("isa",pm) ;
         Node a = new Node("tonio",pm) ;
 
         String [] ej = {"zoe","lila","amandine"} ;
         String [] ei = {"marie","rapha","juju","sasa"} ;
         String [] ea = {"maxime","heloise"} ;
        for (String e : ej ) new Node(e,j) ;
         for (String e : ei ) new Node(e,i) ;
         for (String e : ea ) new Node(e,a) ;
 
         System.out.println( "\n** full tree **" ) ;
         pm.print_tree() ;
         
     }
 }
 
