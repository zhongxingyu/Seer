 package org.tcrun.slickij.api.data;
 
 import com.google.code.morphia.annotations.Embedded;
 import com.google.code.morphia.annotations.Indexed;
 import com.google.code.morphia.annotations.PostLoad;
 import com.google.code.morphia.annotations.PrePersist;
 import com.google.code.morphia.annotations.Property;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import org.bson.types.ObjectId;
 
 /**
  *
  * @author jcorbett
  */
 public class Release implements Serializable
 {
 	@Property
 	@Indexed
 	private ObjectId id;
 
 	@Property
 	private String name;
 
 	@Property
 	private Date target;
 
 	@Property
 	private String defaultBuild;
 
 	@Embedded
 	private List<Build> builds;
 
 	public String getId()
 	{
 		if(id == null)
 			return null;
 		else
 			return id.toString();
 	}
 
 	public void setId(ObjectId id)
 	{
 		this.id = id;
 	}
 
 	public String getName()
 	{
 		return name;
 	}
 
 	public void setName(String name)
 	{
 		this.name = name;
 	}
 
 	public Date getTarget()
 	{
 		return target;
 	}
 
 	public void setTarget(Date target)
 	{
 		this.target = target;
 	}
 
 	public List<Build> getBuilds()
 	{
 		return builds;
 	}
 
 	public void setBuilds(List<Build> builds)
 	{
 		this.builds = builds;
 	}
 
 	public String getDefaultBuild()
 	{
 		return defaultBuild;
 	}
 
 	public void setDefaultBuild(String defaultBuild)
 	{
 		this.defaultBuild = defaultBuild;
 	}
 
 	@PrePersist
 	public void prePersist()
 	{
 		if(id == null)
 			id = ObjectId.get();
 	}
 
 	@PostLoad
 	public void postLoad()
 	{
 		if(builds == null)
 			builds = new ArrayList<Build>();
 	}
 
 	public void validate() throws InvalidDataError
 	{
 		if(getName() == null || getName().equals(""))
 			throw new InvalidDataError("Release", "name", "name cannot be null, or empty.");
 		if(getTarget() == null || getTarget().equals(new Date(0)))
 			setTarget(new Date());
 		if(getBuilds() != null)
 		{
 			Set<String> buildNames = new HashSet<String>();
 			for(Build b : getBuilds())
 			{
 				b.validate();
 				if(buildNames.contains(b.getName()))
 					throw new InvalidDataError("Build", "name", "A build with name '" + b.getName() + "' already exists.");
 				else
 					buildNames.add(b.getName());
 			}
 		}
 	}
 
 	public Build findBuild(String buildId)
 	{
 		for(Build potential : getBuilds())
 			if(potential.getId().equals(buildId))
 				return potential;
 		return null;
 	}
 
 	public Build findBuildByName(String buildName)
 	{
 		for(Build potential : getBuilds())
 			if(potential.getName().equals(buildName))
 				return potential;
 		return null;
 	}
 
 	public Build findBuildByReference(BuildReference ref)
 	{
 		Build retval = null;
 		if(ref.getBuildId() != null)
 			retval = findBuild(ref.getBuildId());
 		if(retval == null && ref.getName() != null)
 			retval = findBuildByName(ref.getName());
 		if(retval != null)
 		{
 			ref.setBuildId(retval.getObjectId());
 			ref.setName(retval.getName());
 		}
 		return retval;
 	}
 
 	public ReleaseReference createReference()
 	{
 		ReleaseReference retval = new ReleaseReference();
 		retval.setReleaseId(id);
 		retval.setName(name);
 		return retval;
 	}
 }
