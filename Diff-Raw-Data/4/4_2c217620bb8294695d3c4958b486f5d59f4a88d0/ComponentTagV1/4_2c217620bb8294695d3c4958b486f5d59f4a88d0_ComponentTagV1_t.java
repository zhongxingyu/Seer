 package org.jboss.pressgang.ccms.rest.v1.components;
 
 import java.util.List;
 
 import org.jboss.pressgang.ccms.rest.v1.collections.items.RESTProjectCollectionItemV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.items.join.RESTCategoryInTagCollectionItemV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTProjectV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTTagV1;
import org.jboss.pressgang.ccms.rest.v1.entities.base.RESTBaseTagV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.join.RESTCategoryInTagV1;
 
 /**
  * This component contains methods that can be applied against tags
  * @author Matthew Casperson
  */
 public class ComponentTagV1 extends ComponentBaseRESTEntityWithPropertiesV1
 {
 	final RESTTagV1 source;
 	
 	public ComponentTagV1(final RESTTagV1 source)
 	{
 		super(source);
 		this.source = source;
 	}
 	
 	public boolean containedInCategory(final Integer categoryId)
 	{
 		return containedInCategory(source, categoryId);
 	}
 	
	static public boolean containedInCategory(final RESTBaseTagV1<?, ?, ?> source, final Integer categoryId)
 	{
 		if (source.getCategories() != null && source.getCategories().getItems() != null)
 		{
 			for (final RESTCategoryInTagCollectionItemV1 categoryItem : source.getCategories().getItems())
 			{
 			    final RESTCategoryInTagV1 category = categoryItem.getItem();
 				if (category != null && categoryId.equals(category.getId()) && !categoryItem.returnIsRemoveItem())
 					return true;
 			}
 		}
 
 		return false;
 	}
 	
 	public boolean containedInCategory(final List<Integer> categoryIds)
     {
         return containedInCategory(source, categoryIds);
     }
 
 	static public boolean containedInCategory(final RESTTagV1 source, final List<Integer> categoryIds)
     {
 	    if (categoryIds == null) return false;
 	    
 	    for (final Integer categoryId : categoryIds)
 	    {
 	        if (containedInCategory(source, categoryId))
 	        {
 	            return true;
 	        }
 	    }
 	    
 	    return false;
     }
 	
 	public boolean containedInProject(final Integer id)
 	{
 		return containedInCategory(source, id);
 	}
 	
 	static public boolean containedInProject(final RESTTagV1 source, final Integer id)
 	{
 		if (source.getProjects() != null && source.getProjects().getItems() != null)
 		{
 			for (final RESTProjectCollectionItemV1 projectItem : source.getProjects().getItems())
 			{
 			    final RESTProjectV1 project = projectItem.getItem();
 				if (id.equals(project.getId()) && !projectItem.returnIsRemoveItem())
 					return true;
 			}
 		}
 
 		return false;
 	}
 }
