 package org.jboss.pressgang.ccms.rest.v1.entities.contentspec;
 
 import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.RESTTranslatedCSNodeCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.RESTTranslatedCSNodeStringCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.contentspec.items.RESTTranslatedCSNodeCollectionItemV1;
import org.jboss.pressgang.ccms.rest.v1.entities.RESTTranslatedTopicV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.base.RESTBasePrimaryEntityV1;
 
 public class RESTTranslatedCSNodeV1 extends RESTBasePrimaryEntityV1<RESTTranslatedCSNodeV1, RESTTranslatedCSNodeCollectionV1, RESTTranslatedCSNodeCollectionItemV1> {
     public static final String NODE_ID_NAME = "nodeId";
     public static final String NODE_REVISION_NAME = "nodeRevision";
     public static final String NODE_NAME = "node";
     public static final String TRANSLATED_STRING_NAME = "translatedNodeStrings_OTM";
     public static final String TRANSLATED_CONTENT_SPEC_NAME = "translatedContentSpec";
     public static final String ORIGINALSTRING_NAME = "originalString";
    public static final String TRANSLATED_TOPIC_NAME = "translatedTopic";
 
     private Integer nodeId = null;
     private Integer nodeRevision = null;
     private String originalString = null;
     private RESTCSNodeV1 node = null;
     private RESTTranslatedContentSpecV1 translatedContentSpec = null;
     private RESTTranslatedCSNodeCollectionV1 children_OTM = null;
     private RESTTranslatedCSNodeStringCollectionV1 translatedNodeStrings_OTM = null;
    private RESTTranslatedTopicV1 translatedTopic = null;
     /**
      * A list of the Envers revision numbers
      */
     private RESTTranslatedCSNodeCollectionV1 revisions = null;
 
     @Override
     public RESTTranslatedCSNodeCollectionV1 getRevisions() {
         return revisions;
     }
 
     @Override
     public void setRevisions(final RESTTranslatedCSNodeCollectionV1 revisions) {
         this.revisions = revisions;
     }
 
     @Override
     public RESTTranslatedCSNodeV1 clone(final boolean deepCopy) {
         final RESTTranslatedCSNodeV1 retValue = new RESTTranslatedCSNodeV1();
 
         cloneInto(retValue, deepCopy);
 
         return retValue;
     }
 
     public void cloneInto(final RESTTranslatedCSNodeV1 clone, final boolean deepCopy) {
         super.cloneInto(clone, deepCopy);
 
         clone.nodeId = nodeId;
         clone.nodeRevision = nodeRevision;
         clone.originalString = originalString;
 
         if (deepCopy) {
             if (children_OTM != null) {
                 clone.children_OTM = new RESTTranslatedCSNodeCollectionV1();
                 children_OTM.cloneInto(clone.children_OTM, deepCopy);
             }
 
             if (translatedNodeStrings_OTM != null) {
                 clone.translatedNodeStrings_OTM = new RESTTranslatedCSNodeStringCollectionV1();
                 translatedNodeStrings_OTM.cloneInto(clone.translatedNodeStrings_OTM, deepCopy);
             }
 
             if (node != null) {
                 clone.node = node.clone(deepCopy);
             }
 
             if (translatedContentSpec != null) {
                 clone.translatedContentSpec = translatedContentSpec.clone(deepCopy);
             }
 
            if (translatedTopic != null) {
                clone.translatedTopic = translatedTopic.clone(deepCopy);
             }
 
             if (revisions != null) {
                 clone.revisions = new RESTTranslatedCSNodeCollectionV1();
                 revisions.cloneInto(clone.revisions, deepCopy);
             }
         } else {
             clone.children_OTM = children_OTM;
             clone.translatedNodeStrings_OTM = translatedNodeStrings_OTM;
             clone.translatedContentSpec = translatedContentSpec;
             clone.node = node;
            clone.translatedTopic = translatedTopic;
             clone.revisions = revisions;
         }
     }
 
     public RESTTranslatedCSNodeStringCollectionV1 getTranslatedNodeStrings_OTM() {
         return translatedNodeStrings_OTM;
     }
 
     public void setTranslatedNodeStrings_OTM(final RESTTranslatedCSNodeStringCollectionV1 translatedNodeStrings) {
         translatedNodeStrings_OTM = translatedNodeStrings;
     }
 
     public void explicitSetTranslatedNodeString_OTM(final RESTTranslatedCSNodeStringCollectionV1 translatedNodeStrings) {
         translatedNodeStrings_OTM = translatedNodeStrings;
         setParameterToConfigured(TRANSLATED_STRING_NAME);
     }
 
     public RESTTranslatedCSNodeCollectionV1 getChildren_OTM() {
         return children_OTM;
     }
 
     public void setChildren_OTM(final RESTTranslatedCSNodeCollectionV1 children_OTM) {
         this.children_OTM = children_OTM;
     }
 
     public Integer getNodeId() {
         return nodeId;
     }
 
     public void setNodeId(final Integer nodeId) {
         this.nodeId = nodeId;
     }
 
     public void explicitSetNodeId(final Integer nodeId) {
         this.nodeId = nodeId;
         setParameterToConfigured(NODE_ID_NAME);
     }
 
     public Integer getNodeRevision() {
         return nodeRevision;
     }
 
     public void setNodeRevision(final Integer nodeRevision) {
         this.nodeRevision = nodeRevision;
     }
 
     public void explicitSetNodeRevision(final Integer nodeRevision) {
         this.nodeRevision = nodeRevision;
         setParameterToConfigured(NODE_REVISION_NAME);
     }
 
     public String getOriginalString() {
         return originalString;
     }
 
     public void setOriginalString(final String originalString) {
         this.originalString = originalString;
     }
 
     public void explicitSetOriginalString(final String originalString) {
         this.originalString = originalString;
         setParameterToConfigured(ORIGINALSTRING_NAME);
     }
 
     public RESTCSNodeV1 getNode() {
         return node;
     }
 
     public void setNode(final RESTCSNodeV1 node) {
         this.node = node;
     }
 
     public RESTTranslatedContentSpecV1 getTranslatedContentSpec() {
         return translatedContentSpec;
     }
 
     public void setTranslatedContentSpec(final RESTTranslatedContentSpecV1 translatedContentSpec) {
         this.translatedContentSpec = translatedContentSpec;
     }
 
     public void explicitSetTranslatedContentSpec(final RESTTranslatedContentSpecV1 translatedContentSpec) {
         this.translatedContentSpec = translatedContentSpec;
         setParameterToConfigured(TRANSLATED_CONTENT_SPEC_NAME);
     }
 
    public RESTTranslatedTopicV1 getTranslatedTopic() {
        return translatedTopic;
     }
 
    public void setTranslatedTopic(final RESTTranslatedTopicV1 translatedTopic) {
        this.translatedTopic = translatedTopic;
     }
 
    public void explicitSetTranslatedTopic(final RESTTranslatedTopicV1 translatedTopic) {
        this.translatedTopic = translatedTopic;
        setParameterToConfigured(TRANSLATED_TOPIC_NAME);
     }
 
     @Override
     public boolean equals(final Object other) {
         if (other == null) return false;
         if (this == other) return true;
         if (!(other instanceof RESTTranslatedCSNodeV1)) return false;
 
         return super.equals(other);
     }
 
 }
