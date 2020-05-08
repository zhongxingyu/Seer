 //////////////////////////////////////////////////////////////////////////////////
 // Class: 	TabPlugFlowReactor
 //
 // Purpose: This class implements all the visual elements in the Plug Flow Reactor
 //			tab of the final application.  It is comprised of a 
 //			FluidFlowReactorPanel, a JTextArea for data, text fields for entering
 //			parameters, and buttons for controlling the reactor
 //
 //////////////////////////////////////////////////////////////////////////////////
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Panel;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusAdapter;
 import java.awt.event.FocusEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.border.LineBorder;
 import javax.swing.SwingConstants;
 
 public class TabPlugFlowReactor extends JPanel 
 {
 	private static final long serialVersionUID = 1L;
 	private JTextField txtInitialConcentration;
 	private JTextField txtRateConstant;	
 	private JTextArea txtConcentrationLog;
 	private JTextField txtTimeRate;
 	private JTextField txtParticleMoveRate;
 	private PFR  pfrPanel;
 	private Preferences preferences;
 	private JSlider flowRateSlider;
 	private JLabel concentrationLabel;
 	private JLabel percentageLabel;
 	
 	//Constructors all use the initialize() method
 	public TabPlugFlowReactor() {
 		initialize();
 	}
 	
 	public TabPlugFlowReactor(Preferences pref) {
 		preferences = pref;
 		initialize();
 	}
 
 	//Constructs all objects in the Plug Flow Reactor Tab of the application
 	private void initialize() 
 	{
 		setBounds(0, 0, 800, 600);
 		setLayout(null);
 		
 		//Create the reactor panel/set appearance based on preferences
 		pfrPanel = new PFR(preferences);
 		pfrPanel.setBounds(65, 307, 626, 209);
 		pfrPanel.setBorder(new LineBorder(new Color(0, 0, 0), 2));
 		add(pfrPanel);
 		pfrPanel.setLayout(null);
 		
 		
 		
 		//Create Labels for text fields
 		JLabel lblInitialConcentration = new JLabel("Initial Concentration");
 		lblInitialConcentration.setFont(new Font("Tahoma", Font.BOLD, 11));
 		lblInitialConcentration.setBounds(30, 137, 192, 15);
 		add(lblInitialConcentration);
 		
 		JLabel lblRateConstant = new JLabel("Rate Constant per Minute");
 		lblRateConstant.setFont(new Font("Tahoma", Font.BOLD, 11));
 		lblRateConstant.setBounds(30, 163, 190, 14);
 		add(lblRateConstant);
 		
 		
 		//Create text fields
 		txtInitialConcentration = new JTextField();
 		lblInitialConcentration.setLabelFor(txtInitialConcentration);
 		txtInitialConcentration.setText(preferences.getPlugFlowInitialC().toString());
 		txtInitialConcentration.setBounds(232, 130, 114, 19);
 		add(txtInitialConcentration);
 		
 		txtTimeRate = new JTextField();
 		txtTimeRate.addFocusListener(new FocusAdapter() {
 			@Override
 			public void focusLost(FocusEvent e) {
 				pfrPanel.setReactionTimeRate(Integer.parseInt(txtTimeRate.getText()));
 			}
 		});
 		txtTimeRate.setText(preferences.getPlugFlowTimeStep().toString());
 		txtTimeRate.setBounds(239, 152, 114, 19);
 		//add(txtTimeRate);
 		txtTimeRate.setColumns(10);
 		
 		txtParticleMoveRate = new JTextField();
 		txtParticleMoveRate.addFocusListener(new FocusAdapter() {
 			@Override
 			public void focusLost(FocusEvent e) {
 				pfrPanel.setParticleMoveRate(Integer.parseInt(txtParticleMoveRate.getText()));
 			}
 		});
 		txtParticleMoveRate.setText(preferences.getPlugFlowMotionRate().toString());
 		txtParticleMoveRate.setBounds(239, 183, 114, 19);
 		//add(txtParticleMoveRate);
 		txtParticleMoveRate.setColumns(10);
 		
 		txtInitialConcentration.addFocusListener(new FocusAdapter() 
 		{
 			@Override
 			public void focusLost(FocusEvent e) 
 			{
 				resetReactor();
 			}
 		});
 
 		txtRateConstant = new JTextField();
 		txtRateConstant.addFocusListener(new FocusAdapter() {
 			@Override
 			public void focusLost(FocusEvent e) {
 				resetReactor();
 			}
 		});
 		txtRateConstant.setText(preferences.getPlugFlowInitialK().toString());
 		txtRateConstant.setBounds(232, 157, 114, 20);
 		add(txtRateConstant);
 		txtRateConstant.setColumns(10);
 		
 		//Create reactor control buttons
 		JButton btnGo = new JButton("Go");
 		btnGo.addActionListener(new ButtonGoListener());
 		btnGo.setBackground(new Color(0, 128, 0));
 		btnGo.setForeground(new Color(240, 255, 255));
 		btnGo.setFont(new Font("Tahoma", Font.BOLD, 28));
 		btnGo.setBounds(65, 11, 174, 71);
 		add(btnGo);
 
 		JButton btnStop = new JButton("Stop");
 		btnStop.addActionListener(new ButtonStopListener());
 		btnStop.setBackground(new Color(255, 0, 0));
 		btnStop.setForeground(new Color(240, 255, 255));
 		btnStop.setFont(new Font("Tahoma", Font.BOLD, 28));
 		btnStop.setBounds(309, 11, 174, 71);
 		add(btnStop);
 
 		JButton btnReset = new JButton("Reset");
 		btnReset.setForeground(new Color(240, 255, 255));
 		btnReset.setBackground(new Color(184, 134, 11));
 		btnReset.setFont(new Font("Tahoma", Font.BOLD, 28));
 		btnReset.addActionListener(new ButtonResetListener());
 		btnReset.setBounds(551, 11, 174, 71);
 		add(btnReset);
 		
 		//Create Toggle Flow button
 		JButton btnToggleFlow = new JButton("Toggle Flow");
 		btnToggleFlow.addActionListener(new ActionListener() 
 		{
 			public void actionPerformed(ActionEvent arg0) 
 			{
 				pfrPanel.toggleFlowing();
 			}
 		});
 		btnToggleFlow.setBounds(453, 273, 126, 23);
 		add(btnToggleFlow);
 		pfrPanel.setParticleMoveRate(Integer.parseInt(txtParticleMoveRate.getText()));
 		pfrPanel.setReactionTimeRate(Integer.parseInt(txtTimeRate.getText()));
 		pfrPanel.setInitialConcentration(Integer.parseInt(txtInitialConcentration.getText()));
 		
 		flowRateSlider = new JSlider();
 		flowRateSlider.setMaximum(300);
 		flowRateSlider.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseReleased(MouseEvent e) {
 				setFlowRate();
 			}
 		});
 
 
 		flowRateSlider.setBounds(114, 280, 200, 16);
 		add(flowRateSlider);
 		
 		JLabel lblFlowRate = new JLabel("Flow Rate");
 		lblFlowRate.setBounds(122, 260, 192, 15);
 		add(lblFlowRate);
 
 		//Create Apply button to apply new parameters entered by user in
 		//text fields		
 		JButton applyButton = new JButton("Apply");
 		applyButton.setFont(new Font("Tahoma", Font.BOLD, 15));
 		applyButton.setToolTipText("Click here to apply parameter changes");
 		applyButton.setBounds(225, 192, 89, 33);
 		applyButton.addActionListener(new ApplyButtonListener());
 		add(applyButton);
 		
 		//Just a cosmetic panel
 		Panel panel_1 = new Panel();
 		panel_1.setBackground(new Color(176, 196, 222));
 		panel_1.setBounds(0, 0, 800, 91);
 		add(panel_1);
 		
 		concentrationLabel = new JLabel("-");
 		concentrationLabel.setBounds(645, 528, 46, 14);
 		add(concentrationLabel);
 		
 		percentageLabel = new JLabel("-");
 		percentageLabel.setBounds(645, 554, 46, 14);
 		add(percentageLabel);
 		
 		pfrPanel.setPercentageLabel(percentageLabel);
 		pfrPanel.setConcentrationLabel(concentrationLabel);
 		
 		JLabel label = new JLabel("Current Concentration:");
 		label.setHorizontalAlignment(SwingConstants.RIGHT);
 		label.setFont(new Font("Dialog", Font.BOLD, 14));
 		label.setBounds(399, 528, 234, 18);
 		add(label);
 		
 		JLabel lblPercentageOfOutflow = new JLabel("Percentage of outflow Concentation:");
 		lblPercentageOfOutflow.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblPercentageOfOutflow.setFont(new Font("Dialog", Font.BOLD, 14));
 		lblPercentageOfOutflow.setBounds(328, 554, 305, 14);
 		add(lblPercentageOfOutflow);
 		
 		resetReactor();		
 	}
 
 	private void setFlowRate(){
 		System.out.println(flowRateSlider.getValue());
 		pfrPanel.setMoveTime(flowRateSlider.getValue() );
 	}
 	
 	//Resets the reactor by reseting the initial concentration
 	//and reaction constant figures and dynamic label figures, 
 	//then remaking the dots and repainting
 	private void resetReactor() 
 	{
		pfrPanel.setAnimationTimers(Integer.parseInt(txtParticleMoveRate.getText()));
		pfrPanel.setInitialConcentration(Integer.parseInt(txtInitialConcentration.getText()));
		pfrPanel.setRateConstant(Double.parseDouble(txtRateConstant.getText()));
 	}
 
 	//Listener for the Go button
 	class ButtonGoListener implements ActionListener 
 	{
 		public void actionPerformed(ActionEvent evt) 
 		{
 			if(pfrPanel.isFlowing() == true && pfrPanel.isStarted() == false)
 			{
 			pfrPanel.setAnimationTimers(Integer.parseInt(txtParticleMoveRate.getText()));
 			pfrPanel.startAnimation();
 			pfrPanel.beginAnimation();
 			}
 		}
 	}
 
 	//Listener for the Stop button
 	class ButtonStopListener implements ActionListener 
 	{
 
 		public void actionPerformed(ActionEvent evt)
 		{
 			pfrPanel.stopReactor();
 			pfrPanel.stopAnimation();
 		}
 
 	}
 
 	//Listener for the Reset button
 	class ButtonResetListener implements ActionListener 
 	{
 		public void actionPerformed(ActionEvent evt) 
 		{
 			resetReactor();
 			pfrPanel.resetBatchReactorLocation();
 		}
 
 	}
 	
 	//Listener for the apply changes button
 	class ApplyButtonListener implements ActionListener
 	{
 		public void actionPerformed(ActionEvent evt) 
 		{
 			pfrPanel.setInitialConcentration(Integer.parseInt(txtInitialConcentration.getText()));
 			pfrPanel.setRateConstant(Double.parseDouble(txtRateConstant.getText()));
 		}
 	}
 }
