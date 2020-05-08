 package com.solidstategroup.radar.web.panels.navigation;
 
 import com.solidstategroup.radar.web.pages.admin.AdminUsersPage;
 import com.solidstategroup.radar.web.pages.admin.AdminConsultantsPage;
 import com.solidstategroup.radar.web.pages.admin.AdminPatientsAllPage;
 import com.solidstategroup.radar.web.pages.admin.AdminPatientsPage;
import com.solidstategroup.radar.web.pages.admin.AdminIssuesPage;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 
 public class AdminNavigationPanel extends BaseNavigationPanel {
     public AdminNavigationPanel() {
         super();
 
         addHomePageLink();
         addLogoutLink(true);
         
         add(new BookmarkablePageLink<AdminUsersPage>("adminUsersPageLink", AdminUsersPage.class));
         add(new BookmarkablePageLink<AdminConsultantsPage>("adminConsultantsPageLink", AdminConsultantsPage.class));
         add(new BookmarkablePageLink<AdminPatientsAllPage>("adminPatientsAllPageLink", AdminPatientsAllPage.class));
         add(new BookmarkablePageLink<AdminPatientsPage>("adminPatientsPageLink", AdminPatientsPage.class));        
        add(new BookmarkablePageLink<AdminIssuesPage>("adminIssuesPageLink", AdminIssuesPage.class));
     }
 }
