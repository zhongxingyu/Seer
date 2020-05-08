 package org.esa.beam.chris.ui;
 
 import com.bc.ceres.binding.swing.BindingContext;
 
 import javax.swing.*;
 import javax.swing.border.TitledBorder;
 import java.awt.*;
 
 class AdvancedSettingsPanel extends JPanel {
 
     private javax.swing.JCheckBox slitCorrectionCheckBox;
     private javax.swing.JSpinner smoothingOrderSpinner;
 
     private javax.swing.JSpinner neighborBandCountComboBox;
     private javax.swing.JComboBox neighborhoodComboBox;
 
     public AdvancedSettingsPanel(AdvancedSettingsPresenter presenter) {
         initComponents();
         bindComponents(presenter);
     }
 
     private void bindComponents(AdvancedSettingsPresenter presenter) {
         BindingContext destripingBinding = new BindingContext(presenter.getDestripingValueContainer());
        destripingBinding.bind(slitCorrectionCheckBox, "slitCorrection");
        destripingBinding.bind(smoothingOrderSpinner, "smoothingOrder");
         BindingContext dropoutCorrectionBinding = new BindingContext(presenter.getDropoutCorrectionValueContainer());
        dropoutCorrectionBinding.bind(neighborBandCountComboBox, "neighborBandCount");
        dropoutCorrectionBinding.bind(neighborhoodComboBox, "neighborhoodType");
         neighborhoodComboBox.setRenderer(new DefaultListCellRenderer() {
             @Override
             public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                           boolean cellHasFocus) {
                 Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                 if (component instanceof JLabel) {
                     JLabel label = (JLabel) component;
                     label.setText(value.toString());
                 }
                 return component;
             }
         });
     }
 
     private void initComponents() {
         setLayout(new BorderLayout(5, 5));
 
         slitCorrectionCheckBox = new JCheckBox();
         slitCorrectionCheckBox.setName("slitCorrectionCheckBox");
 
         smoothingOrderSpinner = new JSpinner();
         smoothingOrderSpinner.setName("smoothingOrderSpinner");
         neighborBandCountComboBox = new JSpinner();
         neighborBandCountComboBox.setName("neighborBandCountComboBox");
         JLabel neighborhoodLabel = new JLabel();
         neighborhoodComboBox = new JComboBox();
         neighborhoodComboBox.setName("neighborhoodComboBox");
 
         JPanel advancedSettingsPanel = new JPanel(new GridBagLayout());
         JPanel verticalStripingPanel = new JPanel(new GridBagLayout());
 
         verticalStripingPanel.setBorder(BorderFactory.createTitledBorder(null,
                 "Destriping",
                 TitledBorder.DEFAULT_JUSTIFICATION,
                 TitledBorder.DEFAULT_POSITION,
                 new Font("Tahoma", 0, 11),
                 new Color(0, 70, 213)));
         slitCorrectionCheckBox.setSelected(true);
         slitCorrectionCheckBox.setText("Apply Slit Correction");
         slitCorrectionCheckBox.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
         slitCorrectionCheckBox.setHorizontalTextPosition(SwingConstants.LEFT);
         slitCorrectionCheckBox.setMargin(new Insets(0, 0, 0, 0));
         GridBagConstraints gridBagConstraints = new GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new Insets(0, 3, 0, 10);
         verticalStripingPanel.add(slitCorrectionCheckBox, gridBagConstraints);
 
         gridBagConstraints = new GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new Insets(0, 10, 0, 3);
         verticalStripingPanel.add(new JLabel("Smoothing Order:"), gridBagConstraints);
 
         Dimension preferredSize = smoothingOrderSpinner.getPreferredSize();
         preferredSize.width = 50;
         smoothingOrderSpinner.setPreferredSize(preferredSize);
         gridBagConstraints = new GridBagConstraints();
         gridBagConstraints.gridx = 2;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 0.6;
         gridBagConstraints.insets = new Insets(0, 0, 0, 3);
         verticalStripingPanel.add(smoothingOrderSpinner, gridBagConstraints);
 
         gridBagConstraints = new GridBagConstraints();
         gridBagConstraints.gridx = 3;
         gridBagConstraints.gridy = 0;
         verticalStripingPanel.add(new JLabel("pixels"), gridBagConstraints);
 
         gridBagConstraints = new GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = GridBagConstraints.WEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 0.5;
         advancedSettingsPanel.add(verticalStripingPanel, gridBagConstraints);
 
         JPanel dropoutPanel = new JPanel(new GridBagLayout());
 
         dropoutPanel.setBorder(BorderFactory.createTitledBorder(null, "Dropout Correction",
                 TitledBorder.DEFAULT_JUSTIFICATION,
                 TitledBorder.DEFAULT_POSITION,
                 new Font("Tahoma", 0, 11),
                 new Color(0, 70, 213)));
         gridBagConstraints = new GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new Insets(0, 3, 3, 10);
         dropoutPanel.add(new JLabel("Number of Neighbor Bands:"), gridBagConstraints);
 
         gridBagConstraints = new GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 0.6;
         gridBagConstraints.insets = new Insets(0, 0, 3, 3);
         dropoutPanel.add(neighborBandCountComboBox, gridBagConstraints);
 
         neighborhoodLabel.setText("Neighborhood Type:");
         gridBagConstraints = new GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
         gridBagConstraints.insets = new Insets(0, 3, 0, 10);
         dropoutPanel.add(neighborhoodLabel, gridBagConstraints);
 
         gridBagConstraints = new GridBagConstraints();
         gridBagConstraints.gridx = 1;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
         gridBagConstraints.weightx = 0.6;
         gridBagConstraints.insets = new Insets(0, 0, 0, 3);
         dropoutPanel.add(neighborhoodComboBox, gridBagConstraints);
 
         gridBagConstraints = new GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
         gridBagConstraints.anchor = GridBagConstraints.WEST;
         gridBagConstraints.weightx = 1.0;
         gridBagConstraints.weighty = 0.5;
         advancedSettingsPanel.add(dropoutPanel, gridBagConstraints);
 
         gridBagConstraints = new GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 1;
         gridBagConstraints.fill = GridBagConstraints.BOTH;
         gridBagConstraints.anchor = GridBagConstraints.NORTH;
         gridBagConstraints.weighty = 0.5;
         gridBagConstraints.insets = new Insets(5, 5, 5, 5);
 
         add(advancedSettingsPanel, BorderLayout.CENTER);
     }
 
 }
