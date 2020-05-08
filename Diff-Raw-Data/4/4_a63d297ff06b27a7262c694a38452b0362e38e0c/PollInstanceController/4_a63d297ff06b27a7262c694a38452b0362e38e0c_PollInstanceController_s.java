 package controllers;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.List;
 import java.util.Random;
 
 import controllers.APIController.NotFoundException;
 import controllers.APIController.UnauthorizedException;
 
 import models.Poll;
 import models.PollInstance;
 import models.User;
 import models.Vote;
 import api.requests.CreatePollInstanceRequest;
 import api.responses.CreatePollInstanceResponse;
 import api.responses.CreatePollResponse;
 import api.responses.EmptyResponse;
 import api.responses.ReadPollInstanceResponse;
 import api.responses.UpdatePollInstanceResponse;
 import api.entities.PollInstanceJSON;
 import api.entities.PollJSON;
 import api.helpers.GsonHelper;
 
 /**
  * Class that manages the responses in the API for PollsInstances.
  * @author OpenARMS Service Team
  *
  */
 public class PollInstanceController extends APIController  {
 	
 	/**
 	 * Method that saves a new PollInstance in the DataBase.
 	 */
 	public static void create() {
         try {
 	    	//Takes the PollInstanceJSON and creates a new PollInstance object with this PollInstanceJSON.
 	        CreatePollInstanceRequest req = GsonHelper.fromJson(request.body, CreatePollInstanceRequest.class);
 	        PollInstance pollinstance = PollInstance.fromJson(req.pollInstance);
 	        Poll p = Poll.find("byId", req.pollInstance.poll_id).first();
 	        if(p == null) {
 		        throw new NotFoundException("The poll_id references a non-exsistant poll.");
 	        }
 	        //If current user is not the same as the poll creator or there is no current user, throws an exception
 			User u = AuthBackend.getCurrentUser();
 			System.out.println("Logged in with this user: "+u.email+" ("+u.id+")");
 			// TODO: Check the null values along the way
 			if (u == null || !p.admin.equals(u)) {
 		        throw new UnauthorizedException();
 		    }
 			
 			pollinstance.poll = p;
 	        pollinstance.save();
 	        
 	        //Creates the PollInstanceJSON Response.
 	        CreatePollInstanceResponse r = new CreatePollInstanceResponse(pollinstance.toJson());
 	    	String jsonresponse = GsonHelper.toJson(r);
 	    	renderJSON(jsonresponse);
         	
 		} catch (Exception e) {
 			renderException(e);
 		}
 	}
 
 	/**
 	 * Method that gets a PollInstance from the DataBase.
 	 */
 	public static void retrieve () {
 		try {
 			String pollinstanceid = params.get("id");
 	
 			//Takes the PollInstance from the DataBase.
 			PollInstance pollinstance = PollInstance.find("byID", pollinstanceid).first();
 	
 			if (pollinstance == null) {
 				throw new NotFoundException();
 			}
 			
 			//Creates the PollInstanceJSON Response.
 			ReadPollInstanceResponse r = new ReadPollInstanceResponse(pollinstance.toJson());
 			String jsonresponse = GsonHelper.toJson(r);
 	
 			renderJSON(jsonresponse);
 			
 		} catch (Exception e) {
 			renderException(e);
 		}
 	}
 	/**
 	 * Method that gets a PollInstance by a token from the DataBase.
 	 */
 	public static void retrieveByToken () {
 		try {
 			String pollinstancetoken = params.get("token");
 	
 			PollInstance pollinstance = null;
 			//Takes the PollInstance from the DataBase.
 			List<PollInstance> pollinstances = PollInstance.find("byPoll.token", pollinstancetoken).fetch();
 			Date lastdate = new Date(0);
 			
 			// FIXME: This should be rewritten to do "orderby" in the database, instead of looping through stuff here
 			for(PollInstance pi: pollinstances ) {
 				if (lastdate.before(pi.endDateTime)) {
 					lastdate = pi.endDateTime;
 					pollinstance = pi;
 				}
 			}
 			
 			if (pollinstance == null) {
 				throw new NotFoundException();
 			}
 			
 			//Creates the PollInstanceJSON Response.
 			ReadPollInstanceResponse r = new ReadPollInstanceResponse(pollinstance.toJson());
 			String jsonresponse = GsonHelper.toJson(r);
 	
 			renderJSON(jsonresponse);
 			
 		} catch (Exception e) {
 			renderException(e);
 		}
 	}
 	
 	/**
 	 * Method that edits a PollInstance already existing in the DataBase.
 	 */
 	public static void edit () {
 		try {
 			String pollinstanceid = params.get("id");
 	
 			//Takes the PollInstance from the DataBase.
 			PollInstance originalpollinstance = PollInstance.find("byID", pollinstanceid).first();
 			
 			if (originalpollinstance == null) {
 				throw new NotFoundException();
 			}
 			
 	        //If current user is not the same as the poll creator or there is no current user, throws an exception
 			User u = AuthBackend.getCurrentUser();
 			if (u == null || originalpollinstance.poll.admin.id != u.id) {
 		        throw new UnauthorizedException();
 		    }
 
 			//Takes the edited PollInstanceJSON and creates a new PollInstance object with this PollInstanceJSON.
			CreatePollInstanceRequest req = GsonHelper.fromJson(request.body, CreatePollInstanceRequest.class);
             PollInstance editedpollinstance = PollInstance.fromJson(req.pollInstance);
             
             //Changes the old fields for the new ones.
             if (editedpollinstance.startDateTime != null) {
             	originalpollinstance.startDateTime = editedpollinstance.startDateTime;
             }
             if (editedpollinstance.endDateTime != null) {
             	originalpollinstance.endDateTime = editedpollinstance.endDateTime;
             }
             if (editedpollinstance.poll != null) {
             	originalpollinstance.poll = editedpollinstance.poll;
             }
             if (editedpollinstance.votes != null) {
             	originalpollinstance.votes = editedpollinstance.votes;
             }
             
             originalpollinstance.save();
             
             //Creates the PollInstanceJSON Response.
             UpdatePollInstanceResponse r = new UpdatePollInstanceResponse(originalpollinstance.toJson());
         	String jsonresponse = GsonHelper.toJson(r);
         	renderJSON(jsonresponse);
             
 		} catch (Exception e) {
 			renderException(e);
 		}
 	}
 	
 	/**
 	 * Method that closes a PollInstance before the end time has run out.
 	 */
 	public static void close () {
 		try {
 			
 			String pollinstanceid = params.get("id");
 			
 			//Takes the PollInstance from the DataBase.
 			PollInstance pollinstance = PollInstance.find("byID", pollinstanceid).first();
 			
 			if (pollinstance == null) {
 				throw new NotFoundException();
 			}
 			
 	        //If current user is not the same as the poll creator or there is no current user, throws an exception
 			User u = AuthBackend.getCurrentUser();
 			if (u == null || pollinstance.poll.admin.id != u.id) {
 		        throw new UnauthorizedException();
 		    }
 			
 			//Closes the PollInstance and save the changes in the DataBase.
 			pollinstance.closePollInstance();
 			pollinstance.save();
 			
 			renderJSON(new EmptyResponse().toJson());
 		} catch (Exception e) {
 			renderException(e);
 		}
 	}
 
 	/**
 	 * Method that deletes a PollInstance existing in the DataBase.
 	 */
 	public static void delete () {
 		try {
 			String pollinstanceid = params.get("id");
 	
 			//Takes the PollInstance from the DataBase.
 			PollInstance pollinstance = PollInstance.find("byID", pollinstanceid).first();
 			
 			if (pollinstance == null) {
 				throw new NotFoundException();
 			}
 			
 	        //If current user is not the same as the poll creator or there is no current user, throws an exception
 			User u = AuthBackend.getCurrentUser();
 			if (u == null || pollinstance.poll.admin.id != u.id) {
 		        throw new UnauthorizedException();
 		    }
 			
 			//Deletes the PollInstance from the DataBase and creates an empty PollInstanceJSON for the response.
 			pollinstance.delete();
 
 			renderJSON(new EmptyResponse().toJson());
 		} catch (Exception e) {
 			renderException(e);
 		}
 	}
 	/**
 	 * Method that generates a summary of a PollInstance existing in the DataBase.
 	 */
 	public static void summary() {
 		try {
 			String pollinstanceid = params.get("id");
 	
 			//Takes the PollInstance from the DataBase.
 			PollInstance pollinstance = PollInstance.find("byID", pollinstanceid).first();
 			
 			if (pollinstance == null) {
 				throw new NotFoundException();
 			}
 			
 			//If current user is not the same as the poll creator or there is no current user, throws an exception
 			User u = AuthBackend.getCurrentUser();
 			if (u == null || pollinstance.poll.admin.id != u.id) {
 		        throw new UnauthorizedException();
 		    }
 			
 			List<Vote> votelist = Vote.find("byPollInstance", pollinstance).fetch();
 			Map<Long, Integer> counts = new HashMap<Long,Integer>();
 			
 			for(Vote v : votelist) {
 				if (counts.containsKey(v.choice.id))
 					counts.put(v.choice.id, counts.get(v.choice.id)+1);
 				else
 					counts.put(v.choice.id, 1);
 			}
 	        
 			System.out.println(counts.toString());
 			
 			// TODO: Use a new response class to return the summary
 			//Creates the PollInstanceJSON Response.
 			//CreatePollInstanceResponse r = new CreatePollInstanceResponse(pollinstance.toJson());
 			//String jsonresponse = GsonHelper.toJson(r);
 			//renderJSON(jsonresponse);
 			
 		} catch (Exception e) {
 			renderException(e);
 		}
 	}
 }
