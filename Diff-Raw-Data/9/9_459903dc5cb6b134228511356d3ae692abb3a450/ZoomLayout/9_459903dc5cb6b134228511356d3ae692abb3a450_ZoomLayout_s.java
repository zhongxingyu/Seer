 /*
  * Copyright (C) 2011 Peransin Nicolas.
  * Use is subject to license terms.
  */
 package org.mypsycho.swing.layout;
 
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.LayoutManager;
 import java.awt.LayoutManager2;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.JComponent;
 
 /**
  * Layout similaire au null layout mais permettant de definir une taille de
  * reference qui sera la taille preferee et de faire zoomer les composants.
 * Les composant doivent tre ajoutes avec une ZoomConstraint.
  *
  * Si une dimension de reference est negative, un calcul est effectue de
  * maniere a ce que le layout inclut tous les composants sur cet axe.
  *
  * Si ZoomConstaint est null, le composant utilise tout l'espace possible
  *
  * Si une contrainte de hauteur ou largeur est negative, la dimension
  *   prefere du composant associe est utilise
  *
  * @author Peransin Nicolas
  */
 public class ZoomLayout implements LayoutManager2, Serializable {
 
     /**
      * Generated serialized version
      */
     private static final long serialVersionUID = 9007898665269952279L;
 
 
     protected int width = ZoomConstraint.NOT_DEFINED; // reference size for container
     protected int height = ZoomConstraint.NOT_DEFINED;
 
     protected List<Component> allComponents = new ArrayList<Component>(); // to keep the order
     protected Map<Component, ZoomConstraint> allConstraints = new HashMap<Component, ZoomConstraint>();
 
 
     public ZoomLayout() {}
     public ZoomLayout(int w, int h) {
         width = w;
         height = h;
     }
 
 
     //
     // Getters and Setters
     //
     public int getWidth() { return width; }
     public void setWidth(int w) { width = w; }
     public int getHeight() { return height; }
     public void setHeight(int h) { height = h; }
 
     public ZoomConstraint getConstraint(Component comp) {
         return allConstraints.get(comp);
     }
 
 
     public void setConstraint(Component comp, ZoomConstraint cst) {
         if (allComponents.contains(comp)) {
             allConstraints.put(comp, cst);
         }
     }
 
     protected Container container = null;
     //
     // Layout Core
     //
     public void addLayoutComponent(Component comp, Object constraints) {
         if ( (constraints == null) || (constraints instanceof ZoomConstraint)) {
             if (allComponents.contains(comp)) {
                 allComponents.remove(comp);
             }
             allComponents.add(comp);
             allConstraints.put(comp, (ZoomConstraint) constraints);
         } else {
             throw new IllegalArgumentException(getClass() + " can only handle ZoomConstraint objects");
         }
     }
 
     public void addLayoutComponent(String name, Component comp) { }
 
     public Dimension preferredLayoutSize(Container parent) {
         container = parent;
         synchronized (parent.getTreeLock()) {
 
             if ( (width == ZoomConstraint.NOT_DEFINED) || (height == ZoomConstraint.NOT_DEFINED)) {
                 int w = width;
                 int h = height;
 
                 /* Ascending computation */
                 Container granParent = container.getParent();
                 if (granParent != null) {
                     LayoutManager granLayout = granParent.getLayout();
                     if ( (granLayout != null) && (granLayout instanceof ZoomLayout)) {
                         ZoomLayout upperLayout = (ZoomLayout) granLayout;
                         ZoomConstraint cst = upperLayout.allConstraints.get(parent);
                         if (cst != null) {
                             if ( (width == ZoomConstraint.NOT_DEFINED) && (cst.bounds.width != ZoomConstraint.NOT_DEFINED)) {
                                 w = cst.bounds.width;
                             }
 
                             if ( (height == ZoomConstraint.NOT_DEFINED) && (cst.bounds.height != ZoomConstraint.NOT_DEFINED)) {
                                 h = cst.bounds.height;
                             }
                         }
                     }
                 }
 
                 /* Descending computation */
                 if ( (w == ZoomConstraint.NOT_DEFINED) || (h == ZoomConstraint.NOT_DEFINED)) {
                     int w2 = w;
                     int h2 = h;
                     int nbComps = allComponents.size();
                     for (int indComp = 0; indComp < nbComps; indComp++) {
                         Component comp = allComponents.get(indComp);
                         ZoomConstraint cst = allConstraints.get(comp);
 
                         if (cst != null) {
                             if (w < 0) {
                                 if (cst.bounds.width == ZoomConstraint.NOT_DEFINED) {
                                     w2 = Math.max(w2, cst.bounds.x + comp.getPreferredSize().width);
                                 } else {
                                     w2 = Math.max(w2, cst.bounds.x + cst.bounds.width);
                                 }
                             }
 
                             if (h < 0) {
                                 if (cst.bounds.height == ZoomConstraint.NOT_DEFINED) {
                                     h2 = Math.max(h2, cst.bounds.y + comp.getPreferredSize().height);
                                 } else {
                                     h2 = Math.max(h2, cst.bounds.y + cst.bounds.height);
                                 }
                             }
                         }
                     }
                     w = w2;
                     h = h2;
                 }
                 return new Dimension(w, h);
             } else {
                 return new Dimension(width, height);
             }
         }
     }
 
 
     public void layoutContainer(Container parent) {
         container = parent;
 
         synchronized (parent.getTreeLock()) {
             Dimension dRef = preferredLayoutSize(parent);
             Dimension dNow = parent.getSize();
             float zX = (dRef.width<=0) ? 1.0f : (float) dNow.width/dRef.width;
             float zY = (dRef.height<=0) ? 1.0f : (float) dNow.height/dRef.height;
 
             zoomComponents(parent, 0, 0, zX, zY, Math.min(zX, zY));
         }
     }
 
     protected void zoomComponents(Container parent, int offsetX, int offsetY,
                 float zX, float zY, float zF) {
 
         Dimension dNow = parent.getSize();
         int nbComps = allComponents.size();
         for (int indComp = 0; indComp < nbComps; indComp++) {
             Component comp = allComponents.get(indComp);
             ZoomConstraint cst = allConstraints.get(comp);
 
             // Zoom axis
             if ((cst == null) || (cst.bounds == null)) {
                 comp.setBounds(0, 0, dNow.width, dNow.height);
             } else {
                 int x = Math.round(cst.bounds.x*zX);
                 int y = Math.round(cst.bounds.y*zY);
                 int w = cst.bounds.width;
                 int h = cst.bounds.height;
 
                 if (w == ZoomConstraint.NOT_DEFINED) {
                     w = Math.round(comp.getPreferredSize().width*zX);
                 } else if (w < 0) {
                     x = Math.round((cst.bounds.x+w)*zX);
                     w = -Math.round(w*zX);
                 } else {
                     w = Math.round(w*zX);
                 }
 
                 if (h == ZoomConstraint.NOT_DEFINED) {
                     h = Math.round(comp.getPreferredSize().height*zY);
                 } else if (h < 0) {
                     y = Math.round((cst.bounds.y+h)*zY);
                     h = -Math.round(h*zY);
                 } else {
                     h = Math.round(h*zY);
                 }
 
                 comp.setBounds(x, y, w, h);
             }
 
             // Zoom fonts
             if ((cst != null) && (cst.getFont() != -1)) {
                 Font f = comp.getFont();
                 comp.setFont(f.deriveFont(Math.round(cst.getFont() * zF)));
             }
 
             if (comp instanceof Zoomable) {
                 ((Zoomable) comp).zoom(zX, zY);
             }
 
             if (comp instanceof JComponent) {
                 ((JComponent) comp).revalidate();
             }
         }
     }
 
 
 
     public void removeLayoutComponent(Component comp) {
         allComponents.remove(comp);
         allConstraints.remove(comp);
     }
 
 
     //
     // Generic behaviour
     //
     public Dimension minimumLayoutSize(Container parent) {
         container = parent;
         return preferredLayoutSize(parent);
     }
     public Dimension maximumLayoutSize(Container target) {
         return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
     }
     public float getLayoutAlignmentX(Container target) { return 0.5f; }
     public float getLayoutAlignmentY(Container target) { return 0.5f; }
     public void invalidateLayout(Container target) { }
 
     @Override
     public String toString() {
         if ((container != null) && (container.getLayout() == this)) {
             Dimension dRef = preferredLayoutSize(container);
             Dimension dNow = container.getSize();
             float zX = (dRef.width <= 0) ? 1.0f : (float) dNow.width / dRef.width;
             float zY = (dRef.height <= 0) ? 1.0f : (float) dNow.height / dRef.height;
             float z = Math.min(zX, zY);
 
             return ("ZoomLayout Ref=" + dRef + "; Real=" + dNow +
                         "; Zoom=[" + zX + ", " + zY + "] ("+ z +")");
         } else {
             container = null;
             return ("ZoomLayout Ref=[" + ", " + "]; no container");
         }
     }
 
 
 
     /* Serialization for beans */
     private void writeObject(ObjectOutputStream oos) throws IOException {
         oos.defaultWriteObject();
     }
     private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
         ois.defaultReadObject();
     }
 
 
 } // endClass ZoomLayout
