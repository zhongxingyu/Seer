 package vahdin.view;
 
import java.util.Date;

 import vahdin.data.Bust;
 import vahdin.data.Mark;
 
 public class BustsSubview extends Subview {
 
     public BustsSubview() {
 
     }
 
     @Override
     public void show(String[] params) {
         // FOR TESTING ONLY
        Mark m1 = new Mark("Markin nimi", new Date(), "Kuvaus", 1, 1);
         m1.addBust(new Bust("Title", 0, "Kuvaus", 0, "aika", 2.2, 1.1));
         m1.addBust(new Bust("Title2", 1, "Toinen kuvaus", 1, "toka aika", 3.3,
                 4.4));
 
         String view = params[0];
         String markId = params[1];
 
         if (params.length > 0) {
             for (int i = 0; i < params.length; i++) {
                 System.out.println(params[i]);
                 if (params[i].equals("kissa")) {
                     System.out.println("NO LOL KISSA!");
                 }
             }
         }
 
         addStyleName("open");
         super.show(params);
     }
 }
