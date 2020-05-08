 package org.concord.CCProbe;
 
 import waba.util.*;
 import waba.ui.*;
 
 import org.concord.waba.extra.io.*;
 import org.concord.waba.extra.util.*;
 import org.concord.waba.extra.event.*;
 import org.concord.waba.extra.ui.*;
 
 import org.concord.LabBook.*;
 import org.concord.ProbeLib.*;
 import org.concord.waba.graph.*;
 
 public class LObjIntProbeTrans extends LObjSubDict
 	implements DataSink, DataSource, DataListener
 {
 	Vector dataListeners = new Vector();
 	private LObjProbeDataSource dataSource = null;
 
 	int chPerSample;
 
     public LObjIntProbeTrans()
     {
 		super(DataObjFactory.INT_PROBE_TRANS);
 	}
 
 	int type = -1;
 	public int getType(){return type;}
 	public void setType(int t)
 	{
 		type = t;
 	}
 
 	public String getQuantityMeasured(LabBookSession session)
 	{
 		if(dataSource == null) getDataSource(session);
 
 		if(dataSource != null) return dataSource.getQuantityMeasured(type);
 		return null;
 	}
 
 	public void dataStreamEvent(DataEvent dataEvent)
 	{
 		notifyDataListenersEvent(dataEvent);
 	}
 
 	public void dataReceived(DataEvent dataEvent)
 	{
 		notifyDataListenersReceived(dataEvent);
 	}
 
 	public void addDataListener(DataListener l){
 		if(dataListeners == null) dataListeners = new waba.util.Vector();
 		if(dataListeners.find(l) < 0) dataListeners.add(l);
 		//		if(dataSource == null) getDataSource();
 	}
 	public void removeDataListener(DataListener l){
 		if(dataListeners == null) return;
 		int index = dataListeners.find(l);
 		if(index >= 0) dataListeners.del(index);
 	}
 
 	public void notifyDataListenersEvent(DataEvent e){
 		if(dataListeners == null) return;
 		for(int i = 0; i < dataListeners.getCount(); i++){
 			DataListener l = (DataListener)dataListeners.get(i);
 			l.dataStreamEvent(e);
 		}
 	}
 
 	public void notifyDataListenersReceived(DataEvent e){
 		if(dataListeners == null) return;
 		for(int i = 0; i < dataListeners.getCount(); i++){
 			DataListener l = (DataListener)dataListeners.get(i);
 			l.dataReceived(e);
 		}
 	}
 
 	public void setDataSource(LObjProbeDataSource ds)
 	{
 		if(dataSource != ds){
 			if(dataSource != null){
 				dataSource.setModeDataListener(null, type);
 			}
 			dataSource = ds;
 			if(dataSource != null){
 				dataSource.setModeDataListener(this, type);
 			}
 			if(ds instanceof LabObject) setObj((LabObject)ds,0);
 		}
 	}
 
 	void setupDataSource(LabBookSession session)
 	{
 		LabObject obj = getObj(0, session);
 		if(obj != null && obj instanceof DataSource){
 			if(dataSource != null && dataSource != obj){
 				dataSource.setModeDataListener(null, type);
 			}
 			dataSource = (LObjProbeDataSource)obj; 
 			dataSource.setModeDataListener(this, type);
 		}
 	}
 
 	DataSource getDataSource(LabBookSession session)
 	{
 		LabObject obj = getObj(0, session);
 		if(obj != null && obj instanceof DataSource){
 			dataSource = (LObjProbeDataSource)obj; 
 			return dataSource;
 		}
 		return null;
 	}
 
 	public void closeEverything()
 	{
 		if(dataSource != null){
 			dataSource.setModeDataListener(null, type);
 			dataSource.closeEverything();
 		}
 		dataSource = null;
 	}
 
 	public void startDataDelivery(LabBookSession session)
 	{
 		setupDataSource(session);
 		if(dataSource != null){
 			dataSource.startDataDelivery(session);
 		}
 	}
 
 	public void stopDataDelivery()
 	{
 		if(dataSource != null){
 			dataSource.stopDataDelivery();
 			dataSource.setModeDataListener(null, type);
 		}
 	}
 
 	public CCUnit getUnit(LabBookSession session)
 	{
 		if(dataSource == null) getDataSource(session);
 		if(dataSource == null) return null;
 		return dataSource.getQuantityUnit(type);
 	}
 
 	public int getPrecision()
 	{
 		if(dataSource == null) return DecoratedValue.UNKNOWN_PRECISION;
		return dataSource.getQuantityPrecision(type);
 	}		
 
 	public boolean 	setUnit(CCUnit unit){return false;}
 
 	public String getLabel(){return "";}
 
 	public String getSummary(LabBookSession session)
 	{
 		String quant =  getQuantityMeasured(session);
 		if(quant == null) return "";
 
 		getDataSource(session);
 		if(dataSource == null) return quant;
 
 		return quant + "(" + dataSource.getProbeSummary() + ")";
 	}
 
 	// add the root sources to the passed in vector
 	public void getRootSources(Vector sources, LabBookSession session)
 	{
 		getDataSource(session);
 		if(dataSource != null && sources != null){
 			dataSource.getRootSources(sources, session);
 		}
 	}
 
 	public void writeExternal(DataStream out)
 	{
 		out.writeInt(type);
 	}
 
 	public void readExternal(DataStream in)
 	{
 		type = in.readInt();
 	}
 }
