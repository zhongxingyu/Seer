 package theresistance.core.state;
 
 import java.util.Collection;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import theresistance.core.Game;
 import theresistance.core.Player;
 import theresistance.core.Proposal;
 import theresistance.core.Proposal.Vote;
 import theresistance.core.Round;
 
 /**
  * State where players vote on a proposal
  */
 public class VoteState extends GameState<VoteAction>
 {
 	private Proposal proposal;
 	private Map<Player, Vote> votes = new TreeMap<>();
 	private Set<String> leftToVote = new TreeSet<>();
 
 	public VoteState(Collection<Player> players, Proposal proposal)
 	{
 		this.proposal = proposal;
 
 		for (Player player : players)
 		{
 			votes.put(player, null);
 			leftToVote.add(player.getName());
 		}
 	}
 
 	@Override
 	public void act(VoteAction action)
 	{
 		votes.put(action.getPlayer(), action.getVote());
 		leftToVote.remove(action.getPlayer().getName());
 	}
 
 	@Override
 	public boolean isFinished()
 	{
 		return leftToVote.isEmpty();
 	}
 	
 	public Set<String> getPlayersLeftToVote()
 	{
 		return leftToVote;
 	}
 	
 	public Set<String> getProposal()
 	{
 		Set<String> players = new TreeSet<>();
 		for (Player player : proposal.getParticipants())
 		{
 			players.add(player.getName());
 		}
 		return players;
 	}
 
 	@Override
 	public void advance(Game game)
 	{
 		proposal.setVotes(votes);
 
 		if (proposal.isApproved())
 		{
			Round round = game.getCurrentRound();
 			round.setParticipants(proposal.getParticipants());
 			game.setState(new MissionState(proposal.getParticipants()));
 		}
 		else
 		{
 			game.gotoNextLeader();
			game.setState(new ProposeState(game.getCurrentLeader()));
 		}
 	}
 }
