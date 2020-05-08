 package at.epu.PresentationLayer;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.SystemColor;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.List;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
import javax.swing.SwingUtilities;
 import javax.swing.table.DefaultTableColumnModel;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.table.TableColumn;
 import javax.swing.table.TableModel;
 
 public class GenericSplitTableView extends JPanel {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private JTable table;
 	
 	/**
 	 * Create the panel.
 	 */
 	public GenericSplitTableView(List<JButton> buttons, List<JLabel> labels, final List<JMenuItem> menuList, TableModel tableModel) {
 		setBackground(SystemColor.control);
 		
 		GridBagLayout gridBagLayout = new GridBagLayout();
 		gridBagLayout.columnWidths = new int[]{0, 0};
 		gridBagLayout.rowHeights = new int[]{0};
 		gridBagLayout.columnWeights = new double[]{Double.MIN_VALUE, 1.0};
 		gridBagLayout.rowWeights = new double[]{1.0};
 		setLayout(gridBagLayout);
 		
 		JPanel panel = new JPanel();
 		GridBagConstraints gbc_panel = new GridBagConstraints();
 		gbc_panel.fill = GridBagConstraints.HORIZONTAL;
 		gbc_panel.insets = new Insets(0, 0, 5, 5);
 		gbc_panel.anchor = GridBagConstraints.NORTHWEST;
 		gbc_panel.gridx = 0;
 		gbc_panel.gridy = 0;
 		add(panel, gbc_panel);
 		
 		table = new JTable(tableModel);
 		table.setBackground(Color.ORANGE);
 		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
 		
 		for(int i = 0; i < table.getColumnCount(); i++) {
 			packColumn(table, i, 10);
 		}
 		
 		GridBagLayout btnGridBagLayout = new GridBagLayout();
 		btnGridBagLayout.columnWidths = new int[]{0};
 		btnGridBagLayout.rowHeights = new int[]{0, 0, 0};
 		btnGridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
 		btnGridBagLayout.rowWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
 		panel.setLayout(btnGridBagLayout);
 		
 		JScrollPane scrollPane = new JScrollPane();
 		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
 		gbc_scrollPane.fill = GridBagConstraints.BOTH;
 		gbc_scrollPane.anchor = GridBagConstraints.NORTHEAST;
 		gbc_scrollPane.gridx = 1;
 		gbc_scrollPane.gridy = 0;
 		add(scrollPane, gbc_scrollPane);
 		scrollPane.setViewportView(table);
 		
 		int i = 0;
 		for(JButton button : buttons) {
 			GridBagConstraints gbc_button = new GridBagConstraints();
 			gbc_button.insets = new Insets(5, 10, 5, 10);
 			gbc_button.gridx = 0;
 			gbc_button.gridy = i++;
 			gbc_button.anchor = GridBagConstraints.NORTH;
 			gbc_button.fill = GridBagConstraints.HORIZONTAL;
 			panel.add(button, gbc_button);
 		}
 		
 		for(JLabel label : labels) {
 			GridBagConstraints gbc_label = new GridBagConstraints();
 			gbc_label.insets = new Insets(5, 10, 5, 10);
 			gbc_label.gridx = 0;
 			gbc_label.gridy = i++;
 			gbc_label.anchor = GridBagConstraints.NORTH;
 			gbc_label.fill = GridBagConstraints.HORIZONTAL;
 			panel.add(label, gbc_label);
 		}
 		
 		table.addMouseListener(new MouseAdapter() {
 	        @Override
 	        public void mouseReleased(MouseEvent e) {
 	            int r = table.rowAtPoint(e.getPoint());
 	            if (r >= 0 && r < table.getRowCount()) {
 	                table.setRowSelectionInterval(r, r);
 	            } else {
 	                table.clearSelection();
 	            }
 
 	            int rowindex = table.getSelectedRow();
 	            if (rowindex < 0)
 	                return;
	            if (SwingUtilities.isRightMouseButton(e) && e.getComponent() instanceof JTable ) {
 	                JPopupMenu popup = new JPopupMenu();
 	                
 	                for(JMenuItem menu : menuList) {
 	                	//remove any before added actionListeners
 	                	for(ActionListener al : menu.getActionListeners()) {
                 			menu.removeActionListener(al);
                 		}
 	                	popup.add(menu);
 	                	if(menu.getLabel() == "Editieren") {
 		                	menu.addActionListener(new ActionListener() {
 		                		public void actionPerformed(ActionEvent e) {
 		                			JFrame newFrame = new JFrame();
 			                		newFrame.setTitle("Editieren");
 			                		newFrame.setBounds(300, 150, 300, 500);
 			                		newFrame.setVisible(true);
 		                		}
 		                	});
 	                	}
 	                	if(menu.getLabel() == "Lschen") {
 	                		menu.addActionListener(new ActionListener() {
 		                		public void actionPerformed(ActionEvent e) {
 			                		JFrame newFrame = new JFrame();
 			                		newFrame.setTitle("Lschen");
 			                		JLabel note = new JLabel("Dieser Datensatz wird gelscht");
 			                		newFrame.add(note);
 			                		newFrame.setBounds(300, 150, 300, 500);
 			                		newFrame.setVisible(true);
 		                		}
 	                		}); 
 	                	}
 	                	//in Bill-Tab
 	                	if(menu.getLabel() == "Details") {
 	                		menu.addActionListener(new ActionListener() {
 		                		public void actionPerformed(ActionEvent e) {
 			                		JFrame newFrame = new JFrame();
 			                		newFrame.setTitle("Rechnungs - Details");
 			                		JLabel note = new JLabel("Hier werden die Rechnungszeilen der Rechnung angezeigt");
 			                		newFrame.add(note);
 			                		newFrame.setBounds(300, 150, 300, 500);
 			                		newFrame.setVisible(true);
 		                		}
 	                		}); 
 	                	}
 	                	//Ausgangsrechnung stellen(in Projekt Tab)
 	                	if(menu.getLabel() == "Ausgangsrechnung stellen") {
 	                		menu.addActionListener(new ActionListener() {
 		                		public void actionPerformed(ActionEvent e) {
 			                		JFrame newFrame = new JFrame();
 			                		newFrame.setTitle("Ausgangsrechnung stellen");
 			                		JLabel note = new JLabel("Hier werden die Ausgangsrechnungen gestellt");
 			                		newFrame.add(note);
 			                		newFrame.setBounds(300, 150, 300, 500);
 			                		newFrame.setVisible(true);
 		                		}
 	                		}); 
 	                	}
 	                	//Splitten in Bankkonto Tab
 	                	if(menu.getLabel() == "Rechnung Splitten") {
 	                		menu.addActionListener(new ActionListener() {
 		                		public void actionPerformed(ActionEvent e) {
 			                		JFrame newFrame = new JFrame();
 			                		newFrame.setTitle("Rechnung Splitten");
 			                		JLabel note = new JLabel("Hier wird die Rechnung gesplittet (Umsatzsteuer/Rechnungssumme)");
 			                		newFrame.add(note);
 			                		newFrame.setBounds(300, 150, 300, 500);
 			                		newFrame.setVisible(true);
 		                		}
 	                		}); 
 	                	}
 	                }
 	                popup.show(e.getComponent(), e.getX(), e.getY());
 	            }
 	        } 
 	 });
 	}
 		
 	public void packColumns(JTable table, int margin) {
 	    for (int c=0; c<table.getColumnCount(); c++) {
 	        packColumn(table, c, 2);
 	    }
 	}
 	
 	// Sets the preferred width of the visible column specified by vColIndex. The column
 	// will be just wide enough to show the column head and the widest cell in the column.
 	// margin pixels are added to the left and right
 	// (resulting in an additional width of 2*margin pixels).
 	public void packColumn(JTable table, int vColIndex, int margin) {
 	    DefaultTableColumnModel colModel = (DefaultTableColumnModel)table.getColumnModel();
 	    TableColumn col = colModel.getColumn(vColIndex);
 	    int width = 0;
 	
 	    // Get width of column header
 	    TableCellRenderer renderer = col.getHeaderRenderer();
 	    if (renderer == null) {
 	        renderer = table.getTableHeader().getDefaultRenderer();
 	    }
 	    Component comp = renderer.getTableCellRendererComponent(
 	        table, col.getHeaderValue(), false, false, 0, 0);
 	    width = comp.getPreferredSize().width;
 	
 	    // Get maximum width of column data
 	    for (int r=0; r<table.getRowCount(); r++) {
 	        renderer = table.getCellRenderer(r, vColIndex);
 	        comp = renderer.getTableCellRendererComponent(
 	            table, table.getValueAt(r, vColIndex), false, false, r, vColIndex);
 	        width = Math.max(width, comp.getPreferredSize().width);
 	    }
 	
 	    // Add margin
 	    width += 2*margin;
 	
 	    // Set the width
 	    col.setPreferredWidth(width);
 	}
 }
