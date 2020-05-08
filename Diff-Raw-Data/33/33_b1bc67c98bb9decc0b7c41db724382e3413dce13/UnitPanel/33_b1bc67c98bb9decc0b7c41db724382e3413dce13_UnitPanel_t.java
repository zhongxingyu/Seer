 package gui;
 
 import backbone.*;
 import middleend.*;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.DefaultCellEditor;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 public class UnitPanel extends JPanel implements GUIConstants {
 
 	private MiddleEnd _middleEnd;
 	private JPanel _mainPanel, _buttonPanel, _tablePanel;
 	private Grouping<Unit> _grouping;
 	
 	public UnitPanel(MiddleEnd m, Unit u) {
 		_middleEnd = m;
 		_grouping = u.getMemberOf();
 		_mainPanel = new JPanel();
 		_buttonPanel = new JPanel();
 		_tablePanel = new JPanel();
 		initialize(u, "Save Changes", "Delete this unit");
 	}
 	
 	public UnitPanel(MiddleEnd m, Unit u, Grouping<Unit> g) {
 		_middleEnd = m;
 		_grouping = g;
 		_mainPanel = new JPanel();
 		_buttonPanel = new JPanel();
 		_tablePanel = new JPanel();
 		initialize(u, "Save Changes and Add Another New Unit", "Clear values");
 	}
 	
 	public void initialize(final Unit unit, String savestring, final String deletestring) {
 		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 		if (COLORSON) {
 			this.setBackground(BACKGROUND_COLOR);
 			this.setForeground(FOREGROUND_COLOR);
 			_mainPanel.setBackground(BACKGROUND_COLOR);
 			_mainPanel.setForeground(FOREGROUND_COLOR);
 			_buttonPanel.setBackground(BACKGROUND_COLOR);
 			_buttonPanel.setForeground(FOREGROUND_COLOR);
 			_tablePanel.setBackground(BACKGROUND_COLOR);
 			_tablePanel.setForeground(FOREGROUND_COLOR);
 		}
 		_mainPanel.setLayout(new BoxLayout(_mainPanel, BoxLayout.X_AXIS));
 		_tablePanel.setLayout(new BoxLayout(_tablePanel, BoxLayout.Y_AXIS));
 		final HashMap<Attribute, JComponent> components = new HashMap<Attribute, JComponent>();
		ArrayList<Attribute> originalattributes = new ArrayList<Attribute>();
 		for (final Attribute attr : unit.getAttributes()) {
			originalattributes.add(attr);
 			JLabel titleLabel = Utility.getTitleLabel(attr);
 			if (attr instanceof GroupingAttribute) {
 				GroupingAttribute<Unit> g = (GroupingAttribute<Unit>) attr;
 				if (null == components.put(attr, new InputTablePane(_middleEnd, g.getBlankUnit().getAttributes(), g))) {
 					_tablePanel.add(components.get(attr));
 					_tablePanel.setVisible(false);
 				}
 			}
 			JComponent comp = Utility.getField(attr);
 			if (comp instanceof JButton) {
 				((JButton) comp).addActionListener(new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						for (int i = 0; i < _tablePanel.getComponentCount(); i++) {
 							if (_tablePanel.getComponent(i) instanceof InputTablePane) {
 								InputTablePane pane = (InputTablePane) _tablePanel.getComponent(i);
 								if (pane == components.get(attr)) {
 									pane.setVisible(!pane.isVisible());
 									_tablePanel.setVisible(pane.isVisible());
 								}
 								else {
 									pane.setVisible(false);
 								}
 								repaint();
 							}
 						}
 						_tablePanel.repaint();
 					}
 				});
 			}
 			if (!components.containsKey(attr))
 				components.put(attr, comp);
 			JPanel toAdd = new JPanel();
 			toAdd.setLayout(new BoxLayout(toAdd, BoxLayout.Y_AXIS));
 			toAdd.add(titleLabel);
 			toAdd.add(comp);
 			if (COLORSON) {
 				toAdd.setBackground(BACKGROUND_COLOR);
 				toAdd.setForeground(FOREGROUND_COLOR);
 			}
 			_mainPanel.add(toAdd);
 		}
 		JButton savebutton = new JButton(savestring);
 		if (IMAGESON)
 			savebutton.setIcon(SAVEBUTTONIMAGE);
 		if (COLORSON) {
 			savebutton.setBackground(BACKGROUND_COLOR);
 			savebutton.setForeground(FOREGROUND_COLOR);
 		}
 		savebutton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				Collection<Attribute> attributes = components.keySet();
 				for (Attribute attr : attributes) {
 					if (attr.getType() == Attribute.Type.BOOLEAN) {
 						boolean value = ((JCheckBox) components.get(attr)).isSelected();
 						unit.setAttribute(new BooleanAttribute(attr.getTitle(), value));
 					}
 					else if (attr.getType() == Attribute.Type.DOUBLE) {
 						double value = Double.parseDouble(((JTextField) components.get(attr)).getText());
 						unit.setAttribute(new DoubleAttribute(attr.getTitle(), value));
 					}
 					else if (attr.getType() == Attribute.Type.GROUPING) {
 						InputTablePane table = (InputTablePane) components.get(attr);
 						GroupingAttribute<Unit> groupattr = (GroupingAttribute) attr;
 						for (int i = 0; i < table.getTable().getRowCount(); i++) {
 							Unit rowunit;
 							boolean alreadyadded = false;
 							if (i < table.getUnitsInRowsList().size()) {
 								rowunit = table.getUnitsInRowsList().get(i);
 								alreadyadded = true;
 							}
 							else {
 								rowunit = groupattr.getBlankUnit();
 							}
 							int j = 0;
 							for (Attribute rowattr : rowunit.getAttributes()) {
 								if (rowattr.getType() == Attribute.Type.BOOLEAN) {
 									boolean value = false;
 									if (table.getTable().getValueAt(i, j) != null) {
 										value = (Boolean) table.getTable().getValueAt(i, j);
 									}
 									rowunit.setAttribute(new BooleanAttribute(rowattr.getTitle(), value));
 								}
 								else if (rowattr.getType() == Attribute.Type.DOUBLE) {
 									double value = 0;
 									if (table.getTable().getValueAt(i, j) != null) {
 										value = (Double) table.getTable().getValueAt(i, j);
 									}
 									rowunit.setAttribute(new DoubleAttribute(rowattr.getTitle(), value));
 								}
 								else if (rowattr.getType() == Attribute.Type.INT) {
 									int value = 0;
 									if (table.getTable().getValueAt(i, j) != null) {
 										value = (Integer) table.getTable().getValueAt(i, j);
 									}
 									rowunit.setAttribute(new IntAttribute(rowattr.getTitle(), value));
 								}
 								else if (rowattr.getType() == Attribute.Type.STRING) {
 									String value = "";
 									if (table.getTable().getValueAt(i, j) != null) {
 										value = (String) table.getTable().getValueAt(i, j);
 									}
 									rowunit.setAttribute(new StringAttribute(rowattr.getTitle(), value));
 								}
 								else if (rowattr.getType() == Attribute.Type.UNIT) {
 									DefaultCellEditor editor = (DefaultCellEditor) table.getTable().getCellEditor(i, j);
 									UnitAttributeComboBox combobox = (UnitAttributeComboBox) editor.getComponent();
 									Grouping<Unit> g = combobox.getGrouping();
 									Unit value = null;
 									if (table.getTable().getValueAt(i, j) != null) {
 										value = combobox.getSelectedUnit();
 										if ((value != null) && (!g.getMembers().contains(value)))
 											g.addMember(value);
 									}
 									rowunit.setAttribute(new UnitAttribute<Unit>(rowattr.getTitle(), value, g));
 								}
 								j++;
 							}
 							boolean rowunitisnull = false;
 							if (rowunit.getName() == null)
 								rowunitisnull = true;
 							else if (rowunit.getName() == "")
 								rowunitisnull = true;
 							if (!rowunitisnull && !alreadyadded) {
 								groupattr.addMember(rowunit);
 								if (!groupattr.getBlankUnit().getMemberOf().getMembers().contains(rowunit))
 									groupattr.getBlankUnit().getMemberOf().addMember(rowunit);
 							}
 							unit.setAttribute(groupattr);
 						}
 						table = new InputTablePane(_middleEnd, groupattr.getBlankUnit().getAttributes(), groupattr);
 						components.put(attr, table);
 					}
 					else if (attr.getType() == Attribute.Type.INT) {
 						int value = Integer.parseInt(((JTextField) components.get(attr)).getText());
 						unit.setAttribute(new IntAttribute(attr.getTitle(), value));
 					}
 					else if (attr.getType() == Attribute.Type.STRING) {
 						String value = ((JTextField) components.get(attr)).getText();
 						unit.setAttribute(new StringAttribute(attr.getTitle(), value));
 					}
 					else if (attr.getType() == Attribute.Type.UNIT) {
 						Unit value = ((UnitAttributeComboBox) components.get(attr)).getSelectedUnit();
 						Grouping grouping = ((UnitAttributeComboBox) components.get(attr)).getGrouping();
 						if ((value != null) && (!grouping.getMembers().contains(value)))
 							grouping.addMember(value);
 						unit.setAttribute(new UnitAttribute(attr.getTitle(), value, grouping));
 					}
 				}
 				
 				if (!_grouping.getMembers().contains(unit)) {
 					_grouping.addMember(unit);
 				}
 				_middleEnd.repaintAll();
 			}
 		});
 		final JButton deletebutton = new JButton(deletestring);
 		if (IMAGESON)
 			deletebutton.setIcon(DELETEBUTTONIMAGE);
 		if (COLORSON) {
 			deletebutton.setBackground(BACKGROUND_COLOR);
 			deletebutton.setForeground(FOREGROUND_COLOR);
 		}
 		deletebutton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (deletebutton.getText().equals(deletestring))
 					deletebutton.setText("Are you sure?");
 				else {
 					_mainPanel.removeAll();
 					_buttonPanel.removeAll();
 					_tablePanel.removeAll();
 					unit.deleteFromGrouping();
 					_middleEnd.repaintAll();
 				}
 			}
 		});
 		_buttonPanel.add(savebutton);
 		_buttonPanel.add(deletebutton);
 		_buttonPanel.setMaximumSize(UNITPANEL_SIZE);
 		this.add(_mainPanel);
 		this.add(Box.createRigidArea(SMALLSPACING_SIZE));
 		this.add(_buttonPanel);
 		this.add(Box.createRigidArea(SMALLSPACING_SIZE));
 		this.add(_tablePanel);
 		this.add(Box.createVerticalGlue());
 		this.repaint();
 	}
 }
