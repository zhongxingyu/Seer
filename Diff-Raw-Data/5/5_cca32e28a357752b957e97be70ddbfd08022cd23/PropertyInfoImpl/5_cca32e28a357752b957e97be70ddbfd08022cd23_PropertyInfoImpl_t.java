 package org.mule.galaxy.impl.jcr;
 
 import javax.jcr.Node;
 
 import org.mule.galaxy.Item;
 import org.mule.galaxy.PropertyInfo;
 import org.mule.galaxy.Registry;
 import org.mule.galaxy.index.Index;
 import org.mule.galaxy.type.PropertyDescriptor;
 import org.mule.galaxy.type.TypeManager;
 
 public class PropertyInfoImpl implements PropertyInfo {
 
     private Node node;
     private String name;
     private boolean index;
     private String description;
     private Object desc;
     private boolean loadedDescriptor;
     private TypeManager tm;
     private final Item item;
     private final Object value;
     
     public PropertyInfoImpl(Item item, String name, Node node, TypeManager tm) {
         this(item, name, node, null, null);
         this.tm = tm;
     }
 
     public PropertyInfoImpl(Item item, String name, Node node, PropertyDescriptor pd, Object value) {
         this.item = item;
         this.node = node;
         this.name= name;
         this.desc = pd;
         this.value = value;
        if (pd != null) {
            loadedDescriptor = true;
            description = pd.getDescription();
        }
     }
     public String getName() {
         return name;
     }
 
     public Object getValue() {
         if (value != null) {
             return value;
         } else {
             return item.getProperty(getName());
         }
     }
 
     public Object getInternalValue() {
         return item.getInternalProperty(getName());
     }
 
     public boolean isLocked() {
         Boolean b = JcrUtil.getBooleanOrNull(node, getName() + JcrVersion.LOCKED);
         if (b == null) {
             return false;
         }
         return b;
     }
 
     public boolean isVisible() {
         Boolean vis = JcrUtil.getBooleanOrNull(node, getName() + JcrVersion.VISIBLE);
         if (vis == null) {
             return true;
         }
         
         return vis;
     }
 
     public String getDescription() {
         loadPropertyOrIndex();
         return description;
     }
 
     private void loadPropertyOrIndex() {
         if (loadedDescriptor) return;
         
         desc = tm.getPropertyDescriptorByName(getName());
         
         if (desc instanceof Index) {
             index = true;
             description = ((Index) desc).getDescription();
         } else if (desc != null) {
             description = ((PropertyDescriptor) desc).getDescription();
         }
         loadedDescriptor = true;
     }
 
     public PropertyDescriptor getPropertyDescriptor() {
         loadPropertyOrIndex();
         if (!index) {
             return (PropertyDescriptor) desc;
         }
         return null;
     }
 
     public boolean isIndex() {
         loadPropertyOrIndex();
         return index;
     }
 
 }
