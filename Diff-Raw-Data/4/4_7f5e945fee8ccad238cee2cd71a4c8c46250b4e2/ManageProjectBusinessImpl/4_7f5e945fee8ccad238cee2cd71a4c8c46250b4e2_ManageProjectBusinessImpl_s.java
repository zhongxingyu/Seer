 package edu.cmu.square.server.business.implementation;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
 import javax.annotation.Resource;
 
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Service;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
import com.google.gwt.dev.util.collect.HashMap;

 import edu.cmu.square.client.exceptions.ExceptionType;
 import edu.cmu.square.client.exceptions.SquareException;
 import edu.cmu.square.client.model.GwtEvaluation;
 import edu.cmu.square.client.model.GwtInspectionTechnique;
 import edu.cmu.square.client.model.GwtProject;
 import edu.cmu.square.client.model.GwtRole;
 import edu.cmu.square.client.model.GwtStep;
 import edu.cmu.square.client.model.GwtTechnique;
 import edu.cmu.square.client.model.GwtTerm;
 import edu.cmu.square.client.model.GwtUser;
 import edu.cmu.square.client.model.ProjectRole;
 import edu.cmu.square.server.authorization.AllowedRoles;
 import edu.cmu.square.server.authorization.Roles;
 import edu.cmu.square.server.business.interfaces.ManageProjectBusiness;
 import edu.cmu.square.server.business.interfaces.StepBusiness;
 import edu.cmu.square.server.business.step.interfaces.AgreeOnDefinitionsBusiness;
 import edu.cmu.square.server.business.step.interfaces.ElicitationTechniqueBusiness;
 import edu.cmu.square.server.business.step.interfaces.InspectionTechniqueBusiness;
 import edu.cmu.square.server.dao.implementation.HbnSoftwarePackageDao;
 import edu.cmu.square.server.dao.interfaces.AsquareCaseDao;
 import edu.cmu.square.server.dao.interfaces.AssetDao;
 import edu.cmu.square.server.dao.interfaces.GoalDao;
 import edu.cmu.square.server.dao.interfaces.MappingDao;
 import edu.cmu.square.server.dao.interfaces.ProjectDao;
 import edu.cmu.square.server.dao.interfaces.ProjectPackageAttributeRatingDao;
 import edu.cmu.square.server.dao.interfaces.QualityAttributeDao;
 import edu.cmu.square.server.dao.interfaces.RequirementDao;
 import edu.cmu.square.server.dao.interfaces.RoleDao;
 import edu.cmu.square.server.dao.interfaces.StepDao;
 import edu.cmu.square.server.dao.interfaces.TermDao;
 import edu.cmu.square.server.dao.interfaces.UserAhpDao;
 import edu.cmu.square.server.dao.interfaces.UserDao;
 import edu.cmu.square.server.dao.model.AsquareCase;
 import edu.cmu.square.server.dao.model.Asset;
 import edu.cmu.square.server.dao.model.Goal;
 import edu.cmu.square.server.dao.model.InspectionTechnique;
 import edu.cmu.square.server.dao.model.Project;
 import edu.cmu.square.server.dao.model.ProjectPackageAttributeRating;
 import edu.cmu.square.server.dao.model.ProjectPackageAttributeRatingId;
 import edu.cmu.square.server.dao.model.QualityAttribute;
 import edu.cmu.square.server.dao.model.Requirement;
 import edu.cmu.square.server.dao.model.Role;
 import edu.cmu.square.server.dao.model.SoftwarePackage;
 import edu.cmu.square.server.dao.model.Step;
 import edu.cmu.square.server.dao.model.Technique;
 import edu.cmu.square.server.dao.model.Term;
 import edu.cmu.square.server.dao.model.User;
 import edu.cmu.square.server.dao.model.UserAhp;
 
 
 @Service
 @Scope("prototype")
 public class ManageProjectBusinessImpl extends BaseBusinessImpl implements ManageProjectBusiness
 {
 
 	private static final long serialVersionUID = -4389713052042815809L;
 	@Resource
 	private StepDao stepDao;
 
 	@Resource
 	private UserDao userDao;
 
 	@Resource
 	private RoleDao roleDao;
 
 	@Resource
 	private ProjectDao projectDao;
 
 	@Resource
 	private UserAhpDao userAhpDao;
 	
 	@Resource
 	private AsquareCaseDao asquareCaseDao;
 
 	@Resource
 	private StepBusiness stepBusiness;
 
 	@Resource
 	private AgreeOnDefinitionsBusiness termsBusiness;
 
 	/*
 	@Resource
 	private ElicitationTechniqueBusiness elicitationTechniqueBusiness;
 
 	@Resource
 	private InspectionTechniqueBusiness inspectionTechniqueBusiness;
 	*/
 	@Resource
 	private HbnSoftwarePackageDao softwarePackageDao;
 	
 	@Resource
 	private QualityAttributeDao qualityAttributeDao;
 	
 	@Resource
 	private ProjectPackageAttributeRatingDao projectPackageAttributeRatingDao;
 	
 	@Resource
 	private TermDao termDao;
 	
 	@Resource
 	private GoalDao goalDao;
 
 	@Resource
 	private AssetDao assetDao;
 	
 	@Resource
 	private RequirementDao requirementDao;
 	
 	@Resource
 	  private MappingDao mappingDao;
 
 	@AllowedRoles(roles = {Roles.Acquisition_Organization_Engineer})
 	public void editRole(GwtUser gwtUser, GwtProject gwtProject) throws SquareException
 	{
 		User user = new User(gwtUser, "");
 		Role role = roleDao.findByName(gwtUser.getRole());
 		Project project = new Project(gwtProject);
 		userDao.editRole(user, role, project);
 	}
 
 	@AllowedRoles(roles = {Roles.All})
 	public List<Role> getAllRoles() throws SquareException
 	{
 		List<Role> roles = null;
 
 		try
 		{
 			roles = roleDao.getAllRolesExceptLeadRequirementsEngineer();
 			if (roles.size() == 0)
 			{
 				throw new SquareException("No roles in the database");
 			}
 		}
 		catch (SquareException ex)
 		{
 			throw ex;
 		}
 		catch (Throwable t)
 		{
 			throw new SquareException(t.getMessage());
 		}
 
 		return roles;
 	}
 
 	@AllowedRoles(roles = {Roles.All})
 	public List<GwtStep> getSteps(GwtProject gwtProject) throws SquareException
 	{
 
 		// List<GwtStep> gwtSteps = new ArrayList<GwtStep>();
 		List<Step> steps = null;
 
 		List<GwtStep> gwtSteps = new ArrayList<GwtStep>();
 		try
 		{
 
 			Project project = new Project();
 			project.setId(gwtProject.getId());
 
 			steps = stepDao.getProjectSteps(project);
 
 			if (steps.size() == 0)
 			{
 				throw new SquareException("No steps in the database");
 			}
 
 			for (Step s : steps)
 			{
 				gwtSteps.add(s.createGwtStep(gwtProject.getId()));
 			}
 
 		}
 		catch (Throwable t)
 		{
 			throw new SquareException(t);
 
 		}
 
 		return gwtSteps;
 	}
 
 	@AllowedRoles(roles = {Roles.All})
 	public List<GwtUser> getUserList(GwtProject gwtProject) throws SquareException
 	{
 		List<GwtUser> gwtUserList = null;
 		try
 		{
 			Project project = new Project(gwtProject);
 			List<User> userList = userDao.getUserListForProject(project);
 
 			gwtUserList = new ArrayList<GwtUser>(userList.size());
 			for (User u : userList)
 			{
 				GwtUser gwtUser = u.createGwtUser();
 				Role role = userDao.getRoleForUserInProject(u.getId(), project.getId());
 				gwtUser.setRole(role.getName());
 				gwtUserList.add(gwtUser);
 			}
 		}
 		catch (Throwable e)
 		{
 			//System.out.println("manageproject buz, get userlist()");
 			throw new SquareException("error " + Arrays.toString(e.getStackTrace()));
 		}
 
 		return gwtUserList;
 	}
 	@AllowedRoles(roles = {Roles.Acquisition_Organization_Engineer})
 	public void removeUserFromProject(GwtProject gwtProject, GwtUser gwtUser) throws SquareException
 	{
 		Project project = new Project(gwtProject);
 		User user = new User(gwtUser, "");
 		if (user.getUserName().equals(super.getUserName()))
 		{
 			SquareException se = new SquareException();
 			se.setType(ExceptionType.selfDelete);
 			throw se;
 		}
 
 		
 		List<UserAhp> comparisons = userAhpDao.getAllComparisons(user.getId(), project.getId());
 
 		for (UserAhp userAhp : comparisons)
 		{
 			userAhpDao.deleteEntity(userAhp);
 		}
 		userDao.removeUserFromProject(user, project);
 	}
 
 	@AllowedRoles(roles = {Roles.Acquisition_Organization_Engineer})
 	public void updateProjectName(GwtProject gwtProject) throws SquareException
 	{
 		try
 		{
 			Project otherProject = projectDao.findByName(gwtProject.getName());
 			if (otherProject != null)
 			{
 				SquareException se = new SquareException("Duplicate project name");
 				se.setType(ExceptionType.duplicateName);
 				throw se;
 			}
 
 			Project project = projectDao.fetch(gwtProject.getId());
 			project.setName(gwtProject.getName());
 		}
 		catch (SquareException ex)
 		{
 			throw ex;
 		}
 		catch (Throwable t)
 		{
 			throw new SquareException("Error updating name", t);
 		}
 	}
 	@AllowedRoles(roles = {Roles.Administrator, Roles.Acquisition_Organization_Engineer})
 	public GwtProject updateProject(int projectId, int acquisitionOEId, String projectName) throws SquareException
 	{
 		try
 		{
 
 			Project project = projectDao.fetch(projectId);
 			project.setName(projectName);
 
 			//User newLeadRequirementEngineer = userDao.fetch(leadRequiremntId);
 			//User oldLeadRequirementEngineer = project.getAcquisitionOrganizationEngineer();
 			
 			User newAcquisitionOrganizationEngineer = userDao.fetch(acquisitionOEId);
 			User oldAcquisitionOrganizationEngineer = project.getAcquisitionOrganizationEngineer();
 			
 			
 			//Role leadRequirementEngineer = roleDao.findByName(ProjectRole.Lead_Requirements_Engineer.getLabel());
 			//Role requirementEngineer = roleDao.findByName(ProjectRole.Requirements_Engineer.getLabel());
 			
 			Role acquisitionOrganizationEngineer = roleDao.findByName(ProjectRole.Acquisition_Organization_Engineer.getLabel());
 						
 			/*
 			if (oldLeadRequirementEngineer != null)
 			{
 				userDao.editRole(oldLeadRequirementEngineer, requirementEngineer, project);
 			}
 			Role userRole = userDao.getRoleForUserInProject(newLeadRequirementEngineer.getId(), project.getId());
 			if (userRole==null)
 			{
 				userDao.addUserToProject(newLeadRequirementEngineer, project, leadRequirementEngineer);
 			}
 			else 
 			{
 				userDao.editRole(newLeadRequirementEngineer, leadRequirementEngineer, project);
 			}
 			project.setLeadRequirementEngineer(newLeadRequirementEngineer);
 			*/
 			
 			if (oldAcquisitionOrganizationEngineer != null)
 			{
 				userDao.editRole(oldAcquisitionOrganizationEngineer, acquisitionOrganizationEngineer, project);
 			}
 			
 			Role userRole = userDao.getRoleForUserInProject(newAcquisitionOrganizationEngineer.getId(), project.getId());
 			
 			if (userRole==null)
 			{
 				userDao.addUserToProject(newAcquisitionOrganizationEngineer, project, acquisitionOrganizationEngineer);
 			}
 			else 
 			{
 				userDao.editRole(newAcquisitionOrganizationEngineer, acquisitionOrganizationEngineer, project);
 			}
 			project.setAcquisitionOrganizationEngineer(newAcquisitionOrganizationEngineer);
 			projectDao.update(project);
 			return project.createGwtProject();
 		}
 		catch (Throwable t)
 		{
 			throw new SquareException("Error updating name", t);
 		}
 	}
 
 	@AllowedRoles(roles = {Roles.Acquisition_Organization_Engineer})
 	public void addUserToProject(GwtProject gwtProject, GwtUser gwtUser, GwtRole gwtRole) throws SquareException
 	{
 		try
 		{
 			Project project = new Project(gwtProject);
 			if (project.getId() == null)
 			{
 				if (project.getName() == null)
 				{
 					throw new SquareException("Either project name or project id must be not null");
 				}
 
 				project = projectDao.findByName(project.getName());
 			}
 
 			User user = new User(gwtUser, "");
 			if (user.getId() == null)
 			{
 				if (user.getUserName() == null)
 				{
 					throw new SquareException("Either userName or user id must be not null");
 				}
 
 				user = userDao.getUsersbyUsername(user.getUserName()).get(0);
 			}
 
 			Role role = new Role(gwtRole);
 			if (role.getId() == null)
 			{
 				if (role.getName() == null)
 				{
 					throw new SquareException("Either role name or role id must be not null");
 				}
 
 				role = roleDao.findByName(role.getName());
 			}
 
 			userDao.addUserToProject(user, project, role);
 		}
 		catch (SquareException ex)
 		{
 			throw ex;
 		}
 		catch (Throwable t)
 		{
 			throw new SquareException(t.getMessage());
 		}
 	}
 
 	@AllowedRoles(roles = {Roles.All})
 	public GwtProject getProject(Integer projectId) throws SquareException
 	{
 		try
 		{
 			Project project = projectDao.fetch(projectId);
 			return project.createGwtProject();
 		}
 		catch (Throwable t)
 		{
 			throw new SquareException(t.getMessage());
 		}
 	}
 /*
 	@AllowedRoles(roles = {Roles.Lead_Requirements_Engineer, Roles.Requirements_Engineer, Roles.Acquisition_Organization_Engineer})
 	public void setTechniqueToProject(Integer projectId, Integer techniqueID, String rationale) throws SquareException
 	{
 		Project project = projectDao.fetch(projectId);
 		Technique technique = new Technique();
 		technique.setId(techniqueID);
 		//project.setSecurityTechnique(technique);
 		//project.setSecurityTechniqueRationale(rationale);
 		projectDao.update(project);
 	}
 	@AllowedRoles(roles = {Roles.Acquisition_Organization_Engineer})
 	public void setTechniqueToProject(Integer projectId, Integer techniqueID, String rationale) throws SquareException
 	{
 		Project project = projectDao.fetch(projectId);
 		Technique technique = new Technique();
 		technique.setId(techniqueID);
 		project.setSecurityTechnique(technique);
 		project.setSecurityTechniqueRationale(rationale);
 		projectDao.update(project);
 	}
 */
 	@AllowedRoles(roles = {Roles.Administrator,Roles.Acquisition_Organization_Engineer})
 	public GwtProject createProject(GwtProject newProject) throws SquareException
 	{		
 		User acquisitionOrganizationEngineer = userDao.fetch(newProject.getAcquisitionOrganizationEngineer().getUserId());
 		
 		Project project = new Project(newProject);
 		
 		project.setAcquisitionOrganizationEngineer(acquisitionOrganizationEngineer);
 		
 		AsquareCase acase = asquareCaseDao.fetch(newProject.getCases().getId());
 		project.setCases(acase);
 		
 		//System.out.println("lre: "+project.getAcquisitionOrgEngineer().getId()+" cases: "+project.getCases().getId()+" date1: "+project.getDateCreated() +" date2 "+project.getDateModified()+" lite "+project.isLite()+" name "+project.getName()+" priv "+project.isPrivacy()+" sec "+project.isSecurity());
 		
 		projectDao.create(project);
 		System.out.println("done1");
 		newProject.setId(project.getId());
 		System.out.println("done2");
 		if(newProject.getCases().getId() == 3)
 		{
 		QualityAttribute qa = new QualityAttribute();
 		qa.setName("Unnamed");
 		qa.setDescription("No description");
 		System.out.println("done3");
 		qualityAttributeDao.create(qa);
 		System.out.println("done4");
 		ProjectPackageAttributeRating ppar = new ProjectPackageAttributeRating();
 		ProjectPackageAttributeRatingId pparid = new ProjectPackageAttributeRatingId();
 		pparid.setAttributeId(qa.getId());
 		pparid.setPackageId(1);
 		pparid.setProjectId(project.getId());
 		
 		System.out.println("projectid: "+project.getId()+" packageid: "+1+" attributeid: "+qa.getId());
 		
 		ppar.setId(pparid);
 		ppar.setProject(project);
 		ppar.setSoftwarePackage(softwarePackageDao.fetch(1));
 		ppar.setQualityAttribute(qa);
 		ppar.setRating(0);
 		
 		projectPackageAttributeRatingDao.create(ppar);
 		}
 		
 //		if(newProject.getCases().getName().equals("Case 3"))
 //		{
 //			SoftwarePackage weightsPackage = new SoftwarePackage();
 ////			This is the software package with the weights
 //			weightsPackage.setId(1);
 //			softwarePackageDao.addSoftwarePackageToProject(project, weightsPackage);
 //			System.out.println("Adding weights");
 //		}
 		
 		
 		
 		
 		// Create the steps
 		stepBusiness.createStepsForProject(newProject);
 
 		// Add the lead requirement engineer to project and assigned the role.
 		GwtRole role = new GwtRole();
 		role.setName(ProjectRole.Acquisition_Organization_Engineer.getLabel());
 
 		this.addUserToProject(newProject, acquisitionOrganizationEngineer.createGwtUser(), role);
 		
 		
 		return newProject;
 
 	}
 	@AllowedRoles(roles = {Roles.Administrator,Roles.Acquisition_Organization_Engineer})
 	public void deleteProject(int projectId) throws SquareException
 	{
 
 		projectDao.deleteById(projectId);
 
 	}
 	@AllowedRoles(roles = {Roles.Administrator,Roles.Acquisition_Organization_Engineer})
 	public List<GwtProject> getAllProjects() throws SquareException
 	{
 		List<GwtProject> allProjects = new ArrayList<GwtProject>();
 		List<Project> projects = projectDao.fetchAll();
 
 		if (projects == null)
 		{
 			return allProjects;
 		}
 
 		for (Project p : projects)
 		{
 			allProjects.add(p.createGwtProject());
 		}
 
 		return allProjects;
 	}
 
 	public List<GwtTerm> findDefaultTerms(Document doc)
 	{
 		if (doc == null)
 		{
 			return null;
 		}
 
 		try
 		{
 
 			NodeList nodeLst = getNodeList(doc, "Term");
 
 			List<GwtTerm> terms = new ArrayList<GwtTerm>();
 
 			for (int s = 0; s < nodeLst.getLength(); s++)
 			{
 				Node fstNode = nodeLst.item(s);
 				if (fstNode.getNodeType() == Node.ELEMENT_NODE)
 				{
 					Element fstElmnt = (Element) fstNode;
 
 					GwtTerm term = new GwtTerm();
 					term.setTerm(fstElmnt.getAttribute("name").trim());
 					term.setDefinition(fstElmnt.getTextContent().trim());
 
 					terms.add(term);
 				}
 			}
 
 			return terms;
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	public List<GwtTechnique> findDefaultElicitationTechniques(Document doc)
 	{
 		if (doc == null)
 		{
 			return null;
 		}
 
 		try
 		{
 			NodeList nodeLst = getNodeList(doc, "ElicitationTechnique");
 
 			List<GwtTechnique> techniques = new ArrayList<GwtTechnique>();
 
 			for (int s = 0; s < nodeLst.getLength(); s++)
 			{
 				Node fstNode = nodeLst.item(s);
 				if (fstNode.getNodeType() == Node.ELEMENT_NODE)
 				{
 					Element fstElmnt = (Element) fstNode;
 
 					GwtTechnique technique = new GwtTechnique();
 					technique.setTitle(fstElmnt.getAttribute("name").trim());
 					technique.setDescription(fstElmnt.getTextContent().trim());
 
 					String isSecurity = fstElmnt.getAttribute("isSecurity").trim();
 					if (isSecurity.trim().equalsIgnoreCase("true"))
 					{
 						technique.setToSecurity();
 					}
 					else
 					{
 						technique.setToPrivacy();
 					}
 
 					techniques.add(technique);
 				}
 			}
 
 			return techniques;
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	public List<GwtEvaluation> findDefaultEvaluationCriteria(Document doc)
 	{
 		if (doc == null)
 		{
 			return null;
 		}
 
 		try
 		{
 			NodeList nodeLst = getNodeList(doc, "EvaluationCriteria");
 
 			List<GwtEvaluation> evaluations = new ArrayList<GwtEvaluation>();
 
 			for (int s = 0; s < nodeLst.getLength(); s++)
 			{
 				Node fstNode = nodeLst.item(s);
 				if (fstNode.getNodeType() == Node.ELEMENT_NODE)
 				{
 					Element fstElmnt = (Element) fstNode;
 
 					GwtEvaluation evaluationCriteria = new GwtEvaluation();
 					evaluationCriteria.setTitle(fstElmnt.getAttribute("name").trim());
 					evaluationCriteria.setDescription(fstElmnt.getTextContent().trim());
 
 					evaluations.add(evaluationCriteria);
 				}
 			}
 
 			return evaluations;
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	public List<GwtInspectionTechnique> findDefaultInspectionTechniques(Document doc)
 	{
 		if (doc == null)
 		{
 			return null;
 		}
 
 		try
 		{
 			NodeList nodeLst = getNodeList(doc, "InspectionTechnique");
 
 			List<GwtInspectionTechnique> inspections = new ArrayList<GwtInspectionTechnique>();
 
 			for (int s = 0; s < nodeLst.getLength(); s++)
 			{
 				Node fstNode = nodeLst.item(s);
 				if (fstNode.getNodeType() == Node.ELEMENT_NODE)
 				{
 					Element fstElmnt = (Element) fstNode;
 
 					GwtInspectionTechnique technique = new GwtInspectionTechnique();
 					technique.setTitle(fstElmnt.getAttribute("name").trim());
 					technique.setDescription(fstElmnt.getTextContent().trim());
 
 					inspections.add(technique);
 				}
 			}
 
 			return inspections;
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	private NodeList getNodeList(Document doc, String element)
 	{
 
 		try
 		{
 			doc.getDocumentElement().normalize();
 
 			NodeList nodeLst = doc.getElementsByTagName(element);
 			return nodeLst;
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	/*********************
 	 * Copy project
 	 */
 	
 	@Override
 	  @AllowedRoles(roles = { Roles.Administrator })
 	  public GwtProject copyProject(GwtProject originalProject) throws SquareException {
 	    Project project = new Project(originalProject);
 	    // Sets the project type
 	    // This is set in CreateProjectDialog, and we allow the project type to be different
 	    // from the original project's type
 	    //project.setProjectType(new ProjectType(originalProject.getProjectType()));
 	    // Clears the approval
 	    //setApprovalStatus(originalProject.getId(), 1, 1);
 	    // resets the id
 	    project.setId(0);
 	    // Gets the new LRE
 	    User aoe =
 	        userDao.fetch(originalProject.getAcquisitionOrganizationEngineer().getUserId());
 	    // Sets the new LRE
 	    project.setAcquisitionOrganizationEngineer(aoe);
 
 	    Date now = new Date();
 	    project.setDateCreated(now);
 	    project.setDateModified(now);
 	    // We create an entry for the project in the db
 	    projectDao.create(project);
 
 	    // We add the user to the user-role table
 	    userDao.addUserToProject(aoe,
 	    		project,
 	    		roleDao.findByName(ProjectRole.Acquisition_Organization_Engineer.getLabel()));
 
 	    Project original = new Project(originalProject);
 
 	    // Here we begin the copying of all project related items.
 	    // We are going in order of the 9 steps of the project
 	    // The function names are usually self-explanatory, and below I have added a discussion on
 	    // specific implementation of each function
 	    copyTerms(project, original);
 
 	    HashMap<Integer, Integer> goalMap = copyGoals(project, original);
 
 	    HashMap<Integer, Integer> assetMap = copyAssets(project, original);
 
 	    copyGoalAsset(original, goalMap, assetMap);
 
 	    //HashMap<Integer, Integer> artifactMap = copyArtifacts(project, original);
 
 	    //HashMap<Integer, Integer> riskMap = copyRisks(project, original);
 
 	    //copyRiskArtifact(original, riskMap, artifactMap);
 
 	    //copyRiskAsset(original, riskMap, assetMap);
 
 	    //HashMap<Integer, Integer> techniqueMap = copyTechniques(project, original);
 
 	    //HashMap<Integer, Integer> evaluationCriteriaMap = copyEvaluationCriteria(project, original);
 
 	    //copyTechniqueEvaluationCriteria(project, original, techniqueMap, evaluationCriteriaMap);
 
 	    //copyPretAnswers(project, original);
 
 	    //HashMap<Integer, Integer> pretRequirementMap = copyPretRequirements(project, original);
 
 	    HashMap<Integer, Integer> requirementMap = copyRequirements(project, original);
 
 	    //updatePretRequirement(project, pretRequirementMap);
 
 	    //HashMap<Integer, Integer> categoryMap = copyCategories(project, original);
 
 	    //copyRequirementArtifact(original, requirementMap, artifactMap);
 
 	    //copyRequirementCategory(original, requirementMap, categoryMap);
 
 	    //copyRequirementRisk(original, requirementMap, riskMap);
 
 	    copyRequirementGoal(original, requirementMap, goalMap);
 
 	    copySteps(project);
 
 	    //HashMap<Integer, Integer> inspectionTechniqueMap = copyInspectionTechniques(project, original);
 
 	    //updateInspectionTechnique(project, original, inspectionTechniqueMap);
 	    
 	    //We return back the gwt project from the newly copied project
 	    return project.createGwtProject();
 	  }
 	
 	
 	
 	 // ------------------Copy Project---------------------------//
 
 	  /**
 	   * There is a lot of repetition in the code, though that is because we are copying the tables
 	   * Making generic functions would have so many parameters, and we'd have to create another layer
 	   * of abstraction that it would more trouble than its worth. So instead of commenting every line
 	   * of code I will present the basic pattern here which applies to all.
 	   * 
 	   * For all void functions, that take two projects as inputs All we are doing is copying all of
 	   * objects belonging to the first project, associating them with the second project, and creating
 	   * a new instance of the same
 	   * 
 	   * For all functions that return a map, do the same thing as above, except they return a map
 	   * associating the previous item's id with the new item's id. We use this because later on, we
 	   * have relationships of "many-to-many"/"many-to-one" and 'one-to-many" and we need the map for
 	   * association.
 	   * 
 	   * Finally void functions that take original project and two hashmaps are in-charge of associating
 	   * and creating the relationships described above. The pattern used here is for all of the items
 	   * belonging to the old project, get their respective relationship entities. From those entities,
 	   * using the map, find the corresponding matching new entity, and put those all in a set. Finally,
 	   * when you have that re-associate that set back to the mapped entity. This is a rather complex
 	   * portion of the code and probably re-reading it over and looking at the code can definitely help
 	   * in understanding it.
 	   * 
 	   * Lastly, there are some update functions that are in charge of setting a specific value for a
 	   * table based on a new item that is subsequently created, these are the easiest functions in code
 	   * :)
 	   */
 
 	  public void copyTerms(Project project, Project originalProject) {
 	    List<Term> terms = termDao.getTermByProject(originalProject);
 	    for (Term t : terms) {
 	      Date now = new Date();
 	      Term newTerm = new Term(project, t.getTerm(), t.getDefinition(), now, now);
 	      termDao.create(newTerm);
 	    }
 
 	  }
 
 	  public HashMap<Integer, Integer> copyGoals(Project project, Project originalProject) {
 	    HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
 	    List<Goal> goals = goalDao.getBusinessGoalByProject(originalProject);
 	    goals.addAll(goalDao.getSubGoalByProject(originalProject));
 
 	    for (Goal g : goals) {
 	      Date now = new Date();
 	      Goal newGoal =
 	          new Goal(g.getGoalType(), project, g.getDescription(), g.getPriority(), now, now);
 	      goalDao.create(newGoal);
 	      map.put(g.getId(), newGoal.getId());
 	    }
 
 	    return map;
 	  }
 
 	  public HashMap<Integer, Integer> copyAssets(Project project, Project originalProject) {
 	    HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
 	    List<Asset> assets = assetDao.getAssetByProject(originalProject);
 
 	    for (Asset a : assets) {
 	      Date now = new Date();
 	      Asset asset = new Asset(a.getDescription(), project, now, now);
 	      assetDao.create(asset);
 	      map.put(a.getId(), asset.getId());
 	    }
 
 	    return map;
 	  }
 
 	  public void copyGoalAsset(Project originalProject,
 	                            HashMap<Integer, Integer> goalMap,
 	                            HashMap<Integer, Integer> assetMap) {
 
 	    List<Asset> oldAssets = assetDao.getAssetByProject(originalProject);
 	    for (Asset a : oldAssets) {
 	      Set<Goal> temp = a.getGoals();
 	      for (Goal g : temp) {
 	        mappingDao.addAssetGoalMapping(goalMap.get(g.getId()), assetMap.get(a.getId()));
 	      }
 	    }
 	  }
 
 	  /*
 	  public HashMap<Integer, Integer> copyArtifacts(Project project, Project originalProject) {
 	    HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
 	    List<Artifact> artifacts = artifactDao.getArtifactsByProject(originalProject);
 
 	    for (Artifact a : artifacts) {
 
 	      Artifact artifact =
 	          new Artifact(null, project, a.getName(), a.getDescription(), a.getRevision(), a.getLink());
 	      Date now = new Date();
 	      artifact.setDateCreated(now);
 	      artifact.setDateModified(now);
 	      artifactDao.create(artifact);
 
 	      map.put(a.getId(), artifact.getId());
 	    }
 
 	    return map;
 	  }
 */
 	  /*
 	  public HashMap<Integer, Integer> copyRisks(Project project, Project originalProject) {
 	    HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
 	    List<Risk> risks = riskDao.getRisksByProject(originalProject);
 
 	    for (Risk r : risks) {
 	      Date now = new Date();
 	      Risk risk =
 	          new Risk(project,
 	                   r.getRiskTitle(),
 	                   r.getThreatSource(),
 	                   r.getThreatAction(),
 	                   r.getCurrentMeasures(),
 	                   r.getPlannedMeasures(),
 	                   r.getImpact(),
 	                   r.getLikelihood(),
 	                   r.getVulnerability(),
 	                   now,
 	                   now);
 	      riskDao.create(risk);
 	      map.put(r.getId(), risk.getId());
 	    }
 
 	    return map;
 	  }
 
 	  
 	  
 	  public void copyRiskArtifact(Project originalProject,
 	                               HashMap<Integer, Integer> riskMap,
 	                               HashMap<Integer, Integer> artifactMap) {
 
 	    Project tempProject = projectDao.fetch(originalProject.getId());
 	    List<Artifact> oldArtifacts = artifactDao.getArtifactsByProject(tempProject);
 	    for (Artifact a : oldArtifacts) {
 	      Set<Risk> temp = a.getRisks();
 	      for (Risk r : temp) {
 	    	  mappingDao.addRiskArtifactMapping(riskMap.get(r.getId()), artifactMap.get(a.getId()));
 	      }
 	    }
 	  }
 
 	  public void copyRiskAsset(Project originalProject,
 	                            HashMap<Integer, Integer> riskMap,
 	                            HashMap<Integer, Integer> assetMap) {
 
 	    List<Asset> oldAssets = assetDao.getAssetByProject(originalProject);
 	    for (Asset a : oldAssets) {
 	      Set<Risk> temp = a.getRisks();
 	      for (Risk r : temp) {
 	    	  mappingDao.addRiskAssetMapping(riskMap.get(r.getId()), assetMap.get(a.getId()));
 	      }
 	    }
 	  }
 
 	  public HashMap<Integer, Integer> copyTechniques(Project project, Project originalProject) {
 	    HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
 	    List<Technique> techniques = techniqueDao.getTechniquesByProject(originalProject);
 
 	    for (Technique t : techniques) {
 	      Date now = new Date();
 	      Technique technique =
 	          new Technique(t.getName(),
 	                        t.getDescription(),
 	                        t.getRationale(),
 	                        t.getProjectType(),
 	                        t.getSelected(),
 	                        now,
 	                        now);
 	      technique.setProject(project);
 	      techniqueDao.create(technique);
 	      map.put(t.getId(), technique.getId());
 	    }
 	    return map;
 	  }
 
 	  public HashMap<Integer, Integer> copyEvaluationCriteria(Project project, Project originalProject) {
 	    HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
 	    List<EvaluationCriteria> ecs = evaCriteriaDao.getAllByProject(originalProject);
 
 	    for (EvaluationCriteria e : ecs) {
 	      Date now = new Date();
 	      EvaluationCriteria ec =
 	          new EvaluationCriteria(e.getName(), e.getDescription(), now, now, e.getWeight());
 	      ec.setProject(project);
 	      evaCriteriaDao.create(ec);
 	      map.put(e.getId(), ec.getId());
 	    }
 	    return map;
 	  }
 /*
 	  public void copyTechniqueEvaluationCriteria(Project project,
 	                                              Project originalProject,
 	                                              HashMap<Integer, Integer> techniqueMap,
 	                                              HashMap<Integer, Integer> evaluationCriteriaMap) {
 	    List<TechniqueEvaluationCriteria> tecList = techEvaCriteriaDao.getAllValues(originalProject);
 
 	    for (TechniqueEvaluationCriteria tec : tecList) {
 	      TechniqueEvaluationCriteria newTec = new TechniqueEvaluationCriteria();
 	      newTec.setProject(project);
 	      newTec.setValue(tec.getValue());
 	      Technique newTechnique = techniqueDao.fetch(techniqueMap.get(tec.getTechnique().getId()));
 	      newTec.setTechnique(newTechnique);
 	      EvaluationCriteria newEvaCriteria =
 	          evaCriteriaDao.fetch(evaluationCriteriaMap.get(tec.getTechniqueEvaluation().getId()));
 	      newTec.setTechniqueEvaluation(newEvaCriteria);
 	      newTec.setId(new TechniqueEvaluationCriteriaId(newTechnique.getId(),
 	                                                      newEvaCriteria.getId(),
 	                                                      project.getId()));
 	      techEvaCriteriaDao.create(newTec);
 	    }
 
 	  }
 
 	  
 	  public void copyPretAnswers(Project project, Project originalProject) {
 	    PretAnswer pretAnswer = pretAnswerDao.getPretAnswerByProject(originalProject);
 	    if (pretAnswer != null) {
 	      PretAnswer newPretAnswer = new PretAnswer(pretAnswer.createGwtPretAnswer());
 	      newPretAnswer.setProject(project);
 	      pretAnswerDao.create(newPretAnswer);
 	    }
 	  }
 
 	  public HashMap<Integer, Integer> copyPretRequirements(Project project, Project originalProject) {
 	    HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
 	    List<PretRequirement> pretRequirements =
 	        pretRequirementDao.getRequirementsByProject(originalProject);
 
 	    for (PretRequirement p : pretRequirements) {
 	      PretRequirement pretRequirement = new PretRequirement(p.createGwtPretRequirement());
 	      pretRequirement.setProjectId(project);
 	      pretRequirement.setRequirementId(0);
 	      pretRequirementDao.create(pretRequirement);
 	      map.put(p.getId(), pretRequirement.getId());
 	    }
 
 	    return map;
 	  }
 */
 	  
 	  public HashMap<Integer, Integer> copyRequirements(Project project, Project originalProject) {
 	    HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
 	    Project oldProject = projectDao.fetch(originalProject.getId());
 	    Set<Requirement> requirements = oldProject.getRequirements();
 
 	    for (Requirement r : requirements) {
 	      Requirement requirement = new Requirement();
 	      requirement.setDescription(r.getDescription());
 	      requirement.setPriority(r.getPriority());
 	      //requirement.setProjectType(project.getProjectType());
 	      //requirement.setRequirementSource(r.getRequirementSource());
 	      requirement.setTitle(r.getTitle());
 	      Date now = new Date();
 	      requirement.setDateCreated(now);
 	      requirement.setDateModified(now);
 	      requirement.setProject(project);
 	      requirementDao.create(requirement);
 	      map.put(r.getId(), requirement.getId());
 	    }
 
 	    return map;
 	  }
 /*
 	  public void updatePretRequirement(Project project, HashMap<Integer, Integer> pretRequirementMap) {
 	    List<PretRequirement> pretRequirements = pretRequirementDao.getRequirementsByProject(project);
 	    pretRequirementMap.put(0, 0);
 	    for (PretRequirement p : pretRequirements) {
 	      p.setRequirementId(pretRequirementMap.get(p.getRequirementId()));
 	    }
 	  }
 
 	  public HashMap<Integer, Integer> copyCategories(Project project, Project originalProject) {
 	    HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
 	    List<Category> categories = categoryDao.getCategoriesByProject(originalProject);
 
 	    for (Category c : categories) {
 	      Category category = new Category();
 	      category.setLabel(c.getLabel());
 	      Date now = new Date();
 	      category.setDateCreated(now);
 	      category.setDateModified(now);
 	      category.setProject(project);
 	      categoryDao.create(category);
 
 	      map.put(c.getId(), category.getId());
 	    }
 
 	    return map;
 	  }
 
 	  public void copyRequirementArtifact(Project originalProject,
 	                                      HashMap<Integer, Integer> requirementMap,
 	                                      HashMap<Integer, Integer> artifactMap) {
 	    Set<Requirement> oldReqs = projectDao.fetch(originalProject.getId()).getRequirements();
 
 	    for (Requirement r : oldReqs) {
 	      Set<Artifact> temp = r.getArtifacts();
 	      for (Artifact a : temp) {
 	    	  mappingDao.addRequirementArtifactMapping(requirementMap.get(r.getId()), artifactMap.get(a.getId()));
 	      }
 	    }
 	  }
 */
 /*
 	  public void copyRequirementCategory(Project originalProject,
 	                                      HashMap<Integer, Integer> requirementMap,
 	                                      HashMap<Integer, Integer> categoryMap) {
 	    Set<Requirement> oldReqs = projectDao.fetch(originalProject.getId()).getRequirements();
 
 	    for (Requirement r : oldReqs) {
 	      Set<Category> temp = r.getCategories();
 	      for (Category a : temp) {
 	    	  mappingDao.addRequirementCategoryMapping(requirementMap.get(r.getId()), categoryMap.get(a.getId()));
 	      }      
 	    }
 	  }
 */
 
 	  public void copyRequirementGoal(Project originalProject,
 	                                  HashMap<Integer, Integer> requirementMap,
 	                                  HashMap<Integer, Integer> goalMap) {
 	    Set<Requirement> oldReqs = projectDao.fetch(originalProject.getId()).getRequirements();
 
 	    for (Requirement r : oldReqs) {
 	      Set<Goal> temp = r.getGoals();
 	      for (Goal g : temp) {
 	    	mappingDao.addRequirementGoalMapping(requirementMap.get(r.getId()), goalMap.get(g.getId()));  
 	      }
 	    }
 	  }
 
 	  public void copySteps(Project project) {
 	    stepBusiness.createStepsForProject(project.createGwtProject());
 	  }
 
 	 
 	
 	
 	
 	
 	
 	
 }
