 /**
  * 
  */
 package org.cotrix.web.importwizard.client.resources;
 
 import com.google.gwt.core.shared.GWT;
 import com.google.gwt.user.cellview.client.DataGrid;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public interface DataGridResource extends DataGrid.Resources {
 
 	public static DataGridResource INSTANCE = GWT.create(DataGridResource.class);
 	
 
 	@Source("datagrid.css")
	DataGrid.Style dataGridStyle();
 
 }
