 package models;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import javax.persistence.Entity;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 
 import play.data.validation.Required;
 import play.db.jpa.Model;
 import api.entities.Jsonable;
 import api.entities.PollInstanceJSON;
 import api.entities.VoteJSON;
 import api.entities.VoteSummaryJSON;
 
 /**
  * Voting round model. Having this enables us to have several voting rounds of a 
  * question. The result returned to the clients should always be the latest.
  * @author OpenARS Server API team
  */
 @Entity
 public class PollInstance extends Model implements Comparable<PollInstance>, Jsonable {
 	private static final long serialVersionUID = -6371181092845400924L;
 	
 	/**
 	 * The votes that students have placed on this poll.
 	 */
     @OneToMany(mappedBy = "pollInstance")
     public List<Vote> votes;
 	/**
 	 * The date and time where this poll starts.
 	 */
     public Date startDateTime;
     /**
 	 * The date and time where this poll ends.
 	 */
     public Date endDateTime;
     /**
 	 * The Poll that this is an instance of.
 	 */
     @ManyToOne
     @Required
     public Poll poll;
 
     public PollInstance(Date startDateTime, Date endDateTime, Poll poll) {
         this.poll = poll;
         this.startDateTime = startDateTime;
         this.endDateTime = endDateTime;
     }
 
     public int compareTo(PollInstance other) {
         return this.endDateTime.compareTo(other.endDateTime);
     }
 
     @Override
     public boolean equals(Object other) {
         if (other == null || !(other instanceof PollInstance)) {
             return false;
         }
         PollInstance vr = (PollInstance) other;
         return this.endDateTime.equals(vr.endDateTime);
     }
     /**
      * This method closes a Poll Instance and change the end time to the time this function is invoked.
      */
     public void closePollInstance () {
     	this.endDateTime = new Date(System.currentTimeMillis());
     }
 
     /*
      * TODO: Consider if this is needed.
      */
     @Override
     public int hashCode() {
         int hash = 7;
         hash = 71 * hash + (this.votes != null ? this.votes.hashCode() : 0);
         hash = 71 * hash + (this.endDateTime != null ? this.endDateTime.hashCode() : 0);
         hash = 71 * hash + (this.poll != null ? this.poll.hashCode() : 0);
         return hash;
     }
 
     public PollInstanceJSON toJson() {
     	return this.toJson(this);
     }
 	public PollInstanceJSON toJson(PollInstance p) {
     	PollInstanceJSON result = new PollInstanceJSON();
     	result.id = p.id;
     	result.poll_id = p.poll.id;
     	result.startDateTime = p.startDateTime;
     	result.endDateTime = p.endDateTime;
    	result.votes = new LinkedList<VoteSummaryJSON>();
    	
     	// Create vote summaries for all votes.
     	Map<Vote, VoteSummaryJSON> votes = new HashMap<Vote, VoteSummaryJSON>();
     	for(Vote v: p.votes) {
     		VoteSummaryJSON summary = votes.get(v);
     		if(summary == null) {
     			summary = new VoteSummaryJSON();
     			summary.choice_id = v.choice.id;
     			summary.choice_text = v.choice.text;
     			summary.count = (long) 0;
     			// Add it to the result.
     			result.votes.add(summary);
     		}
     		summary.count++;
     	}
 		return result;
 	}
 	
 	public static PollInstance fromJson (PollInstanceJSON json) {
 		Poll poll = Poll.find("byID", json.poll_id).first();
 		PollInstance result = new PollInstance(json.startDateTime, json.endDateTime, poll);
 		
 		result.votes = new LinkedList<Vote>();
 
 		// Update the votes
 		/*
 		if(json.votes != null) {
 			for (VoteSummaryJSON c : json.votes) {
 				result.votes.add(Vote.fromJson(c));	
 			}
 		}
 		*/
 		// Update the references.
 		for (Vote c : result.votes) {
 			c.pollInstance = result;
 		}
 		return result;
     }
 }
