 package com.gwtao.ui.viewdriver.client;
 
 import org.shu4j.utils.permission.Permission;
 
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.TakesValue;
 import com.google.gwt.user.client.ui.ValueBoxBase;
 import com.google.gwt.user.client.ui.Widget;
 
 public class WidgetAdapter<M, V> {
 
   private Widget widget;
   private Integer dirtyHash;
   private TakesValue<V> tv;
   private IValueAdapter<M, V> va;
 
   public WidgetAdapter(Widget widget, TakesValue<V> tv, IValueAdapter<M, V> va) {
     this.widget = widget;
     this.tv = tv;
     this.va = va;
   }
 
   public void clearDirty() {
     V value = tv.getValue();
     dirtyHash = value == null ? null : value.hashCode();
   }
 
   public boolean isDirty() {
     V value = tv.getValue();
     Integer currentHash = value == null ? null : value.hashCode();
    if (dirtyHash == null)
      return currentHash != null;
    else if (currentHash == null)
      return false;
    else
      return !dirtyHash.equals(currentHash);
   }
 
   public Widget getWidget() {
     return widget;
   }
 
   public void setPermission(Permission perm) {
     setReadonly(!perm.isAllowed());
     setVisible(perm.isVisible());
   }
 
   private void setVisible(boolean visible) {
     widget.setVisible(visible);
   }
 
   private void setReadonly(boolean readOnly) {
     if (widget instanceof ValueBoxBase) {
       ((ValueBoxBase<?>) widget).setReadOnly(readOnly);
     }
     else {
       DOM.setElementPropertyBoolean(widget.getElement(), "disabled", readOnly);
     }
   }
 
   public void updateView(M model) {
     tv.setValue(va.getValue(model));
   }
 
   public void updateModel(M model) {
     va.setValue(model, tv.getValue());
   }
 }
