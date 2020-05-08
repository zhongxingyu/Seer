 
 package org.alt60m.ministry.servlet.modules.campus.movement;
 
 import java.util.Collection;
 import java.util.Vector;
 
 import org.alt60m.ministry.ActivityExistsException;
 import org.alt60m.ministry.Strategy;
 import org.alt60m.ministry.model.dbio.Activity;
 import org.alt60m.ministry.model.dbio.Contact;
 import org.alt60m.ministry.model.dbio.Person;
 import org.alt60m.ministry.model.dbio.TargetArea;
 import org.alt60m.ministry.servlet.InfoBaseTool;
 import org.alt60m.ministry.servlet.modules.InfoBaseModuleHelper;
 import org.alt60m.ministry.servlet.modules.campus.location.LocationHelper;
 import org.alt60m.ministry.servlet.modules.team.TeamHelper;
 import org.alt60m.servlet.ActionResults;
 import org.alt60m.servlet.Controller.ActionContext;
 import org.alt60m.util.ObjectHashUtil;
 
 public class MovementController extends org.alt60m.ministry.servlet.modules.InfoBaseModuleController {
 
     /**
 	 *
 	 */
 	private static final long serialVersionUID = 1L;
 
 	
 
     public MovementController() {
         log.debug("MovementController constructor");
     }
     private final String module="campus_movement";
 	private final String title="Campus";
     public void init() {
         log.debug("MovementController.init()");
         try {
             initState();
         } catch (Exception e) {
             log.fatal("Failed to init!", e);
         }
     }
     /** @param ctx ActionContext object Request parameters: <none> */
 	public void makeNewPerson(ActionContext ctx){
 		try{
 			ActionResults results = (ActionResults)ctx.getSessionValue("location_response");
         	results.putValue("add_person","true" );
 			results.putValue("teamID","");
 			results.putValue("activityid",ctx.getInputString("activityid"));
 			results.putValue("id",ctx.getInputString("id"));
 			results.putValue("purpose","contact");
 			ctx.setReturnValue(results);
 			ctx.goToView(results.getValue("view"));
 		
 		}
 		catch (Exception e) {
 		 ctx.setError();
 		 ctx.goToErrorView();
 		 log.error("Failed to perform makeNewPerson().", e);
 		}
 	}
  
     public void savePersonContact(ActionContext ctx) {
         try {
             String activityId = ctx.getInputString("activityid", true);
             String personID = ctx.getInputString("personID", true);
             MovementHelper ibt = new MovementHelper();
             ibt.savePersonContact(personID, activityId);
             content(ctx);
             
         }
         catch (Exception e) {
             ctx.setError();
             ctx.goToErrorView();
             log.error("Failed to perform savePersonContact().", e);
         }
     }
     public void removePersonContact(ActionContext ctx) {
         try {
             String activityId = ctx.getInputString("activityid", true);
             String personID = ctx.getInputString("personID");
             MovementHelper ibt = new MovementHelper();
 			ibt.removePersonContact( personID,activityId);
 			content(ctx);
         }
         catch (Exception e) {
             ctx.setError();
             ctx.goToErrorView();
             log.error("Failed to perform removePersonContact().", e);
         }
     }
     public void addPersonContact(ActionContext ctx) {
         try {
         	ActionResults results = new ActionResults("addPersonContact");
             InfoBaseModuleHelper ibt = new InfoBaseModuleHelper();
             String search = "";
             if(!ctx.getInputString("lastName").equals("")){
             	search = ctx.getInputString("lastName") + "%";
             }
            
             String activityId = ctx.getInputString("activityid", true);
             String targetAreaId = ctx.getInputString("targetareaid", true);
             Vector<Contact> contacts;
             contacts = ibt.listContactsByLastName(search.toUpperCase());
             results.addCollection("contacts", contacts);
             results.putValue("activityid", activityId);
             results.putValue("targetareaid", targetAreaId);
             results.addHashtable("search",LocationHelper.sessionSearch(ctx));
 			results.addCollection("content", LocationHelper.content(targetAreaId));
 			results.addHashtable("info", LocationHelper.info(targetAreaId));
 			Person person=getUserPerson(ctx);
             results.putValue("personID",person.getPersonID()+"");
             results.putValue("isRD",isRD(person));
 			results.putValue("module",this.module);
 			results.putValue("title",this.title);
 			results.putValue("mode","addContact");
 			ctx.setReturnValue(results);
 			ctx.goToView("index");
         }
         catch (Exception e) {
 			ctx.setError();
             ctx.goToErrorView();
             log.error("Failed to perform addPersonContact().", e);
         }
     }
     public void edit(ActionContext ctx) {
         try {
         	ActionResults results = (ActionResults)ctx.getSessionValue("location_response");
         	results.putValue("edit_movement","true" );
         	TargetArea ta=new TargetArea((String)results.getHashtable("info").get("targetareaid"));
        	 Boolean isStudentVenture=(((ctx.getInputString("strategy")+"").equals("SV"))&&(!ta.getType().equals("HighSchool")));
 			 if(isStudentVenture){results.putValue("isStudentVenture", "true");}
         	if(ctx.getInputString("new")!=null){
 				
 				results.putValue("strategy",ctx.getInputString("strategy"));
 				results.addHashtable("movement", MovementHelper.info("0"));
 				results.putValue("new_movement","true" ); 
 			}else {
 				results.addHashtable("movement", MovementHelper.info(ctx.getInputString("activityid")));
 				results.removeValue("new_movement");
 			}
 			
 			 Collection colLLs = ObjectHashUtil.list(MovementHelper.getLocalLevelTeamsByRegion(ta.getRegion()));
 	            results.addCollection("teams", colLLs);
 			ctx.setReturnValue(results);
 			ctx.goToView(results.getValue("view"));
         }
         catch (Exception e) {
 			ctx.setError();
             ctx.goToErrorView();
             log.error("Failed to perform editMovement().", e);
         }
     }
     public void saveEditActivity(ActionContext ctx) {
         try {
             String activityId = ctx.getInputString("activityid");
             
             String periodEnd = ctx.getInputString("datechanged", true);
 
             String Url = ctx.getInputString("url", true);
             String Facebook = ctx.getInputString("facebook", true);
             String strategy = ctx.getInputString("strategy", Strategy.strategiesArray());
             String sent_teamID = null;
             if (strategy.equals("SV"))sent_teamID = ctx.getInputString("sent_teamID");
             log.debug(strategy);
             log.debug(sent_teamID);
             log.debug(ctx.getInputString("sent_teamID"));
             String updateOption = ctx.getInputString("updateoption", true);
             if (activityId==null){
             	Activity act=new Activity();
             	act.setStrategy(strategy);
             	act.setTargetAreaId(ctx.getInputString("targetareaid"));
             	if(!act.select()){
             		act.setLocalLevelId(ctx.getInputString("teamid"));
             		act.persist();
             		}else{
             			log.debug("doubleClick");
             		}
             	
             	activityId=act.getActivityId();
             }
            MovementHelper.saveEditActivity(activityId, periodEnd, strategy, updateOption, ctx.getProfileID(), ctx.getInputString("teamid"), Url, Facebook,sent_teamID);
            content(ctx);
         }
         catch (ActivityExistsException aee) {
         	ctx.setError("Strategy is already active for this target area.");
             ctx.goToView("activityError");
         }catch (Exception e) {
             ctx.setError();
             ctx.goToErrorView();
             log.error("Failed to perform saveEditActivity().", e);
         }
     }
 
 }
