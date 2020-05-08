 package vooga.rts.gui.menus;
 
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Observable;
 import javax.imageio.ImageIO;
 import vooga.rts.commands.ClickCommand;
 import vooga.rts.commands.Command;
 import vooga.rts.commands.PositionCommand;
 import vooga.rts.gui.Button;
 import vooga.rts.gui.Menu;
 import vooga.rts.gui.Window;
 import vooga.rts.gui.buttons.ImageButton;
 import vooga.rts.gui.buttons.MainMenuButton;
 import vooga.rts.resourcemanager.ResourceManager;
 import vooga.rts.util.Location;
 
 
 public class GameMenu extends Menu {
 
     private static final String BG_IMAGE_LOCATION =
             "images/gamemenu/menu_bg.png";
     private static final String EXIT_IMAGE_LOCATION =
             "images/gamemenu/menu_button.png";
     private static final String ACTION_IMAGE_LOCATION =
             "images/gamemenu/action_button.png";
     private BufferedImage myBGImage;
     private static final Dimension ACTION_BUTTON_DIMENSION = new Dimension(50, 50);
 
     private static final Dimension EXIT_BUTTON_DIMENSION = new Dimension(200, 40);
     private static final Location EXIT_BUTTON_LOCATION =
             new Location(Window.SCREEN_SIZE.getWidth() - EXIT_BUTTON_DIMENSION.getWidth(), 0);
     private static final int ACTION_MENU_WIDTH = 350;
     private static final int ACTION_MENU_HEIGHT = 180;
 
     private Button myExitButton;
     private ArrayList<Button> myActionButtons;
 
     public GameMenu () {
        File bgImage = new File(BG_IMAGE_LOCATION);

         myBGImage =
                 ResourceManager.getInstance().<BufferedImage> getFile(BG_IMAGE_LOCATION,
                                                                       BufferedImage.class);
         setBGImage(myBGImage);
 
         myExitButton =
                 new ImageButton(EXIT_IMAGE_LOCATION, EXIT_BUTTON_DIMENSION, EXIT_BUTTON_LOCATION);
         addButton(myExitButton);
 
         myActionButtons = new ArrayList<Button>();
         createActionButtons();
     }
 
     private void createActionButtons () {
         int iy = (int) (Window.SCREEN_SIZE.getHeight() - ACTION_MENU_HEIGHT);
         int ix = (int) (Window.SCREEN_SIZE.getWidth() - ACTION_MENU_WIDTH);
         int xPadding = 30;
         int yPadding = 30;
         int numPerRow = 4;
 
         int ly = iy + yPadding;
 
         for (int i = 1; i <= numPerRow; i++) {
             int lx = ix + 
                     (int) (ACTION_BUTTON_DIMENSION.getWidth() * (i - 1) + xPadding * i);
             Button b =
                     new ImageButton(ACTION_IMAGE_LOCATION, ACTION_BUTTON_DIMENSION,
                                     new Location(lx, ly));
             myActionButtons.add(b);
             addButton(b);
         }
         ly += yPadding + ACTION_BUTTON_DIMENSION.getHeight();
         for (int i = 1; i <= numPerRow; i++) {
             int lx = ix + 
                     (int) (ACTION_BUTTON_DIMENSION.getWidth() * (i - 1) + xPadding * i);
             Button b =
                     new ImageButton(ACTION_IMAGE_LOCATION, ACTION_BUTTON_DIMENSION,
                                     new Location(lx, ly));
             myActionButtons.add(b);
             addButton(b);
         }
     }
 
     public void setResources () {
 
     }
 
     public void setSelected () {
 
     }
 
     @Override
     protected void paintBG (Graphics2D pen) {
 
         int screenX = (int) Window.SCREEN_SIZE.getWidth();
         int screenY = (int) Window.SCREEN_SIZE.getHeight();
 
         int bgImgHeight = myBGImage.getHeight();
         int bgImgWidth = myBGImage.getWidth();
 
         int x = 0;
         int y = screenY - bgImgHeight;
 
        double xFactor = screenX / bgImgWidth;
         int newHeight = (int) (xFactor * bgImgHeight);
 
         pen.drawImage(myImage, x, y, screenX, newHeight, null);
 
     }
 
     @Override
     public void update (Observable o, Object arg) {
 
         if (o.equals(myExitButton)) {
             System.exit(0);
         }
 
         setChanged();
         notifyObservers(arg);
     }
 
 
 }
