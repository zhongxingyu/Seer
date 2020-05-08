 package controller.view;
 
 import static org.junit.Assert.*;
 
 import model.Project;
 
 import org.junit.Test;
 
 import controller.ControllerCollection;
 
 import app.ProjectPlanner;
 
 import persistency.BaseTestDatabase;
 import view.ViewContainer;
 
 public class TestProjectMaintainance extends BaseTestDatabase{
 
 	private ControllerCollection controllers;
 	private ViewContainer viewContainer;
 	
 	private void init(){
 		this.controllers = new ControllerCollection(this.db, this.projectPlanner.getTimeService());
 		this.viewContainer = new ViewContainer();
 		
 		this.addProjects();
 		this.addDevelopers();
 	}
 	
 	@Test
 	public void testAssignManager() {
 		this.projectPlanner = new ProjectPlanner(this.db);
 		this.init();
 		
 		assertEquals(this.db.project().readAll().size(), this.db.project().count());

		Project testProject = this.db.project().readAll().get(0);
 		
 		int projectID = testProject.getId();
 		Project project = this.db.project().read(projectID);
 		
 		assertEquals(testProject.getName(), project.getName());
 		
 		
 		ProjectMaintainanceVC projectMaintainance = new ProjectMaintainanceVC(this.db, viewContainer, controllers, projectID);
 		fail("Not yet implemented");
 	}
 	
 	@Test
 	public void testSplitActivities(){
 		fail("Not yet implemented");
 	}
 	
 	@Test
 	public void testAddDeveloper(){
 		fail("Not yet implemented");
 	}
 	
 	@Test
 	public void testRequestAssistance(){
 		fail("Not yet implemented");
 	}
 	
 	@Test
 	public void testAddDevelopersToActivity(){
 		fail("Not yet implemented");
 	}
 
 }
