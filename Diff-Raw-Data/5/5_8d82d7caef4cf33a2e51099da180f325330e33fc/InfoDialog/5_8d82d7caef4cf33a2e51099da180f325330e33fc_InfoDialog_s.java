 package hms.views;
 
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 
 import java.sql.SQLException;
 
 import net.miginfocom.swing.MigLayout;
 
 import hms.models.AbstractModel;
 
 public class InfoDialog {
 	private JDialog dialog;
 	private AbstractInfoPanel panel;
 	
 	final private JButton saveButton = new JButton("Save");
 	final private JButton cancelButton = new JButton("Cancel");
 	
 	/**
 	 * Creates and shows a new InfoDialog that contains the panel given to it
 	 * and with the title given.
 	 * @param parent The parent component that the dialog is anchored to.
 	 * @param title The title for the dialog.
 	 * @param panel The information panel to show on the dialog.
 	 */
 	public InfoDialog(Window parent, String title, AbstractInfoPanel panel) {
 		this.dialog = new JDialog(parent, title, Dialog.ModalityType.APPLICATION_MODAL);
 		this.panel = panel;
 		
 		initUI();
 		
 		this.dialog.setMinimumSize(new Dimension(400, 0));
 		this.dialog.setResizable(false);
 		this.dialog.pack();
 		this.dialog.setLocationRelativeTo(parent);
 		this.dialog.setVisible(true);
 	}
 	
 	/**
 	 * Intializes the user interface components.
 	 */
 	protected void initUI() {
 		panel.signifyRequiredFields(true);
 		
 		Container contentPane = this.dialog.getContentPane();
 		
 		this.cancelButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				close();
 			}
 		});
 		
 		this.saveButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (panel.validateInformation()) {
 					try {
						panel.getStoredModel().delete();
 						panel.modelFromInformation().create();
 					} catch (SQLException sqle) {}
 					close();
 				}
 			}
 		});
 		
 		contentPane.setLayout(new MigLayout("", "[grow,fill]"));
 		contentPane.add(this.panel, "wrap, growx");
 		contentPane.add(createButtonPanel());
 	}
 
 	/**
 	 * Closes the dialog.
 	 */
 	public void close() {
 		this.dialog.dispose();
 	}
 	
 	/**
 	 * Creates the button panel for the dialog.
 	 * @return the button panel for the dialog.
 	 */
 	protected JPanel createButtonPanel() {
 		JPanel buttonPanel = new JPanel(new MigLayout("nogrid, fillx"));
 		buttonPanel.add(saveButton, "sg, gap push");
 		buttonPanel.add(cancelButton, "sg");
 		return buttonPanel;
 	}
 }
