 package maze.gui.mazeeditor;
 
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.TreeSet;
 
 import javax.swing.SwingUtilities;
 
 import maze.gui.MazeView;
 import maze.model.CellSizeModel;
 import maze.model.Direction;
 import maze.model.MazeCell;
 import maze.model.MazeCellPeg;
 import maze.model.MazeModel.MazeWall;
 
 /**
  * This class provides a MazeView on which maze templates can be applied to the
  * underlying model. This view is responsible for drawing the current template
  * that will be applied.
  * @author Johnathan Smith
  */
 public final class EditableMazeView extends MazeView
 {
    private static final int WALL_SIZE_DIVIDER = 3;
    private MazeCell activeCell;
    private Direction activeWallDir;
    private MazeTemplate mCurrentTemplate = null;
 
    private final transient MouseAdapter mouseAdapter = new MouseAdapter()
    {
       @Override
       public void mouseDragged(MouseEvent e)
       {
          if (model != null)
          {
             setActiveWall(null, null);
             final MazeCell cell = getHostMazeCell(e.getPoint());
             if (cell != null)
             {
                final MazeWall wall = getWall(e.getPoint());
                if (wall != null)
                {
                   if (SwingUtilities.isLeftMouseButton(e))
                   {
                      if (wall.isSet() == false)
                      {
                         wall.set(true);
                         invalidateCell(cell);
                      }
                   }
                   else if (SwingUtilities.isRightMouseButton(e))
                   {
                      if (wall.isSet())
                      {
                         wall.set(false);
                         invalidateCell(cell);
                      }
                   }
                }
             }
          }
       }
 
       @Override
       public void mouseMoved(MouseEvent e)
       {
          if (model != null)
          {
             MazeCell newActive = getHostMazeCell(e.getPoint());
             if (newActive != null)
             {
                if (getWallArea(newActive, Direction.East).contains(e.getPoint()) &&
                    newActive.getX() < model.getSize().width)
                {
                   setActiveWall(newActive, Direction.East);
                }
                else if (getWallArea(newActive, Direction.South).contains(e.getPoint()) &&
                         newActive.getY() < model.getSize().height)
                {
                   setActiveWall(newActive, Direction.South);
                }
                else
                {
                   setActiveWall(null, null);
                }
             }
             else
             {
                setActiveWall(null, null);
             }
          }
       }
 
       @Override
       public void mousePressed(MouseEvent e)
       {
          if (model != null)
          {
             final MazeWall wall = getWall(e.getPoint());
             if (wall != null)
             {
                //Flip the status of the wall.
                wall.set(!wall.isSet());
             }
          }
       }
    };
 
    /**
     * Constructor.
     */
    public EditableMazeView()
    {
       super.wallSizeDivider = WALL_SIZE_DIVIDER;
       this.setEditable(true);
    }
 
    /**
     * Recursively fetches all walls from the model that will be affected by the
     * template application. Each peg is only visited once so that this method
     * will not recurse infinitely.
     * @param peg the peg from which to start.
     * @param visited a set of all pegs that have already be visited
     * @param walls a Vector holding all walls that have already been found to be
     *           affected by the template. New walls will be added when found.
     * @param coords the maze coordinates for peg which may be outside of the
     *           actual maze.
     */
    private void applyPeg(TemplatePeg peg, TreeSet<TemplatePeg> visited, List<MazeWall> walls,
          int[] coords)
    {
       if (visited.contains(peg))
          return;
 
       visited.add(peg);
 
       if (peg.top != null)
       {
          if (coords[0] >= 0 &&
              coords[0] < model.getSize().width &&
              coords[1] >= 0 &&
              coords[1] < model.getSize().height)
             walls.add(model.getWall(MazeCell.valueOf(coords[0] + 1, coords[1] + 1), Direction.East));
 
          int[] newCoords =
          {
             coords[0], coords[1] - 1
          };
          applyPeg(peg.top.mRightTop, visited, walls, newCoords);
       }
       if (peg.left != null)
       {
 
          if (coords[0] >= 0 &&
              coords[0] < model.getSize().width &&
              coords[1] >= 0 &&
              coords[1] < model.getSize().height)
             walls.add(model.getWall(MazeCell.valueOf(coords[0] + 1, coords[1] + 1), Direction.South));
 
          int[] newCoords =
          {
             coords[0] - 1, coords[1]
          };
          applyPeg(peg.left.mLeftBottom, visited, walls, newCoords);
       }
       if (peg.right != null)
       {
          int[] newCoords =
          {
             coords[0] + 1, coords[1]
          };
          applyPeg(peg.right.mRightTop, visited, walls, newCoords);
       }
       if (peg.bottom != null)
       {
          int[] newCoords =
          {
             coords[0], coords[1] + 1
          };
          applyPeg(peg.bottom.mLeftBottom, visited, walls, newCoords);
       }
    }
 
    /**
     * Applied the currently selected template either setting walls or clearing
     * walls based on the value of setWall. If setWall is true, the walls under
     * the template will be set, otherwise they will be cleared.
     * @param setWall true if the walls under the template should be set, false
     *           if the walls under the template should be cleared
     */
    public void applyTemplate(boolean setWall)
    {
       if (model == null || mCurrentTemplate == null)
          return;
 
       final TemplatePeg[] centerPegs = mCurrentTemplate.getCenterPegs();
       final Point[] centerPoints = mCurrentTemplate.getCenterPoints(this.getCellSizeModel());
       final List<MazeWall> walls = new ArrayList<MazeWall>();
       final TreeSet<TemplatePeg> appliedPegs = new TreeSet<TemplatePeg>();
 
      for (int i = 0; i < Math.min(centerPegs.length, centerPoints.length) &&
                      appliedPegs.contains(centerPegs[i]) == false; i++)
       {
          // Use the top left corner of the peg next to the center point.
          final Point pegCorner = new Point(centerPoints[i].x - csm.getWallWidth(),
                                            centerPoints[i].y - csm.getWallHeightHalf());
          final MazeCell hostCell = this.getHostMazeCell(pegCorner);
          if (hostCell != null)
          {
             // The goal here is to check if the peg under the cursor is hovering over a peg on the maze.
             final Rectangle cellAreaInner = super.getCellAreaInner(hostCell);
             // The bottom right peg, take the top left corner, and move one wall height-width to the top-left direction.
             final Point cellLimitPoint = new Point(cellAreaInner.x +
                                                    cellAreaInner.width -
                                                    csm.getWallWidth(), cellAreaInner.y +
                                                                        cellAreaInner.height -
                                                                        csm.getWallHeight());
             // If in range of the peg then apply the template to the maze.
             if (pegCorner.x > cellLimitPoint.x && pegCorner.y > cellLimitPoint.y)
             {
                final TreeSet<TemplatePeg> visited = new TreeSet<TemplatePeg>();
                int[] coords =
                {
                   hostCell.getXZeroBased(), hostCell.getYZeroBased()
                };
                applyPeg(centerPegs[i], visited, walls, coords);
                appliedPegs.addAll(visited);
             }
          }
       }
 
       // Set or clear the walls we determined to be under the template.
       for (MazeWall ms : walls)
       {
          ms.set(setWall);
       }
    }
 
    /**
     * Get the cell size model.
     * @return A copy of the model instance.
     */
    public CellSizeModel getCellSizeModel()
    {
       return this.csm.clone();
    }
 
    /**
     * Get the cell that the given point is located in. This gives us a means to
     * translate between coordinate positions and cells.
     * @param pointerLocation A coordinate point in the maze area.
     * @return The cell or null if the point is not inside a valid cell.
     */
    protected MazeCell getHostMazeCell(Point pointerLocation)
    {
       try
       {
          // If the pointer is on the North or West border wall.
          if (pointerLocation.x < csm.getWallWidth() || pointerLocation.y < csm.getWallHeight())
             return null;
          MazeCell cell = MazeCell.valueOf( ( (pointerLocation.x - csm.getWallWidth()) / csm.getCellWidth()) + 1,
                                           ( (pointerLocation.y - csm.getWallHeight()) / csm.getCellHeight()) + 1);
          if (cell.isInRange(this.model.getSize()))
             return cell;
       }
       catch (IllegalArgumentException e)
       {}
       return null;
    }
 
    /**
     * Converts a mouse pointer location into an actual maze wall object.
     * @param pointerLocation Cursor location or other point.
     * @return The maze wall that can be checked or changed, or null if the
     *         pointer is not over a wall.
     */
    protected MazeWall getWall(Point pointerLocation)
    {
       final MazeCell cell = this.getHostMazeCell(pointerLocation);
       if (cell != null)
       {
          if (super.getWallArea(cell, Direction.East).contains(pointerLocation))
             return super.model.getWall(cell, Direction.East);
          if (super.getWallArea(cell, Direction.South).contains(pointerLocation))
             return super.model.getWall(cell, Direction.South);
       }
       return null;
    }
 
    /**
     * Paints the currently selected maze template if one is selected. Also
     * paints valid and invalid pegs.
     * @param arg the Graphics object that will be used for drawing operations.
     */
    @Override
    protected void paintComponent(Graphics arg)
    {
       super.paintComponent(arg);
       final Graphics2D g2 = (Graphics2D) arg;
       if (model != null)
       {
          //TODO: Pegs can be drawn in the base maze view class with better performance.
          for (MazeCellPeg p : model.illegalPegs())
          {
             super.painter.drawPegInvalid(g2, super.getPegArea(p.getMazeCell()));
          }
          if (model.isCenterLegal())
             super.painter.drawPegValid(g2, super.getPegArea(model.getWinningCells()[0]));
 
          if (mCurrentTemplate != null)
             mCurrentTemplate.draw(g2, this.getCellSizeModel(), this.painter);
       }
       if (this.activeCell != null && this.activeWallDir != null)
       {
          super.painter.drawCellHover(g2, super.getWallArea(this.activeCell, this.activeWallDir));
       }
    }
 
    /**
     * The wall that the mouse is hovering over should be set active so it can be
     * highlighted.
     * @param newCell Cell containing the active wall.
     * @param newDir Which wall is active.
     */
    private void setActiveWall(MazeCell newCell, Direction newDir)
    {
       boolean repaint = false;
       if ( (newCell == null && activeCell != null) ||
           (newCell != null && newCell.equals(activeCell) == false))
       {
          activeCell = newCell;
          repaint = true;
       }
       if (this.activeWallDir != newDir)
       {
          this.activeWallDir = newDir;
          repaint = true;
       }
       if (repaint)
          this.repaint();
    }
 
    /**
     * Sets whether this MazeView allows mouse interactions to modify its
     * underlying MazeModel.
     * @param editable true to allow editing.
     */
    public void setEditable(boolean editable)
    {
       if (editable)
       {
          this.addMouseListener(this.mouseAdapter);
          this.addMouseMotionListener(this.mouseAdapter);
       }
       else
       {
          this.removeMouseListener(this.mouseAdapter);
          this.removeMouseMotionListener(this.mouseAdapter);
          this.activeCell = null;
       }
    }
 
    /**
     * Sets the currently selected template to mt
     * @param mt the new current template that will be used. May be null.
     */
    public void setTemplate(MazeTemplate mt)
    {
       mCurrentTemplate = mt;
    }
 
 }
