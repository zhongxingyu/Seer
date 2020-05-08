 /**
  * 
  */
 package org.publicmain.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 
 import org.resources.Help;
 
 /**
  * @author ABerthold
  *
  */
 public class ResultWindow extends JDialog {
 	
 	private JTable resultTable;
 	private JScrollPane scroller;
 	
 	public ResultWindow(JTable result){
 		this.resultTable = result;
 		this.scroller = new JScrollPane(resultTable);
 		
		this.resultTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
 		this.resultTable.setFillsViewportHeight(true);
 		this.resultTable.getModel().addTableModelListener(new TableModelListener() {
 			
 			@Override
 			public void tableChanged(TableModelEvent e) {
 				ColumnsAutoSizer.sizeColumnsToFit(resultTable);
 				
 			}
 		});
 		this.add(scroller);
 		
 		this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 		this.setTitle("Found " + resultTable.getRowCount() + " messages");
 		this.setIconImage(Help.getIcon("pM_Logo.png",64).getImage());
		this.setMinimumSize(new Dimension(750, 200));
 		this.pack();
 		this.setLocationRelativeTo(null);
 		 JButton autoSizeButton = new JButton("Auto-size columns");
 		 
 	        // resize the columns when the user clicks the button
 	        autoSizeButton.addActionListener(new ActionListener() {
 	            public void actionPerformed(ActionEvent e) {
 	                ColumnsAutoSizer.sizeColumnsToFit(resultTable);
 	            }
 	        });
 	        this.add(autoSizeButton,BorderLayout.SOUTH);
 		this.setVisible(true);
 		
 		/*
 		 * 
 		 *  // automatically resize the columns whenever the data in the table changes
         table.getModel().addTableModelListener(new TableModelListener() {
             public void tableChanged(TableModelEvent e) {
                 ColumnsAutoSizer.sizeColumnsToFit(table);
             }
         });
 		 */
 	}
	
	
 	
 }
