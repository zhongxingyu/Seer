 /*
  * Nebula2D is a cross-platform, 2D game engine for PC, Mac, & Linux
  * Copyright (c) 2014 Jon Bonazza
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.nebula2d.editor.ui;
 
 import com.badlogic.gdx.backends.lwjgl.LwjglAWTCanvas;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.math.Circle;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.nebula2d.editor.framework.SceneNode;
 import com.nebula2d.editor.framework.Selection;
 import com.nebula2d.scene.GameObject;
 import com.nebula2d.scene.Layer;
 import com.nebula2d.scene.Scene;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.util.ArrayList;
 import java.util.List;
 
 public class RenderCanvas extends LwjglAWTCanvas implements MouseListener, MouseMotionListener, MouseWheelListener {
 
     protected RenderAdapter adapter;
 
     protected Point lastPoint;
 
     protected boolean isMouseDown;
 
     public RenderCanvas(RenderAdapter adapter) {
         super(adapter);
         this.adapter = adapter;
         this.getCanvas().addMouseListener(this);
         this.getCanvas().addMouseMotionListener(this);
         this.getCanvas().addMouseWheelListener(this);
         this.isMouseDown = false;
     }
 
     public OrthographicCamera getCamera() {
         return adapter.getCamera();
     }
 
     protected List<Selection> getSelectedGameObjects(int x, int y) {
 
         List<Selection> res = new ArrayList<>();
 
         Scene scene = MainFrame.getProject().getCurrentScene();
        for (Layer layer : scene.getLayers()) {
            for (GameObject gameObject : layer.getGameObjects())
                searchGameObjectChildrenForSelection(gameObject, res, x, y);
         }
 
         return res;
     }
 
     private void searchGameObjectChildrenForSelection(GameObject gameObject, List<Selection> selections, float x, float y) {
         for (Actor child : gameObject.getChildren()) {
             GameObject g = (GameObject) child;
             if (isGameObjectSelected(g, getCamera(), x, getCamera().viewportHeight-y)) {
                 selections.add(new Selection(g));
             } else {
                 searchGameObjectChildrenForSelection(g, selections, x, y);
             }
         }
     }
 
     private boolean isGameObjectSelected(GameObject gameObject, OrthographicCamera cam, float x, float y) {
         Selection maybeSelection = new Selection(gameObject);
         if (gameObject.getRenderer() != null && maybeSelection.getBoundingBox(cam).contains(x, y))
             return true;
 
         Vector3 proj = cam.project(new Vector3(gameObject.getX(), gameObject.getY(), 0));
         Circle point = new Circle(proj.x, proj.y, 4);
         return point.contains(x, y);
     }
 
     protected void translateObject(Point mousePos) {
         int dx = mousePos.x - lastPoint.x;
         int dy = mousePos.y - lastPoint.y;
         adapter.getSelectedObject().getGameObject().moveBy(dx, dy);
     }
 
     protected void scaleObject(Point mousePos) {
         int dx = mousePos.x - lastPoint.x;
         int dy = mousePos.y - lastPoint.y;
         adapter.getSelectedObject().getGameObject().scaleBy(dx, dy);
     }
 
     protected void rotateObject(Point mousePos) {
         int dy = mousePos.y - lastPoint.y;
         adapter.getSelectedObject().getGameObject().rotateBy(dy*0.5f);
     }
 
     public void setSelectedObject(GameObject go) {
         this.adapter.setSelectedObject(new Selection(go));
     }
 
     @Override
     public void mouseClicked(MouseEvent e) {}
 
     @Override
     public void mousePressed(MouseEvent e) {
         if (e.getButton() == MouseEvent.BUTTON1) {
             lastPoint = e.getPoint();
             if (e.isControlDown()) {
                 return;
             }
 
             List<Selection> selectedObjects = getSelectedGameObjects(lastPoint.x, lastPoint.y);
             int size = selectedObjects.size();
             if (size > 0) {
                 GameObject selectedObject = selectedObjects.get(size - 1).getGameObject();
                 MainFrame.getSceneGraph().setSelectedNode(new SceneNode<>(selectedObject.getName(), selectedObject));
             } else {
                 MainFrame.getSceneGraph().setSelectionPath(null);
             }
         }
     }
 
     @Override
     public void mouseReleased(MouseEvent e) {}
 
     @Override
     public void mouseEntered(MouseEvent e) {}
 
     @Override
     public void mouseExited(MouseEvent e) {}
 
     @Override
     public void mouseDragged(MouseEvent e) {
         Point pos = e.getPoint();
         GameObject selectedObject = adapter.getSelectedObject().getGameObject();
         if (e.isControlDown()) {
             int dx = (pos.x - lastPoint.x) / 2;
             int dy = -(pos.y - lastPoint.y) / 2;
             getCamera().translate(dx, dy);
             getCamera().update();
 
 
         } else if (selectedObject != null) {
              transformObject(pos);
         }
 
         lastPoint = pos;
     }
 
     private void transformObject(Point mousPos) {
         switch (MainFrame.getToolbar().getSelectedRendererWidget()) {
             case N2DToolbar.RENDERER_WIDGET_TRANSLATE:
                 translateObject(mousPos);
                 break;
             case N2DToolbar.RENDERER_WIDGET_SCALE:
                 scaleObject(mousPos);
                 break;
             case N2DToolbar.RENDERER_WIDGET_ROTATE:
                 rotateObject(mousPos);
                 break;
         }
     }
 
     @Override
     public void mouseMoved(MouseEvent e) {}
 
     @Override
     public void mouseWheelMoved(MouseWheelEvent e) {
         getCamera().zoom += e.getWheelRotation() * 0.25f;
     }
 }
