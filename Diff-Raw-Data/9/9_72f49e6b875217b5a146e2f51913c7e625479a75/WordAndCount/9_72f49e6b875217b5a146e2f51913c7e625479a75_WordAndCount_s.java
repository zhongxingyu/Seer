 /**
  * Created with IntelliJ IDEA.
  * User: Alexey
  * Date: 11.02.13
  * Time: 20:26
  * To change this template use File | Settings | File Templates.
  */
 public class WordAndCount implements Comparable<WordAndCount> {
     private String word;
     private int count;
 
     WordAndCount(String w){
         word = w;
         count = 1;
     }
 
     String getWord(){
         return word;
     }
 
     void addCount(){
         count++;
     }
 
     int getCount(){
         return count;
     }
 
     @Override
     public boolean equals(Object ob){
         if (this == ob){
             return true;
         }
         if (!(ob instanceof WordAndCount)){
             return false;
         }
         WordAndCount wr = (WordAndCount) ob;
         return ( word.equals(wr.getWord()) );
     }
 
     @Override
     public int hashCode(){
         return word.hashCode();
     }
 
     @Override
     public int compareTo(WordAndCount o) {
         WordAndCount tmp = (WordAndCount)o;
         if(this.count <= tmp.count){ //текущее меньше полученного
             return 1;
         }else if(this.count > tmp.count){ //текущее больше полученного
             return -1;
        }else if (this.word != tmp.word){
             return 0;
         }
         return 0;
     }
 
 }
