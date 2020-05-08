 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.eagerlogic.cubee.client.components;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 
 /**
  *
  * @author dipacs
  */
 public final class LayoutChildren implements Iterable<AComponent> {
     
     private final LinkedList<AComponent> children = new LinkedList<AComponent>();
     private final ALayout parent;
 
     LayoutChildren(ALayout parent) {
         this.parent = parent;
     }
     
     public void add(AComponent component) {
         if (component.getParent() != null) {
             throw new IllegalStateException("The component is already a child of a layout.");
         }
         
         component.setParent(parent);
 		
         children.add(component);
         parent.onChildAdded(component);
     }
     
     public void remove(AComponent component) {
 		int idx = children.indexOf(component);
 		if (idx < 0) {
 			throw new IllegalArgumentException("The given component isn't a child of this layout.");
 		}
 		remove(idx);
     }
     
     public void remove(int index) {
         AComponent removedComponent = children.remove(index);
         if (removedComponent != null) {
             removedComponent.setParent(null);
         }
         parent.onChildRemoved(removedComponent, index);
     }
     
     public void clear() {
         for (AComponent component : children) {
             if (component != null) {
                 component.setParent(null);
             }
         }
         children.clear();
 		parent.onChildrenCleared();
     }
     
    public void get(int index) {
        children.get(index);
     }
     
     public int indexOf(AComponent component) {
         return children.indexOf(component);
     }
     
     public int size() {
         return children.size();
     }
 
     @Override
     public Iterator<AComponent> iterator() {
 		// TODO create iterator which notifies parent when element is removed.
         return children.iterator();
     }
     
 }
