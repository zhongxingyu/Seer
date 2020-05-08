 package org.concord.CCProbe;
 
 import waba.ui.*;
 import waba.util.*;
 import org.concord.waba.extra.ui.*;
 import org.concord.waba.extra.event.*;
 import org.concord.waba.extra.probware.probs.*;
 import org.concord.waba.extra.probware.*;
 import extra.ui.*;
 import extra.util.*;
 import org.concord.LabBook.*;
 
 public class LObjDataCollectorProp extends LabObjectView
 {
     LObjDataCollector dc;
     Button doneButton;
     Choice probeChoice = null;
     Edit nameEdit = null;
     Label nameLabel = null;
 	Edit numDCs = null;
 
     public LObjDataCollectorProp(ViewContainer vc, LObjDataCollector d,
 								   LObjDictionary curDict)
     {
 		super(vc);
 
 		dc = d;
 		lObj = dc;
     }
     
     public void layout(boolean sDone)
     {
 		if(didLayout) return;
 		didLayout = true;
 
 		showDone = sDone;
 
 		nameLabel = new Label("Name");
 		nameEdit = new Edit();
 		nameEdit.setText("" + dc.name);
 		add(nameLabel);
 		add(nameEdit);
 
 		probeChoice = new Choice(ProbFactory.getProbNames());	
 		add(probeChoice);
 
 		numDCs = new Edit();
 		numDCs.setText("1");
 		add(numDCs);
 
 		if(showDone){
 			doneButton = new Button("Done");
 			add(doneButton);
 		} 
     }
 
     public void setRect(int x, int y, int width, int height)
     {
 		super.setRect(x,y,width,height);
 		if(!didLayout) layout(false);
 
 		nameLabel.setRect(1, 2, 30, 15);
 		nameEdit.setRect(35, 2, width-40, 15);
 
 		probeChoice.setRect(3,30, width-7, 15);
 
 		numDCs.setRect(3, 60, 30, 15);
 
 		if(showDone){
 			doneButton.setRect(width-30,height-15,30,15);
 		} 
     }
 
     public void close()
     {
 		Debug.println("Got close in document");
 		int probeId = ProbFactory.getIndex(probeChoice.getSelected());
 		LObjGraph graph = dc.getGraph();
 
 		Vector dataSources = new Vector(1);
 		LObjProbeDataSource newDS = LObjProbeDataSource.getProbeDataSource(probeId, dc.interfaceId,
 																		   CCProb.INTERFACE_PORT_A);
 		dataSources.add(newDS);
 
 		int numSources = waba.sys.Convert.toInt(numDCs.getText());
 		for(int i= 1; i < numSources; i++){
 			dataSources.add(null);
 		}
 		dc.setDataSources(dataSources);
 
 		GraphSettings gs = graph.getNewGS();
 		gs.setXUnit(CCUnit.getUnit(CCUnit.UNIT_CODE_S));
 		gs.setXLabel("Time");
 		gs.setYLabel(newDS.getLabel());
 		gs.setYUnit(newDS.getUnit());
 
 		graph.store();
 
 		if(nameEdit.getText() != "" && nameEdit.getText() != null){
 			dc.name = nameEdit.getText();
 		}
 
 		dc.closeSources();
 
 		super.close();
     }
 
     public void onEvent(Event e)
     {
 		if(e.target == doneButton &&
 		   e.type == ControlEvent.PRESSED){
 			if(container != null){
 				container.done(this);
 			}	    
 		}
     }
 }
