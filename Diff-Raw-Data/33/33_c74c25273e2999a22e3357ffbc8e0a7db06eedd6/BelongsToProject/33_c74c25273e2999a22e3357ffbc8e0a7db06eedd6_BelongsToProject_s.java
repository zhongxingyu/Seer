 package org.tcrun.slickij.api.data.testqueries;
 
 import com.google.code.morphia.query.Criteria;
 import com.google.code.morphia.query.Query;
 import org.bson.types.ObjectId;
 import org.tcrun.slickij.api.data.Testcase;
 
 /**
  *
  * @author jcorbett
  */
 public class BelongsToProject implements TestcaseQuery
 {
 	private ObjectId projectId;
 
 	@Override
 	public Criteria toMorphiaQuery(Query<Testcase> original)
 	{
 		return original.criteria("project.id").equal(projectId);
 	}
 
 	@Override
 	public String getQueryDescription()
 	{
 		return "references project with id '" + projectId.toString() + "'.";
 	}
 
     @Override
     public void setQueryDescription(String description)
     {
     }
 
    public ObjectId getProjectId()
 	{
 		return projectId;
 	}
 
 	public void setProjectId(ObjectId projectId)
 	{
 		this.projectId = projectId;
 	}
 
     @Override
     public TestcaseQuery createCopy()
     {
         BelongsToProject copy = new BelongsToProject();
 
         copy.setProjectId(projectId);
 
         return copy;
     }
 }
