 package com.utilis;
 
 public class Math {
 	
 	public static int getGCF(int n1, int n2)
 	{
 		int rem = 0;
 		int gcf = 0;
 		do
 		{
 			rem = n1 % n2;
 			if (rem == 0)
 				gcf = n2;
 			else
 			{
 				n1 = n2;
 				n2 = rem;
 			}
 		}
 		while (rem != 0);
 		return gcf;
 	}
 
 	public static int getLCM(int a, int b)
 	{
 		int num1, num2;
 		if (a > b)
 		{
             num1 = a;
             num2 = b;
 		}
 		else
 		{
 			num1 = b;
 			num2 = a;
 		}
 		for (int i = 1; i <= num2; i++)
 		{
             if ((num1 * i) % num2 == 0)
             {
                     return i * num1;
             }
 		}
     	throw new Error("Error in LCM!");
 	}
 	
 	public static int[] getFirstXTriangleNumbers(int x){
 		
 		int[] nums = new int[x];
 		for(int i=0; i<=x; i++){
 			int num = x;
 			for(int t=num; t>0;t--){
 				num+=t;
 			}
 			nums[x] = num;
 		}
 		return nums;
 	}
 	
 	public static int[] getXNumbersOfFibonacciSequence(int x){
 		
 		int[] nums = new int[x];
 		nums[0] = 0;
 		if(nums.length>=2){
 			nums[1] = 1;
 		}
 		if(nums.length>=3){
 			for(int i=2; i<=x; i++){
 				nums[i] = nums[i-1] + nums[i-2];
 				
 			}
 		}
 		return nums;
 		
 	}
 	
 	// returns the sum of all integers 1 + 2 + .... + n-1 + n
 	public static int sum(int n)
 	{
 		if( n == 1)
 			return 1;
 		return n + sum(n-1);
 	}
 	
 	// returns n factorial
 	public static int factorial(int n)
 	{
 		if (n == 1)
 			return 1; 
		return n * factorial(n-1);
 	}
 	
 	// returns n1 raised to the n2 power
 	public static int power(int n1, int n2)
 	{
 		if(n2==1)
 			return n1;
		return n1 * power(n1,n2 - 1);
 	}
 	
 }
