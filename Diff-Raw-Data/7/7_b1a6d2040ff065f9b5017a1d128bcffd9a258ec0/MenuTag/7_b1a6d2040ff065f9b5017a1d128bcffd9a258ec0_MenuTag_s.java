 package org.synyx.minos.core.web.tags;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.jsp.JspWriter;
 
 import org.springframework.web.context.WebApplicationContext;
 import org.springframework.web.servlet.tags.RequestContextAwareTag;
 import org.synyx.minos.core.authentication.AuthenticationService;
 import org.synyx.minos.core.domain.User;
 import org.synyx.minos.core.web.UrlUtils;
 import org.synyx.minos.core.web.menu.MenuItem;
 import org.synyx.minos.core.web.menu.MenuItemProvider;
 
 
 /**
  * Tag rendering the applications menu from all found {@link MenuItemProvider}s.
  * 
  * @author Oliver Gierke - gierke@synyx.de
  */
 public class MenuTag extends RequestContextAwareTag {
 
     private static final long serialVersionUID = 3562560895559874960L;
 
     private AuthenticationService authenticationService;
 
     private Integer levels = 0;
 
     private boolean alwaysRenderSubmenus = false;
 
     private String id = null;
 
 
     /*
      * (non-Javadoc)
      * 
      * @seeorg.springframework.web.servlet.tags.RequestContextAwareTag#
      * doStartTagInternal()
      */
     @Override
     protected int doStartTagInternal() throws Exception {
 
         initAuthenticationService();
 
         List<MenuItem> menuItems = getSortedMenuItems();
 
         if (null == menuItems || menuItems.isEmpty()) {
             return 0;
         }
 
         StringBuilder builder = new StringBuilder();
         buildHtmlMenu(menuItems, builder, false, levels);
 
         if (0 != builder.length()) {
 
             JspWriter out = pageContext.getOut();
 
             out.append("<div id='");
 
             // unless id provided default to "menu" as id
             if (id != null) {
                 out.append(id);
             } else {
                 out.append("menu");
             }
 
             out.append("'><ul class=\"menu\">");
             out.append(builder.toString());
             out.append("</ul></div>");
         }
 
         return 0;
     }
 
 
     /**
      * Returns all {@link MenuItem}s given by the configured
      * {@link MenuItemProvider}s sorted by their position.
      * 
      * @return
      */
     public List<MenuItem> getSortedMenuItems() {
 
         Collection<MenuItemProvider> beans =
                 getApplicationContext().getBeansOfType(MenuItemProvider.class)
                         .values();
 
         List<MenuItem> sortedMenuItems = new ArrayList<MenuItem>();
 
         for (MenuItemProvider provider : beans) {
 
             for (MenuItem menuItem : provider.getMenuItems()) {
 
                 sortedMenuItems.add(menuItem);
             }
         }
 
         Collections.sort(sortedMenuItems);
 
         return sortedMenuItems;
     }
 
 
     /**
      * Builds an HTML menu from the given {@link MenuItem}s. Traverses the items
      * down to the leafs, if they contain submenues.
      * 
      * @param menuItems
      * @param builder
      * @throws IOException
      */
     private void buildHtmlMenu(List<MenuItem> menuItems, StringBuilder builder,
             boolean submenu, Integer levelsRemaining) throws IOException {
 
         for (MenuItem item : menuItems) {
 
             if (!showMenuItem(item)) {
                 continue;
             }
 
             String id = item.getTitle().replace('.', '_');
             User user =
                     null == authenticationService ? null
                             : authenticationService.getCurrentUser();
 
             if (submenu) {
 
                 boolean isActive =
                         getPathWithinApplication(getRequest()).equals(
                                 item.getUrl(user));
 
                 String aClass = isActive ? " class='active'" : "";
 
                 builder.append(String.format(
                         "<li%s><a id='%s' href='%s' title='%s'%s>%s</a>", "",
                         id, UrlUtils.toUrl(item.getUrl(user), getRequest()),
                         resolveMessage(item.getDesciption()), aClass,
                         resolveMessage(item.getTitle())));
 
             } else {
 
                 boolean isActive =
                         getPathWithinApplication(getRequest()).startsWith(
                                 item.getUrl(null));
 
                 String aClass = isActive ? " class='active'" : "";
 
                 builder.append(String.format(
                         "<li%s><a id='%s' href='%s' title='%s'%s>%s</a>",
                         aClass, id, UrlUtils.toUrl(item.getUrl(user),
                                 getRequest()), resolveMessage(item
                                 .getDesciption()), aClass, resolveMessage(item
                                 .getTitle())));
 
                 if ((isActive || alwaysRenderSubmenus)
                         && (isLevelRestrictionActive() || levelsRemaining > 1)) {
 
                     if (item.hasSubMenues()) {
 
                         builder.append("<ul class='submenu'>");
                         buildHtmlMenu(item.getSubMenues(), builder, true,
                                 levelsRemaining - 1);
                         builder.append("</ul>");
 
                     }
                 }
             }
 
             builder.append("</li>");
         }
     }
 
 
     /**
      * If the attribute levels is set, the tag renders submenues until the given
      * level. This method checks if the level-feature is deactivated : if
      * <minos:menu levels="0"> is used, the number of levels rendered are
      * unlimited. This is also true for negative values of levels.
      * 
      * @return
      */
     private boolean isLevelRestrictionActive() {
 
         return levels <= 0;
     }
 
 
     private String getPathWithinApplication(HttpServletRequest request) {
 
         String uri = request.getRequestURI();
         String contextPath = request.getContextPath();
         String servletPath = request.getServletPath();
 
         return uri.substring(contextPath.length() + servletPath.length(), uri
                 .length());
     }
 
 
     /**
      * Resolves the given resource bundle key to a localized message. Returns
      * the key if no message can be found.
      * 
      * @param key
      * @return
      */
     private String resolveMessage(String key) {
 
         return getApplicationContext().getMessage(key, null,
                 getRequestContext().getLocale());
     }
 
 
     /**
      * Returns the current {@link HttpServletRequest}.
      * 
      * @return
      */
     private HttpServletRequest getRequest() {
 
         return (HttpServletRequest) this.pageContext.getRequest();
     }
 
 
     /**
      * Returns the {@link WebApplicationContext}.
      * 
      * @return
      */
     private WebApplicationContext getApplicationContext() {
 
         return getRequestContext().getWebApplicationContext();
     }
 
 
     /**
      * Returns whether to show the given {@link MenuItem} or not.
      * 
      * @param item
      * @return
      */
     private boolean showMenuItem(MenuItem item) {
 
         if (null == authenticationService) {
             return true;
         }
 
         return authenticationService.hasAllPermissions(item.getPermissions());
     }
 
 
     private void initAuthenticationService() {
 
         if (null == authenticationService) {
 
             Collection<AuthenticationService> beans =
                    getApplicationContext().getBeansOfType(
                             AuthenticationService.class).values();
 
             authenticationService =
                     beans.isEmpty() ? null : beans.iterator().next();
         }
     }
 
 
     public Integer getLevels() {
 
         return levels;
     }
 
 
     public void setLevels(Integer levels) {
 
         this.levels = levels;
     }
 
 
     /**
      * Set whether submenus should always be rendered. Standard is 'false'. If
      * false submenus are only rendered for active menu items. True is useful
      * for dropdown-submenus.
      */
     public void setAlwaysRenderSubmenus(boolean alwaysRenderSubmenus) {
 
         this.alwaysRenderSubmenus = alwaysRenderSubmenus;
     }
 
 
     public void setId(String id) {
 
         this.id = id;
     }
 
 }
