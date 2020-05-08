 package org.jboss.pressgang.ccms.rest.v1.components;
 
import static com.google.common.base.Preconditions.checkArgument;

 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTTopicV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.RESTTranslatedTopicV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.join.RESTAssignedPropertyTagV1;
 import org.jboss.pressgang.ccms.utils.constants.CommonConstants;
 
 /**
  * This component contains methods that can be applied against topics
  *
  * @author Matthew Casperson
  */
 public class ComponentTopicV1 extends ComponentBaseTopicV1 {
     final RESTTopicV1 source;
 
     public ComponentTopicV1(final RESTTopicV1 source) {
         super(source);
         this.source = source;
     }
 
     @Override
     public String returnPressGangCCMSURL() {
         return returnPressGangCCMSURL(source);
     }
 
     public static String returnPressGangCCMSURL(final RESTTopicV1 source) {
         checkArgument(source != null, "The source parameter can not be null");
 
         final String serverUrl = System.getProperty(CommonConstants.PRESS_GANG_UI_SYSTEM_PROPERTY);
         return (serverUrl.endsWith("/") ? serverUrl : (serverUrl + "/")) + "#SearchResultsAndTopicView;query;topicIds=" + source.getId();
     }
 
     /**
      * @return The value to be saved into the Build ID field of any bugzilla bugs assigned to this topic.
      */
     @Override
     public String returnBugzillaBuildId() {
         return returnBugzillaBuildId(source);
     }
 
     public static String returnBugzillaBuildId(final RESTTopicV1 source) {
         checkArgument(source != null, "The source parameter can not be null");
 
         final SimpleDateFormat formatter = new SimpleDateFormat(CommonConstants.FILTER_DISPLAY_DATE_FORMAT);
        return source.getId() + "-" + source.getRevision() + " " + (source.getLastModified() != null ? formatter.format(
                 source.getLastModified()) : formatter.format(new Date())) + " " + source.getLocale();
     }
 
     @Override
     public String returnInternalURL() {
         return returnInternalURL(source);
     }
 
     public static String returnInternalURL(final RESTTopicV1 source) {
         checkArgument(source != null, "The source parameter can not be null");
 
         return "Topic.seam?topicTopicId=" + source.getId() + "&selectedTab=Rendered+View";
     }
 
     @Override
     public RESTTopicV1 returnRelatedTopicByID(final Integer id) {
         return returnRelatedTopicByID(source, id);
     }
 
     public static RESTTopicV1 returnRelatedTopicByID(final RESTTopicV1 source, final Integer id) {
         checkArgument(source != null, "The source parameter can not be null");
 
         if (source.getOutgoingRelationships() != null && source.getOutgoingRelationships().getItems() != null) {
             final List<RESTTopicV1> relatedTopics = source.getOutgoingRelationships().returnItems();
             for (final RESTTopicV1 topic : relatedTopics) {
                 if (topic.getId().equals(id)) return topic;
             }
         }
         return null;
     }
 
     @Override
     public boolean hasRelationshipTo(final Integer id) {
         return hasRelationshipTo(source, id);
     }
 
     public static boolean hasRelationshipTo(final RESTTopicV1 source, final Integer id) {
         return returnRelatedTopicByID(source, id) != null;
     }
 
     @Override
     public String returnErrorXRefID() {
         return returnErrorXRefID(source);
     }
 
     public static String returnErrorXRefID(final RESTTopicV1 source) {
         return CommonConstants.ERROR_XREF_ID_PREFIX + source.getId();
     }
 
     @Override
     public String returnXRefID() {
         return returnXRefID(source);
     }
 
     public static String returnXRefID(final RESTTopicV1 source) {
         checkArgument(source != null, "The source parameter can not be null");
 
         return "TopicID" + source.getId();
     }
 
 
     @Override
     public String returnXrefPropertyOrId(final Integer propertyTagId) {
         return returnXrefPropertyOrId(source, propertyTagId);
     }
 
     public static String returnXrefPropertyOrId(final RESTTopicV1 source, final Integer propertyTagId) {
         final RESTAssignedPropertyTagV1 propTag = returnProperty(source, propertyTagId);
         if (propTag != null) {
             return propTag.getValue();
         } else {
             return returnXRefID(source);
         }
     }
 
     @Override
     public String returnEditorURL() {
         return returnEditorURL(source);
     }
 
     public static String returnEditorURL(final RESTTopicV1 source) {
         return returnPressGangCCMSURL(source);
     }
 
     public RESTTranslatedTopicV1 returnPushedTranslatedTopic() {
         return returnPushedTranslatedTopic(source);
     }
 
     public static RESTTranslatedTopicV1 returnPushedTranslatedTopic(final RESTTopicV1 source) {
         checkArgument(source != null, "The source parameter can not be null");
 
         /* Check that a translation exists that is the same locale as the base topic */
         RESTTranslatedTopicV1 pushedTranslatedTopic = null;
         if (source.getTranslatedTopics_OTM() != null && source.getTranslatedTopics_OTM().getItems() != null) {
             final Integer topicRev = source.getRevision();
             final List<RESTTranslatedTopicV1> topics = source.getTranslatedTopics_OTM().returnItems();
             for (final RESTTranslatedTopicV1 translatedTopic : topics) {
                 if (translatedTopic.getLocale().equals(source.getLocale()) &&
                         // Ensure that the topic revision is less than or equal to the source revision
                         (topicRev == null || translatedTopic.getTopicRevision() <= topicRev) &&
                         // Check if this translated topic is a higher revision then the current stored translation
                         (pushedTranslatedTopic == null || pushedTranslatedTopic.getTopicRevision() < translatedTopic.getTopicRevision()))
                     pushedTranslatedTopic = translatedTopic;
             }
         }
 
         return pushedTranslatedTopic;
     }
 }
