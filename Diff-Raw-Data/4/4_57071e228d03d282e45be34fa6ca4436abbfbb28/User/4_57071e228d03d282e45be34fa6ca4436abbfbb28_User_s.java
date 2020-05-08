 package models;
 
 
 import java.util.*;
 import com.mongodb.*;
 import play.data.validation.Constraints.*;
 
 import org.bson.types.ObjectId;
 import com.fasterxml.jackson.annotation.*;
 import org.jongo.*;
 import org.jongo.Jongo;
 import org.jongo.MongoCollection;
 
 import scala.math.Ordering;
 import uk.co.panaxiom.playjongo.PlayJongo;
 
 @JsonIgnoreProperties(ignoreUnknown = true)
 public class User {
 
     public static MongoCollection users() {
         return PlayJongo.getCollection("users");
     }
 
 
     @JsonProperty("_id")
     public ObjectId id;
 
     public User insert() {
         users().save(this);
         return this;
     }
 
     public void remove() {
         users().remove(this.id);
     }
 
     public static User findByName(String name) {
         return users().findOne("{username: #}", name).as(User.class);
     }
 
     public User(){}
 
     private String username;
     private String emailAddress;
     private String pictureURL;
     private List<String> groups;
     private List<Entity> watches;
     private List<Entity> votes;
     private String password;
     private boolean admin;
     private List<String> querySubscriptions;
     private List<String> initiativeSubscriptions;
     private List<String> milestoneSubscriptions;
     private List<String> riskSubscriptions;
     private int updateFrequency;
 
     /**
      * Returns whether the user is already subscribed to the entity or not.
      * 
      * @param user			user object
      * @param entityId		id of entity to look up
      * @param entityType	type of entity to look up
      * @return
      */
     public static boolean doesUserSubscribeToEntity(User user, String entityId, String entityType){
 		boolean retVal = false;
     	
         /*if(entityType=="initiative"){
         	if( user.initiativeSubscriptions.findOne("{entityId: #}", entityId) ){
         		retVal = true;
         	}
         } else if(entityType=="milestone"){
         	if( user.milestoneSubscriptions.findOne("{entityId: #}", entityId) ){
         		retVal = true;
         	}
         } else {
         	if( user.riskSubscriptions.findOne("{entityId: #}", entityId) ){
         		retVal = true;
         	}
         }*/
 		
     	return retVal;
     }
     
     /**
      * Subscribe to or unsubscribe from an entity. 
      * 
      * @param status		whether you would like to subscribe (true) or unsubscribe (false)
      * @param user			user object
      * @param entityId		id of entity
      * @param entityType	entity's type
      * @return				the new status of the subscription
      */
     public static boolean setUserEntitySubscriptionStatus(boolean status, User user, String entityId, String entityType){
 		boolean retVal = false;
     	
 		// TODO : make this work
 		if( status = true ){
 	        /*if(entityType=="initiative"){
 	        	if( user.initiativeSubscriptions.add("{entityId: #}", entityId) ){
 	        		retVal = true;
 	        	}
 	        } else if(entityType=="milestone"){
 	        	if( user.milestoneSubscriptions.add("{entityId: #}", entityId) ){
 	        		retVal = true;
 	        	}
 	        } else {
 	        	if( user.riskSubscriptions.add("{entityId: #}", entityId) ){
 	        		retVal = true;
 	        	}
 	        }*/
 		}
 		else {
 	        /*if(entityType=="initiative"){
         	if( user.initiativeSubscriptions.remove("{entityId: #}", entityId) ){
         		retVal = true;
         	}
 	        } else if(entityType=="milestone"){
 	        	if( user.milestoneSubscriptions.remove("{entityId: #}", entityId) ){
 	        		retVal = true;
 	        	}
 	        } else {
 	        	if( user.riskSubscriptions.remove("{entityId: #}", entityId) ){
 	        		retVal = true;
 	        	}
 	        }*/			
 		}
 		
     	return retVal;
     }
 
    public int getUpdateFrequency(){
        return this.updateFrequency;
    }*/

     public String getUsername() {
         return username;
     }
 
     public void setUsername(String username) {
         this.username = username;
     }
 
     public String getEmailAddress() {
         return emailAddress;
     }
 
     public void setEmailAddress(String emailAddress) {
         this.emailAddress = emailAddress;
     }
 
     public String getPictureURL() {
         return pictureURL;
     }
 
     public void setPictureURL(String pictureURL) {
         this.pictureURL = pictureURL;
     }
 
     public List<String> getGroups() {
         return groups;
     }
 
     public void setGroups(List<String> groups) {
         this.groups = groups;
     }
 
     public List<Entity> getWatches() {
         return watches;
     }
 
     public void setWatches(List<Entity> watches) {
         this.watches = watches;
     }
 
     public List<Entity> getVotes() {
         return votes;
     }
 
     public void setVotes(List<Entity> votes) {
         this.votes = votes;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public boolean isAdmin() {
         return admin;
     }
 
     public void setAdmin(boolean admin) {
         this.admin = admin;
     }
 
     public List<String> getQuerySubscriptions() {
         return querySubscriptions;
     }
 
     public void setQuerySubscriptions(List<String> querySubscriptions) {
         this.querySubscriptions = querySubscriptions;
     }
 
     public List<String> getInitiativeSubscriptions() {
         return initiativeSubscriptions;
     }
 
     public void setInitiativeSubscriptions(List<String> initiativeSubscriptions) {
         this.initiativeSubscriptions = initiativeSubscriptions;
     }
 
     public List<String> getMilestoneSubscriptions() {
         return milestoneSubscriptions;
     }
 
     public void setMilestoneSubscriptions(List<String> milestoneSubscriptions) {
         this.milestoneSubscriptions = milestoneSubscriptions;
     }
 
     public List<String> getRiskSubscriptions() {
         return riskSubscriptions;
     }
 
     public void setRiskSubscriptions(List<String> riskSubscriptions) {
         this.riskSubscriptions = riskSubscriptions;
     }
 
     public int getUpdateFrequency() {
         return updateFrequency;
     }
 
     public void setUpdateFrequency(int updateFrequency) {
         this.updateFrequency = updateFrequency;
     }
 
 }
 
