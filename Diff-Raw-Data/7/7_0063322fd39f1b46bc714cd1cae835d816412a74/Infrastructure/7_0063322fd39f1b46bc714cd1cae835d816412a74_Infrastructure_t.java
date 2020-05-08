 package anchovy.Components;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import anchovy.InfoPacket;
 import anchovy.Pair;
 import anchovy.Pair.Label;
 
 /**
  * This class is the representation of infrastructure which is supported by the operation of the power plant.
  * Unlike the other components, infrastructure components take an input flow rate in the form of electricity and consume it.
  * These are used to give the plant a purpose as it has something to power. 
  * @author Kieren 
  * @author Harrison
  *
  */
 public class Infrastructure extends Component {
 	/**
 	 * The electricity that is needed to power this piece of infrastructure.
 	 */
 	double electrisityneeded = 0;
 	
 	boolean failed = false;
 	
 	/**
 	 * @see anchovy.Components.Component#Component(String)
 	 */
 	public Infrastructure(String name) {
 		super(name);
 	}
 	/**
 	 * @see anchovy.Components.Component#Component(String, InfoPacket)
 	 */
 	public Infrastructure(String name, InfoPacket info) {
		super(name);
		try {
			takeInfo(info);
		} catch (Exception e) {
			e.printStackTrace();
		}
 	}
 
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public InfoPacket getInfo() {
 		InfoPacket info = super.getInfo();
 		info.namedValues.add(new Pair<Double>(Label.elec, electrisityneeded ));
 		return info;
 	}
 	/** 
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void calculate() {
 		super.setOuputFlowRate(calculateOutputFlowRate());
 	}
 	
 	/** 
 	 * {@inheritDoc}
 	 * The output flow rate of an infrastructure component is the total amount of electricity flowing into the component, minus the electricity that it itself needs to function.
 	 */
 	@Override
 	protected double calculateOutputFlowRate() {
 		double electrisityGenerated = 0;
 		ArrayList<Component> inputComponents = super.getRecievesInputFrom();
 		Iterator<Component> it = inputComponents.iterator();
 		Component comp = null;
 		while(it.hasNext()){
 			comp = it.next();
 			if((comp instanceof Generator) | (comp instanceof Infrastructure) ){
 				electrisityGenerated += comp.getOutputFlowRate();
 			}
 			electrisityGenerated = electrisityGenerated - electrisityneeded;
 			}
 		return electrisityGenerated;
 	}
 	/** 
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void takeInfo(InfoPacket info) throws Exception {
 		super.takeSuperInfo(info);
 		Iterator<Pair<?>> i = info.namedValues.iterator();
 		Pair<?> pair = null;
 		Label label = null;
 		while(i.hasNext()){
 			pair = i.next();
 			label = pair.getLabel();
 			switch (label){
 			case elec:
 				electrisityneeded = (Double) pair.second();
 			default:
 				break;
 			}
 		}
 
 	}
 	/**
 	 * {@inheritDoc}
 	 * Infrastructure components fail when they are receiving less electricity than they need.
 	 */
 	@Override
 	protected boolean calculateFailed() {
 		if (electrisityneeded > this.getOutputFlowRate()){
 			return true;
 		}else{
 			return false;
 		}
 
 	}
 	/**
 	 * @return The electricity that this infrastructure needs to keep functioning
 	 */
 	public double getElectrisityneeded() {
 		return electrisityneeded;
 	}
 	/**
 	 * @param electrisityneeded The amount of electricity that this infrastructure needs to keep functioning.
 	 */
 	public void setElectrisityneeded(double electrisityneeded) {
 		this.electrisityneeded = electrisityneeded;
 	}
 	
 }
