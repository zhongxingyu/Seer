 package main.origo.core.interceptors;
 
 import com.google.common.collect.Lists;
 import main.origo.core.Node;
 import main.origo.core.annotations.Interceptor;
 import main.origo.core.annotations.Provides;
 import main.origo.core.annotations.Types;
 import main.origo.core.helpers.NavigationHelper;
 import main.origo.core.helpers.ProvidesHelper;
 import main.origo.core.ui.NavigationElement;
 import models.origo.core.Alias;
 import models.origo.core.RootNode;
 import models.origo.core.navigation.AliasNavigation;
 import models.origo.core.navigation.BasicNavigation;
 import models.origo.core.navigation.ExternalLinkNavigation;
 import models.origo.core.navigation.PageIdNavigation;
 
 import java.util.Date;
 import java.util.List;
 
 /**
  * Standard implementation of navigation. An alternate navigation provider can be used by changing the the navigation
  * type in the settings. This provider uses a standard tree based navigation stored in a database with parent->child
  * relationships. It provides the type BasicNavigation.
  *
  * @see models.origo.core.navigation.BasicNavigation
  * @see models.origo.core.navigation.AliasNavigation
  * @see models.origo.core.navigation.PageIdNavigation
  * @see models.origo.core.navigation.ExternalLinkNavigation
  */
 @Interceptor
 public class DefaultNavigationProvider {
 
     @Provides(type = Types.NAVIGATION, with = "models.origo.core.navigation.BasicNavigation")
     public static List<NavigationElement> createNavigation(Provides.Context context) {
         List<NavigationElement> navigationElements = Lists.newArrayList();
         String section = (String) context.args.get("section");
         NavigationHelper.triggerBeforeNavigationLoaded(BasicNavigation.class.getName(), context.node, section);
         List<BasicNavigation> navigationModels = BasicNavigation.findWithSection(section);
         for (BasicNavigation navigationModel : navigationModels) {
             NavigationHelper.triggerBeforeNavigationItemLoaded(navigationModel.type, context.node, navigationModel);
             NavigationElement navigationElement = NavigationHelper.triggerProvidesNavigationItemInterceptor(navigationModel.type, context.node, navigationModel);
             NavigationHelper.triggerAfterNavigationItemLoaded(navigationModel.type, context.node, navigationModel, navigationElement);
             List<NavigationElement> children = createNavigationChildren(context.node, section, navigationModel, navigationElement);
             navigationElement.children.addAll(children);
             navigationElements.add(navigationElement);
         }
         NavigationHelper.triggerAfterNavigationLoaded(BasicNavigation.class.getName(), context.node, navigationElements, section);
         return navigationElements;
     }
 
     public static List<NavigationElement> createNavigationChildren(Node node, String section, BasicNavigation navigationModel, NavigationElement parentNavigationElement) {
         List<NavigationElement> navigationElements = Lists.newArrayList();
         List<BasicNavigation> navigationModels = BasicNavigation.findWithSection(section, navigationModel);
         for (BasicNavigation childNavigation : navigationModels) {
             NavigationHelper.triggerBeforeNavigationItemLoaded(childNavigation.type, node, childNavigation);
             NavigationElement childNavigationElement = NavigationHelper.triggerProvidesNavigationItemInterceptor(childNavigation.type, node, childNavigation, parentNavigationElement);
             if (childNavigationElement != null) {
                 NavigationHelper.triggerAfterNavigationItemLoaded(childNavigation.type, node, childNavigation, childNavigationElement);
                 if (childNavigationElement.selected) {
                     parentNavigationElement.selected = true;
                 }
                 navigationElements.add(childNavigationElement);
             }
         }
         return navigationElements;
     }
 
     @Provides(type = Types.NAVIGATION_ITEM, with = "models.origo.core.navigation.AliasNavigation")
     public static NavigationElement createAliasNavigation(Provides.Context context) {
         AliasNavigation navigationModel = AliasNavigation.findWithIdentifier(context.navigation.getReferenceId());
         Alias alias = Alias.findWithPath(navigationModel.alias);
         if (alias != null) {
             RootNode referencedRootNode = RootNode.findLatestPublishedVersionWithNodeId(alias.pageId, new Date());
             if (referencedRootNode != null) {
                 Node referencedNode = ProvidesHelper.triggerInterceptor(Types.NODE, referencedRootNode.nodeType, referencedRootNode);
                boolean selected = context.node.getNodeId().equals(alias.pageId);
                 return new NavigationElement(context.navigation.getSection(), referencedNode.getTitle(), navigationModel.getLink(), selected);
             } else {
                 throw new RuntimeException("Page not found [" + alias.pageId + "]");
             }
         } else {
             throw new RuntimeException("Alias not found [" + navigationModel.alias + "]");
         }
     }
 
     @Provides(type = Types.NAVIGATION_ITEM, with = "models.origo.core.navigation.PageIdNavigation")
     public static NavigationElement createPageIdNavigation(Provides.Context context) {
         PageIdNavigation navigationModel = PageIdNavigation.findWithIdentifier(context.navigation.getReferenceId());
         RootNode referencedRootNode = RootNode.findLatestPublishedVersionWithNodeId(navigationModel.pageId, new Date());
         if (referencedRootNode != null) {
             Node referencedNode = ProvidesHelper.triggerInterceptor(Types.NODE, referencedRootNode.nodeType, referencedRootNode);
            boolean selected = context.node.getNodeId().equals(referencedRootNode.getNodeId());
             return new NavigationElement(context.navigation.getSection(), referencedNode.getTitle(), navigationModel.getLink(), selected);
         } else {
             throw new RuntimeException("Page not found [" + navigationModel.pageId + "]");
         }
     }
 
     @Provides(type = Types.NAVIGATION_ITEM, with = "models.origo.core.navigation.ExternalLinkNavigation")
     public static NavigationElement createExternalLinkNavigation(Provides.Context context) {
         ExternalLinkNavigation navigationModel = ExternalLinkNavigation.findWithIdentifier(context.navigation.getReferenceId());
         if (navigationModel != null) {
             return new NavigationElement(context.navigation.getSection(), navigationModel.title, navigationModel.getLink());
         }
         return null;
     }
 
 }
