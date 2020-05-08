 import java.awt.event.KeyEvent;
 
 public class CONST {
 
     // GUI
     public static final int windowWidth = 1000;
     public static final int windowHeight = 700;
     public static final int graphWidth = 700;
     public static final int menuHeight = 25;
     public static final int queueHeight = 75;
     public static final int graphHeight = 500;
     public static final int controlsButtonsHeight = 50;
     public static final int controlsHeight = 1;
     public static final int controlsWidth = 700;
     public static final int zoomWindowWidth = 300;
     public static final int zoomWindowHeight = 250;
     public static final int informationWidth = 300;
     public static final int informationHeight = 250;
 
     // Graph
     public static final int vertexSize = 10;
 
     // Keys
     public static final int moveKey = KeyEvent.VK_SHIFT;
     public static final int deleteKey = KeyEvent.VK_CONTROL;
 
     // Queue
     // o kolko sa zrychli beh, pri stlaceni forward
     public static final double speedFactor = 1.2;
     public static final double messageSpeedLimit = 1.0;
 
     // nasledovne veci su pre enumeracie zbytocne
     // to zaba : ked si pozries http://stackoverflow.com/questions/5021246/conveniently-map-between-enum-and-int-string/5021384#5021384
     // tak mozes nasledovny kod zmazat
     /*public static int AnonymToInt(Anonym a) {
         if (a == Anonym.anonymOff)
             return 0;
         return 1;
     }*/
 
     /*public static int SynchronedToInt(Synchroned a) {
         if (a == Synchroned.synchronedOff)
             return 0;
         return 1;
     }*/
 
     /*public static int GraphTypeToInt(GraphType g) {
         if (g == GraphType.clique)
             return 1;
         if (g == GraphType.cycle)
             return 2;
         return 0;
     }*/
 
     /*public static Anonym IntToAnonym(int a) {
         if (a == 0)
             return Anonym.anonymOff;
         return Anonym.anonymOn;
     }*/
 
     /*public static Synchroned IntToSynchroned(int a) {
         if (a == 0)
             return Synchroned.synchronedOff;
         return Synchroned.synchronedOn;
     }*/
 
     /*public static GraphType IntToGraphType(int a) {
         if (a == 1)
             return GraphType.clique;
         if (a == 2)
             return GraphType.cycle;
         return GraphType.none;
     }*/
 
     /*public static Anonym StringToAnonym(String s) {
         if (s.equals("yes"))
             return Anonym.anonymOn;
         return Anonym.anonymOff;
     }*/
 
     /*public static Synchroned StringToSynchroned(String s) {
         if (s.equals("yes"))
             return Synchroned.synchronedOn;
         return Synchroned.synchronedOff;
     }*/
 
     /*public static GraphType StringToGraphType(String s) {
         if (s.equals("clique"))
             return GraphType.clique;
         if (s.equals("cycle"))
             return GraphType.cycle;
         return GraphType.none;
     }*/
 
 }
 
 enum RunState {
     stopped, paused, running
 }
 
 enum Preference {
     begin, end, special, wrap
 }
 
 enum CubeState {
     alive, asleep, wakeup, dead
 }
 
 enum Constrast {
     textbw, borderbw, invert
 }
 
 enum Settings {
     anonym, synchroned, graphType
 }
 
enum 




 enum Anonym {
     anonymOn, anonymOff
 }
 
 enum Synchroned {
     synchronedOn, synchronedOff
 }
 
 enum GraphType {
     any, cycle, clique, grid, wheel
 }


