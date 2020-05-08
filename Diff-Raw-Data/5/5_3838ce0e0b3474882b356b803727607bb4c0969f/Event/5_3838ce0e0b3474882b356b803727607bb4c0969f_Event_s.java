 /*
  * Event.java
  * Author: Nick Grauel
  * Group: Nick Grauel, Tyler Janowski, Nick DeRossi
  * LAST UPDATED 12/1
  * This class represents a generic event.  It is the basis for the ActivityEvent
  * and TravelEvent subclasses.
  */
 
 import java.util.*;
 
 
 public class Event{
     //------------------------Instance Variables----------------------
     protected String name;                  //Event name.
     protected String service;               //Service name.
     protected GregorianCalendar startTime;  //Start time for the event.
     protected GregorianCalendar endTime;    //End time for the event.
     protected Providers availableProviders; //Collection providers that offer 
                                                                     //the event.
     protected Providers usedProviders;      //Provider(s) being used for the event.
     protected int dayOfTour;                //The day of the event.
     protected boolean hasProvider;         //True if there are providers being 
                                                              //used for this event.
     protected String location;				//Location of the event.
     //Constructor
     public Event(String n, int d, GregorianCalendar s, GregorianCalendar e, 
             Providers provs, String serv, String loc)
     {
     	location = loc;
 	service = serv;
 	hasProvider = false;
         name = n;
         dayOfTour = d;
         startTime = s;
         endTime = e;
         availableProviders = new Providers();
         usedProviders = new Providers();
 
         for(int x = 0; x < provs.size(); x++)
         {
             Date eOpen = startTime.getTime();
             Date eClose = endTime.getTime();
             GregorianCalendar g1 = new GregorianCalendar(2011, 11, 10, 
            		eOpen.get(Calendar.HOUR_OF_DAY), eOpen.get(Calendar.MINUTE));
             GregorianCalendar g2 = new GregorianCalendar(2011, 11, 10, 
            		eClose.get(Calendar.HOUR_OF_DAY), eClose.get(Calendar.MINUTE));
             eOpen = g1.getTime();
             eClose = g2.getTime();
             GregorianCalendar open = provs.get(x).getOpenTime();
             GregorianCalendar close = provs.get(x).getClosingTime();
             Date o = open.getTime();
             Date c = close.getTime();
             if(o.compareTo(eOpen) <= 0 && c.compareTo(eClose) >= 0)
             {
                 if(provs.get(x).getService().equals(serv))
                 {
                 	//System.err.println("Event Location: " + location + " Provider Location: " + provs.get(x).getLocation());
                 	if(provs.get(x).getLocation().equals(location))
                 	{
                 		boolean done = false;
                 		for(int y = 0; y < availableProviders.size(); y++)
                 		{
                 			if(availableProviders.get(y).getCapacity() < provs.get(x).getCapacity())
                 			{
                 				availableProviders.add(y, provs.get(x));
                     			done = true;
                 			}
                 		}
                 		if(done == false)
                 			availableProviders.add(provs.get(x));
                 	}
                 }
             }
         }
     }
     
     //Updates the providers being used based on the current capacity.
     public void updateProviders(int curCap, int numDoubles)
     {
     	usedProviders.clear();
     	hasProvider = false;
     	Providers p = availableProviders;
     	if(p.size() > 0)
     	{
     		if(p.get(0).getCapacity() > curCap)
     		{
     			hasProvider = true;
     			usedProviders.add(p.get(0));
     		}
     		else{
     			for(int x = 1; x < availableProviders.size(); x++)
     			{
     				if(hasProvider == false)
     				{
     					int totalCap = 0;
     					for(int y = 0; y <= x; y++)
     					{
     						totalCap += p.get(y).getCapacity();
     					}
     					if(totalCap > curCap)
     					{
     						for(int y = 0; y <= x; y++)
     						{	
     							int cap = p.get(y).getCapacity();
     							cap = cap/2;
     							while(cap > 0 && numDoubles > 0)
     							{
     								numDoubles--;
     								cap--;
     							}
     							usedProviders.add(p.get(y));
     						}
     						if(numDoubles == 0)
     							hasProvider = true;
     						else usedProviders.clear();	
 
     					}
     				}
     			}	  
     		} 
     	}
     }
 
     //Returns the type of event.
     public String getType() { return service; }
     
     //Returns the name of the event.
     public String getName() { return name; }
 
     //Returns the number day of tour the even is scheduled for
     public int getDay() { return dayOfTour; }
     
     //Returns the start time of the event in GregorianCalendar form.
     public GregorianCalendar getStartTime() { return startTime; }
 
     //Returns the end time of the event in GregorianCalendar form.
     public GregorianCalendar getEndTime() { return endTime; }
     
     //Adds a provider to the collection of providers.
     public void addProvider(Provider p) 
     {
     	 Date eOpen = startTime.getTime();
          Date eClose = endTime.getTime();
          GregorianCalendar open = p.getOpenTime();
          GregorianCalendar close = p.getClosingTime();
          Date o = open.getTime();
          Date c = close.getTime();
          if(o.compareTo(eOpen) <= 0 && c.compareTo(eClose) >= 0)
          {
             if(p.getService().equals(service))
             {
             	boolean done = false;
             	for(int y = 0; y < availableProviders.size(); y++)
             	{
             		if(availableProviders.get(y).getCapacity() < p.getCapacity())
             		{
             			availableProviders.add(y, p);
             			done = true;
             			break;
             		}
             	}
             	if(done == false)
             		availableProviders.add(p); 
             }
          }
           
     }
 
     //Returns the provider(s) being used by this event.
     public Providers getUsedProviders() { return usedProviders; }
     
     //Returns true if the event has providers for it.  Returns false otherwise.
     public boolean hasProvider() { return hasProvider; }
 
     //Returns true if this event occurs at the same time as a given event.
     public boolean isConflicting(Event e)
     {
         boolean conflicting = false;
         //Start and end dates for this event.
         Date e1Start = startTime.getTime();
         Date e1End = endTime.getTime();
         
         //Start and end dates for argument.
         Date e2Start = e.getStartTime().getTime();
         Date e2End = e.getEndTime().getTime();
         
         //Cases where the event times would conflict.
         if(e1Start.compareTo(e2Start) < 0 && e1End.compareTo(e2Start) > 0)
             conflicting = true;
         if(e1Start.compareTo(e2End) < 0 && e1End.compareTo(e2End) > 0)
             conflicting = true;
         if(e1Start.compareTo(e2Start) < 0 && e1End.compareTo(e2End) > 0)
             conflicting = true;
         if(e1Start.compareTo(e2Start) > 0 && e1End.compareTo(e2End) < 0)
             conflicting = true;
         return conflicting;
     }
     
     /* Returns string representation of the event.  Used for events with no
      * provider.
      */
     public String toString()
     {
         String s = "";
         s += "Day #" + dayOfTour + " " + name + " from " 
                 + startTime.get(Calendar.HOUR_OF_DAY) + ":" 
                 + startTime.get(Calendar.MINUTE) + " to "
                 + endTime.get(Calendar.HOUR_OF_DAY) + ":" 
                 + endTime.get(Calendar.MINUTE);
         return s;
     }
    
 }
