 package org.freecode.irc.votebot.modules.common;
 
 import org.freecode.irc.Notice;
 import org.freecode.irc.Privmsg;
 import org.freecode.irc.votebot.FreeVoteBot;
 import org.freecode.irc.votebot.NoticeFilter;
 import org.freecode.irc.votebot.api.CommandModule;
 import org.freecode.irc.votebot.dao.PollDAO;
 import org.freecode.irc.votebot.dao.VoteDAO;
 import org.freecode.irc.votebot.entity.Poll;
 import org.freecode.irc.votebot.entity.Vote;
 
 import java.sql.Date;
 import java.sql.SQLException;
 
 /**
  * Processes and validates voting commands sent by users
  *
  * @author Shivam Mistry
  */
 public class VoteModule extends CommandModule {
 
     private PollDAO pollDAO;
     private VoteDAO voteDAO;
 
     public void processMessage(Privmsg privmsg) {
         String message = privmsg.getMessage();
         if (message.startsWith("!v ") || message.startsWith("!vote ")) {
             final String msg = privmsg.getMessage().substring(privmsg.getMessage().indexOf(' ')).trim();
             System.out.println(msg);
             final String[] split = msg.split(" ", 2);
             if (split.length == 2) {
                 String ids = split[0];
                 String vote = split[1].toLowerCase();
                 if (!vote.equalsIgnoreCase("yes") && !vote.equalsIgnoreCase("no") && !vote.equalsIgnoreCase("abstain")) {
                     return;
                 }
                 final int nId;
                 if (vote.equalsIgnoreCase("yes")) {
                     nId = 0;
                 } else if (vote.equalsIgnoreCase("no")) {
                     nId = 1;
                 } else {
                     nId = 2;
                 }
                 if (!ids.matches("\\d+")) {
                     return;
                 }
                 final int id = Integer.parseInt(ids);
                 vote(nId, id, privmsg);
             } else if (split.length == 1) {
                 String id = split[0];
                 if (!id.matches("\\d+")) {
                     return;
                 }
                 try {
 
                     int pollId = Integer.parseInt(id);
                     Poll poll = pollDAO.getPoll(pollId);
 
                     if (poll != null) {
                         String expiry = FreeVoteBot.SDF.format(new Date(poll.getExpiry()));
                         String closed = poll.isClosed() ? "Closed" : "Open";
                         if (System.currentTimeMillis() >= poll.getExpiry()) {
                             closed = "Expired";
                         }
 
                         Vote[] votes = voteDAO.getVotesOnPoll(pollId);
                         int yes = 0, no = 0, abstain = 0;
                         for (Vote vote : votes) {
                             int answerIndex = vote.getAnswerIndex();
                             if (answerIndex == 0) {
                                 yes++;
                             } else if (answerIndex == 1) {
                                 no++;
                             } else if (answerIndex == 2) {
                                 abstain++;
                             }
                         }
 
                         boolean open = closed.equals("Open");
                         privmsg.send(poll.getQuestion() +
                                 " Options: " + poll.getOptions() + " Created by: " + poll.getCreator() +
                                 " Yes: " + yes + " No: " + no + " Abstain: " + abstain +
                                 " Status: \u00030" + (open ? "3" : "4") + closed + "\u0003" +
                                 (open ? " Ends: " : " Ended: ") + expiry);
 
                     }
                 } catch (SQLException e) {
                     privmsg.send(e.getMessage());
                 }
             }
 
         } else if (message.startsWith("!y ")) {
             String id = message.replace("!y", "").trim();
             if (id.matches("\\d+")) {
                 voteYes(Integer.parseInt(id), privmsg);
             }
         } else if (message.startsWith("!n ")) {
             String id = message.replace("!n", "").trim();
             if (id.matches("\\d+")) {
                 voteNo(Integer.parseInt(id), privmsg);
             }
         } else if (message.startsWith("!a ")) {
             String id = message.replace("!a", "").trim();
             if (id.matches("\\d+")) {
                 voteAbstain(Integer.parseInt(id), privmsg);
             }
         }
     }
 
     private void voteYes(final int pollId, final Privmsg privmsg) {
         vote(0, pollId, privmsg);
     }
 
     private void voteNo(final int pollId, final Privmsg privmsg) {
         vote(1, pollId, privmsg);
     }
 
     private void voteAbstain(final int pollId, final Privmsg privmsg) {
         vote(2, pollId, privmsg);
     }
 
     private void vote(final int answerIndex, final int pollId, final Privmsg privmsg) {
 
         privmsg.getIrcConnection().addListener(new NoticeFilter() {
             public boolean accept(Notice notice) {
                 if (notice.getNick().equals("ChanServ") && notice.getMessage().equals("Permission denied.")) {
                     notice.getIrcConnection().removeListener(this);
                     return false;
                 }
                 return notice.getNick().equals("ChanServ") && notice.getMessage().contains("Main nick:") && notice.getMessage().contains(privmsg.getNick());
             }
 
             public void run(Notice notice) {
                 try {
                     String mainNick = notice.getMessage().substring(notice.getMessage().indexOf("Main nick:") + 10).trim();
                     System.out.println(mainNick);
 
                     Poll poll = pollDAO.getPoll(pollId);
                     if (poll != null) {
                         long time = poll.getExpiry();
                         if (System.currentTimeMillis() < time && !poll.isClosed()) {
                             Vote vote = voteDAO.getUsersVoteOnPoll(mainNick, pollId);
                             if (vote != null) {
                                 if (vote.getAnswerIndex() == answerIndex) {
                                     privmsg.getIrcConnection().send(new Notice(privmsg.getNick(), "You've already voted with this option!", privmsg.getIrcConnection()));
                                 } else {
                                     vote.setAnswerIndex(answerIndex);
                                     voteDAO.updateUsersVote(vote);
                                     privmsg.getIrcConnection().send(new Notice(privmsg.getNick(), "Vote updated.", privmsg.getIrcConnection()));
                                 }
                             } else {
                                 voteDAO.addUsersVote(mainNick, pollId, answerIndex);
                                 privmsg.getIrcConnection().send(new Notice(privmsg.getNick(), "Vote cast.", privmsg.getIrcConnection()));
                             }
                         } else {
                             privmsg.getIrcConnection().send(new Notice(privmsg.getNick(), "Voting is closed for this poll.", privmsg.getIrcConnection()));
                         }
                     }
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
                 privmsg.getIrcConnection().removeListener(this);
             }
         });
 
         FreeVoteBot.askChanServForUserCreds(privmsg);
     }
 
 
     public String getName() {
        return "(vote|v)";
     }
 
     public void setPollDAO(PollDAO pollDAO) {
         this.pollDAO = pollDAO;
     }
 
     public void setVoteDAO(VoteDAO voteDAO) {
         this.voteDAO = voteDAO;
     }
 }
