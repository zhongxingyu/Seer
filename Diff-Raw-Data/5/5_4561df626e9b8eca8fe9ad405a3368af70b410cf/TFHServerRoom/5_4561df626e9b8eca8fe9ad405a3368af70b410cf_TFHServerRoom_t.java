 package com.weigreen.poler;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ListIterator;
 
 /**
  * Created by wind on 2013/6/7.
  */
 public class TFHServerRoom
 {
     private short pass = 0; // pass times
 
     // godCard
     private short callPlayerNumber; // the player number which call
     private short godCardSuit = 0;
     private short heap = 0;
 
     // play
     private short part = 0;
     private short initialCardSuit; // the first suit per stage
     private short[] stageCardIds = new short[4];
     private short initialPlayerNumber;
     private short easternHeap = 0;
     private short northernHeap = 0;
 
 
     // unfinished!!!!!!!!!!!
     public TFHServerRoom()
     {
 
     }
/*
     public void haveNewData(TFHBridgeMain main)
     {
         short command = main.getCommand();
 
         if(command == TFHComm.GOD_CARD_DATA)
         {
             TFHBridgeDataGodCard godCardData = (TFHBridgeDataGodCard) main.getData();
 
             short newCallPlayerNumber = godCardData.getPlayerNumber();
             short newGodCardSuit = godCardData.getGodCardSuit();
             short newHeap = godCardData.getHeap();
 
             if(newHeap == 0)
             {
                 pass++;
 
                 if(pass >= 3)
                 {
                     initialPlayerNumber = callPlayerNumber;
 
                     TFHBridgeDataGodCard newGodCardData = new TFHBridgeDataGodCard("OVER", callPlayerNumber, godCardSuit, heap);
                     TFHBridgeMain newMain = new TFHBridgeMain(TFHComm.GOD_CARD_DATA, newGodCardData);
 
                     server.sendToAll(newMain);
                 }
                 else
                 {
                     TFHBridgeDataGodCard newGodCardData = new TFHBridgeDataGodCard("KEEP", newCallPlayerNumber, newGodCardSuit, newHeap);
                     TFHBridgeMain newMain = new TFHBridgeMain(TFHComm.GOD_CARD_DATA, newGodCardData);
 
                     server.sendToAll(newMain);
                 }
             }
             else
             {
                 pass = 0;
 
                 callPlayerNumber = newCallPlayerNumber;
                 godCardSuit = newGodCardSuit;
                 heap = newHeap;
 
                 TFHBridgeDataGodCard newGodCardData = new TFHBridgeDataGodCard("KEEP", callPlayerNumber, godCardSuit, heap);
                 TFHBridgeMain newMain = new TFHBridgeMain(TFHComm.GOD_CARD_DATA, newGodCardData);
 
                 server.sendToAll(newMain);
             }
         }
 
 
         if(command == TFHComm.PLAYER_DATA)
         {
             TFHBridgeDataPlayer playerData = (TFHBridgeDataPlayer) main.getData();
 
             TFHBridgeDataRoom roomData = analyze(playerData);
             TFHBridgeMain newMain = new TFHBridgeMain(TFHComm.ROOM_DATA, roomData);
 
             server.sendToAll(newMain);
         }
 
         if(command == TFHComm.CARD_DATA)
         {
             TFHBridgeDataCard cardData = new TFHBridgeDataCard(Functions.dealCard());
             TFHBridgeMain newMain = new TFHBridgeMain(TFHComm.CARD_DATA, cardData);
 
             server.sendToAll(newMain);
         }
     }
*/
 
     private TFHBridgeDataRoom analyze(TFHBridgeDataPlayer playerData)
     {
         short playerNumber = playerData.getPlayerNumber();
         short cardId = playerData.getCardId();
 
         if(++part == 1)
         {
             initialCardSuit = (short)(cardId / 100);
             stageCardIds[playerNumber] = cardId;
 
             TFHBridgeDataRoom roomData = new TFHBridgeDataRoom("KEEP", initialPlayerNumber, (short) ((initialPlayerNumber + part) % 4), cardId, easternHeap, northernHeap);
             return roomData;
         }
         else if(part < 4)
         {
             stageCardIds[playerNumber] = cardId;
 
             TFHBridgeDataRoom roomData = new TFHBridgeDataRoom("KEEP", initialPlayerNumber, (short) ((initialPlayerNumber + part) % 4), cardId, easternHeap, northernHeap);
             return roomData;
         }
         else
         {
             part = 0;
 
             stageCardIds[playerNumber] = cardId;
 
             short winPlayerId = getWinPlayerNumber(stageCardIds);
 
             if(winPlayerId == 0 || winPlayerId == 2)
             {
                 easternHeap++;
             }
             else
             {
                 northernHeap++;
             }
 
             TFHBridgeDataRoom roomData = new TFHBridgeDataRoom("START", winPlayerId, winPlayerId, cardId, easternHeap, northernHeap);
 
             return roomData;
         }
     }
 
 
     private short getWinPlayerNumber(short[] stageCardIds)
     {
         short[] cardSuit = new short[4];
         short[] cardNumber = new short[4];
 
         LinkedList<Short> godCardPlayerList = new LinkedList<Short>();
         LinkedList<Short> normalCardPlayerList = new LinkedList<Short>();
 
         for(int i = 0; i < 4; i++)
         {
             cardSuit[i] = (short)(stageCardIds[i] / 100);
             cardNumber[i] = (short)(stageCardIds[i] % 100);
 
             if(cardSuit[i] == godCardSuit)
             {
                 godCardPlayerList.add((short)i);
             }
             else if(cardSuit[i] == initialCardSuit)
             {
                 normalCardPlayerList.add((short)i);
             }
         }
 
         if(!godCardPlayerList.isEmpty())
         {
             if(godCardPlayerList.size() > 1)
             {
                 ListIterator<Short> iterator = godCardPlayerList.listIterator();
 
                 List<Short> godCardNumbers = new ArrayList<Short>();
 
                 while(iterator.hasNext())
                 {
                     godCardNumbers.add(cardNumber[iterator.next()]);
                 }
 
                 short winCardId = (short)(godCardSuit * 100 + Collections.max(godCardNumbers));
 
                 for(int i = 0; i < 4; i++)
                 {
                     if(stageCardIds[i] == winCardId)
                     {
                         return (short)i;
                     }
                 }
             }
             else
             {
                 return godCardPlayerList.get(0);
             }
         }
         else
         {
             if(normalCardPlayerList.size() > 1)
             {
                 ListIterator<Short> iterator = normalCardPlayerList.listIterator();
 
                 List<Short> normalCardNumbers = new ArrayList<Short>();
 
                 while(iterator.hasNext())
                 {
                     normalCardNumbers.add(cardNumber[iterator.next()]);
                 }
 
                 short winCardId = (short)(initialCardSuit * 100 + Collections.max(normalCardNumbers));
 
                 for(int i = 0; i < 4; i++)
                 {
                     if(stageCardIds[i] == winCardId)
                     {
                         return (short)i;
                     }
                 }
             }
             else
             {
                 return normalCardPlayerList.get(0);
             }
         }
 
         return -1; // error happened
     }
 }
