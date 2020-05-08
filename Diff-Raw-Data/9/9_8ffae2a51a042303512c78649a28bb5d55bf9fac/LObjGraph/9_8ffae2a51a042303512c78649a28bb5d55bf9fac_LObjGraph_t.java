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
 
 public class LObjGraph extends LObjSubDict
 {
     String title = null;
 	boolean autoTitle = false;
 
 	Vector graphSettings = null;
 	//	GraphSettings curGS = null;
 
 	int numDataSources = 0;
 	int curGSIndex = -1;
 
     public LObjGraph()
     {
 		super(DataObjFactory.GRAPH);
 	}
 
     public void showAxisProp()
 	{
 		showAxisProp(0);
 	}
 
     public void showAxisProp(int index)
     {
 		MainWindow mw = MainWindow.getMainWindow();
 		if(mw instanceof ExtraMainWindow){
 			LObjGraphProp gProp = (LObjGraphProp) getPropertyView(null, null);
 			gProp.index = index;
 			ViewDialog vDialog = new ViewDialog((ExtraMainWindow)mw, null, "Properties", gProp);
 			vDialog.setRect(0,0,159,159);
 			vDialog.show();
 		}
     }
 
 	public void clearDataSources()
 	{
 		// need to clear the object that are stored in this one
 		// ick.
 		numDataSources = 0;		
		graphSettings = null;
 	}
 
 	public boolean getVisible(int index)
 	{
 		if(index >= 0 && index < graphSettings.getCount()){
 			GraphSettings gs = (GraphSettings)graphSettings.get(index);
 			return gs.getVisible();
 		}
 		return false;
 	}
 
 	public void setVisible(int index, boolean val)
 	{
 		if(index >= 0 && index < graphSettings.getCount()){
 			GraphSettings gs = (GraphSettings)graphSettings.get(index);
 			gs.setVisible(val);
 		}
 	}
 
 	public void addDataSource(DataSource ds)
 	{
 		addDataSource(ds, true, -1, -1);
 	}
 
 	public void addDataSource(DataSource ds, boolean newSettings, int linkX, int linkY)
 	{
 		if(graphSettings == null){
 			graphSettings = new Vector();
 			curGSIndex = 0;
 			numDataSources = 0;
 		}
 		if(ds instanceof LabObject){
 			setObj((LabObject)ds, numDataSources);
 		} else {
 			setObj(null, numDataSources);
 		}
 
 		if(newSettings){
 			GraphSettings gs = new GraphSettings(this, numDataSources);
 			gs.setXUnit(CCUnit.getUnit(CCUnit.UNIT_CODE_S));
 			gs.setXLabel("Time");
 			if(ds instanceof DataSource){
 				gs.setYLabel(ds.getLabel());
 				gs.setYUnit(ds.getUnit());
 				title = ds.getSummary();
 			}
 			gs.linkX = linkX;
 			gs.linkY = linkY;
 			graphSettings.add(gs);
 		} else if(graphSettings.getCount() > numDataSources){
 			GraphSettings gs = (GraphSettings)graphSettings.get(numDataSources);
 			gs.dsIndex = numDataSources;
 		}
 		numDataSources++;
 	}
 
 	public DataSource getDataSource(int index)
 	{
 		if(index >= 0 && index < numDataSources){
 			LabObject obj = getObj(index);
 			if(obj instanceof DataSource){
 				return (DataSource)obj;
 			}
 		}
 		return null;
 	}
 
 	public void setCurGSIndex(int index)
 	{
 		if(index >= 0 && index < graphSettings.getCount() &&
 		   index != curGSIndex){
 			curGSIndex = index;
 			notifyObjListeners(new LabObjEvent(this, 1));
 		}
 	}
 
 	public GraphSettings getCurGraphSettings()
 	{
 		if(graphSettings != null &&
 		   curGSIndex >= 0 &&
 		   curGSIndex < graphSettings.getCount()){
 			return (GraphSettings)graphSettings.get(curGSIndex);
 		}
 		return null;
 	}
 
     public LabObjectView getPropertyView(ViewContainer vc, LObjDictionary curDict){
 		return new LObjGraphProp(vc, this, 0);
 	}
 
     public LabObjectView getView(ViewContainer vc, boolean edit, LObjDictionary curDict)
     {
 		return new LObjGraphView(vc, this, curDict);
     }
 
 
 	public void store()
 	{
 		if(autoTitle) name = "..auto_title..";
 		
 		super.store();
 	}
 
     public void readExternal(DataStream ds)
     {
 		super.readExternal(ds);
 		title = ds.readString();
 		numDataSources = ds.readInt();
 		if(numDataSources <= 0) return;
 
 		curGSIndex = ds.readInt();
 
 		graphSettings = new Vector(numDataSources);
 		for(int i=0; i<numDataSources; i++){
 			GraphSettings gs = new GraphSettings(this, i);
 			gs.readExternal(ds);
 			graphSettings.add(gs);
 		}
 		
 		
 		if(name != null && name.equals("..auto_title..")){
 			autoTitle = true;
 		} 			
 		// little white hack
 		// curGS = (GraphSettings)graphSettings.get(0);
     }
 
     public void writeExternal(DataStream ds)
     {
 		super.writeExternal(ds);
 		ds.writeString(title);
 
 		if(numDataSources <= 0 ||
 		   graphSettings == null ||
 		   numDataSources != graphSettings.getCount()){
 			ds.writeInt(0);		
 			return;
 		} else {
 			ds.writeInt(numDataSources);
 		}
 		
 		ds.writeInt(curGSIndex);
 
 		for(int i=0; i<graphSettings.getCount(); i++){
 			GraphSettings gs = (GraphSettings)graphSettings.get(i);
 			gs.writeExternal(ds);
 		}
     }
 
 	public LabObject copy(int gsIndex)
 	{
 		if(gsIndex >= 0 &&
 		   gsIndex < numDataSources){
 			LObjGraph g = DataObjFactory.createGraph();
 			
 			g.title = title.toString();
 			g.graphSettings = new Vector(graphSettings.getCount());
 		
 			GraphSettings gs = (GraphSettings)graphSettings.get(gsIndex);
 			g.graphSettings.add(gs.copy(g));
 			g.curGSIndex = 0;
 			g.numDataSources = 0;
 			
 			return g;
 		}
 
 		return null;
 	}
 
 	
 	public LabObject copy()
 	{
 		return copy(0);
 	}
 
 	public void startAll()
 	{ 
 		if(graphSettings == null) return;
 		for(int i=0; i<graphSettings.getCount(); i++){
 			GraphSettings gs = (GraphSettings)graphSettings.get(i);
 			if(gs != null) gs.startDataDelivery();
 		}
 	}
 
 	public void stopAll()
 	{
 		if(graphSettings == null) return;
 		for(int i=0; i<graphSettings.getCount(); i++){
 			GraphSettings gs = (GraphSettings)graphSettings.get(i);
 			if(gs != null) gs.stopDataDelivery();
 		}
 	}		
 
 	public void clearAll()
 	{
 		if(graphSettings == null) return;
 		for(int i=0; i<graphSettings.getCount(); i++){
 			GraphSettings gs = (GraphSettings)graphSettings.get(i);
 			gs.clear();
 		}
 	}
 
 	public void closeAll()
 	{
 		if(graphSettings == null) return;
 		for(int i=0; i<graphSettings.getCount(); i++){
 			GraphSettings gs = (GraphSettings)graphSettings.get(i);
 			if(gs != null) gs.close();
 		}
 	}
 
 	public void saveAllAnnots()
 	{
 		if(graphSettings == null) return;
 		for(int i=0; i<graphSettings.getCount(); i++){
 			GraphSettings gs = (GraphSettings)graphSettings.get(i);
 			if(gs != null) gs.saveAnnots();
 		}
 	}
 
 	public void saveCurData(LObjDictionary dataDict)
 	{
 		LObjDataSet dSet = DataObjFactory.createDataSet();
 
 		LObjGraph dsGraph = (LObjGraph)copy();
 		dsGraph.name = "Graph";
 
 		dSet.setDataViewer(dsGraph);
 		dSet.clearAnnots();
 		GraphSettings curGS = getCurGraphSettings();
 		if(curGS != null){
 			curGS.saveData(dSet);
 			Vector annots = curGS.getAnnots();
 			dSet.addAnnots(annots);
 			
 			if(dataDict != null){
 				dataDict.add(dSet);				
 				dSet.store();
 				dsGraph.addDataSource(dSet, false, -1, -1);
 				dsGraph.store();
 			} else {
 				// for now it is an error
 				// latter it should ask the user for the name
 			}
 		}
 
 	}
 
 	public void exportCurData()
 	{
 		GraphSettings curGS = getCurGraphSettings();
 		if(curGS != null){
 			Bin curBin = curGS.getBin();
 			if(curBin != null){
 				curBin.description = title;
 				DataExport.export(curBin);
 			}
 		}
 	}
 }
