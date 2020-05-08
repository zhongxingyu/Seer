 package bloom.filters;
 
 import java.util.ArrayList;
 import bloom.hash.HashFunction;
 
 public class InvertibleBloomFilter {
 	
 	private int hashCount;
 	private int size;
 	private class cell{
 		protected byte count;
 		protected String idSum;
 		protected int hashSum;
 		
 		public cell(){
 			count = 0;
 			idSum = "                    ";
 			hashSum = 0;
 		}
 	}
 	private cell[] filter;
 	
 	public InvertibleBloomFilter(int hashCount, int size){
 		this.hashCount = hashCount;
 		this.size = size;
 		filter = new cell[size];
 		for (cell c : filter){
 			c = new cell();
 		}
 	}
 	
 	public boolean insert(String key){
		int[] hashes = HashFunction.hash(key, hashCount, size);
 		for (int i : hashes){
 			filter[i].count++;
 			filter[i].idSum = XOR(filter[i].idSum,key);
 			filter[i].hashSum = filter[i].hashSum ^ key.hashCode();
 		}
 		return true;
 	}
 	
 	private String XOR(String s1, String s2) {
 		StringBuilder sb = new StringBuilder();
 		assert s1.length() == s2.length(); //TODO replace with exception
 		
 		for(int i=0; i<s1.length() && i<s2.length();i++)
 		    sb.append((char)(s1.charAt(i) ^ s2.charAt(i)));
 		String result = sb.toString();
 		return result;
 	}
 	
 	public boolean remove(String key){
		int[] hashes = HashFunction.hash(key, hashCount, size);
 		
 		for (int i : hashes){
 			filter[i].count--;
 			filter[i].idSum = XOR(filter[i].idSum,key);
 			filter[i].hashSum = filter[i].hashSum ^ key.hashCode();
 		}
 		return true;
 	}
 	
 	public int find(String key){
 		return key.hashCode();
 		//return true;
 	}
 	
 	public static ArrayList<String>[] subtract(InvertibleBloomFilter ibf1, InvertibleBloomFilter ibf2){
 		return (ArrayList<String>[])new ArrayList[2];
 	}
 
 }
