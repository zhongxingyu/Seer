 package model;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import utility.*;
 import javax.swing.JLabel;
 
 
 import view.*;
 
 public class TaskModel implements Serializable{
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 3243208829863896513L;
 
 
 	private enum Priority{
 		HIGH_PRIORITY, NO_PRIORITY
 	}
 	
 	private boolean checked;
 	private String title;
 	private String description;
 	private Priority prio;
 	private CategoryModel catModel;
 	private EditTaskPanel editTaskPanel;
 	private String deadline;
 	private JLabel deadlineLabel = new JLabel();
 	//private List<TaskModel> uncheckedTaskList = new ArrayList<TaskModel>();
 	//we will have to find a way to keep track of unchecked and checked tasks
 	//I think it will be too many lists with stuff soon...
 	
 	public TaskModel(String title){
 		this.title = title;
 		this.checked = false;
 		this.prio = Priority.NO_PRIORITY;
 	}
 	
 	public TaskModel(String title, CategoryModel catModel){
 		this(title);
 		this.catModel = catModel;
 		catModel.getTaskList().add(this);
 		
<<<<<<< HEAD
 		if(AllTaskListModel.getInstance().contains(this)){
 			AllTaskListModel.getInstance().remove(this);
 		}
 		AllTaskListModel.getInstance().add(this);
 		Save.saveFiles();
 		
=======
 		if(!AllTaskListModel.getInstance().contains(this)){
 			AllTaskListModel.getInstance().add(this);
 		}
 //			Save.saveFiles();
//		}
>>>>>>> fca1811f261e90b5b8c3d4613ece6464e01df881
 		//this.uncheckedTaskList.add(this);
 	}
 	
 	
 	public void changePriority(){				//We probably will be needing a controller-class for the tasks, doing this kind of stuff but not yet! :D
 		switch (prio) {
 			case NO_PRIORITY:
 				prio = Priority.HIGH_PRIORITY;
 				break;
 			case HIGH_PRIORITY:
 				prio = Priority.NO_PRIORITY;
 				break;
 			default:
 				prio = this.getPrio();
 				break;
 		}
 			
 	}
 	
 	public void changeState(){
 		checked = !checked;
 	}
 	
 	public boolean isChecked(){
 		return checked;
 		
 	}
 	
 	public Priority getPrio(){
 		return prio;
 	}
 	
 	public String getTitle(){
 		return title;
 	}
 	
 	public String getDescription(){
 		return description;
 	}
 	
 	public void setTitle(String title){
 		this.title=title;
 	}
 	
 	public void setDescription(String description){
 		this.description=description;
 	}
 	
 	public void setPrio(Priority prio){
 		this.prio=prio;
 	}
 	public void setDeadline(String deadline){
 		this.deadline=deadline;
 	}
 	public void setDeadlineLabel(String deadline){
 		deadlineLabel.setText(deadline);
 	}
 	
 	public String getDeadline(){
 		if(deadline==null){
 			return "No deadline for this task";
 		}else{
 		return deadline;
 		}
 	}
 
 	public CategoryModel getCategory(){
 		return catModel;
 	}
 	 
 	
 	//public StartCategoryPanel getCatPanel(){
 		//return catPanel;
 	//}
 	
 	
 	public void changeCategory(CategoryModel catModel){
 		catModel.getTaskList().remove(this);
 		CategoryModel model = catModel;
 		model.getTaskList().add(this);
 	}
 
 
 }
 
 
