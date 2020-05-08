 /*
  * Copyright (c) 2004 UNINETT FAS
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation; either version 2 of the License, or (at your option)
  * any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
  * more details.
  *
  * You should have received a copy of the GNU General Public License along with
  * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
  * Place - Suite 330, Boston, MA 02111-1307, USA.
  *
  * $Id$
  */
 
 package no.feide.moria.store;
 
 import java.io.Serializable;
 import java.util.Date;
 
 /**
  * This class represents the tickets used as identificators in Moria. Each
  * ticket has an unique key, a type and an assosiated service.
  *
  * These attributes are used for the validation and authorization of incoming
  * requests.
  *
  * @author Bjrn Ola Smievoll &lt;b.o@smievoll.no&gt;
  * @version $Revision$
  */
 final class MoriaTicket implements Serializable {
 
     /** The unique identifier of this ticket. */
     private final String ticketId;
 
     /** The type of this ticket. */
     private final MoriaTicketType ticketType;
 
     /** The id of the service assosiated with this ticket. */
     private final String servicePrincipal;
 
     /** The time when this ticket expires, stored as seconds since epoch. */
     private final Long expiryTime;
 
     /** The data associated with this ticket. */
     private final MoriaStoreData data;
 
     /**
      * Construct a new ticket with auto-generated ticket id.
      *
      * @param ticketType
      *          the type of ticket
      * @param servicePrincipal
      *          the id of the service this ticket relates to
     * @param timeToLive
     *          the number of seconds this ticket should be considered valid
      * @param data
      *          the data object associated with this ticket. May be null
      */
    MoriaTicket(final MoriaTicketType ticketType, final String servicePrincipal, final Long timeToLive, final MoriaStoreData data) {
        this(MoriaTicket.newId(), ticketType, servicePrincipal, timeToLive, data);
     }
 
     /**
      * Construct a new ticket with the given arguments.
      *
      * @param ticketId
      *            a key identifying this ticket
      * @param ticketType
      *            the type of ticket
      * @param servicePrincipal
      *            the id of the service this ticket relates to
     * @param timeToLive
     *            the number of seconds this ticket should be considered valid
      * @param data
      *          the data object associated with this ticket. May be null
      */
    MoriaTicket(final String ticketId, final MoriaTicketType ticketType, final String servicePrincipal, final Long timeToLive,
            final MoriaStoreData data) {
 
         /* Sanity checks on inputs before assignment. */
         if (ticketId == null || ticketId.equals(""))
             throw new IllegalArgumentException("ticketId cannot be null or an empty string");
         this.ticketId = ticketId;
 
         if (ticketType == null)
             throw new IllegalArgumentException("ticketType cannot be null");
         this.ticketType = ticketType;
 
         /* Undefined servicePrincipal is only allowed for SSO tickets. */
         if (!ticketType.equals(MoriaTicketType.SSO_TICKET) && (servicePrincipal == null || servicePrincipal.equals("")))
             throw new IllegalArgumentException("servicePrincipal cannot be null or empty string");
 
         if (ticketType.equals(MoriaTicketType.SSO_TICKET) && servicePrincipal != null)
             throw new IllegalArgumentException("servicePrincipal must be null when creating a SSO ticket");
         this.servicePrincipal = servicePrincipal;
 
        if (timeToLive == null || timeToLive.longValue() < 0)
            throw new IllegalArgumentException("expiryTime must be a positive integer");
        this.expiryTime = new Long(new Date().getTime() + timeToLive.longValue() * 1000);
 
         /* Verify that data type matches ticket type. */
         if (data != null) {
             if (ticketType.equals(MoriaTicketType.LOGIN_TICKET) || ticketType.equals(MoriaTicketType.SERVICE_TICKET)) {
                 if (!(data instanceof MoriaAuthnAttempt)) {
                     throw new IllegalArgumentException("For login and service tickets data must be MoriaAuthAttempt");
                 }
             } else {
                 if (!(data instanceof CachedUserData)) {
                     throw new IllegalArgumentException("For sso, tg and proxy ticket data must be CachedUserData");
                 }
             }
         }
         /* The data object may be null so we just assign. */
         this.data = data;
     }
 
     /**
      * Get the ticket identificator for a object.
      *
      * @return the key identifying the ticket
      */
     String getTicketId() {
         return ticketId;
     }
 
     /**
      * Get the the value of the ticket type of this ticket.
      *
      * @return the type of ticket
      */
     MoriaTicketType getTicketType() {
         return ticketType;
     }
 
     /**
      * Get the value of the service principal assosiated with this ticket.
      *
      * @return the service principal
      */
     String getServicePrincipal() {
         return servicePrincipal;
     }
 
     /**
      * Checks the tickets expiry time versus the current time.
      *
      * @return true if the ticket has exceeded its time to live
      */
     boolean hasExpired() {
        long now = new Date().getTime();
 
         /*
          * If the expiry time is in the future return false, the ticket has not
          * expired.
          */
         if (expiryTime.longValue() > now)
             return false;
 
         return true;
     }
 
     /**
      * Get the the expiry time for this ticket.
      *
      * @return the expiry time in seconds since epoch
      */
     Long getExpiryTime() {
         return expiryTime;
     }
 
     /**
      * Get the data object of this ticket.
      *
      * @return an instance of MoriaStoreData or null if no data
      *         object is assosiated with this ticket.
      */
     MoriaStoreData getData() {
         return data;
     }
 
     /**
      * Equality is defined on basis of the ticketId value. Same id, same
      * ticket.
      *
      * @see java.lang.Object#equals(java.lang.Object)
      * @param object
      *          the object to compare with
      * @return true if equal
      */
     public boolean equals(final Object object) {
         return (object instanceof MoriaTicket && ((MoriaTicket) object).getTicketId().equals(this.ticketId));
     }
 
     /**
      * The hashcode is the hashcode of the ticketId String.
      *
      * @see java.lang.Object#hashCode()
      */
     public int hashCode() {
         return ticketId.hashCode();
     }
 
     /**
      * Give a sensible output from toString().
      *
      * @return a comma separated string of all the internal values
      */
     public String toString() {
         return "ticketId: " + ticketId + ", ticketType: " + ticketType + ", servicePrincipal: " + servicePrincipal;
     }
 
     /**
      * Creates an new key that can be used as an identificator for a ticket.
      *
      * @return a new unique identificator
      */
     static String newId() {
         return RandomId.newId();
     }
}
