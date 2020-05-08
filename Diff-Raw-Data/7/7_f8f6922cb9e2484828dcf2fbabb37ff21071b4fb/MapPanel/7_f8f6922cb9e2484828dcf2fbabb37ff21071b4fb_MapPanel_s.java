 package client;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.JPanel;
 
 
 
 @SuppressWarnings("serial")
 public class MapPanel extends JPanel implements MouseListener{
 
    boolean welcome;
    Map map;
    int borderLength;
    int boxLength;
    int boxHeight;
 
    public MapPanel(Map map){
       welcome = true;
       this.repaint();
       this.map = map;
       this.addMouseListener(this);
       
    }
 
    public void setWelcome(boolean isWelcome){
       welcome = isWelcome;
    }
 
    public boolean getWelcome(){
       return welcome;
    }
 
    public int getBorderLength(){
       return this.borderLength;
    }
 
    public void setOtherPlayerPostion(String name, Point p){
       for(int i=0; i<map.MAXOTHERPLAYERS; i++){
          if (name.equals(map.otherPlayers[i].getName())){
             map.otherPlayers[i].setLocation(p);
          }
       }
    }
 
    public void drawBoard(Graphics g){
 
       borderLength = Math.min(this.getWidth()/10, this.getHeight()/10);
 
       int maxLength = this.getWidth() - 2*borderLength;
       int maxHeight = this.getHeight() - 2*borderLength;
 
       boxLength = maxLength/16;
       boxHeight = maxHeight/26;
 
       //draw lines
       g.setColor(Color.lightGray);
       for(int x=0; x<maxLength; x+= maxLength/16){
          g.drawLine(x+borderLength, 0, x+borderLength, this.getHeight());
       }
       for(int y=0; y<maxHeight; y+= maxHeight/26){
          g.drawLine(0, y+borderLength, this.getWidth(), y+borderLength);
       }
 
       //fill border
       g.setColor(Color.blue);
       g.fillRect(0, 0, borderLength, this.getHeight());
       g.fillRect(0, 0, this.getWidth(), borderLength);
       g.fillRect(0, this.getHeight()-borderLength, this.getWidth(), borderLength);
       g.fillRect(this.getWidth()-borderLength, 0, borderLength, this.getHeight());
    }
 
    public void drawGadgets(Graphics g){
       g.setColor(Color.green);
 
       for(int i=0; i<map.GadgetLocations.length; i++){
          int x = map.GadgetLocations[i].x;
          int y = map.GadgetLocations[i].y;
          g.fillRect(borderLength+x*boxLength, borderLength+y*boxHeight, boxLength/2, boxHeight/2);
       }
    }
 
    public void drawWeapons(Graphics g){
       g.setColor(Color.blue);
 
       for(int i=0; i<map.WeaponLocations.length; i++){
          int x = map.WeaponLocations[i].x;
          int y = map.WeaponLocations[i].y;
          g.fillRect(borderLength+x*boxLength, borderLength+y*boxHeight, boxLength/2, boxHeight/2);
       }
    }
 
    public void drawTeleports(Graphics g){
       g.setColor(Color.yellow);
 
       for(int i=0; i<map.TeleporterLocations.length; i++){
          int x = map.TeleporterLocations[i].x;
          int y = map.TeleporterLocations[i].y;
          g.fillRect(borderLength+x*boxLength, borderLength+y*boxHeight, boxLength/2, boxHeight/2);
       }
    }
 
    public void drawWalls(Graphics g){
       g.setColor(Color.black);
       for(int i=0; i<map.WallLocations.length; i++){
          int x1 = map.WallLocations[i][0].x;
          int y1 = map.WallLocations[i][0].y;
          int x2 = map.WallLocations[i][1].x;
          int y2 = map.WallLocations[i][1].y;
 
          int length = Math.max((x2 - x1)*boxLength, 2);
          int height = Math.max((y2 - y1)*boxHeight, 2);
 
          g.fillRect(borderLength+x1*boxLength, borderLength+y1*boxHeight, length, height);
       }
    }
 
    public void printStats(Graphics g, int health, int speed, int accuracy){
       g.setColor(Color.green);
       g.drawString(map.player.getName(), 0, 10);
       g.drawString(" Health = " + health + " Speed = " + speed + "Accuracy = " + accuracy, this.getWidth()/2, 10);
    }
 
    public void drawPlayer(Graphics g){
       g.setColor(Color.red);
       if (map.player.isDead){
          g.setColor(Color.black);
       }
       else{
          g.setColor(Color.red);
       }
       g.fillRect(map.player.getLocation().x*boxLength+borderLength+2, map.player.getLocation().y*boxHeight+borderLength+2, boxLength-5, boxHeight-5);
    }
 
    public void drawOtherPlayers(Graphics g){
       g.setColor(Color.orange);
 
       for(int i=0; i<map.MAXOTHERPLAYERS; i++){
          if (map.otherPlayers[i] != null){
             if (map.otherPlayers[i].isDead){
                g.setColor(Color.black);
             }
             else{
                g.setColor(Color.orange);
             }
             Point p = map.otherPlayers[i].getLocation();
             g.fillRect(p.x*boxLength+borderLength+2, p.y*boxHeight+borderLength+2, boxLength-5, boxHeight-5);
          }
       }
       
    }
 
    public void drawHand(Graphics g){
       if(map.player != null){
          for(int i=0; i<map.player.getNumCardsInHand(); i++){
             //if is gadget, color = green
             //if is weapon, color = blue
             //if is frag, color = yellow
             g.setColor(Color.pink);
                
             g.fillRect(0, this.getHeight()-borderLength, this.getWidth()*i/map.player.getMaxHandSize(), borderLength);
             
             g.setColor(Color.black);
             g.drawString(map.player.getHand()[i].getName(), this.getWidth()*i/map.player.getMaxHandSize(), this.getHeight()-borderLength);
          }
       }
    }
    
    public void paint(Graphics g){
 
       g.clearRect(0, 0, this.getWidth(), this.getHeight());
 
       if(welcome){
          g.drawString("Welcome, press enter to continue", this.getWidth()/2, this.getHeight()/2);
       }
       else{
          drawBoard(g);
          drawGadgets(g);
          drawWeapons(g);
          drawTeleports(g);
          drawWalls(g);
          drawOtherPlayers(g);
          drawHand(g);
          if (map.player != null){
             drawPlayer(g);
             printStats(g, map.player.getHealth(), map.player.getSpeed(), map.player.getAccuracy());
          }
       }
    }
 
    public int getBoxLength() {
       return boxLength;
    }
 
    public int getBoxHeight(){
       return boxHeight;
    }
 
    @Override
    public void mouseClicked(MouseEvent e) {
 
       Point location = e.getPoint();
 
       if (boxLength <= 0 || boxHeight <= 0){
          return;
       }
 
       int x = (location.x-borderLength)/boxLength;
       int y = (location.y-borderLength)/boxHeight;
 
       for(int i=0; i<map.MAXOTHERPLAYERS; i++){
         Point p = map.otherPlayers[i].getLocation();
         if(p.x == x && p.y == y){
                map.resolveAttack(new Point(x, y), map.otherPlayers[i]);
          }
       }
    }
 
    @Override
    public void mouseEntered(MouseEvent e) {}
 
    @Override
    public void mouseExited(MouseEvent e) {}
 
    @Override
    public void mousePressed(MouseEvent e) {}
 
    @Override
    public void mouseReleased(MouseEvent e) {}
 }
 
