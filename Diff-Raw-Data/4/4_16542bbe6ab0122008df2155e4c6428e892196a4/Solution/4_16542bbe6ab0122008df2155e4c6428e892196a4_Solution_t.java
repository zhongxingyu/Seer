 package evernote;
 import java.io.*;
 import java.util.Enumeration;
 import java.util.Hashtable;
 
 /**
  * Question 2 solution
  * @author Ken
  */
 
 public class Solution 
 {
 	public String termArray[];
 	public int freqArray[];
 	public Hashtable<String, Integer> table;
 	public int n;
 	public int k;
 	
 	public Solution()
 	{
 		termArray = null;
 		freqArray = null;
 		table = null;
 		n = 0;
 		k = 0;
 	}
 	
 	public static void quicksort(String terms[], int freqs[], int left, int right)
 	{
 		if (left == right)
 			return;
 		
 		int loopLeft = left;
 		int loopRight = right;
 		int pivotIndex;
 		int mid = (left + right) / 2;
 		
 		while(true)
 		{
 			pivotIndex = partition(terms, freqs, loopLeft, loopRight, mid);
 			if (pivotIndex == mid)
 				break;
 			else if (mid < pivotIndex)
 				loopRight = pivotIndex - 1;
 			else
 				loopLeft = pivotIndex + 1;
 		}
 		
 		if (left < pivotIndex)
 			quicksort(terms, freqs, left, pivotIndex - 1);
 		if (right > pivotIndex)
 			quicksort(terms, freqs, pivotIndex + 1, right);
 	}
 	
 	public static int partition(String terms[], int freqs[], int left, int right, int pivotIndex)
 	{
 		int pivotValue = freqs[pivotIndex];
		String pivotTerm = terms[pivotIndex];
 		swap(terms, freqs, pivotIndex, right);
 		int storeIndex = left;
 		for (int i = left; i < right; i++)
			if (freqs[i] < pivotValue || (freqs[i] == pivotValue && terms[i].compareTo(pivotTerm) > 0))
 			{
 				swap(terms, freqs, i, storeIndex);
 				storeIndex++;
 			}
 
 		swap(terms, freqs, storeIndex, right);
 		
 		return storeIndex;
 	}
 	
 	public static void swap(String terms[], int freqs[], int first, int second)
 	{
 		int tempFreq = freqs[first];
 		freqs[first] = freqs[second];
 		freqs[second] = tempFreq;
 		String tempTerm = terms[first];
 		terms[first] = terms[second];
 		terms[second] = tempTerm;
 	}
 	
 	public static void main(String args[]) throws Exception
 	{
 		Solution solution = new Solution();
 		solution.table = new Hashtable<String, Integer>();
 		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
 		
 		//System.out.println("Input the number of terms to add.");
 		solution.n = Integer.parseInt(in.readLine());
 		
 		//System.out.println("Input your terms.");
 		for (int i = 0; i < solution.n; i++)
 		{
 			String term = in.readLine();
 			if (!solution.table.containsKey(term))
 				solution.table.put(term, 1);
 			else
 				solution.table.put(term, solution.table.get(term) + 1);
 		}
 		
 		Enumeration<String> termEnum = solution.table.keys();
 		Enumeration<Integer> freqEnum = solution.table.elements();
 		
 		solution.termArray = new String[solution.table.size()];
 		solution.freqArray = new int[solution.table.size()];
 		
 		int i = 0;
 		while (termEnum.hasMoreElements() && freqEnum.hasMoreElements())
 		{
 			solution.termArray[i] = termEnum.nextElement();
 			solution.freqArray[i] = freqEnum.nextElement();
 			i++;
 		}
 		
 		quicksort(solution.termArray, solution.freqArray, 0, solution.table.size() - 1);
 		//System.out.println("Input how many terms to print");
 		solution.k = Integer.parseInt(in.readLine());
 		
 		for (i = solution.termArray.length - 1; i > solution.termArray.length - solution.k - 1; i--)
 			System.out.println(solution.termArray[i]);
 	}
 }
