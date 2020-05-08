 
 package ualberta.g12.adventurecreator;
 
 import java.util.List;
 
<<<<<<< HEAD
 import android.graphics.Bitmap;
 import android.graphics.drawable.Drawable;
 
 /**
  * Controller that dictates all fragment methods. Is responsible for all modification within a fragment.
  * Contains code that allows the user to add, edit or delete, the illustrations, text or choices within 
  * a fragment.  
  * 
  */
=======
 
>>>>>>> 61c1e50a9457caec39513a676961fd5682c1c542
 public class FragmentController implements FController {
 
     @Override
     /**
      * Edit the current title in place of the fragment
      * 
      * @param frag  fragment reference corresponding to the current fragment
      * @param newTitle  title that has been previously stated
      */
     public void editTitle(Fragment frag, String newTitle){
         frag.setTitle(newTitle);
     }
     
     @Override
     /**
      * Allows the user to add a text segment to the current fragment.
      * 
      * @param frag  fragment reference corresponding to the current fragment
      * @param textSegment   reference to the text of the current segment
      * @param dispNum   refers to the position of the text segment 
      */
     public boolean addTextSegment(Fragment frag, String textSegment, int dispNum){
         List<String> textSegments = frag.getTextSegments();
         List<String> displayOrder = frag.getDisplayOrder();
         
         //check for invalid dispNum
         if (displayOrder.size()<dispNum)
             return false;
         
         //Insert the text segment
         int textSegNum=0;
         for (int i=0; i<dispNum;i++){
             if (displayOrder.get(i).equals("t"))
                 textSegNum++;
         }
         if (textSegments.size()==textSegNum){
             textSegments.add(textSegment);
         } else {
             textSegments.add(textSegNum, textSegment);
         }
 
         //insert t into corresponding spot in display order
         displayOrder.add(dispNum, "t");
         
         frag.setDisplayOrder(displayOrder);
         frag.setTextSegments(textSegments);
         
         return true;
     }
     
    
     /**
      * Removes the text segment at the given dispNum
      * returns true if successful
      * 
      * @param frag  fragment reference corresponding to the current fragment
      * @param dispNum   refers to the position of the text segment
      */
     private boolean deleteTextSegment(Fragment frag, int dispNum){
         List<String> textSegments = frag.getTextSegments();
         List<String> displayOrder = frag.getDisplayOrder();
 
         //not a text segment at dispNum
         if (!displayOrder.get(dispNum).equals("t"))
             return false;
         
         int textSegNum=0;
         for (int i=0; i<dispNum;i++){
             if (displayOrder.get(i).equals("t"))
                 textSegNum++;
         }
 
         textSegments.remove(textSegNum);
         frag.setTextSegments(textSegments);
         displayOrder.remove(dispNum);
         frag.setDisplayOrder(displayOrder);
         return true;
     }   
         
     @Override
     /**
      * allows the author to add an illustration into a fragment
      * 
      * @param frag  fragment reference corresponding to the current fragment
      * @param illustration  refers to the illustration that has been previously saved 
      * @param dispNum   refers to the position of the text segment
      * 
      */
     public boolean addIllustration(Fragment frag, String illustration, int dispNum){
         List<String> illustrations = frag.getIllustrations();
         List<String> displayOrder = frag.getDisplayOrder();
         
         System.out.println("add ill start2");
         //check for invalid dispNum
         if (displayOrder.size()<dispNum)
             return false;
         System.out.println("ill 3");
         //Insert the text segment
         int illNum=0;
         for (int i=0; i<dispNum;i++){
             if (displayOrder.get(i).equals("i"))
                 illNum++;
         }
         System.out.println("illNum "+illNum);
         if (illustrations.size()==illNum){
            illustrations.add(illustration);
         } else {
             illustrations.add(illNum, illustration);
         }
 
         //insert t into corresponding spot in display order
         displayOrder.add(dispNum, "i");
         
         frag.setDisplayOrder(displayOrder);
         frag.setIllustrations(illustrations);
         System.out.println("ill disord "+displayOrder.toString());
         System.out.println("ill size "+illustrations.size());
         
         return true;
     }
     
     /**
      * Removes the illustration at the given dispNum
      * returns true if successful
      * 
      * @param frag  fragment reference corresponding to the current fragment
      * @param dispNum   refers to the position of the text segment
      */
     private boolean deleteIllustration(Fragment frag, int dispNum){
         List<String> illustrations = frag.getIllustrations();
         List<String> displayOrder = frag.getDisplayOrder();
 
         //not an illustration at dispNum
         if (!displayOrder.get(dispNum).equals("i"))
             return false;
         
         int illustrationNum=0;
         for (int i=0; i<dispNum;i++){
             if (displayOrder.get(i).equals("i"))
                 illustrationNum++;
         }
 
         illustrations.remove(illustrationNum);
         frag.setIllustrations(illustrations);
         displayOrder.remove(dispNum);
         frag.setDisplayOrder(displayOrder);
         return true;
     }
 
     //  @Override
     //  public void addSound(Fragment frag, Sound sound){
     //      LinkedList<Sound> sounds = frag.getSounds();
     //      sounds.add(sound);
     //      frag.setSounds(sounds);
     //      LinkedList<String> displayOrder = frag.getDisplayOrder();
     //      displayOrder.add("s");
     //      frag.setDisplayOrder(displayOrder);
     //  }
     //
     //  @Override
     //  public void addVideo(Fragment frag, Video video){
     //      LinkedList<Video> videos = frag.getVideos();
     //      videos.add(video);
     //      frag.setVideos(videos);
     //      LinkedList<String> displayOrder = frag.getDisplayOrder();
     //      displayOrder.add("v");
     //      frag.setDisplayOrder(displayOrder);
     //  }
     //  
     
     @Override
     /**
      * Allows the user to add a choice to the fragment. This will allow the user to link two 
      * fragments together.  
      * 
      * @param frag  fragment reference corresponding to the current fragment
      * @param cho   reference to the old choice within that position (null if new)
      */
     public void addChoice(Fragment frag, Choice cho){
         List<Choice> choices = frag.getChoices();
         choices.add(cho);
         frag.setChoices(choices);
         List<String> displayOrder = frag.getDisplayOrder();
         displayOrder.add("c");
         frag.setDisplayOrder(displayOrder);
     }
 
     /**
      * Removes the Choice at the given dispNum
      * returns true if successful
      * 
      * @param frag  fragment reference corresponding to the current fragment
      * @param dispNum   refers to the position of the text segment
      */
     private boolean deleteChoice(Fragment frag, int dispNum){
         List<String> textSegments = frag.getTextSegments();
         List<String> displayOrder = frag.getDisplayOrder();
 
         //not a text segment at dispNum
         if (!displayOrder.get(dispNum).equals("c"))
             return false;
         
         int choiceNum=0;
         for (int i=0; i<dispNum;i++){
             if (displayOrder.get(i).equals("c"))
                 choiceNum++;
         }
 
         textSegments.remove(choiceNum);
         frag.setTextSegments(textSegments);
         displayOrder.remove(dispNum);
         frag.setDisplayOrder(displayOrder);
         return true;
     }
      
     @Override
     /**
      * adds a new element into the listview so that a segment can be added.  
      * 
      * @param frag  fragment reference corresponding to the current fragment
      */
     public void addEmptyPart(Fragment frag){
         List<String> displayOrder = frag.getDisplayOrder();
         displayOrder.add("e");
         frag.setDisplayOrder(displayOrder);
     }
     
     @Override
     /**
      * deletes the selected segment when the user desires to delete a segment. 
      * 
      * @param frag  fragment reference corresponding to the current fragment
      */
     public void removeEmptyPart(Fragment frag){
         List<String> displayOrder = frag.getDisplayOrder();
         displayOrder.remove("e");
         frag.setDisplayOrder(displayOrder);
     }
 
     //  @Override
     //  public void addAnnotation(Fragment frag, Annotation annotation){
     //      Annotation annotations = frag.getAnnotations();
     //      annotations.addAnnotation(annotation);
     //      frag.setAnnotations(annotations);
     //  }
     
     @Override
     public void deleteFragmentPart(Fragment frag, int partNum){
         List<String> displayOrder = frag.getDisplayOrder();
         //check for invalid partNum
         if (partNum >= displayOrder.size())
             return;
         
         String type = displayOrder.get(partNum);
         
         if (type.equals("t"))
             deleteTextSegment(frag, partNum);
         else if (type.equals("i"))
             deleteIllustration(frag, partNum);
         else if (type.equals("c"))
             deleteChoice(frag, partNum);
         else if (type.equals("e"))
             removeEmptyPart(frag);
 
     }
 }
