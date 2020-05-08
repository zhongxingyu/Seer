 package bomberman.client.gui;
 
 import bomberman.client.controller.Game;
 import bomberman.client.elements.Element;
 import bomberman.client.elements.Player;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import javax.swing.JPanel;
 
 public class Board extends JPanel {
 
     private List<Element> elements = null;
     private List<Integer> fire = new ArrayList();
     private Image sprite_floor;
     private Image fire_image;
     private int cols;
     private int rows;
     private int unit_pixels = 10;
     private int fire_duration = 1000;
 
     public Board() {
         super();
         this.sprite_floor = Window.getInstance().getToolkit().getImage("images/sprite_floor.png");
         this.fire_image = Window.getInstance().getToolkit().getImage("images/fire.png");
     }
 
     @Override
     public void paintComponent(Graphics g) {
         super.paintComponent(g);
         if (this.getElements() == null) {
             return;
         }
         Collection<Player> players = Game.getInstance().getPlayers().values();
 
         int size = this.cols * this.rows;
         for (int index = 0; index < size; index++) {
             int x = this.getPosX(index);
             int y = this.getPosY(index);
             g.drawImage(this.sprite_floor, x, y, this);
         }
 
         for (int index = 0; index < size; index++) {
             int x = this.getPosX(index);
             int y = this.getPosY(index);
 
             Element element = this.getElements().get(index);
             if (element != null) {
                 g.drawImage(element.getImage(), x, y, this);
             }
 
             if (this.fire.contains(index)) {
                 g.drawImage(this.fire_image, x, y, this);
             }
 
             for (Player player : players) {
                 if (player.getIndex() == index) {
                     if (player.getMovePogression() == 1) {
                         g.drawImage(player.getImage(), x, y, this);
                     } else {
                         int old_x = this.getPosX(player.getOldIndex());
                         int old_y = this.getPosY(player.getOldIndex());
                         int current_x = (old_x + (int) ((x - old_x) * player.getMovePogression()));
                         int current_y = (old_y + (int) ((y - old_y) * player.getMovePogression()));
                         g.drawImage(player.getImage(), current_x, current_y, this);
                     }
                 }
             }
         }
 
     }
 
     public void setData(List<Map> data) {
         this.elements = new ArrayList<Element>();
         this.rows = data.size() / this.cols;
         int size = data.size();
 
         for (int i = 0; i < size; i++) {
             try {
                 this.getElements().add(Element.factory(data.get(i)));
             } catch (Exception e) {
                 System.out.println(e.getMessage());
             }
         }
 
         Dimension panel_size = new Dimension((3 * this.cols + this.rows) * this.unit_pixels, (this.cols + 2 * this.rows) * this.unit_pixels);
         this.setMinimumSize(panel_size);
         this.setPreferredSize(panel_size);
         Window.getInstance().pack();
         this.repaint();
     }
 
     /**
      * @return the elements
      */
     public List<Element> getElements() {
         return elements;
     }
 
     public void setElement(Element element) {
         this.elements.set(element.getIndex(), element);
     }
 
     public void delElement(int index) {
         this.elements.set(index, null);
     }
 
     public void setCols(int cols) {
         this.cols = cols;
     }
 
     public int getRows() {
         return this.rows;
     }
 
     public int getCols() {
         return this.cols;
     }
 
     private int getPosX(int index) {
         int i = this.cols - 1 - (index % this.cols);
         int j = (int) Math.floor(index / this.cols);
         return (3 * i + j) * this.unit_pixels;
     }
 
     private int getPosY(int index) {
         int i = this.cols - 1 - (index % this.cols);
         int j = (int) Math.floor(index / this.cols);
         return (2 * j - i + this.cols) * this.unit_pixels;
     }
 
     public boolean isSquareWalkable(int index) {
         if (index < 0 || index > this.cols * this.rows) {
             return false;
         }
         Element element = this.elements.get(index);
         if (element == null) {
             return true;
         }
         return element.isWalkable();
     }
 
     public void addFire(final List<Integer> add_fire) {
         this.fire.addAll(add_fire);
 
         new Thread(new Runnable() {
 
             public void run() {
                 try {
                     Thread.sleep(fire_duration);
                 } catch (InterruptedException e) {
                     System.out.println(e.getMessage());
                 }
                for (Integer i : add_fire) {
                    fire.remove(i);
                 }
             }
         }).start();
     }
 }
