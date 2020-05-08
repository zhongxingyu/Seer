 package ted.ui.configdialog;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
import java.util.Dictionary;
 import java.util.Enumeration;
 import java.util.Hashtable;
 
 import javax.swing.ButtonGroup;
 import javax.swing.JCheckBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.JRadioButton;
 import javax.swing.JSeparator;
 import javax.swing.JSlider;
 import javax.swing.JTextField;
 
 import ted.Lang;
 import ted.TedConfig;
 
 import com.jgoodies.forms.layout.CellConstraints;
 import com.jgoodies.forms.layout.FormLayout;
 
 /**
 * This code was edited or generated using CloudGarden's Jigloo
 * SWT/Swing GUI Builder, which is free for non-commercial
 * use. If Jigloo is being used commercially (ie, by a corporation,
 * company or business for any purpose whatever) then you
 * should purchase a license for each developer using Jigloo.
 * Please visit www.cloudgarden.com for details.
 * Use of Jigloo implies acceptance of these licensing terms.
 * A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
 * THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
 * LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
 */
 public class AdvancedPanel extends JPanel implements ActionListener
 {
 
 	{
 		//Set Look & Feel
 		try {
 			javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private JPanel advancedPanel;  //  @jve:decl-index=0:visual-constraint="10,37"
 	private JLabel labelTimeOutSeconds;
 	private JLabel labelTimeOut;
 	private JTextField textTimeOutSecs;
 	private JRadioButton radioTorrentSettings1;
 	private JSeparator jSeparator1;
 	private JSeparator jSeparator2;
 	private JRadioButton radioTorrentSettings2;
 	private JLabel labelTorrentSettings;
 	private JCheckBox useAutoScheduleCheckBox;	
 	private JSeparator jSeparator3;
 	private JSeparator jSeparatorProxy;
 	private JLabel labelTimeOutInSecs;
 	private ButtonGroup seederSettingGroup;  //  @jve:decl-index=0:
 	private JCheckBox checkNotDownloadCompressed = null;
 	private JTextField filterExtensions;
 	private JSlider timeoutInSecondsSlider;
 	private JCheckBox useProxy;
 	private JTextField proxyHost;
 	private JTextField proxyPort;
 	private JCheckBox useAuthProxy;
 	private JTextField proxyUsername;
 	private JTextField proxyPassword;
 	private JLabel proxyUserNameLabel;
 	private JLabel proxyPasswordLabel;
 	private JLabel proxyHostLabel;
 	private JLabel proxyPortLabel;
 	private final int MINTIMEOUT = 1;
 	private final int MAXTIMEOUT = 20;
 		
 	
 	public AdvancedPanel()
 	{
 		this.initGUI();
 	}
 	
 	private void initGUI() 
 	{
 		try 
 		{
 			advancedPanel = new JPanel();
 			this.add(advancedPanel);	
 			FormLayout advancedPanelLayout = new FormLayout(
 					"max(p;6dlu), 10dlu, 22dlu, max(p;6dlu), 88dlu, 35dlu:grow, max(p;16dlu)", 
 			"max(p;5dlu), max(p;5dlu), 30dlu, 15dlu, max(p;15dlu), max(p;15dlu), max(p;15dlu), 15dlu, max(p;15dlu), max(p;15dlu), max(p;15dlu), max(p;15dlu),max(p;15dlu),max(p;15dlu),max(p;15dlu),max(p;15dlu),max(p;15dlu),max(p;15dlu),max(p;15dlu)");
 			advancedPanel.setPreferredSize(new java.awt.Dimension(500, 500));
 			advancedPanel.setSize(new java.awt.Dimension(500,500));
 			advancedPanel.setLayout(advancedPanelLayout);
 
 			labelTimeOut = new JLabel();
 			advancedPanel.add(labelTimeOut, new CellConstraints("2, 2, 5, 1, default, default"));
 			labelTimeOut.setText(Lang.getString("TedConfigDialog.LabelTimeout"));
 			labelTimeOut.setBounds(14, 381, 371, 28);
 
 			timeoutInSecondsSlider = new JSlider(JSlider.HORIZONTAL,
 					MINTIMEOUT, MAXTIMEOUT, 10);
 			timeoutInSecondsSlider.setMajorTickSpacing(19);
 			timeoutInSecondsSlider.setMinorTickSpacing(1);

			//Create the label table
			Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
			labelTable.put( new Integer( MINTIMEOUT ),   new JLabel(MINTIMEOUT+"") );
			labelTable.put( new Integer( MAXTIMEOUT/2 ), new JLabel(Lang.getString("TedConfigDialog.Seconds")) );
			labelTable.put( new Integer( MAXTIMEOUT ),   new JLabel(MAXTIMEOUT+"") );
			timeoutInSecondsSlider.setLabelTable( labelTable );
 
 			timeoutInSecondsSlider.setPaintTicks(true);
 			timeoutInSecondsSlider.setPaintLabels(true);
 			timeoutInSecondsSlider.setPaintTrack(true);
 			timeoutInSecondsSlider.setSnapToTicks(true);
 
 			advancedPanel.add(timeoutInSecondsSlider, new CellConstraints("2, 3, 5, 1, fill, fill"));
 
 			labelTorrentSettings = new JLabel();
 			advancedPanel.add(labelTorrentSettings, new CellConstraints("2, 5, 5, 1, default, default"));
 			labelTorrentSettings.setText(Lang.getString("TedConfigDialog.LabelSeeders"));
 			labelTorrentSettings.setBounds(14, 439, 350, 28);
 
 			radioTorrentSettings1 = new JRadioButton();
 			advancedPanel.add(radioTorrentSettings1, new CellConstraints("2, 6, 5, 1, default, default"));
 			radioTorrentSettings1.setText(Lang
 					.getString("TedConfigDialog.RadioMinimumSeeders"));
 			radioTorrentSettings1.setBounds(7, 459, 364, 28);
 
 			radioTorrentSettings2 = new JRadioButton();
 			advancedPanel.add(radioTorrentSettings2, new CellConstraints("2, 7, 5, 1, default, default"));
 			radioTorrentSettings2.setText(Lang
 					.getString("TedConfigDialog.RadioMostSeeders"));
 			radioTorrentSettings2.setBounds(7, 483, 364, 28);
 
 			seederSettingGroup = new ButtonGroup();
 			seederSettingGroup.add(radioTorrentSettings1);
 			seederSettingGroup.add(radioTorrentSettings2);
 
 			jSeparator1 = new JSeparator();
 			advancedPanel.add(jSeparator1, new CellConstraints("2, 4, 5, 1, default, default"));
 
 			jSeparator2 = new JSeparator();
 			advancedPanel.add(jSeparator2, new CellConstraints("2, 8, 5, 1, default, default"));
 
 			checkNotDownloadCompressed = new JCheckBox();
 			checkNotDownloadCompressed.setBounds(14, 257, 371, 21);
 			checkNotDownloadCompressed.setText(Lang.getString("TedConfigDialog.FilterFiles"));
 			advancedPanel.add(checkNotDownloadCompressed, new CellConstraints("2, 9, 5, 1, default, default"));
 			checkNotDownloadCompressed.addActionListener(this);
 			checkNotDownloadCompressed.setActionCommand("compressed");
 
 			filterExtensions = new JTextField();
 			filterExtensions.setBounds(15, 257, 371, 21);
 			filterExtensions.setText("zip, rar, r01");
 			advancedPanel.add(filterExtensions, new CellConstraints("2, 10, 5, 1, default, default"));
 			{
 				jSeparator3 = new JSeparator();
 				advancedPanel.add(jSeparator3, new CellConstraints("2, 11, 5, 1, default, default"));
 			}
 			{
 				useAutoScheduleCheckBox = new JCheckBox();
				advancedPanel.add(useAutoScheduleCheckBox, new CellConstraints("2, 12, 5, 1, default, default"));
 				useAutoScheduleCheckBox.setText(Lang.getString("TedConfigDialog.AutomaticSchedule"));
 			}
 			
 			{
 				jSeparatorProxy = new JSeparator();
 				advancedPanel.add(jSeparatorProxy, new CellConstraints("2, 13, 5, 1, default, default"));
 			}
 			{
 				useProxy = new JCheckBox();
 				advancedPanel.add(useProxy, new CellConstraints("2, 14, 5, 1, default, default"));
 				useProxy.setText(Lang.getString("TedConfigDialog.UseProxy"));
 				useProxy.addActionListener(this);
 				useProxy.setActionCommand("useProxy");
 			}
 			{
 				proxyHost = new JTextField();
 				advancedPanel.add(proxyHost, new CellConstraints("5, 15, 2, 1, default, default"));
 				proxyHost.setEnabled(false);
 			}
 			{
 				proxyPort = new JTextField();
 				advancedPanel.add(proxyPort, new CellConstraints("5, 16, 2, 1, default, default"));
 				proxyPort.setEnabled(false);
 			}
 			{
 				useAuthProxy = new JCheckBox();
 				advancedPanel.add(useAuthProxy, new CellConstraints("2, 17, 5, 1, default, default"));
 				useAuthProxy.setEnabled(false);
 				useAuthProxy.setText(Lang.getString("TedConfigDialog.UseAuthProxy"));
 				useAuthProxy.addActionListener(this);
 				useAuthProxy.setActionCommand("useAuthProxy");
 			}
 			{
 				proxyUsername = new JTextField();
 				advancedPanel.add(proxyUsername, new CellConstraints("5, 18, 2, 1, default, default"));
 				proxyUsername.setEnabled(false);
 			}
 			{
 				proxyPassword = new JPasswordField();
 				advancedPanel.add(proxyPassword, new CellConstraints("5, 19, 2, 1, default, default"));
 				proxyPassword.setEnabled(false);
 			}
 			{
 				proxyHostLabel = new JLabel();
 				advancedPanel.add(proxyHostLabel, new CellConstraints("2, 15, 3, 1, default, default"));
 				proxyHostLabel.setText(Lang.getString("TedConfigDialog.proxyHostLabel"));
 			}
 			{
 				proxyPortLabel = new JLabel();
 				advancedPanel.add(proxyPortLabel, new CellConstraints("2, 16, 3, 1, default, default"));
 				proxyPortLabel.setText(Lang.getString("TedConfigDialog.ProxyPort"));
 			}
 			{
 				proxyUserNameLabel = new JLabel();
 				advancedPanel.add(proxyUserNameLabel, new CellConstraints("2, 18, 3, 1, default, default"));
 				proxyUserNameLabel.setText(Lang.getString("TedConfigDialog.proxyUserName"));
 			}
 			{
 				proxyPasswordLabel = new JLabel();
 				advancedPanel.add(proxyPasswordLabel, new CellConstraints("2, 19, 3, 1, default, default"));
 				proxyPasswordLabel.setText(Lang.getString("TedConfigDialog.Password"));
 			}
 			
 						
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Set the values of the current config in the fields
 	 */
 	public void setValues()
 	{
 		// get values
 		int timeOut = TedConfig.getInstance().getTimeOutInSecs();
 		if (timeOut < MINTIMEOUT)
 		{
 			timeOut = MINTIMEOUT;
 		}
 		else if (timeOut > MAXTIMEOUT)
 		{
 			timeOut = MAXTIMEOUT;
 		}
 		timeoutInSecondsSlider.setValue(timeOut);
 		
 		
 		this.setSelectedButton(seederSettingGroup, TedConfig.getInstance().getSeederSetting());
 		this.setPreferredSize(new java.awt.Dimension(500, 500));
 		this.setSize(new java.awt.Dimension(500,500));
 		checkNotDownloadCompressed.setSelected(TedConfig.getInstance().getDoNotDownloadCompressed());
 		filterExtensions.setText(TedConfig.getInstance().getFilterExtensions());
 		filterExtensions.setEnabled(checkNotDownloadCompressed.isSelected());
 		useAutoScheduleCheckBox.setSelected(TedConfig.getInstance().isUseAutoSchedule());
 		this.useProxy.setSelected(TedConfig.getInstance().getUseProxy());
 		this.useAuthProxy.setSelected(TedConfig.getInstance().getUseProxyAuth());
 		this.actionPerformed(new ActionEvent(this.useProxy,0,"useProxy"));
 		this.actionPerformed(new ActionEvent(this.useAuthProxy,0,"useAuthProxy"));
 		this.proxyUsername.setText(TedConfig.getInstance().getProxyUsername());
 		this.proxyPassword.setText(TedConfig.getInstance().getProxyPassword());
 		this.proxyHost.setText(TedConfig.getInstance().getProxyHost());
 		this.proxyPort.setText(TedConfig.getInstance().getProxyPort());
 	}
 	
 	/**
 	 * Check filled info
 	 */
 	public boolean checkValues()
 	{
 		return true;
 	}
 	
 	/**
 	 * Save values from the dialog to the config file
 	 * @param config
 	 */
 	public void saveValues()
 	{
 		// get values
 
 		int newTime = timeoutInSecondsSlider.getValue();
 		int seederSetting = this.getSelectedButton(seederSettingGroup);
 		
 		TedConfig.getInstance().setTimeOutInSecs(newTime);
 		TedConfig.getInstance().setSeederSetting(seederSetting);
 		TedConfig.getInstance().setDoNotDownloadCompressed(checkNotDownloadCompressed.isSelected());
 		TedConfig.getInstance().setFilterExtensions(filterExtensions.getText());
 		TedConfig.getInstance().setUseAutoSchedule(useAutoScheduleCheckBox.isSelected());
 		TedConfig.getInstance().setUseProxy(this.useProxy.isSelected());
 		TedConfig.getInstance().setUseProxyAuth(this.useAuthProxy.isSelected());
 		TedConfig.getInstance().setProxyUsername(this.proxyUsername.getText());
 		TedConfig.getInstance().setProxyPassword(this.proxyPassword.getText());
 		TedConfig.getInstance().setProxyHost(this.proxyHost.getText());
 		TedConfig.getInstance().setProxyPassword(this.proxyPort.getText());
		TedConfig.getInstance().setProxyPort(this.proxyPort.getText());
 	}
 	
 	/**
 	 * Get selected state from buttongroup
 	 * @param group
 	 * @return
 	 */
 	private int getSelectedButton(ButtonGroup group)
 	{
 		int i = 0;
 		Enumeration buttons = group.getElements();
 		JRadioButton temp;
 		while (buttons.hasMoreElements())
 		{
 			temp = (JRadioButton)buttons.nextElement();
 			if (temp.isSelected())
 			{
 				// check which state is selected and return it
 				String text = temp.getText();
 				if  (text.equals((Lang.getString("TedConfigDialog.RadioMinimumSeeders")))) //$NON-NLS-1$
 					return TedConfig.DOWNLOADMINIMUMSEEDERS;
 				else if (text.equals((Lang.getString("TedConfigDialog.RadioMostSeeders")))) //$NON-NLS-1$
 					return TedConfig.DOWNLOADMOSTSEEDERS;
 			}
 			else
 				i++;
 		}
 		
 		// default return value
 		return 1;
 	}
 	/**
 	 * Select button from the group
 	 * @param group
 	 * @param toSelect
 	 */
 	private void setSelectedButton(ButtonGroup group, int toSelect)
 	{
 		int i = 0;
 		Enumeration buttons = group.getElements();
 		JRadioButton temp;
 		while (buttons.hasMoreElements())
 		{
 			temp = (JRadioButton)buttons.nextElement();
 			String text = temp.getText();
 			if (((text.equals(Lang.getString("TedConfigDialog.RadioMinimumSeeders")) && toSelect == TedConfig.DOWNLOADMINIMUMSEEDERS) //$NON-NLS-1$
 					|| text.equals(Lang.getString("TedConfigDialog.RadioMostSeeders")) && toSelect == TedConfig.DOWNLOADMOSTSEEDERS)) //$NON-NLS-1$
 			{
 				temp.setSelected(true);
 			}
 			else
 				temp.setSelected(false);
 			i++;
 		}
 	}
 
 	public void actionPerformed(ActionEvent arg0) 
 	{
 		String action = arg0.getActionCommand();
 		
 		if(action.equals("compressed"))
 		{
 			filterExtensions.setEnabled(checkNotDownloadCompressed.isSelected());
 		}
 		else if(action.equals("useProxy"))
 		{
 			this.updateProxyOptions();
 		}
 		else if(action.equals("useAuthProxy"))
 		{
 			this.updateUseAuthProxy();
 		}
 		
 	}
 	
 	private void updateProxyOptions() {	
 		this.updateUseAuthProxy();
 		this.proxyHost.setEnabled(this.useProxy.isSelected());
 		this.proxyPort.setEnabled(this.useProxy.isSelected());
 	}
 
 	private void updateUseAuthProxy() {
 		this.useAuthProxy.setEnabled(this.useProxy.isSelected());
 		if (!this.useProxy.isSelected())
 		{
 			this.useAuthProxy.setSelected(false);
 		}
 		this.updateAuthProxyInput();
 		
 	}
 
 	private void updateAuthProxyInput() {
 		this.proxyUsername.setEnabled(this.useAuthProxy.isSelected());
 		this.proxyPassword.setEnabled(this.useAuthProxy.isSelected());
 	}
 
 	public JCheckBox getUseAutoScheduleCheckBox() {
 		return useAutoScheduleCheckBox;
 	}
 }
