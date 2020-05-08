 package org.jboss.pressgangccms.rest.v1.entities;
 
 import org.jboss.pressgangccms.rest.v1.collections.RESTProjectCollectionV1;
 import org.jboss.pressgangccms.rest.v1.collections.RESTTagCollectionV1;
 import org.jboss.pressgangccms.rest.v1.entities.base.RESTBaseEntityV1;
 
 public class RESTProjectV1 extends RESTBaseEntityV1<RESTProjectV1, RESTProjectCollectionV1>
 {
 	public static final String NAME_NAME = "name";
 	public static final String DESCRIPTION_NAME = "description";
 	public static final String TAGS_NAME = "tags";
 	
 	private String name = null;
 	private String description = null;
 	private RESTTagCollectionV1 tags = null;
 	/** A list of the Envers revision numbers */
 	private RESTProjectCollectionV1 revisions = null;
 	
 	@Override
 	public RESTProjectCollectionV1 getRevisions()
 	{
 		return revisions;
 	}
 
 	@Override
 	public void setRevisions(final RESTProjectCollectionV1 revisions)
 	{
 		this.revisions = revisions;
 	}
 	
 	@Override
 	public RESTProjectV1 clone(boolean deepCopy)
 	{
 		final RESTProjectV1 retValue = new RESTProjectV1();
 		
 		this.cloneInto(retValue, deepCopy);
 		
 		retValue.name = this.name;
 		retValue.description = description;
 
 		if (deepCopy)
 		{
 			if (this.tags != null)
 			{
 				retValue.tags = new RESTTagCollectionV1();
 				this.tags.cloneInto(retValue.tags, deepCopy);
 			}
 			
 			if (this.revisions != null)
 			{
 				retValue.revisions = new RESTProjectCollectionV1();
 				this.revisions.cloneInto(retValue.revisions, deepCopy);
 			}
 		}
 		else
 		{
 			retValue.tags = this.tags;
 			retValue.revisions = this.revisions;
 		}
 		
 		return retValue;
 	}
 
 	public String getName()
 	{
 		return name;
 	}
 
 	public void setName(final String name)
 	{
 		this.name = name;
 	}
 	
	public void setNameExplicit(final String name)
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
 	
	public void setDescriptionExplicit(final String description)
 	{
 		this.description = description;
 		this.setParamaterToConfigured(DESCRIPTION_NAME);
 	}
 
 	public RESTTagCollectionV1 getTags()
 	{
 		return tags;
 	}
 
 	public void setTags(final RESTTagCollectionV1 tags)
 	{
 		this.tags = tags;
 	}
 	
	public void setTagsExplicit(final RESTTagCollectionV1 tags)
 	{
 		this.tags = tags;
 		this.setParamaterToConfigured(TAGS_NAME);
 	}
 }
