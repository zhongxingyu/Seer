 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package jtoodle.api.request;
 
 import java.util.ArrayList;
 import java.util.List;
 import jtoodle.api.beans.BeanWriter;
 import jtoodle.api.beans.IdBean;
 import jtoodle.api.beans.Task;
 import jtoodle.api.intf.TaskConstants;
 
 /**
  *
  * @author justo
  */
 public class DeleteTasks extends AbstractAPIWebRequest<Task>
 implements TaskConstants {
 
 	public DeleteTasks() {
 		super( URI_DELETE_TASKS, Task.class );
 	}
 
 	public void setTasks( List<Task> tasks ) {
 		List<IdBean> idBeans = new ArrayList<>( tasks.size() );
 		idBeans.addAll( tasks );
 
 		super.setParameter(
 			TaskConstants.PARAM_DEL_TASKS_TASK_ID_LIST,
 			BeanWriter.writeIdList( idBeans )
 		);
 	}
 
 }
