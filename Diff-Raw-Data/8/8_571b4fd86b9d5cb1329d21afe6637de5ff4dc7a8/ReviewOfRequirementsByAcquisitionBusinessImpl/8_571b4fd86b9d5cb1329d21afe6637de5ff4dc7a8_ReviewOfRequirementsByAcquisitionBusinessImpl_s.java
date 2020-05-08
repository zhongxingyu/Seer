 package edu.cmu.square.server.business.step.implementation;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 
 import javax.annotation.Resource;
 
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Service;
 
 import edu.cmu.square.client.exceptions.ExceptionType;
 import edu.cmu.square.client.exceptions.SquareException;
 import edu.cmu.square.client.model.GwtProject;
 import edu.cmu.square.client.model.GwtRequirement;
 import edu.cmu.square.client.model.GwtStepVerficationResult;
 import edu.cmu.square.client.model.GwtTerm;
 import edu.cmu.square.client.navigation.StepEnum;
 import edu.cmu.square.server.authorization.AllowedRoles;
 import edu.cmu.square.server.authorization.Roles;
 import edu.cmu.square.server.business.implementation.BaseBusinessImpl;
 import edu.cmu.square.server.business.step.interfaces.AgreeOnDefinitionsBusiness;
 import edu.cmu.square.server.business.step.interfaces.ReviewOfRequirementsByAcquisitionBusiness;
 import edu.cmu.square.server.dao.interfaces.CategoryDao;
 import edu.cmu.square.server.dao.interfaces.ProjectDao;
 import edu.cmu.square.server.dao.interfaces.RequirementDao;
 import edu.cmu.square.server.dao.interfaces.TermDao;
 import edu.cmu.square.server.dao.model.Asset;
 import edu.cmu.square.server.dao.model.Category;
 import edu.cmu.square.server.dao.model.Project;
 import edu.cmu.square.server.dao.model.Requirement;
 import edu.cmu.square.server.dao.model.Term;
 
 
 @Service
 @Scope("prototype")
 public class ReviewOfRequirementsByAcquisitionBusinessImpl extends BaseBusinessImpl implements ReviewOfRequirementsByAcquisitionBusiness
 {
 
 	@Resource
 	private RequirementDao requirementDao;
 	
 	@Resource
 	private ProjectDao projectDao;
 
 	@Resource
 	private CategoryDao categoryDao;
 
 	@AllowedRoles(roles = {Roles.All})
 	public List<GwtRequirement> getRequirements(GwtProject gwtProject) throws SquareException
 	{
 		List<GwtRequirement> requirementList = new ArrayList<GwtRequirement>();
 		Project project = new Project(gwtProject.getId());
 		List<Requirement> requirements = requirementDao.getRequirementByProject(project);
 
 		for (Requirement req : requirements)
 		{
 			requirementList.add(req.createGwtRequirement());
 		}
 
 		return requirementList;
 
 	}
 
 	@Override
 	public GwtStepVerficationResult verifyStep(Project project) throws SquareException
 	{
 		GwtStepVerficationResult result = new GwtStepVerficationResult();
 
 		
 		List<GwtRequirement> requirements = getRequirements(project.createGwtProject());
 
 		if (requirements.size() == 0)
 		{
 			result.getMessages().add("There are no requirements defined yet!");
 			result.setHasWarning(true);
 		}
 
 		return result;
 	}
 		
 
 	@Override
 	public StepEnum getStepDescription() throws SquareException
 	{
 		return StepEnum.Review_Of_Requirements_By_Acquisition_Organization;
 	}
 
 	@Override
 	public void loadDefaultRequirements(int projectId, List<GwtRequirement> defaultRequirements) throws SquareException
 	{
 		GwtProject project = new GwtProject(projectId);
 		for (GwtRequirement req : defaultRequirements)
 		{
 
 			this.addRequirement(project, req);	
 
 		}
 	}
 	
 	@AllowedRoles(roles = {Roles.All})
 	public GwtRequirement addRequirement(GwtProject gwtProject, GwtRequirement gwtRequirement) throws SquareException
 	{
 		Project project = new Project(gwtProject.getId());
 		Requirement requirement = new Requirement(gwtRequirement);
 		requirement.setProject(project);
 
 		requirementDao.create(requirement);
 
 		return requirement.createGwtRequirement();
 	}
 	
 	
	@AllowedRoles(roles = {Roles.Contractor, Roles.Security_Specialist, Roles.Administrator})
 	public int addRequirementToProject(Integer projectId, GwtRequirement gwtRequirement) throws SquareException
 	{
 		
 		Project p = projectDao.fetch(projectId);
 		Requirement requirement = new Requirement(gwtRequirement);
 		requirement.setProject(p);
 
 		/*
 		if(gwtRequirement.getRisks().isEmpty()) 
 		{
 			SquareException se = new SquareException("At least one risk is required.");
 			se.setType(ExceptionType.missingLink);
 			throw se;
 		}
 		*/
 		if(gwtRequirement.getArtifacts().isEmpty()) 
 		{
 			SquareException se = new SquareException("At least one artifact is required.");
 			se.setType(ExceptionType.missingLink);
 			throw se;
 		}
 	
 		requirementDao.create(requirement);
 		p.getRequirements().add(requirement);
 		return requirement.getId();
 
 
 	}
 
 	
 		@AllowedRoles(roles = {Roles.Administrator, Roles.Contractor, Roles.Acquisition_Organization_Engineer, Roles.Security_Specialist})
 		public void removeRequirement(GwtRequirement gwtRequirement) throws SquareException
 		{
 			requirementDao.deleteById(gwtRequirement.getId());
 		}
 		
 		@AllowedRoles(roles = {Roles.Administrator, Roles.Contractor, Roles.Acquisition_Organization_Engineer, Roles.Security_Specialist})
 		public void updateRequirement(GwtProject gwtProject, GwtRequirement gwtRequirement) throws SquareException
 		{
 			Requirement requirement = new Requirement(gwtRequirement);
 			requirement.setProject(new Project(gwtProject.getId()));
 			requirementDao.update(requirement);
 		}
 
		@AllowedRoles(roles = {Roles.Contractor, Roles.Security_Specialist, Roles.Administrator})
 		public void deleteRequirement(Integer requirementId, Integer projectId) throws SquareException
 		{
 			requirementDao.deleteById(requirementId);
 			requirementDao.zeroOutPriorities(projectId);
 		}
 
 		@AllowedRoles(roles = {Roles.All})
 		public List<GwtRequirement> getRequirementsFromProject(int projectId) throws SquareException
 		{
 			Project project = projectDao.fetch(projectId);
 			Set<Requirement> requirements = project.getRequirements();
 			
 			List<GwtRequirement> gwtRequirements = new ArrayList<GwtRequirement>();
 			for (Requirement req: requirements) 
 			{
 				GwtRequirement gr = req.createGwtRequirement();
 				gwtRequirements.add(gr);
 			}
 			
 			Collections.sort(gwtRequirements);
 			return gwtRequirements;	
 			
 			
 		}
 
 		@AllowedRoles(roles = {Roles.All})
 		public void assignRequirementsToCategory(List<GwtRequirement> requirements,int categoryID) throws SquareException
 		{
 			for(GwtRequirement gwtRequirement : requirements)
 			{
 				Requirement  r = requirementDao.fetch(gwtRequirement.getId());
 				 boolean exists=false;
 				  for( Category cat:  r.getCategories())
 				  {
 					  if(cat.getId()==categoryID)
 					  {
 						  exists=true;
 					  }
 				  }
 				  if(!exists)
 				  {
 					  Category category= categoryDao.fetch(categoryID);
 					  r.getCategories().add(category);
 					  requirementDao.update(r);
 				  }
 				
 			}
 			
 			
 		}
 
 		@AllowedRoles(roles = {Roles.All})
 		public void removeRequirementsFromCategory(List<GwtRequirement> requirements,int categoryID) throws SquareException
 		{
 			for(GwtRequirement gwtRequirement : requirements)
 			{
 				Requirement  r = requirementDao.fetch(gwtRequirement.getId());
 				 boolean exists=false;
 				  for( Category cat:  r.getCategories())
 				  {
 					  if(cat.getId()==categoryID)
 					  {
 						  exists=true;
 					  }
 				  }
 				  if(exists)
 				  {
 					  Category category= categoryDao.fetch(categoryID);
 					  r.getCategories().remove(category);
 					  requirementDao.update(r);
 				  }
 				
 			}
 			
 			
 		}
 
		@AllowedRoles(roles = {Roles.Contractor, Roles.Security_Specialist, Roles.Administrator})
 		public void updateRequirement(GwtRequirement gwtRequirement) throws SquareException
 		{
 		
 			Requirement  r = requirementDao.fetch(gwtRequirement.getId());
 			
 			r.getRisks().clear();
 			r.getArtifacts().clear();
 			r.getGoals().clear();
 			//r.getCategories().clear();
 			
 			r.update(gwtRequirement);
 			
 			if(gwtRequirement.getRisks().isEmpty()) 
 			{
 				SquareException se = new SquareException("At least one risk is required.");
 				se.setType(ExceptionType.missingLink);
 				throw se;
 			}
 			if(gwtRequirement.getArtifacts().isEmpty()) 
 			{
 				SquareException se = new SquareException("At least one artifact is required.");
 				se.setType(ExceptionType.missingLink);
 				throw se;
 			}
 			
 			requirementDao.update(r);
 			
 
 
 		}
 
 		@AllowedRoles(roles = {Roles.Administrator, Roles.Contractor, Roles.Acquisition_Organization_Engineer, Roles.Security_Specialist})
 		public void changeStatusToApproveRequirement(Integer projectId, GwtRequirement gwtRequirement)
 		{
 			Project project = new Project();
 			project.setId(projectId);
 			Requirement requirement = requirementDao.fetch(gwtRequirement.getId());
 			
 			requirement.setProject(project);
 			requirement.setId(gwtRequirement.getId());
 			requirementDao.changeStatusToApproved(requirement);
 
 		}
 		
 		@AllowedRoles(roles = {Roles.Administrator, Roles.Contractor, Roles.Acquisition_Organization_Engineer, Roles.Security_Specialist})
 		public void changeStatusToRequestRevisionRequirement(Integer projectId, GwtRequirement gwtRequirement)
 		{
 			Project project = new Project();
 			project.setId(projectId);
 			Requirement requirement = requirementDao.fetch(gwtRequirement.getId());
 			
 			requirement.setProject(project);
 			requirement.setId(gwtRequirement.getId());
 			requirementDao.changeStatusToRequestRevision(requirement);
 
 		}
 	
 	
 }
