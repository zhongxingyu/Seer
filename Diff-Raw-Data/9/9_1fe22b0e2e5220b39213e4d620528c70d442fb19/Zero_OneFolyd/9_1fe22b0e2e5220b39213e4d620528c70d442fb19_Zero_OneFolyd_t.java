 import java.util.Scanner;
 
 /*
  * Program to print floyd triangle as below..
  * 1 
  * 0 1 
  * 1 0 1 
  * 0 1 0 1 
  * 1 0 1 0 1 
  */
 public class Zero_OneFolyd {
 	
 	public static void main(String[] args) {
 		
		int i,j,c;
		Scanner input = new Scanner(System.in);
        System.out.println("Enter the Limit :");
 		int n = input.nextInt();
	
         c=0;
         for(i=1;i<=n;i++)
         {
         	c=i;
             for(j=1;j<=i;j++)
             {
             	if((c%2)==0)
                 {
                 	System.out.print(0+" ");
                 }
                 else
                 {
                 	System.out.print(1+" ");
                 } 
                 c++;
             }
             System.out.println();
        }    
 	}
 }
