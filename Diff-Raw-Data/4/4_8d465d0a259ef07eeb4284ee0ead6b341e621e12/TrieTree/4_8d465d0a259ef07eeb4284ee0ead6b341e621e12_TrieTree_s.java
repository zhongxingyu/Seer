 /*
  * Copyright 2012 Anderson Queiroz <contato(at)andersonq(dot)eti(dot)br>
  * 
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with OFFViewer. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package tree.trie;
 
 import java.util.Vector;
 
 public class TrieTree {
 	private TrieTree[] nodes;
 	private TrieTree father;
 	private boolean finish;
 	private static TrieTree root = new TrieTree();	
 	private TrieTree tmp;
 	private static boolean ok;
 	
 	/**
 	 * Construct TrieTree to a alphabet with 26 characters
 	 */
 	public TrieTree()
 	{
 		nodes = new TrieTree[26];
 		finish = false;
 		father = null;
 	}
 	
 	/**
 	 * Construct TrieTree to a alphabet with 26 characters and set its father
 	 * @param f a father to this node
 	 */
 	public TrieTree(TrieTree f)
 	{
 		nodes = new TrieTree[26];
 		finish = false;
 		father = f;
 	}
 	
 	/**
 	 * Delete a child
 	 * A father killing his own child, amazing!
 	 * @param me a child to be killed
 	 */
 	private void killMeFather(TrieTree me)
 	{
 		for(int goingToDie = 0; goingToDie < 26; goingToDie++)
 		{
 			if(this.nodes[goingToDie] == me)
 			{
 				//Killing the child
 				nodes[goingToDie] = null;
 				return;
 			}
 		}
 	}
 
 	private TrieTree[] getNodes() {
 		return nodes;
 	}
 	
 	private TrieTree getNode(int i) {
 
 		return nodes[i];
 	}
 	
 	private TrieTree getNode(char c) {
 
 		return nodes[c - 97];
 	}
 
 	private void setNodes(TrieTree[] node) {
 		this.nodes = node;
 	}
 	
 	private void setNode(TrieTree node, int i) {
 		this.nodes[i] = node;
 	}
 	
 	private void setNode(TrieTree node, char c) {
 		this.nodes[c - 97] = node;
 	}
 
 	public TrieTree getFather() {
 		return father;
 	}
 
 	public void setFather(TrieTree father) {
 		this.father = father;
 	}
 	
 	private boolean isFinish() {
 		return finish;
 	}
 
 	private void setFinish(boolean finish) {
 		this.finish = finish;
 	}
 
 	private static TrieTree getRoot() {
 		return root;
 	}
 
 	private static void setRoot(TrieTree root) {
 		TrieTree.root = root;
 	}
 	
 	/**
 	 * Verify is a node is terminal and it has children
 	 * @return true or false
 	 */
 	private boolean hasMore()
 	{
 
 		return this.hasCildren() & finish;	
 	}
 	
 	private boolean hasCildren()
 	{
 		for(int i = 0; i < 26; i++)
 		{
 			if (this.nodes[i] != null)
 			{
 				return true;
 			}
 		}
 		return false;	
 	}
 	
 	private int getNumberOfChildren()
 	{
 		int count = 0;
 		for(int i = 0; i < 26; i++)
 		{
 			if (this.nodes[i] != null)
 			{
 				count++;
 			}
 		}
 		return count;
 	}
 	
 	/**
 	 * Get the n first words with a prefix
 	 * @param prefix the prefix to search
 	 * @param n number of words to return
 	 * @return A vector with until n words. If it found less then n, 
 	 * it will return so much words as possible
 	 */
 	public Vector<String> getSuggestions(String prefix, int n)
 	{
		search(prefix);
		if(this.tmp == this.root)
 		{
 			return new Vector<String>();
 		}
 		else
 		{
 			return this.getSuggestions(this.tmp, new StringBuilder(prefix), n, new Vector<String>());
 		}
 	}	
 	
 	/**
 	 * Recursively implementation of getSuggestions
 	 * @param no node after the prefix
 	 * @param buff a buffer to storage the string
 	 * @param n number of strings to search
 	 * @param suggestions a vector to storage the strings
 	 * @return
 	 */
 	private Vector<String> getSuggestions(TrieTree no, StringBuilder buff, int n, Vector<String> suggestions)
 	{
 		StringBuilder buff2 = new StringBuilder();
 		//Break condition
 		if(no == null || suggestions.size() == n)
 		{
 			return suggestions;
 		}
 				
 		TrieTree[] nos = no.getNodes();
 		
 		for(int i = 0; i < 26; i++)
 		{
 			if(suggestions.size() == n)
 			{
 				return suggestions;
 			}
 			//Initialize buff2 with the previous prefix
 			buff2.append(buff.toString());
 			if(nos[i] != null)
 			{
 				//Buffering the string
 				buff2.append(String.format("%c", 97 + i));
 				
 				if(nos[i].isFinish())
 				{
 					//System.out.println();
 					//System.out.print(buff2.toString());
 					suggestions.add(buff2.toString());
 				}
 				this.getSuggestions(nos[i], buff2, n, suggestions);
 			}
 			//Cleaning buff2 of deeper prefix
 			buff2 = new StringBuilder();
 		}
 		return suggestions;
 	}
 	
 	public int search(String key)
 	{
 		this.ok = false;
 		return search(key, this.root, 0);
 	}
 	
 	/**
 	 * Search in a recursive way a key in the TrieTree
 	 * @param key A string to search in the tree
 	 * @param no the node to search
 	 * @param pos the character in the key to find
 	 * @return the index of the last character found in the tree
 	 */
 	private int search(String key, TrieTree no, int pos)
 	{
 		key = key.toLowerCase();
 		
 		if(pos < key.length()) //tinha -1
 		{
 			char c = key.charAt(pos);
 			if(no.getNode(c) != null)
 			{
 				this.tmp = no;
 				return search(key, no.getNode(c), ++pos);
 			}
 		}
 		//Last character of key is a final node, key found!
 		else if(pos < key.length() && no.isFinish())
 		{
 			this.ok = true;
 			this.tmp = no;
 			return ++pos;
 		}
 		
 		this.ok = false;
 		this.tmp = no;
 		return pos;
 	}
 	
 	/**
 	 * Remove a key from the tree
 	 * @param key that will be removed
 	 * @return true or false if success or fail
 	 */
 	public int remove(String key)
 	{
 		int ret = search(key);
 		
 		tmp.remove(key, this.tmp);
 		
 		return ret;
 	}
 	
 	/**
 	 * Remove recursively 
 	 * @param key that will be removed
 	 * @param no a node to process
 	 */
 	private void remove(String key, TrieTree no){
 		
 		//Break condition
 		if(no == null)
 		{
 			return;
 		}
 		
 		//If the node is final and is not a leaf
 		if(no.hasMore())
 		{
 			no.setFinish(false);
 			this.ok = true;
 			return;
 		}
 		//This node is a leaf
 		else
 		{
 			TrieTree father = no.getFather();
 			if(father == null)
 			{
 				//System.out.println("Father null");
 				return;
 			}
 			
 			if(no.getNumberOfChildren() > 1)
 			{
 				//System.out.println("no.no.getNumberOfChildren(): " + no.getNumberOfChildren() );
 				return;
 			}
 			else //if(no.no.getNodes().length == 1)
 			{
 				//System.out.println("seting a node null");
 				father.killMeFather(no);
 				remove(key, father);
 			}
 		}
 		//System.out.println("Return finish!");
 		return;
 	}
 	/**
 	 * Insert a key in TrieTree
 	 * @param key a key to insert
 	 * @return the status of insertion, true or false
 	 */
 	public boolean insert(String key)
 	{
 		return insert(key, this.root);
 	}
 	
 	/**
 	 * Recursive method to insert a key
 	 * @param key to insert
 	 * @param no the node where it will start the search to a prefix of the key
 	 * @return true or success or false
 	 */
 	private boolean insert(String key, TrieTree no)
 	{
 		//Set string to lower case
 		key = key.toLowerCase();
 		
 		TrieTree pt = no;
 		//Search the biggest sub string in the tree
 		int pos = search(key, pt, 0);
 		//If there is not the key in the tree, add rest of the key
 		if(!ok)
 		{
 			//Add in the tree each character from key that there is not in the tree
 			for(int i = pos; i < key.length(); i++)
 			{
 				char j = key.charAt(i);
 				tmp.setNode(new TrieTree(tmp), j);
 				tmp = tmp.getNode(j);
 			}
 			//Set the last node as final node
 			tmp.setFinish(true);
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 	
 	/**
 	 * Print the tree
 	 * @return a string with all words in the tree
 	 */
 	public void print()
 	{
 		print(this.root, new StringBuilder());
 	}
 	
 	/**
 	 * print tree recursively
 	 * @param no a node to start printing 
 	 */
 	private void print(TrieTree no, StringBuilder buff)
 	{
 		StringBuilder buff2 = new StringBuilder();
 		//Break condition
 		if(no == null)
 		{
 			return;
 		}
 		
 		TrieTree[] nos = no.getNodes();
 		
 		for(int i = 0; i < 26; i++)
 		{
 			//Initialize buff2 with the previous prefix
 			buff2.append(buff.toString());
 			if(nos[i] != null)
 			{
 				//Buffering the string
 				buff2.append(String.format("%c", 97 + i));
 				//Print the string and break line
 				if(nos[i].isFinish())
 				{
 					System.out.println();
 					System.out.print(buff2.toString());
 				}
 				this.print(nos[i], buff2);
 			}
 			//Cleaning buff2 of deeper prefix
 			buff2 = new StringBuilder();
 		}
 		return;
 	}
 	
 	/**
 	 * Prints the node
 	 */
 	public String toString()
 	{
 		StringBuilder buff = new StringBuilder();
 		
 		for(int i = 0; i < 26; i++)
 		{
 			if(nodes[i] != null)
 			{
 				buff.append(String.format("%c ", (i + 97)));
 			}
 		}
 		return buff.toString();
 	}
 }
