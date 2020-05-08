 package fr.istic.synthlab.abstraction.module.rep;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.jsyn.unitgen.UnitGenerator;
 
 import fr.istic.synthlab.abstraction.filter.TriplePassThroughFilter;
 import fr.istic.synthlab.abstraction.module.AModule;
 import fr.istic.synthlab.abstraction.port.IInputPort;
 import fr.istic.synthlab.abstraction.port.IOutputPort;
 import fr.istic.synthlab.abstraction.wire.IWire;
 import fr.istic.synthlab.factory.impl.PACFactory;
 
 public class ModuleREP extends AModule implements IModuleREP {
 
 	private static final String MODULE_NAME = "REP";
 	private static final String IN_NAME = "In";
 	private static final String OUT1_NAME = "Out1";
 	private static final String OUT2_NAME = "Out2";
 	private static final String OUT3_NAME = "Out3";
 
 	private IInputPort in;
 	private IOutputPort output1;
 	private IOutputPort output2;
 	private IOutputPort output3;
 	private TriplePassThroughFilter passThrough;
 
 	public ModuleREP() {
 		super(MODULE_NAME);
 
 		this.passThrough = new TriplePassThroughFilter();
 
 		this.in = PACFactory.getFactory().newInputPort(this, IN_NAME,
 				passThrough.getInput());
 
 		this.output1 = PACFactory.getFactory().newOutputPort(this, OUT1_NAME,
 				passThrough.getOutput1());
 		this.output2 = PACFactory.getFactory().newOutputPort(this, OUT2_NAME,
 				passThrough.getOutput2());
 		this.output3 = PACFactory.getFactory().newOutputPort(this, OUT3_NAME,
 				passThrough.getOutput3());
 
 		addPort(in);
 		addPort(output1);
 		addPort(output2);
 		addPort(output3);
 	}
 
 	@Override
 	public String getName() {
 		return MODULE_NAME;
 	}
 
 	@Override
 	public List<UnitGenerator> getJSyn() {
 		List<UnitGenerator> generators = new ArrayList<UnitGenerator>();
		generators.add(passThrough);
 		return generators;
 	}
 
 	@Override
 	public void start() {
 		this.passThrough.start();
 	}
 
 	@Override
 	public void stop() {
 		this.passThrough.stop();
 	}
 	
 	@Override
 	public List<IWire> getWires() {
 
 		List<IWire> wires = new ArrayList<IWire>();
 		if (in.getWire() != null) {
 			if(!wires.contains(in.getWire()))
 				wires.add(in.getWire());
 		}
 		if (output1.getWire() != null) {
 			if(!wires.contains(output1.getWire()))
 				wires.add(output1.getWire());
 		}
 		if (output2.getWire() != null) {
 			if(!wires.contains(output2.getWire()))
 				wires.add(output2.getWire());
 		}
 		if (output3.getWire() != null) {
 			if(!wires.contains(output3.getWire()))
 				wires.add(output3.getWire());
 		}
 		return wires;
 	}
 
 	@Override
 	public IInputPort getInput() {
 		return in;
 	}
 
 	@Override
 	public IOutputPort getOutput1() {
 		return output1;
 	}
 
 	@Override
 	public IOutputPort getOutput2() {
 		return output2;
 	}
 
 	@Override
 	public IOutputPort getOutput3() {
 		return output3;
 	}
 
 }
