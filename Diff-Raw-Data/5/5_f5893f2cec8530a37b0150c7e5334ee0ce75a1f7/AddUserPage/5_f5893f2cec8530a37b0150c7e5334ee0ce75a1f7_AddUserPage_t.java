 package hwinventory.ui;
 
 import hwinventory.domain.User;
 
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.model.CompoundPropertyModel;
 
 public class AddUserPage extends WebPage {
 	
     public AddUserPage() {
     	UserDraft aUserDraft = new UserDraft();
     	CompoundPropertyModel aUserDraftModel = new CompoundPropertyModel(aUserDraft);
     	Form form = new Form("form");
     	add(form);
     	TextField userName = new TextField("name");
     	form.add(userName);
     }
 }
