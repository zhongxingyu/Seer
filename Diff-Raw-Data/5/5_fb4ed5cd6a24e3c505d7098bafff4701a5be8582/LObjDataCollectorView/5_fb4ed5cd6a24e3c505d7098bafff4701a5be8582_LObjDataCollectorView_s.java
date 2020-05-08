 package org.concord.CCProbe;
 
 import graph.*;
 import waba.ui.*;
 import waba.fx.*;
 import waba.util.*;
 import extra.util.*;
 import extra.ui.*;
 import org.concord.waba.extra.ui.*;
 import org.concord.waba.extra.event.*;
 import org.concord.waba.extra.probware.probs.*;
 import org.concord.waba.extra.probware.*;
 import org.concord.LabBook.*;
 	
 public class LObjDataCollectorView extends LabObjectView
     implements ActionListener, ViewContainer, LabObjListener
 {
     LObjDataCollector dc;
     LObjGraphView gv;
     LObjDictionary dataDict = null;
     LObjGraph graph = null;
 
     Label nameLabel = null;
     Edit nameEdit = null;
 
     int gt_height = 40;
 
     Menu menu = new Menu("Edit");
 
     ToggleButton collectButton;
     Button doneB;
 
 	Label title1Label, title2Label;
     String title1 = "";
 	String  title2 = "";
 
 	String [] fileStrings = {"Beam", "Save Data..", "Export Data.."};
 	String [] palmFileStrings = {"Save Data.."};
 
     public LObjDataCollectorView(ViewContainer vc, LObjDataCollector dc, 
 							   LObjDictionary curDict)
     {
 		super(vc);
 		menu.add("Properties..");
 		menu.add("Save Profile..");
 		menu.addActionListener(this);
 
 		this.dc = dc;
 		lObj = dc;
 		dataDict = curDict;
     }
 
 	public void addMenus()
 	{
 		if(container != null){
 			container.getMainView().addMenu(this, menu);
 			if(waba.sys.Vm.getPlatform().equals("PalmOS")){
 				fileStrings = palmFileStrings;
 			}
 			container.getMainView().addFileMenuItems(fileStrings, this);
 		}
 	}
 
 	public void delMenus()
 	{
 		if(container != null){
 			container.getMainView().delMenu(this,menu);
 			container.getMainView().removeFileMenuItems(fileStrings, this);
 		}
 	}
 
 	public void setTitle1(String t1)
 	{
 		if(t1 == null) t1 = "";
 		title1Label.setText(t1);
 	}
 
 	public void setTitle2(String t2)
 	{
 		if(t2 == null) t2 = "";
 		title2Label.setText(t2);
 	}
 
 	/*
 	 *  This is very messy 
 	 * we might get stop called several time
 	 *  when we stop the data sources
 	 * this might trigger a listerner to the 
 	 * data source to post a stop event
 	 * to us.
 	 *  This is a mess.  really
 	 *  I'll fix it soon
 	 */
 	boolean stopping = false;
 
     void stop(boolean notifyGraph)
     {
 		if(stopping) return;
 		stopping = true;
 		collectButton.setSelected(false);
 		collectButton.repaint();
 		if(notifyGraph){
 			graph.stopAll();
 		}
 		stopping = false;
     }
 
     public void layout(boolean sDone)
     {
 		if(didLayout) return;
 		didLayout = true;
 
 		showDone = sDone;
 
 		collectButton = new ToggleButton("Collect", false);
 		add(collectButton);
 
 		graph = (LObjGraph)dc.getObj(0);
 		graph.addLabObjListener(this);
 
 		gv = (LObjGraphView)graph.getView(this, false, dataDict);
 		gv.showTitle(false);
 
 		gv.layout(false);
 
 		title1Label = new Label(title1);
 		add(title1Label);
 
 		title2Label = new Label(graph.title);
 		add(title2Label);
 
 		doneB = new Button("Done");
		add(doneB);

 
 		add(gv);
     }
 
 	public void labObjChanged(LabObjEvent e)
 	{
 		if(e.getObject() == graph &&
 		   graph != null){
 			setTitle2(graph.title);		
 			// this used to happen here but I don't think
 			// it is needed
 			// dc.store();  // maybe
 		}
 	}
 
     public void setRect(int x, int y, int width, int height)
     {
 		super.setRect(x,y,width,height);
 		if(!didLayout) layout(false);
 
 		int curY = 0;
 		int gHeight = height;
 
 		if(gHeight <= 160){
 			gt_height = 22;
 		}
          
 		gv.setRect(0, curY+gt_height, width, gHeight-gt_height);
 	
 		int buttonWidth = gt_height;
 		if(gt_height < 30) buttonWidth = 35;
 
 		int xPos = 0;
         collectButton.setRect(xPos,0,buttonWidth,gt_height);
 		xPos += buttonWidth+2;
 
 		title1Label.setRect(xPos, 0, width-xPos-27, gt_height/2);
 		title2Label.setRect(xPos, gt_height/2, width-xPos, gt_height/2);
 		doneB.setRect(width-27, 0, 27, gt_height/2);
 
 		setTitle1(dc.name);
 
     }
 
     public void actionPerformed(ActionEvent e)
     {
 		String command;
 		Debug.println("Got action: " + e.getActionCommand());
 
 		if(e.getSource() == menu){
 			if(e.getActionCommand().equals("Probe Properties..")){
 				stop(true);
 
 				Vector dataSources = dc.getDataSources();
 				if(dataSources == null || dataSources.getCount() < 1 ||
 				   !(dataSources.get(0) instanceof LObjProbeDataSource)){
 					return;
 				}
 
 				LObjProbeDataSource pds = (LObjProbeDataSource)dataSources.get(0);
 				pds.showProp();
 
 				Debug.println("Callllll");
 			} else if(e.getActionCommand().equals("Properties..")){
 				graph.showAxisProp();
 			} else if(e.getActionCommand().equals("Save Profile...")){
 				/*
 				LObjDocument dProf = DefaultFactory.createDocument();
 				String text = "";
 				for(int i=0; i < gv.curPtime; i++){
 					for(int j=0; j < gv.pTimes[i].length; j++){
 						text += gv.pTimes[i][j] + " ";		
 					}
 					text += "\n";
 				}
 				dProf.setText(text);
 				dProf.name = "Profile";
 		
 				if(dataDict != null){
 					dataDict.add(dProf);
 					dataDict.store();
 					dProf.store();
 				} 
 				*/
 			}
 		} else {
 			if(e.getActionCommand().equals("Save Data..")){
 				graph.saveCurData(dataDict);
 			} else if(e.getActionCommand().equals("Export Data..")){
 				graph.exportCurData();
 			}
 		}
     }
 
     public void close()
     {
 		Debug.println("Got close in graph");
 		stop(true);	
 
 		// need to make sure this unregisters data sources!!
 		gv.close();
 
 		if(graph != null) graph.delLabObjListener(this);
 
 		super.close();
     }
 
     public void onEvent(Event e)
     {		
 		if(e.target == gv){			
 			if(e.type == 1000){
 				// This must have come from the graph so
 				// I don't need to notify it
 				stop(false);
 			} 		
 		} else 	if(e.type == ControlEvent.PRESSED){
 			Control target = (Control)e.target;
 			int index;
 			if(target == collectButton && collectButton.isSelected()){
 				// need to tell the GraphView to start
 				graph.startAll();
 			} else if(target == collectButton && ! collectButton.isSelected()){
 				// need to tell the GraphView to stop
 				stop(true);
 			} else if(target == doneB){
 				// let our parent know we've been done'd
 				if(container != null){
 					container.done(this);
 				}	    
 			}
 		}  
     }
 
 	public MainView getMainView()
 	{
 		if(container != null) return container.getMainView();
 		return null;
 	}
 
     public void reload(LabObjectView source){}
 
     public void done(LabObjectView source) {}
 
 	public int getPreferredWidth(waba.fx.FontMetrics fm){
 		return -1;
 	}
 
 	public int getPreferredHeight(waba.fx.FontMetrics fm){
 		return -1;
 	}
 
 	public extra.ui.Dimension getPreferredSize(){
 		return null;
 	}
 }
