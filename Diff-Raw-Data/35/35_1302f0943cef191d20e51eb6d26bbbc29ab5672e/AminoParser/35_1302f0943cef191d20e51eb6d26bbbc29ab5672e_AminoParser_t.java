 package com.joshondesign.treegui.modes.aminojava;
 
 import com.joshondesign.treegui.AnchorPanel;
 import com.joshondesign.treegui.PropUtils;
 import com.joshondesign.xml.Elem;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.xml.xpath.XPathExpressionException;
 import org.joshy.gfx.draw.FlatColor;
 import org.joshy.gfx.event.*;
 import org.joshy.gfx.node.Node;
 import org.joshy.gfx.node.Parent;
 import org.joshy.gfx.node.control.*;
 import org.joshy.gfx.node.layout.Container;
 import org.joshy.gfx.node.layout.Panel;
 import org.joshy.gfx.util.u;
 
 public class AminoParser {
     public static Node parsePage(Elem root) throws Exception {
         Map<String, Object> objectMap = new HashMap<String, Object>();
         List<Elem> deferredBindings = new ArrayList<Elem>();
 
         Node last = null;
         for(Elem vis : root.xpath("nodes/node")) {
             Object obj = processNode(vis, objectMap, deferredBindings);
             if(obj instanceof Node && vis.attrEquals("visual","true")) {
                 last = (Node) obj;
                 processNodeChildren(last,vis,objectMap, deferredBindings);
             }
             u.p("last node is set to: " + last);
         }
 
         for(final Elem binding : root.xpath("bindings/binding")) {
             if(binding.attrEquals("mirror","true")) {
                 u.p("it's a mirror binding. deferring");
                 deferredBindings.add(binding);
                 continue;
             }
 
             final Object src = objectMap.get(binding.attr("sourceid"));
             final Object tgt = objectMap.get(binding.attr("targetid"));
             final String tgtProp = binding.attr("targetprop");
 
             //applyBinding(binding);
 
             if(binding.attrEquals("sourcetype", GuiTest.TriggerType.class.getName())) {
                 EventBus.getSystem().addListener(src, ActionEvent.Action, new Callback<Event>() {
                     public void call(Event event) throws Exception {
                         tgt.getClass().getMethod(tgtProp).invoke(tgt);
                     }
                 });
                 continue;
             }
             if(binding.attrEquals("sourcetype","java.lang.String") && src instanceof Textbox) {
                 EventBus.getSystem().addListener(src, ChangedEvent.StringChanged, new Callback<ChangedEvent>() {
                     public void call(ChangedEvent changedEvent) throws Exception {
                         setWithSetter(src, binding.attr("sourceprop"),
                                 tgt, tgtProp, binding.attr("targettype"));
                     }
                 });
                 continue;
             }
             if(binding.attrEquals("sourcetype","java.lang.Boolean")) {
                 u.p("doing a boolean bind");
                 EventBus.getSystem().addListener(src, ChangedEvent.BooleanChanged, new Callback<ChangedEvent>() {
                     public void call(ChangedEvent changedEvent) throws Exception {
                         setWithSetter(src, binding.attr("sourceprop"),
                                 tgt, tgtProp, binding.attr("targettype"));
                     }
                 });
                 continue;
             }
             applyBinding(src, tgt, binding);
         }
 
         return last;
     }
 
     private static void applyBinding(Object src, Object tgt, Elem binding) throws Exception {
         final String tgtProp = binding.attr("targetprop");
         if(binding.attrEquals("sourcetype","java.lang.String")) {
             setWithSetter(
                     src,binding.attr("sourceprop"),
                     tgt, tgtProp, binding.attr("targettype"));
             return;
         }
         if(binding.attrEquals("sourcetype","java.lang.Double") && binding.attrEquals("targettype","java.lang.CharSequence")) {
             u.p("we need to coerce a double to a string");
             Integer srcval = (Integer) PropUtils.findGetter(src, binding.attr("sourceprop")).invoke(src);
             String strval = srcval.toString();
             PropUtils.findSetter(tgt,tgtProp).invoke(tgt,strval);
         }
         if(binding.attrEquals("sourcetype", ListModel.class.getName())) {
             setWithSetter(
                     src, binding.attr("sourceprop"),
                     tgt, tgtProp, binding.attr("targettype"));
             return;
         }
     }
 
     private static void setWithSetter(Object src, String sourceprop, Object tgt, String tgtProp, String tgttype) throws Exception {
         Object value = PropUtils.findGetter(src, sourceprop).invoke(src);
         PropUtils.findSetter(tgt,tgtProp).invoke(tgt,value);
     }
 
     private static Object processNode(Elem vis, Map<String, Object> objectMap, List<Elem> deferredBindings) throws IllegalAccessException, InstantiationException, ClassNotFoundException, XPathExpressionException {
         String classname = vis.attr("class");
 
         if(vis.attrEquals("custom","true")) {
             classname = vis.attr("customClass");
         }
 
         if(vis.attrEquals("mirror","true")) {
             u.p("loading up a mirror. we don't really need to do this. skip it");
             return null;
         }
         Class clazz = null;
 
         try {
             clazz  = Class.forName(classname);
         } catch (ClassNotFoundException clfn) {
             u.p("couldn't find a the class: " + classname);
             u.p("   substituting a button");
             clazz = Button.class;
         }
 
         Object obj = clazz.newInstance();
         objectMap.put(vis.attr("id"),obj);
 
         try {
             initObject(obj, vis, objectMap, deferredBindings);
         } catch (Exception ex) {
             u.p("problem with class: " + classname);
             u.p(ex);
         }
 
         if(obj instanceof Control) {
             ((Control)obj).setId(vis.attr("id"));
         }
         return obj;
     }
 
     private static void processNodeChildren(Node root, Elem elem, Map<String, Object> objectMap, List<Elem> deferredBindings) throws XPathExpressionException, ClassNotFoundException, IllegalAccessException, InstantiationException {
         for(Elem vis : elem.xpath("children/node")) {
             Object obj = processNode(vis, objectMap, deferredBindings);
             if(obj instanceof Node) {
                 Node child = (Node) obj;
                 if(root instanceof Container) {
                     Container container = (Container) root;
                     if(container instanceof AnchorPanel && child instanceof Control) {
                         AnchorPanel anchorPanel = (AnchorPanel) container;
                         Control control = (Control) child;
                         AnchorPanel.AnchorSettings anchor = parseAnchor(vis);
                         //anchorPanel.DEBUG = true;
                         anchorPanel.add(control, anchor);
                     }else {
                         container.add(child);
                     }
 
                 }
                 if(root instanceof ScrollPane) {
                     ((ScrollPane)root).setContent(child);
                 }
 
                 processNodeChildren(child, vis, objectMap, deferredBindings);
             }
         }
     }
 
 
     private static void initObject(Object node, final Elem xml, final Map<String, Object> objectMap, final List<Elem> deferredBindings) throws XPathExpressionException {
         List<String> skipList = new ArrayList<String>();
         skipList.add("anchorLeft");
         skipList.add("anchorRight");
         skipList.add("anchorTop");
         skipList.add("anchorBottom");
         skipList.add("right");
         skipList.add("bottom");
 
         Class clazz = node.getClass();
         for(Elem eprop : xml.xpath("property")) {
             if(skipList.contains(eprop.attr("name"))) continue;
             if(eprop.attrEquals("exported", Boolean.FALSE.toString())) continue;
             if(eprop.attrEquals("name", "class")) continue;
 
             String name = eprop.attr("name");
             if(eprop.hasAttr("exportname")) {
                 name = eprop.attr("exportname");
             }
             String setter = "set" + name.substring(0,1).toUpperCase() + name.substring(1);
 
 
             //String value = eprop.attr("value");
             try {
                 if(eprop.attrEquals("type","java.lang.String")) {
                     Method method = clazz.getMethod(setter, Class.forName(eprop.attr("type")));
                     method.invoke(node, eprop.attr("value"));
                 }
                 if(eprop.attrEquals("type","java.lang.CharSequence")) {
                     Method method = clazz.getMethod(setter, Class.forName(eprop.attr("type")));
                     method.invoke(node, eprop.attr("value"));
                 }
                 if(eprop.attrEquals("type","java.lang.Double")) {
                     Method method = findSetter(clazz, setter, eprop);
                     method.invoke(node, Double.parseDouble(eprop.attr("value")));
                 }
                if(eprop.attrEquals("type","java.lang.Boolean")) {
                    Method method = findSetter(clazz, setter, eprop);
                    method.invoke(node, Boolean.parseBoolean(eprop.attr("value")));
                }
                if(eprop.attrEquals("type","org.joshy.gfx.node.control.ListModel")) {
                    u.p("doing a list model");
                    Method method = clazz.getMethod(setter, Class.forName(eprop.attr("type")));
                    method.invoke(node, ListView.createModel(eprop.attr("value").split(",")));
                }
                 if(eprop.attrEquals("type","org.joshy.gfx.draw.FlatColor")) {
                     Method method = clazz.getMethod(setter, Class.forName(eprop.attr("type")));
                     method.invoke(node, new FlatColor(eprop.attr("value")));
                 }
                 if(eprop.attrEquals("enum","true")) {
                     Class clazz2 = clazz.forName(eprop.attr("type"));
                     Method method = clazz.getMethod(setter, clazz2);
                     method.invoke(node, Enum.valueOf(clazz2, eprop.attr("value")));
                 }
             } catch (Exception ex) {
                 ex.printStackTrace();
             }
 
 
             if(node instanceof CompoundListView) {
                 CompoundListView lv = (CompoundListView) node;
                 lv.setItemViewFactory(new CompoundListView.ItemViewFactory() {
                     @Override
                     public Control createItemView(CompoundListView view, int n, Control control) {
                         Object item = view.getModel().get(n);
                         if (item == null) return null;
                         u.p("the item is: " + item);
 
                         try {
                             GrabPanel panel = new GrabPanel();
                             processNodeChildren(panel, xml, objectMap, deferredBindings);
                             List<Control> controls = new ArrayList<Control>();
                             for(Control c : ((Container)panel.getChildren().get(0)).controlChildren()) {
                                 controls.add(c);
                             }
                             processDeferredBindings(controls,objectMap,item, deferredBindings);
                             List<Node> children = panel.getChildren();
                             return (Control) children.get(0);
                         } catch (Exception e) {
                             u.p(e);
                             return null;
                         }
                     }
                 });
             }
         }
 
     }
 
     private static void processDeferredBindings(List<Control> nodes, Map<String, Object> objectMap, Object dataItem, List<Elem> deferredBindings) {
         u.p("----------");
         u.p("doing a deferred binding");
         for(Control node : nodes) {
             String targetID = null;
             for(Map.Entry<String,Object> entry : objectMap.entrySet()) {
                 if(entry.getValue() == node) {
                     u.p("found the id: " + entry.getKey() + " for node " + node);
                     targetID = entry.getKey();
                 }
             }
 
             u.p("target = " + targetID + " " + node);
             if(targetID == null) return;
             for(Elem binding : deferredBindings) {
                 if(binding.attrEquals("targetid",targetID)) {
                     u.p("found matching binding");
                     u.p("binding = " + binding.attr("sourceprop") + " => " + binding.attr("targetprop"));
                     try {
                         applyBinding(dataItem, node, binding);
                     } catch (Exception e) {
                         e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                     }
                 }
             }
         }
 
 
         u.p("------");
     }
 
     private static AnchorPanel.AnchorSettings parseAnchor(Elem echild) throws XPathExpressionException {
 
         double left = 0;
         boolean leftSet = false;
         double right = 0;
         boolean rightSet = false;
         double top = 0;
         boolean topSet = false;
         double bottom = 0;
         boolean bottomSet = false;
 
         double translateX = 0;
         double translateY = 0;
         for(Elem eprop : echild.xpath("property")) {
             if(eprop.attrEquals("name","translateX")) {
                 translateX = Double.parseDouble(eprop.attr("value"));
             }
             if(eprop.attrEquals("name","translateY")) {
                 translateY = Double.parseDouble(eprop.attr("value"));
             }
             if(eprop.attrEquals("name","anchorLeft")) {
                 leftSet = eprop.attrEquals("value", "true");
             }
             if(eprop.attrEquals("name","anchorRight")) {
                 rightSet = eprop.attrEquals("value","true");
             }
             if(eprop.attrEquals("name","right")) {
                 right = Double.parseDouble(eprop.attr("value"));
             }
             if(eprop.attrEquals("name","anchorTop")) {
                 topSet = eprop.attrEquals("value", "true");
             }
             if(eprop.attrEquals("name","anchorBottom")) {
                 bottomSet = eprop.attrEquals("value","true");
             }
             if(eprop.attrEquals("name","bottom")) {
                 bottom = Double.parseDouble(eprop.attr("value"));
             }
         }
         if(leftSet) {
             left = translateX;
         }
         if(topSet) {
             top = translateY;
         }
         return new AnchorPanel.AnchorSettings(left, leftSet, right, rightSet, top, topSet, bottom, bottomSet);
     }
 
     private static Method findSetter(Class clazz, String setter, Elem eprop) throws ClassNotFoundException, NoSuchMethodException {
        Class type = Class.forName(eprop.attr("type"));
         try {
            Method method = clazz.getMethod(setter, type);
             return method;
         } catch (NoSuchMethodException e) {

            //try the primitive versions instead
            if(type == java.lang.Boolean.class) {
                return clazz.getMethod(setter, Boolean.TYPE);
            }
             Method method = clazz.getMethod(setter, Double.TYPE);
             return method;
         }
     }
 
     public static Node find(String name, Control rootControl) {
         if(name.equals(rootControl.getId())) return rootControl;
         if(rootControl instanceof Parent) {
             Parent parent = (Parent) rootControl;
             for(Node node : parent.children()) {
                 if(node instanceof Control) {
                     Node nd = find(name, (Control) node);
                     if(nd != null) return nd;
                 }
             }
         }
         return null;
     }
 
 
     private static class GrabPanel extends Panel {
 
         public List<Node> getChildren() {
             return children;
         }
     }
 }
