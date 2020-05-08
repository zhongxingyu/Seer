 package hotciv.view;
 
 import hotciv.framework.*;
 
 import java.awt.*;
 import java.util.*;
 import java.awt.event.*;
 
 import minidraw.framework.*;
 import minidraw.standard.*;
 import minidraw.standard.handlers.*;
 
 public class MoveTool extends NullTool {
     private Game game;
     private DrawingEditor editor;
     private Drawing drawing;
     private Figure movingUnit;
     private int x;
     private int y;
     private int startX, startY;
 
     public MoveTool(DrawingEditor editor, Game game) {
         this.editor = editor;
         this.game = game;
         drawing = editor.drawing();
     }
 
     @Override public void mouseDown(MouseEvent e, int x, int y) {
         Position pos = coordinateToPosition(x,y);
         if (!onMap(x, y))
             return;
         if (game.getUnitAt(pos) == null)
             return;
         movingUnit = drawing.findFigure(x, y);
         this.x = startX = x;
         this.y = startY = y;
     }
 
     @Override public void mouseDrag(MouseEvent e, int x, int y) {
         if (movingUnit == null)
             return;
         movingUnit.moveBy(x - this.x, y - this.y);
         this.x = x;
         this.y = y;
     }
 
     @Override public void mouseUp(MouseEvent e, int x, int y) {
         if (movingUnit == null)
             return;
         Position startPos = coordinateToPosition(startX, startY);
         Position unitPos = coordinateToPosition(x, y);
         if (!onMap(x, y)) {
             movingUnit.moveBy(startX - x, startY - y);
             movingUnit = null;
             return;
         }
         if (!game.moveUnit(startPos, unitPos)) {
            movingUnit.moveBy(x - startX, y - startY);
         }
         movingUnit = null;
     }
 
     private boolean onMap(int x, int y) {
         Position pos = coordinateToPosition(x, y);
         if (pos.getRow() < 0 || pos.getRow() > GameConstants.WORLDSIZE - 1
             || pos.getColumn() < 0 || pos.getColumn() > GameConstants.WORLDSIZE - 1) {
             return false;
         }
         return true;
     }
 
     private Position coordinateToPosition(int x, int y) {
         return new Position((y - GfxConstants.MAP_OFFSET_Y) / GfxConstants.TILESIZE,
                             (x - GfxConstants.MAP_OFFSET_X) / GfxConstants.TILESIZE);
     }
 }
