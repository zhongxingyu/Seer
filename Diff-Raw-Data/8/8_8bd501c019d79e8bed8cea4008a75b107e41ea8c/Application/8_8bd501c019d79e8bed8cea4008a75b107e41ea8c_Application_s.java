 package gatech.emailtodolist;
 
 import javax.mail.Folder;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.Store;
 import javax.mail.internet.MimeMessage;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JPanel;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.RowSpec;
 import com.jgoodies.forms.factories.FormFactory;
 import javax.swing.JTextField;
 import java.awt.Font;
 import javax.swing.JPasswordField;
 import javax.swing.JButton;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Properties;
 import javax.swing.JSplitPane;
 import javax.swing.BoxLayout;
 import javax.swing.JSeparator;
 import javax.swing.SwingConstants;
 import java.awt.Color;
 import javax.swing.JTextPane;
 import java.awt.FlowLayout;
 import javax.swing.JScrollBar;
 import javax.swing.JScrollPane;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.JEditorPane;
 import javax.swing.text.html.HTMLEditorKit;
 import javax.swing.text.html.StyleSheet;
 import javax.swing.JRadioButton;
 import javax.swing.ButtonGroup;
 
 @SuppressWarnings("serial")
 public class Application extends JFrame{
 	private JTextField txtEmail;
 	private JPasswordField txtPassword;
 	private static Settings settings;
 	private static JEditorPane dtrToDo = new JEditorPane();
 	private static JRadioButton rdoDone = new JRadioButton("Done");
 	private static JRadioButton rdoWorking = new JRadioButton("Working on it");
 	private static JRadioButton rdoHelp = new JRadioButton("Need help");
 	private static JButton btnSubmit = new JButton("Submit");
 	static ArrayList<ToDoList> toDoListArray = new ArrayList<ToDoList>();
 	static ArrayList<JTextPane> visualTodos = new ArrayList<JTextPane>();
 	
 	/**
 	 * @param args
 	 * @throws Exception 
 	 */
 	public static void main(String[] args) throws Exception {
 		new Application();
 	}
 	
 	public Application()
 	{
 		setTitle("E-ToDo");
 		this.setSize(720, 470);
 		getContentPane().setLayout(null);
 		
 		final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
 		tabbedPane.setBounds(10, 11, 684, 409);
 		getContentPane().add(tabbedPane);
 		
 		final JPanel pnlLists = new JPanel();
 		tabbedPane.addTab("Lists", null, pnlLists, null);
 		pnlLists.setLayout(null);
 		
 		final JPanel pnlTodos = new JPanel();
 		JScrollPane pnlScroll = new JScrollPane(pnlTodos);
 		pnlScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 		pnlScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 		pnlTodos.setLayout(null);
 		
 		pnlScroll.setBounds(0, 0, 198, 370);
 		pnlLists.add(pnlScroll);
 		
 		final JButton btnRefresh = new JButton("Refresh");
 		btnRefresh.setBounds(10, 5, 159, 23);
 		pnlTodos.add(btnRefresh);
 		
 		dtrToDo.setText("No ToDo selected");
 		dtrToDo.setEditable(false);
 		HTMLEditorKit kit = new HTMLEditorKit();
 		StyleSheet styleSheet = kit.getStyleSheet();
 		styleSheet.addRule("h2 {text-decoration: underline;}");
 
 		dtrToDo.setEditorKit(kit);
 		dtrToDo.setBounds(203, 0, 476, 328);
 		pnlLists.add(dtrToDo);
 		
 		btnRefresh.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				try 
 				{
 					grabEmails();
 					populateVisualTodos();
 					drawTodos(pnlTodos, btnRefresh);
 				} 
 				catch (Exception e) 
 				{
 					JOptionPane.showMessageDialog(getContentPane(), e.getMessage(), "Error fetching emails!", JOptionPane.ERROR_MESSAGE);
 				}
 			}
 		});
 		btnRefresh.setBackground(Color.GRAY);
 		
 		rdoDone.setEnabled(false);
 		buttonGroup.add(rdoDone);
 		rdoDone.setBounds(204, 347, 54, 23);
 		pnlLists.add(rdoDone);
 		
 		rdoWorking.setEnabled(false);
 		buttonGroup.add(rdoWorking);
 		rdoWorking.setBounds(283, 347, 104, 23);
 		pnlLists.add(rdoWorking);
 		
 		rdoHelp.setEnabled(false);
 		buttonGroup.add(rdoHelp);
 		rdoHelp.setBounds(406, 347, 109, 23);
 		pnlLists.add(rdoHelp);
 		
 		btnSubmit.setEnabled(false);
 		btnSubmit.setBounds(550, 347, 89, 23);
 		pnlLists.add(btnSubmit);
 		
 		JPanel pnlSettings = new JPanel();
 		tabbedPane.addTab("Settings", null, pnlSettings, null);
 		pnlSettings.setLayout(new FormLayout(new ColumnSpec[] {
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("default:grow"),},
 			new RowSpec[] {
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,}));
 		
 		JLabel lblEmail = new JLabel("Email Address:");
 		lblEmail.setFont(new Font("Tahoma", Font.PLAIN, 24));
 		pnlSettings.add(lblEmail, "4, 4");
 		
 		txtEmail = new JTextField();
 		txtEmail.setFont(new Font("Tahoma", Font.PLAIN, 24));
 		pnlSettings.add(txtEmail, "8, 4, fill, default");
 		txtEmail.setColumns(10);
 		
 		JLabel lblPassword = new JLabel("Password:");
 		lblPassword.setFont(new Font("Tahoma", Font.PLAIN, 24));
 		pnlSettings.add(lblPassword, "4, 8, left, bottom");
 		
 		txtPassword = new JPasswordField();
 		txtPassword.setFont(new Font("Tahoma", Font.PLAIN, 24));
 		pnlSettings.add(txtPassword, "8, 8, fill, default");
 		
 		//attempt to load settings
 		if( !loadSettings() ) tabbedPane.setSelectedComponent(pnlSettings);
 		
 		JButton btnSaveSettings = new JButton("Save Settings");
 		btnSaveSettings.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				try
 				{
 					saveSettings();
 					grabEmails();
 					populateVisualTodos();
 					drawTodos(pnlTodos, btnRefresh);
 					tabbedPane.setSelectedComponent(pnlLists);					
 				}
 				catch(Exception e)
 				{
 					JOptionPane.showMessageDialog(getContentPane(), e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
 				}
 			}
 		});
 		
 		btnSaveSettings.setFont(new Font("Tahoma", Font.PLAIN, 24));		
 		pnlSettings.add(btnSaveSettings, "4, 12");
 		
 		if(toDoListArray.size() == 0)
 		{
 			try 
 			{
 				grabEmails();
 				populateVisualTodos();
 				drawTodos(pnlTodos, btnRefresh);
 			} 
 			catch (Exception e) 
 			{
 				JOptionPane.showMessageDialog(getContentPane(), e.getMessage(), "Error fetching emails!", JOptionPane.ERROR_MESSAGE);
 			}
 		}
 		
 		setVisible(true);
 	}
 	
 	private void grabEmails() throws Exception{
 		toDoListArray.clear();
 		String host = "imap.gmail.com";
 		String user = settings.email();
 		String password = settings.plainTextPassword(); 
 
 		// Get system properties 
 		Properties properties = System.getProperties(); 
 
 		// Get the default Session object.
 		Session session = Session.getDefaultInstance(properties);
 
 		// Get a Store object that implements the specified protocol.
 		Store store = session.getStore("imaps");
 
 		//Connect to the current host using the specified username and password.
 		store.connect(host, user, password);
 
 		//Create a Folder object corresponding to the given name.
 		Folder folder = store.getFolder("inbox");
 
 		// Open the Folder.
 		folder.open(Folder.READ_ONLY);
 
 		Message[] messages = folder.getMessages();
 
 		// Display message.
 		for (int i = messages.length - 1; i > 0; i--) {
 			
 			String[] subjectContents;
 			
 			if (messages[i].getSubject() != null){
 				subjectContents = messages[i].getSubject().split(" ");
 				
 				if (subjectContents[0].equals("ToDo:")){
 					
 					String date = "" + messages[i].getSentDate();
 					String from = "" + messages[i].getFrom()[0];
 					String subject = "" + messages[i].getSubject().split(" ", 2)[1];
 					String contents = "" + ((MimeMessage)messages[i]).getContent();
 					
 					ToDoList toDoList = new ToDoList(from, date, subject);
 					
 					String[] messageContents = contents.split("\n");
 					
 					for (int j = 0; j <= messageContents.length - 1; j++){
 						toDoList.toDoItems.add(messageContents[j]);
 					}
 					
 					toDoListArray.add(toDoList);					
 					
 //					System.out.println("------------ Message " + (i + 1) + " ------------");
 //					System.out.println("SentDate : " + messages[i].getSentDate());
 //					System.out.println("From : " + messages[i].getFrom()[0]);
 //					System.out.println("Subject : " + messages[i].getSubject());
 //					System.out.print("Message : \n" + ((MimeMessage)messages[i]).getContent());
 //		
 //					System.out.println();
 				}
 			}						
 		}
 
 		folder.close(true);
 		store.close();
 	}
 	
 	private void populateVisualTodos()
 	{
 		int start = 31;
 		visualTodos.clear();
 		if( toDoListArray.size() == 0 )
 		{
 			JTextPane txtDefault = new JTextPane();
 			txtDefault.setEditable(false);
 			txtDefault.setBounds(0, start, 177, 44);
 			txtDefault.setText("No todos yet.\nTry refreshing...");
 			visualTodos.add(txtDefault);
 		}
 		else
 		{
 			for( ToDoList tl : toDoListArray )
 			{
 				JTextPane txtPane = new JTextPane();
 				txtPane.setEditable(false);
 				txtPane.setBounds(0, start, 177, 44);
 				txtPane.setText(tl.sender.split("<")[0] + "\n  " + tl.title);
 				visualTodos.add(txtPane);
 				start = start + 46;
 			}
 		}
 	}
 	
 	static int count;
 	private final ButtonGroup buttonGroup = new ButtonGroup();
 	private void drawTodos(JPanel pnlTodos, JButton btnRefresh)
 	{
 		pnlTodos.removeAll();
 		pnlTodos.add(btnRefresh);
 		count = 0;
 		for( JTextPane todo : visualTodos)
 		{
 			final int i = count;
 			todo.addMouseListener(new MouseAdapter() {
 				public void mouseClicked(MouseEvent event)
 				{
 					ToDoList data = toDoListArray.get(i);
					String output = "<h1>" + data.sender + "</h1><h2>" + data.title + "</h2>" +
							"<ol>";
 					for( String li : data.toDoItems )
 					{
						output += "<li>" + li + "</li>";
 					}
					output += "</ol>";
 					dtrToDo.setText(output);
 					rdoHelp.setEnabled(true);
 					rdoWorking.setEnabled(true);
 					rdoWorking.setSelected(true);
 					rdoDone.setEnabled(true);
 					btnSubmit.setEnabled(true);
 				}
 			});
 			pnlTodos.add(todo);
 			count++;
 		}
 	}
 	
 	private void saveSettings() throws IOException, Exception
 	{
 		String email = txtEmail.getText();
 		char[] password = txtPassword.getPassword();
 		
 		if(email.length() == 0) throw new Exception("Please input an email address");
 		if(password.length < 1) throw new Exception("Please input a password");
 		
 		//TODO RegEx matching on email
 		
 		if(this.settings == null)
 		{
 			this.settings = new Settings(email, password);
 		}
 		else
 		{
 			this.settings.email(txtEmail.getText());
 			this.settings.password(txtPassword.getPassword());
 		}
 		
 		this.settings.save();
 		this.loadSettings();
 	}
 	
 	private boolean loadSettings()
 	{
 		try
 		{
 			settings = new Settings();
 			txtEmail.setText(settings.email());
 			txtPassword.setText(settings.password());
 			return true;
 		}
 		catch(Exception e)
 		{
 			txtEmail.setText("");
 			txtPassword.setText("");
 			return false;
 		}
 	}
 }
