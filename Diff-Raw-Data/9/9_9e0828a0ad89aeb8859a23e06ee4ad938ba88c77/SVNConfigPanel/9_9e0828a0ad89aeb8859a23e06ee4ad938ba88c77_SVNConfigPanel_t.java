 /**
  *
  */
 package org.jabylon.team.svn.config;
 
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.form.PasswordTextField;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.model.IModel;
 import org.eclipse.emf.common.util.URI;
 import org.jabylon.properties.Project;
 import org.jabylon.properties.PropertiesPackage;
 import org.jabylon.rest.ui.model.EObjectPropertyModel;
 import org.jabylon.rest.ui.model.PreferencesPropertyModel;
 import org.jabylon.rest.ui.wicket.BasicPanel;
 import org.jabylon.rest.ui.wicket.components.ControlGroup;
 import org.jabylon.rest.ui.wicket.config.AbstractConfigSection;
 import org.jabylon.security.CommonPermissions;
 import org.jabylon.team.svn.impl.SVNConstants;
 import org.osgi.service.prefs.Preferences;
 
 /**
  * @author Johannes Utzig (jutzig.dev@googlemail.com)
  *
  */
 public class SVNConfigPanel extends BasicPanel<Project> {
 
 	private static final long serialVersionUID = 1L;
 
 	public SVNConfigPanel(String id, IModel<Project> model, Preferences config) {
 		super(id, model);
 		EObjectPropertyModel<URI, Project> repositoryURI = new EObjectPropertyModel<URI, Project>(model, PropertiesPackage.Literals.PROJECT__REPOSITORY_URI);
 		ControlGroup uriGroup = new ControlGroup("uri-group", nls("uri.label"));
 		TextField<URI> uriField = new TextField<URI>("inputURI", repositoryURI);
 		uriField.setType(URI.class);
 		uriField.setConvertEmptyInputStringToNull(true);
 		uriGroup.add(uriField);
 		add(uriGroup);
 
 		ControlGroup moduleGroup = new ControlGroup("module-group", nls("module.label"));
 		PreferencesPropertyModel moduleModel = new PreferencesPropertyModel(config, SVNConstants.KEY_MODULE, "");
 		moduleGroup.add(new TextField<String>("inputModule",moduleModel));
 		add(moduleGroup);
 
 		ControlGroup usernameGroup = new ControlGroup("username-group", nls("username.label"));
 		PreferencesPropertyModel usernameModel = new PreferencesPropertyModel(config, SVNConstants.KEY_USERNAME, "");
 		usernameGroup.add(new TextField<String>("inputUsername",usernameModel));
 		add(usernameGroup);
 		
 		ControlGroup passwordGroup = new ControlGroup("password-group", nls("password.label"));
 		PreferencesPropertyModel passwordModel = new PreferencesPropertyModel(config, SVNConstants.KEY_PASSWORD, "");
 		PasswordTextField passwordTextField = new PasswordTextField("inputPassword",passwordModel);
 		passwordTextField.setResetPassword(false);
 		passwordTextField.setRequired(false);
 		passwordGroup.add(passwordTextField);
 		add(passwordGroup);
 
 	}
 
 	public static class SVNConfigSection extends AbstractConfigSection<Project>{
 
 		private static final long serialVersionUID = 1L;
 
 		private boolean svnSelected(IModel<Project> model) {
 			return "SVN".equals(model.getObject().getTeamProvider());
 		}
 
 
 		@Override
 		public WebMarkupContainer doCreateContents(String id, IModel<Project> input, Preferences config) {
 			return new SVNConfigPanel(id, input, config);
 		}
 
 		@Override
 		public void commit(IModel<Project> input, Preferences config) {
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public boolean isVisible(IModel<Project> input, Preferences config) {
 			return svnSelected(input);
 		}
 
 		@Override
 		public String getRequiredPermission() {
 			String projectName = null;
 			if(getDomainObject()!=null)
 				projectName = getDomainObject().getName();
			return CommonPermissions.constructPermission(CommonPermissions.PROJECT,projectName,CommonPermissions.ACTION_CONFIG);
 		}
 	}
 
 	
 
 }
