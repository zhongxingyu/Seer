 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package sma;
 
 import java.lang.*;
 import java.io.*;
 import java.sql.SQLException;
 import java.sql.ResultSet;
 import jade.util.leap.ArrayList;
 import jade.util.leap.List;
 import jade.core.*;
 import jade.core.behaviours.*;
 import jade.domain.*;
 import jade.domain.FIPAAgentManagement.*;
 import jade.domain.FIPANames.InteractionProtocol;
 import jade.lang.acl.*;
 import jade.content.*;
 import jade.content.onto.*;
 import jade.proto.AchieveREInitiator;
 import jade.proto.AchieveREResponder;
 import sma.ontology.*;
 import java.util.*;
 /**
  *
  * @author joan
  */
 public class BoatCoordinator extends Agent{
     
    private AID centralAgent;
     
     private jade.util.leap.List listOfBoats;
     
     public BoatCoordinator(){
         super();
     }
     
     
       /**
    * A message is shown in the log area of the GUI
    * @param str String to show
    */
     private void showMessage(String str) {
         System.out.println(getLocalName() + ": " + str);
     }
     
     protected void setup(){
         //Accept jave objects as messages
         this.setEnabledO2ACommunication(true, 0);
         
         showMessage("Agent (" + getLocalName() + ") .... [OK]");
         
         // Register the agent to the DF
         ServiceDescription sd1 = new ServiceDescription();
         sd1.setType(UtilsAgents.BOAT_COORDINATOR);
         sd1.setName(getLocalName());
         sd1.setOwnership(UtilsAgents.OWNER);
         DFAgentDescription dfd = new DFAgentDescription();
         dfd.addServices(sd1);
         dfd.setName(getAID());
         try {
             DFService.register(this, dfd);
             showMessage("Registered to the DF");
         }catch (FIPAException e) {
             System.err.println(getLocalName() + " registration with DF " + "unsucceeded. Reason: " + e.getMessage());
             doDelete();
         }
         
         //Search for the CentralAgent
         ServiceDescription searchBoatCoordCriteria = new ServiceDescription();
        searchBoatCoordCriteria.setType(UtilsAgents.CENTRAL_AGENT);
        this.centralAgent = UtilsAgents.searchAgent(this, searchBoatCoordCriteria);
     }
     
     private jade.util.leap.List buscarAgents(String type,String name) { 
         jade.util.leap.List results = new jade.util.leap.ArrayList();
         DFAgentDescription dfd = new DFAgentDescription();
         ServiceDescription sd = new ServiceDescription();
         if(type!=null) sd.setType(type);
         if(name!=null) sd.setName(name);
         dfd.addServices(sd);
         try {
             SearchConstraints c = new SearchConstraints(); c.setMaxResults(new Long(-1));
             DFAgentDescription[] DFAgents = DFService.search(this,dfd,c); int i=0;
             while ((DFAgents != null) && (i<DFAgents.length)) {
                 DFAgentDescription agent = DFAgents[i];
                 i++;
                 Iterator services = agent.getAllServices();
                 boolean found = false;
                 ServiceDescription service = null;
                 while (services.hasNext() && !found) {
                     service = (ServiceDescription)services.next();
                     found = (service.getType().equals(type) || service.getName().equals(name));
                 }
                 if (found) {
                       results.add((AID)agent.getName());
                       //System.out.println(agent.getName()+"\n");
                 }
             }
         } catch (FIPAException e) {
             System.out.println("ERROR: "+e.toString());
         }
         return results;
     }
 }
