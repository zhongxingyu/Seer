 package grisu.frontend.view.swing.utils.ssh.wizard;
 
 import java.beans.PropertyChangeListener;
 import java.util.List;
 import java.util.Map;
 
 import org.ciscavate.cjwizard.PageFactory;
 import org.ciscavate.cjwizard.WizardPage;
 import org.ciscavate.cjwizard.WizardSettings;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Maps;
 
 public class NesiSetupWizardPageFactory implements PageFactory {
 	
 	Logger myLogger = LoggerFactory.getLogger(NesiSetupWizardPageFactory.class);
 	
 	public static final String LOGIN_PAGE_NAME = "Login";
 	public static final String LOGIN_INFO_NAME = "Login Progress";
 	public static final String SSH_COPY_NAME = "SSH copy";
 	public static final String SSH_COPY_PROGRESS_NAME = "SSH copy Progress";
 	public static final String SSH_CONFIG_NAME = "SSH config";
 	public static final String FINISH_NAME = "Finished";
     
 	private Map<String, WizardPage> pages = Maps.newLinkedHashMap();
     private final NesiSetupWizard wizard;
 
     public NesiSetupWizardPageFactory(NesiSetupWizard w) {
     	this.wizard = w;
     	
     	WizardPage loginPage = new WizardLoginPage(LOGIN_PAGE_NAME, "Logging into institution account", w);
     	pages.put(LOGIN_PAGE_NAME, loginPage);
     	
     	WizardLoginProgressPage infoPage = new WizardLoginProgressPage(LOGIN_INFO_NAME, "Progress");
     	pages.put(LOGIN_INFO_NAME, infoPage);
 
     	WizardSSHCopyPage sshPage = new WizardSSHCopyPage(SSH_COPY_NAME, "Copying ssh public keys");
     	pages.put(SSH_COPY_NAME, sshPage);
     	
     	WizardSSHCopyProgressPage sshProgressPage = new WizardSSHCopyProgressPage(SSH_COPY_PROGRESS_NAME, "Progress");
     	pages.put(SSH_COPY_PROGRESS_NAME, sshProgressPage);
     	
     	SshConfigSetupPage sshconfigPage = new SshConfigSetupPage(SSH_CONFIG_NAME, "Create ssh configuration");
     	pages.put(SSH_CONFIG_NAME, sshconfigPage);
     	
     	FinishPage finishPage = new FinishPage(FINISH_NAME, "Finished");
     	pages.put(FINISH_NAME, finishPage);
     }
     
     private String getTitleForIndex(int index) {
     	
     	return Iterables.get(pages.keySet(), index);
     }
     
     public WizardPage getPage(String name) {
     	
     	return pages.get(name);
     }
 	
 
 	@Override
 	public WizardPage createPage(List<WizardPage> path, WizardSettings settings) {
 		
 		int index = path.size();
 		
 		String title = getTitleForIndex(index);
 		
		if ( SSH_COPY_PROGRESS_NAME.equals(title) || SSH_CONFIG_NAME.equals(title) ) {
 			if ( ! wizard.isEnableSshKeyAccess() || wizard.getSites().size() == 0 ) {
 				index = index+1;
 			}
 			if ( wizard.getSites() == null || wizard.getSites().size() == 0 ) {
 				index = index+1;
 			}
 		}
	
 		
		String newTitle = getTitleForIndex(index);
		return pages.get(newTitle);
 	}
 
 
 
 
 
 }
