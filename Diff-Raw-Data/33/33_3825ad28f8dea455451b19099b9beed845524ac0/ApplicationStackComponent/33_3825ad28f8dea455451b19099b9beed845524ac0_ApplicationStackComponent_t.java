 package org.opennms.applicationstack.view;
 
 import com.vaadin.ui.CustomComponent;
 import com.vaadin.ui.GridLayout;
 import org.opennms.applicationstack.model.ApplicationLayer;
 import org.opennms.applicationstack.model.ApplicationStack;
 import org.opennms.applicationstack.model.Coordinates;
 
 /**
  *
  * @author mvrueden
  */
 public class ApplicationStackComponent extends CustomComponent {
 
     private static final int PADDING_OFFSET = 20;
     
     private final GridLayout gridLayout = new GridLayout();
     
     public ApplicationStackComponent() {
     }
     
     public ApplicationStackComponent render(ApplicationStack stack) {
         setCaption(stack.getLabel());
//      setWidth(stack.computeWidth() + PADDING_OFFSET, Unit.PIXELS);
         setStyleName("applicationStack");
         setCompositionRoot(gridLayout);
         
         gridLayout.addStyleName("applicationStack");
         gridLayout.removeAllComponents();
         gridLayout.setRows(stack.getRowCount());
         gridLayout.setColumns(stack.getColumnCount());
//        gridLayout.setWidth(stack.computeWidth() + PADDING_OFFSET, Unit.PIXELS);
//        gridLayout.setSpacing(false);
//        gridLayout.setMargin(false);
         
         for (ApplicationLayer eachLayer : stack.getLayers()) {
             final Coordinates c = eachLayer.getCoordinates();
             gridLayout.addComponent(
                     new ApplicationLayerComponent()
                         .render(stack, eachLayer), 
                     c.column1, 
                     c.row1, 
                     c.column2,
                     c.row2);
         }
         return this;
     }
 }
