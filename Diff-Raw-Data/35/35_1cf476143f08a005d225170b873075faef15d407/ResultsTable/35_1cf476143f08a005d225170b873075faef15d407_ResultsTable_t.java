 /////////////////////////////////////////////////////////////////////////
 //
 // © University of Southampton IT Innovation Centre, 2011
 //
 // Copyright in this library belongs to the University of Southampton
 // University Road, Highfield, Southampton, UK, SO17 1BJ
 //
 // This software may not be used, sold, licensed, transferred, copied
 // or reproduced in whole or in part in any manner or form or in or
 // on any media by any person other than in accordance with the terms
 // of the Licence Agreement supplied with the software, or otherwise
 // without the prior written consent of the copyright owners.
 //
 // This software is distributed WITHOUT ANY WARRANTY, without even the
 // implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 // PURPOSE, except where stated in the Licence Agreement supplied with
 // the software.
 //
 //	Created By :			Thomas Leonard
 //	Created Date :			2011-11-03
 //	Created for Project :		SERSCIS
 //
 /////////////////////////////////////////////////////////////////////////
 //
 //  License : GNU Lesser General Public License, version 2.1
 //
 /////////////////////////////////////////////////////////////////////////
 
 package eu.serscis.sam.gui;
 
 import java.util.Arrays;
 import org.deri.iris.storage.IRelation;
 import org.deri.iris.api.basics.ITuple;
 import org.eclipse.swt.layout.GridData;
 import eu.serscis.sam.Graph;
 import eu.serscis.sam.Eval;
 import eu.serscis.sam.Results;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.io.File;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.*;
 import org.eclipse.swt.layout.RowLayout;
 import static org.deri.iris.factory.Factory.*;
 
 public class ResultsTable {
 	private ITuple[] rows;
 	private final Table myTable;
 
 	public ResultsTable(Composite parent, String[] headings) {
 		this(parent, headings, null);
 	}
 
 	public ResultsTable(Composite parent, String[] headings, final RowViewer rowViewer) {
		myTable = new Table(parent, SWT.BORDER | SWT.FULL_SELECTION);
 
 		for (String heading : headings) {
 			TableColumn column = new TableColumn(myTable, SWT.LEFT);
 			column.setText(heading);
 		}
 		myTable.setHeaderVisible(true);
 
 		GridData tableLayoutData = new GridData();
 		tableLayoutData.horizontalAlignment = GridData.FILL;
 		tableLayoutData.verticalAlignment = GridData.FILL;
 		tableLayoutData.grabExcessHorizontalSpace = true;
 		tableLayoutData.grabExcessVerticalSpace = true;
 		myTable.setLayoutData(tableLayoutData);
 
 		if (rowViewer != null) {
 			myTable.addSelectionListener(new SelectionAdapter() {
 				public void widgetDefaultSelected(SelectionEvent e) {
 					try {
 						rowViewer.openRow(rows[myTable.getSelectionIndex()]);
 					} catch (Exception ex) {
 						ex.printStackTrace();
 					}
 				}
 			});
 		}
 	}
 
 	public void fillTable(IRelation rel) {
 		rows = new ITuple[rel.size()];
 		for (int i = 0; i < rows.length; i++) {
 			rows[i] = rel.get(i);
 		}
 		Arrays.sort(rows);
 
 		int nColumns = 0;
 		if (rows.length > 0) {
 			nColumns = rows[0].size();
 		}
 
 		myTable.removeAll();
 
 		for (int i = 0; i < rows.length; i++) {
 			TableItem item = new TableItem(myTable, 0);
 
 			for (int c = 0; c < nColumns; c++) {
 				item.setText(c, rows[i].get(c).getValue().toString());
 			}
 		}
 
 		for (TableColumn column : myTable.getColumns()) {
 			column.pack();
 		}
 	}
 
 	public Table getTable() {
 		return myTable;
 	}
 }
