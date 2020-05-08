 package org.patientview.radar.web.panels.navigation;
 
 import org.apache.wicket.MarkupContainer;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 import org.patientview.radar.model.user.ProfessionalUser;
 import org.patientview.radar.model.user.User;
 import org.patientview.radar.web.RadarSecuredSession;
 import org.patientview.radar.web.pages.ProfessionalsPage;
 import org.patientview.radar.web.pages.patient.AddPatientPage;
 import org.patientview.radar.web.pages.patient.ExistingPatientsListingPage;
 import org.patientview.radar.web.pages.patient.srns.SrnsPatientPage;
 import org.patientview.radar.web.pages.patient.srns.SrnsPatientPageReadOnly;
 
 // TODO: this could be split into patient, professional and no one logged in
 public class DefaultNavigationPanel extends BaseNavigationPanel {
     public DefaultNavigationPanel(Class<? extends org.apache.wicket.Page> pageClass) {
         boolean userLoggedIn = (isProfessionalOrSuperUserLoggedIn() || isPatientUserLoggedIn());
 
         // Generic links
         addHomePageLink();
 
         addLogoutLink(userLoggedIn);
 
 //        add(new BookmarkablePageLink<DiseaseIndexPage>("diseaseIndexPageLink", DiseaseIndexPage.class));
 
         // Enter new patient - only visible when a professional is logged in
         BookmarkablePageLink enterNewPatientPageLink =
                 new BookmarkablePageLink<SrnsPatientPage>("enterNewPatientPageLink", AddPatientPage.class);
         enterNewPatientPageLink.setVisible(isProfessionalOrSuperUserLoggedIn() && !isGroupAdmin());
         add(enterNewPatientPageLink);
 
         // Container for existing patients links, only visible when a professional is logged in
         // If they are a group admin then they do not have a renal unit and should not add patients
         MarkupContainer existingPatientsContainer = new WebMarkupContainer("existingPatientsContainer");
         existingPatientsContainer.setVisible(isProfessionalOrSuperUserLoggedIn());
         existingPatientsContainer.add(
                 new BookmarkablePageLink<ExistingPatientsListingPage>("patientsListingPageLink",
                        ExistingPatientsListingPage.class)
         );
         add(existingPatientsContainer);
 
         // Container for clinicians links
         // todo Removed this dropdown from navigation but kep the code per Rob's request
 //        MarkupContainer cliniciansContainer = new WebMarkupContainer("cliniciansContainer");
 //        cliniciansContainer.setVisible(isProfessionalOrSuperUserLoggedIn());
 //        cliniciansContainer.add(
 //                new BookmarkablePageLink<MpgnPage>("mpgnPageLink", MpgnPage.class),
 //                new BookmarkablePageLink<SrnsPage>("srnsPageLink", SrnsPage.class),
 //                new BookmarkablePageLink<ConsentFormsPage>("consentFormsPageLink", ConsentFormsPage.class)
 //        );
 //        add(cliniciansContainer);
 
         BookmarkablePageLink professionalsPageLink = new BookmarkablePageLink<ProfessionalsPage>(
                 "professionalsPageLink", ProfessionalsPage.class);
         professionalsPageLink.setVisible(!userLoggedIn || isProfessionalOrSuperUserLoggedIn());
         add(professionalsPageLink);
 
         BookmarkablePageLink patientsPageLink = new BookmarkablePageLink<SrnsPatientPageReadOnly>("patientsPageLink",
                 SrnsPatientPageReadOnly.class);
         patientsPageLink.setVisible(!userLoggedIn);
         add(patientsPageLink);
     }
 
     protected boolean isProfessionalOrSuperUserLoggedIn() {
         RadarSecuredSession session = RadarSecuredSession.get();
         return session.isSignedIn() ? session.getRoles().hasRole(User.ROLE_PROFESSIONAL) ||
                 session.getRoles().hasRole(User.ROLE_SUPER_USER) : false;
     }
 
     protected boolean isPatientUserLoggedIn() {
         RadarSecuredSession session = RadarSecuredSession.get();
         return session.isSignedIn() ? session.getRoles().hasRole(User.ROLE_PATIENT) : false;
     }
     protected boolean isGroupAdmin() {
         RadarSecuredSession session = RadarSecuredSession.get();
         User user = session.getUser();
         if (user instanceof ProfessionalUser) {
             return ((ProfessionalUser) user).isGroupAdmin();
         }
         return false;
     }
 }
