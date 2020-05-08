 package doom;
 
 import java.awt.GraphicsEnvironment;
 import java.util.Arrays;
 
 /*-----------------------------------------------------*/
 //Achsenbelegung!
 // x = Zeilen von oben nach unten
 // y = Spalten von links nach rechts
 /*-----------------------------------------------------*/
 
public class GameMain {
 
 	private static World world;
         private static Gui mainWindow;
         
         //main-function
 	public static void main (String[] args) {
             GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
             String[] fontNames = ge.getAvailableFontFamilyNames();
 
             if(Arrays.asList(fontNames).contains("Courier New")){
                 runGame();  
               
             }else{
                 FontError fontWindow = new FontError();
                 fontWindow.setVisible(true);}
             
             
             
                 
         }
         
         //Opens Main Window and starts the Game
         public static void runGame(){
             mainWindow = new Gui();
                 world = new World();
                 world.draw(mainWindow);
                 mainWindow.setVisible(true);
         }
         
         //function to reDraw the canvas
         public static void  play(String s) {
             world.move(s);
             world.draw(mainWindow);
             
         }
         
         public static void triggerItems(String s){
             world.useItem(s, mainWindow);
         }
 }
 
 
 
 
 
 
 
 
 
