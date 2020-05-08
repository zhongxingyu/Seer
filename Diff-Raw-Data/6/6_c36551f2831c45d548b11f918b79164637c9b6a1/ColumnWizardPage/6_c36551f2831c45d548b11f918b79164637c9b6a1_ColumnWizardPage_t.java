 package am.lodge.code.scaffold.wizards.single;
 
 import java.sql.DatabaseMetaData;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.jface.wizard.IWizardPage;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.TreeItem;
 
 import am.lodge.code.scaffold.database.Column;
 import am.lodge.code.scaffold.database.Table;
 import am.lodge.code.scaffold.util.Constants;
 import am.lodge.code.scaffold.util.TreeUtils;
 import am.lodge.code.scaffold.wizards.BaseNewWizard;
 
 public class ColumnWizardPage extends WizardPage {
 
   private Tree columnTree;
 
   private String table;
 
   private String schem;
 
   private boolean update = false;
 
   public ColumnWizardPage(String title){
     super(title);
     setTitle(title);
     setPageComplete(false);
   }
 
   @Override
   public void createControl(Composite parent){
     Composite body = new Composite(parent, SWT.NONE);
     body.setLayout(new FormLayout());
     columnTree = new Tree(body, SWT.CHECK | SWT.BORDER);
     FormData formData = new FormData();
     formData.left = new FormAttachment(0, 0);
     formData.top = new FormAttachment(0, 0);
     formData.right = new FormAttachment(100, -1);
     formData.bottom = new FormAttachment(100, -1);
     columnTree.setLayoutData(formData);
     columnTree.addSelectionListener(new SelectionAdapter(){
       public void widgetSelected(SelectionEvent e){
         if(e.detail == SWT.CHECK){
           TreeItem item = (TreeItem)e.item;
           boolean oldState = isPageComplete();
           boolean checked = item.getChecked();
           TreeUtils.checkItems(item, checked);
           TreeUtils.checkPath(item.getParentItem(), checked, false);
           boolean state = getSelectColumns().size() > 0;
           setPageComplete(state);
           if(oldState != state)
             getWizard().getContainer().updateButtons();
         }
       }
     });
     setControl(body);
   }
 
   public void updateColumnTree(){
     DatabaseMetaData dmd = ((BaseNewWizard)getWizard()).getDatabaseMetaData();
     try{
       TableWizardPage wizard = (TableWizardPage)getPreviousPage();
       String _schem = wizard.getSelectSchem();
       String _table = wizard.getSelectTable();
       if(_schem.equals(schem) && _table.equals(table)){
         return;
       }
       schem = _schem;
       table = _table;
       columnTree.removeAll();
       TreeItem root = new TreeItem(columnTree, SWT.NONE);
       root.setText(table);
       Set<String> pks = new HashSet<String>();
       ResultSet rs = dmd.getPrimaryKeys(null, schem, table);
       while(rs.next()){
         pks.add(rs.getString("COLUMN_NAME"));
       }
 
       rs = dmd.getColumns(null, schem, table, "%");
       while(rs.next()){
         TreeItem item = new TreeItem(root, SWT.NONE);
         String name = rs.getString("COLUMN_NAME");
         item.setText(name);
         item.setData(Constants.SQL_TYPE, rs.getString("TYPE_NAME"));
         item.setData(Constants.COLUMN_SIZE, rs.getInt("COLUMN_SIZE"));
         item.setData(Constants.DECIMAL_DIGITS, rs.getInt("DECIMAL_DIGITS"));
         item.setData(Constants.NULLABLE, rs.getInt("NULLABLE") == 1);
         item.setData(Constants.DESC, rs.getString("REMARKS"));
         if(pks.contains(name))
         item.setData("pk", true);
       }
       update = true;
     }catch (SQLException e){
       throw new RuntimeException(e);
     }
   }
 
   private List<TreeItem> getSelectColumns(){
     List<TreeItem> itemList = new ArrayList<TreeItem>();
     TreeItem[] items = columnTree.getItems()[0].getItems();
     for(TreeItem item: items){
       if(item.getChecked()){
         itemList.add(item);
       }
     }
     return itemList;
   }
 
   private Map<String, Object> getData(){
     Map<String, Object> result = new HashMap<String, Object>();
     Set<String> imports = new HashSet<String>();
     result.put(Constants.IMPORTS, imports);
     Table tableData = new Table();
     result.put(Constants.TABLE, tableData);
     tableData.setName(table);
     String tableAliasname = ((TableWizardPage)getPreviousPage()).getAliasTableName();
     tableData.setAliasName(tableAliasname);
 
     TreeItem[] items = columnTree.getItems()[0].getItems();
     for(TreeItem item: items){
       Column column = new Column();
       column.setName(item.getText());
       column.setSqlType((String)item.getData(Constants.SQL_TYPE));
      column.setColumnSize((Integer)item.getData(Constants.COLUMN_SIZE));
      column.setDecimalDigits((Integer)item.getData(Constants.DECIMAL_DIGITS));
      column.setNullable((Boolean)item.getData(Constants.NULLABLE));
       column.setDesc((String)item.getData(Constants.DESC));
       
       if(item.getData("pk") != null){
         tableData.getPrimaryKeys().add(column);
       }else{
         tableData.getColumns().add(column);
       }
       if(item.getChecked()){
         if(Constants.IMPORT_MAP.get(column.getSqlType()) != null){
           imports.add(Constants.IMPORT_MAP.get(column.getSqlType()));
         }
         tableData.getSelectColumns().add(column);
       }
     }
     return result;
   }
 
   public IWizardPage getNextPage(){
     ColumnDescWizardPage wizard = (ColumnDescWizardPage)super.getNextPage();
     if(update){
       wizard.updateColumnData(getData());
       update = false;
     }
     return wizard;
   }
 }
