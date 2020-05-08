 package de.hswt.hrm.contact.ui.wizard;
 
 import java.net.URL;
 import java.util.HashMap;
 
 import org.eclipse.e4.xwt.IConstants;
 import org.eclipse.e4.xwt.XWT;
 import org.eclipse.e4.xwt.forms.XWTForms;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Text;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Optional;
 
 import de.hswt.hrm.contact.model.Contact;
 
 public class ContactWizardPageOne extends WizardPage {
 
     private static final Logger LOG = LoggerFactory.getLogger(ContactWizardPageOne.class);
 
     private Composite container;
     private Optional<Contact> contact;
 
     public ContactWizardPageOne(String pageName, Optional<Contact> contact) {
         super(pageName);
         this.contact = contact;
         setDescription(createDiscription());
     }
 
     private String createDiscription() {
         if (contact.isPresent()) {
             return "Kontakt bearbeiten";
         }
 
         return "Neuen Kontakt anlegen";
     }
 
     @Override
     public void createControl(Composite parent) {
 
         URL url = ContactWizardPageOne.class.getClassLoader().getResource(
                 "de/hswt/hrm/contact/ui/xwt/ContactWizardWindow" + IConstants.XWT_EXTENSION_SUFFIX);
         try {
             container = (Composite) XWTForms.load(parent, url);
         }
         catch (Exception e) {
             LOG.error("An error occured", e);
         }
 
         if (this.contact.isPresent()) {
             updateFields((container));
         }
 
         setKeyListener();
         setControl(container);
         setPageComplete(false);
     }
 
     private void updateFields(Composite Container) {
 Contact c = contact.get();
        
         Text t = (Text) XWT.findElementByName(container, "firstName");
         t.setText(c.getFirstName());
         t = (Text) XWT.findElementByName(container, "lastName");
         t.setText(c.getLastName());
         t = (Text) XWT.findElementByName(container, "street");
         t.setText(c.getStreet());
         t = (Text) XWT.findElementByName(container, "streetNumber");
         t.setText(c.getStreetNo());
         t = (Text) XWT.findElementByName(container, "city");
         t.setText(c.getCity());
        t = (Text) XWT.findElementByName(container, "zipCode");
         t.setText(c.getPostCode());
         
         t = (Text) XWT.findElementByName(container, "phone");
         t.setText(c.getPhone().or(""));
         t = (Text) XWT.findElementByName(container, "fax");
         t.setText(c.getFax().or(""));
         t = (Text) XWT.findElementByName(container, "mobilePhone");
         t.setText(c.getMobile().or(""));
         t = (Text) XWT.findElementByName(container, "email");
         t.setText(c.getEmail().or(""));
         t = (Text) XWT.findElementByName(container, "shortcut");
         t.setText(c.getShortcut().or(""));
     }
 
     public HashMap<String, Text> getMandatoryWidgets() {
         HashMap<String, Text> widgets = new HashMap<String, Text>();
         widgets.put("firstName", (Text) XWT.findElementByName(container, "firstName"));
         widgets.put("lastName", (Text) XWT.findElementByName(container, "lastName"));
         widgets.put("street", (Text) XWT.findElementByName(container, "street"));
         widgets.put("streetNumber", (Text) XWT.findElementByName(container, "streetNumber"));
         widgets.put("city", (Text) XWT.findElementByName(container, "city"));
         widgets.put("zipCode", (Text) XWT.findElementByName(container, "zipCode"));
 
         return widgets;
     }
 
     public HashMap<String, Text> getOptionalWidgets() {
         HashMap<String, Text> widgets = new HashMap<String, Text>();
         widgets.put("shortcut", (Text) XWT.findElementByName(container, "shortcut"));
         widgets.put("phone", (Text) XWT.findElementByName(container, "phone"));
         widgets.put("fax", (Text) XWT.findElementByName(container, "fax"));
         widgets.put("mobilePhone", (Text) XWT.findElementByName(container, "mobilePhone"));
         widgets.put("email", (Text) XWT.findElementByName(container, "email"));
 
         return widgets;
     }
 
     @Override
     public boolean isPageComplete() {
         for (Text textField : getMandatoryWidgets().values()) {
             if (textField.getText().length() == 0) {
                 return false;
             }
         }
         return true;
     }
 
     public void setKeyListener() {
         HashMap<String, Text> widgets = getMandatoryWidgets();
         for (Text text : widgets.values()) {
 
             text.addKeyListener(new KeyListener() {
 
                 @Override
                 public void keyPressed(KeyEvent e) {
                 }
 
                 @Override
                 public void keyReleased(KeyEvent e) {
                     getWizard().getContainer().updateButtons();
                 }
             });
         }
     }
 
 }
