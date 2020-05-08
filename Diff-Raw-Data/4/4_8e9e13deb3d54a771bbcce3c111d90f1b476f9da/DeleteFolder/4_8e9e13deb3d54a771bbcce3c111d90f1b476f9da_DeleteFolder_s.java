 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package jtoodle.api.request;
 
import java.io.IOException;
import java.util.List;
import jtoodle.api.beans.BeanParser;
import jtoodle.api.beans.JToodleException;
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
