 package com.cs408.supersweeper;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.image.BufferedImage;
 import java.io.FileInputStream;
 import java.util.Properties;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 
 public class LevelSelectPanel  extends JPanel implements MouseListener, MouseMotionListener {
     
     private static final long serialVersionUID = -8348029708739390765L;
     private GameFrame _gf;
     private GameState _gs;
     private GridUnit _previouslyPressedGridUnit = null;
     private BufferedImage _previousImage;
     private int _level;
    
     public LevelSelectPanel (GameFrame gf) {
         this._gf = gf;
         //TODO: make it only show the levels that are unlocked
         // Make a new 3x3 GameState for level selection and populate it with appropriate images
         _gs = new GameState(0, 0, 1, 3, 3, 0, null);
         GridUnit[][] grid = _gs.getGrid();
         for(int x = 0; x < 3; x++) {
             for(int y = 0; y < 3; y++){
                 grid[x][y].setImage(GridUnit.images.get(String.valueOf(x + y*3 + 1)));
             }
         }
         grid[2][2].setImage(GridUnit.images.get("mine"));
 
         //Set Min Size for this container panel
         int w = GridUnit.sample.getWidth();
         int h = GridUnit.sample.getHeight();
         setPreferredSize(new Dimension(_gs.getGridWidth() * w, _gs.getGridHeight() * h ));
 
         // Get the user's current level
         try {
             Properties props = new Properties();
             FileInputStream in = new FileInputStream(this.getClass().getResource("/userProgress.properties").getPath());
             props.load(in);
             _level = Integer.parseInt(props.getProperty("level"));
             in.close();
         }
         catch(Exception e)
         {
             e.printStackTrace();
         }
         
         //Add mouse listeners
         addMouseListener(this);
         addMouseMotionListener(this);
     }
 
     /** Internal Methods */
     private GridUnit getGridUnit(MouseEvent e) {
         int x = e.getX() / GridUnit.sample.getWidth();
         int y = e.getY() / GridUnit.sample.getHeight();
 
         if (x >= _gs.getGridWidth() || y >= _gs.getGridWidth() || x < 0 || y < 0) {
             return null;
         }
 
         return _gs.getGridUnit(x, y);
     }
     
     @Override
     public void paint(Graphics g) {
         super.paint(g);
         GridUnit[][] grid = _gs.getGrid();
         BufferedImage unit = new BufferedImage(GridUnit.sample.getWidth(), GridUnit.sample.getHeight(), BufferedImage.TYPE_INT_ARGB);
         Graphics unit_graphics = unit.getGraphics();
         for (int x = 0; x < grid.length; x++) {
             for (int y = 0; y < grid[0].length; y++) {
                 unit_graphics.setColor(Color.WHITE);
                 unit_graphics.fillRect(0, 0, unit.getWidth(), unit.getHeight());
                 unit_graphics.drawImage(grid[x][y].getImage(), 0, 0, null);
                 g.drawImage(unit, x * unit.getWidth(), y * unit.getHeight(), null);
             }
         }
     }
     
     /** Listeners */
     @Override
     public void mousePressed(MouseEvent e) {
         GridUnit gridUnit = getGridUnit(e);
         if (gridUnit == null) {
             return;
         }
 
         if (SwingUtilities.isLeftMouseButton(e)) {
             _previouslyPressedGridUnit = gridUnit;
             _previousImage = gridUnit.getImage();
             gridUnit.setImage(GridUnit.images.get("normal"));
         }
 
         repaint();
     }
 
     @Override
     public void mouseReleased(MouseEvent e) {
         GridUnit gridUnit = getGridUnit(e);
         if (gridUnit == null) {
             if (_previouslyPressedGridUnit != null) {
                 _previouslyPressedGridUnit.setImage(_previousImage);
             }
             repaint();
             return;
         }
 
         if (SwingUtilities.isLeftMouseButton(e)) {
             //Start appropriate game level here
             int x = e.getX() / GridUnit.sample.getWidth();
             int y = e.getY() / GridUnit.sample.getHeight();
             int level = (x + y*3 + 1);
            if(_level+1 < level)
             {
                Utility.infoBox("You have not completed level " + (_level+1) + " yet!", "");
             }
             else
             {
                 if(x == 2  && y == 2) {
                     //Bonus Level!!
                     _gf.startLevel("bonus.properties");
                 }
                 else {
                     //System.out.println(_level + "   " + level);
                     _gf.startLevel("00" + level +".properties");
                 }
             }
             
             //Unreachable Code
             gridUnit.setImage(_previousImage);
             _previouslyPressedGridUnit = null;
         }
 
         repaint();
     }
 
     @Override
     public void mouseDragged(MouseEvent e) {
         if (_previouslyPressedGridUnit == null) {
             return;
         }
 
         GridUnit gridUnit = getGridUnit(e);
         if (gridUnit == null) {
             return;
         }
 
         if (SwingUtilities.isLeftMouseButton(e)) {
             _previouslyPressedGridUnit.setImage(_previousImage);
             _previouslyPressedGridUnit = gridUnit;
             _previousImage = gridUnit.getImage();
             gridUnit.setImage(GridUnit.images.get("normal"));
         }
 
         repaint();
     }
 
     @Override
     public void mouseClicked(MouseEvent e) {
     }
 
     @Override
     public void mouseEntered(MouseEvent e) {
     }
 
     @Override
     public void mouseExited(MouseEvent e) {
     }
 
     @Override
     public void mouseMoved(MouseEvent arg0) {
     }
 }
