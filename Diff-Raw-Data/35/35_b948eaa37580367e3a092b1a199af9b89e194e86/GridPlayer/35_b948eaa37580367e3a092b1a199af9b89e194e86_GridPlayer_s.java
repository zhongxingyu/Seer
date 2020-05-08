 /*
  * Copyright (C) 2010 ECOSUR, Andrew Waterman and Max Pimm
  *
  * Licensed under the Academic Free License v. 3.0.
  * http://www.opensource.org/licenses/afl-3.0.php
  */
 
 /**
  * A GridRegistrant contains persistent information about a player playing a 
  * specific game.  
  * 
  * @author awaterma@ecosur.mx
  *
  */
 
 package mx.ecosur.multigame.grid.model;
 
 import javax.persistence.*;
 
 import mx.ecosur.multigame.grid.Color;
 import mx.ecosur.multigame.grid.comparator.PlayerComparator;
 import mx.ecosur.multigame.model.interfaces.GamePlayer;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 
 import java.io.IOException;
 
 @Entity
 public abstract class GridPlayer implements Comparable, GamePlayer {
 
     private static final long serialVersionUID = -1893870933080422147L;
 
     private int id;
 
     private GridRegistrant registrant;
 
     private Color color;
 
     private boolean turn;
 
     public static String getNamedQuery () {
         return "getGamePlayer";
     }
 
     public GridPlayer () {
         super();
 
     }
 
     public GridPlayer (GridRegistrant registrant, Color color) {
         this.registrant = registrant;
         this.color = color;
     }
 
     @Id
     @GeneratedValue
     public int getId() {
         return id;
     }
 
     public void setId(int id) {
         this.id = id;
     }
 
    @OneToOne (cascade=CascadeType.ALL)
     public GridRegistrant getRegistrant() {
         return registrant;
     }
 
     public void setRegistrant (GridRegistrant r) {
         this.registrant = r;
     }
 
     @Enumerated (EnumType.STRING)
     public Color getColor() {
         return color;
     }
 
     public void setColor (Color color) {
         this.color = color;
     }
 
     public boolean isTurn() {
         return turn;
     }
 
     public void setTurn(boolean turn) {
         this.turn = turn;
     }
 
     @Override
     public boolean equals(Object obj) {
         boolean ret = false;
         if (obj instanceof GridPlayer) {
             GridPlayer test = (GridPlayer) obj;
             ret = registrant.getName().equals(test.getRegistrant().getName()) &&
                 registrant.getId() == test.getId();
         }
 
         return ret;
     }
 
     @Override
     public int hashCode() {
         return new HashCodeBuilder().append(id).append(color).append(registrant).append(turn).toHashCode();
     }
 
     @Override
     public String toString() {
         return registrant.getName() + ", " + color.name() + ", turn=" + turn;
     }
 
     @Override
     public int compareTo(Object o) {
         if (o instanceof GridPlayer) {
             GridPlayer b = (GridPlayer) o;
             PlayerComparator c = new PlayerComparator();
             return c.compare(this, b);
         } else {
             return 0;
         }
     }
 }
