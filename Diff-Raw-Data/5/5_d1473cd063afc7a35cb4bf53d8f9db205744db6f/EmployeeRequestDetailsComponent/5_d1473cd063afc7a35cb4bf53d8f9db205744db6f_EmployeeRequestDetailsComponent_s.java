 package icm.gui.employee;
 
 
 import icm.client.ICMClient;
 import icm.dao.Employee;
 import icm.dao.EmployeeType;
 import icm.dao.Request;
 import icm.dao.RequestStatus;
 import icm.gui.StageListingComponent;
 import icm.intent.AssignSupervisorIntent;
 import icm.intent.BrowseEmployeesIntent;
 import icm.intent.ResumeIntent;
 import icm.intent.SuspendIntent;
 
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextPane;
 import javax.swing.SwingUtilities;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 @SuppressWarnings("serial")
 public class EmployeeRequestDetailsComponent extends JComponent implements Observer
 {
 	private JList<Request> rqst;
 	
 	public EmployeeRequestDetailsComponent(final Employee employee, final JList<Request> requests, final ICMClient client) 
 	{
 		
 		rqst=requests;
 		JLabel lblNewLabel = new JLabel("RequestID:");
 		lblNewLabel.setBounds(10, 11, 94, 20);
 		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 13));
 		
 		JLabel lblSubmitted = new JLabel("Submitted:");
 		lblSubmitted.setBounds(10, 39, 94, 20);
 		lblSubmitted.setFont(new Font("Tahoma", Font.PLAIN, 13));
 		
 		JLabel lblCurrentState = new JLabel("Current State:");
 		lblCurrentState.setBounds(10, 79, 94, 16);
 		lblCurrentState.setFont(new Font("Tahoma", Font.PLAIN, 13));
 		
 		JLabel lblRequest = new JLabel("Request:");
 		lblRequest.setFont(new Font("Tahoma", Font.PLAIN, 13));
 		lblRequest.setBounds(10, 194, 94, 17);
 		
 		JLabel lblComments = new JLabel("Comments:");
 		lblComments.setFont(new Font("Tahoma", Font.PLAIN, 13));
 		lblComments.setBounds(10, 323, 94, 17);
 		
 		JLabel lblSupervisor = new JLabel("Supervisor:");
 		lblSupervisor.setBounds(279, 17, 79, 16);
 		lblSupervisor.setFont(new Font("Tahoma", Font.PLAIN, 13));
 		
 		JLabel lblStatus = new JLabel("Status:");
 		lblStatus.setBounds(287, 45, 71, 16);
 		lblStatus.setFont(new Font("Tahoma", Font.PLAIN, 13));
 		
 		final JTextPane reqIDtext = new JTextPane();
 		reqIDtext.setBounds(108, 11, 80, 22);
 		reqIDtext.setFont(new Font("Tahoma", Font.PLAIN, 13));
 		reqIDtext.setEditable(false);
 		
 		final JTextPane submitText = new JTextPane();
 		submitText.setBounds(108, 39, 161, 22);
 		submitText.setFont(new Font("Tahoma", Font.PLAIN, 13));
 		submitText.setEditable(false);
 		
 		final JTextArea curr_stateText = new JTextArea();
 		curr_stateText.setFont(new Font("Courier New", Font.PLAIN, 13));
 		curr_stateText.setLineWrap(true);
 		curr_stateText.setEditable(false);
 		
 		JScrollPane currTextScrollpane=new JScrollPane(curr_stateText,
 				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
 				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		currTextScrollpane.setBounds(108, 79, 263, 89);
 		
 		final JTextArea requestText = new JTextArea();
 		requestText.setFont(new Font("Courier New", Font.PLAIN, 13));
 		requestText.setLineWrap(true);
 		requestText.setEditable(false);
 		
 		JScrollPane reqTextScrollpane=new JScrollPane(requestText,
 				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
 				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		reqTextScrollpane.setBounds(108, 194, 263, 103);
 		
 		final JTextArea commentText = new JTextArea();
 		commentText.setFont(new Font("Courier New", Font.PLAIN, 13));
 		commentText.setLineWrap(true);
 		commentText.setEditable(false);
 		
 		JScrollPane commentTextScrollpane=new JScrollPane(commentText,
 				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
 				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		commentTextScrollpane.setBounds(108, 323, 263, 70);
 		
 		final JTextPane supervisorText = new JTextPane();
 		supervisorText.setBounds(368, 11, 106, 22);
 		supervisorText.setFont(new Font("Tahoma", Font.PLAIN, 13));
 		supervisorText.setEditable(false);
 		
 		final JTextPane statusText = new JTextPane();
 		statusText.setBounds(368, 39, 106, 22);
 		statusText.setFont(new Font("Tahoma", Font.PLAIN, 13));
 		statusText.setEditable(false);
 		
 		final JButton assignButton = new JButton("Assign");
 		assignButton.setBounds(480, 11, 90, 20);
 		assignButton.setFont(new Font("Tahoma", Font.PLAIN, 13));
 		assignButton.setEnabled(false);
 		assignButton.addActionListener(new ActionListener()
 		{
 		  	public void actionPerformed(ActionEvent e) 
 		  	{
 				AssignEmployeeDialog dialog = new AssignEmployeeDialog(SwingUtilities.windowForComponent(EmployeeRequestDetailsComponent.this));
 				try
 				{
 					BrowseEmployeesIntent browse = new BrowseEmployeesIntent();
 					EmployeeType filter[] = {EmployeeType.ENGINEER};
 					browse.setFilter(filter);
 					client.registerAndSend(browse, dialog);
 					
 					dialog.setVisible(true);
 					if( dialog.getConfirmed() && dialog.getValue()!=null && requests.getSelectedValue()!=null)
 					{
 			  			AssignSupervisorIntent intent = new AssignSupervisorIntent();
 			  			intent.setCandidateid(dialog.getValue().getEmployeeId());
 			  			intent.setRequestid(requests.getSelectedValue().getRequestID());
 			  			
 			  			try 
 			  			{
 							client.registerAndSend(intent,EmployeeRequestDetailsComponent.this);
 						} 
 			  			catch (IOException e1) {e1.printStackTrace();}
 					}
 				} 
 				catch (IOException e2){e2.printStackTrace();}
 		  	}
 		});
 		
 		final JButton suspendButton = new JButton("Suspend");
 		suspendButton.setBounds(480, 39, 90, 20);
 		suspendButton.setFont(new Font("Tahoma", Font.PLAIN, 13));
 		suspendButton.setEnabled(false);
 		suspendButton.addActionListener(new ActionListener()
 		{ 
 		  	public void actionPerformed(ActionEvent e) 
 		  	{
 		  		try 
 		  		{
 			  		SuspendIntent intent = new SuspendIntent();
 			  		intent.setRequestID(requests.getSelectedValue().getRequestID());
 					client.registerAndSend(intent, EmployeeRequestDetailsComponent.this);
					suspendButton.setVisible(false);
 				} 
 		  		catch (IOException e1) {e1.printStackTrace();}
 		  	} 
 		});
 		
 		final JButton resumeButton = new JButton("Resume");
 		resumeButton.setBounds(480, 67, 90, 20);
 		resumeButton.setFont(new Font("Tahoma", Font.PLAIN, 13));
 		resumeButton.setEnabled(false);
 		resumeButton.addActionListener(new ActionListener()
 		{
 		  	public void actionPerformed(ActionEvent e) 
 		  	{
 		  		try 
 		  		{
 			  		ResumeIntent intent = new ResumeIntent();
 			  		intent.setRequestID(requests.getSelectedValue().getRequestID());
 			  		client.registerAndSend(intent, EmployeeRequestDetailsComponent.this);
			  		resumeButton.setVisible(false);
 				} 
 		  		catch (IOException e1) {e1.printStackTrace();}
 		  	}
 		});
 		StageListingComponent stageListComp = new StageListingComponent(client,requests,employee);
 		stageListComp.setBounds(580, 11, 631, 396);
 		setLayout(null);
 		add(lblCurrentState);
 		add(currTextScrollpane);
 		add(lblRequest);
 		add(reqTextScrollpane);
 		add(resumeButton);
 		add(lblNewLabel);
 		add(reqIDtext);
 		add(lblSupervisor);
 		add(lblSubmitted);
 		add(submitText);
 		add(lblStatus);
 		add(statusText);
 		add(suspendButton);
 		add(supervisorText);
 		add(assignButton);
 		add(lblComments);
 		add(commentTextScrollpane);
 		add(stageListComp);
 		
 		ListSelectionListener listener = new ListSelectionListener() 
 		{
 			@Override
 			public void valueChanged(ListSelectionEvent listSelectionEvent) 
 			{
 				if(requests.getSelectedValue()!=null)
 				{
 					assignButton.setEnabled(false);
 					resumeButton.setEnabled(false);
 					suspendButton.setEnabled(false);
 					
 					Request request = requests.getSelectedValue();
 					if(listSelectionEvent.getValueIsAdjusting()==false)//fired twice,still need fix
 					{
 						
 						submitText.setText((new java.util.Date(request.getRecievedAt()).toString()));
 						statusText.setText(request.getStatus().toString());
 						
 						if( request.getSupervisor()!=null )
 							supervisorText.setText(request.getSupervisor().getName()+" "+request.getSupervisor().getSurname());
 						else
 							supervisorText.setText("");
 						
 						if(employee.getType()==EmployeeType.DIRECTOR)
 						{
 							assignButton.setEnabled(true);
 							
 							if(request.getStatus()==RequestStatus.SUSPENDED)
 							{
 								resumeButton.setEnabled(true);	
 							}
 						}
 						
 						if( request.getSupervisor()!=null)
 							if( request.getSupervisor().getEmployeeId()==employee.getEmployeeId() && request.getStatus()!=RequestStatus.SUSPENDED)
 								suspendButton.setEnabled(true);
 						
 						reqIDtext.setText(Integer.toString(request.getRequestID()));
 						curr_stateText.setText(request.getCurrentState());
 						requestText.setText(request.getRequest());
 						commentText.setText(request.getComments());
 						
 					}
 				}
 			}
 				
 		};
 		
 		requests.addListSelectionListener(listener);
 	}
 	
 	/**internal use*/
 	private boolean opSuccess=false;
 	
 	@Override
 	public void update(Observable arg0, Object arg1 ) 
 	{
 		if( arg1 instanceof Boolean )
 		{
 			opSuccess=(Boolean)arg1;
 		}
 		if( arg1 instanceof String )
 		{
 			if( opSuccess )
 				JOptionPane.showMessageDialog(this,(String)arg1, "Operation completed", JOptionPane.INFORMATION_MESSAGE);
 			else
 				JOptionPane.showMessageDialog(this,(String)arg1, "Operation failed", JOptionPane.ERROR_MESSAGE);
 		}
 		else if( arg1 instanceof Request )
 		{
 			DefaultListModel<Request> model = (DefaultListModel<Request>) rqst.getModel();
 			model.addElement((Request)arg1);
 			rqst.setSelectedValue((Request)arg1, true);
 		}
 	}
 }
