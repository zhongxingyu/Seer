 package org.tcrun.slickij.data.dao;
 
 import org.tcrun.slickij.api.data.dao.ProjectDAO;
 import com.google.code.morphia.Datastore;
 import com.google.code.morphia.dao.BasicDAO;
 import com.google.inject.Inject;
 import org.bson.types.ObjectId;
 import org.tcrun.slickij.api.data.Project;
 import org.tcrun.slickij.api.data.ProjectReference;
 
 /**
  *
  * @author jcorbett
  */
 public class ProjectDAOImpl extends BasicDAO<Project, ObjectId> implements ProjectDAO
 {
 	@Inject
 	public ProjectDAOImpl(Datastore ds)
 	{
 		super(Project.class, ds);
 	}
 
 	@Override
 	public Project findByName(String name)
 	{
 		Project retval = null;
 		retval = ds.find(Project.class, "name", name).get();
 		return retval;
 	}
 
 	@Override
 	public Project findByReference(ProjectReference ref)
 	{
		if(ref == null)
			return null;	    
 		Project project = null;
 		if(ref.getObjectId() != null)
 			project = this.get(ref.getObjectId());
 		else
 			project = this.findByName(ref.getName());
 		if(project != null && ref.getId() == null)
 			ref.setId(project.getObjectId());
 		if(project != null && ref.getName() == null)
 			ref.setName(project.getName());
 		return project;
 	}
 
 /*
 	@Override
 	public Project findById(String apikey)
 	{
 		Project retval = null;
 		retval = ds.find(Project.class, "id", Key).get();
 		return retval;
 	}
  *
  */
 }
