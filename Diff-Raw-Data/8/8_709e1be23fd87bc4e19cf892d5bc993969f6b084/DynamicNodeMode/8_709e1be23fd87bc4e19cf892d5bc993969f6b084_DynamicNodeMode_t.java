 package com.joshondesign.treegui.modes;
 
 import com.joshondesign.treegui.BindingUtils;
 import com.joshondesign.treegui.Mode;
 import com.joshondesign.treegui.docmodel.*;
 import com.joshondesign.treegui.model.TreeNode;
 import com.joshondesign.treegui.modes.aminojava.AminoParser;
 import com.joshondesign.treegui.modes.aminojava.DynamicNode;
 import com.joshondesign.xml.Doc;
 import com.joshondesign.xml.XMLParser;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import org.joshy.gfx.event.ActionEvent;
 import org.joshy.gfx.event.AminoAction;
 import org.joshy.gfx.event.Callback;
 import org.joshy.gfx.node.control.*;
 import org.joshy.gfx.stage.Stage;
 
 public abstract class DynamicNodeMode extends Mode {
     protected DynamicNode parse(Object o, DynamicNode.DrawDelegate del, DynamicNode base) {
         DynamicNode nd = BindingUtils.parseAnnotatedPOJO(o, del);
         if(base != null) {
             nd.copyPropertiesFrom(base);
         }
         return nd;
     }
 
     public interface Accumulate<T> {
         public T accum(SketchNode node, T value);
     }
 
     @Override
     public void modifyNodeMenu(Menu nodeMenu, SketchDocument doc) {
         super.modifyNodeMenu(nodeMenu, doc);
         nodeMenu.addItem("Align Left", AlignLeft(doc));
         nodeMenu.addItem("Align Center Horizontal", AlignCenterH(doc));
         nodeMenu.addItem("Align Right", AlignRight(doc));
         nodeMenu.addItem("Align Top", AlignTop(doc));
         nodeMenu.addItem("Align Center Vertical", AlignCenterV(doc));
         nodeMenu.addItem("Align Bottom", AlignBottom(doc));
 
        nodeMenu.addItem("Lower Node To Bottom", "shift OPEN_BRACKET", LowerNodeToBottom(doc));
        nodeMenu.addItem("Lower Node", "OPEN_BRACKET", LowerNode(doc));
        nodeMenu.addItem("Raise Node", "CLOSE_BRACKET", RaiseNode(doc));
        nodeMenu.addItem("Raise Node To Top", "shift CLOSE_BRACKET", RaiseNodeToTop(doc));
         nodeMenu.addItem("Same Width", SameWidth(doc));
         nodeMenu.addItem("Same Height", SameHeight(doc));
         nodeMenu.addItem("Distribute", DistributeNodes(doc));
         nodeMenu.addItem("Normalize Group", NormalizeGroup(doc));
 
     }
 
     @Override
     public void modifyDocumentMenu(Menu documentMenu, SketchDocument doc) {
         super.modifyDocumentMenu(documentMenu, doc);    //To change body of overridden methods use File | Settings | File Templates.
         documentMenu.addItem("Document Settings", EditDocumentSettingsAction(doc));
         documentMenu.addItem("Add Page", AddPageAction(doc));
         documentMenu.addItem("Previous Page", "LEFT", PrevPageAction(doc));
         documentMenu.addItem("Next Page", "RIGHT", NextPageAction(doc));
     }
 
     protected static AminoAction groupOnly(final SketchDocument doc, final AminoAction aminoAction) {
         return new AminoAction() {
             @Override
             public CharSequence getDisplayName() {
                 return aminoAction.getDisplayName();
             }
 
             @Override
             public void execute() throws Exception {
                 if (doc.getSelection().getSize() < 2) return;
                 aminoAction.execute();
             }
         };
     }
 
     private static AminoAction notEmpty(final SketchDocument doc, final AminoAction aminoAction) {
         return new AminoAction() {
             @Override
             public CharSequence getDisplayName() {
                 return aminoAction.getDisplayName();
             }
 
             @Override
             public void execute() throws Exception {
                 if (doc.getSelection().getSize() < 1) return;
                 aminoAction.execute();
             }
         };
     }
 
     protected static AminoAction named(final String name, final AminoAction action) {
         return new AminoAction() {
             @Override
             public CharSequence getDisplayName() {
                 return name;
             }
 
             @Override
             public void execute() throws Exception {
                 action.execute();
             }
         };
     }
 
     protected Double apply(Selection selection, Double minX, Accumulate<Double> acc) {
         for (SketchNode node : selection.children()) {
             minX = acc.accum(node, minX);
         }
         return minX;
     }
 
     private Integer apply(Selection selection, Integer minX, Accumulate<Integer> acc) {
         for (SketchNode node : selection.children()) {
             minX = acc.accum(node, minX);
         }
         return minX;
     }
 
     /* ==== reusable actions ==== */
     protected AminoAction AlignLeft(final SketchDocument doc) {
         return named("Align Left", groupOnly(doc, new AminoAction() {
             @Override
             public void execute() throws Exception {
                 double val = apply(doc.getSelection(), Double.MAX_VALUE, new Accumulate<Double>() {
                     public Double accum(SketchNode node, Double value) {
                         return Math.min(value, node.getInputBounds().getX() + node.getTranslateX());
                     }
                 });
 
                 apply(doc.getSelection(), val, new Accumulate<Double>() {
                     public Double accum(SketchNode node, Double value) {
                         double x = node.getInputBounds().getX() + node.getTranslateX();
                         node.setTranslateX(node.getTranslateX() + value - x);
                         return value;
                     }
                 });
             }
         }));
     }
 
     protected AminoAction NormalizeGroup(final SketchDocument doc) {
         return new AminoAction() {
             @Override
             public CharSequence getDisplayName() {
                 return "Normalize Group";
             }
 
             @Override
             public void execute() throws Exception {
                 if(doc.getSelection().getSize() != 1) return;
                 SketchNode nd = doc.getSelection().get(0);
                 if(! (nd instanceof Group)) return;
                 Group group = (Group) nd;
                 group.normalize();
             }
         };
     }
 
     protected AminoAction AlignCenterH(final SketchDocument doc) {
         return named("Align Center Horizontal", groupOnly(doc, new AminoAction() {
             @Override
             public void execute() throws Exception {
                 double val = apply(doc.getSelection(), Double.MIN_VALUE, new Accumulate<Double>() {
                     public Double accum(SketchNode node, Double value) {
                         return Math.max(value, node.getInputBounds().getCenterX() + node.getTranslateX());
                     }
                 });
                 apply(doc.getSelection(), val, new Accumulate<Double>() {
                     public Double accum(SketchNode node, Double value) {
                         double x = node.getInputBounds().getCenterX() + node.getTranslateX();
                         node.setTranslateX(node.getTranslateX() + value - x);
                         return value;
                     }
                 });
             }
         }));
     }
 
     protected AminoAction AlignRight(final SketchDocument doc) {
         return named("Align Right", groupOnly(doc, new AminoAction() {
             @Override
             public void execute() throws Exception {
                 double val = apply(doc.getSelection(), Double.MIN_VALUE, new Accumulate<Double>() {
                     public Double accum(SketchNode node, Double value) {
                         return Math.max(value, node.getInputBounds().getX2() + node.getTranslateX());
                     }
                 });
                 apply(doc.getSelection(), val, new Accumulate<Double>() {
                     public Double accum(SketchNode node, Double value) {
                         double x = node.getInputBounds().getX2() + node.getTranslateX();
                         node.setTranslateX(node.getTranslateX() + value - x);
                         return value;
                     }
                 });
             }
         }));
     }
 
     protected AminoAction AlignBottom(final SketchDocument doc) {
         return named("Align Bottom", groupOnly(doc, new AminoAction() {
             @Override
             public void execute() throws Exception {
                 double val = apply(doc.getSelection(), Double.MIN_VALUE, new Accumulate<Double>() {
                     public Double accum(SketchNode node, Double value) {
                         return Math.max(value, node.getInputBounds().getY2() + node.getTranslateY());
                     }
                 });
                 apply(doc.getSelection(), val, new Accumulate<Double>() {
                     public Double accum(SketchNode node, Double value) {
                         double y = node.getInputBounds().getY2() + node.getTranslateY();
                         node.setTranslateY(node.getTranslateY() + value - y);
                         return value;
                     }
                 });
             }
         }));
     }
 
     protected AminoAction AlignCenterV(final SketchDocument doc) {
         return named("Align Center Vertical", groupOnly(doc, new AminoAction() {
             @Override
             public void execute() throws Exception {
                 double val = apply(doc.getSelection(), Double.MIN_VALUE, new Accumulate<Double>() {
                     public Double accum(SketchNode node, Double value) {
                         return Math.max(value, node.getInputBounds().getCenterY() + node.getTranslateY());
                     }
                 });
                 apply(doc.getSelection(), val, new Accumulate<Double>() {
                     public Double accum(SketchNode node, Double value) {
                         double y = node.getInputBounds().getCenterY() + node.getTranslateY();
                         node.setTranslateY(node.getTranslateY() + value - y);
                         return value;
                     }
                 });
             }
         }));
     }
 
     protected AminoAction AlignTop(final SketchDocument doc) {
         return named("Align Top", groupOnly(doc, new AminoAction() {
             @Override
             public void execute() throws Exception {
                 double val = apply(doc.getSelection(), Double.MAX_VALUE, new Accumulate<Double>() {
                     public Double accum(SketchNode node, Double value) {
                         return Math.min(value, node.getInputBounds().getY() + node.getTranslateY());
                     }
                 });
                 apply(doc.getSelection(), val, new Accumulate<Double>() {
                     public Double accum(SketchNode node, Double value) {
                         double y = node.getInputBounds().getY() + node.getTranslateY();
                         node.setTranslateY(node.getTranslateY() + value - y);
                         return value;
                     }
                 });
             }
         }));
     }
 
     protected AminoAction LowerNode(final SketchDocument doc) {
         return named("Lower Node", notEmpty(doc, new AminoAction() {
             @Override
             public void execute() throws Exception {
                 final TreeNode<SketchNode> model = doc.getSelection().get(0).getParent();
                 Selection nodes = doc.getSelection();
                 //find the lowest node index
 
                 int min = apply(nodes, Integer.MAX_VALUE, new Accumulate<Integer>() {
                     public Integer accum(SketchNode node, Integer value) {
                         return Math.min(model.indexOf(node),value);
                     }
                 });
                 //if there is room to move down
                 if (min > 0) {
                     SketchNode prevNode = model.get(min - 1);
                     model.removeAll(nodes);
                     model.addAll(model.indexOf(prevNode), nodes);
                 } else {
                     //just remove and move all to the bottom
                     model.removeAll(nodes);
                     model.addAll(0, nodes);
                 }
             }
         }));
     }
 
     protected AminoAction LowerNodeToBottom(final SketchDocument doc) {
         return named("Lower Node", notEmpty(doc, new AminoAction() {
             @Override
             public void execute() throws Exception {
                 TreeNode<SketchNode> model = doc.getSelection().get(0).getParent();
                 Selection nodes = doc.getSelection();
                 //just remove and move all to the bottom
                 model.removeAll(nodes);
                 model.addAll(0, nodes);
             }
         }));
     }
 
     protected AminoAction RaiseNode(final SketchDocument doc) {
         return named("Raise Node", notEmpty(doc, new AminoAction() {
             @Override
             public void execute() throws Exception {
                 final TreeNode<SketchNode> model = doc.getSelection().get(0).getParent();
 
                 Selection nodes = doc.getSelection();
                 int max = apply(nodes, Integer.MIN_VALUE, new Accumulate<Integer>() {
                     public Integer accum(SketchNode node, Integer value) {
                         return Math.max(value, model.indexOf(node));
                     }
                 });
 
                 //if there is room to move up
                 if (max + 1 < model.getSize()) {
                     SketchNode nextNode = model.get(max + 1);
                     model.removeAll(nodes);
                     int n = model.indexOf(nextNode);
                     model.addAll(n + 1, nodes);
                 } else {
                     //just remove and move all to the top
                     model.removeAll(nodes);
                     model.addAll(nodes);
                 }
             }
         }));
     }
 
     protected AminoAction RaiseNodeToTop(final SketchDocument doc) {
         return named("Raise Node To Top", notEmpty(doc, new AminoAction() {
             @Override
             public void execute() throws Exception {
                 TreeNode<SketchNode> model = doc.getSelection().get(0).getParent();
                 Selection nodes = doc.getSelection();
                 //just remove and move all to the bottom
                 model.removeAll(nodes);
                 model.addAll(nodes);
             }
         }));
     }
     private AminoAction SameWidth(final SketchDocument doc) {
         return named("Same Width", groupOnly(doc, new AminoAction() {
             @Override
             public void execute() throws Exception {
                 Selection nodes = doc.getSelection();
                 double width = apply(nodes, Double.MIN_VALUE, new Accumulate<Double>() {
                     public Double accum(SketchNode node, Double value) {
                         return Math.max(node.getInputBounds().getWidth(), value);
                     }
                 });
                 apply(nodes, width, new Accumulate<Double>() {
                     public Double accum(SketchNode node, Double value) {
                         if(!(node instanceof DynamicNode)) return value;
                         DynamicNode nd = (DynamicNode) node;
                         if(nd.getResize() == Resize.None) return value;
                         nd.getProperty("width").setDoubleValue(value);
                         return value;
                     }
                 });
             }
         }));
     }
     private AminoAction SameHeight(final SketchDocument doc) {
         return named("Same Height", groupOnly(doc, new AminoAction() {
             @Override
             public void execute() throws Exception {
                 Selection nodes = doc.getSelection();
                 double width = apply(nodes, Double.MIN_VALUE, new Accumulate<Double>() {
                     public Double accum(SketchNode node, Double value) {
                         return Math.max(node.getInputBounds().getHeight(), value);
                     }
                 });
                 apply(nodes, width, new Accumulate<Double>() {
                     public Double accum(SketchNode node, Double value) {
                         if(!(node instanceof DynamicNode)) return value;
                         DynamicNode nd = (DynamicNode) node;
                         if(nd.getResize() == Resize.None) return value;
                         nd.getProperty("height").setDoubleValue(value);
                         return value;
                     }
                 });
             }
         }));
     }
 
     private AminoAction AddPageAction(final SketchDocument doc) {
         return named("Add New Page", new AminoAction() {
             @Override
             public void execute() throws Exception {
                 Page page = new Page();
                 page.add(new Layer());
                 doc.add(page);
             }
         });
     }
     private AminoAction NextPageAction(final SketchDocument doc) {
         return named("Next Page", new AminoAction() {
             @Override
             public void execute() throws Exception {
                 Page page = doc.getSelectedPage();
                 int index = doc.indexOf(page);
                 index++;
                 if(index > doc.getSize()-1) {
                     index = doc.getSize()-1;
                 }
                 doc.setSelectedPage(doc.get(index));
                 doc.getSelection().clear();
             }
         });
     }
     private AminoAction PrevPageAction(final SketchDocument doc) {
         return named("Previous Page", new AminoAction() {
             @Override
             public void execute() throws Exception {
                 Page page = doc.getSelectedPage();
                 int index = doc.indexOf(page);
                 index--;
                 if(index < 0) {
                     index = 0;
                 }
                 doc.setSelectedPage(doc.get(index));
                 doc.getSelection().clear();
             }
         });
     }
 
     private AminoAction EditDocumentSettingsAction(final SketchDocument doc) {
         return named("Edit Document Settings", new AminoAction() {
             @Override
             public void execute() throws Exception {
                 final Stage stage = Stage.createStage();
 
                 Doc xml = XMLParser.parse(new File("resources/docsettings.xml"));
                 Control root = (Control) AminoParser.parsePage(xml.root());
 
 
                 final PopupMenuButton<Size> popup = (PopupMenuButton) AminoParser.find("sizeBox", root);
                 Size[] sizes =  new Size[]{ new Size(320,480,Units.Pixels), new Size(1024,768,Units.Pixels)};
                 popup.setModel(ListView.createModel(sizes));
                 popup.setTextRenderer(new ListView.TextRenderer<Size>() {
                     public String toString(SelectableControl selectableControl, Size size, int i) {
                         return size.getWidth(Units.Pixels) + " x " + size.getHeight(Units.Pixels) + " px";
                     }
                 });
 
                 Button cancelButton = (Button) AminoParser.find("cancelButton", root);
                 cancelButton.onClicked(new Callback<ActionEvent>() {
                     public void call(ActionEvent actionEvent) throws Exception {
                         stage.hide();
                     }
                 });
 
                 Button doneButton = (Button) AminoParser.find("doneButton", root);
                 doneButton.onClicked(new Callback<ActionEvent>() {
                     public void call(ActionEvent actionEvent) throws Exception {
                         stage.hide();
                         doc.setMasterSize(popup.getSelectedItem());
                     }
                 });
 
 
                 double w = root.getPrefWidth();
                 double h = root.getPrefHeight();
                 stage.setWidth(w);
                 stage.setHeight(h+20);
                 stage.setContent(root);
 
             }
         });
     }
 
 
     private AminoAction DistributeNodes(final SketchDocument doc) {
         return named("Distribute", groupOnly(doc, new AminoAction() {
             @Override
             public void execute() throws Exception {
                 if (doc.getSelection().getSize() < 2) return;
 
                 List<SketchNode> nodes = new ArrayList<SketchNode>();
                 doXParts(nodes);
                 doYParts(nodes);
 
             }
 
             private void doYParts(List<SketchNode> nodes) {
                 double miny = Double.POSITIVE_INFINITY;
                 double maxy = Double.NEGATIVE_INFINITY;
                 double totalHeight = 0;
                 for (SketchNode node : doc.getSelection().children()) {
                     {
                         double y = node.getTranslateY() + node.getInputBounds().getY();
                         if (y < miny) miny = y;
                         double y2 = node.getTranslateY() + node.getInputBounds().getY2();
                         if (y2 > maxy) maxy = y2;
                         totalHeight += node.getInputBounds().getHeight();
                     }
                 }
 
                 Collections.sort(nodes, new Comparator<SketchNode>() {
                     public int compare(SketchNode o1, SketchNode o2) {
                         if ((o1.getInputBounds().getY() + o1.getTranslateY()) < (o2.getInputBounds().getY() + o2.getTranslateY())) {
                             return -1;
                         }
                         return 1;
                     }
                 });
 
                 double extraY = maxy - miny - totalHeight;
                 double extraPerY = extraY / (doc.getSelection().getSize() - 1);
                 double y = miny;
 
                 for (SketchNode node : doc.getSelection().children()) {
                     node.setTranslateY(y);
                     y += node.getInputBounds().getHeight();
                     y += extraPerY;
                 }
             }
 
             private void doXParts(List<SketchNode> nodes) {
                 double minx = Double.POSITIVE_INFINITY;
                 double maxx = Double.NEGATIVE_INFINITY;
                 double totalWidth = 0;
                 for (SketchNode node : doc.getSelection().children()) {
                     {
                         nodes.add(node);
                         double x = node.getTranslateX() + node.getInputBounds().getX();
                         if (x < minx) minx = x;
                         double x2 = node.getTranslateX() + node.getInputBounds().getX2();
                         if (x2 > maxx) maxx = x2;
                         totalWidth += node.getInputBounds().getWidth();
                     }
                 }
 
                 Collections.sort(nodes, new Comparator<SketchNode>() {
                     public int compare(SketchNode o1, SketchNode o2) {
                         if ((o1.getInputBounds().getX() + o1.getTranslateX()) < (o2.getInputBounds().getX() + o2.getTranslateX())) {
                             return -1;
                         }
                         return 1;
                     }
                 });
 
                 double extraX = maxx - minx - totalWidth;
                 double extraPerX = extraX / (doc.getSelection().getSize() - 1);
                 double x = minx;
                 for (SketchNode node : doc.getSelection().children()) {
                     node.setTranslateX(x);
                     x += node.getInputBounds().getWidth();
                     x += extraPerX;
                 }
             }
         }));
     }
 }
