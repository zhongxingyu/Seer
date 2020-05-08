 package origo.listeners;
 
 import models.origo.admin.AdminPage;
 import models.origo.core.BasicPage;
 import models.origo.core.Content;
 import models.origo.core.RootNode;
 import origo.helpers.AdminHelper;
 import origo.helpers.FormHelper;
 import play.Logger;
 import play.modules.origo.admin.annotations.Admin;
 import play.modules.origo.core.Node;
 import play.modules.origo.core.annotations.OnLoad;
 import play.modules.origo.core.annotations.OnSubmit;
 import play.modules.origo.core.annotations.Provides;
 import play.modules.origo.core.annotations.SubmitState;
 import play.modules.origo.core.ui.UIElement;
 import play.mvc.Scope;
 import play.mvc.results.Redirect;
 
 import java.util.List;
 
 public class BasicPageAdminProvider {
 
     private static final String BASE_TYPE = "origo.admin.basicpage";
     private static final String LIST_TYPE = BASE_TYPE + ".list";
     private static final String EDIT_TYPE = BASE_TYPE + ".edit";
     private static final String DASHBOARD_TYPE = BASE_TYPE + ".dashboard";
 
     private static final String TITLE_PARAM = "origo-basicpageform-title";
     private static final String LEAD_PARAM = "origo-basicpageform-lead";
     private static final String BODY_PARAM = "origo-basicpageform-body";
 
     @Provides(type = Admin.DASHBOARD, with = DASHBOARD_TYPE)
     public static UIElement createDashboardItem() {
 
         String url = AdminHelper.getURLForAdminAction(BASE_TYPE + ".list");
 
         return new UIElement(Admin.DASHBOARD).addAttribute("class", "dashboard").
                 addChild(new UIElement(UIElement.PANEL, 10).
                         addChild(new UIElement(UIElement.PARAGRAPH, 10, "Basic Page").addAttribute("class", "dashboard item title")).
                         addChild(new UIElement(UIElement.PARAGRAPH, 20, "Basic pages have a lead and a body").addAttribute("class", "dashboard item description")).
                         addChild(new UIElement(UIElement.ANCHOR, 30, "List All").addAttribute("href", url).addAttribute("class", "dashboard item link"))
                 );
     }
 
     @Provides(type = Provides.NODE, with = LIST_TYPE)
     public static Node createListPage(RootNode rootNode) {
         AdminPage page = new AdminPage(rootNode.nodeId);
         page.setTitle("List Basic Pages");
         page.rootNode = rootNode;
         return page;
     }
 
     @OnLoad(type = Provides.NODE, with = LIST_TYPE)
     public static void createListPage(Node node) {
         List<BasicPage> allPages = BasicPage.findAll();
         for (BasicPage page : allPages) {
             Logger.info("Page \'" + page.title + "\' [" + page.nodeId + "/" + page.version + "]");
         }
         List<BasicPage> basicPages = BasicPage.findAllLatestVersions();
 
         UIElement panelElement = new UIElement(UIElement.PANEL, 10).addAttribute("class", "panel pages");
         for (BasicPage page : basicPages) {
             String editURL = AdminHelper.getURLForAdminAction(EDIT_TYPE, page.getNodeId());
             UIElement panel = new UIElement(UIElement.PANEL).
                     addChild(new UIElement(UIElement.ANCHOR, 10, page.getTitle()).addAttribute("href", editURL)).
                     addChild(new UIElement(UIElement.TEXT, 20, " (" + page.nodeId + " / " + page.getVersion() + ")"));
             panelElement.addChild(panel);
         }
         node.addUIElement(panelElement);
     }
 
     @Provides(type = Provides.NODE, with = EDIT_TYPE)
     public static Node createEditPage(RootNode rootNode) {
         AdminPage page = new AdminPage(rootNode.nodeId);
         page.setTitle("Edit Basic Page");
         if (rootNode.version == null || rootNode.version == 0) {
             page.rootNode = RootNode.findLatestVersionWithNodeId(rootNode.nodeId).copy();
         } else {
             page.rootNode = rootNode;
         }
         return page;
     }
 
     @OnLoad(type = Provides.NODE, with = EDIT_TYPE)
     public static void createEditPage(Node node) {
         BasicPage basicPage = BasicPage.findLatestVersion(node.getNodeId());
         if (basicPage == null) {
             node.addUIElement(new UIElement(UIElement.PARAGRAPH, 10, "Page '" + node.getNodeId() + "' does not exist."));
             return;
         }
 
         Content leadContent = Content.findWithIdentifier(basicPage.leadReferenceId);
         Content bodyContent = Content.findWithIdentifier(basicPage.bodyReferenceId);
 
         UIElement formElement = FormHelper.createFormElement(DefaultFormProvider.TYPE, node, BASE_TYPE).setId("basicpageform").addAttribute("class", "origo-basicpageform, form");
 
         UIElement titleElement = new UIElement(UIElement.PANEL, 10).addAttribute("class", "field");
         titleElement.addChild(new UIElement(UIElement.LABEL, 10, "Title").addAttribute("for", TITLE_PARAM));
         titleElement.addChild(new UIElement(UIElement.INPUT_TEXT, 20).addAttribute("name", TITLE_PARAM).addAttribute("value", basicPage.getTitle()));
         formElement.addChild(titleElement);
 
         UIElement leadElement = new UIElement(UIElement.PANEL, 20).addAttribute("class", "field");
         leadElement.addChild(new UIElement(UIElement.LABEL, 10, "Lead").addAttribute("for", LEAD_PARAM));
         leadElement.addChild(AdminHelper.createRichTextEditor(node, leadContent).setWeight(20).addAttribute("class", "editor richtext").
                 addAttribute("name", LEAD_PARAM).addAttribute("cols", "80").addAttribute("rows", "10"));
         formElement.addChild(leadElement);
 
         UIElement bodyElement = new UIElement(UIElement.PANEL, 30).addAttribute("class", "field");
         bodyElement.addChild(new UIElement(UIElement.LABEL, 10, "Body").addAttribute("for", BODY_PARAM));
         bodyElement.addChild(AdminHelper.createRichTextEditor(node, bodyContent).setWeight(20).addAttribute("class", "editor richtext").
                 addAttribute("name", BODY_PARAM).addAttribute("cols", "80").addAttribute("rows", "20"));
         formElement.addChild(bodyElement);
 
         UIElement actionPanel = new UIElement(UIElement.PANEL, 40).addAttribute("class", "field");
         actionPanel.addChild(new UIElement(UIElement.INPUT_BUTTON, 10, "Save").addAttribute("type", "submit"));
         formElement.addChild(actionPanel);
 
         node.addUIElement(formElement);
     }
 
     @OnSubmit(with = BASE_TYPE)
     public static void storePage(Scope.Params params) {
 
         String nodeId = DefaultFormProvider.getNodeId(params);
         Long version = DefaultFormProvider.getNodeVersion(params);
         RootNode oldRootNode = RootNode.findWithNodeIdAndSpecificVersion(nodeId, version);
         if (oldRootNode == null) {
             throw new RuntimeException("Root node with id=\'" + nodeId + "\' does not exist");
         }
 
         BasicPage oldPageVersion = BasicPage.findLatestVersion(nodeId);
         if (!oldPageVersion.version.equals(version)) {
             // TODO: Maybe add this as validation instead of storing over it
         }
 
         BasicPage newPageVersion = oldPageVersion.copy();
         newPageVersion.version = newPageVersion.version++;
         newPageVersion.nodeId = nodeId;
 
         boolean changed = false;
         Content leadContent = Content.findWithIdentifier(oldPageVersion.leadReferenceId);
         if (!leadContent.value.equals(params.get(LEAD_PARAM))) {
             Content newLeadContent = new Content();
             newLeadContent.value = params.get(LEAD_PARAM);
             newPageVersion.leadReferenceId = newLeadContent.identifier;
             newLeadContent.save();
             changed = true;
         }
         Content bodyContent = Content.findWithIdentifier(oldPageVersion.bodyReferenceId);
         if (!bodyContent.value.equals(params.get(BODY_PARAM))) {
             Content newBodyContent = new Content();
             newBodyContent.value = params.get(BODY_PARAM);
             newPageVersion.bodyReferenceId = newBodyContent.identifier;
             newBodyContent.save();
             changed = true;
         }
 
         if (!oldPageVersion.title.equals(params.get(TITLE_PARAM))) {
             newPageVersion.title = params.get(TITLE_PARAM);
             changed = true;
         }
         if (changed) {
             RootNode rootNode = oldRootNode.copy();
             rootNode.version++;
             rootNode.save();
             newPageVersion.version = rootNode.version;
             newPageVersion.save();
         }
 
     }
 
     @SubmitState(with = BASE_TYPE)
     public static void handleSuccess(Scope.Params params) {
         String endpointURL = AdminHelper.getURLForAdminAction(LIST_TYPE);
         throw new Redirect(endpointURL);
     }
 }
