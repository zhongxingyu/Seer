 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.scrumble.server.services;
 
 import com.scrumble.server.entities.Sprint;
 import com.scrumble.server.entities.Userstory;
 import com.scrumble.server.entities.Userstorysprint;
 import com.scrumble.server.sessionbeans.SprintFacadeLocal;
 import com.scrumble.server.sessionbeans.SprinttaskassignationFacade;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.UriInfo;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Response;
 
 /**
  * REST Web Service
  *
  * @author cyril
  */
 @Path("sprints")
 @Stateless
 public class SprintsResource {
 
     @Context
     private UriInfo context;
     
     @EJB
     private SprintFacadeLocal sprintBean;
     
     @EJB
     private SprinttaskassignationFacade sprinttaskassignationBean;
 
     /**
      * Creates a new instance of SprintsResource
      */
     public SprintsResource() {
     }
     
     /**
      * Retrieves representation of list of com.scrumble.server.entities.Sprint object
      * @return a JSON representation of the list of all sprint.
      */
     @GET
     @Path("all")
     @Produces("application/json")
     public List<Sprint> findAll() {
         return sprintBean.findAll();
     }
     
     /**
      * Retrieves representation of a single com.scrumble.server.entities.Sprint object
      * @param id the id of the Sprint object to retrieve
      * @return a JSON representation of the related Sprint object.
      */
     @GET
     @Path("{id}")
     @Produces("application/json")
     public Sprint getProject(@PathParam("id") String id) {
         return sprintBean.find(Integer.parseInt(id));
     }
     
     /**
      * POST method for creating an instance of Sprint object
      * @param sprint JSON representation for the Sprint object
      * @param idProject the id of the Project object related to the Userstory object
      * @return an HTTP response with content of the created resource.
      */
     @POST
     @Path("add/{idProject}")
     @Consumes("application/json")
     @Produces("application/json")
     public Response addSprint(Sprint sprint,@PathParam("idProject") String idProject) {
         sprintBean.create(sprint);
         sprintBean.add_updateSprintToProject(sprint, Integer.parseInt(idProject));
 
         
         Response reponse=Response.status(200).build();
         return reponse;
     }
     
     /**
      * PUT method for updating an instance of Sprint object
      * @param sprint JSON representation for the Sprint object
      * @param idProject the id of the Project object related to the Userstory object
      * @return an HTTP response with content of the created resource.
      */
     @PUT
     @Path("{idProject}")
     @Consumes("application/json")
     @Produces("application/json")
     public void updateSprint(Sprint sprint,@PathParam("idProject") String idProject) {
         sprintBean.edit(sprint);
         sprintBean.add_updateSprintToProject(sprint, Integer.parseInt(idProject));
     }
     
     /**
      * Removes a single com.scrumble.server.entities.Sprint object
      * @param id the id of the Sprint object to remove
      * @return nothing.
      */
     @DELETE
     @Path("{id}")
     @Produces("application/json")
     public Response removeSprint(@PathParam("id") String id) {
         if(sprintBean.find(Integer.parseInt(id))!=null)
             sprintBean.remove(sprintBean.find(Integer.parseInt(id)));
         
         Response reponse=Response.status(200).build();
         return reponse;
     }
 
     
     /**
      * Retrieves the list of a com.scrumble.server.entities.Userstory linked with the Sprint object
      * @param idSprint the id of the Sprint object to retrieve
      * @return a JSON representation of the list of all userstories.
      */
     @GET
     @Path("{idSprint}/userstories")
     @Produces("application/json")
     public List<Userstory> findAllSprintUserstories(@PathParam("idSprint") String idSprint) {
         List<Userstory> results = null;
         try {
             results = sprintBean.findAllSprintUserstories(Integer.parseInt(idSprint));
         }
         catch (Exception e){
             throw new RESTException(e.getMessage());
         }
         return results;
     }
     
     
     /**
      * Retrieves the list of a com.scrumble.server.entities.Sprint linked with the Project object
      * @param idProject the id of the Project object to retrieve
      * @return a JSON representation of the list of all sprints.
      */
     @GET
     @Path("{idProject}/projects")
     @Produces("application/json")
     public List<Sprint> findAllProjectUserstories(@PathParam("idProject") String idProject) {
         List<Sprint> results = null;
         try {
             results = sprintBean.findAllProjectSprints(Integer.parseInt(idProject));
         }
         catch (Exception e){
             System.out.println(e.getStackTrace());
         }
         return results;
     }
     
     /**
      * Retrieves the list of a com.scrumble.server.entities.Userstorysprints linked with the Sprint object
      * @param id the id of the Sprint object to retrieve
      * @return a JSON representation of the related Project object.
      */
     @GET
     @Path("{idSprint}/userstories/no")
     @Produces("application/json")
     public List<Userstory> findAllNotSprintUserstories(@PathParam("idSprint") String idSprint) {
         List<Userstory> results = null;
 
         try {
             results = sprintBean.findAllNotSprintUserstories(Integer.parseInt(idSprint));
         }
         catch (Exception e){
             System.out.println(e.getStackTrace());
         }
         return results;
     }
     
     /**
      * POST method for saving the list of userstories related to a sprint
      * @param idSprint the id of the Sprint object
      * @param userstories JSON representation for list of userstories id to save in the sprint
      */
     @POST
     @Path("save/{idSprint}")
     @Consumes("application/json")
     public void saveListUserstoriesToSprint(@PathParam("idSprint") String idSprint, String userstories) {
         
         try {
             List<Integer> listUserstories = new ArrayList<Integer>();
             System.out.println(userstories);
             if(!userstories.equals("empty"))
             {
                 String[] listToConvert = userstories.split(",");
                 for (String s : listToConvert) {
                     listUserstories.add(Integer.valueOf(s));
                 }
             }
             System.out.println(listUserstories);
             
             sprintBean.addListUserstoriesToSprint(listUserstories, Integer.parseInt(idSprint));
         }
         catch (Exception e){
             throw new RESTException(e.getMessage());
         }
         
     }
     
     
     /**
      * Retrieves informations to display the sprint burndown chart
      * @param idSprint the id of the Sprint object
      * @return the list of informations needed to display a Sprint Burndown Chart
      */
     @GET
     @Path("{idSprint}/burndown")
     @Produces("application/json")
     public String findSprintBurndownChartInformations(@PathParam("idSprint") String idSprint) {
         String results = "";
        try {
             results = sprintBean.findSprintBurndownChartInformations(Integer.parseInt(idSprint));
         }
        catch (Exception e){
             throw new RESTException(e.getMessage());
         }
         return results;
     }
     
     /**
      * POST method for updating a processStatus of Sprint object
      * @param id id of sprint
      * @param status status of sprint
      * @return an HTTP response with content of the created resource.
      */
     @POST
     @Path("{id}/{status}")
     @Consumes("application/json")
     @Produces("application/json")
     public void updateProcessStatusOfSprint(@PathParam("id") String id, @PathParam("status") String status) {
         sprintBean.updateProcessStatusOfSprint(Integer.parseInt(id), status);
     }
     
     
 }
