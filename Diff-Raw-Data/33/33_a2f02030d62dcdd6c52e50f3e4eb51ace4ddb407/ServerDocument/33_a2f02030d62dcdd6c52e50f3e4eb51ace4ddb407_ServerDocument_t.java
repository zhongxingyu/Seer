 package backend;
 
 import java.util.ArrayList;
 
 import javax.swing.text.DefaultStyledDocument;
 //import javax.swing.event.DocumentListener;
 //import javax.swing.event.UndoableEditListener;
 //import javax.swing.text.AbstractDocument;
 //import javax.swing.text.AttributeSet;
 //import javax.swing.text.BadLocationException;
 //import javax.swing.text.Document;
 //import javax.swing.text.Element;
 //import javax.swing.text.Position;
 //import javax.swing.text.Segment;
 
 /**
  * The ServerDocument is the datatype that stores the document that is being edited.
  * It consists of an ArrayList of Edits.
  *
  */
 public class ServerDocument extends DefaultStyledDocument{
     private static final long serialVersionUID = 1L;
     private String docTitle;
     private ArrayList<Edit> content;
     private final String DOC_NAME = "document";
     //private final String SPLIT_CHAR = Character.toString((char) 0x2605);
     private final String SPLIT_CHAR = "|";
     
     /**
      * Constructor
      * @param title The title of the doc
      */
     public ServerDocument(String title) {
         docTitle = title;
         content = new ArrayList<Edit>();
     }  
     
     /**
      * Returns the title
      * @return The title
      */
     public String getTitle(){
         return docTitle;
     }
 
     /**
      * Sets the title
      * @param title The new title
      */
     public void setTitle(String title){
         docTitle = title;
     }
     
     /**
      * Updates the content from a string
      * @param c The string
      */
     public void updateContent(String c){
         content = stringToList(c);
         System.out.println("updated content is" + content.toString());
     }
     
     /**
      * Takes a string and turns it into a list of Edit object
      * TODO: Fix this, it is wrong
      * @param s
      * @return
      */
     private ArrayList<Edit> stringToList(String s){
         System.out.println("string is" + s);
         char[] newarray = s.toCharArray();
         System.out.println("array is" + newarray.toString() + "with size" + newarray.length);
         ArrayList<Edit> newList = new ArrayList<Edit>();
         for(int i = 0; i < newarray.length; i++){
             // TODO: Is having "document" here correct? I doubt it.
             newList.add(new Edit(String.valueOf(newarray[i]), DOC_NAME));
         }
         
         return newList;
     }
     
     /**
      * Essentially a toString method
      * @param e The list of Edits
      * @return The string version of the list
      */
     private String listToString(ArrayList<Edit> e){
         StringBuilder string = new StringBuilder("");
         int lines = 1;
         for (int i=0; i< e.size(); i++){
             if (e.get(i).getValue().equals("\n")) {
                 string.append(System.getProperty("line.separator"));
                 lines++;
             } else {
                 string.append(e.get(i).toString());
             }
         }
         String numLines = Integer.toString(lines);
         System.out.println("line counter is at: "+numLines);
        System.out.println("string: "+string);
         return numLines + SPLIT_CHAR + string.toString();
     }
     
     /**
      * Returns a String version of the entire doc
      * @return A String version of the entire doc
      */
     public String getDocContent(){
         return listToString(content);
     }
     
     /**
      * Inserts Edits into the Doc
      * @param edit The Edit to insert
      * @param location The location at which it would like to be inserted
      * @param client The name of the client who made the insertion
      * TODO: Maybe pass a cursor instead of a loc and call cursor.getLoc()
      * @return Success message
      */
     public synchronized String insertContent(Edit edit, String location, String client) {
         // Check if the location isn't the beginning or end, and that the location isn't 
         // in between two edits belonging to someone else, aka in a locked location
         
         int loc = Integer.valueOf(location);
         
         // lols this isn't working
         if (loc > 0 && loc < content.size() - 1) {
             if (!content.get(loc - 1).getOwner().equals(client) && !content.get(loc + 1).getOwner().equals(DOC_NAME)) {
                 if (content.get(loc - 1).getOwner().equals(content.get(loc + 1).getOwner())) {
                     return "LockedEdit";
                 }
             }
         }
         content.add(loc, edit);
         System.out.println("currently storing: "+listToString(content));
         return "InsertDone";
     }
     
     /**
      * Removes small or big deletions, checking for locks
      * @param begin The beginning location of the deletion
      * @param end The end location of the deletion
      * @param client The name of the client making the deletion
      * TODO: Maybe pass a cursor instead of locations
      * @return Success message
      */
     public synchronized String removeContent(String b, String e, String client) {
         int begin = Integer.parseInt(b);
         int end = Integer.parseInt(e);
 
         System.out.println("begin location: "+begin);
         System.out.println("end location: "+end);
         if (begin == end && begin >0 && begin < content.size()) {
             if (content.get(begin).getOwner().equals(client) || 
                 content.get(begin).getOwner().equals(DOC_NAME)) {
                
                 content.remove(begin);
                 System.out.println(listToString(content));
                 return "Done";
             } else {
                 return "SingleLock";
             }
             
         }
         int removeLoc = begin;
         boolean conflicts = false;
         for (int i = begin; i < end; i++) {
             if (content.get(i).getOwner().equals(DOC_NAME) || content.get(i).getOwner().equals(client)) {
                 content.remove(removeLoc);
             } else {
                 removeLoc++;
                 conflicts = true;
             }
         }
         
         if (conflicts) {
             return "SomeLocked";
         } else {
             return "Done";
         }
     }
     
     /**
      * Ends an edit by looping through itself and changing the owner of the 
      * finished edit to "document"
      * @param client The client who finished an edit
      * @return Success message
      */
     public synchronized String endEdit(String client) {
         for (int i = 0; i < content.size(); i++) {
             if (content.get(i).getOwner().equals(client)) {
                 content.get(i).changeOwner();
             }
         }
         return "EndEditDone";
     }
 }
