 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package restaurante.Beans;
 
 import java.text.ParseException;
 import java.util.Date;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import restaurante.Utilities.Functions;
 /**
  *
  * @author aluno
  */
 public class Tables {
     
     public enum Status {
         FREE, RESERVED, CLEANING;
         
         public static String[] values = { "Livre", "Reservada", "Em limpeza" };
     }
     
     private int tableNumber;
     private int numberOfChairs;
     private Status status;
     private Date reservation;
     private String reservedFor;
     private int positionX;
     private int positionY;
     private Branches branch;
     private String phoneOfReservedFor;
     
     public Date getReservation() {
         return reservation;
     }
 
     public void setReservation(Date reservation) {
         this.reservation = reservation;
     }
 
     public int getNumberOfChairs() {
         return numberOfChairs;
     }
 
     public void setNumberOfChairs(int numberOfChairs) {
         this.numberOfChairs = numberOfChairs;
     }
 
     public int getTableNumber() {
         return tableNumber;
     }
 
     public void setTableNumber(int tableNumber) {
         this.tableNumber = tableNumber;
     }
 
     public int getPositionX() {
         return positionX;
     }
 
     public void setPositionX(int positionX) {
         this.positionX = positionX;
     }
 
     public int getPositionY() {
         return positionY;
     }
 
     public void setPositionY(int positionY) {
         this.positionY = positionY;
     }
     
     /**
      * @return the status
      */
     public Status getStatus() {
         return status;
     }
 
     /**
      * @param status the status to set
      */
     public void setStatus(Status status) {
         this.status = status;
     }
 
     /**
      * @return the reservedFor
      */
     public String getReservedFor() {
         return reservedFor;
     }
 
     /**
      * @param reservedFor the reservedFor to set
      */
     public void setReservedFor(String reservedTo) {
         this.reservedFor = reservedTo;
     }
 
     /**
      * @return the branch
      */
     public Branches getBranch() {
         return branch;
     }
 
     /**
      * @param branch the branch to set
      */
     public void setBranch(Branches branch) {
         this.branch = branch;
     }
     
     public String getPhoneOfReservedFor() {
         return phoneOfReservedFor;
     }
 
     public void setPhoneOfReservedFor(String phoneOfReservedFor) {
         this.phoneOfReservedFor = phoneOfReservedFor;
     }
 
     @Override
     public String toString() {
        String ret = getClass().getSimpleName() + ": " + getTableNumber() + " - " + Status.values()[getStatus().ordinal()];
         try {
             if(!getReservedFor().isEmpty()) {
                 ret += " para: " + getReservedFor() + " em " + Functions.dateToString(getReservation());
             }
         } catch (ParseException ex) {
             Logger.getLogger(Tables.class.getName()).log(Level.SEVERE, null, ex);
         }
         return ret;
     }
 }
