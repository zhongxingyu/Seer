 package evaluation.simulator.gui.customElements.accordion;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.List;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JPanel;
 import javax.swing.JTable;
 import javax.swing.SwingConstants;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.table.TableColumn;
 
 import evaluation.simulator.annotations.simulationProperty.SimProp;
 
 @SuppressWarnings("serial")
 public class AccordionEntry extends JPanel {
 	private JButton entryButton;
 	private JTable entryTable;
 
 	public AccordionEntry(String name,
 			List<SimProp> listOfAllSectionsInACategory,
 			final JComboBox<String> jComboBox) {
 		this.setLayout(new BorderLayout(0, 0));
 
 		this.entryButton = new JButton(name, new ImageIcon(
 				"etc/img/icons/green/arrow-144-24.png"));
 		this.entryButton.setForeground(Color.BLACK);
 		this.entryButton.setHorizontalAlignment(SwingConstants.LEFT);
 		this.entryButton.setHorizontalTextPosition(SwingConstants.RIGHT);
 
 		this.entryTable = new JTable(new AccordionModel(name)) {
 
 			// This takes care that the non-editable cells are grayed out.
 			@Override
 			public Component prepareRenderer(TableCellRenderer renderer,
 					int row, int column) {
 				Component c = super.prepareRenderer(renderer, row, column);
 				if ((column == 1) && !this.isCellEditable(row, column)) {
 					c.setBackground(new Color(255, 200, 200));
 				}
 				return c;
 			}
 		};
 
 		this.entryTable.addMouseMotionListener(new AccordionMouseMotionAdapter(
 				listOfAllSectionsInACategory, this.entryTable));
 		this.entryTable.setDefaultRenderer(Object.class,
 				new AccordionTableCellRenderer(listOfAllSectionsInACategory));
 		this.entryTable.setVisible(false);
 
 		TableColumn col = this.entryTable.getColumnModel().getColumn(1);
 		col.setCellEditor(new AccordionCellEditor());
 
 		ActionListener actionListener = new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				AccordionEntry.this.toggleVisibility(e.getSource(), jComboBox);
 			}
 		};
 
 		this.entryButton.addActionListener(actionListener);
 		this.entryButton.setAlignmentX(Component.LEFT_ALIGNMENT);
 		this.entryTable.setAlignmentX(Component.LEFT_ALIGNMENT);
 
 		this.add(this.entryButton, BorderLayout.NORTH);
 		jComboBox.setVisible(false);
 		this.add(jComboBox, BorderLayout.CENTER);
 		this.add(this.entryTable, BorderLayout.SOUTH);
 	}
 
 	private void toggleVisibility(Object source, JComboBox<String> jComboBox) {
 
 		JButton btn = (JButton) source;
 		if (!this.entryTable.isVisible()) {
 			btn.setIcon(new ImageIcon("etc/img/icons/red/arrow-144-24.png"));
 		} else {
 			btn.setIcon(new ImageIcon("etc/img/icons/green/arrow-144-24.png"));
 		}
 
 		jComboBox.setVisible(!jComboBox.isVisible());
 		this.entryTable.setVisible(!this.entryTable.isVisible());
 		this.repaint();
 	}
 }
