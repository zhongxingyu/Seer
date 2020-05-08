 package gui;
 
 import gui.turing.TuringMachineEditor;
 
 import java.util.*;
 import javax.swing.*;
 
 import tape.Tape;
 import tape.TapeException;
 import java.awt.*;
 import java.awt.event.*;
 import machine.*;
 
 @SuppressWarnings("serial")
 /**
  * This class is the window in which runs the simulation with the tapes.
  * @author Nessa Baier
  *
  */
 public class SimulationWindow extends JFrame implements Observer, ActionListener{
 	/**
 	 * ScrollPane
 	 */
 	JScrollPane scrollpaneRight;
 	/**
 	 * panel for toolbar, panel for tapes
 	 */
 	JPanel panelall, panelToolbar;
 	/**
 	 * toolbar
 	 */
 	JToolBar toolbar;
 
 
 	JButton buttonPlay, buttonForward;
 	/**
 	 * the simulation's graphic tapes
 	 */
 	ArrayList<tape.GraphicTape> graphicTapes = new ArrayList<tape.GraphicTape>();
 	/**
 	 * Current machine
 	 */
 	Machine currentMachine;
 	/**
 	 * true if simulation is/should be paused
 	 */
 	boolean simulationPaused;
 	/**
 	 * current simulation
 	 */
 	Simulation sim;
 	
 	ImageIcon iconPlay = new ImageIcon(SimulationWindow.class.getResource("images/play.png"));
 	ImageIcon iconPause = new ImageIcon(SimulationWindow.class.getResource("images/pause.png"));
 	ImageIcon iconStepForward = new ImageIcon(SimulationWindow.class.getResource("images/forward.png"));
 
 
 	/**
 	 * Creates a new window for the simulation.
 	 * @param machine
 	 */
 	public SimulationWindow(Machine machine){
 		this.simulationPaused = true;
 		this.currentMachine = machine;
 		for(int i = 0; i< currentMachine.getTapes().size(); i++){
 			if(currentMachine.getTapes().get(i).getType() == tape.Tape.Type.GRAPHIC){
 				graphicTapes.add((tape.GraphicTape)machine.getTapes().get(i));
 				graphicTapes.get(i).addObserver(this);
 				if(this.currentMachine.getType() == Machine.MachineType.TuringMachine){
 					graphicTapes.get(i).addObserver((TuringMachineEditor)(currentMachine.getEditor()));
 				}
 			}
 		}
 
 		this.setTitle("Simulation of " +this.currentMachine.getName());
 		this.setLayout(new GridBagLayout());
 		this.setBounds(200,200,600,700);
 		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 
 		this.panelall = new JPanel(new GridBagLayout());
 		this.scrollpaneRight = new JScrollPane(panelall);
 
 		toolbar = new JToolBar("Functions");
 		buttonPlay = new JButton(this.iconPlay);
 		buttonPlay.setEnabled(false);
 		buttonForward = new JButton(this.iconStepForward);
 		buttonForward.setEnabled(false);
 		buttonPlay.addActionListener(this);
 		buttonForward.addActionListener(this);
 		toolbar.add(buttonPlay);
 		toolbar.add(buttonForward);
 		this.panelToolbar = new JPanel();
 		this.panelToolbar.add(toolbar);
 
 		//initialize tapes
 		try {
 			this.currentMachine.initTapes();
 		}
 		catch (TapeException e){
 			try {
 				this.currentMachine.shutdownTapes();
 			} catch (TapeException e1) {
 				System.out.println("Warning: The tapes couldn't be shutdown correctly.");
 				e1.printStackTrace();
 			}
 			ErrorDialog.showError("The initialization of the tapes failed because of a tape exception.", e);
 			return;
 		}
 		catch (RuntimeException e){
 			try {
 				this.currentMachine.shutdownTapes();
 			} catch (TapeException e1) {
 				System.out.println("Warning: The tapes couldn't be shutdown correctly.");
 				e1.printStackTrace();
 			}
 			ErrorDialog.showError("The initialization of the tapes failed because of an undefined exception.", e);
 
 			return;
 		}
 
 		setVisible(true);
 		this.init();
 	}
 
 	/**
 	 * Initializes window's contents.
 	 */
 	private void init(){
 		//adding toolbar to window
 		GridBagConstraints windowConstraints = new GridBagConstraints();
 		windowConstraints.gridx = 0;
 		windowConstraints.gridy = 0;
 		windowConstraints.weightx = 1.0;
 		windowConstraints.weighty = 0.05;
 		this.add(panelToolbar, windowConstraints);
 
 		//adding tapes to panel
 		GridBagConstraints panelallConstraints = new GridBagConstraints();
 		for(int i = 0; i< graphicTapes.size(); i++){
 			panelallConstraints.gridx = 0;
 			panelallConstraints.gridy = i;
 			panelallConstraints.weightx = 1.0;
 			panelallConstraints.fill = GridBagConstraints.HORIZONTAL;
 			this.panelall.add(this.graphicTapes.get(i).getTapePanel(),panelallConstraints);
 		}
 		JPanel gap = new JPanel();
 		panelallConstraints.fill = GridBagConstraints.BOTH;
 		panelallConstraints.weighty = 1.0;
 		panelallConstraints.gridy = graphicTapes.size();
 		panelallConstraints.gridx = 0;
 		this.panelall.add(gap,panelallConstraints);
 
 		//writing input words
 		try {
 			this.currentMachine.writeInputWords();
 			sim = this.currentMachine.createSimulation();
 			sim.addObserver(this);
 		}
 		catch (TapeException e){
 			try {
 				this.currentMachine.shutdownTapes();
 			} catch (TapeException e1) {
 				System.out.println("Warning: The tapes couldn't be shutdown correctly.");
 				e1.printStackTrace();
 			}
 			ErrorDialog.showError("The initialization of the tapes failed because of a tape exception.", e);
 			return;
 		}
 		catch (RuntimeException e){
 			try {
 				this.currentMachine.shutdownTapes();
 			} catch (TapeException e1) {
 				System.out.println("Warning: The tapes couldn't be shutdown correctly.");
 				e1.printStackTrace();
 			}
 			ErrorDialog.showError("The initialization of the tapes failed because of an undefined exception.", e);
 			return;
 		}
 
 		//adding scrollpane to window
 		windowConstraints.gridx = 0;
 		windowConstraints.gridy = 1;
 		windowConstraints.weightx = 1.0;
 		windowConstraints.weighty = 0.95;
 		windowConstraints.fill = GridBagConstraints.BOTH;
 		this.add(scrollpaneRight, windowConstraints);
 		
 		this.validate();
 		this.repaint();
 	}
 
 	//TODO shutdown tapes, if simwindow closed without finishing simulation
 	public void dispose(){
 		if(!this.sim.isSimulationAlreadyStarted()){
 			for (Tape t : this.graphicTapes){
 				System.out.println("thread wird interrupted");
 				t.setiWishToInterruptThisThread(true);
 
 			}
 		}
 		else if(this.sim.isSimulationAlreadyStarted()){ 
 			this.sim.setAbortSimulation();
 			try {
 				Thread.sleep(500);
 			} catch (InterruptedException e) {}
 
 
 			try{
 				System.out.println(" i shut down the tapes");
 				this.currentMachine.shutdownTapes();
 			}
 			catch (TapeException e) {
 				System.out.println("Warning: The tapes couldn't be shutdown correctly.");
 				e.printStackTrace();
 				ErrorDialog.showError("The initialization of the tapes failed because of an undefined exception.", e);
 				return;
 			}
 		}
 		this.setVisible(false);
 	}
 
 	public void update(Observable observable, Object obj) {
 		if (observable instanceof tape.Tape 
 				&& obj instanceof tape.Tape.Event
 				&& (tape.Tape.Event)obj == tape.Tape.Event.INPUTFINISHED){
			// FIXME: das passiert leider sofort, wenn eins der x tapes fertig ist
 			this.buttonPlay.setEnabled(true);
 			System.out.println("Writing input word finished: notified");
 		}
 
 		else if(this.sim.isSimulationAlreadyStarted()
 				&& observable instanceof Simulation 
 				&& obj instanceof Simulation.simulationState 
 				&&((Simulation.simulationState)obj)==Simulation.simulationState.FINISHED){
 
 			this.buttonForward.setEnabled(false);
 			this.buttonPlay.setEnabled(false);
 		}
 
 		else if(!this.sim.isSimulationAlreadyStarted()
 				&& observable instanceof tape.Tape
 				&& obj instanceof tape.Tape.Event
 				&& (tape.Tape.Event)obj ==tape.Tape.Event.INPUTABORTED){
 
 			try{
 				System.out.println(" i shut down the tapes");
 				this.currentMachine.shutdownTapes();
 			}
 			catch (TapeException e) {
 				System.out.println("Warning: The tapes couldn't be shutdown correctly.");
 				e.printStackTrace();
 				ErrorDialog.showError("The initialization of the tapes failed because of an undefined exception.", e);
 				return;
 			}
 
 		}
 	}
 
 
 	/**
 	 * This method handles button events.
 	 */
 	public void actionPerformed( ActionEvent event){ //TODO inactivate buttons while writing input word
 		//forward button
 		if(event.getSource().equals(buttonForward)){
 			sim.resume();
 			try{
 				Thread.sleep(500);
 			}
 			catch(InterruptedException e){}
 			sim.pause();
 		}
 		//play/pause/resume button
 		else if(event.getSource().equals(buttonPlay)&& !sim.isSimulationAlreadyStarted()){
 			try {
 				simulationPaused = false;
 				sim.start();
 				this.buttonPlay.setIcon(this.iconPause);
 			}
 			catch (RuntimeException e){
 				ErrorDialog.showError("The simulation failed because of an undefined exception.", e);
 			}
 		}
 
 		else if(event.getSource().equals(buttonPlay)&& sim.isSimulationAlreadyStarted()){
 			if(simulationPaused){
 				sim.resume();
 				buttonForward.setEnabled(false);
 				this.buttonPlay.setIcon(this.iconPause);
 			}
 			else{
 				sim.pause();
 				buttonForward.setEnabled(true);
 				this.buttonPlay.setIcon(this.iconPlay);
 			}
 			simulationPaused = !simulationPaused;
 		}
 	}
 }
