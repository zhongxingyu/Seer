 package org.tcrun.slickij.api.data.testqueries;
 
 import com.google.code.morphia.query.Criteria;
 import com.google.code.morphia.query.Query;
 import org.bson.types.ObjectId;
import org.codehaus.jackson.annotate.JsonIgnore;
 import org.tcrun.slickij.api.data.Testcase;
 
 /**
  *
  * @author jcorbett
  */
 public class BelongsToComponent implements TestcaseQuery
 {
 	private ObjectId componentId;
 
 	@Override
 	public Criteria toMorphiaQuery(Query<Testcase> original)
 	{
 		return original.criteria("component.id").equal(componentId);
 	}
 
 	@Override
 	public String getQueryDescription()
 	{
 		return "references component with id '" + componentId.toString() + "'.";
 	}
 
     @Override
     public void setQueryDescription(String description)
     {
     }
 
    @JsonIgnore
    public ObjectId getComponentObjectId()
 	{
 		return componentId;
 	}
 
    public String getComponentId()
    {
        if(componentId == null)
            return null;
        else
            return componentId.toString();
    }

 	public void setComponentId(ObjectId componentId)
 	{
 		this.componentId = componentId;
 	}
 
     @Override
     public TestcaseQuery createCopy()
     {
         BelongsToComponent copy = new BelongsToComponent();
 
         copy.setComponentId(componentId);
 
         return copy;
     }
 }
