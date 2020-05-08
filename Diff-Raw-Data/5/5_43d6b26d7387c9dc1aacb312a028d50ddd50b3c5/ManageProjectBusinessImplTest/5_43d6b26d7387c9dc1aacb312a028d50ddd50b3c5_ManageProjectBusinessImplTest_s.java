 package edu.cmu.square.server.business.implementation;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNotSame;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Map;
 
 import javax.annotation.Resource;
 
 import junit.framework.Assert;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.test.annotation.Rollback;
 import org.springframework.transaction.annotation.Transactional;
 
 import edu.cmu.square.client.exceptions.SquareException;
 import edu.cmu.square.client.model.GwtEvaluation;
 import edu.cmu.square.client.model.GwtInspectionTechnique;
 import edu.cmu.square.client.model.GwtProject;
 import edu.cmu.square.client.model.GwtRole;
 import edu.cmu.square.client.model.GwtStep;
 import edu.cmu.square.client.model.GwtTechnique;
 import edu.cmu.square.client.model.GwtTerm;
 import edu.cmu.square.client.model.GwtUser;
 import edu.cmu.square.client.model.StepStatus;
 import edu.cmu.square.server.base.AbstractSpringBase;
 import edu.cmu.square.server.business.interfaces.ManageProjectBusiness;
 import edu.cmu.square.server.business.step.interfaces.AgreeOnDefinitionsBusiness;
 import edu.cmu.square.server.business.step.interfaces.ElicitationTechniqueBusiness;
 import edu.cmu.square.server.business.step.interfaces.InspectionTechniqueBusiness;
 import edu.cmu.square.server.dao.interfaces.UserDao;
 import edu.cmu.square.server.dao.interfaces.UserProjectRoleDao;
 import edu.cmu.square.server.dao.model.Project;
 import edu.cmu.square.server.dao.model.Role;
 import edu.cmu.square.server.dao.model.User;
 import edu.cmu.square.server.dao.model.UserProjectRole;
 
 
 
 public class ManageProjectBusinessImplTest extends AbstractSpringBase
 {
 	@Resource
 	private ManageProjectBusiness mps;
 	@Resource
 	private UserDao userDao;
 
 	@Resource
 	private UserProjectRoleDao userProjectRoleDao;
 	
 	@Resource
 	private AgreeOnDefinitionsBusiness termBusiness;
 
 	@Resource
 	private ElicitationTechniqueBusiness elicitationTechniqueBusiness;
 
 	@Resource
 	private InspectionTechniqueBusiness inspectionTechniqueBusiness;
 	
 	private Project testProject;
 
 	private User testUser;
 
 	private Role testRole;
 	Map<String, Object> testMap;
 	
 	
 	@Before
 	public void setUp()
 	{
 		testMap = super.createUserProjectforRole();
 		testProject = (Project) testMap.get("project");
 		testUser = (User) testMap.get("user");
 		testRole = (Role) testMap.get("role");
 		userDao.addUserToProject(testUser, testProject, testRole);
 		mps.setProjectName(testProject.getName());
 		mps.setUserName(testUser.getUserName());
 
 	}
 
 
 	@Test
 	public void testAddUserToProject()
 	{
 		try
 		{
 			GwtProject gwtProject = ((Project) testMap.get("project")).createGwtProject();
 			GwtUser gwtUser = ((User) testMap.get("user")).createGwtUser();
 			GwtRole gwtRole = ((Role) testMap.get("role")).createGwtRole();
 
 			mps.addUserToProject(gwtProject, gwtUser, gwtRole);
 		}
 		catch (Exception e)
 		{
 			fail("exception " + e.getMessage());
 		}
 	}
 	@Test
 	public void testCreateProject()
 	{
 		try
 		{
 
 			GwtUser gwtUser = ((User) testMap.get("user")).createGwtUser();
 			GwtProject project = new GwtProject();
 
 			project.setName("Project1");
 			project.setSecurity(true);
 			project.setPrivacy(false);
 			project.setLite(false);
 			project.setAcquisitionOrganizationEngineer(gwtUser);
 
			GwtProject outputProject = mps.createProject(project);
 			outputProject = mps.getProject(outputProject.getId());
 	
 			Assert.assertEquals(outputProject.getName(), project.getName());
 			Assert.assertEquals(outputProject.isSecurity(), true);
 			Assert.assertEquals(outputProject.isPrivacy(), false);
 			Assert.assertEquals(outputProject.isLite(), false);
 
 			
 			Assert.assertEquals(project.getName(), outputProject.getName());
 			
 			Assert.assertTrue(outputProject.isSecurity());
 			Assert.assertFalse(outputProject.isPrivacy());
 			Assert.assertFalse(outputProject.isLite());
 			
 			//Validated that the steps are created for security project
 			List<GwtStep>steps = mps.getSteps(outputProject);
 			Assert.assertEquals(steps.size(), 9);
 			for(GwtStep s: steps)
 			{
 				Assert.assertEquals(s.getStatus(),StepStatus.NotStarted);
 			
 			}
 			
 			//Validated that the user was added to the project
 			List<GwtUser> users = mps.getUserList(outputProject);
 			Assert.assertEquals(1, users.size());
 			Assert.assertEquals("Acquisition Organization Engineer", users.get(0).getRole());
 			Assert.assertEquals(gwtUser.getUserId().intValue(), users.get(0).getUserId().intValue());
 			
 			GwtUser leader = outputProject.getAcquisitionOrganizationEngineer();
 			Assert.assertEquals(gwtUser.getFullName(), leader.getFullName());
 			Assert.assertEquals(gwtUser.getUserId(), leader.getUserId());
 			
 			List<GwtTerm> terms = termBusiness.getTerms(outputProject);
 			Assert.assertEquals(5, terms.size());   
 			
 			List<GwtTechnique> techniques = elicitationTechniqueBusiness.getTechniques(outputProject,null);
 			Assert.assertEquals(5, techniques.size());   
 			
 			List<GwtEvaluation> evaluations = elicitationTechniqueBusiness.getEvaluations(outputProject,null);
 			Assert.assertEquals(5, evaluations.size());  
 			
 			List<GwtInspectionTechnique> inspections = inspectionTechniqueBusiness.getInspectionTechniques(outputProject.getId(),null);
 			Assert.assertEquals(5, inspections.size());  
 			
 			
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 
 	}
 	
 	@Test
 	public void testCreateStepsProject()
 	{
 		try
 		{
 
 			GwtUser gwtUser = ((User) testMap.get("user")).createGwtUser();
 			GwtProject project = new GwtProject();
 
 			project.setName("Project1");
 			project.setSecurity(true);
 			project.setPrivacy(false);
 			project.setLite(false);
 			project.setAcquisitionOrganizationEngineer(gwtUser);
 			
			project =mps.createProject(project);
 			
 			
 
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 
 	}
 	
 
 	@Test
 	public void testEditRole()
 	{
 		GwtUser gwtUser = ((User) testMap.get("user")).createGwtUser();
 		GwtProject gwtProject = ((Project) testMap.get("project")).createGwtProject();
 		// User u = userDao.fetch(gwtUser.getUserId());
 
 		try
 		{
 
 			User user = (User) testMap.get("user");
 			Project project = (Project) testMap.get("project");
 			Role role = (Role) testMap.get("role");
 			userDao.addUserToProject(user, project, role);
 
 			String roleStr = "Acquisition Organization Engineer";
 			gwtUser.setRole(roleStr);
 			mps.setUserName(user.getUserName());
 			mps.setProjectName(project.getName());
 			mps.editRole(gwtUser, gwtProject);
 
 			List<UserProjectRole> uprList = userProjectRoleDao.getByUserProject(user, project);
 			assertTrue(!uprList.isEmpty());
 			assertTrue(uprList.get(0).getUser().equals(user));
 
 		}
 		catch (SquareException e)
 		{
 			throw new RuntimeException(e);
 		}
 	}
 
 
 	@Test
 	public void testGetAllRoles()
 	{
 		try
 		{
 			List<Role> roles = mps.getAllRoles();
 			assertNotNull(roles);
 			assertTrue(roles.size() > 0);
 		}
 		catch (SquareException e)
 		{
 
 			fail("Error " + e.getMessage());
 		}
 	}
 
 	
 	@Test
 	public void testGetAllUsersForAutoComplete()
 	{
 
 	}
 
 	
 	@Test
 	public void testGetStepStatuses()
 	{
 
 	}
 
 	
 /*	@Test
 	@Transactional
 	@Rollback(true)
 	public void testGetStepsForSquareSecurity()
 	{
 
 		GwtProject project = new GwtProject();
 		project.setId(38);
 		try
 		{
 			List<GwtStep> steps = mps.getSteps(project);
 			Collections.sort(steps, new Comparator<GwtStep>()
 				{
 					@Override
 					public int compare(GwtStep stepA, GwtStep stepB)
 					{
 						return stepA.getDescription().compareTo(stepB.getDescription());
 					}
 				});
 
 			assertNotNull(steps);
 			assertTrue(steps.size() == 9);
 
 			// step 1
 			assertTrue(steps.get(0).isSecurity());
 			assertTrue(steps.get(0).isPrivacy());
 
 			// step 2
 			assertTrue(steps.get(1).isSecurity());
 			assertTrue(steps.get(1).isPrivacy());
 
 			// step 3
 //			assertTrue(steps.get(2).isSecurity());
 //			assertTrue(steps.get(2).isPrivacy());
 
 			// step 4
 			assertTrue(steps.get(3).isSecurity());
 			assertTrue(!steps.get(3).isPrivacy());
 
 			// step 5
 			assertTrue(steps.get(4).isSecurity());
 			assertTrue(!steps.get(4).isPrivacy());
 
 			// step 6
 			assertTrue(steps.get(5).isSecurity());
 			assertTrue(!steps.get(5).isPrivacy());
 
 			// step 7
 			assertTrue(steps.get(6).isSecurity());
 			assertTrue(steps.get(6).isPrivacy());
 
 			// step 8
 			assertTrue(steps.get(7).isSecurity());
 			assertTrue(steps.get(7).isPrivacy());
 
 			// step 9
 			assertTrue(steps.get(8).isSecurity());
 			assertTrue(steps.get(8).isPrivacy());
 
 		}
 		catch (SquareException e)
 		{
 			fail("Error " + e.getMessage());
 		}
 	}
 */
 	
 	@Test
 	public void testGetStepsForSquarePrivacy()
 	{
 
 	}
 
 	
 	@Test
 	public void testGetStepsForSquareSecurityPrivacy()
 	{
 
 	}
 
 	
 	
 	// ///Square-lite//////
 	/**
 	 * Test method for
 	 * {@link edu.cmu.square.server.business.implementation.ManageProjectBusinessImpl#getSteps(edu.cmu.square.client.model.GwtProject)}
 	 * .
 	 */
 	@Test
 	public void testGetStepsForSquareLiteSecurity()
 	{
 
 	}
 
 	/**
 	 * Test method for
 	 * {@link edu.cmu.square.server.business.implementation.ManageProjectBusinessImpl#getSteps(edu.cmu.square.client.model.GwtProject)}
 	 * .
 	 */
 	@Test
 	public void testGetStepsForSquareLitePrivacy()
 	{
 
 	}
 
 	/**
 	 * Test method for
 	 * {@link edu.cmu.square.server.business.implementation.ManageProjectBusinessImpl#getSteps(edu.cmu.square.client.model.GwtProject)}
 	 * .
 	 */
 	@Test
 	public void testGetStepsForSquareLiteSecurityPrivacy()
 	{
 
 	}
 
 	/**
 	 * Test method for
 	 * {@link edu.cmu.square.server.business.implementation.ManageProjectBusinessImpl#getUserList(Project)}
 	 * .//FIXME:Tests not written
 	 */
 	@Test
 	public void testGetUserList()
 	{
 		try
 		{
 			GwtUser gwtUser = ((User) testMap.get("user")).createGwtUser();
 			GwtProject gwtProject = ((Project) testMap.get("project")).createGwtProject();
 			GwtRole gwtRole = ((Role) testMap.get("role")).createGwtRole();
 			mps.addUserToProject(gwtProject, gwtUser, gwtRole);
 
 			List<GwtUser> userList = mps.getUserList(gwtProject);
 			assertTrue(userList.size() >= 1);
 		}
 		catch (Exception e)
 		{
 			fail("error " + e.getMessage());
 		}
 	}
 
 	/**
 	 * Test method for
 	 * {@link edu.cmu.square.server.business.implementation.ManageProjectBusinessImpl#removeUserFromProject(edu.cmu.square.client.model.GwtProject, edu.cmu.square.client.model.GwtUser)}
 	 * .
 	 */
 	@Test
 	public void testRemoteUserFromProject()
 	{
 
 	}
 
 	/**
 	 * Test method for
 	 * {@link edu.cmu.square.server.business.implementation.ManageProjectBusinessImpl#updateProjectDetails(edu.cmu.square.client.model.GwtProject)}
 	 * .
 	 */
 	@Test
 	public void testUpdateProjectName()
 	{
 		try
 		{
 			GwtProject gwtProject = ((Project) testMap.get("project")).createGwtProject();
 			gwtProject.setName("I changed my name");
 			GwtProject p = mps.getProject(gwtProject.getId());
 			assertNotSame(p.getName(), gwtProject.getName());
 			mps.updateProjectName(gwtProject);
 			GwtProject p1 = mps.getProject(gwtProject.getId());
 			assertEquals(p1.getName(), gwtProject.getName());
 		}
 		catch (Throwable t)
 		{
 			fail("Error " + t.getMessage());
 		}
 
 	}
 
 }
