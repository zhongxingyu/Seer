 import java.io.*;
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Alexey
  * Date: 11.02.13
  * Time: 17:50
  * To change this template use File | Settings | File Templates.
  */
 public class TextParsing {
 
     public static void main(String[] arg) throws IOException {
         Reader in = null;
         BufferedReader br = null;
         int length = 0;
         Map<String,WordAndCount> wordList = new HashMap<>();
 
             try {
                 in = new InputStreamReader(new BufferedInputStream((new FileInputStream(arg[0]))), "UTF-8");
                 File f = new File(arg[0]);
                 length = (int) f.length();
             } catch (FileNotFoundException e) {
                 e.printStackTrace();
                 System.out.println("Файл не найден");
             }
 
         StringBuilder incompleteWord = new StringBuilder();
         char symbol = '!';
         int c;
         int generalCount = 0;
         while ((c = in.read()) != -1){
             symbol = (char)c;
             if (Character.isLetterOrDigit(symbol)){
                 incompleteWord.append(symbol);
             }else {
                 String completeWord = incompleteWord.toString();              //сформировал слово
                 if (completeWord.isEmpty()){
                     continue;
                 }
                 generalCount++;
                 incompleteWord.setLength(0);
                 WordAndCount word = wordList.get(completeWord);
 
                 if (word == null){
                     word = new WordAndCount(completeWord);
                     wordList.put(completeWord, word);
 
                 }else {
                     word.addCount();
 
                 }
 // Это не правильно, нужно без Set делать.
 //                if ( !wordList.add(word)){                               //если слова нет, то добавить в список
 //                     wordList.                                           //если такое слово есть, то увеличить счетчик
 //                    Iterator it = wordList.iterator();
 //                    WordAndCount current;
 //                    while (it.hasNext()){
 //                        current = (WordAndCount) it.next();
 //                        if ( current.equals(word) ){
 //                            current.addCount();
 //                            break;
 //                        }
 //                    }
 //                }
             }
         }
 
         Set<WordAndCount> orderList = new TreeSet<>(wordList.values());
 
         List<WordAndCount> orderList2 = new ArrayList<>(wordList.values());
 
         Collections.sort(orderList2);
 
         PrintCvs pc2 = new PrintCvs(orderList2, generalCount);
         PrintCvs pc = new PrintCvs(orderList, generalCount);
 
         pc.generateCsvFile("C:\\Text\\result.cvs");
 
     }
 }
