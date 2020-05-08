 // $Id$
 
 package me.kukkii.janken;
 
 import me.kukkii.janken.Hand;
 import me.kukkii.janken.Player;
 
 abstract public class AbstractPlayer implements Player {
 
   protected long id;
   protected String name;
 
   public AbstractPlayer() {
     String s = getClass().getName();
     int n = s.lastIndexOf(".");
     name = s.substring(n+1);
     id = name.hashCode();
   }
 
   public AbstractPlayer(long id, String name) {
     this.id = id;
     this.name = name;
   }
 
   public long getId() {
     return id;
   }
 
   public String getName() {
     return name;
   }
 
   abstract public int hand() ;
 
   public Hand hand2() {
     return Hand.get( hand() );
   }
 
   abstract public int hand(int other) ;
 
   public Hand hand2(Hand other) {
     return Hand.get( hand(other.value()) );
   }
 
  public boolean equals(Object other) {
    if (! (other instanceof Player)) {
      return false;
    }
    Player that = (Player) other;
    // return (id == that.getId());
    return name.equals(that.getName());
  }

  public int hashCode() {
    // return new Long(id).hashCode();
    return name.hashCode();
  }

 }
