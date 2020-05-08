 package net.bubbaland.trivia.client;
 
 //imports for GUI
 import java.awt.*;
 import javax.swing.*;
 
 import net.bubbaland.trivia.TriviaInterface;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class WorkflowQueuePanel.
  */
 public class WorkflowQueuePanel extends TriviaPanel {
 
 	/** The Constant serialVersionUID. */
 	private static final long		serialVersionUID		= 784049314825719490L;
 
 	/** The Constant HEADER_TEXT_COLOR. */
 	protected static final Color	HEADER_TEXT_COLOR		= Color.white;
 	
 	/** The Constant HEADER_BACKGROUND_COLOR. */
 	protected static final Color	HEADER_BACKGROUND_COLOR	= Color.darkGray;
 
 	/** The Constant QNUM_FONT_SIZE. */
 	protected static final float	QNUM_FONT_SIZE			= (float)36.0;
 	
 	/** The Constant VALUE_FONT_SIZE. */
 	protected static final float	VALUE_FONT_SIZE			= (float)36.0;
 	
 	/** The Constant QUESTION_FONT_SIZE. */
 	protected static final float	QUESTION_FONT_SIZE		= (float)12.0;
 
 	/** The Constant BUTTON_PADDING_X. */
 	protected static final int		BUTTON_PADDING_X		= 10;
 	
 	/** The Constant BUTTON_PADDING_Y. */
 	protected static final int		BUTTON_PADDING_Y		= 20;
 
 	/** The Constant HEADER_FONT_SIZE. */
 	private static final float		HEADER_FONT_SIZE		= (float)12.0;
 	
 	/** The Constant HEADER_HEIGHT. */
 	protected static final int		HEADER_HEIGHT			= 20;
 
 	/** The Constant TIME_WIDTH. */
 	protected static final int		TIME_WIDTH				= 52;
 	
 	/** The Constant QNUM_WIDTH. */
 	protected static final int		QNUM_WIDTH				= 40;
 	
 	/** The Constant ANSWER_WIDTH. */
	protected static final int		ANSWER_WIDTH			= 50;
 	
 	/** The Constant CONFIDENCE_WIDTH. */
 	protected static final int		CONFIDENCE_WIDTH		= 30;
 	
 	/** The Constant SUBMITTER_WIDTH. */
	protected static final int		SUB_CALLER_WIDTH		= 100;
 	
 	/** The Constant OPERATOR_WIDTH. */
 	protected static final int		OPERATOR_WIDTH			= 100;
 	
 	/** The Constant STATUS_WIDTH. */
 	protected static final int		STATUS_WIDTH			= 100;
 
 	/** The workflow queue sub panel. */
 	private WorkflowQueueSubPanel	workflowQueueSubPanel;
 	
 	/** The client. */
 	private TriviaClient client;
 	
 	/** The queue size label. */
 	private JLabel queueSizeLabel;
 
 	/**
 	 * Instantiates a new workflow queue panel.
 	 *
 	 * @param server the server
 	 * @param client the client
 	 */
 	public WorkflowQueuePanel( TriviaInterface server, TriviaClient client ) {
 
 		super( new GridBagLayout() );
 		
 		this.client = client;
 					
 		GridBagConstraints solo = new GridBagConstraints();		
 		solo.fill = GridBagConstraints.BOTH;
 		solo.anchor = GridBagConstraints.CENTER;
 		solo.weightx = 1.0; solo.weighty = 1.0;
 		solo.gridx = 0; solo.gridy = 0;
 		
 		GridBagConstraints constraints = new GridBagConstraints();
 		constraints.fill = GridBagConstraints.BOTH;
 		constraints.anchor = GridBagConstraints.NORTH;
 		constraints.weightx = 0.0;
 		constraints.weighty = 0.0;
 
 		constraints.gridx = 0;
 		constraints.gridy = 0;
 		enclosedLabel("Time", TIME_WIDTH, HEADER_HEIGHT, HEADER_TEXT_COLOR, HEADER_BACKGROUND_COLOR, constraints, HEADER_FONT_SIZE, JLabel.CENTER, JLabel.CENTER);
 
 
 		constraints.gridx = 1;
 		constraints.gridy = 0;
 		enclosedLabel("Q#", QNUM_WIDTH, HEADER_HEIGHT, HEADER_TEXT_COLOR, HEADER_BACKGROUND_COLOR, constraints, HEADER_FONT_SIZE, JLabel.CENTER, JLabel.CENTER);
 
 
 		constraints.gridx = 2;
 		constraints.gridy = 0;
 		constraints.weightx = 1.0;
 		enclosedLabel("Proposed Answer", QNUM_WIDTH, HEADER_HEIGHT, HEADER_TEXT_COLOR, HEADER_BACKGROUND_COLOR, constraints, HEADER_FONT_SIZE, JLabel.LEFT, JLabel.CENTER);
 		constraints.weightx = 0.0;
 		
 		constraints.gridx = 3;
 		constraints.gridy = 0;
 		enclosedLabel("", ANSWER_WIDTH, HEADER_HEIGHT, HEADER_TEXT_COLOR, HEADER_BACKGROUND_COLOR, constraints, HEADER_FONT_SIZE, JLabel.LEFT, JLabel.CENTER);
 
 		constraints.gridx = 4;
 		constraints.gridy = 0;
 		enclosedLabel("Conf", CONFIDENCE_WIDTH, HEADER_HEIGHT, HEADER_TEXT_COLOR, HEADER_BACKGROUND_COLOR, constraints, HEADER_FONT_SIZE, JLabel.CENTER, JLabel.CENTER);
 
 		constraints.gridx = 5;
 		constraints.gridy = 0;
 		enclosedLabel("Sub/Caller", SUB_CALLER_WIDTH, HEADER_HEIGHT, HEADER_TEXT_COLOR, HEADER_BACKGROUND_COLOR, constraints, HEADER_FONT_SIZE, JLabel.CENTER, JLabel.CENTER);
 
 		constraints.gridx = 6;
 		constraints.gridy = 0;
 		enclosedLabel("Operator", OPERATOR_WIDTH, HEADER_HEIGHT, HEADER_TEXT_COLOR, HEADER_BACKGROUND_COLOR, constraints, HEADER_FONT_SIZE, JLabel.CENTER, JLabel.CENTER);
 
 		constraints.gridx = 7;
 		constraints.gridy = 0;
 		enclosedLabel("Status", STATUS_WIDTH, HEADER_HEIGHT, HEADER_TEXT_COLOR, HEADER_BACKGROUND_COLOR, constraints, HEADER_FONT_SIZE, JLabel.CENTER, JLabel.CENTER);
 
 		constraints.gridx = 8;
 		constraints.gridy = 0;
 		int scrollBarWidth = ( (Integer)UIManager.get( "ScrollBar.width" ) ).intValue();
 		this.queueSizeLabel = enclosedLabel("0", scrollBarWidth, HEADER_HEIGHT, HEADER_TEXT_COLOR, HEADER_BACKGROUND_COLOR, constraints, HEADER_FONT_SIZE, JLabel.CENTER, JLabel.CENTER);
 
 		constraints.gridx = 0;
 		constraints.gridy = 1;
 		constraints.gridwidth = 9;
 		constraints.weightx = 1.0;
 		constraints.weighty = 1.0;
 		this.workflowQueueSubPanel = new WorkflowQueueSubPanel( server, client );
 		JScrollPane workflowQueuePane = new JScrollPane( this.workflowQueueSubPanel,
 				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER );
 		workflowQueuePane.setPreferredSize( new Dimension(0, WorkflowQueueSubPanel.DEFAULT_N_ANSWERS_SHOW * WorkflowQueueSubPanel.ANSWER_HEIGHT) );
 		workflowQueuePane.setMinimumSize( new Dimension(0, WorkflowQueueSubPanel.ANSWER_HEIGHT) );
 		this.add( workflowQueuePane, constraints );
 		constraints.weighty = 0.0;
 		
 	}
 
 	/* (non-Javadoc)
 	 * @see net.bubbaland.trivia.TriviaPanel#update()
 	 */
 	@Override
 	public synchronized void update() {
 		int queueSize = client.getTrivia().getAnswerQueueSize();
 		
 		this.queueSizeLabel.setText( queueSize + "" );
 		
 		this.workflowQueueSubPanel.update();
 	}
 
 }
