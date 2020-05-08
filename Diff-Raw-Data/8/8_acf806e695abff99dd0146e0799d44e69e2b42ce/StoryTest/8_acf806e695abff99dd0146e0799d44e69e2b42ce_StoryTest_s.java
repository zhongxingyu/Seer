 package br.com.xisp.test.models;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import junit.framework.Assert;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import br.com.xisp.datemath.DateMath;
 import br.com.xisp.datemath.InvalidMaskException;
 import br.com.xisp.models.Project;
 import br.com.xisp.models.Status;
 import br.com.xisp.models.Story;
 import br.com.xisp.models.TypeStory;
 import br.com.xisp.models.User;
 import br.com.xisp.utils.StoryUtil;
 
 public class StoryTest {
 	
 	private Story story;
 	
 	@Before
 	public void setUp(){
 		story = new Story();
 	}
 	
 	@Test
 	public void testShouldReturnStatusRDD(){
 		story.setName("Build a Tower");
 		TypeStory t = givenAType();
 		story.setTypeStory(t);
 		story.setStatus(Status.IN_DEV);
 		story.setDescription("Figure out how build a tower");
 		Assert.assertEquals("Em Dev", story.getStatus().getStatus());
 		Assert.assertEquals("Funcionalidade", story.getTypeStory().getType());
 	}
 
 	@Test
 	public void testStoryOne(){
 		Project project = givenAProject();
 		Story story = givenAStory(project);
 		Assert.assertEquals("Edipo", story.getCreatedBy().getName());
 		Assert.assertEquals("Test Project", story.getProject().getName());
 	}
 	
 	@Test
 	public void testStoryChangeStatus(){
 		Project project = givenAProject();
 		Story story = givenAStory(project);
 		Assert.assertEquals("Nao Iniciada",story.getStatus().getStatus());
 		story.setStatus(Status.READY_FOR_TEST);
 		Assert.assertEquals("Pronta para Testes",story.getStatus().getStatus());
 	}
 	
 	@Test
 	public void testStoryWhenInDevShouldFillStartAt(){
 		Project project = givenAProject();
 		Story story = givenAStory(project);
 		Assert.assertEquals("Nao Iniciada",story.getStatus().getStatus());
 		story.setStatus(Status.IN_DEV);
 		Assert.assertEquals("Em Dev",story.getStatus().getStatus());
 		Assert.assertEquals(new Date(), story.getStartedAt());
 	}
 	
 	@Test
 	public void testFinishStory(){
 		Project project = givenAProject();
 		Story story = givenAStory(project);
 		story.markAsCompleted();
 		Assert.assertEquals("Finalizada", story.getStatus().getStatus());
 	}
 	
 	@Test
 	public void testStoryShouldCreateAsNotStarted(){
 		Project project = givenAProject();
 		Story story = givenAStory(project);
 		Assert.assertEquals("Nao Iniciada", story.getStatus().getStatus());
 	}
 	
 	@Test
 	public void testShouldSetCurrentDateAsEndDateWhenStoryFinish(){
 		Project project = givenAProject();
 		Story story = givenAStory(project);
 		story.setStatus(Status.FINISHED);
 		Assert.assertEquals(new Date(),story.getEndAt());
 	}
 	
 	@Test
 	public void testShouldNotUpdateEndDateWhenStoryBackToTheDevArea(){
 		Date startDate = new Date();
 		startDate.setTime(2);
 		Project project = givenAProject();
 		Story story = givenAStory(project);
 		story.setStartedAt(startDate);
 		story.setStatus(Status.FINISHED);
 		story.setStatus(Status.IN_DEV);
 		Assert.assertSame(startDate, story.getStartedAt());
 	}
 	
 	@Test
 	public void testShouldReturnAGVAllStoriesIteration() throws InvalidMaskException{
 		Date start  = new DateMath().on("29/10/2011 10:30", "dd/MM/yyyy HH:mm").result();
 		Date end = new DateMath().on("02/11/2011 10:30", "dd/MM/yyy HH:mm").result();
 		Story s1  = givenAStoryWithName(givenAProject(), "Um");
 		s1.setStartedAt(start);
 		s1.setEndAt(end);
 		
 		List<Story> list = new ArrayList<Story>();
 		list.add(s1);
 		
		Assert.assertEquals("Media de dias estorias finalizadas: 3.",StoryUtil.calculeAvgForStories(list));
 		
 	}
 	
 	@Test
 	public void testShouldReturnAGVAllStoriesWithOneWitoutEndDate() throws InvalidMaskException{
 		Date start  = new DateMath().on("29/10/2011 10:30", "dd/MM/yyyy HH:mm").result();
 		Date end = new DateMath().on("02/11/2011 10:30", "dd/MM/yyy HH:mm").result();
 		Story s1  = givenAStoryWithName(givenAProject(), "Um");
 		s1.setStartedAt(start);
 		s1.setEndAt(end);
 		
 		Date start1  = new DateMath().on("29/10/2011 10:30", "dd/MM/yyyy HH:mm").result();
 		
 		Story s2  = givenAStoryWithName(givenAProject(), "Um");
 		s2.setStartedAt(start1);
 		
 		List<Story> list = new ArrayList<Story>();
 		list.add(s1);
 		list.add(s2);
 		
		Assert.assertEquals("Media de dias estorias finalizadas: 3.",StoryUtil.calculeAvgForStories(list));
 		
 	}
 	
 	@Test
 	public void testShouldReturnAGVAllStoriesIteration2() throws InvalidMaskException{
 		Date start  = new DateMath().on("29/10/2011 10:30", "dd/MM/yyyy HH:mm").result();
 		Date end = new DateMath().on("02/11/2011 10:30", "dd/MM/yyy HH:mm").result();
 		Story s1  = givenAStoryWithName(givenAProject(), "Um");
 		s1.setStartedAt(start);
 		s1.setEndAt(end);
 		
 		Date start1  = new DateMath().on("29/10/2011 10:30", "dd/MM/yyyy HH:mm").result();
 		Date end1 = new DateMath().on("08/11/2011 10:30", "dd/MM/yyy HH:mm").result();
 		
 		Story s2  = givenAStoryWithName(givenAProject(), "Um");
 		s2.setStartedAt(start1);
 		s2.setEndAt(end1);
 		
 		List<Story> list = new ArrayList<Story>();
 		list.add(s1);
 		list.add(s2);
 		
		Assert.assertEquals("Media de dias estorias finalizadas: 6.", StoryUtil.calculeAvgForStories(list));
 		
 	}
 
 	private Project givenAProject() {
 		Project project = new Project();
 		project.setId(1L);
 		project.setName("Test Project");
 		project.setDescription("Description of Test Project");
 		return project;
 	}
 	
 	private Story givenAStory(final Project project){
 		 Story story = new Story();
 		 story.setCreatedBy(givenAUser());
 		 story.setName("Create a Crud for Users");
 		 story.setDescription("Here Description for the user story");
 		 story.setProject(project);
 		 return story;
 	}
 
 	private Story givenAStoryWithName(final Project project, String name){
 		 Story story = new Story();
 		 story.setCreatedBy(givenAUser());
 		 story.setName(name);
 		 story.setDescription("Here Description for the user story");
 		 story.setProject(project);
 		 return story;
 	}
 	
 	private User givenAUser() {
 		User user =new User();
 		user.setName("Edipo");
 		user.setEmail("edipofederle@gmail.com");
 		user.setPassword("edipo");
 		return user;
 	}
 	
 	private TypeStory givenAType() {
 		TypeStory type = new TypeStory();
 		type.setType("Funcionalidade");
 		return type;
 	}
 	
 }
