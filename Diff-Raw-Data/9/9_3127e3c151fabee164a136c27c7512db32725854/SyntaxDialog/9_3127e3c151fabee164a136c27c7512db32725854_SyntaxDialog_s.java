 package gui;
 
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.*;
 import javax.swing.*;
 
 import javax.swing.table.AbstractTableModel;
 
 import wombat.Options;
 
 public final class SyntaxDialog {
 	public final static String DEFAULT_SYNTAX =
 		"define		2 \n" +
 		"lambda		2 \n" +
 		"if			4 \n" +
 		"cond		2 \n" +
 		"and		5 \n" +
 		"or			4 \n" +
 		"+			2 \n" +
 		"-			2 \n" +
 		"*			2 \n" +
 		"/			2 \n" +
 		"add1		6 \n" +
 		"sub1		6 \n" +
 		"list		6 \n" +
 		"cons		6 \n" +
 		"car		5 \n" +
 		"cdr		5 \n" +
 		"let		2 \n" +
 		"let*		2 \n" +
 		"letrec		2 \n" +
 		"quote		2 \n" +
 		"case		2 \n" +
 		"do			2 \n" +
 		"color\ncolor?\ncolor-equal?\ncolor-ref" +
 		"image?\nimage-equal?\nimage-rows\nimage-cols\nimage-ref\nimage-set!\nread-image\nwrite-image\ndraw-image\nimage-map\nmake-image\ndraw-image-file" +
 		"tree\ntree?\nleaf\nleaf?\nempty-tree\nempty-tree?\nleft-subtree\nright-subtree\nroot-value\ndraw-tree";
 	
 	static JDialog dialog;
 	static List<String> syntaxNames = new ArrayList<String>();
 	
 	/**
 	 * Create a new syntax frame.
 	 */
 	private SyntaxDialog() {
 		dialog = new JDialog();
 		dialog.setTitle("Syntax options");
 		dialog.setSize(400, 400);
 		dialog.setLayout(new BorderLayout());
 		dialog.setLocationByPlatform(true);
 		dialog.setModal(true);
 		dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
 		
 		JTable table = new JTable(new AbstractTableModel() {
 			private static final long serialVersionUID = -8298784246564975586L;
 
 			@Override
 			public Object getValueAt(int row, int col) {
 				if (row == syntaxNames.size())
 					return null;
 				else {
 					if (col == 0) {
 						return syntaxNames.get(row);
 					} else {
 						String name = syntaxNames.get(row);
 						if (Options.Keywords.containsKey(name))
 							return Options.Keywords.get(name);
 						else
 							return 2;
 					}
 				}
 			}
 			
 			@Override
 			public void setValueAt(Object val, int row, int col) {
 				String sval = (String) val;
 				
 				if (row == syntaxNames.size()) {
 					if (syntaxNames.contains(sval))
 						JOptionPane.showConfirmDialog(dialog, "Duplicated keyword: " + sval, "Duplicated keyword", JOptionPane.OK_CANCEL_OPTION);
 					else {
 						syntaxNames.add(sval);
 						Options.Keywords.put(sval, 2);
 					}
 				} else {
 					if (col == 0) {
 						syntaxNames.remove(row);
 						syntaxNames.add(row, sval);
 						
 						Options.Keywords.put(sval, Options.Keywords.get(sval));
 						Options.Keywords.remove(sval);
 					} else {
 						try {
 							Options.Keywords.put(syntaxNames.get(row), Integer.parseInt(sval));
 						} catch (NumberFormatException nfe) {
 							JOptionPane.showConfirmDialog(dialog, "Invalid number format: " + sval, "Invalid number format", JOptionPane.OK_CANCEL_OPTION);
 						}
 					}
 				}
 			}
 			
 			@Override
 			public int getRowCount() { return syntaxNames.size() + 1; }
 			
 			@Override
 			public int getColumnCount() { return 2; }
 			
 			@Override
 			public String getColumnName(int col) { return (col == 0 ? "Keyword" : "Indentation"); }
 			
 			@Override
 			public boolean isCellEditable(int row, int col) { return true; }
 		});
 		dialog.add(new JScrollPane(table));
 		
 		JPanel buttons = new JPanel();
 		buttons.setLayout(new GridLayout(1, 2));
 		
 		JButton resetButton = new JButton("Reset");
 		resetButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				setSyntax(null);
 				dialog.setVisible(false);
 			}
 		});
 		buttons.add(resetButton);
 		
 		JButton closeButton = new JButton("Close");
 		closeButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				dialog.setVisible(false);
 			}
 		});
 		buttons.add(closeButton);
 		
 		dialog.add(buttons, BorderLayout.SOUTH);
 	}
 	
 	/**
 	 * Show the dialg.
 	 */
 	public static void show() {
 		Collections.sort(syntaxNames);
 		if (dialog == null) new SyntaxDialog();
 		dialog.setVisible(true);
 	}
 	
 	/**
 	 * Get an encoded string.
 	 * @return Syntax.
 	 */
 	public static String getSyntax() {
 		Collections.sort(syntaxNames);
 		StringBuilder sb = new StringBuilder();
 		
 		for (String key : syntaxNames) {
 			sb.append(key);
 			sb.append("\t");
 			sb.append(Options.Keywords.get(key));
 			sb.append("\n");
 		}
 		sb.delete(sb.length() - 1, sb.length());
 		
 		return sb.toString();
 	}
 	
 	/**
 	 * Load syntax from an encoded string.
 	 * @param val Encoding
 	 */
 	public static void setSyntax(String val) {
 		syntaxNames.clear();
 		
 		if (val == null)
 			val = DEFAULT_SYNTAX; 
 		
 		val = val.replaceAll("\t+", "\t");
 		for (String line : val.split("\\s*\n\\s*")) {
 			String[] pair = line.split("\t");
 
 			String name = null;
 			int indent = 2;
 			
 			if (pair.length == 1) {
 				name = pair[0].trim();
 				indent = 2;
 			} else if (pair.length == 2) {
 				name = pair[0].trim();
 				try { indent = Integer.parseInt(pair[1].trim()); } catch(NumberFormatException nfe) { }		
 			} else {
 				continue;
 			}
 			
 			syntaxNames.add(name);
 			Options.Keywords.put(name, indent);
 		}
 		
 		Collections.sort(syntaxNames);
 	}
 }
