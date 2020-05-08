 package labb2;
 
 public class Main
 {
 
 	public void binarySearch()
 	{
 
 	}
 
 	public void sorting(int[] arr)
 	{
 		int min;
 		int k;
 		for (int i = 0; i < arr.length; i++)
 		{
			k = i;
 			min = arr[i];
			for (int j = i; j < arr.length; j++)
 			{
 				if(arr[j] < min )
 				{
 					min = arr[j];
 					k = j;
 				}
 			}
 			int temp = arr[i];
 			arr[i] = min;
 			arr[k] = temp;
 		}
 	}
 }
