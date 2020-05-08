 /*
  * To change this license header, choose License Headers in Project Properties.
  * To change this template file, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.eagerlogic.cubee.client.components.fontawesome;
 
 import com.eagerlogic.cubee.client.EventQueue;
 import com.eagerlogic.cubee.client.components.AUserControl;
 import com.eagerlogic.cubee.client.properties.BooleanProperty;
 import com.eagerlogic.cubee.client.properties.ColorProperty;
 import com.eagerlogic.cubee.client.properties.IChangeListener;
 import com.eagerlogic.cubee.client.properties.IntegerProperty;
 import com.eagerlogic.cubee.client.properties.Property;
 import com.eagerlogic.cubee.client.style.Style;
 import com.eagerlogic.cubee.client.style.styles.Color;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.user.client.DOM;
 
 /**
  *
  * @author dipacs
  */
 public final class FAIcon extends AUserControl {
 
     public static class StyleClass<T extends FAIcon> extends AUserControl.StyleClass<T> {
 
         private final Style<Integer> size = new Style<Integer>(null, false);
         private final Style<Color> foreColor = new Style<Color>(null, false);
         private final Style<Boolean> spin = new Style<Boolean>(null, false);
         private final Style<EIcon> icon = new Style<EIcon>(null, false);
 
         @Override
         public void apply(T component) {
             super.apply(component);
 
             size.apply(component.sizeProperty());
             foreColor.apply(component.foreColorProperty());
             spin.apply(component.spinProperty());
             icon.apply(component.iconProperty());
         }
 
         public Style<Integer> getSize() {
             return size;
         }
 
         public Style<Color> getForeColor() {
             return foreColor;
         }
 
         public Style<Boolean> getSpin() {
             return spin;
         }
 
         public Style<EIcon> getIcon() {
             return icon;
         }
 
     }
 
     static {
         initFA();
     }
 
     private static native void initFA() /*-{
      $wnd.fastyle = document.createElement('link');
      $wnd.fastyle.href = "//netdna.bootstrapcdn.com/font-awesome/4.0.3/css/font-awesome.css";
      $wnd.fastyle.rel = "stylesheet";
      $doc.getElementsByTagName('head')[0].appendChild($wnd.fastyle);
      }-*/;
 
     private final IntegerProperty size = new IntegerProperty(16, false, false);
     private final ColorProperty foreColor = new ColorProperty(Color.BLACK, false, false);
     private final BooleanProperty spin = new BooleanProperty(false, false, false);
     private final Property<EIcon> icon = new Property<EIcon>(EIcon.RUB, false, false);
 
     private Element iElement;
 
     private IChangeListener changeListener = new IChangeListener() {
 
         @Override
         public void onChanged(Object sender) {
             refreshStyle();
         }
     };
 
 
 
     public FAIcon(EIcon icon) {
         if (icon == null) {
             throw new NullPointerException("The icon parameter can not be null.");
         }
 
         this.widthProperty().bind(size);
         this.heightProperty().bind(size);
         this.icon.set(icon);
 
         iElement = DOM.createElement("i");
         this.getElement().appendChild(iElement);
 
         size.addChangeListener(changeListener);
         foreColor.addChangeListener(changeListener);
         spin.addChangeListener(changeListener);
         this.icon.addChangeListener(changeListener);
 
        iElement.getStyle().setPosition(com.google.gwt.dom.client.Style.Position.ABSOLUTE);
 
         refreshStyle();
     }
 
     private void refreshStyle() {
         iElement.setClassName("fa");
         if (icon.get() != null) {
             iElement.addClassName(icon.get().getClassName());
         }
         iElement.getStyle().setFontSize(size.get(), com.google.gwt.dom.client.Style.Unit.PX);
         iElement.getStyle().setColor(foreColor.get().toCSS());
 
         if (spin.get()) {
             iElement.addClassName("fa-spin");
         }
 
         final int s = size.get();
         final int width = iElement.getClientWidth();
         EventQueue.getInstance().invokeLater(new Runnable() {
 
             @Override
             public void run() {
 //                iElement.getStyle().setProperty("transform", "translate(" + ((s - width) / 2) + "px, 0px)");
 //                iElement.getStyle().setProperty("msTransform", "translate(" + ((s - width) / 2) + "px, 0px)");
 //                iElement.getStyle().setProperty("webkitTransform", "translate(" + ((s - width) / 2) + "px, 0px)");
             }
         });
 
         iElement.getStyle().setProperty("webkitBackfaceVisibility", "hidden");
     }
 
     public IntegerProperty sizeProperty() {
         return size;
     }
 
     public ColorProperty foreColorProperty() {
         return foreColor;
     }
 
     public BooleanProperty spinProperty() {
         return spin;
     }
 
     public Property<EIcon> iconProperty() {
         return icon;
     }
 
 }
