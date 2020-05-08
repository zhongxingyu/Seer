 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package viriussimulator;
 import java.util.ArrayList;
 import java.util.Random;
 import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
 
 /**
  *
  * @author baroba
  */
 public class City extends location 
 {
     public City (int streets)
     {
         Random generator = new Random();
         System.out.println("Creating city");        
         nodes = new ArrayList<vNode>();        
         nodes.add(new streetNode("Route 1", generator.nextInt(1000)));
         int streetnames = 0;
         for (int i = 0; i < streets; i++) 
         {
            ArrayList<vNode> temp = streetMaker(generator.nextInt(100), generator.nextInt(100), 2 + i, 1 + streetnames);
             streetnames += temp.size() - 1;
             nodes.get(0).addConnection(temp.get(0));
             temp.get(0).addConnection(nodes.get(0));
             nodes.addAll(temp);
            System.out.println(nodes.get(0).connections.size() +" node size");
         }
         buildingNode start = new buildingNode("start point", 5000, 20);
         Virus infection = new illness();
         nodes.get(0).addConnection(start);
         start.addConnection(nodes.get(0));
         nodes.add(start);
         for (int i = 0; i < nodes.size(); i++) 
         {
             nodes.get(i).setMap(nodes);
         }
         for (int i = 0; i < nodes.size(); i++) 
         {
             nodes.get(i).createAgents();
         }
         for (int i = 0; i < start.inhabitants.size(); i++) 
         {
             Agent agent = start.inhabitants.get(i);
             agent.infect(infection);
         }
 /*     nodes.add(new streetNode("route 1", 400));
      nodes.add(new buildingNode("building 1", 500, 10));
      nodes.add(new buildingNode("building 2", 200, 10));
      nodes.add(new buildingNode("building 3", 600, 10));
      nodes.add(new buildingNode("building 4", 1000, 10));
      System.out.println("Adding connections"); 
      for (int i = 1; i < 5; i++) 
      {
         nodes.get(0).addConnection(nodes.get(i));
         nodes.get(i).addConnection(nodes.get(0)); 
         System.out.println("Number of connections is: " + nodes.get(i).connections.size()); 
 
      }
      System.out.println("StreetConnection size is: " + nodes.get(0).connections.size()); 
      
       
      for (int i = 0; i < nodes.size(); i++) 
         {
             nodes.get(i).setMap(nodes);
         }
      for (int i = 0; i < nodes.size(); i++) nodes.get(i).createAgents();
      nodes.get(4).inhabitants.get(0).infect(new illness());*/
      
      
     }
     public  void update()
     {
         for (int i = 0; i < nodes.size(); i++) {
             vNode node = nodes.get(i);
             node.infected();
             node.avoidance();
             node.updateStage();
 
             node.update();
         }
         
         for (int i = 0; i < nodes.size(); i++) 
         {
             vNode node = nodes.get(i);
             System.out.println(node.readOut());
         }
         
     };
 
     
     public ArrayList streetMaker(int size, int population, int street, int building)
     {
         Random generator = new Random();        
         ArrayList<vNode> val=new ArrayList<>();
         val.add(new streetNode("street "+street,generator.nextInt(100)));
         for (int i = 0; i < size; i++) 
         {
            building++;
             val.add(new buildingNode("building "+building, generator.nextInt(2000), population));
         }
         System.out.println("Adding connections"); 
      for (int i = 1; i < val.size(); i++) 
      {
         val.get(0).addConnection(val.get(i));
         val.get(i).addConnection(val.get(0)); 
 
      }
                  System.out.println(val.get(0).connections.size()+" val connections");
 
       
      System.out.println(val.size()+" street size");
      return val;
     }
     
 }
