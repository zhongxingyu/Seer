 /*
  * Created by: Christopher Feveck
  * Purpose: To create the simulate the events of the elevator and the people
  * who get on the elevator as well as the movement.
  * 
  * Note: a priority queue will be used to create this event simulator. 
  * The events will be have numbers associated with the occurance of each event
  * when the event occurs the number associated with it will be added to the
  * event taking place: eg add_person-2 when that event is called two will be
  * added to the already existing number 2 so the add_person will be 
  * add_person-4
  */
 package Elevator;
 import java.util.*;
 public class Events 
 {
     private PriorityQueue<Object> p_queue;
     private int sim_tick;
     private LinkedList[] building_floors;
     
     //speed of elevator
     private int elevator_tick=3;
     //arrival of people
     private int people_tick=2;
     //number of floors
     private int floors = 7;   
     private int tick;
     private String class_type;
     
     private int e_algorithm_type;
     
     public Events()
     {
         sim_tick = 0;
 e_algorithm_type = 1;
         p_queue=new PriorityQueue<Object>();
         this.tick=1;
         building_floors = new LinkedList[floors];
         //building_floors = (LinkedList<Person>[]) new LinkedList[floors];
         for(int x=0; x<floors;x++)
         {
             building_floors[x] = new LinkedList<Person>();
         }
         //this.class_type="elevator";
     }
     
     //object that creates a person object that moves in the elevator
     public void set_floor_person_list(int floor, Person obj)
     {
         building_floors[floor].add(obj);
     }
     
     public LinkedList get_floor_person_list(int floor)
     {
         return building_floors[floor];
     }
     
     public void set_class_type(String class_type)
     {
         this.class_type=class_type;
     }
     
     public String get_class_type()
     {
         return class_type;
     }
     
     public void set_elevator_tick(int elevator_tick)
     {
         this.elevator_tick=elevator_tick;
     }
     
     public int get_elevator_tick()
     {
         return elevator_tick;
     }
     
     private void collective_up_collective_down(Object obj)
     {
                     if(((Elevator)obj).Get_direction() == true)
                     {
                         if(((Elevator)obj).Increment() == false)
                         {
                             ((Elevator)obj).Decrement();
                         }
                     }
                     else if(((Elevator)obj).Get_direction() == false)
                     {
                         if(((Elevator)obj).Decrement() == false)
                         {
                             ((Elevator)obj).Increment();
                         }
                     }   
         
     }
     //need to modify
     private void collective_up_selective_down(Object obj)
     {
         //set the lowest floor to the highest floor
         int lowest = floors;
         
                     if(((Elevator)obj).Get_direction() == true)
                     {
                         if(((Elevator)obj).Increment() == false)
                         {
                             ((Elevator)obj).Decrement();
                         }
                     }
                     
                     
                     else if(((Elevator)obj).Get_direction() == false)
                     {
                         //get the requests list
                         Iterator ite = ((Elevator)obj).Get_RequestIterator();
                         while(ite.hasNext())
                         {
                             //check for the lowest request
                             if(((Request)ite.next()).get_from_floor_request() < lowest)
                             {
                                 //set the lowest request
                                 lowest = ((Request)ite.next()).get_from_floor_request();
                             }
                         }
                         
                         //keep going down until reach the lowest requested floor
                         if(((Elevator)obj).Get_CurrentFloor() > lowest)
                         {
                             //ensure that it does no go past the requested floor
                             if(((Elevator)obj).Get_CurrentFloor() == lowest)
                             {
                                 //change direction to up
                                 ((Elevator)obj).Set_Direction(true);
                             }
                             else if(((Elevator)obj).Decrement() == false)
                             {
                                 ((Elevator)obj).Increment();
                             }
                             
                         }
                         else if(((Elevator)obj).Get_CurrentFloor() == lowest)
                         {
                             ((Elevator)obj).Set_Direction(true);
                         }
                             
                     }   
         
     }
     public void start()            
     {
         p_queue.add(new Elevator(elevator_tick,floors));
         p_queue.add(new Create_class("people", people_tick));
         Object obj;
         
         while(p_queue.size()!=0)
         {
             obj=p_queue.poll();
             
             if(obj instanceof Elevator)
             {
                     sim_tick = ((Elevator)obj).get_tick();
                     
                     switch(e_algorithm_type)
                     {
                         case 0:
                             collective_up_collective_down(obj);
                             break;
                          case 1:
                             collective_up_selective_down(obj);
                             break;
                             
                         default:
                             collective_up_collective_down(obj);
                             break;
                     }
 ((Elevator)obj).set_tick(((Elevator)obj).get_tick()+elevator_tick);
                     
 
 //picks/drop the people                        
                     Iterator iter = ((Elevator)obj).Get_RequestIterator();
                     
                     //Request elevReqs;
                     //iterate the requests on the elevator
                     while(iter.hasNext())
                     {
                         Request elevReqs = null;
                         try
                         {
                             elevReqs = (Request)iter.next();
                         }
                         catch(Exception e){
                             break;
                         }
                         
                         //Collective up collective down
                         if(e_algorithm_type == 0)
                         {
                             //pickup
                             if(elevReqs.get_from_floor_request() == ((Elevator)obj).Get_CurrentFloor())
                             {
                                 Iterator iterf;
                                 iterf = this.building_floors[((Elevator)obj).Get_CurrentFloor()].iterator();
 
                                 Person tmpPer;
                                 //Request perReq;
                                 
 
                                 //iterate the persons on the floor
                                 while(iterf.hasNext())
                                 {
                                         tmpPer = (Person)iterf.next();
                                         if(tmpPer.get_request().get_from_floor_request() == ((Elevator)obj).Get_CurrentFloor())
                                         {
                                             ((Elevator)obj).Set_Persons(tmpPer);
                                             iterf.remove();
                                         }
                                         tmpPer = null;
                                     
                                 }
 
 
                             }
                             
                             //drop person off
                             if((elevReqs.get_to_floor_request() == ((Elevator)obj).Get_CurrentFloor()) &&
                                     (((Elevator)obj).Get_PersonsLinkedList().size() > 0))
                             {
                                 Iterator iterf;
                                 iterf = ((Elevator)obj).Get_PersonsIterator();
 
                                 Person tmpPer;
                                 //Request perReq;
                                 
 
                                 //iterate the persons on the floor
                                 while(iterf.hasNext())
                                 {
                                         tmpPer = (Person)iterf.next();
                                         if(tmpPer.get_request().get_to_floor_request() == ((Elevator)obj).Get_CurrentFloor())
                                         {
                                              this.building_floors[((Elevator)obj).Get_CurrentFloor()].add(tmpPer);
                                             iterf.remove();
                                         }
                                         tmpPer = null;
                                     
                                 }
 
 
                             }
                         }
                         
                         //Collective up selective down
                         else if(e_algorithm_type == 1)
                         {
                             //check that the elevator is moving up
                             //if(((Elevator)obj).Get_direction() == true)
                             {
                                 
                             /*
                              * check if any request is in the Priority Event Queue, 
                              * for the floor the elevator is currently on
                              */
                                 Iterator ite = p_queue.iterator();
                                 //going through all items in the priority Queue
                                 while(ite.hasNext())
                                 {
                                     //holds the cureent object of the iterator
                                     Object tmp = ite.next();
                                     if( tmp instanceof Request)
                                     {
                                         //check if request floor is the same as elevator current floor
                                         // and direction == false, but the next tick will move the elevator
                                         // to floor 1 and direction to true
                                         if (  (((Request)tmp).get_from_floor_request() == 0) && (((Elevator)obj).Get_CurrentFloor() == 0) 
                                                 && (((Elevator)obj).Get_direction() == false )  )
                                         {
                                             //put the request on the elevator
                                             ((Elevator)obj).Set_Request( ((Request)tmp));
                                         }
                                         else if (  (((Request)tmp).get_from_floor_request() == (((Elevator)obj).Get_CurrentFloor())) 
                                                 && (((Elevator)obj).Get_direction() == true )  )
                                         {
                                             //put the request on the elevator
                                             ((Elevator)obj).Set_Request( ((Request)tmp));
                                         }
                                     }
                                 }
                                 
                                 //if request(s) are present, put in elevator request queue
                             }
                                 
 
                             
                             //pickup
                             if(elevReqs.get_from_floor_request() == ((Elevator)obj).Get_CurrentFloor())
                             {
                                 Iterator iterf;
                                 iterf = this.building_floors[((Elevator)obj).Get_CurrentFloor()].iterator();
 
                                 Person tmpPer;
                                 //Request perReq;
                                 
 
                                 //iterate the persons on the floor
                                 while(iterf.hasNext())
                                 {
                                         tmpPer = (Person)iterf.next();
                                         if(tmpPer.get_request().get_from_floor_request() == ((Elevator)obj).Get_CurrentFloor())
                                         {
 /*
  * iterate the priority queue and find the correcpond Request for 
  * each person as you bring them unto the elevator
  */ 
 
 Iterator it1 = p_queue.iterator();
 while(it1.hasNext())
 {
     Object ob = it1.next();
     if(ob instanceof Request)
     {
         if (    
                  (tmpPer.get_request().get_from_floor_request() == ((Request)ob).get_from_floor_request()) &&
                 (tmpPer.get_request().get_to_floor_request() == ((Request)ob).get_to_floor_request()) &&
                 (tmpPer.get_request().get_direction_request() == ((Request)ob).get_direction_request()) )
         {
             ((Elevator)obj).Set_Request((Request)ob);
             it1.remove();
             
         }
     }
 }
                                             ((Elevator)obj).Set_Persons(tmpPer);
                                             iterf.remove();
                                         }
                                         tmpPer = null;
                                    
                                 }
 
 
                             }
 
                             //drop person off
                             if((elevReqs.get_to_floor_request() == ((Elevator)obj).Get_CurrentFloor()) &&
                                     (((Elevator)obj).Get_PersonsLinkedList().size() > 0))
                             {
                                 Iterator iterf;
                                 iterf = ((Elevator)obj).Get_PersonsIterator();
 
                                 Person tmpPer;
                                 //Request perReq;
                                 
 
                                 //iterate the persons on the floor
                                 while(iterf.hasNext())
                                 {
                                         tmpPer = (Person)iterf.next();
                                         if(tmpPer.get_request().get_to_floor_request() == ((Elevator)obj).Get_CurrentFloor())
                                         {
                                              this.building_floors[((Elevator)obj).Get_CurrentFloor()].add(tmpPer);
                                             iterf.remove();
                                             ((Elevator)obj).Remove_Request();
                                         }
                                         tmpPer = null;
                                     
                                 }
 
 
                             }
                             
                             
                         }
                     }
 //*************************                            
 
                     
 
                     
                         
                         
                         
                     p_queue.add(obj);
                     System.out.println("Elevator time:"+((Elevator)obj).get_tick()+", direction: "+((Elevator)obj).Get_direction() + ", Floor:"+((Elevator)obj).Get_CurrentFloor());                ;
             }
             if(obj instanceof Person)
             {
                     sim_tick = ((Person)obj).get_tick();
                     
                     Request personRequest= new Request( ((Person)obj).get_tick());
 personRequest.set_to_floor_request(4);
 personRequest.set_from_floor_request(((Person)obj).get_current_floor());
 //personRequest.set_from_floor_request(2);                    
                     //if the current floor less than destination floor this means that the direction is up
                     if(personRequest.get_from_floor_request() < personRequest.get_to_floor_request())
                     {
                         personRequest.set_direction_request(true);
                     }
                     //else if the current floor is more than the destination florr this means that the direction is down
                     else if(personRequest.get_from_floor_request() > personRequest.get_to_floor_request())
                     {
                         personRequest.set_direction_request(false);
                     }
                     
                     Request elevRequest = personRequest;
                     
                     ((Person)obj).set_request(personRequest);
                     p_queue.add(elevRequest);
                     //put person on the floor linklist
 //building_floors[((Person)obj).get_destination_floor()].add(((Person)obj));
 building_floors[((Person)obj).get_request().get_from_floor_request()].add(((Person)obj));                    
                     
                     System.out.println("Person waiting time:"+((Person)obj).get_tick()+", destination: "+((Person)obj).get_request().get_to_floor_request() + ", Floor:"+((Person)obj).get_current_floor());
                     
                     
                     
                     
                     //after request is added                                         
                     System.out.println("People time:"+((Person)obj).get_tick())                ;
             }
             else if(obj instanceof Create_class)
             {
                 if(((Create_class)obj).get_class_type().compareTo("people") == 0)
                 {
                     sim_tick = ((Create_class)obj).get_tick();
                     
 p_queue.add(new Person(sim_tick,2));
                     ((Create_class)obj).set_tick(sim_tick+people_tick);
                     p_queue.add(obj);
 
                 }
             }
             else if(obj instanceof Request)
             {
                 
                 
                     Iterator it = p_queue.iterator();
                     Object elevator = null;
 //<editor-fold> 
                     while(it.hasNext())
                     {
                         elevator = it.next();
                         if(elevator instanceof Elevator)
                             break;
                         //System.out.println(cur.getClass().getName() )                        ;
                     }
                     if(this.e_algorithm_type == 0)
                     {
                         if((Elevator)elevator instanceof Elevator)
                         {
 
                            // Elevator tmp = ((Elevator)it.next());
                            // tmp.Set_Request(new Request(2));
                             ((Elevator)elevator).Set_Request( ((Request)obj));
                         }
                     }
 
                     else if(this.e_algorithm_type == 1)
                     {
                         if((Elevator)elevator instanceof Elevator)
                         {
                             if((((Elevator)elevator).Get_CurrentFloor() == ((Request)obj).get_from_floor_request() )
                                   && (((Elevator)elevator).Get_direction() == true)  )
                             {
                                 ((Elevator)elevator).Set_Request( ((Request)obj));
                             }
                             else
                             {
                                 ((Request)obj).set_tick(sim_tick+elevator_tick);
 
 p_queue.add((Request)obj);
                             }
                         }
                     } 
 
                     
 //</editor-fold>                 
             }
         }
     }
          
 
     public static void main(String[] args)
     {
         new Events().start();
     
     }
     
     
 }
