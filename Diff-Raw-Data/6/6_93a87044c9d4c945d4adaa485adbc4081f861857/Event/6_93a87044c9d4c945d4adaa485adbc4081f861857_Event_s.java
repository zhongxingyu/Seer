 package models;
 
 import play.db.jpa.*;
 
 import javax.persistence.*;
 import java.math.*;
 import java.text.*;
 import java.util.*;
 
 import static org.apache.commons.lang.StringUtils.*;
 
 @Entity(name = "web_kava")
 public class Event extends GenericModel {
   @Id @Column(name = "page_id")
   public Long id;
   @Column(name = "d_title_est")
   public String title;
   @Column(name = "d_intro_est")
   public String intro;
   @ManyToOne @JoinColumn(name = "event_id")
   public Production production;
 
   @ManyToOne @JoinColumn(name = "place_id")
   public Place place;
 
   public Date time;
 
   @Column(name = "lasttime")
   public boolean lastTime;
 
   public boolean premiere;
 
   @Column(name = "price1")
   public String ticketPrice;
 
   public int getRemainingFanTickets() {
     return EventTickets.getCountForEvent(this);
   }
 
   @Override
   public String toString() {
     return "Event[" +
       "title='" + title + "'" +
       ", production=" + production.title +
       ", place=" + place +
       ", time=" + time +
       "]";
   }
 
   public String toDisplayString() {
     return getNiceTitle() + " " + place.toDisplayString() + " " + new SimpleDateFormat("dd.MM.yyyy HH:mm").format(time);
   }
 
   public String getNiceTitle() {
     return isBlank(title) ? production.getTitle() : title;
   }
 
 
  public Integer getTicketPrice() {
     try {
       return new BigDecimal(ticketPrice).intValue();
     } catch (NumberFormatException e) {
       return null;
     }
   }
 }
