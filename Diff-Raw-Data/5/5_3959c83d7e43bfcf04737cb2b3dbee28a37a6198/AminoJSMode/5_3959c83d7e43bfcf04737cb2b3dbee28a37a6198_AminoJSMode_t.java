 package com.joshondesign.treegui.modes.aminojs;
 
 import com.joshondesign.treegui.BindingUtils;
 import com.joshondesign.treegui.Mode;
 import com.joshondesign.treegui.actions.JAction;
 import com.joshondesign.treegui.docmodel.Layer;
 import com.joshondesign.treegui.docmodel.Page;
 import com.joshondesign.treegui.docmodel.SketchDocument;
 import com.joshondesign.treegui.docmodel.SketchNode;
 import com.joshondesign.treegui.model.TreeNode;
 import com.joshondesign.treegui.modes.aminojava.DynamicNode;
 import com.joshondesign.treegui.modes.aminojava.Property;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.joshy.gfx.draw.FlatColor;
 import org.joshy.gfx.draw.Font;
 import org.joshy.gfx.draw.FontBuilder;
 import org.joshy.gfx.draw.GFX;
 import org.joshy.gfx.node.control.Menu;
 
 public class AminoJSMode extends Mode {
     public static Map<String, DynamicNode.DrawDelegate> drawMap = new HashMap<String, DynamicNode.DrawDelegate>();
 
     public AminoJSMode() {
         setId("com.joshondesign.modes.aminojs");
         add(new TreeNode<JAction>());
 
         TreeNode<SketchNode> symbols = new TreeNode<SketchNode>();
         symbols.setId("symbols");
 
         DynamicNode visualBase = new DynamicNode();
         visualBase
                 .addProperty(new Property("translateX", Double.class, 0))
                 .addProperty(new Property("translateY", Double.class, 0))
                 .addProperty(new Property("width", Double.class, 80))
                 .addProperty(new Property("height", Double.class, 30))
         ;
 
         drawMap.put("PushButton", new DynamicNode.DrawDelegate() {
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
 
         drawMap.put("CheckButton", new DynamicNode.DrawDelegate() {
             public void draw(GFX g, DynamicNode node) {
                 double w = node.getWidth();
                 double h = node.getHeight();
                 String t = node.getProperty("text").getStringValue();
                 g.setPaint(FlatColor.GRAY);
                 g.fillRect(0, 0, h, h);
                 g.setPaint(FlatColor.BLACK);
                 g.drawText(t, Font.DEFAULT, 5 + h, 15);
                 g.drawRect(0,0,h,h);
             }
         });
 
         symbols.add(parse(new PushButton(), drawMap.get("PushButton"), visualBase));
         symbols.add(parse(new ToggleButton(), drawMap.get("PushButton"),visualBase));
         symbols.add(parse(new CheckButton(), drawMap.get("CheckButton"),visualBase));
 
 
         drawMap.put("Slider", new DynamicNode.DrawDelegate() {
             public void draw(GFX g, DynamicNode node) {
                 double w = node.getWidth();
                 double h = node.getHeight();
                 g.setPaint(FlatColor.GRAY);
                 g.fillRect(0, 0, w, h);
                 g.setPaint(FlatColor.BLACK);
                 g.fillRect(0, 0, h, h);
             }
         });
         drawMap.put("Image", new DynamicNode.DrawDelegate() {
             public void draw(GFX g, DynamicNode node) {
                 double w = node.getWidth();
                 double h = node.getHeight();
                 g.setPaint(FlatColor.GRAY);
                 g.fillRect(0, 0, w, h);
                 g.setPaint(FlatColor.BLACK);
                 g.drawRect(0 + 10, 0 + 10, w - 10 * 2, h - 10 * 2);
             }
         });
        drawMap.put("Textbox", new DynamicNode.DrawDelegate() {
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
         drawMap.put("Label", new DynamicNode.DrawDelegate() {
             public void draw(GFX g, DynamicNode node) {
                 double w = node.getWidth();
                 double h = node.getHeight();
                 String text = node.getProperty("text").getStringValue();
                 double size = node.getProperty("fontsize").getDoubleValue();
                 g.setPaint(FlatColor.BLACK);
                 Font font = new FontBuilder(Font.DEFAULT.getName()).size((float)size).resolve();
                 g.drawText(text, font, 5, h-5);
             }
         });
         drawMap.put("ListView", new DynamicNode.DrawDelegate() {
             public void draw(GFX g, DynamicNode node) {
                 double w = node.getWidth();
                 double h = node.getHeight();
                 g.setPaint(FlatColor.GRAY);
                 g.fillRect(0, 0, w, h);
                 g.setPaint(FlatColor.BLACK);
 
                 List<String> data = (List<String>) node.getProperty("data").getRawValue();
                 if(data == null) {
                     data = Arrays.asList("dummy", "dummy", "dummy");
                 }
                 if(data != null) {
                     for(int i=0; i<data.size(); i++) {
                         g.drawText(data.get(i), Font.DEFAULT, 5, i*20+20);
                     }
                 }
 
                 g.drawRect(0, 0, w, h);
             }
         });
 
         symbols.add(parse(new Slider(), drawMap.get("Slider"), visualBase));
         symbols.add(parse(new Image(), drawMap.get("Image"), visualBase));
        symbols.add(parse(new Textbox(), drawMap.get("Textbox"), visualBase));
         symbols.add(parse(new Label(), drawMap.get("Label"), visualBase));
         symbols.add(parse(new ListView(), drawMap.get("ListView"), visualBase));
 
         drawMap.put("PlainPanel", new DynamicNode.DrawDelegate() {
             public void draw(GFX g, DynamicNode node) {
                 double w = node.getWidth();
                 double h = node.getHeight();
                 g.setPaint(FlatColor.GRAY);
                 g.fillRect(0, 0, w, h);
                 g.setPaint(FlatColor.BLACK);
                 g.drawRect(0,0,w,h);
             }
         });
 
         drawMap.put("Spinner", new DynamicNode.DrawDelegate() {
             public void draw(GFX g, DynamicNode node) {
                 double w = node.getWidth();
                 double h = node.getHeight();
                 g.setPaint(FlatColor.BLACK);
                 g.drawOval(10, 10, w - 20, h - 20);
             }
         });
         drawMap.put("FlickrQuery", new DynamicNode.DrawDelegate() {
             public void draw(GFX g, DynamicNode node) {
                 double w = node.getWidth();
                 double h = node.getHeight();
                 g.setPaint(FlatColor.YELLOW);
                 g.fillRoundRect(0, 0, 80, 80, 10, 10);
                 g.setPaint(FlatColor.BLACK);
                 g.drawRoundRect(0, 0, 80, 80, 10, 10);
                 g.drawText("Flickr Query", Font.DEFAULT, 10, 15);
             }
         });
 
         symbols.add(parse(new PlainPanel(), drawMap.get("PlainPanel"), visualBase));
         symbols.add(parse(new TabPanel(), drawMap.get("PlainPanel"), visualBase));
         symbols.add(parse(new Spinner(), drawMap.get("Spinner"), visualBase));
         //symbols.add(parse(new StringListModel(), drawMap.get("FlickrQuery"), visualBase));
         //symbols.add(parse(new ControlListModel(), drawMap.get("FlickrQuery"), visualBase));
         DynamicNode photo = parse(new FlickrQuery.Photo("a","b"), drawMap.get("FlickrQuery"), visualBase);
         DynamicNode flickr =  parse(new FlickrQuery(), drawMap.get("FlickrQuery"), visualBase);
         flickr.getProperty("results").setList(true).setItemPrototype(photo);
         symbols.add(flickr);
 
         add(symbols);
 
     }
 
     @Override
     public String getName() {
         return "Amino JS";
     }
 
     @Override
     public SketchDocument createEmptyDoc() {
         SketchDocument doc = new SketchDocument();
         doc.setModeId(this.getId());
         Layer layer = new Layer();
         layer.add(findSymbol("PlainPanel").duplicate(null));
         Page page = new Page();
         page.add(layer);
         doc.add(page);
         return doc;
     }
 
     @Override
     public void modifyFileMenu(Menu fileMenu, SketchDocument doc) {
         fileMenu.addItem("Test HTML", "R", new HTMLBindingExport(doc,true));
         fileMenu.addItem("Export HTML", "E", new HTMLBindingExport(doc,false));
     }
 
     @Override
     public Map<String,DynamicNode.DrawDelegate> getDrawMap() {
         return drawMap;
     }
 
     private static DynamicNode parse(Object o, DynamicNode.DrawDelegate del, DynamicNode base) {
         DynamicNode nd = BindingUtils.parseAnnotatedPOJO(o, del);
         if(base != null) {
             nd.copyPropertiesFrom(base);
         }
         return nd;
     }
 }
