 package FourLogic;
 
 
 import java.util.ArrayList;
 
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author dave
 */
 public class FourEvalOperations {
     
     public void additem(FourEval list,int product[]){
         FourEval elem = new FourEval();
         while(list.next != null) list = list.next;
         elem.next=list.next;
         elem.product = product;
         list.next = elem;
     }
 
     public  boolean equal(int product1[],int product2[],int nvar){
         for(int i = 0; i < nvar; i++)
         {
             if(product1[i] != product2[i])
                 return false;
         }
         return true;
     }
 
     public boolean complementary(FourEval item1,FourEval item2){
         int sum,i;
         for(i = sum = 0 ; i < item1.nvar; i++){
             if(item1.product[i] != item2.product[i])
                 sum += 1;
          System.out.println(item1.product[i] + " == " +item2.product[i]);
         }
         return (sum == 1);
     }
 
    
     public void deleteitem(FourEval list,FourEval item){
         while(list.next != item){
             list = list.next;
         }
        if(list.next != null)
         list.next = list.next.next;
        else
         list.next = null;
     }
 
     public void Qrec(ArrayList<FourEval> set,int index){
         FourEval item1,item2,trav;
         boolean exist;
         int[] prod;
         int j,i;
         if(index < 1)
             return;
         for(item1 = set.get(index).next; item1 != null;)
         {
              if(item1.next != null)
              {
               item2 = item1.next;
                   while(item2 != null){
 
                       if(this.complementary(item1, item2)){
                         for(j = i = 0,prod = new int[item1.nvar]; i < item1.nvar; i++,j++)
                          if(item1.product[i] == item2.product[i])
                          prod[j]=item1.product[i];
                          else
                          prod[j] = -1;
 
                   for(exist = false,trav = set.get(index-1).next; trav != null; trav = trav.next)
                   {
                       if(equal(prod, trav.product,trav.nvar))
                       {
                           exist = true;
                       }
                   }
                   if(!exist)
                   this.additem(set.get(index-1), prod);
 
                   item1.used = item2.used = 1;
                       }
                   prod = new int[3];
                   item2 = item2.next;
                   }
              }
              item2 = item1;
              item1 = item1.next;
            if(item2.used == 1) deleteitem(set.get(index), item2);
         }
         Qrec(set, index-1);
     }
     public String CollectVar(ArrayList<FourEval> set){
     String var[] = { "A","B","C","D","\0"};
     FourEval trail = new FourEval();
     
     String simplified_bool = new String(" ");
       for(int i = 0; i < 3 ; i++)
       {
             for(FourEval trav = set.get(i).next; trav != null; trav = trav.next){
                 trail = trav;
                 if(trav.product[0] != -1)
                     if(trav.product[0] == 1)
                     {
                      System.out.print(var[0]);
                      simplified_bool = simplified_bool.concat(var[0] + ".");
                     }
                     else
                     {
                      System.out.print("not(" + var[0] + ")");
                      simplified_bool = simplified_bool.concat("not(" + var[0] + ")");
                     }
                 if(trav.product[1] != -1)
                     if(trav.product[1] == 1)
                     {
                      System.out.print("" + var[1]);
                      simplified_bool = simplified_bool.concat(var[1] + ".");
                      }
                     else
                     {
                      System.out.print("not(" + var[1] + ")");
                      simplified_bool = simplified_bool.concat("not(" + var[1] + ")");
                      }
                 if(trav.product[2] != -1)
                     if(trav.product[2] == 1)
                     {
                      System.out.print(var[2]);
                      simplified_bool = simplified_bool.concat(var[2] + ".");
                     }
                     else
                     {
                      System.out.print("not(" + var[2] + ")");
                      simplified_bool = simplified_bool.concat("not(" + var[2] + ")");
                     }
                 if(trav.product[3] != -1)
                     if(trav.product[3] == 1)
                     {
                      System.out.print(var[3]);
                      simplified_bool = simplified_bool.concat(var[3] + ".");
                     }
                     else
                     {
                      System.out.print("not(" + var[3] + ")");
                      simplified_bool = simplified_bool.concat("not(" + var[3] + ")");
                     }
                 if(trav.next != null)
                 {
                 System.out.print("+");
                 simplified_bool = simplified_bool.concat(" + ");
                 }
             }
       if(trail != null)
          simplified_bool = simplified_bool.concat(" + ");
         
    //+  +  + 
           
                  
       }
      
         System.out.println(simplified_bool);
       return simplified_bool;
      
      
     }
 }
