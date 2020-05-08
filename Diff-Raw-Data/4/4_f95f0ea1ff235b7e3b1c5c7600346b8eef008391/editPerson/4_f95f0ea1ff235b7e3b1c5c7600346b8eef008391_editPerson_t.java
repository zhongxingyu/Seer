 /* Ara - capture species and specimen data
  * 
  * Copyright (C) 2009  INBio ( Instituto Naciona de Biodiversidad )
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */ 
 /*
  * editPerson.java
  *
  * Created on October 1, 2007, 12:12 AM
  * Copyright roaguilar
  */
 package org.inbio.ara.web.admin.person;
 
 import com.sun.rave.web.ui.appbase.AbstractPageBean;
 import com.sun.webui.jsf.component.AddRemove;
 import com.sun.webui.jsf.component.Body;
 import com.sun.webui.jsf.component.Button;
 import com.sun.webui.jsf.component.Form;
 import com.sun.webui.jsf.component.Head;
 import com.sun.webui.jsf.component.Html;
 import com.sun.webui.jsf.component.Hyperlink;
 import com.sun.webui.jsf.component.ImageComponent;
 import com.sun.webui.jsf.component.Label;
 import com.sun.webui.jsf.component.Link;
 import com.sun.webui.jsf.component.Message;
 import com.sun.webui.jsf.component.Page;
 import com.sun.webui.jsf.component.PanelLayout;
 import com.sun.webui.jsf.component.Tab;
 import com.sun.webui.jsf.component.TabSet;
 import com.sun.webui.jsf.component.TextArea;
 import com.sun.webui.jsf.component.TextField;
 import com.sun.webui.jsf.model.Option;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.List;
 import java.util.Locale;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.faces.FacesException;
 import javax.faces.application.FacesMessage;
 import javax.faces.component.UIComponent;
 import javax.faces.component.html.HtmlMessages;
 import javax.faces.context.FacesContext;
 import javax.faces.convert.LongConverter;
 import javax.faces.event.ValueChangeEvent;
 import javax.faces.validator.ValidatorException;
 import org.inbio.ara.persistence.institution.Institution;
 import org.inbio.ara.persistence.person.Person;
 import org.inbio.ara.persistence.person.Profile;
 import org.inbio.ara.web.SessionManager;
 import org.inbio.ara.web.audience.AudienceSessionBean;
 import org.inbio.ara.web.gathering.GatheringSessionBeanV2;
 import org.inbio.ara.web.gathering.SpecimenGenerationSessionBean;
 import org.inbio.ara.web.group.GroupSessionBean;
 import org.inbio.ara.web.identification.IdentificationSearchSessionBean;
 import org.inbio.ara.web.nomenclaturalgroup.NomenclaturalGroupSessionBean;
 import org.inbio.ara.web.specimen.SpecimenSessionBean;
 import org.inbio.ara.web.user.UserSessionBean;
 import org.inbio.ara.web.admin.institution.InstitutionSessionBean;
 import org.inbio.ara.web.admin.profile.ProfileSessionBean;
 import org.inbio.ara.web.util.BundleHelper;
 import org.inbio.ara.web.util.MessageBean;
 
 /**
  * <p>Page bean that corresponds to a similarly named JSP page.  This
  * class contains component definitions (and initialization code) for
  * all components that you have defined on this page, as well as
  * lifecycle methods and event handlers where you may add behavior
  * to respond to incoming events.</p>
  */
 public class editPerson extends AbstractPageBean {
 
 	private static final String MODIFICATION_SUCCESS = BundleHelper.getDefaultBundleValue("modification_success");
     // <editor-fold defaultstate="collapsed" desc="Managed Component Definition">
     private int __placeholder;
     private Option[] profileList;
     private Long[] selectedProfiles = new Long[]{};
     private Long[] currentSelectedProfiles;
     
     private Option[] institutionList;
     private Long[] selectedInstitutions = new Long[]{};
     private Long[] currentSelectedInstitutions;
     
     
     /**
      * <p>Automatically managed component initialization.  <strong>WARNING:</strong>
      * This method is automatically generated, so any user-specified code inserted
      * here is subject to being replaced.</p>
      */
     private void _init() throws Exception {
     }
     
     private Page page1 = new Page();
     
     public Page getPage1() {
         return page1;
     }
     
     public void setPage1(Page p) {
         this.page1 = p;
     }
     
     private Html html1 = new Html();
     
     public Html getHtml1() {
         return html1;
     }
     
     public void setHtml1(Html h) {
         this.html1 = h;
     }
     
     private Head head1 = new Head();
     
     public Head getHead1() {
         return head1;
     }
     
     public void setHead1(Head h) {
         this.head1 = h;
     }
     
     private Link link1 = new Link();
     
     public Link getLink1() {
         return link1;
     }
     
     public void setLink1(Link l) {
         this.link1 = l;
     }
     
     private Body body1 = new Body();
     
     public Body getBody1() {
         return body1;
     }
     
     public void setBody1(Body b) {
         this.body1 = b;
     }
     
     private Form form1 = new Form();
     
     public Form getForm1() {
         return form1;
     }
     
     public void setForm1(Form f) {
         this.form1 = f;
     }
 
     private Button btn_cancel1 = new Button();
 
     public Button getBtn_cancel1() {
         return btn_cancel1;
     }
 
     public void setBtn_cancel1(Button b) {
         this.btn_cancel1 = b;
     }
 
     private Label label1 = new Label();
 
     public Label getLabel1() {
         return label1;
     }
 
     public void setLabel1(Label l) {
         this.label1 = l;
     }
 
     private TabSet personTabSet1 = new TabSet();
 
     public TabSet getPersonTabSet1() {
         return personTabSet1;
     }
 
     public void setPersonTabSet1(TabSet ts) {
         this.personTabSet1 = ts;
     }
 
     private Tab personalInfoTab1 = new Tab();
 
     public Tab getPersonalInfoTab1() {
         return personalInfoTab1;
     }
 
     public void setPersonalInfoTab1(Tab t) {
         this.personalInfoTab1 = t;
     }
 
     private PanelLayout layoutPanel1 = new PanelLayout();
 
     public PanelLayout getLayoutPanel1() {
         return layoutPanel1;
     }
 
     public void setLayoutPanel1(PanelLayout pl) {
         this.layoutPanel1 = pl;
     }
 
     private Label label2 = new Label();
 
     public Label getLabel2() {
         return label2;
     }
 
     public void setLabel2(Label l) {
         this.label2 = l;
     }
 
     private Label label3 = new Label();
 
     public Label getLabel3() {
         return label3;
     }
 
     public void setLabel3(Label l) {
         this.label3 = l;
     }
 
     private Label label4 = new Label();
 
     public Label getLabel4() {
         return label4;
     }
 
     public void setLabel4(Label l) {
         this.label4 = l;
     }
 
     private TextField txt_firstName1 = new TextField();
 
     public TextField getTxt_firstName1() {
         return txt_firstName1;
     }
 
     public void setTxt_firstName1(TextField tf) {
         this.txt_firstName1 = tf;
     }
 
     private TextField txt_lastName1 = new TextField();
 
     public TextField getTxt_lastName1() {
         return txt_lastName1;
     }
 
     public void setTxt_lastName1(TextField tf) {
         this.txt_lastName1 = tf;
     }
 
     private TextField txt_secondLastName1 = new TextField();
 
     public TextField getTxt_secondLastName1() {
         return txt_secondLastName1;
     }
 
     public void setTxt_secondLastName1(TextField tf) {
         this.txt_secondLastName1 = tf;
     }
 
     private Label label5 = new Label();
 
     public Label getLabel5() {
         return label5;
     }
 
     public void setLabel5(Label l) {
         this.label5 = l;
     }
 
     private TextField txt_initials1 = new TextField();
 
     public TextField getTxt_initials1() {
         return txt_initials1;
     }
 
     public void setTxt_initials1(TextField tf) {
         this.txt_initials1 = tf;
     }
 
     private Label label6 = new Label();
 
     public Label getLabel6() {
         return label6;
     }
 
     public void setLabel6(Label l) {
         this.label6 = l;
     }
 
     private TextField txt_birthYear1 = new TextField();
 
     public TextField getTxt_birthYear1() {
         return txt_birthYear1;
     }
 
     public void setTxt_birthYear1(TextField tf) {
         this.txt_birthYear1 = tf;
     }
 
     private Label label7 = new Label();
 
     public Label getLabel7() {
         return label7;
     }
 
     public void setLabel7(Label l) {
         this.label7 = l;
     }
 
     private TextField txt_deathYear1 = new TextField();
 
     public TextField getTxt_deathYear1() {
         return txt_deathYear1;
     }
 
     public void setTxt_deathYear1(TextField tf) {
         this.txt_deathYear1 = tf;
     }
 
     private Label label8 = new Label();
 
     public Label getLabel8() {
         return label8;
     }
 
     public void setLabel8(Label l) {
         this.label8 = l;
     }
 
     private TextField txt_occupation1 = new TextField();
 
     public TextField getTxt_occupation1() {
         return txt_occupation1;
     }
 
     public void setTxt_occupation1(TextField tf) {
         this.txt_occupation1 = tf;
     }
 
     private Message message1 = new Message();
 
     public Message getMessage1() {
         return message1;
     }
 
     public void setMessage1(Message m) {
         this.message1 = m;
     }
 
     private Tab tab1 = new Tab();
 
     public Tab getTab1() {
         return tab1;
     }
 
     public void setTab1(Tab t) {
         this.tab1 = t;
     }
 
     private PanelLayout layoutPanel2 = new PanelLayout();
 
     public PanelLayout getLayoutPanel2() {
         return layoutPanel2;
     }
 
     public void setLayoutPanel2(PanelLayout pl) {
         this.layoutPanel2 = pl;
     }
 
     private Label label9 = new Label();
 
     public Label getLabel9() {
         return label9;
     }
 
     public void setLabel9(Label l) {
         this.label9 = l;
     }
 
     private Label label10 = new Label();
 
     public Label getLabel10() {
         return label10;
     }
 
     public void setLabel10(Label l) {
         this.label10 = l;
     }
 
     private Label label11 = new Label();
 
     public Label getLabel11() {
         return label11;
     }
 
     public void setLabel11(Label l) {
         this.label11 = l;
     }
 
     private Label label12 = new Label();
 
     public Label getLabel12() {
         return label12;
     }
 
     public void setLabel12(Label l) {
         this.label12 = l;
     }
 
     private Label label13 = new Label();
 
     public Label getLabel13() {
         return label13;
     }
 
     public void setLabel13(Label l) {
         this.label13 = l;
     }
 
     private Label label14 = new Label();
 
     public Label getLabel14() {
         return label14;
     }
 
     public void setLabel14(Label l) {
         this.label14 = l;
     }
 
     private Label label15 = new Label();
 
     public Label getLabel15() {
         return label15;
     }
 
     public void setLabel15(Label l) {
         this.label15 = l;
     }
 
     private Label label16 = new Label();
 
     public Label getLabel16() {
         return label16;
     }
 
     public void setLabel16(Label l) {
         this.label16 = l;
     }
 
     private TextField txt_email1 = new TextField();
 
     public TextField getTxt_email1() {
         return txt_email1;
     }
 
     public void setTxt_email1(TextField tf) {
         this.txt_email1 = tf;
     }
 
     private TextField txt_url1 = new TextField();
 
     public TextField getTxt_url1() {
         return txt_url1;
     }
 
     public void setTxt_url1(TextField tf) {
         this.txt_url1 = tf;
     }
 
     private TextField txt_telephone1 = new TextField();
 
     public TextField getTxt_telephone1() {
         return txt_telephone1;
     }
 
     public void setTxt_telephone1(TextField tf) {
         this.txt_telephone1 = tf;
     }
 
     private TextField txt_fax1 = new TextField();
 
     public TextField getTxt_fax1() {
         return txt_fax1;
     }
 
     public void setTxt_fax1(TextField tf) {
         this.txt_fax1 = tf;
     }
 
     private TextArea ta_streetAddress1 = new TextArea();
 
     public TextArea getTa_streetAddress1() {
         return ta_streetAddress1;
     }
 
     public void setTa_streetAddress1(TextArea ta) {
         this.ta_streetAddress1 = ta;
     }
 
     private TextField txt_city1 = new TextField();
 
     public TextField getTxt_city1() {
         return txt_city1;
     }
 
     public void setTxt_city1(TextField tf) {
         this.txt_city1 = tf;
     }
 
     private TextField txt_state1 = new TextField();
 
     public TextField getTxt_state1() {
         return txt_state1;
     }
 
     public void setTxt_state1(TextField tf) {
         this.txt_state1 = tf;
     }
 
     private TextField txt_country1 = new TextField();
 
     public TextField getTxt_country1() {
         return txt_country1;
     }
 
     public void setTxt_country1(TextField tf) {
         this.txt_country1 = tf;
     }
 
     private Tab tab2 = new Tab();
 
     public Tab getTab2() {
         return tab2;
     }
 
     public void setTab2(Tab t) {
         this.tab2 = t;
     }
 
     private PanelLayout layoutPanel3 = new PanelLayout();
 
     public PanelLayout getLayoutPanel3() {
         return layoutPanel3;
     }
 
     public void setLayoutPanel3(PanelLayout pl) {
         this.layoutPanel3 = pl;
     }
 
     private AddRemove addRemove_profile1 = new AddRemove();
 
     public AddRemove getAddRemove_profile1() {
         return addRemove_profile1;
     }
 
     public void setAddRemove_profile1(AddRemove ar) {
         this.addRemove_profile1 = ar;
     }
 
     private Tab tab3 = new Tab();
 
     public Tab getTab3() {
         return tab3;
     }
 
     public void setTab3(Tab t) {
         this.tab3 = t;
     }
 
     private PanelLayout layoutPanel4 = new PanelLayout();
 
     public PanelLayout getLayoutPanel4() {
         return layoutPanel4;
     }
 
     public void setLayoutPanel4(PanelLayout pl) {
         this.layoutPanel4 = pl;
     }
 
     private AddRemove addRemove_institution1 = new AddRemove();
 
     public AddRemove getAddRemove_institution1() {
         return addRemove_institution1;
     }
 
     public void setAddRemove_institution1(AddRemove ar) {
         this.addRemove_institution1 = ar;
     }
 
     private Button btn_save1 = new Button();
 
     public Button getBtn_save1() {
         return btn_save1;
     }
 
     public void setBtn_save1(Button b) {
         this.btn_save1 = b;
     }
 
     private Button btn_reset1 = new Button();
 
     public Button getBtn_reset1() {
         return btn_reset1;
     }
 
     public void setBtn_reset1(Button b) {
         this.btn_reset1 = b;
     }
 
     private LongConverter longConverter1 = new LongConverter();
 
     public LongConverter getLongConverter1() {
         return longConverter1;
     }
 
     public void setLongConverter1(LongConverter lc) {
         this.longConverter1 = lc;
     }
 
     private ImageComponent image1 = new ImageComponent();
 
     public ImageComponent getImage1() {
         return image1;
     }
 
     public void setImage1(ImageComponent ic) {
         this.image1 = ic;
     }
 
     private HtmlMessages messageList1 = new HtmlMessages();
 
     public HtmlMessages getMessageList1() {
         return messageList1;
     }
 
     public void setMessageList1(HtmlMessages hm) {
         this.messageList1 = hm;
     }
 
     private Hyperlink spanishLink1 = new Hyperlink();
 
     public Hyperlink getSpanishLink1() {
         return spanishLink1;
     }
 
     public void setSpanishLink1(Hyperlink h) {
         this.spanishLink1 = h;
     }
 
     private Hyperlink englishLink1 = new Hyperlink();
 
     public Hyperlink getEnglishLink1() {
         return englishLink1;
     }
 
     public void setEnglishLink1(Hyperlink h) {
         this.englishLink1 = h;
     }
     
     // </editor-fold>
     
     /**
      * <p>Construct a new Page bean instance.</p>
      */
     public editPerson() {
     }
     
     /**
      * <p>Callback method that is called whenever a page is navigated to,
      * either directly via a URL, or indirectly via page navigation.
      * Customize this method to acquire resources that will be needed
      * for event handlers and lifecycle methods, whether or not this
      * page is performing post back processing.</p>
      *
      * <p>Note that, if the current request is a postback, the property
      * values of the components do <strong>not</strong> represent any
      * values submitted with this request.  Instead, they represent the
      * property values that were saved for this view when it was rendered.</p>
      */
     public void init() {
         // Perform initializations inherited from our superclass
         super.init();
         // Perform application initialization that must complete
         // *before* managed components are initialized
         // TODO - add your own initialiation code here
         
         // <editor-fold defaultstate="collapsed" desc="Managed Component Initialization">
         // Initialize automatically managed components
         // *Note* - this logic should NOT be modified
         try {
             _init();
         } catch (Exception e) {
             log("Module editPerson initialization Failure", e);
             throw e instanceof FacesException ? (FacesException) e: new FacesException(e);
         }
         
         // </editor-fold>
         // Perform application initialization that must complete
         // *after* managed components are initialized
         // TODO - add your own initialization code here
     }
     
     /**
      * <p>Callback method that is called after the component tree has been
      * restored, but before any event processing takes place.  This method
      * will <strong>only</strong> be called on a postback request that
      * is processing a form submit.  Customize this method to allocate
      * resources that will be required in your event handlers.</p>
      */
     public void preprocess() {
     }
     
     /**
      * <p>Callback method that is called just before rendering takes place.
      * This method will <strong>only</strong> be called for the page that
      * will actually be rendered (and not, for example, on a page that
      * handled a postback and then navigated to a different page).  Customize
      * this method to allocate resources that will be required for rendering
      * this page.</p>
      */
     public void prerender() {
         if (!this.getadmin$person$PersonSessionBean().isEditMode()) {
             Person tperson = getadmin$person$PersonSessionBean().getPerson();
 
             this.txt_city1.setText(tperson.getCity());
             this.txt_country1.setText(tperson.getCountry());
 
             try {
                this.txt_birthYear1.setText(tperson.getBirthYear()+"");
                this.txt_deathYear1.setText(tperson.getDeathYear()+"");
             } catch (NullPointerException e) {
             }
 
             this.txt_email1.setText(tperson.getEmail());
             this.txt_fax1.setText(tperson.getFax());
             this.txt_firstName1.setText(tperson.getFirstName());
             this.txt_initials1.setText(tperson.getInitials());
             this.txt_lastName1.setText(tperson.getLastName());
             this.txt_occupation1.setText(tperson.getOccupation());
             this.txt_secondLastName1.setText(tperson.getSecondLastName());
             this.txt_state1.setText(tperson.getStateName());
             this.txt_telephone1.setText(tperson.getTelephone());
             this.txt_url1.setText(tperson.getUrl());
             this.ta_streetAddress1.setText(tperson.getStreetAddress());
             getadmin$person$PersonSessionBean().setEditMode(true);
         }
         populateProfileList();
         populateInstitutionList();
     }
     
     /**
      * <p>Callback method that is called after rendering is completed for
      * this request, if <code>init()</code> was called (regardless of whether
      * or not this was the page that was actually rendered).  Customize this
      * method to release resources acquired in the <code>init()</code>,
      * <code>preprocess()</code>, or <code>prerender()</code> methods (or
      * acquired during execution of an event handler).</p>
      */
     public void destroy() {
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected PersonSessionBean getadmin$person$PersonSessionBean() {
         return (PersonSessionBean)getBean("admin$person$PersonSessionBean");
     }
 
     public String btn_cancel_action() {
         // TODO: Process the action. Return value is a navigation
         // case name where null will return to the same page.
         this.getadmin$person$PersonSessionBean().cleanLists();
         this.getadmin$person$PersonSessionBean().setEditMode(false);
         return "doCancelEditPerson";
     }
 
     public String personalInfoTab_action() {
         // TODO: Process the action. Return value is a navigation
         // case name where null will return to the same page.
         
         return null;
     }
 
     public String tab2_action() {
         // TODO: Process the action. Return value is a navigation
         // case name where null will return to the same page.
         
         return null;
     }
 
     public String tab3_action() {
         // TODO: Process the action. Return value is a navigation
         // case name where null will return to the same page.
         
         return null;
     }
 
     public String btn_save_action() {
         Person person = getadmin$person$PersonSessionBean().getPerson();
         
         person.setFirstName((String)txt_firstName1.getText());
         person.setLastname((String)txt_lastName1.getText());
         person.setSecondLastName((String)txt_secondLastName1.getText());
         person.setInitials((String)txt_initials1.getText());
 
         try {
             person.setBirthYear(Integer.parseInt(txt_birthYear1.getText().toString()));
             person.setDeathYear(Integer.parseInt(txt_deathYear1.getText().toString()));
         } catch (NumberFormatException numberFormatException) {
         } catch (NullPointerException nullPointerException) {
         }
 
         person.setOccupation((String)txt_occupation1.getText());
         person.setTelephone((String)txt_telephone1.getText());
         person.setFax((String)txt_fax1.getText());
         String street = (String)ta_streetAddress1.getText();
         if (street == null) { street = "";}
         person.setStreetAddress(street);
         person.setCity((String)txt_city1.getText());
         person.setStateName((String)txt_state1.getText());
         person.setCountry((String)txt_country1.getText());
         person.setEmail((String)txt_email1.getText());
         person.setUrl((String)txt_url1.getText());
         person.setCreatedBy(getSessionManager().getUser().getUserName());
         person.setLastModificationBy(getSessionManager().getUser().getUserName());
         this.setSelectedProfilesString(this.addRemove_profile1.getValueAsStringArray(getFacesContext()));
         this.setSelectedInstitutionsString(this.addRemove_institution1.getValueAsStringArray(getFacesContext()));
         
         if (this.getadmin$person$PersonSessionBean().update(person,this.getSelectedProfiles(),this.getSelectedInstitutions())) {
             this.getutil$MessageBean().addSuccessMessage(MODIFICATION_SUCCESS);
             this.getadmin$person$PersonSessionBean().setEditMode(false);
             return "case1";
         } else {
             this.getutil$MessageBean().addErrorMessage(getadmin$person$PersonSessionBean().getEjbMessage());
             return null;
         }
     }
     
     public void setSelectedProfilesString(String[] selectedProfiles) {
         int i = 0;
         if (selectedProfiles.length > 0 ) {
              
             this.selectedProfiles = new Long[selectedProfiles.length];
             for (String valor: selectedProfiles) {
                 this.selectedProfiles[i] = Long.parseLong(selectedProfiles[i]);
                 i++;
             }
         } else {
             this.setSelectedProfiles(new Long[] {});
         }
     }
     
     public void setSelectedInstitutionsString(String[] selectedInstitutions) {
         int i = 0;
         if (selectedInstitutions.length > 0 ) {
              
             this.selectedInstitutions = new Long[selectedInstitutions.length];
             for (String valor: selectedInstitutions) {
                 this.selectedInstitutions[i] = Long.parseLong(selectedInstitutions[i]);
                 i++;
             }
         } else {
             this.setSelectedInstitutions(new Long[] {});
         }
     }
 
     public String btn_reset_action() {
         // TODO: Process the action. Return value is a navigation
         // case name where null will return to the same page.
         
         return null;
     }
 
     public Option[] getProfileList() {
         return profileList;
     }
 
     public void setProfileList(Option[] profileList) {
         this.profileList = profileList;
     }
 
     public Long[] getSelectedProfiles() {
         return selectedProfiles;
     }
 
     public void setSelectedProfiles(Long[] selectedProfiles) {
         this.selectedProfiles = selectedProfiles;
     }
 
     public Long[] getCurrentSelectedProfiles() {
         return currentSelectedProfiles;
     }
 
     public void setCurrentSelectedProfiles(Long[] currentSelectedProfiles) {
         this.currentSelectedProfiles = currentSelectedProfiles;
     }
     
     private void populateProfileList() {
         // Cargar todas las opciones disponibles del sistema
         this.setProfileList(new Option[]{});
         List<Profile> tList = this.getadmin$profile$ProfileSessionBean().getAllProfiles();
         this.profileList = new Option[tList.size()];
         int i = 0;
         for (Profile tOption: tList) {
             this.profileList[i] = new Option(tOption.getId(),tOption.getName());
             i++;
         }
     }
     
     private void populateInstitutionList() {
         // Cargar todas las opciones disponibles del sistema
         this.setInstitutionList(new Option[]{});
         List<Institution> tList = this.getadmin$institution$InstitutionSessionBean().getAllInstitutions();
         this.institutionList = new Option[tList.size()];
         int i = 0;
         for (Institution tOption: tList) {
             this.institutionList[i] = new Option(tOption.getId(),tOption.getName());
             i++;
         }
         
         // Cargas todas las instituciones seleccionadas para la persona
         //tList = this.getUserSessionBean().getSelectedUserOptions();
         /*
         tList = this.getadmin$person$PersonSessionBean().getSelectedPersonInstitutions();
         if (tList != null) {
             if (tList.size() > 0) {
                 //this.setCurrentSelectedOptions(new Long[tList.size()]);
                 this.setCurrentSelectedInstitutions(new Long[tList.size()]);
                 i = 0;
                 for (Institution tInstitution: tList) {
                     //this.getCurrentSelectedOptions()[i] = tOption.getId();
                     this.getCurrentSelectedInstitutions()[i] = tInstitution.getId();
                     i++;
                 }
                 this.addRemove_institution1.setSelected(this.getCurrentSelectedInstitutions());
             }            
         } else {
             this.personAlert1.setType("error");
             this.personAlert1.setSummary("Error");
             this.personAlert1.setDetail(getadmin$person$PersonSessionBean().getEjbMessage());
         }*/
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected ProfileSessionBean getadmin$profile$ProfileSessionBean() {
         return (ProfileSessionBean)getBean("admin$profile$ProfileSessionBean");
     }
 
     public void addRemove_profile1_processValueChange(ValueChangeEvent event) {
         // TODO: Replace with your code
         
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected UserSessionBean getuser$UserSessionBean() {
         return (UserSessionBean)getBean("user$UserSessionBean");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected SessionManager getSessionManager() {
         return (SessionManager)getBean("SessionManager");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected org.inbio.ara.web.AraRequestBean getAraRequestBean() {
         return (org.inbio.ara.web.AraRequestBean)getBean("AraRequestBean");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected org.inbio.ara.web.species.SpeciesSessionBean getspecies$SpeciesSessionBean() {
         return (org.inbio.ara.web.species.SpeciesSessionBean)getBean("species$SpeciesSessionBean");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected org.inbio.ara.web.AraApplicationBean getAraApplicationBean() {
         return (org.inbio.ara.web.AraApplicationBean)getBean("AraApplicationBean");
     }
 
     public Option[] getInstitutionList() {
         return institutionList;
     }
 
     public void setInstitutionList(Option[] institutionList) {
         this.institutionList = institutionList;
     }
 
     public Long[] getSelectedInstitutions() {
         return selectedInstitutions;
     }
 
     public void setSelectedInstitutions(Long[] selectedInstitutions) {
         this.selectedInstitutions = selectedInstitutions;
     }
 
     public Long[] getCurrentSelectedInstitutions() {
         return currentSelectedInstitutions;
     }
 
     public void setCurrentSelectedInstitutions(Long[] currentSelectedInstitutions) {
         this.currentSelectedInstitutions = currentSelectedInstitutions;
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected InstitutionSessionBean getadmin$institution$InstitutionSessionBean() {
         return (InstitutionSessionBean)getBean("admin$institution$InstitutionSessionBean");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected GroupSessionBean getgroup$GroupSessionBean() {
         return (GroupSessionBean)getBean("group$GroupSessionBean");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected AudienceSessionBean getaudience$AudienceSessionBean() {
         return (AudienceSessionBean)getBean("audience$AudienceSessionBean");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected org.inbio.ara.web.NIUS.SpeciesTabular getNIUS$SpeciesTabular() {
         return (org.inbio.ara.web.NIUS.SpeciesTabular)getBean("NIUS$SpeciesTabular");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected org.inbio.ara.web.references.ReferenceSessionBean getreferences$ReferenceSessionBean() {
         return (org.inbio.ara.web.references.ReferenceSessionBean)getBean("references$ReferenceSessionBean");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected org.inbio.ara.web.SessionBean1 getSessionBean1() {
         return (org.inbio.ara.web.SessionBean1)getBean("SessionBean1");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected org.inbio.ara.web.ApplicationBean1 getApplicationBean1() {
         return (org.inbio.ara.web.ApplicationBean1)getBean("ApplicationBean1");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected org.inbio.ara.web.RequestBean1 getRequestBean1() {
         return (org.inbio.ara.web.RequestBean1)getBean("RequestBean1");
     }
     
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected MessageBean getutil$MessageBean() {
         return (MessageBean)getBean("util$MessageBean");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected org.inbio.ara.web.util.SelectionListBean getutil$SelectionListBean() {
         return (org.inbio.ara.web.util.SelectionListBean)getBean("util$SelectionListBean");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected GatheringSessionBeanV2 getgathering$GatheringSessionBeanV2() {
         return (GatheringSessionBeanV2)getBean("gathering$GatheringSessionBeanV2");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected org.inbio.ara.web.gathering.GatheringDetailSessionBean getgathering$GatheringDetailSessionBean() {
         return (org.inbio.ara.web.gathering.GatheringDetailSessionBean)getBean("gathering$GatheringDetailSessionBean");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected SpecimenGenerationSessionBean getgathering$SpecimenGenerationSessionBean() {
         return (SpecimenGenerationSessionBean)getBean("gathering$SpecimenGenerationSessionBean");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected org.inbio.ara.web.identification.IdentificationSessionBean getidentification$IdentificationSessionBean() {
         return (org.inbio.ara.web.identification.IdentificationSessionBean)getBean("identification$IdentificationSessionBean");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected org.inbio.ara.web.site.SiteSessionBean getsite$SiteSessionBean() {
         return (org.inbio.ara.web.site.SiteSessionBean)getBean("site$SiteSessionBean");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected NomenclaturalGroupSessionBean getnomenclaturalgroup$NomenclaturalGroupSessionBean() {
         return (NomenclaturalGroupSessionBean)getBean("nomenclaturalgroup$NomenclaturalGroupSessionBean");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected IdentificationSearchSessionBean getidentification$IdentificationSearchSessionBean() {
         return (IdentificationSearchSessionBean)getBean("identification$IdentificationSearchSessionBean");
     }
 
     /**
      * <p>Return a reference to the scoped data bean.</p>
      */
     protected SpecimenSessionBean getspecimen$SpecimenSessionBean() {
         return (SpecimenSessionBean)getBean("specimen$SpecimenSessionBean");
     }
 
 	public void txt_email_validate(FacesContext context, UIComponent component, Object value) {
 
 		Pattern pattern = Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");
 		Matcher matcher = pattern.matcher((String)value);
 
 		if (!matcher.matches()){
 			throw new ValidatorException(new FacesMessage(BundleHelper.getDefaultBundleValue("errWrongEmail")));
 		}
 	}
 
 	public void year_validate(FacesContext context, UIComponent component, Object value) {
 
 		Pattern pattern = Pattern.compile("[1-9][0-9][0-9][0-9]");
 		Matcher matcher = pattern.matcher((String)value);
 
 		int intValue = Integer.parseInt((String)value);
 
 		if (!matcher.matches()){
 			throw new ValidatorException(new FacesMessage(BundleHelper.getDefaultBundleValue("errWrongYear")));
 		}else if(intValue < 1900){
 			throw new ValidatorException(new FacesMessage(BundleHelper.getDefaultBundleValue("errWrongYear")));
 		}
 	}
 
 	public void url_validate(FacesContext context, UIComponent component, Object value) {
 
 		try{
 			URL url = new URL((String)value);
 			
 		}catch(MalformedURLException e){
 			throw new ValidatorException(new FacesMessage(BundleHelper.getDefaultBundleValue("errWrongURL")));
 		}
 	}
 
 	public void telephone_validate(FacesContext context, UIComponent component, Object value) {
 
 		Pattern pattern = Pattern.compile("[0-9)(\\- ]*");
 		Matcher matcher = pattern.matcher((String)value);
 
 		if (!matcher.matches()){
 			throw new ValidatorException(new FacesMessage(BundleHelper.getDefaultBundleValue("errWrongTelephone")));
 		}
 	}
 }
 
