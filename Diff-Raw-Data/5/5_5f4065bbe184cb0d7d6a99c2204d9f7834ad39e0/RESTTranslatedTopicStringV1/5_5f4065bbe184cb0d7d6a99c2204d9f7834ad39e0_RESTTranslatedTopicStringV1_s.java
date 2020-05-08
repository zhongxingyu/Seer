 package com.redhat.topicindex.rest.entities.interfaces;
 
 public class RESTTranslatedTopicStringV1 extends RESTBaseEntityV1<RESTTranslatedTopicStringV1>
 {
 	public static final String ORIGINALSTRING_NAME = "originalstring";
 	public static final String TRANSLATEDSTRING_NAME = "translatedstring";
 	public static final String TRANSLATEDTOPIC_NAME = "translatedtopic";
 	
 	private RESTTranslatedTopicV1 translatedTopic;
 	private String originalString;
 	private String translatedString;
 	
 	@Override
 	public RESTTranslatedTopicStringV1 clone(final boolean deepCopy)
 	{
 		final RESTTranslatedTopicStringV1 retValue = new RESTTranslatedTopicStringV1();
 		
 		this.cloneInto(retValue, deepCopy);
 		
 		retValue.setOriginalString(this.originalString);
 		retValue.setTranslatedString(this.translatedString);
 		
 		if (deepCopy)
 		{
 			retValue.setTranslatedTopic(translatedTopic != null ? this.translatedTopic.clone(deepCopy) : null);
 		}
 		else
 		{
 			retValue.setTranslatedTopic(this.translatedTopic);
 		}
 		return retValue;
 	}
 
 	public RESTTranslatedTopicV1 getTranslatedTopic()
 	{
 		return translatedTopic;
 	}
 
 	public void setTranslatedTopic(final RESTTranslatedTopicV1 translatedTopic)
 	{
 		this.translatedTopic = translatedTopic;
 	}
 
 	public String getOriginalString()
 	{
 		return originalString;
 	}
 
 	public void setOriginalString(final String originalString)
 	{
 		this.originalString = originalString;
 	}
 	
	public void setOriginalStringExplicit(final String originalString)
 	{
 		this.originalString = originalString;
 		this.setParamaterToConfigured(ORIGINALSTRING_NAME);
 	}
 
 	public String getTranslatedString()
 	{
 		return translatedString;
 	}
 
 	public void setTranslatedString(final String translatedString)
 	{
 		this.translatedString = translatedString;
 	}
 	
	public void setTranslatedStringExplicit(final String translatedString)
 	{
 		this.translatedString = translatedString;
 		this.setParamaterToConfigured(TRANSLATEDSTRING_NAME);
 	}
 }
