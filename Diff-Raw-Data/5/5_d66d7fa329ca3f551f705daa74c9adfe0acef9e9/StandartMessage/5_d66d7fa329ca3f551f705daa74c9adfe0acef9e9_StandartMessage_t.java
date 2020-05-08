 package com.novel.stdmsg;
 
 import com.novel.odisp.common.Message;
 import java.util.List;
 import java.util.ArrayList;
 import java.io.Serializable;
 
 /**       ODISP.
  * @author  . 
  * @author (C) 2003,  "-"
 * @version $Id: StandartMessage.java,v 1.5 2004/02/13 22:24:10 valeks Exp $
  */
 public class StandartMessage implements Message, Serializable {
   /**    . */
   private static int id = 0;
   /**     . */
   private int myId;
   /**   . */
   private List fields = new ArrayList();
   /** . */
   private String action;
   /**  . */
   private String destination;
   /** . */
   private String origin;
   /**      . */
   private int inReplyTo;
   /**   . */
   private boolean ce = false;
   /**   .
    * @param newAction    
    * @param newDestination  
    * @param newOrigin  
    * @param newInReplyTo      
    */
   public StandartMessage(final String newAction,
 			 final String newDestination,
 			 final String newOrigin,
 			 final int newInReplyTo) {
     action = newAction;
     destination = newDestination;
     inReplyTo = newInReplyTo;
     origin = newOrigin;
     myId = id++;
   }
 
   /**  -. */
   public StandartMessage() {
     myId = id++;
   }
 
   /**      .
    * @param field     
    */
   public final void addField(final Object field) {
     fields.add(field);
   }
 
   /**        .
    * @param field  
    * @return  
    */
   public final Object getField(final int field) {
     return fields.get(field);
   }
 
   /**     .
    * @return 
    */
   public final String getAction() {
     return action;
   }
 
   /**     .
    * @param newAction 
    */
   public final void setAction(final String newAction) {
     action = newAction;
   }
 
   /**   .
    * @return 
    */
   public final String getDestination() { return destination; }
 
   /**   .
    * @param newDest 
    */
   public final void setDestination(final String newDest) {
     destination = newDest;
   }
 
   /**   .
    * @return 
    */
   public final String getOrigin() {
     return origin;
   }
 
   /**    .
    * @param newOrigin    
    */
   public final void setOrigin(final String newOrigin) {
     this.origin = newOrigin;
   }
 
   /**       .
    * @return 
    */
   public final int getReplyTo() {
     return inReplyTo;
   }
 
   /**       .
    * @param newId 
    */
   public final void setReplyTo(final int newId) {
     inReplyTo = newId;
   }
 
   /**     .
    * @return - 
    */
   public final int getFieldsCount() {
     return fields.size();
   }
 
   /**    .
    * @return 
    */
   public final int getId() {
     return myId;
   }
 
   /**    .
    * @param newId 
    */
   public final void setId(final int newId) {
     myId = newId;
   }
 
   /**      .
    * @return   
    */
   public final String toString() {
     return "stdmessage id=" + myId + " replyto=" + inReplyTo
       + " action=" + action + ", destination=" + destination
       + ", origin=" + origin + ", fields.size()=" + fields.size();
   }
 
   /**   .
    * @return  
    */
  public boolean isCorrect() {
     ce = true;
     return true;
   }
 
   /**     .
    * @return  
    */
   protected final List getFields() {
     return fields;
   }
 
   /**   .
    * @param newCE  
    */
   public final void setCE(final boolean newCE) {
     ce = newCE;
   }
 
   /**   .
    * @return  
    */
   public final boolean isCE() {
     return ce;
   }
 }
