 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ru.gubsky.study.elib.models;
 
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.Date;
 
 /**
 
  @author GG
  */
 public class ClientModel
 {
     final static String HOST = "localhost";
     final static int PORT = 15755;
     final static int OPERATION_SEARCH = 1;
     final static int OPERATION_ADD_VIEW = 2;
     
     public String[] getGenres()
     {
         String[] str = new String[] {
             "новеллы",
             "рассказы",
             "компьютерная литература"
         };
         return str;
     }
 
     public String[] getAuthors()
     {
         String[] str = new String[] {
             "гоша",
             "иванов",
             "николай иванович"
         };
         return str;
     }
 
     public ArrayList<Book> getPopularBook(int start, int end)
     {
         ArrayList<Book> bukz = new ArrayList<>();
         for (int i = start; i <= end; i++) {
             Book b1 = new Book();
             b1.author = "gosha";
             b1.date = new Date(1000303);
             b1.genre = "genre";
             b1.name = "как я вышел погулять";
             b1.popularity = 10;
             bukz.add(b1);
         }
         return bukz;
     }
     
     public String getText(int bookId)
     {
         return "ajskdfjaksdjfk;ajsdfkjadfaksdjfkasjdfkjasdkfjaksdjfkajsdfkjasf"
                 + "aksdfjaksjdfklajsd;afjskdfjaksdfj"
                 + "askdfjak;dsjfk;asjdfkajsf"
                 + "askdfjaskfjaskfjakdfjakldfs"
                 + "afgaskgjaskgjaknkdfb"
                 + "dgaskgjaaskgjaaskgjaaskgjaaskgjaa"
                 + "gaskgjaaskgjaaskgjaaskgjaaskgjaa"
                 + "dsaskgjaaskgjaaskgjag"
                 + "asaskgjaaskgjaaskgjag"
                 + "adaskgjaaskgjaaskgjasg"
                 + "dsfgkdfjgkdjrgkdjgkd"
                 + "rgaskgjaaskgjaaskgjad"
                 + "gaskgjaaskgjaaskgjaaskgjaaskgja"
                 + "dgradjgkajsdgkajskdgjak;dgj";
     }
     
     public ArrayList<Book> getBooksBySearching(String text)
     {
         if (text == null || text.isEmpty()) {
            return new ArrayList<>();
         }
         
         ArrayList<Book> bukz = null;
         try {
             Socket sock = new Socket(HOST, PORT);
             
             // write
             ObjectOutputStream outStream = new ObjectOutputStream(sock.getOutputStream());
             outStream.writeInt(OPERATION_SEARCH);
             outStream.writeObject(text);
             outStream.flush();
             
             // read
             ObjectInputStream inStream = new ObjectInputStream(sock.getInputStream());
             bukz = (ArrayList<Book>) inStream.readObject();
             
             inStream.close();
             outStream.close();
             sock.close();
         } catch (Exception e) {
             System.err.println(e);
         }
         System.out.println("Client model: " + bukz);
         return bukz;
     }
     
     public void addViewForBookId(int id)
     {
         try {
             Socket sock = new Socket(HOST, PORT);
             
             // write
             ObjectOutputStream outStream = new ObjectOutputStream(sock.getOutputStream());
             outStream.writeInt(OPERATION_ADD_VIEW);
             outStream.writeInt(id);
             outStream.flush();
             
             outStream.close();
             sock.close();
         } catch (Exception e) {
             System.err.println(e);
         }
     }
 }
