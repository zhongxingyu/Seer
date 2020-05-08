 package com.epam.cdp.oleshchuk.cinema.model;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 public class Ticket implements Comparable<Ticket> {
 
     private Long id;
     private String title;
     private Date date;
     private TicketCategory ticketCategory;
     private Integer place;
 
     public Ticket() {
         super();
     }
 
     public Ticket(Long id, String title, Date date, TicketCategory ticketCategory, Integer place) {
         this.id = id;
         this.title = title;
         this.date = date;
         this.ticketCategory = ticketCategory;
         this.place = place;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public void setDate(Date date) {
         this.date = date;
     }
 
     public void setTicketCategory(TicketCategory ticketCategory) {
         this.ticketCategory = ticketCategory;
     }
 
     public void setPlace(Integer place) {
         this.place = place;
     }
 
     public Long getId() {
         return id;
     }
 
     public String getTitle() {
         return title;
     }
 
     public Date getDate() {
         return date;
     }
 
     public TicketCategory getTicketCategory() {
         return ticketCategory;
     }
 
     public Integer getPlace() {
         return place;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof Ticket)) return false;
         Ticket ticket = (Ticket) o;
         if (!id.equals(ticket.id)) return false;
         return true;
     }
 
     @Override
     public int hashCode() {
         return id.hashCode();
     }
 
     @Override
     public int compareTo(Ticket o) {
         int result = (int) (this.getDate().getTime() - o.getDate().getTime());
         if (result == 0) {
             result = (this.getTitle().compareTo(o.getTitle()));
             if (result == 0) {
                 result = (this.getTicketCategory().getPriority() - o.getTicketCategory().getPriority());
                 if (result == 0) {
                     result = this.getPlace() - o.getPlace();
                 }
             }
         }
         return result;
     }
 
     @Override
     public String toString() {
         return "Ticket{" +
                 "title='" + title + '\'' +
                 ", date=" + new SimpleDateFormat("dd/MM/yy").format(date) +
                 ", ticketCategory=" + ticketCategory +
                 ", place=" + place +
                 '}';
     }
 
 
 }
