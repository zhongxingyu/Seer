/* $Id: PhysicalPartitionMax.java,v 1.1 2003/12/24 01:49:02 vpapad Exp $ */
 package niagara.physical;
 
 import java.util.ArrayList;
 import java.util.Vector;
 
 import niagara.logical.PartitionMax;
 import niagara.optimizer.colombia.*;
 import niagara.utils.*;
 
 import org.w3c.dom.Node;
 
 public class PhysicalPartitionMax extends PhysicalPartitionGroup {
 	private Attribute maxAttribute;
 	private AtomicEvaluator ae;
 	private ArrayList values;
 	private Double emptyGroupValue;
 
 	public void opInitFrom(LogicalOp logicalOperator) {
 		super.opInitFrom(logicalOperator);
 		// Get the max attribute 
 		maxAttribute = ((PartitionMax) logicalOperator).getMaxAttribute();
 		emptyGroupValue = ((PartitionMax) logicalOperator).getEmptyGroupValue();
 	}
 	
 
 	public void opInitialize() {
 		super.opInitialize();
 		ae = new AtomicEvaluator(maxAttribute.getName());
 		ae.resolveVariables(inputTupleSchemas[0], 0);
 		values = new ArrayList();
 	}
 
 	/**
 	 * @see niagara.query_engine.PhysicalIncrementalGroup#processTuple(Tuple, Object)
 	 */
 	public Object processTuple(
 		Tuple tuple,
 		Object previousGroupInfo) {
 		
 		ae.getAtomicValues(tuple, values);
 	try {
 		if (landmark) {
 			Double prevMax = (Double) previousGroupInfo;
 			
 			Double newValue = Double.valueOf((String) values.get(0));
 			values.clear();
 			Double newMax = new Double(
 				Math.max(prevMax.doubleValue(), newValue.doubleValue()));
 			if (newMax.equals(prevMax)) {
 				// No change in group
 				return prevMax;
 			} else
 				return newMax;
 		} else {
 			Vector newGroup = (Vector) previousGroupInfo;
 			
 			Double newValue = Double.valueOf((String) values.get(0));
 			values.clear();
 			int index = ((Double)newGroup.firstElement()).intValue();
 			newGroup.set(index, newValue);
 			index = index + 1;
 			if (index > range)
 				index = 1;
 			newGroup.set(0, new Double(index));
 			Double max = findMax(newGroup);
 			newGroup.set(range+1, max);
 			return newGroup;
 		}
 				
 	} catch (NumberFormatException nfe) {
 		throw new RuntimeException("XXX vpapad what do we do here?!");
 	}
 	}
 	
 	private Double findMax (Vector group) {
 		Double newMax = emptyGroupValue;
 		for (int i = 1; i <= range; i++) {
			if (newMax.compareTo(group.elementAt(i)) < 0)
 				newMax = (Double)group.elementAt(i);
 		}
 		return newMax;
 	}
 
 	/**
 	 * @see niagara.query_engine.PhysicalIncrementalGroup#emptyGroupValue()
 	 */
 	public Object emptyGroupValue() {
 		return emptyGroupValue;
 	}
 	
 
 	public Vector EmptyGroup() {
 
 		Vector emptyGroup = new Vector(range  +2);
 		emptyGroup.addElement(Double.valueOf("1"));
 		//the first element is the pointer to the next available element to put the new input item;
 		//the last element is used to save the current max;
 		for (int i = 1; i <= range + 1; i++ )
 			emptyGroup.addElement(emptyGroupValue);
 		return emptyGroup;		
 	}
 
 
 	/**
 	 * @see niagara.query_engine.PhysicalIncrementalGroup#constructOutput(Object)
 	 */
 	public Node constructOutput(Object groupInfo) {
 		if (landmark) {
 			return doc.createTextNode(String.valueOf(groupInfo));
 		} else {
 			Double max = (Double)((Vector) groupInfo).elementAt(range+1);
 			return doc.createTextNode(String.valueOf(max));
 		}
 		
 		
 	}
 	
 
 	public Op opCopy() {
 		PhysicalPartitionMax op = new PhysicalPartitionMax();
 		if (logicalGroupOperator != null)
 			op.initFrom(logicalGroupOperator);
 		return op;
 	}
 
 	public boolean equals(Object o) {
 		if (o == null || !(o instanceof PhysicalPartitionMax))
 			return false;
 		if (o.getClass() != PhysicalPartitionMax.class)
 			return o.equals(this);
 		return logicalGroupOperator.equals(
 			((PhysicalPartitionMax) o).logicalGroupOperator);
 	}
 
 	public int hashCode() {
 		return logicalGroupOperator.hashCode();
 	}
 }
