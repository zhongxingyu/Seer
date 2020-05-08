 package org.iucn.sis.client.panels.users;
 
 import org.iucn.sis.client.api.container.SISClientBase;
 import org.iucn.sis.client.api.utils.UriBase;
 
 import com.extjs.gxt.ui.client.event.ButtonEvent;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.widget.Info;
 import com.extjs.gxt.ui.client.widget.Window;
 import com.extjs.gxt.ui.client.widget.button.Button;
 import com.extjs.gxt.ui.client.widget.form.Field;
 import com.extjs.gxt.ui.client.widget.form.TextField;
 import com.extjs.gxt.ui.client.widget.form.Validator;
 import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
 import com.extjs.gxt.ui.client.widget.layout.FormLayout;
 import com.solertium.lwxml.shared.GWTConflictException;
 import com.solertium.lwxml.shared.GenericCallback;
 import com.solertium.lwxml.shared.NativeDocument;
 import com.solertium.util.extjs.client.WindowUtils;
 
 import ext.ux.pwd.client.PasswordField;
 
 /**
  * Allows a username to be entered into the server and validated against the
  * database. Conflict exception is thrown (server) and handled (clietn) is
  * the name exists.
  * 
  * @author carl.scott <carl.scott@solertium.com>
  * 
  */
 public class AddUserWindow extends Window {
 
 	private final TextField<String> username;
 	private final PasswordField password;
 	private final TextField<String> confirmPassword;
 
 	private final TextField<String> firstname;
 	private final TextField<String> lastname;
 	private final TextField<String> initials;
 	private final TextField<String> affiliation;
 	private final TextField<String> nickname;
 
 	private final Button submit;
 	private final GenericCallback<String> callback;
 
 	public AddUserWindow(final boolean createAccount, GenericCallback<String> callback) {
 		super();
 		this.callback = callback;
 		setClosable(true);
 		setModal(true);
 		setHeading("Add User");
 
 		setLayout(new FormLayout(LabelAlign.LEFT));
 
 		add(username = new TextField<String>());
 		username.setFieldLabel("Username (as e-mail address)");
 		username.setAllowBlank(false);
 		username.setValidator(new Validator() {
 			public String validate(Field<?> field, String value) {
 				if (!value.toUpperCase().matches("[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}"))
 					return "This field must be a valid email address";
 
 				return null;
 			}
 		});
 		if (createAccount) {
 			add(password = new PasswordField());
 			password.setFieldLabel("Password");
 
 			add(confirmPassword = new TextField<String>());
 			confirmPassword.setFieldLabel("Confirm Password");
 			confirmPassword.setAllowBlank(false);
 			confirmPassword.setPassword(true);
 			confirmPassword.setValidator(new Validator() {
 				public String validate(Field<?> field, String value) {
 					if (!value.equals(password.getValue()))
 						return "This field must match the password field.";
 
 					return null;
 				}
 			});
 		} else {
 			password = null;
 			confirmPassword = null;
 		}
 
 		add(firstname = new TextField<String>());
 		firstname.setFieldLabel("First Name*");
 		add(lastname = new TextField<String>());
 		lastname.setFieldLabel("Last Name*");
 
 		add(initials = new TextField<String>());
 		initials.setFieldLabel("Initials (optional)");
 		initials.setAllowBlank(true);
 		
 		add(nickname = new TextField<String>());
 		nickname.setFieldLabel("Nickname");
 
 		add(affiliation = new TextField<String>());
 		affiliation.setFieldLabel("Affiliation");
 
 		submit = new Button("Submit", new SelectionListener<ButtonEvent>() {
 			@Override
 			public void componentSelected(ButtonEvent be) {
 				
 				if (!username.isValid())
 					WindowUtils.errorAlert("Error", "Please enter a valid e-mail account as the user name.");				
 				else if(createAccount) {
 					if(password.getValue() == null)
 						WindowUtils.errorAlert("Error", "Please enter the password.");
 					else if(!confirmPassword.isValid())
 						WindowUtils.errorAlert("Error",
 										"The contents of the \"confirm password\" field must match the contents of the \"password\" field exactly.");					
 					else if(firstname.getValue() == null)
 						WindowUtils.errorAlert("Error", "Please enter the first name.");		
 					else if(lastname.getValue() == null)
 						WindowUtils.errorAlert("Error", "Please enter the last name.");	
 					else{
 						submit.setEnabled(false);
 						putNewAccountAndProfile(username.getValue().toLowerCase(), password.getValue());
 					}
 				}else if(firstname.getValue() == null)
 					WindowUtils.errorAlert("Error", "Please enter the first name.");		
 				else if(lastname.getValue() == null)
 					WindowUtils.errorAlert("Error", "Please enter the last name.");	
 				else {
 					submit.setEnabled(false);
 					putNewAccountAndProfile(username.getValue().toLowerCase(), "");
 				}
 				
 			}
 
 			private void putNewAccountAndProfile(String u, String p) {
 				StringBuffer xml = new StringBuffer("<auth>");
 				xml.append("<u>");
 				xml.append(u);
 				xml.append("</u>");
 				xml.append("<p>");
 				xml.append(p);
 				xml.append("</p>");
 				xml.append("</auth>");
 
 				final NativeDocument doc = SISClientBase.getHttpBasicNativeDocument();
 				doc.putAsText(UriBase.getInstance().getSISBase() + "/authn", xml.toString(),
 						new GenericCallback<String>() {
 							public void onFailure(Throwable caught) {
 								WindowUtils.errorAlert("Error!", "An account is already allocated "
 										+ "to this e-mail address. Please try again.");
 								submit.setEnabled(true);
 							}
 
 							public void onSuccess(String result) {
 								putNewProfile();
 							}
 						});
 			}
 
 			private void putNewProfile() {
 				final NativeDocument createDoc = SISClientBase.getHttpBasicNativeDocument();
 				createDoc.post(UriBase.getInstance().getUserBase() + "/users/" + username.getValue().toLowerCase(), getXML(username.getValue()
 						.toLowerCase(), firstname.getValue(), lastname.getValue(),
 						affiliation.getValue() == null ? "" : affiliation.getValue(),
 						initials.getValue() == null ? "" : initials.getValue(),
						nickname.getValue() == null ? "" : nickname.getValue()), new GenericCallback<String>() {
 					public void onSuccess(String result) {
 						hide();
 						AddUserWindow.this.onSuccess(username.getValue().toLowerCase());
 
 					}
 
 					public void onFailure(Throwable caught) {
 						if (caught instanceof GWTConflictException)
 							WindowUtils.errorAlert("Error", "This entry already exists, please try again.");
 						else
 							WindowUtils.errorAlert("Error", "Server error, please try again later.");
 					}
 
 				});
 
 			}
 		});
 		
 		addButton(submit);
 		addButton(new Button("Cancel", new SelectionListener<ButtonEvent>() {
 			@Override
 			public void componentSelected(ButtonEvent ce) {
 				hide();
 			}
 		}));
 	}
 	
 	
 
	public String getXML(String username, String firstname, String lastname, String affiliation, String initials, String nickname) {
 		StringBuilder ret = new StringBuilder("<root>");
 		ret.append("<field name=\"username\"><![CDATA[" + username + "]]></field>");
 		ret.append("<field name=\"firstname\"><![CDATA[" + firstname + "]]></field>");
 		ret.append("<field name=\"lastname\"><![CDATA[" + lastname + "]]></field>");
 		ret.append("<field name=\"initials\"><![CDATA[" + initials + "]]></field>");
 		ret.append("<field name=\"nickname\"><![CDATA[" + nickname + "]]></field>");
 		ret.append("<field name=\"affiliation\"><![CDATA[" + affiliation + "]]></field>");
		ret.append("<field name=\"sis\">true</field>");
 		ret.append("</root>");
 		return ret.toString();
 	}
 
 	public void onSuccess(String username) {
 		Info.display("Success", "User {0} added.", username);
 		if (this.callback != null)
 			this.callback.onSuccess(username);
 	}
 }
