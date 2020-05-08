 package com.avapir.roguelike.core;
 
 import com.avapir.roguelike.core.gui.AbstractGamePanel;
 
 import java.awt.*;
 import java.io.Serializable;
 import java.util.LinkedList;
 
 public class Log implements Serializable, Drawable {
     private static final Font               logFont            = new Font("Times New Roman", Font.PLAIN, 15);
     private static final long               serialVersionUID   = 1L;
     /**
      * {@link java.util.Formatter}-string for logging in {@link #g(String)}
      */
     private static final String             FMT_GAME           = "[%d:%d] %s";
     /**
      * {@link java.util.Formatter}-string for logging in {@link #g(String)}
      */
     private static final String             FMT_GAME_FORMATTED = "[%d:%d] ";
     private static final Log                instance           = new Log();
     /**
      * How much records will remain after end of turn
      */
     private static       int                REMAIN_RECORDS     = 15;
     /**
      * Storage of records
      */
     private final        LinkedList<String> loggedList         = new LinkedList<>();
     /**
      * How many records were made ​​from the beginning of turn
      */
     private transient int perTurn;
     /**
      * Stores number of turn of last send/received record
      */
     private int turn = -1;
 
     private Log() {
     }
 
     public static Log getInstance() {
         return instance;
     }
 
     public static void g(final String s) {
         Log.getInstance().game(s);
     }
 
     public static void g(final String s, final Object... params) {
         Log.getInstance().game(s, params);
     }
 
     /**
      * Sends default record for game
      *
      * @param s user data
      */
     public void game(final String s) {
         String formatted = String.format(FMT_GAME, perTurn, getTurnAndCheck(), s);
         loggedList.add(formatted);
         System.out.println(formatted);
         removePreviousTurnRecord();
     }
 
     /**
      * Sends default record about game
      *
      * @param s formatting data string
      * @param s params data to insert
      */
     public void game(final String s, final Object... params) {
         game(String.format(s, params));
     }
 
     /**
     * Increments amount of records which was added in the current turn and removes records from another turns if log
     * is full
      */
     private void removePreviousTurnRecord() {
         perTurn++;
        System.out.format("%s > %s && %s <= %s", loggedList.size(), REMAIN_RECORDS, perTurn, REMAIN_RECORDS);
        if (loggedList.size() > REMAIN_RECORDS || perTurn > REMAIN_RECORDS) {
             loggedList.poll();
         }
     }
 
     /**
      * Retrieves number of current game turn and invokes {@link #refresh()} if it's not equal to {@link Log#turn}
      *
      * @return {@link GameStateManager#getTurnCounter()}
      */
     private int getTurnAndCheck() {
         int currentTurn = GameStateManager.getInstance().getTurnCounter();
         if (turn != currentTurn) {
             refresh();
             turn = currentTurn;
         }
         return currentTurn;
     }
 
     /**
      * Removes extra records after end of turn.
      */
     private void refresh() {
         perTurn = 0;
         int extra = loggedList.size() - REMAIN_RECORDS;
         for (int i = 0; i < extra; i++) {
             loggedList.poll();
         }
     }
 
     /**
      * Sets new value to {@link Log#REMAIN_RECORDS}, which must be more than zero
      *
      * @param count new value
      */
     public void resetRemainRecordsAmount(int count) {
         if (count < 0) {
             throw new IllegalArgumentException("Amount of records must be greater than zero");
         }
         REMAIN_RECORDS = count;
     }
 
     /**
      * @return the number of elements in the log
      */
     public int getSize() {
         return loggedList.size();
     }
 
     /**
      * @param i index of element to return
      *
      * @return the element at the specified position
      */
     public String get(int i) {
         return loggedList.get(i);
     }
 
     @Override
     public void draw(AbstractGamePanel panel, Graphics2D g2, int x, int y) {
         final Point offset = new Point(x, y);
         g2.setFont(logFont);
         g2.setColor(Color.white);
         for (int i = 0; i < getSize(); i++) {
             g2.drawString(get(i), offset.x, offset.y + i * logFont.getSize() + 3);
         }
     }
 }
