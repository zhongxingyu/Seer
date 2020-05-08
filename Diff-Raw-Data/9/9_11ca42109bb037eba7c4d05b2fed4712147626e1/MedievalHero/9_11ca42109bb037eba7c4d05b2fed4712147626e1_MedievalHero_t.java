 package medievalhero;
 
 import java.awt.Color;
 import javax.swing.JFrame;
 import java.io.IOException;
 
 public class MedievalHero extends JFrame {
 
     public MedievalHero() {
         try {
             add(new Board());
         } catch (IOException e) {
             System.out.println("fout - " + e);
         }
         
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         setSize(800, 678);
         setLocationRelativeTo(null);
         setTitle("Medieval Hero  --  V 0.0.001  --  Basics");
         setVisible(true);
        setResizable(false);
         
     }
     
     public static void main(String[] args) {
         new MedievalHero();
     }
 }
