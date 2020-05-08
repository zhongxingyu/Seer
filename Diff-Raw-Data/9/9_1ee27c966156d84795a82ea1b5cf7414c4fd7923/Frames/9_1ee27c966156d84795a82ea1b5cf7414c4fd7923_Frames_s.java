 package PagingProject;
 
 import java.util.ArrayList;
 
 public class Frames {
   private int numFrames;
   private ArrayList<Integer> pageElements = new ArrayList<Integer>();
 
  public Frames(int numFrames) {
     initializePageList(numFrames);
    this.numFrames = numFrames;
   }
 
   private void initializePageList(int numFrames) {
     for (int i = 0; i < numFrames; i++) {
       pageElements.add(0);
     }
   }
 
   public int getNumFrames() {
     return numFrames;
   }
 
   public boolean lookupFrameElement(int pageID) {
     return pageElements.contains(pageID);
   }
 
   public int getPageAtSlotNumber(int slotNumber) {
     return pageElements.get(slotNumber);
   }
 }
