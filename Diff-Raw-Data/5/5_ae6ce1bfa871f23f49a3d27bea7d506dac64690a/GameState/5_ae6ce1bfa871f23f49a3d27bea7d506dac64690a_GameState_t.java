 package com.cs408.supersweeper;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.image.BufferedImage;
 import java.util.Random;
 
 public class GameState
 {
 
    /** Global Variables */
    private GridUnit[][] _grid;
    private double _time;
    private int _minesFound = 0;
    private int _numMines;
    private int _numOfFlags = 0;
    private double _score = 0;
    private boolean _isTimed = true;
    public int gridWidth;
    public int gridHeight;
    private int _gridNumSides;
 
    /** Constructors */
    public GameState(double time, int numMines, int gridWidth, int gridHeight,
          int gridNumSides)
    {
       this._time = time;
       this._numMines = numMines;
       this.gridHeight = gridHeight;
       this.gridWidth = gridWidth;
       this._gridNumSides = gridNumSides;
       if (time == 0)
       {
          _isTimed = false;
       }
       this.resetGrid();
 
    }
 
    // TODO: so far only says if has mine or not. needs to return state.
    public boolean getState(int x, int y)
    {
       return _grid[x][y].hasMine();
    }
    
    public GridUnit getGridUnit(int x, int y)
    {
       return this._grid[x][y];
    }
    
    public void exposeAllMines()
    {
       for (int x = 0; x < _grid.length; x++)
       {
          for (int y = 0; y < _grid[0].length; y++)
          {
             if(_grid[x][y].hasMine())
                _grid[x][y].setMine();
          }
       }      
    }
 
    public void resetGrid()
    {
       this._grid = new GridUnit[gridWidth][gridHeight];
 
       // populate grid with blank tiles
       for (int x = 0; x < gridWidth; x++)
       {
          for (int y = 0; y < gridHeight; y++)
          {
             _grid[x][y] = new GridUnit(_gridNumSides, new Point(x, y));
          }
       }
    }
    
    public void populateMines(int x, int y)
    {
       // populate grid with bombs!
       // TODO populate bombs after user first clicks
       Random r = new Random();
       for (int i = 0; i < _numMines; i++)
       {
          int randX = r.nextInt(gridWidth);
          int randY = r.nextInt(gridHeight);
          
          if(randX == x && randY == y)
          {
             i--;
             continue;
          }
          
          GridUnit tmp = _grid[randX][randY];
          if (!tmp.hasMine())
             tmp.setHasMine(true);
          else
             i--;
       }
       
       //populate gridunit nearby mines
      for (int i = 0; i < gridWidth; i++)
       {
         for (int j = 0; j < gridHeight; j++)
          {
             _grid[i][j].setNearbyMines(this.countNumberOfMines(_grid[i][j]));
          }
       }
    }
    
    private int countNumberOfMines(GridUnit unit)
    {
       int numMines = 0;
       int x = (int) unit.getCoordinate().getX();
       int y = (int) unit.getCoordinate().getY();
       
       if(unit.hasMine())
          return -1;
       
       for (int X = -1; X < 2; X++)
       {
          for (int Y = -1; Y < 2; Y++)
          {
             if(X==0 && Y==0)
                continue;
             try
             {
                if (_grid[x + X][y + Y].hasMine())
                   numMines++;
                unit.addAdjacenctUnit(new Point(x + X, y + Y));
             }
             catch (Exception e)
             {
                // ignore dis bish
                // I am ripe for bugs
             }
          }
       }
       return numMines;
    }
    
    public void exposeNumber(GridUnit unit)
    {
       if(unit.getNearbyMineCount() > 0)
       {
          unit.setNumber(unit.getNearbyMineCount());
          return;
       }
       else
          unit.setEmpty();
       
       
       for(Point u : unit.getAdjacentUnits())
       {
          System.out.println(u + " | " + unit.getNearbyMineCount());
          int x = (int) u.getX();
          int y = (int) u.getY();
          
          if(_grid[x][y].getState() != GridUnit.State.CHECKED)
             exposeNumber(_grid[x][y]);
          
       }
    }
 
    public void drawState(Graphics g)
    {
       BufferedImage unit = new BufferedImage(
             _grid[0][0].getBitmap().getWidth(), _grid[0][0].getBitmap()
                   .getHeight(), BufferedImage.TYPE_INT_ARGB);
       Graphics unit_graphics = unit.getGraphics();
       for (int x = 0; x < _grid.length; x++)
       {
          for (int y = 0; y < _grid[0].length; y++)
          {
             unit_graphics.setColor(Color.WHITE);
             unit_graphics.fillRect(0, 0, unit.getWidth(), unit.getHeight());
             _grid[x][y].draw(unit_graphics);
             g.drawImage(unit, x * unit.getWidth(), y * unit.getHeight(), null);
          }
       }
    }
 }
