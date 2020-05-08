 package com.redhat.topicindex.rest.entities.interfaces;
 
 import java.util.Date;
 
 import com.redhat.topicindex.rest.collections.RESTTranslatedTopicCollectionV1;
 import com.redhat.topicindex.rest.collections.RESTTranslatedTopicStringCollectionV1;
 
/**
 * The RESTTranslatedTopicV1 class is a combination of the TranslatedTopic and TranslatedTopicData classes. In the database, a TranslatedTopic
 * is a reference to a particular revision of a topic, and it has multiple TranslatedTopicData children for each locale. From the REST point of view
 * each RESTTranslatedTopicV1 combines the topic information from the TranslatedTopic, and the translation information from the TranslatedTopicData class.
 * 
 * @author Matthew Casperson
 */
 public class RESTTranslatedTopicV1 extends RESTBaseTopicV1<RESTTranslatedTopicV1, RESTTranslatedTopicCollectionV1>
 {
 	public static final String TOPICID_NAME = "topicid";
 	public static final String TOPICREVISION_NAME = "topicrevision";
 	public static final String TOPIC_NAME = "topic";
 	public static final String TRANSLATEDTOPICSTRING_NAME = "translatedtopicstring_OTM";
 	public static final String TRANSLATIONPERCENTAGE_NAME = "translationpercentage";
 	public static final String HTML_UPDATED = "htmlUpdated";
 	public static final String OUTGOING_NAME = "outgoingTranslatedRelationships";
 	public static final String INCOMING_NAME = "incomingTranslatedRelationships";
 	public static final String ALL_LATEST_OUTGOING_NAME = "allLatestOutgoingRelationships";
 	public static final String ALL_LATEST_INCOMING_NAME = "allLatestIncomingRelationships";
 	
 	protected RESTTopicV1 topic = null;
 	protected Integer translatedTopicId = null;
 	protected Integer topicId = null;
 	protected Integer topicRevision = null;
 	protected Integer translationPercentage = null;
 	protected Date htmlUpdated = null;
 	protected RESTTranslatedTopicStringCollectionV1 translatedTopicStrings = null;
 	protected RESTTranslatedTopicCollectionV1 outgoingTranslatedRelationships = null;
 	protected RESTTranslatedTopicCollectionV1 incomingTranslatedRelationships = null;
 	protected RESTTranslatedTopicCollectionV1 outgoingRelationships = null;
 	protected RESTTranslatedTopicCollectionV1 incomingRelationships = null;
 	/** A list of the Envers revision numbers */
 	private RESTTranslatedTopicCollectionV1 revisions = null;
 	
 	@Override
 	public RESTTranslatedTopicCollectionV1 getRevisions()
 	{
 		return revisions;
 	}
 
 	@Override
 	public void setRevisions(final RESTTranslatedTopicCollectionV1 revisions)
 	{
 		this.revisions = revisions;
 	}
 
 	@Override
 	public RESTTranslatedTopicV1 clone(final boolean deepCopy)
 	{
 		final RESTTranslatedTopicV1 retValue = new RESTTranslatedTopicV1();
 		
 		this.cloneInto(retValue, deepCopy);
 		
 		retValue.setTopicId(this.topicId);
 		retValue.setTopicRevision(this.topicRevision);
 		retValue.setTranslatedTopicId(this.translatedTopicId);
 		retValue.setTranslationPercentage(this.translationPercentage);
 		
 		if (deepCopy)
 		{
 			if (this.translatedTopicStrings != null)
 			{
 				retValue.translatedTopicStrings = new RESTTranslatedTopicStringCollectionV1();
 				this.translatedTopicStrings.cloneInto(retValue.translatedTopicStrings, deepCopy);
 			}
 			
 			if (this.outgoingTranslatedRelationships != null)
 			{
 				retValue.outgoingTranslatedRelationships = new RESTTranslatedTopicCollectionV1();
 				this.outgoingTranslatedRelationships.cloneInto(retValue.outgoingTranslatedRelationships, deepCopy);
 			}
 			
 			if (this.incomingTranslatedRelationships != null)
 			{
 				retValue.incomingTranslatedRelationships = new RESTTranslatedTopicCollectionV1();
 				this.incomingTranslatedRelationships.cloneInto(retValue.incomingTranslatedRelationships, deepCopy);
 			}
 			
 			if (this.incomingRelationships != null)
 			{
 				retValue.incomingRelationships = new RESTTranslatedTopicCollectionV1();
 				this.incomingRelationships.cloneInto(retValue.incomingRelationships, deepCopy);
 			}
 			
 			if (this.outgoingRelationships != null)
 			{
 				retValue.outgoingRelationships = new RESTTranslatedTopicCollectionV1();
 				this.outgoingRelationships.cloneInto(retValue.outgoingRelationships, deepCopy);
 			}
 			
 			if (this.revisions != null)
 			{
 				retValue.revisions = new RESTTranslatedTopicCollectionV1();
 				this.revisions.cloneInto(retValue.revisions, deepCopy);
 			}
 
 			retValue.setTopic(this.topic != null ? this.topic.clone(deepCopy) : null);
 		}
 		else
 		{
 			retValue.setTranslatedTopicStrings_OTM(this.getTranslatedTopicStrings_OTM());
 			retValue.topic = this.topic;
 			retValue.outgoingTranslatedRelationships = this.outgoingTranslatedRelationships;
 			retValue.incomingTranslatedRelationships = this.incomingTranslatedRelationships;
 			retValue.outgoingRelationships = this.outgoingRelationships;
 			retValue.incomingRelationships = this.incomingRelationships;
 			retValue.revisions = this.revisions;
 		}
 		return retValue;
 	}
 	
 	public Integer getTranslatedTopicId() {
 		return translatedTopicId;
 	}
 
 	public void setTranslatedTopicId(Integer translatedTopicId) {
 		this.translatedTopicId = translatedTopicId;
 	}
 
 	public void explicitSetXml(final String xml)
 	{
 		setXml(xml);
 		setParamaterToConfigured(XML_NAME);
 	}
 	
 	public void explicitSetXmlErrors(final String xmlErrors)
 	{
 		setXmlErrors(xmlErrors);
 		setParamaterToConfigured(XML_ERRORS_NAME);
 	}
 
 	public void explicitSetHtml(final String html)
 	{
 		setHtml(html);
 		setParamaterToConfigured(HTML_NAME);
 	}
 	
 	public Integer getTopicId()
 	{
 		return topicId;
 	}
 
 	public void setTopicId(final Integer topicId)
 	{
 		this.topicId = topicId;
 	}
 	
 	public void explicitSetTopicId(final Integer topicId)
 	{
 		this.topicId = topicId;
 		this.setParamaterToConfigured(TOPICID_NAME);
 	}
 
 	public Integer getTopicRevision()
 	{
 		return topicRevision;
 	}
 
 	public void setTopicRevision(final Integer topicRevision)
 	{
 		this.topicRevision = topicRevision;
 	}
 	
 	public RESTTopicV1 getTopic()
 	{
 		return topic;
 	}
 
 	public void setTopic(final RESTTopicV1 topic)
 	{
 		this.topic = topic;
 	}
 	
 	public void explicitSetTopicRevision(final Integer topicRevision)
 	{
 		this.topicRevision = topicRevision;
 		this.setParamaterToConfigured(TOPICREVISION_NAME);
 	}
 	
 	public Integer getTranslationPercentage() {
 		return translationPercentage;
 	}
 
 	public void setTranslationPercentage(Integer translationPercentage) {
 		this.translationPercentage = translationPercentage;
 	}
 	
 	public void explicitSetTranslationPercentage(Integer translationPercentage) {
 		this.translationPercentage = translationPercentage;
 		this.setParamaterToConfigured(TRANSLATIONPERCENTAGE_NAME);
 	}
 
 	public RESTTranslatedTopicStringCollectionV1 getTranslatedTopicStrings_OTM()
 	{
 		return translatedTopicStrings;
 	}
 
 	public void setTranslatedTopicStrings_OTM(final RESTTranslatedTopicStringCollectionV1 translatedTopicStrings)
 	{
 		this.translatedTopicStrings = translatedTopicStrings;
 	}
 	
 	public void explicitSetTranslatedTopicString_OTM(final RESTTranslatedTopicStringCollectionV1 translatedTopicStrings)
 	{
 		this.translatedTopicStrings = translatedTopicStrings;
 		this.setParamaterToConfigured(TRANSLATEDTOPICSTRING_NAME);
 	}
 	
 	public Date getHtmlUpdated()
 	{
 		return htmlUpdated;
 	}
 
 	public void setHtmlUpdated(final Date htmlUpdated)
 	{
 		this.htmlUpdated = htmlUpdated;
 	}
 	
 	public void explicitSetHtmlUpdated(final Date htmlUpdated)
 	{
 		this.htmlUpdated = htmlUpdated;
 		this.setParamaterToConfigured(HTML_UPDATED);
 	}
 	
 	public void explicitSetLocale(final String locale)
 	{
 		setLocale(locale);
 		setParamaterToConfigured(LOCALE_NAME);
 	}
 	
 	public RESTTranslatedTopicCollectionV1 getOutgoingTranslatedRelationships()
 	{
 		return outgoingTranslatedRelationships;
 	}
 	
 	public void setOutgoingTranslatedRelationships(final RESTTranslatedTopicCollectionV1 outgoingTranslatedRelationships)
 	{
 		this.outgoingTranslatedRelationships = outgoingTranslatedRelationships;
 	}
 	
 	public RESTTranslatedTopicCollectionV1 getIncomingTranslatedRelationships()
 	{
 		return incomingTranslatedRelationships;
 	}
 	
 	public void setIncomingTranslatedRelationships(final RESTTranslatedTopicCollectionV1 incomingTranslatedRelationships)
 	{
 		this.incomingTranslatedRelationships = incomingTranslatedRelationships;
 	}
 	
 	@Override
 	public RESTTranslatedTopicCollectionV1 getOutgoingRelationships()
 	{
 		return outgoingRelationships;
 	}
 
 	@Override
 	public void setOutgoingRelationships(final RESTTranslatedTopicCollectionV1 outgoingRelationships)
 	{
 		this.outgoingRelationships = outgoingRelationships;
 	}
 
 	@Override
 	public RESTTranslatedTopicCollectionV1 getIncomingRelationships()
 	{
 		return incomingRelationships;
 	}
 
 	@Override
 	public void setIncomingRelationships(final RESTTranslatedTopicCollectionV1 incomingRelationships)
 	{
 		this.incomingRelationships = incomingRelationships;
 	}
 }
