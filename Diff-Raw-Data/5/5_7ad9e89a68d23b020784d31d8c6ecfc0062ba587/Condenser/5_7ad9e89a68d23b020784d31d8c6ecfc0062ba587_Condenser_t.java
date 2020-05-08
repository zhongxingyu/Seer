 package org.sepr.anchovy.Components;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.sepr.anchovy.InfoPacket;
 import org.sepr.anchovy.Pair;
 import org.sepr.anchovy.Pair.Label;
 
 public class Condenser extends Component {
 	private double temperature;
 	private double pressure;
 	private double waterLevel;
 	public Condenser(String name) {
 		super(name);
 	}
 
 	@Override
 	public InfoPacket getInfo() {
 		InfoPacket info = super.getSuperInfo();
 		info.namedValues.add(new Pair<Double>(Label.temp, temperature));
 		info.namedValues.add(new Pair<Double>(Label.pres, pressure));
 		info.namedValues.add(new Pair<Double>(Label.wLvl, waterLevel));
 		return info;
 	}
 
 	@Override
 	public void calucalte() {
 		double oldPressure = pressure;
 		pressure = calculatePressure();
 		temperature = calculateTemp(oldPressure);
 		waterLevel = calculateWaterLevel();
 		super.setOuputFlowRate(calculateOutputFlowRate());
 
 	}
 	protected double calculateTemp(double oldPressure){
 		//Temperature = old temp * pressure increase/decrease raito - coolent flow rate
 		
 		ArrayList<Component> inputs = super.getRecievesInputFrom();
 		Iterator<Component> it = inputs.iterator();
 		Component c = null;
 		
 		double totalCoolantFlowRate = 0;
 		
 		while(it.hasNext()){
 			c = it.next();
 			if(c.getName().contains("Coolant")){
 				totalCoolantFlowRate += c.getOutputFlowRate();
 			}
 		}
 		double ratio = pressure/oldPressure;
 		return temperature * ratio - totalCoolantFlowRate;
 	}
 	
 	protected double calculatePressure(){
 		//The pressure of the condenser is the current pressure + input flow of steam - output flow of water.
 		ArrayList<Component> inputs = super.getRecievesInputFrom();
 		Iterator<Component> it = inputs.iterator();
 		Component c = null;
 		double totalInputFlowRate = 0;
 		while(it.hasNext()){
 			c = it.next();
 			if(!(c.getName().contains("Coolant"))){
				totalInputFlowRate += c.getOutputFlowRate();
 			}
 		}
 		if(temperature > 100){
 			return pressure + totalInputFlowRate - super.getOutputFlowRate();
 		}else{
 			return (pressure-pressure/temperature) + totalInputFlowRate - super.getOutputFlowRate();
 		}
 	}
 	protected double calculateWaterLevel(){
 		//Water level = steam condensed + water level - water out
 		double wLevel;
 		if(temperature > 100){
 			wLevel = waterLevel - super.getOutputFlowRate();
 		}else{
			wLevel = (waterLevel - super.getOutputFlowRate()) + pressure / 10;
 		}
 		return wLevel;
 	}
 
 	@Override
 	protected double calculateOutputFlowRate() {
 		// TODO Auto-generated method stub
 		// Must distinguish between a pump that pumps steam in with a pump that pumps coolent round (possibly by name of pump having collent or alike in)
 		ArrayList<Component> outputs = super.getOutputsTo();
 		Iterator<Component> it = outputs.iterator();
 		Component c = null;
 		double totalOPFL = 0;
 		while(it.hasNext()){
 			c = it.next();
 			if(!c.getName().contains("Coolant")){
 				totalOPFL += c.getOutputFlowRate();
 			}
 		}
 		
 		return totalOPFL;
 	}
 
 	@Override
 	public void takeInfo(InfoPacket info) throws Exception {
 		super.takeSuperInfo(info);
 		Iterator<Pair<?>> it = info.namedValues.iterator();
 		Pair<?> pair = null;
 		Label label = null;
 		while(it.hasNext()){
 			pair = it.next();
 			label = pair.getLabel();
 			switch(label){
 			case temp:
 				temperature = (Double) pair.second();
 				break;
 			case pres:
 				pressure = (Double) pair.second();
 				break;
 			case wLvl:
 				waterLevel = (Double) pair.second();
 			default:
 				break;
 			}
 		}
 		
 
 	}
 
 }
