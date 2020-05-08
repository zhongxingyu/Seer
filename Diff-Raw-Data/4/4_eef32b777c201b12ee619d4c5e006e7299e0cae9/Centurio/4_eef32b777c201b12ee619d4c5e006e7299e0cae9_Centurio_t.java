 package Roma.Cards;
 
 import Roma.*;
 
 import java.util.ArrayList;
 
 /**
  * File Name:
  * Creator: Varun Nayyar
  * Date: 11/04/12
  * Desc:
  */
 public class Centurio extends Card {
     public final static String NAME = "Centurio";
     final static String TYPE = Card.CHARACTER;
    final static String DESCRIPTION = "Attacks the card directly opposite, whether it is a character " +
             "or building card." +
             " The value of an unused action die can be added to the value of the battle die (the action die is " +
             "then counted as used)." +
             " This is decided after the battle die has been thrown.";
     final static int COST = 9;
     final static int DEFENCE = 5;
     final static boolean ACTIVATE_ENABLED = true;
 
     public final static int OCCURENCES = 2;
 
 
     public Centurio(PlayArea playArea) {
         super(NAME, TYPE, DESCRIPTION, COST, DEFENCE, playArea, ACTIVATE_ENABLED);
 
     }
 
     public boolean activate(Player player, int position) {
         DiceDiscs diceDiscs = playArea.getDiceDiscs();
         BattleManager battleManager = playArea.getBattleManager();
         DiceHolder diceHolder = playArea.getDiceHolder();
 
         boolean activated = true;
         int targetPlayer = (player.getPlayerID() + 1) % Roma.MAX_PLAYERS;
         boolean battleVictory = false;
         ArrayList<Dice> freeDice = player.getFreeDice();
         Dice chosenDie = null;
         Card targetCard = diceDiscs.getTargetCard(targetPlayer, position);
 
         if(targetCard == null){
             activated = false;
         } else {
             battleVictory = battleManager.battle(targetPlayer, position);
             if(!battleVictory){
                 if(freeDice.size() != 0){
                     chosenDie = player.chooseDie(freeDice);
                     if(chosenDie != null){
                        diceDiscs.addDiceToDisc(position, chosenDie);
                         if(targetCard.getDefense() <= chosenDie.getValue() + diceHolder.getBattleValue()[0]){
                             diceDiscs.discardTarget(targetPlayer, position);
                         }
                     }
                 }
             }
         }
 
         return activated;
     }
 }
