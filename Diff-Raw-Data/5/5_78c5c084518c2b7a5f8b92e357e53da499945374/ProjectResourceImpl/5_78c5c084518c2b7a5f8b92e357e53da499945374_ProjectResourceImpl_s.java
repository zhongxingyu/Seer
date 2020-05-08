 package org.tcrun.slickij.data;
 
 import com.google.code.morphia.query.Query;
 import org.codehaus.jackson.JsonFactory;
 import org.codehaus.jackson.JsonParser;
 import org.tcrun.slickij.api.data.DataExtension;
 import org.tcrun.slickij.api.data.dao.ConfigurationDAO;
 import org.tcrun.slickij.api.data.dao.ProjectDAO;
 import java.util.Map;
 import org.tcrun.slickij.api.ProjectResource;
 import com.google.inject.Inject;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.MultivaluedMap;
 import javax.ws.rs.core.Response.Status;
 import javax.ws.rs.core.UriInfo;
 import org.bson.types.ObjectId;
 import org.tcrun.slickij.api.data.Build;
 import org.tcrun.slickij.api.data.Component;
 import org.tcrun.slickij.api.data.Configuration;
 import org.tcrun.slickij.api.data.DataDrivenPropertyType;
 import org.tcrun.slickij.api.data.InvalidDataError;
 import org.tcrun.slickij.api.data.Project;
 import org.tcrun.slickij.api.data.Release;
 
 /**
  *
  * @author jcorbett
  */
 public class ProjectResourceImpl implements ProjectResource
 {
 	private ProjectDAO m_projectDAO;
 	private ConfigurationDAO m_configDAO;
 
 	@Inject
 	public ProjectResourceImpl(ProjectDAO p_projectDAO, ConfigurationDAO p_configDAO)
 	{
 		m_projectDAO = p_projectDAO;
 		m_configDAO = p_configDAO;
 	}
 
 	@Override
 	public List<Project> getAllMatchingProjects(UriInfo uriInfo)
 	{
 		MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
 		Query<Project> query = m_projectDAO.createQuery();
 
 		if(queryParams.containsKey("name"))
 		{
 			if(queryParams.get("name").size() > 1)
 				query = query.field("name").in(queryParams.get("name"));
 			else
 				query = query.field("name").equal(queryParams.getFirst("name"));
 		} else if(queryParams.containsKey("namecontains"))
 		{
 			query.field("name").contains(queryParams.getFirst("namecontains"));
 		}
 		return query.asList();
 	}
 
 	@Override
 	public Project getProjectById(String id)
 	{
 		Project retval = null;
 		try
 		{
 			retval = m_projectDAO.get(new ObjectId(id));
 		} catch(RuntimeException ex)
 		{
 			// in case they provide an invalid id
 		}
 		if(retval == null)
 			throw new NotFoundError(Project.class, id);
 		return retval;
 	}
 
 	@Override
 	public void deleteProjectById(String id)
 	{
 		m_projectDAO.deleteById(new ObjectId(id));
 	}
 
 	@Override
 	public Project createNewProject(Project project)
 	{
 		if(project.getName() == null || project.getName().equals(""))
 			throw new WebApplicationException(Status.BAD_REQUEST);
 
 		if(project.getReleases() == null)
 			project.setReleases(new ArrayList<Release>());
 
 		if(project.getConfiguration() == null)
 		{
 			Configuration configuration = new Configuration();
 			project.setConfiguration(configuration);
 		}
 		project.getConfiguration().setConfigurationType("PROJECT");
 		project.getConfiguration().setName(project.getName() + "'s Project Configuration");
 
 		m_configDAO.save(project.getConfiguration());
 		m_projectDAO.save(project);
 		return project;
 	}
 
 	@Override
 	public Project changeProjectName(String id, String name)
 	{
 		Project project = getProjectById(id);
         if(name.startsWith("\"") && name.endsWith("\""))
         {
             name = jsonDecodeString(name);
         }
 		project.setName(name);
 		m_projectDAO.save(project);
 		return project;
 	}
 
     private String jsonDecodeString(String jsonstring)
     {
         String retval = "";
         JsonFactory jsonFactory = new JsonFactory();
         try
         {
             JsonParser parser = jsonFactory.createJsonParser(jsonstring);
             parser.nextToken();
             retval = parser.getText();
         } catch(Exception e)
         {
             throw new WebApplicationException(e, Status.BAD_REQUEST);
         }
         return retval;
     }
 
 	@Override
 	public Project changeProjectDescription(String id, String description)
 	{
 		Project project = getProjectById(id);
         if(description.startsWith("\"") && description.endsWith("\""))
         {
             description = jsonDecodeString(description);
         }
 		project.setDescription(description);
 		m_projectDAO.save(project);
 		return project;
 	}
 
 	@Override
 	public Release addRelease(String projectId, Release release)
 	{
 		Project project = getProjectById(projectId);
 
 		try
 		{
 			project.addRelease(release);
 		} catch(InvalidDataError e)
 		{
 			throw new WebApplicationException(e, Status.BAD_REQUEST);
 		}
 
 		m_projectDAO.save(project);
 
 		return release;
 	}
 
 	@Override
 	public Map<String, String> getAttributes(String projectId)
 	{
 		Project project = getProjectById(projectId);
 		return project.getAttributes();
 	}
 
 	@Override
 	public Map<String, String> addAttributes(String projectId, Map<String, String> attributes)
 	{
 		Project project = getProjectById(projectId);
 		project.getAttributes().putAll(attributes);
 		m_projectDAO.save(project);
 		return project.getAttributes();
 	}
 
 	@Override
 	public Map<String, String> deleteAttribute(String projectid, String attributeName)
 	{
 		Project project = getProjectById(projectid);
 		project.getAttributes().remove(attributeName);
 		m_projectDAO.save(project);
 		return project.getAttributes();
 	}
 
 	@Override
 	public List<Release> getReleases(String projectId)
 	{
 		Project project = getProjectById(projectId);
 		return project.getReleases();
 	}
 
 	@Override
 	public Release getRelease(String projectId, String releaseId)
 	{
 		Project project = getProjectById(projectId);
 		Release retval = project.findRelease(releaseId);
 		if(retval == null)
 			throw new NotFoundError(Release.class, releaseId);
 		else
 			return retval;
 	}
 
 	@Override
 	public Release getDefaultRelease(String projectId)
 	{
 		Project project = getProjectById(projectId);
 		if(project.getDefaultRelease() == null)
 			throw new NotFoundError(Release.class);
 		Release retval = project.findRelease(project.getDefaultRelease());
 		if(retval == null)
 			throw new NotFoundError(Release.class);
 		return retval;
 	}
 
 	@Override
 	public List<Release> deleteRelease(String projectId, String releaseId)
 	{
 		Project project = getProjectById(projectId);
 		if(project.deleteRelease(releaseId) == null)
 			throw new NotFoundError(Release.class);
 		m_projectDAO.save(project);
 		return project.getReleases();
 	}
 
 	@Override
 	public Build addBuild(String projectId, String releaseId, Build build)
 	{
 		Project p = getProjectById(projectId);
 
 		try
 		{
 			p.addBuild(releaseId, build);
 		} catch(InvalidDataError e)
 		{
 			throw new WebApplicationException(e, Status.BAD_REQUEST);
 		}
 		m_projectDAO.save(p);
 		return build;
 	}
 
 	@Override
 	public List<Build> getBuilds(String projectId, String releaseId)
 	{
 		Release release = getRelease(projectId, releaseId);
 		return release.getBuilds();
 	}
 
 	@Override
 	public Build getBuild(String projectId, String releaseId, String buildId)
 	{
 		Project project = getProjectById(projectId);
 		Build build = project.findBuild(releaseId, buildId);
 		if(build == null)
 			throw new NotFoundError(Build.class, buildId);
 		return build;
 	}
 
 	@Override
 	public Build getDefaultBuild(String projectId, String releaseId)
 	{
 		Release release = getRelease(projectId, releaseId);
 		if(release.getDefaultBuild() == null)
 			throw new NotFoundError(Build.class);
 		Build retval = release.findBuild(release.getDefaultBuild());
 		if(retval == null)
 			throw new NotFoundError(Build.class);
 		return retval;
 	}
 
 	@Override
 	public List<Build> deleteBuild(String projectId, String releaseId, String buildId)
 	{
 		Project project = getProjectById(buildId);
 		if(project.deleteBuild(releaseId, buildId) == null)
 			throw new NotFoundError(Build.class, buildId);
 		m_projectDAO.save(project);
 		return project.findRelease(releaseId).getBuilds();
 	}
 
 	@Override
 	public Component addComponent(String projectId, Component component)
 	{
 		Project project = getProjectById(projectId);
 		try
 		{
 			project.addComponent(component);
 		} catch(InvalidDataError e)
 		{
 			throw new WebApplicationException(e, Status.BAD_REQUEST);
 		}
 		m_projectDAO.save(project);
 		return component;
 	}
 
 	@Override
 	public List<Component> getComponents(String projectId)
 	{
 		Project project = getProjectById(projectId);
 		return project.getComponents();
 	}
 
 	@Override
 	public Component getComponent(String projectId, String componentId)
 	{
 		Project project = getProjectById(projectId);
 		Component retval = project.findComponent(componentId);
 		if(retval == null)
 			throw new NotFoundError(Component.class, componentId);
 		return retval;
 	}
 
 	@Override
 	public Component updateComponent(String projectId, String componentId, Component component)
 	{
 		Project project = getProjectById(projectId);
 		Component realComponent = project.findComponent(componentId);
 		if(realComponent == null)
 			throw new NotFoundError(Component.class, componentId);
 		if(component.getName() != null && !component.getName().equals(realComponent.getName()))
 			realComponent.setName(component.getName());
 		if(component.getDescription() != null && !component.getDescription().equals(realComponent.getDescription()))
 			realComponent.setDescription(component.getDescription());
 		if(component.getCode() != null && !component.getCode().equals(realComponent.getCode()))
 			realComponent.setCode(component.getCode());
 		m_projectDAO.save(project);
 		return realComponent;
 	}
 
 	@Override
 	public List<Component> deleteComponent(String projectId, String componentId)
 	{
 		Project project = getProjectById(projectId);
 		if(project.deleteComponent(componentId) == null)
 			throw new NotFoundError(Component.class, componentId);
 		m_projectDAO.save(project);
 		return project.getComponents();
 	}
 
 	@Override
 	public List<String> getAllTags(String projectId)
 	{
 		Project project = getProjectById(projectId);
 		return project.getTags();
 	}
 
 	@Override
 	public List<String> addTags(String projectId, List<String> tagsToAdd)
 	{
 		Project project = getProjectById(projectId);
 		for(String tag : tagsToAdd)
 		{
 			if(!project.getTags().contains(tag))
 				project.getTags().add(tag);
 		}
 		m_projectDAO.save(project);
 		return project.getTags();
 	}
 
 	@Override
 	public List<String> deleteTag(String projectId, String tagName)
 	{
 		Project project = getProjectById(projectId);
 		if(!project.getTags().contains(tagName))
 			throw new NotFoundError(String.class, "tagName");
 		project.getTags().remove(tagName);
 		m_projectDAO.save(project);
 		return project.getTags();
 	}
 
 	@Override
 	public Release setDefaultRelease(String projectId, String releaseId)
 	{
 		Project project = getProjectById(projectId);
 		Release release = project.findRelease(releaseId);
 		if(release == null)
 			throw new NotFoundError(Release.class, releaseId);
 		project.setDefaultRelease(release.getId());
 		m_projectDAO.save(project);
 		return release;
 	}
 
 	@Override
 	public Build setDefaultBuild(String projectId, String releaseId, String buildId)
 	{
 		Project project = getProjectById(projectId);
 		Release release = project.findRelease(releaseId);
 		if(release == null)
 			throw new NotFoundError(Release.class, releaseId);
 		Build build = release.findBuild(buildId);
 		if(build == null)
 			throw new NotFoundError(Build.class, buildId);
 		release.setDefaultBuild(build.getId());
 		m_projectDAO.save(project);
 		return build;
 	}
 
 	@Override
 	public List<String> getAllAutomationTools(String projectId)
 	{
 		Project project = m_projectDAO.get(new ObjectId(projectId));
 		if(project == null)
 			throw new WebApplicationException(Status.NOT_FOUND);
		return project.getAutomationTools();
 	}
 
 	@Override
 	public List<String> addAutomationTools(String projectId, List<String> toolsToAdd)
 	{
 		Project project = getProjectById(projectId);
 		for(String tool : toolsToAdd)
 		{
 			if(!project.getAutomationTools().contains(tool))
 				project.getAutomationTools().add(tool);
 		}
 		m_projectDAO.save(project);
 		return project.getAutomationTools();
 	}
 
 	@Override
 	public List<String> deleteAutomationTool(String projectId, String automationTool)
 	{
 		Project project = getProjectById(projectId);
 		if(!project.getAutomationTools().contains(automationTool))
 			throw new NotFoundError(String.class, automationTool);
 		project.getAutomationTools().remove(automationTool);
 		m_projectDAO.save(project);
 		return project.getAutomationTools();
 	}
 
 	@Override
 	public Release updateRelease(String projectId, String releaseId, Release release)
 	{
 		Project project = getProjectById(projectId);
 		Release realRelease = project.findRelease(releaseId);
 		if(realRelease == null)
 			throw new NotFoundError(Release.class, releaseId);
 		if(release.getName() != null && !release.getName().equals(realRelease.getName()))
 			realRelease.setName(release.getName());
 		if(release.getDefaultBuild() != null && !release.getDefaultBuild().equals(realRelease.getDefaultBuild()))
 			realRelease.setDefaultBuild(release.getDefaultBuild());
 		if(release.getTarget() != null && !release.getTarget().equals(new Date(0)) && !release.getTarget().equals(realRelease.getTarget()))
 			realRelease.setTarget(release.getTarget());
 
 		m_projectDAO.save(project);
 		return realRelease;
 	}
 
 	@Override
 	public Build updateBuild(String projectId, String releaseId, String buildId, Build build)
 	{
 		Project project = getProjectById(projectId);
 		Build realBuild = project.findBuild(releaseId, buildId);
 		if(realBuild == null)
 			throw new NotFoundError(Build.class, buildId);
 		if(build.getName() != null && !build.getName().equals(realBuild.getName()))
 			realBuild.setName(build.getName());
 		if(build.getBuilt() != null && !build.getBuilt().equals(new Date(0)) && !build.getBuilt().equals(realBuild.getBuilt()))
 			realBuild.setBuilt(build.getBuilt());
 
 		m_projectDAO.save(project);
 		return realBuild;
 	}
 
 	@Override
 	public List<DataDrivenPropertyType> getDataDrivenProperties(String projectId)
 	{
 		Project project = getProjectById(projectId);
 		return project.getDatadrivenProperties();
 	}
 
 	@Override
 	public DataDrivenPropertyType addDataDrivenProperty(String projectId, DataDrivenPropertyType property)
 	{
 		Project project = getProjectById(projectId);
 		try
 		{
 			project.addDataDrivenProperty(property);
 		} catch(InvalidDataError ex)
 		{
 			throw new WebApplicationException(ex, Status.BAD_REQUEST);
 		}
 		m_projectDAO.save(project);
 		return property;
 	}
 
 	@Override
 	public List<DataDrivenPropertyType> deleteDataDrivenPropertyByName(String projectId, String propertyName)
 	{
 		Project project = getProjectById(projectId);
 		DataDrivenPropertyType property = project.findDataDrivenPropertyByName(propertyName);
 		if(property == null)
 			throw new NotFoundError(DataDrivenPropertyType.class, propertyName);
 		project.getDatadrivenProperties().remove(property);
 		m_projectDAO.save(project);
 		return project.getDatadrivenProperties();
 	}
 
 	@Override
 	public List<DataExtension<Project>> getExtensions(String projectId)
 	{
 		Project project = getProjectById(projectId);
 		return project.getExtensions();
 	}
 
 	protected DataExtension<Project> getExtensionById(Project project, String extensionid)
 	{
 		DataExtension retval = null;
 		for(DataExtension potential : project.getExtensions())
 		{
 			if(potential.getId().equals(extensionid))
 			{
 				retval = potential;
 				break;
 			}
 		}
 		if(retval == null)
 			throw new NotFoundError(DataExtension.class, extensionid);
 		return retval;
 	}
 
 	@Override
 	public DataExtension<Project> getExtensionById(String projectId, String extensionid)
 	{
 		Project project = getProjectById(projectId);
 		return getExtensionById(project, extensionid);
 	}
 
 	@Override
 	public DataExtension<Project> addExtension(String projectId, DataExtension<Project> extension)
 	{
 		Project project = getProjectById(projectId);
 		try
 		{
 			project.addExtension(extension);
 		} catch(InvalidDataError ex)
 		{
 			throw new WebApplicationException(ex, Status.BAD_REQUEST);
 		}
 		m_projectDAO.save(project);
 		return extension;
 	}
 
 	@Override
 	public DataExtension<Project> updateExtension(String projectId, String extensionid, DataExtension<Project> extension)
 	{
 		Project project = getProjectById(projectId);
 		DataExtension realExtension = getExtensionById(project, extensionid);
 		realExtension.update(extension);
 		m_projectDAO.save(project);
 		return realExtension;
 	}
 
 	@Override
 	public List<DataExtension<Project>> deleteExtensionById(String projectId, String extensionid)
 	{
 		Project project = getProjectById(projectId);
 		DataExtension<Project> extension = getExtensionById(project, extensionid);
 		project.getExtensions().remove(extension);
 		m_projectDAO.save(project);
 		return project.getExtensions();
 	}
 
 	@Override
 	public Project getProjectByName(String name)
 	{
 		Project retval = m_projectDAO.findByName(name);
 		if(retval == null)
 			throw new NotFoundError(Project.class, name);
 		return retval;
 	}
 }
