 import smoke11.DebugView;
 import smoke11.pudparser.Tile;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.image.BufferedImage;
 
 /**
  * Created with IntelliJ IDEA.
  * User: nao
  * Date: 07.03.13
  * Time: 19:09
  * To change this template use File | Settings | File Templates.
  */
 public class MapViewPanel extends JPanel implements IToolboxListenerMapPanel {   //http://stackoverflow.com/questions/148478/java-2d-drawing-optimal-performance
     private BufferedImage[] terrainSprites;
     private BufferedImage[] unitSprites;
     private Tile[][] mapTiles;
     private Tile[][] unitTiles;
     private int cameraOffsetX=0,cameraOffsetY=0;
     private int mouseLastX=0,mouseLastY=0; //used for cameraoffset
     private int actualMouseX=0, actualMouseY=0;
     private boolean drawText = false;
     private boolean drawTileBoxAlways =false;//used for drawing tile size boxed which follows mouse cursor
     private boolean drawTileBox = false;  //used for drawing only tilebox and clean tile with last tilebox position
     private boolean drawTerrain = true;
     private boolean drawUnits = true;
     private boolean drawUnitInfo = true;
     private boolean leftMouseClicked =false ,rightMouseClicked=false;
     public MapViewPanel(Dimension d)
     {
         this.setPreferredSize(d);
         this.setBackground(Color.orange);
         super.addMouseListener(new MouseListener() {
             @Override
             public void mouseReleased(MouseEvent e) {
                 //System.out.println(":"+getClass().getName()+"MOUSE_RELEASED_EVENT:");
                 if(e.getButton()== MouseEvent.BUTTON3)
                 {
                     rightMouseClicked =false;
                     if(drawUnitInfo)
                         repaint();
                 }
                 else if (e.getButton()== MouseEvent.BUTTON1)
                     leftMouseClicked=false;
             }
             @Override
             public void mousePressed(MouseEvent e) {
                 //System.out.println(":"+getClass().getName()+"MOUSE_PRESSED_EVENT:");
                     mouseLastX=e.getX();
                     mouseLastY=e.getY();
 
 /*
                 if(e.isAltDown())
                     DebugView.writeDebug(DebugView.DEBUGLVL_LESSINFO,this.getClass().getSimpleName(),"isAltDown");
                 if(e.isMetaDown())
                     DebugView.writeDebug(DebugView.DEBUGLVL_LESSINFO,this.getClass().getSimpleName(),"isMetaDown");
                 if(e.isShiftDown())
                     DebugView.writeDebug(DebugView.DEBUGLVL_LESSINFO,this.getClass().getSimpleName(),"isShiftDown");
                 if(e.isControlDown())
                     DebugView.writeDebug(DebugView.DEBUGLVL_LESSINFO,this.getClass().getSimpleName(),"isControlDown");
                 if(e.isAltGraphDown())
                     DebugView.writeDebug(DebugView.DEBUGLVL_LESSINFO,this.getClass().getSimpleName(),"isAltGraphdown");
 */
 
                 if(e.getButton()== MouseEvent.BUTTON3)
                 {
                     rightMouseClicked=true;
                     if(drawUnitInfo)
                         repaint();
                 }
                 else if(e.getButton()== MouseEvent.BUTTON1)
                     leftMouseClicked =true;
                 if(!drawTileBoxAlways)
                 {
                     drawTileBox=true;
                     repaint();
                 }
             }
             @Override
             public void mouseExited(MouseEvent e) {
                 //System.out.println(":"+getClass().getName()+"MOUSE_EXITED_EVENT:");
             }
             @Override
             public void mouseEntered(MouseEvent e) {
                 //System.out.println(":"+getClass().getName()+"MOUSE_ENTER_EVENT:");
             }
             @Override
             public void mouseClicked(MouseEvent e) {
                 //System.out.println(":"+getClass().getName()+"MOUSE_CLICK_EVENT:");
 
             }
         });
 
         super.addMouseMotionListener(new MouseMotionListener() {
             @Override
             public void mouseDragged(MouseEvent e) {
                 if(!e.isMetaDown()&& !e.isAltDown())
                 {
                     //move camera
                     actualMouseX=e.getX();
                     actualMouseY=e.getY();
 
                     cameraOffsetX+=actualMouseX-mouseLastX;
                     cameraOffsetY+=actualMouseY-mouseLastY;
                     //System.out.println("Camera Offset: "+cameraOffsetX+", "+cameraOffsetY);
                     drawTerrain=true;
 
                     mouseLastX=actualMouseX;
                     mouseLastY=actualMouseY;
                     if(!drawTileBoxAlways) //if not drawing TileBox that means we need to specify when repaint, because when drawing TileBox its repaiting all the time
                         drawTileBox=true;
                     repaint();
                 }
             }
 
             @Override
             public void mouseMoved(MouseEvent e) {
                 actualMouseX=e.getX();
                 actualMouseY=e.getY();
                 if(drawTileBoxAlways)
                 {
                     drawTileBox=true;
                     repaint();
                 }
 
             }
         });
 
     }
     private void makeRepaint()
     {
         super.repaint();
     }
     public void setImages(BufferedImage[] unitsSprites, BufferedImage[] tileSprites)
     {
         terrainSprites =tileSprites;
         unitSprites=unitsSprites;
     }
     public void prepareTiles(Tile[][] MapTiles, Tile[][] UnitTiles)
     {
 
         mapTiles = MapTiles;
         unitTiles = UnitTiles;
 
     }
     @Override public void paintComponent(Graphics g) {
         super.paintComponent(g);    // paints background
         if(mapTiles!=null)
         {
 
             Graphics2D g2d = (Graphics2D) g;
 
             int x, y;
             Font f = new Font("serif", Font.PLAIN, 10);
             g2d.setFont(f);
             if(drawTerrain)
             {
                 for (x=0;x<mapTiles.length;x++)
                 {
                     for(y=0;y<mapTiles[0].length;y++)
                         if (mapTiles[x][y] != null) {
                             g2d.drawImage(terrainSprites[mapTiles[x][y].ID], cameraOffsetX + x * 32, cameraOffsetY + y * 32, this);
                         } else {
                             g2d.setColor(Color.BLACK);
                             g2d.fillRect(cameraOffsetX + x * 32, cameraOffsetY + y * 32, 32, 32);
                             g2d.setColor(Color.CYAN);
                             g2d.drawString(mapTiles[x][y].PudID, cameraOffsetX + 4 + x * 32, cameraOffsetY + 10 + y * 32);
                         }
                 }
                 for (x=0;x<mapTiles.length;x++)
                 {
                     for(y=0;y<mapTiles[0].length;y++)
                     {
                         if(drawUnits)
                         {
                             if(unitTiles[x][y]!=null)
                             {
                                 if(unitSprites[unitTiles[x][y].ID]!=null)
                                 {
                                 if(checkIfBuilding(x,y)) // if building draw normally
                                     g2d.drawImage(unitSprites[unitTiles[x][y].ID], cameraOffsetX+x*32, cameraOffsetY+y*32, this);
                                 else //if not building calculate real drawing x,y for unit and draw
                                 {
                                     Point truepos = calculateUnitDrawingPosition(x,y,1,unitSprites[unitTiles[x][y].ID]);
                                     g2d.drawImage(unitSprites[unitTiles[x][y].ID], cameraOffsetX+truepos.x, cameraOffsetY+truepos.y, this);
                                 }
                                 }
                             }
                         }
                         if(drawText)
                         {
                             g2d.setColor(Color.CYAN);
                             g2d.drawString(mapTiles[x][y].PudID,cameraOffsetX+5+x*32,cameraOffsetY+10+y*32);
                             if(unitTiles[x][y]!=null)
                             {
                                 g2d.setColor(Color.RED);
                                 g2d.drawString(unitTiles[x][y].PudID,cameraOffsetX+5+x*32,cameraOffsetY+20+y*32);
                             }
                         }
 
                     }
                 }
             }
             if(drawTileBox)
             {
                int tilex = (actualMouseX-cameraOffsetX)/32;  //TODO: calculate this on begin of method
                int tiley = (actualMouseY-cameraOffsetY)/32;
                 int unitX, unitY;
                 Point p = getUnitPosForMousePos(tilex,tiley);
                 if(p!=null&&drawUnits)//check if mouse hovers on some building, if yes, draw tilebox contaning this unit
                 {
                     unitX=p.x;
                     unitY=p.y;
                     g2d.setColor(Color.WHITE);
                     if(checkIfBuilding(unitX,unitY))
                         g2d.drawRect(cameraOffsetX+unitX*32,cameraOffsetY+unitY*32,unitTiles[unitX][unitY].SizeX,unitTiles[unitX][unitY].SizeY);
                     else //if not building calculate real drawing x,y for unit and draw
                     {
                         Point truepos = calculateUnitDrawingPosition(unitX,unitY,1,unitSprites[unitTiles[unitX][unitY].ID]);
                         g2d.drawRect(cameraOffsetX+truepos.x, cameraOffsetY+truepos.y,unitTiles[unitX][unitY].SizeX,unitTiles[unitX][unitY].SizeY);
                         //g2d.drawImage(unitSprites[unitTiles[x][y].ID], cameraOffsetX+truepos.x, cameraOffsetY+truepos.y, this);
                     }
 
                 }
                 else //draw regular tilebox
                 {
                     g2d.setColor(Color.WHITE);
                     g2d.drawRect(cameraOffsetX+tilex*32,cameraOffsetY+tiley*32,32,32);
                 }
                 drawTileBox=false;
             }
             if(drawUnitInfo&&rightMouseClicked)
             {
                int tilex = (actualMouseX-cameraOffsetX)/32;  //TODO: calculate this on begin of method
                int tiley = (actualMouseY-cameraOffsetY)/32;
                 int tileX, tileY, actualX,actualY;
                 Point p = getUnitPosForMousePos(tilex,tiley);
                 if(p!=null)
                 {
                     g2d.setColor(Color.BLACK);
                     g2d.fillRect(actualMouseX,actualMouseY,150,65);
                     tileX=p.x;
                     tileY=p.y;
                     f = new Font("serif", Font.PLAIN, 10);
                     g2d.setFont(f);
                     actualX= actualMouseX;
                     actualY=actualMouseY;
                     int starty=actualY;
                     g2d.setColor(Color.GREEN);
                     actualX+=10;
                     actualY+=15;
                     g2d.drawString("Name: "+unitTiles[tileX][tileY].Name,actualX,actualY);
                     actualY+=10;
                     g2d.drawString("ID: "+unitTiles[tileX][tileY].ID,actualX,actualY);
                     actualY+=10;
                     g2d.drawString("PUDID: "+unitTiles[tileX][tileY].PudID,actualX,actualY);
                     actualY+=10;
                     g2d.drawString("OffsetX: "+unitTiles[tileX][tileY].OffsetX,actualX,actualY);
                     actualY+=10;
                     g2d.drawString("OffsetY: "+unitTiles[tileX][tileY].OffsetY,actualX,actualY);
                     g2d.drawRect(actualMouseX,actualMouseY,150,65);
                 }
 
 
 
             }
 
         }
     }
 
     private Point calculateUnitDrawingPosition(int tileX, int tileY, int howManyTilesUse, BufferedImage unitSprite) //howManyTilesUse, for most of units its 1(1x1) (for air and water its 2x2), its how much space it can take
     {
          Point result = new Point(tileX*32,tileY*32);
         int spritesizex = result.x+unitSprite.getWidth();
         int tilesizex = result.x+howManyTilesUse*32;
          int spritesizey = result.y+unitSprite.getHeight();
          int tilesizey = result.y+howManyTilesUse*32;
          int newx=tileX*32,newy=tileY*32;
         if(spritesizex>tilesizex)//if sprite taking more space that tile can offer, move starting x
             newx=result.x-(spritesizex-tilesizex)/2;
         if(spritesizey>tilesizey)//if sprite taking more space that tile can offer, move starting y
             newy=result.y-(spritesizey-tilesizey)/2;
         if(checkIfAirUnit(tileX,tileY))
             newy-=10; //move air unit higher
         return new Point(newx,newy); //returning point is already multiply by 32
     }
     private boolean checkIfRectContainsPoint(Rectangle rect, int x, int y)
     {
         return rect.contains(x,y);
     }
     private boolean checkIfBuilding(int tilex, int tiley)   //TODO: move this type of methods to wc2utils package or something like that
     {
         return tilex >= 0 && tiley >= -0 && ((unitTiles[tilex][tiley].Name.contains("Human") && !unitTiles[tilex][tiley].Name.contains("Transport") && !unitTiles[tilex][tiley].Name.contains("Tanker")) || (unitTiles[tilex][tiley].Name.contains("Orc") && !unitTiles[tilex][tiley].Name.contains("Transport") && !unitTiles[tilex][tiley].Name.contains("Tanker")) || unitTiles[tilex][tiley].Name.contains("Gold Mine") || unitTiles[tilex][tiley].Name.contains("Oil Patch"));
     }
     private boolean checkIfAirUnit(int tilex, int tiley)
     {
         return tilex >= 0 && tiley >= -0 && ((unitTiles[tilex][tiley].Name.contains("Gnomish Flying Machine")) || (unitTiles[tilex][tiley].Name.contains("Gryphon Rider")) || (unitTiles[tilex][tiley].Name.contains("Goblin Zepplin")) || (unitTiles[tilex][tiley].Name.contains("Dragon")));
     }
     private Point getUnitPosForMousePos(int tileX, int tileY) //put tile x,y for mouse position, return null for no
     {
         int x1=(tileX-3)<0?0:(tileX-3);
         int y1=(tileY-3)<0?0:(tileY-3);
 
         for(int x=tileX;x>=x1;x--)
         {
             for(int y=tileY;y>=y1;y--)
             {
                 if(unitTiles[x][y]==null)
                     continue;
                 if(checkIfRectContainsPoint(new Rectangle(x*32,y*32,unitTiles[x][y].SizeX,unitTiles[x][y].SizeY),tileX*32,tileY*32))
                     return new Point(x,y);
             }
         }
         return null;
     }
     @Override
     public void showID(ToolboxEvents e) {
         drawText =!drawText;
         DebugView.writeDebug(DebugView.DEBUGLVL_LESSINFO, this.getClass().getSimpleName(), "Drawing id info: " + drawText);
         if(!drawTileBoxAlways)   //if not drawing TileBox that means we need to specify when repaint, because when drawing TileBox its repaiting all the time
             makeRepaint();
     }
 
     @Override
     public void drawTilebox(ToolboxEvents e) {
        drawTileBoxAlways=!drawTileBoxAlways;
     }
 
     @Override
     public void drawUnits(ToolboxEvents e) {
         drawUnits=!drawUnits;
         repaint();
     }
 }
 
