 package net.mademocratie.gae.server.json;
 
 import com.google.inject.Inject;
 import net.mademocratie.gae.server.json.entities.GetContributionsResult;
 import net.mademocratie.gae.server.json.entities.ProposalInformations;
 import net.mademocratie.gae.server.entities.dto.ProposalDTO;
 import net.mademocratie.gae.server.entities.v1.*;
 import net.mademocratie.gae.server.exception.AnonymousCantVoteException;
 import net.mademocratie.gae.server.exception.MaDemocratieException;
 import net.mademocratie.gae.server.json.entities.ProposalInput;
 import net.mademocratie.gae.server.services.*;
 
 import javax.ws.rs.*;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.HttpHeaders;
 import javax.ws.rs.core.MediaType;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 @Path("/proposal")
 @Produces(MediaType.APPLICATION_JSON)
 public class ProposalService extends AbstractMaDemocratieJsonService implements IProposalService {
     Logger log = Logger.getLogger(ProposalService.class.getName());
 
     @Inject
     IManageProposal manageProposals;
 
     @Inject
     IManageVote manageVote;
 
     @Inject
     IManageComment manageComment;
 
     @GET
     @Path("/last")
     public String getProposals() {
         List<ProposalDTO> lastProposals = manageMD.latestProposalsDTO(100);
         String proposalsTitle = lastProposals.size() + " last proposals";
         GetContributionsResult result = new GetContributionsResult(
                 new ArrayList<ProposalDTO>(lastProposals),
                 proposalsTitle
         );
         return result.toJSON().toString();
     }
 
     @POST
     @Path("/add")
     public String addProposal(ProposalInput proposal, @Context HttpHeaders httpHeaders) {
         if (proposal == null) return null;
         Citizen authenticatedUser = getAuthenticatedCitizen(httpHeaders);
         Proposal newProposal = new Proposal(proposal.getTitle(), proposal.getContent());
         if (authenticatedUser != null) {
             newProposal.setAuthorFromValue(authenticatedUser);
         }
         log.info("addProposal POST received : " + proposal.toString());
         Proposal addedProposal = manageProposals.addProposal(newProposal);
         log.info("addProposal POST received ; result=" + addedProposal.toString());
         return addedProposal.getContributionId().toString();
     }
 
     @POST
     @Path("/addcomment")
     public String addProposalComment(Comment inComment, @Context HttpHeaders httpHeaders) {
         if (inComment == null) return null;
         if (inComment.getParentContribution() == null) return null;
         if (inComment.getParentContribution() == null) return null;
         if (inComment.getContent() == null) return null;
         Citizen authenticatedUser = getAuthenticatedCitizen(httpHeaders);
         log.info("addComment POST received : " + inComment.toString());
         Comment addedComment = manageComment.comment(authenticatedUser, inComment);
         log.info("addComment POST received ; result=" + addedComment.toString());
         return addedComment.getContributionId().toString();
     }
 
 
     /*
      src: http://stackoverflow.com/questions/7430270/post-put-delete-http-json-with-additional-parameters-in-jersey-general-design
      */
     @GET
     @Path("/proposal/{id}")
     public String getProposal(@PathParam("id") String proposalId) {
         if (proposalId == null) return null;
         Long propId = Long.valueOf(proposalId);
         ProposalInformations proposalInformations = manageMD.getProposalInformations(propId);
         log.info("getProposalId " + propId + " : " + proposalInformations.toString());
         return proposalInformations.toJsonString();
     }
 
    private Vote voteProposal(String proposalId, HttpHeaders httpHeaders, VoteKind voteKind) throws AnonymousCantVoteException, MaDemocratieException {
         Citizen authenticatedUser = getAuthenticatedCitizen(httpHeaders);
         if (authenticatedUser == null) {
             // throw new AnonymousCantVoteException();
             log.info("vote "+ voteKind.toString()+" on proposal id " + proposalId + " *ignored* : anonymous can't vote");
            return null;
         }
        return manageVote.vote(authenticatedUser, Long.valueOf(proposalId), voteKind);
     }
 
     @GET
     @Path("/proposal/vote/pro/{id}")
    public Vote voteProposalPro(@PathParam("id") String proposalId, @Context HttpHeaders httpHeaders) throws AnonymousCantVoteException, MaDemocratieException {
        return voteProposal(proposalId, httpHeaders, VoteKind.PRO);
     }
 
     @GET
     @Path("/proposal/vote/con/{id}")
    public Vote voteProposalCon(@PathParam("id") String proposalId, @Context HttpHeaders httpHeaders) throws AnonymousCantVoteException, MaDemocratieException {
        return voteProposal(proposalId, httpHeaders, VoteKind.CON);
     }
 
     @GET
     @Path("/proposal/vote/neutral/{id}")
    public Vote voteProposalNeutral(@PathParam("id") String proposalId, @Context HttpHeaders httpHeaders) throws AnonymousCantVoteException, MaDemocratieException {
        return voteProposal(proposalId, httpHeaders, VoteKind.NEUTRAL);
     }
 }
