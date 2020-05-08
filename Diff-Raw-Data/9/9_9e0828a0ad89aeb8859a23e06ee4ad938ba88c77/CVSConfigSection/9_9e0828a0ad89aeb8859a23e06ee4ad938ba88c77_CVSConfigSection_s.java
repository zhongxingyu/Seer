 /**
  *
  */
 package org.jabylon.team.cvs.config;
 
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.model.IModel;
 import org.osgi.service.prefs.Preferences;
 
 import org.jabylon.properties.Project;
 import org.jabylon.rest.ui.wicket.config.AbstractConfigSection;
 import org.jabylon.security.CommonPermissions;
 
 
 /**
  * @author Johannes Utzig (jutzig.dev@googlemail.com)
  *
  */
 public class CVSConfigSection extends AbstractConfigSection<Project>{
 
 	private static final long serialVersionUID = 1L;
 
 	private boolean cvsSelected(IModel<Project> model) {
 		return "CVS".equals(model.getObject().getTeamProvider());
 	}
 
 
 	@Override
 	public WebMarkupContainer doCreateContents(String id, IModel<Project> input, Preferences config) {
 		return new CVSConfigPanel(id, input, config);
 	}
 
 	@Override
 	public void commit(IModel<Project> input, Preferences config) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public boolean isVisible(IModel<Project> input, Preferences config) {
 		return cvsSelected(input);
 	}
 
 	@Override
 	public String getRequiredPermission() {
 		String projectName = null;
 		if(getDomainObject()!=null)
 			projectName = getDomainObject().getName();
		return CommonPermissions.constructPermission(CommonPermissions.PROJECT,projectName,CommonPermissions.ACTION_EDIT);
 	}
 }
