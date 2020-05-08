 package spell;
 
 import java.util.Arrays;
 import java.lang.StringBuilder;
 import java.util.ArrayList;
 
 public class Words implements Trie {
 	
   private WordNode root;
   private int numberNodes;
   private int numberWords;
   private int hashCode;
   private String xml;
 
   private ArrayList<String> buildList = new ArrayList<String>();
   //private ArrayList<listNode> buildList = new ArrayList<listNode>();
  
   private class listNode {
     String str;
     int count;
   }
 
   public Words() {
     root = new WordNode();
     numberNodes = 1;
     numberWords = 0;
   }
  public void clearList() {
    this.buildList.clear();
  }

   public void add(String word) {  
     WordNode scanner = root;
     word = word.toLowerCase();
     //System.out.println(word);
     int length = word.length();
     this.numberWords++;
     for (int i = 0; i < length; i++) {
       char letter = word.charAt(i);
       int iChar = Character.getNumericValue(letter) - 10;
       hashCode = hashCode + 7 * iChar;
       //if exists than just up count, change scanning node, keep traversing
       Character c1, c2;
       c1 = new Character(letter);
       if (scanner.subNodes[iChar] != null)
         c2 = new Character(scanner.subNodes[iChar].letter);
       else
         c2 = null;
       if (c2 != null) {
         if (c1.compareTo(c2) == 0) {
           scanner = scanner.subNodes[iChar];
           if ( i == length - 1) {
             scanner.count++;
           }
         }
       } else {
       //if !exists we need to make it
         WordNode nn = scanner.subNodes[iChar] = new WordNode();
         nn.letter = letter;
         if (i == length -1)
           nn.count++;
         nn.prev = scanner;
         //Arrays.sort(scanner.subNodes);
         scanner = nn;
         this.numberNodes++;
       }
       
       
     }
   }
 	
 	/**
 	 * Searches the trie for the specified word
 	 * @param word The word being searched for
 	 * @return A reference to the trie node that represents the word,
 	 * 			or null if the word is not in the trie
 	 */
 	public Trie.Node find(String word) { 
 		WordNode s = root;
 		word = word.toLowerCase();
     int length = word.length();
     for (int i = 0; i < length; i++) {
       char letter = word.charAt(i);
       int iChar = Character.getNumericValue(letter) - 10;
       Character c1, c2;
       c1 = new Character(letter);
       if (s.subNodes[iChar] != null) 
         c2 = new Character(s.subNodes[iChar].letter);
       else
         c2 = null;
       if (c2 != null) {
         if (c1.compareTo(c2) == 0) {
           s = s.subNodes[iChar];
         } 
       }else {
         return null;
       }
     }
     if (s.getValue() == 0 )
       return null;
     return s;
 	}
 
   protected void findSimilar(String word) {
     Trie.Node t = new WordNode();
     //Delete Edit
     for (int i = 0; i < word.length(); i++) {
       StringBuilder foo = new StringBuilder(word);
       //System.out.println("Delete");
       Trie.Node s = new WordNode();
       String tmp = foo.deleteCharAt(i).toString();
       buildList.add(tmp);
     }
     //Transpose
     for (int i = 0; i < word.length() -1; i++) {
       StringBuilder foo = new StringBuilder(word);
       //System.out.println("Transpose");
       Trie.Node s = new WordNode();
       char a = foo.charAt(i);
       char b = foo.charAt(i+1);
       StringBuilder tmp = foo;
       tmp.setCharAt(i, b);
       tmp.setCharAt(i+1,a);
       buildList.add(tmp.toString());
     }
     //Alteration
     for (int i = 0; i < word.length(); i++) {
       for (int j = 97; j < 123; j++) {
         StringBuilder foo = new StringBuilder(word);
         Trie.Node s = new WordNode();
         char a = (char) j;
         StringBuilder tmp = foo;
         tmp.setCharAt(i,a);
         buildList.add(tmp.toString());
       }
     }
     //Insertion
     for (int i = 0; i < word.length()+1; i++) {
       for (int j = 97; j < 123; j++) {
         StringBuilder foo = new StringBuilder(word);
         Trie.Node s = new WordNode();
         char a = (char) j;
         StringBuilder tmp = foo;
         tmp.insert(i,a);
         buildList.add(tmp.toString());
       }
     }
   }
   protected String filterList(String word) {
     word = word.toLowerCase();
     String shortest = null;
     findSimilar(word);
     Trie.Node t = new WordNode();
     Trie.Node s = new WordNode();
 
     int length = buildList.size();
     for (int i = 0; i < length; i++) {
       s = find(buildList.get(i));
       if (s != null ) {
        //System.out.println(buildList.get(i));
         if (s.getValue() > t.getValue()) {
           shortest = buildList.get(i);
           t = s;
         } else if (s.getValue() == t.getValue()) {
           //s = get(i) && t = shortest
          if (buildList.get(i).compareTo(shortest) < 0) {
             shortest = buildList.get(i);
           }
         }
       }
     }
     if (shortest == null) {
       for (int i = 0; i < length; i++) {
         findSimilar(buildList.get(i));
       }
       length = buildList.size();
       for (int i = 0; i < length; i++) {
         s = find(buildList.get(i));
         if (s != null ) {
           //System.out.println(buildList.get(i));
           if (s.getValue() > t.getValue()) {
             shortest = buildList.get(i);
             t = s;
           } else if (s.getValue() == t.getValue()) {
             //s = get(i) && t = shortest
             if (buildList.get(i).compareTo(shortest) > 0) {
               shortest = buildList.get(i);
             }
           }
         }
       }
     }
     return shortest;
   }
 
 
 
 	public int getWordCount() { 
 		return this.numberWords;
 	}
 
 	public int getNodeCount() { 
 		return this.numberNodes;
 	}
 	public String toString() { 
     WordNode k = root;
     StringBuilder j = new StringBuilder(this.xml);
     traverse(k, j);
     return this.xml;
 	}
 
   private void traverse(WordNode k, StringBuilder str) {
     if (k == null) {
       return;
     }
     for (int i = 0; i < 26; i++) {
       if (k.subNodes[i] != null) {
         str = str.append(k.letter);
         if (k.subNodes[i].count > 0) {
           this.xml = this.xml + "<"+str.toString()+"><"+k.subNodes[i].count+">\n";
         }
         traverse(k.subNodes[i],str);
       }
     }
   }
 
 	@Override
 	public int hashCode() { 
 		return this.hashCode;
 	}
 
 	@Override
 	public boolean equals(Object o) { 
 		WordNode s = root;
     WordNode t = (WordNode) o;
     System.out.println("equals");
     return compare(s,t);
 	}
   private boolean compare(WordNode a, WordNode b) {
     if (a.count != b.count || a.letter != b.letter) {
       return false;
     }
     for (int i = 0; i < 26; i++) {
       compare(a.subNodes[i], b.subNodes[i]);
     }
     return true;
   }
 
 }
