 package sorting;
 import java.util.Scanner;
 public class bubble {
 	public static void main(String c[])
 	{
 		Scanner sc = new Scanner(System.in);
 		System.out.print("Enter the limit:");
 		int n = sc.nextInt();
 		int [] a = new int[n];
 		for(int i=0;i<n;i++)
 		{
 			System.out.print("Enter the number:");
 			a[i]=sc.nextInt(); 
 		}
 		int temp=0;
 	
 		for(int i=0;i<n;i++)
 		{
 			for(int j=0;j <i;j++)
 			{
 				if(a[j]>a[i])
 				{
 					temp=a[j];
 					a[j]=a[i];
 					a[i]=temp;
 				}
 			}
 		}
 	
 		System.out.println("Sorted List:");
 		for(int i=0;i<n;i++)
 		{
 			System.out.println(a[i]);
 			
 		}
 		}
 }
 

