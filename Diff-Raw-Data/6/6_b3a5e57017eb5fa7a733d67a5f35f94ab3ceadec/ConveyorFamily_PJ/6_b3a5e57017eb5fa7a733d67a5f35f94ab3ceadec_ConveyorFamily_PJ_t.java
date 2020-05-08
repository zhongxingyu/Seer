 package engine.conveyorfamily_Poojan;
 
 
 import engine.conveyorfamily.Interfaces_Poojan.Operator_PJ;
 import engine.conveyorfamily.Interfaces_Poojan.TransducerInterface_PJ;
 import engine.interfaces.ConveyorFamily;
 import engine.agent.Alex.*;
 import shared.Glass;
 import shared.enums.SensorPosition;
 import shared.enums.SharedData.SensorType;
 import transducer.TChannel;
 import transducer.TEvent;
 import transducer.Transducer;
 
 public class ConveyorFamily_PJ implements ConveyorFamily
 {
 	private int ConveryorFamilyNo;
 	private ConveyorAgent_PJ conveyor;
 	private HalfConveyorAgent halfconveyor;
 	
 	private PopupAgent_PJ popup;
 	private InLineMachineAgent_PJ inline;
 	private ConveyorFamily nextConveyorFamily;
 	private ConveyorFamily previousConveyorFamily;
 
 
 	public boolean isNextConveyorFamilyBusy;
 
 	private Transducer transducer;
 	private boolean isHalfFamily;
 
 	public ConveyorFamily_PJ(int number, Transducer transducer2,ConveyorFamily cprev)
 	{
 		this.previousConveyorFamily=cprev;
 		this.ConveryorFamilyNo=number;
 		this.transducer=transducer2;
 		this.popup = new PopupAgent_PJ("MyPopup",number,this,transducer);
 
 		this.inline = new InLineMachineAgent_PJ("NCCUTTER",number,this,transducer);
 
 		this.conveyor = new ConveyorAgent_PJ("MyConveyor",number,this,transducer, popup, inline,cprev);
 		this.halfconveyor = new HalfConveyorAgent("HalfConveyor",1,this,transducer, popup, inline,cprev);
 		this.inline.setConveyor(conveyor,halfconveyor);
 		this.halfconveyor.setConveyor(this.conveyor);
 
 	}
 
 
 	public void msgHereIsGlass(Glass glass) {
 		// TODO Auto-generated method stub
 		this.conveyor.msgHereIsGlass(glass);
 
 	}
 
 
 	public ConveyorFamily getNextConveyorFamily()
 	{
 		return nextConveyorFamily;
 	}
 
 	public String getName() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
 
 
 	@Override
 	public void msgIAmFree() {
 		System.out.println("My CFnumber is"+this.ConveryorFamilyNo+"I am Free received from NEXT CONVEYOR FAMILY");
 
 		this.halfconveyor.msgIamFree();
 	}
 
 
 	public void msgIHaveFinishedGlass(Operator_PJ o) {
 		// TODO Auto-generated method stub
 		System.out.println("operator has finished processing Glass");
 
 		this.popup.msgOperatorHasFinishedGlass(o);
 
 	}
 
 
 	public void msgHereIsFinishedGlass(Glass g, Operator_PJ operatorAgent) {
 		// TODO Auto-generated method stub
 		System.out.println("Sending it back to popup");
 		this.popup.msgHereIsFinishedGlass(g,operatorAgent);
 	}
 
 
 	public ConveyorAgent_PJ getConveyor()
 	{
 		return this.conveyor;
 	}
 
 
 	@Override
 	public void msgHereIsFinishedGlass(Operator operator, Glass glass) {
 		// TODO Auto-generated method stub
 
 	}
 
 
 	@Override
 	public void msgIHaveGlassFinished(Operator operator) {
 		// TODO Auto-generated method stub
 
 	}
 
 
 	@Override
 	public void setNextConveyorFamily(ConveyorFamily c3) {
 		// TODO Auto-generated method stub
 		nextConveyorFamily=c3;
 		//this.conveyor.NEXTFamily=nextConveyorFamily;
 		this.halfconveyor.NEXTFamily=c3;
 		this.inline.setNextFamily(c3);
 	}
 
 
 	@Override
 	public void startThreads() {
 		// TODO Auto-generated method stub
 		this.conveyor.startThread();
 		this.inline.startThread();
 		this.halfconveyor.startThread();
 		//this.popup.startThread();
 	}
 
 
 	public void setPreviousConveyorFamily(ConveyorFamily c2) {
 		// TODO Auto-generated method stub
 		this.conveyor.PREVIOUSFamily=c2;
 
 	}
 
 
 	@Override
 	public void setConveyorBroken(boolean s, int conveyorno) {

 		// TODO Auto-generated method stub
 		if(conveyorno==0)
 		{
 			conveyor.setbrokenstatus(s, conveyorno);
 		}
 		else
 		{
 			halfconveyor.setbrokenstatus(s, conveyorno);
 		}
 	}
 
 
 	@Override
 	public void setInlineBroken(boolean s, TChannel channel) {
 		// TODO Auto-generated method stub
 
 		this.inline.setbroken(s);
 	}
 
 
 
 }
