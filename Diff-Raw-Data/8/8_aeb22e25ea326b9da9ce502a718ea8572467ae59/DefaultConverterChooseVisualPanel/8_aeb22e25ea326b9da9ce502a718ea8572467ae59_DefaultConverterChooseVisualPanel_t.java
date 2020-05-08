 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.commons.gui.wizard.converter;
 
 import org.apache.log4j.Logger;
 
 import org.openide.util.WeakListeners;
 
 import java.awt.Component;
 import java.awt.EventQueue;
 import java.awt.GridBagConstraints;
 import java.awt.Insets;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 
 import de.cismet.commons.converter.Converter;
 import de.cismet.commons.converter.FormatHint;
 
 /**
  * Basic ConverterChooser that makes use of the FormatHint interface to display appropriate information for a chosen
  * Converter.
  *
  * @author   mscholl
  * @version  1.0
  */
 public class DefaultConverterChooseVisualPanel extends JPanel {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final transient Logger LOG = Logger.getLogger(DefaultConverterChooseVisualPanel.class);
 
     //~ Instance fields --------------------------------------------------------
 
     private final transient AbstractConverterChooseWizardPanel model;
     private final transient ItemListener converterL;
     private final transient PropertyChangeListener propChangeL;
 
     // assure EDT access only
     private transient boolean initialising;
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private final transient javax.swing.JComboBox cboConverterChooser = new javax.swing.JComboBox();
     private final transient javax.swing.JPanel jPanel1 = new javax.swing.JPanel();
     private final transient javax.swing.JLabel lblConverter = new javax.swing.JLabel();
     private final transient javax.swing.JLabel lblFormatDescription = new javax.swing.JLabel();
     private final transient javax.swing.JLabel lblFormatDescriptionValue = new javax.swing.JLabel();
     private final transient javax.swing.JLabel lblFormatExample = new javax.swing.JLabel();
     private final transient javax.swing.JLabel lblFormatExampleValue = new javax.swing.JLabel();
     private final transient javax.swing.JPanel pnlFormatExample = new javax.swing.JPanel();
     // End of variables declaration//GEN-END:variables
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new DefaultConverterChooseVisualPanel object.
      *
      * @param   model  the model for this component
      *
      * @throws  IllegalArgumentException  if the model is <code>null</code>
      */
     public DefaultConverterChooseVisualPanel(final AbstractConverterChooseWizardPanel model) {
         if (model == null) {
             throw new IllegalArgumentException("model must not be null"); // NOI18N
         }
         this.model = model;
 
         this.converterL = new ConverterItemListener();
         this.propChangeL = new ModelPropertyChangeL();
         this.initialising = false;
 
         initComponents();
 
         this.setName(model.getText("DefaultConverterChooseVisualPanel.name")); // NOI18N
 
         cboConverterChooser.addItemListener(WeakListeners.create(ItemListener.class, converterL, cboConverterChooser));
         cboConverterChooser.setRenderer(new ConverterRenderer());
 
         model.addPropertyChangeListener(WeakListeners.propertyChange(propChangeL, model));
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * Getter for the underlying model.
      *
      * @return  the underlying model
      */
     public AbstractConverterChooseWizardPanel getModel() {
         return model;
     }
 
     /**
      * DOCUMENT ME!
      */
     private void init() {
         assert EventQueue.isDispatchThread() : "only EDT allowed"; // NOI18N
 
        // we set the initialising flag so that listeners do not perform their usual actions
         initialising = true;
 
         final Converter selectedConv = model.getConverter();
 
         this.cboConverterChooser.removeAllItems();
 
         final List<? extends Converter> converters = model.getAvailableConverters();
         Collections.sort(converters, new Comparator<Converter>() {
 
                 @Override
                 public int compare(final Converter o1, final Converter o2) {
                     if ((o1 instanceof FormatHint) && (o2 instanceof FormatHint)) {
                         return ((FormatHint)o1).getFormatDisplayName()
                                     .compareTo(((FormatHint)o2).getFormatDisplayName());
                     } else {
                         return o1.hashCode() - o2.hashCode();
                     }
                 }
             });
 
         for (final Converter converter : converters) {
             this.cboConverterChooser.addItem(converter);
         }
 
        // initialising is unset as we want the listeners to resume normal operations
        initialising = false;

         if (selectedConv == null) {
             this.cboConverterChooser.setSelectedIndex(0);
         } else {
             this.cboConverterChooser.setSelectedItem(selectedConv);
         }
     }
 
     /**
      * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
      * content of this method is always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         java.awt.GridBagConstraints gridBagConstraints;
 
         setLayout(new java.awt.BorderLayout());
 
         jPanel1.setLayout(new java.awt.GridBagLayout());
 
         cboConverterChooser.setMinimumSize(new java.awt.Dimension(300, 27));
         cboConverterChooser.setPreferredSize(new java.awt.Dimension(300, 27));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         jPanel1.add(cboConverterChooser, gridBagConstraints);
 
         lblConverter.setText(model.getText("DefaultConverterChooseVisualPanel.lblConverter.text"));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         jPanel1.add(lblConverter, gridBagConstraints);
 
         lblFormatDescription.setText(model.getText("DefaultConverterChooseVisualPanel.lblFormatDescription.text"));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         jPanel1.add(lblFormatDescription, gridBagConstraints);
 
         lblFormatDescriptionValue.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         lblFormatDescriptionValue.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
         lblFormatDescriptionValue.setText(model.getText(
                 "DefaultConverterChooseVisualPanel.lblFormatDescriptionValue.text"));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 2;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 10, 5);
         jPanel1.add(lblFormatDescriptionValue, gridBagConstraints);
 
         lblFormatExample.setText(model.getText("DefaultConverterChooseVisualPanel.lblFormatExample.text"));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 3;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
         jPanel1.add(lblFormatExample, gridBagConstraints);
 
         pnlFormatExample.setBackground(new java.awt.Color(255, 255, 255));
         pnlFormatExample.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0), 2));
         pnlFormatExample.setLayout(new java.awt.GridBagLayout());
 
         lblFormatExampleValue.setFont(new java.awt.Font("Tahoma", 0, 10)); // NOI18N
         lblFormatExampleValue.setText(model.getText("DefaultConverterChooseVisualPanel.lblFormatExampleValue.text"));
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         pnlFormatExample.add(lblFormatExampleValue, gridBagConstraints);
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 4;
         gridBagConstraints.gridwidth = 2;
         gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.weighty = 1.0;
         gridBagConstraints.insets = new java.awt.Insets(10, 10, 10, 10);
         jPanel1.add(pnlFormatExample, gridBagConstraints);
 
         add(jPanel1, java.awt.BorderLayout.CENTER);
     } // </editor-fold>//GEN-END:initComponents
 
     /**
      * keep in sync with initComponents.
      */
     private void applyL10N() {
         this.setName(model.getText("DefaultConverterChooseVisualPanel.name"));                                        // NOI18N
         lblFormatDescriptionValue.setText(model.getText(
                 "DefaultConverterChooseVisualPanel.lblFormatDescriptionValue.text"));                                 // NOI18N
         lblFormatExampleValue.setText(model.getText("DefaultConverterChooseVisualPanel.lblFormatExampleValue.text")); // NOI18N
         lblConverter.setText(model.getText("DefaultConverterChooseVisualPanel.lblConverter.text"));                   // NOI18N
         lblFormatDescription.setText(model.getText("DefaultConverterChooseVisualPanel.lblFormatDescription.text"));   // NOI18N
         lblFormatDescriptionValue.setText(model.getText(
                 "DefaultConverterChooseVisualPanel.lblFormatDescriptionValue.text"));                                 // NOI18N
         lblFormatExample.setText(model.getText("DefaultConverterChooseVisualPanel.lblFormatExample.text"));           // NOI18N
         lblFormatExampleValue.setText(model.getText("DefaultConverterChooseVisualPanel.lblFormatExampleValue.text")); // NOI18N
     }
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private final class ModelPropertyChangeL implements PropertyChangeListener {
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public void propertyChange(final PropertyChangeEvent evt) {
             final String prop = evt.getPropertyName();
             if ("converter".equals(prop)) {             // NOI18N
                 cboConverterChooser.setSelectedItem(evt.getNewValue());
             } else if ("resourceBundle".equals(prop)) { // NOI18N
                 applyL10N();
             } else if (AbstractConverterChooseWizardPanel.PROPERTY_INIT.equals(prop)) {
                 init();
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private final class ConverterRenderer extends DefaultListCellRenderer {
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public Component getListCellRendererComponent(final JList list,
                 final Object value,
                 final int index,
                 final boolean isSelected,
                 final boolean cellHasFocus) {
             final Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
 
             if ((c instanceof JLabel) && (value instanceof FormatHint)) {
                 final JLabel label = (JLabel)c;
                 final FormatHint formatHint = (FormatHint)value;
 
                 label.setText(formatHint.getFormatDisplayName());
             }
 
             return c;
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private final class ConverterItemListener implements ItemListener {
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public void itemStateChanged(final ItemEvent e) {
             if (initialising) {
                 // we don't want to do anything while the component is initialising, ignoring event
                 return;
             }
 
             if (ItemEvent.SELECTED == e.getStateChange()) {
                 @SuppressWarnings("unchecked")
                 final Converter converter = (Converter)e.getItem();
                 if (converter instanceof FormatHint) {
                     final FormatHint hint = (FormatHint)converter;
 
                     if (hint.getFormatHtmlDescription() == null) {
                         lblFormatDescriptionValue.setText(hint.getFormatDescription());
                     } else {
                         lblFormatDescriptionValue.setText(hint.getFormatHtmlDescription());
                     }
 
                     final Object formatExample = hint.getFormatExample();
                     if (formatExample instanceof String) {
                         lblFormatExampleValue.setText((String)formatExample);
                     } else if (formatExample instanceof Component) {
                         pnlFormatExample.removeAll();
                         pnlFormatExample.add((Component)formatExample);
                     } else {
                         LOG.warn("unsupported example format: " + formatExample); // NOI18N
                         resetExample();
                     }
                 } else {
                     lblFormatDescriptionValue.setText(model.getText(
                             "DefaultConverterChooseVisualPanel.lblFormatDescriptionValue.text")); // NOI18N
 
                     resetExample();
                 }
 
                 model.setConverter(converter);
             }
         }
 
         /**
          * DOCUMENT ME!
          */
         private void resetExample() {
             lblFormatExampleValue.setText(model.getText(
                     "DefaultConverterChooseVisualPanel.lblFormatExampleValue.text")); // NOI18N
             pnlFormatExample.removeAll();
 
             final GridBagConstraints constraints = new GridBagConstraints();
             constraints.gridx = 0;
             constraints.gridy = 0;
             constraints.fill = GridBagConstraints.HORIZONTAL;
             constraints.anchor = GridBagConstraints.NORTHWEST;
             constraints.weightx = 1.0;
             constraints.weighty = 1.0;
             constraints.insets = new Insets(10, 10, 10, 10);
 
             pnlFormatExample.add(lblFormatExampleValue, constraints);
         }
     }
 }
