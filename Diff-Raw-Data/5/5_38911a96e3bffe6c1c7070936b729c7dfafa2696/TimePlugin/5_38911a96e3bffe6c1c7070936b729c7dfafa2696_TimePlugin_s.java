 package de.deepamehta.plugins.time;
 
 import de.deepamehta.core.model.DataField;
 import de.deepamehta.core.model.Topic;
 import de.deepamehta.core.model.TopicType;
 import de.deepamehta.core.service.Plugin;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 
 
 public class TimePlugin extends Plugin {
 
     // -------------------------------------------------------------------------------------------------- Static Methods
 
     public static DataField createDateCreatedField() {
         DataField dateCreatedField = new DataField("Date Created", "number");
         dateCreatedField.setUri("de/deepamehta/core/property/DateCreated");
        // dateCreatedField.setReadOnly(true);
         dateCreatedField.setIndexingMode("FULLTEXT_KEY");
         dateCreatedField.setRendererClass("TimestampFieldRenderer");
         return dateCreatedField;
     }
 
     public static DataField createDateModifiedField() {
         DataField dateModifiedField = new DataField("Date Modified", "number");
         dateModifiedField.setUri("de/deepamehta/core/property/DateModified");
        // dateModifiedField.setReadOnly(true);
         dateModifiedField.setIndexingMode("FULLTEXT_KEY");
         dateModifiedField.setRendererClass("TimestampFieldRenderer");
         return dateModifiedField;
     }
 
     // ---------------------------------------------------------------------------------------------- Instance Variables
 
     private Logger logger = Logger.getLogger(getClass().getName());
 
 
 
     // ************************
     // *** Overriding Hooks ***
     // ************************
 
 
 
     @Override
     public void preCreateHook(Topic topic, Map<String, String> clientContext) {
         // 1) extend type definition
         if (topic.typeUri.equals("de/deepamehta/core/topictype/TopicType")) {
             // Add "Date Created" and "Date Modified" data fields to the topic type being created.
             //
             // Note: Topic types created before the time plugin was activated get these fields through the initial
             // migration. See de.deepamehta.plugins.time.migrations.Migration1
             //
             // TODO: Avoid this code doubling by providing a "update type definition" facility.
             //
             ((TopicType) topic).addDataField(createDateCreatedField());
             ((TopicType) topic).addDataField(createDateModifiedField());
         }
         //
         // 2) set timestamp
         logger.info("Setting timestamp of " + topic);
         long time = System.currentTimeMillis();
         TopicType type = dms.getTopicType(topic.typeUri);
         // Note: the timestamp data fields might be (interactively) removed meanwhile
         if (type.hasDataField("de/deepamehta/core/property/DateCreated")) {
             topic.setProperty("de/deepamehta/core/property/DateCreated", time);
         }
         if (type.hasDataField("de/deepamehta/core/property/DateModified")) {
             topic.setProperty("de/deepamehta/core/property/DateModified", time);
         }
     }
 
     @Override
     public void preUpdateHook(Topic topic, Map<String, Object> newProperties) {
         long time = System.currentTimeMillis();
         topic.setProperty("de/deepamehta/core/property/DateModified", time);
     }
 
     // ---
 
     @Override
     public void providePropertiesHook(Topic topic) {
         topic.setProperty("de/deepamehta/core/property/DateModified",
             dms.getTopicProperty(topic.id, "de/deepamehta/core/property/DateModified"));
     }
 }
