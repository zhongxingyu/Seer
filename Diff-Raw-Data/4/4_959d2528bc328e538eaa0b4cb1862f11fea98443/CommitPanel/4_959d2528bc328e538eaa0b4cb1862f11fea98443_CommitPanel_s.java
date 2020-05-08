 package view;
 
 import java.awt.*;
 import java.util.Vector;
 
 import javax.swing.*;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableModel;
 import javax.swing.tree.DefaultMutableTreeNode;
 
 public class CommitPanel extends JPanel {
 
 	private static final long serialVersionUID = 1L;
 	
 	private model.Model _model;
 	private JTable _table = null;
 	private Vector<Vector<String>> _tableData = null;
 	private TableModel _tableModel = null;
 	
 	public CommitPanel(model.Model model)
 	{		
 		_model = model;
 		setLayout(new BorderLayout());
 		Vector<String> headers = new Vector<String>();
 		headers.add(headers.size(),"Commit");
 		headers.add(headers.size(),"Author");
 		headers.add(headers.size(),"When");
 		headers.add(headers.size(),"Message");
 		
 		_tableData = model.commit.getCommitData(_model.references.getCurrentReference());
 		_table = new JTable(_tableData,headers);
		
 		JScrollPane scrollPane = new JScrollPane(_table);
		add(scrollPane,BorderLayout.PAGE_START);
 	}
 	
 	public void refresh()
 	{
 		DefaultTableModel tableModel = (DefaultTableModel)_table.getModel();
 		int numRows = tableModel.getRowCount();
 		for (int n=0;n<numRows;n++)
 		{
 			tableModel.removeRow(0);
 		}
 		_tableData = _model.commit.getCommitData(_model.references.getCurrentReference());
 		for (Vector<String> row : _tableData)
 		{
 			tableModel.addRow(row);
 		}
 		tableModel.fireTableDataChanged();
 	}
 }
