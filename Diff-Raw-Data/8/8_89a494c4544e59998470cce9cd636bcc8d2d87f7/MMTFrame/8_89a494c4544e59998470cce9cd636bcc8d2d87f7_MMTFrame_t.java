 package org.ndaguan.micromanager.mmtracker;
 
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.event.ActionListener;
 
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JRadioButtonMenuItem;
 import javax.swing.JSeparator;
 import javax.swing.JToolBar;
 import javax.swing.SwingConstants;
 
 import org.micromanager.MMStudioMainFrame;
 
 
 public class MMTFrame extends JFrame {
 	private static MMTFrame instance_;
 
  
 	private static final long serialVersionUID = 1L;
 	final String[] PARALIST  = new String[]{"BallRadius","DNALength","ZCalScale_","ZCalStep","RinterStep","HalfCorrWin","MagnetStep","Frame2Acq","FrameCalcF","ITEM0","ITEM1","ITEM2"};
 
 	private final int DEFAULT_WIDTH = 720;
 	private final int DEFAULT_HEIGHT =130;
 	private final int DEFAULT_LOCATION_X = 0;
 	private final int DEFAULT_LOCATION_Y = 10;
	private final String DEFAULT_TITLE = MMT.DEFAULT_TITLE;;
 	private final String DEFAULT_IMAGE = "icon/I.gif";
 	private final int DEFAULT_CLOSE_OPERATION =JFrame.HIDE_ON_CLOSE;
 
 	private ButtonGroup buttonGroup_0 = new ButtonGroup();
 	ActionListener menuListener = null;
 
 	public JRadioButtonMenuItem MagnetAuto;
 	public  JRadioButtonMenuItem MagnetManual;
 	private static Listener listener_;
 	public PreferDailog preferDailog;
 	public StageControlFrame myStageControlFrame_;
 
 
 	public JMenuItem LiveView;
 
 
 	public JMenuItem MACQ;
 
 	private JButton butCalibration ;
 	private JButton butLiveView;
 	public JLabel infomation_;
 	private Image imgC;
 	private Image imgL;
 	private Image imgXC;
 	private Image imgXL;
 
 
 
 
 	public static MMTFrame getInstance(MMStudioMainFrame app, Listener listener, Preferences preferences) {
 		if(instance_ == null)
 			instance_ = new MMTFrame(app,listener,preferences);
 		return instance_;
 	}
 	public static MMTFrame getInstance() {		
 		return instance_;
 	}
 
 	public static void main(String[] arg){
 		MMTFrame zm = new MMTFrame(null,null,null);
 		zm.setVisible(true);
 	}
 	public MMTFrame(MMStudioMainFrame app, Listener listener, Preferences preferences) {
 
 		listener_ = listener;
 		if (myStageControlFrame_ == null)
 			myStageControlFrame_ = new StageControlFrame(app);
 
 		preferDailog = new PreferDailog(preferences);
 		initialize();
 		//Set Looking
 		setTitle(DEFAULT_TITLE);
 		Toolkit kit = Toolkit.getDefaultToolkit();
 		Image img = kit.getImage(DEFAULT_IMAGE);
 		setIconImage(img);
 		setDefaultCloseOperation(DEFAULT_CLOSE_OPERATION);
 		ij.ImageJ.getFrames()[0].setBounds(DEFAULT_WIDTH,0,560,120);
 //		ij.ImageJ.getFrames()[0].setVisible(false);
 		setBounds(DEFAULT_LOCATION_X,DEFAULT_LOCATION_Y,DEFAULT_WIDTH,DEFAULT_HEIGHT);
 		app.setBounds(DEFAULT_LOCATION_X,DEFAULT_LOCATION_Y+DEFAULT_HEIGHT+5,DEFAULT_WIDTH,DEFAULT_WIDTH);
 	}
 
 
 	private void initialize() {
 
 		getContentPane().setLayout(null);
 
 
 		final JMenu ROI = new JMenu("ROI");//ROI	
 		final JMenuItem Add = new JMenuItem("Add");	
 		Add.setToolTipText("Add ROI");
 		Add.addActionListener(listener_);
 		ROI.add(Add);
 		final JMenuItem Delete = new JMenuItem("Delete ROI");	
 		Delete.setToolTipText("Delete ROI");
 		Delete.addActionListener(listener_);
 		ROI.add(Delete);
 		final JMenuItem SetPref = new JMenuItem("SetPref");	
 		SetPref.setToolTipText("Set Current ROI as Preferences");
 		SetPref.addActionListener(listener_);
 		ROI.add(SetPref);
 		final JMenuItem ShowChart = new JMenuItem("ShowChart");
 		ShowChart.setToolTipText("Show Detail in Chart");
 		ShowChart.addActionListener(listener_);
 		ROI.add(ShowChart);
 
 		final JMenu Capture = new JMenu("Capture");//ROI	
 		LiveView = new JMenuItem("Live View");	
 		LiveView.setToolTipText("Capture under Live View(fast)");
 		LiveView.addActionListener(listener_);
 		LiveView.setEnabled(false);
 		Capture.add(LiveView);
 		MACQ = new JMenuItem("MultACQ");	
 		MACQ.setToolTipText("Capture under Multi-DACQ(full)");
 		MACQ.setEnabled(false);
 		MACQ.addActionListener(listener_);
 		Capture.add(MACQ);
 
 
 		final JMenu Option = new JMenu("Option");//Option		
 		final JMenuItem Preferences = new JMenuItem("Preferences");		
 		final JMenu Magnet = new JMenu("Magnet");
 		MagnetAuto = new JRadioButtonMenuItem("Auto");		
 		MagnetManual = new JRadioButtonMenuItem("Manual");
 		buttonGroup_0.add(MagnetAuto);
 		buttonGroup_0.add(MagnetManual);
 
 		Magnet.add(MagnetAuto);
 		Magnet.addSeparator();
 		Magnet.add(MagnetManual);	
 
 		final JMenu Other = new JMenu("Other");//Option		
 		final JMenuItem guiHide = new JMenuItem("guiHide/show");		
 		guiHide.setToolTipText("hide/show gui");	
 		guiHide.addActionListener(listener_);
 		Other.add(guiHide);
 
 		Preferences.addActionListener(listener_);
 		MagnetManual.addActionListener(listener_);
 		Preferences.setToolTipText("Preferences");
 		MagnetManual.setToolTipText("MagnetManual");
 		Option.add(Preferences);
 		Option.addSeparator();			
 		Option.add(Magnet);
 
 		final JMenuBar menuBar = new JMenuBar();
 		setJMenuBar(menuBar);
 		menuBar.add(ROI);
 		menuBar.add(Capture);
 		menuBar.add(Option);
 		menuBar.add(Other);
 
 		final int toolItemWidth = 120;
 		final int toolItemHeight = 20;
 		final int toolbarheight = toolItemHeight+10;
 		final JToolBar toolBar = new JToolBar();
 		toolBar.setBounds(0,0, DEFAULT_WIDTH, toolbarheight);
 
 		Toolkit kit = Toolkit.getDefaultToolkit();
 
 		imgC = kit.getImage("icons/C.gif");
 		imgL = kit.getImage("icons/L.gif");
 		imgXC = kit.getImage("icons/XC.gif");
 		imgXL = kit.getImage("icons/XL.gif");
 
 		final	Image imgS = kit.getImage("icons/S.gif");
 		final	Image imgH = kit.getImage("icons/H.gif");
 		final	Image imgAuto = kit.getImage("icons/Auto.gif");
 
 		butCalibration = new JButton();
 		butLiveView = new JButton();
 		JButton butSelectROI = new JButton();
 		JButton butHandTool = new JButton();
 		JButton butAuto = new JButton();
 
 
 		int offsetx = 0;
 		butAuto.setIcon(new javax.swing.ImageIcon(imgAuto)); // NOI18N
 		butAuto.setToolTipText("Auto Contrast");
 		butAuto.setFocusable(false);
 		butAuto.setBounds(offsetx,0, toolItemWidth, toolItemHeight);
 		offsetx += toolItemWidth;	
 
 
 		butCalibration.setIcon(new javax.swing.ImageIcon(imgC)); // NOI18N
 		butCalibration.setToolTipText("Calibrate");
 		butCalibration.setFocusable(false);
 		butCalibration.setBounds(offsetx,0, toolItemWidth, toolItemHeight);
 		offsetx += toolItemWidth;
 
 		butLiveView.setIcon(new javax.swing.ImageIcon(imgL)); // NOI18N
 		butLiveView.setToolTipText("Live View");
 		butLiveView.setFocusable(false);
 		butLiveView.setBounds(offsetx,0, toolItemWidth, toolItemHeight);
 		offsetx += toolItemWidth;
 
 		butSelectROI.setIcon(new javax.swing.ImageIcon(imgS)); // NOI18N
 		butSelectROI.setToolTipText("Rectangle tool for select a ROI");
 		butSelectROI.setFocusable(false);
 		butSelectROI.setBounds(offsetx,0, toolItemWidth, toolItemHeight);
 		offsetx += toolItemWidth;
 
 		butHandTool.setIcon(new javax.swing.ImageIcon(imgH)); // NOI18N
 		butHandTool.setToolTipText("Hand tool for moving the xyStage");
 		butHandTool.setFocusable(false);
 		butHandTool.setBounds(offsetx,0, toolItemWidth, toolItemHeight);
 		offsetx += toolItemWidth;
 
 		final JSeparator separator2 = new JSeparator(SwingConstants.VERTICAL);
 		separator2.setBounds(offsetx+10,0,1, 10);
 
 
 		butLiveView.addActionListener(listener_); 		 
 		butCalibration.addActionListener(listener_); 
 		butSelectROI.addActionListener(listener_); 			 
 		butHandTool.addActionListener(listener_);  
 		butAuto.addActionListener(listener_); 
 
 		toolBar.add(butCalibration);
 		toolBar.add(butLiveView);
 		toolBar.add(butAuto);
 		toolBar.add(butSelectROI);
 		toolBar.add(butHandTool);
 		toolBar.add(separator2);
 
 		getContentPane().add(toolBar);
 		final JSeparator separator3 = new JSeparator(SwingConstants.HORIZONTAL);
 		separator3.setBounds(0,38,DEFAULT_WIDTH ,toolItemHeight);
 		getContentPane().add(separator3);
 		infomation_ = new JLabel("Everything is fine by now");
 		infomation_.setBounds(20, 42, DEFAULT_WIDTH,  toolItemHeight);
 		getContentPane().add(infomation_);
 
 		// MagnetManualDialBox
 
 	}
 	public void setCalibrateIcon(boolean flag)
 	{
 		if(flag){
 			butCalibration.setIcon(new javax.swing.ImageIcon(imgC)); // NOI18N
 		}
 		else{
 			butCalibration.setIcon(new javax.swing.ImageIcon(imgXC)); // NOI18N
 		}
 	}
 	public void setLiveViewIcon(boolean flag)
 	{
 		if(flag){
 			butLiveView.setIcon(new javax.swing.ImageIcon(imgL)); // NOI18N
 		}
 		else{
 			butLiveView.setIcon(new javax.swing.ImageIcon(imgXL)); // NOI18N
 		}
 	}
 
 	public void LiveViewCaptureEnable(boolean flag)
 	{
 		LiveView.setEnabled(flag);
 	}
 	public void MultCaptureEnable(boolean flag)
 	{
 		MACQ.setEnabled(flag);
 	}
 	public boolean isMagnetAuto() {
 		return MagnetAuto.isSelected();
 	}
 
 
 
 }
