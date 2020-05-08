 import java.lang.Runtime;
 
 public class AlgoRhythm1 
 {
 
 
 	public static void main(String[] args)
 	{
 		//Runtime runtime = new Runtime(); 
 		int base = 2; // = a
 		int power  = Integer.MAX_VALUE; // 2^31-1= k
 		int division = 5;//=m
 		int result1;
 		int result2;
 		//int runtime1;
		//result1 = powmod(base, power, division);
 		result2 = smartPowmod(base,power,division);
		System.out.printf("faster way =",result2);

 	}//main
 	/*
 	 * poemod method that find the reminder of (a^k)/m. 
 	 * condition is a,k,and m are bigger than 1, are integer values and every value in java int-values 
 	 * (the biggest will be 2.1billion = 21oku  )
 	*/
 	
 	
 	static int powmod(int a, int k, int m)
 	{
 		long t = 1; //3^100 > 2147483647(= 2^31 -1)that is why define with long-integer type.
 		for(int i = 0; i < k; ++i)
 		{
 			t = (t * a) % m;
 		}
 		return (int)  t;
 	}//powmod
 	
 	/* when k = 2t,   n^k = n^t * n^t
 	*  when k = 2t+1, n^k = n^t * n^t * n
 	*  attention that these show up n^t raptly  
 	*/
 	static int smartPowmod(int a, int k, int m)
 	{
 		if(k == 0)
 		{
 			return 1; // it does not matter what the base is, if power is 1, then result is always 1;
 		}
 		long t = smartPowmod(a, k/2, m);
 		t = (t * t ) % m;
 		if(k % 2 == 1)
 		{
 			t = (t * a ) % m;
 		}
 		
 		
 		return (int) t;
 	}
 	
 
 
 	
 	
 	
 	
 	
 }//class 
