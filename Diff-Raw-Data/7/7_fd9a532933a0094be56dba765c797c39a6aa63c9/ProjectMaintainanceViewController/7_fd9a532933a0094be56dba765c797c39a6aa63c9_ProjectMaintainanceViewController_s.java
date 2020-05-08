 package controller.view;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.concurrent.TimeUnit;
 
 import model.Activity;
 import model.Developer;
 import model.Project;
 import persistency.Database;
 import utils.ActionUtils;
 import utils.Dialog;
 import utils.DialogChoice;
 import utils.TimeService;
 import view.ViewContainer;
 import view.state.AbstractViewState;
 import view.state.ProjectMaintainanceViewState;
 import controller.ControllerCollection;
 import controller.action.ChangeViewAction;
 import factory.ViewControllerFactory;
 
 public class ProjectMaintainanceViewController extends AbstractViewController {
 
 	private Project project;
 	private ProjectMaintainanceViewState viewState;
 	
 	public ProjectMaintainanceViewController(Database database, ViewContainer viewContainer, ControllerCollection controllers, int projectID){
 		super(database,viewContainer,controllers);
 		this.project = this.database.project().read(projectID);
 	}
 
 	@Override
 	public AbstractViewState getViewState() {
 		return viewState;
 	}
 
 	@Override
 	public void initialize() {
 		this.viewState = new ProjectMaintainanceViewState(); 
 		this.viewState.setProjectName(this.project.getName());
 		
 		this.viewState.getBackButton().addActionListener(new ChangeViewAction(this.viewContainer, ViewControllerFactory.CreateProjectsViewController()));
 		ActionUtils.addListener(this.viewState.getAddDevButton(), this, "addDeveloper");
 		ActionUtils.addListener(this.viewState.getAssManagerButton(), this, "assignManager");
 		ActionUtils.addListener(this.viewState.getAddActivityButton(), this, "addActivity");
 		this.viewState.getCreateRapportButton().addActionListener(new ChangeViewAction(this.viewContainer, ViewControllerFactory.CreateProjectRapportViewController(this.project.getId())));
 		
 		this.fillActivityList();
 		this.fillManagerList();
 	}
 	
 	public void addDeveloper(){
 		Activity selectedActivity = this.viewState.getSelectedActivity();
 		Developer selectedDev = this.viewState.getSelectedDeveloper();
 		
 		if(selectedActivity == null){
 			Dialog.message("Please select an activity to add a developer to.");
 			return;
 		}else if(selectedDev == null){
 			Dialog.message("Please select a developer to add to the selected activity.");
 			return;
 		}
 		selectedActivity.addDeveloper(selectedDev);
 		
 //update view		
 		this.viewContainer.setViewState(ViewControllerFactory.CreateProjectMaintainanceViewController(this.project.getId()));
 	}
 	
 	public void assignManager(Developer dev){
 		boolean setManager = false;
 		if(this.project.getManager() != null)
 		{
 			DialogChoice choice = Dialog.confirm("This project already has a manager, " + this.project.getManager().getName() + ", do you want him/her replaced?");
 			if(choice.equals(DialogChoice.Yes))
 				setManager = true;
 		}else if(dev != null)
 			setManager = true;
 		
 		if(setManager)
 			this.project.setManager(dev);
 	}
 	
 //TODO 
 	public void assignManager(){
 		Developer developerInput = this.viewState.getSelectedDeveloper();
 		this.assignManager(developerInput);
 	}
 	
 	public Project getProject(){
 		return this.project;
 	}
 	
 	private void fillManagerList() {
 		this.viewState.setManagers(this.database.developer().readAll());
 	}
 	
 	private void fillActivityList() {
 		this.viewState.setActivities(this.database.activity().readAllWhereEquals("project_id", this.project.getId()));
 	}
 	
 	public void addActivity() {
 		String name = this.viewState.getNameInput();
 		int hourBudget = Integer.valueOf(this.viewState.getHourBudgetInput());
 		String deadline = this.viewState.getDeadlineInput();
 		
 		DateFormat format = new SimpleDateFormat("dd-MM-yyyy");
 		try {
 			Calendar deadlineDate = Calendar.getInstance();
 			deadlineDate.setTime(format.parse(deadline.trim()));
 			this.addNewActivity(name, hourBudget, 0, deadlineDate.getTimeInMillis());
 			this.fillActivityList();
 		} catch (ParseException e) {
 			Dialog.message("Invalid date format, must use dd-mm-yyy");
 		}
 	}
 	
 	public void addNewActivity(String description, int expectedTime, long startTime, long endTime){
 		this.database.activity().createProjectActivity(this.project.getId(), description, expectedTime, startTime, endTime);
 	}
 }
