 import java.util.*;
 
 class arreglo
 {
 	public static void main(String args[])
 	{
 		Scanner sc=new Scanner(System.in);
 		
 		int x[]=new int[10];
 		int m=0;
 		int m1=0;
 		
 		for(int i=0;i<x.length;i++)
 		{
				System.out.println("Ingrese algo que parezca numero");
 				x[i]=sc.nextInt();
 				
 			if(i==0)
 			{
 					m=x[i];
 			
 			}else if(x[i]>x[i-1])
 			{
 				m1=x[i];
 				
 			}else if(x[i]>x[i-1])
 			{
 				m1=x[i-1];
 			}
 				m=m1;
 		}
 		
 			
			System.out.println("El Numero mayor es: "+m + " :D ");
 	}
 }
