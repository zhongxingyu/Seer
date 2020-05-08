 package de.escidoc.vaadin.dialog;
 
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.FormLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Window;
 
 import de.escidoc.vaadin.utilities.LayoutHelper;
 
 public class ErrorDialog extends Window {
     private static final long serialVersionUID = 6255824594582824620L;
 
     private final FormLayout layout = new FormLayout();
 
     /**
      * Displays an error message dialog to the customer.
      * 
      * @param mainWindow
      *            the main window of the application.
      * @param caption
      *            the headline.
      * @param errorMessage
      *            the message, describing what went wrong.
      */
     public ErrorDialog(final Window mainWindow, final String caption,
         final String errorMessage) {
         this(mainWindow, caption, errorMessage, 600, 300);
     }
 
     /**
      * Displays an error message dialog to the customer.
      * 
      * @param mainWindow
      *            the main window of the application.
      * @param caption
      *            the headline.
      * @param errorMessage
      *            the message, describing what went wrong.
      * @param width
      *            the width of the window.
      * @param height
      *            the height of the window.
      */
     public ErrorDialog(final Window mainWindow, final String caption,
         final String errorMessage, int width, int height) {
        super.setWidth(width + "px");
        super.setHeight(height + "px");
         super.setCaption(caption);
         super.setModal(true);
         layout.addComponent(LayoutHelper.create("", new Label(errorMessage),
             10, false));
         Button button = new Button("OK");
         layout.addComponent(LayoutHelper.create("", button, 10, false));
         button.addListener(new Button.ClickListener() {
             private static final long serialVersionUID = 6160566882245069146L;
 
             @Override
             public void buttonClick(ClickEvent event) {
                 mainWindow.removeWindow(ErrorDialog.this);
             }
         });
         super.addComponent(layout);
     }
 }
