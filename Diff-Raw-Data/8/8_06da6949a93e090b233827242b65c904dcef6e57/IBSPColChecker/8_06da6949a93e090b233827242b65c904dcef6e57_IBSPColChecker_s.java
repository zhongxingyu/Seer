 /*
  This file is part of the Greenfoot program.
  Copyright (C) 2005-2009,2010  Poul Henriksen and Michael Kolling
 
  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License
  as published by the Free Software Foundation; either version 2
  of the License, or (at your option) any later version.
 
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 
  This file is subject to the Classpath exception as provided in the
  LICENSE.txt file that accompanied this code.
  */
 package sofia.graphics.collision;
 
 import sofia.graphics.Shape;
 import static sofia.graphics.ShapeAccessUtilities.*;
 import java.util.*;
 
 /**
  * A collision checker using a Binary Space Partition tree.
  *
  * <p>Each node of the tree represents a rectangular area, and potentially has
  * two non-overlapping child nodes which together cover the same area as their
  * parent.
  *
  * @author Davin McCall
  */
 public class IBSPColChecker implements CollisionChecker
 {
     public static final int X_AXIS = 0;
     public static final int Y_AXIS = 1;
 
     public static final int PARENT_LEFT = 0;
     public static final int PARENT_RIGHT = 1;
     public static final int PARENT_NONE = 3; // no particular side
 
     public static final int REBALANCE_THRESHOLD = 20;
 
     private GOCollisionQuery shapeQuery = new GOCollisionQuery();
     private NeighbourCollisionQuery neighbourQuery = new NeighbourCollisionQuery();
     private PointCollisionQuery pointQuery = new PointCollisionQuery();
     private InRangeQuery inRangeQuery = new InRangeQuery();
 
     private BSPNode bspTree;
 
 
     /*
      * @see greenfoot.collision.CollisionChecker#addObject(greenfoot.Actor)
      */
     public synchronized void addObject(Shape shape)
     {
         // checkConsistency();
         Rect bounds = getShapeBounds(shape);
        
         // FIXME Hack hack hack hack hack hack hack
         // (The while loop below does NOT like shapes with zero width or zero
         // height)
         if (bounds.getWidth() == 0)
         {
         	bounds.setWidth(0.001f);
         }
         if (bounds.getHeight() == 0)
         {
         	bounds.setHeight(0.001f);
         }
 
         if (bspTree == null) {
             // The tree is currently empty; just create a new node containing only the one actor
             int splitAxis;
             float splitPos;
             if (bounds.getWidth() > bounds.getHeight()) {
                 splitAxis = X_AXIS;
                 splitPos = bounds.getMiddleX();
             }
             else {
                 splitAxis = Y_AXIS;
                 splitPos = bounds.getMiddleY();
             }
             bspTree = BSPNodeCache.getBSPNode();
             bspTree.getArea().copyFrom(bounds);
             bspTree.setSplitAxis(splitAxis);
             bspTree.setSplitPos(splitPos);
             bspTree.addShape(shape);
         }
         else {
             Rect treeArea = bspTree.getArea();
             while (! treeArea.contains(bounds)) {
                 // We increase the tree area in up to four directions:
             	System.out.println("bounds = " + bounds + ", treeArea = " + treeArea);
                 if (bounds.getX() < treeArea.getX()) {
                     // double the width out to the left
                     float bx = treeArea.getX() - treeArea.getWidth();
                     Rect newArea = new Rect(bx, treeArea.getY(),
                             treeArea.getRight() - bx, treeArea.getHeight());
                     BSPNode newTop = BSPNodeCache.getBSPNode();
                     newTop.getArea().copyFrom(newArea);
                     newTop.setSplitAxis(X_AXIS);
                     newTop.setSplitPos(treeArea.getX());
                     newTop.setChild(PARENT_RIGHT, bspTree);
                     bspTree = newTop;
                     treeArea = newArea;
                 	System.out.println("left: newArea = " + newArea);
                 }
                 if (bounds.getRight() > treeArea.getRight()) {
                     // double the width out to the right
                     float bx = treeArea.getRight() + treeArea.getWidth();
                     Rect newArea = new Rect(treeArea.getX(), treeArea.getY(),
                             bx - treeArea.getX(), treeArea.getHeight());
                     BSPNode newTop = BSPNodeCache.getBSPNode();
                     newTop.getArea().copyFrom(newArea);
                     newTop.setSplitAxis(X_AXIS);
                     newTop.setSplitPos(treeArea.getRight());
                     newTop.setChild(PARENT_LEFT, bspTree);
                     bspTree = newTop;
                     treeArea = newArea;
                 	System.out.println("right: newArea = " + newArea);
                 }
                 if (bounds.getY() < treeArea.getY()) {
                     // double the height out the top
                     float by = treeArea.getY() - treeArea.getHeight();
                     Rect newArea = new Rect(treeArea.getX(), by,
                             treeArea.getWidth(), treeArea.getTop() - by);
                     BSPNode newTop = BSPNodeCache.getBSPNode();
                     newTop.getArea().copyFrom(newArea);
                     newTop.setSplitAxis(Y_AXIS);
                     newTop.setSplitPos(treeArea.getY());
                     newTop.setChild(PARENT_RIGHT, bspTree);
                     bspTree = newTop;
                     treeArea = newArea;
                 	System.out.println("top: newArea = " + newArea);
                 }
                 if (bounds.getTop() > treeArea.getTop()) {
                     // double the height out the bottom
                     float by = treeArea.getTop() + treeArea.getHeight();
                     Rect newArea = new Rect(treeArea.getX(), treeArea.getY(),
                             treeArea.getWidth(), by - treeArea.getY());
                     BSPNode newTop = BSPNodeCache.getBSPNode();
                     newTop.getArea().copyFrom(newArea);
                     newTop.setSplitAxis(Y_AXIS);
                     newTop.setSplitPos(treeArea.getTop());
                     newTop.setChild(PARENT_LEFT, bspTree);
                     bspTree = newTop;
                     treeArea = newArea;
                 	System.out.println("bottom: newArea = " + newArea);
                 }
             }
 
             insertObject(shape, bounds, bounds, treeArea, bspTree);
         }
         // checkConsistency();
     }
 
     /**
      * Check the consistency of the tree, useful for debugging.
      */
     /*
     public void checkConsistency()
     {
         if (! debugging) {
             return;
         }
 
         LinkedList<BSPNode> stack = new LinkedList<BSPNode>();
 
         stack.add(bspTree);
         while(! stack.isEmpty()) {
             BSPNode node = stack.removeLast();
             if (node != null) {
                 //Actor actor = node.getActor();
                 //Rect actorBounds = getActorBounds(actor);
                 Rect nodeArea = node.getArea();
                 //if (movingActor != actor && Rect.getIntersection(actorBounds, nodeArea) == null) {
                 //    println("Node doesn't contain part of actor!");
                 //    throw new IllegalStateException();
                 //}
 
                 // check the same actor doesn't occur further up tree
                 BSPNode p = node.getParent();
                 while (p != null) {
                     if (p.getActor() == actor) {
                         println("Actor " + actor + " occurs further up tree! node=" + node);
                         throw new IllegalStateException();
                     }
                     p = p.getParent();
                 }
 
                 stack.add(node.getLeft());
                 stack.add(node.getRight());
             }
         }
     }
     */
 
     /**
      * Insert a shape into the tree at the given position
      *
      * @param shape   The shape to insert
      * @param shapeBounds  The total bounds of the shape
      * @param bounds  The bounds of the shape (limited to the present area)
      * @param area    The total area represented by the current search node
      * @param node    The current search node (null, if the search has reached its end!)
      */
     private void insertObject(Shape shape, Rect shapeBounds, Rect bounds, Rect area, BSPNode node)
     {
         // the current search node might already contain the
         // actor...
         if (node.containsShape(shape)) {
             return;
         }
 
         // If there's no actor at all in the node yet, then we can stop here.
         // Also, if the area is sufficiently small, there's no point subdividing it.
         if (node.isEmpty() || (area.getWidth() <= shapeBounds.getWidth()
                 && area.getHeight() <= shapeBounds.getHeight())) {
             node.addShape(shape);
             return;
         }
 
         // The search continues...
         Rect leftArea = node.getLeftArea();
         Rect rightArea = node.getRightArea();
 
         Rect leftIntersects = Rect.getIntersection(leftArea, bounds);
         Rect rightIntersects = Rect.getIntersection(rightArea, bounds);
 
         if (leftIntersects != null) {
             if (node.getLeft() == null) {
                 BSPNode newLeft = createNewNode(leftArea);
                 newLeft.addShape(shape);
                 node.setChild(PARENT_LEFT, newLeft);
             }
             else {
                 insertObject(shape, shapeBounds, leftIntersects, leftArea, node.getLeft());
             }
         }
 
         if (rightIntersects != null) {
             if (node.getRight() == null) {
                 BSPNode newRight = createNewNode(rightArea);
                 newRight.addShape(shape);
                 node.setChild(PARENT_RIGHT, newRight);
             }
             else {
                 insertObject(shape, shapeBounds, rightIntersects, rightArea, node.getRight());
             }
         }
     }
 
     /**
      * Create a new node for the given area.
      */
     private BSPNode createNewNode(Rect area)
     {
         int splitAxis;
         float splitPos;
         if (area.getWidth() > area.getHeight()) {
             splitAxis = X_AXIS;
             splitPos = area.getMiddleX();
         }
         else {
             splitAxis = Y_AXIS;
             splitPos = area.getMiddleY();
         }
         BSPNode newNode = BSPNodeCache.getBSPNode();
         newNode.setArea(area);
         newNode.setSplitAxis(splitAxis);
         newNode.setSplitPos(splitPos);
         return newNode;
     }
 
     public final Rect getShapeBounds(Shape shape)
     {
         Rect r = new Rect(shape.getBounds());
         return r;
     }
 
 //    public static void printTree(BSPNode node, String indent, String lead)
 //    {
 //        if (node == null) {
 //            return;
 //        }
 //
 //        String xx = lead;
 //        xx += node + ": ";
 //        xx += node.getArea();
 //        println(xx);
 //
 //        BSPNode left = node.getLeft();
 //        BSPNode right = node.getRight();
 //
 //        if (left != null) {
 //            String newIndent;
 //            if (right != null) {
 //                newIndent = indent + " |";
 //            }
 //            else {
 //                newIndent = indent + "  ";
 //            }
 //            printTree(left, newIndent, indent + " \\L-");
 //        }
 //
 //        if (right != null) {
 //            printTree(node.getRight(), indent + "  ", indent + " \\R-");
 //        }
 //    }
 
 //    public void printTree()
 //    {
 //        printTree(bspTree, "", "");
 //    }
 
     public synchronized void removeObject(Shape object)
     {
         // checkConsistency();
         ShapeNode node = getNodeForShape(object);
 
         while (node != null) {
             BSPNode bspNode = node.getBSPNode();
             node.remove();
             checkRemoveNode(bspNode);
             node = getNodeForShape(object);
         }
         // checkConsistency();
     }
 
     /**
      * Check whether a node can be removed, and remove it if so, traversing up the
      * tree and so on. Returns the highest node which wasn't removed.
      */
     private BSPNode checkRemoveNode(BSPNode node)
     {
         while (node != null && node.isEmpty()) {
             BSPNode parent = node.getParent();
             int side = (parent != null) ? parent.getChildSide(node) : PARENT_NONE;
             BSPNode left = node.getLeft();
             BSPNode right = node.getRight();
             if (left == null) {
                 if (parent != null) {
                     if (right != null) {
                         right.setArea(node.getArea());
                     }
                     parent.setChild(side, right);
                 }
                 else {
                     bspTree = right;
                     if (right != null) {
                         right.setParent(null);
                     }
                 }
                 node.setChild(PARENT_RIGHT, null);
                 BSPNodeCache.returnNode(node);
                 node = parent;
             }
             else if (right == null) {
                 if (parent != null) {
                     if (left != null) {
                         left.setArea(node.getArea());
                     }
                     parent.setChild(side, left);
                 }
                 else {
                     bspTree = left;
                     if (left != null) {
                         left.setParent(null);
                     }
                 }
                 node.setChild(PARENT_LEFT, null);
                 BSPNodeCache.returnNode(node);
                 node = parent;
             }
             else {
                 break;
             }
         }
 
         return node;
     }
 
 //    private static int dbgCounter = 0;
 //
 //    private static void println(String s)
 //    {
 //        if (dbgCounter < 3000) {
 //            System.out.println(s);
 //            // dbgCounter++;
 //        }
 //    }
 
     public static ShapeNode getNodeForShape(Shape object)
     {
         return getShapeNode(object);
     }
 
     public static void setNodeForShape(Shape object, ShapeNode node)
     {
         setShapeNode(object, node);
     }
 
     /**
      * An actors position or size has changed - update the tree.
      */
     private synchronized void updateObject(Shape object)
     {
         //checkConsistency();
         ShapeNode node = getNodeForShape(object);
         if (node == null) {
             // It seems that this can get called before the actor is added to the
             // checker...
             return;
         }
 
         Rect newBounds = getShapeBounds(object);
         if (! bspTree.getArea().contains(newBounds)) {
             // The actor has moved out of the existing tree area
             while (node != null) {
                 BSPNode rNode = node.getBSPNode();
                 node.remove();
                 checkRemoveNode(rNode);
                 node = node.getNext();
             }
             addObject(object);
             return;
         }
 
         // First process all existing actor nodes. We cull nodes which
         // no longer contain any part of the actor; also, if we find a
         // BSPNode which completely contains the actor, we just throw
         // all the other actor nodes away.
         while (node != null) {
             //updateNodeForMovedObject(object, newBounds, bspNode);
             BSPNode bspNode = node.getBSPNode();
             Rect bspArea = bspNode.getArea();
             if (bspArea.contains(newBounds)) {
                 // Ok, we found a BSPNode which completely contains the
                 // actor - we can throw all other actor nodes away
                 ShapeNode iter = getNodeForShape(object);
                 while (iter != null) {
                     if (iter != node) {
                         BSPNode rNode = iter.getBSPNode();
                         iter.remove();
                         checkRemoveNode(rNode);
                     }
                     iter = iter.getNext();
                 }
                 return;
             }
             else if (! bspArea.intersects(newBounds)) {
                 // This actor node is no longer needed
                 BSPNode rNode = node.getBSPNode();
                 node.remove();
                 checkRemoveNode(rNode);
             }
             node.clearMark();
             node = node.getNext();
         }
 
         // If we got here, there was no single node which contained the whole
         // actor (and we have culled any nodes which no longer contain any
         // part of the actor). We now need to find a suitable BSPNode
         // and do a re-insertion.
         node = getNodeForShape(object);
         BSPNode bspNode;
         Rect bspArea;
         if (node != null) {
             bspNode = node.getBSPNode();
             while (bspNode != null && ! bspNode.getArea().contains(newBounds)) {
                 bspNode = bspNode.getParent();
             }
             if (bspNode == null) {
                 // No node contains the whole actor; we need to expand the tree size
                 // First: remove old actor nodes
                 while (node != null) {
                     bspNode = node.getBSPNode();
                     node.remove();
                     checkRemoveNode(bspNode);
                     node = node.getNext();
                 }
                 // Now: expand the tree
                 addObject(object);
                 return;
             }
         }
         else {
             bspNode = bspTree;
         }
 
         // Note, we can pass null as the parent because bspNode is guaranteed not to be null.
         bspArea = bspNode.getArea();
         insertObject(object, newBounds, newBounds, bspArea, bspNode);
 
         // Finally, it's possible the object changed size and therefore has been stored
         // in higher nodes than previously. This means there are duplicate actor nodes.
         // The insertObject call will mark all the nodes it touches, so we need remove
         // any unmarked nodes.
         node = getNodeForShape(object);
         while (node != null) {
             if (! node.checkMark()) {
                 bspNode = node.getBSPNode();
                 node.remove();
                 checkRemoveNode(bspNode);
             }
             node = node.getNext();
         }
 
         // checkConsistency();
     }
 
     public void updateObjectLocation(Shape object)
     {
         updateObject(object);
     }
 
     public void updateObjectSize(Shape object)
     {
         updateObject(object);
     }
 
     private Set<Shape> getIntersectingObjects(Rect r, CollisionQuery query)
     {
         Set<Shape> set = new HashSet<Shape>();
         getIntersectingObjects(r, query, set, bspTree);
         return set;
     }
 
     private void getIntersectingObjects(Rect r, CollisionQuery query, Set<Shape> resultSet, BSPNode startNode)
     {
         LinkedList<BSPNode> nodeStack = new LinkedList<BSPNode>();
 
         if (startNode != null) {
             nodeStack.add(startNode);
         }
 
         while (! nodeStack.isEmpty()) {
             BSPNode node = nodeStack.removeLast();
             if (node.getArea().intersects(r)) {
                 for (Shape shape : node)
                 {
                     if (query.checkCollision(shape)) {
                         if (! resultSet.contains(shape)) {
                             resultSet.add(shape);
                         }
                     }
                 }
 
                 BSPNode left = node.getLeft();
                 BSPNode right = node.getRight();
                 if (left != null) {
                     nodeStack.add(left);
                 }
                 if (right != null) {
                     nodeStack.add(right);
                 }
             }
         }
     }
 
     /**
      * Check if there is at least one actor in the given BSPNode which matches
      * the given collision query, and return it if so.
      */
     private Shape checkForOneCollision(Shape ignore, BSPNode node, CollisionQuery query)
     {
         for (Shape candidate : node)
         {
             if (ignore != candidate && query.checkCollision(candidate)) {
                 return candidate;
             }
         }
 
         return null;
     }
 
     /**
      * Search for a single object which matches the given collision
      * query, starting from the given tree node and searching only
      * down the tree.
      *
      * @param ignore - do not return this actor
      * @param r  Bounds - do not search nodes which don't intersect this
      * @param query  The query to check objects against
      * @param startNode  The node to begin the search from
      * @return  The actor found, or null
      */
     private Shape getOneObjectDownTree(Shape ignore, Rect r, CollisionQuery query, BSPNode startNode)
     {
         if (startNode == null) {
             return null;
         }
 
         LinkedList<BSPNode> nodeStack = new LinkedList<BSPNode>();
         nodeStack.add(startNode);
 
         while (! nodeStack.isEmpty()) {
             BSPNode node = nodeStack.removeLast();
             if (node.getArea().intersects(r)) {
                 Shape res = checkForOneCollision(ignore, node, query);
                 if (res != null) {
                     return res;
                 }
 
                 BSPNode left = node.getLeft();
                 BSPNode right = node.getRight();
                 if (left != null) {
                     nodeStack.add(left);
                 }
                 if (right != null) {
                     nodeStack.add(right);
                 }
             }
         }
 
         return null;
     }
 
     /**
      * Search down the tree, but only so far as the last node which fully contains the area.
      * @param r
      * @param query
      * @param shape
      * @return
      */
     private Shape getOneIntersectingDown(Rect r, CollisionQuery query, Shape shape)
     {
         if (bspTree == null) {
             return null;
         }
 
         LinkedList<BSPNode> nodeStack = new LinkedList<BSPNode>();
         nodeStack.add(bspTree);
 
         while (! nodeStack.isEmpty()) {
             BSPNode node = nodeStack.removeLast();
             if (node.getArea().contains(r)) {
                 Shape res = checkForOneCollision(shape, node, query);
                 if (res != null) {
                     return res;
                 }
 
                 BSPNode left = node.getLeft();
                 BSPNode right = node.getRight();
                 if (left != null) {
                     nodeStack.add(left);
                 }
                 if (right != null) {
                     nodeStack.add(right);
                 }
             }
         }
 
         return null;
     }
 
     /**
      * Search up the tree, up to (not including) the node which fully contains the area.
      * @param r
      * @param query
      * @param shape
      * @param start
      */
     public Shape getOneIntersectingUp(Rect r, CollisionQuery query, Shape shape, BSPNode start)
     {
         while (start != null && ! start.getArea().contains(r)) {
             Shape res = checkForOneCollision(shape, start, query);
             if (res != null) {
                 return res;
             }
             start = start.getParent();
         }
         return null;
     }
 
     @SuppressWarnings("unchecked")
     public <T extends Shape> Set<T> getObjectsAt(float x, float y, Class<T> cls)
     {
         synchronized (pointQuery) {
             pointQuery.init(x, y, cls);
             return (Set<T>) getIntersectingObjects(new Rect(x, y, 1, 1), pointQuery);
         }
     }
 
     @SuppressWarnings("unchecked")
     public <T extends Shape> Set<T> getIntersectingObjects(Shape shape,
             Class<T> cls)
     {
         Rect r = getShapeBounds(shape);
 
         synchronized (shapeQuery) {
             shapeQuery.init(cls, shape);
             return (Set<T>) getIntersectingObjects(r, shapeQuery);
         }
     }
 
     @SuppressWarnings("unchecked")
     public <T extends Shape> Set<T> getObjectsInRange(float x, float y, float r,
             Class<T> cls)
     {
         float size = 2 * r;
 
         Rect rect = new Rect((x - r),
                 (y - r),
                 size,
                 size);
 
         Set<T> result;
         synchronized (shapeQuery) {
             shapeQuery.init(cls, null);
             result = (Set<T>) getIntersectingObjects(rect, shapeQuery);
         }
 
         Iterator<T> i = result.iterator();
         synchronized (inRangeQuery) {
             inRangeQuery.init(x, y, r);
             while (i.hasNext()) {
                 if (! inRangeQuery.checkCollision(i.next())) {
                     i.remove();
                 }
             }
         }
 
         return result;
     }
 
     @SuppressWarnings("unchecked")
     public <T extends Shape> Set<T> getNeighbors(Shape shape, float distance,
             boolean diag, Class<T> cls)
     {
         float x = shape.getX();
         float y = shape.getY();
 
         Rect r = new Rect(x - distance, y - distance, distance * 2 + 1, distance * 2 + 1);
 
         synchronized (neighbourQuery) {
             neighbourQuery.init(x, y, distance, diag, cls);
             return (Set<T>) getIntersectingObjects(r, neighbourQuery);
         }
     }
 
     public <T extends Shape> Set<T> getObjectsInDirection(float x, float y,
             float angle, float length, Class<T> cls)
     {
         // non-functional
         // return new ArrayList<T>();
         throw new UnsupportedOperationException("not implemented!");
     }
 
     @SuppressWarnings("unchecked")
     public <T extends Shape> Set<T> getObjects(Class<T> cls)
     {
         Set<T> set = new HashSet<T>();
         LinkedList<BSPNode> nodeStack = new LinkedList<BSPNode>();
 
         if (bspTree != null) {
             nodeStack.add(bspTree);
         }
 
         while (! nodeStack.isEmpty()) {
             BSPNode node = nodeStack.removeLast();
             for (Shape shape : node)
             {
                 if (cls == null || cls.isInstance(shape)) {
                     set.add((T) shape);
                 }
             }
             BSPNode left = node.getLeft();
             BSPNode right = node.getRight();
             if (left != null) {
                 nodeStack.add(left);
             }
             if (right != null) {
                 nodeStack.add(right);
             }
         }
 
         return set;
     }
 
     public Set<Shape> getObjects()
     {
         return getObjects(null);
     }
 
     public final void startSequence()
     {
         // Nothing necessary.
     }
 
     @SuppressWarnings("unchecked")
     public <T extends Shape> T getOneObjectAt(Shape object, float dx, float dy,
             Class<T> cls)
     {
         synchronized (pointQuery) {
             float px = dx;
             float py = dy;
             pointQuery.init(px, py, cls);
             CollisionQuery query = pointQuery;
             if (cls != null) {
                 query = new ClassQuery(cls, pointQuery);
             }
             // Use of getOneIntersectingDown is ok, because the area is only 1x1 pixel
             // in size - it will be contained by all nodes.
             return (T) getOneIntersectingDown(new Rect(px, py, 1, 1), query, object);
         }
     }
 
     @SuppressWarnings("unchecked")
     public <T extends Shape> T getOneIntersectingObject(Shape shape, Class<T> cls)
     {
         Rect r = getShapeBounds(shape);
         synchronized (shapeQuery) {
             shapeQuery.init(cls, shape);
 
             ShapeNode node = getNodeForShape(shape);
             do {
                 BSPNode bspNode = node.getBSPNode();
                 T ret = (T) getOneObjectDownTree(shape, r, shapeQuery, bspNode);
                 if (ret != null) {
                     return ret;
                 }
                 ret = (T) getOneIntersectingUp(r, shapeQuery, shape, bspNode.getParent());
                 if (ret != null) {
                     return ret;
                 }
                 node = node.getNext();
             }
             while (node != null);
             return (T) getOneIntersectingDown(r, shapeQuery, shape);
         }
     }
 }
