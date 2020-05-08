 /*******************************************************************************
  * Copyright (c) 2013 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.rc.javafx.util;
 
 import javafx.geometry.BoundingBox;
 import javafx.geometry.Point2D;
 import javafx.scene.Node;
 
 /**
  * Helperclass for checking node bounds
  */
 public class NodeBounds {
 
     /**
      * Private Constructor
      */
     private NodeBounds() {
         // Empty
     }
 
     /**
      * Checks if the given point with coordinates relative to the scene is in
      * the given Node.
      *
      * @param point
      *            the Point
      * @param n
      *            the Node
      * @return true if the Point is in the Node, false if not.
      */
     public static boolean checkIfContains(Point2D point, Node n) {
         Point2D nodePos = n.localToScreen(0, 0);
 
        // A null value here means that the Node is not in a Window, so its 
        // (non-existent) bounds cannot contain the given point.
        if (nodePos == null) {
            return false;
        }
        
         BoundingBox box = new BoundingBox(nodePos.getX(),
                                           nodePos.getY(),
                                           n.getBoundsInParent().getWidth(),
                                           n.getBoundsInParent().getHeight());
         return box.contains(point);
     }
 
 }
