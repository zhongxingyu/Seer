 package br.com.xisp.test.controllers;
 
 import java.util.Calendar;
 import java.util.Date;
 
 import junit.framework.Assert;
 
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 import org.junit.Before;
 import org.junit.Test;
 
 import br.com.caelum.vraptor.util.test.MockResult;
 import br.com.caelum.vraptor.util.test.MockValidator;
 import br.com.xisp.controllers.InterationsController;
 import br.com.xisp.models.Interation;
 import br.com.xisp.models.Project;
 import br.com.xisp.repository.InteractionRepository;
 import br.com.xisp.repository.ProjectRepository;
 import br.com.xisp.session.ProjectSession;
 
 public class InterationControllerTest {
 	
 	
 
 	private Mockery mockery;
 	private MockResult result;
 	private ProjectRepository projectRepo;
 	private InteractionRepository interationRepo;
 	private ProjectSession projectSession;
 	private InterationsController controller;
 	private Project project;
 	
 
 	@Before
 	public void setUp() throws Exception {
 		project = givenAProject();
 		mockery = new Mockery();
 		result = new MockResult();
 		projectRepo = mockery.mock(ProjectRepository.class);
 		interationRepo = mockery.mock(InteractionRepository.class);
 		projectSession = mockery.mock(ProjectSession.class);
 		result = new MockResult();
		controller = new InterationsController(interationRepo,projectRepo,projectSession, result, new MockValidator());
 	}
 	
 	@Test
 	public void shouldCreateInteration(){
 		Interation interationOne = givenAInteration("One Interation", project);
 		willCreateANewInteration(interationOne);
 		controller.save(interationOne);
 		Assert.assertNotNull(result.included("successInteration"));
 	}
 	
 	private void willCreateANewInteration(final Interation interation) {
 		mockery.checking(new Expectations() {
 			{
 				one(projectSession).getProject();will(returnValue(project));
 				one(interationRepo).add(interation);
 			}
 		});
 	}
 
 	private Interation givenAInteration(String name, Project project) {
 		Interation interation = new Interation();
 		interation.setName(name);
 		Date minhaData = new Date();  
 		Calendar calendar = Calendar.getInstance();  
 		calendar.setTime(minhaData);
 		// incrementa minha data mais sete dias  
 		calendar.add(Calendar.DAY_OF_MONTH, 1); 
 		interation.setStartDate(calendar.getTime());
 		Date minhaDataend = new Date();  
 		Calendar calendarend = Calendar.getInstance();  
 		calendar.setTime(minhaDataend);
 		// incrementa minha data mais sete dias  
 		calendar.add(Calendar.DAY_OF_MONTH, 2); 
 		interation.setEndDate(calendarend.getTime());
 		interation.setProject(project);
 		return interation;
 	}
 
 	private Project givenAProject() {
 		Project project = new Project();
 		project.setId(1L);
 		project.setName("Test Project");
 		project.setDescription("Description of Test Project");
 		return project;
 	}
 	
 }
