 package controller;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.ArrayList;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import model.algorithms.Algorithm;
 import model.algorithms.AlgorithmState;
 import model.algorithms.MSTAlgorithm;
 import model.algorithms.SearchAlgorithm;
 import model.algorithms.ShortestRouteAlgorithm;
 import model.algorithms.SortAlgorithm;
 import model.algorithms.elements.BFS;
 import model.algorithms.elements.DFS;
 import model.algorithms.elements.Dijkstras;
 import model.algorithms.elements.Kruskals;
 import model.algorithms.elements.TopologicalSort;
 import model.elements.Graph;
 import model.persistency.GraphsParser;
 import utils.ActionUtils;
 import utils.Dialog;
 import view.MainView;
 import exceptions.AlgorithmAlreadyFinishedException;
 import exceptions.AlgorithmException;
 import exceptions.GraphComponentException;
 import exceptions.GraphException;
 
 public class MainViewController {
 	
 	private static final int FIRE_RATE = 1000;
 
 	private final MainView view;
 	private ArrayList<Graph> graphs;
 	private ArrayList<Algorithm> algorithms;
 	private TimerTask timerTask;
	
//	private boolean ready;
 
 	/**
 	 * @param view
 	 */
 	public MainViewController(final MainView view) {
 		this.view = view;
 		this.algorithms = new ArrayList<Algorithm>();
 		this.algorithms.add(new BFS());
 		this.algorithms.add(new DFS());
 		this.algorithms.add(new Kruskals());
 		this.algorithms.add(new Dijkstras());
 		this.algorithms.add(new TopologicalSort());
//		this.ready = true;
 
 		ActionUtils.addActionListener(this.view.getStopButton(), this, "stopAlgorithm");
 		ActionUtils.addActionListener(this.view.getResetButton(), this, "reset");
 		this.view.getStepButton().addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				runAlgorithm(true);
 			}
 		});
 		this.view.getRunButton().addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				runAlgorithm(false);
 			}
 		});
 		
 		this.view.getGraphComboBox().addItemListener(new ItemListener() {
 			@Override
 			public void itemStateChanged(ItemEvent e) {
 				if (e.getStateChange() == ItemEvent.SELECTED) {
 					view.getCanvas().setGraph(view.getSelectedGraph());
 					reset();
 				}
 			}	
 		});
 		
 		this.view.getAlgoComboBox().addItemListener(new ItemListener() {
 			@Override
 			public void itemStateChanged(ItemEvent e) {
 				reset();
 				if (e.getStateChange() == ItemEvent.SELECTED) {
 					if (view.getSelectedAlgorithm() instanceof SearchAlgorithm ||
 							view.getSelectedAlgorithm() instanceof ShortestRouteAlgorithm) {
 						view.getFindLabel().setVisible(true);
 						view.getFindInput().setVisible(true);
 					} else {
 						view.getFindLabel().setVisible(false);
 						view.getFindInput().setVisible(false);
 					}
 				}
 			}	
 		});
 		
 		try {
 			this.graphs = new GraphsParser().parse();
 		} catch (Exception e) {
 			Dialog.message(e.getMessage());
 			return;
 		}
 		this.fillGraphs();
 		this.fillAlgorithms();
 		
 		this.view.getGraphComboBox().setSelectedIndex(0);
 		this.view.getCanvas().setGraph(graphs.get(0));
 	}
 	
 	public void reset() {
 		Graph graph = this.view.getSelectedGraph();
 		if (graph != null) {
 			graph.reset();
 			this.view.getCanvas().setGraph(graph);
 		}
 		
 		Algorithm algorithm = this.view.getSelectedAlgorithm();
 		if (algorithm != null) {
 			algorithm.reset();
 		}
 		
 		this.view.setResultLabel("");
 		this.setUIEnabled(true);
 	}
 	
 	private void fillAlgorithms() {
 		this.view.setAlgorithms(this.algorithms);
 	}
 
 	private void fillGraphs() {
 		this.view.setGraphs(this.graphs);
 	}
 	
 	public void runAlgorithm(boolean step) {
 		final Algorithm algorithm = this.view.getSelectedAlgorithm();
 		Graph graph = this.view.getSelectedGraph();
 		String find = this.view.getFindInput().getText();
 		
 		// Safety
 		if (graph == null) {
 			Dialog.message("No graph selected!");
 			return;
 		} else if (algorithm == null) {
 			Dialog.message("No algorithm selected!");
 			return;
 		}
 		
 		// Initialize
 		if (algorithm.getState() == AlgorithmState.Clean) {
 			if (algorithm instanceof SearchAlgorithm) {
 				if (find.equals("")) {
 					Dialog.message("No vertex name entered!");
 					return;
 				}
 				SearchAlgorithm searchAlgorithm = (SearchAlgorithm) algorithm;
 				try {
 					searchAlgorithm.initialize(find, graph);
 				} catch (GraphComponentException e) {
 					Dialog.message(e.getMessage());
 					return;
 				}	
 			} else if (algorithm instanceof ShortestRouteAlgorithm) {
 				if (find.equals("")) {
 					Dialog.message("No vertex name entered!");
 					return;
 				}
 				ShortestRouteAlgorithm shorthAlgorithm = (ShortestRouteAlgorithm) algorithm;
 
 				try {
 					shorthAlgorithm.initialize(find, graph);
 				} catch (GraphComponentException | GraphException e) {
 					Dialog.message(e.getMessage());
 					return;
 				}	
 			} else if (algorithm instanceof MSTAlgorithm) {
 				MSTAlgorithm mstAlgorithm = (MSTAlgorithm) algorithm;
 
 				try {
 					mstAlgorithm.initialize(graph);
 				} catch (GraphComponentException | GraphException e) {
 					Dialog.message(e.getMessage());
 					return;
 				}
 
 			} else if (algorithm instanceof SortAlgorithm) {
 				SortAlgorithm sortAlgorithm = (SortAlgorithm) algorithm;
 
 				try {
 					sortAlgorithm.initialize(graph);
 				} catch (GraphComponentException | GraphException e) {
 					Dialog.message(e.getMessage());
 					return;
 				}
 			}
 		}
 		
 		if (step) {
 			try {
 				algorithm.run();
 			} catch (AlgorithmAlreadyFinishedException e) {
 				Dialog.message("Algorithm already finished!");
 				return;
 			} catch (AlgorithmException e) {
 				e.printStackTrace();
 			}
 			
 			this.view.getFindInput().setEditable(false);
 			this.view.getCanvas().repaint();	
 
 			if (algorithm.getResult() != null) {
 				this.view.setResultLabel(algorithm.getResult());
 			}
 		} else {
 			setUIEnabled(false);
 			
 			this.timerTask = new TimerTask() {
 				@Override
 				public void run() {
 					if (algorithm.getResult() != null) {
 						view.setResultLabel(algorithm.getResult());
 					} 
 					if (algorithm.getState() == AlgorithmState.Initialized) {
 						view.getStopButton().setVisible(true);
 					} else if (algorithm.getState() == AlgorithmState.Finished) {
 						this.cancel();
 						view.getStopButton().setVisible(false);
 						setUIEnabled(true);
 						return;
 					}
 					
 					try {
 						algorithm.run();
 					} catch (AlgorithmAlreadyFinishedException e) {
 						Dialog.message("Algorithm already finished!");
 						return;
 					} catch (AlgorithmException e) {
 						e.printStackTrace();
 					}
 					view.getCanvas().repaint();
 				}
 			};
 			new Timer().scheduleAtFixedRate(this.timerTask, 0, FIRE_RATE);
 		}
 		
 		if (algorithm.getState() == AlgorithmState.Clean ||
 				algorithm.getState() == AlgorithmState.Finished) {
 			this.setUIEnabled(true);			
 		}
 	}
 	
 	private void setUIEnabled(boolean editable) {
 		this.view.getFindInput().setEditable(editable);
 		this.view.getStepButton().setEnabled(editable);
 		this.view.getRunButton().setEnabled(editable);
 		this.view.getResetButton().setEnabled(editable);
 	}
 	
 	public void stopAlgorithm() {
 		this.timerTask.cancel();
 		this.view.getStopButton().setVisible(false);
 		this.setUIEnabled(true);
 	}
 
 }
