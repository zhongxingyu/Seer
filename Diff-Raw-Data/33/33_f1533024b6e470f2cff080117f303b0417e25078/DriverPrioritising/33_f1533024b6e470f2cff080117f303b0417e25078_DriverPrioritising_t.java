 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  *
  * Class that prioritises, it mains function is to return an array list of
  * prioritised drivers in order. Each driver is put into a group.
  * 5 groups work on each day, each group works 5 days a week.
  * It will prioritise which group should have its drivers picked first on which day
  * Also, within each group, all drivers will be prioritised within in th group based on hours
  * worked
  * @author nathantilsley
  */
 public class DriverPrioritising {
     
     // Arraylist of arraylist of drivers
     private static ArrayList<ArrayList<Integer>> groupDrivers;
     
     // Groups timetable for a week
     private static int [][] weekdaysGroups = {
    {4,5,6,7,1},
    {5,6,7,1,2},
    {6,7,1,2,3},
    {7,1,2,3,4},
     {1,2,3,4,5},
     {2,3,4,5,6},
     {3,4,5,6,7}
     };
     
     // method use date and number of drivers to calculate the correct groups to work on that day
     // along with the correct drivers
     // returns 2D array [group] [workers in that group]
     public static ArrayList<Integer> getDrivers(Date date, int numberOfDrivers)
     {
         int[] groupsWorkingOnDate = getGroupsInAWeekday(date);
         groupDrivers = groupDrivers();
         
         int extraDrivers = 0;
         
         if(numberOfDrivers % 5 != 0)
             extraDrivers = numberOfDrivers % 5;
         
         
         int numberOfDriversPerGroup = numberOfDrivers / 5;
         
         
         if(numberOfDriversPerGroup == 0)
             numberOfDriversPerGroup = 1;
         
         int numberOfGroups = numberOfDrivers;
         
         // when there is more than 5 dr
         if(numberOfDrivers >= 5)
             numberOfGroups = 5;
         
         // an arraylist of arraylists that contains arraylists of groups that can work
         ArrayList<ArrayList<Integer>> groups = new ArrayList<ArrayList<Integer>>();
         
         for(int i = 0; i < numberOfGroups; i++)
         {
             int temp = 0;
             if(extraDrivers > 0)
             {
                 temp++;
                 extraDrivers--;
             }
             ArrayList<Integer> groupToAdd = new ArrayList<Integer>();
             groupToAdd = groupDrivers.get(groupsWorkingOnDate[i] - 1);
             groupToAdd = prioritiseDriversInGroup(groupToAdd, numberOfDriversPerGroup + temp, date);
             groups.add(groupToAdd);
         }
         
         ArrayList<Integer> drivers = new ArrayList<Integer>();
         
         for(int i = 0;i < groups.size(); i++)
         {
             ArrayList<Integer> tempArray = groups.get(i);
             for(int j =0; j < tempArray.size(); j++)
             {
                 drivers.add(tempArray.get(j));
             }
         }
         
         return drivers;
     }
     
     public static ArrayList<ArrayList<Integer>> addGroup(int numberOfDrivers, int numberOfDriversPerGroup,
                                                          int[] groupsWorkingOnDate, Date date)
     {
         ArrayList<ArrayList<Integer>> groups = new ArrayList<ArrayList<Integer>>();
         
         for(int i = 0; i < numberOfDrivers; i++)
         {
             ArrayList<Integer> groupToAdd = new ArrayList<Integer>();
             groupToAdd = groupDrivers.get(groupsWorkingOnDate[i] - 1);
             groupToAdd = (ArrayList) prioritiseDriversInGroup(groupToAdd, numberOfDriversPerGroup, date);
             groups.add(groupToAdd);
         }
         return groups;
     }
     
     
     // author Nikita
     public static ArrayList<Integer> prioritiseDriversInGroup(ArrayList<Integer> group,
                                                               int numberOfDriversPerGroup, Date date)
     {
         int prioritisedDriver;
         ArrayList<Integer> groupPrioritised = new ArrayList<Integer>();
         
         while(numberOfDriversPerGroup != 0)
         {
             prioritisedDriver = calculatePriority(group, date);
             for (int i=0; i< group.size(); i++) {
                 int val = group.get(i);
                 //System.out.println(val);
                 if (val == prioritisedDriver) {
                     groupPrioritised.add(group.get(i));
                     group.remove(i);
                     break;
                 }
             }
             numberOfDriversPerGroup--;
         }
         return groupPrioritised;
     }
     
     public static int[] getGroupsInAWeekday(Date date)
     {
         Calendar c = Calendar.getInstance();
         c.setTime(date);
         int[] groupsInAWeekday = weekdaysGroups[c.get(Calendar.DAY_OF_WEEK) - 1];
         return groupsInAWeekday;
     }
     
     // author Nikita
     // Returns a 2 dimensional ArrayList [Group][List of drivers ID]
     public static ArrayList<ArrayList<Integer>> groupDrivers()
     {
         //database.openBusDatabase();
         ArrayList<ArrayList<Integer>> groups = new ArrayList<ArrayList<Integer>>();
         
         for(int i = 0; i < 7; i++)
         {
             groups.add(new ArrayList());
         }
         
         int [] drivers = DriverInfo.getDrivers();
         for (int i = 0; i < drivers.length; i++)
         {
             groups.get((drivers[i] - 2012) % 7).add(drivers[i]);
         }
         return groups;
     }
     
     // Search through database and prioritise a driver in that group
     public static int calculatePriority(ArrayList<Integer> group, Date date)
     {
         int chosenDriver = - 1;
         
         //
         //
         //
         ArrayList<Integer> available = new ArrayList<Integer>();
         
         // check which drivers in that group are available on that day
         for(int i = 0; i < group.size(); i++)
             if(DriverInfo.isAvailable(group.get(i)))
             {
                 available.add(group.get(i));
             }
         
         //
         //
         //
         // check to see which driver has the least hours this week so far
         ArrayList<Integer> leastHoursInWeek = new ArrayList<Integer>();
         
         // start at the max and go down
         int leastHoursWorkedThisWeek = 50;
         
         // find the least hours worked by a driver in that group
         for(int i = 0; i < available.size(); i++)
             if(DriverInfo.getHoursThisWeek(available.get(i)) < leastHoursWorkedThisWeek)
             {
                 leastHoursWorkedThisWeek = DriverInfo.getHoursThisWeek(available.get(i));
             }
         
         // check which drivers in that group are available on that day
         for(int i = 0; i < available.size(); i++)
             if(DriverInfo.getHoursThisWeek(available.get(i)) == leastHoursWorkedThisWeek)
             {
                 leastHoursInWeek.add(available.get(i));
             }
         
         //
         //
         // Then check which driver has had the least hours this year?
         
         // an array for drivers with least hours
         ArrayList<Integer> leastHoursInYear = new ArrayList<Integer>();
         
         if(leastHoursInWeek.size() > 1)
         {
             
             
             // start at the max hours possible in a year (50 * 52)
             int leastHoursWorkedThisYear = 2600;
             
             // find the least hours worked by a driver in that group
             for(int i = 0; i < leastHoursInWeek.size(); i++)
                 if(DriverInfo.getHoursThisYear(leastHoursInWeek.get(i)) < leastHoursWorkedThisYear)
                 {
                     leastHoursWorkedThisYear = DriverInfo.getHoursThisWeek(leastHoursInWeek.get(i));
                 }
             
             // check which drivers that have worked the least hours this year
             for(int i = 0; i < leastHoursInWeek.size(); i++)
                 if(DriverInfo.getHoursThisWeek(leastHoursInWeek.get(i)) == leastHoursWorkedThisYear)
                 {
                     leastHoursInYear.add(leastHoursInWeek.get(i));
                 }
         }
         else
             return leastHoursInWeek.get(0);
         
         //
         //
         //
         // if not one driver left, pick a random one, as all are even
         if(leastHoursInYear.size() > 1)
         {
             // make a random index, multiply it by array length - 1 because its an index
             int randomIndex = (leastHoursInYear.size() - 1) * (int)Math.random();
             chosenDriver = leastHoursInYear.get(randomIndex);
         }
         else
             chosenDriver = leastHoursInYear.get(0);
         
         return chosenDriver;
     }
     
     public static void main (String[] args)
     {
         database.openBusDatabase();
         Date test = new Date(2013, 2, 16);
         ArrayList<Integer> groupsThatCanWork = new ArrayList<Integer>();
         groupsThatCanWork = getDrivers(test, 24);
         
         for (int i = 0; i < groupsThatCanWork.size(); i++)
         {
             System.out.println("Driver that can work: " + groupsThatCanWork.get(i) + " " + (i+1));
         }
         int[] dayGroups = getGroupsInAWeekday(test);
         SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM");
         System.out.println("Groups for " + dateFormat.format(test) + ": ");
         
         for (int i = 0; i < dayGroups.length; i++)
         {
             System.out.print(dayGroups[i] + ", ");
         }
         
     }
 }
