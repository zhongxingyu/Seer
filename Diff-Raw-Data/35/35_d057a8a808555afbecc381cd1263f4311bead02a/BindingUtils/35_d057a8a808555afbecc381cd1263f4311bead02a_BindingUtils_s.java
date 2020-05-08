 package com.joshondesign.treegui;
 
 import com.joshondesign.treegui.docmodel.SketchDocument;
 import com.joshondesign.treegui.docmodel.SketchNode;
 import com.joshondesign.treegui.modes.aminojava.DynamicNode;
 import com.joshondesign.treegui.modes.aminojava.Prop;
 import com.joshondesign.treegui.modes.aminojava.Property;
 import com.joshondesign.treegui.modes.aminojs.ActionProp;
 import com.joshondesign.treegui.modes.aminojs.TriggerProp;
 import java.lang.reflect.Field;
 import org.joshy.gfx.util.u;
 
 public class BindingUtils {
 
     public static void populateWithBindablePropertiesDynamic(BindingBox popup, DynamicNode node, Canvas canvas, SketchDocument doc) {
         for(Property prop : node.getSortedProperties()) {
             if(!prop.isBindable()) continue;
             String name = prop.getName();
 
             if(prop.isCompound()) {
                 u.p("a compound prop. exploding");
                 Property master = node.getProperty(prop.getMasterProperty());
                 u.p("master = " + master.getName());
                 u.p("value = " + master.getRawValue());
                 Binding binding = findTargetBinding(node, master.getName(), doc);
                 u.p("current binding = " + binding);
                 if(binding == null) continue;
                 Property srcProp = binding.getSource().getProperty(binding.getSourceProperty().getName());
                 u.p("src prop = " + srcProp);
                 if(srcProp.isList()) {
                     u.p("master is bound to a list prop. we can get the prototype");
                     DynamicNode proto = srcProp.getItemPrototype();
                     u.p("proto = " + proto);
                     for(Property p2 :proto.getSortedProperties()) {
                         u.p("p2 = " + p2.getName());
                         if(p2.isBindable()) {
                             Property px = new Property(p2.getName(),p2.getType(),p2.getRawValue());
                             px.setProxy(true);
                             px.setMasterProperty(prop.getName());
                             popup.addProperty(canvas, doc, node, px, null, null, true, true);
                         }
                     }
                     continue;
                 }
             }
 
             final Binding sourceBinding = findSourceBinding(node, name, doc);
             final Binding targetBinding = findTargetBinding(node, name, doc);
             popup.addProperty(canvas, doc, node, prop, sourceBinding, targetBinding,  true, true);
         }
     }
 
     private static Binding findTargetBinding(DynamicNode selection, String prop, SketchDocument doc) {
         for(Binding binding : doc.getBindings()) {
             if(binding.getTarget() == selection) {
                 if(binding.getTargetProperty().getName().equals(prop)) {
                     return binding;
                 }
             }
         }
         return null;
     }
 
     private static Binding findSourceBinding(SketchNode selection, String prop, SketchDocument doc) {
         for(Binding binding : doc.getBindings()) {
             if(binding.getSource() == selection) {
                 if(binding.getSourceProperty().getName().equals(prop)) {
                     return binding;
                 }
             }
         }
         return null;
     }
 
     public static void populateWithTargetPropertiesDynamic(BindingBox popup2, DynamicNode node, Binding currentBinding, Canvas canvas, SketchDocument doc) {
         for(Property prop : node.getSortedProperties()) {
             if(!prop.isBindable()) continue;
             String name = prop.getName();
             final Binding sourceBinding = findSourceBinding(node, name, doc);
             final Binding targetBinding = findTargetBinding(node, name, doc);
             boolean canUse = true;
             if(currentBinding.getSourceProperty().getType() == TriggerProp.class) {
                 if(prop.getType() != ActionProp.class) {
                     canUse = false;
                 }
             }
             popup2.addProperty(canvas, doc, node, prop, sourceBinding, targetBinding,  false, canUse);
         }
     }
 
 
     public static DynamicNode parseAnnotatedPOJO(Object obj, DynamicNode.DrawDelegate base) {
         u.p("processing: " + obj.getClass().getSimpleName());
         DynamicNode node = new DynamicNode();
         node.setName(obj.getClass().getSimpleName());
         node.setResizable(false);
         node.setDrawDelegate(base);
         node.addProperty(new Property("translateX", Double.class, 0).setExported(false))
                 .addProperty(new Property("translateY", Double.class, 0).setExported(false))
                 .addProperty(new Property("width", Double.class, 90).setBindable(false).setExported(false))
                 .addProperty(new Property("height", Double.class, 50).setBindable(false).setExported(false))
         ;
         try {
             parseFields(obj.getClass().getFields(), obj, node);
         } catch (Exception ex) {
             ex.printStackTrace();
         }
 
         return node;
 
     }
 
     private static void parseFields(Field[] fields, Object obj, DynamicNode node) throws IllegalAccessException {
         for(Field field : fields) {
 //            for(Annotation an : field.getAnnotations()) {
                 //u.p("  ann " + an);
 //            }
 
             if(field.isAnnotationPresent(Prop.class)) {
                 Prop prop = field.getAnnotation(Prop.class);
                 u.p("field = " + field.getName() + " type = " + field.getType() + " value = " + field.get(obj));
 //                    u.p("  bindable = " + prop.bindable());
                 String name = field.getName();
                 if(name.equals("clazz")) {
                     name = "class";
                 }
                 Property p = new Property(name, field.getType(), field.get(obj));
 
                 p.setBindable(prop.bindable());
                 p.setVisible(prop.visible());
                 p.setExported(prop.exported());
                 node.addProperty(p);
             }
         }
     }
 
     public static Binding createBinding(DynamicNode source, String sname,
                                         DynamicNode target, String tname) {
         Binding binding = new Binding();
         binding.setSource(source);
         binding.setSourceProperty(source.getProperty(sname));
         binding.setTarget(target);
         binding.setTargetProperty(target.getProperty(tname));
         return binding;
     }
 }
