 package de.osmui.ui;
 
 import java.awt.BorderLayout;
 
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTable;
 
 import de.osmui.i18n.I18N;
 import de.osmui.model.osm.TTask;
 import de.osmui.ui.models.TaskBoxCellRenderer;
 import de.osmui.ui.models.TaskBoxTableModel;
 
 public class TabBox extends JTabbedPane{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -2984123985661193020L;
 	
 	public TabBox(TaskBoxTableModel tableModel) {
 
 		this.setTabPlacement(JTabbedPane.TOP);
 		this.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
 		
 		TaskBox taskBox = new TaskBox(tableModel);
 		TaskBoxCellRenderer taskBoxCellRenderer = new TaskBoxCellRenderer();
 		taskBox.setDefaultRenderer(TTask.class, taskBoxCellRenderer);
 
 		JPanel taskTab = new JPanel();
 		taskTab.setLayout(new BorderLayout());
 
         JScrollPane taskScrollPane = new JScrollPane(taskBox);
         taskTab.add(taskScrollPane,BorderLayout.CENTER);
         taskTab.add(new JButton("hinzufügen"),BorderLayout.SOUTH);
         
         this.add(I18N.getString("Content.tabBox"),taskTab); 
 		
         
         JPanel parameterTab = new JPanel();
 		parameterTab.add(new JTable());
 		this.add(I18N.getString("Content.pipelineBox"),parameterTab); 	
 	}
 	
 }
