 /*
  * Copyright (c) Novedia Group 2012.
  *
  *    This file is part of Hubiquitus
  *
  *    Permission is hereby granted, free of charge, to any person obtaining a copy
  *    of this software and associated documentation files (the "Software"), to deal
  *    in the Software without restriction, including without limitation the rights
  *    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
  *    of the Software, and to permit persons to whom the Software is furnished to do so,
  *    subject to the following conditions:
  *
  *    The above copyright notice and this permission notice shall be included in all copies
  *    or substantial portions of the Software.
  *
  *    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
  *    INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
  *    PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
  *    FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
  *    ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  *
  *    You should have received a copy of the MIT License along with Hubiquitus.
  *    If not, see <http://opensource.org/licenses/mit-license.php>.
  */
 
 
 package main;
 
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 
 import org.hubiquitus.hapi.client.HClient;
 import org.hubiquitus.hapi.client.HMessageDelegate;
 import org.hubiquitus.hapi.client.HStatusDelegate;
 import org.hubiquitus.hapi.exceptions.MissingAttrException;
 import org.hubiquitus.hapi.hStructures.ConnectionError;
 import org.hubiquitus.hapi.hStructures.HArrayOfValue;
 import org.hubiquitus.hapi.hStructures.HCondition;
 import org.hubiquitus.hapi.hStructures.HMessage;
 import org.hubiquitus.hapi.hStructures.HMessageOptions;
 import org.hubiquitus.hapi.hStructures.HOptions;
 import org.hubiquitus.hapi.hStructures.HStatus;
 import org.hubiquitus.hapi.hStructures.OperandNames;
 import org.hubiquitus.hapi.transport.socketio.ConnectedCallback;
 import org.hubiquitus.hapi.transport.socketio.HAuthCallback;
 import org.joda.time.DateTime;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  * 
  * @author speed
  * @version 0.3 The panel for this example
  */
 
 @SuppressWarnings("serial")
 public class MainPanel extends JPanel implements HStatusDelegate,
 		HMessageDelegate {
 	private HClient client;
 	final MainPanel outerClass = this;
 
 	private HOptions option = new HOptions();
 
 	private JTextField usernameField = new JTextField("u1@localhost");
 	private JTextField passwordField = new JTextField("u1");
 	private JTextField endPointField = new JTextField("http://localhost:8080");
 	private JTextField actorField = new JTextField("#test@localhost");
 	private JTextField messageField = new JTextField("test");
 	private JTextField nbLastMessagesField = new JTextField("");
 	private JTextField convidField = new JTextField("");
 	private JTextField convstateField = new JTextField("");
 	private JTextField relevantField = new JTextField("");
 	private JTextField timeoutField = new JTextField("");
 	private JTextField filterNameField = new JTextField("");
 	private JTextField filterAttrField = new JTextField("");
 	private JTextField filterValueField = new JTextField("");
 
 	private JButton connectButton = new JButton("Connect");
 	private JButton disconnectButton = new JButton("Disconnect");
 	private JButton sendButton = new JButton("send");
 	private JButton createChannelButton = new JButton("createChannel");
 	private JButton subscribeButton = new JButton("subscribe");
 	private JButton unsubscribeButton = new JButton("unsubscribe");
 	// private JButton publishButton = new JButton("publish");
 	private JButton getLastMessagesButton = new JButton("getLstMsg");
 	private JButton getSubscriptionsButton = new JButton("getSubs");
 	private JButton getThreadButton = new JButton("getThread");
 	private JButton getThreadsButton = new JButton("getThreads");
 	private JButton pubConvStateButton = new JButton("pubConvState");
 	private JButton setFilterButton = new JButton("setFilter");
 	private JButton listFiltersButton = new JButton("listFilters");
 	private JButton unsetFilterButton = new JButton("unsetFilter");
 	private JButton getRelevantMessagesButton = new JButton(
 			"getRelevantMessages");
 	private JButton cleanButton = new JButton("Clean");
 
 	// private JRadioButton xmppRadioButton = new JRadioButton("XMPP");
 	private JRadioButton socketRadioButton = new JRadioButton("Socket IO");
 	private ButtonGroup buttonGroup = new ButtonGroup();
 
 	private JRadioButton persistentRadioButton = new JRadioButton("persistent");
 	private JRadioButton notPersistentRadioButton = new JRadioButton(
 			"not persistent");
 	private ButtonGroup persistentGroup = new ButtonGroup();
 
 	private JTextArea logArea = new JTextArea(20, 100);
 	private JTextArea statusArea = new JTextArea(1, 90);
 
 	public MainPanel() {
 		super();
 		initComponents();
 		initListeners();
 		client = new HClient();
 		client.onStatus(this);
 		client.onMessage(this);
 	}
 
 	/**
 	 * Initialization of all the component
 	 */
 	public void initComponents() {
 		BorderLayout layout = new BorderLayout();
 		this.setLayout(layout);
 
 		buttonGroup.add(socketRadioButton);
 		socketRadioButton.setSelected(true);
 
 		persistentGroup.add(persistentRadioButton);
 		persistentGroup.add(notPersistentRadioButton);
 		notPersistentRadioButton.setSelected(true);
 
 		// Initialization of Labels,TextFields and RadioButtons
 		JPanel paramsPanel = new JPanel();
 		GridLayout paramsLayout = new GridLayout(16, 2);
 		paramsPanel.setLayout(paramsLayout);
 		paramsPanel.add(new JLabel("username"));
 		paramsPanel.add(usernameField);
 		paramsPanel.add(new JLabel("password"));
 		paramsPanel.add(passwordField);
 		paramsPanel.add(new JLabel("endPoint"));
 		paramsPanel.add(endPointField);
 		// paramsPanel.add(new JLabel("serverHost"));
 		// paramsPanel.add(serverHostField);
 		// paramsPanel.add(new JLabel("serverPort"));
 		// paramsPanel.add(serverPortField);
 		paramsPanel.add(new JLabel("Actor"));
 		paramsPanel.add(actorField);
 		paramsPanel.add(new JLabel("nbLastMessages"));
 		paramsPanel.add(nbLastMessagesField);
 		paramsPanel.add(new JLabel("Message"));
 		paramsPanel.add(messageField);
 		paramsPanel.add(new JLabel("convid"));
 		paramsPanel.add(convidField);
 		paramsPanel.add(new JLabel("status"));
 		paramsPanel.add(convstateField);
 		paramsPanel.add(new JLabel("relevant"));
 		paramsPanel.add(relevantField);
 		paramsPanel.add(new JLabel("timeout"));
 		paramsPanel.add(timeoutField);
 		paramsPanel.add(new JLabel("Filter Name"));
 		paramsPanel.add(filterNameField);
 		paramsPanel.add(new JLabel("Filter attr"));
 		paramsPanel.add(filterAttrField);
 		paramsPanel.add(new JLabel("Filter value"));
 		paramsPanel.add(filterValueField);
 		paramsPanel.add(persistentRadioButton);
 		paramsPanel.add(notPersistentRadioButton);
 		// paramsPanel.add(xmppRadioButton);
 		paramsPanel.add(socketRadioButton);
 
 		statusArea.setEditable(false);
 		paramsPanel.add(statusArea);
 
 		// Initialization of Buttons
 		JPanel controlsPanel = new JPanel();
 		GridLayout controlsLayout = new GridLayout(3, 7);
 		controlsPanel.setLayout(controlsLayout);
 		controlsPanel.add(connectButton);
 		controlsPanel.add(disconnectButton);
 		controlsPanel.add(sendButton);
 		controlsPanel.add(createChannelButton);
 		controlsPanel.add(subscribeButton);
 		controlsPanel.add(unsubscribeButton);
 		// controlsPanel.add(publishButton);
 		controlsPanel.add(getLastMessagesButton);
 		controlsPanel.add(getSubscriptionsButton);
 		controlsPanel.add(getThreadButton);
 		controlsPanel.add(getThreadsButton);
 		controlsPanel.add(pubConvStateButton);
 		controlsPanel.add(setFilterButton);
 		controlsPanel.add(listFiltersButton);
 		controlsPanel.add(unsetFilterButton);
 		controlsPanel.add(getRelevantMessagesButton);
 		controlsPanel.add(cleanButton);
 
 		// Initialization of the TextArea
 		JPanel consolePanel = new JPanel();
 		logArea.setEditable(false);
 		consolePanel.add(logArea);
 		JScrollPane txtScrol = new JScrollPane(logArea);
 		txtScrol.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		consolePanel.add(txtScrol);
 
 		// Add all in the layout
 		this.add(paramsPanel, BorderLayout.NORTH);
 		this.add(controlsPanel, BorderLayout.CENTER);
 		this.add(consolePanel, BorderLayout.SOUTH);
 	}
 
 	/**
 	 * Initialization of the listeners
 	 */
 	public void initListeners() {
 		connectButton.addMouseListener(new ConnectionButtonListener());
 		disconnectButton.addMouseListener(new DisconnectionButtonListener());
 		sendButton.addMouseListener(new SendButtonListener());
 		createChannelButton.addMouseListener(new CreateChannelButtonListener());
 		subscribeButton.addMouseListener(new SubscribeButtonListener());
 		unsubscribeButton.addMouseListener(new UnsubscribeButtonListener());
 		// publishButton.addMouseListener(new PublishButtonListener());
 		getLastMessagesButton
 				.addMouseListener(new GetLastMessagesButtonListener());
 		getSubscriptionsButton
 				.addMouseListener(new GetSubscriptionButtonListener());
 		getThreadButton.addMouseListener(new GetThreadButtonListener());
 		getThreadsButton.addMouseListener(new GetThreadsButtonListener());
 		pubConvStateButton.addMouseListener(new PubConvStateButtonListener());
 		setFilterButton.addMouseListener(new SetFilterListener());
 		listFiltersButton.addMouseListener(new ListFiltersListener());
 		unsetFilterButton.addMouseListener(new UnsetFilterListener());
 		getRelevantMessagesButton
 				.addMouseListener(new GetRelevantMessagesListener());
 		cleanButton.addMouseListener(new CleanButtonListener());
 
 	}
 
 	/**
 	 * Add a text to the TextArea
 	 * 
 	 * @param text
 	 */
 	public void addTextArea(String text) {
 		this.logArea.setText(text);
 	}
 
 	/**
 	 * Clean the TextArea
 	 */
 	public void cleanTextArea() {
 		this.logArea.setText("clean");
 	}
 
 	/**
 	 * Change the status
 	 * 
 	 * @param text
 	 */
 	public void setStatusArea(String text) {
 		this.statusArea.setText(text);
 	}
 
 	
 	
 	class ACB implements HAuthCallback{
 			
 			private String login;
 			private String password;
 			
 			public ACB(String l, String p){
 				this.login = l;
 				this.password = p;
 			}
 			@Override
 			public void authCb(String username, ConnectedCallback connectedCB) {
 				connectedCB.connect(login, password);
 			}
 			
 		}
 		
 		
 	
 	// Listener of button connection
 	class ConnectionButtonListener extends MouseAdapter {
 		public void mouseClicked(MouseEvent event) {
 
 			String endpoint = endPointField.getText();
 			String username = usernameField.getText();
 			String password = passwordField.getText();
 			if (endpoint == null || endpoint.equalsIgnoreCase("")) {
 				option.setEndpoints(null);
 			} else {
 				JSONArray endpoints = new JSONArray();
 				endpoints.put(endpoint);
 				option.setEndpoints(endpoints);
 			}
 
 			if (socketRadioButton.isSelected())
 				option.setTransport("socketio");
 			else
 				option.setTransport("xmpp");
 
 //			option.setAuthCB(new ACB(username, password));
 			client.connect(username	, password, option);
 		}
 	}
 
 	// Listener of button disconnection
 	class DisconnectionButtonListener extends MouseAdapter {
 		public void mouseClicked(MouseEvent event) {
 			client.disconnect();
 		}
 	}
 
 	// Listener of button hcommand
 	class SendButtonListener extends MouseAdapter {
 		public void mouseClicked(MouseEvent event) {
 			JSONObject jsonObj = new JSONObject();
 
 			HMessageOptions msgOptions = new HMessageOptions();
 
 			if (persistentRadioButton.isSelected())
 				msgOptions.setPersistent(true);
 			else
 				msgOptions.setPersistent(false);
 			if (!relevantField.getText().isEmpty()) {
 				String temp = relevantField.getText();
 				int millisecond = Integer.parseInt(temp);
 //			    DateTime nowDate = new DateTime();
 				msgOptions.setRelevanceOffset(millisecond);
 			}
 			if (!timeoutField.getText().isEmpty()) {
 				String temp = timeoutField.getText();
 				int timeout = Integer.parseInt(temp);
 				msgOptions.setTimeout(timeout);
 			}
 
 			try {
 				jsonObj.put("text", messageField.getText());
 				HMessage message = client.buildMessage(actorField.getText(),
 						"text", jsonObj, msgOptions);
 				client.send(message, outerClass);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	class CreateChannelButtonListener extends MouseAdapter {
 		public void mouseClicked(MouseEvent event) {
 			JSONObject channelToCreate = new JSONObject();
 
 			try {
 				channelToCreate.put("type", "channel");
 				channelToCreate.put("actor", actorField.getText());
 				channelToCreate.put("owner", usernameField.getText());
 				JSONArray jsonArray = new JSONArray();
 				jsonArray.put(usernameField.getText());
				jsonArray.put("u2@localhost");
 				channelToCreate.put("subscribers", jsonArray);
 				channelToCreate.put("active", true);
				HMessage message = client.buildCommand("hnode@localhost.com",
 						"hcreateupdatechannel", channelToCreate, null);
 				client.send(message, outerClass);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	// Listener of button clean
 	class CleanButtonListener extends MouseAdapter {
 		public void mouseClicked(MouseEvent event) {
 			cleanTextArea();
 		}
 	}
 
 	// Listener of button subscribe
 	class SubscribeButtonListener extends MouseAdapter {
 		
 		public void mouseClicked(MouseEvent event) {
 			try {
 				client.subscribe(actorField.getText(), outerClass);
 			} catch (MissingAttrException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 
 	// Listener of button unsubscribe
 	class UnsubscribeButtonListener extends MouseAdapter {
 		public void mouseClicked(MouseEvent event) {
 			try {
 				client.unsubscribe(actorField.getText(), outerClass);
 			} catch (MissingAttrException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 
 
 
 	// Listener of button publish
 	class GetLastMessagesButtonListener extends MouseAdapter {
 		public void mouseClicked(MouseEvent event) {
 			String actor = actorField.getText();
 			try {
 				int nbLastMessage = Integer.parseInt(nbLastMessagesField
 						.getText());
 				if (nbLastMessage > 0) {
 					client.getLastMessages(actor, nbLastMessage, outerClass);
 				} else {
 					client.getLastMessages(actor, outerClass);
 				}
 			} catch (Exception e) {
 				try {
 					client.getLastMessages(actor, outerClass);
 				} catch (MissingAttrException e1) {
 					
 				}
 			}
 		}
 	}
 
 	// Listener of button getsubscriptions
 	class GetSubscriptionButtonListener extends MouseAdapter {
 		public void mouseClicked(MouseEvent event) {
 			try {
 				client.getSubscriptions(outerClass);
 			} catch (MissingAttrException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 
 	// Listener of button getThread
 	class GetThreadButtonListener extends MouseAdapter {
 		public void mouseClicked(MouseEvent event) {
 			String actor = actorField.getText();
 			String convid = convidField.getText();
 			try {
 				client.getThread(actor, convid, outerClass);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	// Listener of button getThreads
 	class GetThreadsButtonListener extends MouseAdapter {
 		public void mouseClicked(MouseEvent event) {
 			String actor = actorField.getText();
 			String status = convstateField.getText();
 			try {
 				client.getThreads(actor, status, outerClass);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	// Listener of button pubConvState
 	class PubConvStateButtonListener extends MouseAdapter {
 		public void mouseClicked(MouseEvent event) {
 			String actor = actorField.getText();
 			String convid = convidField.getText();
 			String status = convstateField.getText();
 			HMessageOptions msgOptions = new HMessageOptions();
 
 			if (persistentRadioButton.isSelected())
 				msgOptions.setPersistent(true);
 			else
 				msgOptions.setPersistent(false);
 
 			try {
 				HMessage pubMsg = client.buildConvState(actor, convid, status,
 						msgOptions);
 				pubMsg.setTimeout(30000);
 				client.send(pubMsg, outerClass);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	// Listener of button setFilter
 	class SetFilterListener extends MouseAdapter {
 		 public void mouseClicked(MouseEvent event) {
 			 HCondition filter = new HCondition();
 			 HArrayOfValue values = new HArrayOfValue();
 			 values.setName("publisher");
 			 JSONArray jsonArray = new JSONArray();
 			 jsonArray.put("u1@localhost");
 //			 jsonArray.put("u2@localhost");
 			 values.setValues(jsonArray);
 			 filter.setValueArray(OperandNames.IN, values);
 			 try {
 				client.setFilter(filter, outerClass);
 			} catch (MissingAttrException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		// String actor = actorField.getText();
 		// String filterName = filterNameField.getText();
 		// String filterAttr = filterAttrField.getText();
 		// String filterValue = filterValueField.getText();
 		//
 		// JSONObject jsonObj = new JSONObject();
 		// System.out.println("filter Attr : " + filterAttr + "filter valu : " +
 		// filterValue);
 		// try {
 		// jsonObj.put(filterAttr, filterValue);
 		// } catch (JSONException e) {
 		// e.printStackTrace();
 		// }
 		// HMessage template = new HMessage(jsonObj);
 		// HFilterTemplate filter = new HFilterTemplate();
 		// filter.setChid(actor);
 		// filter.setName(filterName);
 		// filter.setTemplate(template);
 		// client.setFilter(filter, outerClass);
 		 }
 	}
 
 	// Listener of button listerFilter
 	class ListFiltersListener extends MouseAdapter {
 		// public void mouseClicked(MouseEvent event) {
 		// String actor = chidField.getText();
 		// if(actor == "")
 		// client.listFilters(null, outerClass);
 		// else
 		// client.listFilters(actor, outerClass);
 		// }
 	}
 
 	// Listener of button unsetFilter
 	class UnsetFilterListener extends MouseAdapter {
 		// public void mouseClicked(MouseEvent event) {
 		// String actor = chidField.getText();
 		// String filterName = filterNameField.getText();
 		// client.unsetFilter(filterName,actor,outerClass);
 		// }
 	}
 
 	// Listener of button getRelevantMessages
 	class GetRelevantMessagesListener extends MouseAdapter {
 		public void mouseClicked(MouseEvent event) {
 			String actor = actorField.getText();
 			try {
 				client.getRelevantMessages(actor, outerClass);
 			} catch (MissingAttrException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/* Override for Delegate */
 
 	@Override
 	public void onMessage(HMessage message) {
 		String txtComplete = this.logArea.getText() + "\n" +  "CallBack !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! \n";
 		txtComplete +=message.toString();
 		if (message.getPayload() != null)
 			txtComplete += "\n" + "Payload >>>>>> "
 					+ message.getPayload().toString();
 		this.addTextArea(txtComplete);
 	}
 
 	@Override
 	public void onStatus(HStatus status) {
 		this.setStatusArea("hstatus");
 		this.addTextArea(status.toString());
 		if (status.getErrorCode() == ConnectionError.NO_ERROR
 				|| status.getErrorMsg() == null) {
 			this.setStatusArea(status.getStatus().toString());
 		} else {
 			this.setStatusArea(status.getStatus().toString() + " : "
 					+ status.getErrorMsg());
 		}
 	}
 
 	/* Getters & Setters */
 	public void setTextArea(String text) {
 		this.logArea.setText(text);
 	}
 
 }
