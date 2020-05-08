 package maze.model;
 
 import java.util.Observable;
 
 /**
  * This model stores the sizes of the cells and wall segments that are drawn to
  * the screen.
  */
 public class CellSizeModel extends Observable implements CellSizeModelInterface
 {
 
    private int cellWidth = 40;
    private int cellHeight = 40;
    private int wallWidth = 10;
    private int wallHeight = 10;
 
    @Override
    public String toString()
    {
       return "Cell[w=" +
              this.cellWidth +
              ",h=" +
              this.cellHeight +
              "] Wall[w=" +
              this.wallWidth +
              ",h=" +
              this.wallHeight +
              "]";
    }
 
    public void setCellWidth(int cellWidth)
    {
       if ( (cellWidth & 1) == 1)
       {
          cellWidth--;
       }
       if (this.cellWidth != cellWidth)
       {
          this.cellWidth = cellWidth;
          super.setChanged();
       }
    }
 
    public void setCellHeight(int cellHeight)
    {
       if ( (cellHeight & 1) == 1)
       {
          cellHeight--;
       }
       if (this.cellHeight != cellHeight)
       {
          this.cellHeight = cellHeight;
         System.out.println("Cell Height Changed: " + this.cellHeight);
          super.setChanged();
       }
    }
 
    public void setWallWidth(int wallWidth)
    {
       if ( (wallWidth & 1) == 1)
       {
          wallWidth++;
       }
       if (this.wallWidth != wallWidth)
       {
          this.wallWidth = wallWidth;
          super.setChanged();
       }
    }
 
    public void setWallHeight(int wallHeight)
    {
       if ( (wallHeight & 1) == 1)
       {
          wallHeight++;
       }
       if (this.wallHeight != wallHeight)
       {
          this.wallHeight = wallHeight;
          super.setChanged();
       }
    }
 
    public int getCellWidth()
    {
       return cellWidth;
    }
 
    public int getCellHeight()
    {
       return cellHeight;
    }
 
    public int getWallWidth()
    {
       return wallWidth;
    }
 
    public int getWallHeight()
    {
       return wallHeight;
    }
 
    public int getCellWidthHalf()
    {
       return cellWidth / 2;
    }
 
    public int getCellHeightHalf()
    {
       return cellHeight / 2;
    }
 
    public int getWallWidthHalf()
    {
       return wallWidth / 2;
    }
 
    public int getWallHeightHalf()
    {
       return wallHeight / 2;
    }
 
    public int getCellWidthInner()
    {
       return this.cellWidth - this.wallWidth;
    }
 
    public int getCellHeightInner()
    {
       return this.cellHeight = this.wallHeight;
    }
 }
