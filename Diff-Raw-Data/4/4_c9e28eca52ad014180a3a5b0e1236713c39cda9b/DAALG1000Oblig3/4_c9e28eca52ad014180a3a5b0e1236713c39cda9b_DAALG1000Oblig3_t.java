 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package da.alg1000.oblig3;
 
 import java.util.AbstractMap;
 import java.util.Scanner;
 
 /**
  *
  * @author Martin
  */
 public class DAALG1000Oblig3 {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         Scanner inp = new Scanner(System.in);
 
 
         int buckets = 0;
         while (buckets == 0) {
             System.out.print("Number-of-buckets> ");
             String s = inp.nextLine();
             if (!s.trim().equals("")) {
                 try {
                     buckets = Integer.parseInt(s);
                 } catch (Exception e) {
                 }
             }
         }
 
         int op = 0;
         while (op == 0) {
             System.out.println("0: None");
            System.out.println("1: Most recently used");
            System.out.println("2: Least recently used");
             System.out.print("Access optimization policy> ");
             String s = inp.nextLine();
             if (!s.trim().equals("")) {
                 try {
                     op = Integer.parseInt(s);
                     if (!(op >= 0 && op < 3)) {
                         continue;
                     }
                 } catch (Exception e) {
                 }
             }
         }
         AccessOptimizationPolicy OptimizationPolicy = op == 0 ? AccessOptimizationPolicy.None : op == 1 ? AccessOptimizationPolicy.MostRecentlyUsed : AccessOptimizationPolicy.LeastRecentlyUsed;
 
         hashTable<String, String> ht = new hashTable<>(buckets, OptimizationPolicy);
         String key;
 
         while (true) {
             System.out.println();
             System.out.println("0: Quit");
             System.out.println("1: Store");
             System.out.println("2: Retrieve");
             System.out.println("3: Delete");
             System.out.println("4: Print table");
             System.out.println("LF=" + ht.calculateLoadFactor());
             System.out.println();
             int i = Integer.MAX_VALUE;
             do {
                 System.out.print("COMMAND> ");
                 String s = inp.nextLine();
                 if (!s.trim().equals("")) {
                     try {
                         i = Integer.parseInt(s);
                     } catch (Exception e) {
                         if (s.contains("-") && s.length() < 9 && s.startsWith("555") && s.endsWith("8632")) {
                             System.out.println("Strange game, the only winning move is, not to play");
                         } else {
                             System.out.println(e);
                         }
                     }
                 }
             } while (i == Integer.MAX_VALUE);
             switch (i) {
                 case 0:
                     try {
                         exitManager eM = new exitManager();
                     } catch (Exception e) {
                     }
                     return;
                 case 1:
                     System.out.print("Key> ");
                     key = inp.nextLine();
                     System.out.print("Value> ");
                     String value = inp.nextLine();
                     if (!ht.contains(key)) {
                         ht.add(key, value);
                     } else {
                         ht.set(key, value);
                     }
                     break;
                 case 2:
                     System.out.print("Key> ");
                     key = inp.nextLine();
                     System.out.println(ht.get(key));
                     break;
                 case 3:
                     System.out.print("Key> ");
                     key = inp.nextLine();
                     ht.remove(key);
                     break;
                 case 4:
                     for (Object o : ht.toArray()) {
                         System.out.println("\"" + ((AbstractMap.SimpleEntry<String, String>) o).getKey() + "\":\"" + ((AbstractMap.SimpleEntry<String, String>) o).getValue() + "\"");
                     }
                     break;
                 default:
                     System.out.println("\n\nInvalid option");
                     break;
             }
         }
     }
 }
