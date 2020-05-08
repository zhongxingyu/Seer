 package org.jboss.pressgang.ccms.rest.v1.entities;
 
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTTranslatedTopicCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.RESTTranslatedTopicStringCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.items.RESTTranslatedTopicCollectionItemV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.base.RESTBaseTopicV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.contentspec.RESTTranslatedCSNodeV1;
 
 /**
  * The RESTTranslatedTopicV1 class is a combination of the TranslatedTopic and TranslatedTopicData classes. In the database,
  * a TranslatedTopic is a reference to a particular revision of a topic, and it has multiple TranslatedTopicData children for each locale
  * . From the REST point of view each RESTTranslatedTopicV1 combines the topic information from the TranslatedTopic,
  * and the translation information from the TranslatedTopicData class.
  *
  * @author Matthew Casperson
  */
 public class RESTTranslatedTopicV1 extends RESTBaseTopicV1<RESTTranslatedTopicV1, RESTTranslatedTopicCollectionV1,
         RESTTranslatedTopicCollectionItemV1> {
     public static final String TOPICID_NAME = "topicId";
     public static final String TOPICREVISION_NAME = "topicRevision";
     public static final String TOPIC_NAME = "topic";
     public static final String TRANSLATEDTOPICSTRING_NAME = "translatedTopicString_OTM";
     public static final String TRANSLATIONPERCENTAGE_NAME = "translationPercentage";
     public static final String OUTGOING_NAME = "outgoingTranslatedRelationships";
     public static final String INCOMING_NAME = "incomingTranslatedRelationships";
     public static final String ALL_LATEST_OUTGOING_NAME = "allLatestOutgoingRelationships";
     public static final String ALL_LATEST_INCOMING_NAME = "allLatestIncomingRelationships";
     public static final String TRANSLATED_CSNODE_NAME = "translatedCSNode";
     public static final String TRANSLATED_XML_CONDITION = "translatedXMLCondition";
 
     protected RESTTopicV1 topic = null;
     protected Integer translatedTopicId = null;
     protected Integer topicId = null;
     protected Integer topicRevision = null;
     protected Integer translationPercentage = null;
     protected Boolean containsFuzzyTranslation = false;
     protected String translatedXMLCondition = null;
     protected RESTTranslatedTopicStringCollectionV1 translatedTopicStrings = null;
     protected RESTTranslatedTopicCollectionV1 outgoingTranslatedRelationships = null;
     protected RESTTranslatedTopicCollectionV1 incomingTranslatedRelationships = null;
     protected RESTTranslatedTopicCollectionV1 outgoingRelationships = null;
     protected RESTTranslatedTopicCollectionV1 incomingRelationships = null;
     protected RESTTranslatedCSNodeV1 translatedCSNode = null;
 
     /**
      * A list of the Envers revision numbers
      */
     private RESTTranslatedTopicCollectionV1 revisions = null;
 
     @Override
     public RESTTranslatedTopicCollectionV1 getRevisions() {
         return revisions;
     }
 
     @Override
     public void setRevisions(final RESTTranslatedTopicCollectionV1 revisions) {
         this.revisions = revisions;
     }
 
     @Override
     public RESTTranslatedTopicV1 clone(final boolean deepCopy) {
         final RESTTranslatedTopicV1 retValue = new RESTTranslatedTopicV1();
 
         cloneInto(retValue, deepCopy);
 
         return retValue;
     }
 
     public void cloneInto(final RESTTranslatedTopicV1 clone, final boolean deepCopy) {
         super.cloneInto(clone, deepCopy);
 
         clone.topicId = topicId;
         clone.topicRevision = topicRevision;
         clone.translatedTopicId = translatedTopicId;
         clone.translationPercentage = translationPercentage;
         clone.containsFuzzyTranslation = containsFuzzyTranslation;
         clone.translatedXMLCondition = translatedXMLCondition;
 
         if (deepCopy) {
             if (translatedTopicStrings != null) {
                 clone.translatedTopicStrings = new RESTTranslatedTopicStringCollectionV1();
                 translatedTopicStrings.cloneInto(clone.translatedTopicStrings, deepCopy);
             }  else {
                 clone.translatedTopicStrings = null;
             }
 
             if (outgoingTranslatedRelationships != null) {
                 clone.outgoingTranslatedRelationships = new RESTTranslatedTopicCollectionV1();
                 outgoingTranslatedRelationships.cloneInto(clone.outgoingTranslatedRelationships, deepCopy);
             }  else {
                 clone.outgoingTranslatedRelationships = null;
             }
 
             if (incomingTranslatedRelationships != null) {
                 clone.incomingTranslatedRelationships = new RESTTranslatedTopicCollectionV1();
                 incomingTranslatedRelationships.cloneInto(clone.incomingTranslatedRelationships, deepCopy);
             }  else {
                 clone.incomingTranslatedRelationships = null;
             }
 
             if (incomingRelationships != null) {
                 clone.incomingRelationships = new RESTTranslatedTopicCollectionV1();
                 incomingRelationships.cloneInto(clone.incomingRelationships, deepCopy);
             }  else {
                 clone.incomingRelationships = null;
             }
 
             if (outgoingRelationships != null) {
                 clone.outgoingRelationships = new RESTTranslatedTopicCollectionV1();
                 outgoingRelationships.cloneInto(clone.outgoingRelationships, deepCopy);
             }  else {
                 clone.outgoingRelationships = null;
             }
 
             if (revisions != null) {
                 clone.revisions = new RESTTranslatedTopicCollectionV1();
                 revisions.cloneInto(clone.revisions, deepCopy);
             }  else {
                 clone.revisions = null;
             }
 
             clone.setTopic(topic != null ? topic.clone(deepCopy) : null);
             clone.translatedCSNode = translatedCSNode != null ? translatedCSNode.clone(deepCopy) : null;
         } else {
             clone.translatedTopicStrings = translatedTopicStrings;
             clone.topic = topic;
             clone.outgoingTranslatedRelationships = outgoingTranslatedRelationships;
             clone.incomingTranslatedRelationships = incomingTranslatedRelationships;
             clone.outgoingRelationships = outgoingRelationships;
             clone.incomingRelationships = incomingRelationships;
             clone.revisions = revisions;
             clone.translatedCSNode = translatedCSNode;
         }
     }
 
     public Integer getTranslatedTopicId() {
         return translatedTopicId;
     }
 
     public void setTranslatedTopicId(Integer translatedTopicId) {
         this.translatedTopicId = translatedTopicId;
     }
 
     public void explicitSetXml(final String xml) {
         setXml(xml);
         setParameterToConfigured(XML_NAME);
     }
 
     public void explicitSetXmlErrors(final String xmlErrors) {
         setXmlErrors(xmlErrors);
         setParameterToConfigured(XML_ERRORS_NAME);
     }
 
     public Integer getTopicId() {
         return topicId;
     }
 
     public void setTopicId(final Integer topicId) {
         this.topicId = topicId;
     }
 
     public void explicitSetTopicId(final Integer topicId) {
         this.topicId = topicId;
         setParameterToConfigured(TOPICID_NAME);
     }
 
     public Integer getTopicRevision() {
         return topicRevision;
     }
 
     public void setTopicRevision(final Integer topicRevision) {
         this.topicRevision = topicRevision;
     }
 
     public RESTTopicV1 getTopic() {
         return topic;
     }
 
     public void setTopic(final RESTTopicV1 topic) {
         this.topic = topic;
     }
 
     public void explicitSetTopicRevision(final Integer topicRevision) {
         this.topicRevision = topicRevision;
         setParameterToConfigured(TOPICREVISION_NAME);
     }
 
     public Integer getTranslationPercentage() {
         return translationPercentage;
     }
 
     public void setTranslationPercentage(Integer translationPercentage) {
         this.translationPercentage = translationPercentage;
     }
 
     public void explicitSetTranslationPercentage(Integer translationPercentage) {
         this.translationPercentage = translationPercentage;
         setParameterToConfigured(TRANSLATIONPERCENTAGE_NAME);
     }
 
     public RESTTranslatedTopicStringCollectionV1 getTranslatedTopicStrings_OTM() {
         return translatedTopicStrings;
     }
 
     public void setTranslatedTopicStrings_OTM(final RESTTranslatedTopicStringCollectionV1 translatedTopicStrings) {
         this.translatedTopicStrings = translatedTopicStrings;
     }
 
     public void explicitSetTranslatedTopicString_OTM(final RESTTranslatedTopicStringCollectionV1 translatedTopicStrings) {
         this.translatedTopicStrings = translatedTopicStrings;
         setParameterToConfigured(TRANSLATEDTOPICSTRING_NAME);
     }
 
     public void explicitSetLocale(final String locale) {
         setLocale(locale);
         setParameterToConfigured(LOCALE_NAME);
     }
 
     public RESTTranslatedTopicCollectionV1 getOutgoingTranslatedRelationships() {
         return outgoingTranslatedRelationships;
     }
 
     public void setOutgoingTranslatedRelationships(final RESTTranslatedTopicCollectionV1 outgoingTranslatedRelationships) {
         this.outgoingTranslatedRelationships = outgoingTranslatedRelationships;
     }
 
     public RESTTranslatedTopicCollectionV1 getIncomingTranslatedRelationships() {
         return incomingTranslatedRelationships;
     }
 
     public void setIncomingTranslatedRelationships(final RESTTranslatedTopicCollectionV1 incomingTranslatedRelationships) {
         this.incomingTranslatedRelationships = incomingTranslatedRelationships;
     }
 
     @Override
     public RESTTranslatedTopicCollectionV1 getOutgoingRelationships() {
         return outgoingRelationships;
     }
 
     @Override
     public void setOutgoingRelationships(final RESTTranslatedTopicCollectionV1 outgoingRelationships) {
         this.outgoingRelationships = outgoingRelationships;
     }
 
     @Override
     public RESTTranslatedTopicCollectionV1 getIncomingRelationships() {
         return incomingRelationships;
     }
 
     @Override
     public void setIncomingRelationships(final RESTTranslatedTopicCollectionV1 incomingRelationships) {
         this.incomingRelationships = incomingRelationships;
     }
 
     public Boolean getContainsFuzzyTranslation() {
         return containsFuzzyTranslation;
     }
 
     public void setContainsFuzzyTranslation(final boolean containsFuzzyTranslation) {
         this.containsFuzzyTranslation = containsFuzzyTranslation;
     }
 
     public RESTTranslatedCSNodeV1 getTranslatedCSNode() {
         return translatedCSNode;
     }
 
     public void setTranslatedCSNode(final RESTTranslatedCSNodeV1 translatedCSNode) {
         this.translatedCSNode = translatedCSNode;
     }
 
     public void explicitSetTranslatedCSNode(final RESTTranslatedCSNodeV1 translatedCSNode) {
         this.translatedCSNode = translatedCSNode;
         setParameterToConfigured(TRANSLATED_CSNODE_NAME);
     }
 
     public String getTranslatedXMLCondition() {
         return translatedXMLCondition;
     }
 
     public void setTranslatedXMLCondition(final String translatedXMLCondition) {
         this.translatedXMLCondition = translatedXMLCondition;
     }
 
    public void explicitSetTranslationCondition(final String translationCondition) {
        translatedXMLCondition = translationCondition;
         setParameterToConfigured(TRANSLATED_XML_CONDITION);
     }
 
     @Override
     public boolean equals(final Object other) {
         if (other == null) return false;
         if (this == other) return true;
         if (!(other instanceof RESTTranslatedTopicV1)) return false;
 
         return super.equals(other);
     }
 }
