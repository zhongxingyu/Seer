 package vahdin.view;
 
 import java.io.File;
 import java.sql.SQLException;
 
 import vahdin.VahdinUI;
 import vahdin.VahdinUI.LoginEvent;
 import vahdin.VahdinUI.LoginListener;
 import vahdin.data.Mark;
 import vahdin.data.User;
 
 import com.google.gwt.safehtml.shared.SafeHtmlUtils;
 import com.vaadin.navigator.View;
 import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
 import com.vaadin.server.ExternalResource;
 import com.vaadin.server.FileResource;
 import com.vaadin.server.ThemeResource;
 import com.vaadin.shared.ui.label.ContentMode;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.CustomLayout;
 import com.vaadin.ui.Embedded;
 import com.vaadin.ui.Image;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.UI;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.Window;
 
 public class SuggestedMarkView extends CustomLayout implements View {
 
     private final VahdinUI ui = (VahdinUI) UI.getCurrent();
     private final LoginListener loginListener;
 
     public SuggestedMarkView() {
         super("single-suggested-mark-sidebar");
         loginListener = new LoginListener() {
             @Override
             public void login(LoginEvent event) {
                 User user = ui.getCurrentUser();
                 if (!user.isAdmin()) {
                     ui.getNavigator().navigateTo("");
                 }
             }
         };
     }
 
     @Override
     public void enter(ViewChangeEvent event) {
         final String[] s = event.getParameters().split("/");
         final int markId = Integer.parseInt(s[0]);
         final Mark mark = Mark.getMarkById(markId);
         final User submitter = User.load(mark.getUserID());
 
         final Label title = new Label("<h2>"
                 + SafeHtmlUtils.htmlEscape(mark.getTitle()) + "</h2>",
                 ContentMode.HTML);
 
         final Button backButton = new Button();
         backButton.setStyleName("go-back-button");
         backButton.setIcon(new ExternalResource(
                 "VAADIN/themes/vahdintheme/img/back-button.png"));
         backButton.addClickListener(new Button.ClickListener() {
             @Override
             public void buttonClick(ClickEvent event) {
                 UI.getCurrent().getNavigator().navigateTo("");
             }
         });
 
         final Label creationDate = new Label("<h4>" + mark.getTime() + "</h4>",
                 ContentMode.HTML);
 
         final Label nickname = new Label("<h4 class=\"nickname\">"
                 + submitter.getName() + "</h4>", ContentMode.HTML);
        
        final Label description = new Label("<p class=\"mark-description\">"
                + mark.getDescription() + "</p>", ContentMode.HTML);
 
         final Button viewImageButton = new Button("View image");
         viewImageButton.setStyleName("view-image-button");
         viewImageButton.addClickListener(new Button.ClickListener() {
 
             @Override
             public void buttonClick(ClickEvent event) {
                 showImage(mark);
             }
         });
 
         final Button deleteButton = new Button("Delete");
         deleteButton.addStyleName("cancel-button");
         deleteButton.addClickListener(new Button.ClickListener() {
             @Override
             public void buttonClick(ClickEvent event) {
                 mark.delete();
                 ui.getNavigator().navigateTo("");
             }
         });
 
         final Button approveButton = new Button("Approve");
         approveButton.addStyleName("submit-button");
         approveButton.addClickListener(new Button.ClickListener() {
             @SuppressWarnings("unchecked")
             @Override
             public void buttonClick(ClickEvent event) {
                 mark.getItemProperty("APPROVED").setValue(true);
                 try {
                     Mark.commit();
                     ui.getNavigator().navigateTo("");
                 } catch (SQLException e) {
                     e.printStackTrace();
                 }
             }
         });
 
         addComponent(title, "mark-title");
         addComponent(backButton, "back-button");
         addComponent(creationDate, "mark-creation-date");
         addComponent(nickname, "mark-submitter-nickname");
        addComponent(description, "mark-description");
         addComponent(viewImageButton, "view-image-button");
         addComponent(deleteButton, "new-mark-cancel-button");
         addComponent(approveButton, "new-mark-submit-button");
 
         loginListener.login(null); // force login actions
     }
 
     @Override
     public void attach() {
         ui.addLoginListener(loginListener);
     }
 
     @Override
     public void detach() {
         ui.removeLoginListener(loginListener);
     }
 
     /*
      * Method that shows an image of a mark in a new window on top of the
      * current interface.
      */
     private void showImage(Mark mark) {
         final VahdinUI ui = (VahdinUI) UI.getCurrent(); // Get main window
         final Window imagewin = new Window(); // Create the window
         imagewin.setStyleName("single-image-window"); // Set style name
         imagewin.setModal(true); // Make it modal
         VerticalLayout layout = new VerticalLayout(); // Create layout for the
                                                       // image
         Button close = new Button("Click this bar to close the image",
                 new Button.ClickListener() { // Add a close button for the image
                     public void buttonClick(ClickEvent event) { // inline
                                                                 // click-listener
                         ((UI) imagewin.getParent()).removeWindow(imagewin); // close
                                                                             // the
                                                                             // window
                                                                             // by
                                                                             // removing
                                                                             // it
                                                                             // from
                                                                             // the
                                                                             // parent
                                                                             // window
                     }
                 });
         layout.addComponent(close);
 
         String basepath = System.getProperty("user.home");
         File imgDirectory = new File(basepath + "/contentimgs");
         String lookingForFilename = "m" + mark.getId();
         String tempFilename = null;
         String finalFilename = null;
 
         if (imgDirectory.isDirectory()) { // check to make sure it is a
                                           // directory
             String filenames[] = imgDirectory.list();
             for (int i = 0; i < filenames.length; i++) {
                 if (filenames[i].contains(lookingForFilename)) {
                     tempFilename = filenames[i];
                     break;
                 }
             }
         }
 
         if (tempFilename != null) {
             finalFilename = basepath + "/contentimgs/" + tempFilename;
             FileResource resource = new FileResource(new File(finalFilename));
             Image img = new Image(mark.getTitle(), resource);
             layout.addComponent(img);
         } else {
             Embedded img = new Embedded(mark.getTitle(), new ThemeResource(
                     "../vahdintheme/img/notfound.jpg"));
             layout.addComponent(img);
         }
 
         imagewin.setContent(layout);
         ui.addWindow(imagewin); // add modal window to main window
     }
 }
