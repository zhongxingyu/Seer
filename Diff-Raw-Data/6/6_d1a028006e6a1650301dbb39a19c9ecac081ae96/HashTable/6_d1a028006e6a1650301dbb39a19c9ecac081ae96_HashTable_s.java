 package hash;
 
//import bin.tree.trie.*;

 //TODO treat collisions: Without two different zones
 
 public class HashTable {
 
 	private final int weight = 2;
 	private int size;
 	private NodeT[] nodes;
 	
 	/**
 	 * Create HashTable with a default size 10
 	 */
 	public HashTable()
 	{
 		//Default size
 		this.size = 10;
 	}
 	
 	/**
 	 * Create HashTable to 's' elements
 	 * The table size is 120% of 's'
 	 * @param s number of elements to be stored in hashtable
 	 */
 	public HashTable(int s)
 	{
 		//Define size as user defined
 		this.size = s;
 	}
 	
 	/**
 	 * Add the value in the storage vector treating collisions
 	 * TODO treat collisions!!!!!!
 	 * @param key
 	 * @param lines element to add
 	 */
 	public void add(String key, int[] lines)
 	{
 		//Get the hash value to key
 		int hashkey  = getHash(key);
 		//Position at nodes vector
 		int pos = hashkey % this.size;
 		
 		nodes[pos] = new NodeT(key, lines);
 	}
 	
 	/**
 	 * Get the hash value to key
 	 * @param key a string to calculate the hash value
 	 * @return the hash value as a integer
 	 */
 	private int getHash(String key)
 	{
 		//Hash value to be returned
 		int hash = 0;
 		//key length
 		int keysize = key.length();
 		
 		//Set key to lower case
 		key = key.toLowerCase();
 		
 		//Calculate the hash value
 		for(int i = 0; i < keysize; i++)
 		{
 			hash = (key.charAt(i) - 97) * weight * (i+1);
 		}
 
 		return hash;
 	}
 	
 	/**
 	 * Debug method: print all nodes stotagede in hashtable
 	 */
 	public void printElements()
 	{
 		for(NodeT n: nodes)
 		{
 			if(n != null)
 			{
 				System.out.println(n.toString());
 			}
 		}
 	}
 		
 	private class NodeT
 	{
 		private String key;
 		private int[] lines;
 		
 		public NodeT(String key, int[] lines)
 		{
 			this.key = key;
 			this.lines = lines;
 		}
 		
 		/**
 		 * Prints a hashtable node
 		 */
 		public String toString()
 		{
 			StringBuilder str = new StringBuilder();
 			str.append(key);
 			str.append(" -> lines: ");
 			for(int l: lines)
 			{
 				str.append(l);
 				str.append(" ");
 			}
 			
 			return str.toString();
 		}
 	}
 }
