 /* Copyright (c) 2011 Danish Maritime Authority
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this library.  If not, see <http://www.gnu.org/licenses/>.
  */
 package dk.dma.ais.virtualnet.server;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import net.jcip.annotations.ThreadSafe;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import dk.dma.ais.virtualnet.common.message.ReserveMmsiReplyMessage.ReserveResult;
 
 @ThreadSafe
 public class MmsiBroker {
 
     private static final Logger LOG = LoggerFactory.getLogger(MmsiBroker.class);
 
     /**
      * How long to wait between reservation and activation
      */
     private static final long ACTIVATE_TIME = 60 * 1000; // 1 min
 
     /**
      * Map from mmsi to booking
      */
     private final Map<Integer, Booking> mmsiBookingMap = new HashMap<>();
     /**
      * Map from authToken to mmsi
      */
     private final Map<String, Integer> authTokenMmsiMap = new HashMap<>();
 
     public MmsiBroker() {
     }
 
     /**
      * Reserve a mmsi
      * 
      * @param mmsi
      * @param authToken
      * @return
      */
     public synchronized ReserveResult reserve(int mmsi, String authToken) {
         LOG.info("Reserve mmsi: " + mmsi + " authToken: " + authToken);
        // No reservation needed for 0 and 9xxxx MMSI
        if (nonReservableMmsi(mmsi)) {
            authTokenMmsiMap.put(authToken, mmsi);
            return ReserveResult.MMSI_RESERVED;
        }
         Booking booking = mmsiBookingMap.get(mmsi);
         if (booking == null || !booking.isReserved()) {
             // Determine if target is visible
            // if (!targetTable.exists(mmsi)) {
            // return ReserveResult.MMSI_NOT_FOUND;
            // }
             booking = new Booking();
             mmsiBookingMap.put(mmsi, booking);
             authTokenMmsiMap.put(authToken, mmsi);
             return ReserveResult.MMSI_RESERVED;
         }
         return ReserveResult.MMSI_ALREADY_RESERVED;
     }
 
     /**
      * Activate a reservation
      * 
      * @param authToken
      */
     public synchronized boolean activate(String authToken) {
         LOG.info("Activate mmsi authToken: " + authToken);
         Integer mmsi = authTokenMmsiMap.get(authToken);
         if (mmsi == null) {
             LOG.error("No MMSI for authToken: " + authToken);
             return false;
         }
        if (nonReservableMmsi(mmsi)) {
            return true;
        }
         Booking booking = mmsiBookingMap.get(mmsi);
         if (booking == null) {
             LOG.error("No booking for MMSI: " + mmsi);
             return false;
         }
         if (booking.isActivated()) {
             LOG.error("Booking already activate for MMSI: " + mmsi);
             return false;
         }
         booking.activate();
         return true;
     }
 
     public synchronized void release(String authToken) {
         LOG.info("Release mmsi authToken: " + authToken);
         Integer mmsi = authTokenMmsiMap.get(authToken);
        authTokenMmsiMap.remove(authToken);
         if (mmsi == null) {
             LOG.error("No MMSI for authToken: " + authToken);
             return;
         }
        if (nonReservableMmsi(mmsi)) {
            return;
        }
         Booking booking = mmsiBookingMap.get(mmsi);
         if (booking == null) {
             LOG.error("No booking for MMSI: " + mmsi);
             return;
         }
         mmsiBookingMap.remove(mmsi);
     }
 
    public static boolean nonReservableMmsi(int mmsi) {
        return mmsi == 0 || mmsi >= 900000000;
    }

     private class Booking {
         private final long created;
         private long activated;
 
         public Booking() {
             this.created = System.currentTimeMillis();
         }
 
         public void activate() {
             this.activated = System.currentTimeMillis();
         }
 
         public boolean isActivated() {
             return activated > 0;
         }
 
         public boolean isReserved() {
             if (isActivated()) {
                 return true;
             }
             long reserveAge = created - System.currentTimeMillis();
             return reserveAge < ACTIVATE_TIME;
         }
 
     }
 
 }
