 package com.java.gwt.libertycinema.client.views;
 
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.FileUpload;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.FormPanel;
 import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
 import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
 
 import com.java.gwt.libertycinema.client.MainPanel;
 
 
 public class ImageUpload implements Command {
 
    private final MainPanel body;
     private final FormPanel form = new FormPanel();
 
    public ImageUpload(MainPanel body) {
        this.body = body;
     }
 
     public void execute() {
        setupUploadForm(body);
    }

    private void setupUploadForm(MainPanel body) {
         form.setAction("/libertycinema/putimage");
         form.setEncoding(FormPanel.ENCODING_MULTIPART);
         form.setMethod(FormPanel.METHOD_POST);
         
         Grid grid = new Grid(4, 2);
         
         grid.setWidget(0, 0, new Label("Title:"));
         final TextBox title = new TextBox();
         title.setName("title");
         grid.setWidget(0, 1, title);
         
         grid.setWidget(1, 0, new Label("Description:"));
         final TextArea description = new TextArea();
         description.setName("description");
         grid.setWidget(1, 1, description);
         
         grid.setWidget(2, 0, new Label("Pick a file:"));
         FileUpload upload = new FileUpload();
         upload.setName("image");
         grid.setWidget(2, 1, upload);
         
         grid.setWidget(3, 1, new Button("Submit", new ClickHandler() {
             public void onClick(ClickEvent event) {
                     form.submit();
             }
         }));
         
         form.setWidget(grid);
         
         form.addSubmitHandler(new FormPanel.SubmitHandler() {
             public void onSubmit(SubmitEvent event) {
                 if (title.getText().length() == 0) {
                     event.cancel();
                     }
                 }
         });
         
         form.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {
        	public void onSubmitComplete(SubmitCompleteEvent event) {
         
        	}
         });
         
        body.getMainPanel().clear();
        body.getMainPanel().add(form);
     }
 }
