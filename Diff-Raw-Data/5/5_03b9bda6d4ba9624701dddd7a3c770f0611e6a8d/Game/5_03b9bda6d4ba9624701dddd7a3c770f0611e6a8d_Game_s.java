 package com.yanchuanli.games.pokr.game;
 
 import com.google.code.tempusfugit.temporal.Duration;
 import com.yanchuanli.games.pokr.basic.Card;
 import com.yanchuanli.games.pokr.basic.Deck;
 import com.yanchuanli.games.pokr.basic.HandEvaluator;
 import com.yanchuanli.games.pokr.basic.PlayerRankComparator;
 import com.yanchuanli.games.pokr.dao.EventDao;
 import com.yanchuanli.games.pokr.dao.PlayerDao;
 import com.yanchuanli.games.pokr.dao.RoomDao;
 import com.yanchuanli.games.pokr.dto.PlayerDTO;
 import com.yanchuanli.games.pokr.game.workers.AddFriendRequestWorker;
 import com.yanchuanli.games.pokr.game.workers.ChatThreadWorker;
 import com.yanchuanli.games.pokr.game.workers.DealerSaysWorker;
 import com.yanchuanli.games.pokr.game.workers.VoiceChatThreadWorker;
 import com.yanchuanli.games.pokr.model.Action;
 import com.yanchuanli.games.pokr.model.Player;
 import com.yanchuanli.games.pokr.model.Pot;
 import com.yanchuanli.games.pokr.model.Record;
 import com.yanchuanli.games.pokr.util.Config;
 import com.yanchuanli.games.pokr.util.DTOUtil;
 import com.yanchuanli.games.pokr.util.NotificationCenter;
 import com.yanchuanli.games.pokr.util.Util;
 import org.apache.commons.lang3.exception.ExceptionUtils;
 import org.apache.log4j.Logger;
 
 import java.util.*;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 /**
  * Copyright Candou.com
  * Author: Yanchuan Li
  * Email: mail@yanchuanli.com
  * Date: 12-6-13
  */
 
 public class Game implements Runnable {
 
     private GameConfig gc;
 
     private static Logger log = Logger.getLogger(Game.class);
 
     //还活在游戏中的用户
     private List<Player> activePlayers;
     //进入房间了站着的用户
     private Map<String, Player> standingPlayers;
     //坐下但是等下局的用户
     private Map<String, Player> waitingPlayers;
 
     private Map<Integer, String> table;
 
 
     //本次游戏的所有用户
     private List<Player> allPlayersInGame;
     //本次游戏的所有赢钱用户
     private Map<String, Integer> allWinningUsers;
 
 
     private List<Card> cardsOnTable;
     private Deck deck;
     private int dealerPosition;
     private PlayerRankComparator comparator;
     private int bet;
     private int moneyOnTable;
     private Player actor;
 
     private int actorPosition;
     private boolean gaming = false;
     private boolean stop = false;
     private HandEvaluator handEval;
     private Pot pot;
     private Random random;
     private ExecutorService pool;
     private int playersToAct;
 
 
     public Game(GameConfig gc) {
         this.gc = gc;
         activePlayers = new CopyOnWriteArrayList<>();
         allPlayersInGame = new CopyOnWriteArrayList<>();
         allWinningUsers = new ConcurrentHashMap<>();
         waitingPlayers = new ConcurrentHashMap<>();
         standingPlayers = new ConcurrentHashMap<>();
         table = new ConcurrentHashMap<>();
         for (int i = 1; i <= gc.getMaxPlayersCount(); i++) {
             table.put(i, Config.EMPTY_SEAT);
         }
         cardsOnTable = new ArrayList<>();
         pot = new Pot();
         deck = new Deck();
         comparator = new PlayerRankComparator();
         handEval = new HandEvaluator();
         pot = new Pot();
         random = new Random();
         pool = Executors.newCachedThreadPool();
     }
 
     public void enterRoom(Player player) {
         player.setRoomId(gc.getId());
         PlayerDao.updateRoomId(player);
         standingPlayers.put(player.getUdid(), player);
 
         List<PlayerDTO> playerDTOs = new ArrayList<>();
         for (Player aplayer : activePlayers) {
             playerDTOs.add(new PlayerDTO(aplayer, Config.GAMESTATUS_ACTIVE));
         }
         for (String s : waitingPlayers.keySet()) {
             Player aplayer = waitingPlayers.get(s);
             playerDTOs.add(new PlayerDTO(aplayer, Config.GAMESTATUS_WAITING));
         }
 
 
         NotificationCenter.respondToPrepareToEnter(player.getSession(), DTOUtil.writeValue(playerDTOs));
 
 
         log.debug(gc.getId() + "房间内已坐下的玩家：" + DTOUtil.writeValue(playerDTOs));
 
         if (gaming) {
             log.debug("for NewComer:" + Util.cardsToString(cardsOnTable) + " bet:" + bet + " MoneyOnTable:" + moneyOnTable);
             NotificationCenter.notifyUserAboutGameStatus(player.getSession(), "1");
             NotificationCenter.dealCardsOnTableForNewcomers(player.getSession(), Util.cardsToGIndexes(cardsOnTable) + "," + bet + "," + moneyOnTable);
         } else {
             NotificationCenter.notifyUserAboutGameStatus(player.getSession(), "0");
         }
 
         allPlayersInGame.add(player);
     }
 
     public boolean buyIn(Player player, int amount) {
         boolean result = false;
         if (amount >= gc.getMinHolding() && amount <= gc.getMaxHolding()) {
             result = PlayerDao.buyIn(player, amount);
         }
         return result;
     }
 
     public synchronized void sitDown(Player player, int index) {
         log.debug(player.getName() + ":" + player.getMoneyInGame() + " tries to sit down at " + index + "...");
         boolean sitDownFailed = false;
 
 
         if (index >= 0 && index <= gc.getMaxPlayersCount()) {
             if (index != 0 && !table.get(index).equals(Config.EMPTY_SEAT)) {
                 log.debug("seat " + index + " is taken already");
                 sitDownFailed = true;
                 log.debug(table);
             } else {
                 log.debug(table);
             }
         } else {
             log.debug("wrong index:" + index);
             sitDownFailed = true;
         }
 
 
         if (gaming) {
             log.debug("gaming ...");
             if (activePlayers.size() < gc.getMaxPlayersCount()) {
                 //防止同一个人因黑客多次坐下
                 for (Player aplayer : activePlayers) {
                     if (aplayer.getUdid().equals(player.getUdid())) {
                         log.debug(player.getName() + " has already sitted down ...");
                         sitDownFailed = true;
                         break;
                     }
                 }
                 if (!sitDownFailed) {
                     if (player.getMoneyInGame() > 0) {
 
                     } else {
                         log.debug(player.getName() + " sitdown failed because of empty pocket ...");
                         sitDownFailed = true;
                     }
                 }
             } else {
                 sitDownFailed = true;
             }
 
         } else {
             int freeSitsCount = gc.getMaxPlayersCount() - activePlayers.size();
             if (freeSitsCount > 0) {
 
             } else {
                 log.debug(player.getName() + " sitdown failed because of no seats available ...");
                 sitDownFailed = true;
             }
 
         }
 
         if (!sitDownFailed) {
             standingPlayers.remove(player.getUdid());
             waitingPlayers.put(player.getUdid(), player);
 
             if (index == 0) {
                 int randomSeat = getNextRandomSeat();
                 player.setSeatIndex(randomSeat);
                 table.put(randomSeat, player.getUdid());
 
             } else {
                 player.setSeatIndex(index);
                 table.put(index, player.getUdid());
             }
 
             List<PlayerDTO> playerDTOs = new ArrayList<>();
             for (Player aplayer : activePlayers) {
                 playerDTOs.add(new PlayerDTO(aplayer, Config.GAMESTATUS_ACTIVE));
             }
             for (String s : waitingPlayers.keySet()) {
                 Player aplayer = waitingPlayers.get(s);
                 playerDTOs.add(new PlayerDTO(aplayer, Config.GAMESTATUS_WAITING));
             }
             RoomDao.updateCurrentPlayerCount(gc.getId(), activePlayers.size() + waitingPlayers.size());
             NotificationCenter.sitDownResult(player.getSession(), String.valueOf(Config.RESULT_SITDOWNSUCEESS));
             NotificationCenter.sayHello(allPlayersInGame, DTOUtil.writeValue(playerDTOs));
             log.debug(DTOUtil.writeValue(playerDTOs));
         } else {
             NotificationCenter.sitDownResult(player.getSession(), String.valueOf(Config.RESULT_SITDOWNFAILED));
         }
 
     }
 
 
     private void start() {
 
         reset();
         // notify every player in game about others
         sayHello();
 
 
         // rotate markCurrentDealer position
 
 
         if (activePlayers.size() > 1) {
             rotateDealer();
             deal2Cards();
             if (activePlayers.size() > 1) {
                 // deal 2 cards per player
                 doBettingRound(true);
                 // pre flop betting round
                 // deal 3 flp cards on the table
                 try {
                     if (activePlayers.size() > 1 && !stop) {
                         deal3FlipCards();
                         doBettingRound(false);
                         // flop the betting round
                         // deal the turn card (4th) on the table
                         if (activePlayers.size() > 1 && !stop) {
                             dealTurnCard();
                             doBettingRound(false);
                             if (activePlayers.size() > 1 && !stop) {
                                 dealRiverCard();
                                 doBettingRound(false);
                                 if (activePlayers.size() > 1 && !stop) {
                                     bet = 0;
                                     showdown();
                                 }
                             } else {
                                 showdown();
                             }
                         } else {
                             showdown();
                         }
                     } else {
                         showdown();
                     }
                 } catch (Exception e) {
                     showdown();
                 }
             }
 
         }
 
 
     }
 
     private void rotateDealer() {
         List<Player> playersInThisRound = getRestActivePlayers();
 
         dealerPosition = (dealerPosition + 1) % playersInThisRound.size();
         actorPosition = dealerPosition;
         Player dealer = playersInThisRound.get(actorPosition);
 
         NotificationCenter.markCurrentDealer(allPlayersInGame, dealer.getUdid());
         int smallBlindIndex = (actorPosition + 1) % playersInThisRound.size();
         int bigBlindIndex = (actorPosition + 2) % playersInThisRound.size();
         Player smallBlind = playersInThisRound.get(smallBlindIndex);
         smallBlind.setSmallBlind(true);
         Player bigBlind = playersInThisRound.get(bigBlindIndex);
         bigBlind.setBigBlind(true);
         NotificationCenter.markSmallBlind(allPlayersInGame, smallBlind.getUdid());
         NotificationCenter.markBigBlind(allPlayersInGame, bigBlind.getUdid());
 
 
         log.debug("[RotateDealer] current markCurrentDealer:" + dealerPosition);
         log.debug("current dealer:" + dealer.getName());
         log.debug("current smallblind:" + smallBlind.getName());
         log.debug("current bigblind:" + bigBlind.getName());
 
         rotateActor(playersInThisRound);
     }
 
     private void deal2Cards() {
         for (Player player : activePlayers) {
             for (int i = 0; i < 2; i++) {
                 Card card = deck.dealCard();
                 player.getHand().addCard(card);
                 log.debug(player.getHand().getGIndexes());
             }
 //            log.debug(player.getName() + " got " + player.getHand().toChineseString());
             NotificationCenter.deal2Cards(player.getSession(), player.getUdid() + "," + player.getName() + "," + player.getHand().getGIndexes());
         }
         NotificationCenter.deal2CardsOnAllDevices(allPlayersInGame, actor.getUdid());
     }
 
     private void deal3FlipCards() {
         for (int i = 0; i < 3; i++) {
             Card card = deck.dealCard();
             cardsOnTable.add(card);
         }
         log.debug("OnTable:" + Util.cardsToString(cardsOnTable) + " bet:" + bet + " MoneyOnTable:" + moneyOnTable);
         log.info(String.format("#已出第三张牌#当前桌上牌:%s 当前桌上注:%d 当前奖池的钱数:%d", Util.cardsToString(cardsOnTable), bet, moneyOnTable));
         NotificationCenter.deal3FlipCards(allPlayersInGame, Util.cardsToGIndexes(cardsOnTable) + "," + bet + "," + moneyOnTable);
     }
 
     private void dealTurnCard() {
         Card card = deck.dealCard();
         cardsOnTable.add(card);
         log.debug("OnTable-Turn:" + Util.cardsToString(cardsOnTable) + " bet:" + bet + " MoneyOnTable:" + moneyOnTable);
         log.info(String.format("#已出第四张牌#当前桌上牌:%s 当前桌上注:%d 当前奖池的钱数:%d", Util.cardsToString(cardsOnTable), bet, moneyOnTable));
         NotificationCenter.dealTurnCard(allPlayersInGame, Util.cardsToGIndexes(cardsOnTable) + "," + bet + "," + moneyOnTable);
     }
 
     private void dealRiverCard() {
         Card card = deck.dealCard();
         cardsOnTable.add(card);
         log.debug("OnTable-River:" + Util.cardsToString(cardsOnTable) + " bet:" + bet + " MoneyOnTable:" + moneyOnTable);
         log.info(String.format("#已出第五张牌#当前桌上牌:%s 当前桌上注:%d 当前奖池的钱数:%d", Util.cardsToString(cardsOnTable), bet, moneyOnTable));
         NotificationCenter.dealRiverCard(allPlayersInGame, Util.cardsToGIndexes(cardsOnTable) + "," + bet + "," + moneyOnTable);
     }
 
     private void showdown() {
 
         log.debug("showdown ...");
 
         NotificationCenter.showdown(allPlayersInGame);
         try {
             Thread.sleep(Duration.seconds(2).inMillis());
         } catch (InterruptedException e) {
             log.error(ExceptionUtils.getStackTrace(e));
         }
         pot.finish();
 
         log.debug("OnTable: " + Util.cardsToString(cardsOnTable));
 
         List<Player> results = new ArrayList<>();
 
         StringBuilder cardsInfo = new StringBuilder();
 
         for (Player player : activePlayers) {
             if (player.isOnline()) {
                 cardsInfo.append(player.getUdid()).append(",").append(player.getHand().getGIndexes()).append(";");
                 for (Card card : cardsOnTable) {
                     player.getHand().addCard(card);
                 }
                 results.add(player);
             }
         }
         log.debug("show2cards:" + cardsInfo.toString());
         NotificationCenter.show2cards(allPlayersInGame, cardsInfo.toString());
 
 
         if (results.size() > 1) {
             List<List<Player>> rankedPlayerList = GameUtil.rankPlayers(results);
             int[] cardsArray = new int[5];
             for (int i = 0; i < cardsOnTable.size(); i++) {
                 cardsArray[i] = cardsOnTable.get(i).getIndex();
             }
             for (int i = pot.potsCount() - 1; i >= 0; i--) {
                 StringBuilder sb = new StringBuilder();
                 Map<String, Integer> playersInThisPot = pot.getPotAtIndex(i);
                 List<Player> playersListInThisPot = new ArrayList<>();
                 boolean thisIsWinnerGroup = true;
                 for (List<Player> players : rankedPlayerList) {
                     for (Player player : players) {
                         if (playersInThisPot.containsKey(player.getUdid())) {
 
                         } else {
                             thisIsWinnerGroup = false;
                         }
                     }
                     if (thisIsWinnerGroup) {
 
                         int totalMoney = pot.getMoneyAtIndex(i);
                         int moneyForEveryOne = totalMoney / players.size();
                         log.debug("total money:" + totalMoney);
 
                         for (Player player : players) {
                             player.addMoney(moneyForEveryOne);
 
 
                             if (allWinningUsers.containsKey(player.getUdid())) {
                                 int hisMoney = allWinningUsers.get(player.getUdid());
                                 hisMoney += moneyForEveryOne;
                                 allWinningUsers.put(player.getUdid(), hisMoney);
                             } else {
                                 allWinningUsers.put(player.getUdid(), moneyForEveryOne);
                             }
 
                             sb.append(player.getUdid()).append(",").append(player.getNameOfBestHand()).append(",").append(String.valueOf(player.getGIndexesForOwnCardsUsedInBestFive())).append(",").append(player.getIndexesForUsedCommunityCardsInBestFive(cardsArray)).append(",").append(String.valueOf(moneyForEveryOne)).append(";");
                             playersInThisPot.remove(player.getUdid());
                             playersListInThisPot.add(player);
                         }
 
                         for (String s : playersInThisPot.keySet()) {
                             for (Player player : activePlayers) {
                                 if (player.getUdid().equals(s)) {
                                     sb.append(player.getUdid()).append(",").append(player.getNameOfBestHand()).append(",").append(String.valueOf(player.getGIndexesForOwnCardsUsedInBestFive())).append(",").append(player.getIndexesForUsedCommunityCardsInBestFive(cardsArray)).append(",").append(String.valueOf(0)).append(";");
                                     playersListInThisPot.add(player);
                                     break;
                                 }
                             }
                         }
                         break;
                     }
                 }
 
                 for (Player p : activePlayers) {
                     if (allWinningUsers.containsKey(p.getUdid())) {
                         PlayerDao.updateWinCount(p);
                         PlayerDao.updateMaxWin(p.getUdid(), allWinningUsers.get(p.getUdid()));
                         PlayerDao.cashBack(p, allWinningUsers.get(p.getUdid()));
                         EventDao.insertWinOrLoseEvent(p, gc.getName(), allWinningUsers.get(p.getUdid()), String.valueOf(p.getGIndexesForOwnCardsUsedInBestFive()), true);
                     } else {
                         PlayerDao.updateLoseCount(p);
                         EventDao.insertWinOrLoseEvent(p, gc.getName(), p.getBetThisGame(), String.valueOf(p.getGIndexesForOwnCardsUsedInBestFive()), false);
                     }
                     PlayerDao.updateBestHandOfPlayer(p);
                 }
 
                 //每个边池给客户端足够做动画的时间
 
                 log.debug(sb.toString());
                 NotificationCenter.winorlose(allPlayersInGame, sb.toString());
                 try {
                     Thread.sleep(Duration.seconds(4).inMillis());
                 } catch (InterruptedException e) {
                     log.error(ExceptionUtils.getStackTrace(e));
                 }
 
 
             }
         } else {
             if (results.size() == 1) {
                 Player player1 = results.get(0);
                 log.debug(player1.getName() + " is the only left player ...");
                 StringBuilder sb = new StringBuilder();
                 player1.addMoney(pot.getMoney());
                 PlayerDao.cashBack(player1, pot.getMoney());
                 PlayerDao.updateMaxWin(player1.getUdid(), pot.getMoney());
                 PlayerDao.updateWinCount(player1);
 
                 sb.append(player1.getUdid()).append(",").append("").append(",").append("").append(",").append("").append(",").append(String.valueOf(pot.getMoney())).append(";");
                 NotificationCenter.winorlose(allPlayersInGame, sb.toString());
                 try {
                     Thread.sleep(Duration.seconds(4).inMillis());
                 } catch (InterruptedException e) {
                     log.error(ExceptionUtils.getStackTrace(e));
                 }
             } else {
                 log.debug("no player left ...");
             }
         }
 
         //eject user who's money is zero when game's over.
         List<PlayerDTO> playerDTOs = new ArrayList<>();
         String brokenPlayersUDID = "";
         for (Player aplayer : activePlayers) {
             if (aplayer.getMoneyInGame() <= 0) {
                 log.debug(aplayer.getName() + " is rejected because of empty pocket");
                 if (aplayer.getSeatIndex() != 0) {
                     table.put(aplayer.getSeatIndex(), Config.EMPTY_SEAT);
                 }
 //                NotificationCenter.youAreBroke(aplayer);
                 activePlayers.remove(aplayer);
                 standingPlayers.put(aplayer.getUdid(), aplayer);
                 playerDTOs.add(new PlayerDTO(aplayer, Config.GAMESTATUS_WAITING));
                 brokenPlayersUDID += aplayer.getUdid() + ";";
             }
         }
 
        if (brokenPlayersUDID.length() > 0) {
            NotificationCenter.showBrokenPlayers(allPlayersInGame, brokenPlayersUDID);
        }
 
 
         results.clear();
         gaming = false;
         log.debug("showBrokenPlayers ...");
     }
 
     private void reset() {
         deck.reset();
         deck.shuffle();
 
         moneyOnTable = 0;
         cardsOnTable.clear();
 //        activePlayers.clear();
         allWinningUsers.clear();
         pot = new Pot();
         bet = 0;
 
         for (Player player : allPlayersInGame) {
             player.reset();
         }
         log.debug("Game has been resetted ...");
     }
 
     private void doBettingRound(boolean preflop) {
         try {
             List<Player> playersInThisRound = getRestActivePlayers();
             playersToAct = playersInThisRound.size();
             if (playersToAct > 1) {
                 actorPosition = dealerPosition;
                 bet = 0;
 
                 for (Player player : playersInThisRound) {
                     player.setBetThisRound(0);
                 }
 
                 while (playersToAct > 0 && !stop) {
                     //rotate the actor
 
                     playersInThisRound = getRestActivePlayers();
                     playersToAct--;
                     rotateActor(playersInThisRound);
                     log.debug("playersToAct: " + playersToAct + " id: " + actor.getUdid() + " name: " + actor.getName());
                     log.info(String.format("现在轮到 %s 出牌了", actor.getName()));
 
                     Set<Action> allowedActions = getAllowedActions(actor);
 
                     List<Player> playersToForward = new ArrayList<>();
                     for (Player player : allPlayersInGame) {
                         if (player != actor) {
                             playersToForward.add(player);
                         }
                     }
 
 
                     Action action = null;
 
                     if (preflop) {
                         if (actor.isSmallBlind()) {
                             action = actor.act(allowedActions, bet, moneyOnTable, gc.getBettingDuration(), gc.getInactivityCheckInterval(), 1, gc.getSmallBlindAmount());
                             actor.setSmallBlind(false);
                             playersToAct++;
                             log.debug("small blind playersToAct:" + playersToAct);
                             // 再让小盲跟一次
                         } else if (actor.isBigBlind()) {
                             action = actor.act(allowedActions, bet, moneyOnTable, gc.getBettingDuration(), gc.getInactivityCheckInterval(), 2, gc.getBigBlindAmount());
                             actor.setBigBlind(false);
                         } else {
                             NotificationCenter.otherPlayerStartAction(playersToForward, actor.getUdid());
                             action = actor.act(allowedActions, bet, moneyOnTable, gc.getBettingDuration(), gc.getInactivityCheckInterval(), 0, 0);
                         }
                     } else {
 
                         if (allowedActions.size() == 1 && allowedActions.contains(Action.CONTINUE)) {
                             action = actor.act(allowedActions, bet, moneyOnTable, gc.getBettingDuration(), gc.getInactivityCheckInterval(), 3, 0);
                         } else {
                             NotificationCenter.otherPlayerStartAction(playersToForward, actor.getUdid());
                             action = actor.act(allowedActions, bet, moneyOnTable, gc.getBettingDuration(), gc.getInactivityCheckInterval(), 0, 0);
                         }
 
                     }
 
                     Record record = new Record(actor.getUdid(), action.getVerbType(), actor.getBetThisTime());
                     pot.addRecord(record);
 
                     log.debug(" id: " + actor.getUdid() + " name: " + actor.getName() + " has " + action.getVerb() + " " + actor.getBetThisTime());
 
                     switch (action) {
                         case CHECK:
                             // do nothing
                             break;
                         case CALL:
                             moneyOnTable += actor.getBetThisTime();
                             break;
                         case RAISE:
                             bet = bet >= actor.getBetThisRound() ? bet : actor.getBetThisRound();
                             moneyOnTable += actor.getBetThisTime();
                             playersToAct = activePlayers.size() - 1;
                             break;
                         case FOLD:
                             actor.getHand().makeEmpty();
                             if (activePlayers.contains(actor)) {
                                 this.activePlayers.remove(actor);
                                 this.waitingPlayers.put(actor.getUdid(), actor);
                             } else {
                                 //如果因为站起而Fold，则不把这人加入等待列表了。
                             }
 
                             actorPosition--;
                             if (this.activePlayers.size() == 1) {
                                 log.debug(this.activePlayers.get(0).getName() + " win ...");
                                 log.info(this.activePlayers.get(0).getName() + " 赢得了本局比赛");
                                 playersToAct = 0;
                             }
 
                             EventDao.insertWinOrLoseEvent(actor, gc.getName(), actor.getBetThisGame(), actor.getHand().getGIndexes(), false);
                             PlayerDao.updateLoseCount(actor);
                             break;
                         case SMALL_BLIND:
                             bet = bet >= actor.getBetThisRound() ? bet : actor.getBetThisRound();
                             moneyOnTable += actor.getBetThisTime();
                             break;
                         case BIG_BLIND:
                             bet = bet >= actor.getBetThisRound() ? bet : actor.getBetThisRound();
                             moneyOnTable += actor.getBetThisTime();
                             break;
                         case ALLIN:
                             if (actor.getBetThisTime() > bet) {
                                 playersToAct = activePlayers.size() - 1;
                                 bet = bet >= actor.getBetThisRound() ? bet : actor.getBetThisRound();
                             }
                             moneyOnTable += actor.getBetThisTime();
                             break;
                         case CONTINUE:
                             break;
 
                     }
 
                     //扣钱
 
                     String info = actor.getUdid() + "," + action.getVerb() + ":" + actor.getBetThisTime() + "," + moneyOnTable;
 
                     if (action.getName().equals(Action.SMALL_BLIND.getName())) {
                         NotificationCenter.paySmallBlind(allPlayersInGame, info);
                     } else if (action.getName().equals(Action.BIG_BLIND.getName())) {
                         NotificationCenter.payBigBlind(allPlayersInGame, info);
                     } else {
                         NotificationCenter.forwardAction(allPlayersInGame, info);
                         playersToForward.clear();
                         playersToForward = null;
                     }
 
                     //reset actor's bet
                     actor.setBetThisTime(0);
 
 
                 }
 
                 pot.buildPotList();
             }
         } catch (Exception e) {
             log.error(e);
         }
 
 
     }
 
 
     public Set<Action> getAllowedActions(Player player) {
         Set<Action> actions = new HashSet<Action>();
         if (player.getMoneyInGame() != 0) {
             if (bet == 0) {
                 actions.add(Action.CHECK);
                 actions.add(Action.RAISE);
             } else {
                 if (player.getMoneyInGame() >= bet - player.getBetThisRound()) {
                     actions.add(Action.CALL);
                 }
                 if (player.getMoneyInGame() >= bet * 2) {
                     actions.add(Action.RAISE);
                 }
             }
             actions.add(Action.ALLIN);
             actions.add(Action.FOLD);
         } else {
             actions.add(Action.CONTINUE);
         }
 
         return actions;
     }
 
     private void rotateActor(List<Player> playersInThisRound) {
         if (playersInThisRound.size() > 0) {
             do {
                 actorPosition = (actorPosition + 1) % playersInThisRound.size();
                 actor = playersInThisRound.get(actorPosition);
             } while (!playersInThisRound.contains(actor));
 
         } else {
             throw new IllegalStateException("No active activePlayers left");
         }
     }
 
     private void sayHello() {
         gaming = true;
 
         for (String s : waitingPlayers.keySet()) {
             Player player = waitingPlayers.get(s);
             if (player.isOnline() && player.inRoom(gc.getId())) {
                 if (activePlayers.size() < gc.getMaxPlayersCount()) {
                     activePlayers.add(player);
                 }
             }
         }
 
         for (Player player : activePlayers) {
             if (player.getMoneyInGame() <= 0) {
                 activePlayers.remove(player);
                 standingPlayers.put(player.getUdid(), player);
             }
         }
 
         waitingPlayers.clear();
 
         NotificationCenter.sayHello(allPlayersInGame, DTOUtil.writeValue(DTOUtil.getPlayerDTOList(activePlayers, Config.GAMESTATUS_ACTIVE)));
         log.debug(DTOUtil.writeValue(DTOUtil.getPlayerDTOList(activePlayers, Config.GAMESTATUS_ACTIVE)));
 
     }
 
 
     public String getName() {
         return gc.getName();
     }
 
     public List<Player> getActivePlayers() {
         return activePlayers;
     }
 
     @Override
     public void run() {
         try {
             while (!stop) {
 
                 checkAvailablePlayers();
                 if (activePlayers.size() + waitingPlayers.size() >= 2) {
                     try {
                         log.info("game will start in 3 seconds ...");
                         Thread.sleep(Duration.seconds(1).inMillis());
                         log.info("game will start in 2 seconds ...");
                         Thread.sleep(Duration.seconds(1).inMillis());
                         log.info("game will start in 1 seconds ...");
                         Thread.sleep(Duration.seconds(1).inMillis());
                     } catch (InterruptedException e) {
                         log.error(ExceptionUtils.getStackTrace(e));
                     }
                     if (activePlayers.size() + waitingPlayers.size() >= 2) {
                         start();
                     }
 
                 } else {
 //                    log.debug("activeplayers:" + activePlayers.size());
 //                    log.debug("waitingplayers:" + standingPlayers.size());
                 }
                 try {
                     Thread.sleep(gc.getGameCheckInterval().inMillis());
                 } catch (InterruptedException e) {
                     log.error(ExceptionUtils.getStackTrace(e));
                 }
 
             }
         } catch (Exception e) {
             log.error(ExceptionUtils.getStackTrace(e));
         }
 
     }
 
     public void stopGame() {
         stop = true;
     }
 
     public Map<String, Player> getStandingPlayers() {
         return standingPlayers;
     }
 
     private void checkAvailablePlayers() {
         for (String s : standingPlayers.keySet()) {
             Player player = standingPlayers.get(s);
             if (!player.isOnline()) {
                 standingPlayers.remove(s);
             }
         }
 
 
         for (Player player : activePlayers) {
             if (player.isOnline() && player.inRoom(gc.getId())) {
 
             } else {
                 activePlayers.remove(player);
             }
 
         }
 
         for (String udid : waitingPlayers.keySet()) {
             Player player = waitingPlayers.get(udid);
             if (player.isOnline() && player.inRoom(gc.getId())) {
 
             } else {
                 waitingPlayers.remove(udid);
             }
 
         }
 
     }
 
     public void removePlayer(Player player) {
         boolean activeOrWaitingPlayerLeft = standUp(player);
 
 
         if (!activeOrWaitingPlayerLeft) {
             for (String s : standingPlayers.keySet()) {
                 Player aplayer = standingPlayers.get(s);
                 if (aplayer.getUdid().equals(player.getUdid())) {
                     standingPlayers.remove(s);
                     break;
                 }
             }
         }
 
         player.setRoomId(0);
         PlayerDao.updateRoomId(player);
         player.setSeatIndex(Config.SEAT_INDEX_NOTSITTED);
 
         allPlayersInGame.remove(player);
 
     }
 
 
     private int getNextRandomSeat() {
         int randomChairIndex = 0;
         List<Integer> availableChairs = new ArrayList<>();
         for (Integer i : table.keySet()) {
             if (table.get(i).equals(Config.EMPTY_SEAT)) {
                 availableChairs.add(i);
             }
         }
         randomChairIndex = availableChairs.get(random.nextInt(availableChairs.size()));
         return randomChairIndex;
     }
 
     // 游戏中站起，离开现有座位
     public boolean standUp(Player player) {
 
         boolean result = false;
 
         if (activePlayers.contains(player) || waitingPlayers.containsKey(player.getUdid())) {
             log.debug(player.getName() + " stands up ...");
             if (player.getSeatIndex() != Config.SEAT_INDEX_NOTSITTED) {
                 table.put(player.getSeatIndex(), Config.EMPTY_SEAT);
             }
 
             if (waitingPlayers.containsKey(player.getUdid())) {
                 log.debug(player.getName() + " stands up from waiting players");
                 waitingPlayers.remove(player.getUdid());
             } else if (activePlayers.contains(player)) {
                 activePlayers.remove(player);
                 if (actor == player) {
                     player.setInput("f");
                 } else {
                     // 如果当前是A玩家在思考，B玩家站起，需变化该轮还剩下的步数。
                     if (activePlayers.size() == 1) {
                         log.debug(actor.getName() + " should be stopped now while he's the only player left ...");
                         actor.stopNow();
                     }
                 }
                 log.debug(player.getName() + " stands up from active players");
 
 
             }
 
 
             NotificationCenter.standUp(allPlayersInGame, player.getUdid());
             RoomDao.updateCurrentPlayerCount(gc.getId(), activePlayers.size() + waitingPlayers.size());
             player.setSeatIndex(Config.SEAT_INDEX_NOTSITTED);
             standingPlayers.put(player.getUdid(), player);
 
             result = true;
         }
 
         return result;
 
     }
 
 
     public void dealerSays(String content) {
         log.debug("Dealer in Room " + gc.getId() + " says:[" + content + "]");
         DealerSaysWorker dsw = new DealerSaysWorker(content, allPlayersInGame);
         pool.submit(dsw);
     }
 
     public void chat(Player player, String content) {
         log.debug(player.getName() + " says:[" + content + "]");
         ChatThreadWorker ctw = new ChatThreadWorker(player, content, allPlayersInGame);
         pool.submit(ctw);
     }
 
     public void voiceChat(Player player, String content) {
         log.debug(player.getName() + " says in voice:[" + content + "]");
         VoiceChatThreadWorker ctw = new VoiceChatThreadWorker(player, content, allPlayersInGame);
         pool.submit(ctw);
     }
 
     public void printUserList() {
         List<PlayerDTO> playerDTOs = new ArrayList<>();
         for (Player aplayer : activePlayers) {
             playerDTOs.add(new PlayerDTO(aplayer, Config.GAMESTATUS_ACTIVE));
         }
         for (String s : waitingPlayers.keySet()) {
             Player aplayer = waitingPlayers.get(s);
             playerDTOs.add(new PlayerDTO(aplayer, Config.GAMESTATUS_WAITING));
         }
         log.debug(DTOUtil.writeValue(playerDTOs));
         log.debug(table);
     }
 
     //当部分人allin时，可能有玩家还存活着并且有余力进行下一轮下注，那则需要继续是否还有人有钱可以继续下注。
     public List<Player> getRestActivePlayers() {
         List<Player> players = new ArrayList<>();
         for (int i = 1; i <= gc.getMaxPlayersCount(); i++) {
             String udid = table.get(i);
             if (!udid.equals(Config.EMPTY_SEAT)) {
                 for (Player player : activePlayers) {
                     if (player.getUdid().equals(udid) && player.getMoneyInGame() > 0) {
                         players.add(player);
                         break;
                     }
                 }
             }
         }
 
         return players;
     }
 
     //参考博雅，只有坐下的人能互加好友
     public void forwardAddFriendRequest(Player fromPlayer, Player toPlayer) {
         AddFriendRequestWorker afrw = new AddFriendRequestWorker(waitingPlayers, activePlayers, fromPlayer, toPlayer);
         pool.submit(afrw);
     }
 }
