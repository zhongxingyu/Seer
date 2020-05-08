 package net.mademocratie.gae.server.json;
 
 import net.mademocratie.gae.server.entities.v1.Comment;
 import net.mademocratie.gae.server.entities.v1.Proposal;
 import net.mademocratie.gae.server.entities.v1.Vote;
 import net.mademocratie.gae.server.exception.AnonymousCantVoteException;
 import net.mademocratie.gae.server.exception.MaDemocratieException;
 import net.mademocratie.gae.server.json.entities.ProposalInput;
 
 import javax.ws.rs.*;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.HttpHeaders;
 import javax.ws.rs.core.MediaType;
 
 @Produces(MediaType.APPLICATION_JSON)
 public interface IProposalService {
     String getProposals();
     String addProposal(ProposalInput proposal, @Context HttpHeaders httpHeaders);
     String addProposalComment(Comment inComment, @Context HttpHeaders httpHeaders);
     String getProposal(@PathParam("id") String proposalId);
    void voteProposalPro(@PathParam("id") String proposalId, @Context HttpHeaders httpHeaders) throws AnonymousCantVoteException, MaDemocratieException;
    void voteProposalCon(@PathParam("id") String proposalId, @Context HttpHeaders httpHeaders) throws AnonymousCantVoteException, MaDemocratieException;
    void voteProposalNeutral(@PathParam("id") String proposalId, @Context HttpHeaders httpHeaders) throws AnonymousCantVoteException, MaDemocratieException;
 }
