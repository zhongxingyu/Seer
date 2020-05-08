 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cz.mgn.collabdesktop.gui.desk.paintengine;
 
 import cz.mgn.collabcanvas.canvas.CollabCanvas;
 import cz.mgn.collabcanvas.interfaces.listenable.CollabPanelKeyEvent;
 import cz.mgn.collabcanvas.interfaces.listenable.CollabPanelListener;
 import cz.mgn.collabcanvas.interfaces.listenable.CollabPanelMouseEvent;
 import cz.mgn.collabdesktop.gui.desk.paintengine.tool.CanvasInterface;
 import cz.mgn.collabdesktop.gui.desk.paintengine.tool.Tool;
 import java.awt.Color;
 import java.util.ArrayList;
 
 /**
  *
  * @author indy
  */
 public class PaintEngine implements PaintEngineInterface, CollabPanelListener {
 
     protected ArrayList<PaintEngineListener> paintEngineListeners = new ArrayList<PaintEngineListener>();
     protected Color color = Color.BLACK;
     protected Tool tool;
     protected CollabCanvas canvas;
 
     public PaintEngine(CollabCanvas canvas) {
         this.canvas = canvas;
         canvas.getListenable().addListener(this);
     }
 
     public void addPaintEngineListener(PaintEngineListener paintEngineListener) {
         paintEngineListeners.add(paintEngineListener);
     }
 
     public void removePaintEngineListener(PaintEngineListener paintEngineListener) {
         paintEngineListeners.remove(paintEngineListener);
     }
 
     @Override
     public void setColor(int color) {
         this.color = new Color(color);
        if (tool != null) {
            tool.setColor(color);
        }
     }
 
     public void setTool(Tool tool) {
         if (this.tool != null) {
             this.tool.setCanvsaInterface(null);
             this.tool.setPaintEngineInterface(null);
         }
         this.tool = tool;
         if (tool != null) {
             tool.setCanvsaInterface(new CanvasInterface(canvas.getVisible(), canvas.getPaintable(), canvas.getSelectionable()));
             tool.setColor(color.getRGB());
             tool.setPaintEngineInterface(this);
         }
     }
 
     public void setCanvas(CollabCanvas canvas) {
         this.canvas = canvas;
         if (tool != null) {
             tool.setCanvsaInterface(new CanvasInterface(canvas.getVisible(), canvas.getPaintable(), canvas.getSelectionable()));
         }
     }
 
     public CollabCanvas getCanvas() {
         return canvas;
     }
 
     @Override
     public void keyEvent(CollabPanelKeyEvent cpke) {
        if(tool != null) {
            tool.keyEvent(cpke);
        }
     }
 
     @Override
     public void mouseEvent(CollabPanelMouseEvent cpme) {
         if(tool != null) {
             tool.mouseEvent(cpme);
         }
     }
 }
