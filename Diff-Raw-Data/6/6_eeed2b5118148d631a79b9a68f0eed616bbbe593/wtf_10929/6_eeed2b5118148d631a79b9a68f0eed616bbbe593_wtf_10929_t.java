 import java.math.*;
 import java.util.*;
 
/*So the input for this problem is messed up.
 *Make sure the last line of input is a single 0 followed by a \n
 *also echo out the string you read in as the number, not the number that is the BigInteger
 *because leading zeroes apparently count... but it didn't say. fuck.
 */ 

 public class wtf_10929
 {
     public static void main(String[] args)
     {
         BigInteger n = new BigInteger("1");
         BigInteger elv = new BigInteger("11");
         String l;
         Scanner in = new Scanner(System.in);
         while (true)
         {
             l = in.nextLine();
             if (l.compareTo("0") == 0) break;
             n = new BigInteger(l);
             if (n.mod(elv).toString().compareTo("0") == 0)
                 System.out.println(l + " is a multiple of 11.");
             else
                 System.out.println(l + " is not a multiple of 11.");
         }
 
     }
 }
