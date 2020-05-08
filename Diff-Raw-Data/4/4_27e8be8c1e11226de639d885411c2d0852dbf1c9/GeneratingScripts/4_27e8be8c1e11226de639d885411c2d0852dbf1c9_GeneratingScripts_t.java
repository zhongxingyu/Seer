 /**
  * 
  */
 package no.whg.mini;
 
 import java.util.Vector;
 
 
 /**
  * @author Peer Andreas Stange
  *
  */
 public class GeneratingScripts {
 	
 	private Vector<TableData> generatingVector;
 	public String longAssString;
 	
 	public GeneratingScripts()
 	{
 		
 	}
 	
 	public GeneratingScripts(Vector<TableData> genVec)
 	{
 		this.generatingVector = genVec;
 		longAssString = new String("import javax.swing.*; \n import java.awt.*; \n public class"  /*sett inn navn fra filnavn*/
 		+ "extends JPanel{");
 	}
 	
	public String generate()
 	{
 		for(int i = 0; i < generatingVector.size(); i++)
 		{
 			TableData currentRow = generatingVector.elementAt(i);
 			String currentType = currentRow.getType();
 			String varName = currentRow.getVarName();
 			String text = currentRow.getText();
 		
 			
 			if(currentType == "JLabel")
 			{
 				longAssString += "JLabel " + varName + " = new JLabel(\"" + text +"\");\n";  
 			}
 			else if(currentType == "JButton")
 			{
 				longAssString += "JButton " + varName + " = new JButton(" + text + ");\n";
 			}
 			else if(currentType == "JTextField")
 			{
 				longAssString += "JTextField " + varName + " = new JTextField(\"" + text + "\");\n";
 			}
 			else if(currentType == "JTextArea")
 			{
 				longAssString += "JTextArea " + varName + " = new JTextArea(\"" + text + "\");\n";
 			}
 			else if(currentType == "JCheckBox")
 			{
 				longAssString += "JCheckBox " + varName + " = new JCheckBox(\"" + text + "\");\n";
 			}
 			else if(currentType == "JList")
 			{
 				longAssString += "JList " + varName + " = new JList(\"" + text + "\");\n";
  			}
 			else if(currentType == "JComboBox")
 			{
 				longAssString += "JComboBox" + varName + " = new JComboBox(\"" + text + "\");\n";
 			}
 			else if(currentType == "JSpinnerList")
 			{
 				longAssString += "JSpinnerList " + varName + " = new JSpinnerList(\"" + text + "\");\n";
 			}
 			else if(currentType == "JSpinnerNumber")
 			{
 				longAssString += "JSpinnerNumber " + varName + " = new JSpinnerNumber(\"" + text + "\");\n";
 			}
 			
 			
 		}
 		
 		longAssString += "public " + /*insert class name from filename*/ "(){\n GridBagLayout layout  = new GridBagLayout();\n"
 				+ "GridBagConstraints gbc = new  GridBagConstraints();\n"
 				+ "setLayout(layout);\n";
 		
 		for(int j = 0; j < generatingVector.size(); j++)
 		{
 			TableData currentRow = generatingVector.elementAt(j);
 			String name = currentRow.getVarName();
 			String row = currentRow.getRow();
 			String column = currentRow.getColumn();
 			String rows = currentRow.getRows();
 			String columns = currentRow.getColumns();
 			String fill = currentRow.getFill();
 			String anchor = currentRow.getAnchor();
 			
 			longAssString += "gbc.gridx = " + row + ";\n";
 			longAssString += "gbc.gridy = " + column + ";\n";
 			longAssString += "gbc.gridwidth = " + rows + ";\n";
 			longAssString += "gbc.gridheight = " + columns + ";\n";
 			longAssString += "gbc.anchor = jawa.awt.GridBagConstraints." + anchor + ";\n";
 			longAssString += "gbc.fill = jawa.awt.GridBagConstraints." + fill + ";\n";
 			longAssString += "layout.SetConstraints(" + name + ", gbc);\n";
 			longAssString += "add(" + name + ");\n";
 			
 		}
 		
 		longAssString += "}}";
		return longAssString;
 	}
 }
