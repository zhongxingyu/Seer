 package Roma.Cards;
 
 import java.util.ArrayList;
 
 /**
  * File Name:
  * Creator: Varun Nayyar
  * Date: 10/05/12
  * Desc:
  */
 public class WrapperMaker {
     private int costShift = Wrapper.INITIAL_SHIFT;
     private int costScale = Wrapper.INITIAL_SCALE;
     private int defenseShift = Wrapper.INITIAL_SHIFT;
     private int defenseScale = Wrapper.INITIAL_SCALE;
    private ArrayList<Wrapper> wrapperList = new ArrayList<Wrapper>();
     private int ownerID;
 
     public WrapperMaker(int playerID) {
         ownerID = playerID;
     }
 
     public int getOwnerID(){
         return ownerID;
     }
 
     public void setCostShift(int costShift) {
         this.costShift = costShift;
     }
 
     public void setCostScale(int costScale) {
         this.costScale = costScale;
     }
 
     public void setDefenseShift(int defenseShift) {
         this.defenseShift = defenseShift;
     }
 
     public void setDefenseScale(int defenseScale) {
         this.defenseScale = defenseScale;
     }
 
     public Wrapper insertWrapper(CardHolder card) {
         Wrapper wrapper = new Wrapper(card);
 
         wrapper.setCostScale(costScale);
         wrapper.setCostShift(costShift);
         wrapper.setDefenseScale(defenseScale);
         wrapper.setDefenseShift(defenseShift);
 
         wrapperList.add(wrapper);
 
         return wrapper;
     }
 
     public ArrayList<Wrapper> getWrapperList() {
         return wrapperList;
     }
 
     public void clearWrapperList() {
         wrapperList.clear();
     }
 }
