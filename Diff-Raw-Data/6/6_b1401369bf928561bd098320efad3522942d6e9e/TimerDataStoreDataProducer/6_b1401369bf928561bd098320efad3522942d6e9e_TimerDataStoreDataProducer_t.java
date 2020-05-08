 package org.concord.data.stream;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.Timer;
 
 import org.concord.data.state.DefaultDataStoreFilterDescription;
 import org.concord.data.state.filter.DataStrictXStepFilter;
 import org.concord.framework.data.stream.DataStore;
 import org.concord.framework.data.stream.DefaultMultipleDataProducer;
 
 public class TimerDataStoreDataProducer extends DefaultMultipleDataProducer implements ActionListener
 {
 	private float lastT = Float.MIN_VALUE;
 	private int currentIndex = 0;
 	private float currentT;
 	private float currentValue;
 	protected Timer timer = null;
 	private float currentTime = 0;
 	private float timeScale = 1;
 	
 	private DataStore dataStore;
 	
 	public void setDataStore(DataStore dataStore){
 		this.dataStore = dataStore;
 		DataStrictXStepFilter xStep = new DataStrictXStepFilter();
 		DefaultDataStoreFilterDescription desc = new DefaultDataStoreFilterDescription("");
 		desc.setProperty(DataStrictXStepFilter.PROP_X_STEP, "0.1");
 		xStep.setFilterDescription(desc);
 		
 		xStep.setInputDataStore(dataStore);
 		this.dataStore = xStep.getResultDataStore();
 	}
 	
 	public void start()
 	{
 		if(timer == null) {
 	//		float dt = getDataDescription().getDt();
 	//		int tt = (int)((1000f * dt) * getTimeScale());	
 			currentT = ((Float)dataStore.getValueAt(currentIndex, 0)).floatValue();
 			currentValue = ((Float)dataStore.getValueAt(currentIndex, 1)).floatValue();
 			timer = new Timer((int)(1000*currentT), this);
 		}
 		timer.start();
 	}
 	
 	public void reset()
 	{
 		currentTime = 0;
 		stop();
 	}
 	
 	public void stop()
 	{
 		if(timer == null) return;
 		timer.stop();
 	}
 	
 	public void actionPerformed(ActionEvent e)
 	{		
 		addValues(new float[] {currentT, currentValue});
 		lastT = currentT;
 		currentIndex++;
 		if (currentIndex > dataStore.getTotalNumSamples()-1){
 			timer.stop();
 			return;
 		}
 		currentT = ((Float)dataStore.getValueAt(currentIndex, 0)).floatValue();
 		currentValue = ((Float)dataStore.getValueAt(currentIndex, 1)).floatValue();
 		timer.setDelay((int) (1000*(currentT-lastT)));
 	}
 	
 	private class Pair 
 	{
 		public Float obj1;
 		public Float obj2;
 
 		public Pair(Float obj1, Float obj2){
 			this.obj1 = obj1;
 			this.obj2 = obj2;
 		}
 	}
 
 }
