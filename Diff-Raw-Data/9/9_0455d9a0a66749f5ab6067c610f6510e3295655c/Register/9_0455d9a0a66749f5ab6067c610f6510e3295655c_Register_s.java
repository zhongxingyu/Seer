 package org.biz.employeesVS.UI.screens;
 
 import org.biz.employeesVS.MainApp;
 import org.biz.employeesVS.data.DataModel;
 import org.biz.employeesVS.ws.ReturnStatus;
 import org.biz.employeesVS.data.SoapServiceFactory;
 
 import com.vaadin.data.util.ObjectProperty;
 import com.vaadin.data.util.PropertysetItem;
 import com.vaadin.terminal.Sizeable;
 import com.vaadin.terminal.ThemeResource;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Form;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Button.ClickListener;
 import com.vaadin.ui.Window;
 
 @SuppressWarnings("serial")
 public class Register extends Window {
 
 	private DataModel dataModel;
    	private SoapServiceFactory ssf;
     private Form form = new Form();
     
 	private PropertysetItem registerProperties;
 
 
     public Register(final MainApp app, final Label lblRegister) {
 		System.out.println("Register created");
         setCaption("Register");
         addComponent(form);
         setWidth(300, Sizeable.UNITS_PIXELS);
         setHeight(350, Sizeable.UNITS_PIXELS);
        dataModel = app.getDataModel();
        ssf = dataModel.getSoapServiceFactory();
         
         form.setWriteThrough(false);
         
         /* Init form footer with buttons */
 		Button btnOK     = new Button("OK");
 		Button btnCancel = new Button("Cancel");
 		
         btnOK.setIcon(new ThemeResource("icons/16/ok.png"));
         btnCancel.setIcon(new ThemeResource("icons/16/cancel.png"));
 
         HorizontalLayout buttons = new HorizontalLayout();
         buttons.setSpacing(true);
         buttons.addComponent(btnOK);
         buttons.addComponent(btnCancel);
         form.setFooter(buttons);
         
         form.setReadOnly(false);
         
         registerProperties = new PropertysetItem();
         registerProperties.addItemProperty("userName", new ObjectProperty<Object>(""));
         registerProperties.addItemProperty("password", new ObjectProperty<Object>(""));
         registerProperties.addItemProperty("email", new ObjectProperty<Object>(""));
 
         form.setItemDataSource(registerProperties);
                 
         btnOK.addListener(new ClickListener() {
 			@Override
 			public void buttonClick(ClickEvent event) {
                 if (form.isValid()) {
                 	form.commit();
                 	try {
         				ReturnStatus sts = ssf.register(
         						form.getField("userName").getValue().toString(),
         						form.getField("password").getValue().toString(),
         						form.getField("email").getValue().toString());
         				if (sts.isSucces()) {					
         					app.getHeader().LoggedIn();
         					app.showNotification("Your Registration has been sent!");
         					lblRegister.setValue("Registration sent!");
         				} else {
         					app.showNotification("Registration is not successfull : " + sts.getMessage());										
         				}
 					} catch (UnsupportedOperationException e) {
 						e.printStackTrace();
 					}
                 }
                 app.removeDialog(getWindow());
 			}
 		});
        
         btnCancel.addListener(new ClickListener() {
 			@Override
 			public void buttonClick(ClickEvent event) {
 				form.discard();
                 app.removeDialog(getWindow());
 			}
 		});
     }        
     
 }
