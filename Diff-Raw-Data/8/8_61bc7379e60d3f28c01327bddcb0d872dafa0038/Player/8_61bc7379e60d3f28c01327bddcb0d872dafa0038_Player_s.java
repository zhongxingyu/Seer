 /**
  * <copyright>
  *
  * Copyright (c) 2010 http://www.big.tuwien.ac.at All rights reserved. This
  * program and the accompanying materials are made available under the terms of
  * the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * </copyright>
  */
 package formel0api;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 
 /**
  * Class representing a player playing in a {@link Game}.
  */
 @ManagedBean(name="player")
 @SessionScoped
 public class Player {
 
     /**
      * History of the positions of the player
      */
     private List<Integer> history = new ArrayList<Integer>();
     /**
      * The name of this player
      */
     private String name;
     /**
      * The password of the player
      */
     private String pwd;
     /**
      * The current position of the player's car
      */
     private int position = 0;
 
    private String first = "";
    private String last = "";
    private Date birthdate = new Date();
    private String sex = "";
     
     /** Creates a new instance of Player */
     public Player() {
     }
     
     /**
      * Initializes a {@link Player} with specified
      * <code>name</code> and <code>pwd</code>.
      *
      * @param name to set
      * @param pwd to set
      */
     public Player(String name, String pwd) {
         this.name = name;
         this.pwd = pwd;
         setPosition(0);
     }
     
     public Player(Player p) {
         this.name = p.getName();
         this.pwd = p.getPwd();
         this.first = p.getFirstName();
         this.last = p.getLastName();
         this.birthdate = p.getBirthDate();
         this.sex = p.getSex();
         setPosition(0);
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getName() {
         return name;
     }
 
     public void setPwd(String pwd) {
         this.pwd = pwd;
     }
 
     public String getPwd() {
         return pwd;
     }
 
     /**
      * Sets the actual position of this player's car
      *
      * @param pos actual position of this player's car
      */
     public void setPosition(int pos) {
         this.position = pos;
         history.add(new Integer(pos));
     }
 
     /**
      * Return the actual position of this player's car
      *
      * @return the actual position of this player's car
      */
     public int getPosition() {
         return this.position;
     }
 
     /**
      * Returns the position at time (now - t) (i.e., if t=0 returns the current
      * position, if t=1 returns last position, etc.)
      *
      * @param t position at time (now - t)
      */
     public int getPositionMinusT(int t) {
         int index = history.size() - 1 - t;
         if (index >= 0 && index < history.size()) {
             return history.get(index);
         }
         return -1;
     }
     
     public void setFirstName(String first) {
         this.first = first;
     }
     
     public String getFirstName() {
         return first;
     }
     
     public void setLastName(String last) {
         this.last = last;
     }
     
     public String getLastName() {
         return last;
     }
     
     public void setBirthDate(Date birthdate) {
         this.birthdate = birthdate;
     }
     
     public Date getBirthDate() {
         return birthdate;
     }
     
     public void setSex(String sex) {
         this.sex = sex;
     }
     
     public String getSex() {
         return sex;
     }
 }
