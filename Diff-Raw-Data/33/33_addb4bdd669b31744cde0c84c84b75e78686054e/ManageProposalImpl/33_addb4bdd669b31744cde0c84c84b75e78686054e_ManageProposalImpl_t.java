 package net.mademocratie.gae.server.services.impl;
 
 import net.mademocratie.gae.server.entities.Citizen;
 import net.mademocratie.gae.server.entities.Proposal;
 import net.mademocratie.gae.server.services.IManageProposal;
 
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Logger;
 
 import static com.googlecode.objectify.ObjectifyService.ofy;
 
 public class ManageProposalImpl implements IManageProposal {
     private final static Logger LOGGER = Logger.getLogger(ManageProposalImpl.class.getName());
 
     public Proposal addProposal(Proposal inputProposal, Citizen author) {
         if (author != null) {
             inputProposal.setAuthorEmail(author.getEmail());
             inputProposal.setAuthorPseudo(author.getPseudo());
         }
         inputProposal.setDate(new Date());
         ofy().save().entity(inputProposal).now();
         LOGGER.info("* Proposal ADDED : " + inputProposal);
         return inputProposal;
     }
 
     public List<Proposal> latest(int max) {
        List<Proposal> latestProposals = ofy().load().type(Proposal.class)
                .order("-date")
                .limit(max)
                .list();
         LOGGER.info("* latest proposals asked " + max + " result " + latestProposals.size());
         return latestProposals;
     }
 
     public void removeAll() {
         ofy().delete().type(Proposal.class);
     }
 
     public Proposal getById(Long proposalId) {
         return ofy().load().type(Proposal.class).id(proposalId).get();
     }
 }
