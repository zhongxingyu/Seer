 package controllers.origo.admin;
 
 import models.origo.core.RootNode;
 import origo.helpers.NavigationHelper;
 import origo.helpers.NodeHelper;
 import origo.helpers.SettingsHelper;
 import origo.helpers.ThemeHelper;
 import play.Logger;
import play.modules.origo.core.CachedThemeVariant;
 import play.modules.origo.core.Node;
import play.modules.origo.core.Themes;
 import play.modules.origo.core.ui.NavigationElement;
 import play.modules.origo.core.ui.RenderedNode;
 
 import java.util.Collection;
 
 public class AdminLoader {
 
     public static RenderedNode getStartPage() {
         return loadAndDecorateStartPage();
     }
 
     public static RenderedNode getPage(String withType) {
         return loadAndDecoratePage(withType);
     }
 
     public static RenderedNode getPage(String withType, String identifier) {
         return loadAndDecoratePage(withType, identifier);
     }
 
     private static RenderedNode loadAndDecorateStartPage() {
         return loadAndDecoratePage(SettingsHelper.Admin.getDashboardType());
     }
 
     private static RenderedNode loadAndDecoratePage(String withType) {
         Node node = loadNode(withType);
         return decorateNode(node);
     }
 
     private static RenderedNode loadAndDecoratePage(String withType, String identifier) {
         Node node = loadNode(withType, identifier);
         return decorateNode(node);
     }
 
     private static Node loadNode(String withType) {
         Logger.debug("Loading [" + withType + "] as type");
         return loadByType(withType);
     }
 
     private static Node loadNode(String withType, String identifier) {
         Logger.debug("Loading [" + withType + "] as type and identifier [" + identifier + "]");
         return loadByType(withType, identifier);
     }
 
     private static Node loadByType(final String withType) {
         RootNode rootNode = loadRootNode(withType);
         return NodeHelper.load(rootNode);
     }
 
     private static Node loadByType(String withType, String identifier) {
         RootNode rootNode = loadRootNode(withType, identifier);
         return NodeHelper.load(rootNode);
     }
 
     private static RootNode loadRootNode(String withType) {
         RootNode rootNode = new RootNode(0L);
         rootNode.type = withType;
         return rootNode;
     }
 
     private static RootNode loadRootNode(String withType, String identifier) {
         RootNode rootNode = loadRootNode(withType);
         rootNode.nodeId = identifier;
         return rootNode;
     }
 
     private static RenderedNode decorateNode(Node node) {
        CachedThemeVariant themeVariant = Themes.getThemeVariant(SettingsHelper.Admin.getThemeVariant());
        RenderedNode renderedNode = ThemeHelper.decorate(node, themeVariant);
         if (Logger.isDebugEnabled()) {
             Logger.debug("Decorated " + renderedNode);
         }
         return renderedNode;
     }
 
     public static Collection<NavigationElement> getNavigation(String withType) {
         Node node = loadRootNode(withType);
         Collection<NavigationElement> navigationLinks = NavigationHelper.getNavigation(node, NavigationElement.ADMIN);
         if (Logger.isDebugEnabled()) {
             Logger.debug("Navigation loaded " + navigationLinks);
         }
         return navigationLinks;
     }
 
 
 }
