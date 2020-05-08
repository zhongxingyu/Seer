 package br.edu.cultural.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.BorderFactory;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JSlider;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 import javax.swing.border.Border;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.plaf.basic.BasicComboBoxRenderer;
 
 import net.miginfocom.swing.MigLayout;
 import br.edu.cultural.network.CulturalNetwork;
 import br.edu.cultural.network.State;
 import br.edu.cultural.plot.ActiveEdgesScatterPlot;
 import br.edu.cultural.plot.ActiveNodesScatterPlot;
 import br.edu.cultural.plot.ActiveRoomScatterPlot;
 import br.edu.cultural.plot.ActiveRoomsHistogram;
 import br.edu.cultural.plot.CultureDistributionScatterPlot;
 import br.edu.cultural.plot.Plot;
 import br.edu.cultural.plot.ScatterPlotter;
 import br.edu.cultural.simulation.AxelrodSimulation;
 import br.edu.cultural.simulation.BelousovZhabotinskySimulation;
 import br.edu.cultural.simulation.CultureDisseminationSimulation;
 import br.edu.cultural.simulation.FacilitatedDisseminationWithSurfaceTension;
 import br.edu.cultural.simulation.FacilitatedDisseminationWithoutSurfaceTension;
 import br.edu.cultural.simulation.KupermanSimulationOneFeatureOverlap;
 import br.edu.cultural.simulation.KupermanSimulationOverallOverlap;
 import br.edu.cultural.simulation.CultureDisseminationSimulation.SimulationEventAdapter;
 import br.edu.cultural.simulation.CultureDisseminationSimulation.SimulationEventListener;
 import br.edu.cultural.simulation.CultureDisseminationSimulation.SimulationState;
 
 public class MainApplicationFrame extends JFrame {
 
 	/** */
 	private static final long serialVersionUID = -2360890643572400835L;
 
 	@SuppressWarnings("unused")
 	private static final int FRAME_WIDTH = 1000;
 	@SuppressWarnings("unused")
 	private static final int FRAME_HEIGHT = 800;
 	private static final int CANVAS_WIDTH = 800;
 	private static final boolean BORDERS = false;
 
 	private static final String APP_TITLE = "Axelrod Simulation";
 
 	private static JFileChooser fc;
 
 	JComboBox simulationSelect;
 	JCheckBox periodicBoundarySelect;
 	JCheckBox deferredUpdateSelect;
 
 	JMenuBar menuBar = new JMenuBar();
 	JMenu fileMenu = new JMenu("File");
 	JMenu plotMenu = new JMenu("Plot");
 
 	private JList plotsJList;
 	private DefaultListModel plotsListModel;
 	private JList activePlotsJList;
 	private DefaultListModel activePlotsListModel;
 
 	JPanel controls = new JPanel(new MigLayout("fillx"));
 
 	JLabel lLbl = new JLabel("Size:");
 	JTextField lTxtIn = new JTextField("100");
 
 	JLabel fLbl = new JLabel("F:");
 	JTextField fTxtIn = new JTextField("2");
 
 	JLabel qLbl = new JLabel("q:");
 	JTextField qTxtIn = new JTextField("2");
 
 	JLabel paintLbl = new JLabel("Paint with state: ");
 	JTextField paintTxtIn = new JTextField();
 	PaintSample paintSample = new PaintSample();
 
 	JButton resetBtn = new JButton("Reset");
 	JButton toggleSimBtn = new JButton("Start");
 	JButton stepSimBtn = new JButton("Step");
 	JCheckBox enableVisualOut = new JCheckBox("Enable visual representation");
 
 	JSlider speedSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 100);
 	
 	JSlider networkRefreshRateSlider = new JSlider(JSlider.HORIZONTAL, 1, 100, 10);
 
 	JTextArea out;
 
 	// JLabel tLbl = new JLabel("Time: ");
 	JLabel iterationsLbl = new JLabel("Iterations: ");
 	JLabel iterationsLblOut = new JLabel("0");
 	JLabel interactionsLbl = new JLabel("Interactions: ");
 	JLabel interactionsLblOut = new JLabel("0");
 	JLabel epochsLbl = new JLabel("Epochs: ");
 	JLabel epochsLblOut = new JLabel("0");
 
 	public CultureCanvas canvas;
 	public CultureDisseminationSimulation sim;
 	Thread simThr = new Thread(sim);
 
 	private PlotAction activeNodesPlotAction;
 	private PlotAction activeEdgesPlotAction;
 	private PlotAction cultureDistributionPlotAction;
 	private PlotAction activeRoomsPlotAction;
 	private PlotAction activeRoomsHistogramAction;
 
 	public MainApplicationFrame() {
 
 		setTitle(APP_TITLE);
 
 		simulationSelect = new JComboBox();
 		simulationSelect
 				.addItem(FacilitatedDisseminationWithSurfaceTension.class);
 		simulationSelect
 				.addItem(FacilitatedDisseminationWithoutSurfaceTension.class);
 		simulationSelect.addItem(BelousovZhabotinskySimulation.class);
 		simulationSelect.addItem(AxelrodSimulation.class);
 		simulationSelect.addItem(KupermanSimulationOneFeatureOverlap.class);
 		simulationSelect.addItem(KupermanSimulationOverallOverlap.class);
 
 		simulationSelect.setRenderer(new ClassNameComboBoxRenderer());
 		
 		periodicBoundarySelect = new JCheckBox("Periodic boundary condition");
 		deferredUpdateSelect = new JCheckBox("Defer representation updates (optimization)");
		deferredUpdateSelect.setSelected(true);
 
 		resetBtn.addActionListener(resetSim);
 		toggleSimBtn.addActionListener(toggleSim);
 		stepSimBtn.addActionListener(stepSim);
 		enableVisualOut.setSelected(true);
 
 		fileMenu.add(loadFromFile);
 		fileMenu.add(saveToFile);
 		fileMenu.add(quit);
 
 		Dimension listSize = new Dimension(200, 100);
 		plotsListModel = new DefaultListModel();
 		plotsJList = new JList(plotsListModel);
 		plotsJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		plotsJList.setSelectedIndex(0);
 		plotsJList.setVisibleRowCount(5);
 		plotsJList.addMouseListener(plotsListMouseHandler());
 		JScrollPane plotListScrollPane = new JScrollPane(plotsJList);
 		plotListScrollPane.setPreferredSize(listSize);
 
 		activePlotsListModel = new DefaultListModel();
 		activePlotsJList = new JList(activePlotsListModel);
 		activePlotsJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		activePlotsJList.setSelectedIndex(0);
 		activePlotsJList.setVisibleRowCount(5);
 		activePlotsJList.addMouseListener(activePlotsListMouseHandler());
 		JScrollPane activePlotListScrollPane = new JScrollPane(activePlotsJList);
 		activePlotListScrollPane.setPreferredSize(listSize);
 
 		resetSim.actionPerformed(null);
 
 		menuBar.add(fileMenu);
 		menuBar.add(plotMenu);
 
 		lTxtIn.setPreferredSize(new Dimension(60, 18));
 		fTxtIn.setPreferredSize(new Dimension(30, 18));
 		qTxtIn.setPreferredSize(new Dimension(30, 18));
 
 		resetBtn.setPreferredSize(new Dimension(80, 20));
 		toggleSimBtn.setPreferredSize(new Dimension(80, 20));
 		stepSimBtn.setPreferredSize(new Dimension(80, 20));
 
 		enableVisualOut.setPreferredSize(new Dimension(240, 36));
 		speedSlider.setPreferredSize(new Dimension(240, 36));
 		networkRefreshRateSlider.setPreferredSize(new Dimension(240, 36));
 
 		paintTxtIn.setPreferredSize(new Dimension(320, 18));
 		paintSample.setPreferredSize(new Dimension(16, 16));
 		paintTxtIn.addActionListener(stateStrokeActionHandler());
 
 		speedSlider.addChangeListener(speedSliderChangeHandler());
 		// Turn on labels at major tick marks.
 		speedSlider.setMajorTickSpacing(10);
 		speedSlider.setMinorTickSpacing(1);
 		speedSlider.setPaintTicks(true);
 		speedSlider.setPaintLabels(true);
 		
 		networkRefreshRateSlider.addChangeListener(networkRefreshSliderHandler());
 		// Turn on labels at major tick marks.
 		networkRefreshRateSlider.setMajorTickSpacing(10);
 		networkRefreshRateSlider.setMinorTickSpacing(1);
 		networkRefreshRateSlider.setPaintTicks(true);
 		networkRefreshRateSlider.setPaintLabels(true);
 
 		controls.add(simulationSelect, "span 3, grow, wrap");
 		controls.add(periodicBoundarySelect, "span 3, grow, wrap");
 		controls.add(deferredUpdateSelect, "span 3, grow, wrap");
 
 		controls.add(lLbl, "split 6");
 		controls.add(lTxtIn, "");
 
 		controls.add(fLbl, "");
 		controls.add(fTxtIn, "");
 
 		controls.add(qLbl, "");
 		controls.add(qTxtIn, "wrap, gapbottom 18");
 
 		controls.add(paintLbl, "wrap, gapbottom 3");
 		controls.add(paintTxtIn, "span 3, split 2, growx");
 		controls.add(paintSample, "wrap");
 
 		controls.add(new JLabel("Plots:"), "wrap");
 		controls.add(plotListScrollPane, "span 3, split 2, grow");
 		controls.add(activePlotListScrollPane, "wrap, grow");
 		controls.add(enableVisualOut, "span 3, wrap, gapbottom 18");
 
 		controls.add(toggleSimBtn, "span 3, split 3, growx");
 		controls.add(stepSimBtn, "growx");
 		controls.add(resetBtn, "growx, wrap, gapbottom 18");
 
 		controls.add(new JLabel("Speed:"), "wrap");
 		controls.add(speedSlider, "span 3, grow, wrap, gapbottom 18");
 		
 		controls.add(new JLabel("Network refresh adjust:"), "wrap");
 		controls.add(networkRefreshRateSlider, "span 3, grow, wrap, gapbottom 18");
 
 		controls.setPreferredSize(new Dimension(400, 800));
 
 		Container pane = this.getContentPane();
 		pane.add(menuBar, BorderLayout.NORTH);
 		pane.add(controls, BorderLayout.WEST);
 
 		final JPopupMenu outputMenu = new JPopupMenu("Output");
 		outputMenu.add(new AbstractAction("Clear") {
 			private static final long serialVersionUID = 5606674484787535063L;
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				out.setText("");
 			}
 		});
 		out = new JTextArea(5, 30);
 		out.setEditable(false);
 		out.addMouseListener( new MouseAdapter() {
 			public void mouseClicked(MouseEvent e) {
 				if(e.getButton() == MouseEvent.BUTTON3){
 					outputMenu.show(out, e.getX(), e.getY());
 				}
 			}
 		});
 		
 		JScrollPane scrollOut = new JScrollPane(out);
 		System.setOut(new PrintStream(textAreaOutputStream(out)));
 		System.setErr(new PrintStream(textAreaOutputStream(out)));
 		pane.add(scrollOut, BorderLayout.SOUTH);
 	}
 
 	private ChangeListener speedSliderChangeHandler() {
 		return new ChangeListener() {
 			public void stateChanged(ChangeEvent e) {
 				MainApplicationFrame.this.sim
 						.setSpeed(((JSlider) e.getSource()).getValue());
 			}
 		};
 	}
 	
 	private ChangeListener networkRefreshSliderHandler() {
 		return new ChangeListener() {
 			public void stateChanged(ChangeEvent e) {
 				MainApplicationFrame.this.sim.nw
 						.setRefreshAdjust(((JSlider) e.getSource()).getValue());
 			}
 		};
 	}
 
 	private ActionListener stateStrokeActionHandler() {
 		return new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				int[] state = State.parseState(paintTxtIn.getText(),
 						sim.nw.features, sim.nw.traits);
 				if (state != null) {
 					canvas.setStateStroke(state);
 					MainApplicationFrame.this.updateStroke();
 				}
 			}
 		};
 	}
 
 	private MouseAdapter activePlotsListMouseHandler() {
 		return new MouseAdapter() {
 			public void mouseClicked(MouseEvent e) {
 				if (e.getClickCount() == 2) {
 					int index = activePlotsJList.locationToIndex(e.getPoint());
 					PlotAction plot = (PlotAction) activePlotsListModel
 							.getElementAt(index);
 					activePlotsJList.ensureIndexIsVisible(index);
 					activePlotsListModel.removeElement(plot);
 					plotsListModel.addElement(plot);
 					plot.unlink();
 				}
 			}
 		};
 	}
 
 	private MouseAdapter plotsListMouseHandler() {
 		return new MouseAdapter() {
 			public void mouseClicked(MouseEvent e) {
 				if (e.getClickCount() == 2) {
 					int index = plotsJList.locationToIndex(e.getPoint());
 					PlotAction plot = (PlotAction) plotsListModel
 							.getElementAt(index);
 					plotsJList.ensureIndexIsVisible(index);
 					plotsListModel.removeElement(plot);
 					activePlotsListModel.addElement(plot);
 					plot.link();
 				}
 			}
 		};
 	}
 
 	class PaintSample extends JPanel {
 		private static final long serialVersionUID = 4009724729060328197L;
 
 		public PaintSample() {
 			Border blackline = BorderFactory.createLineBorder(Color.black);
 			setBorder(blackline);
 		}
 
 		public void paintComponent(Graphics g) {
 			int color = canvas.rp.color(canvas.stateStroke);
 			g.setColor(new Color(color));
 			g.fillRect(0, 0, getWidth(), getHeight());
 		}
 	}
 
 	private OutputStream textAreaOutputStream(final JTextArea t) {
 		return new OutputStream() {
 			JTextArea ta = t;
 
 			public void write(int b) {
 				byte[] bs = new byte[1];
 				bs[0] = (byte) b;
 				ta.append(new String(bs));
 				ta.setCaretPosition(ta.getDocument().getLength());
 			}
 		};
 	}
 
 	public void updateStroke() {
 		paintSample.repaint();
 	}
 
 	private void resetGui() {
 		Container pane = this.getContentPane();
 		if (canvas != null) {
 			pane.remove(canvas);
 		}
 		if (enableVisualOut.isSelected()) {
 			canvas = new CultureCanvas(CANVAS_WIDTH, sim.nw, BORDERS);
 			pane.add(canvas, BorderLayout.CENTER);
 			SimulationEventListener canvasRepaintListener = new SimulationEventAdapter() {
 				public void interaction(int i, int j) {
 					canvas.repaint();
 				}
 			};
 			sim.addListener(canvasRepaintListener);
 		}
 		simThr = new Thread(sim);
 		toggleSimBtn.setText("Start");
 		stepSimBtn.setEnabled(true);
 		lTxtIn.setText(String.valueOf(sim.nw.size));
 		fTxtIn.setText(String.valueOf(sim.nw.features));
 		qTxtIn.setText(String.valueOf(sim.nw.traits));

 		activeNodesPlotAction = new PlotAction("Active Nodes",
 				"Active Nodes - Scatter Plot", new ActiveNodesScatterPlot()) {
 			private static final long serialVersionUID = -2489956318996611551L;
 
 			public void defer_init() {
 				plotter.addChartScaleSelectorX("time");
 				plotter.validate();
 				plotter.repaint();
 			}
 		};
 		
 		activeEdgesPlotAction = new PlotAction("Active Edges",
 				"Active Edges - Scatter Plot", new ActiveEdgesScatterPlot()) {
 			private static final long serialVersionUID = -2489956318996611551L;
 
 			public void defer_init() {
 				plotter.addChartScaleSelectorX("time");
 				plotter.validate();
 				plotter.repaint();
 			}
 		};
 
 		cultureDistributionPlotAction = new PlotAction("Culture Distribution",
 				"Cultures sizes distribution plot",
 				new CultureDistributionScatterPlot());
 		activeRoomsPlotAction = new PlotAction("Rooms plot",
 				"Active Rooms - Scatter Plot", new ActiveRoomScatterPlot());
 		activeRoomsHistogramAction = new PlotAction("Rooms histogram",
 				"Active Rooms - Histogram", new ActiveRoomsHistogram());
 
 
 		clearPlots();
 		addPlots(activeNodesPlotAction, activeEdgesPlotAction, cultureDistributionPlotAction,
 				activeRoomsPlotAction, activeRoomsHistogramAction);
 
 		this.pack();
 		this.repaint();
 	}
 
 	private void addPlots(PlotAction... plotActions) {
 		for (PlotAction plot : plotActions) {
 			plotMenu.add(plot);
 			plotsListModel.addElement(plot);
 		}
 		plotMenu.add(new AbstractAction("critical Q plot...") {
 			private static final long serialVersionUID = -1945523231629447680L;
 
 			public void actionPerformed(ActionEvent e) {
 				new Thread(new Runnable() {
 					public void run() {
 						q_crit_plot();
 					}
 				}).start();
 			}
 		});
 	}
 
 	private void clearPlots() {
 		plotMenu.removeAll();
 		plotsListModel.removeAllElements();
 		activePlotsListModel.removeAllElements();
 	}
 
 	private ActionListener resetSim = new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 			System.out.println("resetting simulation thread...");
 			if (sim != null){
 				System.out.println("Simulation interrupted.");
 				System.out.println(sim.execution_statistics_string());
 				sim.quit();
 			}
 			try {
 				sim = CultureDisseminationSimulation.factory(
 						(Class<? extends CultureDisseminationSimulation>) simulationSelect
 								.getSelectedItem(), new CulturalNetwork(Integer
 								.parseInt(lTxtIn.getText()), Integer
 								.parseInt(fTxtIn.getText()), Integer
 								.parseInt(qTxtIn.getText()), periodicBoundarySelect.isSelected()));
 				
 				sim.addListener(new SimulationEventAdapter(){
 					public void finished(){
 						System.out.println("Simulation finished.");
 						System.out.println(sim.execution_statistics_string());
 					}
 				});
 				
 				deferredUpdateSelect.addActionListener(new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						sim.setDefer_update(deferredUpdateSelect.isSelected());
 					}
 				});
 				sim.setDefer_update(deferredUpdateSelect.isSelected());
 				
 			} catch (NumberFormatException ex) {
 				JOptionPane.showMessageDialog(MainApplicationFrame.this,
 						"Numbers only please!", "Error",
 						JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 			resetGui();
 		}
 
 	};
 
 	private ActionListener toggleSim = new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 			if (!simThr.isAlive()) {
 				simThr.start();
 			}
 			if (sim.state == SimulationState.RUNNING) {
 				sim.stop();
 				toggleSimBtn.setText("Start");
 				stepSimBtn.setEnabled(true);
 			} else {
 				System.out.println("starting simulation thread...");
 				sim.start();
 				toggleSimBtn.setText("Stop");
 				stepSimBtn.setEnabled(false);
 			}
 		}
 	};
 
 	private ActionListener stepSim = new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 			sim.simulation_step();
 		}
 	};
 
 	Action saveToFile = new AbstractAction("Save...") {
 		private static final long serialVersionUID = -4675654942388512094L;
 
 		public void actionPerformed(ActionEvent e) {
 			int select = getFileChooser().showSaveDialog(MainApplicationFrame.this);
 			if (select == JFileChooser.APPROVE_OPTION) {
 				File f = getFileChooser().getSelectedFile();
 				try {
 					sim.nw.save_to_file(f);
 				} catch (IOException e1) {
 					JOptionPane.showMessageDialog(MainApplicationFrame.this,
 							"Error saving to file: " + e1.getMessage(),
 							"Error!", JOptionPane.ERROR_MESSAGE);
 				}
 			}
 		}
 	};
 
 	Action loadFromFile = new AbstractAction("Load...") {
 		private static final long serialVersionUID = -4675654942388512094L;
 
 		public void actionPerformed(ActionEvent e) {
 			System.out.println("load simulation from file...");
 			int select = getFileChooser().showOpenDialog(MainApplicationFrame.this);
 			if (select == JFileChooser.APPROVE_OPTION) {
 				File f = getFileChooser().getSelectedFile();
 				try {
 					CulturalNetwork nw = new CulturalNetwork(f);
 					if (sim != null)
 						sim.quit();
 					sim = CultureDisseminationSimulation.factory((Class<? extends CultureDisseminationSimulation>) simulationSelect.getSelectedItem(), nw);
 					resetGui();
 				} catch (IOException e1) {
 					JOptionPane.showMessageDialog(MainApplicationFrame.this,
 							"Error saving to file: " + e1.getMessage(),
 							"Error!", JOptionPane.ERROR_MESSAGE);
 				}
 			}
 		}
 	};
 
 	Action quit = new AbstractAction("Exit") {
 		private static final long serialVersionUID = -4675654942388512094L;
 
 		public void actionPerformed(ActionEvent e) {
 			System.exit(0);
 		}
 	};
 
 	class PlotAction extends AbstractAction {
 		private static final long serialVersionUID = 8874503720547085785L;
 		protected Plotter plotter;
 		protected Plot<CultureDisseminationSimulation, ?> plot;
 		private String pTitle;
 
 		public PlotAction(String actionCaption, String plotTitle, Plot<?, ?> p) {
 			super(actionCaption);
 			plot = (Plot<CultureDisseminationSimulation, ?>) p;
 			pTitle = plotTitle;
 		}
 
 		public void actionPerformed(ActionEvent e) {
 			if (plotter == null) {
 				plotter = new Plotter(pTitle, plot);
 				defer_init();
 			}
 			plotter.mostra();
 		}
 
 		public void defer_init() {
 		};
 
 		public void link() {
 			plot.link(sim);
 		}
 
 		public void unlink() {
 			plot.unlink();
 		}
 
 		public String toString() {
 			return pTitle;
 		}
 	}
 
 	private void q_crit_plot() {
 		int size = Integer.parseInt(lTxtIn.getText());
 		int f = Integer.parseInt(fTxtIn.getText());
 		int numSims = 10;
 		int qMin = 2;
 		int qMax = 100;
 
 		double[][] series = new double[2][(qMax - qMin + 1) * numSims];
 		int si = 0;
 		ScatterPlotter plotter = null;
 		for (int q = qMin; q <= qMax; q++) {
 			System.out.println("q: " + q);
 			double[][] cSizes = new double[2][numSims];
 			for (int i = 0; i < numSims; i++) {
 				CultureDisseminationSimulation sim =
 					CultureDisseminationSimulation.factory(
 							(Class<? extends CultureDisseminationSimulation>) simulationSelect.getSelectedItem(),
 							new CulturalNetwork(size, f, q, periodicBoundarySelect.isSelected()));
 				sim.start();
 				sim.run();
 				Integer[] culture_sizes = new ArrayList<Integer>(sim.nw
 						.count_cultures().values()).toArray(new Integer[0]);
 				Arrays.sort(culture_sizes);
 				Integer largest_culture = (Integer) culture_sizes[culture_sizes.length - 1];
 				cSizes[0][i] = q; // current q
 				cSizes[1][i] = ((double) largest_culture)
 						/ (sim.nw.size * sim.nw.size); // largest culture,
 				// normalized
 			}
 			for (int i = 0; i < numSims; i++) {
 				series[0][si] = cSizes[0][i];
 				series[1][si] = cSizes[1][i];
 				si++;
 			}
 			if (plotter == null) {
 				plotter = new ScatterPlotter("Axelrod Simulation Plot", String
 						.format("L = %d, F = %d", size, f), series);
 				plotter.mostra();
 			}
 			plotter.setSeries(series);
 		}
 	}
 
 	class ClassNameComboBoxRenderer extends BasicComboBoxRenderer{
 		private static final long serialVersionUID = 6965028171205010114L;
 		public Component getListCellRendererComponent(JList list, Object value,
 				int index, boolean isSelected, boolean cellHasFocus) {
 			Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
 			setText(humanize(((Class<?>) value).getSimpleName()));
 			return c;
 		}
 		
 		private String humanize(String camelized){
 			Pattern upper = Pattern.compile("[A-Z][a-z]*");
 			Matcher m = upper.matcher(camelized);
 			StringBuilder sb = new StringBuilder();
 			while(m.find())
 			sb.append(m.group().toLowerCase()+" ");
 			return sb.toString();
 		}
 	}
 
 	public static void main(String[] args) {
 		MainApplicationFrame axelrod = new MainApplicationFrame();
 		axelrod.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		axelrod.pack();
 		axelrod.setVisible(true);
 	}
 
 	public static JFileChooser getFileChooser() {
 		return fc == null? fc = new JFileChooser() : fc;
 	}
 }
