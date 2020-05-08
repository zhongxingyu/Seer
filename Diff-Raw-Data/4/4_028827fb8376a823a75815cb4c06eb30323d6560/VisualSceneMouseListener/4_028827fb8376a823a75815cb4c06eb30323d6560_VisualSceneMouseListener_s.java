 /*
     This file is part of The Simplicity Engine.
 
     The Simplicity Engine is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published
     by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 
     The Simplicity Engine is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License along with The Simplicity Engine. If not, see <http://www.gnu.org/licenses/>.
  */
 package com.se.simplicity.editor.ui.editors;
 
 import java.awt.Dimension;
 
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.widgets.Control;
 
 import com.se.simplicity.picking.engine.PickingEngine;
 
 /**
  * <p>
  * Listens for mouse events on a 3D canvas and picks the <code>SceneGraph</code> displayed on it.
  * </p>
  * 
  * @author Gary Buyn
  */
 public class VisualSceneMouseListener extends MouseAdapter
 {
     /**
      * <p>
      * The <code>PickingEngine</code> to register picks with.
      * </p>
      */
     private PickingEngine fPickingEngine;
 
     /**
      * <p>
      * Creates an instance of <code>VisualSceneMouseListener</code>.
      * </p>
      * 
      * @param newPickingEngine The <code>PickingEngine</code> to register picks with.
      */
     public VisualSceneMouseListener(final PickingEngine newPickingEngine)
     {
         fPickingEngine = newPickingEngine;
     }
 
     @Override
     public void mouseUp(final MouseEvent event)
     {
         if (event.button == 1)
         {
             Dimension viewportSize = new Dimension();
            viewportSize.width = ((Control) event.widget).getBounds().x;
            viewportSize.height = ((Control) event.widget).getBounds().y;
 
             fPickingEngine.pickViewport(viewportSize, event.x, event.y, 5, 5);
         }
     }
 }
