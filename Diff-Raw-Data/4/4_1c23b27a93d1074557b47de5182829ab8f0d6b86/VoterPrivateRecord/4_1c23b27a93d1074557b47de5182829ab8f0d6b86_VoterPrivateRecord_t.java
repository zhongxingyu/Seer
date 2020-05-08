 package net.buhacoff.netvote.model;
 
import org.codehaus.jackson.annotate.JsonIgnore;

 /**
  *
  * @author jbuhacoff
  */
 public class VoterPrivateRecord extends Voter 
 {
 	public String ssn;
     public String firstName;
 	public String lastName;
     public String address;
     
    @JsonIgnore
     public Voter getPublic() {
         Voter voter = new Voter();
         voter.registrationAuthorityId = this.registrationAuthorityId;
         voter.voterId = this.voterId;
         return voter;
     }
 }
