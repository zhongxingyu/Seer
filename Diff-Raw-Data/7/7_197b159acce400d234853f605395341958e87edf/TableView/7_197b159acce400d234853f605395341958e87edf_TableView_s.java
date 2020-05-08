 /*******************************************************************************
  * Copyright (c) 2009 Siemens AG
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Kai Toedter - initial API and implementation
  *******************************************************************************/
 
 package com.siemens.ct.pm.ui.views.tableview.ds;
 
 import java.awt.Color;
 
 import javax.swing.BorderFactory;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JComponent;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingUtilities;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.AbstractTableModel;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.siemens.ct.pm.application.service.ISelectionService;
 import com.siemens.ct.pm.application.service.IViewContribution;
 import com.siemens.ct.pm.model.IPerson;
 import com.siemens.ct.pm.model.IPersonListener;
 import com.siemens.ct.pm.model.IPersonManager;
 import com.siemens.ct.pm.model.event.PersonEvent;
 
 public class TableView implements IViewContribution, IPersonListener {
 
 	private ImageIcon icon;
 	private JComponent view;
 	private IPersonManager personManager;
 	private ISelectionService selectionService;
 	private JTable table;
 	private final Logger logger = LoggerFactory.getLogger(TableView.class);
 
 	@SuppressWarnings("serial")
 	class TableModel extends AbstractTableModel {
 
 		@Override
 		public String getColumnName(int column) {
 			switch (column) {
 			case 0:
 				return "First Name";
 
 			case 1:
 				return "Last Name";
 
 			case 2:
 				return "Company";
 
 			default:
 				return null;
 			}
 		}
 
 		@Override
 		public int getColumnCount() {
 			return 3;
 		}
 
 		@Override
 		public int getRowCount() {
 			if (personManager == null) {
 				return 0;
 			}
 			return personManager.getPersons().size();
 		}
 
 		@Override
 		public Object getValueAt(int rowIndex, int columnIndex) {
 			if (personManager == null) {
 				return null;
 			}
 
 			IPerson person = personManager.getPersons().get(rowIndex);
 			switch (columnIndex) {
 			case 0:
 				return person.getFirstName();
 
 			case 1:
 				return person.getLastName();
 
 			case 2:
 				return person.getCompany();
 
 			default:
 				return null;
 			}
 		}
 	}
 
 	public TableView() {
 		Runnable uiCreator = new Runnable() {
 			public void run() {
 				icon = new ImageIcon(this.getClass().getResource(
 						"/icons/table.png"));
 				table = new JTable(new TableModel());
 
 				table.getColumnModel().getColumn(0).setPreferredWidth(100);
 				table.getColumnModel().getColumn(0).setMaxWidth(100);
 				table.getColumnModel().getColumn(1).setPreferredWidth(100);
 				table.getColumnModel().getColumn(1).setMaxWidth(100);
 
 				table.setColumnSelectionAllowed(false);
 				table.setRowSelectionAllowed(true);
 				table.getSelectionModel().setSelectionMode(
 						ListSelectionModel.SINGLE_SELECTION);
 				table.getSelectionModel().addListSelectionListener(
 						new ListSelectionListener() {
 
 							@Override
 							public void valueChanged(ListSelectionEvent e) {
 								if (e.getValueIsAdjusting()
 										&& table.getSelectedRow() != -1) {
 									IPerson selectedPerson = personManager
 											.getPersons().get(
 													table.getSelectedRow());
 									selectionService
 											.objectSelected(selectedPerson);
 								}
 							}
 
 						});
 
 				view = new JScrollPane(table);
 				view.setBorder(BorderFactory.createCompoundBorder(BorderFactory
 						.createEmptyBorder(2, 2, 2, 2), BorderFactory
 						.createLineBorder(Color.lightGray)));
 			}
 		};
 		SwingUtilities.invokeLater(uiCreator);
 	}
 
 	@Override
 	public Icon getIcon() {
 		return icon;
 	}
 
 	@Override
 	public String getName() {
 		return "Table View (DS)";
 	}
 
 	@Override
 	public JComponent getView() {
 		return view;
 	}
 
 	@Override
 	public int getPosition() {
 		return 4;
 	}
 
 	public synchronized void removeSelectionService(
 			ISelectionService selectionService) {
 		this.selectionService = null;
 	}
 
 	public synchronized void setSelectionService(
 			ISelectionService selectionService) {
 		this.selectionService = selectionService;
 	}
 
 	public void removePersonManager(final IPersonManager personManager) {
 
 		final TableView tableView = this;
 		Runnable uiCreator = new Runnable() {
 			public void run() {
 				logger.info("removePersonManager: " + personManager);
 				if (personManager == tableView.personManager) {
 					tableView.personManager = null;
 					((AbstractTableModel) table.getModel())
 							.fireTableDataChanged();
 				}
 			}
 		};
 
 		SwingUtilities.invokeLater(uiCreator);
 	}
 
 	public void setPersonManager(final IPersonManager personManager) {
 		final TableView tableView = this;
 		Runnable uiCreator = new Runnable() {
 			public void run() {
 				logger.info("setPersonManager: " + personManager);
				if (personManager == tableView.personManager) {
					tableView.personManager = null;
					((AbstractTableModel) table.getModel())
							.fireTableDataChanged();
				}
 			}
 		};
 
 		SwingUtilities.invokeLater(uiCreator);
 	}
 
 	@Override
 	public void handleEvent(PersonEvent event) {
 		((AbstractTableModel) table.getModel()).fireTableDataChanged();
 	}
 }
