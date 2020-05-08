 package org.zaproxy.zap.extension.saml;
 
 import org.parosproxy.paros.view.View;
 
 import javax.swing.*;
 import javax.swing.border.EmptyBorder;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 public class AddNewAttribute extends JDialog {
 
 	private final JPanel contentPanel = new JPanel();
     private JComboBox comboBoxAttribSelect;
    private JTextArea textAreaAttribValues;
 
 //	/**
 //	 * Launch the application.
 //	 */
 //	public static void main(String[] args) {
 //		try {
 //			AddNewAttribute dialog = new AddNewAttribute();
 //			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 //			dialog.setVisible(true);
 //		} catch (Exception e) {
 //			e.printStackTrace();
 //		}
 //	}
 
 	/**
 	 * Create the dialog.
 	 */
 	public AddNewAttribute(final DesiredAttributeChangeListener listener) {
         setTitle("Add New Attribute");
 		setBounds(100, 100, 450, 300);
 		getContentPane().setLayout(new BorderLayout());
 		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
 		getContentPane().add(contentPanel, BorderLayout.CENTER);
 		contentPanel.setLayout(new BorderLayout(0, 0));
 		{
 			JPanel attribNamePanel = new JPanel();
 			FlowLayout flowLayout = (FlowLayout) attribNamePanel.getLayout();
 			flowLayout.setHgap(0);
 			flowLayout.setAlignment(FlowLayout.LEFT);
 			contentPanel.add(attribNamePanel, BorderLayout.NORTH);
 			{
                 attribNamePanel.setBorder(BorderFactory.createTitledBorder("Attribute Name"));
 				comboBoxAttribSelect = new JComboBox();
                 for (String attribute : SAMLUtils.getSAMLAttributes()) {
                     if(!listener.getDesiredAttributes().contains(attribute)){
                         comboBoxAttribSelect.addItem(SAMLUtils.getAttributeViewValue(attribute));
                     }
                 }
                 comboBoxAttribSelect.setMaximumRowCount(5);
 				attribNamePanel.add(comboBoxAttribSelect);
 			}
 		}
 		{
 			JPanel attribValuesPanel = new JPanel();
 			contentPanel.add(attribValuesPanel, BorderLayout.CENTER);
 			attribValuesPanel.setLayout(new BorderLayout(5, 5));
 			{
 				JLabel lblAttributeValues = new JLabel("Attribute Values");
 				lblAttributeValues.setHorizontalAlignment(SwingConstants.LEFT);
 				attribValuesPanel.add(lblAttributeValues, BorderLayout.NORTH);
 			}
 			{
 				JScrollPane scrollPaneAttribValues = new JScrollPane();
 				attribValuesPanel.add(scrollPaneAttribValues, BorderLayout.CENTER);
 				{
 					textAreaAttribValues = new JTextArea();
                     scrollPaneAttribValues.setViewportView(textAreaAttribValues);
 				}
 			}
 		}
 		{
 			final JPanel buttonPane = new JPanel();
 			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
 			getContentPane().add(buttonPane, BorderLayout.SOUTH);
 			{
 				final JButton okButton = new JButton("OK");
 				okButton.addActionListener(new ActionListener() {
                     @Override
                     public void actionPerformed(ActionEvent e) {
                         if(comboBoxAttribSelect.getSelectedItem()==null){
                             View.getSingleton().showWarningDialog("No Attribute selected, " +
                                     "please select one from combo box");
                             return;
                         }
                         String[] values = textAreaAttribValues.getText().split("\n");
                         if(values.length==0){
                             View.getSingleton().showWarningDialog("No values given, " +
                                     "please provide values, one per line");
                             return;
                         }
                         StringBuilder stringBuilder = new StringBuilder();
                         for (int i = 0; i < values.length; i++) {
                             if(i!=0){
                                 stringBuilder.append(",");
                             }
                             stringBuilder.append(values[i]);
                         }
                         String attribute = comboBoxAttribSelect.getSelectedItem().toString();
                         for (String s : SAMLUtils.getSAMLAttributes()) {
                             if(SAMLUtils.getAttributeViewValue(s).equals(attribute)){
                                 listener.onAddDesiredAttribute(s,stringBuilder.toString());
                                 break;
                             }
                         }
                         AddNewAttribute.this.setVisible(false);
                     }
                 });
 				buttonPane.add(okButton);
 				getRootPane().setDefaultButton(okButton);
 			}
 			{
 				JButton cancelButton = new JButton("Cancel");
 				cancelButton.addActionListener(new ActionListener() {
                     @Override
                     public void actionPerformed(ActionEvent e) {
                         AddNewAttribute.this.setVisible(false);
                     }
                 });
 				buttonPane.add(cancelButton);
 			}
 		}
 	}
 
     public JComboBox getComboBoxAttribSelect() {
         return comboBoxAttribSelect;
     }
 
     public JTextArea getTextAreaAttribValues() {
         return textAreaAttribValues;
     }
 
 
 }
