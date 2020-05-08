 /* vim: set ts=2: */
 /**
 * Copyright (C) Gerardo Huck, 2010
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published 
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *  
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
 
 
 /** 
  * It was modified as part of Google Summer of Code 2010.
  * Mentor: Mike Smoot
  * @author <a href="mailto:gerardohuck .at. gmail .dot. com">Gerardo Huck</a>
  * @version 0.1
  */
 
 
 package csplugins.layout.algorithms.graphPartition;
 
 import cern.colt.list.*;
 
 import cern.colt.map.*;
 
 import csplugins.layout.LayoutNode;
 import csplugins.layout.LayoutLabelNodeImpl;
 import csplugins.layout.LayoutPartition;
 import csplugins.layout.LayoutLabelPartition;
 import csplugins.layout.EdgeWeighter;
 
 import cytoscape.layout.Tunable;
 import cytoscape.layout.LayoutProperties;
 import cytoscape.plugin.CytoscapePlugin;
 import cytoscape.CyNetwork;
 import cytoscape.Cytoscape;
 import cytoscape.CyNode;
 import cytoscape.layout.AbstractLayout;
 import cytoscape.data.CyAttributes;
 import cytoscape.logger.CyLogger;
 import cytoscape.task.*;
 import cytoscape.view.CyNetworkView;
 
 import giny.model.*;
 
 import java.lang.Throwable;
 import java.util.*;
 import javax.swing.JOptionPane;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentListener;
 import java.awt.event.ComponentEvent;
 
 enum LayoutTypes {
     NODE("Nodes Only"),
 	LABEL("Labels Only"),
 	BOTH("Both Nodes and Labels");
     
     private String name;
     private LayoutTypes(String str) { name=str; }
     public String toString() { return name; }
 }
 
 
 /**
  * An abstract class that handles the partitioning of graphs so that
  * the partitions will be laid out individually.
  */
 public abstract class AbstractGraphPartition extends AbstractLayout implements ActionListener {
     double incr = 100;
     protected List <LayoutPartition> partitionList = null;
     protected EdgeWeighter edgeWeighter = null;
     protected boolean singlePartition = false;
     protected CyLogger logger = CyLogger.getLogger(AbstractGraphPartition.class);
 
     /**
      * Which kinf of layout are we going to perform
      */
     protected LayoutTypes layoutType = LayoutTypes.NODE;
 
     /**
      * Whether Labels should be repositioned in their default positions 
      */
     protected boolean resetPosition = false;
     
     /**
      * Coefficient to determine label edge weights
      */
     protected double weightCoefficient = 10.0;
 
 
     static LayoutTypes[] layoutChoices = {LayoutTypes.NODE,
 					  LayoutTypes.LABEL,
 					  LayoutTypes.BOTH};
 
 
     // Information for taskMonitor
     double current_start = 0; // Starting node number
     double current_size  = 0; // Partition size
     double total_nodes   = 0; // Total number of nodes
 
     /**
      * Creates a new AbstractGraphPartition object.
      */
     public AbstractGraphPartition() {
 	super();
 	
     }
 
     /**
      * Override this method and layout the LayoutPartion just
      * like you would a NetworkView.
      *
      * @param partition The LayoutPartion to be laid out. 
      */
     public abstract void layoutPartition(LayoutPartition partition);
     
     /**
      *  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public boolean supportsSelectedOnly() {
 	return true;
     }
 
     /**
      * If overridden by a subclass to return true, before calling to 
      * layoutPartition mathod, fake nodes will be created to represent label 
      * positions, and labels will be placed acordingly. 
      *
      * It is an easy way of creating label layout algorithms.
      *
      * For this to work fine with labels, weights should be supported.
      *
      * @return Whether the layout algorithm supports label layout. 
      */
     public boolean supportsLabelLayout() {
 	return false;
     }
 
 
     /**
      * Sets the singlePartition flag, which disables partitioning. This
      * can be used by users who do not want to partition their graph for
      * some reason.
      *
      * @param flag if false, no paritioning will be done
      */
     public void setPartition(boolean flag) {
 	if (flag)
 	    this.singlePartition = false;
 	else
 	    this.singlePartition = true;
     }
 
     /**
      * Sets the singlePartition flag, which disables partitioning. This
      * can be used by users who do not want to partition their graph for
      * some reason.
      *
      * @param value if "false", no paritioning will be done
      */
     public void setPartition(String value) {
 	Boolean val = new Boolean(value);
 	setPartition(val.booleanValue());
     }
 
     /**
      *  DOCUMENT ME!
      *
      * @param percent The percentage of completion for this partition
      */
     protected void setTaskStatus(int percent) {
 	if (taskMonitor != null) {
 	    // Calculate the nodes done for this partition
 	    double nodesDone = current_size*(double)percent/100.;
 	    // Calculate the percent done overall
 	    double pDone = ((nodesDone+current_start)/total_nodes)*100.;
 	    taskMonitor.setPercentCompleted((int)pDone);
 	    taskMonitor.setStatus("Completed " + (int)pDone + "%");
 	}
     }
 
     /**
      * AbstractGraphPartitionLayout implements the construct method
      * and calls layoutPartition for each partition.
      */
     public void construct() {
 	initialize();
 
 	if (edgeWeighter != null) 
 	    edgeWeighter.reset();
 
 	// Depending on whether we are partitioned or not,
 	// we use different initialization.  Note that if the user only wants
 	// to lay out selected nodes, partitioning becomes a very bad idea!
 	if (selectedOnly || singlePartition) {
 
 	    // We still use the partition abstraction, even if we're
 	    // not partitioning.  This makes the code further down
 	    // much cleaner
 	    LayoutPartition partition = new LayoutPartition(network, networkView, selectedOnly, edgeWeighter);
 	    partitionList = new ArrayList(1);
 	    partitionList.add(partition);
 
 	} else if (staticNodes != null && staticNodes.size() > 0) {
 
 	    // Someone has programmatically locked a set of nodes -- construct
 	    // the list of unlocked nodes
 	    List<CyNode> unlockedNodes = new ArrayList();
 	    for (CyNode node: (List<CyNode>)network.nodesList()) {
 		if (!isLocked(networkView.getNodeView(node.getRootGraphIndex()))) {
 		    unlockedNodes.add(node);
 		}
 	    }
 	    LayoutPartition partition = new LayoutPartition(network, networkView, unlockedNodes, edgeWeighter);
 	    partitionList = new ArrayList(1);
 	    partitionList.add(partition);
 
 	} else {
 	    partitionList = LayoutPartition.partition(network, networkView, false, edgeWeighter);
 	}
 
 	total_nodes = network.getNodeCount();
 	logger.info("TOTAL NODES: " + total_nodes);	
 
 	current_start = 0;
 
 	// Set up offsets -- we start with the overall min and max
 	double xStart = (partitionList.get(0)).getMinX();
 	double yStart = (partitionList.get(0)).getMinY();
 
 	for (LayoutPartition part: partitionList) {
 	    xStart = Math.min(xStart, part.getMinX());
 	    yStart = Math.min(yStart, part.getMinY());
 	}
 
 	double next_x_start = xStart;
 	double next_y_start = yStart;
 	double current_max_y = 0;
 
 	double max_dimensions = Math.sqrt((double) network.getNodeCount());
 	// give each node room
 	max_dimensions *= incr;
 	max_dimensions += xStart;
 
 	for (LayoutPartition partition: partitionList) {
 	    if (canceled) break;
 	    // get the partition
 	    current_size = (double)partition.size();
 	    logger.info("Partition #"+partition.getPartitionNumber()+" has "+current_size+" nodes");
 	    setTaskStatus(1);
 
 	    // Partitions Requiring Layout
 	    if (partition.nodeCount() >=  1) { // LABEL
 		try {
 		    layoutSinglePartition(partition);
 		} catch (OutOfMemoryError _e) {
 		    System.gc();
 		    logger.error("Layout algorithm failed: Out of memory");
 		    return;
 		} catch (Exception _e) {
 		    logger.error("Layout algorithm failed: ", (Throwable)_e);
 		    return;
 		}
 
 		if (!selectedOnly && !singlePartition) {
 		    // logger.debug("Offsetting partition #"+partition.getPartitionNumber()+" to "+next_x_start+", "+next_y_start);
 		    // OFFSET
 		    partition.offset(next_x_start, next_y_start);
 		}
 
 		// single nodes
 	    } else if ( partition.nodeCount() == 1 ) { // TODO: do something with this!!!
 		// Reset our bounds
 		partition.resetNodes();
 
 		// Single node -- get it
 		LayoutNode node = (LayoutNode) partition.getNodeList().get(0);
 		node.setLocation(next_x_start, next_y_start);
 		partition.moveNodeToLocation(node);
 	    } else {
 		logger.info("Done nothing with this partition");
 		continue;
 	    }
 
 	    double last_max_x = partition.getMaxX();
 	    double last_max_y = partition.getMaxY();
 
 	    if (last_max_y > current_max_y) {
 		current_max_y = last_max_y;
 	    }
 
 	    if (last_max_x > max_dimensions) {
 		next_x_start = xStart;
 		next_y_start = current_max_y;
 		next_y_start += incr;
 	    } else {
 		next_x_start = last_max_x;
 		next_x_start += incr;
 	    }
 
 	    setTaskStatus( 100 );
 	    current_start += current_size;
 	} 
     }
 
 
     /**
      * DOCUMENT ME!
      */
     protected void layoutSinglePartition(LayoutPartition partition){
 
 	if(supportsLabelLayout() && layoutType != LayoutTypes.NODE ) {
 
 	    Dimension initialLocation = null;
 
 	    if (canceled)
 		return;
 
 	    Boolean moveNodes;
 
 	    if (layoutType == LayoutTypes.BOTH) 
 		moveNodes = true;
 	    else
 		moveNodes = false;
 
 	    // Create new Label partition
 	    LayoutLabelPartition newPartition = new LayoutLabelPartition(partition,
 									 weightCoefficient,
 									 moveNodes,
 									 selectedOnly);
 
 	    //	logger.info("New partition succesfully created!");
 
 	    if (canceled)
 		return;
 
 	    // Layout the new partition using the parent class layout algorithm
 	    layoutPartition(newPartition);
 
 	    if (canceled)
 		return;
 
 	    // make sure nodes are where they should be
 	    for(LayoutNode node: newPartition.getLabelToParentMap().values() ) {
 
 		if (canceled)
 		    return;
 
 		node.moveToLocation();
 		// logger.info( node.toString() );
 	    }
 
 	    // make sure that all labels are where they should be 
 	    for(LayoutLabelNodeImpl node: newPartition.getLabelNodes() ) {	
 
 		if (canceled)
 		    return;
 
 		node.moveToLocation();
 		// logger.info( node.toString() );
 	    }
 
 	    taskMonitor.setStatus("Updating Display...");
 
 	    // redraw the network so that the new label positions are visible
 	    networkView.updateView();
 	    networkView.redrawGraph(true, true);
 
 	} else { // normal (non-label) layout
 
 	    layoutPartition(partition);
 	}
 
     }
 
     /**
      * Adds the necessary tunables to control the label layout.
      */
     public void getLabelTunables(LayoutProperties layoutProperties) {
 
 	layoutProperties.add(new Tunable("labels_settings", 
 					 "General Layout Settings",
					 Tunable.GROUP, new Integer(4))); 
 
 	layoutProperties.add(new Tunable("layout_type", 
 					 "Which elements to layout",
 					 Tunable.LIST, new Integer(0),
 					 (Object) layoutChoices, (Object) null, 0));
 
 	layoutProperties.add(new Tunable("resetSelectedLabelsButton", 
 					 "(Affects only selected nodes)",
 					 Tunable.BUTTON, "Reset Label Positions", this, null, 0));
 
 	layoutProperties.add(new Tunable("resetAllLabelsButton", 
 					 "(Affects all nodes)",
 					 Tunable.BUTTON, "Reset Label Positions", this, null, 0));
 
 	layoutProperties.add(new Tunable("weightCoefficient", 
					 "Weight coefficient",
 					 Tunable.DOUBLE, new Double(weightCoefficient)));
     }
 
     /**
      *  Update our tunable settings
      *
      * @param layoutProperties the LayoutProperties handler for this layout
      * @param force whether or not to force the update
      */
     public void updateSettings(LayoutProperties layoutProperties, boolean force) {
 
 	Tunable t = layoutProperties.get("layout_type");
 	if ((t != null) && (t.valueChanged() || force)) {
 	    layoutType = layoutChoices[((Integer) t.getValue()).intValue()];
 	}
 
 	t = layoutProperties.get("weightCoefficient");
 	if ((t != null) && (t.valueChanged() || force))
 	    weightCoefficient = ((Double) t.getValue()).doubleValue();
     }
 
     public void actionPerformed(ActionEvent e) {
 	String command = e.getActionCommand();
 
 	if (command.equals("resetSelectedLabelsButton")) {
 	    
 	    this.networkView =  Cytoscape.getCurrentNetworkView(); 
 	    this.network = networkView.getNetwork();
 	    
 	    boolean oldSelectedOnly = selectedOnly;
 	    selectedOnly = true;
 
 	    // reset label positions
 	    resetLabelPositions();      
 
 	    selectedOnly = oldSelectedOnly;
 	}
 
 	if (command.equals("resetAllLabelsButton")) {
 	    
 	    this.networkView =  Cytoscape.getCurrentNetworkView(); 
 	    this.network = networkView.getNetwork();
 
 	    boolean oldSelectedOnly = selectedOnly;
 	    selectedOnly = false;
 
 	    // reset label positions
 	    resetLabelPositions();      
 
 	    selectedOnly = oldSelectedOnly;
 	}
 
     }
 
 
 }
