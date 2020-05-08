 package app.gui.components.panels;
 
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 import javax.swing.BorderFactory;
 import javax.swing.GroupLayout;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.LayoutStyle;
 import javax.swing.border.BevelBorder;
 
 import app.di.annotations.Mail;
 import app.gui.events.ClearFieldsEvent;
 import app.gui.events.CreateMessageEvent;
 import app.gui.events.EmailSentEvent;
 import app.gui.events.EventBusService;
 import app.gui.events.FillMessageRecipientsEvent;
 import app.gui.events.SendMessageEvent;
 import app.log.Logger;
 import app.log.Severity;
 import app.mail.Mailer;
 import app.mail.configuration.GmailConfiguration;
 import app.mail.configuration.HotmailConfiguration;
 import app.mail.configuration.MailerConfiguration;
 
 import com.google.common.eventbus.Subscribe;
 import com.google.inject.Inject;
 
 public class MailPanel extends JPanel {
 
 	private static final long serialVersionUID = 7435439774062236519L;
 
 	private JLabel accountLabelPartOne;
 	private JComboBox accountComboBox;
 	private JLabel accountLabelPartTwo;
 	private JLabel userNameLabel;
 	private JTextField userNameTextField;
 	private JLabel passwordLabel;
 	private JPasswordField passwordField;
 	private JLabel recipientsLabel;
 	private JScrollPane recipientsScrollPane;
 	private JLabel subjectLabel;
 	private JTextField subjectTextField;
 	
 	private MailerConfiguration configuration;
 	
 	@Inject
 	public MailPanel(@Mail JComboBox accountComboBox, 
 			@Mail JScrollPane recipientsScrollPane) {
 		
 		GroupLayout mailPanelLayout = new GroupLayout((JComponent) this);
 		setLayout(mailPanelLayout);
 		setBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED));
 		
 		accountLabelPartOne = new JLabel("Send mail using my ");
 		this.accountComboBox = accountComboBox;
 		accountLabelPartTwo = new JLabel(" account.");
 		userNameLabel = new JLabel("Username:");
 		passwordLabel = new JLabel("Password:");
 		userNameTextField = new JTextField();
 		passwordField = new JPasswordField();
 		recipientsLabel = new JLabel("Recipients:");
 		this.recipientsScrollPane = recipientsScrollPane;
 		subjectLabel = new JLabel("Subject:");
 		subjectTextField = new JTextField();
 		
 		setContent(mailPanelLayout);
 		
 		EventBusService.getEventBus().register(this);
 	}
 
 	@Subscribe
 	public void createMessage(CreateMessageEvent e) {
 		configuration = selectConfiguration();
 		if (configuration == null) {
 			Logger.INSTANCE.log(Severity.WARNING, 
 					"Invalid email provider selected.");
 			return;
 		}
 		Message msg = new MimeMessage(configuration.getSession());
 		try {
 			msg.setSubject(subjectTextField.getText());
 			msg.setFrom(new InternetAddress(userNameTextField.getText()));
 			msg.setText(e.getText());
 			EventBusService.getEventBus().post(new FillMessageRecipientsEvent(msg));
 		}
 		catch (MessagingException me) {
 			Logger.INSTANCE.log(Severity.ERROR, "Could not set message sender.");
 		}
 	}
 	
 	@Subscribe
 	public void sendMessage(SendMessageEvent e) {
 		if (configuration == null) {
 			Logger.INSTANCE.log(Severity.WARNING, 
 					"Invalid email provider selected.");
 			return;
 		}
 		Mailer mailer = new Mailer(configuration);
 		mailer.setMessage(e.getMessage());
 		try {
 			mailer.sendMail(userNameTextField.getText(), 
					passwordField.getPassword().toString());
 			EventBusService.getEventBus().post(new EmailSentEvent());
 		}
 		catch (MessagingException me) {
 			Logger.INSTANCE.log(Severity.ERROR, 
 					"Error sending email message.");
 		}
 	}
 	
 	@Subscribe
 	public void clearFields(ClearFieldsEvent e) {
 		userNameTextField.setText("");
 		passwordField.setText("");
 		subjectTextField.setText("");
 	}
 	
 	private MailerConfiguration selectConfiguration() {
 		String provider = (String) accountComboBox.getSelectedItem();
 		String username = userNameTextField.getText(); 
 		String password = passwordField.getPassword().toString();
 		if ("Gmail".equals(provider)) {
 			return GmailConfiguration.configureSession(username, password);
 		}
 		else if ("Hotmail".equals(provider)) {
 			return HotmailConfiguration.configureSession(username, password);
 		}
 		return null;
 	}
 
 	private void setContent(GroupLayout mailPanelLayout) {
 		mailPanelLayout.setVerticalGroup(mailPanelLayout.createSequentialGroup()
 				.addContainerGap(12, 12)
 				.addGroup(
 					mailPanelLayout.createParallelGroup(
 							GroupLayout.Alignment.BASELINE)
 				    .addComponent(subjectTextField, 
 				    	GroupLayout.Alignment.BASELINE, 
 				    	GroupLayout.PREFERRED_SIZE, 
 				    	GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 				    .addComponent(accountLabelPartOne, 
 				    	GroupLayout.Alignment.BASELINE, 
 				    	GroupLayout.PREFERRED_SIZE, 
 				    	GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 				    .addComponent(subjectLabel, GroupLayout.Alignment.BASELINE, 
 				    	GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, 
 				    	GroupLayout.PREFERRED_SIZE)
 				)
 				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
 				.addGroup(mailPanelLayout.createParallelGroup()
 				    .addGroup(
 				    	mailPanelLayout.createSequentialGroup()
 				        .addGroup(
 				        	mailPanelLayout.createParallelGroup(
 				        		GroupLayout.Alignment.BASELINE)
 				            .addComponent(accountComboBox, 
 				            	GroupLayout.Alignment.BASELINE, 
 				            	GroupLayout.PREFERRED_SIZE, 
 				            	GroupLayout.DEFAULT_SIZE, 
 				            	GroupLayout.PREFERRED_SIZE)
 				            .addComponent(accountLabelPartTwo, 
 				            	GroupLayout.Alignment.BASELINE, 
 				            	GroupLayout.PREFERRED_SIZE, 
 				            	GroupLayout.DEFAULT_SIZE, 
 				            	GroupLayout.PREFERRED_SIZE)
 				            .addComponent(recipientsLabel, 
 				            	GroupLayout.Alignment.BASELINE, 
 				            	GroupLayout.PREFERRED_SIZE, 
 				            	GroupLayout.DEFAULT_SIZE, 
 				            	GroupLayout.PREFERRED_SIZE)
 				        )
 				        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
 				        .addGroup(
 				        	mailPanelLayout.createParallelGroup(
 				        		GroupLayout.Alignment.BASELINE)
 				            .addComponent(userNameTextField, 
 				            	GroupLayout.Alignment.BASELINE, 
 				            	GroupLayout.PREFERRED_SIZE, 
 				            	GroupLayout.DEFAULT_SIZE, 
 				            	GroupLayout.PREFERRED_SIZE)
 				            .addComponent(userNameLabel, 
 				            	GroupLayout.Alignment.BASELINE, 
 				            	GroupLayout.PREFERRED_SIZE, 
 				            	GroupLayout.DEFAULT_SIZE, 
 				            	GroupLayout.PREFERRED_SIZE)
 				        )
 				        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
 				        .addGroup(
 				        	mailPanelLayout.createParallelGroup(
 				        		GroupLayout.Alignment.BASELINE)
 				            .addComponent(passwordField, 
 				            	GroupLayout.Alignment.BASELINE, 
 				            	GroupLayout.PREFERRED_SIZE, 
 				            	GroupLayout.DEFAULT_SIZE, 
 				            	GroupLayout.PREFERRED_SIZE)
 				            .addComponent(passwordLabel, 
 				            	GroupLayout.Alignment.BASELINE, 
 				            	GroupLayout.PREFERRED_SIZE, 
 				            	GroupLayout.DEFAULT_SIZE, 
 				            	GroupLayout.PREFERRED_SIZE)
 				        )
 				    )
 				    .addComponent(recipientsScrollPane, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 75, GroupLayout.PREFERRED_SIZE)
 				)
 				.addContainerGap(7, 7)
 			);
 			mailPanelLayout.setHorizontalGroup(
 				mailPanelLayout.createSequentialGroup()
 				.addContainerGap(7, 7)
 				.addGroup(
 					mailPanelLayout.createParallelGroup()
 				    .addGroup(
 				    	mailPanelLayout.createSequentialGroup()
 				        .addGroup(
 				        	mailPanelLayout.createParallelGroup()
 				            .addComponent(userNameLabel, 
 				            	GroupLayout.Alignment.LEADING, 
 				            	GroupLayout.PREFERRED_SIZE, 66, 
 				            	GroupLayout.PREFERRED_SIZE)
 				            .addComponent(passwordLabel, 
 				            	GroupLayout.Alignment.LEADING, 
 				            	GroupLayout.PREFERRED_SIZE, 66, 
 				            	GroupLayout.PREFERRED_SIZE)
 				         )
 				        .addGroup(
 				        	mailPanelLayout.createParallelGroup()
 				            .addComponent(userNameTextField, 
 				            	GroupLayout.Alignment.LEADING, 
 				            	GroupLayout.PREFERRED_SIZE, 195, 
 				            	GroupLayout.PREFERRED_SIZE)
 				            .addComponent(passwordField, 
 				            	GroupLayout.Alignment.LEADING, 
 				            	GroupLayout.PREFERRED_SIZE, 195, 
 				            	GroupLayout.PREFERRED_SIZE)
 				        )
 				    )
 				    .addComponent(accountLabelPartOne, 
 				    	GroupLayout.Alignment.LEADING, 
 				    	GroupLayout.PREFERRED_SIZE, 113, 
 				    	GroupLayout.PREFERRED_SIZE)
 				    .addGroup(
 				    	mailPanelLayout.createSequentialGroup()
 				        .addComponent(accountComboBox, 
 				        	GroupLayout.PREFERRED_SIZE, 96, 
 				        	GroupLayout.PREFERRED_SIZE)
 				        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
 				        .addComponent(accountLabelPartTwo, 
 				        	GroupLayout.PREFERRED_SIZE, 65, 
 				        	GroupLayout.PREFERRED_SIZE))
 				    )
 				.addGroup(
 					mailPanelLayout.createParallelGroup()
 				    .addComponent(subjectLabel, 
 				    	GroupLayout.Alignment.LEADING, 
 				    	GroupLayout.PREFERRED_SIZE, 80, 
 				    	GroupLayout.PREFERRED_SIZE)
 				    .addComponent(recipientsLabel, 
 				    	GroupLayout.Alignment.LEADING, 
 				    	GroupLayout.PREFERRED_SIZE, 80, 
 				    	GroupLayout.PREFERRED_SIZE)
 				)
 				.addGroup(
 					mailPanelLayout.createParallelGroup()
 				    .addComponent(subjectTextField, 
 				    	GroupLayout.Alignment.LEADING, 
 				    	GroupLayout.PREFERRED_SIZE, 362, 
 				    	GroupLayout.PREFERRED_SIZE)
 				    .addComponent(recipientsScrollPane, 
 				    	GroupLayout.Alignment.LEADING, 
 				    	GroupLayout.PREFERRED_SIZE, 362, 
 				    	GroupLayout.PREFERRED_SIZE)
 				)
 				.addContainerGap(12, 12)
 			);
 	}
 
 }
