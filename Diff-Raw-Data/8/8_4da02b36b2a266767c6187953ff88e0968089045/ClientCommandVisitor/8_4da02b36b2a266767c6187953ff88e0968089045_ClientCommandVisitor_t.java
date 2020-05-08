 package risk.protocol;
 
 import java.util.Collection;
 
 import risk.common.Logger;
 import risk.game.Attack;
 import risk.game.Country;
 import risk.game.CountryPair;
 import risk.game.GameController;
 import risk.game.GameView;
 import risk.game.MessageListener;
 import risk.game.Player;
 import risk.game.RoundPhase;
 import risk.game.server.ClientHandler;
 import risk.protocol.command.*;
 
 public class ClientCommandVisitor implements CommandVisitor {
     private GameView gameView;
     private GameController gameCtrl;
 
     public ClientCommandVisitor(GameView gameView, GameController gameCtrl) {
         super();
         this.gameView = gameView;
         this.gameCtrl = gameCtrl;
     }
     
     private void broadcastMessage(String msg, boolean popup) {
         Collection<MessageListener> listeners = gameView.getMessageListeners();
         for (MessageListener ml : listeners) {
             ml.onNewMessage(msg, popup);
         }
     }
     
     private void broadcastMessage(String msg) {
         broadcastMessage(msg, false);
     }
 
     @Override
     public void visit(HelloCmd cmd) {
         Logger.logwarn("Client shouldn't receive HelloCmd");
     }
 
     @Override
     public void visit(PlayerJoinedCmd cmd) {
         Logger.logdebug("Got PlayerJoinedCmd: " + cmd.getPlayer().getName() + (cmd.isControlledByMe() ? " It's me!" : ""));
         gameCtrl.addPlayer(cmd.getPlayer());
         if (cmd.isControlledByMe()) {
             gameCtrl.setMyPlayer(cmd.getPlayer());
         }
         broadcastMessage("Player \"" + cmd.getPlayer().getName() + "\" has joined the game.");
     }
 
     @Override
     public void visit(GameStartedCmd cmd) {
         Logger.logdebug("Game started");
         gameCtrl.setGameStarted(true);
        broadcastMessage("Game started");
     }
 
     @Override
     public void visit(NextRoundCmd cmd) {
         Logger.logdebug("New round started");
         gameCtrl.setRoundPlayers(cmd.getPlayers());
         gameCtrl.setRoundPhases(cmd.getRoundPhases());
         broadcastMessage("New round");
     }
     
     @Override
     public void visit(NextPhaseCmd cmd) {
         if (!gameCtrl.swicthToNextPhase()) {
             // TODO: error handling
             Logger.logerror("Cannot switch phase!");
             return;
         }
         RoundPhase phase = gameView.getRoundPhase();
         Logger.logdebug("Switched to phase: " + phase.toString());
         if (phase == RoundPhase.REINFORCEMENT) {
             gameCtrl.setAvailableReinforcement(cmd.getReinforcement());
             Logger.logdebug("Got " + cmd.getReinforcement() + "reinforcement");
         }
         broadcastMessage("Next phase: " + phase);
     }
 
     @Override
     public void visit(CountyInitCmd cmd) {
         Logger.logdebug("Got CountryInitCmd: " + cmd.getCountry().getName() + " to " + cmd.getCountry().getOwner().getName());
         Country c = cmd.getCountry();
         gameCtrl.initCountry(c.getName(), c.getOwner().getName(), c.getTroops());
     }
 
     @Override
     public void visit(GameEndedCmd cmd) {
         String reason = cmd.getReason() == GameEndedCmd.WIN ? "won" : "quit";
         String player = cmd.getPlayer() == null ? "" : cmd.getPlayer().getName();
         Logger.logdebug("Got GameEnded command: " + player + " has " + reason);
        broadcastMessage("The game has ended, player " + player + " has " + reason, true);
     }
 
     @Override
     public void visit(PlaceReinforcementCmd cmd) {
         Country c = gameView.getCountry(cmd.getCountry().getName());
         Logger.logdebug("Player " + cmd.getPlayer().getName() + " placed " + cmd.getTroops() + " units to country " + cmd.getCountry().getName());
         gameCtrl.setAvailableReinforcement(gameView.getAvailableReinforcement() - cmd.getTroops());
         gameCtrl.addTroopsToCountry(c, cmd.getTroops());
         broadcastMessage("Player " + cmd.getPlayer().getName() + " placed " + cmd.getTroops() + " units to " + cmd.getCountry().getName());
     }
     
     private void WrongCommand(Command cmd) {
         Logger.logwarn("Clienet shouldn't receive " + cmd.toString());
     }
 
     @Override
     public void visit(NextPlayerCmd nextPlayerCmd) {
         Logger.logdebug("Got NextPlayerCmd!");
         if (!gameCtrl.switchToNextPlayer()) {
             Logger.logerror("No more player in NextPlayerCmd handler!");
         }
         Logger.logdebug("Current player is: " + gameView.getCurrentPlayer().getName());
         gameCtrl.resetPhases();
         broadcastMessage("Current player is " + gameView.getCurrentPlayer().getName());
     }
 
     @Override
     public void visit(RegroupCmd cmd) {
         Country from = gameView.getCountry(cmd.getCountryPair().From.getName());
         Country to = gameView.getCountry(cmd.getCountryPair().To.getName());
         Logger.logdebug("Got regroup command " + from.getName() + " -> " + to.getName() + " : " + cmd.getTroops());
         CountryPair cp = new CountryPair(from, to);
         gameCtrl.regroup(cp, cmd.getTroops());
         String playerName = from.getOwner().getName();
        broadcastMessage("Player " + playerName + " moved " + cmd.getTroops() + " units from " + from.getName() + " to " + to.getName());
     }
 
     @Override
     public void visit(AttackStartCmd cmd) {
         Country from = gameView.getCountry(cmd.getCountryPair().From.getName());
         Country to = gameView.getCountry(cmd.getCountryPair().To.getName());
         Logger.logdebug("Got AttackStartCmd " + from.getName() + " -> " + to.getName());
         gameCtrl.setAttack(new Attack(new CountryPair(from, to)));
     }
 
     @Override
     public void visit(AttackSetADiceCmd cmd) {
         int dice = cmd.getADice();
         Logger.logdebug("Got attackSetADiceCmd: " + dice);
         gameCtrl.setAttackADice(dice);
     }
 
     @Override
     public void visit(AttackSetDDiceCmd cmd) {
         int dice = cmd.getDDice();
         Logger.logdebug("Got attackSetDDiceCmd: " + dice);
         gameCtrl.setAttackDDice(dice);        
     }
 
     @Override
     public void visit(AttackRoundResultCmd cmd) {
         Logger.logdebug("Got AttackResultCmd");
         gameCtrl.setAttackRoundResults(cmd.getADiceResults(), cmd.getDDiceResults());
     }
 
     @Override
     public void visit(AttackRetreatCmd cmd) {
         Logger.logdebug("Got AttackretreatCmd");
         gameCtrl.clearAttack();
     }
 
     @Override
     public void visit(EndTurnCmd cmd) {
         WrongCommand(cmd);
     }
 
     @Override
     public void visit(ErrorCmd cmd) {
         String error = "";
         switch (cmd.getErrorCode()) {
         case ErrorCmd.ILLEGAL_ARGUMENT:
             error = "Illegal argument";
             break;
         case ErrorCmd.INVALID_PHASE:
             error = "Invalid phase";
             break;
         case ErrorCmd.INVALID_ATTACK_PHASE:
             error = "Invalid attack phase";
             break;
         case ErrorCmd.NAME_ALREADY_USED:
             error = "Name is already in use";
             break;
         default:
             error = "Unknown";
             break;
         }
         
         Logger.logerror("Server error: " + error);
         broadcastMessage("Server rejected last command: " + error);
     }
 
     @Override
     public void visit(StartGameCmd cmd) {
         WrongCommand(cmd);
     }
 }
