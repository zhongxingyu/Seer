 package view.menu.subMenuPanels;
 
 import inputOutput.SettingsModel;
 
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Locale;
 
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import resources.MenuLookAndFeel;
 import resources.Translator;
 import view.menu.MenuButton;
 import view.menu.MenuLabel;
 
 /**
  * 
  * @author Vidar Eriksson
  *
  */
 @SuppressWarnings("serial")
 public class Settings extends SubMenuPanel {
 	private static JTextField nameField = new JTextField();
 	private static JComboBox<Locale> language = new JComboBox<Locale>();
	private static JCheckBox fullscreen = new JCheckBox();
 	
 	public Settings(MenuButton button) {
 		super(Translator.getString("settings"), getPanel(), button);
 		button.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				saveSettings();
 			}
 		});
 	}
 
 	private static JPanel getPanel() {
 		JPanel p = new JPanel();		
 		
 		p.setLayout(new GridLayout(0, 2, resources.MenuLookAndFeel.getGap(), resources.MenuLookAndFeel.getGap()));
 		
 		p.add(new MenuLabel(Translator.getString("playerName")+ ":"));
 		p.add(nameField);
 		nameField.setFont(resources.MenuLookAndFeel.getLargeFont());
 		nameField.setText(SettingsModel.getUserName());
 		nameField.setBackground(MenuLookAndFeel.getSubMenuPanelColor());
 		nameField.setBorder(MenuLookAndFeel.getSettingsTextFieldFont());
 		
 		startComboBox();
 		p.add(new MenuLabel(Translator.getString("language")+":"));
 		p.add(language);
 		
 		p.add(new MenuLabel(Translator.getString("fullscreen")+":"));
 		p.add(fullscreen);
		fullscreen.setText(Translator.getString("on"));
 		fullscreen.setBackground(MenuLookAndFeel.getSubMenuPanelColor());
 		fullscreen.setFont(resources.MenuLookAndFeel.getLargeFont());
 		fullscreen.setSelected(SettingsModel.getFullscreen());
 		
 
 		return p;
 	}
 	
 	private static void startComboBox() {
 		language.removeAllItems();
 		language.addItem(inputOutput.SettingsModel.getLocale());
 		Locale[] l = inputOutput.SettingsModel.getAllLocales();
 		for (int a=0; a< l.length; a++){
 			if (!(l[a]).equals(inputOutput.SettingsModel.getLocale())){
 				language.addItem(l[a]);
 			}
 		}
 		
 		language.setFont(MenuLookAndFeel.getLargeFont());
 		language.setBackground(MenuLookAndFeel.getSubMenuPanelColor());
 		language.setBorder(resources.MenuLookAndFeel.getSettingsTextFieldFont());
 	}
 
 	private static void saveSettings(){
 		SettingsModel.setUserName(nameField.getText());
 		SettingsModel.setFullscreen(fullscreen.isSelected());
 		SettingsModel.setLocale((Locale) language.getSelectedItem());
 		SettingsModel.save();
 	}
 
 }
