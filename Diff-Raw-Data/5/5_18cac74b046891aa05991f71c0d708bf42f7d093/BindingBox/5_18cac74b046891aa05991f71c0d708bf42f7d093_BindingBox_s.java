 package com.joshondesign.treegui;
 
 import com.joshondesign.treegui.docmodel.SketchDocument;
 import com.joshondesign.treegui.docmodel.SketchNode;
 import java.awt.geom.Point2D;
 import org.joshy.gfx.draw.FlatColor;
 import org.joshy.gfx.draw.Font;
 import org.joshy.gfx.draw.GFX;
 import org.joshy.gfx.event.ActionEvent;
 import org.joshy.gfx.event.Callback;
 import org.joshy.gfx.event.EventBus;
 import org.joshy.gfx.event.MouseEvent;
 import org.joshy.gfx.node.control.Button;
 import org.joshy.gfx.node.control.Label;
 import org.joshy.gfx.node.layout.GridBox;
 import org.joshy.gfx.util.u;
 
 public class BindingBox extends GridBox {
     public BindingBox() {
         setFill(FlatColor.BLACK);
         reset();
     }
 
 
     public void addProperty(final Canvas canvas, final SketchDocument doc, final SketchNode node,
                             final String prop,
                             final Binding sourceBinding, Binding targetBinding, boolean isSource, boolean canUse) {
         //HFlexBox row = new HFlexBox();
         BindingStateButton targetButton = new BindingStateButton();
         if(targetBinding != null) {
             targetButton.setSelected(true);
         }
         if(isSource) targetButton.setEnabled(false);
         if(!canUse) targetButton.setEnabled(false);
 
         final BindingStateButton sourceButton = new BindingStateButton();
         if(sourceBinding != null) {
             sourceButton.setSelected(true);
         }
         if(!isSource) sourceButton.setEnabled(false);
         if(!canUse) sourceButton.setEnabled(false);
 
         addControl(targetButton);
         Font fnt = Font.name(Font.DEFAULT.getName()).weight(Font.Weight.Bold).size(Font.DEFAULT.getSize()).resolve();
         addControl(new Label(prop).setFont(fnt).setColor(FlatColor.hsb(0, 0, 0.9)));
         addControl(sourceButton);
         nextRow();
         this.setPrefHeight(this.getPrefHeight()+20);
         //row.add(targetButton);
         //row.add(new Label(prop));
         //row.add(sourceButton);
         //add(row);
 
         if(isSource) {
             Callback<MouseEvent> callback = new Callback<MouseEvent>() {
                 public void call(MouseEvent mouseEvent) throws Exception {
                     if(mouseEvent.getType() == MouseEvent.MousePressed) {
                        canvas.startDragPoint = mouseEvent.getPointInNodeCoords(canvas);
                         if(sourceBinding != null) {
                             sourceButton.setSelected(false);
                             doc.getBindings().remove(sourceBinding);
                         }
                         canvas.currentBinding.setSourceProperty(prop);
                     }
                     if(mouseEvent.getType() == MouseEvent.MouseDragged) {
                         canvas.dragging = true;
                        canvas.currentDragPoint = mouseEvent.getPointInNodeCoords(canvas);
                         setDrawingDirty();
                     }
                     if(mouseEvent.getType() == MouseEvent.MouseReleased) {
                         canvas.dragging = false;
                         canvas.startDragPoint = null;
                         canvas.currentDragPoint = null;
                         setDrawingDirty();
                         Point2D pt = canvas.toEditRootCoords(mouseEvent.getPointInNodeCoords(canvas));
                         SketchNode node = canvas.findNode(pt);
                         if(node == null) return;
                         canvas.showTargetPopup(node, mouseEvent);
                     }
                 }
             };
             EventBus.getSystem().addListener(sourceButton, MouseEvent.MouseAll, callback);
         } else {
             targetButton.onClicked(new Callback<ActionEvent>() {
                 public void call(ActionEvent actionEvent) throws Exception {
                     Binding currentBinding = canvas.currentBinding;
                     canvas.currentBinding.setTarget(node);
                     currentBinding.setTargetProperty(prop);
                     u.p("added a binding: " + currentBinding);
                     doc.getBindings().add(currentBinding);
                     canvas.currentBinding = null;
                     canvas.popup2.setVisible(false);
                     canvas.popup.setVisible(false);
                 }
             });
         }
     }
 
     public void reset() {
         super.removeAll();
         super.reset();
         this.setPrefWidth(140);
         this.setPrefHeight(20);
         setPadding(0);
         this.createColumn(18, Align.Center);
         this.createColumn(80, Align.Center);
         this.createColumn(18, Align.Center);
         debug(false);
     }
 
     private class BindingStateButton extends Button {
         public BindingStateButton() {
             setPrefWidth(16);
             setPrefHeight(16);
         }
 
         public void draw(GFX g) {
 
             //bound enabled
             //bound disabled
             //unbound enabled
             //unbound disabled
 
             //rim
             double s = 14;
             g.setStrokeWidth(2);
             g.setPaint(FlatColor.hsb(0,0,0.8));
             if(!isEnabled()) {
                 g.setPaint(FlatColor.hsb(0,0,0.4));
             }
             g.drawOval(0,0,s,s);
 
             //center
             if(isSelected()) {
                 drawCenter(g);
             }
 
             if(isEnabled() && !isSelected() && isHovered()) {
                 drawCenter(g);
             }
             g.setStrokeWidth(1);
 
         }
 
         private void drawCenter(GFX g) {
             double d = 10;
             g.setPaint(FlatColor.RED);
             if(!isEnabled()) {
                 g.setPaint(FlatColor.hsb(0,0.5,0.8));
             }
             g.fillOval(3,3,d,d);
         }
     }
 }
