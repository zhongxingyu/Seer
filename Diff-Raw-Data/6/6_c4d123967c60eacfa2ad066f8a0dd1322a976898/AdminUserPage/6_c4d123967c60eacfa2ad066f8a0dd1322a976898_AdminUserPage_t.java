 package com.solidstategroup.radar.web.pages.admin;
 
 import com.solidstategroup.radar.model.user.ProfessionalUser;
 import com.solidstategroup.radar.service.UserManager;
 import com.solidstategroup.radar.web.behaviours.RadarBehaviourFactory;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.AjaxLink;
 import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.RequiredTextField;
 import org.apache.wicket.markup.html.link.BookmarkablePageLink;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.apache.wicket.util.string.StringValue;
 
 import java.util.Date;
 
 public class AdminUserPage extends AdminsBasePage {
 
     @SpringBean
     private UserManager userManager;
 
     private static final String PARAM_ID = "ID";
     private boolean editMode = false;
     private boolean newUser = false;
 
     public AdminUserPage() {
         this(new PageParameters());
     }
 
     public AdminUserPage(PageParameters parameters) {
         super();
 
         final ProfessionalUser user;
 
         // if id is empty or -1 then its a new user else try pull back record
         StringValue idValue = parameters.get(PARAM_ID);
         if (idValue.isEmpty() || idValue.toLong() == -1) {
             user = new ProfessionalUser();
 
             // if its new user just show edit mode straight away
             editMode = true;
             newUser = true;
         } else {
             user = userManager.getProfessionalUser(idValue.toLongObject());
         }
 
         CompoundPropertyModel<ProfessionalUser> professionalUserModel =
                 new CompoundPropertyModel<ProfessionalUser>(user);
 
         final FeedbackPanel feedback = new FeedbackPanel("feedback");
         feedback.setOutputMarkupId(true);
         feedback.setOutputMarkupPlaceholderTag(true);
         add(feedback);
 
         final Form<ProfessionalUser> userForm = new Form<ProfessionalUser>("userForm", professionalUserModel) {
             protected void onSubmit() {
                 try {
                     userManager.saveProfessionalUser(getModelObject());
 
                     if (newUser) {
                         setResponsePage(AdminUsersPage.class);
                     }
                 } catch (Exception e) {
                    error("Could not save user");
                 }
             }
         };
         add(userForm);
 
         UserLabel idLabel = new UserLabel("idLabel", new PropertyModel<Long>(user, "id"));
         idLabel.setHideable(false);
         userForm.add(idLabel);
 
         userForm.add(new UserLabel("surnameLabel", new PropertyModel<String>(user, "surname")));
         userForm.add(new UserTextField("surname"));
 
         userForm.add(new UserLabel("forenameLabel", new PropertyModel<String>(user, "forename")));
         userForm.add(new UserTextField("forename"));
 
         userForm.add(new UserLabel("titleLabel", new PropertyModel<String>(user, "title")));
         userForm.add(new UserTextField("title"));
 
         userForm.add(new UserLabel("roleLabel", new PropertyModel<String>(user, "role")));
         userForm.add(new UserTextField("role"));
 
         userForm.add(new UserLabel("emailLabel", new PropertyModel<String>(user, "email")));
         userForm.add(new UserTextField("email"));
 
         userForm.add(new UserLabel("phoneLabel", new PropertyModel<String>(user, "phone")));
         userForm.add(new UserTextField("phone"));
 
         userForm.add(new UserLabel("centreLabel", new PropertyModel<Long>(user.getCentre(), "id")));
         userForm.add(new UserTextField("centre", new PropertyModel<Long>(user.getCentre(), "id")));
 
         userForm.add(new UserLabel("dateRegisteredLabel", new PropertyModel<Date>(user, "dateRegistered")));
         userForm.add(new UserTextField("dateRegistered"));
 
         UserLabel usernameLabel = new UserLabel("usernameLabel", user.getUsername());
         // hide this field if new user as username will be set to email
         usernameLabel.setVisible(!newUser);
         usernameLabel.setHideable(false);
         userForm.add(usernameLabel);
 
         /**
          * Add a container to hold links for Edit and Delete options
          * This will show when not in editMode
          */
         WebMarkupContainer userOptions = new WebMarkupContainer("userOptions") {
             public boolean isVisible() {
                 return !editMode;
             }
         };
         userForm.add(userOptions);
 
         userOptions.add(new AjaxLink("edit") {
             public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                 editMode = true;
                 ajaxRequestTarget.add(userForm);
             }
         });
 
         AjaxLink deleteLink = new AjaxLink("delete") {
             public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                 try {
                     userManager.deleteProfessionalUser(user);
                     setResponsePage(AdminUsersPage.class);
                 } catch (Exception e) {
                     ajaxRequestTarget.add(feedback);
                     error("Could not delete user: " + e.toString());
                 }
             }
         };
         userOptions.add(deleteLink);
         deleteLink.add(RadarBehaviourFactory.getDeleteConfirmationBehaviour());
 
         /**
          * Add a container to hold the options for when the page is in edit mode
          */
         WebMarkupContainer editOptions = new WebMarkupContainer("editOptions") {
             public boolean isVisible() {
                 return editMode;
             }
         };
 
         editOptions.add(new AjaxSubmitLink("update") {
             protected void onSubmit(AjaxRequestTarget ajaxRequestTarget, Form<?> form) {
                 editMode = false;
                 ajaxRequestTarget.add(feedback);
                 ajaxRequestTarget.add(userForm);
             }
 
             protected void onError(AjaxRequestTarget ajaxRequestTarget, Form<?> form) {
                editMode = true;
                 ajaxRequestTarget.add(feedback);
                ajaxRequestTarget.add(userForm);
             }
         });
 
         editOptions.add(new AjaxLink("cancel") {
             public void onClick(AjaxRequestTarget ajaxRequestTarget) {
                 editMode = false;
 
                 // if its a new user then cancel back to list
                 if (newUser) {
                     setResponsePage(AdminUsersPage.class);
                 } else {
                     ajaxRequestTarget.add(userForm);
                 }
             }
         });
 
         userForm.add(editOptions);
 
         add(new BookmarkablePageLink<AdminUsersPage>("back",
                 AdminUsersPage.class));
     }
 
     /**
      * Helper class to add a label to the page that will show and hide when the form is reloading
      * depending on the mode of the page - Edit will be hidden, Normal will be shown
      */
     private class UserLabel extends Label {
         private boolean isHideable = true;
 
         private UserLabel(String s, String s1) {
             super(s, s1);
         }
 
         private UserLabel(String s, IModel<?> iModel) {
             super(s, iModel);
         }
 
         public boolean isVisible() {
             return !isHideable || !editMode;
         }
 
         public boolean isHideable() {
             return isHideable;
         }
 
         public void setHideable(boolean hideable) {
             isHideable = hideable;
         }
     }
 
     /**
      * Helper class to add a text field to the form that will show and hide when the form is reloaded and depending
      * on page mode - Edit will be shown, Normal will be hidden
      */
     private class UserTextField extends RequiredTextField {
         private UserTextField(String s) {
             super(s);
         }
 
         private UserTextField(String s, IModel iModel) {
             super(s, iModel);
         }
 
         public boolean isVisible() {
             return editMode;
         }
     }
 
     public static PageParameters getPageParameters(ProfessionalUser user) {
         return new PageParameters().set(PARAM_ID, user.getId());
     }
 }
