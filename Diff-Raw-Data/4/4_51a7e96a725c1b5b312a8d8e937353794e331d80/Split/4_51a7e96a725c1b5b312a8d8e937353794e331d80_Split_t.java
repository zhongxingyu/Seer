 package net.unikernel.bummel.logic_elements.Split;
 
 import java.util.Map;
 import net.unikernel.bummel.project_model.api.BasicElement;
 import net.unikernel.bummel.project_model.api.PortsData;
 import org.openide.util.lookup.ServiceProvider;
 
 /**
  * <b>Pinout:</b>
  * <ol start='0'>
  * <li>Input</li>
  * <li>Output</li>
  * <li>Output</li>
  * </ol>
  * @author mcangel
  */
 @ServiceProvider(service=BasicElement.class)
 @PortsData(portsFile="ports.xml")
 public class Split extends BasicElement
 {
 
 	public Split()
 	{
 		super(new String[]{"input","output1","output2"});
 	}
 	
 	/**
 	 * Duplicates input port value to the output ports.
 	 * <b>Pinout:</b>
 	 * <ol start='0'>
 	 * <li>Input</li>
 	 * <li>Output</li>
 	 * <li>Output</li>
 	 * </ol>
 	 */
 	@Override
 	public Map<String, Double> process(Map<String, Double> valuesOnPorts)
 	{
		valuesOnPorts.put(getPorts().get(1), valuesOnPorts.get(getPorts().get(0)).doubleValue());
		valuesOnPorts.put(getPorts().get(2), valuesOnPorts.get(getPorts().get(0)).doubleValue());
 		valuesOnPorts.put(getPorts().get(0), 0.);
 		return valuesOnPorts;
 	}
 }
