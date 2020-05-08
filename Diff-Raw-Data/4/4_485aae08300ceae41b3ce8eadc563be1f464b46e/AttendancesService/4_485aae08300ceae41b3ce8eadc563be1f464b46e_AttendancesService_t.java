 /*
  * Copyright 2012 Danylo Vashchilenko
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package census.business;
 
 import census.business.api.BusinessException;
 import census.business.api.SecurityException;
 import census.business.api.ValidationException;
 import census.business.dto.AttendanceDTO;
 import census.persistence.*;
 import java.math.BigDecimal;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 import javax.persistence.NoResultException;
 import org.joda.time.DateMidnight;
 import org.joda.time.DateTime;
 import org.joda.time.LocalTime;
 
 /**
  *
  * @author Danylo Vashchilenko
  */
 public class AttendancesService extends BusinessService {
 
     /**
      * Checks in a registered client.
      * <p>
      * 
      * <ul>
      * <li> The client's attendances balance has to be more than 0 </li>
      * <li> The expiration date has to be at least tomorrow </li>
      * <li> The key has to have 0 open attendances associated with it </li>
 
      * </ul>
      *
      * @param clientId the client's ID
      * @param keyId the key's ID
      * @return the new attendance's ID
      * @throws NullPointerException if any of the arguments is null
      * @throws BusinessException if current business rules restrict this
      * operation
      * @throws IllegalStateException if the transaction is not active; if no 
      * session is not open
      */
     public Short checkInRegisteredClient(Short clientId, Short keyId)
             throws BusinessException, ValidationException {
         Client client;
         Key key;
 
         assertOpenSessionExists();
         assertTransactionActive();
 
         /*
          * Arguments validation.
          */
         if (clientId == null) {
             throw new NullPointerException("The clientId is null."); //NOI18N
         }
 
         if (keyId == null) {
             throw new NullPointerException("The keyId is null."); //NOI18N
         }
 
         client = (Client) entityManager.find(Client.class, clientId);
         if(client == null) {
             throw new ValidationException(bundle.getString("ClientIDInvalid"));
         }
 
         key = entityManager.find(Key.class, keyId);
         if (key == null) {
             throw new ValidationException(bundle.getString("KeyIDInvalid"));
         }
 
         if (!entityManager.createNamedQuery("Key.findAvailable").getResultList().contains(key)) { //NOI18N
             throw new BusinessException(bundle.getString("KeyNotAvailable"));
         }
 
         /*
          * Client should have attendances.
          */
         if (client.getAttendancesBalance() < 1) {
             throw new BusinessException(bundle.getString("ClientNoAttendancesLeft"));
         }
 
         /*
          * Client's can't be expired.
          */
         if (client.getExpirationDate().compareTo(new Date()) < 0) {
             throw new BusinessException(bundle.getString("ClientSubscriptionExpired"));
         }
 
         client.setAttendancesBalance((short) (client.getAttendancesBalance() - 1));
 
         /*
          * Builds an attendance record.
          */
         Attendance attendance = new Attendance();
         attendance.setClient(client);
         attendance.setKey(key);
         attendance.setDatetimeBegin(new Date());
         attendance.setDatetimeEnd(Attendance.DATETIME_END_UNKNOWN);
         
         /*
          * Finds the client's current subscription.
          */
         ItemSubscription itemSubscription;
         
         List<ItemSubscription> itemSubscriptions = (List<ItemSubscription>)entityManager
                 .createNamedQuery("ItemSubscription.findByClientOrderByDateRecordedDesc") //NOI18N
                 .setParameter("client", client) //NOI18N
                 .getResultList();
         
         if(!itemSubscriptions.isEmpty()) {
             itemSubscription = itemSubscriptions.get(0);
             
             List<TimeSplit> timeSplits = entityManager
                     .createNamedQuery("TimeSplit.findAll") //NOI18N
                     .getResultList();
 
             /*
             * Calculates the qunatity of penalties to apply.
             */
             int penalties = -1;
             LocalTime now = new LocalTime();
             LocalTime begin = now;
 
             for (TimeSplit timeSplit : timeSplits) {
                 LocalTime end = new LocalTime(timeSplit.getTime());
 
                 if(timeSplit.equals(itemSubscription.getTimeSplit())) {
                     penalties = 0;
                 } else if(penalties != -1) {
                     penalties++;
                 } 
 
                 if(now.compareTo(begin) >= 0 && now.compareTo(end) < 0) {
                     break;
                 }
 
                 begin = end;
             }
 
             /*
             * If there are penalties to apply, does it.
             */
             if(penalties > 0) {
                 Short orderId = OrdersService.getInstance().findByClientIdAndDate(clientId, new DateMidnight(), true);
                 Short itemId = ((Property)entityManager
                         .createNamedQuery("Property.findByName") //NOI18N
                         .setParameter("name", "time_range_mismatch_penalty_item_id") //NOI18N
                         .getSingleResult())
                         .getShort();
 
                 try {       
                     for(int i = 0; i < penalties;i++) {
                         OrdersService.getInstance().addPurchase(orderId, itemId, null);
                     }
                 } catch (SecurityException ex) {
                     throw new RuntimeException("Unexpected SecurityException.");
                 }
             }
         }
 
         entityManager.persist(attendance);
         entityManager.flush();
 
         return attendance.getId();
     }
 
     /**
      * Checks in a casual client.
      * <p>
      * 
      * <ul>
      * <li>There should be a subscription having 1 attendance unit, a term of 1
      * day, allowing to open an attendance right now without penalties </li>
      * </ul>
      *
      * @param keyId the key's ID.
      * @return the new attendance's ID.
      * @throws NullPointerException if keyId is null
      * @throws ValidationExceltion if the key's ID is invalid
      * @throws BusinessException if current business rules restrict this
      * operation
      * @throws IllegalStateException if the transaction is not active; if no session
      * is open
      */
     public Short checkInCasualClient(Short keyId)
             throws BusinessException, ValidationException {
         Attendance attendance;
         Key key;
         OrderEntity order;
 
         assertOpenSessionExists();
         assertTransactionActive();
 
         /*
          * Arguments validation.
          */
         if (keyId == null) {
             throw new NullPointerException("The keyId is null."); //NOI18N
         }
 
         key = entityManager.find(Key.class, keyId);
 
         if (key == null) {
             throw new ValidationException(bundle.getString("KeyIDInvalid"));
         }
        
        if (!entityManager.createNamedQuery("Key.findAvailable").getResultList().contains(key)) { //NOI18N
            throw new BusinessException(bundle.getString("KeyNotAvailable"));
        }
 
         /*
          * Build an attendance record.
          */
         attendance = new Attendance();
         attendance.setDatetimeBegin(new Date());
         attendance.setDatetimeEnd(Attendance.DATETIME_END_UNKNOWN);
         attendance.setKey(key);
 
         order = new OrderEntity();
         order.setId(OrdersService.getInstance().getNextId());
         order.setAttendance(attendance);
         order.setDate(attendance.getDatetimeBegin());
         order.setPayment(BigDecimal.ZERO);
 
         ItemSubscription itemSubscription = findValidCasualSubscription();
         if (itemSubscription == null) {
             throw new BusinessException(bundle.getString("AttendanceAnonymousSubscriptionNotAvailable"));
         }
         
         OrderLine orderLine = new OrderLine();
         orderLine.setItem(itemSubscription.getItem());
         orderLine.setOrder(order);
         orderLine.setQuantity((short)1);
         
         List<OrderLine> orderLines = new LinkedList<>();
         orderLines.add(orderLine);
         order.setOrderLines(orderLines);
 
         attendance.setOrder(order);
 
         entityManager.persist(orderLine);
         entityManager.persist(order);
         entityManager.persist(attendance);
         entityManager.flush();
 
         return attendance.getId();
     }
 
     /**
      * Gets an attendance by its ID.
      * <p>
      *
      * @param attendanceId the attendance's ID
      * @return the attendance, or null, if the ID is invalid
      * @throws NullPointerException if attendanceId is null
      * @throws SecurityException if current security rules restrict this
      * operation
      * @throws IllegalStateException if no session is open
      */
     public AttendanceDTO getAttendanceById(Short attendanceId)
             throws IllegalArgumentException, SecurityException {
 
         assertOpenSessionExists();
 
         if (attendanceId == null) {
             throw new NullPointerException("The attendanceId is null."); //NOI18N
         }
 
         Attendance attendance = entityManager.find(Attendance.class, attendanceId);
 
         if (attendance == null) {
             return null;
         }
         
         AttendanceDTO attendanceDTO = wrapAttendance(attendance);
         return attendanceDTO;
     }
 
     /**
      * Finds all attendance that were open on the date.
      * <p>
      * 
      * <ul>
      * <li> The permission level has to be ALL to access attendances open not
      * today </li>
      * </ul>
      *
      * @param date the date
      * @return the list of all attendances open on the date
      * @throws NullPointerException if the date is null
      * @throws SecurityException if current security rules restrict this
      * operation
      * @throws IllegalStateException if no session is open
      */
     public List<AttendanceDTO> findAttendancesByDate(DateMidnight date)
             throws SecurityException {
 
         assertOpenSessionExists();
 
         /*
          * Arguments validation.
          */
         if (date == null) {
             throw new NullPointerException("The date is null."); //NOI18N
         }
 
         if (!date.equals(new DateMidnight()) && !SessionsService.getInstance().getPermissionsLevel().equals(SessionsService.PL_ALL)) {
             throw new SecurityException(bundle.getString("AccessDenied"));
         }
 
         List<Attendance> attendances = entityManager.createNamedQuery("Attendance.findByDatetimeBeginRangeOrderByDateTimeBeginDesc") //NOI18N
                 .setParameter("low", date.toDate()) //NOI18N
                 .setParameter("high", date.plusDays(1).toDate()) //NOI18N
                 .getResultList();
         List<AttendanceDTO> result = new LinkedList<>();
 
         for (Attendance attendance : attendances) {
             result.add(wrapAttendance(attendance));
         }
 
         return result;
     }
 
     /**
      * Finds the client's attendances.
      *
      * @param id the client's ID
      * @return the list of the client's attendances
      * @throws NullPointerException if the id is null
      * @throws ValidationException if the client's ID is invalid
      * @throws IllegalStateException if no session is open
      */
     public List<AttendanceDTO> findAttendancesByClient(Short id)
             throws ValidationException {
 
         assertOpenSessionExists();
 
         if (id == null) {
             throw new NullPointerException("The id is null."); //NOI18N
         }
 
         Client client = entityManager.find(Client.class, id);
 
         if (client == null) {
             throw new ValidationException(bundle.getString("ClientIDInvalid"));
         }
 
         List<Attendance> attendances = entityManager
                 .createNamedQuery("Attendance.findByClientOrderByDateTimeBeginDesc")
                 .setParameter("client", client)
                 .getResultList();
         
         List<AttendanceDTO> result = new LinkedList<>();
 
         for (Attendance attendance : attendances) {
             result.add(wrapAttendance(attendance));
         }
 
         return result;
     }
 
     /**
      * Gets whether the attendance is casual.
      * <p>
      * 
      * <ul> 
      * <li>The attendance is casual, if it does not have a client associated
      * with it</li>
      * </ul>
      *
      * @param attendanceId the attendance's ID
      * @return true, if the attendance is casual; false, otherwise
      * @throws IllegalStateException if no session is open
      * @throws NullPointerException if the attendanceId is null
      * @throws ValidationException if the attendance's ID is invalid
      */
     public Boolean isCasual(Short attendanceId) throws ValidationException {
         assertOpenSessionExists();
 
         if (attendanceId == null) {
             throw new NullPointerException("The attendanceId is null."); //NOI18N
         }
 
         Attendance attendance = entityManager.find(Attendance.class, attendanceId);
 
         if (attendance == null) {
             throw new ValidationException(bundle.getString("AttendanceIDInvalid"));
         }
 
         return attendance.getClient() == null;
     }
 
     /**
      * Checks out a client.
      * <p>
      * 
      * <ul>
      * <li> The attendance has to be open. </li>
      * <li> If the attendance is anonymous, it has to have full payment 
      * recorded in the associated order. </li>
      * </ul>
      *
      * @param attendanceId the attendance's ID
      * @throws NullPointerException if the attendanceId is null
      * @throws ValidationException if the attendance's ID is invalid
      * @throws BusinessException if current business rules restrict this
      * @throws IllegalStateException if the transaction or the session is not
      * active operation
      */
     public void checkOut(Short attendanceId)
             throws BusinessException, ValidationException {
 
         assertOpenSessionExists();
         assertTransactionActive();
 
         if (attendanceId == null) {
             throw new NullPointerException("The attendanceId is null."); //NOI18N
         }
         Attendance attendance = entityManager.find(Attendance.class, attendanceId);
 
         if (attendance == null) {
             throw new ValidationException(bundle.getString("AttendanceIDInvalid"));
         }
 
         if (!attendance.getDatetimeEnd().equals(Attendance.DATETIME_END_UNKNOWN)) {
             throw new BusinessException(bundle.getString("AttendanceAlreadyClosed"));
         }
 
         OrderEntity order = attendance.getOrder();
 
         /*
          * If there is an order associeated with the attendance, the attendance
          * is anonymous, and, therefore, the order should have a full payment.
          */
         if (order != null) {
             if (order.getTotal().compareTo(order.getPayment()) != 0) {
                 throw new BusinessException(bundle.getString("ExactPaymentRequiredToCloseAnonymousAttendance"));
             }
         }
         
         attendance.setDatetimeEnd(new Date());
         
         entityManager.flush();
     }
 
     /**
      * Finds an open attendance with the key provided.
      *
      * @param keyId the key's ID
      * @return the attendance's ID
      * @throws NullPointerException if they keyId is null
      * @throws ValidationException if the key's ID is invalid
      * @throws BusinessException there isn't any open attendances with the key
      * provided
      * @throws IllegalStateException if no session is open
      */
     public Short findOpenAttendanceByKey(Short keyId) throws ValidationException, BusinessException {
 
         assertOpenSessionExists();
 
         /*
          * Arguments validation.
          */
         if (keyId == null) {
             throw new NullPointerException("The keyId is null."); // NOI18N
         }
 
         Key key = entityManager.find(Key.class, keyId);
 
         if (key == null) {
             throw new ValidationException(bundle.getString("KeyIDInvalid"));
         }
 
         Attendance attendance;
         try {
             attendance = (Attendance) entityManager.createNamedQuery("Attendance.findOpenByKey") //NOI18N
                     .setParameter("key", key) //NOI18N
                     .getSingleResult();
         } catch (NoResultException ex) {
             throw new BusinessException(bundle.getString("NoOpenAttendanceWithKey"));
         }
 
         return attendance.getId();
     }
     
     /**
      * Builds an attendance DTO from an attendance entity.
      * 
      * @param attendance the entity to build DTO from
      * @return the DTO
      */
     private AttendanceDTO wrapAttendance(Attendance attendance) {
         AttendanceDTO attendanceDTO = new AttendanceDTO();
         attendanceDTO.setId(attendance.getId());
         attendanceDTO.setDateTimeBegin(new DateTime(attendance.getDatetimeBegin()));
         attendanceDTO.setClientId(attendance.getClient() == null ? null : attendance.getClient().getId());
         attendanceDTO.setClientFullName(attendance.getClient() == null ? null : attendance.getClient().getFullName());
         attendanceDTO.setKeyId(attendance.getKey().getId());
         attendanceDTO.setKeyTitle(attendance.getKey().getTitle());
         attendanceDTO.setDateTimeEnd(attendance.getDatetimeEnd().equals(Attendance.DATETIME_END_UNKNOWN)
                 ? null
                 : new DateTime(attendance.getDatetimeEnd()));
 
         return attendanceDTO;
     }
 
     /**
      * Finds the current time split.
      * 
      * @return the current time split or null, if none is found
      */
     private TimeSplit findCurrentTimeSplit() {
         List<TimeSplit> timeSplits = entityManager.createNamedQuery("TimeSplit.findAll") //NOI18N
                 .getResultList();
         
         LocalTime now = new LocalTime();
         LocalTime begin = now;
         
         for (TimeSplit timeSplit : timeSplits) {
             LocalTime end = new LocalTime(timeSplit.getTime());
 
             if (now.compareTo(begin) >= 0 && now.compareTo(end) < 0) {
                 return timeSplit;
             }
             
             begin = end;
         }
         
         return null;
     }
 
     /**
      * Finds an anonymous subscription appropriate for the current moment.
      *
      * @return the subscription, or null if none is found
      */
     private ItemSubscription findValidCasualSubscription() {
         TimeSplit currentTimeSplit = findCurrentTimeSplit();
 
         /*
          * No subscription can be valid now, if the current time split is null.
          */
         if (currentTimeSplit == null) {
             return null;
         }
 
         ItemSubscription itemSubscription;
         try {
             itemSubscription = (ItemSubscription) entityManager.createNamedQuery("ItemSubscription.findCasualByTimeSplit") //NOI18N
                     .setParameter("timeSplit", currentTimeSplit) //NOI18N
                     .getSingleResult();
         } catch (NoResultException ex) {
             itemSubscription = null;
         }
         return itemSubscription;
     }
     
     /**
      * Singleton instance.
      */
     private static AttendancesService instance;
 
     /**
      * Gets an instance of this class.
      *
      * @return an instance of this class
      */
     public static AttendancesService getInstance() {
         if (instance == null) {
             instance = new AttendancesService();
         }
         return instance;
     }
 }
