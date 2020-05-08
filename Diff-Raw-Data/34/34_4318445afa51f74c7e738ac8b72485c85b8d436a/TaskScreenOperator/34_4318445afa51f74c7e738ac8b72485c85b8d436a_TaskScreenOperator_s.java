 package tasks.adapters.ui.operators;
 
 import java.awt.event.KeyEvent;
 
 import org.netbeans.jemmy.operators.JButtonOperator;
 import org.netbeans.jemmy.operators.JDialogOperator;
 import org.netbeans.jemmy.operators.JTextFieldOperator;
 
 public class TaskScreenOperator {
 
 	
 	private final JDialogOperator frame;
 	private final JTextFieldOperator jiraKeyTextField;
 	private JTextFieldOperator taskNameTextField;
 
 	public TaskScreenOperator(){
 		frame = new JDialogOperator("Task edition");
 		jiraKeyTextField = new JTextFieldOperator(frame,0);
 		taskNameTextField = new JTextFieldOperator(frame,1);
 	}
 
 	public void setTaskName(String taskNewName) {
 		taskNameTextField.setText(taskNewName);		
 		taskNameTextField.waitText(taskNewName);
 	}
 
 	public void clickOk() {
 		new JButtonOperator(frame).push();
 	}
 
 	public void setTaskNameAndOk(String taskName) {
 		setTaskName(taskName);
 		clickOk();
 		
 	}
 
 	public void setJiraKey(String jiraKey) {
 		jiraKeyTextField.setText(jiraKey);
 		jiraKeyTextField.pressKey(KeyEvent.VK_ENTER);
 		
 	}
 
 	public void waitJiraKey(String jiraKey) {
 		jiraKeyTextField.waitText(jiraKey);		
 	}
 
	public void assertName(String name) {
 		taskNameTextField.waitText(name);		
 	}
 	
 }
