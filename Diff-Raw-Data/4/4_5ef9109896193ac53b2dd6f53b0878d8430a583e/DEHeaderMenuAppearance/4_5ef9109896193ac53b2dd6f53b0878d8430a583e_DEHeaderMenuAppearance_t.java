 package org.iplantc.de.client.desktop.views;
 
import org.iplantc.core.uicommons.client.appearance.widgets.AnchorDefaultResources;
 import org.iplantc.core.uicommons.client.widgets.IPlantAnchorDefaultAppearance;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.resources.client.ClientBundle;
 
 public class DEHeaderMenuAppearance extends IPlantAnchorDefaultAppearance {
 
     public interface HeaderResources extends ClientBundle {
         @Source("DEHeaderMenuAppearance.css")
        AnchorDefaultResources.Style style();
     }
 
     public DEHeaderMenuAppearance() {
         HeaderResources resources = ((HeaderResources)GWT.create(HeaderResources.class));
         this.style = resources.style();
         this.style.ensureInjected();
         this.template = GWT.create(Template.class);
     }
 }
