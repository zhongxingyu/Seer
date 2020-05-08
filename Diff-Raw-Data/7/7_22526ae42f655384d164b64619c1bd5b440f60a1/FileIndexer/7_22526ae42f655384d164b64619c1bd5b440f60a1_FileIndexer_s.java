 package ru.maratyv.indexer;
 
 import ru.maratyv.indexer.index.HashMapIndex;
 import ru.maratyv.indexer.index.Index;
 import ru.maratyv.indexer.tokenizers.FileTokenizer;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: mir
  * Date: 9/30/13
  * Time: 4:33 PM
  * Porsche is the only car
  */
 public class FileIndexer implements Indexer {
 
     private Index index = new HashMapIndex();
 
     public FileIndexer(File inputFile) throws IOException {
         FileTokenizer ft = new FileTokenizer(inputFile);
         ft.addTokensTo(index);
     }
 
     public List<Integer> find(String word) {
        return index.get(word);
     }
 }
