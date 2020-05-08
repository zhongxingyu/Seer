 package spell;
 
 import java.util.Scanner;
 import java.io.File;
 import java.io.IOException;
 import java.io.FileNotFoundException;
 
 public class SpellCorrectorImpl implements SpellCorrector {
 
   private Words root;
 
   public SpellCorrectorImpl() {
     this.root = new Words();
   }
 
 	public void useDictionary(String dictionaryFileName) throws IOException {
 		try {
      root.clearList();
      this.root = new Words();
 			Scanner sc = new Scanner(new File(dictionaryFileName));
 			while (sc.hasNext()) {
        root.add(sc.next());
 			}
       sc.close();
 		} catch (FileNotFoundException e) {
 			System.out.println("");
 		}
 
 	}
 
 
 	public String suggestSimilarWord(String inputWord) throws NoSimilarWordFoundException {
     //System.out.println(this.root.getWordCount());
    root.clearList();
     if (root.find(inputWord) != null) {
       return inputWord;
     } else {
      String tmp = root.filterList(inputWord); 
      if (tmp == null)
        throw new NoSimilarWordFoundException();
      
      return tmp;
        
     }
     
   }
 
 }
