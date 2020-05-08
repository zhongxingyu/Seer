 
 class Collatz {
     public static void main(String[] aArg) {
         //int j = 21474836;
         int j = 1214740;
         int i;
         int z = 0;
 
 
         while (j > 5) {
             i = j;
             j--;
             Console.write("Badum"); // + j +"\n");
             
            /*
             while (i > 5) {
                 if ((i % 2) == 0) {
                     i = i / 2;
                 } else {
                     i = 1 + (i * 3);
                     z++;
                 }
             }
            */
 
         }
         //System.out.println(z);
         if (z == 52591218)
             Console.write("Yahy!");
         else
             Console.write("Nahy!");
     }
 }
