 import java.util.*;
 public class Hand{
     private ArrayList<Card> cards;
 
     public Hand(){
         cards = new ArrayList<Card>();
     }        
 
     public Hand(ArrayList<Card> cards){
         this.cards = cards;
     }
 
     public ArrayList<Integer> getValue(){
         ArrayList<Integer> valueList = new ArrayList<Integer>();
         valueList.add(0);
         for(int i = 0; i < cards.size(); i ++){
             if(cards.get(i).getType().equals("ACE")){
                 valueList = copyAndMerge(valueList);
                 for(int j = 0; j < valueList.size() / 2; j ++)
                     valueList.set(i , valueList.get(j) + 1); //Can make less hackish later
                 for(int j = valueList.size() / 2; j < valueList.size(); j ++)
                     valueList.set(i , valueList.get(j) + 11);
             }
             else{
                 for(int j = 0; j < valueList.size(); j ++)
                     valueList.set(j , valueList.get(j) + cards.get(i).getValue());
             }
         }
         return valueList;
     }
 
     public ArrayList<Integer> copyAndMerge(ArrayList<Integer> valueList){
         for(int i = 0; i < valueList.size(); i ++){
             valueList.add(valueList.get(i));
         }
         return valueList;
     }
     
     public void addCard(Card card){
         cards.add(card);
     }
     
     public ArrayList<Card> getCards(){
         return cards;
     }
 
     public int leastValue(){
         ArrayList<Integer> valueList = getValue();
         Collections.sort(valueList);
         return valueList.get(0);
     }
 
     public int maxValue(){
         ArrayList<Integer> valueList = getValue();
         Collections.sort(valueList);
         for(int i = valueList.size() - 1; i > 0; i --){
             if(valueList.get(i) < 21)
                 return valueList.get(i);
         }
         return 0;
     }
     
     
     public int size(){
         return cards.size();
     }
 }
       
