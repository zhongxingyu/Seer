 package com.eagerlogic.cubee.client.components;
 
 import com.eagerlogic.cubee.client.style.styles.Padding;
 import com.google.gwt.dom.client.Element;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  *
  * @author dipacs
  */
 public abstract class ALayout extends AComponent {
 
     private final LayoutChildren children = new LayoutChildren(this);
 
     public ALayout(Element element) {
         super(element);
 
         this.applyDefaultStyle(ALayout.class);
     }
 
     protected LayoutChildren getChildren() {
         return children;
     }
 
     protected abstract void onChildAdded(AComponent child);
 
     protected abstract void onChildRemoved(AComponent child, int index);
 
     protected abstract void onChildrenCleared();
 
     @Override
     public final void layout() {
         this.needsLayout = false;
         for (AComponent child : getChildren()) {
             if (child != null) {
                 if (child.isNeedsLayout()) {
                     child.layout();
                 }
             }
         }
         this.onLayout();
         this.measure();
     }
 
     @Override
     final boolean doPointerEventClimbingUp(int screenX, int screenY, int x, int y, int wheelVelocity,
             boolean altPressed, boolean ctrlPressed, boolean shiftPressed, boolean metaPressed, int type) {
         if (!handlePointerProperty().get()) {
             return false;
         }
         if (!enabledProperty().get()) {
             return true;
         }
         if (!visibleProperty().get()) {
             return false;
         }
         if (onPointerEventClimbingUp(screenX, screenY, x, y, wheelVelocity, altPressed,
                 ctrlPressed, shiftPressed, metaPressed, type)) {
             for (int i = getChildren().size() - 1; i >= 0; i--) {
                 AComponent child = getChildren().get(i);
                 if (child != null) {
                     int parentX = x;
                     int parentY = y;
                     Padding p = this.paddingProperty().get();
                     if (p != null) {
                         parentX -= p.getLeftPadding();
                         parentY -= p.getTopPadding();
                     }
                     if (child.isIntersectsPoint(parentX, parentY)) {
                         int childX = parentX - child.getLeft() - child.translateXProperty().get();
                         int childY = parentY - child.getTop() - child.translateYProperty().get();
                         // TODO rotate and scale child point
                         if (child.doPointerEventClimbingUp(screenX, screenY, childX, childY, wheelVelocity,
                                 altPressed, ctrlPressed, shiftPressed, metaPressed, type)) {
                             return true;
                         }
                     }
                 }
             }
         }
         if (pointerTransparentProperty().get()) {
             return false;
         } else {
             return onPointerEventFallingDown(screenX, screenY, x, y, wheelVelocity, altPressed,
                     ctrlPressed, shiftPressed, metaPressed, type);
         }
 
     }
 
     /**
      * Called by the layout after the children of this layout are measured but before this layout measured. You need to
      * set the size of this layout if needed and set the positions of the children if needed.
      */
     protected abstract void onLayout();
     
     public final List<AComponent> getComponentsAtPosition(int x, int y) {
         LinkedList<AComponent> res = new LinkedList<AComponent>();
         getComponentsAtPosition(this, x, y, res);
         return res;
     }
     
     private void getComponentsAtPosition(ALayout root, int x, int y, LinkedList<AComponent> result) {
         if (x >= 0 && x < root.boundsWidthProperty().get() && y >= 0 && y <= root.boundsHeightProperty().get()) {
            result.addFirst(root);
            for (AComponent component : root.getChildren()) {
                int tx = x - component.getLeft() - root.translateXProperty().get();
                int ty = y - component.getTop() - root.translateYProperty().get();
                 if (component instanceof ALayout) {
                     getComponentsAtPosition((ALayout)component, tx, ty, result);
                 } else {
                     if (tx >= 0 && tx < component.boundsWidthProperty().get() && y >= 0 && y <= component.boundsHeightProperty().get()) {
                         result.addFirst(component);
                     }
                 }
             }
         }
     }
 }
