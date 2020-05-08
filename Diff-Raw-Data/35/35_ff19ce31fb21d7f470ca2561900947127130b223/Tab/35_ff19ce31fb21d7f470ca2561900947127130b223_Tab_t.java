 package telnet;
 
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 public class Tab {
 
 	private static Tab instance = new Tab();
 	private Trie trie = new Trie();
 
 	private Tab() {
 		Scanner f;
 		try {
 			f = new Scanner(new FileReader("src/Clojure_Bindings.clj"));
 			while (f.hasNextLine()) {
 				String word = f.nextLine();
 				if (word.startsWith("def", 1))
 					trie.addWord(word.split(" ")[1]);
 			}
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public static Tab getInstance() {
 		return instance;
 	}
 
 	public String autoComplete(String s, int caretPos) {
 		SuperString string = findWordAndPos(s, caretPos);
 		if (string != null) {
 			return s.substring(0, string.end)
 					+ trie.autoComplete(string.string)
 					+ s.substring(string.end, s.length());
 		}
 		return s;
 	}
 
 	public String suggestions(String s, int caretPos) {
 		SuperString string = findWordAndPos(s, caretPos);
 		String toReturn = "";
 		if (string != null) {
 			toReturn = "Sugestions for " + string.string + ": ";
 			ArrayList<String> posibilities = trie.getMutations(string.string);
 			if (posibilities.size() == 0) {
 				toReturn += "NONE FOUND";
			} else if(posibilities.size() == 1 && posibilities.get(0).equals(string.string)){
				toReturn = "input options";
			}else{
 				for (int i = 0; i < posibilities.size(); i++) {
 					toReturn += posibilities.get(i)
 							+ (i != posibilities.size() - 1 ? ", " : "");
 				}
 			}
 		}
 		return toReturn;
 	}
 
 	private SuperString findWordAndPos(String s, int caretPos) {
 		caretPos--;
 		if (s.length() != 0 && containsSeparator(s, caretPos)) {
 			caretPos--;
 		} else if (caretPos > 0) {
 
 			int front;
 			int end;
 			do {
 				front = caretPos--;
 			} while (caretPos != -1 && !containsSeparator(s, caretPos));
 			do {
 				end = caretPos++;
 			} while (caretPos != s.length() && !containsSeparator(s, caretPos));
 			end++;
 			return new SuperString(s.substring(front, end), end);
 		}
 		return null;
 	}
 
 	private boolean containsSeparator(String s, int caretPos) {
 		String toCheck = " \n().";
 		for (char c : toCheck.toCharArray()) {
 			if (s.charAt(caretPos) == c)
 				return true;
 		}
 		return false;
 	}
 
 	private class SuperString {
 		public String string;
 		public int end;
 
 		public SuperString(String string, int end) {
 			this.string = string;
 			this.end = end;
 		}
 	}

 	public class Trie {
 		private Node top;
 		private Node location;
 
 		public Trie() {
 			top = new Node();
 			location = top;
 		}
 
 		public void addWord(String word) {
 			for (Character c : (word + "\r").toCharArray()) {
 				location = location.addNode(c);
 			}
 			location = top;
 		}
 
 		public String autoComplete(String word) {
 			String toReturn = "";
 			for (Character c : word.toCharArray()) {
 				if (location != null) {
 					location = location.getNode(c);
 				}
 			}
 			if (location != null) {
 				while (location.branchCount() == 1) {
 					location = location.getNode(0);
 					toReturn += location.getCharacter();
 				}
 			}
 			location = top;
 			return toReturn.replaceAll("\r+$", "");
 		}
 
 		public ArrayList<String> getMutations(String word) {
 			ArrayList<String> toReturn = new ArrayList<String>();
 			for (Character c : word.toCharArray()) {
 				if (location != null) {
 					location = location.getNode(c);
 				}
 			}
 			if (location != null) {
 				ArrayList<Node> nodes = new ArrayList<Node>();
 				nodes.add(location);
 				for (String s : getPossibleCombos(nodes)) {
 					toReturn.add(word + s.substring(1, s.length() - 1));
 				}
 			}
 			location = top;
 			return toReturn;
 		}
 
 		private ArrayList<String> getPossibleCombos(ArrayList<Node> nodes) {
 			ArrayList<String> toReturn = new ArrayList<String>();
 			for (Node n : nodes.get(nodes.size() - 1).getBranch()) {
 				ArrayList<Node> newFoundTiles = new ArrayList<Node>();
 				for (int i = 0; i < nodes.size(); i++)
 					newFoundTiles.add(nodes.get(i));
 				newFoundTiles.add(n);
 				if (n.branchCount() == 0) {
 					String toAdd = "";
 					for (Node nf : newFoundTiles) {
 						toAdd += nf.getCharacter();
 					}
 					toReturn.add(toAdd);
 				}
 				for (String s : getPossibleCombos(newFoundTiles)) {
 					toReturn.add(s);
 				}
 			}
 			return toReturn;
 		}
 
 		public class Node {
 			private Character c;
 			private ArrayList<Node> branch = new ArrayList<Node>();
 
 			public Node() {
 			}
 
 			public Node(Character c) {
 				this.c = c;
 			}
 
 			public ArrayList<Node> getBranch() {
 				return branch;
 			}
 
 			public Node addNode(Character c) {
 				Node toReturn = getNode(c);
 				if (toReturn == null) {
 					toReturn = new Node(c);
 					branch.add(toReturn);
 				}
 				return toReturn;
 
 			}
 
 			public Node getNode(Character c) {
 				for (Node n : branch) {
 					if (n.c.equals(c)) {
 						return n;
 					}
 				}
 				return null;
 			}
 
 			public Node getNode(int i) {
 				return branch.get(i);
 			}
 
 			public int branchCount() {
 				return branch.size();
 			}
 
 			public char getCharacter() {
 				return c;
 			}
 
 		}
 	}
 }
