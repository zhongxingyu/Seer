 package net.bubbaland.trivia.client;
 
 // imports for GUI
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.rmi.RemoteException;
 import java.util.Arrays;
 
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.SwingConstants;
 import javax.swing.UIManager;
 
 import net.bubbaland.trivia.Answer;
 import net.bubbaland.trivia.Trivia;
 import net.bubbaland.trivia.TriviaInterface;
 import net.bubbaland.trivia.client.TriviaClient.QueueSort;
 
 /**
  * A panel that show the current answers in the queue
  * 
  * @author Walter Kolczynski
  * 
  */
 public class WorkflowQueuePanel extends TriviaPanel {
 
 	/**
 	 * A panel that will show the current answer queue
 	 */
 	private class WorkflowQueueSubPanel extends TriviaPanel implements ItemListener {
 
 		/** The Constant serialVersionUID */
 		private static final long			serialVersionUID	= -5462544756397828556L;
 
 		/** The last status (used for determining if the status has changed) */
 		private volatile String[]			lastStatus;
 
 		/**
 		 * GUI elements that will be updated
 		 */
 		final private JLabel[]				queuenumberLabels, timestampLabels, qNumberLabels, confidenceLabels,
 				submitterLabels, operatorLabels, callerLabels;
 		final private JComboBox<String>[]	statusComboBoxes;
 		final private JTextArea[]			answerTextAreas;
 		
 		private Answer[] answerQueue;
 
 		/**
 		 * Data sources
 		 */
 		final private TriviaInterface		server;
 		final private TriviaClient			client;
 
 
 		/**
 		 * Instantiates a new workflow queue sub panel.
 		 * 
 		 * @param server
 		 *            the server
 		 * @param client
 		 *            the client
 		 */
 		@SuppressWarnings("unchecked")
 		public WorkflowQueueSubPanel(TriviaInterface server, TriviaClient client) {
 
 			super();
 
 			this.server = server;
 			this.client = client;
 			this.answerQueue = new Answer[0];
 
 			// Set up layout constraints
 			final GridBagConstraints constraints = new GridBagConstraints();
 			constraints.fill = GridBagConstraints.BOTH;
 			constraints.anchor = GridBagConstraints.NORTH;
 			constraints.weightx = 0.0;
 			constraints.weighty = 0.0;
 
 			this.queuenumberLabels = new JLabel[MAX_QUEUE_LENGTH];
 			this.timestampLabels = new JLabel[MAX_QUEUE_LENGTH];
 			this.qNumberLabels = new JLabel[MAX_QUEUE_LENGTH];
 			this.confidenceLabels = new JLabel[MAX_QUEUE_LENGTH];
 			this.submitterLabels = new JLabel[MAX_QUEUE_LENGTH];
 			this.operatorLabels = new JLabel[MAX_QUEUE_LENGTH];
 			this.callerLabels = new JLabel[MAX_QUEUE_LENGTH];
 			this.statusComboBoxes = new JComboBox[MAX_QUEUE_LENGTH];
 			this.answerTextAreas = new JTextArea[MAX_QUEUE_LENGTH];
 			this.lastStatus = new String[MAX_QUEUE_LENGTH];
 
 			/**
 			 * Create the GUI elements for each row
 			 */
 			for (int a = 0; a < MAX_QUEUE_LENGTH; a++) {
 
 				constraints.gridheight = 1;
 				constraints.gridx = 0;
 				constraints.gridy = 2 * a;
 				this.queuenumberLabels[a] = this.enclosedLabel("#" + ( a + 1 ), TIME_WIDTH, ANSWER_HEIGHT / 2,
 						NOT_CALLED_IN_COLOR, BACKGROUND_COLOR, constraints, SMALL_FONT_SIZE, SwingConstants.CENTER,
 						SwingConstants.CENTER);
 
 				constraints.gridx = 0;
 				constraints.gridy = 2 * a + 1;
 				this.timestampLabels[a] = this.enclosedLabel("", TIME_WIDTH, ANSWER_HEIGHT / 2, NOT_CALLED_IN_COLOR,
 						BACKGROUND_COLOR, constraints, SMALL_FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);
 
 				constraints.gridheight = 2;
 				constraints.gridx = 1;
 				constraints.gridy = 2 * a;
 				this.qNumberLabels[a] = this.enclosedLabel("", QNUM_WIDTH, ANSWER_HEIGHT, NOT_CALLED_IN_COLOR,
 						BACKGROUND_COLOR, constraints, LARGE_FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);
 
 				constraints.gridx = 2;
 				constraints.gridy = 2 * a;
 				constraints.weightx = 1.0;
 				this.answerTextAreas[a] = this.scrollableTextArea("", ANSWER_WIDTH, ANSWER_HEIGHT, NOT_CALLED_IN_COLOR,
 						BACKGROUND_COLOR, constraints, SMALL_FONT_SIZE, Component.LEFT_ALIGNMENT,
 						Component.TOP_ALIGNMENT, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER,
 						ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
 				this.answerTextAreas[a].setEditable(false);
 				constraints.weightx = 0.0;
 
 				constraints.gridx = 3;
 				constraints.gridy = 2 * a;
 				this.confidenceLabels[a] = this.enclosedLabel("", CONFIDENCE_WIDTH, ANSWER_HEIGHT, NOT_CALLED_IN_COLOR,
 						BACKGROUND_COLOR, constraints, SMALL_FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);
 
 				constraints.gridheight = 1;
 				constraints.gridx = 4;
 				constraints.gridy = 2 * a;
 				this.submitterLabels[a] = this.enclosedLabel("", SUB_CALLER_WIDTH, ANSWER_HEIGHT / 2,
 						NOT_CALLED_IN_COLOR, BACKGROUND_COLOR, constraints, SMALL_FONT_SIZE, SwingConstants.CENTER,
 						SwingConstants.CENTER);
 
 				constraints.gridx = 4;
 				constraints.gridy = 2 * a + 1;
 				this.callerLabels[a] = this.enclosedLabel("", SUB_CALLER_WIDTH, ANSWER_HEIGHT / 2, NOT_CALLED_IN_COLOR,
 						BACKGROUND_COLOR, constraints, SMALL_FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);
 				constraints.gridheight = 2;
 
 				constraints.gridx = 5;
 				constraints.gridy = 2 * a;
 				this.operatorLabels[a] = this.enclosedLabel("", OPERATOR_WIDTH, ANSWER_HEIGHT, NOT_CALLED_IN_COLOR,
 						BACKGROUND_COLOR, constraints, SMALL_FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);
 
 				constraints.gridx = 6;
 				constraints.gridy = 2 * a;
 				final JPanel panel = new JPanel(new GridBagLayout());
 				panel.setBackground(BACKGROUND_COLOR);
 				panel.setPreferredSize(new Dimension(STATUS_WIDTH, ANSWER_HEIGHT));
 				panel.setMinimumSize(new Dimension(STATUS_WIDTH, ANSWER_HEIGHT));
 				this.add(panel, constraints);
 				this.statusComboBoxes[a] = new JComboBox<String>(STATUSES);
 				this.statusComboBoxes[a].setName(a + "");
 				this.statusComboBoxes[a].addItemListener(this);
 				this.statusComboBoxes[a].setBackground(BACKGROUND_COLOR);
 				panel.add(this.statusComboBoxes[a]);
 
 				this.lastStatus[a] = "new";
 
 			}
 
 			/**
 			 * Create a blank spacer row at the bottom
 			 */
 			constraints.gridx = 0;
 			constraints.gridy = MAX_QUEUE_LENGTH;
 			constraints.gridwidth = 8;
 			constraints.weightx = 1.0;
 			constraints.weighty = 1.0;
 			final JPanel blank = new JPanel();
 			blank.setBackground(BACKGROUND_COLOR);
 			blank.setPreferredSize(new Dimension(0, 0));
 			this.add(blank, constraints);
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
 		 */
 		@SuppressWarnings("unchecked")
 		@Override
 		public synchronized void itemStateChanged(ItemEvent event) {
 			final JComponent source = (JComponent) event.getSource();
 			if (source instanceof JComboBox<?> && event.getStateChange() == ItemEvent.SELECTED) {
 				// Event was a change to the status combo box
 				final String newStatus = (String) ( ( (JComboBox<String>) source ).getSelectedItem() );
 				final int queueIndex = Integer.parseInt(( (JComboBox<String>) source ).getName());
 
 				// Update status on server
 				int tryNumber = 0;
 				boolean success = false;
 				while (tryNumber < TriviaClient.MAX_RETRIES && success == false) {
 					tryNumber++;
 					try {
 						switch (newStatus) {
 							case "Not Called In":
 								this.server.markUncalled(client.getUser(), queueIndex);
 								break;
 							case "Calling":
 								this.server.callIn(queueIndex, this.client.getUser());
 								break;
 							case "Incorrect":
 								this.server.markIncorrect(queueIndex, this.client.getUser());
 								break;
 							case "Partial":
 								this.server.markPartial(queueIndex, this.client.getUser());
 								break;
 							case "Correct":
 								new CorrectEntryPanel(this.server, this.client, this.client.getUser(), queueIndex,
 										( (JComboBox<String>) source ));
 								break;
 							default:
 								break;
 						}
 						success = true;
 					} catch (final RemoteException e) {
 						this.client.log("Couldn't change answer status on server (try #" + tryNumber + ").");
 					}
 				}
 
 				// Unable to update status on server, show disconnected dialog
 				if (!success) {
 					this.client.disconnected();
 					return;
 				}
 
 				this.client.log("Changed status of answer #" + ( queueIndex + 1 ) + " in the queue to " + newStatus);
 
 			}
 
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see net.bubbaland.trivia.TriviaPanel#update()
 		 */
 		@Override
 		public synchronized void update(boolean force) {
 			// Get the current Trivia data object
 			final Trivia trivia = this.client.getTrivia();
 			
 			final boolean hideClosed = this.client.hideClosed();
 			final QueueSort sortMethod = this.client.getQueueSort();
 			
 			// Get the queue data from the server
 			final Answer[] newAnswerQueue = trivia.getAnswerQueue();
 			
 			switch (sortMethod) {
 				case TIMESTAMP:
 					Arrays.sort(newAnswerQueue, new Answer.TimestampCompare());
 					break;
 				case QNUMBER:
 					Arrays.sort(newAnswerQueue, new Answer.QNumberCompare());
 					break;
 				case STATUS:
 					Arrays.sort(newAnswerQueue, new Answer.StatusCompare());
 					break;
 			}
 			
 			final int queueSize = newAnswerQueue.length;
 
 			// Determine if each item in the queue has been updated
 			final boolean[] qUpdated = new boolean[queueSize];
 			for (int a = 0; a < queueSize; a++) {
 				if( a < answerQueue.length ) {
 					qUpdated[a] = !(answerQueue[a].equals(newAnswerQueue[a]));
 				} else {
 					qUpdated[a]= true;
 				}
 			}
 			
 			this.answerQueue = newAnswerQueue;
 			
 			for (int a = 0; a < queueSize; a++) {
 				
 				final int newQueueNumber = answerQueue[a].getQueueLocation(); 
 				final String newTimestamp = answerQueue[a].getTimestamp();
 				final int newQNumber = answerQueue[a].getQNumber();
 				final String newAnswer = answerQueue[a].getAnswer();
 				final int newConfidence = answerQueue[a].getConfidence();
 				final String newSubmitter = answerQueue[a].getSubmitter();
 				final String newOperator = answerQueue[a].getOperator();
 				final String newCaller = answerQueue[a].getCaller();
 				final String newStatus = answerQueue[a].getStatusString();
 
 				this.lastStatus[a] = newStatus;
 								
 				boolean closed = !trivia.isOpen(newQNumber);
 				
 				if (qUpdated[a] || force) {
 					// If the status has changed, update the labels and color
 					Color color = NOT_CALLED_IN_COLOR;
 					final int statusIndex = Arrays.asList(STATUSES).indexOf(newStatus);
 					switch (newStatus) {
 						case "Not Called In":
 							color = NOT_CALLED_IN_COLOR;
 							break;
 						case "Calling":
 							color = CALLING_COLOR;
 							break;
 						case "Incorrect":
 							color = INCORRECT_COLOR;
 							break;
 						case "Partial":
 							color = PARTIAL_COLOR;
 							break;
 						case "Correct":
 							color = CORRECT_COLOR;
 							break;
 						default:
 							color = NOT_CALLED_IN_COLOR;
 							break;
 					}
 					
 					this.queuenumberLabels[a].setText("#" + newQueueNumber);
 
 					this.timestampLabels[a].setText(newTimestamp);
 					this.timestampLabels[a].setForeground(color);
 
 					this.qNumberLabels[a].setText(newQNumber + "");
 					this.qNumberLabels[a].setForeground(color);
 
 					this.answerTextAreas[a].setText(newAnswer);
 					this.answerTextAreas[a].setForeground(color);
 					this.answerTextAreas[a].setCaretPosition(0);
 
 					this.confidenceLabels[a].setText(newConfidence + "");
 					this.confidenceLabels[a].setForeground(color);
 
 					this.submitterLabels[a].setText(newSubmitter);
 					this.submitterLabels[a].setForeground(color);
 
 					this.operatorLabels[a].setText(newOperator);
 					this.operatorLabels[a].setForeground(color);
 
 					this.callerLabels[a].setText(newCaller);
 					this.callerLabels[a].setForeground(color);
 
 					// Temporarily remove the status box listener to prevent trigger when we change it to match server
 					// status
 					final ItemListener[] listeners = this.statusComboBoxes[a].getItemListeners();
 					for (final ItemListener listener : listeners) {
 						this.statusComboBoxes[a].removeItemListener(listener);
 					}
 					this.statusComboBoxes[a].setForeground(color);
					this.statusComboBoxes[a].setName((newQueueNumber-1) + "");					
 					this.statusComboBoxes[a].setSelectedIndex(statusIndex);
 					// Add the status box listener back to monitor user changes
 					for (final ItemListener listener : listeners) {
 						this.statusComboBoxes[a].addItemListener(listener);
 					}
 
 					if(hideClosed && closed) {
 						// Hide this row
 						this.queuenumberLabels[a].setVisible(false);
 						this.timestampLabels[a].setVisible(false);
 						this.qNumberLabels[a].setVisible(false);
 						this.answerTextAreas[a].setVisible(false);
 						this.answerTextAreas[a].getParent().setVisible(false);
 						this.answerTextAreas[a].getParent().getParent().setVisible(false);
 						this.confidenceLabels[a].setVisible(false);
 						this.submitterLabels[a].setVisible(false);
 						this.operatorLabels[a].setVisible(false);
 						this.callerLabels[a].setVisible(false);
 						this.statusComboBoxes[a].setVisible(false);
 	
 						this.queuenumberLabels[a].getParent().setVisible(false);
 						this.timestampLabels[a].getParent().setVisible(false);
 						this.qNumberLabels[a].getParent().setVisible(false);
 						this.answerTextAreas[a].getParent().setVisible(false);
 						this.answerTextAreas[a].getParent().getParent().setVisible(false);
 						this.confidenceLabels[a].getParent().setVisible(false);
 						this.submitterLabels[a].getParent().setVisible(false);
 						this.operatorLabels[a].getParent().setVisible(false);
 						this.callerLabels[a].getParent().setVisible(false);
 						this.statusComboBoxes[a].getParent().setVisible(false);
 					} else {
 						// Make this row visible
 						this.queuenumberLabels[a].setVisible(true);
 						this.timestampLabels[a].setVisible(true);
 						this.qNumberLabels[a].setVisible(true);
 						this.answerTextAreas[a].setVisible(true);
 						this.answerTextAreas[a].getParent().setVisible(true);
 						this.answerTextAreas[a].getParent().getParent().setVisible(true);
 						this.confidenceLabels[a].setVisible(true);
 						this.submitterLabels[a].setVisible(true);
 						this.operatorLabels[a].setVisible(true);
 						this.callerLabels[a].setVisible(true);
 						this.statusComboBoxes[a].setVisible(true);
 	
 						this.queuenumberLabels[a].getParent().setVisible(true);
 						this.timestampLabels[a].getParent().setVisible(true);
 						this.qNumberLabels[a].getParent().setVisible(true);
 						this.answerTextAreas[a].getParent().setVisible(true);
 						this.answerTextAreas[a].getParent().getParent().setVisible(true);
 						this.confidenceLabels[a].getParent().setVisible(true);
 						this.submitterLabels[a].getParent().setVisible(true);
 						this.operatorLabels[a].getParent().setVisible(true);
 						this.callerLabels[a].getParent().setVisible(true);
 						this.statusComboBoxes[a].getParent().setVisible(true);
 					}
 
 				}
 
 			}
 
 			// Hide unused rows
 			for (int a = queueSize; a < MAX_QUEUE_LENGTH; a++) {
 				this.queuenumberLabels[a].setVisible(false);
 				this.timestampLabels[a].setVisible(false);
 				this.qNumberLabels[a].setVisible(false);
 				this.answerTextAreas[a].setVisible(false);
 				this.confidenceLabels[a].setVisible(false);
 				this.submitterLabels[a].setVisible(false);
 				this.operatorLabels[a].setVisible(false);
 				this.callerLabels[a].setVisible(false);
 				this.statusComboBoxes[a].setVisible(false);
 
 				this.queuenumberLabels[a].getParent().setVisible(false);
 				this.timestampLabels[a].getParent().setVisible(false);
 				this.qNumberLabels[a].getParent().setVisible(false);
 				this.answerTextAreas[a].getParent().setVisible(false);
 				this.answerTextAreas[a].getParent().getParent().setVisible(false);
 				this.confidenceLabels[a].getParent().setVisible(false);
 				this.submitterLabels[a].getParent().setVisible(false);
 				this.operatorLabels[a].getParent().setVisible(false);
 				this.callerLabels[a].getParent().setVisible(false);
 				this.statusComboBoxes[a].getParent().setVisible(false);
 
 			}
 
 		}
 		
 
 	}
 
 	/** The Constant serialVersionUID. */
 	private static final long			serialVersionUID		= 784049314825719490L;
 	/**
 	 * Colors
 	 */
 	private static final Color			HEADER_BACKGROUND_COLOR	= Color.darkGray;
 
 	private static final Color			HEADER_TEXT_COLOR		= Color.white;
 	private static final Color			BACKGROUND_COLOR		= Color.BLACK;
 	private static final Color			NOT_CALLED_IN_COLOR		= Color.WHITE;
 	private static final Color			CALLING_COLOR			= Color.CYAN;
 	private static final Color			INCORRECT_COLOR			= Color.RED;
 	private static final Color			PARTIAL_COLOR			= Color.ORANGE;
 
 	private static final Color			CORRECT_COLOR			= Color.GREEN;
 	/**
 	 * Sizes
 	 */
 	private static final int			HEADER_HEIGHT			= 20;
 
 	private static final int			ANSWER_HEIGHT			= 40;
 	private static final int			TIME_WIDTH				= 60;
 	private static final int			QNUM_WIDTH				= 40;
 	private static final int			ANSWER_WIDTH			= 50;
 	private static final int			CONFIDENCE_WIDTH		= 35;
 	private static final int			SUB_CALLER_WIDTH		= 100;
 	private static final int			OPERATOR_WIDTH			= 100;
 
 	private static final int			STATUS_WIDTH			= 120;
 	/**
 	 * Font sizes
 	 */
 	private static final float			HEADER_FONT_SIZE		= (float) 12.0;
 	private static final float			LARGE_FONT_SIZE			= (float) 24.0;
 
 	private static final float			SMALL_FONT_SIZE			= (float) 12.0;
 
 	/** The number of questions to show at startup */
 	private static final int			DEFAULT_N_ANSWERS_SHOW	= 4;
 
 	/** Maximum number of answers in the queue */
 	private static final int			MAX_QUEUE_LENGTH		= 500;
 
 	/** Valid statuses for queue items */
 	private static final String[]		STATUSES				= { "Not Called In", "Calling", "Incorrect", "Partial",
 			"Correct"											};
 
 	/**
 	 * GUI elements that will be updated
 	 */
 	final private JLabel				queueSizeLabel;
 
 	/** The workflow queue sub panel */
 	final private WorkflowQueueSubPanel	workflowQueueSubPanel;
 
 	/** The local client */
 	final private TriviaClient			client;
 
 	/**
 	 * Instantiates a new workflow queue panel.
 	 * 
 	 * @param server
 	 *            The remote trivia server
 	 * @param client
 	 *            The local trivia client
 	 */
 	public WorkflowQueuePanel(TriviaInterface server, TriviaClient client) {
 
 		super();
 
 		this.client = client;
 
 		// Set up layout constraints
 		final GridBagConstraints constraints = new GridBagConstraints();
 		constraints.fill = GridBagConstraints.BOTH;
 		constraints.anchor = GridBagConstraints.NORTH;
 		constraints.weightx = 0.0;
 		constraints.weighty = 0.0;
 
 		/**
 		 * Create the header row
 		 */
 		constraints.gridx = 0;
 		constraints.gridy = 0;
 		this.enclosedLabel("Time", TIME_WIDTH, HEADER_HEIGHT, HEADER_TEXT_COLOR, HEADER_BACKGROUND_COLOR, constraints,
 				HEADER_FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);
 
 		constraints.gridx = 1;
 		constraints.gridy = 0;
 		this.enclosedLabel("Q#", QNUM_WIDTH, HEADER_HEIGHT, HEADER_TEXT_COLOR, HEADER_BACKGROUND_COLOR, constraints,
 				HEADER_FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);
 
 		constraints.gridx = 2;
 		constraints.gridy = 0;
 		constraints.weightx = 1.0;
 		this.enclosedLabel("Proposed Answer", QNUM_WIDTH, HEADER_HEIGHT, HEADER_TEXT_COLOR, HEADER_BACKGROUND_COLOR,
 				constraints, HEADER_FONT_SIZE, SwingConstants.LEFT, SwingConstants.CENTER);
 		constraints.weightx = 0.0;
 
 		constraints.gridx = 3;
 		constraints.gridy = 0;
 		this.enclosedLabel("", ANSWER_WIDTH, HEADER_HEIGHT, HEADER_TEXT_COLOR, HEADER_BACKGROUND_COLOR, constraints,
 				HEADER_FONT_SIZE, SwingConstants.LEFT, SwingConstants.CENTER);
 
 		constraints.gridx = 4;
 		constraints.gridy = 0;
 		this.enclosedLabel("Conf", CONFIDENCE_WIDTH, HEADER_HEIGHT, HEADER_TEXT_COLOR, HEADER_BACKGROUND_COLOR,
 				constraints, HEADER_FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);
 
 		constraints.gridx = 5;
 		constraints.gridy = 0;
 		this.enclosedLabel("Sub/Caller", SUB_CALLER_WIDTH, HEADER_HEIGHT, HEADER_TEXT_COLOR, HEADER_BACKGROUND_COLOR,
 				constraints, HEADER_FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);
 
 		constraints.gridx = 6;
 		constraints.gridy = 0;
 		this.enclosedLabel("Operator", OPERATOR_WIDTH, HEADER_HEIGHT, HEADER_TEXT_COLOR, HEADER_BACKGROUND_COLOR,
 				constraints, HEADER_FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);
 
 		constraints.gridx = 7;
 		constraints.gridy = 0;
 		this.enclosedLabel("Status", STATUS_WIDTH, HEADER_HEIGHT, HEADER_TEXT_COLOR, HEADER_BACKGROUND_COLOR,
 				constraints, HEADER_FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);
 
 		constraints.gridx = 8;
 		constraints.gridy = 0;
 		final int scrollBarWidth = ( (Integer) UIManager.get("ScrollBar.width") ).intValue();
 		this.queueSizeLabel = this.enclosedLabel("0", scrollBarWidth, HEADER_HEIGHT, HEADER_TEXT_COLOR,
 				HEADER_BACKGROUND_COLOR, constraints, HEADER_FONT_SIZE, SwingConstants.CENTER, SwingConstants.CENTER);
 
 		/**
 		 * Create the sub-panel that will show the queue data and put it in a scroll pane
 		 */
 		constraints.gridx = 0;
 		constraints.gridy = 1;
 		constraints.gridwidth = 9;
 		constraints.weightx = 1.0;
 		constraints.weighty = 1.0;
 		this.workflowQueueSubPanel = new WorkflowQueueSubPanel(server, client);
 		final JScrollPane workflowQueuePane = new JScrollPane(this.workflowQueueSubPanel,
 				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 		workflowQueuePane.setPreferredSize(new Dimension(0, DEFAULT_N_ANSWERS_SHOW * ANSWER_HEIGHT));
 		workflowQueuePane.setMinimumSize(new Dimension(0, ANSWER_HEIGHT));
 		this.add(workflowQueuePane, constraints);
 		constraints.weighty = 0.0;
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see net.bubbaland.trivia.TriviaPanel#update()
 	 */
 	@Override
 	public synchronized void update(boolean force) {
 		// Update the queue size
 		final int queueSize = this.client.getTrivia().getAnswerQueueSize();
 		this.queueSizeLabel.setText(queueSize + "");
 		this.workflowQueueSubPanel.update(force);
 	}
 
 }
