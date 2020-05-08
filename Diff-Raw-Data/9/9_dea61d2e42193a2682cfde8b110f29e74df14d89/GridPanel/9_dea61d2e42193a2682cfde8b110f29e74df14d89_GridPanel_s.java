 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package student.gui;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Polygon;
 import java.awt.Rectangle;
 import java.awt.image.FilteredImageSource;
 import java.awt.image.ImageFilter;
 import java.util.Arrays;
 import java.util.HashMap;
 import javax.swing.*;
 import student.grid.HexGrid.Reference;
 import student.grid.Tile;
 import student.gui.render.PNGImagePack;
 import student.gui.render.TintFilter;
 import student.world.World;
 
 public class GridPanel extends JPanel implements Scrollable{
     private static final PNGImagePack defaultImgs;
     private static final String[] imgnames = new String[]{"tile", "rock", "plnt", "food", "nn", "ne", "nw", "se", "sw", "ss"};
     //private static final Image TILE, ROCK, PLNT, FOOD, CNN, CNE, CNW, CSE, CSW, CSS;
     
     static {
         //try {
         
         defaultImgs = new PNGImagePack("data.zip", imgnames, null);
         /*TILE = defaultImgs.get(imgnames[0]);
         ROCK = defaultImgs.get(imgnames[1]);
         PLNT = defaultImgs.get(imgnames[2]);
         FOOD = defaultImgs.get(imgnames[3]);
         CNN = defaultImgs.get(imgnames[4]);
         CNE = defaultImgs.get(imgnames[5]);
         CNW = defaultImgs.get(imgnames[6]);
         CSE = defaultImgs.get(imgnames[7]);
         CSW = defaultImgs.get(imgnames[8]);
         CSS = defaultImgs.get(imgnames[9]);*/
         if (!defaultImgs.isValid()) {
             System.err.println("Could not load image data!");
             System.exit(254);
         }
 
         /*ZipFile zf = new ZipFile("data.zip");
          ZipEntry eti = zf.getEntry("tile.png"),
          erk = zf.getEntry("rock.png"),
          epl = zf.getEntry("plnt.png"),
          efd = zf.getEntry("food.png"),
          enn = zf.getEntry("nn.png"),
          ene = zf.getEntry("ne.png"),
          enw = zf.getEntry("nw.png"),
          ese = zf.getEntry("se.png"),
          esw = zf.getEntry("sw.png"),
          ess = zf.getEntry("ss.png");
          InputStream iti = zf.getInputStream(eti),
          irk = zf.getInputStream(erk),
          ipl = zf.getInputStream(epl),
          ifd = zf.getInputStream(efd),
          inn = zf.getInputStream(enn),
          ine = zf.getInputStream(ene),
          inw = zf.getInputStream(enw),
          ise = zf.getInputStream(ese),
          isw = zf.getInputStream(esw),
          iss = zf.getInputStream(ess);
          TILE = ImageIO.read(iti);
          ROCK = ImageIO.read(irk);
          PLNT = ImageIO.read(ipl);
          FOOD = ImageIO.read(ifd);
          CNN  = ImageIO.read(inn);
          CNE  = ImageIO.read(ine);
          CNW  = ImageIO.read(inw);
          CSE  = ImageIO.read(ise);
          CSW  = ImageIO.read(isw);
          CSS  = ImageIO.read(iss);
          } catch (IOException ex) {
          System.err.println("Could not load image data!");
          System.exit(254);
          } catch (RuntimeException ex) {
          System.err.println("Could not load image data!");
          System.exit(253);
          }*/
     }
     private HashMap<String, PNGImagePack> imagepackages;
     private Polygon hexen[][];
     //A MULTIPLE OF FOUR
     private int HEXSIZE = 140;
     public World world; 
     public GridPanel(World world) {
         this.world = world;
         //this.setBorder(new LineBorder(Color.MAGENTA, 3));
         hexen = new Polygon[world.height()][world.width()];
         for (int c = 0; c < this.world.width(); c++) {
             int x = pnX(0, c);
             for (int r = 0; r < this.world.height(); r++) {
                 int y = pnY(r, c);
                 //System.out.println(""+x+","+y);
                 //gp.fillOval(x, y, 10, 10);
                 hexen[r][c] = makePoly(x,y,HEXSIZE);
                 Reference<Tile> rt = world.at(r, c);
                 if(rt.contents() == null)
                     rt.setContents(new Tile(false, 0));
             }
         }
         this.setFocusable(true);
         this.requestFocusInWindow();
     }
     public int []hexAt(int x, int y) {
         x -= this.getX();
         y -= this.getY();
         for(int r = 0; r < world.height(); r++)
             for(int c = 0; c < world.width(); c++)
                 if(hexen[r][c].contains(x, y))
                     return new int[]{r,c};
         return null;
     }
     
     /**
      * Calculates the dimensions and coordinates of the grid based off of
      * the panel size then paints the display.
      *
      * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
      */
     public void paintComponent(Graphics g) {
      update(g);
     }
 
     /**
      *
      * @see javax.swing.JComponent#update(java.awt.Graphics)
      */
     @Override
     public void update(Graphics g)//overrides update method to prevent continuous uneccessary repainting
     {
         g.setColor(Color.BLACK);
         g.fillRect(0, 0, this.WIDTH, this.HEIGHT);
         drawGrid(HEXSIZE, g);
     }
 
     /**
      * Draws the entire grid of hexagons
      *
      * @param length the number of hexagons that make of a side length of the
      * hexagon grid
      * @param hexsize the size of each hexagon
      */
 
     /**
      * Draws a hexagon of the given coordinates
      *
      * @param hexCoordinates the given coordinates
      * @param g graphics
      */
     public void drawHexagon(int x, int y, int r, int c, int hexsize, Graphics g, Image i) {       
         g.drawImage(i, x, y, hexsize, hexsize, this);
     }
 
     public int pnX(int row, int col) {
         return col * HEXSIZE / 4 * 3;
     }
     
     public int pnY(int row, int col) {
         return row * HEXSIZE
              + (col%2==0
                  ? HEXSIZE/2 
                  : 0);
     }
 
     /**
      * Returns the coordinates (in the form [x, y]) of a single hexagon drawn
      * starting from the given coordinates Precondition: length is divisible by
      * 4
      *
      * @param startX the given start x coordinate
      * @param startY the given start y coordinate
      * @param size the side length of square the hexagon will be contained in
      * @return
      */
     public static Polygon makePoly(int startX, int startY, int size) {
         int xs[] = new int[6], ys[] = new int[6]; //[Xj, Yj] for j= 1,2,3,4,5,6
         xs[0] = size / 4 + startX;
             ys[0] = startY;
         xs[1] = size * 3 / 4 + startX;
             ys[1] = startY;
         xs[2] = size + startX;
             ys[2] = size / 2+ startY;
         xs[3] = size * 3 / 4 + startX;
             ys[3] = size+ startY;
         xs[4] = size / 4 + startX;
             ys[4] = size+ startY;
         xs[5] = startX;
             ys[5] = size / 2+ startY;
         return new Polygon(xs, ys, 6);
     }
    /**
      * Draws the entire grid
      */
     public void drawGrid(int hexsize, Graphics gp) {//HWHdrawGrid(int hxsz, Graphics gp) {
         for (int c = 0; c < world.width(); c++) {
             for (int r = 0; r < world.height(); r++) {
                 Polygon loc = hexen[r][c];
                 Rectangle bbx = loc.getBounds();
                 Tile t = world.at(r, c).contents();
                 PNGImagePack imagepack = defaultImgs;
                 if(t != null && t.rock()) 
                     drawHexagon(bbx.x, bbx.y, r, c, hexsize, gp, imagepack.get(imgnames[1]));//ROCK);
                 else {
                     drawHexagon(bbx.x, bbx.y, r, c, hexsize, gp, imagepack.get(imgnames[0]));//TILE); 
                     if(t.food())
                         drawHexagon(bbx.x, bbx.y, r, c, hexsize, gp, imagepack.get(imgnames[3])); 
                     if(t.plant())
                         drawHexagon(bbx.x, bbx.y, r, c, hexsize, gp, imagepack.get(imgnames[2])); 
                     if(t.critter()) {
                         Image i = null;
                         String s = t.getCritter().getAppearance();
                         Color color = t.getCritter().getColor();
                        System.out.println(s + color);
                         if (s == null) {
                             s = "data.zip";
                         }
                         if (imagepackages == null) {
                             imagepackages = new HashMap<String, PNGImagePack>();
                         }
                         if (imagepackages.containsKey(s + color)) {
                             imagepack = imagepackages.get(s + color);
                         } else {
                             imagepack = new PNGImagePack(s, Arrays.copyOfRange(imgnames, 4, imgnames.length), color);
                             if (imagepack.isValid()) {
                                 imagepackages.put(s + color, imagepack);
                             } else {
                                 imagepack = defaultImgs;
                             }
                         }
                         switch (t.getCritter().direction()) {
                              case N:  i = imagepack.get(imgnames[4]); break;//CNN; break;
                              case NE: i = imagepack.get(imgnames[5]); break;//CNE; break;
                              case NW: i = imagepack.get(imgnames[6]); break;//CNW; break;
                              case S:  i = imagepack.get(imgnames[9]); break;//CSS; break;
                              case SW: i = imagepack.get(imgnames[8]); break;//CSW; break;
                              case SE: i = imagepack.get(imgnames[7]); break;//CSE; break;
                             } 
                         
                         drawHexagon(bbx.x, bbx.y, r, c, hexsize, gp, i);
                     }
                 }
                 /*/
                 gp.setColor(Color.RED);
                 String s = "("+r+","+c+")";
                 gp.drawChars(s.toCharArray(), 0, s.length(), bbx.x+20, bbx.y+20);
                 /*/
             }
         }
     }
 
     @Override
     public Dimension getPreferredSize()
     {
         return new Dimension(world.width()*HEXSIZE - (world.width() - 1)*HEXSIZE/4, (world.height()+1)*HEXSIZE);
     }
    @Override
     public Dimension getPreferredScrollableViewportSize() {
         return getPreferredSize();
     }
 
     @Override
     public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
         return 0;
     }
 
     @Override
     public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
         return 0;
     }
 
     @Override
     public boolean getScrollableTracksViewportWidth() {
         return false;
     }
 
     @Override
     public boolean getScrollableTracksViewportHeight() {
         return false;
     }
 }
