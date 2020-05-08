 package engine.agent.Luis;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.concurrent.Semaphore;
 
 import engine.interfaces.*;
 import shared.Glass;
 import transducer.TChannel;
 import transducer.TEvent;
 import transducer.Transducer;
 import engine.agent.Agent;
 import engine.agent.Alex.*;
 import engine.agent.Luis.*;
 import engine.agent.Luis.Interface.*;
 
 public class PopUpAgent_LV extends Agent implements PopUp_LV{
 
 	int index;
 	TChannel work;
 	boolean nextComponentFree,popUpBreak = false;
 	Status status;
 	PopUpState state;
 	List<Machine> operators;
 
 	Conveyor_LV conveyor;
 	GlassPackage currentGlass;
 	List<GlassPackage> myGlassPieces = Collections.synchronizedList(new ArrayList<GlassPackage>());
 	enum GlassState {INCOMING, WAITING, NEEDS_WORK, /*FINISHED,*/ MOVE, NONE};
 	enum PopUpState {FULL, OPEN};
 	enum Status{RAISED,LOWERED};
 
 
 	Semaphore load = new Semaphore(0,true);
 	Semaphore release = new Semaphore(0,true);
 
 	Semaphore brokenPopUp = new Semaphore(0,true);
 
 	Semaphore statusSemaphore = new Semaphore(0,true);
 
 	ConveyorFamily next;
 
 	Transducer t;
 	TChannel channel;
 	private ConveyorFamilyAgent_LV parentCF;
 	private enum ConveyorStatus{NULL, GLASS_WAITING_NO_PROC, GLASS_WAITING_YES_PROC};
 	ConveyorStatus conveyorStatus = ConveyorStatus.NULL;
 	private Semaphore waitingForFinshedGlass = new Semaphore(0);
 	private Semaphore popupUp = new Semaphore(0);
 	private Semaphore popupDown = new Semaphore(0);
 
 
 	public void setParent(ConveyorFamilyAgent_LV parent){
 		parentCF = parent;
 	}
 	public class Machine
 	{
 		Operator operator;
 		int number;
 		TChannel channel;
 		boolean occupied, working = true;
 		Semaphore semaphore = new Semaphore(0,true);
 		public boolean readyToGiveFinishedGlass = false;
 
 		public Machine(Operator o, TChannel t, boolean b,  int n)
 		{
 			operator = o;
 			number = n;
 			channel = t;
 			occupied = b;
 		}
 	}
 
 	public class GlassPackage
 	{
 		Operator operator;
 		Glass glass;
 		GlassState state;
 		int operatorNumber;
 
 		public GlassPackage(Glass g, GlassState s)
 		{
 			glass = g;
 			state = s;
 		}
 	}
 
 	public PopUpAgent_LV(String n, int i)
 	{
 		name = n;
 		index = i;
 		nextComponentFree = true;
 		status = Status.LOWERED;
 		state = PopUpState.OPEN;
 		operators = Collections.synchronizedList(new ArrayList<Machine>());
 
 	}
 
 
 	/*
 	 * MESSAGES
 	 */
 
 	public void msgIAmFree()
 	{
 		nextComponentFree = true;
 		stateChanged();
 	}
 
 
 
 	public void msgHereIsGlass(Glass glass) 
 	{
 		GlassPackage g = new GlassPackage(glass, GlassState.INCOMING);
 		myGlassPieces.add(g);
 		currentGlass = g;
 		stateChanged();
 
 	}
 
 	public void msgIHaveGlassFinished(Operator operator) 
 	{
 
 		if (operators.get(0).operator == operator)
 			operators.get(0).readyToGiveFinishedGlass = true;
 		else
 			operators.get(1).readyToGiveFinishedGlass = true;
 		stateChanged();	
 	} 
 
 	public void msgHereIsFinishedGlass(Operator operator, Glass glass) 
 	{
 		GlassPackage g = new GlassPackage(glass, GlassState.MOVE);
 		myGlassPieces.add(g);
 		currentGlass = g;
 		waitingForFinshedGlass.release();
 	}
 	public void msgIHaveNoGlass(Operator operator) {
 		waitingForFinshedGlass.release();
 		currentGlass = null;
 	}
 
 	public void msgCannotPass()
 	{
 		//popup busy
 	}
 
 
 	/*
 	 * SCHEDULER
 	 */
 	public boolean pickAndExecuteAnAction() {
 
 		GlassPackage temp = null;
 		if(getOperatorStatus(0) == false || getOperatorStatus(1) == false)
 		{
 			synchronized(myGlassPieces)
 			{
 				for(GlassPackage g : myGlassPieces)
 				{
 					if(g.state == GlassState.NEEDS_WORK)
 					{
 						if(getOperatorStatus(0) == false)
 							giveGlassToOperator(g, 0);
 						else if (getOperatorStatus(1) == false)
 							giveGlassToOperator(g, 1);
 						return true;
 					}
 				}
 			}
 		}
 		if(state == PopUpState.OPEN)
 		{
 			synchronized(myGlassPieces)
 			{
 				for(GlassPackage g : myGlassPieces)
 				{
 					if(g.state == GlassState.INCOMING)
 					{
 						//if((!g.glass.getRecipe(channel)) || !(getOperatorStatus(0) == true && getOperatorStatus(1) == true) || (!operators.get(0).working && !operators.get(1).working))
 						if((g.glass.getRecipe(channel) == false))
 						{
 								takeGlass(g);
 								return true;
 						}
 						else if((g.glass.getRecipe(channel) == true) && (getOperatorStatus(0) == false || getOperatorStatus(1) == false))
 						{
 							if(state == PopUpState.OPEN)
 							{
 								takeGlass(g);
 								return true;
 							}
 						}
 					}
 				}
 			}
 		}
 
 		synchronized(myGlassPieces)
 		{
 			for(GlassPackage g : myGlassPieces)
 			{
 				if(g.state == GlassState.WAITING)
 				{
 					checkGlass(g);
 					return true;
 				}
 			}
 		}
 
 		if(state == PopUpState.OPEN)
 		{
 			if (operators.get(0).readyToGiveFinishedGlass)
 			{
 				axnLoadGlassFromOperator(operators.get(0));
 				return true;
 			}
 			else if (operators.get(1).readyToGiveFinishedGlass){
 				axnLoadGlassFromOperator(operators.get(1));
 				return true;
 			}
 		}
 
 
 		if(nextComponentFree)
 		{
 			synchronized(myGlassPieces)
 			{
 				for(GlassPackage g : myGlassPieces)
 				{
 					if(g.state == GlassState.MOVE)
 					{
 						//if(state == PopUpState.FULL )
 						//{
 							moveGlass(g);
 							return true;
 						//}
 					}
 				}
 			}
 		}
 
 		return false;
 	}
 
 	/*
 	 * ACTIONS
 	 */
 
 	private void axnLoadGlassFromOperator(Machine machine) {
 		print("AXN, load glass form operator");
 
 		if (status == Status.LOWERED)
 
 			{
 			raisePopUp();
 
 			}
 		//popup up
 		print("Letting operator know im free");
 		machine.operator.msgIAmFree();
 
 
 		try {
 			waitingForFinshedGlass.acquire();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		if (currentGlass == null){
 			print("No glass returned, lost by machine");
 			machine.occupied = false;
 			state = PopUpState.OPEN;
 		}
 		else{
 
 
 			print("Message that finished glass is here recieved, waiting for load..");
 			//load finished
 			try {
 				load.acquire();
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			machine.occupied = false;
 			machine.readyToGiveFinishedGlass = false;
 			state = PopUpState.FULL;
 		}
 
 	}
 
 
 	private void takeGlass(GlassPackage g)
 	{
 		print("Taking glass from conveyor");
 		if(status == Status.RAISED)
 			lowerPopUp();
 		try{
 			load.acquire();
 		} catch(InterruptedException e){
 			e.printStackTrace();
 		}
 		state = PopUpState.FULL;
 		g.state = GlassState.WAITING;
 		//TEST conveyor.msgPopUpBusy();
 		stateChanged();
 	}
 
 	private void giveGlassToOperator(GlassPackage g, int operatorNumber)
 	{
 		print("Giving operator glass");
 		if(status == Status.LOWERED);
 			raisePopUp();
 
 		operators.get(operatorNumber).operator.msgHereIsGlass(g.glass);
 		operators.get(operatorNumber).occupied = true;
 		myGlassPieces.remove(g);
 		try{
 			operators.get(operatorNumber).semaphore.acquire();
 		} catch(InterruptedException e){
 			e.printStackTrace();
 		}
 
 
 		Do("REACHED HERE !!!");
 		//operators.get(operatorNumber).occupied = true;
 		//g.state = GlassState.NONE;
 		state = PopUpState.OPEN;
 
 		conveyor.msgPopUpFree();
 		currentGlass = null;
 		stateChanged();
 	}
 
 	private void sendImFree() {
 		// TODO Auto-generated method stub
 		if ((state == PopUpState.OPEN && (!(operators.get(0).occupied && operators.get(1).occupied) ) || conveyorStatus == ConveyorStatus.GLASS_WAITING_NO_PROC))
 		{
 			conveyor.msgPopUpFree();
 		}
 	}
 
 
 	private void moveGlass(GlassPackage g)
 	{
 		print("Moving glass to next conveyor");
 		if(status == Status.RAISED)
 			lowerPopUp();
 		myGlassPieces.remove(g);
 
 		Integer[] args = new Integer[1];
 		args[0] = index;
 		t.fireEvent(TChannel.POPUP, TEvent.POPUP_RELEASE_GLASS, args);
 
 		next.msgHereIsGlass(g.glass);
 		try{
 			release.acquire();
 		} catch(InterruptedException e){
 			e.printStackTrace();
 		}
 
 		state = PopUpState.OPEN;
 		sendImFree();
 		currentGlass = null;
 		nextComponentFree= false;
 		stateChanged();
 	}
 
 	private void checkGlass(GlassPackage g)
 	{
 		print("Checking if glass needs work");
 		if(g.glass.getRecipe(channel))
 			g.state = GlassState.NEEDS_WORK;
 		else
 			g.state = GlassState.MOVE;
 
 		stateChanged();
 	}
 
 	private void lowerPopUp()
 	{
 		print("lowering popup");
 
 		if(popUpBreak)
 		{
 			print("PopUp broken!");
 			
 			try {
 				brokenPopUp.acquire();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 
 		Integer[] args = new Integer[1];
 
 		args[0] = index; //Note: popup offset
 		t.fireEvent(TChannel.POPUP, TEvent.POPUP_DO_MOVE_DOWN, args);
 
 		try{
 			popupDown.acquire();
 		} catch(InterruptedException e){
 			e.printStackTrace();
 		}
 		status = Status.LOWERED;
 		stateChanged();
 	}
 
 	private void raisePopUp()
 	{
 		print("raising popup");
 
 		if(popUpBreak)
 		{
 			print("PopUp broken!");
 			
 			try {
 				brokenPopUp.acquire();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 
 		Integer[] args = new Integer[1];
 
 		args[0] = index;
 		t.fireEvent(TChannel.POPUP, TEvent.POPUP_DO_MOVE_UP, args);
 
 		try{
 			popupUp.acquire();
 		} catch(InterruptedException e){
 			e.printStackTrace();
 		}
 		conveyor.msgPopUpBusy();
 		status = Status.RAISED;
 		stateChanged();
 	}
 
 	@Override
 	public void eventFired(TChannel channel, TEvent event, Object[] args) {
 		print("Index: " + index);
 		if((channel == TChannel.POPUP) && ((Integer)(args[0]) == index)) //Note: popup offset
 		{
 			if(event == TEvent.POPUP_GUI_MOVED_DOWN)
 				popupDown.release();
 			if(event == TEvent.POPUP_GUI_MOVED_UP)
 				popupUp.release();
 			if(event == TEvent.POPUP_GUI_LOAD_FINISHED)
 				{
 				load.release();
 				}
 			if(event == TEvent.POPUP_GUI_RELEASE_FINISHED)
 				{
 
 				release.release();
 				//conveyor.msgPopUpFree();
 				}
 
 		}
 		else if((channel == operators.get(0).channel) && ((Integer)(args[0]) == 0) )
 		{
 			Do("Event: "+event+" Channel: "+channel+" Parameter passed in: "+(Integer)(args[0])+
 					" Operator No: "+operators.get(0).number);
 			if(event == TEvent.WORKSTATION_LOAD_FINISHED)
 				{
 					operators.get(0).semaphore.release();
 				}
 			if(event == TEvent.WORKSTATION_RELEASE_FINISHED)
 			{
 				operators.get(0).semaphore.release();
 			}
 
 		}
 
 		else if((channel == operators.get(1).channel) && ((Integer)(args[0]) == 1) )
 		{
 			Do("Event: "+event+" Channel: "+channel+" Parameter passed in: "+(Integer)(args[0])+
 					" Operator No: "+operators.get(0).number);
 			if(event == TEvent.WORKSTATION_LOAD_FINISHED)
 				operators.get(1).semaphore.release();
 			if(event == TEvent.WORKSTATION_RELEASE_FINISHED)
 				operators.get(1).semaphore.release();
 
 		}
 
 	}
 
 	public String getName()
 	{
 		return name;
 
 	}
 
 	public void setPopUpBroken(boolean s)
 	{
 		print("setting popup " + index + " to broken = " + s);
 		
 		if(s)
 			popUpBreak = true;
 		else
 		{
 			brokenPopUp.release();
 			popUpBreak = false;
 		}
 		stateChanged();
 	}
 
 	public void setOperators(Operator operatorOne, Operator operatorTwo, TChannel c)
 	{
 		this.operators.add(new Machine(operatorOne,c,false,0));
 		this.operators.add(new Machine(operatorTwo,c,false,1));
 		t.register(this, c);
 	}
 
 	public void setTransducer(Transducer trans)
 	{
 		t = trans;
 		t.register(this, TChannel.POPUP);
 	}
 
 	public void setInteractions(ConveyorFamily cf, Conveyor_LV c, Transducer trans)
 	{
 		conveyor = c;
 		next = cf;
 		t = trans;
 		t.register(this, TChannel.POPUP);
 	}
 
 	public void setConveyor(Conveyor_LV c)
 	{
 		conveyor = c;
 	}
 
 	public void setNextComponentFree(boolean b)
 	{
 		nextComponentFree = b;
 	}
 
 	public boolean getNextComponentFree()
 	{
 		return nextComponentFree;
 	}
 
 	public Conveyor_LV getConveyor()
 	{
 		return conveyor;
 	}
 
 	public ConveyorFamily getNextConveyorFamily()
 	{
 		return next;
 	}
 
 	public int getOperatorsSize()
 	{
 		return operators.size();
 	}
 
 	public int getGlassPiecesSize()
 	{
 		return myGlassPieces.size();
 	}
 
 	public Operator getOperator(int i)
 	{
 		return operators.get(i).operator;
 	}
 
 	public boolean getOperatorStatus(int i)
 	{
 		return operators.get(i).occupied;
 	}
 
 	public void setOperatorStatus(int i,boolean b)
 	{
 		operators.get(i).occupied = b;
 	}
 
 
 	public void setInteractions(ConveyorFamily c3) {
 		// TODO Auto-generated method stub
 		next = c3;
 	}
 
 
 	public void msgOperatorBroken(boolean isBroken, int operatorNum) {
 
 		if (isBroken){
 			print("Popup " +operatorNum + " is broken, not using");
 			if (operators.get(operatorNum).working && !operators.get(operatorNum).occupied)
 				{operators.get(operatorNum).occupied = true;
 				operators.get(operatorNum).working = false;
 				}
 			else {
 				print("Already broken or occupied!");
 			}
 
 		}
 		else {
 			print("Popup " +operatorNum +"is working again");
 			if (!operators.get(operatorNum).working && operators.get(operatorNum).occupied == true)
 				{operators.get(operatorNum).occupied = false;
 				operators.get(operatorNum).working = true;
 				}
 			else
 				print("Popup was not seen as broken, why would you unbreak it?");
 		}
 		stateChanged();
 	}
 
 
 	@Override
 	public void msgIHaveGlassReady(boolean needsProc) {
 		if (!needsProc)
 		conveyorStatus = ConveyorStatus.GLASS_WAITING_NO_PROC;
 		else
 			conveyorStatus = ConveyorStatus.GLASS_WAITING_YES_PROC;
 		stateChanged();
 	}
 
 
	public void msgBreakNextGlass(int i) {
		operators.get(i).operator.breakNextGlass();
	}


 
 
 
 
 
 }
