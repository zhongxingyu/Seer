 package org.tcrun.slickij.api.data;
 
 import com.google.code.morphia.annotations.*;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.bson.types.ObjectId;
 import org.codehaus.jackson.annotate.JsonIgnore;
 
 /**
  * Class representing a project in the database.
  *
  * @author jcorbett
  */
 @Entity("projects")
 public class Project implements Serializable
 {
 	@Id
 	private ObjectId id;
 
 	@Property
 	@Indexed
 	private String name;
 
 	@Property
 	private String description;
 
 	@Reference
 	private Configuration configuration;
 
 	@Property
 	private String defaultRelease;
 
 	@Embedded
 	private List<Release> releases;
 
 	@Property
 	private Date lastUpdated;
 
 	@Property
 	private List<String> tags;
 
 	@Property
 	private Map<String, String> attributes;
 
 	@Property
 	private List<String> automationTools;
 
 	@Embedded
 	private List<Component> components;
 
 	@Embedded
 	private List<DataDrivenPropertyType> datadrivenProperties;
 
 	@Embedded
 	private List<DataExtension<Project>> extensions;
 
 	public String getDescription()
 	{
 		return description;
 	}
 
 	public void setDescription(String description)
 	{
 		this.description = description;
 	}
 
 	public String getName()
 	{
 		return name;
 	}
 
 	public void setName(String name)
 	{
 		this.name = name;
 	}
 
 	public String getId()
 	{
 		return id.toString();
 	}
 
 	public void setId(ObjectId id)
 	{
 		this.id = id;
 	}
 
 	public List<Release> getReleases()
 	{
 		return releases;
 	}
 
 	public void setReleases(List<Release> releases)
 	{
 		this.releases = releases;
 	}
 
 	public Date getLastUpdated()
 	{
 		return lastUpdated;
 	}
 
 	public void setLastUpdated(Date updated)
 	{
 		lastUpdated = updated;
 	}
 
 	public Map<String, String> getAttributes()
 	{
 		return attributes;
 	}
 
 	public void setAttributes(Map<String, String> attributes)
 	{
 		this.attributes = attributes;
 	}
 
 	public List<String> getAutomationTools()
 	{
 		return automationTools;
 	}
 
 	public void setAutomationTools(List<String> automationTools)
 	{
 		this.automationTools = automationTools;
 	}
 
 	public String getDefaultRelease()
 	{
 		return defaultRelease;
 	}
 
 	public void setDefaultRelease(String defaultRelease)
 	{
 		this.defaultRelease = defaultRelease;
 	}
 
 	public List<String> getTags()
 	{
 		return tags;
 	}
 
 	public void setTags(List<String> tags)
 	{
 		this.tags = tags;
 	}
 
 	public List<Component> getComponents()
 	{
 		return components;
 	}
 
 	public void setComponents(List<Component> components)
 	{
 		this.components = components;
 	}
 
 	@PrePersist
 	public void prePersist()
 	{
 		lastUpdated = new Date();
 	}
 
 	@PostLoad
 	public void postLoad()
 	{
 		if(releases == null)
 			releases = new ArrayList<Release>();
 		if(tags == null)
 			tags = new ArrayList<String>();
 		if(automationTools == null)
 			automationTools = new ArrayList<String>();
 		if(attributes == null)
 			attributes = new HashMap<String, String>();
 		if(components == null)
 			components = new ArrayList<Component>();
 		if(datadrivenProperties == null)
 			datadrivenProperties = new ArrayList<DataDrivenPropertyType>();
 		if(extensions == null)
 			extensions = new ArrayList<DataExtension<Project>>();
 		for(DataExtension<Project> extension : extensions)
 			extension.setParent(this);
 	}
 
 	public Release findRelease(String releaseId)
 	{
 		for(Release potential : getReleases())
 		{
 			if(potential.getId().equals(releaseId))
 				return potential;
 		}
 		return null;
 	}
 
 	public Release deleteRelease(String releaseId)
 	{
 		Release r = findRelease(releaseId);
 		if(r == null)
 			return null;
 		getReleases().remove(r);
 		return r;
 	}
 
 	public Release findReleaseByName(String releaseName)
 	{
 		for(Release potential : getReleases())
 		{
 			if(potential.getName().equals(releaseName))
 				return potential;
 		}
 		return null;
 	}
 
 	public Release findReleaseByReference(ReleaseReference ref)
 	{
 		Release retval = null;
 		if(ref.getReleaseId() != null)
 			retval = findRelease(ref.getReleaseId());
 		if(retval == null && ref.getName() != null)
 			retval = findReleaseByName(ref.getName());
 		if(retval != null)
 		{
 			ref.setReleaseId(new ObjectId(retval.getId()));
 			ref.setName(retval.getName());
 		}
 
 		return retval;
 	}
 
 	public Release addRelease(Release release) throws InvalidDataError
 	{
 		release.validate();
 		if(findReleaseByName(this.getName()) != null)
 			throw new InvalidDataError("Release", "name", "A this with name '" + this.getName() + "' already exists.");
 		getReleases().add(release);
 		return release;
 	}
 
 	public Build findBuild(String releaseId, String buildId)
 	{
 		Release r = findRelease(releaseId);
 		if(r == null)
 			return null;
 		return r.findBuild(buildId);
 	}
 
 	public Build findBuildByName(String releaseId, String buildName)
 	{
 		Release r = findRelease(releaseId);
 		if(r == null)
 			return null;
 		return r.findBuildByName(buildName);
 	}
 
 	public Build addBuild(String releaseId, Build build) throws InvalidDataError
 	{
 		build.validate();
 		Release r = findRelease(releaseId);
 		if(r == null)
 			throw new InvalidDataError("Release", "id", "A Release with id " + releaseId + " does not exist.");
 		if(r.findBuildByName(build.getName()) != null)
 			throw new InvalidDataError("Build", "name", "A Build with name '" + build.getName() + "' already exists in release '" + r.getName() + "'.");
 		r.getBuilds().add(build);
 		return build;
 	}
 
 	public Build deleteBuild(String releaseId, String buildId)
 	{
 		Release r = findRelease(releaseId);
 		if(r == null)
 			return null;
 		Build b = r.findBuild(buildId);
 		if(b == null)
 			return b;
 		r.getBuilds().remove(b);
 		return b;
 	}
 
 	public Component findComponent(String componentId)
 	{
 		for(Component potential : getComponents())
 			if(potential.getId().equals(componentId))
 				return potential;
 		return null;
 	}
 
 	public Component findComponentByCode(String code)
 	{
 		for(Component potential : getComponents())
 			if(potential.getCode().equals(code))
 				return potential;
 		return null;
 	}
 
 	public Component findComponentByName(String name)
 	{
 		for(Component potential : getComponents())
 			if(potential.getName().equals(name))
 				return potential;
 		return null;
 	}
 
 	public Component findComponentByReference(ComponentReference ref)
 	{
 		Component retval = null;
 		if(ref.getId() != null)
 			retval = findComponent(ref.getId());
 		if(retval == null && ref.getCode() != null)
 			retval = findComponentByCode(ref.getCode());
 		if(retval == null && ref.getName() != null)
 			retval = findComponentByName(ref.getName());
 		if(retval != null)
 		{
 			ref.setCode(retval.getCode());
 			ref.setName(retval.getName());
 			ref.setId(retval.getObjectId());
 		}
 		return retval;
 	}
 
 	public Component addComponent(Component component) throws InvalidDataError
 	{
 		component.validate();
 		if(findComponentByName(component.getName()) != null)
 			throw new InvalidDataError("Component", "name", "a component with name '" + component.getName() + "' already exists.");
 		if(findComponentByCode(component.getCode()) != null)
 			throw new InvalidDataError("Component", "code", "a component with code '" + component.getCode() + "' already exists.");
 		getComponents().add(component);
 		return component;
 	}
 
 	public Component deleteComponent(String componentId)
 	{
 		Component retval = findComponent(componentId);
 		if(retval == null)
 			return null;
 		getComponents().remove(retval);
 		return retval;
 	}
 
 	public List<DataDrivenPropertyType> getDatadrivenProperties()
 	{
 		return datadrivenProperties;
 	}
 
 	public void setDatadrivenProperties(List<DataDrivenPropertyType> datadrivenProperties)
 	{
 		this.datadrivenProperties = datadrivenProperties;
 	}
 
 	public DataDrivenPropertyType findDataDrivenPropertyByName(String name)
 	{
 		DataDrivenPropertyType retval = null;
 		for(DataDrivenPropertyType potential : getDatadrivenProperties())
 		{
 			if(potential.getName().equals(name))
 			{
 				retval = potential;
 				break;
 			}
 		}
 
 		return retval;
 	}
 
 	public DataDrivenPropertyType addDataDrivenProperty(DataDrivenPropertyType dataDrivenProp) throws InvalidDataError
 	{
 		dataDrivenProp.validate();
 		if(findDataDrivenPropertyByName(dataDrivenProp.getName()) != null)
 			throw new InvalidDataError("DataDrivenPropertyType", "name", "There is already a data driven property type by the name of '" + dataDrivenProp.getName() + "'.");
 		getDatadrivenProperties().add(dataDrivenProp);
 		return dataDrivenProp;
 	}
 
 	public Configuration getConfiguration()
 	{
 		return configuration;
 	}
 
 	public void setConfiguration(Configuration configuration)
 	{
 		this.configuration = configuration;
 	}
 
 	public List<DataExtension<Project>> getExtensions()
 	{
 		return extensions;
 	}
 
 	public void setExtensions(List<DataExtension<Project>> extensions)
 	{
 		this.extensions = extensions;
 	}
 
 	public void addExtension(DataExtension extension) throws InvalidDataError
 	{
 		extension.validate();
 		extensions.add(extension);
 	}
 
 	@JsonIgnore
 	public ObjectId getObjectId()
 	{
 		return id;
 	}
 
 	public ProjectReference createReference()
 	{
 		ProjectReference ref = new ProjectReference();
 		ref.setId(id);
 		ref.setName(name);
 		return ref;
 	}
     
     public String getDefaultBuildName()
     {
         String retval = "Not Set";
         if(getDefaultRelease() != null && !getDefaultRelease().isEmpty())
         {
             Release defRelease = findRelease(getDefaultRelease());
             if(defRelease != null)
             {
                 retval = defRelease.getName();
                 if(defRelease.getDefaultBuild() != null && !defRelease.getDefaultBuild().isEmpty())
                 {
                     Build defBuild = defRelease.findBuild(defRelease.getDefaultBuild());
                     if(defBuild != null)
                     {
                         retval += " Build " + defBuild.getName();
                     }
                 }
             }
         }
         
         return retval;
     }
 
 }
