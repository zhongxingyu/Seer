 package net.buhacoff.netvote.model;
 
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
     
     public Voter getPublic() {
         Voter voter = new Voter();
         voter.registrationAuthorityId = this.registrationAuthorityId;
         voter.voterId = this.voterId;
         return voter;
     }
 }
