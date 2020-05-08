 package ui;
 
 import Persist.PersistentStorage;
 
 /**
  *
  * @author anasalkhatib
  */
 public class Main {
 
     /**
      *
      * @param args Array of any command line arguments passed in
      */
     public static void main(String args[]) {
         UserInterface gui = UserInterface.getSwingUserInterface();
         gui.display();
         PersistentStorage persistentStorage = PersistentStorage.getFileSystemStorage();
        //persistentStorage.load(); ???
     }
 }
