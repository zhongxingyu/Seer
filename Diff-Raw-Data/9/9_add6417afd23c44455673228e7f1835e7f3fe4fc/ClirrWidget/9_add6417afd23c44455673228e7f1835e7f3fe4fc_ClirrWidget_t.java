 package org.sonar.plugins.clirr;
 
 import org.sonar.api.web.AbstractRubyTemplate;
 import org.sonar.api.web.RubyRailsWidget;
 import org.sonar.api.web.UserRole;
 
 @UserRole(UserRole.VIEWER)
 public class ClirrWidget extends AbstractRubyTemplate implements RubyRailsWidget {
 
   public String getId() {
     return "clirr_widget";
   }
 
   public String getTitle() {
     return "Clirr";
   }
 
   protected String getTemplatePath() {
    return "/clirr_widget.html.erb";
   }
 }
