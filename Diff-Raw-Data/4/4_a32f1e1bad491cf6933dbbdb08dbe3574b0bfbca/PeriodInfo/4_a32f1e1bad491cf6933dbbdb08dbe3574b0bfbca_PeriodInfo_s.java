 package Client.Entities;
 
 import java.util.List;
 
 public  class PeriodInfo {
   private  List<Period> periods;
    private int maxPeriods;
    private int actPeriod;
    
    public PeriodInfo(){
 	   this.actPeriod = 0;
 	   periods.add(new Period());
    }
    
    public Period getActualPeriod(){
 	   return periods.get(actPeriod);
    }
    
    public Period getPeriod(int period){
 	   return periods.get(period);
    }
 
    public void nextPeriod(){
 	   incNumberOfActPeriod();
 	   periods.add(new Period());
    }
 
    public int getMaxPeriods() {
 	return maxPeriods;
    }
    
    public void setMaxPeriods(int maxPeriods){
 	   this.maxPeriods = maxPeriods;
    }
 
    public int getNumberOfActPeriod() {
 	return actPeriod;
 }
 
    private void incNumberOfActPeriod(){
 	actPeriod++;
    }
 	
 }
