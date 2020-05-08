 package au.org.scoutmaster.views.wizards.bulkSMS;
 
 import java.util.List;
 
 import org.vaadin.teemu.wizards.WizardStep;
 
 import au.com.vaadinutils.crud.MultiColumnFormLayout;
 import au.org.scoutmaster.application.SMSession;
 import au.org.scoutmaster.dao.DaoFactory;
 import au.org.scoutmaster.dao.SMSProviderDao;
 import au.org.scoutmaster.domain.Phone;
 import au.org.scoutmaster.domain.SMSProvider;
 import au.org.scoutmaster.domain.access.User;
 
 import com.vaadin.data.validator.StringLengthValidator;
 import com.vaadin.event.FieldEvents.TextChangeEvent;
 import com.vaadin.event.FieldEvents.TextChangeListener;
 import com.vaadin.shared.ui.label.ContentMode;
 import com.vaadin.ui.ComboBox;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Notification;
 import com.vaadin.ui.TextArea;
 import com.vaadin.ui.TextField;
 import com.vaadin.ui.VerticalLayout;
 
 public class MessageDetailsStep implements WizardStep
 {
 
 	private TextField subject;
 	private TextArea message;
 	private Label remaining;
 
 	// SMFormHelper<SMSProvider> formHelper;
 	private TextField from;
 	private ComboBox providers;
 	private BulkSMSWizardView wizard;
 	private MultiColumnFormLayout<SMSProvider> formLayout;
 	private VerticalLayout layout;
 	private Label recipientCount;
 
 	public MessageDetailsStep(BulkSMSWizardView messagingWizardView)
 	{
 		wizard = messagingWizardView;
 		
 		layout = new VerticalLayout();
 		layout.setDescription("MessageDetailsContent");
 		
 		layout.addComponent(new Label("Enter the subject and message and then click next."));
 		formLayout = new MultiColumnFormLayout<>(1, null);
 		formLayout.setColumnFieldWidth(0, 500);
 		formLayout.setSizeFull();
 		
 		SMSProviderDao daoSMSProvider = new DaoFactory().getSMSProviderDao();
 		List<SMSProvider> list = daoSMSProvider.findAll();
 		if (list.size() == 0)
 			throw new IllegalStateException("You must first configure an SMS Provider");
 		SMSProvider provider = list.get(0);
 
 		providers = new ComboBox("Provider");
 		providers.setContainerDataSource(daoSMSProvider.createVaadinContainer());
		
 		providers.select(provider.getId());
 		
 		recipientCount = new Label();
 		recipientCount.setContentMode(ContentMode.HTML);
 		layout.addComponent(recipientCount);
 
 		from = formLayout.bindTextField("From Mobile No.", "from");
 		from.addValidator(new StringLengthValidator("'From Mobile' must be supplied", 1, 15, false));
 		User user = SMSession.INSTANCE.getLoggedInUser();
 		String senderID = provider.getDefaultSenderID();
 		if (user.getSenderMobile()!= null && user.getSenderMobile().length() > 0)
 			senderID = user.getSenderMobile();
 		from.setValue(senderID);
 		from.setDescription("Enter your mobile phone no. so that all messages appear to come from you and recipients can send a text directly back to your phone.");
 		subject = formLayout.bindTextField("Subject", "subject");
 		subject.addValidator(new StringLengthValidator("'Subject' must be supplied", 1, 255, false));
 		message = formLayout.bindTextAreaField("Message", "message", 4);
 		message.addValidator(new StringLengthValidator("'Message' must be supplied", 1, 160, false));
 		remaining = formLayout.bindLabel("Characters remaining 160");
 		remaining.setImmediate(true);
 		
 		layout.addComponent(formLayout);
 		layout.setMargin(true);
 		
 		message.addTextChangeListener(new TextChangeListener()
 		{
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void textChange(TextChangeEvent event)
 			{
 				remaining.setValue("Characters remaining " + (160 - event.getText().length()));
 				
 			}
 		});
 
 	}
 
 	@Override
 	public String getCaption()
 	{
 		return "Message Details";
 	}
 
 	@Override
 	public Component getContent()
 	{
 		recipientCount.setValue("<p><b>" + wizard.getRecipientStep().getRecipientCount()
 				+ " recipients have been selected to recieve the following SMS.</b></p>");
 
 		return layout;
 	}
 
 	@Override
 	public boolean onAdvance()
 	{
 
 		boolean advance = notEmpty("Message", message.getValue()) && notEmpty("From", from.getValue())
 				&& notEmpty("Subject", subject.getValue());
 
 		if (!advance)
 			Notification.show("Please enter your Mobile, Subject and a Message then click Next");
 		return advance;
 	}
 
 	private boolean notEmpty(String label, String value)
 	{
 		return value != null && value.length() > 0;
 	}
 
 	@Override
 	public boolean onBack()
 	{
 		return true;
 	}
 
 	public Message getMessage()
 	{
 		return new Message(subject.getValue(), message.getValue(), new Phone(from.getValue()));
 	}
 
 	public SMSProvider getProvider()
 	{
		return (SMSProvider) providers.getConvertedValue();
 	}
 
 	public String getFrom()
 	{
 		return from.getValue();
 	}
 
 	public String getSubject()
 	{
 		return subject.getValue();
 	}
 
 }
