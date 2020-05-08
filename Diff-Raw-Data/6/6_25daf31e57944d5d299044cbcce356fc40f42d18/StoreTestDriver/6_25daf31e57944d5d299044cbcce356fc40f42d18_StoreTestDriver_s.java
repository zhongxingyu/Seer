 import java.io.FileNotFoundException;
 import java.util.LinkedList;
 
 public class StoreTestDriver
 {
     public static void main(String[] args)
     {
         MediaStore.Login("Kevin","nanana");
         
         try {
            MediaStore.addMedia(new Music("music111", "ffff", 3, 9.99, "1.mp3"));
            MediaStore.addMedia(new Music("music222", "ffff", 5, 11.99, "2.mp3"));
            MediaStore.addMedia(new AudioBook("book", "FDFDFD", 6, 8.57, "3.mp3"));
         } 
         catch (FileNotFoundException e)
         {
             System.err.println("Tried to add media that did not exist.\n" + e.getMessage());
         }
         LinkedList<Music> allMusic = MediaStore.listMusic();
         
         for (Music m : allMusic)
         {
             System.out.println(m.getTitle());
         }
         System.out.println("-----------------All--------------------------");
         for (Media m : MediaStore.getAllMedia())
         {
             System.out.println(m.getTitle());
         }
         System.out.println("----------------Books--------------------------");
         for (Media m : MediaStore.listAudioBooks())
         {
             System.out.println(m.getTitle());
         }
         
         try {
             MediaStore.purchaseProduct(1, "src/new.mp3");
         } catch (Exception e)
         {
             System.err.println("ERROR: could not copy files");
         }
     }
 }
