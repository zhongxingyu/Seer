 package org.synyx.minos.core.web.tags.menu;
 
 /**
  * Default implementation of MenuRenderer.
  *
  * If the property {@code levels} is set to a value greater than zero,
  * the tag only renders submenus down to the given level. Any value less or equal
  * than zero disables this restriction and simply renders the complete menu (the default).
  * This also holds true if the renderer has {@code alwaysRenderSubmenus} set to {@code true}.
  *
  * @author David Linsin
  */
 public class DefaultMenuRenderer implements MenuRenderer {
 
     private Integer levels = 0;
     private boolean alwaysRenderSubmenus = false;
     private String rootId = null;
 
     @Override
     public String beforeMenu(MenuMetaInfo info) {
 
         StringBuilder builder = new StringBuilder();
         builder.append("<div id=\"");
 
         // unless id provided default to "menu" as id
         if (info.getId() != null) {
             builder.append(rootId);
         } else {
             builder.append("menu");
         }
 
         builder.append("\"><ul class=\"menu\">");
 
         return builder.toString();
     }
 
 
     @Override
     public String afterMenu(MenuMetaInfo info) {
 
         return "</ul></div>";
     }
 
 
     @Override
     public String beforeMenuItem(MenuMetaInfo info) {
 
         String aClass = info.isActive() ? " class='active'" : "";
 
         return String.format("<li%s>", aClass);
     }
 
 
     @Override
     public String afterMenuItem(MenuMetaInfo info) {
 
         return "</li>";
     }
 
 
     @Override
     public String renderItem(MenuMetaInfo info) {
 
         if (info.getUrl() != null) {
             String aClass = info.isActive() ? " class='active'" : "";
 
             return String.format("<a id='%s' href='%s' title='%s'%s>%s</a>", info.getId(), info.getUrl(),
                     info.getDescription(), aClass, info.getTitle());
         }
 
         return "";
     }
 
 
     @Override
     public boolean proceedWithRenderingSubmenus(MenuMetaInfo info) {
 
         if (isAlwaysRenderSubmenus()) {
             return true;
         }
 
        return isLevelRestrictionDisabled() || levels <= info.getDepth();
     }
 
 
     @Override
     public String beforeSubmenuItems(MenuMetaInfo info) {
 
         return "<ul class='submenu'>";
     }
 
 
     @Override
     public String afterSubmenuItems(MenuMetaInfo info) {
 
         return "</ul>";
     }
 
 
     public boolean isAlwaysRenderSubmenus() {
 
         return alwaysRenderSubmenus;
     }
 
 
     public void setAlwaysRenderSubmenus(boolean alwaysRenderSubmenus) {
 
         this.alwaysRenderSubmenus = alwaysRenderSubmenus;
     }
 
 
     public String getRootId() {
 
         return rootId;
     }
 
 
     public void setRootId(String rootId) {
 
         this.rootId = rootId;
     }
 
 
     public Integer getLevels() {
 
         return levels;
     }
 
 
     public void setLevels(Integer levels) {
 
         this.levels = levels;
     }
 
 
     private boolean isLevelRestrictionDisabled() {
 
         return levels <= 0;
     }
 }
