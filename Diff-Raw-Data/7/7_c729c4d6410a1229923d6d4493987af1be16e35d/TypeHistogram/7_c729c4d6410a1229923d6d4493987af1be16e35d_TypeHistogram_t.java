 package myTypeData;
 import histogram.SimpleHistogramPanel;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import javax.swing.BorderFactory;
 import javax.swing.border.BevelBorder;
 
 
 public class TypeHistogram {
     private SimpleHistogramPanel dp;
     private ArrayList data;
 
     public TypeHistogram() {
         dp = new SimpleHistogramPanel("My histogram");
       // dp.setBarNames(true);
         dp.setBackground(new Color(255, 255, 255));
         dp.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
         setData(new ArrayList<Type>());
     }
 
     public void addType(Type e) {
     	getData().add(e);
     	calcRelativeFreq();
     } 
     
     
     public SimpleHistogramPanel draw(){
     	dp.setData(getData());
     	return dp;
     }
     
     private void calcRelativeFreq(){
 		Iterator<Type> iterator = getData().iterator();
 		int total = 0;
 		Type e;
 
 		// Conta total de eventos
 		while(iterator.hasNext()){
 			e = iterator.next();
 			total += e.getFreq();
 		}
 
 		iterator = getData().iterator();
 		// Ajusta os eventos
 		while(iterator.hasNext()){
 			e = iterator.next();
 			e.setRelativeFreq(e.getFreq()/(float)(total));
 		}
 	}
 
 	public ArrayList getData() {
 		return data;
 	}
 
 	public void setData(ArrayList data) {
 		this.data = data;
 	}
  
 } 
