 package grisu.frontend.view.swing.login;
 
 import grisu.frontend.control.login.LoginManager;
 import grisu.settings.ClientPropertiesManager;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 
 import org.apache.commons.lang.StringUtils;
 
 import com.jgoodies.forms.factories.FormFactory;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.RowSpec;
 
 public class ServiceInterfaceUrlPanel extends JPanel {
 
 	private JComboBox comboBox;
 	private final DefaultComboBoxModel urlModel = new DefaultComboBoxModel();
 	private JLabel lblServiceinterfaceUrl;
 
 	/**
 	 * Create the panel.
 	 */
 	public ServiceInterfaceUrlPanel() {
 
 		for (final String url : ClientPropertiesManager
 				.getServiceInterfaceUrls()) {
 
 			final String posAlias = LoginManager.SERVICEALIASES.inverse().get(
					url.toLowerCase());
 			if (StringUtils.isNotBlank(posAlias)) {
 
 				if (urlModel.getIndexOf(posAlias) < 0) {
 					urlModel.addElement(posAlias);
 				}
 			} else {
 				if (urlModel.getIndexOf(url) < 0) {
 					urlModel.addElement(url);
 				}
 			}
 		}
 
 		setLayout(new FormLayout(new ColumnSpec[] {
 				FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("default:grow"),
 				FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
 				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC, }));
 		add(getLblServiceinterfaceUrl(), "2, 2, right, default");
 		add(getComboBox(), "4, 2, fill, default");
 
 	}
 
 	private JComboBox getComboBox() {
 		if (comboBox == null) {
 			comboBox = new JComboBox(urlModel);
 			comboBox.setEditable(true);
 
 			String defaultUrl = ClientPropertiesManager
 					.getDefaultServiceInterfaceUrl();
 			final String posAlias = LoginManager.SERVICEALIASES.inverse().get(
					defaultUrl.toLowerCase());
 			if (StringUtils.isNotBlank(posAlias)) {
 				defaultUrl = posAlias;
 			}
 			if (StringUtils.isNotBlank(defaultUrl)) {
 				urlModel.setSelectedItem(defaultUrl);
 			}
 		}
 		return comboBox;
 	}
 
 	private JLabel getLblServiceinterfaceUrl() {
 		if (lblServiceinterfaceUrl == null) {
 			lblServiceinterfaceUrl = new JLabel("Grisu backend:");
 		}
 		return lblServiceinterfaceUrl;
 	}
 
 	public String getServiceInterfaceUrl() {
 		return (String) (urlModel.getSelectedItem());
 	}
 
 	public void lockUI(final boolean lock) {
 
 		SwingUtilities.invokeLater(new Thread() {
 			@Override
 			public void run() {
 				getComboBox().setEnabled(!lock);
 			}
 		});
 
 	}
 }
