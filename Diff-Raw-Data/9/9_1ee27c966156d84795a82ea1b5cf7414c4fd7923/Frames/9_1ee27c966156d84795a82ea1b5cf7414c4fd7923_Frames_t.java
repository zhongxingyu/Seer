 package PagingProject;
 
 import java.util.ArrayList;
 
 public class Frames {
   private int numFrames;
   private ArrayList<Integer> pageElements = new ArrayList<Integer>();
 
  public Frames() {
     initializePageList(numFrames);
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

  public void setNumFrames(int numFrames) {
    this.numFrames = numFrames;
  }
 }
