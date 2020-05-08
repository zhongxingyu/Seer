 package org.concord.CCProbe;
 
 import waba.util.*;
 import waba.ui.*;
 import extra.io.*;
 import extra.util.*;
 import org.concord.LabBook.*;
 import graph.*;
 import org.concord.waba.extra.event.*;
 import org.concord.waba.extra.ui.*;
 import org.concord.waba.extra.util.*;
 import extra.util.*;
 
 public class LObjGraphProp extends LabObjectView
 	implements ActionListener
 {
     PropContainer propsGraph = null;
 	PropContainer propsXAxis = null;
 	PropContainer propsYAxis = null;
 	PropObject propDataSources;
 	PropObject propVisibleSources = null;
 	PropObject propTitle;
 
 	LObjGraph graph;
 
 	Label graphSummary = new Label("");
 	Axis visYAxis = null;
 	Axis visXAxis = null;
 
 	PropertyView propView = null;
 	int index=0;
 
 	String [] testList = {"test1", "test2"};
 
 	public LObjGraphProp(ViewContainer vc, LObjGraph g, int index)
     {
 		super(vc);
 		graph = g;
 		lObj = g;	
 		this.index = index;
 
 		setupProperties();
 	}
 
 	public void layout(boolean sDone)
 	{
 		if(didLayout) return;
 		didLayout = true;
 
 		propView = new PropertyView(this);
 		PropertyPane graphPane = new PropertyPane(propsGraph, propView);		
 		graphPane.addTopLabel(graphSummary);
 		graphPane.setAlignment(PropertyView.ALIGN_TOP);
 
 		propView.addPane(graphPane);
 		propView.addContainer(propsYAxis);
 		propView.addContainer(propsXAxis);
 		propView.setCurTab(index);
 		add(propView);
 	}
 
     public void setRect(int x, int y, int width, int height)
     {
 		super.setRect(x,y,width,height);
 		if(!didLayout) layout(false);
 
 		propView.setRect(0,0,width,height);
 	}
 
 	String [] dsStrings = null;
 
 	public void setupProperties()
 	{
 		int id = 0;
 
 		GraphSettings curGS = graph.getCurGraphSettings();
 		if(curGS == null) return;
 
 		if(propsGraph == null){
 			propsGraph = new PropContainer("Graph");
 			visYAxis = curGS.getYAxis();
 			visXAxis = curGS.getXAxis();
 
 			if(visYAxis == null || visXAxis == null) return;
 			propsYAxis = visYAxis.getPropContainer();
 			propsYAxis.setName("YAxis");
 			propsXAxis = visXAxis.getPropContainer();
 			propsXAxis.setName("XAxis");
 
 			propTitle = new PropObject("Title", "Title", id++, graph.getTitleNoSummary());
 			propTitle.prefWidth = 120;
 
			if(propDataSources != null) propsGraph.addProperty(propDataSources);
			if(propVisibleSources != null) propsGraph.addProperty(propVisibleSources);
 			propsGraph.addProperty(propTitle);
 			
 			String summary = graph.getSummary();
 			if(summary == null) summary = "";
 			graphSummary.setText(summary);
 
 			dsStrings = new String [graph.numDataSources];
 			for(int i=0; i<graph.numDataSources; i++){
 				DataSource ds = graph.getDataSource(i);
 				dsStrings[i] = ds.getQuantityMeasured();
 			}
 			if(graph.getMaxLines() != 1){
 				int defIndex = graph.getCurGraphSettings().dsIndex;
 				propDataSources = new PropObject("Data", "Data", id++, dsStrings, defIndex);
 				propDataSources.prefWidth = 120;
 				propDataSources.setType(PropObject.CHOICE_SETTINGS);
 				propDataSources.setSettingsButtonName("Setup");
 			}
 
 			if(dsStrings.length > 1){
 				propVisibleSources = new PropObject("Visible", "Visible", id++, dsStrings);
 				propVisibleSources.prefWidth = 120;
 				propVisibleSources.setType(PropObject.MULTIPLE_SEL_LIST);
 				if(graph.getMaxLines() == 1) propVisibleSources.setRadio(true);
 				for(int i=0; i<dsStrings.length; i++){
 					propVisibleSources.setCheckedValue(i, graph.getVisible(i));
 				}
 			}
 			
 		} else {
 			propTitle.setValue(graph.getTitleNoSummary());
 
 			String summary = graph.getSummary();
 			if(summary == null) summary = "";
 			graphSummary.setText(summary);
 
 			if(propDataSources != null){
 				propDataSources.setValue(dsStrings[graph.getCurGraphSettings().dsIndex]);
 			}
 
 			if(propVisibleSources != null){
 				if(graph.getMaxLines() == 1) propVisibleSources.setRadio(true);
 				for(int i=0; i<dsStrings.length; i++){
 					propVisibleSources.setCheckedValue(i, graph.getVisible(i));
 				}
 			}
 
 			visYAxis = curGS.getYAxis();
 			visXAxis = curGS.getXAxis();
 
 			if(visYAxis == null || visXAxis == null) return;
 			propsYAxis = visYAxis.getPropContainer();
 			propsYAxis.setName("YAxis");
 			propsXAxis = visXAxis.getPropContainer();
 			propsXAxis.setName("XAxis");
 		}
 	}
 
 	public void actionPerformed(ActionEvent e)
 	{
 		GraphSettings curGS = graph.getCurGraphSettings();
 
 		if(e.getActionCommand().equals("Apply")){
 			int firstDS = -1;
 			if(propVisibleSources != null){
 				String [] dsNames = propVisibleSources.getPossibleValues();
 				for(int i=0; i<dsNames.length; i++){
 					graph.setVisible(i, propVisibleSources.getCheckedValue(i));
 					if(propVisibleSources.getCheckedValue(i)){
 						firstDS = i;
 					}
 				}
 			}
 
 			GraphSettings newGS;
 			if(propDataSources == null){
 				newGS = graph.getCurGraphSettings();
 			} else {
 				int dsIndex = propDataSources.getIndex();
 				graph.setCurGSIndex(dsIndex);
 				newGS = graph.getCurGraphSettings();
 			}
 
 			if(newGS == null || visXAxis == null || visYAxis == null) return;
 			visXAxis.applyProperties();
 			visYAxis.applyProperties();
 			newGS.updateAxis();
 
 			// This should be cleaned up
 			String newTitle = propTitle.getValue();
 			if(newTitle != null && newTitle.length() <= 0){
 				newTitle = null;
 			}
 			graph.setTitle(newTitle);
 
 			graph.notifyObjListeners(new LabObjEvent(graph, 0));
 		} else if(e.getActionCommand().equals("Setup")){
 			// This should be an index for safety
 			String dataSourceName = propDataSources.getValue();
 			if(dsStrings != null){
 				for(int i=0; i<dsStrings.length; i++){
 					if(dsStrings[i].equals(dataSourceName)){
 						DataSource selDS = graph.getDataSource(i);
 						if(selDS instanceof LObjProbeDataSource){
 							LObjProbeDataSource pds = (LObjProbeDataSource)selDS;
 							pds.showProp();
 						}
 						return;
 					}
 				}
 			}
 		} else if(e.getActionCommand().equals("Close")){
 			// this is a cancel or close
 			if(container != null){
 				container.done(this);
 			}	    
 		}
 	}
 }
