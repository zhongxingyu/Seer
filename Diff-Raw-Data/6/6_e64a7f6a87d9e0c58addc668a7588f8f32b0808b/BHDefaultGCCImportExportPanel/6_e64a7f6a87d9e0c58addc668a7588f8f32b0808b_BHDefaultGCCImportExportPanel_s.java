 package org.bh.gui.swing;
 
 import info.clearthought.layout.TableLayout;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JCheckBox;
 import javax.swing.JFileChooser;
 import javax.swing.JPanel;
 import javax.swing.JSeparator;
 import javax.swing.UIManager;
 import javax.swing.border.Border;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 import org.bh.gui.ValidationMethods;
 import org.bh.gui.ViewException;
 import org.bh.platform.PlatformController;
 import org.bh.platform.i18n.BHTranslator;
 
 public class BHDefaultGCCImportExportPanel extends JPanel {
 
 	BHTextField txtPath;
 	JCheckBox open;
 
 	String fileDesc;
 	String fileExt;
 	boolean export = false;
 	private BHButton btnExport;
 	private BHDescriptionTextArea lblDescr;
 	
 
 	public BHDefaultGCCImportExportPanel(String fileDesc, String fileExt, boolean export) {
 		this.fileDesc = fileDesc;
 		this.fileExt = fileExt;
 		this.export = export;
 		
 		setLayout(new BorderLayout());
 
 		JPanel descrPanel = createDescriptionPanel();
 		JPanel actionPanel = createActionArea();
 
 		add(descrPanel, BorderLayout.NORTH);
 		add(new SelectionArea(), BorderLayout.CENTER);
 		add(actionPanel, BorderLayout.SOUTH);
 	}
 
 	private JPanel createActionArea() {
 		// Panel on which the Buttons will be placed
 		JPanel actionPanel = new JPanel(new BorderLayout());
 		// Add seperator for optical seperation
 		actionPanel.add(new JSeparator(), BorderLayout.NORTH);
 
 		// Button panel for adding an empty border as margin
 		JPanel buttonPanel = new JPanel();
 		buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
 		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
 		buttonPanel.add(Box.createHorizontalGlue());
 
 		// Cancel
 		BHButton btnCancel = new BHButton("Bcancel");
 		
 		// Back 
 		BHButton btnBack = new BHButton("Bback");
 
 		// Start export
 		btnExport = null;
 		if (export)
 			btnExport = new BHButton("Mexport");
 		else
 			btnExport = new BHButton("Mimport");		
 		btnExport.setAlignmentX(Component.RIGHT_ALIGNMENT);
 		btnExport.setEnabled(false);
 
 		buttonPanel.add(btnCancel);
 		buttonPanel.add(btnBack);
 		buttonPanel.add(btnExport);
 
 		actionPanel.add(buttonPanel, BorderLayout.CENTER);
 		return actionPanel;
 	}
 
 	private JPanel createDescriptionPanel() {
 		// Create description section
 		JPanel panDescr = new JPanel();
 		panDescr.setForeground(Color.white);
 		panDescr.setLayout(new BorderLayout());
 
 		// Text area with description
 		lblDescr = null;
 		if (export)
 			lblDescr = new BHDescriptionTextArea("DGCCXMLExportDescription");
 		else
 			lblDescr = new BHDescriptionTextArea("DGCCXMLImportDescription");
 		lblDescr.setFocusable(false);
 		lblDescr.setToolTipText(null);
 
 		// Create border for that textarea
 		Border marginBorder = BorderFactory.createEmptyBorder(15, 5, 15, 5);
 		Border whiteBorder = BorderFactory.createLineBorder(Color.white);
 		Border grayBorder = BorderFactory.createLineBorder(UIManager
 				.getColor("controlDkShadow"));
 		Border outerBorder = BorderFactory.createCompoundBorder(whiteBorder,
 				grayBorder);
 
 		// Set border
 		lblDescr.setBorder(BorderFactory.createCompoundBorder(outerBorder,
 				marginBorder));
 
 		panDescr.add(lblDescr, BorderLayout.CENTER);
 		return panDescr;
 	}
 
 	public String getFilePath() {
 		return txtPath.getText();
 	}
 
 	public boolean openAfterExport() {
 		return open.isSelected();
 	}
 	
 	public void setDescription(String key)
 	{
 		lblDescr.setText(BHTranslator.getInstance().translate(key));
 	}
 
 	public class SelectionArea extends JPanel implements ActionListener {
 		
 		public SelectionArea(){
 			try {
 //				BHDefaultScenarioExportPanelView bhDefScenExpPanView = new BHDefaultScenarioExportPanelView(
 //						this, new ValidationMethods());
 				new BHDefaultScenarioExportPanelView(this, new ValidationMethods());
 				
 				this.setLayout(new BorderLayout());
 				this.setBorder(BorderFactory.createEmptyBorder(20, 15, 15,15));
 
 				// An extra panel for file selection
 				JPanel fileSelectionPanel = new JPanel();
 				fileSelectionPanel.setBorder(BorderFactory.createEmptyBorder(7, 0,
 						0, 0));
 				// 2 columns, 2 rows
 				double size[][] = { { 0.7, 0.3 }, { 0.5, 0.5 } };
 				fileSelectionPanel.setLayout(new TableLayout(size));
 
 				// Small label for instruction
 				BHDescriptionLabel lblselExportPath;
 				if (export)
 					lblselExportPath = new BHDescriptionLabel(
 						"DExportPathSelection");
 				else
 					lblselExportPath = new BHDescriptionLabel(
 						"DImportPathSelection");
 				fileSelectionPanel.add(lblselExportPath, "0,0");
 
 				// Text field which will show the chosen path
 				txtPath = new BHTextField("DTxtExportImportPath", "");
 				txtPath.setEditable(false);
 				fileSelectionPanel.add(txtPath, "0,1");
 
 				// Button to start the file choosing dialog
 				BHButton btnChooseFile = new BHButton("Bbrowse");
 				btnChooseFile.addActionListener(this);
 				fileSelectionPanel.add(btnChooseFile, "1,1");
 
 				this.add(fileSelectionPanel, BorderLayout.NORTH);				
 			} catch (ViewException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			IBHComponent comp = (IBHComponent) e.getSource();
 			if (comp.getKey().equals("Bbrowse")) {
 				JFileChooser fileChooser = new JFileChooser();
 				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
 
 				String strDefDir = PlatformController.preferences.get(
 						"lastExportDirectory", null);
 				if (strDefDir != null) {
 					File defDir = new File(strDefDir);
 					fileChooser.setCurrentDirectory(defDir);
 				}
 
 				fileChooser.setFileFilter(new FileNameExtensionFilter(fileDesc,
 						fileExt));
 				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				int returnVal = fileChooser.showSaveDialog(this);
 
 				if (returnVal == JFileChooser.APPROVE_OPTION) {
 					PlatformController.preferences.put("lastExportDirectory",
 							fileChooser.getSelectedFile().getParent());
 					String filePath = fileChooser.getSelectedFile().getAbsolutePath();
 					if(!filePath.endsWith("." + fileExt)) {
 						filePath = filePath + "." + fileExt;
 					}
 					txtPath.setText(filePath);
 					btnExport.setEnabled(true);
 				}
 			}
 		}
 	}
 }
