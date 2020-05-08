 package mel.fencing.server;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 public class Lobby
 {
     private UserSession openChallenger = null;
     private ArrayList<UserSession> unavailable = new ArrayList<UserSession>();
     private HashMap<UserSession,UserSession> challengers = new HashMap<UserSession,UserSession>();
     private HashMap<UserSession,UserSession> targets = new HashMap<UserSession,UserSession>();
     
     /*
      * OPCODES FOR LOBBY
      * E = Error
      * T = Targeted (you have been challenged)
      * A = Accept
      * C = Canceled by challenger
      * c = rejected by target
      */
     
     synchronized public void challengeOpen(UserSession challenger)
     {
         if(openChallenger == null)
         {
             openChallenger = challenger;
             challenger.send("Wan opponent");
         }
        else acceptChallenge(challenger, openChallenger);
     }
     
     synchronized public void challengeTarget(UserSession challenger, UserSession target)
     {
         if(target == null)
         {
             challenger.send("EOpponent is not logged in");
             return;
         }
         if(challengers.containsKey(challenger))
         {
             challenger.send("ECancel prior challenge first");
             return;
         }
         if(challenger == target)
         {
             challenger.send("ECannot challenge self.");
             return;
         }
         if(unavailable.contains(target))
         {
             challenger.send("E"+target.getName()+" is already in a game.");
             return;
         }
         challengers.put(challenger, target);
         targets.put(target, challenger);
         challenger.send("W"+target.getName());
         target.send("T"+challenger.getName());
     }
     
     synchronized public void cancel(UserSession out)
     {
         if(out == openChallenger) openChallenger = null;
         if(challengers.containsKey(out))
         {
             UserSession target = challengers.get(out);
             target.send("C"+out.getName());
             challengers.remove(out);
             targets.remove(target);
         }
         if(targets.containsKey(out)) rejectChallenge(out);
     }
     
     public void acceptChallenge(UserSession target)
     {
         UserSession challenger = targets.get(target);
         acceptChallenge(challenger, target);
     }
     
     public void acceptChallenge(UserSession challenger, UserSession target)
     {
         if(unavailable.contains(challenger))
         {
             target.send("E"+challenger.getName()+" is already in a game");
             return;
         }
         unavailable.add(challenger);
         unavailable.add(target);
         challengers.remove(challenger);
         targets.remove(target);
         Game.newGame(challenger,target);
     }
     
     public void rejectChallenge(UserSession target)
     {
         UserSession challenger = targets.get(target);
         challengers.remove(challenger);
         targets.remove(target);
         challenger.send("c"+target.getName());
     }
 }
