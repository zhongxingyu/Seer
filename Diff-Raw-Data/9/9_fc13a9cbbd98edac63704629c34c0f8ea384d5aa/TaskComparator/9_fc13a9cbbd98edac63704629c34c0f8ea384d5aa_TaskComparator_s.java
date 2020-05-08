 package ca.ualberta.cs.completemytask.userdata;
 
 import java.util.Comparator;
 
 
 /**
  * Compares to tasks. This is used to sort
  * the tasks for the main menu list view.
  * 
  * @author Michael Feist
  *
  */
 public class TaskComparator implements Comparator<Task>{
  
     public int compare(Task task1, Task task2) {
  
     	int sharedOrder1 = (task1.isPublic() ? 1 : 0) + (task1.isComplete() ? 2 : 0);
     	int sharedOrder2 = (task2.isPublic() ? 1 : 0) + (task2.isComplete() ? 2 : 0);
     	
     	int sharedOrder = sharedOrder1 - sharedOrder2;
     	
         // Compare if Shared
         if (sharedOrder > 0){
             return +1;
         }else if (sharedOrder < 0){
             return -1;
         }else{
         	String date1 = task1.getDate();
             String date2 = task2.getDate();
             
            //int date = 0;
             
            //if (date1 != null && date2 != null){
            	int date = date1.compareTo(date2);
            //}
             	
         	if (date != 0) {
                 return date;
             } else {
                 
             	String name1 = task1.getName();
                 String name2 = task2.getName();
                 
                 int name = name1.compareTo(name2);
                 
                 return name;
             }
         }
     }
 }
