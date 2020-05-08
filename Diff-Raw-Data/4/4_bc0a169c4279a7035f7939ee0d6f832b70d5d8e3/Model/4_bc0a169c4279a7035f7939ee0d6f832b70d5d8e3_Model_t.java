 package test.writing.box;
 
 import classes.Word;
 import classes.SmartPoint;
 import java.util.ArrayList;
 
 
 public class Model {
     
     // Sketchy Model
     // I'll add to it as I need to
     
     private Word liveWord = null;
     private ArrayList<Word> words;
     // Scale Factor is super important
     private final static int spaceFactor = 100;
     
     public Model(){
         words = new ArrayList<Word>();
     }
     
     public void addPoint(SmartPoint p){
         if(liveWord == null){
              liveWord = new Word(p);
              System.out.println("new Word");
        // This still is not working properly
        // The contains() method isn't returning what we want
        } else if(Math.abs(liveWord.right() - p.x) > spaceFactor && !liveWord.contains(p)){
             Word temp = liveWord;
             words.add(temp);
             liveWord = new Word(p);
             System.out.println("new Word");
         } else{
             liveWord.add(p);
         }
     }
     
     public void addPointDirect(SmartPoint p){
         liveWord.add(p);
     }
     
     public void addWord(Word w){
         words.add(w);
     }
     
     // This will need tweaked
     public ArrayList<Word> getViewData(){
         ArrayList<Word> temp = new ArrayList<Word>();
         temp.add(liveWord);
         return temp;
     }
 }
