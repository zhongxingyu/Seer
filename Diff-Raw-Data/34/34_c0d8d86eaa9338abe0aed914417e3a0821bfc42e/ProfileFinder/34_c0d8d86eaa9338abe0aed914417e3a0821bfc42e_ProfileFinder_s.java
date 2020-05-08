 package at.ac.tuwien;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.wicket.AttributeModifier;
 import org.apache.wicket.Component;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.form.AjaxButton;
 import org.apache.wicket.behavior.SimpleAttributeModifier;
 import org.apache.wicket.extensions.ajax.markup.html.autocomplete.AutoCompleteTextField;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.Button;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.RequiredTextField;
 import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
 import org.apache.wicket.markup.html.link.ExternalLink;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.markup.html.panel.ComponentFeedbackPanel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.apache.wicket.util.string.Strings;
 
 import at.ac.tuwien.components.FormTemplate;
 import at.ac.tuwien.domain.KeyValueEntry;
 import at.ac.tuwien.domain.Profile;
 import at.ac.tuwien.service.APIService;
 import at.ac.tuwien.service.DBService;
 
 public class ProfileFinder extends BasePage {
 
     private static final long serialVersionUID = 7734810518718389158L;
 
     @SpringBean(name = "APIService")
     private APIService apiService;
 
     @SpringBean(name = "DBService")
     private DBService dbService;
 
     private Form<Void> activationForm;
     private Form<Void> facebookForm;
     private Form<Void> queryForm;
 
     private Model<String> linkedInCode = new Model<String>();
     private Model<String> twitterCode = new Model<String>();
     private Model<String> facebookCode = new Model<String>();
     private ListView<String[]> linkedInUsers;
     private ListView<String[]> twitterUsers;
     private ListView<String[]> facebookUsers;
     private Component XingUser;
     private ExternalLink linkedInLink;
     private ExternalLink twitterLink;
 
     private WebMarkupContainer result;
 
     public ProfileFinder() {
         body.add(new AttributeModifier("id", true, new Model<String>("profileFinder")));
 
         result = new WebMarkupContainer("result");
         result.setOutputMarkupId(true);
         // result.setVisible(false);
 
         linkedInLink = new ExternalLink("linkedInLink", apiService.getLinkedInRequestURL(),
             "LinkedIn Verification");
         linkedInLink.setOutputMarkupId(true);
 
         twitterLink = new ExternalLink("twitterLink", apiService.getTwitterRequestURL(),
             "Twitter Verification");
         twitterLink.setOutputMarkupId(true);
 
         activationForm = new FormTemplate("activationForm") {
             private static final long serialVersionUID = 4775409828987880486L;
 
             @Override
             public void saveAction() {
                 apiService.verifyLinkedIn(linkedInCode.getObject());
                 apiService.verifyTwitter(twitterCode.getObject());
 
                 queryForm.setVisible(true);
                 activationForm.setVisible(false);
             }
 
             @Override
             public void setupForm() {
                 RequiredTextField<String> linkedIn = new RequiredTextField<String>("linkedIn", linkedInCode);
                 RequiredTextField<String> twitter = new RequiredTextField<String>("twitter", twitterCode);
 
                 add(linkedIn, twitter);
                 add(new ComponentFeedbackPanel("linkedInErrors", linkedIn));
                 add(new ComponentFeedbackPanel("twitterErrors", twitter));
             }
         };
 
         facebookForm = new FormTemplate("facebookForm") {
             private static final long serialVersionUID = 4775409728987880486L;
 
             @Override
             public void saveAction() {
                 apiService.setFacebookToken(facebookCode.getObject());
 
                 queryForm.setVisible(true);
                 facebookForm.setVisible(false);
             }
 
             @Override
             public void setupForm() {
                 RequiredTextField<String> facebook = new RequiredTextField<String>("facebook", facebookCode);
 
                 add(facebook);
                 add(new ComponentFeedbackPanel("facebookErrors", facebook));
             }
         };
 
         List<String[]> dummyList = new ArrayList<String[]>();
 
         linkedInUsers = new ListView<String[]>("linkedInUsers", dummyList) {
             private static final long serialVersionUID = 2265469743009974359L;
 
             @Override
             protected void populateItem(ListItem<String[]> item) {
                 String[] profile = item.getModelObject();
                 item.add(new Label("name", profile[1] + " " + profile[2]));
                item.add(new Image("profile-image", new Model<String>(profile[3])));
             }
         };
 
         twitterUsers = new ListView<String[]>("twitterUsers", dummyList) {
             private static final long serialVersionUID = 2265469743009974359L;
 
             @Override
             protected void populateItem(ListItem<String[]> item) {
                 String[] profile = item.getModelObject();
                 item.add(new Label("name", profile[1]));
                item.add(new Image("profile-image", new Model<String>(profile[2])));
             }
         };
 
         facebookUsers = new ListView<String[]>("facebookUsers", dummyList) {
             private static final long serialVersionUID = 2265469743009974359L;
 
             @Override
             protected void populateItem(ListItem<String[]> item) {
                 String[] profile = item.getModelObject();
                 item.add(new Label("name", profile[1]));
                item.add(new Image("profile-image", new Model<String>(profile[2])));
             }
         };
 
         final List<KeyValueEntry> possibleFriends = new ArrayList<KeyValueEntry>();
 
         for (Profile profile : dbService.getProfiles()) {
             possibleFriends.add(new KeyValueEntry(profile.getValue("UUID"), (profile.getPrename() + " " + profile
                 .getSurname())));
         }
 
         final TextField<String> customUser = new TextField<String>("customUser", new Model<String>());
         final Model<KeyValueEntry> autocompleteModel = new Model<KeyValueEntry>();
         final AutoCompleteTextField<KeyValueEntry> userautocomplete = new AutoCompleteTextField<KeyValueEntry>(
             "user", autocompleteModel) {
             private static final long serialVersionUID = -8859788147650177034L;
 
             @Override
             protected Iterator<KeyValueEntry> getChoices(String input) {
                 if (Strings.isEmpty(input)) {
                     return Collections.EMPTY_LIST.iterator();
                 }
 
                 List<KeyValueEntry> choices = new ArrayList<KeyValueEntry>(10);
 
                 for (final KeyValueEntry item : possibleFriends) {
                     if (item.getValue().toUpperCase().contains(input.toUpperCase())) {
                         choices.add(item);
                         if (choices.size() > 9) {
                             break;
                         }
                     }
                 }
                 return choices.iterator();
             }
         };
 
         final Button submit = new AjaxButton("submit", queryForm) {
             private static final long serialVersionUID = -5414315645065495551L;
 
             @Override
             protected void onSubmit(AjaxRequestTarget target, Form<?> queryForm) {
                 String uuid = "";
 
                 for (KeyValueEntry entry : possibleFriends) {
                     if (entry.getValue().equals(userautocomplete.getModelObject())) {
                         uuid = entry.getKey();
                         break;
                     }
                 }
 
                 if (uuid.equals("")) {
                     uuid = customUser.getModelObject();
                     if (uuid.equals("")) {
                         return;
                     }
                 }
 
                 if (apiService.checkNetworkId("facebook_id", uuid)) {
                     result.addOrReplace(new Label("fb_image", "<img src=\"/ok.png\"/>").setEscapeModelStrings(false));
                 } else {
                     result.addOrReplace(new Label("fb_image", "<img src=\"/fail.png\"/>").setEscapeModelStrings(false));
                 }
 
                 if (apiService.checkNetworkId("linkedin_id", uuid)) {
                     result.addOrReplace(new Label("linkedin_image", "<img src=\"/ok.png\"/>")
                         .setEscapeModelStrings(false));
                 } else {
                     result.addOrReplace(new Label("linkedin_image", "<img src=\"/fail.png\"/>")
                         .setEscapeModelStrings(false));
                 }
 
                 if (apiService.checkNetworkId("xing_id", uuid)) {
                     result.addOrReplace(new Label("xing_image", "<img src=\"/ok.png\"/>").setEscapeModelStrings(false));
                 } else {
                     result.addOrReplace(new Label("xing_image", "<img src=\"/fail.png\"/>")
                         .setEscapeModelStrings(false));
                 }
 
                 if (apiService.checkNetworkId("twitter_id", uuid)) {
                     result.addOrReplace(new Label("twitter_image", "<img src=\"/ok.png\"/>")
                         .setEscapeModelStrings(false));
                 } else {
                     result.addOrReplace(new Label("twitter_image", "<img src=\"/fail.png\"/>")
                         .setEscapeModelStrings(false));
                 }
                 twitterUsers.setList(apiService.executeTwitterQuery(uuid));
                 linkedInUsers.setList(apiService.executeLinkedInQuery(uuid));
                 facebookUsers.setList(apiService.executeFacebookQuery(uuid));
 
                 result.addOrReplace(new Label("facebookQuery", apiService.getFacebookQuery(uuid)));
                 result.addOrReplace(new Label("linkedinQuery", apiService.getLinkedinQuery(uuid)));
 
                 XingUser = new WebMarkupContainer("XINGuser").add(new SimpleAttributeModifier("src",
                     apiService.excecuteXingQuery(uuid)));
                 XingUser.setOutputMarkupId(true);
                 result.addOrReplace(XingUser);
                 XingUser.setVisible(true);
 
                 activationForm.setVisible(false);
                 result.setVisible(true);
                 target.add(result);
                 target.add(activationForm);
             }
 
             @Override
             protected void onError(AjaxRequestTarget arg0, Form<?> arg1) {
             }
         };
 
         queryForm = new FormTemplate("queryForm") {
             private static final long serialVersionUID = -998287126496140172L;
 
             @Override
             public void setupForm() {
                 add(submit);
                 add(userautocomplete);
                 add(customUser);
                 setOutputMarkupId(true);
             }
 
             @Override
             public void saveAction() {
                 result.setVisible(true);
             }
         };
 
         XingUser = new WebMarkupContainer("XINGuser").add(new SimpleAttributeModifier("src",
             ""));
         XingUser.setVisible(false);
 
         linkedInUsers.setOutputMarkupId(true);
         twitterUsers.setOutputMarkupId(true);
         facebookUsers.setOutputMarkupId(true);
         XingUser.setOutputMarkupId(true);
         result.add(linkedInUsers, twitterUsers, facebookUsers, XingUser);
 
         queryForm.setOutputMarkupId(true);
         queryForm.setVisible(false);
 
         if (apiService.alreadySet()) {
             activationForm.setVisible(false);
             queryForm.setVisible(true);
         }
 
         result.addOrReplace(new Label("facebookQuery", ""));
         result.addOrReplace(new Label("linkedinQuery", ""));
         result.add(new Label("fb_image", "<img src=\"/fail.png\"/>").setEscapeModelStrings(false));
         result.add(new Label("linkedin_image", "<img src=\"/fail.png\"/>").setEscapeModelStrings(false));
         result.add(new Label("xing_image", "<img src=\"/fail.png\"/>").setEscapeModelStrings(false));
         result.add(new Label("twitter_image", "<img src=\"/fail.png\"/>").setEscapeModelStrings(false));
         body.add(queryForm);
         activationForm.add(linkedInLink);
         activationForm.add(twitterLink);
 
         body.add(result);
         body.add(activationForm);
         body.add(facebookForm);
     }
 }
