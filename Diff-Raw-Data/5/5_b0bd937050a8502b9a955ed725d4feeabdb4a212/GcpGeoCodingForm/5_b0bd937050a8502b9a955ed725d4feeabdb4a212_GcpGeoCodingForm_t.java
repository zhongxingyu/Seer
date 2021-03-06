 package org.esa.beam.visat.toolviews.pin;
 
 import org.esa.beam.framework.datamodel.GcpGeoCoding;
 import org.esa.beam.framework.datamodel.GeoCoding;
 import org.esa.beam.framework.datamodel.Pin;
 import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.ProductNode;
 import org.esa.beam.framework.datamodel.ProductNodeEvent;
 import org.esa.beam.framework.datamodel.ProductNodeGroup;
 import org.esa.beam.framework.datamodel.ProductNodeListener;
 import org.esa.beam.framework.dataop.maptransf.Datum;
 import org.esa.beam.framework.ui.TableLayout;
 import org.esa.beam.util.Debug;
 
 import javax.swing.AbstractAction;
 import javax.swing.BorderFactory;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.JToggleButton;
 import javax.swing.SwingWorker;
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.text.DecimalFormat;
 import java.text.FieldPosition;
 import java.text.Format;
 import java.text.NumberFormat;
 import java.text.ParsePosition;
 import java.util.concurrent.ExecutionException;
 
 /**
  * GCP geo-coding form.
  *
  * @author Marco Peters
  * @version $Revision:$ $Date:$
  */
 class GcpGeoCodingForm extends JPanel {
 
     private JTextField methodTextField;
     private JTextField rmseLatTextField;
     private JTextField rmseLonTextField;
 
     private JComboBox methodComboBox;
     private JToggleButton attachButton;
     private JTextField warningLabel;
 
     private Product currentProduct;
     private Format rmseNumberFormat;
     private GcpGroupListener currentGcpGroupListener;
 
     public GcpGeoCodingForm() {
         rmseNumberFormat = new RmseNumberFormat();
         currentGcpGroupListener = new GcpGroupListener();
         initComponents();
     }
 
     private void initComponents() {
         TableLayout layout = new TableLayout(2);
         this.setLayout(layout);
         layout.setTableAnchor(TableLayout.Anchor.WEST);
         layout.setTableWeightY(1.0);
         layout.setTableFill(TableLayout.Fill.BOTH);
         layout.setTablePadding(2, 2);
         layout.setColumnWeightX(0, 0.5);
         layout.setColumnWeightX(1, 0.5);
 
         add(createInfoPanel());
         add(createAttachDetachPanel());
 
         updateUIState();
     }
 
     private JPanel createInfoPanel() {
         TableLayout layout = new TableLayout(2);
         layout.setTablePadding(2, 4);
         layout.setColumnWeightX(0, 0.0);
         layout.setColumnWeightX(1, 1.0);
         layout.setTableAnchor(TableLayout.Anchor.WEST);
         layout.setTableFill(TableLayout.Fill.BOTH);
 
         JPanel panel = new JPanel(layout);
         panel.setBorder(BorderFactory.createTitledBorder("Current GCP Geo-Coding"));
         panel.add(new JLabel("Method:"));
         methodTextField = new JTextField();
         setComponentName(methodTextField, "methodTextField");
         methodTextField.setEditable(false);
         methodTextField.setHorizontalAlignment(JLabel.TRAILING);
         panel.add(methodTextField);
         rmseLatTextField = new JTextField();
         setComponentName(rmseLatTextField, "rmseLatTextField");
         rmseLatTextField.setEditable(false);
         rmseLatTextField.setHorizontalAlignment(JLabel.TRAILING);
         panel.add(new JLabel("RMSE Lat:"));
         panel.add(rmseLatTextField);
 
         rmseLonTextField = new JTextField();
         setComponentName(rmseLonTextField, "rmseLonTextField");
         rmseLonTextField.setEditable(false);
         rmseLonTextField.setHorizontalAlignment(JLabel.TRAILING);
         panel.add(new JLabel("RMSE Lon:"));
         panel.add(rmseLonTextField);
         return panel;
     }
 
     private JPanel createAttachDetachPanel() {
         methodComboBox = new JComboBox(GcpGeoCoding.Method.values());
         setComponentName(methodComboBox, "methodComboBox");
         methodComboBox.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 updateUIState();
             }
         });
         attachButton = new JToggleButton();
         setComponentName(attachButton, "attachButton");
         attachButton.setName("attachButton");
 
         AbstractAction applyAction = new AbstractAction() {
             public void actionPerformed(ActionEvent e) {
 
                 if (!(currentProduct.getGeoCoding() instanceof GcpGeoCoding)) {
                     attachGeoCoding(currentProduct);
                 } else {
                     detachGeoCoding(currentProduct);
                 }
             }
         };
 
         attachButton.setAction(applyAction);
         attachButton.setHideActionText(true);
         warningLabel = new JTextField();
         warningLabel.setEditable(false);
 
         TableLayout layout = new TableLayout(2);
         layout.setTablePadding(2, 4);
         layout.setColumnWeightX(0, 0.0);
         layout.setColumnWeightX(1, 1.0);
         layout.setTableAnchor(TableLayout.Anchor.WEST);
         layout.setTableFill(TableLayout.Fill.BOTH);
         layout.setCellColspan(2, 0, 2);
         layout.setCellFill(2, 0, TableLayout.Fill.VERTICAL);
         layout.setCellAnchor(2, 0, TableLayout.Anchor.CENTER);
 
         JPanel panel = new JPanel(layout);
         panel.setBorder(BorderFactory.createTitledBorder("Attach / Detach GCP Geo-Coding"));
         panel.add(new JLabel("Method:"));
         panel.add(methodComboBox);
         panel.add(new JLabel("Status:"));
         panel.add(warningLabel);
         panel.add(attachButton);
 
         return panel;
     }
 
     void updateUIState() {
         if (currentProduct != null && currentProduct.getGeoCoding() instanceof GcpGeoCoding) {
             final GcpGeoCoding gcpGeoCoding = (GcpGeoCoding) currentProduct.getGeoCoding();
 
             rmseLatTextField.setText(rmseNumberFormat.format(gcpGeoCoding.getRmseLat()));
             rmseLonTextField.setText(rmseNumberFormat.format(gcpGeoCoding.getRmseLon()));
             methodTextField.setText(gcpGeoCoding.getMethod().getName());
             methodComboBox.setSelectedItem(gcpGeoCoding.getMethod());
 
             methodComboBox.setEnabled(false);
             attachButton.setText("Detach");
             attachButton.setSelected(true);
             attachButton.setEnabled(true);
             warningLabel.setText("GCP geo-coding attached");
             warningLabel.setForeground(Color.BLACK);
         } else {
             methodComboBox.setEnabled(true);
             methodTextField.setText("Not available");
             rmseLatTextField.setText(rmseNumberFormat.format(Double.NaN));
             rmseLonTextField.setText(rmseNumberFormat.format(Double.NaN));
             attachButton.setText("Attach");
             attachButton.setSelected(false);
             updateAttachButtonAndStatus();
         }
     }
 
     private void updateAttachButtonAndStatus() {
         final GcpGeoCoding.Method method = (GcpGeoCoding.Method) methodComboBox.getSelectedItem();
         if (currentProduct != null && currentProduct.getGcpGroup().getNodeCount() >= method.getTermCountP()) {
             attachButton.setEnabled(true);
             warningLabel.setText("OK, enough GCP's for selected method");
             warningLabel.setForeground(Color.GREEN.darker());
         } else {
             attachButton.setEnabled(false);
             warningLabel.setText("Not enough GCP's for selected method");
             warningLabel.setForeground(Color.RED.darker());
         }
     }
 
     private void detachGeoCoding(Product product) {
         if (product.getGeoCoding() instanceof GcpGeoCoding) {
             GeoCoding gc = ((GcpGeoCoding) product.getGeoCoding()).getOriginalGeoCoding();
             product.setGeoCoding(gc);
         }
         updateUIState();
     }
 
     private void attachGeoCoding(final Product product) {
         final GcpGeoCoding.Method method = (GcpGeoCoding.Method) methodComboBox.getSelectedItem();
         final ProductNodeGroup<Pin> gcpGroup = product.getGcpGroup();
         final Pin[] gcps = gcpGroup.toArray(new Pin[0]);
         final GeoCoding geoCoding = product.getGeoCoding();
         final Datum datum;
         if (geoCoding == null) {
             datum = Datum.WGS_84;
         } else {
             datum = geoCoding.getDatum();
         }
 
         SwingWorker sw = new SwingWorker<GcpGeoCoding, GcpGeoCoding>() {
             protected GcpGeoCoding doInBackground() throws Exception {
                 GcpGeoCoding gcpGeoCoding = new GcpGeoCoding(method, gcps,
                                                              product.getSceneRasterWidth(),
                                                              product.getSceneRasterHeight(),
                                                              datum);
                 gcpGeoCoding.setOriginalGeoCoding(product.getGeoCoding());
                 return gcpGeoCoding;
             }
 
             @Override
             protected void done() {
                 final GcpGeoCoding gcpGeoCoding;
                 try {
                     gcpGeoCoding = get();
                     product.setGeoCoding(gcpGeoCoding);
                     updateUIState();
                 } catch (InterruptedException e) {
                     Debug.trace(e);
                 } catch (ExecutionException e) {
                     Debug.trace(e.getCause());
                 }
             }
         };
         sw.execute();
     }
 
     public void setProduct(Product product) {
         if (product == currentProduct) {
             return;
         }
         if(currentProduct != null) {
             currentProduct.removeProductNodeListener(currentGcpGroupListener);
         }
         currentProduct = product;
         if(currentProduct != null) {
             currentProduct.addProductNodeListener(currentGcpGroupListener);
         }
 
     }
 
 
     private void setComponentName(JComponent component, String name) {
         component.setName(getClass().getName() + name);
     }
 
     private static class RmseNumberFormat extends NumberFormat {
 
         DecimalFormat format = new DecimalFormat("0.0####");
 
         public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
             if (Double.isNaN(number)) {
                 return toAppendTo.append("Not available");
             } else {
                 return format.format(number, toAppendTo, pos);
             }
         }
 
         public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
             return format.format(number, toAppendTo, pos);
         }
 
         public Number parse(String source, ParsePosition parsePosition) {
             return format.parse(source, parsePosition);
         }
     }
 
     private class GcpGroupListener implements ProductNodeListener {
 
         public void nodeChanged(ProductNodeEvent event) {
             // exclude geo-coding changes to prevent recursion
            if (!Product.PROPERTY_NAME_GEOCODING.equals(event.getPropertyName()) &&
                !ProductNode.PROPERTY_NAME_SELECTED.equals(event.getPropertyName())) {
                 updateGcpGeoCoding();
             }
         }
 
         public void nodeDataChanged(ProductNodeEvent event) {
             updateGcpGeoCoding();
         }
 
         public void nodeAdded(ProductNodeEvent event) {
             updateGcpGeoCoding();
         }
 
         public void nodeRemoved(ProductNodeEvent event) {
             updateGcpGeoCoding();
         }
 
         private void updateGcpGeoCoding() {
             GeoCoding geoCoding = currentProduct.getGeoCoding();
             if (geoCoding instanceof GcpGeoCoding) {
                 GcpGeoCoding gcpGeoCoding = ((GcpGeoCoding) geoCoding);
                 if(currentProduct.getGcpGroup().getNodeCount() < gcpGeoCoding.getMethod().getTermCountP()){
                     detachGeoCoding(currentProduct);
                 }else {
                     Pin[] gcps = currentProduct.getGcpGroup().toArray(new Pin[0]);
                     GcpGeoCoding newGcpGeoCoding = new GcpGeoCoding(gcpGeoCoding.getMethod(),
                                                                     gcps,
                                                                     currentProduct.getSceneRasterWidth(),
                                                                     currentProduct.getSceneRasterHeight(),
                                                                     gcpGeoCoding.getDatum());
                     newGcpGeoCoding.setOriginalGeoCoding(gcpGeoCoding.getOriginalGeoCoding());
                     gcpGeoCoding.setGcps(gcps);
                     currentProduct.setGeoCoding(newGcpGeoCoding);
                     updateUIState();
                 }
             }
         }
     }
 }
