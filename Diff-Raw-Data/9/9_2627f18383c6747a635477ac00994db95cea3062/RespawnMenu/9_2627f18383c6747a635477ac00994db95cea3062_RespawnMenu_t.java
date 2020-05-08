 package graphics;
 
 import javax.media.opengl.GL2;
 
 import game.Player.ShipType;
 import graphics.core.Texture;
 /**
  * Allows user to choose what ship to respawn as
  * @author Tim Mikeladze
  *
  */
 public class RespawnMenu extends HUDTools {
     private static boolean respawnOpen = false;
      static Texture respawnMenu;
     private static int selection = 0;
     private int s = HEIGHT / 2;
     private final static String FIGHTER = "assets/images/respawnmenu/fighter_selected.png";
     private final static String SCOUT = "assets/images/respawnmenu/scout_selected.png";
     private final static String BOMBER = "assets/images/respawnmenu/bomber_selected.png";
 
     public void drawRespawnMenu(GL2 gl) {
         this.gl = gl;
         if(isRespawnOpen()) {
             start2D();
             if(selection == 0) {
                 respawnMenu = Texture.findOrCreateByName(FIGHTER);
                 if(respawnMenu != null) {
                     respawnMenu.bind(gl);
                     gl.glBegin(GL2.GL_QUADS);
                     draw(-s / 2, -s / 2, s, s);
                     gl.glEnd();
                 }
             }
             if(selection == 1) {
                 respawnMenu = Texture.findOrCreateByName(SCOUT);
                 respawnMenu.bind(gl);
                 gl.glBegin(GL2.GL_QUADS);
                 draw(-s / 2, -s / 2, s, s);
                 gl.glEnd();
             }
             if(selection == 2) {
                 respawnMenu = Texture.findOrCreateByName(BOMBER);
                 respawnMenu.bind(gl);
                 gl.glBegin(GL2.GL_QUADS);
                 draw(-s / 2, -s / 2, s, s);
                 gl.glEnd();
             }
             if(isRespawnOpen() == false) {
                 unBind();
             }
             stop2D();
         }
     }
     public static void selectionUp() {
        if (selection > 0)
            selection --;
     }
     public static void selectionDown() {
        if (selection < 2)
            selection ++;
     }
     public static ShipType getSelection() {
         switch (selection) {
             case 0:
                 return ShipType.FIGHTER;
             case 1:
                 return ShipType.SCOUT;
             case 2:
                 return ShipType.BOMBER;
             default:
                 System.err.println("Should not happen check Respawn Menu");
                 return null;
         }
     }
     public static void setRespawnOpen(boolean respawnOpen) {
         RespawnMenu.respawnOpen = respawnOpen;
     }
     public static boolean isRespawnOpen() {
         return respawnOpen;
     }
 
 }
