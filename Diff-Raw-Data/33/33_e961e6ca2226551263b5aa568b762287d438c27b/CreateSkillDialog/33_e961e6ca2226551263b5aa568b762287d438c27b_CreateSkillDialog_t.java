 package com.xmleditor.gui.dialogs;
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusAdapter;
 import java.awt.event.FocusEvent;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.concurrent.ConcurrentHashMap;
 
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 import javax.swing.SwingConstants;
 
 import javax.swing.WindowConstants;
 import javax.swing.SwingUtilities;
 
 import com.xmleditor.io.XMLWriter;
 
 import skillsplanner.beans.DFOClass;
 import skillsplanner.beans.Skill;
 import skillsplanner.resources.ClassManager;
 import skillsplanner.resources.SkillManager;
 import skillsplanner.utils.ListUtils;
 
 
 /**
 * This code was edited or generated using CloudGarden's Jigloo
 * SWT/Swing GUI Builder, which is free for non-commercial
 * use. If Jigloo is being used commercially (ie, by a corporation,
 * company or business for any purpose whatever) then you
 * should purchase a license for each developer using Jigloo.
 * Please visit www.cloudgarden.com for details.
 * Use of Jigloo implies acceptance of these licensing terms.
 * A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
 * THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
 * LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
 */
 public class CreateSkillDialog extends javax.swing.JFrame implements ActionListener{
 	private JPanel inputPanel;
 	private JTextField nameField;
 	private JLabel spLabel;
 	private JTextField levelsText;
 	private JButton jButton1;
 	private JScrollPane jScrollPane1;
 	private JTextPane jTextArea1;
 	private JCheckBox startLevelCheck;
 	private JTextField initialCostText;
 	private JLabel jLabel1;
 	private JLabel levelLabel;
 	private JTextField spText;
 	private JLabel nameLabel;
 	private JComboBox<DFOClass> baseClass;
 	private JTextField requiredLevelText;
 	
 	public CreateSkillDialog() {
 		super();
 		initGUI();
 		this.setVisible(true);
 	}
 	
 	private void initGUI() {
 		try {
 			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 			{
 				inputPanel = new JPanel();
 				getContentPane().add(inputPanel, BorderLayout.CENTER);
 				inputPanel.setPreferredSize(new java.awt.Dimension(440, 319));
 				inputPanel.setAlignmentX(0.0f);
 				{
 					nameLabel = new JLabel();
 					inputPanel.add(nameLabel);
 					nameLabel.setText("Name");
 					nameLabel.setPreferredSize(new java.awt.Dimension(156, 16));
 					nameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
 				}
 				{
 					nameField = new JTextField();
 					inputPanel.add(nameField);
 					nameField.setPreferredSize(new java.awt.Dimension(198, 22));
 //					nameField.addFocusListener(new FocusAdapter() {
 //						public void focusGained(FocusEvent evt) {
 //							System.out.println("nameField.focusGained, event="+evt);
 //							//TODO add your code for nameField.focusGained
 //						}
 //					});
 				}
 				{
 					spLabel = new JLabel();
 					inputPanel.add(spLabel);
 					spLabel.setText("SPCost");
 					spLabel.setPreferredSize(new java.awt.Dimension(155, 16));
 					spLabel.setHorizontalAlignment(SwingConstants.RIGHT);
 				}
 				{
 					spText = new JTextField();
 					inputPanel.add(spText);
 					spText.setPreferredSize(new java.awt.Dimension(195, 22));
 //					spText.addFocusListener(new FocusAdapter() {
 //						public void focusGained(FocusEvent evt) {
 //							System.out.println("spText.focusGained, event="+evt);
 //							//TODO add your code for spText.focusGained
 //						}
 //					});
 				}
 				{
 					levelLabel = new JLabel();
 					inputPanel.add(levelLabel);
 					levelLabel.setText("Number of Levels");
 					levelLabel.setSize(155, 16);
 					levelLabel.setPreferredSize(new java.awt.Dimension(155, 16));
 					levelLabel.setHorizontalAlignment(SwingConstants.RIGHT);
 				}
 				{
 					levelsText = new JTextField();
 					inputPanel.add(levelsText);
 					levelsText.setPreferredSize(new java.awt.Dimension(195, 22));
 //					levelsText.addFocusListener(new FocusAdapter() {
 //						public void focusGained(FocusEvent evt) {
 //							System.out.println("levelsText.focusGained, event="+evt);
 //							//TODO add your code for levelsText.focusGained
 //						}
 //					});
 				}
 				{
 					jLabel1 = new JLabel();
 					inputPanel.add(jLabel1);
 					jLabel1.setText("Initial Cost");
 					jLabel1.setHorizontalAlignment(SwingConstants.RIGHT);
 					jLabel1.setPreferredSize(new java.awt.Dimension(155, 16));
 					jLabel1.setSize(155, 16);
 				}
 				{
 					initialCostText = new JTextField();
 					inputPanel.add(initialCostText);
 					initialCostText.setPreferredSize(new java.awt.Dimension(195,22));
 //					initialCostText.addFocusListener(new FocusAdapter() {
 //						public void focusGained(FocusEvent evt) {
 //							System.out.println("initialCostText.focusGained, event="+evt);
 //							//TODO add your code for initialCostText.focusGained
 //						}
 //					});
 				}
 				{
 					JLabel label = new JLabel();
 					inputPanel.add(label);
 					label.setText("Minimum Level to Obtain Skill:");
 					label.setHorizontalAlignment(SwingConstants.RIGHT);
 					label.setPreferredSize(new java.awt.Dimension(155, 16));
 					label.setSize(155, 16);
 				}
 				{
 					requiredLevelText = new JTextField();
 					inputPanel.add(requiredLevelText);
 					requiredLevelText.setPreferredSize(new java.awt.Dimension(195,22));
 				}
 				{
 					startLevelCheck = new JCheckBox();
 					inputPanel.add(startLevelCheck);
 					startLevelCheck.setText("Start at level 1");
 					startLevelCheck.setPreferredSize(new java.awt.Dimension(169, 20));
 				}
 				{
 					ConcurrentHashMap<String, DFOClass> map = ClassManager.getInstance().getAllClasses();
 					Collection<DFOClass> collection = ListUtils.sortList(ListUtils.getListFromMap(map));
 					baseClass = new JComboBox(collection.toArray());
 					inputPanel.add(baseClass);
 					
 				}
 				{
 					jButton1 = new JButton();
 					inputPanel.add(jButton1);
 					jButton1.setText("Generate XML");
 					jButton1.addActionListener(this);
 				}
 			}
 			pack();
 			this.setSize(500, 500);
 		} catch (Exception e) {
 		    //add your error handling code here
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		Skill st = new Skill();
 		st.setName(nameField.getText());
 		st.setSpcost(Integer.parseInt(this.spText.getText()));
 		st.setMaxLevel(Integer.parseInt(this.levelsText.getText()));
 		st.setRequiredLevel(Integer.parseInt(requiredLevelText.getText()));
 		st.setTree(getFolderToPutIn());
 		if(this.startLevelCheck.isSelected()){
 			st.setEntrycost(-1);
 		}
 		else{
 			st.setEntrycost(Integer.parseInt(this.initialCostText.getText()));
 		}
 		
 		if(XMLWriter.getInstance().skillToXML(st,getFolderToPutIn())){
 			JOptionPane.showMessageDialog(this, "File created successfully");
 		}
 	}
 	
 	private String getFolderToPutIn(){
 		DFOClass selected = (DFOClass) baseClass.getSelectedItem();
 		
		return selected.getUniqueName();
 	}
 
 }
