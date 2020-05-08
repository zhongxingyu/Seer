 package views.timesheet;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 import javax.swing.JTabbedPane;
 import javax.swing.JButton;
 import java.awt.*;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import javax.swing.BorderFactory;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JOptionPane;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JLabel;
 import javax.swing.JComboBox;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingWorker;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 import javax.swing.border.LineBorder;
 import javax.swing.JList;
 
 import models.Client;
 import models.TimeSheet;
 import models.User;
 import models.UserPermission;
 
 import views.SystemUI;
 import views.dataentry.CreateDataEntryUI;
 
 import utils.Logging;
 import utils.UserSession;
 
 import controllers.ClientCtrl;
 import controllers.UserCtrl;
 import controllers.UserPermissionCtrl;
 
 public class CreateTimeSheetUI
 {
     private static JFrame _frame;
     private static CreateTimeSheetUI _instance;
 	private JPanel contentPane;
 	private JList<String> lstGroup;
 	private JList<String> lstUser;
 	private JComboBox<String> drpClients;
 	private DefaultComboBoxModel<String> mdlClient;
 	private JPanel pnlTimeSheet;
 	private JPanel pnlPermission;
 	private JTextPane txtNotes;
     private JTextField txtCaseId;
     
 	// Controllers
 	private ClientCtrl _clientCtrl;
 	private UserCtrl _userCtrl;
 	private UserPermissionCtrl _userPermissionCtrl;
 	
     public static JFrame createWindow()
     {
         if(_instance == null)
             _instance = new CreateTimeSheetUI();
 
         return _frame;
     }
 	
 	private CreateTimeSheetUI()
 	{
         _clientCtrl = new ClientCtrl();
         _userCtrl = new UserCtrl();
         _userPermissionCtrl = new UserPermissionCtrl();
         
 		_frame = new JFrame();
 		_frame.setIconImage(Toolkit.getDefaultToolkit().getImage(SystemUI.class.getResource("/new_timesheet.png")));
 		_frame.setTitle("Ny time-sag");
 		_frame.setVisible(true);
 		_frame.setResizable(false);
 		_frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		_frame.setSize(new Dimension(420,400));
 		_frame.setLocationRelativeTo(null);
 		_frame.addWindowListener(new WindowAdapter()
         {
             public void windowClosing(WindowEvent e)
             {
                 _instance = null;
                 _frame.dispose();
             }
         });
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		
 		_frame.setContentPane(contentPane);
 		contentPane.setLayout(null);
 
 		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
 		tabbedPane.setBounds(10, 58, 392, 262);
 		tabbedPane.setFont(new Font("Dialog", Font.PLAIN, 12));
 		contentPane.add(tabbedPane);
 		
 		JButton btnNewButton = new JButton("Annuller");
 		btnNewButton.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 _instance = null;
                 _frame.dispose();
             }
         });
 		btnNewButton.setBounds(276, 331, 125, 23);
 		contentPane.add(btnNewButton);
 		
 		JButton btnNext = new JButton("N\u00E6ste (1/2)");
 		btnNext.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 createTimeSheet();
             }
         });
 		btnNext.setBounds(139, 331, 125, 23);
 		contentPane.add(btnNext);
 		
 		JSeparator separator = new JSeparator();
 		separator.setBounds(10, 45, 392, 2);
 		contentPane.add(separator);
 		
 		JLabel lblKlient = new JLabel("Klient");
 		lblKlient.setFont(new Font("Dialog", Font.PLAIN, 12));
 		lblKlient.setBounds(10, 12, 53, 23);
 		contentPane.add(lblKlient);
 		
 		drpClients = new JComboBox<String>();
 		drpClients.setBounds(55, 12, 347, 22);
 		drpClients.setFont(new Font("Dialog", Font.PLAIN, 12));
 		drpClients.setBackground(Color.WHITE);
 		contentPane.add(drpClients);
 		mdlClient = new DefaultComboBoxModel<String>(populateClientList());
         drpClients.setModel(mdlClient);
 
 		// pane1 start
         pnlTimeSheet = new JPanel();
         pnlTimeSheet.setLayout(null);
 		
 		JLabel lblCaseId = new JLabel("Sags Nr.:");
 		lblCaseId.setFont(new Font("Dialog", Font.PLAIN, 12));
 		lblCaseId.setBounds(5, 11, 60, 20);
 		pnlTimeSheet.add(lblCaseId);
 		
 		JLabel lblNote = new JLabel("Note:");
 		lblNote.setFont(new Font("Dialog", Font.PLAIN, 12));
 		lblNote.setBounds(5, 43, 60, 20);
 		pnlTimeSheet.add(lblNote);
 		
 		// for display ClientId
 		txtCaseId = new JTextField();
 		txtCaseId.setBounds(67, 12, 310, 20);
 		pnlTimeSheet.add(txtCaseId);
 		
 		// for notes
 		txtNotes = new JTextPane();
 		txtNotes.setBounds(67, 44, 310, 182);
 		pnlTimeSheet.add(txtNotes);
         txtNotes.setBorder(new LineBorder(Color.LIGHT_GRAY));
 		
 		tabbedPane.add("Time-Sag", pnlTimeSheet);
 		
 		pnlPermission = new JPanel();
 		pnlPermission.setLayout(null);
 				
 		tabbedPane.add("Rettigheder", pnlPermission);
 		
 		JPanel pnlGroup = new JPanel();
 		pnlGroup.setBounds(10, 11, 179, 212);
 		pnlGroup.setBorder(BorderFactory.createTitledBorder(null, "Gruppe", 0, 0, new Font("Dialog", Font.PLAIN, 12)));
 		pnlGroup.setFont(new Font("Dialog", Font.PLAIN, 12));
 		pnlPermission.add(pnlGroup);
 		pnlGroup.setLayout(null);
 		
 		lstGroup = new JList<String>();		
 		lstGroup.setListData(populateUserGroups());
 		lstGroup.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		lstGroup.setFont(new Font("Dialog", Font.PLAIN, 12));
 		lstGroup.setBorder(new LineBorder(Color.LIGHT_GRAY));
 		lstGroup.setBounds(13, 17, 153, 182);
 		JScrollPane groupListScroll = new JScrollPane(lstGroup);
 		pnlGroup.add(groupListScroll);
 		pnlGroup.add(lstGroup);
 		
 		JPanel pnlUser = new JPanel();
 		pnlUser.setBorder(BorderFactory.createTitledBorder(null, "Bruger", 0, 0, new Font("Dialog", Font.PLAIN, 12)));
 		pnlUser.setBounds(197, 11, 179, 212);
 		pnlPermission.add(pnlUser);
 		pnlUser.setLayout(null);
 		
 		lstUser = new JList<String>();		
 		lstUser.setListData(populateUserList());
 		lstUser.setFont(new Font("Dialog", Font.PLAIN, 12));
 		lstUser.setBorder(new LineBorder(Color.LIGHT_GRAY));
 		lstUser.setBounds(13, 17, 153, 182);
 		JScrollPane userListScroll = new JScrollPane(lstUser);
 		pnlUser.add(userListScroll);
 		pnlUser.add(lstUser);
 	}
 
 	public void createTimeSheet()
 	{
 		String caseId = txtCaseId.getSelectedText();
         User user = UserSession.getLoggedInUser();
         long clientPhone = Long.parseLong(drpClients.getSelectedItem().toString().substring(drpClients.getSelectedItem().toString().indexOf("(") + 1,
                 drpClients.getSelectedItem().toString().indexOf(")")));
 
         Client client = null;
         try
         {
             client = _clientCtrl.getClientByPhone(clientPhone);
         }
         catch (Exception e)
         {
             e.printStackTrace();
         }
         String note = txtNotes.getText();
         Calendar cal = Calendar.getInstance();
         Date creationDate = cal.getTime();
         Date editedDate =  cal.getTime();
         
 		TimeSheet ts = new TimeSheet(caseId, user, client, note, creationDate, editedDate);
 		CreateDataEntryUI.createWindow(ts);
 	}
 	
 	private String[] populateClientList()
 	{
 		try
 		{
 			return new PopulateClientList().doInBackground();
 		}
 		catch(Exception ex)
 		{
 			JOptionPane.showMessageDialog(null, Logging.handleException(ex, 99), "Fejl!", JOptionPane.ERROR_MESSAGE);
 		}
 		
 		return null;
 	}
 	
 	private String[] populateUserList()
 	{
 		try
 		{
 			return new PopulateUserList().doInBackground();
 		}
 		catch(Exception ex)
 		{
 			JOptionPane.showMessageDialog(null, Logging.handleException(ex, 99), "Fejl!", JOptionPane.ERROR_MESSAGE);
 		}
 		
 		return null;
 	}
 	
 	private String[] populateUserGroups()
 	{
 		try
 		{
 			return new PopulateUserGroups().doInBackground();
 		}
 		catch(Exception ex)
 		{
 			JOptionPane.showMessageDialog(null, Logging.handleException(ex, 99), "Fejl!", JOptionPane.ERROR_MESSAGE);
 		}
 		
 		return null;
 	}
 	
 	class PopulateClientList extends SwingWorker<String[], Integer>
 	{
 		@Override
 		protected String[] doInBackground() throws Exception
 		{
 			ArrayList<Client> clients;
 			try
 			{
 				clients = _clientCtrl.getAllClients();
 				String[] clientNames = new String[clients.size()];
 				for(int i = 0; i < clients.size(); i++)
 					clientNames[i] = clients.get(i).getName() + " (" + clients.get(i).getPhoneNo() +")";
 				
 				return clientNames;
 			}
 			catch(Exception ex)
 			{
 				JOptionPane.showMessageDialog(null, Logging.handleException(ex, 0), "Fejl", JOptionPane.ERROR_MESSAGE);
 			}
 			
 			return null;
 		}
 	}
 	
 	class PopulateUserList extends SwingWorker<String[], Integer>
 	{
 		@Override
 		protected String[] doInBackground() throws Exception
 		{
 			ArrayList<User> users;
 			try
 			{
 				users = _userCtrl.getAllUsers();
 				String[] userNames = new String[users.size()];
 				for (int i = 0; i < users.size(); i++)
 					userNames[i] = users.get(i).getFirstName() + " " + users.get(i).getLastName();
 				
 				return userNames;
 					
 			}
 			catch(Exception ex)
 			{
 				JOptionPane.showMessageDialog(null, Logging.handleException(ex, 0), "Fejl", JOptionPane.ERROR_MESSAGE);
 			}
 			
 			return null;
 		}
 	}
 	
 	class PopulateUserGroups extends SwingWorker<String[], Integer>
 	{
 		@Override
 		protected String[] doInBackground() throws Exception
 		{
 			ArrayList<UserPermission> permissions;
 			try
 			{
 				permissions = _userPermissionCtrl.getAllRoles();
 				String[] permissionTitles = new String[permissions.size()];
 				for (int i = 0; i < permissions.size(); i++)
 					permissionTitles[i] = permissions.get(i).getUserRole();
 				
 				return permissionTitles;
 			}
 			catch(Exception ex)
 			{
 				JOptionPane.showMessageDialog(null, Logging.handleException(ex, 0), "Fejl", JOptionPane.ERROR_MESSAGE);
 			}
 			
 			return null;
 		}
 	}
 }
