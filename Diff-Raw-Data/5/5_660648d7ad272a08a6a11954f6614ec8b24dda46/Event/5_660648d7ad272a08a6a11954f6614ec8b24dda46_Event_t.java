 
 /**
  * Stores information regarding an appointment/event.
  *
  * @author cmcl
  * @version 1.0
  */
public class Event implements Comparable<Event> {
     private String name;
     private String location;
     private String desc; // description
     private Date startDate;
     private Date endDate;
     private Repetition rep;
     
     public Event(String name, String loc, String desc, Date start,
                  Date end) {
         this.name = name;
         location = loc;
         this.desc = desc;
         startDate = start;
         endDate = end;
     }
     
     public Event() {
         this("", "", "", new Date(), new Date());
     }
     
     public String getName() { return name; }
     public String getLocation() { return location; }
     public String getDescription() { return desc; }
     public Date getStartDate() { return startDate; }
     public Date getEndDate() { return endDate; }
     public Repetition getRepetition() {return rep; }
     
    public int compareTo(Event v) {
         return 0;
     }
     
     public void setName(String name) { this.name = name; }
     public void setLocation(String loc) { location = loc; }
     public void setDescription(String desc) { this.desc = desc; }
     public void setStartDate(Date start) { startDate = start; }
     public void setEndDate(Date end) { endDate = end; }
     public void setRepetition(Repetition rp) { rep = rp; }
     
     public String toString() {
         StringBuilder str = new StringBuilder();
         
         str.append(String.format("Name : "+name+"%n"));
         str.append(String.format("Location : "+location+"%n"));
         str.append(String.format("Description : "+desc+"%n"));
         str.append(String.format("Start Date : "+startDate+"%n"));
         str.append(String.format("End Date : "+endDate+"%n"));
         str.append(String.format("Repetition : "+rep));
         
         return str.toString();
     }
     
     /**
      * Test method
      *
      */
     public static void main(String[] args) {
         Event e1 = new Event("Partae", "Ma hoose", 
                 "Gettin' wrecked", new Date(31, 12, 2012), 
                 new Date(2, 1, 2013));
         Event e2 = new Event();
         System.out.println(e1);
         System.out.println("==============");
         System.out.println(e2);
     }
     
 }
