 package de.deepamehta.plugins.eduzen.migrations;
 
 import java.util.logging.Logger;
 import de.deepamehta.core.service.Migration;
 import de.deepamehta.core.AssociationType;
 import de.deepamehta.core.Topic;
 import de.deepamehta.core.TopicType;
 import de.deepamehta.core.model.*;
 
 public class Migration2 extends Migration {
 
     private Logger logger = Logger.getLogger(getClass().getName());
 
     private String RESOURCE_URI = "dm4.resources.resource";
     private String RESOURCE_CONTENT_URI = "dm4.resources.content";
     private String TAG_URI = "dm4.tags.tag";
     private String RATING_URI = "dm4.ratings.score";
     // private String FILE_URI = "dm4.files.file";
     // private String WEB_RESOURCE_URI = "dm4.webbrowser.web_resource";
     private String WS_DEFAULT_URI = "de.workspaces.deepamehta";
 
     @Override
     public void run() {
 
         TopicType resource = dms.getTopicType(RESOURCE_URI, null);
         // 1) Enrich the "Resource"-Type about many "Tags", one "Score", one "File" and one "Web Resource"
         resource.addAssocDef(new AssociationDefinitionModel("dm4.core.aggregation_def",
             RESOURCE_URI, TAG_URI, "dm4.core.one", "dm4.core.many"));
         resource.addAssocDef(new AssociationDefinitionModel("dm4.core.composition_def",
             RESOURCE_URI, RATING_URI, "dm4.core.one", "dm4.core.one"));
         dms.getTopicType(RESOURCE_CONTENT_URI, null).getViewConfig().addSetting("dm4.webclient.view_config",
                 "dm4.webclient.simple_renderer_uri", "tub.eduzen.mathjax_field_renderer");
         // probably we want to do this later.. but we'll see
         // resource.addAssocDef(new AssociationDefinitionModel("dm4.core.aggregation_def",
             // RESOURCE_URI, WEB_RESOURCE_URI, "dm4.core.one", "dm4.core.one"));
         // resource.addAssocDef(new AssociationDefinitionModel("dm4.core.aggregation_def",
             // RESOURCE_URI, FILE_URI, "dm4.core.one", "dm4.core.one"));
         // hide "Web Resources" from "Create"-Menu, thus forcing usage of our new "Resource"-Topic
        // dms.getTopicType(WEB_RESOURCE_URI, null).getViewConfig()
           // .addSetting("dm4.webclient.view_config", "dm4.webclient.add_to_create_menu", false);
         assignWorkspace(resource);
         assignWorkspace(dms.getTopicType(TAG_URI, null));
 
     }
 
     // === Workspace ===
 
     private void assignWorkspace(Topic topic) {
         if (hasWorkspace(topic)) {
             return;
         }
         Topic defaultWorkspace = dms.getTopic("uri", new SimpleValue(WS_DEFAULT_URI), false, null);
         dms.createAssociation(new AssociationModel("dm4.core.aggregation",
             new TopicRoleModel(topic.getId(), "dm4.core.parent"),
             new TopicRoleModel(defaultWorkspace.getId(), "dm4.core.child")
         ), null);
     }
 
     private boolean hasWorkspace(Topic topic) {
         return topic.getRelatedTopics("dm4.core.aggregation", "dm4.core.parent", "dm4.core.child",
             "dm4.workspaces.workspace", false, false, 0, null).getSize() > 0;
     }
 }
