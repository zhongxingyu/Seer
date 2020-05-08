 package chalmers.dax021308.ecosystem.view;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 import javax.swing.BoxLayout;
 import javax.swing.JMenuBar;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JMenuItem;
 import javax.swing.JMenu;
 
 import chalmers.dax021308.ecosystem.controller.WindowController;
 import chalmers.dax021308.ecosystem.model.environment.EcoWorld;
 
 import java.awt.event.ActionEvent;
 
 /**
  * The view that holds the entire application.
  * 
  * @author Hanna
  *
  */
 
 public class MainWindow extends JFrame implements IView {
 	private static final long serialVersionUID = -8023060073777907757L;
 	private JPanel contentPane;
 	private ParameterView parameterView = new ParameterView(); 
 	private ControlView controlView;
 	private JPanel simulationPanel = new JPanel();
 	private JPanel left = new JPanel();
 	private JPanel right = new JPanel();
 	private SettingsMenuView smv = new SettingsMenuView(this);
 	private AWTSimulationView awt;
 	private OpenGLSimulationView openGL;
 	private HeatMapView heatMap;
 	private GraphPopulationAmountView graphView1;  //M�ste ta in en ekomodell, + �r jframe nu
 	//private GraphView graphView2 = new GraphView();  //M�ste ta in en ekomodell, + �r jframe nu
 	
 
 	/**
 	 * Create the frame.
 	 */
 	public MainWindow(EcoWorld model) {
 		setTitle("Simulated Ecosystem");
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 613, 516);
 		this.setExtendedState(MAXIMIZED_BOTH);
 		new NewSimulationView(model);
 		//OpenGL   
 	    Dimension d = model.getSize();
 		openGL = new OpenGLSimulationView(model, d, true);
 		openGL.init();
 		openGL.setSize(new Dimension(980,700));
		heatMap = new HeatMapView(model, d, 100, "Deers");
 		//
 		controlView = new ControlView(model);
 		graphView1 = new GraphPopulationAmountView(model);
 		
 		JMenuBar menuBar = new JMenuBar();
 		setJMenuBar(menuBar);
 		
 		JMenu mnFile = new JMenu("File");
 		menuBar.add(mnFile);
 		
 		JMenuItem mntmLoad = new JMenuItem("Load simulation");
 		mnFile.add(mntmLoad);
 		
 		JMenuItem mntmSave = new JMenuItem("Save simulation");
 		mnFile.add(mntmSave);
 		
 		JMenuItem mntmExit = new JMenuItem("Exit");
 		mnFile.add(mntmExit);
 		
 		JMenu mnControls = new JMenu("Controls");
 		menuBar.add(mnControls);
 		
 		JMenuItem mntmStart = new JMenuItem("Start");
 		mnControls.add(mntmStart);
 		
 		JMenuItem mntmStop = new JMenuItem("Stop");
 		mnControls.add(mntmStop);
 		
 		JMenuItem mntmPause = new JMenuItem("Pause");
 		mnControls.add(mntmPause);
 		
 		JMenu mnSettings = new JMenu("Settings");
 		menuBar.add(mnSettings);
 		
 		JMenuItem mntmSimulationSettings = new JMenuItem("Simulation settings");
 		//Only this in this class should be in the Controller
 		mntmSimulationSettings.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				smv.setVisible(true);
 			}
 		});
 		mnSettings.add(mntmSimulationSettings);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		contentPane.setLayout(new BorderLayout(0, 0));
 		left.setLayout(new BorderLayout(0,0));
 		
 		/*
 		 * Quick fix...
 		 */
 		right.setLayout(new GridLayout(3,1));
 //		GridBagConstraints gbc = new GridBagConstraints();
 //		gbc.gridx = 0;
 //		gbc.gridwidth = gbc.gridheight = 1;
 //		gbc.fill = GridBagConstraints.HORIZONTAL; 
 //		gbc.gridy = GridBagConstraints.RELATIVE; //makes sure every new add is placed beneath the previous
 //		gbc.anchor = GridBagConstraints.PAGE_START;
 		setContentPane(contentPane);
 		
 //		simulationPanel.setSize(d);
 		simulationPanel.add(openGL);
 		simulationPanel.setBackground(Color.RED);
 		left.add(simulationPanel, BorderLayout.CENTER);
 		left.add(controlView, BorderLayout.SOUTH);  
 		right.add(parameterView, BorderLayout.CENTER);
 		//graphView1.setSize(200, 200);
 		right.add(graphView1, BorderLayout.SOUTH);
 		right.add(heatMap, BorderLayout.SOUTH); 
 		right.setBackground(Color.BLUE);
 		parameterView.setBackground(Color.GREEN);
 		
 		contentPane.add(left, BorderLayout.CENTER);
 		contentPane.add(right, BorderLayout.EAST);
 		
 		//contentPane.add(graphView2);
 	}
 
 	@Override
 	public void propertyChange(PropertyChangeEvent evt) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void init() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void addController(ActionListener controller) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void onTick() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void release() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	public void setSimulationPanel(int i) {
 		if(i == 0) {
 			simulationPanel.add(awt);
 		}
 		else if(i == 1) {
 			simulationPanel.add(openGL);
 		}
 	}
 }
