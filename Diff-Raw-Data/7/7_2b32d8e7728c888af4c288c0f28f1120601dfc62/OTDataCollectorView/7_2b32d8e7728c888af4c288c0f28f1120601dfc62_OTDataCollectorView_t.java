 
 /*
  *  Copyright (C) 2004  The Concord Consortium, Inc.,
  *  10 Concord Crossing, Concord, MA 01741
  *
  *  Web Site: http://www.concord.org
  *  Email: info@concord.org
  *
  *  This library is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU Lesser General Public
  *  License as published by the Free Software Foundation; either
  *  version 2.1 of the License, or (at your option) any later version.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *  Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public
  *  License along with this library; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  */
 
 /*
  * Created on Mar 8, 2005
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package org.concord.datagraph.state;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Insets;
 import java.awt.Window;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.geom.Point2D;
 import java.util.EventObject;
 import java.util.Vector;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JPanel;
 import javax.swing.JToggleButton;
 import javax.swing.SwingUtilities;
 import javax.swing.text.ComponentView;
 
 import org.concord.data.state.OTDataStore;
 import org.concord.data.ui.DataFlowControlAction;
 import org.concord.data.ui.DataFlowControlButton;
 import org.concord.data.ui.DataFlowControlToolBar;
 import org.concord.data.ui.DataStoreLabel;
 import org.concord.data.ui.DataValueLabel;
 import org.concord.datagraph.engine.ControllableDataGraphable;
 import org.concord.datagraph.engine.DataGraphable;
 import org.concord.datagraph.ui.AddDataPointLabelAction;
 import org.concord.datagraph.ui.DataGraph;
 import org.concord.datagraph.ui.DataPointLabel;
 import org.concord.datagraph.ui.SingleDataAxisGrid;
 import org.concord.framework.data.stream.DataProducer;
 import org.concord.framework.otrunk.OTObjectList;
 import org.concord.framework.otrunk.OTrunk;
 import org.concord.framework.otrunk.view.OTObjectView;
 import org.concord.framework.otrunk.view.OTViewContainer;
 import org.concord.graph.engine.GraphableList;
 import org.concord.graph.engine.SelectableList;
 import org.concord.graph.event.GraphableListListener;
 import org.concord.graph.examples.GraphWindowToolBar;
 import org.concord.graph.ui.Grid2D;
 import org.concord.graph.ui.SingleAxisGrid;
 import org.concord.graph.util.control.DrawingAction;
 import org.concord.graph.util.ui.ResourceLoader;
 import org.concord.swing.SelectableToggleButton;
 
 /**
  * @author scott
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 public class OTDataCollectorView
     implements OTObjectView, GraphableListListener
 {
     OTDataCollector dataCollector;
 	protected OTViewContainer viewContainer;
 	DataGraph dataGraph;
 	SelectableList notesLayer;
 	DataGraphable sourceGraphable;
 	DataProducer sourceProducer;
 	DataValueLabel valueLabel;
 	OTDataStore dataStore = null;
 	JDialog dialog;
 	
 	OTDataAxis xOTAxis;
 	OTDataAxis yOTAxis;
 	
 	public OTDataCollectorView(OTDataCollector collector, OTViewContainer container)
 	{
 	    dataCollector = collector;
 	    viewContainer = container; 
 	}
     
     /* (non-Javadoc)
      * @see org.concord.framework.otrunk.view.OTObjectView#getComponent(boolean)
      */
     public JComponent getComponent(boolean editable)
     {
 		dataGraph = new DataGraph();
 		dataGraph.changeToDataGraphToolbar();
 
 		//Add notes button
 		notesLayer = new SelectableList();
 		dataGraph.getGraph().add(notesLayer);
 		SelectableToggleButton addNoteButton = new SelectableToggleButton(new AddDataPointLabelAction(notesLayer, dataGraph.getObjList()));
 		dataGraph.getToolBar().addButton(addNoteButton, "Add a note to a point in the graph");
 		//
 		
 		dataGraph.setAutoFitMode(DataGraph.AUTO_SCROLL_RUNNING_MODE);
 		
 		xOTAxis = dataCollector.getXDataAxis();
 		yOTAxis = dataCollector.getYDataAxis();
 
 		OTObjectList pfGraphables = dataCollector.getGraphables();
 
 		OTObjectList pfDPLabels = dataCollector.getLabels();
 		
 		DataFlowControlToolBar toolBar = null;
 
 		dataGraph.setLimitsAxisWorld(xOTAxis.getMin(), xOTAxis.getMax(),
 				yOTAxis.getMin(), yOTAxis.getMax());
 
 		GraphWindowToolBar gwToolbar = dataGraph.getToolBar();
 		if(gwToolbar != null) {
 		    // FIXME
 			gwToolbar.setVisible(editable);
 		}
 		
 		Grid2D grid = dataGraph.getGrid();
 
 		SingleDataAxisGrid sXAxis = (SingleDataAxisGrid)grid.getXGrid();
 
 		DataGraphStateManager.setupAxisLabel(sXAxis, xOTAxis);
 		
 		SingleDataAxisGrid sYAxis = (SingleDataAxisGrid)grid.getYGrid();
 		DataGraphStateManager.setupAxisLabel(sYAxis, yOTAxis);
 
 		Vector realGraphables = new Vector();
 		
 		// for each list item get the data producer object
 		// add it to the data graph
 		for(int i=0; i<pfGraphables.size(); i++) {
 			OTDataGraphable otGraphable = (OTDataGraphable)pfGraphables.get(i);
 			OTDataStore dataStore = (OTDataStore)otGraphable.getDataStore();
 			
 			// dProducer.getDataDescription().setDt(0.1f);
 			DataGraphable realGraphable = otGraphable.getDataGraphable();
 			
 			if(dataStore == null) {
 			    System.err.println("Trying to display graphable with out a data store");
 			    continue;
 			}
 			
 			realGraphable.setDataStore(dataStore);
 			realGraphable.setChannelX(otGraphable.getXColumn());
 			realGraphable.setChannelY(otGraphable.getYColumn());
 
 			realGraphables.add(realGraphable);
 			dataGraph.addBackgroundDataGraphable(realGraphable);
 		}
 
 		OTDataGraphable source = dataCollector.getSource();
 		if(source != null) {
 			sourceProducer = (DataProducer)source.getDataProducer();
 			dataStore = (OTDataStore)source.getDataStore();
 
 			String title = dataCollector.getTitle(); 
 			if(title == null) {
 			    title = source.getName();			    
 			}
 			
 			if(title != null) {
 			    dataGraph.setTitle(title);
 			}
 
 			sourceGraphable = source.getDataGraphable();
 
 			// dProducer.getDataDescription().setDt(0.1f);
 			if(sourceGraphable instanceof ControllableDataGraphable) {
 				sourceGraphable.setDataStore(dataStore, 
 						source.getXColumn(), 
 						source.getYColumn());
 
 				// TODO need to add the sketch components here
 			    JPanel bottomPanel = new JPanel(new FlowLayout());
 			    JButton clearButton = new JButton("Clear");
 			    clearButton.addActionListener(new ActionListener(){
 			       public void actionPerformed(ActionEvent e){
 			           dataGraph.reset();			           
 			       }
 			    });
 			    
 				DrawingAction a = new DrawingAction();
 				a.setDrawingObject((ControllableDataGraphable)sourceGraphable);
 				gwToolbar.addButton(new SelectableToggleButton(a), "Draw a function");
 				
 			    bottomPanel.add(clearButton);
 
 			    dataGraph.add(bottomPanel, BorderLayout.SOUTH);  			    			    
 			} else if(sourceProducer != null) {
 			    // need to set the data store to be the data store for this
 			    // graphable
 			    if(dataStore != null && !dataCollector.getSingleValue()){
 			        dataStore.setDataProducer(sourceProducer);
 					sourceGraphable.setDataStore(dataStore);
 			    } else {
 					sourceGraphable.setDataProducer(sourceProducer);
 					// this doesn't add the producer to the datagraphs
 					// producers list
 			    }
 				sourceGraphable.setChannelX(source.getXColumn());
 				sourceGraphable.setChannelY(source.getYColumn());
 			    
 			    JPanel bottomPanel = new JPanel(new FlowLayout());
 			    valueLabel = new DataValueLabel(sourceProducer);
 			    valueLabel.setColumns(4);
 			    bottomPanel.add(valueLabel);
 			    if(!dataCollector.getSingleValue()) {
 			        toolBar = createFlowToolBar();
 			        bottomPanel.add(toolBar);
 			        toolBar.addDataFlowObject((DataProducer)sourceProducer);
 			    } else {
 			        JButton record = new JButton("Record");
 			        record.addActionListener(new ActionListener(){
 			            public void actionPerformed(ActionEvent e){
 			                sourceProducer.stop();
 			                float currentValue = valueLabel.getValue();
 			                int lastSample = dataStore.getTotalNumSamples();
 			                dataStore.setValueAt(lastSample, 0, new Float(currentValue));
 			                dataGraph.reset();
 			                dialog.hide();
 			            }
 			        });
 			        
 			        JButton cancel = new JButton("Cancel");
 			        cancel.addActionListener(new ActionListener(){
 			            public void actionPerformed(ActionEvent e){
 			                sourceProducer.stop();
 			                dataGraph.reset();
 			                dialog.hide();
 			            }
 			        });
 			        bottomPanel.add(record);
 			        bottomPanel.add(cancel);
 			    }
 
 			    dataGraph.add(bottomPanel, BorderLayout.SOUTH);  			    
 			}
 
 			if(sourceGraphable != null) {
 			    realGraphables.insertElementAt(sourceGraphable, 0);
 			    dataGraph.addDataGraphable(sourceGraphable);
 			}
 		}
 
 		if(realGraphables.size() > 1) {
 		    
 		    DataGraphableTree dTree = new DataGraphableTree();
 		    // add legend to the left
 		    for(int i=0; i<realGraphables.size(); i++){
 		        dTree.addGraphable((DataGraphable)realGraphables.get(i));
 		    }
 		    
 		    dataGraph.add(dTree, BorderLayout.WEST);
 		}
 				
 		/*
 		JPanel graphWrapper = new JPanel(){
 		  public void removeNotify()
 		  {
 		      System.err.println("got remove notify");
 		      
 		      // FIXME need to only reset the sourceGraphable
 		      // dataGraph.reset();
 		  }
 		};
 		
 		graphWrapper.setLayout(new BorderLayout());
 		graphWrapper.add(dataGraph, BorderLayout.CENTER);
 		return graphWrapper;
 		*/
 
         if(dataCollector.getSingleValue()){
             DataStoreLabel dataLabel = new DataStoreLabel(dataStore, 0);
             if(!editable) return dataLabel;
             
             JPanel svPanel = new JPanel(new FlowLayout());
             dataLabel.setColumns(4);
             svPanel.add(dataLabel);
             JButton cDataButton = new JButton();
             ImageIcon icon = ResourceLoader.getImageIcon("data_graph_button.gif", "Collect Data");
             cDataButton.setIcon(icon);
             cDataButton.setToolTipText(icon.getDescription());
             cDataButton.setMargin(new Insets(2,2,2,2));
             cDataButton.addActionListener(new ActionListener(){
                 public void actionPerformed(ActionEvent event)
                 {
                     boolean needPack = false;
                     if(dialog == null) {
                         dialog = new JDialog();
                         dialog.setSize(400,400);
                         needPack = true;
                     }
                     dialog.getContentPane().setLayout(new BorderLayout());
                     dialog.getContentPane().removeAll();
                     dialog.getContentPane().add(dataGraph, BorderLayout.CENTER);
                     /*
                     if(needPack){
                         dialog.pack();
                     }
                     */
                     
                     dialog.show();
                     sourceProducer.start();
                     dataGraph.start();
                 }                
             });
             svPanel.add(cDataButton);
             return svPanel;
         }
         
         //Load the data point labels
         for (int i=0; i<pfDPLabels.size(); i++){
 			OTDataPointLabel otDPLabel = (OTDataPointLabel)pfDPLabels.get(i);
         	
 			//Create a data point label
 			DataPointLabel l = new DataPointLabel();
 			
 			notesLayer.add(l);
 			
 			loadStateLabel(l, otDPLabel);
         }
         //
         
 		GraphableList graphableList = dataGraph.getObjList();
 		graphableList.addGraphableListListener(this);
 		
 		notesLayer.addGraphableListListener(this);
 		
 		dataGraph.setPreferredSize(new Dimension(400,320));
 		
 		return dataGraph;
     }
 
 	public DataFlowControlToolBar createFlowToolBar()
 	{
 	    DataFlowControlToolBar toolbar = 
 	        new DataFlowControlToolBar(false);
 
 		DataFlowControlButton b = null;
 		
 		b = new DataFlowControlButton(DataFlowControlAction.FLOW_CONTROL_START);
 		toolbar.add(b);
 
 		b = new DataFlowControlButton(DataFlowControlAction.FLOW_CONTROL_STOP);
 		toolbar.add(b);
 		
 		b = new DataFlowControlButton(DataFlowControlAction.FLOW_CONTROL_RESET);
 		b.setText("Clear");
 		toolbar.add(b);
 	 
 		toolbar.addDataFlowObject(dataGraph);
 		
 	    return toolbar;
 	}
 	
 	/**
 	 * This only works for graphables that came from a loaded
 	 * pfgraphables.  It doesn't yet handel cases where new
 	 * graphables are created by some external thing
 	 *
 	 */
 	public void updateState()
 	{
 		Grid2D grid = dataGraph.getGrid();
 
 		xOTAxis.setMin((float)dataGraph.getMinXAxisWorld());
 		xOTAxis.setMax((float)dataGraph.getMaxXAxisWorld());
 		yOTAxis.setMin((float)dataGraph.getMinYAxisWorld());
 		yOTAxis.setMax((float)dataGraph.getMaxYAxisWorld());
 
 		SingleAxisGrid sXAxis = grid.getXGrid();
 		if(sXAxis.getAxisLabel() != null){
 			xOTAxis.setLabel(sXAxis.getAxisLabel());
 		}
 		
 		SingleAxisGrid sYAxis = grid.getYGrid();
 		if(sYAxis.getAxisLabel() != null){
 			yOTAxis.setLabel(sYAxis.getAxisLabel());
 		}
 
 		OTDataGraphable source = dataCollector.getSource();
 
 		source.saveObject();
 		
 		// Save data point labels
 		for (int i=0; i<notesLayer.size(); i++){
 			OTDataPointLabel otDPLabel = (OTDataPointLabel)dataCollector.getLabels().get(i);
 			DataPointLabel l = (DataPointLabel)notesLayer.elementAt(i);
 			
 			saveStateLabel(otDPLabel, l);
         }
 		
 	}
 	
 	/**
 	 * @see org.concord.graph.event.GraphableListListener#listGraphableAdded(java.util.EventObject)
 	 */
 	public void listGraphableAdded(EventObject e)
 	{
 		Object obj = e.getSource();
 		if (obj instanceof DataPointLabel){
 			DataPointLabel l;
 			OTDataPointLabel otLabel;
 
 			try{
 				otLabel = (OTDataPointLabel)dataCollector.getOTDatabase().createObject(OTDataPointLabel.class);
 			}
 			catch (Exception ex) {
 				ex.printStackTrace();
 				return;
 			}
 			
 			l = (DataPointLabel)obj;
 			
 			saveStateLabel(otLabel, l);
 			
 			dataCollector.getLabels().add(otLabel);
 		}
 	}
 		
 	/* (non-Javadoc)
 	 * @see org.concord.graph.event.GraphableListListener#listGraphableChanged(java.util.EventObject)
 	 */
 	public void listGraphableChanged(EventObject e)
 	{
 		updateState();
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.concord.graph.event.GraphableListListener#listGraphableRemoved(java.util.EventObject)
 	 */
 	public void listGraphableRemoved(EventObject e)
 	{
 	}
 	
 	/**
 	 * @param otLabel
 	 * @param l
 	 */
 	private void saveStateLabel(OTDataPointLabel otLabel, DataPointLabel l)
 	{
 		otLabel.setColor(l.getBackground().getRGB());
 		if (l.getDataPoint() != null){
 			otLabel.setXData((float)l.getDataPoint().getX());
 			otLabel.setYData((float)l.getDataPoint().getY());
 		}
		if (l.getDataGraphable() != null){
//			otLabel.setDataGraphable();
		}
 		otLabel.setX((float)l.getLocation().getX());
 		otLabel.setY((float)l.getLocation().getY());
 
 		otLabel.setText(l.getMessage());
 		
 		OTrunk otrunk = dataCollector.getOTDatabase();
 		OTDataGraphable otGraphable = (OTDataGraphable)otrunk.getWrapper(l.getDataGraphable());
 		otLabel.setDataGraphable(otGraphable);
 	}
 
 	/**
 	 * @param l
 	 * @param otDPLabel
 	 */
 	private void loadStateLabel(DataPointLabel l, OTDataPointLabel otDPLabel)
 	{
 		Point2D locPoint = null;
 		Point2D dataPoint = null;
 		
 		l.setMessage(otDPLabel.getText());
 		l.setBackground(new Color(otDPLabel.getColor()));
 		if (!Float.isNaN(otDPLabel.getX()) && !Float.isNaN(otDPLabel.getY())){
 			locPoint = new Point2D.Double(otDPLabel.getX(), otDPLabel.getY());
 		}
 		if (!Float.isNaN(otDPLabel.getXData()) && !Float.isNaN(otDPLabel.getYData())){
 			dataPoint = new Point2D.Double(otDPLabel.getXData(), otDPLabel.getYData());
 		}
 		
 		if (dataPoint != null){
 			l.setDataPoint(dataPoint);
 			if (locPoint != null){
 				l.setLocation(locPoint);
 			}
 			else{
 				l.setLocation(l.getTextBoxDefaultLocation(null, dataPoint));
 			}
 		}
 		else{
 			l.setLocation(locPoint);
 		}
		
 		OTDataGraphable otGraphable = otDPLabel.getDataGraphable();
 		if (otGraphable != null){		    
 			l.setDataGraphable(otGraphable.getDataGraphable());
 		}
		l.setGraphableList(dataGraph.getObjList());
 		l.setSelectionEnabled(otDPLabel.getSelectable());
 	}
 
 }
