 package com.joshondesign.treegui.modes.aminojava;
 
 import org.joshy.gfx.draw.FlatColor;
 import org.joshy.gfx.node.control.ListModel;
 import org.joshy.gfx.util.u;
 
 public class Property {
 
     private final String name;
     private final Class type;
     private Object value;
     private boolean exported = true;
     private String exportName;
     private boolean visible = true;
     private boolean bindable = false;
     private boolean list;
     private DynamicNode itemPrototype;
     private String displayName;
     private DynamicNode node;
     private boolean compound = false;
     private String masterProperty = null;
     private boolean proxy;
 
     public Property(String name, Class type, Object value) {
         this.name = name;
         this.type = type;
         this.value = value;
     }
 
     public Property setExported(boolean exported) {
         this.exported = exported;
         return this;
     }
 
     public boolean isExported() {
         return exported;
     }
 
     public String getName() {
         return name;
     }
 
     public String encode() {
         if(type == String.class) return (String)value;
         if(type == Boolean.class) return ((Boolean)value).toString();
         if(type == Boolean.TYPE) {
             if(value == null) return Boolean.toString(false);
             return ((Boolean)value).toString();
         }
         if(type == Double.class) {
             if(value instanceof Integer) {
                 return ((Integer)value).toString();
             }
             return ((Double)value).toString();
         }
         if(type == Double.TYPE) {
             if(value == null) return ""+0;
             return Double.toString((Double)value);
         }
         if(type == Integer.class) {
             if(value instanceof Integer) {
                 return ((Integer)value).toString();
             }
             return ((Double)value).toString();
         }
         if(type == CharSequence.class) {
             return ((CharSequence)value).toString();
         }
         if(type == FlatColor.class) {
             return Integer.toHexString(((FlatColor)value).getRGBA());
         }
         if(type.isEnum()) {
             Object[] vals = type.getEnumConstants();
             return value.toString();
         }
         if(type == ListModel.class) {
             if(value != null && value instanceof ListModel) {
                 ListModel model = (ListModel) value;
                 StringBuffer sb = new StringBuffer();
                 for(int i=0; i<model.size(); i++) {
                     sb.append(model.get(i)+",");
                 }
                 return sb.toString();
             }
         }
         if(type == Class.class && value != null) {
             return ((Class)value).getName();
         }
        u.p("WARNING: returning null for the encoding of property " + getName());
         return null;
     }
 
     public Class getType() {
         return type;
     }
 
     public Property duplicate() {
         Property p = new Property(this.name,this.type,this.value);
         p.exportName = this.exportName;
         p.setVisible(this.isVisible());
         p.setExported(this.isExported());
         p.setBindable(this.isBindable());
         p.setList(this.isList());
         p.setItemPrototype(this.getItemPrototype());
         p.setDisplayName(this.displayName);
         p.setCompound(this.isCompound());
         p.setMasterProperty(this.getMasterProperty());
         return p;
     }
 
     public double getDoubleValue() {
         if(type == Double.class || type == Double.TYPE) {
             if(value instanceof Double) {
                 return ((Double)value).doubleValue();
             }
             if(value instanceof Integer) {
                 return ((Integer)value).doubleValue();
             }
         }
         return -9999;
     }
 
     public int getIntegerValue() {
         if(type == Integer.class || type == Integer.TYPE) {
             if(value instanceof Integer) {
                 return ((Integer)value).intValue();
             }
             if(value instanceof Integer) {
                 return ((Integer)value).intValue();
             }
         }
         return -9999;
     }
 
     public String getStringValue() {
         if(type == String.class) {
             return ((String)value);
         }
         if(type == CharSequence.class) {
             return ((CharSequence)value).toString();
         }
         return "ERROR";
     }
 
     public boolean getBooleanValue() {
         if(type == Boolean.class || type == Boolean.TYPE) {
             return ((Boolean)value);
         }
         return false;
     }
 
     public FlatColor getColorValue() {
         return (FlatColor) value;
     }
 
     public void setDoubleValue(double value) {
         this.value = new Double(value);
         markChanged();
     }
 
     public void setIntegerValue(int value) {
         this.value = new Integer(value);
         markChanged();
     }
 
     private void markChanged() {
         if(node != null) {
             node.markPropertyChanged();
         }
     }
 
     public Property setExportName(String exportName) {
         this.exportName = exportName;
         markChanged();
         return this;
     }
 
     public String getExportName() {
         return exportName;
     }
 
     public void setStringValue(String text) {
         this.value = text;
         markChanged();
     }
 
     public void setDoubleValue(String text) {
         this.value = Double.parseDouble(text);
         markChanged();
     }
 
     public void setIntegerValue(String text) {
         this.value = Integer.parseInt(text);
         markChanged();
     }
 
     public void setBooleanValue(boolean selected) {
         this.value = new Boolean(selected);
         markChanged();
     }
 
     public Property setVisible(boolean visible) {
         this.visible = visible;
         markChanged();
         return this;
     }
 
     public boolean isVisible() {
         return visible;
     }
 
     public void setEnumValue(Object o) {
         this.value = o;
         markChanged();
     }
 
     public void setColorValue(FlatColor value) {
         this.value = value;
         markChanged();
     }
 
     public Enum getEnumValue() {
         return (Enum) this.value;
     }
 
     public Property setBindable(boolean bindable) {
         this.bindable = bindable;
         markChanged();
         return this;
     }
 
     public boolean isBindable() {
         return bindable;
     }
 
     public Property setList(boolean list) {
         this.list = list;
         markChanged();
         return this;
     }
 
     public boolean isList() {
         return list;
     }
 
     public Property setItemPrototype(DynamicNode itemPrototype) {
         this.itemPrototype = itemPrototype;
         markChanged();
         return this;
     }
 
     public DynamicNode getItemPrototype() {
         return itemPrototype;
     }
 
     public Property setDisplayName(String name) {
         this.displayName = name;
         markChanged();
         return this;
     }
 
     public String getDisplayName() {
         if(displayName != null) return displayName;
         return getName();
     }
 
     public void setNode(DynamicNode node) {
         this.node = node;
         markChanged();
     }
 
     public DynamicNode getNode() {
         return node;
     }
 
     public Object getRawValue() {
         return value;
     }
 
     public void setRawValue(Object rawValue) {
         this.value = rawValue;
     }
 
     public void setCompound(boolean compound) {
         this.compound = compound;
     }
 
     public boolean isCompound() {
         return compound;
     }
 
     public void setMasterProperty(String masterProperty) {
         this.masterProperty = masterProperty;
     }
 
     public String getMasterProperty() {
         return masterProperty;
     }
 
     @Override
     public String toString() {
         return "Property{" +
                 "name='" + name + '\'' +
                 ", type=" + type +
                 ", value=" + value +
                 ", exported=" + exported +
                 ", exportName='" + exportName + '\'' +
                 ", visible=" + visible +
                 ", bindable=" + bindable +
                 ", list=" + list +
                 ", itemPrototype=" + itemPrototype +
                 ", displayName='" + displayName + '\'' +
                 ", node=" + node +
                 ", compound=" + compound +
                 ", masterProperty='" + masterProperty + '\'' +
                 ", proxy=" + proxy +
                 '}';
     }
 
     public void setProxy(boolean proxy) {
         this.proxy = proxy;
     }
 
     public boolean isProxy() {
         return proxy;
     }
 }
