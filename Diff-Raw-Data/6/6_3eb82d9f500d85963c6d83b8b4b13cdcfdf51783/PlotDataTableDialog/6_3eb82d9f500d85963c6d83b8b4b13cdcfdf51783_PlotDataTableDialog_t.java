 /*
  * Copyright © 2011 Diamond Light Source Ltd.
  * Contact :  ScientificSoftware@diamond.ac.uk
  * 
  * This is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  * 
  * This software is distributed in the hope that it will be useful, but 
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
  * Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along
  * with this software. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package uk.ac.diamond.scisoft.analysis.rcp.plotting;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.OutputStreamWriter;
 import java.text.DateFormat;
 import java.util.Date;
 import java.util.Iterator;
 
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Dialog;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.rcp.AnalysisRCPActivator;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.tools.PlotActionComplexEvent;
 import uk.ac.diamond.scisoft.analysis.rcp.plotting.tools.SelectedWindow;
import uk.ac.gda.common.rcp.util.DialogUtils;
 
 /**
  *
  */
 public class PlotDataTableDialog extends Dialog {
 
 	private Shell shell;
 	private PlotActionComplexEvent eventObj;
 	private Table tblData;
 	private Color grey;
 	
 	/**
 	 * @param parent
 	 */
 	public PlotDataTableDialog(Shell parent)
 	{
 		super(parent);
 	}
 	
 	/**
 	 * @param parent
 	 * @param event
 	 */
 	public PlotDataTableDialog(Shell parent, PlotActionComplexEvent event)
 	{
 		this(parent);
 		eventObj = event;
 	}
 	
 	private void fillTable(IDataset data, SelectedWindow window)
 	{
 		for (int i = 0; i < data.getSize(); i++)
 		{
 			double value = data.getDouble(i);
 			TableItem item = new TableItem(tblData, SWT.NONE);
 			item.setText(0,""+i);
 			item.setText(1,""+value);
 			if (i < window.getStartWindowX() || i > window.getEndWindowX())
 			{
 				item.setForeground(0,grey);
 
 				item.setForeground(1,grey);
 			}
 		}		
 	}
 	
 	private void fillTable(IDataset data, AxisValues axis, SelectedWindow window) 
 	{
 		Iterator<Double> iter = axis.iterator();
 		
 		for (int i = 0, imax = data.getSize(); i < imax; i++) 
 		{
 			double xvalue = iter.next();
 			double yvalue = data.getDouble(i);
 			TableItem item = new TableItem(tblData,SWT.NONE);
 			item.setText(0,""+xvalue);
 			item.setText(1,""+yvalue);
 			if (xvalue < window.getStartWindowX() || xvalue > window.getEndWindowX())
 			{
 				item.setForeground(0,grey);
 				item.setForeground(1,grey);
 			}
 		}
 	}
 	
 	/**
 	 * @return always true
 	 */
 	public boolean open() {
 		Shell parent = getParent();
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.MODELESS);
 		shell.setSize(418, 280);
 		shell.setText("Data table");
 		shell.setImage(AnalysisRCPActivator.getImageDescriptor("icons/information.png").createImage());
 		shell.setLayout(new GridLayout());
 		tblData = new Table(shell,SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION | SWT.VIRTUAL);
 		GridData gridData = new GridData(SWT.FILL,SWT.FILL,true, true);
 		gridData.heightHint = 280;	
 		tblData.setLayoutData(gridData);
 		tblData.setLinesVisible(true);
 		tblData.setHeaderVisible(true);
 		
 		final Composite buttons = new Composite(shell, SWT.NONE);
 		buttons.setLayoutData(new GridData(SWT.RIGHT,SWT.NONE,false,false));
 		buttons.setLayout(new RowLayout(SWT.HORIZONTAL));
 		
 		
 		Button btnExport = new Button(buttons, SWT.NONE);
 		btnExport.setText("Export...");
 		btnExport.setImage(AnalysisRCPActivator.getImageDescriptor("icons/folder_go.png").createImage());
 		btnExport.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				try {
 					exportFile();
 				} catch (Exception e1) {
 					e1.printStackTrace();
 				}
 			}
 		});
 
 		Button btnClose = new Button(buttons, SWT.NONE);
 		btnClose.setText("Close");
 		btnClose.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				shell.dispose();
 			}
 		});
 	
 		grey = shell.getParent().getDisplay().getSystemColor(SWT.COLOR_GRAY);
 		String[] titles = {"X value","Y value"};
 		for (int i = 0; i < titles.length; i++) {
 			TableColumn column = new TableColumn(tblData,SWT.NONE);
 			column.setText(titles[i]);
 			column.pack();
 			column.setWidth(180);
 		}
		DialogUtils.centerDialog(parent, shell);
 		shell.open();
 		IDataset data = eventObj.getDataSet();
 		AxisValues axis = eventObj.getAxisValue();
 		SelectedWindow window = eventObj.getDataWindow();
 		
 		if (data == null)
 			return true;
 		
 		if (axis == null)
 			fillTable(data,window);
 		else
 			fillTable(data,axis,window);
 		
 		Display display = parent.getDisplay();
 	
 		while (!shell.isDisposed())
 		{
 			if (!display.readAndDispatch()) display.sleep();
 		}
 		return true;
 	}
 
 	protected void exportFile() throws Exception {
 		
 		final FileDialog dialog = new FileDialog(shell, SWT.SAVE);
 		dialog.setFilterNames (new String [] {"Text Files", "All Files (*)"});
 		dialog.setFilterExtensions (new String [] {"*.txt;*.dat", "*"});
 		dialog.setFilterPath (System.getProperty("gda.data"));
 		final String name = (eventObj.getDataSet().getName()!=null&&!"".equals(eventObj.getDataSet().getName()))
 		                  ? eventObj.getDataSet().getName()
 		                  : "Export";
 		dialog.setFileName (name+".dat");
 		
         final String path = dialog.open();
         if (path == null) return;
 		
         // Write the data to a .dat file directly from the table for now    
         final File file = new File(path);
         if (!file.exists()) {
         	file.createNewFile();
         } else {
         	final boolean ok = MessageDialog.openConfirm(shell, "Confirm Overwrite", "Would you like to overwrite the existing file '"+file.getName()+"'?");
         	if (!ok) return;
         }
         
         final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
         try {
         	writer.write("# GDA data exported "+DateFormat.getDateTimeInstance().format(new Date()));
         	writer.newLine();
             writer.write("# x\ty");
         	writer.newLine();
             for (int i=0;i<tblData.getItemCount();++i) {
             	final TableItem item = tblData.getItem(i);
             	writer.write(item.getText(0));
             	writer.write("\t");
             	writer.write(item.getText(1));
             	writer.newLine();
            }
         } finally {
         	writer.close();
         }
 	}
 }
