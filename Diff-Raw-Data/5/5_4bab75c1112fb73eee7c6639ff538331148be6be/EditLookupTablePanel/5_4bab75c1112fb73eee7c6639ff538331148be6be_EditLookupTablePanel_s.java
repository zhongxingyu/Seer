 package net.rptools.maptool.client.ui.lookuptable;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.Window;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JList;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.SwingUtilities;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.table.TableModel;
 
 import net.rptools.lib.MD5Key;
 import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.client.MapToolUtil;
 import net.rptools.maptool.client.swing.AbeillePanel;
 import net.rptools.maptool.client.swing.ImageChooserDialog;
 import net.rptools.maptool.client.ui.token.ImageAssetPanel;
 import net.rptools.maptool.model.AssetManager;
 import net.rptools.maptool.model.LookupTable;
 import net.rptools.maptool.model.LookupTable.LookupEntry;
 
 public class EditLookupTablePanel extends AbeillePanel {
 
 	private LookupTable lookupTable;
 	private ImageAssetPanel tableImageAssetPanel;
 	private int defaultRowHeight;
 	
 	private boolean accepted = false;
 	private boolean newTable = false;
 	
 	public EditLookupTablePanel() {
 		super("net/rptools/maptool/client/ui/forms/editLookuptablePanel.jfrm");
 
 		panelInit();
 	}
 	
 	public void initTableDefinitionTable() {
 
 		defaultRowHeight = getTableDefinitionTable().getRowHeight();
 		
 		getTableDefinitionTable().setDefaultRenderer(ImageAssetPanel.class, new ImageCellRenderer());
 		getTableDefinitionTable().setModel(createLookupTableModel(new LookupTable()));
 		getTableDefinitionTable().addMouseListener(new MouseAdapter() {
 			
 			@Override
 			public void mousePressed(MouseEvent e) {
 				
 				
 				int column = getTableDefinitionTable().columnAtPoint(e.getPoint());
 				if (column < 2) {
 					return;
 				}
 				
 				int row = getTableDefinitionTable().rowAtPoint(e.getPoint());
 				
 				String imageIdStr = (String) getTableDefinitionTable().getModel().getValueAt(row, column);
 				
 				// HACK: this is a hacky way to figure out if the button was pushed :P
 				if (e.getPoint().x > getTableDefinitionTable().getSize().width - 15) {
 					if (imageIdStr == null || imageIdStr.length() == 0) {
 						// Add
 						ImageChooserDialog chooserDialog = MapTool.getFrame().getImageChooserDialog();
 						
 						chooserDialog.setVisible(true);
 						
 						MD5Key imageId = chooserDialog.getImageId();
 						if (imageId == null) {
 							return;
 						}
 						
 						imageIdStr = imageId.toString();
 						
 					} else {
 						// Cancel
 						imageIdStr = null;
 					}
 				} else if (e.getPoint().x > getTableDefinitionTable().getSize().width - 30) {
 					// Add
 					ImageChooserDialog chooserDialog = MapTool.getFrame().getImageChooserDialog();
 					
 					chooserDialog.setVisible(true);
 					
 					MD5Key imageId = chooserDialog.getImageId();
 					if (imageId == null) {
 						return;
 					}
 					
 					imageIdStr = imageId.toString();
 				}
 				
 				getTableDefinitionTable().getModel().setValueAt(imageIdStr, row, column);
 
 				updateDefinitionTableRowHeights();
 				
 				getTableDefinitionTable().repaint();
 			}
 		});
 	}
 	
 	public void initTableImage() {
 
 		tableImageAssetPanel = new ImageAssetPanel();
 		tableImageAssetPanel.setPreferredSize(new Dimension(150, 150));
 		tableImageAssetPanel.setBorder(BorderFactory.createLineBorder(Color.black));
 		replaceComponent("mainForm", "tableImage", tableImageAssetPanel);
 	}
 
 	public void attach(LookupTable lookupTable) {
 	
 		newTable = lookupTable == null;
 		
 		this.lookupTable = newTable ? new LookupTable() : lookupTable;
 		
 		accepted = false;
 
 		getTableNameTextField().setText(this.lookupTable.getName());
 		getTableRollTextField().setText(this.lookupTable.getRoll());
 		tableImageAssetPanel.setImageId(this.lookupTable.getTableImage());
 
 		getTableNameTextField().requestFocusInWindow();
 		
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				getTableDefinitionTable().setModel(createLookupTableModel(EditLookupTablePanel.this.lookupTable));
 				updateDefinitionTableRowHeights();
 			}
 		});
 	}
 	
 	public boolean accepted() {
 		return accepted;
 	}
 	
 	public JTextField getTableNameTextField() {
 		return (JTextField) getComponent("tableName");
 	}
 
 	public JTextField getTableRollTextField() {
 		return (JTextField) getComponent("defaultTableRoll");
 	}
 
 	public JTable getTableDefinitionTable() {
 		return (JTable) getComponent("definitionTable");
 	}
 
 	public JList getTableList() {
 		return (JList) getComponent("tableList");
 	}
 	
 	public void initCancelButton() {
 		JButton button = (JButton) getComponent("cancelButton");
 		button.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				accepted = false;
 				close();
 			}
 		});
 	}
 	
 	public void initAcceptButton() {
 		JButton button = (JButton) getComponent("acceptButton");
 		button.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				
 				// Commit any in-process edits
 				if (getTableDefinitionTable().isEditing()) {
 					getTableDefinitionTable().getCellEditor().stopCellEditing();
 				}
 				
 				String name = getTableNameTextField().getText().trim();
 				if (name.length() == 0) {
 					MapTool.showError("Must have a name");
 					return;
 				}
 				
 				LookupTable existingTable = MapTool.getCampaign().getLookupTableMap().get(name);
 				if (existingTable != null && existingTable != lookupTable) {
 					MapTool.showError("A table with that name already exists");
 					return;
 				}
 				
 				TableModel tableModel = getTableDefinitionTable().getModel();
 				if (tableModel.getRowCount() < 1) {
 					MapTool.showError("Must have at least one row");
 					return;
 				}
 
 				lookupTable.setName(name);
 				lookupTable.setRoll(getTableRollTextField().getText());
 				lookupTable.setTableImage(tableImageAssetPanel.getImageId());
 
 				MapToolUtil.uploadAsset(AssetManager.getAsset(tableImageAssetPanel.getImageId()));
 
 				lookupTable.clearEntries();
 				for (int i = 0; i < tableModel.getRowCount(); i++) {
 					
 					String range = ((String) tableModel.getValueAt(i, 0)).trim();
 					String value = ((String) tableModel.getValueAt(i, 1)).trim();
 					String imageId = (String) tableModel.getValueAt(i, 2);
 					
 					if (range.length() == 0) {
 						continue;
 					}
 					
 					int min = 0;
 					int max = 0;
 					
 					int split = range.indexOf("-", range.charAt(0) == '-' ? 1 : 0); // Allow negative numbers
 					try {
 						if (split < 0) {
 							min = Integer.parseInt(range);
 							max = min;
 						} else {
 							min = Integer.parseInt(range.substring(0, split).trim());
 							max = Integer.parseInt(range.substring(split+1).trim());
 						}
 					} catch (NumberFormatException nfe) {
 						MapTool.showError("Could not parse range: " + range);
 						return;
 					}
 					
 					MD5Key image = null;
 					if (imageId != null && imageId.length() > 0) {
 						image = new MD5Key(imageId);
 						MapToolUtil.uploadAsset(AssetManager.getAsset(image));
 					}
 					lookupTable.addEntry(min, max, value, image);
 				}
 
 				// This will add it if it is new
 				MapTool.getCampaign().getLookupTableMap().put(name, lookupTable);
 				MapTool.serverCommand().updateCampaign(MapTool.getCampaign().getCampaignProperties());
 				
 				accepted = true;
 				
 				close();
 			}
 		});
 	}
 
 	private void close() {
 		SwingUtilities.getWindowAncestor(this).setVisible(false);
 	}
 	
 	private void updateDefinitionTableRowHeights() {
 
 		JTable table = getTableDefinitionTable();
 		for (int row = 0; row < table.getRowCount(); row++) {
 			
 			String imageId = (String)table.getModel().getValueAt(row, 2);
 			table.setRowHeight(row, imageId != null && imageId.length() > 0 ? 100 : defaultRowHeight);
 		}
 	}
 	
 	private LookupTableTableModel createLookupTableModel(LookupTable lookupTable) {
 
 		List<List<String>> rows = new ArrayList<List<String>>();
 		if (lookupTable != null) {
 			for (LookupEntry entry : lookupTable.getEntryList()) {
 				
 				String range = entry.getMax() != entry.getMin() ? entry.getMin() + "-" + entry.getMax() : "" + entry.getMin();
 				String value = entry.getValue();
 				MD5Key imageId = entry.getImageId();
 				
 				rows.add(Arrays.asList(new String[]{range, value, imageId != null ? imageId.toString() : null}));
 			}
 		}
 			
 		return new LookupTableTableModel(rows, "Range", "Value", "Image");
 	}
 	
 	private static class ImageCellRenderer extends ImageAssetPanel implements TableCellRenderer {
 
 		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
 
 			setImageId(value != null && ((String) value).length() > 0 ? new MD5Key((String)value) : null);
 			
 			return this;
 		}
 		
 	}
 	
 	private static class LookupTableTableModel extends AbstractTableModel {
 		
 		private List<String> newRow = new ArrayList<String>();
 		private List<List<String>> rowList;
 		private String[] cols;
 
 		public LookupTableTableModel(List<List<String>> rowList, String... cols) {
 			this.rowList = rowList;
 			this.cols = cols;
 		}
 		
 		public int getColumnCount() {
 			return cols.length;
 		}
 		public int getRowCount() {
 			return rowList.size() + 1;
 		}
 		public Object getValueAt(int rowIndex, int columnIndex) {
 
 			List<String> row = null;
 			
 			// Existing value
 			if (rowIndex < rowList.size()) {
 				row = rowList.get(rowIndex);
 			} else {
 				row = newRow;
 			}
 			
 			return columnIndex < row.size() ? row.get(columnIndex) : "";
 		}
 
 		@Override
 		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
 
 			boolean hasNewRow = false;
 			List<String> row = null;
 			if (rowIndex < rowList.size()) {
 				row = rowList.get(rowIndex);
 			} else {
 				row = newRow;
 				
 				rowList.add(newRow);
 				newRow = new ArrayList<String>();
 				
 				hasNewRow = true;
 			}
 
 			while (columnIndex >= row.size()) {
 				row.add("");
 			}
 			
 			row.set(columnIndex, (String)aValue);
 			
 			if (hasNewRow) {
 				fireTableRowsInserted(rowList.size(), rowList.size());
 			}
 		}
 
 		@Override
 		public Class<?> getColumnClass(int columnIndex) {
 			return columnIndex < 2 ? String.class : ImageAssetPanel.class;
 		}
 		
 		@Override
 		public String getColumnName(int column) {
 			return cols[column];
 		}
 		
 		@Override
 		public boolean isCellEditable(int rowIndex, int columnIndex) {
 			return columnIndex != 2;
 		}
 
 	}
 }
