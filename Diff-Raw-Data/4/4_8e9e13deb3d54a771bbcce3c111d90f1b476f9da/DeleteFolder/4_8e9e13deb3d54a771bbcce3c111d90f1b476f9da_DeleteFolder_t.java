 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package jtoodle.api.request;
 
 import jtoodle.api.beans.Folder;
 import jtoodle.api.intf.FolderConstants;
 
 /**
  *
  * @author justo
  */
 public class DeleteFolder extends AbstractAPIWebRequest<Folder>
 implements FolderConstants {
 
 	public DeleteFolder() {
 		super( URI_DELETE_FOLDERS, Folder.class );
 	}
 
 	public void setFolder( Folder folder ) {
 		super.setParameter(	PARAM_DEL_FOLDER_ID, folder.getId().toString() );
 	}
 
 }
