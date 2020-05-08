 package com.joshondesign.treegui.modes.aminojava;
 
 import com.joshondesign.treegui.BindingUtils;
 import com.joshondesign.treegui.Mode;
 import com.joshondesign.treegui.actions.JAction;
 import com.joshondesign.treegui.docmodel.*;
 import com.joshondesign.treegui.model.TreeNode;
 import com.joshondesign.treegui.modes.amino.ActionProp;
 import com.joshondesign.treegui.modes.amino.Rect;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.joshy.gfx.draw.FlatColor;
 import org.joshy.gfx.draw.Font;
 import org.joshy.gfx.draw.GFX;
 import org.joshy.gfx.event.AminoAction;
 import org.joshy.gfx.node.control.ListModel;
 import org.joshy.gfx.node.control.ListView;
 import org.joshy.gfx.node.control.Menu;
 import org.joshy.gfx.node.control.ScrollPane;
 
 public class AminoJavaMode extends Mode {
     public static Map<String, DynamicNode.DrawDelegate> drawMap = new HashMap<String, DynamicNode.DrawDelegate>();
 
     public AminoJavaMode() {
         setId("com.joshondesign.modes.aminojava");
 
         TreeNode<JAction> javaactions = new TreeNode<JAction>();
         add(javaactions);
 
         TreeNode<SketchNode> symbols = new TreeNode<SketchNode>();
         add(symbols);
 
         DynamicNode visualBase = new DynamicNode();
         visualBase.addProperty(new Property("translateX", Double.class, 0));
         visualBase.addProperty(new Property("translateY", Double.class, 0));
         visualBase.addProperty(new Property("width", Double.class, 80)
                 .setExportName("prefWidth"));
         visualBase.addProperty(new Property("height", Double.class, 30)
                 .setExportName("prefHeight"))
                 .addProperty(new Property("anchorLeft", Boolean.class, true).setBindable(false))
                 .addProperty(new Property("anchorRight", Boolean.class, false).setBindable(false))
                 .addProperty(new Property("anchorTop", Boolean.class, true).setBindable(false))
                 .addProperty(new Property("anchorBottom", Boolean.class, false).setBindable(false))
                 ;
 
 
         DynamicNode pushButton = new DynamicNode()
                 .setName("Button");
         pushButton.setResizable(true);
         pushButton.setVisual(true);
 
         pushButton.copyPropertiesFrom(visualBase);
         pushButton.addProperty(new Property("class", String.class, "org.joshy.gfx.node.control.Button"));
         pushButton.addProperty(new Property("text", CharSequence.class, "pushbutton").setBindable(true));
         pushButton.addProperty(new Property("resize", String.class, "any")
                 .setExported(false)
                 .setVisible(false));
         pushButton.addProperty(new Property("trigger", GuiTest.TriggerType.class, 0)
                 .setExported(false)
                 .setVisible(false)
                 .setBindable(true)
         );
 
         drawMap.put(pushButton.getName(), new DynamicNode.DrawDelegate() {
             public void draw(GFX g, DynamicNode node) {
                 double w = node.getWidth();
                 double h = node.getHeight();
                 String t = node.getProperty("text").getStringValue();
                 g.setPaint(FlatColor.GRAY);
                 g.fillRect(0, 0, w, h);
                 g.setPaint(FlatColor.BLACK);
                 g.drawText(t, Font.DEFAULT, 5, 15);
                 g.drawRect(0, 0, w, h);
             }
         });
         pushButton.setDrawDelegate(drawMap.get(pushButton.getName()));
         symbols.add(pushButton);
 
         DynamicNode toggleButton = new DynamicNode();
         toggleButton.setName("Togglebutton");
         toggleButton.setResizable(true);
         toggleButton.setVisual(true);
 
         toggleButton.copyPropertiesFrom(visualBase);
         toggleButton.addProperty(new Property("class", String.class, "org.joshy.gfx.node.control.Togglebutton"));
         toggleButton.addProperty(new Property("text", CharSequence.class, "togglebutton").setBindable(true));
         toggleButton.addProperty(new Property("selected", Boolean.class, false));
         toggleButton.addProperty(new Property("resize", String.class, "any")
                 .setExported(false)
                 .setVisible(false));
         toggleButton.addProperty(new Property("trigger", GuiTest.TriggerType.class, 0)
                 .setExported(false)
                 .setVisible(false)
                 .setBindable(true)
         );
 
         drawMap.put(toggleButton.getName(), new DynamicNode.DrawDelegate() {
             public void draw(GFX g, DynamicNode node) {
                 double w = node.getProperty("width").getDoubleValue();
                 double h = node.getProperty("height").getDoubleValue();
                 String t = node.getProperty("text").getStringValue();
                 boolean b = node.getProperty("selected").getBooleanValue();
 
                 g.setPaint(FlatColor.GRAY);
                 if (b) {
                     g.setPaint(FlatColor.BLUE);
                 }
                 g.fillRect(0, 0, w, h);
                 g.setPaint(FlatColor.BLACK);
                 g.drawText(t, Font.DEFAULT, 5, 15);
                 g.drawRect(0, 0, w, h);
             }
         });
         toggleButton.setDrawDelegate(drawMap.get(toggleButton.getName()));
         symbols.add(toggleButton);
 
 
         DynamicNode label = new DynamicNode();
         label.copyPropertiesFrom(visualBase);
         label.setName("Label").setVisual(true).setResizable(true);
         label.copyPropertiesFrom(visualBase);
         label.addProperty(new Property("class", String.class, "org.joshy.gfx.node.control.Label"));
         label.addProperty(new Property("resize", String.class, "width").setExported(false));
         label.addProperty(new Property("text", CharSequence.class, "a label").setBindable(true));
         drawMap.put(label.getName(), new DynamicNode.DrawDelegate() {
             public void draw(GFX g, DynamicNode node) {
                 String t = node.getProperty("text").getStringValue();
                 g.setPaint(FlatColor.BLACK);
                 g.drawText(t, Font.DEFAULT, 5, 15);
             }
         });
         label.setDrawDelegate(drawMap.get(label.getName()));
         symbols.add(label);
 
         drawMap.put("Checkbox", new DynamicNode.DrawDelegate() {
             public void draw(GFX g, DynamicNode node) {
                double w = node.getWidth();
                 double h = node.getHeight();
                 String t = node.getProperty("text").getStringValue();
 
                 g.setPaint(FlatColor.GRAY);
                 g.fillRect(0, 0, h , h);
                 g.setPaint(FlatColor.BLACK);
                 g.drawText(t, Font.DEFAULT, 5 + h, 15);
                 g.drawRect(0,0, h, h);
             }
         });
         DynamicNode checkbox = BindingUtils.parseAnnotatedPOJO(new CheckboxWrapper(), drawMap.get("Checkbox"));
        checkbox.setName("Checkbox").setResizable(true).setWidth(80).setHeight(20);
         checkbox.copyPropertiesFrom(visualBase);
         symbols.add(checkbox);
 
         drawMap.put("Radiobutton", new DynamicNode.DrawDelegate() {
             public void draw(GFX g, DynamicNode node) {
                double w = node.getWidth();
                 double h = node.getHeight();
                 String t = node.getProperty("text").getStringValue();
 
                 g.setPaint(FlatColor.GRAY);
                 g.fillOval(0, 0, h, h);
                 g.setPaint(FlatColor.BLACK);
                 g.drawOval(0, 0, h, h);
                 g.drawText(t, Font.DEFAULT, 5 + h, 15);
             }
         });
         DynamicNode radiobutton = BindingUtils.parseAnnotatedPOJO(new RadiobuttonWrapper(), drawMap.get("Radiobutton"));
         radiobutton.copyPropertiesFrom(visualBase);
         radiobutton.setName("Radiobutton").setResizable(true).setWidth(80).setHeight(20);
         symbols.add(radiobutton);
 
 
         drawMap.put("Popupbutton", new DynamicNode.DrawDelegate() {
             public void draw(GFX g, DynamicNode node) {
                 double w = node.getWidth();
                 double h = node.getHeight();
                 Property pmodel  = node.getProperty("model");
                 ListModel model = (ListModel) pmodel.getRawValue();
                 g.setPaint(FlatColor.GRAY);
                 g.fillRect(0, 0, w, h);
                 g.setPaint(FlatColor.BLACK);
                 if (model != null && model.size() >= 1) {
                     String t = model.get(0).toString();
                     g.drawText(t, Font.DEFAULT, 5, 15);
                 }
                 g.drawRect(0, 0, w, h);
             }
         });
         DynamicNode popupbutton = BindingUtils.parseAnnotatedPOJO(new PopupMenuButtonWrapper(), drawMap.get("Popupbutton"));
         popupbutton.copyPropertiesFrom(visualBase);
         popupbutton.addProperty(new Property("model", ListModel.class, ListView.createModel(new String[]{"Ethernet","WiFi","Bluetooth","FireWire","USB hack"}))
                .setBindable(true).setExported(false).setVisible(true).setList(true));
 
         popupbutton.setName("Popupbutton").setResizable(true).setWidth(80).setHeight(20);
         symbols.add(popupbutton);
 
         DynamicNode panel = new DynamicNode();
         panel.copyPropertiesFrom(visualBase);
         panel.setContainer(true).setName("Panel").setVisual(true).setResizable(true);
         panel.addProperty(new Property("class", String.class, "com.joshondesign.treegui.AnchorPanel"))
                 .addProperty(new Property("resize", String.class, "any")
                         .setExported(false).setVisible(false))
                 .addProperty(new Property("fill", FlatColor.class, FlatColor.PURPLE))
         ;
         drawMap.put(panel.getName(), new DynamicNode.DrawDelegate() {
             public void draw(GFX g, DynamicNode node) {
                 double w = node.getProperty("width").getDoubleValue();
                 double h = node.getProperty("height").getDoubleValue();
                 g.setPaint(FlatColor.GRAY);
                 g.fillRect(0, 0, w, h);
                 g.setPaint(FlatColor.BLACK);
                 g.drawRect(0, 0, w, h);
             }
         });
         panel.setDrawDelegate(drawMap.get(panel.getName()));
 
 
         DynamicNode listview = new DynamicNode();
         listview.copyPropertiesFrom(visualBase);
         listview.setName("ListView").setVisual(true).setResizable(true);
         listview.addProperty(new Property("class", String.class, "org.joshy.gfx.node.control.ListView"))
                 .addProperty(new Property("resize", String.class, "any")
                         .setExported(false).setVisible(false))
                 .addProperty(new Property("rowHeight", Double.class, 20))
                 .addProperty(new Property("columnWidth", Double.class, 100))
                 .addProperty(new Property("renderer", ListView.ItemRenderer.class, "none"))
                 .addProperty(new Property("model", ListModel.class, null)
                         .setBindable(true).setExported(false).setVisible(false))
                 .addProperty(new Property("orientation",
                         ListView.Orientation.class, ListView.Orientation.Vertical))
         ;
         drawMap.put(listview.getName(), new DynamicNode.DrawDelegate() {
             public void draw(GFX g, DynamicNode node) {
                 double w = node.getProperty("width").getDoubleValue();
                 double h = node.getProperty("height").getDoubleValue();
                 g.setPaint(FlatColor.GRAY);
                 g.fillRect(0, 0, w, h);
                 g.setPaint(FlatColor.BLACK);
                 g.drawRect(0, 0, w, h);
                 //lines
                 for (int i = 0; i < 5; i++) {
                     g.drawRect(0, i * 15, w, 15);
                 }
             }
         });
         listview.setDrawDelegate(drawMap.get(listview.getName()));
         listview.getProperty("width").setDoubleValue(60);
         listview.getProperty("height").setDoubleValue(90);
 
 
         DynamicNode scroll = new DynamicNode();
         scroll.copyPropertiesFrom(visualBase);
         scroll.setContainer(true).setName("ScrollPane").setVisual(true).setResizable(true);
         scroll.addProperty(new Property("class", String.class,
                 "org.joshy.gfx.node.control.ScrollPane"))
                 .addProperty(new Property("resize", String.class, "any")
                         .setExported(false).setVisible(false))
                 .addProperty(new Property("horizontalVisiblePolicy",
                         ScrollPane.VisiblePolicy.class,
                         ScrollPane.VisiblePolicy.WhenNeeded
                 ).setDisplayName("Horiz Scroll")
                 )
                 .addProperty(new Property("verticalVisiblePolicy",
                         ScrollPane.VisiblePolicy.class,
                         ScrollPane.VisiblePolicy.WhenNeeded
                 ).setDisplayName("Vert Scroll")
                 )
         ;
         drawMap.put(scroll.getName(), new DynamicNode.DrawDelegate() {
             public void draw(GFX g, DynamicNode node) {
                 double w = node.getProperty("width").getDoubleValue();
                 double h = node.getProperty("height").getDoubleValue();
                 g.setPaint(FlatColor.GRAY);
                 g.fillRect(0, 0, w, h);
                 g.setPaint(FlatColor.BLACK);
                 g.drawRect(0, 0, w, h);
                 g.drawRect(0, h - 10, w, 10);
                 g.drawRect(w - 10, 0, 10, h);
             }
         });
         scroll.setDrawDelegate(drawMap.get(scroll.getName()));
         scroll.getProperty("width").setDoubleValue(60);
         scroll.getProperty("height").setDoubleValue(90);
 
 
         DynamicNode spinner = new DynamicNode();
         spinner.copyPropertiesFrom(visualBase);
         spinner.setName("Spinner").setVisual(true).setResizable(true);
         spinner
                 .addProperty(new Property("class", String.class,
                         "com.joshondesign.treegui.modes.aminojava.Spinner"))
                 .addProperty(new Property("resize", String.class, "any")
                         .setExported(false).setVisible(false))
                 .addProperty(new Property("active", Boolean.class, Boolean.TRUE)
                         .setBindable(true))
         ;
         drawMap.put(spinner.getName(), new DynamicNode.DrawDelegate() {
             public void draw(GFX g, DynamicNode node) {
                 double w = node.getProperty("width").getDoubleValue();
                 double h = node.getProperty("height").getDoubleValue();
                 g.setPaint(FlatColor.GRAY);
                 g.fillOval(0, 0, w, h);
                 g.setPaint(FlatColor.BLACK);
                 g.drawOval(0, 0, w, h);
             }
         });
         spinner.setDrawDelegate(drawMap.get(spinner.getName()));
         spinner.getProperty("width").setDoubleValue(50);
         spinner.getProperty("height").setDoubleValue(50);
 
 
         DynamicNode custom = new DynamicNode();
         custom.setName("Custom View");
         custom.copyPropertiesFrom(visualBase);
         custom.setCustom(true).setContainer(true).setVisual(true).setResizable(true);
         custom
                 .addProperty(new Property("class", String.class, "org.joshy.gfx.node.control.ScrollPane"))
                 .addProperty(new Property("customClass", String.class, "none"))
                 .addProperty(new Property("resize", String.class, "any")
                         .setExported(false).setVisible(false))
         ;
         drawMap.put(custom.getName(), new DynamicNode.DrawDelegate() {
             public void draw(GFX g, DynamicNode node) {
                 double w = node.getProperty("width").getDoubleValue();
                 double h = node.getProperty("height").getDoubleValue();
                 g.setPaint(FlatColor.GRAY);
                 g.fillRect(0, 0, w, h);
                 g.setPaint(FlatColor.BLACK);
                 g.drawRect(0, 0, w, h);
                 g.drawLine(0, 0, w, h);
                 g.drawLine(0, h, w, 0);
             }
         });
         custom.setDrawDelegate(drawMap.get(custom.getName()));
         custom.getProperty("width").setDoubleValue(90);
         custom.getProperty("height").setDoubleValue(90);
 
         DynamicNode textbox = new DynamicNode();
         textbox.setName("Textbox");
         textbox.copyPropertiesFrom(visualBase);
         textbox.setVisual(true);
         textbox.setResizable(true);
         textbox.setContainer(false);
         textbox
                 .addProperty(new Property("class", String.class,
                         "org.joshy.gfx.node.control.Textbox"))
                 .addProperty(new Property("hintText", CharSequence.class, "hint").setBindable(true))
                 .addProperty(new Property("text", String.class, "a textfield").setBindable(true))
                 .addProperty(new Property("resize", String.class, "any")
                         .setExported(false).setVisible(false))
         ;
         drawMap.put(textbox.getName(), new DynamicNode.DrawDelegate() {
             public void draw(GFX g, DynamicNode node) {
                 double w = node.getProperty("width").getDoubleValue();
                 double h = node.getProperty("height").getDoubleValue();
                 String t = node.getProperty("text").getStringValue();
                 g.setPaint(FlatColor.GRAY);
                 g.fillRect(0, 0, w, h);
                 g.setPaint(FlatColor.BLACK);
                 g.drawRect(0, 0, w, h);
                 g.drawText(t, Font.DEFAULT, 5, 15);
             }
         });
         textbox.setDrawDelegate(drawMap.get(textbox.getName()));
         textbox.getProperty("width").setDoubleValue(80);
         textbox.getProperty("height").setDoubleValue(30);
 
         symbols.add(textbox);
         symbols.add(spinner);
         symbols.add(listview);
         symbols.add(panel);
         symbols.add(scroll);
         symbols.add(custom);
 
         DynamicNode serviceBase = new DynamicNode();
         serviceBase.addProperty(new Property("translateX", Double.class, 0).setExported(false))
                 .addProperty(new Property("translateY", Double.class, 0).setExported(false))
                 .addProperty(new Property("width", Double.class, 90).setBindable(false).setExported(false))
                 .addProperty(new Property("height", Double.class, 50).setBindable(false).setExported(false))
         ;
         drawMap.put("servicebase", new DynamicNode.DrawDelegate() {
             public void draw(GFX g, DynamicNode node) {
                 double w = 90;
                 double h = 50;
                 g.setPaint(FlatColor.YELLOW);
                 g.fillRoundRect(0, 0, w, h, 10, 10);
                 g.setPaint(FlatColor.BLACK);
                 g.drawRoundRect(0, 0, w, h, 10, 10);
                 g.drawText(node.getName(), Font.DEFAULT, 5, 15);
             }
         });
 
         DynamicNode flickrQuery = new DynamicNode();
         flickrQuery.setName("Flickr Search");
         flickrQuery.setVisual(false);
         flickrQuery.setResizable(false);
         flickrQuery.copyPropertiesFrom(serviceBase);
         flickrQuery
                 .addProperty(new Property("class", String.class,
                         "com.joshondesign.treegui.modes.aminojava.FlickrQuery"))
                 .addProperty(new Property("execute", ActionProp.class, null)
                         .setBindable(true).setVisible(false))
                 .addProperty(new Property("query", String.class, "london")
                         .setBindable(true).setVisible(true))
                 .addProperty(new Property("results", ListModel.class, null)
                         .setBindable(true).setVisible(false))
                 .addProperty(new Property("active", Boolean.class, Boolean.TRUE)
                         .setBindable(true))
         ;
         flickrQuery.setDrawDelegate(drawMap.get("servicebase"));
         symbols.add(flickrQuery);
 
         DynamicNode action = new DynamicNode();
         action.setName("action");
         action.setVisual(false);
         action.setResizable(false);
         action.copyPropertiesFrom(serviceBase);
         action.addProperty(new Property("class", String.class, "com.joshondesign.flickr.FlickrQuery"))
                 .addProperty(new Property("execute", ActionProp.class, null).setBindable(true))
         ;
         action.setDrawDelegate(drawMap.get("servicebase"));
         symbols.add(action);
 
         DynamicNode document = new DynamicNode();
         document.setName("Document");
         document.setVisual(false);
         document.setResizable(false);
         document.copyPropertiesFrom(serviceBase);
         document
                 .addProperty(new Property("class", String.class, "com.joshondesign.flickr.FlickrQuery"))
                 .addProperty(new Property("pages", String.class, null).setBindable(true))
                 .addProperty(new Property("currentPage", Page.class, null).setBindable(true))
                 .addProperty(new Property("selection", List.class, null).setBindable(true))
         ;
         document.setDrawDelegate(drawMap.get("servicebase"));
         symbols.add(document);
 
         DynamicNode stringList = new DynamicNode();
         stringList.setName("String List");
         stringList.setVisual(false);
         stringList.setResizable(false);
         stringList.copyPropertiesFrom(serviceBase);
         List<String> dummyStringListData = new ArrayList<String>();
         dummyStringListData.add("foo");
         dummyStringListData.add("bar");
         dummyStringListData.add("baz");
         stringList
                 .addProperty(new Property("class", String.class,
                         "com.joshondesign.flickr.FlickrQuery"))
                 .addProperty(new Property("data", List.class, dummyStringListData)
                         .setVisible(true).setBindable(true))
                 .addProperty(new Property("this", DynamicNode.class, null)
                         .setVisible(false).setBindable(true))
         ;
         stringList.setDrawDelegate(drawMap.get("servicebase"));
         symbols.add(stringList);
 
 
         DynamicNode alarmList = new DynamicNode();
         alarmList.setName("Alarm List");
         alarmList.setVisual(false);
         alarmList.setResizable(false);
         alarmList.copyPropertiesFrom(serviceBase);
         List<String> data2 = new ArrayList<String>();
         data2.add("foo");
         data2.add("bar");
         data2.add("baz");
         DynamicNode.DrawDelegate servicebaseDrawDelegate = drawMap.get("servicebase");
 
         Property dataProp = new Property("data", ListModel.class, data2);
         dataProp.setVisible(false);
         dataProp.setBindable(true);
         dataProp.setList(true);
         DynamicNode alarm = new DynamicNode();
         alarm.setName("Alarm");
         alarm.setVisual(false);
         alarm.setResizable(false);
         alarm.copyPropertiesFrom(serviceBase);
         alarm.addProperty(new Property("label", String.class, "Alarm Label").setBindable(true));
         alarm.addProperty(new Property("time", Double.class, 5).setBindable(true));
         alarm.addProperty(new Property("enabled", Boolean.class, false).setBindable(true));
         alarm.setDrawDelegate(servicebaseDrawDelegate);
         dataProp.setItemPrototype(alarm);
         alarmList.addProperty(new Property("class", String.class,
                 "com.joshondesign.treegui.modes.aminojava.AlarmList"))
                 .addProperty(dataProp)
         ;
         alarmList.setDrawDelegate(servicebaseDrawDelegate);
         symbols.add(alarmList);
 
         DynamicNode compoundList = new DynamicNode();
         compoundList.setName("Compound List");
         compoundList.copyPropertiesFrom(visualBase);
         compoundList.setVisual(true);
         compoundList.setResizable(true);
         compoundList.setContainer(true);
         compoundList
                 .addProperty(new Property("class", String.class,
                         "org.joshy.gfx.node.control.CompoundListView"))
                 .addProperty(new Property("resize", String.class, "any")
                         .setExported(false).setVisible(false))
                 .addProperty(new Property("rowHeight", Double.class, 30))
                 .addProperty(new Property("model", ListModel.class, null)
                         .setBindable(true).setExported(false).setVisible(false).setList(true))
         ;
         compoundList.setDrawDelegate(drawMap.get(listview.getName()));
         compoundList.getProperty("width").setDoubleValue(80);
         compoundList.getProperty("height").setDoubleValue(80);
         DynamicNode template = (DynamicNode) panel.duplicate(null);
         template.setPositionLocked(true);
         DynamicNode mirror = new DynamicNode();
         mirror.setMirror(true);
         mirror.setResizable(false);
         mirror.setName("Mirror");
         mirror.setMirrorTarget("model");
         mirror.copyPropertiesFrom(serviceBase);
         mirror.setDrawDelegate(servicebaseDrawDelegate);
         mirror.getProperty("translateX").setDoubleValue(-100);
         //new Property().set
         template.add(mirror);
         //when entering the template, it checks if it has a mirror child. if so it asks the mirror to rebuild itself?
         compoundList.add(template);
         symbols.add(compoundList);
 
 
         doGenerated(symbols, servicebaseDrawDelegate);
 
     }
 
     private void doGenerated(TreeNode<SketchNode> symbols, DynamicNode.DrawDelegate base) {
         FlickrSearch obj = new FlickrSearch();
         DynamicNode node = BindingUtils.parseAnnotatedPOJO(obj, base);
         symbols.add(node);
     }
 
     public static class CheckboxWrapper {
         @Prop public CharSequence text = "checkbox";
         @Prop public Boolean selected = false;
         @Prop(exported = false, visible = false) public String resize = "horizontal";
         @Prop(visible = false) public String clazz = org.joshy.gfx.node.control.Checkbox.class.getName();
     }
 
     public static class RadiobuttonWrapper {
         @Prop public CharSequence text = "radiobutton";
         @Prop public Boolean selected = false;
         @Prop(exported = false, visible = false) public String resize = "horizontal";
         @Prop(visible = false) public String clazz = org.joshy.gfx.node.control.Radiobutton.class.getName();
 
     }
     public static class PopupMenuButtonWrapper {
         @Prop public Boolean selected = false;
         @Prop public Integer selectedIndex = 0;
         @Prop(exported = false, visible = false) public String resize = "horizontal";
         @Prop(visible = false) public String clazz = org.joshy.gfx.node.control.PopupMenuButton.class.getName();
     }
 
     public static class FlickrSearch {
 
         @Prop public String query = "london";
 
         @Prop(visible = false)
         public ListModel<String> results = new StringListModel();
 
         @Prop(visible = false)
         public Boolean active = false;
 
         @Prop(visible = false)
         public ActionProp execute;
 
         @Prop(bindable = false, visible = false)
         public String clazz = "com.joshondesign.treegui.modes.aminojava.FlickrQuery";
     }
 
 
     @Override
     public String getName() {
         return "Amino Java";
     }
 
     @Override
     public SketchDocument createEmptyDoc() {
         SketchDocument doc = new SketchDocument();
         Layer layer = new Layer();
         layer.add(new Rect().setFill(FlatColor.GREEN).setWidth(50).setHeight(50));
         Page page = new Page();
         page.add(layer);
         doc.add(page);
         return doc;
     }
 
     @Override
     public void modifyNodeMenu(Menu nodeMenu, final SketchDocument doc) {
         super.modifyNodeMenu(nodeMenu, doc);
         nodeMenu.addItem("Align Left", AlignLeft(doc));
         nodeMenu.addItem("Align Center Horizontal", AlignCenterH(doc));
         nodeMenu.addItem("Align Right", AlignRight(doc));
         nodeMenu.addItem("Align Top", AlignTop(doc));
         nodeMenu.addItem("Align Center Vertical", AlignCenterV(doc));
         nodeMenu.addItem("Align Bottom", AlignBottom(doc));
 
         nodeMenu.addItem("Lower Node To Bottom", "shift CLOSE_BRACKET", LowerNodeToBottom(doc));
         nodeMenu.addItem("Lower Node", "CLOSE_BRACKET", LowerNode(doc));
         nodeMenu.addItem("Raise Node", "OPEN_BRACKET", RaiseNode(doc));
         nodeMenu.addItem("Raise Node To Top", "shift OPEN_BRACKET", RaiseNodeToTop(doc));
 
         nodeMenu.addItem("Same Width", SameWidth(doc));
         nodeMenu.addItem("Same Height", SameHeight(doc));
     }
 
     @Override
     public void modifyDocumentMenu(Menu documentMenu, SketchDocument doc) {
         super.modifyDocumentMenu(documentMenu, doc);
         documentMenu.addItem("Add Page", AddPageAction(doc));
         documentMenu.addItem("Previous Page", "LEFT", PrevPageAction(doc));
         documentMenu.addItem("Next Page", "RIGHT", NextPageAction(doc));
     }
 
     @Override
     public void modifyFileMenu(Menu fileMenu, SketchDocument doc) {
         fileMenu.addItem("Run", "R",  new AminoJavaXMLExport.Test(doc));
     }
 
     @Override
     public void modifyViewMenu(Menu viewMenu, SketchDocument doc) {
         super.modifyViewMenu(viewMenu, doc);
         viewMenu.addItem("Snap to Grid", SnapToGridAction(doc));
     }
 
     private AminoAction SnapToGridAction(final SketchDocument doc) {
         return new AminoAction() {
             @Override
             public void execute() throws Exception {
                 doc.setSnapToGrid(!doc.isSnapToGrid());
             }
         };
     }
 
     @Override
     public List<AminoAction> getContextMenuActions(final SketchDocument doc, Selection selection) {
         List<AminoAction> list = super.getContextMenuActions(doc, selection);
         list.add(AlignLeft(doc));
         list.add(AlignRight(doc));
         list.add(AlignTop(doc));
         list.add(AlignBottom(doc));
         return list;
     }
 
 
     private static AminoAction groupOnly(final SketchDocument doc, final AminoAction aminoAction) {
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
     private static AminoAction named(final String name, final AminoAction action) {
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
 
     private Double apply(Selection selection, Double minX, Accumulate<Double> acc) {
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
     private AminoAction AlignLeft(final SketchDocument doc) {
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
     private AminoAction AlignCenterH(final SketchDocument doc) {
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
     private AminoAction AlignRight(final SketchDocument doc) {
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
     private AminoAction AlignBottom(final SketchDocument doc) {
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
     private AminoAction AlignCenterV(final SketchDocument doc) {
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
     private AminoAction AlignTop(final SketchDocument doc) {
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
     private AminoAction LowerNode(final SketchDocument doc) {
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
     private AminoAction LowerNodeToBottom(final SketchDocument doc) {
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
     private AminoAction RaiseNode(final SketchDocument doc) {
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
     private AminoAction RaiseNodeToTop(final SketchDocument doc) {
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
                         if(!nd.isResizable()) return value;
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
                         if(!nd.isResizable()) return value;
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
 
 }
 
 
 
 
 interface Accumulate<T> {
     public T accum(SketchNode node, T value);
 }
