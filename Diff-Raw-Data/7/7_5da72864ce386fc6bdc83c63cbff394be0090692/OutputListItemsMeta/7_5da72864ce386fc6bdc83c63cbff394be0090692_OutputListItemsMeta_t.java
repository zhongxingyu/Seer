 package org.icefaces.component.list;
 
 import org.icefaces.ace.meta.annotation.Component;
 import org.icefaces.ace.meta.annotation.Property;
 import org.icefaces.ace.meta.baseMeta.UIDataMeta;
import org.icefaces.ace.meta.baseMeta.UISeriesBaseMeta;
 
 
 @Component(
         tagName = "outputListItems",
         componentClass = "org.icefaces.component.list.OutputListItems",
         rendererClass = "org.icefaces.component.list.OutputListItemsRenderer",
         generatedClass = "org.icefaces.component.list.OutputListItemsBase",
         componentType = "org.icefaces.OutputListItems",
         rendererType = "org.icefaces.OutputListItemsRenderer",
        extendsClass = "org.icefaces.impl.component.UISeriesBase",
         componentFamily = "org.icefaces.OutputListItems",
         tlddoc = "This mobility component is used within an outputlist tag to group lists of items."
 )
public class OutputListItemsMeta extends UISeriesBaseMeta {
 
     @Property(defaultValue="false",
     		  tlddoc = "true enables padding around list group")
     private boolean inset;
 
     @Property(defaultValue="default", tlddoc = " default or thumb  " )
     private String type;
 
     @Property(tlddoc = "style will be rendered on the root element of this " +
      "component.")
      private String style;
 
     @Property(tlddoc = "style class will be rendered on the root element of " +
         "this component.")
     private String styleClass;
 
 }
 
