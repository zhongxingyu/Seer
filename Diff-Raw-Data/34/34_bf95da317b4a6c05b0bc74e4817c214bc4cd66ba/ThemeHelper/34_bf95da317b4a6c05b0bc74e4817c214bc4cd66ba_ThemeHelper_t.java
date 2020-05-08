 package origo.helpers;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import play.modules.origo.core.CachedThemeVariant;
 import play.modules.origo.core.Node;
 import play.modules.origo.core.Themes;
 import play.modules.origo.core.ui.RenderedNode;
 import play.modules.origo.core.ui.RenderingContext;
 import play.modules.origo.core.ui.UIElement;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class ThemeHelper {
 
     private static final Logger LOG = Logger.getLogger(ThemeHelper.class);
 
    public static RenderedNode decorate(Node node) {
         CachedThemeVariant themeVariant = loadTheme(node);
        return decorate(node, themeVariant);
    }

    public static RenderedNode decorate(Node node, CachedThemeVariant themeVariant) {
        RenderedNode renderedNode = new RenderedNode(node.getNodeId());
         setupRegions(themeVariant, renderedNode);
         renderedNode.setTitle(node.getTitle());
         renderedNode.setTemplate(ReflectionHelper.getTemplate(themeVariant));
         RenderingContext renderingContext = new RenderingContext(themeVariant, node);
         for (String region : node.getRegions()) {
             for (UIElement uiElement : node.getUIElements(region)) {
                 String decoratedContent = decorate(uiElement, renderingContext);
                 if (UIElement.META.equalsIgnoreCase(uiElement.getType())) {
                     if (Node.HEAD.equalsIgnoreCase(region)) {
                         renderedNode.addMeta(decoratedContent);
                     } else {
                         throw new RuntimeException("META is not allowed outside of head");
                     }
                 } else if (UIElement.LINK.equalsIgnoreCase(uiElement.getType())) {
                     if (Node.HEAD.equalsIgnoreCase(region)) {
                         renderedNode.addLink(decoratedContent);
                     } else {
                         throw new RuntimeException("LINK is not allowed outside of head");
                     }
                 } else if (UIElement.SCRIPT.equalsIgnoreCase(uiElement.getType())) {
                     if (Node.HEAD.equalsIgnoreCase(region)) {
                         renderedNode.addScript(decoratedContent);
                     } else {
                         renderedNode.add(region, decoratedContent);
                     }
                 } else if (UIElement.STYLE.equalsIgnoreCase(uiElement.getType())) {
                     if (Node.HEAD.equalsIgnoreCase(region)) {
                         renderedNode.addStyle(decoratedContent);
                     } else {
                         renderedNode.add(region, decoratedContent);
                     }
                 } else {
                     renderedNode.add(region, decoratedContent);
                 }
             }
         }
         return renderedNode;
     }
 
     /**
      * Sets all the regions in the rendered node so the template can access them without
      * nullpointer even if the page has no ui elements.
      *
      * @param themeVariant the theme variant that holds the regions available
      * @param renderedNode the node about to rendered
      */
     private static void setupRegions(CachedThemeVariant themeVariant, RenderedNode renderedNode) {
         Map<String, String> regions = new HashMap<String, String>();
         for (String region : themeVariant.regions) {
             regions.put(region, "");
         }
         renderedNode.setRegions(regions);
     }
 
     public static String decorate(UIElement uiElement, RenderingContext renderingContext) {
         Map<String, play.modules.origo.core.CachedDecorator> decorators = Themes.getDecoratorsForTheme(renderingContext.getThemeVariant().themeId);
         renderingContext.nestle(uiElement);
         String decoratedOutput = null;
         if (decorators.containsKey(uiElement.getType())) {
             play.modules.origo.core.CachedDecorator decorator = decorators.get(uiElement.getType());
             decoratedOutput = ReflectionHelper.invokeDecorator(decorator, null);
         }
         if (decoratedOutput == null) {
             decoratedOutput = DefaultDecorator.decorate(uiElement, renderingContext);
         }
         renderingContext.unNestle();
         return decoratedOutput;
     }
 
     public static String decorateChildren(UIElement parent, RenderingContext renderingContext) {
         if (!parent.hasChildren()) {
             return null;
         }
         StringBuilder sb = new StringBuilder();
         renderingContext.nestle(parent);
         for (UIElement childElement : parent.getChildren()) {
             sb.append(decorate(childElement, renderingContext));
         }
         renderingContext.unNestle();
         return sb.toString();
     }
 
     private static CachedThemeVariant loadTheme(Node node) {
         CachedThemeVariant themeVariant = Themes.getThemeVariant(node.getThemeVariant());
         if (themeVariant == null) {
             String themeVariantId = SettingsHelper.Core.getThemeVariant();
             if (StringUtils.isEmpty(themeVariantId)) {
                 throw new RuntimeException("No theme set for node and no default theme variant set");
             }
             LOG.debug("Using default theme variant [" + themeVariantId + "]");
            themeVariant = Themes.getThemeVariant(themeVariantId);
         }
         if (themeVariant == null) {
             // TODO: Add some sort of fallback for when a theme is removed
             throw new RuntimeException("No theme selected for " + node.toString());
         }
         return themeVariant;
     }
 
 }
