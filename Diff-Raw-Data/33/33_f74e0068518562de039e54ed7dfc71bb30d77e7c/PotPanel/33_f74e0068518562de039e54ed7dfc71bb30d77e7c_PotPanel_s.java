 package iceepotpc.ui;
 
 
 import iceepotpc.appication.Context;
 import iceepotpc.appication.Pot;
 import iceepotpc.charteng.ChartCreator;
 import iceepotpc.servergw.Meauserement;
 import iceepotpc.servergw.Server;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.ScrollPaneConstants;
 
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 
 import java.awt.event.ItemListener;
 import java.awt.event.ItemEvent;
 import java.awt.Component;
 import javax.swing.border.CompoundBorder;
 import java.awt.ComponentOrientation;
 import java.awt.Dimension;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.factories.FormFactory;
 import com.jgoodies.forms.layout.RowSpec;
 import java.awt.GridBagLayout;
 import java.awt.GridBagConstraints;
 import java.awt.Insets;
 import javax.swing.border.LineBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import java.awt.Color;
 import javax.swing.JProgressBar;
 import javax.swing.JSlider;
 
 /**
  * @author tsantakis A tab where information and inputs per pot is displayed.
  * 
  */
 public class PotPanel extends JPanel{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	private String[] availableMonths = { "Jan", "Feb", "Mar", "Apr", "May",
 			"Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
 	private String[] availableYears = {"2013", "2014" };
 
 	private JTextField txtLastValue;
 	private JTextField txtLastTime;
 	private JTextArea txtResults;
 	private JButton btnGet;
 	private JProgressBar prgBarGetting;
 	
 	private ChartPanel pnlChart;
 	private JScrollPane pnlResults;
 
 	DateFormat df = new SimpleDateFormat();
 	
 	private Calendar from;
 	private Calendar to;
 	
 	ArrayList<Meauserement> measurements = null;
 	
 	private Pot pot;
 	private JFrame frame;
 	private JTextField txtMinMoistDispl;
 	private JTextField txtMaxMoistDispl;
 
 	/**
 	 * Create the panel.
 	 * @throws Exception 
 	 */
 	public PotPanel(Pot p, final JFrame frame) throws Exception {
 		
 		this.frame = frame;
 		pot = p;
 		
 		final Context c = Context.getInstance();
 		setSize(new Dimension(900, 780));
 		setMinimumSize(new Dimension(900, 780));
 		this.setToolTipText("");
 		GridBagLayout gridBagLayout = new GridBagLayout();
 		gridBagLayout.columnWidths = new int[] { 200, 620 };
 		gridBagLayout.rowHeights = new int[] {29, 450, 85, 0};
 		gridBagLayout.columnWeights = new double[] { 0.0, 1.0 };
 		gridBagLayout.rowWeights = new double[] { 0.0, 1.0, 1.0,
 				Double.MIN_VALUE };
 		setLayout(gridBagLayout);
 
 		// last values panel
 		JPanel pnlLastValues = new JPanel();
 		pnlLastValues.setPreferredSize(new Dimension(410, 20));
 		pnlLastValues.setMinimumSize(new Dimension(410, 20));
 		pnlLastValues.setMaximumSize(new Dimension(410, 20));
 		pnlLastValues
 				.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
 		pnlLastValues.setAlignmentY(Component.TOP_ALIGNMENT);
 		pnlLastValues.setAlignmentX(Component.LEFT_ALIGNMENT);
 		GridBagConstraints gbc_pnlLastValues = new GridBagConstraints();
 		gbc_pnlLastValues.gridwidth = 2;
 		gbc_pnlLastValues.insets = new Insets(0, 0, 5, 5);
 		gbc_pnlLastValues.ipady = 5;
 		gbc_pnlLastValues.ipadx = 5;
 		gbc_pnlLastValues.anchor = GridBagConstraints.NORTHWEST;
 		gbc_pnlLastValues.gridx = 0;
 		gbc_pnlLastValues.gridy = 0;
 		add(pnlLastValues, gbc_pnlLastValues);
 		GridBagLayout gbl_pnlLastValues = new GridBagLayout();
 		gbl_pnlLastValues.columnWidths = new int[] { 150, 90, 40, 120 };
 		gbl_pnlLastValues.rowHeights = new int[] { 20 };
 		gbl_pnlLastValues.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0 };
 		gbl_pnlLastValues.rowWeights = new double[] { 0.0 };
 		pnlLastValues.setLayout(gbl_pnlLastValues);
 
 		JLabel lblLastMeasruement = new JLabel("Last measurement");
 		lblLastMeasruement.setPreferredSize(new Dimension(130, 20));
 		lblLastMeasruement.setSize(new Dimension(130, 20));
 		lblLastMeasruement.setMinimumSize(new Dimension(130, 20));
 		lblLastMeasruement.setMaximumSize(new Dimension(130, 20));
 		lblLastMeasruement.setBorder(new CompoundBorder());
 		GridBagConstraints gbc_lblLastMeasruement = new GridBagConstraints();
 		gbc_lblLastMeasruement.anchor = GridBagConstraints.EAST;
 		gbc_lblLastMeasruement.fill = GridBagConstraints.VERTICAL;
 		gbc_lblLastMeasruement.insets = new Insets(0, 0, 0, 5);
 		gbc_lblLastMeasruement.gridx = 0;
 		gbc_lblLastMeasruement.gridy = 0;
 		pnlLastValues.add(lblLastMeasruement, gbc_lblLastMeasruement);
 
 		txtLastValue = new JTextField();
 		txtLastValue.setColumns(10);
 		txtLastValue.setPreferredSize(new Dimension(90, 20));
 		txtLastValue.setMinimumSize(new Dimension(90, 20));
 		txtLastValue.setMaximumSize(new Dimension(90, 20));
 		GridBagConstraints gbc_txtLastValue = new GridBagConstraints();
 		gbc_txtLastValue.fill = GridBagConstraints.BOTH;
 		gbc_txtLastValue.gridx = 1;
 		gbc_txtLastValue.gridy = 0;
 		pnlLastValues.add(txtLastValue, gbc_txtLastValue);
 		txtLastValue.setEditable(false);
 
 		JLabel lblAt = new JLabel("at");
 		lblAt.setPreferredSize(new Dimension(40, 20));
 		lblAt.setMinimumSize(new Dimension(40, 20));
 		lblAt.setMaximumSize(new Dimension(40, 20));
 		lblAt.setAlignmentX(Component.RIGHT_ALIGNMENT);
 		GridBagConstraints gbc_lblAt = new GridBagConstraints();
 		gbc_lblAt.anchor = GridBagConstraints.EAST;
 		gbc_lblAt.fill = GridBagConstraints.VERTICAL;
 		gbc_lblAt.insets = new Insets(0, 5, 0, 5);
 		gbc_lblAt.gridx = 2;
 		gbc_lblAt.gridy = 0;
 		pnlLastValues.add(lblAt, gbc_lblAt);
 
 		txtLastTime = new JTextField();
 		txtLastTime.setSize(new Dimension(120, 20));
 		txtLastTime.setPreferredSize(new Dimension(120, 20));
 		txtLastTime.setMinimumSize(new Dimension(120, 20));
 		txtLastTime.setMaximumSize(new Dimension(120, 20));
 		GridBagConstraints gbc_txtLastTime = new GridBagConstraints();
 		gbc_txtLastTime.fill = GridBagConstraints.BOTH;
 		gbc_txtLastTime.gridx = 3;
 		gbc_txtLastTime.gridy = 0;
 		pnlLastValues.add(txtLastTime, gbc_txtLastTime);
 		txtLastTime.setColumns(10);
 		txtLastTime.setEditable(false);
 
 		// panel results
 		txtResults = new JTextArea();
 		txtResults.setEditable(false);
 		txtResults.setTabSize(2);
 		
 
 		pnlResults = new JScrollPane(txtResults);
 		pnlResults.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 		pnlResults.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		GridBagConstraints gbc_pnlResults = new GridBagConstraints();
 		gbc_pnlResults.fill = GridBagConstraints.BOTH;
 		gbc_pnlResults.insets = new Insets(5, 5, 5, 5);
 		gbc_pnlResults.gridx = 0;
 		gbc_pnlResults.gridy = 1;
 		this.add(pnlResults, gbc_pnlResults);
 		txtResults.setColumns(1);
 		txtResults.setRows(30);
 
 		pnlChart = new ChartPanel(null);
 		pnlChart.setMaximumDrawWidth(2048);
 		pnlChart.setMinimumDrawWidth(620);
 		pnlChart.setMinimumSize(new Dimension(620, 450));
 		pnlChart.setPreferredSize(new Dimension(620, 450));
 		GridBagConstraints gbc_pnlChart = new GridBagConstraints();
 		gbc_pnlChart.fill = GridBagConstraints.BOTH;
 		gbc_pnlChart.insets = new Insets(0, 0, 5, 0);
 		gbc_pnlChart.gridx = 1;
 		gbc_pnlChart.gridy = 1;
 		add(pnlChart, gbc_pnlChart);
 		
 		JPanel pnlMoistLimits = new JPanel();
 		GridBagConstraints gbc_pnlMoistLimits = new GridBagConstraints();
 		gbc_pnlMoistLimits.anchor = GridBagConstraints.SOUTH;
 		gbc_pnlMoistLimits.fill = GridBagConstraints.HORIZONTAL;
 		gbc_pnlMoistLimits.insets = new Insets(0, 5, 5, 5);
 		gbc_pnlMoistLimits.gridx = 0;
 		gbc_pnlMoistLimits.gridy = 2;
 		add(pnlMoistLimits, gbc_pnlMoistLimits);
 		pnlMoistLimits.setLayout(new FormLayout(new ColumnSpec[] {
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,},
 			new RowSpec[] {
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,}));
 		
 		JLabel lblMiniMoistureLevel = new JLabel("Min Moisture Level");
 		pnlMoistLimits.add(lblMiniMoistureLevel, "2, 2, left, default");
 		
 		txtMinMoistDispl = new JTextField();
 		txtMinMoistDispl.setEditable(false);
 		txtMinMoistDispl.setText(String.valueOf(pot.getMinMoistVal()));
 		pnlMoistLimits.add(txtMinMoistDispl, "4, 2, fill, default");
 		txtMinMoistDispl.setColumns(10);
 		
 		final JSlider sldMinMoist = new JSlider();
 		sldMinMoist.setMinimum(0);
 		sldMinMoist.setMaximum(950);
 		sldMinMoist.setValue((int)pot.getMinMoistVal());
 		sldMinMoist.addChangeListener(new ChangeListener() {
 			public void stateChanged(ChangeEvent arg0) {
 				txtMinMoistDispl.setText(String.valueOf(sldMinMoist.getValue()));
 				pot.setMinMoistVal(sldMinMoist.getValue());
 				try {
 					c.updateMoistLimits(pot.getPin(), pot.getMinMoistVal(), pot.getMaxMoistVal());
 				} catch (Exception e) {
 					JOptionPane.showMessageDialog(frame,
 							e.getMessage(),
 							"Warning", JOptionPane.WARNING_MESSAGE);
 				}
 			}
 		});
		pnlMoistLimits.add(sldMinMoist, "2, 4, 3, 1, left, default");
 		
 		
 		JLabel lblMaxMoistureLevel = new JLabel("Max Moisture Level");
 		pnlMoistLimits.add(lblMaxMoistureLevel, "2, 6, left, default");
 		
 		txtMaxMoistDispl = new JTextField();
 		txtMaxMoistDispl.setEditable(false);
 		txtMaxMoistDispl.setText(String.valueOf(pot.getMaxMoistVal()));
 		pnlMoistLimits.add(txtMaxMoistDispl, "4, 6, fill, default");
 		txtMaxMoistDispl.setColumns(10);
 		
 		final JSlider sldMaxMoist = new JSlider();
 		sldMaxMoist.setMinimum(0);
 		sldMaxMoist.setMaximum(950);
 		sldMaxMoist.setValue((int)pot.getMaxMoistVal());
 		sldMaxMoist.addChangeListener(new ChangeListener() {
 			public void stateChanged(ChangeEvent arg0) {
 
 				
 				if(sldMaxMoist.getValue()<sldMinMoist.getValue()){
 					sldMaxMoist.setValue(sldMinMoist.getValue());
 					sldMaxMoist.updateUI();
 				}
 				
 				txtMaxMoistDispl.setText(String.valueOf(sldMaxMoist.getValue()));
 				pot.setMaxMoistVal(sldMaxMoist.getValue());
 				try {
 					c.updateMoistLimits(pot.getPin(), pot.getMinMoistVal(), pot.getMaxMoistVal());
 				} catch (Exception e) {
 					JOptionPane.showMessageDialog(frame,
 							e.getMessage(),
 							"Warning", JOptionPane.WARNING_MESSAGE);
 				}
 			}
 		});
 		
		pnlMoistLimits.add(sldMaxMoist, "2, 8, 3, 1, left, default");
 
 		// criteria panel
 		JPanel pnlCriteria = new JPanel();
 		pnlCriteria.setSize(new Dimension(500, 85));
 		pnlCriteria.setMinimumSize(new Dimension(500, 85));
 		pnlCriteria.setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
 		pnlCriteria.setAlignmentY(Component.BOTTOM_ALIGNMENT);
 		pnlCriteria.setAlignmentX(Component.RIGHT_ALIGNMENT);
 		GridBagConstraints gbc_pnlCriteria = new GridBagConstraints();
 		gbc_pnlCriteria.insets = new Insets(0, 5, 0, 0);
 		gbc_pnlCriteria.anchor = GridBagConstraints.SOUTHEAST;
 		gbc_pnlCriteria.gridx = 1;
 		gbc_pnlCriteria.gridy = 2;
 		add(pnlCriteria, gbc_pnlCriteria);
 		pnlCriteria.setLayout(new FormLayout(new ColumnSpec[] {
 				FormFactory.UNRELATED_GAP_COLSPEC,
 				ColumnSpec.decode("60px"),
 				FormFactory.UNRELATED_GAP_COLSPEC,
 				ColumnSpec.decode("112px"),
 				FormFactory.UNRELATED_GAP_COLSPEC,
 				ColumnSpec.decode("112px"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.UNRELATED_GAP_COLSPEC,},
 			new RowSpec[] {
 				FormFactory.UNRELATED_GAP_ROWSPEC,
 				RowSpec.decode("26px"),
 				FormFactory.UNRELATED_GAP_ROWSPEC,
 				RowSpec.decode("26px"),
 				FormFactory.UNRELATED_GAP_ROWSPEC,}));
 
 		JLabel lblDateFrom = new JLabel("From");
 		pnlCriteria.add(lblDateFrom, "2, 2, fill, fill");
 		final JComboBox cmbMonthFrom = new JComboBox();
 		pnlCriteria.add(cmbMonthFrom, "4, 2, fill, fill");
 		
 		prgBarGetting = new JProgressBar();
 		prgBarGetting.setMaximumSize(new Dimension(109, 23));
 		prgBarGetting.setMinimumSize(new Dimension(109, 23));
 		prgBarGetting.setMinimum(0);
 		prgBarGetting.setVisible(false);
 		pnlCriteria.add(prgBarGetting, "8, 4, fill, center");
 		JLabel lblDateTo = new JLabel("To");
 		pnlCriteria.add(lblDateTo, "2, 4, fill, fill");
 		final JComboBox cmbMonthTo = new JComboBox();
 		pnlCriteria.add(cmbMonthTo, "4, 4, fill, fill");
 		final JComboBox cmbYearTo = new JComboBox();
 		pnlCriteria.add(cmbYearTo, "6, 4, fill, fill");
 
 		btnGet = new JButton("Get Information");
 		pnlCriteria.add(btnGet, "8, 2, fill, fill");
 
 		// handlers
 		this.addComponentListener(new ComponentListener() {
 			
 			@Override
 			public void componentShown(ComponentEvent e) {
 				
 				
 			}
 			
 			@Override
 			public void componentResized(ComponentEvent e) {
 				if(measurements!= null){
 				JFreeChart fc = ChartCreator
 						.createChart(measurements, pot.getMinMoistVal(), pot.getMaxMoistVal());
 				pnlChart.setChart(fc);
 				pnlChart.setVisible(true);
 				}
 				
 			}
 			
 			@Override
 			public void componentMoved(ComponentEvent e) {
 				
 				
 			}
 			
 			@Override
 			public void componentHidden(ComponentEvent e) {
 				
 				
 			}
 		});
 		
 		
 		//month-from dropdown handler
 		cmbMonthFrom.addItemListener(new ItemListener() {
 			public void itemStateChanged(ItemEvent arg0) {
 				cmbMonthTo.setSelectedIndex(cmbMonthFrom.getSelectedIndex());
 			}
 		});
 		final JComboBox cmbYearFrom = new JComboBox();
 		pnlCriteria.add(cmbYearFrom, "6, 2, fill, fill");
 
 		cmbYearFrom.addItemListener(new ItemListener() {
 			public void itemStateChanged(ItemEvent e) {
 				cmbYearTo.setSelectedIndex(cmbYearFrom.getSelectedIndex());
 			}
 		});
 
 		
 		//Get information button
 		btnGet.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				
 				from = Calendar.getInstance();
 				from.set(Calendar.MONTH, cmbMonthFrom.getSelectedIndex() + 1);
 				from.set(Calendar.DAY_OF_MONTH, 1);
 				from.set(Calendar.YEAR, Integer.parseInt((String) cmbYearFrom
 						.getSelectedItem()));
 
 				to = Calendar.getInstance();
 				to.set(Calendar.MONTH, cmbMonthTo.getSelectedIndex() + 1);
 				to.set(Calendar.DAY_OF_MONTH, 1);
 				to.set(Calendar.YEAR,
 						Integer.parseInt((String) cmbYearTo.getSelectedItem()));
 
 				if (from.getTime().getTime() > to.getTime().getTime()) {
 					JOptionPane.showMessageDialog(frame,
 							"The \"From\" date is after the \"To\" one",
 							"Warning", JOptionPane.ERROR_MESSAGE);
 				} else {
 					ProgressBarThread pbt = new ProgressBarThread();
 					Thread t = new Thread(pbt, "getInfoThrd");
 					t.start();
 				}
 			}
 		});
 
 		// filling data
 		for (int i = 0; i < availableMonths.length; i++)
 			cmbMonthTo.addItem(availableMonths[i]);
 		for (int i = 0; i < availableYears.length; i++)
 			cmbYearTo.addItem(availableYears[i]);
 		for (int i = 0; i < availableMonths.length; i++)
 			cmbMonthFrom.addItem(availableMonths[i]);
 		for (int i = 0; i < availableYears.length; i++)
 			cmbYearFrom.addItem(availableYears[i]);
 		
 	}
 	
 	
 	
 	
 	/**
 	 * @author tsantakis
 	 * private internal class which models the separate (from UI) thread
 	 * to be responsible for:
 	 * - make the actual request with the use of Server static methods 
 	 * - the handling of UI elements during  that request (progress bar , buttons etc)
 	 * - fill the UI elements after each successful request
 	 */
 	private class ProgressBarThread implements Runnable{
 		
 		@Override
 		public void run() {
 			
 			SetUIBeforeRequest();
 			
 			measurements = null;
 			try {
 				measurements = new ArrayList<Meauserement>();
 				
 				//for all months in range
 				while((from.get(Calendar.MONTH) != to.get(Calendar.MONTH)) 
 						|| (from.get(Calendar.YEAR) != to.get(Calendar.YEAR))){
 					measurements.addAll(Server.GetMeasurements(from, pot.getPin()));
 					from.add(Calendar.MONTH, 1);
 					prgBarGetting.setValue(prgBarGetting.getValue()+1);
 				}
 				
 				measurements.addAll(Server.GetMeasurements(to, pot.getPin()));
 				prgBarGetting.setValue(prgBarGetting.getValue()+1);
 				
 				//measurements = Server.GetMeasurements(from, to, pot);
 			} catch (Exception e1) {
 				JOptionPane.showMessageDialog(frame, e1.getMessage(),
 						"Warning", JOptionPane.ERROR_MESSAGE);
 			} 
 			
 			//request has finished, now fill the UI
 			if (measurements == null || measurements.size() == 0)
 				JOptionPane.showMessageDialog(frame,
 						"Measurements not available yet",
 						"Warning", JOptionPane.WARNING_MESSAGE);
 			else
 			{
 				for (int i = 0; i < measurements.size(); i++){
 					txtResults.setText(txtResults.getText()
 							+ "\n"
 							+ df.format(new Date(measurements
 									.get(i).getMoment())) + "\t"
 							+ measurements.get(i).getValue());
 					Dimension d = txtResults.getPreferredSize();
 					pnlResults.setPreferredSize(d);
 				}
 				Calendar c = Calendar.getInstance();
 				c.setTimeInMillis((long) measurements.get(
 						measurements.size() - 1).getMoment());
 
 				txtLastTime.setText(df.format(c.getTime()));
 
 				txtLastValue.setText(String.valueOf(measurements
 						.get(measurements.size() - 1).getValue()));
 
 				JFreeChart fc = ChartCreator
 						.createChart(measurements, pot.getMinMoistVal(), pot.getMaxMoistVal());
 				pnlChart.setChart(fc);
 				pnlChart.setVisible(true);
 			}
 			SetUIAfterRequest();
 				
 		}
 				
 		private void SetUIBeforeRequest(){
 			txtResults.setText("");
 			txtLastValue.setText("");
 			txtLastTime.setText("");
 			pnlChart.setVisible(false);
 			btnGet.setEnabled(false);
 			prgBarGetting.setMaximum(MontsToGet(to, from));
 			prgBarGetting.setVisible(true);
 			prgBarGetting.setValue(prgBarGetting.getMinimum());
 		}
 		private void SetUIAfterRequest(){
 			prgBarGetting.setValue(prgBarGetting.getMaximum());
 			prgBarGetting.setVisible(false);
 			btnGet.setEnabled(true);
 		}
 		
 		//  to-from in months
 		private int MontsToGet(Calendar to, Calendar from){
 			
 			int res = 0;
 			double millisPerMonth = 2628000000d;
 			
 			res = (int) Math.ceil((to.getTimeInMillis() - from.getTimeInMillis())/millisPerMonth);
 			
 			return res+1;
 			
 		}
 		
 		
 	}
 
 
 
 
 	/**
 	 * @author tsantakis
 	 * private internal class implementing the asynchronous 
 	 * calling to the static functions defined 
 	 * in Server class
 	 */
 	/*private class ServerThread implements Runnable{
 
 		@Override
 		public void run() {
 			
 			measurements = null;
 			try {
 				measurements = Server.GetMeasurements(from, to, pot);
 			} catch (Exception e1) {
 				JOptionPane.showMessageDialog(frame, e1.getMessage(),
 						"Warning", JOptionPane.ERROR_MESSAGE);
 			} finally{
 				
 			}
 			
 			
 		}
 		
 	}*/
 
 }
