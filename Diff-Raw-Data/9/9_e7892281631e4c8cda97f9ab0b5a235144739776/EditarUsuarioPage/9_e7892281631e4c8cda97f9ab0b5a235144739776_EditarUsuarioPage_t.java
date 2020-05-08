 package com.odea;
 
 import org.apache.shiro.SecurityUtils;
 import org.apache.shiro.subject.Subject;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.form.AjaxButton;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.PasswordTextField;
 import org.apache.wicket.markup.html.form.RequiredTextField;
 import org.apache.wicket.markup.html.panel.FeedbackPanel;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.LoadableDetachableModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.apache.wicket.validation.IValidatable;
 import org.apache.wicket.validation.IValidator;
 import org.apache.wicket.validation.ValidationError;
 
 import com.odea.domain.Usuario;
 import com.odea.services.DAOService;
import com.odea.validators.ticketExterno.OnRelatedFieldsNullValidator;
 
 public class EditarUsuarioPage extends BasePage {
 
 
 	private static final long serialVersionUID = 1L;
 
 	@SpringBean
 	public DAOService daoService;
 	
 	public CompoundPropertyModel<Usuario> usuarioModel;	
 	
 	
 	public EditarUsuarioPage() {
 		
 		final Subject subject = SecurityUtils.getSubject();
 		if (!subject.isAuthenticated()) {
 			this.redirectToInterceptPage(new LoginPage());
 		}
 		
 		
 		this.usuarioModel = new CompoundPropertyModel<Usuario>(
 				new LoadableDetachableModel<Usuario>() {
 
 					private static final long serialVersionUID = 1L;
 
 					@Override
 					protected Usuario load() {
 						return daoService.getUsuario(subject.getPrincipal().toString());
 					}
 				});
 		
 		
 		
 		EditUsuarioForm form = new EditUsuarioForm("form", usuarioModel) {
 
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			protected void onSubmit(AjaxRequestTarget target, EditUsuarioForm form) {
 				daoService.modificarUsuario(getModelObject());
 				setResponsePage(LoginPage.class);
 			}
 
 		};
 				
 		form.setOutputMarkupId(true);
 		add(form);
 		
 	}
 	
 	public abstract class EditUsuarioForm extends Form<Usuario> {
 
 		private static final long serialVersionUID = 1L;
 		
 		public PasswordTextField password;
 		public PasswordTextField confirmPassword;
 		
 		public EditUsuarioForm(String id, IModel<Usuario> model) {
 			super(id, model);
 			
 			final FeedbackPanel feedback = new FeedbackPanel("feedback");
 			feedback.setOutputMarkupId(true);
 			
 			RequiredTextField<String> login = new RequiredTextField<String>("nombre");
 			
 			password = new PasswordTextField("password");
 			
 			confirmPassword = new PasswordTextField("confirmPassword", new Model<String>());
 			
 			IValidator<String> passwordValidator = new IValidator<String>() {
 
 				private static final long serialVersionUID = 1L;
 
 				@Override
 				public void validate(IValidatable<String> validatable) {
 					
 					if (!password.getConvertedInput().equals(confirmPassword.getConvertedInput())) {
 						error(validatable, "Los passwords que ha ingresado son diferentes");
 					}
 					
 				}
 				
 				private void error(IValidatable<String> validatable, String errorKey) {
 					ValidationError error = new ValidationError();
 					error.addKey(getClass().getSimpleName() + "." + errorKey);
 					error.setMessage(errorKey);
 					validatable.error(error);
 				}
 				
 			};
 			
 			
 			confirmPassword.add(passwordValidator);
 
 			
 			AjaxButton submit = new AjaxButton("submit") {
 	
 				private static final long serialVersionUID = 1L;
 
 				@Override
 				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
 					EditUsuarioForm.this.onSubmit(target, (EditUsuarioForm) form);
 				}
				
				@Override
				protected void onError(AjaxRequestTarget target, Form<?> form) {
					target.add(feedback);
				}
 			};
 			
 			
 			add(feedback);
 			add(login);
 			add(password);
 			add(confirmPassword);
 			add(submit);
 			
 		}
 		
 		protected abstract void onSubmit(AjaxRequestTarget target, EditUsuarioForm form);
 
 	}
 	
 }
