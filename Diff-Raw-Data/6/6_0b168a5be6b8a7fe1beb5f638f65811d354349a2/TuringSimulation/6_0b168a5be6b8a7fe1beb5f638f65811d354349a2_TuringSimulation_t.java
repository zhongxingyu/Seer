 package machine.turing;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import machine.Simulation;
 import gui.turing.TuringMachineEditor;
 import tape.*;
 
 /**
  * This class simulates the turingmachine.
  * @author Nessa Baier
  *
  */
 public class TuringSimulation extends Simulation{
 
 	/**
 	 * The current machine object.
 	 */
 	TuringMachine machine;
 
 	/**
 	 * Specific states
 	 */
 	@SuppressWarnings("javadoc")
 	State currentState, startState, nextState;
 
 	/** 
 	 * The tapes of the current turingmachine.
 	 */
 	ArrayList<Tape> tapes = null;
 	
 	ArrayList<LEGOTape> legoTapes = null;
 
 	/**
 	 * The currently read symbols.
 	 */
 	ArrayList<Character> currentSymbols = new ArrayList<Character>();
 
 
 
 
 	/**
 	 * Constructs a new turingmachine.
 	 * @param machine The current machine object.
 	 * @throws TapeException If something went wrong with the tapes.
 	 */
 	public TuringSimulation(TuringMachine machine) throws TapeException{
 		super(machine);
 		this.addObserver((TuringMachineEditor)machine.getEditor());
 		this.machine = machine;
 		this.tapes = machine.getTapes();
		this.legoTapes = new ArrayList<LEGOTape>();
 		for( Tape t : this.tapes){
 			if (t.getType()== Tape.Type.LEGO)
 				this.legoTapes.add((tape.LEGOTape)t);
 		}
 		this.init();
 		findEdge();
 	}
 
 	/**
 	 * Intializes the simulation.
 	 */
 	private void init(){
 		for( int i=0; i < machine.getStates().size(); i++){
 			if(machine.getStates().get(i).isStartState()){
 				startState = machine.getStates().get(i);
 				currentState = startState;
 				super.setChanged();
 				super.notifyObservers((Object)currentState);
 
 			}
 		}
 	}
 
 	/**
 	 * This method runs the simulation.
 	 * 
 	 */
 	public void runMachine() throws TapeException{
 		System.out.println(" \n +Steps: " +numberOfSteps);
 		if (this.maxNumberOfSteps >= this.numberOfSteps){ 
 			this.numberOfSteps++;
 			if(!this.abortSimulation){
 				
 				//send current state to robot
				for ( LEGOTape t : this.legoTapes){
 					try {
 						t.getSlave().sendStateName(this.currentState.name);
 					} catch (IOException e1) {
 						// TODO Auto-generated catch block
 						e1.printStackTrace();
 					}
				}
 				
 				currentSymbols.clear();
 
 				//read symbol(s)
 				for(int i = 0; i < machine.getNumberOfTapes(); i++)
 					currentSymbols.add(i,this.tapes.get(i).read());
 
 				//searching for the right label
 				Transition rightLabel = getRightLabel();
 				if(currentState.isFinalState() && rightLabel == null){
 					System.out.println("ich habe fertig");
 					super.setChanged();
 					super.notifyObservers((Object)Simulation.simulationState.FINISHED);
 
 				}
 				else if(!currentState.isFinalState() && rightLabel == null){
 					System.out.println("abgekackt");
 					super.setChanged();
 					super.notifyObservers((Object)Simulation.simulationState.FAILED);
 
 				}
 
 				else{
 					System.out.println("State: "+ this.currentState);
 					System.out.println("Transition: "+ rightLabel);
 					System.out.println("Next State: "+ this.nextState);
 
 
 					//go/do label
 					for(int i = 0; i < machine.getNumberOfTapes(); i++){
 						if (rightLabel.getWrite().get(i) != '*') { //* means reproduce symbol -> do nothing
 							this.numberOfWritings++;
 							tapes.get(i).write(rightLabel.getWrite().get(i));
 						}
 
 						switch(rightLabel.getAction().get(i)){
 
 						case 'R':
 							tapes.get(i).moveRight();
 							this.numberOfMovements++;
 							break;
 						case 'L':
 							tapes.get(i).moveLeft();
 							this.numberOfMovements++;
 							break;
 						default:
 							break;
 						}
 					}
 
 					currentState = nextState;
 					super.setChanged();
 					super.notifyObservers((Object)currentState);
 
 					while(this.simulationIsPaused){
 						if(this.abortSimulation){
 							break;
 						}
 						try{
 							Thread.sleep(400);
 						}catch(InterruptedException e){}
 					}
 					//do the next step
 					runMachine();
 				}
 			}
 			else{
 				this.simulationAborted = true;
 			}
 		}
 		//		else{
 		//			this.simulationAborted = true; // TODO is needed?
 		//		}
 
 	}
 
 	/**
 	 * This method returns the right label.
 	 * @return rightLabel
 	 */
 	private Transition getRightLabel(){
 		Transition label= null;
 		for(Edge e : currentState.getEdge()){
 			for(int j = 0; j < e.getTransitions().size(); j++){
 				for(int i = 0; i < machine.getNumberOfTapes(); i++){
 					if(e.getTransitions().get(j).getRead().get(i) == '*'  //* matches every symbol
 							|| e.getTransitions().get(j).getRead().get(i) == currentSymbols.get(i)){ 
 						super.setChanged();
 						super.notifyObservers((Object)e);
 						label = e.getTransitions().get(j);
 						nextState = e.getTo();
 					}
 					else{
 						label = null;
 						nextState = null;
 						break;
 					}
 				}
 				if(label != null){
 					return label;
 				}
 			}
 		}
 		return label;
 	}
 
 	/**
 	 * This method adds the edge to its state.
 	 */
 	private void findEdge(){
 		for(State s : this.machine.getStates()) {
 			s.getEdge().clear();
 			for(Edge e : this.machine.getEdges())
 				if(e.getFrom().getId().equals(s.getId()))
 					s.getEdge().add(e);
 		}
 	}
 }
