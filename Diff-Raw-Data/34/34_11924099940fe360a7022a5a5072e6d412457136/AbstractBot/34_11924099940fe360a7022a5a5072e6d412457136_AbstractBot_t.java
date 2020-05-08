 // $Id$
 
 package me.kukkii.janken.bot;
 
 import me.kukkii.janken.AbstractPlayer;
 import me.kukkii.janken.Hand;
 
 abstract public class AbstractBot extends AbstractPlayer {
 
   public AbstractBot() {
     super();
    id *= (-1);
   }
 
   public AbstractBot(long id, String name) {
     super(id, name);
    id *= (-1);
   }
 
   abstract public int hand() ;
 
   public int hand(int other) {
     return hand();
   }
 
 }
