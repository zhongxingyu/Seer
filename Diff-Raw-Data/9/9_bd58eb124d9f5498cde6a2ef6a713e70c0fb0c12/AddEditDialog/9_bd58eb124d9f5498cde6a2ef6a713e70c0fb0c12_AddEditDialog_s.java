 package steam.dbexplorer.view;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JDialog;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import steam.dbexplorer.controller.ExplorerController;
 import steam.dbexplorer.dbobject.DBReference;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.Set;
  
 class AddEditDialog extends JDialog {
     private JTextField textField;
     private String currentTable;
     private String tableDbString;
  
     private String createStr = "Create";
     private String cancelStr = "Cancel";
     private LinkedHashMap<String, JTextField> inputs = new LinkedHashMap<String, JTextField>();
     
     public AddEditDialog(JFrame parent, final ResultsTab motherFrame, String tableName) {
     	super(parent, true);
     	JPanel main = new JPanel();
     	main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
     	
     	currentTable = tableName;
     	tableDbString = tableName;
     	tableDbString = tableDbString.substring(0, tableDbString.length()-1); //remove s
     	tableDbString = tableDbString.replace(" ", ""); //remove spaces 
 		
 		
     	String[] attr = DBReference.displayNames.get(tableDbString);
     	for(int i=0;i<attr.length;i++) {
     		JPanel p = new JPanel();
     		JLabel label = new JLabel(attr[i]);
     		JTextField input = new JTextField(15);
     		p.add(label);
     		p.add(input);
     		p.setMinimumSize(new Dimension(400,60));
     		main.add(p);
     		inputs.put(attr[i], input);
     	}
         
     	
     	JPanel options = new JPanel();
     	JButton create = new JButton(createStr);
     	create.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 				String[] keys = new String[inputs.size()];
 				String[] results = new String[inputs.size()];
 				String[] displayResults = new String[inputs.size()];
 				keys = inputs.keySet().toArray(keys);
 				for(int i=0;i<keys.length;i++) {
 					String attrName = keys[i];
 					String val = inputs.get(attrName).getText();
 					String tmp = val;
 					if(val.length() == 0) {
 						val = null;
 					} else if("string".equals(ExplorerController.getAttrType(attrName)) && val != null ) {
						//tmp = "'" + val + "'";
 						val = "'" + val + "'";
 					}
 					results[i] = val;
 					//int idx = getIndex(attrName);
 					//displayResults[idx] = tmp;
 					//results[idx] = val;
 				}
 				
				motherFrame.addElemToTable(currentTable,results);
 				//motherFrame.addElemToTable(currentTable,displayResults);
 				ExplorerController.createEntry(currentTable, results);
 				AddEditDialog.this.dispose();
 			}
 		});
     	JButton cancel = new JButton(cancelStr);
     	cancel.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 				AddEditDialog.this.dispose();
 			}
 		});
     	options.add(create);
     	options.add(cancel);
     	main.add(options);
     	main.setMinimumSize(new Dimension(300,250));
     	this.add(main);
     	this.setMinimumSize(new Dimension(300,250));
     	this.setVisible(true);
     }
     
     private int getIndex(String attrName) {
     	String[] attr = DBReference.displayNames.get(tableDbString);
     	for(int i=0;i<attr.length;i++) {
     		if(attr[i].equals(attrName)) {
     			return i;
     		}
     	}
     	return -1;
     }
 
 }
