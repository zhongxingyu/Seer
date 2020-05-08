 package org.jboss.pressgangccms.rest.v1.entities;
 
 import org.jboss.pressgangccms.rest.v1.collections.RESTCategoryCollectionV1;
 import org.jboss.pressgangccms.rest.v1.collections.RESTProjectCollectionV1;
 import org.jboss.pressgangccms.rest.v1.collections.RESTPropertyTagCollectionV1;
 import org.jboss.pressgangccms.rest.v1.collections.RESTTagCollectionV1;
 import org.jboss.pressgangccms.rest.v1.entities.base.RESTBaseEntityWithPropertiesV1;
 
 public class RESTTagV1 extends RESTBaseEntityWithPropertiesV1<RESTTagV1, RESTTagCollectionV1>
 {
 	public static final String NAME_NAME = "name";
 	public static final String DESCRIPTION_NAME = "description";
 	public static final String CATEGORIES_NAME = "categories";
	public static final String PARENT_TAGS_NAME = "parenttags";
	public static final String CHILD_TAGS_NAME = "childtags";
 	public static final String PROJECTS_NAME = "projects";
 	
 	protected String name = null;
 	protected String description = null;
 	protected RESTCategoryCollectionV1 categories = null;
 	protected RESTTagCollectionV1 parentTags = null;
 	protected RESTTagCollectionV1 childTags = null;
 	protected RESTProjectCollectionV1 projects = null;
 	/** A list of the Envers revision numbers */
 	private RESTTagCollectionV1 revisions = null;
 	
 	@Override
 	public RESTTagCollectionV1 getRevisions()
 	{
 		return revisions;
 	}
 
 	@Override
 	public void setRevisions(final RESTTagCollectionV1 revisions)
 	{
 		this.revisions = revisions;
 	}
 	
 	@Override
 	public RESTTagV1 clone(boolean deepCopy)
 	{
 		final RESTTagV1 retValue = new RESTTagV1();
 		
 		this.cloneInto(retValue, deepCopy);
 		
 		retValue.name = this.name;
 		retValue.description = description;
 		
 		if (deepCopy)
 		{
 			if (this.categories != null)
 			{
 				retValue.categories = new RESTCategoryCollectionV1();
 				this.categories.cloneInto(retValue.categories, deepCopy);
 			}
 			
 			if (this.parentTags != null)
 			{
 				retValue.parentTags = new RESTTagCollectionV1();
 				this.parentTags.cloneInto(retValue.parentTags, deepCopy);
 			}
 			
 			if (this.childTags != null)
 			{
 				retValue.childTags = new RESTTagCollectionV1();
 				this.childTags.cloneInto(retValue.childTags, deepCopy);
 			}
 			
 			if (this.projects != null)
 			{
 				retValue.projects = new RESTProjectCollectionV1();
 				this.projects.cloneInto(retValue.projects, deepCopy);
 			}
 			
 			if (this.revisions != null)
 			{
 				retValue.revisions = new RESTTagCollectionV1();
 				this.revisions.cloneInto(retValue.revisions, deepCopy);
 			}
 		}
 		else
 		{
 			retValue.categories = this.categories;
 			retValue.parentTags = this.parentTags;
 			retValue.childTags = this.childTags;
 			retValue.projects = this.projects;
 			retValue.revisions = this.revisions;
 		}
 		
 		return retValue;
 	}
 	
 	public void explicitSetProperties(final RESTPropertyTagCollectionV1 properties)
 	{
 		this.properties = properties;
 		setParamaterToConfigured(PROPERTIES_NAME);
 	}
 
 	public String getName()
 	{
 		return name;
 	}
 
 	public void setName(final String name)
 	{
 		this.name = name;
 	}
 	
 	public void explicitSetName(final String name)
 	{
 		this.name = name;
 		this.setParamaterToConfigured(NAME_NAME);
 	}
 
 	public String getDescription()
 	{
 		return description;
 	}
 
 	public void setDescription(final String description)
 	{
 		this.description = description;
 	}
 	
 	public void explicitSetDescription(final String description)
 	{
 		this.description = description;
 		this.setParamaterToConfigured(DESCRIPTION_NAME);
 	}
 
 	public RESTCategoryCollectionV1 getCategories()
 	{
 		return categories;
 	}
 
 	public void setCategories(final RESTCategoryCollectionV1 categories)
 	{
 		this.categories = categories;
 	}
 	
 	public void explicitSetCategories(final RESTCategoryCollectionV1 categories)
 	{
 		this.categories = categories;
 		this.setParamaterToConfigured(CATEGORIES_NAME);
 	}
 
 	public RESTTagCollectionV1 getParentTags()
 	{
 		return parentTags;
 	}
 
 	public void setParentTags(final RESTTagCollectionV1 parentTags)
 	{
 		this.parentTags = parentTags;
 	}
 	
 	public void explicitSetParentTags(final RESTTagCollectionV1 parentTags)
 	{
 		this.parentTags = parentTags;
 		this.setParamaterToConfigured(PARENT_TAGS_NAME);
 	}
 
 	public RESTTagCollectionV1 getChildTags()
 	{
 		return childTags;
 	}
 
 	public void setChildTags(final RESTTagCollectionV1 childTags)
 	{
 		this.childTags = childTags;
 	}
 	
 	public void explicitSetChildTags(final RESTTagCollectionV1 childTags)
 	{
 		this.childTags = childTags;
 		this.setParamaterToConfigured(CHILD_TAGS_NAME);
 	}
 	
 	public RESTProjectCollectionV1 getProjects()
 	{
 		return projects;
 	}
 
 	public void setProjects(final RESTProjectCollectionV1 projects)
 	{
 		this.projects = projects;
 	}
 	
 	public void explicitSetProjects(final RESTProjectCollectionV1 projects)
 	{
 		this.projects = projects;
 		this.setParamaterToConfigured(PROJECTS_NAME);
 	}
 }
