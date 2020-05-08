 package org.opennms.applicationstack.view;
 
 import com.vaadin.ui.Alignment;
 import com.vaadin.ui.CustomComponent;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Panel;
 import com.vaadin.ui.VerticalLayout;
 import org.opennms.applicationstack.model.ApplicationLayer;
 import org.opennms.applicationstack.model.ApplicationStack;
 
 /**
  *
  * @author mvrueden
  */
 public class ApplicationLayerComponent extends CustomComponent {
     
     private final HealthIndicator healthIndicator = new HealthIndicator();
     
     private final Label label = new Label();
     
     public ApplicationLayerComponent() {
         setStyleName("applicationLayer");        
         
         Panel p = new Panel();
         p.setSizeFull();
         p.setStyleName("applicationLayer");
         
         VerticalLayout content = new VerticalLayout();
         content.setSizeFull();
         content.addComponent(label);
         content.addComponent(healthIndicator);
         content.setComponentAlignment(content.getComponent(1), Alignment.BOTTOM_LEFT);
        content.setSpacing(false);
        content.setMargin(false);
         
         setCompositionRoot(p);
         p.setContent(content);
     }
     
     public ApplicationLayerComponent render(ApplicationStack stack, ApplicationLayer layer) {
         label.setCaption(layer.getLabel());
         setWidth(stack.computeColumnWidth() * layer.getWidth(), Unit.PIXELS);
         setHeight(stack.computeRowHeight() * layer.getHeight(), Unit.PIXELS);
         healthIndicator.render(layer);
 
         // TODO Margin/Padding, usw...
         return this;
     }
 }
