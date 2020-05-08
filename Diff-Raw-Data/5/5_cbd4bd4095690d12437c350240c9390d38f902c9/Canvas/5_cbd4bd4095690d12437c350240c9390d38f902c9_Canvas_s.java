 package dungeon.ui.screens;
 
 import dungeon.load.messages.LevelLoadedEvent;
 import dungeon.messages.Message;
 import dungeon.messages.MessageHandler;
 import dungeon.models.*;
 import dungeon.models.messages.Transform;
 
 import javax.swing.*;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 
 public class Canvas extends JPanel implements MessageHandler {
   private final Color blockingTile = new Color(181, 125, 147);
 
   private final Color passableTile = new Color(139, 108, 217);
 
   private final Color victoryTile = new Color(255, 244, 25);
 
   private final Color teleporterTile = new Color(0, 0, 0);
 
   private final Color playerColor = new Color(101, 202, 227);
 
   private final Color enemyColor = new Color(33, 237, 60);
 
   private final Color savePointColor = new Color(50, 122, 88);
 
   private final Color hpColor = new Color(235, 58, 58);
 
  private final Color LifeColor = new Color(60, 179, 113);
 
   private final Color white = new Color(255, 255, 255);
 
   private final Color moneyColor = new Color(253, 225, 54);
 
   private final Color itemColor = new Color(151, 151, 151);
 
   private final Font font = new Font("Arial", Font.PLAIN, 20);
 
   private World world;
 
   /**
    * The unit to pixel conversion factors for the current room.
    */
   private double xPixelPerUnit;
 
   private double yPixelPerUnit;
 
   public Canvas () {
     this.setFocusable(true);
   }
 
   @Override
   public void handleMessage (Message message) {
     if (message instanceof Transform) {
       this.world = this.world.apply((Transform)message);
     } else if (message instanceof LevelLoadedEvent) {
       this.world = ((LevelLoadedEvent)message).getWorld();
     }
 
     repaint();
   }
 
   @Override
   protected void paintComponent (Graphics g) {
     super.paintComponent(g);
 
     if (this.world == null) {
       return;
     }
 
     Room room = this.world.getCurrentRoom();
 
     this.xPixelPerUnit = (double)g.getClipBounds().width / room.getXSize();
     this.yPixelPerUnit = (double)g.getClipBounds().height / room.getYSize();
 
     this.drawTiles(g, room);
     this.drawDrops(g, room);
     this.drawEnemies(g, room);
     this.drawSavepoints(g, room);
     this.drawPlayer(g, this.world.getPlayer());
     this.drawHpIndicator(g);
     this.drawMoneyIndicator(g);
     this.drawLifeIndicator(g);
 
   }
 
   private void drawTiles (Graphics g, Room room) {
     for (Tile tile : room.getTiles()) {
       if (tile instanceof TeleporterTile) {
         g.setColor(this.teleporterTile);
       } else if (tile instanceof VictoryTile) {
         g.setColor(this.victoryTile);
       } else if (tile.isBlocking()) {
         g.setColor(this.blockingTile);
       } else {
         g.setColor(this.passableTile);
       }
 
       this.drawSquare(g, tile.getPosition(), Tile.SIZE);
     }
   }
 
   private void drawDrops (Graphics g, Room room) {
     for (Drop drop : room.getDrops()) {
       if (drop.isMoney()) {
         g.setColor(this.moneyColor);
       } else {
         g.setColor(this.itemColor);
       }
 
       this.drawSquare(g, drop.getPosition(), Drop.SIZE);
     }
   }
 
   private void drawEnemies (Graphics g, Room room) {
     for (Enemy enemy : room.getEnemies()) {
       g.setColor(this.enemyColor);
 
       this.drawSquare(g, enemy.getPosition(), Enemy.SIZE);
     }
   }
 
   private void drawSavepoints (Graphics g, Room room) {
     for (SavePoint savePoint : room.getSavePoints()) {
       Position position = savePoint.getPosition();
 
       g.setColor(this.savePointColor);
       this.drawSquare(g, savePoint.getPosition(), savePoint.SIZE);
     }
   }
 
   private void drawPlayer (Graphics g, Player player) {
     g.setColor(this.playerColor);
 
     this.drawSquare(g, player.getPosition(), Player.SIZE);
   }
 
   private void drawHpIndicator (Graphics g) {
     g.setColor(this.hpColor);
     g.fillRect(20, 20, 20, 20);
 
     g.setColor(this.white);
     g.setFont(this.font);
     g.drawString(String.format("%d / %d", this.world.getPlayer().getHitPoints(), this.world.getPlayer().getMaxHitPoints()), 60, 38);
   }
 
   private void drawLifeIndicator (Graphics g) {
    g.setColor(this.LifeColor);
     g.fillRect(20, 100, 20, 20);
 
     g.setColor(this.white);
     g.setFont(this.font);
     g.drawString(String.format("%d", this.world.getPlayer().getLives()), 60, 118);
   }
 
   private void drawMoneyIndicator (Graphics g) {
     g.setColor(this.moneyColor);
     g.fillRect(20, 60, 20, 20);
 
     g.setColor(this.white);
     g.setFont(this.font);
     g.drawString(String.format("%d", this.world.getPlayer().getMoney()), 60, 78);
   }
 
   /**
    * Draw a square with the positions converted from our abstract unit to pixels.
    */
   private void drawSquare (Graphics g, Position position, int widthUnits) {
     g.fillRect((int)(position.getX() * this.xPixelPerUnit), (int)(position.getY() * this.yPixelPerUnit), (int)(widthUnits * this.xPixelPerUnit), (int)(widthUnits * this.yPixelPerUnit));
   }
 }
