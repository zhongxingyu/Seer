 package gui;
 
 import static gui.GuiConstants.DEFAULT_END_HOUR;
 import static gui.GuiConstants.DEFAULT_START_HOUR;
 import static gui.GuiConstants.FRAME_HEIGHT;
 import static gui.GuiConstants.FRAME_WIDTH;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 
 import java.awt.Image;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.net.MalformedURLException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.imageio.ImageIO;
 import javax.swing.AbstractButton;
 import javax.swing.GroupLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JToggleButton;
 
 import cal_master.Communicator;
 import cal_master.NameIDPair;
 import calendar.CalendarResponses;
 import calendar.Event;
 import calendar.Event.PaintMethod;
 import calendar.UserCal;
 import calendar.When2MeetEvent;
 
 public class CalendarGui {
 
 	private Event _slotGroup = null;
 	private int _startHour = DEFAULT_START_HOUR;
 	private int _numHours = DEFAULT_END_HOUR - DEFAULT_START_HOUR;
 	private JFrame _frame;
 	private ReplyPanel _replyPanel;
 //	private JPanel _hourOfDayLabels;
 //	private ArrayList<Integer> _hoursOfDay = new ArrayList<Integer>();
 	private Communicator _communicator = new Communicator();
 	private UserCalPanel _userCalPanel;
 	//private JButton _eventDispButton = new JButton("Toggle Event Display");
 	private PaintMethod _eventDispStyle = PaintMethod.Bars;
 	private EventPanel _eventPanel = new EventPanel(_communicator, this);
 	private UpdatesPanel _updatesPanel = new UpdatesPanel();
 	private FriendBar _friendBar = new FriendBar(this);
 
 	private ImageIcon _findTimeIcon = new ImageIcon("small_logo_button.png");
 	private ImageIcon _findTimeIconInverted = new ImageIcon("small_logo_button_invert.png");
 	
 	private ImageIcon _toggleIcon = new ImageIcon("small_switch_button.png");
 	private ImageIcon _toggleIconInverted = new ImageIcon("small_switch_button_invert.png");
 	
 	private ImageIcon _refreshIcon = new ImageIcon("small_refresh_button.png");
 	private ImageIcon _refreshIconInverted = new ImageIcon("small_refresh_button_invert.png");
 	
 	private ImageIcon _submitIcon = new ImageIcon("small_submit_button.png");
 	private ImageIcon _submitIconInverted = new ImageIcon("small_submit_button_invert.png");
 	
 	private ImageIcon _prevIcon = new ImageIcon("small_left_button.png");
 	private ImageIcon _prevIconInverted = new ImageIcon("small_left_button_invert.png");
 	
 	private ImageIcon _nextIcon = new ImageIcon("small_right_button.png");
 	private ImageIcon _nextIconInverted = new ImageIcon("small_right_button_invert.png");
 
 	
 	private JToggleButton _refreshButton = new JToggleButton(_refreshIcon);
 	private JButton _eventDispButton = new JButton(_toggleIcon);
 	private JButton _timeFindButton = new JButton(_findTimeIcon);
 	private JButton _submitButton = new JButton(_submitIcon);
 	private JButton _nextButton = new JButton(_nextIcon);
 	private JButton _prevButton = new JButton(_prevIcon);
 	public static enum DaysOfWeek {Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday};
 	private JLabel _picLabel;
 
 	public CalendarGui() throws URISyntaxException{
 		
 		this.displayButton(_refreshButton);
 		this.displayButton(_eventDispButton);
 		this.displayButton(_timeFindButton);
 		this.displayButton(_submitButton);
 		this.displayButton(_prevButton);
 		this.displayButton(_nextButton);
 		
 		
 		_communicator.startUp();
 
 		_startHour = 9;
 		_friendBar.setEvent(null);
 
 		UserCal userCal = null;
 		
 		if(_communicator.hasUserCal())
 			userCal =_communicator.getUserCal();
 
 		_replyPanel = new ReplyPanel(userCal, null);
 
 		ArrayList<NameIDPair> pairs = _communicator.getNameIDPairs();
 		for(NameIDPair pair : pairs) {
 			_eventPanel.addEvent(new EventLabel(pair.getName(), pair.getID(), _communicator, this));
 		}
 
 		_userCalPanel = new UserCalPanel(_communicator, this);
 
 		_submitButton.addActionListener(new SubmitListener());
 		_submitButton.setFont(new Font(GuiConstants.FONT_NAME, _submitButton.getFont().getStyle(), _submitButton.getFont().getSize()));
 		_submitButton.setToolTipText("Submit Response");
 		_submitButton.setPressedIcon(_submitIconInverted);
 		
 		
 		_timeFindButton.addActionListener(new TimeFindListener());
 		_timeFindButton.setFont(new Font(GuiConstants.FONT_NAME, _timeFindButton.getFont().getStyle(), _timeFindButton.getFont().getSize()));
 		_timeFindButton.setToolTipText("Find Best Times");
 		_timeFindButton.setPressedIcon(_findTimeIconInverted);
 		
 		_nextButton.addActionListener(new NextListener());
 		_nextButton.setFont(new Font(GuiConstants.FONT_NAME, _nextButton.getFont().getStyle(), _nextButton.getFont().getSize()));
 		_nextButton.setPressedIcon(_nextIconInverted);
 		_nextButton.setToolTipText("Next");
 		
 		_prevButton.addActionListener(new PrevListener());
 		_prevButton.setFont(new Font(GuiConstants.FONT_NAME, _prevButton.getFont().getStyle(), _prevButton.getFont().getSize()));
 		_prevButton.setFocusable(false);
 		_prevButton.setPressedIcon(_prevIconInverted);
 		_prevButton.setToolTipText("Previous");
 		
 		_eventDispButton.addActionListener(new EventDispButtonListener());
 		_eventDispButton.setFocusable(false);
 		_eventDispButton.setToolTipText("Toggle Display");
 //		_eventDispButton.setFont(new Font(GuiConstants.FONT_NAME, _eventDispButton.getFont().getStyle(), _eventDispButton.getFont().getSize()));
 
 		_refreshButton.addActionListener(new RefreshListener());
 		_refreshButton.setFocusable(false);
 		_refreshButton.setToolTipText("Refresh");
 		_refreshButton.setPressedIcon(_refreshIconInverted);
 		
 		BufferedImage kairosLogo;
 		try {
 			kairosLogo = ImageIO.read(new File("KairosLogo.png"));
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 //			e.printStackTrace();
 			kairosLogo = null;
 		}
 		_picLabel = new JLabel(new ImageIcon(kairosLogo));
 		
 		_numHours = 8;
 		
 		buildFrame();
 	}
 	
 	public void displayButton(AbstractButton button) {
 		button.setBorderPainted(false);  
 		button.setContentAreaFilled(false);  
 		button.setFocusPainted(false);  
 		button.setOpaque(false); 
 	}
 
 	public Event getEvent(){
 		return _slotGroup;
 	}
 
 	public void setEvent(Event event){
 		_slotGroup= event;
 		
 		if(_slotGroup != null){
 			_slotGroup.init();
 			event.setPaintMethod(_eventDispStyle);
 		}
 		System.out.println("SLOT GROUP IN SET EVENT: " + _slotGroup);
 		_replyPanel.setEvent(_slotGroup);
 		System.out.println("Setting event for reply panel");
 		UserCal userCal = _communicator.getUserCal();
 		_replyPanel.setUserCal(userCal);
 		_replyPanel.repaint();
 		if(_slotGroup != null){
 			_startHour = event.getStartTime().getHourOfDay();
 			_numHours = event.getNumHours();
 		}
 		else{
 			_startHour = 9;
 			_numHours = 8;
 		}
 		_updatesPanel.setEvent(_slotGroup);
 		_friendBar.setEvent(_slotGroup);
 //		updateHourLabels();
 		_eventPanel.refresh();
 
 	}
 
 
 	public void setUserCal(UserCal userCal){
 		_replyPanel.setUserCal(userCal);
 	}
 
 
 	private class InnerWindowListener extends WindowAdapter {
 		@Override
 		public void windowClosing(WindowEvent e) {
 			//System.out.println("Window closing triggered");
 			//_communicator.saveAll();
 		}
 	}
 
 	public void buildFrame(){
 		_frame = new JFrame("Kairos");
 		_frame.addWindowListener(new InnerWindowListener());
 
 		JPanel calPanel = new JPanel();
 		GroupLayout calLayout = new GroupLayout(calPanel);
 		calPanel.setLayout(calLayout);
 
 		// TODO change to false
 		_frame.setResizable(true);
 
 		calLayout.setHorizontalGroup(
 				calLayout.createSequentialGroup()
 //				.addComponent(_hourOfDayLabels, GroupLayout.PREFERRED_SIZE, _hourOfDayLabels.getPreferredSize().width,
 //						GroupLayout.PREFERRED_SIZE)
 						.addComponent(_replyPanel, GroupLayout.PREFERRED_SIZE, (int) (FRAME_WIDTH*.75),
 								GroupLayout.PREFERRED_SIZE));
 
 		calLayout.setVerticalGroup(
 				calLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
 				.addComponent(_replyPanel, GroupLayout.PREFERRED_SIZE, FRAME_HEIGHT - _replyPanel.getPreferredSize().height,
 						GroupLayout.PREFERRED_SIZE)
 //						.addComponent(_hourOfDayLabels, GroupLayout.PREFERRED_SIZE, FRAME_HEIGHT - _replyPanel.getPreferredSize().height - _replyPanel.getWeekDayPanelHeight(),
 //								GroupLayout.PREFERRED_SIZE)
 								);
 
 		_frame.add(calPanel, BorderLayout.CENTER);
 
 
 		JPanel nextPrevPanel = new JPanel();
 		nextPrevPanel.add(_prevButton);
 		nextPrevPanel.add(_nextButton);
 		
 		JPanel buttonFunctionsPanel = new JPanel();
 
 		buttonFunctionsPanel.add(_eventDispButton);
 		buttonFunctionsPanel.add(_submitButton);
 		buttonFunctionsPanel.add(_timeFindButton);
 		buttonFunctionsPanel.add(_refreshButton);
 
 
 		JPanel buttonPanel = new JPanel(new GridLayout(1, 0));
 		buttonPanel.add(nextPrevPanel);
 		buttonPanel.add(buttonFunctionsPanel);
 
 		JPanel logoPanel = new JPanel();
 		logoPanel.add(_picLabel);
 		
 		//new GridLayout(1,2)
 		JPanel northPanel = new JPanel(new GridBagLayout());
 		
 		GridBagConstraints c = new GridBagConstraints();
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.weightx = 0.5;
 		c.weighty = 1.0;
 		c.ipadx = 200;
 		c.gridx = 0;
 		c.gridy = 0;
 		c.ipady = 15;
 		
 		northPanel.add(logoPanel,c);
 		
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.weightx = 1.0;
 		c.weighty = 1.0;
 		c.ipadx = 0 ;
 		c.gridx = 1;
 		c.gridy = 0;
 		c.ipady = 0;
 		
 		northPanel.add(buttonPanel,c);
 
		_frame.add(buttonPanel, BorderLayout.NORTH);
 		
 		JPanel westPanel = new JPanel(new GridLayout(0, 1));
 		westPanel.add(_friendBar);
 		_friendBar.mySetSize(new Dimension((int) ((FRAME_WIDTH*.25*.25)), 400));
 		_frame.add(westPanel, BorderLayout.WEST);
 
 		JPanel eastPanel = new JPanel(new GridLayout(0,1));
 		eastPanel.add(_userCalPanel);
 		eastPanel.add(_eventPanel);
 		eastPanel.add(_updatesPanel);
 
 		eastPanel.setPreferredSize(new Dimension((int) ((FRAME_WIDTH*.25)*.60), 700));
 
 		_frame.add(eastPanel, BorderLayout.EAST);
 
 		_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		//_frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
 		_frame.pack();
 		_frame.setVisible(true);
 	}
 
 	public void replyToEvent(){
 		if(_slotGroup != null && _replyPanel.getClicks() != null)
 			_communicator.submitResponse(Integer.toString(((When2MeetEvent) _slotGroup).getID()), _replyPanel.getClicks());
 	}
 
 
 	public void repaint(){
 		if(_frame != null){
 			_frame.invalidate();
 			_frame.validate();
 			_frame.repaint();
 		}
 	}
 
 	public void setBestTimes(int duration){
 		if(_slotGroup != null){
 			CalendarResponses bestTimes = _communicator.getBestTimes(String.valueOf(_slotGroup.getID()), duration);
 			_replyPanel.setBestTimes(bestTimes);
 			repaint();
 		}
 	}
 
 	private class SubmitListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			if(_slotGroup != null){
 				int selection = JOptionPane.showConfirmDialog(null,"Are you sure you want to submit?", "", 
 						JOptionPane.YES_NO_OPTION);
 				if(selection == JOptionPane.YES_OPTION)
 					replyToEvent();
 			}
 		}
 
 	}
 
 	private class NextListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			if(_slotGroup != null)
 				_replyPanel.nextWeek();
 		}
 
 	}
 	private class PrevListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			if(_slotGroup != null)
 				_replyPanel.prevWeek();
 		}
 
 	}
 	
 	private class EventDispButtonListener implements ActionListener {
 		public void actionPerformed(ActionEvent arg0) {
 			if(_slotGroup != null) {
 				if(_eventDispStyle == PaintMethod.Bars){
 					_eventDispStyle = PaintMethod.HeatMap;
 				} else if(_eventDispStyle == PaintMethod.HeatMap) {
 					_eventDispStyle = PaintMethod.Bars;
 				}
 				
 				if(_eventDispButton.getIcon() == _toggleIcon) {
 					_eventDispButton.setIcon(_toggleIconInverted);
 				}
 				else {
 					_eventDispButton.setIcon(_toggleIcon);
 				}
 		
 				CalendarGui.this.getEvent().setPaintMethod(_eventDispStyle);
 				repaint();
 			}
 		}
 	}
 
 	private class TimeFindListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			if(_slotGroup != null && !_slotGroup.getCalendars().isEmpty() && !(_slotGroup.getCalendars().size() ==1 && _slotGroup.userHasSubmitted()))
 				new SliderPane(_numHours, CalendarGui.this);
 		}
 
 	}
 
 	private class RefreshListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			URL url=null;
 			try {
 				url = new URL("http://www.google.com");
 			} catch (MalformedURLException e) {
 				e.printStackTrace();
 			}
 			if (Communicator.webConnected(url)) {
 
 				_communicator.refresh();
 				// Retrieve this when2meet in case it has changed
 				if(_slotGroup != null){
 					setEvent(_communicator.getEvent(""+_slotGroup.getID()));
 					System.out.println("After setting event in GUI");
 					_slotGroup.printUpdates();
 					System.out.println("=====");
 				}
 				
 				setUserCal(_communicator.getUserCal());
 				_userCalPanel.initLabels();
 				_refreshButton.setSelected(false);
 				repaint();
 			}
 			else {
 				ImageIcon grey = new ImageIcon("small_logo_button.png");
 				JOptionPane.showMessageDialog(null, "You are not connected to the Internet.\nKairos cannot import current data.", "Connection Error", JOptionPane.ERROR_MESSAGE, grey);
 			}
 		}
 
 
 
 	}
 
 }
