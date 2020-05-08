 package war.webapp.model;
 
 public class UserDuty {
     private int shift;
     private DayDuty dayDuty;
 
     public UserDuty() {
     }
 
     public UserDuty(int shift, DayDuty dayDuty) {
         this.shift = shift;
         this.dayDuty = dayDuty;
     }
 
     public int getShift() {
         return shift;
     }
 
     public void setShift(int shift) {
         this.shift = shift;
     }
 
     public DayDuty getDayDuty() {
         return dayDuty;
     }
 
     public void setDayDuty(DayDuty dayDuty) {
         this.dayDuty = dayDuty;
     }
    
    public int getDayOfWeek() {
        return dayDuty.getDayOfWeek();
    }
 }
