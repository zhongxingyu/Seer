 package com.binout.soccer5.entity;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import javax.persistence.*;
 import javax.xml.bind.annotation.XmlRootElement;
 
 import org.joda.time.DateTime;
 
 @Entity
 @Table(name="RMATCH")
 @NamedQueries({
     @NamedQuery(name = Match.FIND_ALL,
     query = "SELECT m FROM Match m order by m.date desc"),
     @NamedQuery(name = Match.FIND_BY_DATE,
     query = "SELECT m FROM Match m where m.date=:date"),
     @NamedQuery(name = Match.FIND_NEXT_MATCHES,
     query = "SELECT m "
         + "FROM Match m "
         + "where m.date >= :today order by m.date asc")
 })
 @XmlRootElement
 public class Match {
     
     public final static String FIND_ALL = "match.findAll";
     public final static String FIND_BY_DATE = "match.findByDate";
     public final static String FIND_NEXT_MATCHES = "match.findNextMatches";
     public static final int MAX_PLAYERS = 10;
 
     @Id
     @GeneratedValue
     private Long id;
     @Temporal(TemporalType.TIMESTAMP)
     private Date date;
     
     @ManyToMany(cascade=CascadeType.DETACH, fetch= FetchType.EAGER)
     private List<Player> players;
     
     @ElementCollection(fetch= FetchType.EAGER)
     private Set<String> guests;    
     
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
     
     public Date getDate() {
         return date;
     }
 
     public String getDateStr() {
         return new SimpleDateFormat().format(getDate());
     }
 
     public void setDate(Date date) {
         this.date = date;
     }
     
     public Date getEndDate() {
         DateTime dt = new DateTime(date);
         dt.plusHours(2);
         return dt.toDate();
     }
 
     public String getEndDateStr() {
         return new SimpleDateFormat().format(getEndDate());
     }
 
     public List<Player> getPlayers() {
         return players;
     }
 
     public void addPlayer(Player p) {
         if (players==null) {
             players = new ArrayList<Player>();
         }
         players.add(p);
     }
     
     public void removePlayer(Player p) {
         if (players!=null) {
             players.remove(p);
         } 
     }
     
     public void setPlayers(List<Player> players) {
         this.players = players;
     }
 
     public int getNbPlayers() {
         return players == null ? 0 : players.size();
     }
     
     public int getNbPlayersAndGuests() {
         return getNbPlayers() + getNbGuests();
     }
     
     public int getMissingPlayers() {
         return MAX_PLAYERS - getNbPlayersAndGuests();
     }
     
     public String[] getGuests() {
         return guests.toArray(new String[0]);
     }
 
      public void addGuest(String p) {
         if (guests==null) {
             guests = new HashSet<String>();
         }
         guests.add(p);
     }
     
     public void removeGuest(String p) {
         if (guests!=null) {
             guests.remove(p);
         } 
     }
     
     public void setGuests(Set<String> guests) {
         this.guests = guests;
     }
     
     public int getNbGuests() {
         return guests == null ? 0 : guests.size();
     }
     
     public boolean isFull() {
         return MAX_PLAYERS == (getNbGuests() + getNbPlayers());
     }
     
     public boolean isOpen() {
         return !isFull();
     }
     
 }
